package com.hrznstudio.albedo.lighting;

import com.hrznstudio.albedo.event.GatherLightsEvent;
import com.hrznstudio.albedo.util.TriConsumer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class BlockLighting extends ForgeRegistryEntry<BlockLighting> {
    private final Block block;
    private final TriConsumer<BlockPos, IBlockState, GatherLightsEvent> consumer;

    public BlockLighting(Block block, TriConsumer<BlockPos, IBlockState, GatherLightsEvent> consumer) {
        this.block = block;
        this.consumer = consumer;
    }

    public Block getBlock() {
        return block;
    }

    public TriConsumer<BlockPos, IBlockState, GatherLightsEvent> getConsumer() {
        return consumer;
    }
}
