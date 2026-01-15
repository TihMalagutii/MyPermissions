package com.mypermissions.command.user;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.mypermissions.Main;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.UUID;

public class UserAddCommand extends CommandBase {
    private final RequiredArg<String> playerArg;
    private final RequiredArg<String> groupArg;

    public UserAddCommand() {
        super("mpuser-add", "Adds user to a group");
        this.addAliases("permuser-add");
        this.playerArg = withRequiredArg("player", "Player name", ArgTypes.STRING);
        this.groupArg = withRequiredArg("group", "Group name", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        if (!ctx.sender().hasPermission("myperms.admin")) {
            ctx.sendMessage(Message.translation("server.commands.error.noPermission").color(Color.RED));
            return;
        }

        String playerName = playerArg.get(ctx);
        String groupName = groupArg.get(ctx);
        
        String uuid = Main.getConfig().getUuidByUsername(playerName);
        if (uuid == null) {
            ctx.sendMessage(Message.raw("Player '" + playerName + "' not found!").color(Color.RED));
            return;
        }

        if (!Main.getConfig().getGroups().containsKey(groupName)) {
            ctx.sendMessage(Message.raw("Group '" + groupName + "' does not exist!").color(Color.RED));
            return;
        }

        var userData = Main.getConfig().getUsers().get(uuid);
        if (userData.getGroups().contains(groupName)) {
            ctx.sendMessage(Message.raw("User already in this group!").color(Color.YELLOW));
            return;
        }

        userData.getGroups().add(groupName);
        Main.getConfigManager().save();
        Main.getPermissionManager().clearCache(UUID.fromString(uuid));
        ctx.sendMessage(Message.raw("[OK] User '" + playerName + "' added to group '" + groupName + "'!").color(Color.GREEN));
    }
}
