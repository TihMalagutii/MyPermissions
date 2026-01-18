package com.mypermissions.command;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.mypermissions.page.MainPage;

public class MainUICommand extends AbstractPlayerCommand {
    public MainUICommand() {
        super("myperms", "Opens the MyPermissions UI");
        this.addAliases("mypermissions", "mpui", "mp");
        requirePermission("myperms.admin");  // Nur OPs oder Spieler mit dieser Permission
    }

    @Override
    protected void execute(
        @Nonnull CommandContext context,
        @Nonnull Store<EntityStore> store,
        @Nonnull Ref<EntityStore> ref,
        @Nonnull PlayerRef playerRef,
        @Nonnull World world
    ) {
        Player player = (Player) store.getComponent(ref, Player.getComponentType());
        MainPage page = new MainPage(playerRef);
        player.getPageManager().openCustomPage(ref, store, page);
    }
}
