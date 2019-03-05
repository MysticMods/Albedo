package com.hrznstudio.albedo.event;

import com.google.common.collect.ImmutableList;
import com.hrznstudio.albedo.lighting.Light;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;

public class GatherLightsEvent extends Event {
    private final ArrayList<Light> lights;
    private final float maxDistance;
    private final Vec3d cameraPosition;
    private final ICamera camera;

    public GatherLightsEvent(ArrayList<Light> lights, float maxDistance, Vec3d cameraPosition, ICamera camera) {
        this.lights = lights;
        this.maxDistance = maxDistance;
        this.cameraPosition = cameraPosition;
        this.camera = camera;
    }

    public ImmutableList<Light> getLightList() {
        return ImmutableList.copyOf(lights);
    }

    public float getMaxDistance() {
        return maxDistance;
    }

    public Vec3d getCameraPosition() {
        return cameraPosition;
    }

    public ICamera getCamera() {
        return camera;
    }

    public void add(Light l) {
        if (cameraPosition != null && cameraPosition.squareDistanceTo(l.x, l.y, l.z) > l.radius + maxDistance) {
            return;
        }

        if (camera != null && !camera.isBoundingBoxInFrustum(new AxisAlignedBB(
                l.x - l.radius,
                l.y - l.radius,
                l.z - l.radius,
                l.x + l.radius,
                l.y + l.radius,
                l.z + l.radius
        ))) {
            return;
        }
        lights.add(l);
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}