package com.hrznstudio.albedo.lighting;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ILightProvider {
    @OnlyIn(Dist.CLIENT)
    Light provideLight();
}
