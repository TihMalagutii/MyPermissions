package com.mypermissions.listener;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.mypermissions.Main;
import com.mypermissions.config.MyPermissionsConfig;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class PlayerListener {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static void register(@Nonnull JavaPlugin plugin) {
        // Register listener for when player joins the world
        plugin.getEventRegistry().registerGlobal(AddPlayerToWorldEvent.class, PlayerListener::onPlayerJoin);
    }

    private static void onPlayerJoin(@Nonnull AddPlayerToWorldEvent event) {
        try {
            // Get player components
            var player = event.getHolder().getComponent(Player.getComponentType());
            var playerRef = event.getHolder().getComponent(PlayerRef.getComponentType());
            
            if (player == null || playerRef == null) return;
            
            String uuid = playerRef.getUuid().toString();
            String username = player.getDisplayName();
            
            // Check if player already has user data
            MyPermissionsConfig.UserData userData = Main.getConfig().getUsers().get(uuid);
            
            // If no data OR no groups, add to default group
            if (userData == null || userData.getGroups().isEmpty()) {
                String defaultGroup = Main.getConfig().getDefaultGroup();
                
                // Add to default group
                Main.getPermissionManager().addUserToGroup(uuid, defaultGroup);
                
                // Update username
                userData = Main.getConfig().getUsers().get(uuid);
                if (userData != null) {
                    userData.setUsername(username);
                }
                
                Main.getConfigManager().save();
                
                // Log to console
                LOGGER.at(Level.INFO).log(
                    "Player " + username + " (" + uuid + ") assigned to default group: " + defaultGroup
                );
            } else {
                // Update username if changed
                if (!username.equals(userData.getUsername())) {
                    userData.setUsername(username);
                    Main.getConfigManager().save();
                    LOGGER.at(Level.INFO).log("Updated username for " + uuid + " to " + username);
                }
            }
            
        } catch (Exception e) {
            LOGGER.at(Level.SEVERE).log("Error handling AddPlayerToWorldEvent: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
