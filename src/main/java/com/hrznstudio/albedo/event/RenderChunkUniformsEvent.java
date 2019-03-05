package com.hrznstudio.albedo.event;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraftforge.eventbus.api.Event;

public class RenderChunkUniformsEvent extends Event {
    private final RenderChunk renderChunk;

    public RenderChunkUniformsEvent(RenderChunk r) {
        super();
        this.renderChunk = r;
    }

    public RenderChunk getChunk() {
        return renderChunk;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}
