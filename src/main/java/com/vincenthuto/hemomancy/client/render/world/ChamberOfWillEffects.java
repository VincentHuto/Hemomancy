package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.data.ChamberOfWillClientData;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Random;
import java.util.function.Supplier;

public class ChamberOfWillEffects extends DimensionSpecialEffects {

    public ChamberOfWillEffects() {
        super(Float.NaN, false, SkyType.NONE, false, false);
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {

        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(modelViewMatrix);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        Tesselator tesselator = Tesselator.getInstance();
        ChamberSkyTheme theme = ChamberSkyThemeRegistry.activeTheme();


        float skyDistance = 128.0F;
        if (theme.renderBaseSkybox()) {
            renderBox(poseStack, tesselator, skyDistance, 0, 1, GameRenderer::getPositionTexColorShader,
                    theme.skyTexture(), theme.skyboxColor());
        }
        /*
         * Stars
         */
        float f = (level.getGameTime() + partialTick) * theme.motionMultiplier();
        float membranePulse = membranePulse(f) * theme.pulseStrength();
        float scale = .80f; // give buffer so rotated cubes don't clip through main skybox
        int layers = 6;
        Random random = new Random(431);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        if (theme.renderCloudLayers()) {
            for (int i = 0; i < layers; i++) {
                poseStack.pushPose();
                int j = layers - i - 1;
                float speed = (0.01f + i * i * 0.09f) * .015f;
                float x = (i * 68731 + f * speed * (random.nextFloat() - 0.5f)) % 360;
                float y = (i * 74869 + f * speed * (random.nextFloat() - 0.5f)) % 360;
                float z = (i * 98744 + f * speed * (random.nextFloat() - 0.5f)) % 360;
                poseStack.mulPose(Axis.XP.rotationDegrees(x));
                poseStack.mulPose(Axis.YP.rotationDegrees(y));
                poseStack.mulPose(Axis.ZP.rotationDegrees(z));
                Vector3f rgb = new Vector3f(random.nextFloat() * 0.5f + 0.5f, random.nextFloat() * 0.5f + 0.5f, random.nextFloat() * 0.5f + 0.5f);
                float intensity = Mth.lerp(j / (float) layers, 0.25f, 0.18f);
                intensity *= 1.0F - membranePulse * 0.32F;
                rgb.mul(intensity);
                rgb = new Vector3f(Math.min(rgb.x, 1), Math.min(rgb.y, 1), Math.min(rgb.z, 1));
                RenderSystem.setShaderColor(rgb.x, rgb.y, rgb.z, 1f);
                renderBox(poseStack, tesselator, skyDistance * scale, 0, 4f + 2f * scale,
                        GameRenderer::getPositionTexColorShader, theme.cloudTexture(), theme.cloudColor());
                poseStack.popPose();
                scale -= 0.04f; // give slight separation between layers to prevent too much zfighting/artifacting
            }
        } else {
            scale -= 0.04f * layers;
        }
        /*
         * Nebula
         */
        var color = colorVector(theme.nebulaPrimary());
        color.mul(0.075f);
        // use ever-enclosing z offset to ensure new planes are always in front of old planes, preventing alpha clipping
        if (theme.renderNebulaLayers()) {
            float zoff = renderNebula(poseStack, color, random, f, skyDistance, tesselator, scale, 0f, theme.wispTexture());
            color = colorVector(theme.nebulaSecondary());
            color.mul(0.125f);
            zoff = renderNebula(poseStack, color, random, f, skyDistance, tesselator, scale, zoff, theme.wispTexture());
            color = colorVector(theme.nebulaAccent());
            color.mul(0.125f);
            zoff = renderNebula(poseStack, color, random, f, skyDistance, tesselator, scale, zoff, theme.wispTexture());
        }

        renderSilentArchonDepthEffects(poseStack, tesselator, f, skyDistance, theme);
        RenderSystem.depthMask(true);
        if (theme.renderMonolithPillars()) {
            renderSilentArchonMonolithPillars(poseStack, f, skyDistance, theme);
        }
        renderSilentArchonForegroundStormClouds(poseStack, f, skyDistance, theme);

        int capillaryDepthLayers = theme.capillaryLayers();
        int blueVeinDepthLayers = theme.blueVeinLayers();
        int bloodVesselDepthLayers = theme.bloodVesselLayers();
        int neuralDepthLayers = theme.neuralLayers();

        //renderCorticalFolds(poseStack, tesselator, f, skyDistance);
        renderCapillaryWeb(poseStack, tesselator, f, skyDistance, capillaryDepthLayers, theme);
        renderBlueVeins(poseStack, tesselator, f, skyDistance, blueVeinDepthLayers, theme);
        renderBloodVessels(poseStack, tesselator, f, skyDistance, membranePulse, bloodVesselDepthLayers, theme);
        renderNeuralStructures(poseStack, tesselator, f, skyDistance, neuralDepthLayers, theme);
        if (theme.renderMembranePulse()) {
            renderMembranePulseVignette(poseStack, tesselator, skyDistance, membranePulse);
        }
        //renderBorderAura(level, ticks, partialTick, modelViewMatrix, camera, projectionMatrix);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        return true;
    }

    private static float renderNebula(PoseStack poseStack, Vector3f color, Random random, float f,
                                      float skyDistance, Tesselator tesselator, float scale, float zoff,
                                      ResourceLocation wispTexture) {
        RenderSystem.setShaderColor(color.x, color.y, color.z, 1f);
        int layerSeed = random.nextInt();
        float nextZoff = zoff;
        for (int face = 0; face < 6; face++) {
            poseStack.pushPose();
            rotateSkyFace(poseStack, face);
            Random faceRandom = new Random(layerSeed + face * 72931L);
            nextZoff = Math.max(nextZoff, renderNebulaFaceVolume(poseStack, faceRandom, f, skyDistance, tesselator, scale, zoff + face * 0.002F, wispTexture));
            poseStack.popPose();
        }
        return nextZoff;
    }

    private static float renderNebulaFaceVolume(PoseStack poseStack, Random random, float f,
                                                float skyDistance, Tesselator tesselator, float scale, float zoff,
                                                ResourceLocation wispTexture) {
        int clusterCount = 8;
        float nextZoff = zoff;
        for (int cluster = 0; cluster < clusterCount; cluster++) {
            int lane = cluster % 8;
            float anchorX = nebulaAnchorX(lane, random) * skyDistance * scale;
            float anchorZ = nebulaAnchorZ(lane, random) * skyDistance * scale;
            int puffCount = 2 + random.nextInt(2);
            for (int puff = 0; puff < puffCount; puff++) {
                poseStack.pushPose();
                float speed = 0.003F + cluster * 0.00028F + puff * 0.00018F;
                float driftX = Mth.sin(f * speed + cluster * 1.71F + puff * 0.83F) * skyDistance * 0.035F;
                float driftZ = Mth.cos(f * speed * 0.82F + cluster * 1.13F + puff * 0.47F) * skyDistance * 0.035F;
                float depth = zoff + cluster * 0.012F + puff * 0.018F;
                float spin = (random.nextInt(360) + f * speed) % 360;
                float pitch = Mth.lerp(random.nextFloat(), -16.0F, 16.0F);
                float roll = Mth.lerp(random.nextFloat(), -18.0F, 18.0F);
                poseStack.translate(anchorX + driftX + (random.nextFloat() - 0.5F) * skyDistance * 0.10F,
                        depth,
                        anchorZ + driftZ + (random.nextFloat() - 0.5F) * skyDistance * 0.10F);
                poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
                poseStack.mulPose(Axis.YP.rotationDegrees(spin));
                poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
                float uvMin = (random.nextFloat() * 0.85F + f * speed * 0.002F) % 1.0F;
                float uvMax = uvMin + 1.18F + random.nextFloat() * 0.42F;
                float puffScale = Mth.lerp(random.nextFloat(), 0.34F, 0.56F);
                renderPlane(poseStack, tesselator, skyDistance * scale, uvMin, uvMax,
                        GameRenderer::getPositionTexColorShader, wispTexture, puffScale, 0xFF282828);
                poseStack.popPose();
                nextZoff = Math.max(nextZoff, depth + 0.018F);
            }
        }
        return nextZoff;
    }

    private static float nebulaAnchorX(int lane, Random random) {
        float jitter = Mth.lerp(random.nextFloat(), -0.10F, 0.10F);
        return switch (lane) {
            case 0, 3, 5 -> -0.62F + jitter;
            case 1, 4, 7 -> 0.62F + jitter;
            default -> Mth.lerp(random.nextFloat(), -0.28F, 0.28F);
        };
    }

    private static float nebulaAnchorZ(int lane, Random random) {
        float jitter = Mth.lerp(random.nextFloat(), -0.10F, 0.10F);
        return switch (lane) {
            case 0, 1, 6 -> -0.62F + jitter;
            case 3, 4, 7 -> 0.62F + jitter;
            default -> Mth.lerp(random.nextFloat(), -0.28F, 0.28F);
        };
    }

    private static Vector3f colorVector(int rgb) {
        return new Vector3f(
                ((rgb >> 16) & 255) / 255.0F,
                ((rgb >> 8) & 255) / 255.0F,
                (rgb & 255) / 255.0F);
    }

    private static int tinted(int channel, int tint, int shift) {
        float tintChannel = ((tint >> shift) & 255) / 255.0F;
        return (int) Mth.clamp(channel * tintChannel, 0.0F, 255.0F);
    }

    private static int proceduralAlpha(float alpha) {
        float PROCEDURAL_ALPHA_MULTIPLIER = 2.35F;
        return (int) Mth.clamp(alpha * PROCEDURAL_ALPHA_MULTIPLIER, 0.0F, 255.0F);
    }

    private static float proceduralSize(float size) {
        float PROCEDURAL_SIZE_MULTIPLIER = 1.16F;
        return size * PROCEDURAL_SIZE_MULTIPLIER;
    }

    private static DepthLayer depthLayer(int layer, int layerCount, int seedSalt) {
        int safeCount = Math.max(1, layerCount);
        float t = safeCount <= 1 ? 0.5F : layer / (float) (safeCount - 1);
        float easedT = t * t * (3.0F - 2.0F * t);
        return new DepthLayer(layer, t, Mth.lerp(easedT, 1.075F, 0.390F), Mth.lerp(easedT, 0.42F, 0.88F), Mth.lerp(easedT, 0.24F, 1.34F), Mth.lerp(easedT, 0.30F, 1.18F), Mth.lerp(easedT, 0.20F, 1.14F), seedSalt + layer * 271);
    }

    private static DepthLayer vascularDepthLayer(int layer, int layerCount, int seedSalt) {
        int safeCount = Math.max(1, layerCount);
        float t = safeCount <= 1 ? 0.5F : layer / (float) (safeCount - 1);
        float easedT = t * t * (3.0F - 2.0F * t);
        return new DepthLayer(layer, t, Mth.lerp(easedT, 1.075F, 0.640F), Mth.lerp(easedT, 0.42F, 0.74F), Mth.lerp(easedT, 0.24F, 0.68F), Mth.lerp(easedT, 0.30F, 1.03F), Mth.lerp(easedT, 0.20F, 1.02F), seedSalt + layer * 271);
    }

    private static DepthLayer neuralDepthLayer(int layer, int layerCount, int seedSalt) {
        int safeCount = Math.max(1, layerCount);
        float t = safeCount <= 1 ? 0.5F : layer / (float) (safeCount - 1);
        float easedT = t * t * (3.0F - 2.0F * t);
        return new DepthLayer(layer, t, Mth.lerp(easedT, 1.075F, 0.390F), Mth.lerp(easedT, 0.42F, 0.88F), Mth.lerp(easedT, 0.24F, 1.34F), Mth.lerp(easedT, 0.16F, 1.16F), Mth.lerp(easedT, 0.20F, 1.14F), seedSalt + layer * 271);
    }

    private static int depthLayerCount(int layerCount) {
        return Math.max(0, layerCount);
    }

    private static float layeredTime(float time, DepthLayer layer) {
        return time * layer.motionScale() + layer.seedOffset() * 0.017F;
    }

    private static int layeredAlpha(float alpha, DepthLayer layer) {
        return (int) Mth.clamp(alpha * layer.alphaScale(), 0.0F, 255.0F);
    }

    private static float bloodVesselDetailT(DepthLayer layer) {
        return layer.index() == 0 && Mth.abs(layer.t() - 0.5F) < 0.001F ? 1.0F : layer.t();
    }

    private static float neuralLayerVisibility(DepthLayer layer) {
        float nearBias = layer.t() * layer.t() * layer.t();
        return Mth.lerp(nearBias, 1.0F, 1.0F);
    }

    private static int neuralLayerAlpha(float alpha, DepthLayer layer) {
        return layeredAlpha(alpha * neuralLayerVisibility(layer), layer);
    }

    private static float layeredSize(float size, DepthLayer layer) {
        return size * layer.sizeScale();
    }
    private static float layeredSpan(float span, DepthLayer layer) {
        return span * layer.spanScale();
    }
    private static float membranePulse(float time) {
        float cycle = (time % 208.0F) / 208.0F;
        float beat = wrappedPulse(cycle, 0.04F, 0.055F) * 0.72F;
        float afterbeat = wrappedPulse(cycle, 0.18F, 0.09F) * 0.42F;
        float pulse = Mth.clamp(beat + afterbeat, 0.0F, 1.0F);
        return pulse * pulse * (3.0F - 2.0F * pulse);
    }

    private static float wrappedPulse(float t, float center, float width) {
        float distance = Mth.abs(t - center);
        distance = Math.min(distance, 1.0F - distance);
        float pulse = Mth.clamp(1.0F - distance / width, 0.0F, 1.0F);
        return pulse * pulse * (3.0F - 2.0F * pulse);
    }

    private static void renderMembranePulseVignette(PoseStack poseStack, Tesselator tesselator, float skyDistance, float pulse) {
        if (pulse <= 0.01F) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.depthMask(false);

        int outerAlpha = (int) Mth.clamp(4.0F + pulse * 54.0F, 0.0F, 56.0F);
        int innerAlpha = (int) Mth.clamp(2.0F + pulse * 38.0F, 0.0F, 32.0F);
        for (int face = 0; face < 6; face++) {
            poseStack.pushPose();
            rotateSkyFace(poseStack, face);
            renderMembranePulseFace(poseStack, tesselator, skyDistance * 0.665F, -skyDistance * 0.612F, 46, 2, 16, outerAlpha);
            renderMembranePulseFace(poseStack, tesselator, skyDistance * 0.505F, -skyDistance * 0.488F, 92, 6, 24, innerAlpha);
            poseStack.popPose();
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private static void renderMembranePulseFace(PoseStack poseStack, Tesselator tesselator, float halfSize, float depth, int red, int green, int blue, int alpha) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        int pulseSegments = 48;
        float edgeRadius = halfSize * 1.34F;
        for (int segment = 0; segment < pulseSegments; segment++) {
            float angleA = segment / (float) pulseSegments * Mth.TWO_PI;
            float angleB = (segment + 1) / (float) pulseSegments * Mth.TWO_PI;
            buffer.addVertex(matrix, 0.0F, depth, 0.0F).setColor(red, green, blue, alpha);
            buffer.addVertex(matrix, Mth.cos(angleA) * edgeRadius, depth, Mth.sin(angleA) * edgeRadius).setColor(red, green, blue, 0);
            buffer.addVertex(matrix, Mth.cos(angleB) * edgeRadius, depth, Mth.sin(angleB) * edgeRadius).setColor(red, green, blue, 0);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void renderCorticalFolds(PoseStack poseStack, Tesselator tesselator, float time, float skyDistance) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.depthMask(false);

        for (int face = 0; face < 6; face++) {
            poseStack.pushPose();
            rotateSkyFace(poseStack, face);
            renderCorticalFoldFace(poseStack, tesselator, time, skyDistance, face);
            poseStack.popPose();
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private static void renderCorticalFoldFace(PoseStack poseStack, Tesselator tesselator, float time, float skyDistance, int face) {
        for (int fold = 0; fold < 4; fold++) {
            int seed = 1700 + face * 89 + fold * 31;
            float mirrored = shouldMirrorVesselFace(face) ? -1.0F : 1.0F;
            float laneBase = switch (fold) {
                case 0 -> -0.72F;
                case 1 -> -0.28F;
                case 2 -> 0.23F;
                default -> 0.69F;
            } * mirrored;
            float depth = -skyDistance * (0.94F + fold * 0.006F + face * 0.002F);
            float span = skyDistance * (0.78F + (seed % 3) * 0.055F);
            float laneOffset = (laneBase + (((seed * 7) % 13) - 6) * 0.016F) * skyDistance;
            float angle = faceCorticalFoldAngle(face, fold) + Mth.sin(time * 0.0009F + seed) * 2.2F;
            float width = skyDistance * (0.095F + (seed % 4) * 0.014F);
            float phase = seed * 0.733F;
            float breath = 0.92F + 0.08F * Mth.sin(time * 0.012F + seed * 0.19F);
            int alpha = fold % 2 == 0 ? 48 : 38;

            renderCorticalFoldBand(poseStack, tesselator, depth, span, laneOffset, angle, width * breath, phase, time, 92, 31, 18, alpha, 72);
            renderCorticalFoldBand(poseStack, tesselator, depth - 0.05F, span * 0.92F, laneOffset + Mth.sin(seed) * skyDistance * 0.035F, angle + 7.5F, width * 0.42F * breath, phase + 2.7F, time * 0.84F, 52, 14, 10, alpha + 18, 64);
        }
    }

    private static void renderCorticalFoldBand(PoseStack poseStack, Tesselator tesselator, float depth, float span, float laneOffset, float angleDegrees, float baseWidth, float phase, float time, int red, int green, int blue, int alpha, int segments) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float angle = angleDegrees * Mth.DEG_TO_RAD;
        float alongX = Mth.cos(angle);
        float alongZ = Mth.sin(angle);
        float crossX = -alongZ;
        float crossZ = alongX;
        float[] pointsX = new float[segments + 1];
        float[] pointsZ = new float[segments + 1];
        float[] foldWidths = new float[segments + 1];
        int[] centerAlpha = new int[segments + 1];
        int[] shoulderAlpha = new int[segments + 1];

        for (int point = 0; point <= segments; point++) {
            float t = point / (float) segments;
            float along = Mth.lerp(t, -span, span);
            float slowFold = Mth.sin(t * Mth.TWO_PI * 0.78F + phase + time * 0.0022F);
            float secondary = Mth.sin(t * Mth.TWO_PI * 1.55F + phase * 0.43F - time * 0.0017F);
            float lateral = laneOffset + (slowFold * 0.105F + secondary * 0.038F) * span;
            float centerX = along * alongX + lateral * crossX;
            float centerZ = along * alongZ + lateral * crossZ;
            float softEdge = Mth.clamp(Mth.sin(t * Mth.PI) * 1.15F, 0.0F, 1.0F);
            float foldWidth = baseWidth * (0.82F + 0.18F * Mth.sin(time * 0.010F + phase + t * 4.0F));
            pointsX[point] = centerX;
            pointsZ[point] = centerZ;
            foldWidths[point] = foldWidth;
            centerAlpha[point] = (int) Mth.clamp(alpha * softEdge, 0, 255);
            shoulderAlpha[point] = centerAlpha[point] * 2 / 5;
        }

        for (int point = 0; point < segments; point++) {
            addFoldQuad(matrix, buffer, pointsX, pointsZ, foldWidths, centerAlpha, shoulderAlpha, crossX, crossZ, depth, red, green, blue, point, -1.85F, -0.95F, 0, 1);
            addFoldQuad(matrix, buffer, pointsX, pointsZ, foldWidths, centerAlpha, shoulderAlpha, crossX, crossZ, depth - 0.02F, red, green, blue, point, -0.95F, 0.0F, 1, 2);
            addFoldQuad(matrix, buffer, pointsX, pointsZ, foldWidths, centerAlpha, shoulderAlpha, crossX, crossZ, depth - 0.02F, red, green, blue, point, 0.0F, 0.95F, 2, 1);
            addFoldQuad(matrix, buffer, pointsX, pointsZ, foldWidths, centerAlpha, shoulderAlpha, crossX, crossZ, depth, red, green, blue, point, 0.95F, 1.85F, 1, 0);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void addFoldQuad(Matrix4f matrix, BufferBuilder buffer, float[] pointsX, float[] pointsZ, float[] foldWidths, int[] centerAlpha, int[] shoulderAlpha, float crossX, float crossZ, float depth, int red, int green, int blue, int point, float leftScale, float rightScale, int leftAlphaMode, int rightAlphaMode) {
        addFoldVertex(matrix, buffer, pointsX[point], pointsZ[point], foldWidths[point], crossX, crossZ, depth, red, green, blue, foldAlpha(centerAlpha[point], shoulderAlpha[point], leftAlphaMode), leftScale);
        addFoldVertex(matrix, buffer, pointsX[point + 1], pointsZ[point + 1], foldWidths[point + 1], crossX, crossZ, depth, red, green, blue, foldAlpha(centerAlpha[point + 1], shoulderAlpha[point + 1], leftAlphaMode), leftScale);
        addFoldVertex(matrix, buffer, pointsX[point + 1], pointsZ[point + 1], foldWidths[point + 1], crossX, crossZ, depth, red, green, blue, foldAlpha(centerAlpha[point + 1], shoulderAlpha[point + 1], rightAlphaMode), rightScale);
        addFoldVertex(matrix, buffer, pointsX[point], pointsZ[point], foldWidths[point], crossX, crossZ, depth, red, green, blue, foldAlpha(centerAlpha[point], shoulderAlpha[point], rightAlphaMode), rightScale);
    }

    private static void addFoldVertex(Matrix4f matrix, BufferBuilder buffer, float centerX, float centerZ, float foldWidth, float crossX, float crossZ, float depth, int red, int green, int blue, int alpha, float scale) {
        buffer.addVertex(matrix, centerX + proceduralSize(foldWidth) * scale * crossX, depth, centerZ + proceduralSize(foldWidth) * scale * crossZ).setColor(red, green, blue, proceduralAlpha(alpha));
    }

    private static int foldAlpha(int centerAlpha, int shoulderAlpha, int mode) {
        return switch (mode) {
            case 1 -> shoulderAlpha;
            case 2 -> centerAlpha;
            default -> 0;
        };
    }

    private static float faceCorticalFoldAngle(int face, int fold) {
        float localAngle = switch (fold) {
            case 0 -> -37.0F;
            case 1 -> 18.0F;
            case 2 -> 64.0F;
            default -> -78.0F;
        };
        float faceAngle = switch (face) {
            case 1 -> 74.0F;
            case 2 -> -88.0F;
            case 3 -> 156.0F;
            case 4 -> 46.0F;
            case 5 -> -54.0F;
            default -> -10.0F;
        };
        return faceAngle + (shouldMirrorVesselFace(face) ? -localAngle : localAngle);
    }

    private static void renderCapillaryWeb(PoseStack poseStack, Tesselator tesselator, float time,
                                           float skyDistance, int layerCount, ChamberSkyTheme theme) {
        layerCount = depthLayerCount(layerCount);
        if (layerCount <= 0) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.depthMask(false);

        for (int layer = 0; layer < layerCount; layer++) {
            DepthLayer depthLayer = depthLayer(layer, layerCount, 2200);
            for (int face = 0; face < 6; face++) {
                poseStack.pushPose();
                rotateSkyFace(poseStack, face);
                renderCapillaryWebLayerFace(poseStack, tesselator, time, skyDistance, face, depthLayer, theme);
                poseStack.popPose();
            }
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private static void renderCapillaryWebLayerFace(PoseStack poseStack, Tesselator tesselator, float time,
                                                   float skyDistance, int face, DepthLayer layer,
                                                   ChamberSkyTheme theme) {
        int strands = 4 + Mth.floor(layer.t() * 4.0F);
        float layerTime = layeredTime(time, layer);
        for (int strand = 0; strand < strands; strand++) {
            int seed = layer.seedOffset() + face * 101 + strand * 17;
            float laneT = strands <= 1 ? 0.5F : strand / (float) (strands - 1);
            float laneBase = Mth.lerp(laneT, -0.82F, 0.82F);
            float mirroredLaneBase = shouldMirrorVesselFace(face) ? -laneBase : laneBase;
            float laneJitter = (((seed * 23) % 19) - 9) * Mth.lerp(layer.t(), 0.010F, 0.018F);
            float depth = -skyDistance * (layer.depthScale() + strand * 0.004F + face * 0.0010F);
            float span = layeredSpan(skyDistance * (0.25F + (seed % 5) * 0.025F), layer);
            float laneOffset = (mirroredLaneBase + laneJitter) * skyDistance;
            float angle = faceCapillaryAngle(face, strand % 7) + Mth.sin(layerTime * 0.0014F + seed) * Mth.lerp(layer.t(), 1.0F, 3.0F);
            float wave = 0.72F + (seed % 5) * 0.09F + layer.t() * 0.18F;
            float phase = seed * 1.917F;
            float baseWidth = layeredSize(0.030F + layer.t() * 0.105F + (seed % 3) * 0.010F, layer);
            int red = tinted(52 + Mth.floor(layer.t() * 48.0F) + (seed % 3) * 7, theme.capillaryTint(), 16);
            int green = tinted(12 + Mth.floor(layer.t() * 14.0F), theme.capillaryTint(), 8);
            int blue = tinted(8, theme.capillaryTint(), 0);
            int alpha = layeredAlpha(9.0F + layer.t() * 28.0F + (strand % 2) * 4.0F, layer);

            renderVesselRibbon(poseStack, tesselator, depth, span, laneOffset, angle, wave, phase, layerTime * 0.42F, baseWidth, 38 + Mth.floor(layer.t() * 30.0F), red, green, blue, alpha);

            if (strand % 2 == layer.index() % 2) {
                renderVesselBranch(poseStack, tesselator, depth - 0.04F, span, laneOffset, angle, wave, phase, phase, layerTime * 0.40F, baseWidth * 0.58F, 0.30F + (seed % 4) * 0.12F, seed % 2 == 0, layeredSpan(skyDistance * (0.06F + layer.t() * 0.09F), layer), 18 + Mth.floor(layer.t() * 24.0F), red - 8, Math.max(6, green - 4), blue, layeredAlpha(7.0F + layer.t() * 18.0F, layer));
            }
        }
    }

    private static float faceCapillaryAngle(int face, int capillary) {
        float localAngle = switch (capillary % 7) {
            case 0 -> -66.0F;
            case 1 -> -31.0F;
            case 2 -> 14.0F;
            case 3 -> 47.0F;
            case 4 -> 78.0F;
            case 5 -> -9.0F;
            default -> 33.0F;
        };
        float faceAngle = switch (face) {
            case 1 -> 82.0F;
            case 2 -> -96.0F;
            case 3 -> 171.0F;
            case 4 -> 51.0F;
            case 5 -> -58.0F;
            default -> -18.0F;
        };
        return faceAngle + (shouldMirrorVesselFace(face) ? -localAngle : localAngle);
    }

    private static void renderBloodVessels(PoseStack poseStack, Tesselator tesselator, float time,
                                           float skyDistance, float membranePulse, int layerCount,
                                           ChamberSkyTheme theme) {
        layerCount = depthLayerCount(layerCount);
        if (layerCount <= 0) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.depthMask(false);

        for (int layer = 0; layer < layerCount; layer++) {
            DepthLayer depthLayer = vascularDepthLayer(layer, layerCount, 5600);
            for (int face = 0; face < 6; face++) {
                poseStack.pushPose();
                rotateSkyFace(poseStack, face);
                renderBloodVesselLayerFace(poseStack, tesselator, time, skyDistance, face, membranePulse, depthLayer, theme);
                poseStack.popPose();
            }
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private static void renderBloodVesselLayerFace(PoseStack poseStack, Tesselator tesselator, float time,
                                                  float skyDistance, int face, float membranePulse,
                                                  DepthLayer layer, ChamberSkyTheme theme) {
        float vesselDetailT = bloodVesselDetailT(layer);
        int vesselCount = 2 + Mth.floor(vesselDetailT * 2.0F);
        float layerTime = layeredTime(time, layer);
        for (int vessel = 0; vessel < vesselCount; vessel++) {
            int seed = layer.seedOffset() + face * 73 + vessel * 19;
            float laneT = vesselCount <= 1 ? 0.5F : vessel / (float) (vesselCount - 1);
            float laneBase = Mth.lerp(laneT, -0.76F, 0.76F);
            float mirroredLaneBase = shouldMirrorVesselFace(face) ? -laneBase : laneBase;
            boolean edgeLane = Mth.abs(laneBase) > 0.62F;
            float laneJitter = (((seed * 17) % 13) - 6) * Mth.lerp(vesselDetailT, 0.010F, 0.016F);
            float depth = -skyDistance * (layer.depthScale() + vessel * 0.009F + face * 0.0009F);
            //max LENGTH of the veins
            float span = layeredSpan(skyDistance * ((edgeLane ? 0.16F : 0.94F) + (seed % 4) * 0.132F), layer);
            float laneOffset = (mirroredLaneBase + laneJitter) * skyDistance;
            float angle = faceVesselAngle(face, vessel % 5) + Mth.sin(layerTime * 0.0020F + seed) * Mth.lerp(vesselDetailT, 1.0F, 3.0F);
            float wave = 0.50F + (seed % 4) * 0.10F + vesselDetailT * 0.16F;
            float phase = seed * 5.83F;
            float baseWidth = layeredSize(Mth.lerp(vesselDetailT, 0.060F, edgeLane ? 0.46F : 0.62F) + (seed % 4) * Mth.lerp(vesselDetailT, 0.008F, 0.038F), layer);
            int red = tinted((int) Mth.clamp(Mth.lerp(layer.t(), 70.0F, 168.0F) + membranePulse * 12.0F, 0.0F, 255.0F), theme.bloodTint(), 16);
            int green = tinted(0, theme.bloodTint(), 8);
            int blue = tinted((int) Mth.clamp(Mth.lerp(layer.t(), 14.0F, 38.0F) + membranePulse * 8.0F, 0.0F, 255.0F), theme.bloodTint(), 0);
            int alpha = layeredAlpha(Mth.lerp(layer.t(), 14.0F, edgeLane ? 48.0F : 62.0F) + membranePulse * 6.0F, layer);

            renderPulsingVesselRibbon(poseStack, tesselator, depth, span, laneOffset, angle, wave, phase, layerTime, baseWidth, seed, 42 + Mth.floor(vesselDetailT * 54.0F), red, green, blue, alpha);

            if (layer.t() > 0.42F) {
                renderBloodCellStream(poseStack, tesselator, depth - 0.11F, span, laneOffset, angle, wave, phase, layerTime, baseWidth, seed, edgeLane ? 4 : 7);
            }

            renderVesselBranches(poseStack, tesselator, depth - 0.04F, span, laneOffset, angle, wave, phase, layerTime, baseWidth * Mth.lerp(vesselDetailT, 0.42F, 0.62F), 3 + Mth.floor(vesselDetailT * 2.0F), 0.24F + (seed % 3) * 0.05F, layeredSpan(skyDistance * Mth.lerp(vesselDetailT, 0.085F, 0.26F), layer), 28 + Mth.floor(vesselDetailT * 44.0F), seed, Math.max(24, red - 52), green, Math.max(9, blue - 12), layeredAlpha(Mth.lerp(layer.t(), 16.0F, 48.0F), layer));
        }
    }

    private static void renderBlueVeins(PoseStack poseStack, Tesselator tesselator, float time,
                                        float skyDistance, int layerCount, ChamberSkyTheme theme) {
        layerCount = depthLayerCount(layerCount);
        if (layerCount <= 0) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.depthMask(false);

        for (int layer = 0; layer < layerCount; layer++) {
            DepthLayer depthLayer = vascularDepthLayer(layer, layerCount, 5200);
            for (int face = 0; face < 6; face++) {
                poseStack.pushPose();
                rotateSkyFace(poseStack, face);
                renderBlueVeinLayerFace(poseStack, tesselator, time, skyDistance, face, depthLayer, theme);
                poseStack.popPose();
            }
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private static void renderBlueVeinLayerFace(PoseStack poseStack, Tesselator tesselator, float time,
                                               float skyDistance, int face, DepthLayer layer,
                                               ChamberSkyTheme theme) {
        int veins = 3 + Mth.floor(layer.t() * 3.0F);
        float layerTime = layeredTime(time, layer);
        for (int vein = 0; vein < veins; vein+=2) {
            int seed = layer.seedOffset() + face * 67 + vein * 13;
            float laneT = veins <= 1 ? 0.5F : vein / (float) (veins - 1);
            float laneBase = Mth.lerp(laneT, -0.74F, 0.74F);
            float mirroredLaneBase = shouldMirrorVesselFace(face) ? -laneBase : laneBase;
            float laneJitter = (((seed * 19) % 17) - 8) * Mth.lerp(layer.t(), 0.009F, 0.015F);
            float depth = -skyDistance * (layer.depthScale() + vein * 0.008F + face * 0.008F);
            float span = layeredSpan(skyDistance * (0.22F + (seed % 4) * 0.026F), layer);
            float laneOffset = (mirroredLaneBase + laneJitter) * skyDistance;
            float angle = faceVenousAngle(face, vein % 4) + Mth.sin(layerTime * 1.1122F + seed) * Mth.lerp(layer.t(), 1.0F, 3.2F);
            float wave = 0.62F + (seed % 4) * 0.08F + layer.t() * 0.20F;
            float phase = seed * .71F;
            float baseWidth = layeredSize(Mth.lerp(layer.t(), 0.035F, 0.24F) + (seed % 3) * 0.016F, layer);
            int alpha = layeredAlpha(Mth.lerp(layer.t(), 10.0F, 78.0F), layer);

            int red = tinted(5, theme.veinTint(), 16);
            int green = tinted(28, theme.veinTint(), 8);
            int blue = tinted(105, theme.veinTint(), 0);
            renderVesselRibbon(poseStack, tesselator, depth, span, laneOffset, angle, wave, phase, layerTime * 0.72F, baseWidth, 36 + Mth.floor(layer.t() * 42.0F), red, green, blue, alpha);

            renderVesselBranches(poseStack, tesselator, depth - 0.04F, span, laneOffset, angle, wave, phase, layerTime * 0.72F, baseWidth * 0.54F, 2 + Mth.floor(layer.t() * 2.0F), 0.28F + (seed % 3) * 0.06F, layeredSpan(skyDistance * Mth.lerp(layer.t(), 0.060F, 0.17F), layer), 20 + Mth.floor(layer.t() * 28.0F), seed, Math.max(0, red - 1), Math.max(0, green - 6), Math.max(0, blue - 17), layeredAlpha(Mth.lerp(layer.t(), 12.0F, 44.0F), layer));
        }
    }

    private static boolean shouldMirrorVesselFace(int face) {
        return face == 1 || face == 3 || face == 5;
    }

    private static float faceVesselAngle(int face, int vessel) {
        float localAngle = switch (vessel) {
            case 0 -> -58.0F;
            case 1 -> 24.0F;
            case 2 -> -18.0F;
            case 3 -> 52.0F;
            default -> -34.0F;
        };
        float faceAngle = switch (face) {
            case 1 -> 91.0F;
            case 2 -> -87.0F;
            case 3 -> 179.0F;
            case 4 -> 43.0F;
            case 5 -> -47.0F;
            default -> 0.0F;
        };
        return faceAngle + (shouldMirrorVesselFace(face) ? -localAngle : localAngle);
    }

    private static float faceVenousAngle(int face, int vein) {
        float localAngle = switch (vein) {
            case 0 -> 38.0F;
            case 1 -> -46.0F;
            case 2 -> 17.0F;
            default -> -24.0F;
        };
        float faceAngle = switch (face) {
            case 1 -> 74.0F;
            case 2 -> -102.0F;
            case 3 -> 162.0F;
            case 4 -> 58.0F;
            case 5 -> -64.0F;
            default -> -12.0F;
        };
        return faceAngle + (shouldMirrorVesselFace(face) ? -localAngle : localAngle);
    }

    private static void renderVesselRibbon(PoseStack poseStack, Tesselator tesselator, float depth, float span, float laneOffset, float angleDegrees, float wave, float phase, float time, float baseWidth, int segments, int red, int green, int blue, int alpha) {
        renderVesselRibbon(poseStack, tesselator, depth, span, laneOffset, angleDegrees, wave, phase, time, baseWidth, segments, red, green, blue, alpha, false, 0);
    }

    private static void renderPulsingVesselRibbon(PoseStack poseStack, Tesselator tesselator, float depth, float span, float laneOffset, float angleDegrees, float wave, float phase, float time, float baseWidth, int seed, int segments, int red, int green, int blue, int alpha) {
        renderVesselRibbon(poseStack, tesselator, depth, span, laneOffset, angleDegrees, wave, phase, time, baseWidth, segments, red, green, blue, alpha, true, seed);
    }

    private static void renderVesselRibbon(PoseStack poseStack, Tesselator tesselator, float depth, float span, float laneOffset, float angleDegrees, float wave, float phase, float time, float baseWidth, int segments, int red, int green, int blue, int alpha, boolean pressurePulse, int seed) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        float[] pointsX = new float[segments + 1];
        float[] pointsZ = new float[segments + 1];
        float[] widths = new float[segments + 1];
        float angle = angleDegrees * Mth.DEG_TO_RAD;
        float alongX = Mth.cos(angle);
        float alongZ = Mth.sin(angle);
        float crossX = -alongZ;
        float crossZ = alongX;

        for (int point = 0; point <= segments; point++) {
            float t = point / (float) segments;
            float along = Mth.lerp(t, -span, span);
            float lateral = laneOffset + vesselOffset(t, wave, phase, time, span);
            pointsX[point] = along * alongX + lateral * crossX;
            pointsZ[point] = along * alongZ + lateral * crossZ;

            float endTaper = Mth.clamp(Mth.sin(t * Mth.PI) * 1.4F, 0.18F, 1.0F);
            float livingRipple = 0.88F + 0.12F * Mth.sin(time * 0.045F + phase + t * 8.0F);
            float pulseBulge = pressurePulse ? pressureBulge(t, time, phase, seed) : 1.0F;
            widths[point] = proceduralSize(baseWidth * endTaper * livingRipple * pulseBulge);
        }

        for (int point = 0; point <= segments; point++) {
            int previous = Math.max(0, point - 1);
            int next = Math.min(segments, point + 1);
            float dx = pointsX[next] - pointsX[previous];
            float dz = pointsZ[next] - pointsZ[previous];
            float len = Math.max(0.001F, Mth.sqrt(dx * dx + dz * dz));
            float nx = -dz / len * widths[point];
            float nz = dx / len * widths[point];
            float t = point / (float) segments;
            float alphaFade = Mth.clamp(Mth.sin(t * Mth.PI) * 1.65F, 0.0F, 1.0F);
            int pointAlpha = proceduralAlpha(alpha * alphaFade);

            buffer.addVertex(matrix, pointsX[point] - nx, depth, pointsZ[point] - nz).setColor(red, green, blue, pointAlpha);
            buffer.addVertex(matrix, pointsX[point] + nx, depth, pointsZ[point] + nz).setColor(red, green, blue, pointAlpha);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static float pressureBulge(float t, float time, float phase, int seed) {
        float direction = seed % 2 == 0 ? 1.0F : -1.0F;
        float speed = 0.0028F + (seed % 4) * 0.00038F;
        float centerA = time * speed * direction + phase * 0.071F;
        float centerB = time * speed * direction * 0.72F + phase * 0.113F + 0.48F;
        float widthA = 0.085F + (seed % 3) * 0.012F;
        float widthB = 0.12F + (seed % 2) * 0.018F;
        float bulgeA = movingPulse(t, centerA, widthA);
        float bulgeB = movingPulse(t, centerB, widthB) * 0.58F;
        float pressure = Mth.clamp(bulgeA + bulgeB, 0.0F, 1.15F);
        return 1.0F + pressure * (0.34F + (seed % 3) * 0.035F);
    }

    private static float movingPulse(float t, float center, float width) {
        center = center - Mth.floor(center);
        float distance = Mth.abs(t - center);
        distance = Math.min(distance, 1.0F - distance);
        float pulse = Mth.clamp(1.0F - distance / width, 0.0F, 1.0F);
        return pulse * pulse * (3.0F - 2.0F * pulse);
    }

    private static void renderBloodCellStream(PoseStack poseStack, Tesselator tesselator, float depth, float span, float laneOffset, float angleDegrees, float wave, float phase, float time, float vesselWidth, int seed, int cellCount) {
        float angle = angleDegrees * Mth.DEG_TO_RAD;
        float alongX = Mth.cos(angle);
        float alongZ = Mth.sin(angle);
        float crossX = -alongZ;
        float crossZ = alongX;
        float direction = seed % 2 == 0 ? 1.0F : -1.0F;
        float speed = 0.0048F + (seed % 4) * 0.00075F;

        for (int cell = 0; cell < cellCount; cell++) {
            float baseT = (cell + ((seed * 17 + cell * 11) % 13) / 17.0F) / cellCount;
            float drift = Mth.sin(time * 0.017F + seed * 0.43F + cell * 2.1F) * 0.004F;
            float cellT = baseT + direction * time * speed + drift;
            cellT = cellT - Mth.floor(cellT);
            float along = Mth.lerp(cellT, -span, span);
            float lateral = laneOffset + vesselOffset(cellT, wave, phase, time, span);
            float scatter = (((seed + cell * 7) % 9) - 4) * vesselWidth * 0.11F;
            float jitter = Mth.sin(time * 0.041F + seed * 0.7F + cell) * vesselWidth * 0.026F;
            float x = along * alongX + (lateral + scatter + jitter) * crossX;
            float z = along * alongZ + (lateral + scatter + jitter) * crossZ;
            float radius = (0.13F + ((seed + cell) % 4) * 0.027F) * Mth.clamp(vesselWidth, 0.65F, 1.4F);
            float endFade = Mth.clamp(Mth.sin(cellT * Mth.PI) * 1.55F, 0.0F, 1.0F);
            int alpha = (int) ((56 + ((seed + cell * 11) % 4) * 10) * endFade);

            renderNeuronSoma(poseStack, tesselator, x, depth - cell * 0.003F, z, radius * 1.8F, 42, 0, 7, alpha);
            renderNeuronSoma(poseStack, tesselator, x, depth - 0.026F - cell * 0.003F, z, radius * 0.68F, 96, 2, 14, alpha + 20);
        }
    }

    private static void renderVesselBranch(PoseStack poseStack, Tesselator tesselator, float depth, float parentSpan, float parentLaneOffset, float parentAngleDegrees, float parentWave, float parentPhase, float branchPhase, float time, float baseWidth, float parentT, boolean leftSide, float branchLength, int segments, int red, int green, int blue, int alpha) {
        float parentAngle = parentAngleDegrees * Mth.DEG_TO_RAD;
        float parentAlongX = Mth.cos(parentAngle);
        float parentAlongZ = Mth.sin(parentAngle);
        float parentCrossX = -parentAlongZ;
        float parentCrossZ = parentAlongX;
        float parentAlong = Mth.lerp(parentT, -parentSpan, parentSpan);
        float parentLateral = parentLaneOffset + vesselOffset(parentT, parentWave, parentPhase, time, parentSpan);
        float startX = parentAlong * parentAlongX + parentLateral * parentCrossX;
        float startZ = parentAlong * parentAlongZ + parentLateral * parentCrossZ;
        float side = leftSide ? 1.0F : -1.0F;
        float branchAngle = parentAngleDegrees + side * (46.0F + 14.0F * Mth.sin(branchPhase + parentT * 5.0F));
        float angle = branchAngle * Mth.DEG_TO_RAD;
        float alongX = Mth.cos(angle);
        float alongZ = Mth.sin(angle);
        float crossX = -alongZ;
        float crossZ = alongX;
        float rootInset = 0.0F;

        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        float[] pointsX = new float[segments + 1];
        float[] pointsZ = new float[segments + 1];
        float[] widths = new float[segments + 1];

        for (int point = 0; point <= segments; point++) {
            float t = point / (float) segments;
            float lateral = (Mth.sin(t * Mth.PI * 1.35F + branchPhase + time * 0.012F) * 0.036F + Mth.sin(t * Mth.PI * 2.4F + branchPhase * 0.53F - time * 0.01F) * 0.016F) * parentSpan;
            pointsX[point] = startX + (branchLength * t - rootInset) * alongX + lateral * crossX;
            pointsZ[point] = startZ + (branchLength * t - rootInset) * alongZ + lateral * crossZ;
            float taper = Mth.clamp(1.12F - t * 1.12F, 0.02F, 1.0F);
            float rootWidth = Mth.clamp(0.22F + t * 4.5F, 0.22F, 1.0F);
            widths[point] = proceduralSize(baseWidth * rootWidth * taper * (0.86F + 0.14F * Mth.sin(time * 0.04F + branchPhase + t * 7.0F)));
        }

        for (int point = 0; point <= segments; point++) {
            int previous = Math.max(0, point - 1);
            int next = Math.min(segments, point + 1);
            float dx = pointsX[next] - pointsX[previous];
            float dz = pointsZ[next] - pointsZ[previous];
            float len = Math.max(0.001F, Mth.sqrt(dx * dx + dz * dz));
            float nx = -dz / len * widths[point];
            float nz = dx / len * widths[point];
            float t = point / (float) segments;
            float rootJoin = Mth.clamp(0.24F + t * 7.5F, 0.0F, 1.0F);
            rootJoin = rootJoin * rootJoin * (3.0F - 2.0F * rootJoin);
            int pointAlpha = proceduralAlpha(alpha * rootJoin * (1.0F - t * 0.92F));

            buffer.addVertex(matrix, pointsX[point] - nx, depth, pointsZ[point] - nz).setColor(red, green, blue, pointAlpha);
            buffer.addVertex(matrix, pointsX[point] + nx, depth, pointsZ[point] + nz).setColor(red, green, blue, pointAlpha);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void renderVesselBranches(PoseStack poseStack, Tesselator tesselator, float depth, float parentSpan, float parentLaneOffset, float parentAngleDegrees, float parentWave, float parentPhase, float time, float baseWidth, int branchCount, float firstParentT, float branchLength, int segments, int seed, int red, int green, int blue, int alpha) {
        int safeBranchCount = Math.max(0, branchCount);
        for (int branch = 0; branch < safeBranchCount; branch++) {
            float step = safeBranchCount <= 1 ? 0.0F : branch / (float) (safeBranchCount - 1);
            float parentT = Mth.clamp(firstParentT + step * 0.48F + Mth.sin(seed * 0.37F + branch * 1.91F) * 0.035F, 0.12F, 0.88F);
            boolean leftSide = (seed + branch) % 2 == 0;
            float lengthScale = 0.76F + (branch % 3) * 0.13F;
            float widthScale = 0.82F - step * 0.18F;
            float branchPhase = parentPhase + branch * 0.73F;
            renderVesselBranch(poseStack, tesselator, depth - branch * 0.006F, parentSpan, parentLaneOffset, parentAngleDegrees, parentWave, parentPhase, branchPhase, time, baseWidth * widthScale, parentT, leftSide, branchLength * lengthScale, segments, red, green, blue, alpha);
        }
    }

    private static void renderNeuralStructures(PoseStack poseStack, Tesselator tesselator, float time,
                                               float skyDistance, int layerCount, ChamberSkyTheme theme) {
        layerCount = depthLayerCount(layerCount);
        if (layerCount <= 0) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.depthMask(false);

        for (int layer = 0; layer < layerCount; layer++) {
            DepthLayer depthLayer = neuralDepthLayer(layer, layerCount, 6000);
            for (int face = 0; face < 6; face++) {
                poseStack.pushPose();
                rotateSkyFace(poseStack, face);
                renderNeuralLayerFace(poseStack, tesselator, time, skyDistance, face, depthLayer, theme);
                poseStack.popPose();
            }
        }

        for (int face = 0; face < 6; face++) {
            poseStack.pushPose();
            rotateSkyFace(poseStack, face);
            renderSynapseFlareEvents(poseStack, tesselator, time, skyDistance, face);
            poseStack.popPose();
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private static void renderNeuralLayerFace(PoseStack poseStack, Tesselator tesselator, float time,
                                             float skyDistance, int face, DepthLayer layer,
                                             ChamberSkyTheme theme) {
        int neurons = 2 + Mth.floor(layer.t() * 3.0F);
        float layerTime = layeredTime(time, layer);
        for (int neuron = 0; neuron < neurons; neuron++) {
            int seed = layer.seedOffset() + face * 89 + neuron * 31;
            float neuronT = neurons <= 1 ? 0.5F : neuron / (float) (neurons - 1);
            float mirrored = shouldMirrorVesselFace(face) ? -1.0F : 1.0F;
            float baseX = Mth.lerp(neuronT, -0.62F, 0.68F) * mirrored;
            float baseZ = switch (neuron % 3) {
                case 0 -> 0.46F;
                case 1 -> -0.44F;
                default -> 0.02F;
            };
            float centerX = (baseX + Mth.sin(layerTime * 0.0035F + seed * 0.31F) * 0.020F) * skyDistance;
            float centerZ = (baseZ + Mth.sin(layerTime * 0.0028F + seed * 0.43F) * 0.020F) * skyDistance;
            float depth = -skyDistance * (layer.depthScale() + neuron * 0.014F + face * 0.0009F);
            float somaPulse = 0.92F + 0.08F * Mth.sin(layerTime * 0.050F + seed);
            float somaRadius = layeredSize(Mth.lerp(layer.t(), 1.00F, 1.82F) * somaPulse, layer);

            int neuralRed = tinted(Mth.floor(Mth.lerp(layer.t(), 72.0F, 148.0F)), theme.neuralTint(), 16);
            int neuralGreen = tinted(Mth.floor(Mth.lerp(layer.t(), 48.0F, 96.0F)), theme.neuralTint(), 8);
            int neuralBlue = tinted(Mth.floor(Mth.lerp(layer.t(), 8.0F, 16.0F)), theme.neuralTint(), 0);
            int neuralCoreRed = tinted(Mth.floor(Mth.lerp(layer.t(), 132.0F, 236.0F)), theme.neuralTint(), 16);
            int neuralCoreGreen = tinted(Mth.floor(Mth.lerp(layer.t(), 96.0F, 178.0F)), theme.neuralTint(), 8);
            int neuralCoreBlue = tinted(Mth.floor(Mth.lerp(layer.t(), 22.0F, 44.0F)), theme.neuralTint(), 0);

            renderNeuronSoma(poseStack, tesselator, centerX, depth + 0.18F, centerZ, somaRadius * Mth.lerp(layer.t(), 2.2F, 3.9F), 24, 16, 0, neuralLayerAlpha(Mth.lerp(layer.t(), 2.0F, 16.0F), layer));
            renderNeuronSoma(poseStack, tesselator, centerX, depth - 0.02F, centerZ, somaRadius * Mth.lerp(layer.t(), 0.82F, 1.55F), neuralRed, neuralGreen, neuralBlue, neuralLayerAlpha(Mth.lerp(layer.t(), 5.0F, 56.0F), layer));
            renderNeuronSoma(poseStack, tesselator, centerX, depth - 0.12F, centerZ, somaRadius * Mth.lerp(layer.t(), 0.34F, 0.62F), neuralCoreRed, neuralCoreGreen, neuralCoreBlue, neuralLayerAlpha(Mth.lerp(layer.t(), 8.0F, 104.0F), layer));

            int axonCount = 3 + Mth.floor(layer.t() * 3.0F) + seed % 2;
            int primaryAxon = Math.floorMod(seed, axonCount);
            for (int axon = 0; axon < axonCount; axon++) {
                boolean primary = axon == primaryAxon;
                float spread = axon * (360.0F / axonCount);
                float faceTwist = switch (face) {
                    case 1 -> 34.0F;
                    case 2 -> -58.0F;
                    case 3 -> 104.0F;
                    case 4 -> 70.0F;
                    case 5 -> -32.0F;
                    default -> 6.0F;
                };
                float angle = spread + faceTwist + Mth.sin(layerTime * 0.0038F + seed + axon) * Mth.lerp(layer.t(), 4.0F, 10.0F);
                float length = layeredSpan(skyDistance * (primary ? Mth.lerp(layer.t(), 0.08F, 0.26F) : Mth.lerp(layer.t(), 0.045F, 0.13F)), layer);
                float width = layeredSize(primary ? Mth.lerp(layer.t(), 0.050F, 0.22F) : Mth.lerp(layer.t(), 0.035F, 0.12F), layer);
                float phase = seed * 2.13F + axon * 4.9F;
                float axonDepth = depth - 0.08F - axon * Mth.lerp(layer.t(), 0.012F, 0.035F);
                int alpha = neuralLayerAlpha(primary ? Mth.lerp(layer.t(), 4.0F, 72.0F) : Mth.lerp(layer.t(), 3.0F, 36.0F), layer);

                renderNeuronAxon(poseStack, tesselator, centerX, axonDepth, centerZ, somaRadius * 0.55F, angle, length, width, phase, layerTime, 28 + Mth.floor(layer.t() * 34.0F), neuralRed, neuralGreen, neuralBlue, alpha);

                if (primary && layer.t() > 0.28F) {
                    renderNeuronImpulse(poseStack, tesselator, centerX, axonDepth - 0.04F, centerZ, somaRadius * 0.55F, angle, length, phase, layerTime, layer.t() > 0.66F ? 2 : 1, true, neuralLayerVisibility(layer));
                }
            }
        }
    }

    private static void renderNeuronSoma(PoseStack poseStack, Tesselator tesselator, float centerX, float depth, float centerZ, float radius, int red, int green, int blue, int alpha) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        int segments = 28;
        int scaledAlpha = proceduralAlpha(alpha);
        float scaledRadius = proceduralSize(radius);
        for (int segment = 0; segment < segments; segment++) {
            float angleA = segment / (float) segments * Mth.TWO_PI;
            float angleB = (segment + 1) / (float) segments * Mth.TWO_PI;
            buffer.addVertex(matrix, centerX, depth, centerZ).setColor(red, green, blue, scaledAlpha);
            buffer.addVertex(matrix, centerX + Mth.cos(angleA) * scaledRadius, depth, centerZ + Mth.sin(angleA) * scaledRadius).setColor(red, green, blue, 0);
            buffer.addVertex(matrix, centerX + Mth.cos(angleB) * scaledRadius, depth, centerZ + Mth.sin(angleB) * scaledRadius).setColor(red, green, blue, 0);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void renderNeuronAxon(PoseStack poseStack, Tesselator tesselator, float centerX, float depth, float centerZ, float rootOffset, float angleDegrees, float length, float baseWidth, float phase, float time, int segments, int red, int green, int blue, int alpha) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        float angle = angleDegrees * Mth.DEG_TO_RAD;
        float alongX = Mth.cos(angle);
        float alongZ = Mth.sin(angle);
        float crossX = -alongZ;
        float crossZ = alongX;
        float[] pointsX = new float[segments + 1];
        float[] pointsZ = new float[segments + 1];
        float[] widths = new float[segments + 1];

        for (int point = 0; point <= segments; point++) {
            float t = point / (float) segments;
            float rootEase = Mth.clamp(t * 3.0F, 0.0F, 1.0F);
            float wave = (Mth.sin(t * Mth.TWO_PI * 1.25F + phase + time * 0.017F) * 0.052F + Mth.sin(t * Mth.TWO_PI * 2.9F + phase * 0.47F - time * 0.013F) * 0.022F) * length;
            pointsX[point] = centerX + (rootOffset + length * t) * alongX + wave * rootEase * crossX;
            pointsZ[point] = centerZ + (rootOffset + length * t) * alongZ + wave * rootEase * crossZ;
            float taper = Mth.clamp(1.08F - t * 1.04F, 0.025F, 1.0F);
            widths[point] = proceduralSize(baseWidth * taper * (0.86F + 0.14F * Mth.sin(time * 0.05F + phase + t * 9.0F)));
        }

        for (int point = 0; point <= segments; point++) {
            int previous = Math.max(0, point - 1);
            int next = Math.min(segments, point + 1);
            float dx = pointsX[next] - pointsX[previous];
            float dz = pointsZ[next] - pointsZ[previous];
            float len = Math.max(0.001F, Mth.sqrt(dx * dx + dz * dz));
            float nx = -dz / len * widths[point];
            float nz = dx / len * widths[point];
            float t = point / (float) segments;
            float rootJoin = Mth.clamp(t * 5.0F, 0.0F, 1.0F);
            rootJoin = rootJoin * rootJoin * (3.0F - 2.0F * rootJoin);
            int pointAlpha = proceduralAlpha(alpha * rootJoin * (1.0F - t * 0.82F));

            buffer.addVertex(matrix, pointsX[point] - nx, depth, pointsZ[point] - nz).setColor(red, green, blue, pointAlpha);
            buffer.addVertex(matrix, pointsX[point] + nx, depth, pointsZ[point] + nz).setColor(red, green, blue, pointAlpha);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void renderNeuronImpulse(PoseStack poseStack, Tesselator tesselator, float centerX, float depth, float centerZ, float rootOffset, float angleDegrees, float length, float phase, float time, int sparkCount, boolean primary, float visibility) {
        float angle = angleDegrees * Mth.DEG_TO_RAD;
        float alongX = Mth.cos(angle);
        float alongZ = Mth.sin(angle);
        float crossX = -alongZ;
        float crossZ = alongX;

        for (int spark = 0; spark < sparkCount; spark++) {
            float travel = (time * (primary ? 0.018F : 0.026F) + phase * 0.071F + spark * 0.37F) % 1.0F;
            float t = Mth.clamp(travel, 0.02F, 0.98F);
            float rootEase = Mth.clamp(t * 3.0F, 0.0F, 1.0F);
            float wave = (Mth.sin(t * Mth.TWO_PI * 1.25F + phase + time * 0.017F) * 0.052F + Mth.sin(t * Mth.TWO_PI * 2.9F + phase * 0.47F - time * 0.013F) * 0.022F) * length;
            float x = centerX + (rootOffset + length * t) * alongX + wave * rootEase * crossX;
            float z = centerZ + (rootOffset + length * t) * alongZ + wave * rootEase * crossZ;
            float pulse = 0.65F + 0.35F * Mth.sin(time * 0.32F + phase + spark * 1.9F);
            float fade = Mth.clamp(Mth.sin(t * Mth.PI) * 1.35F, 0.0F, 1.0F);
            float radius = (primary ? 0.86F : 0.58F) * pulse;
            int glowAlpha = (int) Mth.clamp((primary ? 52 : 34) * fade * visibility, 0, 255);
            int coreAlpha = (int) Mth.clamp((primary ? 118 : 84) * fade * visibility, 0, 255);

            renderNeuronSoma(poseStack, tesselator, x, depth - 0.03F, z, radius * 2.35F, 104, 70, 10, glowAlpha);
            renderNeuronSoma(poseStack, tesselator, x, depth - 0.07F, z, radius * 0.78F, 240, 202, 94, coreAlpha);
        }
    }

    private static void renderSynapseFlareEvents(PoseStack poseStack, Tesselator tesselator, float time, float skyDistance, int face) {
        for (int pair = 0; pair < 2; pair++) {
            int startNeuron = pair;
            int endNeuron = pair + 1;
            int startSeed = face * 61 + startNeuron * 17;
            int endSeed = face * 61 + endNeuron * 17;
            float period = 152.0F + face * 7.0F + pair * 19.0F;
            float eventTime = (time + face * 31.0F + pair * 47.0F) % period;
            float activeWindow = 30.0F;
            if (eventTime > activeWindow) {
                continue;
            }

            float startX = neuronCenterX(time, skyDistance, face, startNeuron);
            float startZ = neuronCenterZ(time, skyDistance, face, startNeuron);
            float startDepth = neuronDepth(skyDistance, face, startNeuron) - 0.18F;
            float endX = neuronCenterX(time, skyDistance, face, endNeuron);
            float endZ = neuronCenterZ(time, skyDistance, face, endNeuron);
            float endDepth = neuronDepth(skyDistance, face, endNeuron) - 0.18F;
            float startRadius = neuronSomaRadius(time, startSeed);
            float endRadius = neuronSomaRadius(time, endSeed);
            float[] startPoint = primaryAxonPoint(startX, startZ, startRadius, skyDistance, face, startSeed, time, 0.68F);
            float[] endPoint = primaryAxonPoint(endX, endZ, endRadius, skyDistance, face, endSeed, time, 0.46F);
            float fadeIn = Mth.clamp(eventTime / 6.0F, 0.0F, 1.0F);
            float fadeOut = Mth.clamp((activeWindow - eventTime) / 9.0F, 0.0F, 1.0F);
            float intensity = fadeIn * fadeOut;
            float travel = Mth.clamp(eventTime / activeWindow, 0.0F, 1.0F);

            renderSynapseChain(poseStack, tesselator, startPoint[0], startDepth, startPoint[1], endPoint[0], endDepth, endPoint[1], time, startSeed + pair * 23, travel, intensity);
        }
    }

    private static float neuronCenterX(float time, float skyDistance, int face, int neuron) {
        int seed = face * 61 + neuron * 17;
        float mirrored = shouldMirrorVesselFace(face) ? -1.0F : 1.0F;
        float baseX = switch (neuron) {
            case 0 -> -0.62F;
            case 1 -> 0.12F;
            default -> 0.68F;
        } * mirrored;
        return (baseX + Mth.sin(time * 0.006F + seed * 0.41F) * 0.018F) * skyDistance;
    }

    private static float neuronCenterZ(float time, float skyDistance, int face, int neuron) {
        int seed = face * 61 + neuron * 17;
        float baseZ = switch (neuron) {
            case 0 -> 0.42F;
            case 1 -> -0.18F;
            default -> -0.55F;
        };
        return (baseZ + Mth.sin(time * 0.0045F + seed * 0.67F) * 0.018F) * skyDistance;
    }

    private static float neuronDepth(float skyDistance, int face, int neuron) {
        return -skyDistance * (0.715F + neuron * 0.020F + face * 0.002F);
    }

    private static float neuronSomaRadius(float time, int seed) {
        return (1.22F + (seed % 4) * 0.19F) * (0.9F + 0.1F * Mth.sin(time * 0.055F + seed));
    }

    private static float[] primaryAxonPoint(float centerX, float centerZ, float somaRadius, float skyDistance, int face, int seed, float time, float t) {
        int axonCount = 4 + seed % 2;
        int primaryAxon = seed % axonCount;
        float spread = (360.0F / axonCount) * primaryAxon;
        float faceTwist = switch (face) {
            case 1 -> 35.0F;
            case 2 -> -62.0F;
            case 3 -> 108.0F;
            case 4 -> 74.0F;
            case 5 -> -28.0F;
            default -> 0.0F;
        };
        float angleDegrees = spread + faceTwist + Mth.sin(time * 0.004F + seed + primaryAxon) * 10.0F;
        float length = skyDistance * (0.27F + (seed % 3) * 0.030F);
        float phase = seed * 3.19F + primaryAxon * 5.7F;
        float angle = angleDegrees * Mth.DEG_TO_RAD;
        float alongX = Mth.cos(angle);
        float alongZ = Mth.sin(angle);
        float crossX = -alongZ;
        float crossZ = alongX;
        float rootOffset = somaRadius * 0.55F;
        float rootEase = Mth.clamp(t * 3.0F, 0.0F, 1.0F);
        float wave = (Mth.sin(t * Mth.TWO_PI * 1.25F + phase + time * 0.017F) * 0.052F + Mth.sin(t * Mth.TWO_PI * 2.9F + phase * 0.47F - time * 0.013F) * 0.022F) * length;
        return new float[]{centerX + (rootOffset + length * t) * alongX + wave * rootEase * crossX, centerZ + (rootOffset + length * t) * alongZ + wave * rootEase * crossZ};
    }

    private static void renderSynapseChain(PoseStack poseStack, Tesselator tesselator, float startX, float startDepth, float startZ, float endX, float endDepth, float endZ, float time, int seed, float travel, float intensity) {
        int nodes = 8;
        float bendX = Mth.sin(time * 0.018F + seed) * 7.0F;
        float bendZ = Mth.cos(time * 0.015F + seed * 0.37F) * 7.0F;
        float previousX = startX;
        float previousZ = startZ;
        float previousDepth = startDepth;

        for (int node = 1; node <= nodes; node++) {
            float t = node / (float) nodes;
            float arch = Mth.sin(t * Mth.PI);
            float jitter = Mth.sin(t * Mth.TWO_PI * 2.7F + seed + time * 0.02F) * 2.4F;
            float x = Mth.lerp(t, startX, endX) + bendX * arch + jitter;
            float z = Mth.lerp(t, startZ, endZ) + bendZ * arch - jitter * 0.55F;
            float depth = Mth.lerp(t, startDepth, endDepth) - arch * 0.18F;
            float headDistance = Mth.abs(t - travel);
            float signal = Mth.clamp(1.0F - headDistance / 0.22F, 0.0F, 1.0F);
            signal = signal * signal * (3.0F - 2.0F * signal) * intensity;
            if (signal > 0.035F) {
                int linkAlpha = (int) Mth.clamp(110.0F * signal, 0.0F, 180.0F);
                renderSynapseLink(poseStack, tesselator, previousX, previousDepth, previousZ, x, depth, z, 0.10F + signal * 0.16F, linkAlpha);
                renderNeuronSoma(poseStack, tesselator, x, depth - 0.03F, z, 1.85F + signal * 2.25F, 188, 122, 16, (int) (44.0F * signal));
                renderNeuronSoma(poseStack, tesselator, x, depth - 0.06F, z, 0.44F + signal * 0.5F, 255, 236, 154, (int) (176.0F * signal));
            }
            previousX = x;
            previousZ = z;
            previousDepth = depth;
        }
    }

    private static void renderSynapseLink(PoseStack poseStack, Tesselator tesselator, float startX, float startDepth, float startZ, float endX, float endDepth, float endZ, float width, int alpha) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        float dx = endX - startX;
        float dz = endZ - startZ;
        float len = Math.max(0.001F, Mth.sqrt(dx * dx + dz * dz));
        float scaledWidth = proceduralSize(width);
        int scaledAlpha = proceduralAlpha(alpha);
        float nx = -dz / len * scaledWidth;
        float nz = dx / len * scaledWidth;

        buffer.addVertex(matrix, startX - nx, startDepth, startZ - nz).setColor(255, 244, 170, 0);
        buffer.addVertex(matrix, startX + nx, startDepth, startZ + nz).setColor(255, 244, 170, scaledAlpha);
        buffer.addVertex(matrix, endX + nx, endDepth, endZ + nz).setColor(255, 244, 170, scaledAlpha);
        buffer.addVertex(matrix, startX - nx, startDepth, startZ - nz).setColor(255, 244, 170, 0);
        buffer.addVertex(matrix, endX + nx, endDepth, endZ + nz).setColor(255, 244, 170, scaledAlpha);
        buffer.addVertex(matrix, endX - nx, endDepth, endZ - nz).setColor(255, 244, 170, 0);

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void renderSilentArchonDepthEffects(PoseStack poseStack, Tesselator tesselator, float f,
                                                       float skyDistance, ChamberSkyTheme theme) {
        if (ChamberOfWillManager.THEME_SILENT_ARCHON.equals(theme.id())) {
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.depthMask(false);
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
            renderSilentArchonCloudStrata(poseStack, tesselator, f, skyDistance, theme);
            renderSilentArchonCloudDeck(poseStack, tesselator, f, skyDistance, theme);
            renderSilentArchonDistantMonolithSilhouettes(poseStack, tesselator, f, skyDistance);
            RenderSystem.enableCull();
        }
    }

    private static void renderSilentArchonCloudStrata(PoseStack poseStack, Tesselator tesselator, float time,
                                                      float skyDistance, ChamberSkyTheme theme) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, theme.cloudTexture());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int strataLayers = 6;
        for (int layer = 0; layer < strataLayers; layer++) {
            float t = layer / (float) Math.max(1, strataLayers - 1);
            float drift = time * (0.00055F + layer * 0.00014F);
            float angle = layer * 61.0F + Mth.sin(drift * 0.77F + layer) * 7.0F;
            float distance = skyDistance * Mth.lerp(t, 0.62F, 0.92F);
            float y = Mth.lerp(t, skyDistance * 0.32F, -skyDistance * 0.10F)
                    + Mth.sin(drift * 2.3F + layer * 1.37F) * skyDistance * 0.035F;
            float centerX = Mth.cos(angle * Mth.DEG_TO_RAD) * distance
                    + Mth.sin(drift + layer) * skyDistance * 0.07F;
            float centerZ = Mth.sin(angle * Mth.DEG_TO_RAD) * distance
                    + Mth.cos(drift * 0.83F + layer) * skyDistance * 0.07F;
            float yaw = angle + 88.0F + Mth.sin(drift * 1.4F) * 4.0F;
            float pitch = Mth.lerp(t, -11.0F, 8.0F) + Mth.cos(drift * 1.9F + layer) * 2.5F;
            float tilt = Mth.lerp(t, -18.0F, 19.0F) + Mth.sin(drift * 1.6F + layer * 0.91F) * 4.5F;
            float halfSpan = skyDistance * Mth.lerp(t, 0.70F, 1.18F);
            float halfDepth = skyDistance * Mth.lerp(t, 0.080F, 0.145F);
            int alpha = (int) Mth.clamp(Mth.lerp(t, 52.0F, 88.0F), 0.0F, 118.0F);
            int red = Mth.floor(Mth.lerp(t, 48.0F, 74.0F));
            int green = Mth.floor(Mth.lerp(t, 65.0F, 94.0F));
            int blue = Mth.floor(Mth.lerp(t, 66.0F, 92.0F));

            poseStack.pushPose();
            poseStack.translate(centerX, y, centerZ);
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees(tilt));
            renderSilentArchonCloudStratumPlane(poseStack, tesselator, halfSpan, halfDepth,
                    drift + layer * 0.19F, red, green, blue, alpha);
            poseStack.popPose();
        }
    }

    private static void renderSilentArchonCloudStratumPlane(PoseStack poseStack, Tesselator tesselator,
                                                            float halfSpan, float halfDepth, float uvScroll,
                                                            int red, int green, int blue, int alpha) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        float u0 = uvScroll % 1.0F;
        float v0 = (uvScroll * 0.37F) % 1.0F;
        float u1 = u0 + 3.65F;
        float v1 = v0 + 0.72F;
        buffer.addVertex(matrix, -halfSpan, 0.0F, -halfDepth).setUv(u0, v0).setColor(red, green, blue, 0);
        buffer.addVertex(matrix, -halfSpan, 0.0F, halfDepth).setUv(u0, v1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, halfSpan, 0.0F, halfDepth).setUv(u1, v1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, halfSpan, 0.0F, -halfDepth).setUv(u1, v0).setColor(red, green, blue, 0);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void renderSilentArchonCloudDeck(PoseStack poseStack, Tesselator tesselator, float time,
                                                    float skyDistance, ChamberSkyTheme theme) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, theme.cloudTexture());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        float deckY = -skyDistance * 0.72F;
        int deckLayers = 7;
        for (int layer = 0; layer < deckLayers; layer++) {
            float t = layer / (float) Math.max(1, deckLayers - 1);
            float drift = time * (0.00036F + layer * 0.00006F);
            float y = deckY + skyDistance * Mth.lerp(t, -0.075F, 0.165F)
                    + Mth.sin(time * 0.0018F + layer * 1.13F) * skyDistance * 0.010F;
            float x = Mth.sin(time * 0.0011F + layer * 0.91F) * skyDistance * 0.035F;
            float z = Mth.cos(time * 0.0009F + layer * 1.07F) * skyDistance * 0.040F;
            float yaw = layer * 31.0F + Mth.sin(time * 0.0014F + layer) * 4.0F;
            float pitch = Mth.lerp(t, -2.5F, 3.0F) + Mth.cos(time * 0.0012F + layer) * 1.2F;
            float tilt = Mth.lerp(t, -4.0F, 5.0F) + Mth.sin(time * 0.0015F + layer * 0.77F) * 1.5F;
            float halfSpan = skyDistance * Mth.lerp(t, 0.98F, 1.42F);
            float halfDepth = skyDistance * Mth.lerp(t, 0.78F, 1.18F);
            float uvScale = Mth.lerp(t, 3.0F, 4.8F);
            int alpha = (int) Mth.clamp(Mth.lerp(t, 118.0F, 38.0F), 0.0F, 136.0F);
            int red = Mth.floor(Mth.lerp(t, 54.0F, 82.0F));
            int green = Mth.floor(Mth.lerp(t, 76.0F, 102.0F));
            int blue = Mth.floor(Mth.lerp(t, 76.0F, 100.0F));

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees(tilt));
            renderSilentArchonCloudDeckPlane(poseStack, tesselator, halfSpan, halfDepth,
                    drift + layer * 0.27F, uvScale, red, green, blue, alpha, 8);
            poseStack.popPose();
        }
    }

    private static void renderSilentArchonCloudDeckPlane(PoseStack poseStack, Tesselator tesselator,
                                                         float halfSpan, float halfDepth, float uvScroll,
                                                         float uvScale, int red, int green, int blue,
                                                         int alpha, int cells) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        int grid = Math.max(2, cells);
        for (int xCell = 0; xCell < grid; xCell++) {
            for (int zCell = 0; zCell < grid; zCell++) {
                float x0T = xCell / (float) grid;
                float x1T = (xCell + 1) / (float) grid;
                float z0T = zCell / (float) grid;
                float z1T = (zCell + 1) / (float) grid;
                float x0 = Mth.lerp(x0T, -halfSpan, halfSpan);
                float x1 = Mth.lerp(x1T, -halfSpan, halfSpan);
                float z0 = Mth.lerp(z0T, -halfDepth, halfDepth);
                float z1 = Mth.lerp(z1T, -halfDepth, halfDepth);
                float u0 = x0T * uvScale + uvScroll;
                float u1 = x1T * uvScale + uvScroll;
                float v0 = z0T * uvScale * 0.72F + uvScroll * 0.41F;
                float v1 = z1T * uvScale * 0.72F + uvScroll * 0.41F;

                buffer.addVertex(matrix, x0, 0.0F, z0).setUv(u0, v0)
                        .setColor(red, green, blue, cloudDeckAlpha(x0T, z0T, alpha));
                buffer.addVertex(matrix, x0, 0.0F, z1).setUv(u0, v1)
                        .setColor(red, green, blue, cloudDeckAlpha(x0T, z1T, alpha));
                buffer.addVertex(matrix, x1, 0.0F, z1).setUv(u1, v1)
                        .setColor(red, green, blue, cloudDeckAlpha(x1T, z1T, alpha));
                buffer.addVertex(matrix, x1, 0.0F, z0).setUv(u1, v0)
                        .setColor(red, green, blue, cloudDeckAlpha(x1T, z0T, alpha));
            }
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static int cloudDeckAlpha(float xT, float zT, int alpha) {
        float xFade = 1.0F - Math.abs(xT - 0.5F) * 2.0F;
        float zFade = 1.0F - Math.abs(zT - 0.5F) * 2.0F;
        float edgeFade = Mth.clamp(Math.min(xFade, zFade) * 1.7F, 0.0F, 1.0F);
        float unevenness = 0.78F + 0.22F * Mth.sin((xT * 7.0F + zT * 5.0F) * Mth.PI);
        return (int) Mth.clamp(alpha * edgeFade * unevenness, 0.0F, 160.0F);
    }

    private static void renderSilentArchonForegroundStormClouds(PoseStack poseStack, float time,
                                                                float skyDistance, ChamberSkyTheme theme) {
        if (!ChamberOfWillManager.THEME_SILENT_ARCHON.equals(theme.id())) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        Random random = new Random(91077L);
        int count = Math.max(0, theme.monolithPillarCount());
        int pillarPairs = count / 2;
        int pillar = 0;
        for (int pair = 0; pair < pillarPairs; pair++) {
            float ringT = (pair + 0.5F) / (float) Math.max(1, pillarPairs);
            float angle = ringT * Mth.TWO_PI * 0.5F + Mth.lerp(random.nextFloat(), -0.12F, 0.12F);
            float distance = skyDistance * Mth.lerp(random.nextFloat(), 0.34F, 0.94F);
            float radius = skyDistance * Mth.lerp(random.nextFloat(), 0.014F, 0.038F);
            float baseY = -skyDistance * Mth.lerp(random.nextFloat(), 0.98F, 1.18F);
            random.nextFloat(); // topY, consumed to stay in lockstep with foreground pillar placement.
            float twist = random.nextFloat() * Mth.TWO_PI;
            random.nextInt(3); // sideCount, consumed to stay in lockstep with foreground pillar placement.
            renderSilentArchonForegroundStormCloud(poseStack, bufferSource, time, pillar++, angle, distance, radius,
                    baseY, skyDistance, twist);

            float oppositeAngle = angle + Mth.TWO_PI * 0.5F;
            float oppositeDistance = distance * Mth.lerp(random.nextFloat(), 0.94F, 1.06F);
            float oppositeRadius = radius * Mth.lerp(random.nextFloat(), 0.92F, 1.08F);
            float oppositeBaseY = baseY * Mth.lerp(random.nextFloat(), 0.96F, 1.04F);
            random.nextFloat(); // opposite topY, consumed to stay in lockstep with foreground pillar placement.
            renderSilentArchonForegroundStormCloud(poseStack, bufferSource, time, pillar++, oppositeAngle,
                    oppositeDistance, oppositeRadius, oppositeBaseY, skyDistance, twist + 0.73F);
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }

    private static void renderSilentArchonForegroundStormCloud(PoseStack poseStack,
                                                               MultiBufferSource.BufferSource bufferSource,
                                                               float time, int pillar, float angle, float distance,
                                                               float radius, float baseY, float skyDistance,
                                                               float twist) {
        float x = Mth.cos(angle) * distance;
        float z = Mth.sin(angle) * distance;
        float distanceT = Mth.clamp(distance / skyDistance, 0.0F, 1.0F);
        // anchor clouds at the visible lower-shaft zone (~18-33° below horizontal), not the underground pillar base
        float cloudCenterY = -skyDistance * Mth.lerp(distanceT, 0.36F, 0.56F);
        float stormSeed = (pillar * 53 + 17) / 997.0F;
        float density = Mth.lerp(distanceT, 1.36F, 0.94F);
        RenderType renderType = HemoRenderTypes.silentArchonStormCloud(time * 0.05F, stormSeed, density);
        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        Matrix4f matrix = poseStack.last().pose();

        int bankLayers = 5;
        int cellColumns = 6;
        int cellRows = 4;
        float bankSpan = radius * Mth.lerp(distanceT, 7.2F, 11.4F) + skyDistance * 0.014F;
        for (int layer = 0; layer < bankLayers; layer++) {
            float layerT = layer / (float) Math.max(1, bankLayers - 1);
            float layerAngle = twist + layer * 0.37F + Mth.sin(time * 0.0013F + pillar + layer) * 0.08F;
            float rightX = Mth.cos(layerAngle);
            float rightZ = Mth.sin(layerAngle);
            float forwardX = -rightZ;
            float forwardZ = rightX;
            float layerDrift = time * (0.00024F + layer * 0.000022F);
            float centerX = x + Mth.sin(layerDrift + pillar * 0.61F + layer) * radius * 1.75F;
            float centerZ = z + Mth.cos(layerDrift * 0.83F + pillar * 0.47F + layer) * radius * 1.75F;
            float centerY = cloudCenterY + Mth.lerp(layerT, -radius * 2.35F, radius * 2.35F)
                    + Mth.sin(time * 0.0021F + pillar + layer * 0.71F) * radius * 0.58F;
            float halfWidth = bankSpan * Mth.lerp(layerT, 1.18F, 0.72F);
            float halfDepth = bankSpan * Mth.lerp(layerT, 0.56F, 0.34F);
            int alpha = (int) Mth.clamp(Mth.lerp(distanceT, 128.0F, 96.0F) * Mth.lerp(layerT, 1.0F, 0.72F),
                    54.0F, 138.0F);
            int red = Mth.floor(Mth.lerp(distanceT, 188.0F, 166.0F));
            int green = Mth.floor(Mth.lerp(distanceT, 204.0F, 184.0F));
            int blue = Mth.floor(Mth.lerp(distanceT, 208.0F, 190.0F));

            renderSilentArchonStormCloudBankLayer(consumer, matrix, centerX, centerY, centerZ, rightX, rightZ,
                    forwardX, forwardZ, halfWidth, halfDepth, layerDrift + pillar * 0.137F, red, green, blue,
                    alpha, cellColumns, cellRows);
        }
        bufferSource.endBatch(renderType);
    }

    private static void renderSilentArchonStormCloudBankLayer(VertexConsumer consumer, Matrix4f matrix,
                                                              float centerX, float centerY, float centerZ,
                                                              float rightX, float rightZ, float forwardX,
                                                              float forwardZ, float halfWidth, float halfDepth,
                                                              float ripple, int red, int green, int blue,
                                                              int alpha, int cellColumns, int cellRows) {
        float cellWidth = halfWidth * 2.0F / cellColumns;
        float cellDepth = halfDepth * 2.0F / cellRows;
        for (int xCell = 0; xCell < cellColumns; xCell++) {
            float xT = (xCell + 0.5F) / cellColumns;
            float xOffset = Mth.lerp(xT, -halfWidth, halfWidth);
            float xEdge = 1.0F - Math.abs(xT - 0.5F) * 2.0F;
            for (int zCell = 0; zCell < cellRows; zCell++) {
                float zT = (zCell + 0.5F) / cellRows;
                float zOffset = Mth.lerp(zT, -halfDepth, halfDepth);
                float zEdge = 1.0F - Math.abs(zT - 0.5F) * 2.0F;
                float noise = Mth.sin((xCell * 31.7F + zCell * 17.3F + ripple * 149.0F) * 0.37F) * 0.5F + 0.5F;
                float edgeFade = Mth.clamp(Math.min(xEdge, zEdge) * 1.85F + noise * 0.22F, 0.0F, 1.0F);
                if (edgeFade < 0.20F) {
                    continue;
                }

                float jitterRight = (noise - 0.5F) * cellWidth * 0.56F;
                float jitterForward = Mth.cos(noise * 9.1F + ripple * 11.0F) * cellDepth * 0.38F;
                float cellCenterX = centerX + rightX * (xOffset + jitterRight) + forwardX * (zOffset + jitterForward);
                float cellCenterZ = centerZ + rightZ * (xOffset + jitterRight) + forwardZ * (zOffset + jitterForward);
                float cellCenterY = centerY
                        + Mth.sin(ripple * 13.0F + xCell * 0.83F + zCell * 1.17F) * halfDepth * 0.052F;
                float cellHalfWidth = cellWidth * Mth.lerp(noise, 0.54F, 0.84F);
                float cellHalfDepth = cellDepth * Mth.lerp(1.0F - noise, 0.62F, 0.96F);
                int cellAlpha = (int) Mth.clamp(alpha * edgeFade * Mth.lerp(noise, 0.68F, 1.10F), 0.0F, 132.0F);
                if (cellAlpha < 12) {
                    continue;
                }

                renderSilentArchonStormCloudBankCell(consumer, matrix, cellCenterX, cellCenterY, cellCenterZ,
                        rightX, rightZ, forwardX, forwardZ, cellHalfWidth, cellHalfDepth, ripple + noise * 0.71F,
                        red, green, blue, cellAlpha);
            }
        }
    }

    private static void renderSilentArchonStormCloudBankCell(VertexConsumer consumer, Matrix4f matrix,
                                                             float centerX, float centerY, float centerZ,
                                                             float rightX, float rightZ, float forwardX,
                                                             float forwardZ, float halfWidth, float halfDepth,
                                                             float ripple, int red, int green, int blue,
                                                             int alpha) {
        float y0 = centerY + Mth.sin(ripple * 15.0F) * halfDepth * 0.080F;
        float y1 = centerY + Mth.cos(ripple * 11.0F + 1.7F) * halfDepth * 0.070F;
        float y2 = centerY + Mth.sin(ripple * 13.0F + 2.3F) * halfDepth * 0.085F;
        float y3 = centerY + Mth.cos(ripple * 9.0F + 0.9F) * halfDepth * 0.060F;

        float x0 = centerX - rightX * halfWidth - forwardX * halfDepth;
        float z0 = centerZ - rightZ * halfWidth - forwardZ * halfDepth;
        float x1 = centerX - rightX * halfWidth + forwardX * halfDepth;
        float z1 = centerZ - rightZ * halfWidth + forwardZ * halfDepth;
        float x2 = centerX + rightX * halfWidth + forwardX * halfDepth;
        float z2 = centerZ + rightZ * halfWidth + forwardZ * halfDepth;
        float x3 = centerX + rightX * halfWidth - forwardX * halfDepth;
        float z3 = centerZ + rightZ * halfWidth - forwardZ * halfDepth;

        consumer.addVertex(matrix, x0, y0, z0).setUv(0.0F, 1.0F).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, x1, y1, z1).setUv(0.0F, 0.0F).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, x2, y2, z2).setUv(1.0F, 0.0F).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, x3, y3, z3).setUv(1.0F, 1.0F).setColor(red, green, blue, alpha);
    }

    private static void renderSilentArchonDistantMonolithSilhouettes(PoseStack poseStack, Tesselator tesselator,
                                                                     float time, float skyDistance) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Random random = new Random(448921L);
        int silhouetteCount = 28;
        for (int silhouette = 0; silhouette < silhouetteCount; silhouette++) {
            float depthT = random.nextFloat();
            float angle = silhouette / (float) silhouetteCount * Mth.TWO_PI
                    + Mth.lerp(random.nextFloat(), -0.16F, 0.16F);
            float distance = skyDistance * Mth.lerp(depthT, 0.96F, 0.72F);
            float x = Mth.cos(angle) * distance;
            float z = Mth.sin(angle) * distance;
            float radius = skyDistance * Mth.lerp(random.nextFloat(), 0.006F, 0.015F)
                    * Mth.lerp(depthT, 0.72F, 1.08F);
            float baseY = -skyDistance * Mth.lerp(random.nextFloat(), 0.95F, 1.24F);
            float topY = skyDistance * Mth.lerp(random.nextFloat(), 0.16F, 0.84F)
                    - Mth.lerp(depthT, skyDistance * 0.24F, 0.0F);
            float sway = Mth.sin(time * 0.00075F + silhouette * 0.61F) * 0.012F;
            int alpha = (int) Mth.clamp(Mth.lerp(depthT, 26.0F, 82.0F)
                    * Mth.lerp(random.nextFloat(), 0.62F, 1.05F), 10.0F, 92.0F);
            int red = random.nextInt(5) == 0 ? 18 : 9;
            int green = random.nextInt(4) == 0 ? 17 : 11;
            int blue = random.nextInt(3) == 0 ? 18 : 13;
            int sideCount = 4 + random.nextInt(3);
            renderDistantMonolithSilhouetteGeometry(buffer, matrix, x, baseY, z, radius, topY,
                    angle + sway, sideCount, red, green, blue, alpha, silhouette);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void renderDistantMonolithSilhouetteGeometry(BufferBuilder buffer, Matrix4f matrix,
                                                               float centerX, float baseY, float centerZ,
                                                               float radius, float topY, float twist,
                                                               int sideCount, int red, int green, int blue,
                                                               int alpha, int seed) {
        sideCount = Math.max(3, sideCount);
        float[] cornerX = new float[sideCount + 1];
        float[] cornerZ = new float[sideCount + 1];
        float[] cornerTop = new float[sideCount + 1];
        for (int corner = 0; corner < sideCount; corner++) {
            float angle = twist + corner / (float) sideCount * Mth.TWO_PI;
            float cornerRadius = radius * (0.82F + ((seed + corner * 11) % 7) * 0.038F);
            cornerX[corner] = centerX + Mth.cos(angle) * cornerRadius;
            cornerZ[corner] = centerZ + Mth.sin(angle) * cornerRadius;
            cornerTop[corner] = topY - ((seed + corner * 17) % 6) * radius * 0.22F;
        }
        cornerX[sideCount] = cornerX[0];
        cornerZ[sideCount] = cornerZ[0];
        cornerTop[sideCount] = cornerTop[0];

        int baseAlpha = Math.max(0, alpha / 4);
        int topAlpha = Math.max(0, (int) (alpha * 0.72F));
        for (int side = 0; side < sideCount; side++) {
            float x0 = cornerX[side];
            float z0 = cornerZ[side];
            float x1 = cornerX[side + 1];
            float z1 = cornerZ[side + 1];
            float top0 = cornerTop[side];
            float top1 = cornerTop[side + 1];

            buffer.addVertex(matrix, x0, baseY, z0).setColor(red, green, blue, baseAlpha);
            buffer.addVertex(matrix, x1, baseY, z1).setColor(red, green, blue, baseAlpha);
            buffer.addVertex(matrix, x1, top1, z1).setColor(red, green, blue, topAlpha);
            buffer.addVertex(matrix, x0, top0, z0).setColor(red, green, blue, alpha);
        }
    }

    private static void rotateSkyFace(PoseStack poseStack, int face) {
        if (face == 1) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        } else if (face == 2) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        } else if (face == 3) {
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        } else if (face == 4) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        } else if (face == 5) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
        }
    }

    private static float vesselOffset(float t, float wave, float phase, float time, float span) {
        float main = Mth.sin(t * Mth.TWO_PI * wave + phase + time * 0.011F);
        float drift = Mth.sin(t * Mth.TWO_PI * (wave * 1.65F + 0.23F) + phase * 0.37F - time * 0.016F);
        float flutter = Mth.sin(t * Mth.TWO_PI * (wave * 2.45F + 0.38F) + phase * 0.21F + time * 0.008F);
        return (main * 0.185F + drift * 0.082F + flutter * 0.028F) * span;
    }

    private static void renderSilentArchonVolumetricFog(PoseStack poseStack, Tesselator tesselator, float time,
                                                        float skyDistance, ChamberSkyTheme theme) {
        if (!isSilentArchonFogTheme()) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        int fogSlices = 18;
        float fogFloorY = -skyDistance * 0.74F;
        for (int slice = 0; slice < fogSlices; slice++) {
            float t = slice / (float) Math.max(1, fogSlices - 1);
            float phase = slice * 0.71F + time * 0.0019F;
            float angle = slice / (float) fogSlices * Mth.TWO_PI
                    + Mth.sin(phase * 0.67F) * 0.22F;
            float distance = skyDistance * Mth.lerp(t, 0.10F, 0.82F);
            float x = Mth.cos(angle) * distance + Mth.sin(time * 0.0013F + slice) * skyDistance * 0.045F;
            float z = Mth.sin(angle) * distance + Mth.cos(time * 0.0011F + slice) * skyDistance * 0.045F;
            float baseY = fogFloorY + skyDistance * Mth.lerp(t, -0.055F, 0.060F)
                    + Mth.sin(time * 0.0022F + slice * 0.63F) * skyDistance * 0.010F;
            float topY = baseY + skyDistance * Mth.lerp(t, 0.30F, 0.47F);
            float halfSpan = skyDistance * Mth.lerp(t, 0.36F, 0.82F);
            float depthBow = skyDistance * Mth.lerp(t, 0.035F, 0.080F);
            float yaw = angle * Mth.RAD_TO_DEG + 90.0F
                    + Mth.sin(time * 0.0017F + slice * 0.47F) * 12.0F;
            float pitch = Mth.sin(time * 0.0012F + slice * 0.53F) * 3.5F;
            float uvScroll = time * (0.00042F + slice * 0.000012F) + slice * 0.097F;
            float density = Mth.lerp(t, 0.32F, 0.18F);
            int alpha = (int) Mth.clamp(Mth.lerp(t, 42.0F, 24.0F), 0.0F, 48.0F);
            int red = Mth.floor(Mth.lerp(t, 174.0F, 150.0F));
            int green = Mth.floor(Mth.lerp(t, 204.0F, 178.0F));
            int blue = Mth.floor(Mth.lerp(t, 210.0F, 188.0F));

            RenderType renderType = HemoRenderTypes.silentArchonVolumetricFog(theme.cloudTexture(), time * 0.05F,
                    slice * 0.137F + 0.19F, t, density);
            VertexConsumer consumer = bufferSource.getBuffer(renderType);
            poseStack.pushPose();
            poseStack.translate(x, 0.0F, z);
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            renderSilentArchonVolumetricFogSheet(poseStack, consumer, halfSpan, baseY, topY, depthBow,
                    uvScroll, red, green, blue, alpha, 6, 7);
            poseStack.popPose();
            bufferSource.endBatch(renderType);
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }

    private static void renderSilentArchonVolumetricFogSheet(PoseStack poseStack, VertexConsumer consumer,
                                                             float halfSpan, float bottomY, float topY,
                                                             float depthBow, float uvScroll, int red, int green,
                                                             int blue, int alpha, int columns, int rows) {
        Matrix4f matrix = poseStack.last().pose();
        int xCells = Math.max(2, columns);
        int yCells = Math.max(2, rows);
        for (int xCell = 0; xCell < xCells; xCell++) {
            for (int yCell = 0; yCell < yCells; yCell++) {
                float x0T = xCell / (float) xCells;
                float x1T = (xCell + 1) / (float) xCells;
                float y0T = yCell / (float) yCells;
                float y1T = (yCell + 1) / (float) yCells;
                float x0 = Mth.lerp(x0T, -halfSpan, halfSpan);
                float x1 = Mth.lerp(x1T, -halfSpan, halfSpan);
                float y0 = Mth.lerp(y0T, bottomY, topY);
                float y1 = Mth.lerp(y1T, bottomY, topY);
                float z00 = volumetricFogBow(x0T, y0T, depthBow, uvScroll);
                float z01 = volumetricFogBow(x0T, y1T, depthBow, uvScroll);
                float z11 = volumetricFogBow(x1T, y1T, depthBow, uvScroll);
                float z10 = volumetricFogBow(x1T, y0T, depthBow, uvScroll);
                float u0 = x0T * 2.7F + uvScroll;
                float u1 = x1T * 2.7F + uvScroll;
                float v0 = y0T * 1.35F + uvScroll * 0.31F;
                float v1 = y1T * 1.35F + uvScroll * 0.31F;

                consumer.addVertex(matrix, x0, y0, z00).setUv(u0, v0)
                        .setColor(red, green, blue, volumetricFogAlpha(x0T, y0T, alpha));
                consumer.addVertex(matrix, x0, y1, z01).setUv(u0, v1)
                        .setColor(red, green, blue, volumetricFogAlpha(x0T, y1T, alpha));
                consumer.addVertex(matrix, x1, y1, z11).setUv(u1, v1)
                        .setColor(red, green, blue, volumetricFogAlpha(x1T, y1T, alpha));
                consumer.addVertex(matrix, x1, y0, z10).setUv(u1, v0)
                        .setColor(red, green, blue, volumetricFogAlpha(x1T, y0T, alpha));
            }
        }
    }

    private static float volumetricFogBow(float xT, float yT, float depthBow, float uvScroll) {
        float center = 1.0F - Math.abs(xT - 0.5F) * 2.0F;
        float wave = Mth.sin((xT * 4.0F + yT * 2.2F + uvScroll * 2.7F) * Mth.PI) * 0.34F;
        return depthBow * (center * 0.72F + wave);
    }

    private static int volumetricFogAlpha(float xT, float yT, int alpha) {
        float horizontalFade = Mth.clamp(1.0F - Math.abs(xT - 0.5F) * 1.85F, 0.0F, 1.0F);
        float verticalFade = Mth.clamp(1.0F - Math.abs(yT - 0.48F) * 1.72F, 0.0F, 1.0F);
        float baseLift = Mth.clamp(1.0F - yT * 0.62F, 0.42F, 1.0F);
        float brokenEdge = 0.74F + 0.26F * Mth.sin((xT * 6.0F + yT * 4.5F) * Mth.PI);
        return (int) Mth.clamp(alpha * horizontalFade * verticalFade * baseLift * brokenEdge, 0.0F, 150.0F);
    }

    private static void renderSilentArchonForegroundCloudWisps(PoseStack poseStack, Tesselator tesselator, float time,
                                                               float skyDistance, ChamberSkyTheme theme) {
        if (!ChamberOfWillManager.THEME_SILENT_ARCHON.equals(theme.id())) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, theme.cloudTexture());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        float wispY = -skyDistance * 0.54F;
        int wispLayers = 5;
        for (int layer = 0; layer < wispLayers; layer++) {
            float t = layer / (float) Math.max(1, wispLayers - 1);
            float drift = time * (0.00048F + layer * 0.00008F);
            float angle = layer / (float) wispLayers * Mth.TWO_PI
                    + Mth.sin(time * 0.0011F + layer * 0.83F) * 0.12F;
            float distance = skyDistance * Mth.lerp(t, 0.16F, 0.58F);
            float x = Mth.cos(angle) * distance + Mth.sin(time * 0.0015F + layer) * skyDistance * 0.05F;
            float z = Mth.sin(angle) * distance + Mth.cos(time * 0.0013F + layer) * skyDistance * 0.05F;
            float y = wispY + skyDistance * Mth.lerp(t, -0.085F, 0.075F)
                    + Mth.sin(time * 0.0021F + layer * 1.41F) * skyDistance * 0.012F;
            float yaw = angle * Mth.RAD_TO_DEG + 92.0F + Mth.sin(time * 0.0016F + layer) * 5.0F;
            float pitch = Mth.lerp(t, -4.5F, 2.0F) + Mth.cos(time * 0.0014F + layer) * 1.8F;
            float tilt = Mth.lerp(t, -7.0F, 8.0F) + Mth.sin(time * 0.0017F + layer * 0.7F) * 2.2F;
            float halfSpan = skyDistance * Mth.lerp(t, 0.78F, 1.18F);
            float halfDepth = skyDistance * Mth.lerp(t, 0.16F, 0.32F);
            float uvScale = Mth.lerp(t, 2.25F, 3.25F);
            int alpha = (int) Mth.clamp(Mth.lerp(t, 108.0F, 72.0F), 0.0F, 128.0F);
            int red = Mth.floor(Mth.lerp(t, 62.0F, 86.0F));
            int green = Mth.floor(Mth.lerp(t, 86.0F, 108.0F));
            int blue = Mth.floor(Mth.lerp(t, 86.0F, 106.0F));

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees(tilt));
            renderSilentArchonCloudDeckPlane(poseStack, tesselator, halfSpan, halfDepth,
                    drift + layer * 0.31F, uvScale, red, green, blue, alpha, 6);
            poseStack.popPose();
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }

    private static void renderSilentArchonMonolithPillars(PoseStack poseStack, float time,
                                                          float skyDistance, ChamberSkyTheme theme) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        Random random = new Random(91077L);
        int count = Math.max(0, theme.monolithPillarCount());
        int pillarPairs = count / 2;
        int pillar = 0;
        for (int pair = 0; pair < pillarPairs; pair++) {
            float ringT = (pair + 0.5F) / (float) Math.max(1, pillarPairs);
            float angle = ringT * Mth.TWO_PI * 0.5F + Mth.lerp(random.nextFloat(), -0.12F, 0.12F);
            float distance = skyDistance * Mth.lerp(random.nextFloat(), 0.34F, 0.94F);
            float radius = skyDistance * Mth.lerp(random.nextFloat(), 0.014F, 0.038F);
            float baseY = -skyDistance * Mth.lerp(random.nextFloat(), 0.98F, 1.18F);
            float topY = skyDistance * Mth.lerp(random.nextFloat(), 0.52F, 1.05F);
            float twist = random.nextFloat() * Mth.TWO_PI;
            int sideCount = 6 + random.nextInt(3);
            renderSilentArchonMonolithPillar(poseStack, bufferSource, time, pillar++, angle, distance, radius, baseY,
                    topY, twist, sideCount);
            renderSilentArchonMonolithPillar(poseStack, bufferSource, time, pillar++,
                    angle + Mth.TWO_PI * 0.5F, distance * Mth.lerp(random.nextFloat(), 0.94F, 1.06F),
                    radius * Mth.lerp(random.nextFloat(), 0.92F, 1.08F),
                    baseY * Mth.lerp(random.nextFloat(), 0.96F, 1.04F),
                    topY * Mth.lerp(random.nextFloat(), 0.96F, 1.04F), twist + 0.73F, sideCount);
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }

    private static void renderSilentArchonMonolithPillar(PoseStack poseStack,
                                                         MultiBufferSource.BufferSource bufferSource,
                                                         float time, int pillar, float angle, float distance,
                                                         float radius, float baseY, float topY, float twist,
                                                         int sideCount) {
        float x = Mth.cos(angle) * distance;
        float z = Mth.sin(angle) * distance;
        int color = pillar % 5 == 0 ? 0xFF110708 : 0xFF080607;
        float seed = (pillar * 37 + 11) / 997.0F;

        RenderType renderType = HemoRenderTypes.monolithEntitySurface(time * 0.05F, seed, 0.72F, 0.90F, 10.5F);
        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        renderSilentArchonPillarGeometry(poseStack, consumer, x, baseY, z, radius, topY, sideCount,
                twist + time * 0.0007F, color, pillar);
        bufferSource.endBatch(renderType);
    }

    private static void renderSilentArchonPillarGeometry(PoseStack poseStack, VertexConsumer consumer,
                                                         float centerX, float baseY, float centerZ,
                                                         float radius, float topY, int sideCount,
                                                         float twist, int color, int seed) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        float height = topY - baseY;
        sideCount = Math.max(3, sideCount);
        float[] cornerX = new float[sideCount + 1];
        float[] cornerZ = new float[sideCount + 1];
        float[] cornerTop = new float[sideCount + 1];
        for (int corner = 0; corner < sideCount; corner++) {
            float angle = twist + corner / (float) sideCount * Mth.TWO_PI;
            float cornerRadius = radius * (0.90F + ((seed + corner * 19) % 7) * 0.028F);
            cornerX[corner] = centerX + Mth.cos(angle) * cornerRadius;
            cornerZ[corner] = centerZ + Mth.sin(angle) * cornerRadius;
            cornerTop[corner] = topY - ((seed + corner * 31) % 9) * radius * 0.11F;
        }
        cornerX[sideCount] = cornerX[0];
        cornerZ[sideCount] = cornerZ[0];
        cornerTop[sideCount] = cornerTop[0];

        for (int side = 0; side < sideCount; side++) {
            float a0 = twist + side / (float) sideCount * Mth.TWO_PI;
            float a1 = twist + (side + 1) / (float) sideCount * Mth.TWO_PI;
            float x0 = cornerX[side];
            float z0 = cornerZ[side];
            float x1 = cornerX[side + 1];
            float z1 = cornerZ[side + 1];
            float top0 = cornerTop[side];
            float top1 = cornerTop[side + 1];
            float normalX = Mth.cos((a0 + a1) * 0.5F);
            float normalZ = Mth.sin((a0 + a1) * 0.5F);
            float u0 = side / (float) sideCount;
            float u1 = (side + 1) / (float) sideCount;
            float v1 = Math.max(2.0F, height / Math.max(1.0F, radius * 7.0F));

            addSilentArchonPillarVertex(consumer, pose, matrix, x0, baseY, z0, u0, 0.0F, color, normalX, normalZ);
            addSilentArchonPillarVertex(consumer, pose, matrix, x1, baseY, z1, u1, 0.0F, color, normalX, normalZ);
            addSilentArchonPillarVertex(consumer, pose, matrix, x1, top1, z1, u1, v1, color, normalX, normalZ);
            addSilentArchonPillarVertex(consumer, pose, matrix, x0, top0, z0, u0, v1, color, normalX, normalZ);
        }
    }

    private static void addSilentArchonPillarVertex(VertexConsumer consumer, PoseStack.Pose pose, Matrix4f matrix,
                                                    float x, float y, float z, float u, float v, int color,
                                                    float normalX, float normalZ) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, normalX, 0.0F, normalZ);
    }

    private static void renderBox(PoseStack poseStack, Tesselator tesselator, float skyDistance, float uvMin, float uvMax, Supplier<ShaderInstance> shaderSupplier, ResourceLocation texture, int color) {
        RenderSystem.setShader(shaderSupplier);
        RenderSystem.setShaderTexture(0, texture);
        for (int i = 0; i < 6; i++) {
            poseStack.pushPose();
            if (i == 1) {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            }

            if (i == 2) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            }

            if (i == 3) {
                poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            }

            if (i == 4) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            }

            if (i == 5) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
            }
            Matrix4f matrix4f = poseStack.last().pose();
            BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.addVertex(matrix4f, -skyDistance, -skyDistance, -skyDistance).setUv(uvMin, uvMin).setColor(color);
            bufferbuilder.addVertex(matrix4f, -skyDistance, -skyDistance, skyDistance).setUv(uvMin, uvMax).setColor(color);
            bufferbuilder.addVertex(matrix4f, skyDistance, -skyDistance, skyDistance).setUv(uvMax, uvMax).setColor(color);
            bufferbuilder.addVertex(matrix4f, skyDistance, -skyDistance, -skyDistance).setUv(uvMax, uvMin).setColor(color);
            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
            poseStack.popPose();
        }
    }

    private static void renderPlane(PoseStack poseStack, Tesselator tesselator, float skyDistance, float uvMin, float uvMax, Supplier<ShaderInstance> shaderSupplier, ResourceLocation texture, float scale, int color) {
        RenderSystem.setShader(shaderSupplier);
        RenderSystem.setShaderTexture(0, texture);
        poseStack.pushPose();
        Matrix4f matrix4f = poseStack.last().pose();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.addVertex(matrix4f, -skyDistance * scale, -skyDistance, -skyDistance * scale).setUv(uvMin, uvMin).setColor(color);
        bufferbuilder.addVertex(matrix4f, -skyDistance * scale, -skyDistance, skyDistance * scale).setUv(uvMin, uvMax).setColor(color);
        bufferbuilder.addVertex(matrix4f, skyDistance * scale, -skyDistance, skyDistance * scale).setUv(uvMax, uvMax).setColor(color);
        bufferbuilder.addVertex(matrix4f, skyDistance * scale, -skyDistance, -skyDistance * scale).setUv(uvMax, uvMin).setColor(color);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        poseStack.popPose();
    }

    public void renderBorderAura(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix) {
        PoseStack poseStack = new PoseStack();
        Quaternionf quaternionf = camera.rotation().conjugate(new Quaternionf());
        Vec3 cameraPos = camera.getPosition();
        Matrix4f matrix4f1 = new Matrix4f().rotation(quaternionf).translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);
        poseStack.mulPose(matrix4f1);
        // Each player's cell is centred at (0, FLOOR_Y, CHAMBER_SPACING * id); snap the aura to the current cell along Z.
        int traversal = (int) (cameraPos.z / ChamberOfWillManager.CHAMBER_SPACING) * ChamberOfWillManager.CHAMBER_SPACING;
        float chamberWidth = ChamberOfWillClientData.radius() * 2.0f + 1.0f;
        float halfWidth = chamberWidth / 2.0f;
        float HARDCODE_X = 0.5f;
        float HARDCODE_Y = 1.25f;
        float HARDCODE_Z = traversal + 0.5f;
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE); //additive
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        poseStack.translate(HARDCODE_X, HARDCODE_Y, HARDCODE_Z);

        Tesselator tesselator = Tesselator.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, ChamberSkyThemeRegistry.activeTheme().noiseTexture());
        float uvScrollMin = ((ticks + partialTick) / 21 / 12) % 4;
        float uvScrollMax = uvScrollMin + 5f / 20 / 12;
        float uvTile = Mth.floor(chamberWidth / 4f); // times for x axis to tile
        for (int i = 0; i < 4; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(i * 90));

            Matrix4f matrix4f = poseStack.last().pose();
            BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            int baseColor = 0xFFff1188;
            bufferbuilder.addVertex(matrix4f, -halfWidth, HARDCODE_Y - 1, halfWidth).setUv(0, uvScrollMax).setColor(baseColor);
            bufferbuilder.addVertex(matrix4f, -halfWidth, HARDCODE_Y + 2, halfWidth).setUv(0, uvScrollMin).setColor(0xFF000000);
            bufferbuilder.addVertex(matrix4f, halfWidth, HARDCODE_Y + 2, halfWidth).setUv(uvTile, uvScrollMin).setColor(0xFF000000);
            bufferbuilder.addVertex(matrix4f, halfWidth, HARDCODE_Y - 1, halfWidth).setUv(uvTile, uvScrollMax).setColor(baseColor);
            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
            poseStack.popPose();
        }
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        if (isSilentArchonFogTheme()) {
            float fogBrightness = Mth.clamp(brightness * 0.32F + 0.68F, 0.58F, 1.0F);
            return new Vec3(0.52D, 0.66D, 0.68D).scale(fogBrightness);
        }
        return fogColor;
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    private static boolean isSilentArchonFogTheme() {
        return ChamberOfWillManager.THEME_SILENT_ARCHON.equals(ChamberSkyThemeRegistry.activeTheme().id());
    }

    private record DepthLayer(int index, float t, float depthScale, float spanScale, float sizeScale, float alphaScale,
                              float motionScale, int seedOffset) {
    }
}
