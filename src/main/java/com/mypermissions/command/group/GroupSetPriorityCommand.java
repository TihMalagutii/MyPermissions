package com.mypermissions.command.group;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.mypermissions.Main;

import javax.annotation.Nonnull;
import java.awt.Color;

public class GroupSetPriorityCommand extends CommandBase {
    private final RequiredArg<String> groupArg;
    private final RequiredArg<Integer> priorityArg;

    public GroupSetPriorityCommand() {
        super("mpgroup-setpriority", "Sets group priority for prefix/suffix selection");
        this.addAliases("permgroup-setpriority");
        this.groupArg = withRequiredArg("group", "Group name", ArgTypes.STRING);
        this.priorityArg = withRequiredArg("priority", "Priority number (higher = more important)", ArgTypes.INTEGER);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        if (!ctx.sender().hasPermission("myperms.admin")) {
            ctx.sendMessage(Message.translation("server.commands.error.noPermission").color(Color.RED));
            return;
        }

        String groupName = groupArg.get(ctx);
        int priority = priorityArg.get(ctx);
        
        var groupData = Main.getConfig().getGroups().get(groupName);
        if (groupData == null) {
            ctx.sendMessage(Message.raw("Group '" + groupName + "' does not exist!").color(Color.RED));
            return;
        }

        groupData.setPriority(priority);
        Main.getConfigManager().save();
        Main.getPermissionManager().clearAllCache();
        ctx.sendMessage(Message.raw("[OK] Priority of group '" + groupName + "' set to " + priority).color(Color.GREEN));
    }
}
