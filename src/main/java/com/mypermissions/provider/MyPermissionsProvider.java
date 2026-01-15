package com.mypermissions.provider;

import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;
import com.mypermissions.config.ConfigManager;
import com.mypermissions.config.MyPermissionsConfig;
import com.mypermissions.manager.PermissionManager;

import javax.annotation.Nonnull;
import java.util.*;

public class MyPermissionsProvider implements PermissionProvider {
    
    private final ConfigManager configManager;
    private final PermissionManager permissionManager;
    
    public MyPermissionsProvider(ConfigManager configManager, PermissionManager permissionManager) {
        this.configManager = configManager;
        this.permissionManager = permissionManager;
    }
    
    @Nonnull
    @Override
    public String getName() {
        return "MyPermissionsProvider";
    }
    
    @Override
    public void addUserPermissions(@Nonnull UUID uuid, @Nonnull Set<String> permissions) {
        var config = configManager.getConfig();
        var user = config.getUsers().computeIfAbsent(uuid.toString(), k -> new MyPermissionsConfig.UserData());
        
        List<String> permList = user.getPermissions();
        if (permList == null) {
            permList = new ArrayList<>();
            user.setPermissions(permList);
        }
        
        for (String perm : permissions) {
            if (!permList.contains(perm)) {
                permList.add(perm);
            }
        }
        
        configManager.save();
        permissionManager.clearCache(uuid);
    }
    
    @Override
    public void removeUserPermissions(@Nonnull UUID uuid, @Nonnull Set<String> permissions) {
        var config = configManager.getConfig();
        var user = config.getUsers().get(uuid.toString());
        
        if (user != null) {
            List<String> permList = user.getPermissions();
            if (permList != null) {
                permList.removeAll(permissions);
                if (permList.isEmpty()) {
                    user.setPermissions(null);
                }
                configManager.save();
                permissionManager.clearCache(uuid);
            }
        }
    }
    
    @Nonnull
    @Override
    public Set<String> getUserPermissions(@Nonnull UUID uuid) {
        var config = configManager.getConfig();
        var user = config.getUsers().get(uuid.toString());
        
        if (user != null) {
            List<String> permList = user.getPermissions();
            if (permList != null) {
                return new HashSet<>(permList);
            }
        }
        
        return Collections.emptySet();
    }
    
    @Override
    public void addGroupPermissions(@Nonnull String group, @Nonnull Set<String> permissions) {
        var config = configManager.getConfig();
        var groupData = config.getGroups().computeIfAbsent(group, k -> new MyPermissionsConfig.GroupData());
        
        List<String> permList = groupData.getPermissions();
        if (permList == null) {
            permList = new ArrayList<>();
            groupData.setPermissions(permList);
        }
        
        for (String perm : permissions) {
            if (!permList.contains(perm)) {
                permList.add(perm);
            }
        }
        
        configManager.save();
        permissionManager.clearAllCache();
    }
    
    @Override
    public void removeGroupPermissions(@Nonnull String group, @Nonnull Set<String> permissions) {
        var config = configManager.getConfig();
        var groupData = config.getGroups().get(group);
        
        if (groupData != null) {
            List<String> permList = groupData.getPermissions();
            if (permList != null) {
                permList.removeAll(permissions);
                if (permList.isEmpty()) {
                    groupData.setPermissions(null);
                }
                configManager.save();
                permissionManager.clearAllCache();
            }
        }
    }
    
    @Nonnull
    @Override
    public Set<String> getGroupPermissions(@Nonnull String group) {
        var config = configManager.getConfig();
        var groupData = config.getGroups().get(group);
        
        if (groupData != null) {
            List<String> permList = groupData.getPermissions();
            if (permList != null) {
                return new HashSet<>(permList);
            }
        }
        
        return Collections.emptySet();
    }
    
    @Override
    public void addUserToGroup(@Nonnull UUID uuid, @Nonnull String group) {
        var config = configManager.getConfig();
        var user = config.getUsers().computeIfAbsent(uuid.toString(), k -> new MyPermissionsConfig.UserData());
        
        List<String> groupList = user.getGroups();
        if (groupList == null) {
            groupList = new ArrayList<>();
            user.setGroups(groupList);
        }
        
        if (!groupList.contains(group)) {
            groupList.add(group);
            configManager.save();
            permissionManager.clearCache(uuid);
        }
    }
    
    @Override
    public void removeUserFromGroup(@Nonnull UUID uuid, @Nonnull String group) {
        var config = configManager.getConfig();
        var user = config.getUsers().get(uuid.toString());
        
        if (user != null) {
            List<String> groupList = user.getGroups();
            if (groupList != null) {
                groupList.remove(group);
                if (groupList.isEmpty()) {
                    user.setGroups(null);
                }
                configManager.save();
                permissionManager.clearCache(uuid);
            }
        }
    }
    
    @Nonnull
    @Override
    public Set<String> getGroupsForUser(@Nonnull UUID uuid) {
        var config = configManager.getConfig();
        var user = config.getUsers().get(uuid.toString());
        
        if (user != null) {
            List<String> groupList = user.getGroups();
            if (groupList != null && !groupList.isEmpty()) {
                return new HashSet<>(groupList);
            }
        }
        
        return Collections.emptySet();
    }
}
