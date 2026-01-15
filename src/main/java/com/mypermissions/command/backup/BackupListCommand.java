package com.mypermissions.command.backup;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.mypermissions.Main;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.List;

public class BackupListCommand extends CommandBase {
    public BackupListCommand() {
        super("mpbackup-list", "Lists all available backups");
        this.addAliases("permbackup-list");
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        if (!ctx.sender().hasPermission("myperms.admin")) {
            ctx.sendMessage(Message.translation("server.commands.error.noPermission").color(Color.RED));
            return;
        }

        List<String> backups = Main.getConfigManager().listBackups();
        
        if (backups.isEmpty()) {
            ctx.sendMessage(Message.raw("No backups found!").color(Color.YELLOW));
            return;
        }

        ctx.sendMessage(Message.raw("========== Available Backups ==========").color(new Color(255, 215, 0)).bold(true));
        for (int i = 0; i < backups.size(); i++) {
            String backup = backups.get(i);
            if (i == 0) {
                ctx.sendMessage(Message.raw("• " + backup + " (latest)").color(Color.GREEN));
            } else {
                ctx.sendMessage(Message.raw("• " + backup).color(Color.WHITE));
            }
        }
        ctx.sendMessage(Message.raw("Use /mpbackup-restore <backup_name> to restore").color(Color.GRAY));
    }
}
