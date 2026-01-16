package com.mypermissions.command.user;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.mypermissions.Main;

import javax.annotation.Nonnull;
import java.awt.Color;

public class UserInfoCommand extends CommandBase {
    private final RequiredArg<String> playerArg;

    public UserInfoCommand() {
        super("mpuser-info", "Shows user permission information");
        this.addAliases("permuser-info");
        this.playerArg = withRequiredArg("player", "Player name", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        if (!ctx.sender().hasPermission("myperms.admin")) {
            ctx.sendMessage(Message.translation("server.commands.error.noPermission").color(Color.RED));
            return;
        }

        String playerName = playerArg.get(ctx);
        
        String uuid = Main.getConfig().getUuidByUsername(playerName);
        if (uuid == null) {
            ctx.sendMessage(Message.raw("Player '" + playerName + "' not found!").color(Color.RED));
            return;
        }

        var userData = Main.getConfig().getUsers().get(uuid);
        ctx.sendMessage(Message.raw("User Info: " + playerName).color(new Color(255, 215, 0)).bold(true));
        ctx.sendMessage(Message.raw("UUID: " + uuid).color(Color.GRAY));
        
        ctx.sendMessage(Message.raw("Groups:").color(Color.YELLOW));
        if (userData.getGroups().isEmpty()) {
            ctx.sendMessage(Message.raw("  (none)").color(Color.GRAY));
        } else {
            userData.getGroups().forEach(group -> 
                ctx.sendMessage(Message.raw("  - " + group).color(Color.WHITE)));
        }
        
        ctx.sendMessage(Message.raw("Direct Permissions:").color(Color.YELLOW));
        if (userData.getPermissions().isEmpty()) {
            ctx.sendMessage(Message.raw("  (none)").color(Color.GRAY));
        } else {
            userData.getPermissions().forEach(perm -> 
                ctx.sendMessage(Message.raw("  - " + perm).color(Color.WHITE)));
        }
    }
}
