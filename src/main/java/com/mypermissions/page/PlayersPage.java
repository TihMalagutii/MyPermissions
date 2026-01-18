package com.mypermissions.page;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.mypermissions.Main;
import com.mypermissions.config.MyPermissionsConfig;

public class PlayersPage extends InteractiveCustomUIPage<PlayersPage.PlayersEventData> {
    private final String groupName;
    private String errorMessage;

    public static class PlayersEventData {
        public String action;
        public String playerName;
        public String removeIndex;

        public static final BuilderCodec<PlayersEventData> CODEC = 
            ((BuilderCodec.Builder<PlayersEventData>) ((BuilderCodec.Builder<PlayersEventData>) ((BuilderCodec.Builder<PlayersEventData>)
            BuilderCodec.builder(PlayersEventData.class, PlayersEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (o, v) -> o.action = v, (o) -> o.action)
                .add())
                .append(new KeyedCodec<>("@PlayerName", Codec.STRING), (o, v) -> o.playerName = v, (o) -> o.playerName)
                .add())
                .append(new KeyedCodec<>("RemoveIndex", Codec.STRING), (o, v) -> o.removeIndex = v, (o) -> o.removeIndex)
                .add())
            .build();
    }

    public PlayersPage(@Nonnull PlayerRef playerRef, String groupName) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, PlayersEventData.CODEC);
        this.groupName = groupName;
    }

    @Override
    public void build(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull UICommandBuilder cmd,
        @Nonnull UIEventBuilder evt,
        @Nonnull Store<EntityStore> store
    ) {
        cmd.append("Pages/PlayersPage.ui");

        // Set title with group name
        cmd.set("#GroupTitle.Text", "PLAYERS - " + groupName);

        // Show error message if present
        if (errorMessage != null && !errorMessage.isEmpty()) {
            cmd.set("#Error.Text", errorMessage);
        }

        // Build player list
        buildPlayerList(cmd, evt);

        // Bind add player button
        evt.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#AddPlayerButton",
            new EventData()
                .append("Action", "add")
                .append("@PlayerName", "#PlayerInput.Value")
        );

        // Bind back button
        evt.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#BackButton",
            new EventData().append("Action", "back")
        );
    }

    private void buildPlayerList(UICommandBuilder cmd, UIEventBuilder evt) {
        cmd.clear("#PlayerList");

        // Get all players that have this group
        List<String> playersInGroup = new ArrayList<>();
        for (Map.Entry<String, MyPermissionsConfig.UserData> entry : Main.getConfig().getUsers().entrySet()) {
            if (entry.getValue().getGroups().contains(groupName)) {
                // Use username instead of UUID
                String username = entry.getValue().getUsername();
                if (username != null && !username.isEmpty()) {
                    playersInGroup.add(username);
                }
            }
        }

        if (playersInGroup.isEmpty()) {
            cmd.appendInline("#PlayerList", "Label { Text: \"No players in this group\"; Anchor: (Height: 40); Style: (FontSize: 14, TextColor: #6e7da1, HorizontalAlignment: Center, VerticalAlignment: Center); }");
            return;
        }

        int i = 0;
        for (String playerName : playersInGroup) {
            String selector = "#PlayerList[" + i + "]";
            cmd.append("#PlayerList", "Pages/PlayerItem.ui");
            cmd.set(selector + " #PlayerName.Text", playerName);

            evt.addEventBinding(
                CustomUIEventBindingType.Activating,
                selector + " #RemoveButton",
                new EventData()
                    .append("Action", "remove")
                    .append("RemoveIndex", String.valueOf(i)),
                false
            );

            i++;
        }
    }

    @Override
    public void handleDataEvent(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull Store<EntityStore> store,
        @Nonnull PlayersEventData data
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if ("add".equals(data.action)) {
            handleAdd(ref, store, player, data);
        } else if ("remove".equals(data.action)) {
            handleRemove(ref, store, player, data);
        } else if ("back".equals(data.action)) {
            handleBack(ref, store, player);
        }
    }

    private void handleAdd(Ref<EntityStore> ref, Store<EntityStore> store, Player player, PlayersEventData data) {
        if (data.playerName == null || data.playerName.trim().isEmpty()) {
            showError(ref, store, player, "Please enter a player name");
            return;
        }

        String targetPlayerName = data.playerName.trim();

        // Check if group exists
        if (!Main.getConfig().getGroups().containsKey(groupName)) {
            return;
        }

        // Find UUID by username
        String targetUUID = null;
        for (Map.Entry<String, MyPermissionsConfig.UserData> entry : Main.getConfig().getUsers().entrySet()) {
            if (targetPlayerName.equalsIgnoreCase(entry.getValue().getUsername())) {
                targetUUID = entry.getKey();
                break;
            }
        }

        // If player not found, show error
        if (targetUUID == null) {
            showError(ref, store, player, "Player '" + targetPlayerName + "' not found. Player must have joined the server at least once.");
            return;
        }

        MyPermissionsConfig.UserData userData = Main.getConfig().getUsers().get(targetUUID);
        if (userData == null) {
            return;
        }

        // Check if player already has this group
        if (userData.getGroups().contains(groupName)) {
            showError(ref, store, player, "Player '" + targetPlayerName + "' already has this group");
            return;
        }

        // Add group to player
        userData.getGroups().add(groupName);

        // Save config
        Main.getConfigManager().save();

        // Clear cache
        Main.getPermissionManager().clearAllCache();

        // Refresh page
        PlayersPage refreshed = new PlayersPage(playerRef, groupName);
        player.getPageManager().openCustomPage(ref, store, refreshed);
    }

    private void showError(Ref<EntityStore> ref, Store<EntityStore> store, Player player, String errorMessage) {
        // Create new page with error message
        PlayersPage refreshed = new PlayersPage(playerRef, groupName);
        refreshed.errorMessage = errorMessage;
        
        player.getPageManager().openCustomPage(ref, store, refreshed);
    }

    private void handleRemove(Ref<EntityStore> ref, Store<EntityStore> store, Player player, PlayersEventData data) {
        if (data.removeIndex == null) {
            return;
        }

        try {
            int index = Integer.parseInt(data.removeIndex);

            // Get list of players in group (with usernames)
            List<String> playersInGroup = new ArrayList<>();
            List<String> uuidsInGroup = new ArrayList<>();
            for (Map.Entry<String, MyPermissionsConfig.UserData> entry : Main.getConfig().getUsers().entrySet()) {
                if (entry.getValue().getGroups().contains(groupName)) {
                    String username = entry.getValue().getUsername();
                    if (username != null && !username.isEmpty()) {
                        playersInGroup.add(username);
                        uuidsInGroup.add(entry.getKey());
                    }
                }
            }

            if (index >= 0 && index < playersInGroup.size()) {
                String targetUUID = uuidsInGroup.get(index);
                MyPermissionsConfig.UserData userData = Main.getConfig().getUsers().get(targetUUID);

                if (userData != null) {
                    // Remove group from player
                    userData.getGroups().remove(groupName);

                    // Add default group if player has no groups
                    String defaultGroup = Main.getConfig().getDefaultGroup();
                    if (userData.getGroups().isEmpty() && !userData.getGroups().contains(defaultGroup)) {
                        userData.getGroups().add(defaultGroup);
                    }

                    // Save config
                    Main.getConfigManager().save();

                    // Clear cache
                    Main.getPermissionManager().clearAllCache();
                }

                // Refresh page
                PlayersPage refreshed = new PlayersPage(playerRef, groupName);
                player.getPageManager().openCustomPage(ref, store, refreshed);
            }
        } catch (NumberFormatException e) {
            // Invalid index
        }
    }

    private void handleBack(Ref<EntityStore> ref, Store<EntityStore> store, Player player) {
        // Go back to main page
        MainPage mainPage = new MainPage(playerRef);
        player.getPageManager().openCustomPage(ref, store, mainPage);
    }
}
