package com.mypermissions.config;

import java.util.*;

public class MyPermissionsConfig {
    private String defaultGroup = "default";
    private Map<String, GroupData> groups = new HashMap<>();
    private Map<String, UserData> users = new HashMap<>(); // UUID as String

    public MyPermissionsConfig() {
        // Create default groups
        groups.put("default", new GroupData(
            Arrays.asList("mypermissions.user"),
            new ArrayList<>(),
            "&8[Default] &f",
            "",
            0  // Lowest priority
        ));
        groups.put("admin", new GroupData(
            Arrays.asList("*"),
            Arrays.asList("default"),
            "&c[Admin] &f",
            "",
            100  // Highest priority
        ));
    }

    // Getters and Setters
    public String getDefaultGroup() {
        return defaultGroup;
    }

    public void setDefaultGroup(String defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

    public Map<String, GroupData> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, GroupData> groups) {
        this.groups = groups;
    }

    public Map<String, UserData> getUsers() {
        return users;
    }

    public void setUsers(Map<String, UserData> users) {
        this.users = users;
    }

    // Helper method to search UUID by username
    public String getUuidByUsername(String username) {
        for (Map.Entry<String, UserData> entry : users.entrySet()) {
            if (entry.getValue().getUsername().equalsIgnoreCase(username)) {
                return entry.getKey();
            }
        }
        return null;
    }

    // Inner classes
    public static class GroupData {
        private List<String> permissions;
        private List<String> parents;
        private String prefix;
        private String suffix;
        private int priority; // Group weight (higher = more important)

        public GroupData() {
            this.permissions = new ArrayList<>();
            this.parents = new ArrayList<>();
            this.prefix = "";
            this.suffix = "";
            this.priority = 0;
        }

        public GroupData(List<String> permissions, List<String> parents, String prefix, String suffix, int priority) {
            this.permissions = permissions;
            this.parents = parents;
            this.prefix = prefix;
            this.suffix = suffix;
            this.priority = priority;
        }

        // Getters and Setters
        public List<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<String> permissions) {
            this.permissions = permissions;
        }

        public List<String> getParents() {
            return parents;
        }

        public void setParents(List<String> parents) {
            this.parents = parents;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }
    }

    public static class UserData {
        private String username;
        private List<String> groups;
        private List<String> permissions;

        public UserData() {
            this.username = "";
            this.groups = new ArrayList<>();
            this.permissions = new ArrayList<>();
        }

        public UserData(String username, List<String> groups, List<String> permissions) {
            this.username = username;
            this.groups = groups;
            this.permissions = permissions;
        }

        // Getters and Setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public List<String> getGroups() {
            return groups;
        }

        public void setGroups(List<String> groups) {
            this.groups = groups;
        }

        public List<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<String> permissions) {
            this.permissions = permissions;
        }
    }
}