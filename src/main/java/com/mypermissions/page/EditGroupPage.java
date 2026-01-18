package com.mypermissions.page;

import java.util.ArrayList;
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

public class EditGroupPage extends InteractiveCustomUIPage<EditGroupPage.EditGroupEventData> {
    private final String originalGroupName;
    private final List<String> parents;
    private String currentName;
    private String currentPrefix;
    private String currentSuffix;
    private String currentPriority;
    private String errorMessage;

    public static class EditGroupEventData {
        public String action;
        public String name;
        public String prefix;
        public String suffix;
        public String priority;
        public String parent;
        public String removeIndex;

        public static final BuilderCodec<EditGroupEventData> CODEC = 
            ((BuilderCodec.Builder<EditGroupEventData>) ((BuilderCodec.Builder<EditGroupEventData>) ((BuilderCodec.Builder<EditGroupEventData>) ((BuilderCodec.Builder<EditGroupEventData>) ((BuilderCodec.Builder<EditGroupEventData>) ((BuilderCodec.Builder<EditGroupEventData>) ((BuilderCodec.Builder<EditGroupEventData>)
            BuilderCodec.builder(EditGroupEventData.class, EditGroupEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (o, v) -> o.action = v, (o) -> o.action)
                .add())
                .append(new KeyedCodec<>("@Name", Codec.STRING), (o, v) -> o.name = v, (o) -> o.name)
                .add())
                .append(new KeyedCodec<>("@Prefix", Codec.STRING), (o, v) -> o.prefix = v, (o) -> o.prefix)
                .add())
                .append(new KeyedCodec<>("@Suffix", Codec.STRING), (o, v) -> o.suffix = v, (o) -> o.suffix)
                .add())
                .append(new KeyedCodec<>("@Priority", Codec.STRING), (o, v) -> o.priority = v, (o) -> o.priority)
                .add())
                .append(new KeyedCodec<>("@Parent", Codec.STRING), (o, v) -> o.parent = v, (o) -> o.parent)
                .add())
                .append(new KeyedCodec<>("RemoveIndex", Codec.STRING), (o, v) -> o.removeIndex = v, (o) -> o.removeIndex)
                .add())
            .build();
    }

    public EditGroupPage(@Nonnull PlayerRef playerRef, String groupName) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, EditGroupEventData.CODEC);
        this.originalGroupName = groupName;
        this.parents = new ArrayList<>();
        
        // Load existing group data
        MyPermissionsConfig.GroupData groupData = Main.getConfig().getGroups().get(groupName);
        if (groupData != null) {
            this.currentName = groupName;
            this.currentPrefix = groupData.getPrefix() != null ? groupData.getPrefix() : "";
            this.currentSuffix = groupData.getSuffix() != null ? groupData.getSuffix() : "";
            this.currentPriority = String.valueOf(groupData.getPriority());
            if (groupData.getParents() != null) {
                this.parents.addAll(groupData.getParents());
            }
        }
    }

    @Override
    public void build(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull UICommandBuilder cmd,
        @Nonnull UIEventBuilder evt,
        @Nonnull Store<EntityStore> store
    ) {
        cmd.append("Pages/EditGroup.ui");

        // Set current values
        if (currentName != null && !currentName.isEmpty()) {
            cmd.set("#NameInput.Value", currentName);
        }
        if (currentPrefix != null && !currentPrefix.isEmpty()) {
            cmd.set("#PrefixInput.Value", currentPrefix);
        }
        if (currentSuffix != null && !currentSuffix.isEmpty()) {
            cmd.set("#SuffixInput.Value", currentSuffix);
        }
        if (currentPriority != null && !currentPriority.isEmpty()) {
            cmd.set("#PriorityInput.Value", currentPriority);
        }

        // Show error message if present
        if (errorMessage != null && !errorMessage.isEmpty()) {
            cmd.set("#Error.Text", errorMessage);
        }

        // Build parent list
        buildParentList(cmd, evt);

        // Bind add parent button
        evt.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#AddParentButton",
            new EventData()
                .append("Action", "addparent")
                .append("@Name", "#NameInput.Value")
                .append("@Prefix", "#PrefixInput.Value")
                .append("@Suffix", "#SuffixInput.Value")
                .append("@Priority", "#PriorityInput.Value")
                .append("@Parent", "#ParentInput.Value")
        );

        // Bind confirm button
        evt.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#ConfirmButton",
            new EventData()
                .append("Action", "save")
                .append("@Name", "#NameInput.Value")
                .append("@Prefix", "#PrefixInput.Value")
                .append("@Suffix", "#SuffixInput.Value")
                .append("@Priority", "#PriorityInput.Value")
        );

        // Bind cancel button
        evt.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CancelButton",
            new EventData().append("Action", "cancel")
        );
    }

    private void buildParentList(UICommandBuilder cmd, UIEventBuilder evt) {
        cmd.clear("#ParentList");

        if (parents.isEmpty()) {
            cmd.appendInline("#ParentList", "Label { Text: \"No parents added\"; Anchor: (Height: 30); Style: (FontSize: 11, TextColor: #6e7da1, HorizontalAlignment: Center, VerticalAlignment: Center); }");
            return;
        }

        int i = 0;
        for (String parent : parents) {
            String selector = "#ParentList[" + i + "]";
            cmd.append("#ParentList", "Pages/ParentItem.ui");
            cmd.set(selector + " #ParentName.Text", parent);

            evt.addEventBinding(
                CustomUIEventBindingType.Activating,
                selector + " #RemoveButton",
                new EventData()
                    .append("Action", "removeparent")
                    .append("@Name", "#NameInput.Value")
                    .append("@Prefix", "#PrefixInput.Value")
                    .append("@Suffix", "#SuffixInput.Value")
                    .append("@Priority", "#PriorityInput.Value")
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
        @Nonnull EditGroupEventData data
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if ("addparent".equals(data.action)) {
            handleAddParent(ref, store, player, data);
        } else if ("removeparent".equals(data.action)) {
            handleRemoveParent(ref, store, player, data);
        } else if ("save".equals(data.action)) {
            handleSave(ref, store, player, data);
        } else if ("cancel".equals(data.action)) {
            handleCancel(ref, store, player);
        }
    }

    private void handleAddParent(Ref<EntityStore> ref, Store<EntityStore> store, Player player, EditGroupEventData data) {
        if (data.parent == null || data.parent.trim().isEmpty()) {
            showError(ref, store, player, data, "Please enter a parent group name");
            return;
        }

        String parentName = data.parent.trim();

        // Check if parent group exists
        if (!Main.getConfig().getGroups().containsKey(parentName)) {
            showError(ref, store, player, data, "Parent group '" + parentName + "' does not exist");
            return;
        }

        // Check if already added
        if (parents.contains(parentName)) {
            showError(ref, store, player, data, "Parent '" + parentName + "' is already added");
            return;
        }

        // Prevent circular dependency
        if (parentName.equals(originalGroupName)) {
            showError(ref, store, player, data, "Cannot add group as its own parent");
            return;
        }

        parents.add(parentName);

        // Refresh page preserving input values
        EditGroupPage refreshed = new EditGroupPage(playerRef, originalGroupName);
        refreshed.parents.clear();
        refreshed.parents.addAll(this.parents);
        refreshed.currentName = data.name;
        refreshed.currentPrefix = data.prefix;
        refreshed.currentSuffix = data.suffix;
        refreshed.currentPriority = data.priority;
        player.getPageManager().openCustomPage(ref, store, refreshed);
    }

    private void handleRemoveParent(Ref<EntityStore> ref, Store<EntityStore> store, Player player, EditGroupEventData data) {
        if (data.removeIndex == null) {
            return;
        }

        try {
            int index = Integer.parseInt(data.removeIndex);
            if (index >= 0 && index < parents.size()) {
                parents.remove(index);

                // Refresh page preserving input values
                EditGroupPage refreshed = new EditGroupPage(playerRef, originalGroupName);
                refreshed.parents.clear();
                refreshed.parents.addAll(this.parents);
                refreshed.currentName = data.name;
                refreshed.currentPrefix = data.prefix;
                refreshed.currentSuffix = data.suffix;
                refreshed.currentPriority = data.priority;
                player.getPageManager().openCustomPage(ref, store, refreshed);
            }
        } catch (NumberFormatException e) {
            // Invalid index
        }
    }

    private void handleSave(Ref<EntityStore> ref, Store<EntityStore> store, Player player, EditGroupEventData data) {
        // Validate name
        if (data.name == null || data.name.trim().isEmpty()) {
            showError(ref, store, player, data, "Group name is required");
            return;
        }

        String newGroupName = data.name.trim();

        // Check if renaming to existing group (but not itself)
        if (!newGroupName.equals(originalGroupName) && Main.getConfig().getGroups().containsKey(newGroupName)) {
            showError(ref, store, player, data, "Group '" + newGroupName + "' already exists");
            return;
        }

        // Get existing group data
        MyPermissionsConfig.GroupData groupData = Main.getConfig().getGroups().get(originalGroupName);
        if (groupData == null) {
            return;
        }

        // Update group data
        if (data.prefix != null && !data.prefix.trim().isEmpty()) {
            groupData.setPrefix(data.prefix.trim());
        } else {
            groupData.setPrefix("");
        }

        if (data.suffix != null && !data.suffix.trim().isEmpty()) {
            groupData.setSuffix(data.suffix.trim());
        } else {
            groupData.setSuffix("");
        }

        if (data.priority != null && !data.priority.trim().isEmpty()) {
            try {
                int priority = Integer.parseInt(data.priority.trim());
                groupData.setPriority(priority);
            } catch (NumberFormatException e) {
                // Keep existing priority
            }
        }

        groupData.setParents(new ArrayList<>(parents));

        // If group name changed, rename it
        if (!newGroupName.equals(originalGroupName)) {
            Main.getConfig().getGroups().remove(originalGroupName);
            Main.getConfig().getGroups().put(newGroupName, groupData);

            // Update all users that have this group
            Main.getConfig().getUsers().values().forEach(userData -> {
                if (userData.getGroups().remove(originalGroupName)) {
                    userData.getGroups().add(newGroupName);
                }
            });

            // Update all groups that have this as parent
            for (MyPermissionsConfig.GroupData otherGroup : Main.getConfig().getGroups().values()) {
                if (otherGroup.getParents().remove(originalGroupName)) {
                    otherGroup.getParents().add(newGroupName);
                }
            }
        }

        // Save config
        Main.getConfigManager().save();

        // Clear cache
        Main.getPermissionManager().clearAllCache();

        // Go back to main page
        MainPage mainPage = new MainPage(playerRef);
        player.getPageManager().openCustomPage(ref, store, mainPage);
    }

    private void showError(Ref<EntityStore> ref, Store<EntityStore> store, Player player, EditGroupEventData data, String errorMessage) {
        // Create new page with error message
        EditGroupPage refreshed = new EditGroupPage(playerRef, originalGroupName);
        refreshed.parents.clear();
        refreshed.parents.addAll(this.parents);
        refreshed.currentName = data.name;
        refreshed.currentPrefix = data.prefix;
        refreshed.currentSuffix = data.suffix;
        refreshed.currentPriority = data.priority;
        refreshed.errorMessage = errorMessage;
        
        player.getPageManager().openCustomPage(ref, store, refreshed);
    }

    private void handleCancel(Ref<EntityStore> ref, Store<EntityStore> store, Player player) {
        // Go back to main page
        MainPage mainPage = new MainPage(playerRef);
        player.getPageManager().openCustomPage(ref, store, mainPage);
    }
}
