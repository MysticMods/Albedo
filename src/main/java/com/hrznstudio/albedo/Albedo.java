package com.hrznstudio.albedo;

import com.hrznstudio.albedo.util.ShaderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("albedo")
public class Albedo {
    public Albedo() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preinit);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigManager.spec);
    }

    public void preinit(FMLClientSetupEvent event) {
        DeferredWorkQueue.runLater(() -> ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(new ShaderUtil()));
        MinecraftForge.EVENT_BUS.register(new EventManager());
        MinecraftForge.EVENT_BUS.register(new ConfigManager());
    }
}