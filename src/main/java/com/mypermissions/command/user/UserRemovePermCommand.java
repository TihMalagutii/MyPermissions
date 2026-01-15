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

public class UserRemovePermCommand extends CommandBase {
    private final RequiredArg<String> playerArg;
    private final RequiredArg<String> permArg;

    public UserRemovePermCommand() {
        super("mpuser-removeperm", "Removes direct permission from user");
        this.addAliases("permuser-removeperm");
        this.playerArg = withRequiredArg("player", "Player name", ArgTypes.STRING);
        this.permArg = withRequiredArg("permission", "Permission node", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        if (!ctx.sender().hasPermission("myperms.admin")) {
            ctx.sendMessage(Message.translation("server.commands.error.noPermission").color(Color.RED));
            return;
        }

        String playerName = playerArg.get(ctx);
        String permission = permArg.get(ctx);
        
        String uuid = Main.getConfig().getUuidByUsername(playerName);
        if (uuid == null) {
            ctx.sendMessage(Message.raw("Player '" + playerName + "' not found!").color(Color.RED));
            return;
        }

        var userData = Main.getConfig().getUsers().get(uuid);
        if (!userData.getPermissions().remove(permission)) {
            ctx.sendMessage(Message.raw("User doesn't have this direct permission!").color(Color.YELLOW));
            return;
        }

        Main.getConfigManager().save();
        Main.getPermissionManager().clearCache(UUID.fromString(uuid));
        ctx.sendMessage(Message.raw("[OK] Permission '" + permission + "' removed from user '" + playerName + "'!").color(Color.GREEN));
    }
}
