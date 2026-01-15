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

public class GroupCreateCommand extends CommandBase {
    private final RequiredArg<String> groupArg;

    public GroupCreateCommand() {
        super("mpgroup-create", "Creates a new permission group");
        this.addAliases("permgroup-create");
        this.groupArg = withRequiredArg("group", "Group name", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        if (!ctx.sender().hasPermission("myperms.admin")) {
            ctx.sendMessage(Message.translation("server.commands.error.noPermission").color(Color.RED));
            return;
        }

        String groupName = groupArg.get(ctx);
        
        if (Main.getConfig().getGroups().containsKey(groupName)) {
            ctx.sendMessage(Message.raw("Group '" + groupName + "' already exists!").color(Color.RED));
            return;
        }

        Main.getConfig().getGroups().put(groupName, new MyPermissionsConfig.GroupData());
        Main.getConfigManager().save();
        Main.getPermissionManager().clearAllCache();
        ctx.sendMessage(Message.raw("[OK] Group '" + groupName + "' created successfully!").color(Color.GREEN));
    }
}
