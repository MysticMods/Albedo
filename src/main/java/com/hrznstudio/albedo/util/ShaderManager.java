package com.hrznstudio.albedo.util;

import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL20;

public class ShaderManager {
    private static ShaderManager currentShader = null;
    private static int currentProgram = -1;
    private final int program;

    public ShaderManager(ResourceLocation shader, IResourceManager resourceManager) {
        program = ShaderUtil.loadProgram(
                String.format("%s:shader/%s.vs", shader.getNamespace(), shader.getPath()),
                String.format("%s:shader/%s.fs", shader.getNamespace(), shader.getPath()),
                resourceManager
        );
    }

    public static ShaderManager getCurrentShader() {
        return currentShader;
    }

    public void useShader() {
        if (!isCurrentShader()) {
            GL20.glUseProgram(program);
            currentProgram = program;
            currentShader = this;
        }
    }

    public static void stopShader() {
        if (currentProgram != 0) {
            GL20.glUseProgram(0);
            currentProgram = 0;
            currentShader = null;
        }
    }

    public boolean isCurrentShader() {
        return currentProgram == program;
    }

    public void setUniform(String uniform, int value) {
        if (isCurrentShader()) {
            GL20.glUniform1i(GL20.glGetUniformLocation(currentProgram, uniform), value);
        }
    }
    public void setUniform(String uniform, boolean value) {
        if (isCurrentShader()) {
            GL20.glUniform1i(GL20.glGetUniformLocation(currentProgram, uniform), value ? 1 : 0);
        }
    }

    public void setUniform(String uniform, float value) {
        if (isCurrentShader()) {
            GL20.glUniform1f(GL20.glGetUniformLocation(currentProgram, uniform), value);
        }
    }
    public void setUniform(String uniform, int v1, int v2) {
        if (isCurrentShader()) {
            GL20.glUniform2i(GL20.glGetUniformLocation(currentProgram, uniform), v1, v2);
        }
    }
    public void setUniform(String uniform, int v1, int v2, int v3) {
        if (isCurrentShader()) {
            GL20.glUniform3i(GL20.glGetUniformLocation(currentProgram, uniform), v1, v2, v3);
        }
    }

    public void setUniform(String uniform, float v1, float v2) {
        if (isCurrentShader()) {
            GL20.glUniform2f(GL20.glGetUniformLocation(currentProgram, uniform), v1, v2);
        }
    }

    public void setUniform(String uniform, float v1, float v2, float v3) {
        if (isCurrentShader()) {
            GL20.glUniform3f(GL20.glGetUniformLocation(currentProgram, uniform), v1, v2, v3);
        }
    }
    public void setUniform(String uniform, float v1, float v2, float v3, float v4) {
        if (isCurrentShader()) {
            GL20.glUniform4f(GL20.glGetUniformLocation(currentProgram, uniform), v1, v2, v3,v4);
        }
    }
}