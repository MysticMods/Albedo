package com.hrznstudio.albedo;

import com.hrznstudio.albedo.event.*;
import com.hrznstudio.albedo.lighting.ILightProvider;
import com.hrznstudio.albedo.lighting.Light;
import com.hrznstudio.albedo.lighting.LightManager;
import com.hrznstudio.albedo.util.ShaderUtil;
import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
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
import org.lwjgl.opengl.GL20;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

public class EventManager {
    public static boolean isGui = false;
    int ticks = 0;
    boolean postedLights = false;
    boolean precedesEntities = true;
    String section = "";
    Thread thread;

    public void checkBitNightmare() {
        int bitLoc = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "bits");
        GL20.glUniform1i(bitLoc, ConfigManager.eightBitNightmare.get() ? 1 : 0);
    }

    public static final HashMap<BlockPos, IBlockState> EXISTING = new HashMap<>();

    @SubscribeEvent
    public void onProfilerChange(ProfilerStartEvent event) {
        section = event.getSection();
        if (ConfigManager.isLightingEnabled()) {
            if (event.getSection().compareTo("terrain") == 0) {
                if (Minecraft.getInstance().player != null) {
                    EntityPlayer player = Minecraft.getInstance().player;
                    if (Minecraft.getInstance().world != null) {
                        IWorldReader reader = Minecraft.getInstance().world;
                        BlockPos playerPos = player.getPosition();
                        int r = 16;
                        Iterable<BlockPos.MutableBlockPos> posIterable = BlockPos.getAllInBoxMutable(playerPos.add(-r, -r, -r), playerPos.add(r, r, r));
                        for (BlockPos.MutableBlockPos pos : posIterable) {
                            IBlockState state = reader.getBlockState(pos);
                            if (state.getBlock() == Blocks.REDSTONE_WIRE) {
                                int power  = state.get(BlockRedstoneWire.POWER);
                                if(power!=0) {
                                    Light light = Light.builder()
                                            .pos(pos.getX(), pos.getY(), pos.getZ())
                                            .color(1.0f, 0.2f, 0, (power/16f))
                                            .radius(6)
                                            .build();
                                    LightManager.lights.add(light);
                                }
                            }
                            if (state.getBlock() == Blocks.REDSTONE_TORCH||state.getBlock()== Blocks.REDSTONE_WALL_TORCH) {
                                if(state.get(BlockRedstoneTorch.LIT)) {
                                    Light light = Light.builder()
                                            .pos(pos.getX(), pos.getY(), pos.getZ())
                                            .color(1.0f, 0.2f, 0, 1.0f)
                                            .radius(6)
                                            .build();
                                    LightManager.lights.add(light);
                                }
                            }
                        }
                    }
                }
                isGui = false;
                precedesEntities = true;
                ShaderUtil.useProgram(ShaderUtil.fastLightProgram);
                checkBitNightmare();
                int tickLoc = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "ticks");
                GL20.glUniform1f(tickLoc, (float) ticks + Minecraft.getInstance().getRenderPartialTicks());
                int texloc = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "sampler");
                GL20.glUniform1i(texloc, 0);
                texloc = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "lightmap");
                GL20.glUniform1i(texloc, 1);
                texloc = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "brightlayer");
                GL20.glUniform1i(texloc, 2);
                int playerPos = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "playerPos");
                GL20.glUniform3f(playerPos, (float) Minecraft.getInstance().player.posX, (float) Minecraft.getInstance().player.posY, (float) Minecraft.getInstance().player.posZ);
                if (!postedLights) {
                    LightManager.update(Minecraft.getInstance().world);
                    ShaderUtil.useProgram(0);
                    MinecraftForge.EVENT_BUS.post(new LightUniformEvent());
                    ShaderUtil.useProgram(ShaderUtil.fastLightProgram);
                    LightManager.uploadLights();
                    ShaderUtil.useProgram(ShaderUtil.entityLightProgram);
                    checkBitNightmare();
                    tickLoc = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "ticks");
                    GL20.glUniform1f(tickLoc, (float) ticks + Minecraft.getInstance().getRenderPartialTicks());
                    texloc = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "sampler");
                    GL20.glUniform1i(texloc, 0);
                    texloc = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "lightmap");
                    GL20.glUniform1i(texloc, 1);
                    texloc = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "brightlayer");
                    GL20.glUniform1i(texloc, 2);
                    LightManager.uploadLights();
                    playerPos = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "playerPos");
                    GL20.glUniform3f(playerPos, (float) Minecraft.getInstance().player.posX, (float) Minecraft.getInstance().player.posY, (float) Minecraft.getInstance().player.posZ);
                    int lightPos = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "lightingEnabled");
                    GL20.glUniform1i(lightPos, GL11.glIsEnabled(GL11.GL_LIGHTING) ? 1 : 0);
                    ShaderUtil.useProgram(ShaderUtil.fastLightProgram);
                    postedLights = true;
                    LightManager.clear();
                }
            }
            if (event.getSection().compareTo("sky") == 0) {
                ShaderUtil.useProgram(0);
            }
            if (event.getSection().compareTo("litParticles") == 0) {
                ShaderUtil.useProgram(ShaderUtil.fastLightProgram);
                int texloc = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "sampler");
                GL20.glUniform1i(texloc, 0);
                texloc = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "lightmap");
                GL20.glUniform1i(texloc, 1);
                int playerPos = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "playerPos");
                GL20.glUniform3f(playerPos, (float) Minecraft.getInstance().player.posX, (float) Minecraft.getInstance().player.posY, (float) Minecraft.getInstance().player.posZ);
                int chunkX = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "chunkX");
                int chunkY = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "chunkY");
                int chunkZ = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "chunkZ");
                GL20.glUniform1i(chunkX, 0);
                GL20.glUniform1i(chunkY, 0);
                GL20.glUniform1i(chunkZ, 0);
            }
            if (event.getSection().compareTo("particles") == 0) {
                ShaderUtil.useProgram(0);
            }
            if (event.getSection().compareTo("weather") == 0) {
                ShaderUtil.useProgram(0);
            }
            if (event.getSection().compareTo("entities") == 0) {
                if (Minecraft.getInstance().isCallingFromMinecraftThread()) {
                    ShaderUtil.useProgram(ShaderUtil.entityLightProgram);
                    int lightPos = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "lightingEnabled");
                    GL20.glUniform1i(lightPos, 1);
                    int fogPos = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "fogIntensity");
                    GL20.glUniform1f(fogPos, Minecraft.getInstance().world.getDimension().getType() == DimensionType.NETHER ? 0.015625f : 1.0f);
                }
            }
            if (event.getSection().compareTo("blockEntities") == 0) {
                if (Minecraft.getInstance().isCallingFromMinecraftThread()) {
                    ShaderUtil.useProgram(ShaderUtil.entityLightProgram);
                    int lightPos = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "lightingEnabled");
                    GL20.glUniform1i(lightPos, 1);
                }
            }
            if (event.getSection().compareTo("outline") == 0) {
                ShaderUtil.useProgram(0);
            }
            if (event.getSection().compareTo("aboveClouds") == 0) {
                ShaderUtil.useProgram(0);
            }
            if (event.getSection().compareTo("destroyProgress") == 0) {
                ShaderUtil.useProgram(0);
            }
            if (event.getSection().compareTo("translucent") == 0) {
                ShaderUtil.useProgram(ShaderUtil.fastLightProgram);
                int texloc = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "sampler");
                GL20.glUniform1i(texloc, 0);
                texloc = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "lightmap");
                GL20.glUniform1i(texloc, 1);
                int playerPos = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "playerPos");
                GL20.glUniform3f(playerPos, (float) Minecraft.getInstance().player.posX, (float) Minecraft.getInstance().player.posY, (float) Minecraft.getInstance().player.posZ);
            }
            if (event.getSection().compareTo("hand") == 0) {
                ShaderUtil.useProgram(ShaderUtil.entityLightProgram);
                int entityPos = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "entityPos");
                EntityPlayer player = Minecraft.getInstance().player;
                GL20.glUniform3f(entityPos, (float) player.posX, (float) player.posY + player.height / 2.0f, (float) player.posZ);
                precedesEntities = true;
            }
            if (event.getSection().compareTo("gui") == 0) {
                isGui = true;
                ShaderUtil.useProgram(0);
            }
        }
    }

    @SubscribeEvent
    public void onRenderEntity(RenderEntityEvent event) {
        if (ConfigManager.isLightingEnabled()) {
            if (event.getEntity() instanceof EntityLightningBolt) {
                ShaderUtil.useProgram(0);
            } else if (section.equalsIgnoreCase("entities") || section.equalsIgnoreCase("blockEntities")) {
                ShaderUtil.useProgram(ShaderUtil.entityLightProgram);
            }
            if (ShaderUtil.currentProgram == ShaderUtil.entityLightProgram) {
                int entityPos = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "entityPos");
                GL20.glUniform3f(entityPos, (float) event.getEntity().posX, (float) event.getEntity().posY + event.getEntity().height / 2.0f, (float) event.getEntity().posZ);
                int colorMult = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "colorMult");
                GL20.glUniform4f(colorMult, 1.0f, 1.0f, 1.0f, 0.0f);
                if (event.getEntity() instanceof EntityLivingBase) {
                    EntityLivingBase e = (EntityLivingBase) event.getEntity();
                    if (e.hurtTime > 0 || e.deathTime > 0) {
                        GL20.glUniform4f(colorMult, 1.0f, 0.0f, 0.0f, 0.3f);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderTileEntity(RenderTileEntityEvent event) {
        if (ConfigManager.isLightingEnabled()) {
            if (event.getEntity() instanceof TileEntityEndPortal || event.getEntity() instanceof TileEntityEndGateway) {
                ShaderUtil.useProgram(0);
            } else if (section.equalsIgnoreCase("entities") || section.equalsIgnoreCase("blockEntities")) {
                ShaderUtil.useProgram(ShaderUtil.entityLightProgram);
            }
            if (ShaderUtil.currentProgram == ShaderUtil.entityLightProgram) {
                int entityPos = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "entityPos");
                GL20.glUniform3f(entityPos, (float) event.getEntity().getPos().getX(), (float) event.getEntity().getPos().getY(), (float) event.getEntity().getPos().getZ());
                int colorMult = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "colorMult");
                GL20.glUniform4f(colorMult, 1.0f, 1.0f, 1.0f, 0.0f);
            }
        }
    }

    @SubscribeEvent
    public void onRenderChunk(RenderChunkUniformsEvent event) {
        if (ConfigManager.isLightingEnabled()) {
            if (ShaderUtil.currentProgram == ShaderUtil.fastLightProgram) {
                BlockPos pos = event.getChunk().getPosition();
                int chunkX = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "chunkX");
                int chunkY = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "chunkY");
                int chunkZ = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "chunkZ");
                GL20.glUniform1i(chunkX, pos.getX());
                GL20.glUniform1i(chunkY, pos.getY());
                GL20.glUniform1i(chunkZ, pos.getZ());
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
            ShaderUtil.useProgram(0);
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
                    .color(1.0f, 0.2f, 0)
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