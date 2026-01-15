package com.mypermissions.command.group;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.mypermissions.Main;

import javax.annotation.Nonnull;
import java.awt.Color;

public class GroupDeleteCommand extends CommandBase {
    private final RequiredArg<String> groupArg;

    public GroupDeleteCommand() {
        super("mpgroup-delete", "Deletes a permission group");
        this.addAliases("permgroup-delete");
        this.groupArg = withRequiredArg("group", "Group name", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        if (!ctx.sender().hasPermission("myperms.admin")) {
            ctx.sendMessage(Message.translation("server.commands.error.noPermission").color(Color.RED));
            return;
        }

        String groupName = groupArg.get(ctx);
        
        if (!Main.getConfig().getGroups().containsKey(groupName)) {
            ctx.sendMessage(Message.raw("Group '" + groupName + "' does not exist!").color(Color.RED));
            return;
        }

        if (groupName.equals(Main.getConfig().getDefaultGroup())) {
            ctx.sendMessage(Message.raw("Cannot delete the default group!").color(Color.RED));
            return;
        }

        Main.getConfig().getGroups().remove(groupName);
        Main.getConfigManager().save();
        Main.getPermissionManager().clearAllCache();
        ctx.sendMessage(Message.raw("[OK] Group '" + groupName + "' deleted successfully!").color(Color.GREEN));
    }
}
