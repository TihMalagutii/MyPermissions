package com.mypermissions.command;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.mypermissions.Main;

import javax.annotation.Nonnull;
import java.awt.Color;

public class MyPermsReloadCommand extends CommandBase {

    public MyPermsReloadCommand() {
        super("mpreload", "Reloads MyPermissions configuration");
        this.addAliases("mypermsreload", "permreload");
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        // Check permission
        if (!ctx.sender().hasPermission("myperms.admin")) {
            ctx.sendMessage(Message.translation("server.commands.error.noPermission").color(Color.RED));
            return;
        }

        try {
            Main.getConfigManager().reload();
            Main.getPermissionManager().clearCache();
            ctx.sendMessage(Message.raw("Configuration reloaded successfully!").color(Color.GREEN));
        } catch (Exception e) {
            ctx.sendMessage(Message.raw("Error reloading configuration: " + e.getMessage()).color(Color.RED));
        }
    }
}
