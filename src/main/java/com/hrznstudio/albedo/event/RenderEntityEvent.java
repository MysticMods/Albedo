package com.hrznstudio.albedo.event;

import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

public class RenderEntityEvent extends Event {
    private final Entity e;

    private RenderEntityEvent(Entity e) {
        super();
        this.e = e;
    }

    public static void postNewEvent(Entity e) {
        MinecraftForge.EVENT_BUS.post(new RenderEntityEvent(e));
    }

    public Entity getEntity() {
        return e;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}
