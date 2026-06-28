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
import com.vincenthuto.hutoslib.client.particle.data.TendrilGeometry;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Random;

final class QliphothCommunionChamberEffects extends AbstractChamberThemeEffects {
	private static final double QLIPHOTH_SMALL_TENDRIL_PULL = 58.0D;
	private static final double QLIPHOTH_FINAL_TENDRIL_PULL = 68.0D;
	static final float[][] QLIPHOTH_SMALL_HOLE_DIRECTIONS = {
			{-68.0F, 34.0F, 0.74F},
			{-122.0F, 12.0F, 0.52F},
			{-22.0F, -8.0F, 0.46F},
			{42.0F, 30.0F, 0.66F},
			{92.0F, 8.0F, 0.44F},
			{142.0F, -18.0F, 0.50F},
			{-168.0F, 48.0F, 0.58F},
			{18.0F, -34.0F, 0.42F}
	};

	QliphothCommunionChamberEffects(ChamberSkyTheme theme) {
		super(theme);
	}

	@Override
	protected void renderBeforeSharedLayers(ChamberThemeRenderContext context) {
		PoseStack poseStack = context.poseStack();
		Tesselator tesselator = context.tesselator();
		float f = context.time();
		float skyDistance = context.skyDistance();
		ChamberSkyTheme theme = context.theme();
		int qliphothPomeCount = context.qliphothPomesConsumed();
		renderQliphothCommunionBackdrop(poseStack, tesselator, f, skyDistance, theme, qliphothPomeCount);
	}

	@Override
	protected void renderAfterSharedLayers(ChamberThemeRenderContext context) {
		PoseStack poseStack = context.poseStack();
		Tesselator tesselator = context.tesselator();
		float f = context.time();
		float skyDistance = context.skyDistance();
		ChamberSkyTheme theme = context.theme();
		int qliphothPomeCount = context.qliphothPomesConsumed();
		renderQliphothCommunionSky(poseStack, tesselator, f, skyDistance, theme, qliphothPomeCount);
	}
    static void renderQliphothCommunionBackdrop(PoseStack poseStack, Tesselator tesselator, float time,
                                                        float skyDistance, ChamberSkyTheme theme, int pomeCount) {
        if (!ChamberOfWillManager.THEME_QLIPHOTH_COMMUNION.equals(theme.id())) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        renderQliphothRootScript(poseStack, tesselator, time, skyDistance);
        renderQliphothConstellationSigils(poseStack, tesselator, time, skyDistance, pomeCount);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }

    static void renderQliphothCommunionSky(PoseStack poseStack, Tesselator tesselator, float time,
                                                   float skyDistance, ChamberSkyTheme theme, int pomeCount) {
        if (!ChamberOfWillManager.THEME_QLIPHOTH_COMMUNION.equals(theme.id())) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        renderQliphothBlackHoles(poseStack, time, skyDistance, theme, pomeCount);
        renderQliphothSkyTendrils(poseStack, tesselator, time, skyDistance, pomeCount);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }

    static void renderQliphothSkyTendrils(PoseStack poseStack, Tesselator tesselator, float time,
                                                  float skyDistance, int pomeCount) {
        int smallHoleCount = Math.min(8, pomeCount);
        if (smallHoleCount <= 0 && pomeCount < 9) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();
        for (int hole = 0; hole < smallHoleCount; hole++) {
            float[] direction = QLIPHOTH_SMALL_HOLE_DIRECTIONS[hole];
            float[] migratedDirection = qliphothMigratedSmallBlackHoleDirection(time, hole, direction);
            float halfSize = qliphothSmallBlackHoleHalfSize(time, skyDistance, hole, direction, pomeCount >= 9);
            renderQliphothSkyTendrilCluster(buffer, matrix, time, skyDistance, migratedDirection[0], migratedDirection[1],
                    qliphothSmallBlackHoleSeed(hole), halfSize, hole, false);
        }
        if (pomeCount >= 9) {
            renderQliphothSkyTendrilCluster(buffer, matrix, time, skyDistance, 0.0F, -88.0F, 9.13F,
                    qliphothZenithBlackHoleHalfSize(time, skyDistance), 9, true);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    static void renderQliphothSkyTendrilCluster(BufferBuilder buffer, Matrix4f matrix, float time,
                                                        float skyDistance, float yaw, float pitch, float seed,
                                                        float halfSize, int holeIndex, boolean finalHole) {
        Vector3f skyPoint = qliphothSkyPoint(yaw, pitch, skyDistance * 0.82F);
        Vec3 forward = new Vec3(skyPoint.x, skyPoint.y, skyPoint.z).normalize();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        if (right.lengthSqr() < 0.0001D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }
        Vec3 up = right.cross(forward).normalize();
        Vec3 anchor = new Vec3(skyPoint.x, skyPoint.y, skyPoint.z);
        int tendrilCount = finalHole ? 9 : 2;
        double rimRadius = Mth.clamp(halfSize * (finalHole ? 0.36D : 0.42D),
                finalHole ? 7.5D : 2.0D, finalHole ? 22.0D : 5.5D);
        double pull = finalHole ? QLIPHOTH_FINAL_TENDRIL_PULL : QLIPHOTH_SMALL_TENDRIL_PULL;
        for (int tendril = 0; tendril < tendrilCount; tendril++) {
            double angle = seed * Mth.TWO_PI + tendril / (double) tendrilCount * Mth.TWO_PI;
            Vec3 rim = right.scale(Math.cos(angle) * rimRadius).add(up.scale(Math.sin(angle) * rimRadius));
            double twistAngle = angle + Math.PI * 0.5D;
            Vec3 twist = right.scale(Math.cos(twistAngle)).add(up.scale(Math.sin(twistAngle)));
            Vec3 sourceAtBlackHole = anchor.add(forward.scale(2.0D)).add(rim.scale(finalHole ? 0.58D : 0.18D));
            Vec3 targetTowardCamera = anchor.subtract(forward.scale(pull))
                    .add(rim.scale(finalHole ? 1.12D : 0.46D))
                    .add(twist.scale((finalHole ? 18.0D : 2.2D)
                            * Math.sin(time * 0.0305D + tendril * 1.73D + holeIndex)));
            long tendrilSeed = qliphothSkyTendrilSeed(holeIndex, tendril, finalHole);
            TendrilEffectConfig config = qliphothSkyTendrilConfig(tendrilSeed, finalHole);
            TendrilGeometry geometry = TendrilGeometry.generate(sourceAtBlackHole, targetTowardCamera, config,
                    tendrilSeed, time * 0.05F, TendrilGeometry.SurfaceResolver.NONE);
            emitQliphothSkyTendrilGeometry(buffer, matrix, geometry, finalHole);
        }
    }

    static void emitQliphothSkyTendrilGeometry(BufferBuilder buffer, Matrix4f matrix,
                                                       TendrilGeometry geometry, boolean finalHole) {
        int coreAlpha = finalHole ? 210 : 176;
        int glowAlpha = finalHole ? 112 : 82;
        for (TendrilGeometry.Strand strand : geometry.strands()) {
            TendrilGeometry.TubeQuads glowQuads = TendrilGeometry.createTubeQuads(strand, finalHole ? 2.65F : 2.20F);
            for (Vec3 vertex : glowQuads.vertices()) {
                buffer.addVertex(matrix, (float) vertex.x, (float) vertex.y, (float) vertex.z)
                        .setColor(209, 11, 26, glowAlpha);
            }
            TendrilGeometry.TubeQuads coreQuads = TendrilGeometry.createTubeQuads(strand, 1.0F);
            for (Vec3 vertex : coreQuads.vertices()) {
                buffer.addVertex(matrix, (float) vertex.x, (float) vertex.y, (float) vertex.z)
                        .setColor(6, 0, 4, coreAlpha);
            }
        }
    }

    static TendrilEffectConfig qliphothSkyTendrilConfig(long seed, boolean finalHole) {
        return TendrilEffectConfig.defaults()
                .withMode(TendrilEffectConfig.Mode.FREEFORM)
                .withColors(0xF0060004, finalHole ? 0xB8D10B1A : 0x96D10B1A)
                .withBlendColors(false)
                .withLifecycle(1, 1, 1)
                .withShape(finalHole ? 36 : 18, 1, finalHole ? 0.155F : 0.052F, finalHole ? 0.18F : 0.10F)
                .withBranching(finalHole ? 3 : 1, 1, finalHole ? 0.24F : 0.13F, finalHole ? 1.35F : 0.38F)
                .withWrithe(finalHole ? 2.0F : 0.10F, finalHole ? 0.095F : 0.045F,
                        finalHole ? 4.0F : 0.46F, finalHole ? -1.55F : -0.05F)
                .withFixedSeed(true, seed);
    }

    static long qliphothSkyTendrilSeed(int hole, int tendril, boolean finalHole) {
        long seed = finalHole ? 0xC2B2AE3D27D4EB4FL : 0x9E3779B97F4A7C15L;
        seed ^= (long) hole * 0x94D049BB133111EBL;
        seed ^= (long) tendril * 0xD6E8FEB86659FD93L;
        return seed;
    }

    static void renderQliphothConstellationSigils(PoseStack poseStack, Tesselator tesselator, float time,
                                                          float skyDistance, int pomeCount) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        for (int face = 0; face < 6; face++) {
            poseStack.pushPose();
            ChamberOfWillRenderHelpers.rotateSkyFace(poseStack, face);
            renderQliphothConstellationSigilFace(poseStack, tesselator, time, skyDistance, face, pomeCount);
            poseStack.popPose();
        }
    }

    static void renderQliphothConstellationSigilFace(PoseStack poseStack, Tesselator tesselator, float time,
                                                             float skyDistance, int face, int pomeCount) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        Random random = new Random(881271L + face * 12491L);
        float depth = -skyDistance * 0.974F;
        int sigilCount = 3 + face % 2;
        for (int sigil = 0; sigil < sigilCount; sigil++) {
            float phase = face * 0.91F + sigil * 2.17F;
            float drift = time * (0.00036F + sigil * 0.00008F) + phase;
            float centerX = Mth.lerp(random.nextFloat(), -0.62F, 0.62F) * skyDistance
                    + Mth.sin(drift * 0.73F) * skyDistance * 0.035F;
            float centerZ = Mth.lerp(random.nextFloat(), -0.62F, 0.62F) * skyDistance
                    + Mth.cos(drift * 0.61F) * skyDistance * 0.035F;
            float radius = skyDistance * Mth.lerp(random.nextFloat(), 0.095F, 0.175F);
            float rotation = drift * 0.52F + random.nextFloat() * Mth.TWO_PI;
            int arcAlpha = 58 + Math.min(42, pomeCount * 4);
            int shadowAlpha = 78 + Math.min(54, pomeCount * 5);
            int nodeAlpha = 96 + Math.min(70, pomeCount * 6);
            float width = radius * 0.010F;
            int variant = random.nextInt(8);
            float handedness = random.nextBoolean() ? 1.0F : -1.0F;

            if (variant == 1) {
                renderQliphothHookedBarSigil(buffer, matrix, centerX, centerZ, radius, rotation, handedness,
                        depth, width, arcAlpha, shadowAlpha, nodeAlpha, skyDistance, time, pomeCount, face,
                        random);
                continue;
            }
            if (variant == 2) {
                renderQliphothCrownedLoopSigil(buffer, matrix, centerX, centerZ, radius, rotation, handedness,
                        depth, width, arcAlpha, shadowAlpha, nodeAlpha, skyDistance, time, pomeCount, face,
                        random);
                continue;
            }
            if (variant == 3) {
                renderQliphothForkedPendantSigil(buffer, matrix, centerX, centerZ, radius, rotation, handedness,
                        depth, width, arcAlpha, shadowAlpha, nodeAlpha, skyDistance, time, pomeCount, face,
                        random);
                continue;
            }
            if (variant == 4) {
                renderQliphothLadderScrollSigil(buffer, matrix, centerX, centerZ, radius, rotation, handedness,
                        depth, width, arcAlpha, shadowAlpha, nodeAlpha, skyDistance, time, pomeCount, face,
                        random);
                continue;
            }
            if (variant == 5) {
                renderQliphothCrescentHookSigil(buffer, matrix, centerX, centerZ, radius, rotation, handedness,
                        depth, width, arcAlpha, shadowAlpha, nodeAlpha, skyDistance, time, pomeCount, face,
                        random);
                continue;
            }
            if (variant == 6) {
                renderQliphothTridentKnotSigil(buffer, matrix, centerX, centerZ, radius, rotation, handedness,
                        depth, width, arcAlpha, shadowAlpha, nodeAlpha, skyDistance, time, pomeCount, face,
                        random);
                continue;
            }
            if (variant == 7) {
                renderQliphothSteppedKeySigil(buffer, matrix, centerX, centerZ, radius, rotation, handedness,
                        depth, width, arcAlpha, shadowAlpha, nodeAlpha, skyDistance, time, pomeCount, face,
                        random);
                continue;
            }

            float[] bodyA = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    -0.92F, -0.03F, skyDistance, time, pomeCount, face);
            float[] bodyB = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    -0.48F, -0.44F, skyDistance, time, pomeCount, face);
            float[] bodyC = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    0.36F, 0.44F, skyDistance, time, pomeCount, face);
            float[] bodyD = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    0.88F, 0.04F, skyDistance, time, pomeCount, face);
            addQliphothOrganicSigilCurve(buffer, matrix, bodyA, bodyB, bodyC, bodyD,
                    depth, width, arcAlpha + 10, shadowAlpha + 18);

            float stemX = Mth.lerp(random.nextFloat(), -0.18F, 0.24F);
            float[] stemA = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    stemX, -0.82F, skyDistance, time, pomeCount, face);
            float[] stemB = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    stemX - 0.12F, -0.32F, skyDistance, time, pomeCount, face);
            float[] stemC = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    stemX + 0.16F, 0.34F, skyDistance, time, pomeCount, face);
            float[] stemD = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    stemX, 0.78F, skyDistance, time, pomeCount, face);
            addQliphothOrganicSigilCurve(buffer, matrix, stemA, stemB, stemC, stemD,
                    depth - 0.010F, width * 0.82F, arcAlpha, shadowAlpha);

            float[] barA = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    -0.74F, 0.22F, skyDistance, time, pomeCount, face);
            float[] barB = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    -0.34F, 0.34F, skyDistance, time, pomeCount, face);
            float[] barC = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    0.24F, 0.18F, skyDistance, time, pomeCount, face);
            float[] barD = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    0.72F, 0.28F, skyDistance, time, pomeCount, face);
            addQliphothOrganicSigilCurve(buffer, matrix, barA, barB, barC, barD,
                    depth - 0.014F, width * 0.64F, arcAlpha - 4, shadowAlpha - 8);

            int branchCount = 3 + random.nextInt(3);
            for (int branch = 0; branch < branchCount; branch++) {
                float side = branch % 2 == 0 ? 1.0F : -1.0F;
                float anchor = Mth.lerp(random.nextFloat(), -0.58F, 0.52F);
                float rise = Mth.lerp(random.nextFloat(), -0.36F, 0.42F);
                float[] branchA = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                        anchor, rise, skyDistance, time, pomeCount, face);
                float[] branchB = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                        anchor + side * 0.12F, rise + 0.24F, skyDistance, time, pomeCount, face);
                float[] branchC = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                        anchor + side * 0.34F, rise + Mth.sin(phase + branch) * 0.28F,
                        skyDistance, time, pomeCount, face);
                float[] branchD = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                        anchor + side * 0.54F, rise - 0.12F, skyDistance, time, pomeCount, face);
                addQliphothOrganicSigilCurve(buffer, matrix, branchA, branchB, branchC, branchD,
                        depth - 0.020F, width * 0.48F, arcAlpha - 8, shadowAlpha - 12);
                addQliphothOrganicSigilCurl(buffer, matrix, centerX, centerZ, radius, rotation,
                        anchor + side * 0.56F, rise - 0.12F, 0.115F, side > 0.0F ? -1.35F : 0.30F,
                        side * 4.85F, depth - 0.026F, width * 0.42F, arcAlpha - 2, shadowAlpha,
                        skyDistance, time, pomeCount, face);
                float[] terminal = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                        anchor + side * 0.58F, rise - 0.12F, skyDistance, time, pomeCount, face);
                float nodeRadius = skyDistance * (0.0027F + branch % 3 * 0.00052F);
                addQliphothSigilNode(buffer, matrix, terminal[0], terminal[1], depth - 0.030F,
                        nodeRadius, 255, 214, 54, nodeAlpha);
            }

            addQliphothOrganicSigilCurl(buffer, matrix, centerX, centerZ, radius, rotation,
                    -0.84F, -0.04F, 0.14F, 0.65F, -4.80F,
                    depth - 0.024F, width * 0.46F, arcAlpha, shadowAlpha,
                    skyDistance, time, pomeCount, face);
            addQliphothOrganicSigilCurl(buffer, matrix, centerX, centerZ, radius, rotation,
                    0.84F, 0.04F, 0.14F, 2.40F, 4.80F,
                    depth - 0.024F, width * 0.46F, arcAlpha, shadowAlpha,
                    skyDistance, time, pomeCount, face);

            float[] runeAnchor = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    stemX, 0.08F, skyDistance, time, pomeCount, face);
            float[] runeTangent = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    stemX + 0.28F, 0.15F, skyDistance, time, pomeCount, face);
            addQliphothGoeticRuneBar(buffer, matrix, runeAnchor[0], runeAnchor[1],
                    runeTangent[0] - runeAnchor[0], runeTangent[1] - runeAnchor[1],
                    depth - 0.030F, radius * 0.145F, arcAlpha + 8, shadowAlpha);
            addQliphothSigilNode(buffer, matrix, centerX, centerZ, depth - 0.024F,
                    skyDistance * 0.0042F, 255, 214, 54, nodeAlpha);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    static void renderQliphothHookedBarSigil(BufferBuilder buffer, Matrix4f matrix, float centerX,
                                                     float centerZ, float radius, float rotation,
                                                     float handedness, float depth, float width, int arcAlpha,
                                                     int shadowAlpha, int nodeAlpha, float skyDistance,
                                                     float time, int pomeCount, int face, Random random) {
        float barLift = Mth.lerp(random.nextFloat(), -0.18F, 0.18F);
        float[] barA = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                -0.94F, barLift, skyDistance, time, pomeCount, face);
        float[] barB = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                -0.38F, barLift - 0.18F * handedness, skyDistance, time, pomeCount, face);
        float[] barC = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                0.34F, barLift + 0.15F * handedness, skyDistance, time, pomeCount, face);
        float[] barD = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                0.96F, barLift - 0.04F, skyDistance, time, pomeCount, face);
        addQliphothOrganicSigilCurve(buffer, matrix, barA, barB, barC, barD,
                depth, width * 1.05F, arcAlpha + 12, shadowAlpha + 14);

        float postX = Mth.lerp(random.nextFloat(), -0.32F, 0.28F);
        float[] postA = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                postX, -0.86F, skyDistance, time, pomeCount, face);
        float[] postB = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                postX + 0.08F * handedness, -0.30F, skyDistance, time, pomeCount, face);
        float[] postC = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                postX - 0.10F * handedness, 0.28F, skyDistance, time, pomeCount, face);
        float[] postD = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                postX, 0.86F, skyDistance, time, pomeCount, face);
        addQliphothOrganicSigilCurve(buffer, matrix, postA, postB, postC, postD,
                depth - 0.012F, width * 0.80F, arcAlpha + 2, shadowAlpha);

        addQliphothOrganicSigilCurl(buffer, matrix, centerX, centerZ, radius, rotation,
                -0.95F, barLift, 0.17F, 0.25F, -handedness * 4.95F,
                depth - 0.024F, width * 0.48F, arcAlpha, shadowAlpha,
                skyDistance, time, pomeCount, face);
        addQliphothOrganicSigilCurl(buffer, matrix, centerX, centerZ, radius, rotation,
                0.96F, barLift - 0.04F, 0.13F, 2.20F, handedness * 4.20F,
                depth - 0.024F, width * 0.44F, arcAlpha, shadowAlpha,
                skyDistance, time, pomeCount, face);

        for (int mark = 0; mark < 3; mark++) {
            float markX = Mth.lerp(mark / 2.0F, -0.45F, 0.52F);
            float[] anchor = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    markX, barLift + (mark - 1) * 0.07F, skyDistance, time, pomeCount, face);
            float[] tangent = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    markX + 0.20F, barLift + 0.04F * handedness, skyDistance, time, pomeCount, face);
            addQliphothGoeticRuneBar(buffer, matrix, anchor[0], anchor[1],
                    tangent[0] - anchor[0], tangent[1] - anchor[1],
                    depth - 0.030F, radius * 0.105F, arcAlpha, shadowAlpha - 6);
        }
        addQliphothSigilNode(buffer, matrix, postD[0], postD[1], depth - 0.030F,
                skyDistance * 0.0038F, 255, 214, 54, nodeAlpha);
        addQliphothSigilNode(buffer, matrix, barA[0], barA[1], depth - 0.030F,
                skyDistance * 0.0030F, 255, 214, 54, nodeAlpha - 10);
    }

    static void renderQliphothCrownedLoopSigil(BufferBuilder buffer, Matrix4f matrix, float centerX,
                                                       float centerZ, float radius, float rotation,
                                                       float handedness, float depth, float width, int arcAlpha,
                                                       int shadowAlpha, int nodeAlpha, float skyDistance,
                                                       float time, int pomeCount, int face, Random random) {
        float[] bowlA = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                -0.74F, -0.08F, skyDistance, time, pomeCount, face);
        float[] bowlB = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                -0.58F, 0.72F, skyDistance, time, pomeCount, face);
        float[] bowlC = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                0.56F, 0.74F, skyDistance, time, pomeCount, face);
        float[] bowlD = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                0.78F, -0.06F, skyDistance, time, pomeCount, face);
        addQliphothOrganicSigilCurve(buffer, matrix, bowlA, bowlB, bowlC, bowlD,
                depth, width * 1.08F, arcAlpha + 8, shadowAlpha + 12);

        float crownBase = Mth.lerp(random.nextFloat(), -0.44F, -0.30F);
        for (int crown = 0; crown < 4; crown++) {
            float x = Mth.lerp(crown / 3.0F, -0.54F, 0.54F);
            float top = crownBase - (crown % 2 == 0 ? 0.30F : 0.18F);
            float[] stemA = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    x, crownBase, skyDistance, time, pomeCount, face);
            float[] stemB = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    x + 0.07F * handedness, top, skyDistance, time, pomeCount, face);
            addQliphothGoeticSealStroke(buffer, matrix, stemA[0], stemA[1], stemB[0], stemB[1],
                    depth - 0.016F, width * 0.58F, arcAlpha, shadowAlpha);
            addQliphothOrganicSigilCurl(buffer, matrix, centerX, centerZ, radius, rotation,
                    x + 0.04F * handedness, top, 0.082F, crown * 0.45F, handedness * 4.40F,
                    depth - 0.024F, width * 0.34F, arcAlpha + 4, shadowAlpha,
                    skyDistance, time, pomeCount, face);
            addQliphothSigilNode(buffer, matrix, stemB[0], stemB[1], depth - 0.030F,
                    skyDistance * 0.0028F, 255, 214, 54, nodeAlpha);
        }

        float[] axisA = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                0.0F, -0.74F, skyDistance, time, pomeCount, face);
        float[] axisB = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                0.12F * handedness, 0.66F, skyDistance, time, pomeCount, face);
        addQliphothGoeticSealStroke(buffer, matrix, axisA[0], axisA[1], axisB[0], axisB[1],
                depth - 0.020F, width * 0.72F, arcAlpha - 2, shadowAlpha - 6);
        addQliphothGoeticRuneBar(buffer, matrix, axisA[0], axisA[1],
                handedness * radius * 0.20F, radius * 0.06F,
                depth - 0.030F, radius * 0.135F, arcAlpha + 4, shadowAlpha);
        addQliphothSigilNode(buffer, matrix, axisA[0], axisA[1], depth - 0.030F,
                skyDistance * 0.0036F, 255, 214, 54, nodeAlpha);
    }

    static void renderQliphothForkedPendantSigil(BufferBuilder buffer, Matrix4f matrix, float centerX,
                                                         float centerZ, float radius, float rotation,
                                                         float handedness, float depth, float width,
                                                         int arcAlpha, int shadowAlpha, int nodeAlpha,
                                                         float skyDistance, float time, int pomeCount, int face,
                                                         Random random) {
        float[] spineA = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                -0.58F * handedness, -0.76F, skyDistance, time, pomeCount, face);
        float[] spineB = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                -0.24F * handedness, -0.24F, skyDistance, time, pomeCount, face);
        float[] spineC = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                0.28F * handedness, 0.28F, skyDistance, time, pomeCount, face);
        float[] spineD = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                0.66F * handedness, 0.82F, skyDistance, time, pomeCount, face);
        addQliphothOrganicSigilCurve(buffer, matrix, spineA, spineB, spineC, spineD,
                depth, width * 1.02F, arcAlpha + 8, shadowAlpha + 10);

        for (int fork = 0; fork < 4; fork++) {
            float baseT = Mth.lerp(fork / 3.0F, -0.46F, 0.44F);
            float side = fork % 2 == 0 ? 1.0F : -1.0F;
            float forkLength = Mth.lerp(random.nextFloat(), 0.38F, 0.66F);
            float[] branchA = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    baseT * handedness, Mth.lerp(fork / 3.0F, -0.42F, 0.42F),
                    skyDistance, time, pomeCount, face);
            float[] branchB = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    (baseT + side * forkLength * 0.26F) * handedness, 0.10F + fork * 0.04F,
                    skyDistance, time, pomeCount, face);
            float[] branchC = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    (baseT + side * forkLength * 0.46F) * handedness, -0.12F + fork * 0.12F,
                    skyDistance, time, pomeCount, face);
            float[] branchD = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    (baseT + side * forkLength) * handedness, -0.24F + fork * 0.20F,
                    skyDistance, time, pomeCount, face);
            addQliphothOrganicSigilCurve(buffer, matrix, branchA, branchB, branchC, branchD,
                    depth - 0.018F, width * 0.52F, arcAlpha - 4, shadowAlpha - 10);
            addQliphothOrganicSigilCurl(buffer, matrix, centerX, centerZ, radius, rotation,
                    (baseT + side * forkLength) * handedness, -0.24F + fork * 0.20F,
                    0.075F + fork * 0.012F, side > 0.0F ? 0.10F : 2.40F, side * handedness * 4.15F,
                    depth - 0.026F, width * 0.34F, arcAlpha, shadowAlpha,
                    skyDistance, time, pomeCount, face);
            addQliphothSigilNode(buffer, matrix, branchD[0], branchD[1], depth - 0.030F,
                    skyDistance * (0.0026F + fork * 0.00025F), 255, 214, 54, nodeAlpha - 8);
        }

        float pendantX = Mth.lerp(random.nextFloat(), -0.16F, 0.18F) * handedness;
        addQliphothOrganicSigilCurl(buffer, matrix, centerX, centerZ, radius, rotation,
                pendantX, 0.94F, 0.16F, 1.55F, handedness * 6.20F,
                depth - 0.026F, width * 0.46F, arcAlpha + 4, shadowAlpha,
                skyDistance, time, pomeCount, face);
        float[] pendant = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                pendantX, 0.94F, skyDistance, time, pomeCount, face);
        addQliphothSigilNode(buffer, matrix, pendant[0], pendant[1], depth - 0.030F,
                skyDistance * 0.0040F, 255, 214, 54, nodeAlpha);
    }

    static void renderQliphothLadderScrollSigil(BufferBuilder buffer, Matrix4f matrix, float centerX,
                                                        float centerZ, float radius, float rotation,
                                                        float handedness, float depth, float width, int arcAlpha,
                                                        int shadowAlpha, int nodeAlpha, float skyDistance,
                                                        float time, int pomeCount, int face, Random random) {
        float lean = Mth.lerp(random.nextFloat(), -0.16F, 0.18F) * handedness;
        float[] railA = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                -0.32F + lean, -0.86F, skyDistance, time, pomeCount, face);
        float[] railB = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                -0.48F + lean, -0.20F, skyDistance, time, pomeCount, face);
        float[] railC = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                -0.18F + lean, 0.34F, skyDistance, time, pomeCount, face);
        float[] railD = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                -0.34F + lean, 0.88F, skyDistance, time, pomeCount, face);
        addQliphothOrganicSigilCurve(buffer, matrix, railA, railB, railC, railD,
                depth, width * 0.92F, arcAlpha + 8, shadowAlpha + 8);

        float[] rail2A = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                0.28F + lean, -0.78F, skyDistance, time, pomeCount, face);
        float[] rail2B = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                0.12F + lean, -0.16F, skyDistance, time, pomeCount, face);
        float[] rail2C = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                0.48F + lean, 0.30F, skyDistance, time, pomeCount, face);
        float[] rail2D = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                0.20F + lean, 0.76F, skyDistance, time, pomeCount, face);
        addQliphothOrganicSigilCurve(buffer, matrix, rail2A, rail2B, rail2C, rail2D,
                depth - 0.010F, width * 0.78F, arcAlpha, shadowAlpha);

        for (int rung = 0; rung < 4; rung++) {
            float y = Mth.lerp(rung / 3.0F, -0.58F, 0.58F);
            float slant = Mth.sin(rung * 1.7F + handedness) * 0.08F;
            float[] rungA = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    -0.46F + lean, y, skyDistance, time, pomeCount, face);
            float[] rungB = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    0.44F + lean, y + slant, skyDistance, time, pomeCount, face);
            addQliphothGoeticSealStroke(buffer, matrix, rungA[0], rungA[1], rungB[0], rungB[1],
                    depth - 0.018F, width * 0.46F, arcAlpha - 4, shadowAlpha - 10);
            if (rung % 2 == 0) {
                addQliphothOrganicSigilCurl(buffer, matrix, centerX, centerZ, radius, rotation,
                        0.50F + lean, y + slant, 0.082F, 2.10F, handedness * 4.20F,
                        depth - 0.026F, width * 0.32F, arcAlpha, shadowAlpha,
                        skyDistance, time, pomeCount, face);
            }
        }

        addQliphothOrganicSigilCurl(buffer, matrix, centerX, centerZ, radius, rotation,
                -0.34F + lean, -0.88F, 0.12F, 1.30F, -handedness * 5.20F,
                depth - 0.026F, width * 0.40F, arcAlpha, shadowAlpha,
                skyDistance, time, pomeCount, face);
        addQliphothSigilNode(buffer, matrix, railD[0], railD[1], depth - 0.030F,
                skyDistance * 0.0034F, 255, 214, 54, nodeAlpha);
        addQliphothSigilNode(buffer, matrix, rail2A[0], rail2A[1], depth - 0.030F,
                skyDistance * 0.0028F, 255, 214, 54, nodeAlpha - 8);
    }

    static void renderQliphothCrescentHookSigil(BufferBuilder buffer, Matrix4f matrix, float centerX,
                                                        float centerZ, float radius, float rotation,
                                                        float handedness, float depth, float width, int arcAlpha,
                                                        int shadowAlpha, int nodeAlpha, float skyDistance,
                                                        float time, int pomeCount, int face, Random random) {
        float openSide = handedness;
        float[] crescentA = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                -0.70F * openSide, -0.62F, skyDistance, time, pomeCount, face);
        float[] crescentB = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                -1.00F * openSide, -0.04F, skyDistance, time, pomeCount, face);
        float[] crescentC = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                -0.78F * openSide, 0.60F, skyDistance, time, pomeCount, face);
        float[] crescentD = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                -0.20F * openSide, 0.78F, skyDistance, time, pomeCount, face);
        addQliphothOrganicSigilCurve(buffer, matrix, crescentA, crescentB, crescentC, crescentD,
                depth, width * 1.10F, arcAlpha + 10, shadowAlpha + 14);

        float[] chordA = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                -0.16F * openSide, -0.66F, skyDistance, time, pomeCount, face);
        float[] chordB = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                0.18F * openSide, -0.20F, skyDistance, time, pomeCount, face);
        float[] chordC = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                0.05F * openSide, 0.36F, skyDistance, time, pomeCount, face);
        float[] chordD = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                0.42F * openSide, 0.72F, skyDistance, time, pomeCount, face);
        addQliphothOrganicSigilCurve(buffer, matrix, chordA, chordB, chordC, chordD,
                depth - 0.012F, width * 0.70F, arcAlpha, shadowAlpha - 4);

        float hookY = Mth.lerp(random.nextFloat(), -0.24F, 0.24F);
        float[] hookA = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                -0.18F * openSide, hookY, skyDistance, time, pomeCount, face);
        float[] hookB = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                0.28F * openSide, hookY - 0.20F, skyDistance, time, pomeCount, face);
        float[] hookC = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                0.62F * openSide, hookY + 0.18F, skyDistance, time, pomeCount, face);
        float[] hookD = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                0.86F * openSide, hookY - 0.04F, skyDistance, time, pomeCount, face);
        addQliphothOrganicSigilCurve(buffer, matrix, hookA, hookB, hookC, hookD,
                depth - 0.018F, width * 0.58F, arcAlpha + 2, shadowAlpha - 8);
        addQliphothOrganicSigilCurl(buffer, matrix, centerX, centerZ, radius, rotation,
                0.88F * openSide, hookY - 0.04F, 0.13F, 2.00F, openSide * 5.60F,
                depth - 0.026F, width * 0.38F, arcAlpha + 4, shadowAlpha,
                skyDistance, time, pomeCount, face);

        addQliphothGoeticRuneBar(buffer, matrix, chordA[0], chordA[1],
                chordD[0] - chordA[0], chordD[1] - chordA[1],
                depth - 0.030F, radius * 0.135F, arcAlpha, shadowAlpha);
        addQliphothSigilNode(buffer, matrix, crescentA[0], crescentA[1], depth - 0.030F,
                skyDistance * 0.0030F, 255, 214, 54, nodeAlpha - 8);
        addQliphothSigilNode(buffer, matrix, crescentD[0], crescentD[1], depth - 0.030F,
                skyDistance * 0.0038F, 255, 214, 54, nodeAlpha);
    }

    static void renderQliphothTridentKnotSigil(BufferBuilder buffer, Matrix4f matrix, float centerX,
                                                       float centerZ, float radius, float rotation,
                                                       float handedness, float depth, float width, int arcAlpha,
                                                       int shadowAlpha, int nodeAlpha, float skyDistance,
                                                       float time, int pomeCount, int face, Random random) {
        float baseX = Mth.lerp(random.nextFloat(), -0.12F, 0.12F);
        float[] stemA = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                baseX, 0.88F, skyDistance, time, pomeCount, face);
        float[] stemB = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                baseX - 0.16F * handedness, 0.28F, skyDistance, time, pomeCount, face);
        float[] stemC = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                baseX + 0.12F * handedness, -0.28F, skyDistance, time, pomeCount, face);
        float[] stemD = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                baseX, -0.88F, skyDistance, time, pomeCount, face);
        addQliphothOrganicSigilCurve(buffer, matrix, stemA, stemB, stemC, stemD,
                depth, width * 1.00F, arcAlpha + 8, shadowAlpha + 8);

        for (int tine = -1; tine <= 1; tine++) {
            float x = baseX + tine * 0.34F;
            float[] tineA = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    baseX, -0.36F, skyDistance, time, pomeCount, face);
            float[] tineB = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    x - tine * 0.12F, -0.54F, skyDistance, time, pomeCount, face);
            float[] tineC = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    x + tine * 0.10F, -0.70F, skyDistance, time, pomeCount, face);
            float[] tineD = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    x, -0.94F, skyDistance, time, pomeCount, face);
            addQliphothOrganicSigilCurve(buffer, matrix, tineA, tineB, tineC, tineD,
                    depth - 0.016F, width * (tine == 0 ? 0.60F : 0.48F), arcAlpha, shadowAlpha - 8);
            if (tine != 0) {
                addQliphothOrganicSigilCurl(buffer, matrix, centerX, centerZ, radius, rotation,
                        x, -0.94F, 0.08F, tine > 0 ? 0.15F : 2.45F, tine * handedness * 4.45F,
                        depth - 0.026F, width * 0.32F, arcAlpha, shadowAlpha,
                        skyDistance, time, pomeCount, face);
            }
            addQliphothSigilNode(buffer, matrix, tineD[0], tineD[1], depth - 0.030F,
                    skyDistance * 0.0028F, 255, 214, 54, nodeAlpha - 8);
        }

        addQliphothOrganicSigilCurl(buffer, matrix, centerX, centerZ, radius, rotation,
                baseX, 0.12F, 0.19F, 0.40F, handedness * 6.35F,
                depth - 0.022F, width * 0.50F, arcAlpha + 4, shadowAlpha,
                skyDistance, time, pomeCount, face);
        addQliphothOrganicSigilCurl(buffer, matrix, centerX, centerZ, radius, rotation,
                baseX, 0.12F, 0.11F, 2.80F, -handedness * 5.10F,
                depth - 0.026F, width * 0.34F, arcAlpha, shadowAlpha,
                skyDistance, time, pomeCount, face);
        addQliphothSigilNode(buffer, matrix, stemA[0], stemA[1], depth - 0.030F,
                skyDistance * 0.0038F, 255, 214, 54, nodeAlpha);
    }

    static void renderQliphothSteppedKeySigil(BufferBuilder buffer, Matrix4f matrix, float centerX,
                                                      float centerZ, float radius, float rotation,
                                                      float handedness, float depth, float width, int arcAlpha,
                                                      int shadowAlpha, int nodeAlpha, float skyDistance,
                                                      float time, int pomeCount, int face, Random random) {
        float baseY = Mth.lerp(random.nextFloat(), -0.12F, 0.18F);
        float[][] keyPoints = {
                {-0.78F * handedness, -0.54F + baseY},
                {-0.28F * handedness, -0.54F + baseY},
                {-0.28F * handedness, -0.18F + baseY},
                {0.20F * handedness, -0.18F + baseY},
                {0.20F * handedness, 0.18F + baseY},
                {0.72F * handedness, 0.18F + baseY}
        };
        for (int i = 0; i < keyPoints.length - 1; i++) {
            float[] a = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    keyPoints[i][0], keyPoints[i][1], skyDistance, time, pomeCount, face);
            float[] b = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    keyPoints[i + 1][0], keyPoints[i + 1][1], skyDistance, time, pomeCount, face);
            addQliphothGoeticSealStroke(buffer, matrix, a[0], a[1], b[0], b[1],
                    depth - i * 0.003F, width * 0.72F, arcAlpha + (i % 2 == 0 ? 4 : -4), shadowAlpha);
            if (i == 1 || i == 3) {
                addQliphothGoeticRuneBar(buffer, matrix, b[0], b[1],
                        handedness * radius * 0.16F, radius * 0.04F,
                        depth - 0.028F, radius * 0.095F, arcAlpha, shadowAlpha - 8);
            }
        }

        float[] loopRoot = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                -0.78F * handedness, -0.54F + baseY, skyDistance, time, pomeCount, face);
        addQliphothOrganicSigilCurl(buffer, matrix, centerX, centerZ, radius, rotation,
                -0.78F * handedness, -0.54F + baseY, 0.17F, 0.20F, -handedness * 6.10F,
                depth - 0.026F, width * 0.45F, arcAlpha + 4, shadowAlpha,
                skyDistance, time, pomeCount, face);
        addQliphothOrganicSigilCurl(buffer, matrix, centerX, centerZ, radius, rotation,
                0.72F * handedness, 0.18F + baseY, 0.12F, 2.30F, handedness * 4.90F,
                depth - 0.026F, width * 0.36F, arcAlpha, shadowAlpha,
                skyDistance, time, pomeCount, face);

        float[] pendantA = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                0.20F * handedness, 0.18F + baseY, skyDistance, time, pomeCount, face);
        float[] pendantB = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                0.04F * handedness, 0.78F + baseY, skyDistance, time, pomeCount, face);
        addQliphothGoeticSealStroke(buffer, matrix, pendantA[0], pendantA[1], pendantB[0], pendantB[1],
                depth - 0.020F, width * 0.50F, arcAlpha - 2, shadowAlpha - 8);
        addQliphothSigilNode(buffer, matrix, loopRoot[0], loopRoot[1], depth - 0.030F,
                skyDistance * 0.0035F, 255, 214, 54, nodeAlpha);
        addQliphothSigilNode(buffer, matrix, pendantB[0], pendantB[1], depth - 0.030F,
                skyDistance * 0.0032F, 255, 214, 54, nodeAlpha - 8);
    }

    static void addQliphothGoeticSealStroke(BufferBuilder buffer, Matrix4f matrix, float startX, float startZ,
                                                    float endX, float endZ, float depth, float width, int redAlpha,
                                                    int shadowAlpha) {
        addQliphothRootStroke(buffer, matrix, startX, startZ, endX, endZ, depth + 0.004F, width * 1.85F,
                0x070006, shadowAlpha);
        addQliphothRootStroke(buffer, matrix, startX, startZ, endX, endZ, depth - 0.004F, width,
                0xFFD63A, redAlpha);
        addQliphothRootStroke(buffer, matrix, startX, startZ, endX, endZ, depth - 0.008F, width * 0.38F,
                0x3A2100, Math.min(180, shadowAlpha + 28));
    }

    static void addQliphothGoeticRuneBar(BufferBuilder buffer, Matrix4f matrix, float anchorX, float anchorZ,
                                                 float tangentX, float tangentZ, float depth, float size,
                                                 int redAlpha, int shadowAlpha) {
        float length = Mth.sqrt(tangentX * tangentX + tangentZ * tangentZ);
        if (length <= 0.0001F) {
            return;
        }
        float normalX = -tangentZ / length;
        float normalZ = tangentX / length;
        float tangentNormalX = tangentX / length;
        float tangentNormalZ = tangentZ / length;
        float barHalf = size * 0.50F;
        float hookHalf = size * 0.28F;
        float width = size * 0.070F;

        addQliphothGoeticSealStroke(buffer, matrix, anchorX - normalX * barHalf, anchorZ - normalZ * barHalf,
                anchorX + normalX * barHalf, anchorZ + normalZ * barHalf, depth, width, redAlpha, shadowAlpha);
        addQliphothGoeticSealStroke(buffer, matrix,
                anchorX + normalX * barHalf - tangentNormalX * hookHalf,
                anchorZ + normalZ * barHalf - tangentNormalZ * hookHalf,
                anchorX + normalX * barHalf + tangentNormalX * hookHalf,
                anchorZ + normalZ * barHalf + tangentNormalZ * hookHalf,
                depth - 0.004F, width * 0.72F, redAlpha - 12, shadowAlpha - 16);
    }

    static void addQliphothOrganicSigilCurve(BufferBuilder buffer, Matrix4f matrix, float[] start,
                                                     float[] controlA, float[] controlB, float[] end,
                                                     float depth, float width, int redAlpha, int shadowAlpha) {
        int segments = 16;
        float previousX = start[0];
        float previousZ = start[1];
        for (int segment = 1; segment <= segments; segment++) {
            float t = segment / (float) segments;
            float inverse = 1.0F - t;
            float x = inverse * inverse * inverse * start[0]
                    + 3.0F * inverse * inverse * t * controlA[0]
                    + 3.0F * inverse * t * t * controlB[0]
                    + t * t * t * end[0];
            float z = inverse * inverse * inverse * start[1]
                    + 3.0F * inverse * inverse * t * controlA[1]
                    + 3.0F * inverse * t * t * controlB[1]
                    + t * t * t * end[1];
            addQliphothGoeticSealStroke(buffer, matrix, previousX, previousZ, x, z, depth, width,
                    redAlpha, shadowAlpha);
            previousX = x;
            previousZ = z;
        }
    }

    static void addQliphothOrganicSigilCurl(BufferBuilder buffer, Matrix4f matrix, float centerX,
                                                    float centerZ, float radius, float rotation, float localX,
                                                    float localZ, float localRadius, float startAngle, float arc,
                                                    float depth, float width, int redAlpha, int shadowAlpha,
                                                    float skyDistance, float time, int pomeCount, int face) {
        int segments = 18;
        float[] previous = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                localX + Mth.cos(startAngle) * localRadius, localZ + Mth.sin(startAngle) * localRadius,
                skyDistance, time, pomeCount, face);
        for (int segment = 1; segment <= segments; segment++) {
            float t = segment / (float) segments;
            float angle = startAngle + arc * t;
            float spiralScale = Mth.lerp(t, 1.0F, 0.28F);
            float[] next = qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,
                    localX + Mth.cos(angle) * localRadius * spiralScale,
                    localZ + Mth.sin(angle) * localRadius * spiralScale,
                    skyDistance, time, pomeCount, face);
            addQliphothGoeticSealStroke(buffer, matrix, previous[0], previous[1], next[0], next[1],
                    depth, width, redAlpha, shadowAlpha);
            previous = next;
        }
    }

    static float[] qliphothOrganicSigilPoint(float centerX, float centerZ, float radius, float rotation,
                                                     float localX, float localZ, float skyDistance, float time,
                                                     int pomeCount, int face) {
        float cos = Mth.cos(rotation);
        float sin = Mth.sin(rotation);
        float x = centerX + (localX * cos - localZ * sin) * radius;
        float z = centerZ + (localX * sin + localZ * cos) * radius;
        float yaw = face * 61.0F + x / skyDistance * 42.0F;
        float pitch = -z / skyDistance * 36.0F;
        float[] pull = qliphothSigilLensPull(time, pomeCount, yaw, pitch);
        return new float[]{x + pull[0] * skyDistance * 0.018F, z + pull[1] * skyDistance * 0.018F};
    }

    static float[] qliphothSigilLensPull(float time, int pomeCount, float yaw, float pitch) {
        int smallHoleCount = Math.min(8, pomeCount);
        float yawPull = 0.0F;
        float pitchPull = 0.0F;
        for (int hole = 0; hole < smallHoleCount; hole++) {
            float[] direction = QLIPHOTH_SMALL_HOLE_DIRECTIONS[hole];
            float[] migratedDirection = qliphothMigratedSmallBlackHoleDirection(time, hole, direction);
            float yawDelta = Mth.wrapDegrees(migratedDirection[0] - yaw);
            float pitchDelta = migratedDirection[1] - pitch;
            float distance = yawDelta * yawDelta + pitchDelta * pitchDelta;
            float pull = direction[2] * Mth.clamp(1.0F - distance / 3600.0F, 0.0F, 1.0F);
            yawPull += yawDelta * pull * 0.020F;
            pitchPull += pitchDelta * pull * 0.020F;
        }
        return new float[]{yawPull, pitchPull};
    }

    static void addQliphothSigilNode(BufferBuilder buffer, Matrix4f matrix, float nodeX, float nodeZ,
                                             float depth, float nodeRadius, int red, int green, int blue,
                                             int nodeAlpha) {
        int segments = 10;
        for (int segment = 0; segment < segments; segment++) {
            float angleA = segment / (float) segments * Mth.TWO_PI;
            float angleB = (segment + 1) / (float) segments * Mth.TWO_PI;
            buffer.addVertex(matrix, nodeX, depth, nodeZ).setColor(red, green, blue, nodeAlpha);
            buffer.addVertex(matrix, nodeX + Mth.cos(angleA) * nodeRadius, depth,
                    nodeZ + Mth.sin(angleA) * nodeRadius).setColor(red, green, blue, 0);
            buffer.addVertex(matrix, nodeX + Mth.cos(angleB) * nodeRadius, depth,
                    nodeZ + Mth.sin(angleB) * nodeRadius).setColor(red, green, blue, 0);
        }
    }

    static void renderQliphothRootScript(PoseStack poseStack, Tesselator tesselator, float time,
                                                 float skyDistance) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        for (int face = 0; face < 6; face++) {
            poseStack.pushPose();
            ChamberOfWillRenderHelpers.rotateSkyFace(poseStack, face);
            renderQliphothRootScriptFace(poseStack, tesselator, time, skyDistance, face);
            poseStack.popPose();
        }
    }

    static void renderQliphothRootScriptFace(PoseStack poseStack, Tesselator tesselator, float time,
                                                     float skyDistance, int face) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        Random random = new Random(712631L + face * 9643L);
        float depth = -skyDistance * 0.985F;
        int rootCount = 8;
        for (int root = 0; root < rootCount; root++) {
            float rootT = root / (float) rootCount;
            float phase = root * 1.37F + face * 0.61F;
            float startX = Mth.lerp(random.nextFloat(), -0.78F, 0.78F) * skyDistance;
            float startZ = Mth.lerp(random.nextFloat(), -0.78F, 0.78F) * skyDistance;
            float angle = rootT * Mth.TWO_PI + Mth.sin(time * 0.0008F + phase) * 0.21F;
            float span = skyDistance * Mth.lerp(random.nextFloat(), 0.18F, 0.42F);
            int segments = 5 + random.nextInt(4);
            float prevX = startX;
            float prevZ = startZ;
            for (int segment = 1; segment <= segments; segment++) {
                float t = segment / (float) segments;
                float bend = Mth.sin(t * Mth.PI * 2.0F + phase + time * 0.0012F) * skyDistance * 0.035F;
                float branch = Mth.sin(t * Mth.PI + root * 0.73F) * skyDistance * 0.030F;
                float nextX = startX + Mth.cos(angle) * span * t - Mth.sin(angle) * (bend + branch);
                float nextZ = startZ + Mth.sin(angle) * span * t + Mth.cos(angle) * (bend - branch);
                float width = skyDistance * Mth.lerp(t, 0.0048F, 0.0016F);
                int color = root % 3 == 0 ? 0xB24B174E : root % 3 == 1 ? 0x96322174 : 0x885D3FA2;
                int alpha = (int) Mth.clamp(Mth.lerp(t, 42.0F, 16.0F)
                        + Mth.sin(time * 0.018F + phase) * 6.0F, 8.0F, 54.0F);
                addQliphothRootStroke(buffer, matrix, prevX, prevZ, nextX, nextZ, depth, width, color, alpha);
                if ((segment + root) % 3 == 0) {
                    float branchAngle = angle + Mth.lerp(random.nextFloat(), -0.82F, 0.82F);
                    float branchLength = span * Mth.lerp(random.nextFloat(), 0.12F, 0.25F);
                    addQliphothRootStroke(buffer, matrix, nextX, nextZ,
                            nextX + Mth.cos(branchAngle) * branchLength,
                            nextZ + Mth.sin(branchAngle) * branchLength,
                            depth - 0.02F, width * 0.62F, color, alpha / 2);
                }
                if ((segment + face) % 4 == 0) {
                    addQliphothStarGlyph(buffer, matrix, nextX, nextZ, depth - 0.04F,
                            skyDistance * Mth.lerp(random.nextFloat(), 0.004F, 0.009F), alpha + 16);
                }
                prevX = nextX;
                prevZ = nextZ;
            }
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    static void addQliphothRootStroke(BufferBuilder buffer, Matrix4f matrix, float startX, float startZ,
                                              float endX, float endZ, float depth, float width, int color,
                                              int alpha) {
        float dx = endX - startX;
        float dz = endZ - startZ;
        float length = Mth.sqrt(dx * dx + dz * dz);
        if (length <= 0.0001F) {
            return;
        }
        float nx = -dz / length * width;
        float nz = dx / length * width;
        int red = (color >> 16) & 255;
        int green = (color >> 8) & 255;
        int blue = color & 255;
        int edgeAlpha = Math.max(0, alpha / 5);

        buffer.addVertex(matrix, startX - nx, depth, startZ - nz).setColor(red, green, blue, edgeAlpha);
        buffer.addVertex(matrix, startX + nx, depth, startZ + nz).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, endX + nx, depth, endZ + nz).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, startX - nx, depth, startZ - nz).setColor(red, green, blue, edgeAlpha);
        buffer.addVertex(matrix, endX + nx, depth, endZ + nz).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, endX - nx, depth, endZ - nz).setColor(red, green, blue, edgeAlpha);
    }

    static void addQliphothStarGlyph(BufferBuilder buffer, Matrix4f matrix, float x, float z, float depth,
                                             float radius, int alpha) {
        int clampedAlpha = (int) Mth.clamp(alpha, 0.0F, 86.0F);
        addQliphothRootStroke(buffer, matrix, x - radius, z, x + radius, z, depth, radius * 0.11F,
                0xFFE9B5, clampedAlpha);
        addQliphothRootStroke(buffer, matrix, x, z - radius, x, z + radius, depth, radius * 0.11F,
                0xFFE9B5, clampedAlpha);
    }

    static void renderQliphothBlackHoles(PoseStack poseStack, float time, float skyDistance,
                                                 ChamberSkyTheme theme, int pomeCount) {
        int smallHoleCount = Math.min(8, pomeCount);
        if (smallHoleCount <= 0 && pomeCount < 9) {
            return;
        }

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        for (int hole = 0; hole < smallHoleCount; hole++) {
            float[] direction = QLIPHOTH_SMALL_HOLE_DIRECTIONS[hole];
            float[] migratedDirection = qliphothMigratedSmallBlackHoleDirection(time, hole, direction);
            if (pomeCount >= 9) {
                renderQliphothResidualRing(poseStack, bufferSource, theme, time, skyDistance, hole, direction,
                        migratedDirection);
            } else {
                renderQliphothSmallBlackHole(poseStack, bufferSource, theme, time, skyDistance, hole, direction,
                        migratedDirection);
            }
        }
        if (pomeCount >= 9) {
            renderQliphothZenithBlackHole(poseStack, bufferSource, theme, time, skyDistance);
        }
    }

    static void renderQliphothSmallBlackHole(PoseStack poseStack,
                                                     MultiBufferSource.BufferSource bufferSource,
                                                     ChamberSkyTheme theme, float time, float skyDistance,
                                                     int hole, float[] direction, float[] migratedDirection) {
        float growth = (hole + 1) / 8.0F;
        float halfSize = qliphothSmallBlackHoleHalfSize(time, skyDistance, hole, direction, false);
        renderQliphothBlackHoleBillboard(poseStack, bufferSource, theme.skyTexture(), time, skyDistance,
                migratedDirection[0], migratedDirection[1], halfSize, 255, qliphothSmallBlackHoleSeed(hole),
                0.62F + growth * 0.28F, 0.94F + growth * 0.32F, false);
    }

    static void renderQliphothResidualRing(PoseStack poseStack,
                                                   MultiBufferSource.BufferSource bufferSource,
                                                   ChamberSkyTheme theme, float time, float skyDistance,
                                                   int hole, float[] direction, float[] migratedDirection) {
        float halfSize = qliphothSmallBlackHoleHalfSize(time, skyDistance, hole, direction, true);
        renderQliphothBlackHoleBillboard(poseStack, bufferSource, theme.skyTexture(), time, skyDistance,
                migratedDirection[0], migratedDirection[1], halfSize, 82, qliphothSmallBlackHoleSeed(hole),
                0.20F, 1.26F, false);
    }

    static void renderQliphothZenithBlackHole(PoseStack poseStack,
                                                      MultiBufferSource.BufferSource bufferSource,
                                                      ChamberSkyTheme theme, float time, float skyDistance) {
        renderQliphothZenithBlackHoleBillboard(poseStack, bufferSource, theme.skyTexture(), time, skyDistance,
                qliphothZenithBlackHoleHalfSize(time, skyDistance), 246, 0.913F, 1.48F, 1.84F);
    }

    static float qliphothSmallBlackHoleSeed(int hole) {
        return hole * 0.137F + 0.17F;
    }

    static float[] qliphothMigratedSmallBlackHoleDirection(float time, int hole, float[] direction) {
        float seed = qliphothSmallBlackHoleSeed(hole);
        float slowTime = time * 0.0014F;
        float yawDrift = Mth.sin(slowTime + seed * 17.0F) * 8.5F
                + Mth.sin(slowTime * 0.57F + hole * 2.13F) * 3.5F;
        float pitchDrift = Mth.cos(slowTime * 0.83F + seed * 23.0F) * 5.0F
                + Mth.sin(slowTime * 0.41F + hole * 1.71F) * 2.0F;
        return new float[]{
                direction[0] + yawDrift,
                Mth.clamp(direction[1] + pitchDrift, -54.0F, 58.0F),
                direction[2]
        };
    }

    static float qliphothSmallBlackHoleHalfSize(float time, float skyDistance, int hole, float[] direction,
                                                boolean residualRing) {
        float pulse = residualRing
                ? 0.90F + 0.10F * Mth.sin(time * 0.012F + hole * 1.27F)
                : 0.92F + 0.08F * Mth.sin(time * 0.018F + hole * 0.91F);
        return skyDistance * Mth.lerp(direction[2], residualRing ? 0.055F : 0.070F,
                residualRing ? 0.090F : 0.126F) * pulse;
    }

    static float qliphothZenithBlackHoleHalfSize(float time, float skyDistance) {
        float pulse = 0.96F + 0.04F * Mth.sin(time * 0.010F);
        return skyDistance * 0.435F * pulse;
    }

    static void renderQliphothBlackHoleBillboard(PoseStack poseStack,
                                                         MultiBufferSource.BufferSource bufferSource,
                                                         ResourceLocation texture, float time, float skyDistance,
                                                         float yaw, float pitch, float halfSize, int alpha,
                                                         float seed, float lensStrength, float ringIntensity,
                                                         boolean finalHole) {
        RenderType renderType = HemoRenderTypes.qliphothBlackHole(texture, time * 0.050F, seed, lensStrength,
                ringIntensity, finalHole);
        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F - pitch));
        poseStack.translate(0.0F, -skyDistance * 0.82F, 0.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(seed * 360.0F + time * (finalHole ? 0.010F : 0.018F)));
        renderQliphothBlackHoleQuad(poseStack, consumer, halfSize, alpha);
        poseStack.popPose();
        bufferSource.endBatch(renderType);
    }

    static void renderQliphothZenithBlackHoleBillboard(PoseStack poseStack,
                                                               MultiBufferSource.BufferSource bufferSource,
                                                               ResourceLocation texture, float time, float skyDistance,
                                                               float halfSize, int alpha, float seed,
                                                               float lensStrength, float ringIntensity) {
        RenderType renderType = HemoRenderTypes.qliphothBlackHole(texture, time * 0.050F, seed, lensStrength,
                ringIntensity, true);
        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        Vector3f zenith = qliphothSkyPoint(0.0F, -88.0F, skyDistance * 0.82F);
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 center = new Vec3(zenith.x, zenith.y, zenith.z);
        Vec3 right = new Vec3(camera.getLeftVector()).scale(-halfSize);
        Vec3 up = new Vec3(camera.getUpVector()).scale(halfSize);
        float spin = (seed * 360.0F + time * 0.010F) * Mth.DEG_TO_RAD;
        float cos = Mth.cos(spin);
        float sin = Mth.sin(spin);
        Vec3 spunRight = right.scale(cos).add(up.scale(sin));
        Vec3 spunUp = up.scale(cos).subtract(right.scale(sin));
        Matrix4f matrix = poseStack.last().pose();
        addQliphothBlackHoleBillboardVertex(matrix, consumer, center.subtract(spunRight).subtract(spunUp),
                0.0F, 0.0F, alpha);
        addQliphothBlackHoleBillboardVertex(matrix, consumer, center.subtract(spunRight).add(spunUp),
                0.0F, 1.0F, alpha);
        addQliphothBlackHoleBillboardVertex(matrix, consumer, center.add(spunRight).add(spunUp),
                1.0F, 1.0F, alpha);
        addQliphothBlackHoleBillboardVertex(matrix, consumer, center.add(spunRight).subtract(spunUp),
                1.0F, 0.0F, alpha);
        bufferSource.endBatch(renderType);
    }

    static void addQliphothBlackHoleBillboardVertex(Matrix4f matrix, VertexConsumer consumer, Vec3 position,
                                                            float u, float v, int alpha) {
        consumer.addVertex(matrix, (float) position.x, (float) position.y, (float) position.z)
                .setUv(u, v).setColor(255, 255, 255, alpha);
    }

    static void renderQliphothBlackHoleQuad(PoseStack poseStack, VertexConsumer consumer, float halfSize,
                                                    int alpha) {
        Matrix4f matrix = poseStack.last().pose();
        consumer.addVertex(matrix, -halfSize, 0.0F, -halfSize).setUv(0.0F, 0.0F).setColor(255, 255, 255, alpha);
        consumer.addVertex(matrix, -halfSize, 0.0F, halfSize).setUv(0.0F, 1.0F).setColor(255, 255, 255, alpha);
        consumer.addVertex(matrix, halfSize, 0.0F, halfSize).setUv(1.0F, 1.0F).setColor(255, 255, 255, alpha);
        consumer.addVertex(matrix, halfSize, 0.0F, -halfSize).setUv(1.0F, 0.0F).setColor(255, 255, 255, alpha);
    }

    static Vector3f qliphothSkyPoint(float yaw, float pitch, float distance) {
        float yawRadians = yaw * Mth.DEG_TO_RAD;
        float pitchRadians = pitch * Mth.DEG_TO_RAD;
        float horizontal = Mth.cos(pitchRadians);
        return new Vector3f(
                -Mth.sin(yawRadians) * horizontal * distance,
                -Mth.sin(pitchRadians) * distance,
                -Mth.cos(yawRadians) * horizontal * distance);
    }

}
