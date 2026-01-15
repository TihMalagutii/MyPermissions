package com.mypermissions.command.group;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.mypermissions.Main;

import javax.annotation.Nonnull;
import java.awt.Color;

public class GroupAddPermCommand extends CommandBase {
    private final RequiredArg<String> groupArg;
    private final RequiredArg<String> permArg;

    public GroupAddPermCommand() {
        super("mpgroup-addperm", "Adds permission to a group");
        this.addAliases("permgroup-addperm");
        this.groupArg = withRequiredArg("group", "Group name", ArgTypes.STRING);
        this.permArg = withRequiredArg("permission", "Permission node", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        if (!ctx.sender().hasPermission("myperms.admin")) {
            ctx.sendMessage(Message.translation("server.commands.error.noPermission").color(Color.RED));
            return;
        }

        String groupName = groupArg.get(ctx);
        String permission = permArg.get(ctx);
        
        var groupData = Main.getConfig().getGroups().get(groupName);
        if (groupData == null) {
            ctx.sendMessage(Message.raw("Group '" + groupName + "' does not exist!").color(Color.RED));
            return;
        }

        if (groupData.getPermissions().contains(permission)) {
            ctx.sendMessage(Message.raw("Group already has this permission!").color(Color.YELLOW));
            return;
        }

        groupData.getPermissions().add(permission);
        Main.getConfigManager().save();
        Main.getPermissionManager().clearAllCache();
        ctx.sendMessage(Message.raw("[OK] Permission '" + permission + "' added to group '" + groupName + "'!").color(Color.GREEN));
    }
}
