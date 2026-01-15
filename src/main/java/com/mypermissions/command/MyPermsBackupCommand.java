package com.mypermissions.command;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import javax.annotation.Nonnull;
import java.awt.Color;

// DEPRECATED: Use individual commands like mpbackup-list, mpbackup-restore
public class MyPermsBackupCommand extends CommandBase {
    public MyPermsBackupCommand() {
        super("mpbackup", "Manages config backups (deprecated)");
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        if (!ctx.sender().hasPermission("myperms.admin")) {
            ctx.sendMessage(Message.translation("server.commands.error.noPermission").color(Color.RED));
            return;
        }

        ctx.sendMessage(Message.raw("This command is deprecated! Use new commands:").color(Color.YELLOW));
        ctx.sendMessage(Message.raw("/mpbackup-list").color(Color.WHITE));
        ctx.sendMessage(Message.raw("/mpbackup-restore <backup_name>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("Type /myperms for full help").color(Color.GRAY));
    }
}
