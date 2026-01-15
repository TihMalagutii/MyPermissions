package com.mypermissions.command.backup;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.mypermissions.Main;

import javax.annotation.Nonnull;
import java.awt.Color;

public class BackupRestoreCommand extends CommandBase {
    private final RequiredArg<String> backupArg;

    public BackupRestoreCommand() {
        super("mpbackup-restore", "Restores config from backup");
        this.addAliases("permbackup-restore");
        this.backupArg = withRequiredArg("backup", "Backup file name", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        if (!ctx.sender().hasPermission("myperms.admin")) {
            ctx.sendMessage(Message.translation("server.commands.error.noPermission").color(Color.RED));
            return;
        }

        String backupName = backupArg.get(ctx);
        
        try {
            boolean success = Main.getConfigManager().restoreFromBackup(backupName);
            if (success) {
                Main.getPermissionManager().clearAllCache();
                ctx.sendMessage(Message.raw("[OK] Config restored from backup: " + backupName).color(Color.GREEN));
                ctx.sendMessage(Message.raw("Use /mpreload to apply changes").color(Color.YELLOW));
            } else {
                ctx.sendMessage(Message.raw("Failed to restore backup!").color(Color.RED));
            }
        } catch (Exception e) {
            ctx.sendMessage(Message.raw("Failed to restore backup: " + e.getMessage()).color(Color.RED));
        }
    }
}
