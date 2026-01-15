package com.mypermissions.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    private static final DateTimeFormatter BACKUP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final int MAX_BACKUPS = 5; // Keep last 5 backups
    
    private final Path configPath;
    private final Path backupFolder;
    private MyPermissionsConfig config;

    public ConfigManager(Path dataFolder) {
        this.configPath = dataFolder.resolve("config.json");
        this.backupFolder = dataFolder.resolve("backups");
        this.config = load();
    }

    private MyPermissionsConfig load() {
        try {
            // Create directories if they don't exist
            Files.createDirectories(configPath.getParent());
            Files.createDirectories(backupFolder);

            // If file doesn't exist, create with default values
            if (!Files.exists(configPath)) {
                MyPermissionsConfig defaultConfig = new MyPermissionsConfig();
                save(defaultConfig);
                createConfigGuide();
                return defaultConfig;
            }

            // Read JSON file
            String json = Files.readString(configPath);
            MyPermissionsConfig loadedConfig = GSON.fromJson(json, MyPermissionsConfig.class);
            
            // Validate if config was loaded correctly
            if (loadedConfig == null || loadedConfig.getGroups() == null) {
                System.err.println("[MyPermissions] Config corrupted! Attempting to restore from backup...");
                return restoreFromLatestBackup();
            }
            
            return loadedConfig;

        } catch (Exception e) {
            System.err.println("[MyPermissions] Error loading config: " + e.getMessage());
            e.printStackTrace();
            
            // Try to restore from backup
            try {
                return restoreFromLatestBackup();
            } catch (Exception ex) {
                System.err.println("[MyPermissions] Failed to restore from backup! Using default config.");
                return new MyPermissionsConfig();
            }
        }
    }

    public void save() {
        save(this.config);
    }

    private void save(MyPermissionsConfig config) {
        try {
            // Create backup before saving
            if (Files.exists(configPath)) {
                createBackup();
            }
            
            // Save config
            String json = GSON.toJson(config);
            Files.writeString(configPath, json);
            
            // Clean old backups
            cleanOldBackups();
            
        } catch (IOException e) {
            System.err.println("[MyPermissions] Error saving config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a backup of the current config
     */
    private void createBackup() {
        try {
            String timestamp = LocalDateTime.now().format(BACKUP_FORMAT);
            Path backupFile = backupFolder.resolve("config_" + timestamp + ".json");
            Files.copy(configPath, backupFile, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[MyPermissions] Backup created: " + backupFile.getFileName());
        } catch (IOException e) {
            System.err.println("[MyPermissions] Failed to create backup: " + e.getMessage());
        }
    }

    /**
     * Removes old backups, keeping only the last MAX_BACKUPS
     */
    private void cleanOldBackups() {
        try (Stream<Path> backups = Files.list(backupFolder)) {
            List<Path> backupList = backups
                .filter(p -> p.getFileName().toString().startsWith("config_"))
                .filter(p -> p.getFileName().toString().endsWith(".json"))
                .sorted(Comparator.comparing(Path::toString).reversed())
                .toList();
            
            // Delete excess backups
            for (int i = MAX_BACKUPS; i < backupList.size(); i++) {
                Files.deleteIfExists(backupList.get(i));
            }
        } catch (IOException e) {
            System.err.println("[MyPermissions] Failed to clean old backups: " + e.getMessage());
        }
    }

    /**
     * Restores config from the most recent backup
     */
    private MyPermissionsConfig restoreFromLatestBackup() throws IOException {
        Path latestBackup = getLatestBackup();
        if (latestBackup == null) {
            throw new IOException("No backup available");
        }
        
        System.out.println("[MyPermissions] Restoring from backup: " + latestBackup.getFileName());
        String json = Files.readString(latestBackup);
        MyPermissionsConfig restored = GSON.fromJson(json, MyPermissionsConfig.class);
        
        // Save as current config
        Files.copy(latestBackup, configPath, StandardCopyOption.REPLACE_EXISTING);
        
        return restored;
    }

    /**
     * Restores config from a specific backup
     */
    public boolean restoreFromBackup(String backupName) {
        try {
            Path backupFile = backupFolder.resolve(backupName);
            if (!Files.exists(backupFile)) {
                System.err.println("[MyPermissions] Backup not found: " + backupName);
                return false;
            }
            
            String json = Files.readString(backupFile);
            MyPermissionsConfig restored = GSON.fromJson(json, MyPermissionsConfig.class);
            
            if (restored == null || restored.getGroups() == null) {
                System.err.println("[MyPermissions] Backup file is corrupted: " + backupName);
                return false;
            }
            
            // Create backup of current config before restoring
            createBackup();
            
            // Restore
            this.config = restored;
            Files.copy(backupFile, configPath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("[MyPermissions] Config restored from: " + backupName);
            return true;
            
        } catch (Exception e) {
            System.err.println("[MyPermissions] Failed to restore backup: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lists all available backups
     */
    public List<String> listBackups() {
        try (Stream<Path> backups = Files.list(backupFolder)) {
            return backups
                .filter(p -> p.getFileName().toString().startsWith("config_"))
                .filter(p -> p.getFileName().toString().endsWith(".json"))
                .sorted(Comparator.comparing(Path::toString).reversed())
                .map(p -> p.getFileName().toString())
                .toList();
        } catch (IOException e) {
            System.err.println("[MyPermissions] Failed to list backups: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets the most recent backup
     */
    private Path getLatestBackup() throws IOException {
        try (Stream<Path> backups = Files.list(backupFolder)) {
            return backups
                .filter(p -> p.getFileName().toString().startsWith("config_"))
                .filter(p -> p.getFileName().toString().endsWith(".json"))
                .max(Comparator.comparing(Path::toString))
                .orElse(null);
        }
    }

    /**
     * Creates the configuration guide file for users
     */
    private void createConfigGuide() {
        try {
            Path guidePath = configPath.getParent().resolve("config-guide.json");
            
            // Don't overwrite if already exists
            if (Files.exists(guidePath)) {
                return;
            }
            
            String guideContent = """
{
  "_comment_1": "====================================================================================",
  "_comment_2": "MyPermissions Configuration Guide",
  "_comment_3": "This is an annotated example. Your actual config.json won't have these _comment fields.",
  "_comment_4": "====================================================================================",
  "_IMPORTANT_NOTE": "⚠️ Permission nodes shown here (server.fly, server.kick, etc.) are EXAMPLES ONLY!",
  "_IMPORTANT_NOTE_2": "Replace them with actual Hytale permission nodes from your server/plugins.",
  "_IMPORTANT_NOTE_3": "Refer to official Hytale documentation and plugin docs for real permission names.",
  
  "defaultGroup": "default",
  "_defaultGroup_info": "The group automatically assigned to new players when they join the server",
  
  "groups": {
    "default": {
      "_group_description": "Basic group for all regular players",
      
      "permissions": [
        "mypermissions.user",
        "_permissions_info: Add permission nodes here",
        "_WARNING: The permission examples below (server.*) are PLACEHOLDERS!",
        "_WARNING: Replace with actual permissions from Hytale/your plugins documentation",
        "_example: server.chat - allows chatting (EXAMPLE ONLY)",
        "_example: server.build - allows building (EXAMPLE ONLY)",
        "_wildcard: * - grants ALL permissions",
        "_partial_wildcard: server.* - grants all permissions starting with 'server.'",
        "_negation: -server.fly - denies flying even if granted by wildcard"
      ],
      
      "parents": [],
      "_parents_info": "List of parent groups to inherit permissions from",
      "_example": ["parentGroup1", "parentGroup2"],
      
      "prefix": "&8[Default] ",
      "_prefix_info": "Text shown before player name in chat",
      "_color_codes": "&0-9, &a-f for colors | &l for bold | &o for italic | &r for reset",
      "_example_prefix": "&6[VIP] &f - shows gold [VIP] with white space after",
      
      "suffix": "",
      "_suffix_info": "Text shown after player name in chat",
      "_example_suffix": " &7[Level 50] - shows gray level badge after name",
      
      "priority": 0,
      "_priority_info": "Higher priority = takes precedence for prefix/suffix display",
      "_priority_example": "If player is in 'vip'(50) and 'helper'(60), helper prefix shows"
    },
    
    "admin": {
      "_group_description": "Full server access - Administrator group",
      
      "permissions": [
        "*"
      ],
      "_permissions_note": "The * wildcard grants ALL permissions on the server",
      
      "parents": [
        "default"
      ],
      "_parents_note": "Inherits all permissions from the 'default' group",
      
      "prefix": "&c[Admin] ",
      "_prefix_note": "&c = red color",
      
      "suffix": "",
      
      "priority": 100,
      "_priority_note": "Highest priority - admin prefix always shows"
    },
    
    "vip": {
      "_example_group": "Example VIP group - customize as needed",
      "_IMPORTANT": "⚠️ Replace 'server.*' permissions below with REAL Hytale permissions!",
      
      "permissions": [
        "example.fly",
        "example.speed",
        "example.feature",
        "-example.denied.feature"
      ],
      "_permission_explanation": [
        "⚠️ These are EXAMPLE permission names only!",
        "Replace with actual permissions from your Hytale server plugins",
        "Check official Hytale documentation for real permission nodes",
        "Negation example: -example.denied.feature - explicitly denies this"
      ],
      
      "parents": [
        "default"
      ],
      
      "prefix": "&6&l[VIP]&r &f",
      "_prefix_breakdown": "&6 (gold) &l (bold) [VIP] &r (reset) &f (white space)",
      
      "suffix": " &7⭐",
      "_suffix_note": "Shows a gray star after player name",
      
      "priority": 50,
      "_priority_explanation": "Mid-priority - between default(0) and admin(100)"
    },
    
    "moderator": {
      "_example_group": "Example Moderator group - for server helpers",
      "_IMPORTANT": "⚠️ Replace with actual moderation permissions from your server!",
      
      "permissions": [
        "example.kick",
        "example.ban",
        "example.mute",
        "example.warn",
        "myperms.admin"
      ],
      "_note": "myperms.admin is a REAL permission from MyPermissions plugin",
      
      "parents": [
        "default"
      ],
      
      "prefix": "&9[Mod] ",
      "_prefix_note": "&9 = blue color",
      
      "suffix": "",
      
      "priority": 75
    }
  },
  
  "users": {
    "_users_info": "Player data stored by UUID",
    "_structure_example": "uuid-string",
    
    "00000000-0000-0000-0000-000000000000": {
      "_example_user": "This is just an example entry",
      
      "username": "ExamplePlayer",
      "_username_info": "Display name - updated automatically when player joins",
      
      "groups": [
        "vip",
        "helper"
      ],
      "_groups_info": "List of groups this player belongs to",
      "_groups_note": "Players inherit permissions from all their groups",
      
      "permissions": [
        "example.permission",
        "-example.denied.permission"
      ],
      "_permissions_info": "User-specific permissions (override group permissions)",
      "_WARNING": "⚠️ Replace 'example.*' with REAL permission nodes from your plugins!",
      "_example_1": "example.permission - grants this specific player a permission",
      "_example_2": "-example.denied.permission - denies a permission even if granted by group"
    }
  },
  
  "_configuration_tips": [
    "1. Always test changes with /mpcheck <player> <permission>",
    "2. Use /mpreload after editing this file manually",
    "3. Backups are automatically created in plugins/MyPermissions/backups/",
    "4. Higher priority groups override lower priority for prefix/suffix",
    "5. User-specific permissions always override group permissions",
    "6. Negated permissions (-permission) override granted permissions"
  ],
  
  "_color_code_reference": {
    "&0": "Black",
    "&1": "Dark Blue", 
    "&2": "Dark Green",
    "&3": "Dark Cyan",
    "&4": "Dark Red",
    "&5": "Purple",
    "&6": "Gold",
    "&7": "Gray",
    "&8": "Dark Gray",
    "&9": "Blue",
    "&a": "Green",
    "&b": "Cyan",
    "&c": "Red",
    "&d": "Pink",
    "&e": "Yellow",
    "&f": "White",
    "&l": "Bold",
    "&o": "Italic",
    "&r": "Reset"
  },
  
  "_command_reference": [
    "/mpgroup-create <name> - Create new group",
    "/mpgroup-addperm <group> <perm> - Add permission to group",
    "/mpgroup-setpriority <group> <number> - Set group priority",
    "/mpuser-add <player> <group> - Add player to group",
    "/mpuser-addperm <player> <perm> - Give player specific permission",
    "/mpcheck <player> <perm> - Test if player has permission",
    "/mpreload - Reload configuration"
  ]
}
                """;
            
            Files.writeString(guidePath, guideContent);
            System.out.println("[MyPermissions] Configuration guide created: config-guide.json");
            
        } catch (IOException e) {
            System.err.println("[MyPermissions] Failed to create config guide: " + e.getMessage());
        }
    }

    public MyPermissionsConfig getConfig() {
        return config;
    }

    public void reload() {
        this.config = load();
    }
}
