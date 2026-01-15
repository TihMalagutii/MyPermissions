package com.mypermissions.command;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.mypermissions.Main;
import com.mypermissions.config.MyPermissionsConfig;

import javax.annotation.Nonnull;
import java.awt.Color;

public class MyPermsCommand extends CommandBase {

    public MyPermsCommand() {
        super("myperms", "Manages permissions, groups, and users");
        this.addAliases("mp", "perms", "permission");
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        // Check basic permission
        if (!ctx.sender().hasPermission("myperms.admin")) {
            ctx.sendMessage(Message.translation("server.commands.error.noPermission").color(Color.RED));
            return;
        }

        sendHelp(ctx);
    }

    private void sendHelp(@Nonnull CommandContext ctx) {
        ctx.sendMessage(Message.raw("========== MyPermissions Help ==========").color(new Color(255, 215, 0)).bold(true));
        ctx.sendMessage(Message.raw("Group Commands:").color(Color.YELLOW));
        ctx.sendMessage(Message.raw("  /mpgroup-list").color(Color.WHITE));
        ctx.sendMessage(Message.raw("  /mpgroup-info <group>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("  /mpgroup-create <group>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("  /mpgroup-delete <group>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("  /mpgroup-rename <oldName> <newName>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("  /mpgroup-addperm <group> <permission>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("  /mpgroup-removeperm <group> <permission>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("  /mpgroup-setparent <group> <parent>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("  /mpgroup-setpriority <group> <number>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("User Commands:").color(Color.YELLOW));
        ctx.sendMessage(Message.raw("  /mpuser-add <player> <group>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("  /mpuser-remove <player> <group>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("  /mpuser-info <player>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("  /mpuser-addperm <player> <permission>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("  /mpuser-removeperm <player> <permission>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("Other Commands:").color(Color.YELLOW));
        ctx.sendMessage(Message.raw("  /mpcheck <player> <permission>").color(Color.WHITE));
        ctx.sendMessage(Message.raw("  /mpreload").color(Color.WHITE));
        ctx.sendMessage(Message.raw("  /mpbackup-list").color(Color.WHITE));
        ctx.sendMessage(Message.raw("  /mpbackup-restore <backup>").color(Color.WHITE));
    }
}
