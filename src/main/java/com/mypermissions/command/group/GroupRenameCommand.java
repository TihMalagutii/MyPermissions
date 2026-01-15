package com.mypermissions.command.group;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.mypermissions.Main;
import com.mypermissions.config.MyPermissionsConfig;

import javax.annotation.Nonnull;
import java.awt.Color;

public class GroupRenameCommand extends CommandBase {
    private final RequiredArg<String> oldNameArg;
    private final RequiredArg<String> newNameArg;

    public GroupRenameCommand() {
        super("mpgroup-rename", "Renames a group");
        this.addAliases("permgroup-rename");
        this.oldNameArg = withRequiredArg("oldName", "Current group name", ArgTypes.STRING);
        this.newNameArg = withRequiredArg("newName", "New group name", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        // Check permission
        if (!ctx.sender().hasPermission("myperms.admin")) {
            ctx.sendMessage(Message.translation("server.commands.error.noPermission").color(Color.RED));
            return;
        }

        String oldName = oldNameArg.get(ctx);
        String newName = newNameArg.get(ctx);
        MyPermissionsConfig config = Main.getConfig();

        // Check if old group exists
        if (!config.getGroups().containsKey(oldName)) {
            ctx.sendMessage(Message.raw("Group '" + oldName + "' does not exist!").color(Color.RED));
            return;
        }

        // Check if new name is already in use
        if (config.getGroups().containsKey(newName)) {
            ctx.sendMessage(Message.raw("Group '" + newName + "' already exists!").color(Color.RED));
            return;
        }

        // Get the group data
        MyPermissionsConfig.GroupData groupData = config.getGroups().get(oldName);

        // Remove old group and add with new name
        config.getGroups().remove(oldName);
        config.getGroups().put(newName, groupData);

        // Update defaultGroup if it was renamed
        if (config.getDefaultGroup().equals(oldName)) {
            config.setDefaultGroup(newName);
        }

        // Update all users that have this group
        for (MyPermissionsConfig.UserData userData : config.getUsers().values()) {
            if (userData.getGroups().contains(oldName)) {
                userData.getGroups().remove(oldName);
                userData.getGroups().add(newName);
            }
        }

        // Update parent references in other groups
        for (MyPermissionsConfig.GroupData otherGroup : config.getGroups().values()) {
            if (otherGroup.getParents().contains(oldName)) {
                otherGroup.getParents().remove(oldName);
                otherGroup.getParents().add(newName);
            }
        }

        // Clear cache and save
        Main.getPermissionManager().clearCache();
        Main.getConfigManager().save();

        ctx.sendMessage(Message.raw("[OK] Group renamed from '" + oldName + "' to '" + newName + "'!").color(Color.GREEN));
    }
}
