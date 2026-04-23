package com.vincenthuto.hemomancy.client.render.shader;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ExtendedShaderInstance extends ShaderInstance {

    protected Map<String, Consumer<Uniform>> defaultUniformData;

    public ExtendedShaderInstance(ResourceProvider pResourceProvider, ResourceLocation location, VertexFormat pVertexFormat) throws IOException {
        super(pResourceProvider, location, pVertexFormat);
        cacheDefaultUniformData();
    }

    public void setUniformDefaults() {
        for (Map.Entry<String, Consumer<Uniform>> defaultDataEntry : getDefaultUniformData().entrySet()) {
            Uniform uniform = getUniform(defaultDataEntry.getKey());
            if (uniform != null) {
                defaultDataEntry.getValue().accept(uniform);
            }
        }
    }

    public abstract ShaderHolder getShaderHolder();

    public Map<String, Consumer<Uniform>> getDefaultUniformData() {
        if (defaultUniformData == null) {
            defaultUniformData = new HashMap<>();
        }
        return defaultUniformData;
    }

    private void cacheDefaultUniformData() {
        for (String uniformName : getShaderHolder().uniformsToCache) {
            Uniform uniform = getUniform(uniformName);
            if (uniform == null) {
                continue;
            }

            Consumer<Uniform> consumer;
            if (uniform.getType() <= 3) {
                IntBuffer buffer = uniform.getIntBuffer();
                int[] defaults = new int[uniform.getCount()];
                for (int i = 0; i < uniform.getCount(); i++) {
                    defaults[i] = buffer.get(i);
                }
                consumer = u -> u.setSafe(
                        defaults.length > 0 ? defaults[0] : 0,
                        defaults.length > 1 ? defaults[1] : 0,
                        defaults.length > 2 ? defaults[2] : 0,
                        defaults.length > 3 ? defaults[3] : 0
                );
            } else {
                FloatBuffer buffer = uniform.getFloatBuffer();
                float[] defaults = new float[uniform.getCount()];
                for (int i = 0; i < uniform.getCount(); i++) {
                    defaults[i] = buffer.get(i);
                }
                if (uniform.getType() <= 7) {
                    consumer = u -> u.setSafe(
                            defaults.length > 0 ? defaults[0] : 0,
                            defaults.length > 1 ? defaults[1] : 0,
                            defaults.length > 2 ? defaults[2] : 0,
                            defaults.length > 3 ? defaults[3] : 0
                    );
                } else {
                    consumer = u -> u.set(Arrays.copyOf(defaults, defaults.length));
                }
            }

            getDefaultUniformData().put(uniformName, consumer);
        }
    }
}