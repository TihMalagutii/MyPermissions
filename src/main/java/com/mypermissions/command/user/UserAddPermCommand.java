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

public class UserAddPermCommand extends CommandBase {
    private final RequiredArg<String> playerArg;
    private final RequiredArg<String> permArg;

    public UserAddPermCommand() {
        super("mpuser-addperm", "Adds direct permission to user");
        this.addAliases("permuser-addperm");
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
        if (userData.getPermissions().contains(permission)) {
            ctx.sendMessage(Message.raw("User already has this permission!").color(Color.YELLOW));
            return;
        }

        userData.getPermissions().add(permission);
        Main.getConfigManager().save();
        Main.getPermissionManager().clearCache(UUID.fromString(uuid));
        ctx.sendMessage(Message.raw("[OK] Permission '" + permission + "' added to user '" + playerName + "'!").color(Color.GREEN));
    }
}
