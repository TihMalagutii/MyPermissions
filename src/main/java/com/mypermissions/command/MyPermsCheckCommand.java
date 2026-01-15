package com.mypermissions.command;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.mypermissions.Main;

import javax.annotation.Nonnull;
import java.awt.Color;

public class MyPermsCheckCommand extends CommandBase {
    private final RequiredArg<String> playerArg;
    private final RequiredArg<String> permissionArg;

    public MyPermsCheckCommand() {
        super("mpcheck", "Tests if a user has a specific permission");
        this.addAliases("mypermscheck", "permcheck");
        
        this.playerArg = withRequiredArg("player", "Player name", ArgTypes.STRING);
        this.permissionArg = withRequiredArg("permission", "Permission node", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        // Check permission
        if (!ctx.sender().hasPermission("myperms.admin")) {
            ctx.sendMessage(Message.translation("server.commands.error.noPermission").color(Color.RED));
            return;
        }

        String playerName = playerArg.get(ctx);
        String permission = permissionArg.get(ctx);
        
        // Search UUID by username
        String uuid = Main.getConfig().getUuidByUsername(playerName);
        if (uuid == null) {
            ctx.sendMessage(Message.raw("Player '" + playerName + "' not found!").color(Color.RED));
            return;
        }
        
        boolean hasPermission = Main.getPermissionManager().hasPermission(uuid, permission);

        if (hasPermission) {
            ctx.sendMessage(Message.raw("Player '" + playerName + "' HAS permission '" + permission + "'").color(Color.GREEN));
        } else {
            ctx.sendMessage(Message.raw("Player '" + playerName + "' DOES NOT have permission '" + permission + "'").color(Color.RED));
        }
    }
}
