package com.hrznstudio.albedo;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigManager {

    public static ForgeConfigSpec spec;
    //
//	public static Configuration config;
//
//	//LIGHTING
    public static ForgeConfigSpec.IntValue maxLights;
    public static ForgeConfigSpec.BooleanValue disableLights;
    //
//	//MISC
    public static ForgeConfigSpec.BooleanValue eightBitNightmare;

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

        builder.pop();
        spec = builder.build();
    }

    public static boolean isLightingDisabled() {
        return disableLights.get();
    }
//
//	public static void init(File configFile){
//		if(config == null)
//		{
//			config = new Configuration(configFile);
//			load();
//		}
//	}
//
//	public static void load(){
//		config.addCustomCategoryComment("light", "Settings related to lighting.");
//		config.addCustomCategoryComment("misc", "Settings related to random effects.");
//
//		maxLights = config.getInt("maxLights", "light", 10, 0, 100, "The maximum number of lights allowed to render in a scene. Lights are sorted nearest-first, so further-away lights will be culled after nearer lights.");
//		enableLights = config.getBoolean("enableLights", "light", true, "Enables lighting in general.");
//
//		eightBitNightmare = config.getBoolean("eightBitNightmare", "misc", false, "Enables retro mode.");
//
//		if (config.hasChanged())
//		{
//			config.save();
//		}
//	}
//
//	@SubscribeEvent
//	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event){
//		if(event.getModID().equalsIgnoreCase(Albedo.MODID))
//		{
//			load();
//		}
//	}
}
