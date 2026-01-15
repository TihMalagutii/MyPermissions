package com.mypermissions;

import java.util.logging.Level;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import com.mypermissions.config.MyPermissionsConfig;

public class Main extends JavaPlugin {
    public static Config<MyPermissionsConfig> CONFIG;

    public Main(@NonNullDecl JavaPluginInit init) {
        super(init);
        CONFIG = this.withConfig("Config", MyPermissionsConfig.CODEC);
    }

    @Override
    protected void setup() {
        super.setup();
        CONFIG.save();

        this.getLogger().at(Level.INFO).log("MyPermissions plugin has been enabled.");
        this.getLogger().at(Level.INFO).log("MyPermissions loaded with DefaultGroup: " + CONFIG.get().getDefaultGroup());
    }

}
