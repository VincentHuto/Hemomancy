package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.data.ChamberOfWillClientData;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import com.vincenthuto.hutoslib.client.particle.data.TendrilGeometry;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
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

final class ChamberOfWillRenderHelpers {
    private ChamberOfWillRenderHelpers() {
    }

    static float renderNebula(PoseStack poseStack, Vector3f color, Random random, float f,
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

    static float renderNebulaFaceVolume(PoseStack poseStack, Random random, float f,
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

    static float nebulaAnchorX(int lane, Random random) {
        float jitter = Mth.lerp(random.nextFloat(), -0.10F, 0.10F);
        return switch (lane) {
            case 0, 3, 5 -> -0.62F + jitter;
            case 1, 4, 7 -> 0.62F + jitter;
            default -> Mth.lerp(random.nextFloat(), -0.28F, 0.28F);
        };
    }

    static float nebulaAnchorZ(int lane, Random random) {
        float jitter = Mth.lerp(random.nextFloat(), -0.10F, 0.10F);
        return switch (lane) {
            case 0, 1, 6 -> -0.62F + jitter;
            case 3, 4, 7 -> 0.62F + jitter;
            default -> Mth.lerp(random.nextFloat(), -0.28F, 0.28F);
        };
    }

    static Vector3f colorVector(int rgb) {
        return new Vector3f(
                ((rgb >> 16) & 255) / 255.0F,
                ((rgb >> 8) & 255) / 255.0F,
                (rgb & 255) / 255.0F);
    }

    static int tinted(int channel, int tint, int shift) {
        float tintChannel = ((tint >> shift) & 255) / 255.0F;
        return (int) Mth.clamp(channel * tintChannel, 0.0F, 255.0F);
    }

    static int proceduralAlpha(float alpha) {
        float PROCEDURAL_ALPHA_MULTIPLIER = 2.35F;
        return (int) Mth.clamp(alpha * PROCEDURAL_ALPHA_MULTIPLIER, 0.0F, 255.0F);
    }

    static float proceduralSize(float size) {
        float PROCEDURAL_SIZE_MULTIPLIER = 1.16F;
        return size * PROCEDURAL_SIZE_MULTIPLIER;
    }

    static void rotateSkyFace(PoseStack poseStack, int face) {
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

    static float vesselOffset(float t, float wave, float phase, float time, float span) {
        float main = Mth.sin(t * Mth.TWO_PI * wave + phase + time * 0.011F);
        float drift = Mth.sin(t * Mth.TWO_PI * (wave * 1.65F + 0.23F) + phase * 0.37F - time * 0.016F);
        float flutter = Mth.sin(t * Mth.TWO_PI * (wave * 2.45F + 0.38F) + phase * 0.21F + time * 0.008F);
        return (main * 0.185F + drift * 0.082F + flutter * 0.028F) * span;
    }

    static DepthLayer depthLayer(int layer, int layerCount, int seedSalt) {
        int safeCount = Math.max(1, layerCount);
        float t = safeCount <= 1 ? 0.5F : layer / (float) (safeCount - 1);
        float easedT = t * t * (3.0F - 2.0F * t);
        return new DepthLayer(layer, t, Mth.lerp(easedT, 1.075F, 0.390F), Mth.lerp(easedT, 0.42F, 0.88F), Mth.lerp(easedT, 0.24F, 1.34F), Mth.lerp(easedT, 0.30F, 1.18F), Mth.lerp(easedT, 0.20F, 1.14F), seedSalt + layer * 271);
    }

    static DepthLayer vascularDepthLayer(int layer, int layerCount, int seedSalt) {
        int safeCount = Math.max(1, layerCount);
        float t = safeCount <= 1 ? 0.5F : layer / (float) (safeCount - 1);
        float easedT = t * t * (3.0F - 2.0F * t);
        return new DepthLayer(layer, t, Mth.lerp(easedT, 1.075F, 0.640F), Mth.lerp(easedT, 0.42F, 0.74F), Mth.lerp(easedT, 0.24F, 0.68F), Mth.lerp(easedT, 0.30F, 1.03F), Mth.lerp(easedT, 0.20F, 1.02F), seedSalt + layer * 271);
    }

    static DepthLayer neuralDepthLayer(int layer, int layerCount, int seedSalt) {
        int safeCount = Math.max(1, layerCount);
        float t = safeCount <= 1 ? 0.5F : layer / (float) (safeCount - 1);
        float easedT = t * t * (3.0F - 2.0F * t);
        return new DepthLayer(layer, t, Mth.lerp(easedT, 1.075F, 0.390F), Mth.lerp(easedT, 0.42F, 0.88F), Mth.lerp(easedT, 0.24F, 1.34F), Mth.lerp(easedT, 0.16F, 1.16F), Mth.lerp(easedT, 0.20F, 1.14F), seedSalt + layer * 271);
    }

    static int depthLayerCount(int layerCount) {
        return Math.max(0, layerCount);
    }

    static float layeredTime(float time, DepthLayer layer) {
        return time * layer.motionScale() + layer.seedOffset() * 0.017F;
    }

    static int layeredAlpha(float alpha, DepthLayer layer) {
        return (int) Mth.clamp(alpha * layer.alphaScale(), 0.0F, 255.0F);
    }

    static float bloodVesselDetailT(DepthLayer layer) {
        return layer.index() == 0 && Mth.abs(layer.t() - 0.5F) < 0.001F ? 1.0F : layer.t();
    }

    static float neuralLayerVisibility(DepthLayer layer) {
        float nearBias = layer.t() * layer.t() * layer.t();
        return Mth.lerp(nearBias, 1.0F, 1.0F);
    }

    static int neuralLayerAlpha(float alpha, DepthLayer layer) {
        return layeredAlpha(alpha * neuralLayerVisibility(layer), layer);
    }

    static float layeredSize(float size, DepthLayer layer) {
        return size * layer.sizeScale();
    }
    static float layeredSpan(float span, DepthLayer layer) {
        return span * layer.spanScale();
    }
    static float membranePulse(float time) {
        float cycle = (time % 208.0F) / 208.0F;
        float beat = wrappedPulse(cycle, 0.04F, 0.055F) * 0.72F;
        float afterbeat = wrappedPulse(cycle, 0.18F, 0.09F) * 0.42F;
        float pulse = Mth.clamp(beat + afterbeat, 0.0F, 1.0F);
        return pulse * pulse * (3.0F - 2.0F * pulse);
    }

    static float wrappedPulse(float t, float center, float width) {
        float distance = Mth.abs(t - center);
        distance = Math.min(distance, 1.0F - distance);
        float pulse = Mth.clamp(1.0F - distance / width, 0.0F, 1.0F);
        return pulse * pulse * (3.0F - 2.0F * pulse);
    }

    static void renderMembranePulseVignette(PoseStack poseStack, Tesselator tesselator, float skyDistance, float pulse) {
        if (pulse <= 0.01F) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
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

    static void renderMembranePulseFace(PoseStack poseStack, Tesselator tesselator, float halfSize, float depth, int red, int green, int blue, int alpha) {
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

    static void renderCorticalFolds(PoseStack poseStack, Tesselator tesselator, float time, float skyDistance) {
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

    static void renderCorticalFoldFace(PoseStack poseStack, Tesselator tesselator, float time, float skyDistance, int face) {
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

    static void renderCorticalFoldBand(PoseStack poseStack, Tesselator tesselator, float depth, float span, float laneOffset, float angleDegrees, float baseWidth, float phase, float time, int red, int green, int blue, int alpha, int segments) {
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

    static void addFoldQuad(Matrix4f matrix, BufferBuilder buffer, float[] pointsX, float[] pointsZ, float[] foldWidths, int[] centerAlpha, int[] shoulderAlpha, float crossX, float crossZ, float depth, int red, int green, int blue, int point, float leftScale, float rightScale, int leftAlphaMode, int rightAlphaMode) {
        addFoldVertex(matrix, buffer, pointsX[point], pointsZ[point], foldWidths[point], crossX, crossZ, depth, red, green, blue, foldAlpha(centerAlpha[point], shoulderAlpha[point], leftAlphaMode), leftScale);
        addFoldVertex(matrix, buffer, pointsX[point + 1], pointsZ[point + 1], foldWidths[point + 1], crossX, crossZ, depth, red, green, blue, foldAlpha(centerAlpha[point + 1], shoulderAlpha[point + 1], leftAlphaMode), leftScale);
        addFoldVertex(matrix, buffer, pointsX[point + 1], pointsZ[point + 1], foldWidths[point + 1], crossX, crossZ, depth, red, green, blue, foldAlpha(centerAlpha[point + 1], shoulderAlpha[point + 1], rightAlphaMode), rightScale);
        addFoldVertex(matrix, buffer, pointsX[point], pointsZ[point], foldWidths[point], crossX, crossZ, depth, red, green, blue, foldAlpha(centerAlpha[point], shoulderAlpha[point], rightAlphaMode), rightScale);
    }

    static void addFoldVertex(Matrix4f matrix, BufferBuilder buffer, float centerX, float centerZ, float foldWidth, float crossX, float crossZ, float depth, int red, int green, int blue, int alpha, float scale) {
        buffer.addVertex(matrix, centerX + proceduralSize(foldWidth) * scale * crossX, depth, centerZ + proceduralSize(foldWidth) * scale * crossZ).setColor(red, green, blue, proceduralAlpha(alpha));
    }

    static int foldAlpha(int centerAlpha, int shoulderAlpha, int mode) {
        return switch (mode) {
            case 1 -> shoulderAlpha;
            case 2 -> centerAlpha;
            default -> 0;
        };
    }

    static float faceCorticalFoldAngle(int face, int fold) {
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

    static void renderCapillaryWeb(PoseStack poseStack, Tesselator tesselator, float time,
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

    static void renderCapillaryWebLayerFace(PoseStack poseStack, Tesselator tesselator, float time,
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

    static float faceCapillaryAngle(int face, int capillary) {
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

    static void renderBloodVessels(PoseStack poseStack, Tesselator tesselator, float time,
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

    static void renderBloodVesselLayerFace(PoseStack poseStack, Tesselator tesselator, float time,
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

    static void renderBlueVeins(PoseStack poseStack, Tesselator tesselator, float time,
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

    static void renderBlueVeinLayerFace(PoseStack poseStack, Tesselator tesselator, float time,
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

    static boolean shouldMirrorVesselFace(int face) {
        return face == 1 || face == 3 || face == 5;
    }

    static float faceVesselAngle(int face, int vessel) {
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

    static float faceVenousAngle(int face, int vein) {
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

    static void renderVesselRibbon(PoseStack poseStack, Tesselator tesselator, float depth, float span, float laneOffset, float angleDegrees, float wave, float phase, float time, float baseWidth, int segments, int red, int green, int blue, int alpha) {
        renderVesselRibbon(poseStack, tesselator, depth, span, laneOffset, angleDegrees, wave, phase, time, baseWidth, segments, red, green, blue, alpha, false, 0);
    }

    static void renderPulsingVesselRibbon(PoseStack poseStack, Tesselator tesselator, float depth, float span, float laneOffset, float angleDegrees, float wave, float phase, float time, float baseWidth, int seed, int segments, int red, int green, int blue, int alpha) {
        renderVesselRibbon(poseStack, tesselator, depth, span, laneOffset, angleDegrees, wave, phase, time, baseWidth, segments, red, green, blue, alpha, true, seed);
    }

    static void renderVesselRibbon(PoseStack poseStack, Tesselator tesselator, float depth, float span, float laneOffset, float angleDegrees, float wave, float phase, float time, float baseWidth, int segments, int red, int green, int blue, int alpha, boolean pressurePulse, int seed) {
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

    static float pressureBulge(float t, float time, float phase, int seed) {
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

    static float movingPulse(float t, float center, float width) {
        center = center - Mth.floor(center);
        float distance = Mth.abs(t - center);
        distance = Math.min(distance, 1.0F - distance);
        float pulse = Mth.clamp(1.0F - distance / width, 0.0F, 1.0F);
        return pulse * pulse * (3.0F - 2.0F * pulse);
    }

    static void renderBloodCellStream(PoseStack poseStack, Tesselator tesselator, float depth, float span, float laneOffset, float angleDegrees, float wave, float phase, float time, float vesselWidth, int seed, int cellCount) {
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

    static void renderVesselBranch(PoseStack poseStack, Tesselator tesselator, float depth, float parentSpan, float parentLaneOffset, float parentAngleDegrees, float parentWave, float parentPhase, float branchPhase, float time, float baseWidth, float parentT, boolean leftSide, float branchLength, int segments, int red, int green, int blue, int alpha) {
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

    static void renderVesselBranches(PoseStack poseStack, Tesselator tesselator, float depth, float parentSpan, float parentLaneOffset, float parentAngleDegrees, float parentWave, float parentPhase, float time, float baseWidth, int branchCount, float firstParentT, float branchLength, int segments, int seed, int red, int green, int blue, int alpha) {
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

    static void renderNeuralStructures(PoseStack poseStack, Tesselator tesselator, float time,
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

    static void renderNeuralLayerFace(PoseStack poseStack, Tesselator tesselator, float time,
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

    static void renderNeuronSoma(PoseStack poseStack, Tesselator tesselator, float centerX, float depth, float centerZ, float radius, int red, int green, int blue, int alpha) {
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

    static void renderNeuronAxon(PoseStack poseStack, Tesselator tesselator, float centerX, float depth, float centerZ, float rootOffset, float angleDegrees, float length, float baseWidth, float phase, float time, int segments, int red, int green, int blue, int alpha) {
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

    static void renderNeuronImpulse(PoseStack poseStack, Tesselator tesselator, float centerX, float depth, float centerZ, float rootOffset, float angleDegrees, float length, float phase, float time, int sparkCount, boolean primary, float visibility) {
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

    static void renderSynapseFlareEvents(PoseStack poseStack, Tesselator tesselator, float time, float skyDistance, int face) {
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

    static float neuronCenterX(float time, float skyDistance, int face, int neuron) {
        int seed = face * 61 + neuron * 17;
        float mirrored = shouldMirrorVesselFace(face) ? -1.0F : 1.0F;
        float baseX = switch (neuron) {
            case 0 -> -0.62F;
            case 1 -> 0.12F;
            default -> 0.68F;
        } * mirrored;
        return (baseX + Mth.sin(time * 0.006F + seed * 0.41F) * 0.018F) * skyDistance;
    }

    static float neuronCenterZ(float time, float skyDistance, int face, int neuron) {
        int seed = face * 61 + neuron * 17;
        float baseZ = switch (neuron) {
            case 0 -> 0.42F;
            case 1 -> -0.18F;
            default -> -0.55F;
        };
        return (baseZ + Mth.sin(time * 0.0045F + seed * 0.67F) * 0.018F) * skyDistance;
    }

    static float neuronDepth(float skyDistance, int face, int neuron) {
        return -skyDistance * (0.715F + neuron * 0.020F + face * 0.002F);
    }

    static float neuronSomaRadius(float time, int seed) {
        return (1.22F + (seed % 4) * 0.19F) * (0.9F + 0.1F * Mth.sin(time * 0.055F + seed));
    }

    static float[] primaryAxonPoint(float centerX, float centerZ, float somaRadius, float skyDistance, int face, int seed, float time, float t) {
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

    static void renderSynapseChain(PoseStack poseStack, Tesselator tesselator, float startX, float startDepth, float startZ, float endX, float endDepth, float endZ, float time, int seed, float travel, float intensity) {
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

    static void renderSynapseLink(PoseStack poseStack, Tesselator tesselator, float startX, float startDepth, float startZ, float endX, float endDepth, float endZ, float width, int alpha) {
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

    static void renderBox(PoseStack poseStack, Tesselator tesselator, float skyDistance, float uvMin, float uvMax, Supplier<ShaderInstance> shaderSupplier, ResourceLocation texture, int color) {
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

    static void renderPlane(PoseStack poseStack, Tesselator tesselator, float skyDistance, float uvMin, float uvMax, Supplier<ShaderInstance> shaderSupplier, ResourceLocation texture, float scale, int color) {
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

    static void renderBorderAura(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix) {
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

    private record DepthLayer(int index, float t, float depthScale, float spanScale, float sizeScale, float alphaScale,
                              float motionScale, int seedOffset) {
    }
}
