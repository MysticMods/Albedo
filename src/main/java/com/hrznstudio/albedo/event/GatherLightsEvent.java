package com.hrznstudio.albedo.event;

import com.hrznstudio.albedo.lighting.Light;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;

public class GatherLightsEvent extends Event {
    private final ArrayList<Light> lights;

    public GatherLightsEvent(ArrayList<Light> lights) {
        super();
        this.lights = lights;
    }

    public ArrayList<Light> getLightList() {
        return lights;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}