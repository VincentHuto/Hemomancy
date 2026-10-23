package com.vincenthuto.hemomancy.client.render.outpouring;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.shader.ExtendedShaderInstance;
import com.vincenthuto.hemomancy.client.render.shader.ShaderHolder;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.client.render.shader.ShaderInit;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Lux light and Tenebris darkness as full-screen passes that read scene depth: Lux lights the surfaces around its orbs
 * without placing block light; Tenebris drains the light around the caster and rolls fog through it. Entries are
 * camera-relative; each pass takes the eight nearest.
 */
public final class OutpouringScreenPasses {
    public record Packed(float[] values, int count) {}
    static final int MAX = 8, MAX_CASTERS = 4;
    private static TextureTarget scene;

    private OutpouringScreenPasses() {}

    static List<double[]> nearest(List<double[]> entries, int max) {
        return entries.stream()
                .sorted(Comparator.comparingDouble(e -> e[0] * e[0] + e[1] * e[1] + e[2] * e[2]))
                .limit(max).toList();
    }

    /** Up to eight xyzw entries as two column-major mat4s (32 floats): one entry per column. */
    static Packed pack(List<double[]> entries) {
        float[] values = new float[32];
        int count = Math.min(MAX, entries.size());
        for (int i = 0; i < count; i++) for (int c = 0; c < 4; c++) values[i * 4 + c] = (float) entries.get(i)[c];
        return new Packed(values, count);
    }

    /** (strength, self) pairs for up to eight shades, two per column of one mat4. */
    static float[] shadeInfo(List<double[]> pairs) {
        float[] values = new float[16];
        for (int i = 0; i < Math.min(MAX, pairs.size()); i++) {
            values[i * 2] = (float) pairs.get(i)[0];
            values[i * 2 + 1] = (float) pairs.get(i)[1];
        }
        return values;
    }

    /**
     * Runs before other AFTER_LEVEL visuals (the dispatcher calls it at high priority) so spell geometry drawn on top
     * is not relit or darkened by stale depth.
     */
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        var mc = Minecraft.getInstance();
        if (mc.level == null) return;
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        Vec3 camera = event.getCamera().getPosition();
        var lights = nearest(OutpouringClientState.luxLights(camera, partial), MAX);
        var shades = nearest(OutpouringClientState.shades(camera, partial), MAX);
        if (lights.isEmpty() && shades.isEmpty()) return;
        Matrix4f invProj = new Matrix4f(event.getProjectionMatrix()).invert();
        Matrix4f invView = new Matrix4f(event.getModelViewMatrix()).invert();
        float time = (float) (AnimationTime.gameTime(mc.level, partial) % 24000);
        if (!lights.isEmpty()) pass(ShaderInit.CAST_LUMEN, pack(lights), null, casters(camera, partial, EnumBloodTendency.LUX), invProj, invView, time);
        if (!shades.isEmpty()) {
            List<double[]> spheres = new ArrayList<>(), info = new ArrayList<>();
            for (double[] s : shades) { spheres.add(new double[]{s[0], s[1], s[2], s[3]}); info.add(new double[]{s[4], s[5]}); }
            pass(ShaderInit.CAST_UMBRA, pack(spheres), shadeInfo(info), casters(camera, partial, EnumBloodTendency.TENEBRIS), invProj, invView, time);
        }
    }

    private static Packed casters(Vec3 camera, float partial, EnumBloodTendency school) {
        return pack(nearest(OutpouringClientState.casters(camera, partial, school), MAX_CASTERS));
    }

    private static void pass(ShaderHolder holder, Packed packed, float[] info, Packed casters, Matrix4f invProj, Matrix4f invView, float time) {
        ExtendedShaderInstance shader = (ExtendedShaderInstance) holder.getInstance().get();
        if (shader == null) return;
        var mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        if (main.width <= 0 || main.height <= 0) return;
        // A depth blit needs matching depth/stencil formats; mirrors and dome renderers turn stencil on for the main target.
        if (scene == null || scene.width != main.width || scene.height != main.height || scene.isStencilEnabled() != main.isStencilEnabled()) {
            if (scene != null) scene.destroyBuffers();
            scene = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
            if (main.isStencilEnabled()) scene.enableStencil();
            scene.setFilterMode(GL11.GL_NEAREST);
        }
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, scene.frameBufferId);
        GL30.glBlitFramebuffer(0, 0, main.width, main.height, 0, 0, scene.width, scene.height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        scene.copyDepthFrom(main);
        main.bindWrite(false);
        shader.safeGetUniform("HemoTime").set(time);
        shader.safeGetUniform("InvProj").set(invProj);
        shader.safeGetUniform("InvView").set(invView);
        float[] v = packed.values();
        shader.safeGetUniform("LightsA").set(new Matrix4f().set(java.util.Arrays.copyOfRange(v, 0, 16)));
        shader.safeGetUniform("LightsB").set(new Matrix4f().set(java.util.Arrays.copyOfRange(v, 16, 32)));
        shader.safeGetUniform("Count").set((float) packed.count());
        shader.safeGetUniform("Casters").set(new Matrix4f().set(java.util.Arrays.copyOfRange(casters.values(), 0, 16)));
        shader.safeGetUniform("CasterCount").set((float) casters.count());
        boolean firstPerson = mc.options.getCameraType().isFirstPerson();
        if (info != null) shader.safeGetUniform("SelfShade").set(firstPerson ? .2f : 1f);
        if (info != null) shader.safeGetUniform("ShadeInfo").set(new Matrix4f().set(info));
        RenderSystem.setShaderTexture(0, scene.getColorTextureId());
        RenderSystem.setShaderTexture(1, scene.getDepthTextureId());
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(holder.getInstance());
        var b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        b.addVertex(-1, -1, 0).setUv(0, 0);
        b.addVertex(1, -1, 0).setUv(1, 0);
        b.addVertex(1, 1, 0).setUv(1, 1);
        b.addVertex(-1, 1, 0).setUv(0, 1);
        BufferUploader.drawWithShader(b.buildOrThrow());
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.setShaderTexture(1, 0);
    }
}
