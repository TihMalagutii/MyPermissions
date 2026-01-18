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

public class NewGroupPage extends InteractiveCustomUIPage<NewGroupPage.NewGroupEventData> {
    private final List<String> parents;
    private String currentName;
    private String currentPrefix;
    private String currentSuffix;
    private String currentPriority;
    private String errorMessage;

    public static class NewGroupEventData {
        public String action;
        public String name;
        public String prefix;
        public String suffix;
        public String priority;
        public String parent;
        public String removeIndex;

        public static final BuilderCodec<NewGroupEventData> CODEC = 
            ((BuilderCodec.Builder<NewGroupEventData>) ((BuilderCodec.Builder<NewGroupEventData>) ((BuilderCodec.Builder<NewGroupEventData>) ((BuilderCodec.Builder<NewGroupEventData>) ((BuilderCodec.Builder<NewGroupEventData>) ((BuilderCodec.Builder<NewGroupEventData>) ((BuilderCodec.Builder<NewGroupEventData>)
            BuilderCodec.builder(NewGroupEventData.class, NewGroupEventData::new)
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

    public NewGroupPage(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, NewGroupEventData.CODEC);
        this.parents = new ArrayList<>();
    }

    @Override
    public void build(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull UICommandBuilder cmd,
        @Nonnull UIEventBuilder evt,
        @Nonnull Store<EntityStore> store
    ) {
        cmd.append("Pages/NewGroup.ui");

        // Restore input values if they exist
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
                .append("Action", "create")
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
        @Nonnull NewGroupEventData data
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if ("addparent".equals(data.action)) {
            handleAddParent(ref, store, player, data);
        } else if ("removeparent".equals(data.action)) {
            handleRemoveParent(ref, store, player, data);
        } else if ("create".equals(data.action)) {
            handleCreate(ref, store, player, data);
        } else if ("cancel".equals(data.action)) {
            handleCancel(ref, store, player);
        }
    }

    private void handleAddParent(Ref<EntityStore> ref, Store<EntityStore> store, Player player, NewGroupEventData data) {
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

        parents.add(parentName);

        // Refresh page preserving input values
        NewGroupPage refreshed = new NewGroupPage(playerRef);
        refreshed.parents.addAll(this.parents);
        refreshed.currentName = data.name;
        refreshed.currentPrefix = data.prefix;
        refreshed.currentSuffix = data.suffix;
        refreshed.currentPriority = data.priority;
        player.getPageManager().openCustomPage(ref, store, refreshed);
    }

    private void handleRemoveParent(Ref<EntityStore> ref, Store<EntityStore> store, Player player, NewGroupEventData data) {
        if (data.removeIndex == null) {
            return;
        }

        try {
            int index = Integer.parseInt(data.removeIndex);
            if (index >= 0 && index < parents.size()) {
                parents.remove(index);

                // Refresh page preserving input values
                NewGroupPage refreshed = new NewGroupPage(playerRef);
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

    private void handleCreate(Ref<EntityStore> ref, Store<EntityStore> store, Player player, NewGroupEventData data) {
        // Validate name
        if (data.name == null || data.name.trim().isEmpty()) {
            showError(ref, store, player, data, "Group name is required");
            return;
        }

        String groupName = data.name.trim();

        // Check if group already exists
        if (Main.getConfig().getGroups().containsKey(groupName)) {
            showError(ref, store, player, data, "Group '" + groupName + "' already exists");
            return;
        }

        // Create new group
        MyPermissionsConfig.GroupData groupData = new MyPermissionsConfig.GroupData();

        // Set prefix (keep & as is)
        if (data.prefix != null && !data.prefix.trim().isEmpty()) {
            groupData.setPrefix(data.prefix.trim());
        }

        // Set suffix (keep & as is)
        if (data.suffix != null && !data.suffix.trim().isEmpty()) {
            groupData.setSuffix(data.suffix.trim());
        }

        // Set priority
        if (data.priority != null && !data.priority.trim().isEmpty()) {
            try {
                int priority = Integer.parseInt(data.priority.trim());
                groupData.setPriority(priority);
            } catch (NumberFormatException e) {
                // Invalid priority, use default 0
            }
        }

        // Set parents
        groupData.setParents(new ArrayList<>(parents));

        // Add to config
        Main.getConfig().getGroups().put(groupName, groupData);

        // Save config
        Main.getConfigManager().save();

        // Clear cache
        Main.getPermissionManager().clearAllCache();

        // Go back to main page
        MainPage mainPage = new MainPage(playerRef);
        player.getPageManager().openCustomPage(ref, store, mainPage);
    }

    private void showError(Ref<EntityStore> ref, Store<EntityStore> store, Player player, NewGroupEventData data, String errorMessage) {
        // Create new page with error message
        NewGroupPage refreshed = new NewGroupPage(playerRef);
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
