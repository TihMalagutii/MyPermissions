package com.mypermissions.page;

import java.util.Map;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.mypermissions.Main;
import com.mypermissions.config.MyPermissionsConfig.GroupData;

public class MainPage extends InteractiveCustomUIPage<MainPage.MainEventData> {
    private String message;

    public static class MainEventData {
        public String action;
        public String name;
        public String prefix;

        public static final BuilderCodec<MainEventData> CODEC = ((BuilderCodec.Builder<MainEventData>) ((BuilderCodec.Builder<MainEventData>) ((BuilderCodec.Builder<MainEventData>)
            BuilderCodec.builder(MainEventData.class, MainEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (MainEventData o, String v) -> o.action = v, (MainEventData o) -> o.action)
                .add())
                .append(new KeyedCodec<>("Name", Codec.STRING), (MainEventData o, String v) -> o.name = v, (MainEventData o) -> o.name)
                .add())
                .append(new KeyedCodec<>("Prefix", Codec.STRING), (MainEventData o, String v) -> o.prefix = v, (MainEventData o) -> o.prefix)
                .add())
            .build();
    }

    public MainPage(
        @Nonnull PlayerRef playerRef
    ) {
        super(
            playerRef,
            CustomPageLifetime.CanDismissOrCloseThroughInteraction,
            MainEventData.CODEC
        );
        this.message = null;
    }

    public MainPage(
        @Nonnull PlayerRef playerRef,
        String message
    ) {
        super(
            playerRef,
            CustomPageLifetime.CanDismissOrCloseThroughInteraction,
            MainEventData.CODEC
        );
        this.message = message;
    }

    @Override
    public void build(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull UICommandBuilder commandBuilder,
        @Nonnull UIEventBuilder eventBuilder,
        @Nonnull Store<EntityStore> store
    ) {
        commandBuilder.append("Pages/MainPage.ui");

        // Exibir mensagem se houver
        if (message != null && !message.isEmpty()) {
            commandBuilder.set("#Message.Text", message);
        }

        // pegar os grupos no config.json e buildar a lista
        Map<String, GroupData> groups = Main.getConfig().getGroups();

        buildGroupList(commandBuilder, eventBuilder, groups);

        // Bind new group button
        eventBuilder.addEventBinding(
            com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType.Activating,
            "#NewGroupButton",
            new com.hypixel.hytale.server.core.ui.builder.EventData()
                .append("Action", "newgroup")
        );

        // Bind reload button
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#ReloadButton",
            new EventData().append("Action", "reload")
        );

        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CloseButton",
            new EventData().append("Action", "Close")
        );
    }

    private void buildGroupList(
        @Nonnull UICommandBuilder commandBuilder,
        @Nonnull UIEventBuilder eventBuilder,
        Map<String, GroupData> groups
    ) {
        commandBuilder.clear("#GroupList");

        if (groups.isEmpty()) {
            commandBuilder.append("#GroupList", "Label { Text: \"No groups found\"; Anchor: (Height: 40); Style: (FontSize: 14, TextColor: #6e7da1, HorizontalAlignment: Center, VerticalAlignment: Center); }");
            return;
        }

        int i = 0;
        for (Map.Entry<String, GroupData> entry : groups.entrySet()) {
            String groupName = entry.getKey();
            String selector = "#GroupList[" + i + "]";
            commandBuilder.append("#GroupList", "Pages/GroupEntry.ui");

            commandBuilder.set(selector + " #Name.Text", groupName);
            
            // Bind players button event
            eventBuilder.addEventBinding(
                com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType.Activating,
                selector + " #PlayersButton",
                new com.hypixel.hytale.server.core.ui.builder.EventData()
                    .append("Action", "players")
                    .append("Name", groupName),
                false
            );
            
            // Bind permissions button event
            eventBuilder.addEventBinding(
                com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType.Activating,
                selector + " #PermissionsButton",
                new com.hypixel.hytale.server.core.ui.builder.EventData()
                    .append("Action", "permissions")
                    .append("Name", groupName),
                false
            );
            
            // Bind edit button event
            eventBuilder.addEventBinding(
                com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType.Activating,
                selector + " #EditButton",
                new com.hypixel.hytale.server.core.ui.builder.EventData()
                    .append("Action", "edit")
                    .append("Name", groupName),
                false
            );
            
            // Bind delete button event
            eventBuilder.addEventBinding(
                com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType.Activating,
                selector + " #DeleteButton",
                new com.hypixel.hytale.server.core.ui.builder.EventData()
                    .append("Action", "delete")
                    .append("Name", groupName),
                false
            );
            
            i++;
        }
    }

    @Override
    public void handleDataEvent(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull Store<EntityStore> store,
        @Nonnull MainEventData data
    ) {
        Player player = (Player) store.getComponent(ref, Player.getComponentType());

        switch (data.action) {
            case "newgroup":
                NewGroupPage newGroupPage = new NewGroupPage(playerRef);
                player.getPageManager().openCustomPage(ref, store, newGroupPage);
                break;

            case "players":
                if (data.name != null) {
                    PlayersPage playersPage = new PlayersPage(playerRef, data.name);
                    player.getPageManager().openCustomPage(ref, store, playersPage);
                }
                break;

            case "permissions":
                if (data.name != null) {
                    PermissionsPage permissionsPage = new PermissionsPage(playerRef, data.name);
                    player.getPageManager().openCustomPage(ref, store, permissionsPage);
                }
                break;

            case "edit":
                if (data.name != null) {
                    EditGroupPage editGroupPage = new EditGroupPage(playerRef, data.name);
                    player.getPageManager().openCustomPage(ref, store, editGroupPage);
                }
                break;

            case "delete":
                String groupName = data.name;

                // Check if group exists
                if (!Main.getConfig().getGroups().containsKey(groupName)) {
                    return;
                }
                
                // Get default group name
                String defaultGroup = Main.getConfig().getDefaultGroup();
                
                // Remove group from all users and add default group
                Main.getConfig().getUsers().values().forEach(userData -> {
                    if (userData.getGroups().remove(groupName)) {
                        // User had this group, add default if not already present
                        if (!userData.getGroups().contains(defaultGroup)) {
                            userData.getGroups().add(defaultGroup);
                        }
                    }
                });
                
                // Remove this group from all parent lists
                for (GroupData group : Main.getConfig().getGroups().values()) {
                    group.getParents().remove(groupName);
                }
                
                // Delete group from config
                Main.getConfig().getGroups().remove(groupName);
                
                // Clear permission cache
                Main.getPermissionManager().clearAllCache();
                
                // Save config
                Main.getConfigManager().save();
                
                // Refresh page
                MainPage refreshedPage = new MainPage(playerRef);
                player.getPageManager().openCustomPage(ref, store, refreshedPage);

                break;
            
            case "reload":
                // Reload config (same as /mpreload command)
                Main.getConfigManager().reload();
                Main.getPermissionManager().clearCache();
                
                // Refresh page to show updated data with success message
                MainPage reloadedPage = new MainPage(playerRef, "Configuration reloaded successfully!");
                player.getPageManager().openCustomPage(ref, store, reloadedPage);
                break;

            case "Close":
                player.getPageManager().setPage(ref, store, Page.None);
                break;

            default:
                break;
        }
    }
}
