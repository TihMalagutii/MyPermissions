package com.mypermissions.command.group;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.mypermissions.Main;

import javax.annotation.Nonnull;
import java.awt.Color;

public class GroupSetParentCommand extends CommandBase {
    private final RequiredArg<String> groupArg;
    private final RequiredArg<String> parentArg;

    public GroupSetParentCommand() {
        super("mpgroup-setparent", "Sets parent group for inheritance");
        this.addAliases("permgroup-setparent");
        this.groupArg = withRequiredArg("group", "Group name", ArgTypes.STRING);
        this.parentArg = withRequiredArg("parent", "Parent group name", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        if (!ctx.sender().hasPermission("myperms.admin")) {
            ctx.sendMessage(Message.translation("server.commands.error.noPermission").color(Color.RED));
            return;
        }

        String groupName = groupArg.get(ctx);
        String parentName = parentArg.get(ctx);
        
        var groupData = Main.getConfig().getGroups().get(groupName);
        if (groupData == null) {
            ctx.sendMessage(Message.raw("Group '" + groupName + "' does not exist!").color(Color.RED));
            return;
        }

        if (!Main.getConfig().getGroups().containsKey(parentName)) {
            ctx.sendMessage(Message.raw("Parent group '" + parentName + "' does not exist!").color(Color.RED));
            return;
        }

        if (groupName.equals(parentName)) {
            ctx.sendMessage(Message.raw("A group cannot be its own parent!").color(Color.RED));
            return;
        }

        if (!groupData.getParents().contains(parentName)) {
            groupData.getParents().add(parentName);
        }
        
        Main.getConfigManager().save();
        Main.getPermissionManager().clearAllCache();
        ctx.sendMessage(Message.raw("[OK] Parent '" + parentName + "' set for group '" + groupName + "'!").color(Color.GREEN));
    }
}
