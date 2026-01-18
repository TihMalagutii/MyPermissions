package com.mypermissions.page;

import java.util.List;

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

public class PermissionsPage extends InteractiveCustomUIPage<PermissionsPage.PermissionsEventData> {
    private final String groupName;
    private String errorMessage;

    public static class PermissionsEventData {
        public String action;
        public String permission;
        public String removeIndex;

        public static final BuilderCodec<PermissionsEventData> CODEC = 
            ((BuilderCodec.Builder<PermissionsEventData>) ((BuilderCodec.Builder<PermissionsEventData>) ((BuilderCodec.Builder<PermissionsEventData>)
            BuilderCodec.builder(PermissionsEventData.class, PermissionsEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (o, v) -> o.action = v, (o) -> o.action)
                .add())
                .append(new KeyedCodec<>("@Permission", Codec.STRING), (o, v) -> o.permission = v, (o) -> o.permission)
                .add())
                .append(new KeyedCodec<>("RemoveIndex", Codec.STRING), (o, v) -> o.removeIndex = v, (o) -> o.removeIndex)
                .add())
            .build();
    }

    public PermissionsPage(@Nonnull PlayerRef playerRef, String groupName) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, PermissionsEventData.CODEC);
        this.groupName = groupName;
    }

    @Override
    public void build(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull UICommandBuilder cmd,
        @Nonnull UIEventBuilder evt,
        @Nonnull Store<EntityStore> store
    ) {
        cmd.append("Pages/PermissionsPage.ui");

        // Set title with group name
        cmd.set("#GroupTitle.Text", "PERMISSIONS - " + groupName);

        // Show error message if present
        if (errorMessage != null && !errorMessage.isEmpty()) {
            cmd.set("#Error.Text", errorMessage);
        }

        // Build permission list
        buildPermissionList(cmd, evt);

        // Bind add permission button
        evt.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#AddPermissionButton",
            new EventData()
                .append("Action", "add")
                .append("@Permission", "#PermissionInput.Value")
        );

        // Bind back button
        evt.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#BackButton",
            new EventData().append("Action", "back")
        );
    }

    private void buildPermissionList(UICommandBuilder cmd, UIEventBuilder evt) {
        cmd.clear("#PermissionList");

        MyPermissionsConfig.GroupData groupData = Main.getConfig().getGroups().get(groupName);
        if (groupData == null || groupData.getPermissions().isEmpty()) {
            cmd.appendInline("#PermissionList", "Label { Text: \"No permissions in this group\"; Anchor: (Height: 40); Style: (FontSize: 14, TextColor: #6e7da1, HorizontalAlignment: Center, VerticalAlignment: Center); }");
            return;
        }

        List<String> permissions = groupData.getPermissions();

        int i = 0;
        for (String permission : permissions) {
            String selector = "#PermissionList[" + i + "]";
            cmd.append("#PermissionList", "Pages/PermissionItem.ui");
            cmd.set(selector + " #PermissionName.Text", permission);

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
        @Nonnull PermissionsEventData data
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

    private void handleAdd(Ref<EntityStore> ref, Store<EntityStore> store, Player player, PermissionsEventData data) {
        if (data.permission == null || data.permission.trim().isEmpty()) {
            showError(ref, store, player, "Please enter a permission");
            return;
        }

        String permission = data.permission.trim();

        // Check if group exists
        MyPermissionsConfig.GroupData groupData = Main.getConfig().getGroups().get(groupName);
        if (groupData == null) {
            return;
        }

        // Check if permission already exists
        if (groupData.getPermissions().contains(permission)) {
            showError(ref, store, player, "Permission '" + permission + "' already exists in this group");
            return;
        }

        // Add permission
        groupData.getPermissions().add(permission);

        // Save config
        Main.getConfigManager().save();

        // Clear cache
        Main.getPermissionManager().clearAllCache();

        // Refresh page
        PermissionsPage refreshed = new PermissionsPage(playerRef, groupName);
        player.getPageManager().openCustomPage(ref, store, refreshed);
    }

    private void showError(Ref<EntityStore> ref, Store<EntityStore> store, Player player, String errorMessage) {
        // Create new page with error message
        PermissionsPage refreshed = new PermissionsPage(playerRef, groupName);
        refreshed.errorMessage = errorMessage;
        
        player.getPageManager().openCustomPage(ref, store, refreshed);
    }

    private void handleRemove(Ref<EntityStore> ref, Store<EntityStore> store, Player player, PermissionsEventData data) {
        if (data.removeIndex == null) {
            return;
        }

        try {
            int index = Integer.parseInt(data.removeIndex);

            MyPermissionsConfig.GroupData groupData = Main.getConfig().getGroups().get(groupName);
            if (groupData == null) {
                return;
            }

            List<String> permissions = groupData.getPermissions();
            if (index >= 0 && index < permissions.size()) {
                // Remove permission
                permissions.remove(index);

                // Save config
                Main.getConfigManager().save();

                // Clear cache
                Main.getPermissionManager().clearAllCache();

                // Refresh page
                PermissionsPage refreshed = new PermissionsPage(playerRef, groupName);
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
