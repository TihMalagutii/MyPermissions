package com.mypermissions.command.group;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.mypermissions.Main;

import javax.annotation.Nonnull;
import java.awt.Color;

public class GroupInfoCommand extends CommandBase {
    private final RequiredArg<String> groupArg;

    public GroupInfoCommand() {
        super("mpgroup-info", "Shows group information");
        this.addAliases("permgroup-info");
        this.groupArg = withRequiredArg("group", "Group name", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        if (!ctx.sender().hasPermission("myperms.admin")) {
            ctx.sendMessage(Message.translation("server.commands.error.noPermission").color(Color.RED));
            return;
        }

        String groupName = groupArg.get(ctx);
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
}
