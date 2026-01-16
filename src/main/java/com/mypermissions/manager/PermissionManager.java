package com.mypermissions.manager;

import com.mypermissions.Main;
import com.mypermissions.config.MyPermissionsConfig;
import com.mypermissions.config.MyPermissionsConfig.GroupData;
import com.mypermissions.config.MyPermissionsConfig.UserData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe permission engine with support for:
 * - Group inheritance (recursive)
 * - Wildcards (* and plugin.*)
 * - Negated permissions (-permission.node)
 * - Infinite loop prevention
 */
public class PermissionManager {
    
    // Permission cache for better performance
    private final Map<String, Boolean> permissionCache = new ConcurrentHashMap<>();

    /**
     * Checks if a user has a specific permission
     * 
     * @param uuid User's UUID (as String)
     * @param node Permission node (e.g., "mypermissions.admin")
     * @return true if the user has the permission, false otherwise
     */
    public boolean hasPermission(String uuid, String node) {
        if (uuid == null || node == null) {
            return false;
        }

        // Check cache first for performance
        String cacheKey = uuid + ":" + node;
        Boolean cached = permissionCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        MyPermissionsConfig config = Main.getConfig();
        UserData userData = config.getUsers().get(uuid);

        // Set to prevent infinite loops in inheritance chain
        Set<String> visitedGroups = new HashSet<>();
        
        boolean result;

        if (userData != null) {
            // 1. Check user-specific permissions first (highest priority)
            Boolean userPermission = checkPermissionList(userData.getPermissions(), node);
            if (userPermission != null) {
                result = userPermission;
                permissionCache.put(cacheKey, result);
                return result;
            }

            // 2. Check permissions from user's groups
            for (String groupName : userData.getGroups()) {
                Boolean groupPermission = checkGroupPermission(groupName, node, visitedGroups, config);
                if (groupPermission != null) {
                    result = groupPermission;
                    permissionCache.put(cacheKey, result);
                    return result;
                }
            }
        }

        // 3. Fall back to default group
        String defaultGroup = config.getDefaultGroup();
        Boolean defaultPermission = checkGroupPermission(defaultGroup, node, visitedGroups, config);
        
        result = defaultPermission != null && defaultPermission;
        permissionCache.put(cacheKey, result);
        return result;
    }

    /**
     * Checks permission in a specific group (with recursive inheritance)
     * 
     * @param groupName Group name
     * @param node Permission node
     * @param visited Set of already visited groups (prevents loops)
     * @param config Current configuration
     * @return true/false if permission found, null if not found
     */
    private Boolean checkGroupPermission(String groupName, String node, Set<String> visited, MyPermissionsConfig config) {
        // Prevent infinite loops in inheritance chain
        if (visited.contains(groupName)) {
            return null;
        }

        visited.add(groupName);

        GroupData group = config.getGroups().get(groupName);
        if (group == null) {
            return null;
        }

        // 1. Check permissions from the group itself
        Boolean groupPermission = checkPermissionList(group.getPermissions(), node);
        if (groupPermission != null) {
            return groupPermission;
        }

        // 2. Check permissions from parent groups (recursive inheritance)
        for (String parentGroup : group.getParents()) {
            Boolean parentPermission = checkGroupPermission(parentGroup, node, visited, config);
            if (parentPermission != null) {
                return parentPermission;
            }
        }

        return null;
    }

    /**
     * Checks if a permission list contains the requested node
     * Supports wildcards and negated permissions
     * 
     * @param permissions Permission list
     * @param node Permission node to search for
     * @return true if allowed, false if denied, null if not found
     */
    private Boolean checkPermissionList(List<String> permissions, String node) {
        if (permissions == null || permissions.isEmpty()) {
            return null;
        }

        // Check exact negated permission first (highest priority)
        if (permissions.contains("-" + node)) {
            return false;
        }

        // Check exact permission match
        if (permissions.contains(node)) {
            return true;
        }

        // Check full wildcard permission
        if (permissions.contains("*")) {
            return true;
        }

        for (String permission : permissions) {
            boolean isNegation = permission.startsWith("-");
            String permNode = isNegation ? permission.substring(1) : permission;

            // Check partial wildcard (e.g., "mypermissions.*" covers "mypermissions.admin")
            if (permNode.endsWith(".*")) {
                String prefix = permNode.substring(0, permNode.length() - 2);
                if (node.startsWith(prefix + ".") || node.equals(prefix)) {
                    return !isNegation;
                }
            }

            // Check wildcard in the middle (e.g., "mypermissions.*.admin")
            if (permNode.contains("*")) {
                String regex = permNode.replace(".", "\\.").replace("*", ".*");
                if (node.matches(regex)) {
                    return !isNegation;
                }
            }
        }

        return null;
    }

    /**
     * Adds a user to a group
     * 
     * @param uuid User's UUID
     * @param groupName Group name
     * @return true if successfully added
     */
    public boolean addUserToGroup(String uuid, String groupName) {
        MyPermissionsConfig config = Main.getConfig();
        
        // Check if the group exists
        if (!config.getGroups().containsKey(groupName)) {
            return false;
        }

        // Get or create UserData
        UserData userData = config.getUsers().computeIfAbsent(uuid, k -> new UserData());
        
        // Add group if not already present
        if (!userData.getGroups().contains(groupName)) {
            userData.getGroups().add(groupName);
            clearCache();
            Main.getConfigManager().save();
            return true;
        }

        return false;
    }

    /**
     * Remove um usuário de um grupo
     * 
     * @param uuid UUID do usuário
     * @param groupName Nome do grupo
     * @return true se removido com sucesso
     */
    public boolean removeUserFromGroup(String uuid, String groupName) {
        MyPermissionsConfig config = Main.getConfig();
        UserData userData = config.getUsers().get(uuid);

        if (userData != null && userData.getGroups().remove(groupName)) {
            clearCache();
            Main.getConfigManager().save();
            return true;
        }

        return false;
    }

    /**
     * Adiciona uma permissão específica a um usuário
     * 
     * @param uuid UUID do usuário
     * @param permission Permissão a adicionar
     */
    public void addUserPermission(String uuid, String permission) {
        MyPermissionsConfig config = Main.getConfig();
        UserData userData = config.getUsers().computeIfAbsent(uuid, k -> new UserData());
        
        if (!userData.getPermissions().contains(permission)) {
            userData.getPermissions().add(permission);
            clearCache();
            Main.getConfigManager().save();
        }
    }

    /**
     * Remove uma permissão específica de um usuário
     * 
     * @param uuid UUID do usuário
     * @param permission Permissão a remover
     */
    public void removeUserPermission(String uuid, String permission) {
        MyPermissionsConfig config = Main.getConfig();
        UserData userData = config.getUsers().get(uuid);

        if (userData != null && userData.getPermissions().remove(permission)) {
            clearCache();
            Main.getConfigManager().save();
        }
    }

    /**
     * Gets all groups of a user (including inheritance)
     * 
     * @param uuid User's UUID
     * @return Set with all groups (direct and inherited)
     */
    public Set<String> getUserGroups(String uuid) {
        Set<String> allGroups = new LinkedHashSet<>();
        MyPermissionsConfig config = Main.getConfig();
        UserData userData = config.getUsers().get(uuid);

        if (userData != null) {
            for (String groupName : userData.getGroups()) {
                collectGroupHierarchy(groupName, allGroups, new HashSet<>(), config);
            }
        } else {
            // Include default group
            collectGroupHierarchy(config.getDefaultGroup(), allGroups, new HashSet<>(), config);
        }

        return allGroups;
    }

    /**
     * Recursively collects all groups in the hierarchy
     */
    private void collectGroupHierarchy(String groupName, Set<String> result, Set<String> visited, MyPermissionsConfig config) {
        if (visited.contains(groupName)) {
            return;
        }

        visited.add(groupName);
        result.add(groupName);

        GroupData group = config.getGroups().get(groupName);
        if (group != null) {
            for (String parent : group.getParents()) {
                collectGroupHierarchy(parent, result, visited, config);
            }
        }
    }

    /**
     * Clears the permission cache
     */
    public void clearCache() {
        permissionCache.clear();
    }

    /**
     * Clears the permission cache for a specific user
     */
    public void clearCache(UUID uuid) {
        if (uuid == null) return;
        permissionCache.entrySet().removeIf(entry -> entry.getKey().startsWith(uuid.toString() + ":"));
    }

    /**
     * Clears all permission cache (alias for clearCache)
     */
    public void clearAllCache() {
        permissionCache.clear();
    }

    /**
     * Gets the user's prefix based on the highest priority group
     * 
     * @param uuid User's UUID
     * @return User's prefix
     */
    public String getUserPrefix(String uuid) {
        MyPermissionsConfig config = Main.getConfig();
        UserData userData = config.getUsers().get(uuid);

        if (userData != null && !userData.getGroups().isEmpty()) {
            // Find group with highest priority
            String highestPriorityGroup = getHighestPriorityGroup(userData.getGroups(), config);
            if (highestPriorityGroup != null) {
                GroupData group = config.getGroups().get(highestPriorityGroup);
                if (group != null && group.getPrefix() != null && !group.getPrefix().isEmpty()) {
                    return group.getPrefix();
                }
            }
        }

        // Return default group prefix
        GroupData defaultGroup = config.getGroups().get(config.getDefaultGroup());
        return defaultGroup != null && defaultGroup.getPrefix() != null ? defaultGroup.getPrefix() : "";
    }

    /**
     * Gets the user's suffix based on the highest priority group
     * 
     * @param uuid User's UUID
     * @return User's suffix
     */
    public String getUserSuffix(String uuid) {
        MyPermissionsConfig config = Main.getConfig();
        UserData userData = config.getUsers().get(uuid);

        if (userData != null && !userData.getGroups().isEmpty()) {
            // Find group with highest priority
            String highestPriorityGroup = getHighestPriorityGroup(userData.getGroups(), config);
            if (highestPriorityGroup != null) {
                GroupData group = config.getGroups().get(highestPriorityGroup);
                if (group != null && group.getSuffix() != null && !group.getSuffix().isEmpty()) {
                    return group.getSuffix();
                }
            }
        }

        // Return default group suffix
        GroupData defaultGroup = config.getGroups().get(config.getDefaultGroup());
        return defaultGroup != null && defaultGroup.getSuffix() != null ? defaultGroup.getSuffix() : "";
    }

    /**
     * Gets the highest priority group from a list of groups
     * 
     * @param groups List of group names
     * @param config Current configuration
     * @return Name of the highest priority group, or null if none exist
     */
    private String getHighestPriorityGroup(List<String> groups, MyPermissionsConfig config) {
        String highestGroup = null;
        int highestPriority = Integer.MIN_VALUE;

        for (String groupName : groups) {
            GroupData groupData = config.getGroups().get(groupName);
            if (groupData != null) {
                int priority = groupData.getPriority();
                if (priority > highestPriority) {
                    highestPriority = priority;
                    highestGroup = groupName;
                }
            }
        }

        return highestGroup;
    }
}
