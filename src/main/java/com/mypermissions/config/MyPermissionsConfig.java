package com.mypermissions.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class MyPermissionsConfig {
    public static final BuilderCodec<MyPermissionsConfig> CODEC =
        BuilderCodec.builder(MyPermissionsConfig.class, MyPermissionsConfig::new)
            .append(new KeyedCodec<String>("DefaultGroup", Codec.STRING),
                    (config, value, extraInfo) -> config.DefaultGroup = value,
                    (config, extraInfo) -> config.DefaultGroup).add()
            .build();

    private String DefaultGroup = "default";

    public MyPermissionsConfig() { }

    public String getDefaultGroup() {
        return DefaultGroup;
    }
}
