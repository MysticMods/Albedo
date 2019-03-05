package com.hrznstudio.albedo.util;

import com.hrznstudio.albedo.ConfigManager;
import com.hrznstudio.albedo.EventManager;
import com.hrznstudio.albedo.event.RenderChunkUniformsEvent;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL20;

public class RenderUtil {
    public static boolean lightingEnabled = false;
    public static int previousShader = 0;
    public static boolean enabledLast = false;
    public static ItemCameraTransforms.TransformType itemTransformType = TransformType.NONE;

    public static void renderChunkUniforms(RenderChunk c) {
        MinecraftForge.EVENT_BUS.post(new RenderChunkUniformsEvent(c));
    }

    public static void enableLightingUniforms() {
        if (!EventManager.isGui && !ConfigManager.isLightingDisabled()) {
            if (enabledLast) {
                ShaderUtil.useProgram(previousShader);
                enabledLast = false;
            }
            if (ShaderUtil.currentProgram == ShaderUtil.entityLightProgram) {
                int lightPos = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "lightingEnabled");
                GL20.glUniform1i(lightPos, 1);
            }
        }
    }

    public static void disableLightingUniforms() {
        if (!EventManager.isGui && !ConfigManager.isLightingDisabled()) {
            if (ShaderUtil.currentProgram == ShaderUtil.entityLightProgram) {
                int lightPos = GL20.glGetUniformLocation(ShaderUtil.currentProgram, "lightingEnabled");
                GL20.glUniform1i(lightPos, 0);
            }
            if (!enabledLast) {
                previousShader = ShaderUtil.currentProgram;
                enabledLast = true;
                ShaderUtil.useProgram(0);
            }
        }
    }

    public static void setTransform(TransformType t) {
        itemTransformType = t;
    }

}
