package com.mypermissions.command;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import javax.annotation.Nonnull;
import java.awt.Color;

// DEPRECATED: Use individual commands like mpuser-add, mpuser-info, etc.
public class MyPermsUserCommand extends CommandBase {
    public MyPermsUserCommand() {
        super("mpuser", "Manages user permissions (deprecated)");
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        if (!ctx.sender().hasPermission("myperms.admin")) {
            ctx.sendMessage(Message.translation("server.commands.error.noPermission").color(Color.RED));
            return;
        }

        ctx.sendMessage(Message.raw("This command is deprecated! Use new commands:").color(Color.YELLOW));
        ctx.sendMessage(Message.raw("/mpuser-add <player> <group>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("/mpuser-remove <player> <group>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("/mpuser-info <player>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("/mpuser-addperm <player> <perm>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("/mpuser-removeperm <player> <perm>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("Type /myperms for full help").color(Color.GRAY));
    }
}
