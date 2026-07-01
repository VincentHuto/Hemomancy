package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Random;

final class SilentArchonChamberEffects extends AbstractChamberThemeEffects {
    private static final int SILENT_ARCHON_PILLAR_BOTTOM_FADE_SEGMENTS = 4;
    private static final int SILENT_ARCHON_PILLAR_TOP_FADE_SEGMENTS = 4;

	SilentArchonChamberEffects(ChamberSkyTheme theme) {
		super(theme);
	}

	@Override
	protected void renderAfterNebula(ChamberThemeRenderContext context) {
		PoseStack poseStack = context.poseStack();
		Tesselator tesselator = context.tesselator();
		float f = context.time();
		float skyDistance = context.skyDistance();
		ChamberSkyTheme theme = context.theme();
		renderSilentArchonDepthEffects(poseStack, tesselator, f, skyDistance, theme);
	}

	@Override
	protected void renderBeforeSharedLayers(ChamberThemeRenderContext context) {
		PoseStack poseStack = context.poseStack();
		float f = context.time();
		float skyDistance = context.skyDistance();
		ChamberSkyTheme theme = context.theme();
		if (theme.renderMonolithPillars()) {
			renderSilentArchonMonolithPillars(poseStack, f, skyDistance, theme);
		}
		renderSilentArchonForegroundStormClouds(poseStack, f, skyDistance, theme);
	}

	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
		return silentArchonFogColor(brightness);
	}
    static void renderSilentArchonDepthEffects(PoseStack poseStack, Tesselator tesselator, float f,
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

    static void renderSilentArchonCloudStrata(PoseStack poseStack, Tesselator tesselator, float time,
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

    static void renderSilentArchonCloudStratumPlane(PoseStack poseStack, Tesselator tesselator,
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

    static void renderSilentArchonCloudDeck(PoseStack poseStack, Tesselator tesselator, float time,
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

    static void renderSilentArchonCloudDeckPlane(PoseStack poseStack, Tesselator tesselator,
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

    static int cloudDeckAlpha(float xT, float zT, int alpha) {
        float xFade = 1.0F - Math.abs(xT - 0.5F) * 2.0F;
        float zFade = 1.0F - Math.abs(zT - 0.5F) * 2.0F;
        float edgeFade = Mth.clamp(Math.min(xFade, zFade) * 1.7F, 0.0F, 1.0F);
        float unevenness = 0.78F + 0.22F * Mth.sin((xT * 7.0F + zT * 5.0F) * Mth.PI);
        return (int) Mth.clamp(alpha * edgeFade * unevenness, 0.0F, 160.0F);
    }

    static void renderSilentArchonForegroundStormClouds(PoseStack poseStack, float time,
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

    static void renderSilentArchonForegroundStormCloud(PoseStack poseStack,
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

        float baseShadowY = Mth.lerp(0.34F, baseY, cloudCenterY);
        float shadowAngle = twist + 0.18F + Mth.sin(time * 0.0011F + pillar) * 0.09F;
        float shadowRightX = Mth.cos(shadowAngle);
        float shadowRightZ = Mth.sin(shadowAngle);
        float shadowForwardX = -shadowRightZ;
        float shadowForwardZ = shadowRightX;
        renderSilentArchonMonolithBaseShadow(consumer, matrix, x, baseShadowY, z, shadowRightX, shadowRightZ,
                shadowForwardX, shadowForwardZ, radius, skyDistance, distanceT, pillar);

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

    static void renderSilentArchonMonolithBaseShadow(VertexConsumer consumer, Matrix4f matrix,
                                                             float centerX, float centerY, float centerZ,
                                                             float rightX, float rightZ, float forwardX,
                                                             float forwardZ, float radius, float skyDistance,
                                                             float distanceT, int pillar) {
        int shadowLayers = 4;
        float shadowSpan = radius * Mth.lerp(distanceT, 11.4F, 17.2F) + skyDistance * 0.026F;
        float shadowDepth = radius * Mth.lerp(distanceT, 7.0F, 11.6F) + skyDistance * 0.017F;
        for (int layer = 0; layer < shadowLayers; layer++) {
            float t = layer / (float) Math.max(1, shadowLayers - 1);
            float layerYaw = layer * 0.31F + Mth.sin(pillar * 0.73F + layer) * 0.10F;
            float rotatedRightX = rightX * Mth.cos(layerYaw) - forwardX * Mth.sin(layerYaw);
            float rotatedRightZ = rightZ * Mth.cos(layerYaw) - forwardZ * Mth.sin(layerYaw);
            float rotatedForwardX = -rotatedRightZ;
            float rotatedForwardZ = rotatedRightX;
            float layerY = centerY + Mth.lerp(t, -radius * 2.1F, radius * 5.8F);
            float halfWidth = shadowSpan * Mth.lerp(t, 1.12F, 0.64F);
            float halfDepth = shadowDepth * Mth.lerp(t, 0.86F, 0.46F);
            int alpha = (int) Mth.clamp(Mth.lerp(distanceT, 126.0F, 86.0F) * Mth.lerp(t, 1.0F, 0.50F),
                    32.0F, 134.0F);
            int red = Mth.floor(Mth.lerp(distanceT, 20.0F, 12.0F));
            int green = Mth.floor(Mth.lerp(distanceT, 30.0F, 22.0F));
            int blue = Mth.floor(Mth.lerp(distanceT, 31.0F, 24.0F));
            renderSilentArchonMonolithBaseShadowQuad(consumer, matrix, centerX, layerY, centerZ, rotatedRightX,
                    rotatedRightZ, rotatedForwardX, rotatedForwardZ, halfWidth, halfDepth, red, green, blue, alpha);
        }
    }

    static void renderSilentArchonMonolithBaseShadowQuad(VertexConsumer consumer, Matrix4f matrix,
                                                                float centerX, float centerY, float centerZ,
                                                                float rightX, float rightZ, float forwardX,
                                                                float forwardZ, float halfWidth, float halfDepth,
                                                                int red, int green, int blue, int alpha) {
        float x0 = centerX - rightX * halfWidth - forwardX * halfDepth;
        float z0 = centerZ - rightZ * halfWidth - forwardZ * halfDepth;
        float x1 = centerX - rightX * halfWidth + forwardX * halfDepth;
        float z1 = centerZ - rightZ * halfWidth + forwardZ * halfDepth;
        float x2 = centerX + rightX * halfWidth + forwardX * halfDepth;
        float z2 = centerZ + rightZ * halfWidth + forwardZ * halfDepth;
        float x3 = centerX + rightX * halfWidth - forwardX * halfDepth;
        float z3 = centerZ + rightZ * halfWidth - forwardZ * halfDepth;

        consumer.addVertex(matrix, x0, centerY, z0).setUv(0.0F, 1.0F).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, x1, centerY, z1).setUv(0.0F, 0.0F).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, x2, centerY, z2).setUv(1.0F, 0.0F).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, x3, centerY, z3).setUv(1.0F, 1.0F).setColor(red, green, blue, alpha);
    }

    static void renderSilentArchonStormCloudBankLayer(VertexConsumer consumer, Matrix4f matrix,
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
                float edgeFade = Mth.clamp(Math.min(xEdge, zEdge) *0.85F + noise * 0.1F, .0F, 1.0F);
                if (edgeFade < 0.20F) {
                    continue;
                }

                float jitterRight = (noise - 0.5F) * cellWidth * 0.56F;
                float jitterForward = Mth.cos(noise * 9.1F + ripple * 11.0F) * cellDepth * 0.38F;
                float cellCenterX = centerX + rightX * (xOffset + jitterRight) + forwardX * (zOffset + jitterForward);
                float cellCenterZ = centerZ + rightZ * (xOffset + jitterRight) + forwardZ * (zOffset + jitterForward);
                float cellCenterY = centerY
                        + Mth.sin(ripple * 13.0F + xCell * 0.83F + zCell * 1.17F) * halfDepth * 0.052F;
                float cellHalfWidth = cellWidth * Mth.lerp(noise, 1.54F, 1.84F);
                float cellHalfDepth = cellDepth * Mth.lerp(1.0F - noise, 1.62F, 1.96F);
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

    static void renderSilentArchonStormCloudBankCell(VertexConsumer consumer, Matrix4f matrix,
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

    static void renderSilentArchonDistantMonolithSilhouettes(PoseStack poseStack, Tesselator tesselator,
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

    static void renderDistantMonolithSilhouetteGeometry(BufferBuilder buffer, Matrix4f matrix,
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

    static void renderSilentArchonVolumetricFog(PoseStack poseStack, Tesselator tesselator, float time,
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

    static void renderSilentArchonVolumetricFogSheet(PoseStack poseStack, VertexConsumer consumer,
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

    static float volumetricFogBow(float xT, float yT, float depthBow, float uvScroll) {
        float center = 1.0F - Math.abs(xT - 0.5F) * 2.0F;
        float wave = Mth.sin((xT * 4.0F + yT * 2.2F + uvScroll * 2.7F) * Mth.PI) * 0.34F;
        return depthBow * (center * 0.72F + wave);
    }

    static int volumetricFogAlpha(float xT, float yT, int alpha) {
        float horizontalFade = Mth.clamp(1.0F - Math.abs(xT - 0.5F) * 1.85F, 0.0F, 1.0F);
        float verticalFade = Mth.clamp(1.0F - Math.abs(yT - 0.48F) * 1.72F, 0.0F, 1.0F);
        float baseLift = Mth.clamp(1.0F - yT * 0.62F, 0.42F, 1.0F);
        float brokenEdge = 0.74F + 0.26F * Mth.sin((xT * 6.0F + yT * 4.5F) * Mth.PI);
        return (int) Mth.clamp(alpha * horizontalFade * verticalFade * baseLift * brokenEdge, 0.0F, 150.0F);
    }

    static void renderSilentArchonForegroundCloudWisps(PoseStack poseStack, Tesselator tesselator, float time,
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

    static void renderSilentArchonMonolithPillars(PoseStack poseStack, float time,
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

    static void renderSilentArchonMonolithPillar(PoseStack poseStack,
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

    static void renderSilentArchonPillarGeometry(PoseStack poseStack, VertexConsumer consumer,
                                                         float centerX, float baseY, float centerZ,
                                                         float radius, float topY, int sideCount,
                                                         float twist, int color, int seed) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        float height = topY - baseY;
        float extendedBaseY = baseY - height * 0.24F;
        float bottomFadeEnd = Mth.lerp(0.16F, baseY, topY);
        float topFadeStart = Mth.lerp(0.78F, baseY, topY);
        float extendedHeight = topY - extendedBaseY;
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
            float v1 = Math.max(2.0F, extendedHeight / Math.max(1.0F, radius * 7.0F));
            float bottomFadeEndV = v1 * Mth.clamp((bottomFadeEnd - extendedBaseY) / Math.max(1.0F,
                    extendedHeight), 0.0F, 1.0F);
            float fadeStartV = v1 * Mth.clamp((topFadeStart - extendedBaseY) / Math.max(1.0F, extendedHeight),
                    0.0F, 1.0F);

            for (int segment = 0; segment < SILENT_ARCHON_PILLAR_BOTTOM_FADE_SEGMENTS; segment++) {
                float fadeT0 = segment / (float) SILENT_ARCHON_PILLAR_BOTTOM_FADE_SEGMENTS;
                float fadeT1 = (segment + 1) / (float) SILENT_ARCHON_PILLAR_BOTTOM_FADE_SEGMENTS;
                float y0 = Mth.lerp(fadeT0, extendedBaseY, bottomFadeEnd);
                float y1 = Mth.lerp(fadeT1, extendedBaseY, bottomFadeEnd);
                float v0 = Mth.lerp(fadeT0, 0.0F, bottomFadeEndV);
                float v2 = Mth.lerp(fadeT1, 0.0F, bottomFadeEndV);
                int color0 = silentArchonPillarColorWithAlpha(color, silentArchonPillarBottomAlpha(fadeT0));
                int color1 = silentArchonPillarColorWithAlpha(color, silentArchonPillarBottomAlpha(fadeT1));

                addSilentArchonPillarVertex(consumer, pose, matrix, x0, y0, z0, u0, v0, color0, normalX, normalZ);
                addSilentArchonPillarVertex(consumer, pose, matrix, x1, y0, z1, u1, v0, color0, normalX, normalZ);
                addSilentArchonPillarVertex(consumer, pose, matrix, x1, y1, z1, u1, v2, color1, normalX, normalZ);
                addSilentArchonPillarVertex(consumer, pose, matrix, x0, y1, z0, u0, v2, color1, normalX, normalZ);
            }

            addSilentArchonPillarVertex(consumer, pose, matrix, x0, bottomFadeEnd, z0, u0, bottomFadeEndV, color,
                    normalX, normalZ);
            addSilentArchonPillarVertex(consumer, pose, matrix, x1, bottomFadeEnd, z1, u1, bottomFadeEndV, color,
                    normalX, normalZ);
            addSilentArchonPillarVertex(consumer, pose, matrix, x1, topFadeStart, z1, u1, fadeStartV, color,
                    normalX, normalZ);
            addSilentArchonPillarVertex(consumer, pose, matrix, x0, topFadeStart, z0, u0, fadeStartV, color,
                    normalX, normalZ);

            for (int segment = 0; segment < SILENT_ARCHON_PILLAR_TOP_FADE_SEGMENTS; segment++) {
                float fadeT0 = segment / (float) SILENT_ARCHON_PILLAR_TOP_FADE_SEGMENTS;
                float fadeT1 = (segment + 1) / (float) SILENT_ARCHON_PILLAR_TOP_FADE_SEGMENTS;
                float y00 = Mth.lerp(fadeT0, topFadeStart, top0);
                float y01 = Mth.lerp(fadeT0, topFadeStart, top1);
                float y10 = Mth.lerp(fadeT1, topFadeStart, top0);
                float y11 = Mth.lerp(fadeT1, topFadeStart, top1);
                float v0 = Mth.lerp(fadeT0, fadeStartV, v1);
                float v2 = Mth.lerp(fadeT1, fadeStartV, v1);
                int color0 = silentArchonPillarColorWithAlpha(color, silentArchonPillarTopAlpha(fadeT0));
                int color1 = silentArchonPillarColorWithAlpha(color, silentArchonPillarTopAlpha(fadeT1));

                addSilentArchonPillarVertex(consumer, pose, matrix, x0, y00, z0, u0, v0, color0, normalX, normalZ);
                addSilentArchonPillarVertex(consumer, pose, matrix, x1, y01, z1, u1, v0, color0, normalX, normalZ);
                addSilentArchonPillarVertex(consumer, pose, matrix, x1, y11, z1, u1, v2, color1, normalX, normalZ);
                addSilentArchonPillarVertex(consumer, pose, matrix, x0, y10, z0, u0, v2, color1, normalX, normalZ);
            }
        }
    }

    static int silentArchonPillarBottomAlpha(float fadeT) {
        return Mth.floor(Mth.lerp(1.0F - (1.0F - fadeT) * (1.0F - fadeT), 18.0F, 255.0F));
    }

    static int silentArchonPillarTopAlpha(float fadeT) {
        return Mth.floor(Mth.lerp(fadeT * fadeT, 255.0F, 24.0F));
    }

    static int silentArchonPillarColorWithAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (Math.max(0, Math.min(255, alpha)) << 24);
    }

    static void addSilentArchonPillarVertex(VertexConsumer consumer, PoseStack.Pose pose, Matrix4f matrix,
                                                    float x, float y, float z, float u, float v, int color,
                                                    float normalX, float normalZ) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, normalX, 0.0F, normalZ);
    }

    static Vec3 silentArchonFogColor(float brightness) {
        float fogBrightness = Mth.clamp(brightness * 0.32F + 0.68F, 0.58F, 1.0F);
        return new Vec3(0.52D, 0.66D, 0.68D).scale(fogBrightness);
    }

    static boolean isSilentArchonFogTheme() {
        return ChamberOfWillManager.THEME_SILENT_ARCHON.equals(ChamberSkyThemeRegistry.activeTheme().id());
    }

}
