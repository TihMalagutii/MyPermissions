package com.mypermissions.command.group;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.mypermissions.Main;

import javax.annotation.Nonnull;
import java.awt.Color;

public class GroupListCommand extends CommandBase {
    public GroupListCommand() {
        super("mpgroup-list", "Lists all permission groups");
        this.addAliases("permgroup-list");
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        if (!ctx.sender().hasPermission("myperms.admin")) {
            ctx.sendMessage(Message.translation("server.commands.error.noPermission").color(Color.RED));
            return;
        }

        var groups = Main.getConfig().getGroups();
        if (groups.isEmpty()) {
            ctx.sendMessage(Message.raw("No groups found!").color(Color.YELLOW));
            return;
        }

        ctx.sendMessage(Message.raw("========== Permission Groups ==========").color(new Color(255, 215, 0)).bold(true));
        groups.keySet().forEach(name -> {
            var group = groups.get(name);
            ctx.sendMessage(Message.raw("• " + name + " (priority: " + group.getPriority() + ")").color(Color.WHITE));
        });
    }
}
