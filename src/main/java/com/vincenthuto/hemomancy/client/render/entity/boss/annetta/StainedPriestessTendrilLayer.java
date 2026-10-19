package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.annetta.StainedPriestessModel;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.PriestessTendrilRules.Heading;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.PriestessTendrilRules.Pose;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.PriestessTendrilRules.Wave;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaEncounterRules;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.PriestessCombatRules;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.StainedPriestessEntity;
import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Everything alive on the Stained Priestess: wings of writhing finger tendrils with torn webbing between them,
 * a boiling knot of thorned tendrils where her head was with three yellow eyes, and tendrils wrapping her arms,
 * spilling down her chest, bursting from her back and dragging from her hem. Visual only.
 */
final class StainedPriestessTendrilLayer extends RenderLayer<StainedPriestessEntity, StainedPriestessModel> {
    private static final ResourceLocation TENDRIL = Hemomancy.rloc("textures/entity/boss/annetta/stained_priestess_tendril.png");
    private static final ResourceLocation WEBBING = Hemomancy.rloc("textures/entity/boss/annetta/stained_priestess_webbing.png");
    private static final ResourceLocation EYE = Hemomancy.rloc("textures/entity/boss/annetta/stained_priestess_eye.png");
    private static final int SEG = PriestessTendrilRules.SEGMENTS;
    private static final int HEAD_TENDRILS = 24;
    private static final float HEAD_RADIUS = 3.4F;
    private static final float[] HEAD_CENTRE = {0.0F, -3.3F, 0.2F};

    /** Per-entity pose blend, so wings unfold and fold instead of snapping. */
    private final Map<StainedPriestessEntity, float[]> blends = new WeakHashMap<>();

    StainedPriestessTendrilLayer(RenderLayerParent<StainedPriestessEntity, StainedPriestessModel> parent) {
        super(parent);
    }

    private record Motion(float alive, float spasm, float lash, float flare, float surge, float walk, float flight,
            float wingGrowth, float headGrowth) {
        static Motion of(StainedPriestessEntity entity, float partialTick, float limbSwingAmount) {
            float slack = entity.deathTime > 0 ? Math.min(1.0F, (entity.deathTime + partialTick) / 20.0F) : 0.0F;
            float spasm = entity.hurtTime > 0 ? (entity.hurtTime - partialTick) / Math.max(1, entity.hurtDuration) : 0.0F;
            PriestessCombatRules.Mode mode = entity.getMode();
            int modeTick = entity.getModeTick();
            boolean diving = mode == PriestessCombatRules.Mode.SWOOP && modeTick >= PriestessCombatRules.SWOOP_TELEGRAPH
                    && modeTick < PriestessCombatRules.SWOOP_DIVE_END;
            // Only the spinning reap lashes the wings; held forward through the plunge they would bury her pose.
            int comboTick = entity.getActionTick();
            boolean combo = entity.getAction() == StainedPriestessEntity.ACTION_COMBO
                    && comboTick >= PriestessCombatRules.REAP_START - 1 && comboTick <= PriestessCombatRules.REAP_END;
            boolean blast = mode == PriestessCombatRules.Mode.WIND_BLAST && modeTick >= PriestessCombatRules.WIND_BLAST_RELEASE - 2
                    && modeTick < PriestessCombatRules.WIND_BLAST_RELEASE + 8;
            return new Motion(1.0F - slack, Math.max(0.0F, spasm), diving || combo ? 1.0F : 0.0F,
                    entity.getAction() == StainedPriestessEntity.ACTION_LANCE ? 1.0F : 0.0F, blast ? 1.0F : 0.0F,
                    Math.min(1.0F, limbSwingAmount), entity.isFlying() ? 1.0F : 0.0F,
                    entity.wingGrowth(partialTick), entity.headGrowth(partialTick));
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, StainedPriestessEntity entity,
            float limbSwing, float limbSwingAmount, float partialTick, float time, float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) return;
        long seed = entity.getUUID().getMostSignificantBits() ^ entity.getUUID().getLeastSignificantBits();
        Motion motion = Motion.of(entity, partialTick, limbSwingAmount);
        float[] weights = blend(entity, motion, time);
        int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);
        StainedPriestessModel model = getParentModel();
        VertexConsumer core = buffer.getBuffer(RenderType.entityCutoutNoCull(TENDRIL));

        poseStack.pushPose();
        model.translateToBody(poseStack);
        PoseStack.Pose body = poseStack.last();
        boolean winged = motion.wingGrowth() > 0.02F;
        float[][][] wings = winged
                ? new float[][][]{wing(1, seed, weights, time, motion), wing(-1, seed ^ 0x5DEECE66DL, weights, time, motion)}
                : new float[0][][];
        for (float[][] wing : wings) {
            for (int i = 0; i < wing.length; i++) {
                TendrilTube.core(body, core, wing[i], radii(i, wing[i], seed, motion.wingGrowth()), i * 0.37F, light, overlay, 1.0F);
            }
        }
        float[][] bodyTendrils = bodyTendrils(seed, time, motion);
        for (int i = 0; i < bodyTendrils.length; i++) {
            float[] line = bodyTendrils[i];
            TendrilTube.core(body, core, line, TendrilTube.taper(line.length / 3, 0.75F, 0.12F, 2.0F, seed + i), i * 0.21F, light, overlay, 0.9F);
        }
        poseStack.popPose();

        // The hem drags from the hips, so it swings with the legs rather than the chest.
        poseStack.pushPose();
        model.translateToHips(poseStack);
        PoseStack.Pose hips = poseStack.last();
        float[][] hem = hemTendrils(seed, time, motion);
        for (int i = 0; i < hem.length; i++) {
            TendrilTube.core(hips, core, hem[i], TendrilTube.taper(hem[i].length / 3, 0.75F, 0.12F, 2.0F, seed + 4 + i), i * 0.21F, light, overlay, 0.9F);
        }
        poseStack.popPose();

        // Each arm's wrap coils round the upper arm and the forearm separately, so it bends at the elbow.
        for (HumanoidArm arm : HumanoidArm.values()) {
            poseStack.pushPose();
            model.translateToArm(arm, poseStack);
            float[] upper = armWrap(arm, seed, time, motion, -1.0F, 5.4F, 1.3F);
            TendrilTube.core(poseStack.last(), core, upper, TendrilTube.taper(upper.length / 3, 0.5F, 0.3F, 3.0F, seed), 0.0F, light, overlay, 0.85F);
            poseStack.popPose();
            poseStack.pushPose();
            model.translateToForearm(arm, poseStack);
            float[] lower = armWrap(arm, seed ^ 0x2545F491L, time, motion, -0.6F, 5.0F, 1.1F);
            TendrilTube.core(poseStack.last(), core, lower, TendrilTube.taper(lower.length / 3, 0.4F, 0.15F, 3.0F, seed), 0.3F, light, overlay, 0.85F);
            poseStack.popPose();
        }

        poseStack.pushPose();
        model.translateToHead(poseStack);
        PoseStack.Pose head = poseStack.last();
        float[][] knot = headKnot(seed, time, motion);
        for (int i = 0; i < knot.length; i++) {
            TendrilTube.core(head, core, knot[i], TendrilTube.taper(knot[i].length / 3, 0.85F, 0.06F, 1.0F, seed + i), i * 0.13F, light, overlay, 0.95F);
        }
        VertexConsumer eyes = buffer.getBuffer(RenderType.eyes(EYE));
        float blink = motion.alive() * AnnettaEncounterRules.smooth(motion.headGrowth() * 1.4F - 0.4F);
        TendrilTube.card(head, eyes, 0.0F, -3.4F, -2.75F, 2.8F * blink, 1.4F, 0.0F, 1.0F);
        TendrilTube.card(head, eyes, -1.95F, -4.6F, -2.72F, 1.7F * blink, 0.85F, -22.0F, 1.0F);
        TendrilTube.card(head, eyes, 1.95F, -4.6F, -2.72F, 1.7F * blink, 0.85F, 22.0F, 1.0F);
        poseStack.popPose();

        // Webbing after the opaque cores so it sorts over them.
        poseStack.pushPose();
        model.translateToBody(poseStack);
        body = poseStack.last();
        VertexConsumer web = buffer.getBuffer(RenderType.entityTranslucent(WEBBING));
        int webTo = Math.round(SEG * 0.72F);
        for (float[][] wing : wings) {
            for (int finger = 1; finger < PriestessTendrilRules.FINGERS; finger++) {
                TendrilTube.webbing(body, web, wing[finger], wing[finger + 1], 1, webTo, 1.6F + motion.walk(), light, overlay, motion.alive());
            }
        }
        poseStack.popPose();

        renderGlow(poseStack, buffer, model, wings, bodyTendrils, knot, seed, time, motion);
    }

    private void renderGlow(PoseStack poseStack, MultiBufferSource buffer, StainedPriestessModel model, float[][][] wings,
            float[][] bodyTendrils, float[][] knot, long seed, float time, Motion motion) {
        if (motion.alive() <= 0.0F) return;
        VertexConsumer glow = buffer.getBuffer(HLRenderTypeInit.TENDRIL_GLOW);
        float strength = motion.alive() * (1.0F + motion.surge() * 1.5F);
        poseStack.pushPose();
        model.translateToBody(poseStack);
        PoseStack.Pose body = poseStack.last();
        for (float[][] wing : wings) {
            for (int i = 0; i < wing.length; i++) {
                float[] line = wing[i];
                TendrilTube.glow(body, glow, line, radii(i, line, seed, motion.wingGrowth()), 1.7F, 0.85F, 0.05F, 0.07F, pulse(line, time + i * 7.0F, strength, 0.03F));
            }
        }
        for (int i = 0; i < bodyTendrils.length; i++) {
            float[] line = bodyTendrils[i];
            TendrilTube.glow(body, glow, line, TendrilTube.taper(line.length / 3, 0.75F, 0.12F, 2.0F, seed + i), 1.7F, 0.85F, 0.08F, 0.05F, pulse(line, time + i * 5.0F, strength, 0.03F));
        }
        poseStack.popPose();
        poseStack.pushPose();
        model.translateToHead(poseStack);
        PoseStack.Pose head = poseStack.last();
        for (int i = 0; i < knot.length; i++) {
            float[] line = knot[i];
            TendrilTube.glow(head, glow, line, TendrilTube.taper(line.length / 3, 0.85F, 0.06F, 1.0F, seed + i), 1.6F, 0.9F, 0.12F, 0.05F, pulse(line, time + i * 3.0F, strength, 0.02F));
        }
        // A faint yellow bloom round the eyes.
        float bloom = 0.16F + 0.05F * Mth.sin(time * 0.2F);
        TendrilTube.halo(head, glow, 0.0F, -3.4F, -2.9F, 4.6F, 2.6F, 1.0F, 0.75F, 0.2F, bloom * motion.alive());
        poseStack.popPose();
    }

    /** A slow pulse of infected blood travelling outward along a tendril. */
    private static float[] pulse(float[] line, float time, float strength, float base) {
        int n = line.length / 3;
        float[] alpha = new float[n];
        for (int i = 0; i < n; i++) {
            float s = i / (n - 1.0F);
            float wave = 0.5F + 0.5F * Mth.sin(time * 0.14F - s * 7.0F);
            alpha[i] = strength * (base + 0.09F * wave * wave * wave) * (1.0F - s * 0.6F);
        }
        return alpha;
    }

    private float[] blend(StainedPriestessEntity entity, Motion motion, float time) {
        Pose target;
        StainedPriestessModel.WingPose review = StainedPriestessModel.reviewWingPose;
        if (review != null) {
            float[] snapped = PriestessTendrilRules.weightsFor(Pose.valueOf(review.name()));
            blends.put(entity, new float[]{snapped[0], snapped[1], snapped[2], time});
            return snapped;
        }
        target = switch (entity.getWing()) {
            case FOLDED -> Pose.FOLDED;
            case SPREAD -> Pose.SPREAD;
            case SHIELD -> Pose.SHIELD;
        };
        float[] state = blends.computeIfAbsent(entity, e -> {
            float[] start = PriestessTendrilRules.weightsFor(target);
            return new float[]{start[0], start[1], start[2], time};
        });
        float dt = Mth.clamp(time - state[3], 0.0F, 5.0F);
        float[] next = PriestessTendrilRules.stepWeights(new float[]{state[0], state[1], state[2]}, target, dt);
        state[0] = next[0];
        state[1] = next[1];
        state[2] = next[2];
        state[3] = time;
        return next;
    }

    /** Index 0 is the arm, 1..6 the fingers, the rest the drips; X mirrored for her right wing. */
    private static float[][] wing(int side, long seed, float[] weights, float time, Motion motion) {
        float alive = motion.alive();
        float beat = weights[Pose.SPREAD.ordinal()] * alive * (1.0F + motion.surge() + 0.4F * motion.flight());
        // Airborne, the wings work hard; on the ground they barely stir.
        float beatTime = time * (1.0F + 0.9F * motion.flight());
        float slack = 1.0F - alive;
        float grow = motion.wingGrowth();
        Heading armHeading = PriestessTendrilRules.armHeading(weights, beatTime, beat);
        if (motion.flare() > 0) armHeading = new Heading(armHeading.azimuth() + 18.0F, armHeading.elevation() + 14.0F);
        float[] arm = PriestessTendrilRules.integrate(PriestessTendrilRules.WING_ROOT, armHeading,
                PriestessTendrilRules.ARM_LENGTH * grow, SEG, wave(seed, 0, 0.25F * alive, 0.07F, 0.4F * slack * 6), time);
        float[][] lines = new float[1 + PriestessTendrilRules.FINGERS + 3][];
        lines[0] = arm;
        for (int finger = 0; finger < PriestessTendrilRules.FINGERS; finger++) {
            float[] root = PriestessTendrilRules.pointAt(arm, PriestessTendrilRules.fingerRoot(finger));
            Heading heading = PriestessTendrilRules.fingerHeading(finger, weights, beatTime, beat);
            if (motion.lash() > 0) heading = new Heading(heading.azimuth() - 40.0F * motion.lash(), heading.elevation());
            float amplitude = (0.75F + 0.6F * motion.lash() + 1.2F * motion.spasm()) * alive;
            lines[1 + finger] = PriestessTendrilRules.integrate(root, heading,
                    PriestessTendrilRules.fingerLength(finger, weights) * grow, SEG,
                    wave(seed, finger + 1, amplitude, 0.11F + 0.04F * motion.spasm(), 0.3F + slack * 3.0F), time);
        }
        for (int drip = 0; drip < 3; drip++) {
            float[] root = PriestessTendrilRules.pointAt(arm, 0.35F + drip * 0.25F);
            lines[1 + PriestessTendrilRules.FINGERS + drip] = PriestessTendrilRules.integrate(root,
                    new Heading(armHeading.azimuth() + 20.0F, -86.0F), (12.0F + drip * 4.0F) * grow, 12,
                    wave(seed, 20 + drip, 0.45F * alive, 0.06F, 0.2F), time);
        }
        if (side < 0) {
            for (float[] line : lines) for (int i = 0; i < line.length; i += 3) line[i] = -line[i];
        }
        return lines;
    }

    private static float[] radii(int index, float[] line, long seed, float grow) {
        int stations = line.length / 3;
        float g = 0.35F + 0.65F * grow;
        if (index == 0) return TendrilTube.taper(stations, 1.7F * g, 0.8F * g, 3.0F, seed);
        if (index <= PriestessTendrilRules.FINGERS) return TendrilTube.taper(stations, 0.95F * g, 0.12F * g, 2.5F, seed + index);
        return TendrilTube.taper(stations, 0.45F * g, 0.08F * g, 1.5F, seed + index);
    }

    private static Wave wave(long seed, int index, float amplitude, float speed, float droop) {
        long mixed = seed * 0x9E3779B97F4A7C15L + index * 0xBF58476D1CE4E5B9L;
        float phaseA = (mixed & 0xFFFF) / 65535.0F * Mth.TWO_PI;
        float phaseB = ((mixed >>> 16) & 0xFFFF) / 65535.0F * Mth.TWO_PI;
        float jitter = 0.85F + 0.3F * (((mixed >>> 32) & 0xFF) / 255.0F);
        return new Wave(amplitude, speed * jitter, phaseA, phaseB, droop);
    }

    /** Back burst between the wings, chest spill from the stump and hem drag to the floor. */
    private static float[][] bodyTendrils(long seed, float time, Motion motion) {
        float alive = motion.alive();
        float amp = (0.9F + motion.spasm()) * alive;
        float slack = (1.0F - alive) * 3.0F;
        return new float[][]{
                PriestessTendrilRules.integrate(new float[]{-0.9F, 3.2F, 2.2F}, new Heading(110.0F, 55.0F), 11.0F, 12, wave(seed, 40, amp, 0.12F, slack), time),
                PriestessTendrilRules.integrate(new float[]{0.9F, 4.4F, 2.2F}, new Heading(70.0F, 40.0F), 9.0F, 12, wave(seed, 41, amp, 0.13F, slack), time),
                PriestessTendrilRules.integrate(new float[]{-1.2F, -0.4F, -1.6F}, new Heading(-95.0F, -70.0F), 13.0F, 12, wave(seed, 42, amp * 0.6F, 0.08F, 0.5F + slack), time),
                PriestessTendrilRules.integrate(new float[]{0.6F, -0.4F, -1.8F}, new Heading(-80.0F, -78.0F), 15.0F, 12, wave(seed, 43, amp * 0.6F, 0.07F, 0.4F + slack), time),
        };
    }

    /** Tendrils dragging from the hem toward the floor, rooted in hips space. */
    private static float[][] hemTendrils(long seed, float time, Motion motion) {
        float alive = motion.alive();
        float amp = (0.9F + motion.spasm()) * alive;
        float slack = (1.0F - alive) * 3.0F;
        return new float[][]{
                PriestessTendrilRules.integrate(new float[]{-2.6F, -1.0F, -1.6F}, new Heading(-110.0F, -84.0F), 13.0F, 12, wave(seed, 44, amp * 0.7F, 0.06F, 1.6F + slack), time),
                PriestessTendrilRules.integrate(new float[]{0.4F, -1.0F, 2.0F}, new Heading(95.0F, -82.0F), 14.0F, 12, wave(seed, 45, amp * 0.7F, 0.06F, 1.8F + slack), time),
                PriestessTendrilRules.integrate(new float[]{2.8F, -1.0F, -0.4F}, new Heading(10.0F, -84.0F), 12.0F, 12, wave(seed, 46, amp * 0.7F, 0.07F, 1.5F + slack), time),
        };
    }

    /** A tendril spiralling down one arm segment from {@code top} for {@code length}, its coils sliding slowly. */
    private static float[] armWrap(HumanoidArm arm, long seed, float time, Motion motion, float top, float length, float turns) {
        int stations = 18;
        float[] line = new float[stations * 3];
        float spin = time * 0.03F * motion.alive() + (arm == HumanoidArm.LEFT ? 1.3F : 0.0F) + (seed & 7);
        for (int i = 0; i < stations; i++) {
            float s = i / (stations - 1.0F);
            float angle = s * Mth.TWO_PI * turns + spin;
            float radius = 1.85F + 0.3F * Mth.sin(s * 9.0F + time * 0.1F + seed % 5);
            line[i * 3] = Mth.cos(angle) * radius;
            line[i * 3 + 1] = top + s * length;
            line[i * 3 + 2] = Mth.sin(angle) * radius;
        }
        return line;
    }

    /**
     * Tendrils boil up out of the stump and over a sphere the size of her lost head, each ending in a thorn pushed
     * outward; a few crown tendrils whip free above. The front stays open around the eyes.
     */
    private static float[][] headKnot(long seed, float time, Motion motion) {
        float alive = motion.alive();
        float[][] lines = new float[HEAD_TENDRILS + 5][];
        int stations = 16;
        for (int t = 0; t < HEAD_TENDRILS; t++) {
            // Spread azimuths round the back and sides; leave a window either side of -Z for the eyes.
            float phi = -Mth.HALF_PI + 0.75F + (Mth.TWO_PI - 1.5F) * (t + 0.5F) / HEAD_TENDRILS;
            long hash = seed * 0x9E3779B97F4A7C15L + t * 0xBF58476D1CE4E5B9L;
            float end = 0.18F * Mth.PI + 0.55F * Mth.PI * (((hash >>> 20) & 7) / 7.0F);
            float[] line = new float[(stations + 3) * 3];
            float boil = Mth.sin(time * 0.09F + t * 1.7F) * alive;
            for (int i = 0; i < stations; i++) {
                float s = i / (stations - 1.0F);
                float theta = Mth.PI + (end - Mth.PI) * s;
                float azimuth = phi + 0.55F * Mth.sin(s * Mth.PI + time * 0.05F * alive + t) * alive;
                float radius = HEAD_RADIUS * (1.0F + 0.2F * Mth.sin(s * Mth.PI)) + 0.45F * boil * Mth.sin(s * 6.0F + t);
                radius *= motion.headGrowth();
                radius *= 1.0F - 0.25F * (1.0F - alive);
                line[i * 3] = HEAD_CENTRE[0] + Mth.sin(theta) * Mth.cos(azimuth) * radius;
                line[i * 3 + 1] = HEAD_CENTRE[1] - Mth.cos(theta) * radius;
                line[i * 3 + 2] = HEAD_CENTRE[2] + Mth.sin(theta) * Mth.sin(azimuth) * radius;
            }
            // The thorn: three stations driven straight out from the centre.
            int last = (stations - 1) * 3;
            float dx = line[last] - HEAD_CENTRE[0], dy = line[last + 1] - HEAD_CENTRE[1], dz = line[last + 2] - HEAD_CENTRE[2];
            float dl = Mth.sqrt(dx * dx + dy * dy + dz * dz);
            float thorn = (1.8F + 1.6F * (((hash >>> 40) & 3) / 3.0F)) * motion.headGrowth();
            for (int k = 1; k <= 3; k++) {
                line[last + k * 3] = line[last] + dx / dl * thorn * k / 3.0F;
                line[last + k * 3 + 1] = line[last + 1] + dy / dl * thorn * k / 3.0F;
                line[last + k * 3 + 2] = line[last + 2] + dz / dl * thorn * k / 3.0F;
            }
            lines[t] = line;
        }
        for (int c = 0; c < 5; c++) {
            float azimuth = 60.0F + c * 72.0F;
            float[] root = {HEAD_CENTRE[0] + Mth.cos(azimuth * Mth.DEG_TO_RAD) * 1.6F, HEAD_CENTRE[1] - HEAD_RADIUS * 0.8F,
                    HEAD_CENTRE[2] + Mth.sin(azimuth * Mth.DEG_TO_RAD) * 1.6F};
            lines[HEAD_TENDRILS + c] = PriestessTendrilRules.integrate(root, new Heading(azimuth, 62.0F), (7.0F + c) * motion.headGrowth(), 12,
                    wave(seed, 60 + c, (1.1F + motion.spasm()) * alive, 0.15F, (1.0F - alive) * 3.0F), time);
        }
        return lines;
    }
}
