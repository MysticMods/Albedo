package com.hrznstudio.albedo;

import com.hrznstudio.albedo.event.GatherLightsEvent;
import com.hrznstudio.albedo.lighting.DefaultLightProvider;
import com.hrznstudio.albedo.lighting.ILightProvider;
import com.hrznstudio.albedo.lighting.Light;
import com.hrznstudio.albedo.util.ShaderUtil;
import com.hrznstudio.albedo.util.TriConsumer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.INBTBase;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Mod("albedo")
public class Albedo {

    public static Map<Block, TriConsumer<BlockPos, IBlockState, GatherLightsEvent>> MAP = new HashMap<>();

    public Albedo() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigManager.spec);
    }

    @CapabilityInject(ILightProvider.class)
    public static Capability<ILightProvider> LIGHT_PROVIDER_CAPABILITY;

    public void commonSetup(FMLCommonSetupEvent event) {
        CapabilityManager.INSTANCE.register(ILightProvider.class, new Capability.IStorage<ILightProvider>() {
            @Nullable
            @Override
            public INBTBase writeNBT(Capability<ILightProvider> capability, ILightProvider instance, EnumFacing side) {
                return null;
            }

            @Override
            public void readNBT(Capability<ILightProvider> capability, ILightProvider instance, EnumFacing side, INBTBase nbt) {

            }
        }, DefaultLightProvider::new);
    }

    public void loadComplete(FMLLoadCompleteEvent event) {
        DeferredWorkQueue.runLater(() -> ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(new ShaderUtil()));
    }

    public void registerBlockHandler(Block block, TriConsumer<BlockPos, IBlockState, GatherLightsEvent> consumer) {
        MAP.put(block, consumer);
    }

    public void clientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new EventManager());
        MinecraftForge.EVENT_BUS.register(new ConfigManager());
        registerBlockHandler(Blocks.REDSTONE_TORCH, (pos, state, evt) -> {
            if (state.get(BlockRedstoneTorch.LIT)) {
                evt.add(Light.builder()
                        .pos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                        .color(1.0f, 0, 0, 1.0f)
                        .radius(6)
                        .build());
            }
        });
    }
}