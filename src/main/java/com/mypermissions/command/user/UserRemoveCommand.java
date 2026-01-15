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

public class UserRemoveCommand extends CommandBase {
    private final RequiredArg<String> playerArg;
    private final RequiredArg<String> groupArg;

    public UserRemoveCommand() {
        super("mpuser-remove", "Removes user from a group");
        this.addAliases("permuser-remove");
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

        var userData = Main.getConfig().getUsers().get(uuid);
        if (!userData.getGroups().remove(groupName)) {
            ctx.sendMessage(Message.raw("User is not in this group!").color(Color.YELLOW));
            return;
        }

        Main.getConfigManager().save();
        Main.getPermissionManager().clearCache(UUID.fromString(uuid));
        ctx.sendMessage(Message.raw("[OK] User '" + playerName + "' removed from group '" + groupName + "'!").color(Color.GREEN));
    }
}
