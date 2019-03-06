package com.hrznstudio.albedo.util;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ShaderUtil implements ISelectiveResourceReloadListener {

    //public static int currentProgram = -1;
    public static ShaderManager fastLightProgram;
    public static ShaderManager entityLightProgram;
    public static ShaderManager depthProgram;

    public static void init(IResourceManager manager) {
        fastLightProgram=new ShaderManager(new ResourceLocation("albedo:fastlight"), manager);
        entityLightProgram=new ShaderManager(new ResourceLocation("albedo:entitylight"), manager);
        depthProgram=new ShaderManager(new ResourceLocation("albedo:depth"), manager);
    }

    public static int loadProgram(String vsh, String fsh, IResourceManager manager) {
        int vertexShader = createShader(vsh, OpenGlHelper.GL_VERTEX_SHADER, manager);
        int fragmentShader = createShader(fsh, OpenGlHelper.GL_FRAGMENT_SHADER, manager);
        int program = OpenGlHelper.glCreateProgram();
        OpenGlHelper.glAttachShader(program, vertexShader);
        OpenGlHelper.glAttachShader(program, fragmentShader);
        OpenGlHelper.glLinkProgram(program);
        String s = GL20.glGetProgramInfoLog(program);
        System.out.println("GL LOG: "+s);
        return program;
    }

    public static int createShader(String filename, int shaderType, IResourceManager manager) {
        int shader = OpenGlHelper.glCreateShader(shaderType);
        if (shader == 0)
            return 0;
        try {
            String s =readFileAsString(filename, manager);
            OpenGlHelper.glShaderSource(shader, s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        OpenGlHelper.glCompileShader(shader);

        if (GL20.glGetShaderi(shader, OpenGlHelper.GL_COMPILE_STATUS) == GL11.GL_FALSE)
            throw new RuntimeException("Error creating shader: " + getLogInfo(shader));

        return shader;
    }

    public static String getLogInfo(int obj) {
        return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }

    public static String readFileAsString(String filename, IResourceManager manager) throws Exception {
        System.out.println("Loading shader [" + filename + "]...");
        InputStream in = null;
        try {
            IResource resource = manager.getResource(new ResourceLocation(filename));
            in = resource.getInputStream();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String s = "";

        if (in != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
                s = reader.lines().collect(Collectors.joining("\n"));
            }
        }
        return s;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        if (resourcePredicate.test(VanillaResourceType.SHADERS)) {
            init(resourceManager);
        }
    }
}
