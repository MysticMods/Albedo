package com.hrznstudio.albedo.event;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

public class ProfilerStartEvent extends Event {
    private final String section;

    private ProfilerStartEvent(String section) {
        super();
        this.section = section;
    }

    public static void postNewEvent(String section) {
        MinecraftForge.EVENT_BUS.post(new ProfilerStartEvent(section));
    }

    public String getSection() {
        return section;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}
