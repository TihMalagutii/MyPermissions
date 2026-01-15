package com.mypermissions;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.mypermissions.command.MyPermsBackupCommand;
import com.mypermissions.command.MyPermsCheckCommand;
import com.mypermissions.command.MyPermsCommand;
import com.mypermissions.command.MyPermsReloadCommand;
import com.mypermissions.command.backup.*;
import com.mypermissions.command.group.*;
import com.mypermissions.command.user.*;
import com.mypermissions.config.ConfigManager;
import com.mypermissions.config.MyPermissionsConfig;
import com.mypermissions.listener.ChatListener;
import com.mypermissions.listener.PlayerListener;
import com.mypermissions.manager.PermissionManager;
import com.mypermissions.provider.MyPermissionsProvider;

public class Main extends JavaPlugin {
    private static ConfigManager configManager;
    private static PermissionManager permissionManager;

    public Main(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();

        try {
            // Get plugin data folder
            Path pluginJar = this.getFile();
            Path pluginsFolder = pluginJar.getParent();
            Path dataFolder = pluginsFolder.resolve("MyPermissions");
            
            // Initialize ConfigManager for JSON persistence
            configManager = new ConfigManager(dataFolder);
            
            // Initialize PermissionManager for permission checking
            permissionManager = new PermissionManager();

            // Register our provider with Hytale's permission system
            MyPermissionsProvider provider = new MyPermissionsProvider(configManager, permissionManager);
            PermissionsModule.get().addProvider(provider);

            // Register event listeners
            PlayerListener.register(this);
            ChatListener.register(this);

            // Register main commands
            this.getCommandRegistry().registerCommand(new MyPermsCommand());
            this.getCommandRegistry().registerCommand(new MyPermsReloadCommand());
            this.getCommandRegistry().registerCommand(new MyPermsCheckCommand());
            this.getCommandRegistry().registerCommand(new MyPermsBackupCommand());

            // Register group management commands
            this.getCommandRegistry().registerCommand(new GroupListCommand());
            this.getCommandRegistry().registerCommand(new GroupInfoCommand());
            this.getCommandRegistry().registerCommand(new GroupCreateCommand());
            this.getCommandRegistry().registerCommand(new GroupDeleteCommand());
            this.getCommandRegistry().registerCommand(new GroupAddPermCommand());
            this.getCommandRegistry().registerCommand(new GroupRemovePermCommand());
            this.getCommandRegistry().registerCommand(new GroupSetParentCommand());
            this.getCommandRegistry().registerCommand(new GroupSetPriorityCommand());

            // Register user management commands
            this.getCommandRegistry().registerCommand(new UserAddCommand());
            this.getCommandRegistry().registerCommand(new UserRemoveCommand());
            this.getCommandRegistry().registerCommand(new UserInfoCommand());
            this.getCommandRegistry().registerCommand(new UserAddPermCommand());
            this.getCommandRegistry().registerCommand(new UserRemovePermCommand());

            // Register backup management commands
            this.getCommandRegistry().registerCommand(new BackupListCommand());
            this.getCommandRegistry().registerCommand(new BackupRestoreCommand());

            this.getLogger().at(Level.INFO).log("MyPermissions plugin has been enabled.");
            this.getLogger().at(Level.INFO).log("DefaultGroup: " + 
                configManager.getConfig().getDefaultGroup());
            this.getLogger().at(Level.INFO).log("Groups loaded: " + 
                configManager.getConfig().getGroups().size());
            
        } catch (Exception e) {
            this.getLogger().at(Level.SEVERE).log("ERRO no setup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static MyPermissionsConfig getConfig() {
        return configManager.getConfig();
    }

    public static PermissionManager getPermissionManager() {
        return permissionManager;
    }

    // ==================== Public API for Other Plugins ====================
    
    /**
     * Checks if a player has a specific permission
     * @param uuid Player's UUID
     * @param permission Permission node to check
     * @return true if the player has the permission
     */
    public static boolean hasPermission(UUID uuid, String permission) {
        if (permissionManager == null || uuid == null || permission == null) {
            return false;
        }
        return permissionManager.hasPermission(uuid.toString(), permission);
    }

    /**
     * Obtém o prefix do jogador baseado no grupo de maior prioridade
     * @param uuid UUID do jogador
     * @return Prefix do jogador (pode conter códigos de cor &)
     */
    public static String getPlayerPrefix(UUID uuid) {
        if (permissionManager == null || uuid == null) {
            return "";
        }
        String prefix = permissionManager.getUserPrefix(uuid.toString());
        return prefix != null ? prefix : "";
    }

    /**
     * Obtém o suffix do jogador baseado no grupo de maior prioridade
     * @param uuid UUID do jogador
     * @return Suffix do jogador (pode conter códigos de cor &)
     */
    public static String getPlayerSuffix(UUID uuid) {
        if (permissionManager == null || uuid == null) {
            return "";
        }
        String suffix = permissionManager.getUserSuffix(uuid.toString());
        return suffix != null ? suffix : "";
    }

    /**
     * Obtém os grupos do jogador
     * @param uuid UUID do jogador
     * @return Lista de nomes dos grupos
     */
    public static List<String> getPlayerGroups(UUID uuid) {
        if (configManager == null || uuid == null) {
            return new ArrayList<>();
        }
        var userData = configManager.getConfig().getUsers().get(uuid.toString());
        if (userData != null && userData.getGroups() != null) {
            return new ArrayList<>(userData.getGroups());
        }
        return new ArrayList<>();
    }
}
