package com.hrznstudio.albedo.event;

import net.minecraftforge.eventbus.api.Event;

public class LightUniformEvent extends Event {
    public LightUniformEvent() {
        super();
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}
