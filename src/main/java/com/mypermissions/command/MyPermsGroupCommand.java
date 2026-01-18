package com.mypermissions.command;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.mypermissions.Main;
import com.mypermissions.config.MyPermissionsConfig;

import javax.annotation.Nonnull;
import java.awt.Color;

public class MyPermsGroupCommand extends CommandBase {
    private final OptionalArg<String> arg1, arg2, arg3;

    public MyPermsGroupCommand() {
        super("mpgroup", "Manages permission groups");
        this.addAliases("mypermsgroup", "permgroup");
        
        this.arg1 = withOptionalArg("arg1", "First argument", ArgTypes.STRING);
        this.arg2 = withOptionalArg("arg2", "Second argument", ArgTypes.STRING);
        this.arg3 = withOptionalArg("arg3", "Third argument", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        // Check permission
        if (!ctx.sender().hasPermission("myperms.admin")) {
            ctx.sendMessage(Message.translation("server.commands.error.noPermission").color(Color.RED));
            return;
        }

        if (!ctx.provided(arg1)) {
            sendHelp(ctx);
            return;
        }

        String action = arg1.get(ctx).toLowerCase();

        switch (action) {
            case "list" -> handleList(ctx);
            case "create", "delete", "info" -> {
                if (!ctx.provided(arg2)) {
                    ctx.sendMessage(Message.raw("Usage: /mpgroup " + action + " <group>").color(Color.RED));
                    return;
                }
                String groupName = arg2.get(ctx);
                switch (action) {
                    case "create" -> handleCreate(ctx, groupName);
                    case "delete" -> handleDelete(ctx, groupName);
                    case "info" -> handleInfo(ctx, groupName);
                }
            }
            case "addperm", "removeperm", "setparent", "setpriority" -> {
                if (!ctx.provided(arg2) || !ctx.provided(arg3)) {
                    ctx.sendMessage(Message.raw("Usage: /mpgroup " + action + " <group> <value>").color(Color.RED));
                    return;
                }
                String groupName = arg2.get(ctx);
                String value = arg3.get(ctx);
                switch (action) {
                    case "addperm" -> handleAddPerm(ctx, groupName, value);
                    case "removeperm" -> handleRemovePerm(ctx, groupName, value);
                    case "setparent" -> handleSetParent(ctx, groupName, value);
                    case "setpriority" -> handleSetPriority(ctx, groupName, value);
                }
            }
            default -> sendHelp(ctx);
        }
    }

    private void sendHelp(@Nonnull CommandContext ctx) {
        ctx.sendMessage(Message.raw("This command is deprecated! Use new commands:").color(Color.YELLOW));
        ctx.sendMessage(Message.raw("/mpgroup-list").color(Color.WHITE));
        ctx.sendMessage(Message.raw("/mpgroup-create <name>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("/mpgroup-delete <name>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("/mpgroup-info <name>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("/mpgroup-addperm <group> <permission>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("/mpgroup-removeperm <group> <permission>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("/mpgroup-setparent <group> <parent>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("/mpgroup-setpriority <group> <number>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("Type /myperms for full help").color(Color.GRAY));
    }

    private void handleCreate(@Nonnull CommandContext ctx, @Nonnull String groupName) {
        if (Main.getConfig().getGroups().containsKey(groupName)) {
            ctx.sendMessage(Message.raw("Group '" + groupName + "' already exists!").color(Color.RED));
            return;
        }
        Main.getConfig().getGroups().put(groupName, new MyPermissionsConfig.GroupData());
        Main.getConfigManager().save();
        ctx.sendMessage(Message.raw("Group '" + groupName + "' created successfully!").color(Color.GREEN));
    }

    private void handleDelete(@Nonnull CommandContext ctx, @Nonnull String groupName) {
        if (!Main.getConfig().getGroups().containsKey(groupName)) {
            ctx.sendMessage(Message.raw("Group '" + groupName + "' does not exist!").color(Color.RED));
            return;
        }
        
        // Get default group name
        String defaultGroup = Main.getConfig().getDefaultGroup();
        
        // Count how many users will be affected
        int affectedUsers = 0;
        
        // Remove group from all users and add default group
        for (var userData : Main.getConfig().getUsers().values()) {
            if (userData.getGroups().remove(groupName)) {
                affectedUsers++;
                // User had this group, add default if not already present
                if (!userData.getGroups().contains(defaultGroup)) {
                    userData.getGroups().add(defaultGroup);
                }
            }
        }
        
        // Remove this group from all parent lists
        for (var group : Main.getConfig().getGroups().values()) {
            group.getParents().remove(groupName);
        }
        
        // Delete the group
        Main.getConfig().getGroups().remove(groupName);
        
        // Clear permission cache
        Main.getPermissionManager().clearAllCache();
        
        // Save config
        Main.getConfigManager().save();
        
        ctx.sendMessage(Message.raw("Group '" + groupName + "' deleted successfully!").color(Color.GREEN));
        if (affectedUsers > 0) {
            ctx.sendMessage(Message.raw(affectedUsers + " user(s) moved to '" + defaultGroup + "' group").color(Color.YELLOW));
        }
    }

    private void handleList(@Nonnull CommandContext ctx) {
        var groups = Main.getConfig().getGroups();
        if (groups.isEmpty()) {
            ctx.sendMessage(Message.raw("No groups found!").color(Color.YELLOW));
            return;
        }
        ctx.sendMessage(Message.raw("Groups (" + groups.size() + "):").color(new Color(255, 215, 0)).bold(true));
        groups.forEach((name, data) -> {
            ctx.sendMessage(Message.raw("  - " + name).color(Color.YELLOW)
                    .insert(Message.raw(" (" + data.getPermissions().size() + " perms)").color(Color.GRAY)));
        });
    }

    private void handleInfo(@Nonnull CommandContext ctx, @Nonnull String groupName) {
        var groupData = Main.getConfig().getGroups().get(groupName);
        if (groupData == null) {
            ctx.sendMessage(Message.raw("Group '" + groupName + "' does not exist!").color(Color.RED));
            return;
        }
        ctx.sendMessage(Message.raw("Group Info: " + groupName).color(new Color(255, 215, 0)).bold(true));
        ctx.sendMessage(Message.raw("Priority: " + groupData.getPriority()).color(new Color(255, 165, 0)));
        ctx.sendMessage(Message.raw("Permissions:").color(Color.YELLOW));
        if (groupData.getPermissions().isEmpty()) {
            ctx.sendMessage(Message.raw("  (none)").color(Color.GRAY));
        } else {
            groupData.getPermissions().forEach(perm -> 
                ctx.sendMessage(Message.raw("  - " + perm).color(Color.WHITE)));
        }
        if (!groupData.getParents().isEmpty()) {
            ctx.sendMessage(Message.raw("Parents:").color(Color.YELLOW));
            groupData.getParents().forEach(parent -> 
                ctx.sendMessage(Message.raw("  - " + parent).color(Color.WHITE)));
        }
        if (groupData.getPrefix() != null && !groupData.getPrefix().isEmpty()) {
            ctx.sendMessage(Message.raw("Prefix: " + groupData.getPrefix()).color(new Color(0, 255, 255)));
        }
        if (groupData.getSuffix() != null && !groupData.getSuffix().isEmpty()) {
            ctx.sendMessage(Message.raw("Suffix: " + groupData.getSuffix()).color(new Color(0, 255, 255)));
        }
    }

    private void handleSetPriority(@Nonnull CommandContext ctx, @Nonnull String groupName, @Nonnull String priorityStr) {
        var groupData = Main.getConfig().getGroups().get(groupName);
        if (groupData == null) {
            ctx.sendMessage(Message.raw("Group '" + groupName + "' does not exist!").color(Color.RED));
            return;
        }

        try {
            int priority = Integer.parseInt(priorityStr);
            groupData.setPriority(priority);
            Main.getConfigManager().save();
            Main.getPermissionManager().clearAllCache();
            ctx.sendMessage(Message.raw("[OK] Priority of group '" + groupName + "' set to " + priority).color(Color.GREEN));
        } catch (NumberFormatException e) {
            ctx.sendMessage(Message.raw("Invalid number! Usage: /mpgroup setpriority <group> <number>").color(Color.RED));
        }
    }

    private void handleAddPerm(@Nonnull CommandContext ctx, @Nonnull String groupName, @Nonnull String permission) {
        var groupData = Main.getConfig().getGroups().get(groupName);
        if (groupData == null) {
            ctx.sendMessage(Message.raw("Group '" + groupName + "' does not exist!").color(Color.RED));
            return;
        }
        if (groupData.getPermissions().contains(permission)) {
            ctx.sendMessage(Message.raw("Group '" + groupName + "' already has permission '" + permission + "'!").color(Color.YELLOW));
            return;
        }
        groupData.getPermissions().add(permission);
        Main.getConfigManager().save();
        Main.getPermissionManager().clearCache();
        ctx.sendMessage(Message.raw("Permission '" + permission + "' added to group '" + groupName + "'!").color(Color.GREEN));
    }

    private void handleRemovePerm(@Nonnull CommandContext ctx, @Nonnull String groupName, @Nonnull String permission) {
        var groupData = Main.getConfig().getGroups().get(groupName);
        if (groupData == null) {
            ctx.sendMessage(Message.raw("Group '" + groupName + "' does not exist!").color(Color.RED));
            return;
        }
        if (!groupData.getPermissions().remove(permission)) {
            ctx.sendMessage(Message.raw("Group '" + groupName + "' does not have permission '" + permission + "'!").color(Color.YELLOW));
            return;
        }
        Main.getConfigManager().save();
        Main.getPermissionManager().clearCache();
        ctx.sendMessage(Message.raw("Permission '" + permission + "' removed from group '" + groupName + "'!").color(Color.GREEN));
    }

    private void handleSetParent(@Nonnull CommandContext ctx, @Nonnull String groupName, @Nonnull String parentName) {
        var groupData = Main.getConfig().getGroups().get(groupName);
        if (groupData == null) {
            ctx.sendMessage(Message.raw("Group '" + groupName + "' does not exist!").color(Color.RED));
            return;
        }
        if (!Main.getConfig().getGroups().containsKey(parentName)) {
            ctx.sendMessage(Message.raw("Parent group '" + parentName + "' does not exist!").color(Color.RED));
            return;
        }
        if (!groupData.getParents().contains(parentName)) {
            groupData.getParents().add(parentName);
        }
        Main.getConfigManager().save();
        Main.getPermissionManager().clearCache();
        ctx.sendMessage(Message.raw("Parent '" + parentName + "' set for group '" + groupName + "'!").color(Color.GREEN));
    }
}
