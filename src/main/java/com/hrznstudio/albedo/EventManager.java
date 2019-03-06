package com.hrznstudio.albedo;

import com.hrznstudio.albedo.event.*;
import com.hrznstudio.albedo.lighting.ILightProvider;
import com.hrznstudio.albedo.lighting.Light;
import com.hrznstudio.albedo.lighting.LightManager;
import com.hrznstudio.albedo.util.ShaderManager;
import com.hrznstudio.albedo.util.ShaderUtil;
import com.hrznstudio.albedo.util.TriConsumer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class EventManager {
    public static boolean isGui = false;
    int ticks = 0;
    boolean postedLights = false;
    boolean precedesEntities = true;
    String section = "";
    Thread thread;

    public static final Map<BlockPos, List<Light>> EXISTING = Collections.synchronizedMap(new HashMap<>());

    public void startThread(){
        thread = new Thread(() -> {
            while (!thread.isInterrupted()) {
                if (Minecraft.getInstance().player != null) {
                    EntityPlayer player = Minecraft.getInstance().player;
                    if (Minecraft.getInstance().world != null) {
                        IWorldReader reader = Minecraft.getInstance().world;
                        BlockPos playerPos = player.getPosition();
                        int maxDistance = ConfigManager.maxDistance.get();
                        int r = maxDistance / 2;
                        Iterable<BlockPos.MutableBlockPos> posIterable = BlockPos.getAllInBoxMutable(playerPos.add(-r, -r, -r), playerPos.add(r, r, r));
                        for (BlockPos.MutableBlockPos pos : posIterable) {
                            Vec3d cameraPosition = LightManager.cameraPos;
                            ICamera camera = LightManager.camera;
                            IBlockState state = reader.getBlockState(pos);
                            ArrayList<Light> lights = new ArrayList<>();
                            GatherLightsEvent lightsEvent = new GatherLightsEvent(lights, maxDistance, cameraPosition, camera);
                            TriConsumer<BlockPos, IBlockState, GatherLightsEvent> consumer = Albedo.MAP.get(state.getBlock());
                            if (consumer != null)
                                consumer.apply(pos, state, lightsEvent);
                            if (lights.isEmpty()) {
                                EXISTING.remove(pos);
                            } else {
                                EXISTING.put(pos.toImmutable(), lights);
                            }
                        }
                    }
                }
            }
        });
        thread.start();
    }

    @SubscribeEvent
    public void onProfilerChange(ProfilerStartEvent event) {
        section = event.getSection();
        if (ConfigManager.isLightingEnabled()) {
            if (event.getSection().compareTo("terrain") == 0) {
                isGui = false;
                precedesEntities = true;
                ShaderUtil.fastLightProgram.useShader();
                ShaderUtil.fastLightProgram.setUniform("ticks", ticks + Minecraft.getInstance().getRenderPartialTicks());
                ShaderUtil.fastLightProgram.setUniform("sampler", 0);
                ShaderUtil.fastLightProgram.setUniform("lightmap", 1);
                ShaderUtil.fastLightProgram.setUniform("playerPos", (float) Minecraft.getInstance().player.posX, (float) Minecraft.getInstance().player.posY, (float) Minecraft.getInstance().player.posZ);
                if (!postedLights) {
                    if (thread == null || !thread.isAlive()) {
                        startThread();
                    }
                    EXISTING.forEach((pos, lights) -> LightManager.lights.addAll(lights));
                    LightManager.update(Minecraft.getInstance().world);
                    ShaderManager.stopShader();
                    MinecraftForge.EVENT_BUS.post(new LightUniformEvent());
                    ShaderUtil.fastLightProgram.useShader();
                    LightManager.uploadLights();
                    ShaderUtil.entityLightProgram.useShader();
                    ShaderUtil.entityLightProgram.setUniform("ticks", ticks + Minecraft.getInstance().getRenderPartialTicks());
                    ShaderUtil.entityLightProgram.setUniform("sampler", 0);
                    ShaderUtil.entityLightProgram.setUniform("lightmap", 1);
                    LightManager.uploadLights();
                    ShaderUtil.entityLightProgram.setUniform("playerPos", (float) Minecraft.getInstance().player.posX, (float) Minecraft.getInstance().player.posY, (float) Minecraft.getInstance().player.posZ);
                    ShaderUtil.entityLightProgram.setUniform("lightingEnabled", GL11.glIsEnabled(GL11.GL_LIGHTING));
                    ShaderUtil.fastLightProgram.useShader();
                    postedLights = true;
                    LightManager.clear();
                }
            }
            if (event.getSection().compareTo("sky") == 0) {
                ShaderManager.stopShader();
            }
            if (event.getSection().compareTo("litParticles") == 0) {
                ShaderUtil.fastLightProgram.useShader();
                ShaderUtil.fastLightProgram.setUniform("sampler", 0);
                ShaderUtil.fastLightProgram.setUniform("lightmap", 1);
                ShaderUtil.fastLightProgram.setUniform("playerPos", (float) Minecraft.getInstance().player.posX, (float) Minecraft.getInstance().player.posY, (float) Minecraft.getInstance().player.posZ);
                ShaderUtil.fastLightProgram.setUniform("chunkX", 0);
                ShaderUtil.fastLightProgram.setUniform("chunkY", 0);
                ShaderUtil.fastLightProgram.setUniform("chunkZ", 0);
            }
            if (event.getSection().compareTo("particles") == 0) {
                ShaderManager.stopShader();
            }
            if (event.getSection().compareTo("weather") == 0) {
                ShaderManager.stopShader();
            }
            if (event.getSection().compareTo("entities") == 0) {
                if (Minecraft.getInstance().isCallingFromMinecraftThread()) {
                    ShaderUtil.entityLightProgram.useShader();
                    ShaderUtil.entityLightProgram.setUniform("lightingEnabled", true);
                    ShaderUtil.entityLightProgram.setUniform("fogIntensity", Minecraft.getInstance().world.getDimension().getType() == DimensionType.NETHER ? 0.015625f : 1.0f);
                }
            }
            if (event.getSection().compareTo("blockEntities") == 0) {
                if (Minecraft.getInstance().isCallingFromMinecraftThread()) {
                    ShaderUtil.entityLightProgram.useShader();
                    ShaderUtil.entityLightProgram.setUniform("lightingEnabled", true);
                }
            }
            if (event.getSection().compareTo("outline") == 0) {
                ShaderManager.stopShader();
            }
            if (event.getSection().compareTo("aboveClouds") == 0) {
                ShaderManager.stopShader();
            }
            if (event.getSection().compareTo("destroyProgress") == 0) {
                ShaderManager.stopShader();
            }
            if (event.getSection().compareTo("translucent") == 0) {
                ShaderUtil.fastLightProgram.useShader();
                ShaderUtil.fastLightProgram.setUniform("sampler", 0);
                ShaderUtil.fastLightProgram.setUniform("lightmap", 1);
                ShaderUtil.fastLightProgram.setUniform("playerPos", (float) Minecraft.getInstance().player.posX, (float) Minecraft.getInstance().player.posY, (float) Minecraft.getInstance().player.posZ);
            }
            if (event.getSection().compareTo("hand") == 0) {
                ShaderUtil.entityLightProgram.useShader();
                ShaderUtil.fastLightProgram.setUniform("entityPos", (float) Minecraft.getInstance().player.posX, (float) Minecraft.getInstance().player.posY, (float) Minecraft.getInstance().player.posZ);
                precedesEntities = true;
            }
            if (event.getSection().compareTo("gui") == 0) {
                isGui = true;
                ShaderManager.stopShader();
            }
        }
    }

    @SubscribeEvent
    public void onRenderEntity(RenderEntityEvent event) {
        if (ConfigManager.isLightingEnabled()) {
            if (event.getEntity() instanceof EntityLightningBolt) {
                ShaderManager.stopShader();
            } else if (section.equalsIgnoreCase("entities") || section.equalsIgnoreCase("blockEntities")) {
                ShaderUtil.entityLightProgram.useShader();
            }
            if (ShaderManager.isCurrentShader(ShaderUtil.entityLightProgram)) {
                ShaderUtil.entityLightProgram.setUniform("entityPos", (float) event.getEntity().posX, (float) event.getEntity().posY + event.getEntity().height / 2.0f, (float) event.getEntity().posZ);
                ShaderUtil.entityLightProgram.setUniform("colorMult", 1f, 1f, 1f, 0f);
                if (event.getEntity() instanceof EntityLivingBase) {
                    EntityLivingBase e = (EntityLivingBase) event.getEntity();
                    if (e.hurtTime > 0 || e.deathTime > 0) {
                        ShaderUtil.entityLightProgram.setUniform("colorMult", 1f, 0f, 0f, 0.3f);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderTileEntity(RenderTileEntityEvent event) {
        if (ConfigManager.isLightingEnabled()) {
            if (event.getEntity() instanceof TileEntityEndPortal || event.getEntity() instanceof TileEntityEndGateway) {
                ShaderManager.stopShader();
            } else if (section.equalsIgnoreCase("entities") || section.equalsIgnoreCase("blockEntities")) {
                ShaderUtil.entityLightProgram.useShader();
            }
            if (ShaderManager.isCurrentShader(ShaderUtil.entityLightProgram)) {
                ShaderUtil.entityLightProgram.setUniform("entityPos", (float) event.getEntity().getPos().getX(), (float) event.getEntity().getPos().getY(), (float) event.getEntity().getPos().getZ());
                ShaderUtil.entityLightProgram.setUniform("colorMult", 1f, 1f, 1f, 0f);
            }
        }
    }

    @SubscribeEvent
    public void onRenderChunk(RenderChunkUniformsEvent event) {
        if (ConfigManager.isLightingEnabled()) {
            if (ShaderManager.isCurrentShader(ShaderUtil.fastLightProgram)) {
                BlockPos pos = event.getChunk().getPosition();
                ShaderUtil.fastLightProgram.setUniform("chunkX", pos.getX());
                ShaderUtil.fastLightProgram.setUniform("chunkY", pos.getY());
                ShaderUtil.fastLightProgram.setUniform("chunkZ", pos.getZ());
            }
        }
    }

    @SubscribeEvent
    public void clientTick(ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            ticks++;
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        postedLights = false;
        if (Minecraft.getInstance().isCallingFromMinecraftThread()) {
            GlStateManager.disableLighting();
            ShaderManager.stopShader();
        }
    }

    public static class TorchLightProvider implements ILightProvider {
        @Override
        public void gatherLights(GatherLightsEvent event, Entity entity) {
            event.add(Light.builder()
                    .pos(
                            (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) Minecraft.getInstance().getRenderPartialTicks()),
                            (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) Minecraft.getInstance().getRenderPartialTicks()),
                            (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) Minecraft.getInstance().getRenderPartialTicks())
                    )
                    .color(1.0f, 0.78431374f, 0)
                    .radius(10)
                    .build()
            );
        }
    }

    public static class RedstoneTorchProvider implements ILightProvider {
        @Override
        public void gatherLights(GatherLightsEvent event, Entity entity) {
            event.add(Light.builder()
                    .pos(
                            (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) Minecraft.getInstance().getRenderPartialTicks()),
                            (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) Minecraft.getInstance().getRenderPartialTicks()),
                            (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) Minecraft.getInstance().getRenderPartialTicks())
                    )
                    .color(1.0f, 0, 0)
                    .radius(6)
                    .build()
            );
        }
    }

    private LazyOptional<ILightProvider> torchProvider = LazyOptional.of(TorchLightProvider::new);
    private LazyOptional<ILightProvider> redstoneProvider = LazyOptional.of(RedstoneTorchProvider::new);

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        if (ConfigManager.enableTorchImplementation.get()) {
            if (event.getObject().getItem() == Blocks.TORCH.asItem()) {
                event.addCapability(new ResourceLocation("albedo", "light_provider"), new ICapabilityProvider() {
                    @Nonnull
                    @Override
                    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
                        if (cap == Albedo.LIGHT_PROVIDER_CAPABILITY)
                            return torchProvider.cast();
                        return LazyOptional.empty();
                    }
                });
            } else if (event.getObject().getItem() == Blocks.REDSTONE_TORCH.asItem()) {
                event.addCapability(new ResourceLocation("albedo", "light_provider"), new ICapabilityProvider() {
                    @Nonnull
                    @Override
                    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
                        if (cap == Albedo.LIGHT_PROVIDER_CAPABILITY)
                            return redstoneProvider.cast();
                        return LazyOptional.empty();
                    }
                });
            }
        }
    }
}