package com.hrznstudio.albedo.event;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

public class RenderTileEntityEvent extends Event {
    private final TileEntity e;

    private RenderTileEntityEvent(TileEntity e) {
        super();
        this.e = e;
    }

    public static void postNewEvent(TileEntity e) {
        MinecraftForge.EVENT_BUS.post(new RenderTileEntityEvent(e));
    }

    public TileEntity getEntity() {
        return e;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}
