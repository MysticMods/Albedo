package com.hrznstudio.albedo;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigManager {

    public static ForgeConfigSpec spec;

    public static ForgeConfigSpec.IntValue maxLights;
    public static ForgeConfigSpec.IntValue maxDistance;
    public static ForgeConfigSpec.BooleanValue disableLights;
    public static ForgeConfigSpec.BooleanValue eightBitNightmare;
    public static ForgeConfigSpec.BooleanValue enableTorchImplementation;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Albedo Config");
        builder.push("Albedo");

        disableLights = builder
                .comment("Disables albedo lighting.")
                .translation("albedo.config.enableLights")
                .define("disableLights", false);
        maxLights = builder
                .comment("The maximum number of lights allowed to render in a scene. Lights are sorted nearest-first, so further-away lights will be culled after nearer lights.")
                .translation("albedo.config.maxLights")
                .defineInRange("maxLights", 20, 0, 200);
        eightBitNightmare = builder
                .comment("Enables retro mode.")
                .translation("albedo.config.eightBitNightmare")
                .define("eightBitNightmare", false);
        enableTorchImplementation = builder
                .comment("Enables default torch item implementation.")
                .translation("albedo.config.enableTorchImplementation")
                .worldRestart()
                .define("enableTorchImplementation", true);
        maxDistance = builder
                .comment("The maximum distance lights can be before being culled.")
                .translation("albedo.config.maxDistance")
                .defineInRange("maxDistance", 64, 16, 256);

        builder.pop();
        spec = builder.build();
    }

    public static boolean isLightingEnabled() {
        return !disableLights.get();
    }
}
