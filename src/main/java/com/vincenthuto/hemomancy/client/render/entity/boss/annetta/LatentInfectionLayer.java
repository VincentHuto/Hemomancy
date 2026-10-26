package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.annetta.LatentAnnettaInfectionModel;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.PriestessTendrilRules.Heading;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.PriestessTendrilRules.Wave;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaEncounterRules;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.LatentAnnettaInfectionEntity;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.LatentCombatRules;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.LatentCombatRules.Attack;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * Everything alive on the Latent Infection beyond its boxes: the infection pumping under its skin, vessel tendrils
 * bursting from its back, feelers lolling from the chest maw, a fine lash off the end of the whip arm, roots
 * dragging from its hem over the floor, and the red bloom of the core and the ember deep in the hood. Visual only.
 */
final class LatentInfectionLayer extends RenderLayer<LatentAnnettaInfectionEntity, LatentAnnettaInfectionModel> {
    private static final ResourceLocation TENDRIL = Hemomancy.rloc("textures/entity/boss/annetta/stained_priestess_tendril.png");
    private static final ResourceLocation SKIN = Hemomancy.rloc("textures/entity/boss/annetta/latent_annetta_infection.png");
    /** Height of the floor in the coil base's frame: the knot the serpent body rises from sits on it. */
    private static final float HEM_FLOOR = 2.0F;
    private static final int HEM_DRAGS = 6;

    LatentInfectionLayer(RenderLayerParent<LatentAnnettaInfectionEntity, LatentAnnettaInfectionModel> parent) {
        super(parent);
    }

    private record Motion(float alive, float spasm, float bloom, float lash, float dig, float grow, float thump) {
        static Motion of(LatentAnnettaInfectionEntity entity, float partialTick, float time) {
            float slack = entity.deathTime > 0 ? Math.min(1.0F, (entity.deathTime + partialTick) / 20.0F) : 0.0F;
            float spasm = entity.hurtTime > 0 ? (entity.hurtTime - partialTick) / Math.max(1, entity.hurtDuration) : 0.0F;
            Attack attack = entity.getAttack();
            float t = entity.actionTime(partialTick);
            float bloom = attack == Attack.INFECTION_BLOOM ? window(t, 4.0F, 14.0F, 26.0F) : 0.0F;
            float lash = attack == Attack.WHIP_LASH ? window(t, 9.0F, 14.0F, 24.0F) : 0.0F;
            float dig = attack == Attack.ROOT_ERUPTION ? window(t, 4.0F, 20.0F, 34.0F) : 0.0F;
            float grow = entity.isEmerging() ? LatentCombatRules.rise(entity.emergeTime(partialTick)) : 1.0F;
            return new Motion(1.0F - slack, Math.max(0.0F, spasm), bloom, lash, dig, grow, heartbeat(time));
        }
    }

    private static float window(float t, float in, float peak, float out) {
        return HemoEasing.smoothstep((t - in) / (peak - in)) * (1.0F - HemoEasing.smoothstep((t - peak) / (out - peak)));
    }

    /** Two thumps every two seconds, matching the skin shader's beat. */
    static float heartbeat(float time) {
        float beat = (time / 40.0F) % 1.0F;
        float a = (beat - 0.06F) / 0.03F;
        float b = (beat - 0.19F) / 0.035F;
        return (float) (Math.exp(-a * a) + 0.7D * Math.exp(-b * b));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, LatentAnnettaInfectionEntity entity,
            float limbSwing, float limbSwingAmount, float partialTick, float time, float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) return;
        long seed = entity.getUUID().getMostSignificantBits() ^ entity.getUUID().getLeastSignificantBits();
        Motion motion = Motion.of(entity, partialTick, time);
        int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);
        LatentAnnettaInfectionModel model = getParentModel();

        // The infection under its skin, pumping harder as it attacks.
        float surge = Mth.clamp(Math.max(motion.bloom(), Math.max(motion.lash() * 0.6F, motion.dig() * 0.6F)) + motion.spasm() * 0.5F, 0.0F, 1.0F);
        int skinColor = FastColor.ARGB32.color(Math.round(255 * motion.alive() * motion.grow()), Math.round(255 * surge), 0, 0);
        float shaderTime = (float) (AnimationTime.gameTime(entity.level(), partialTick) % 24000.0D);
        model.renderToBuffer(poseStack, buffer.getBuffer(HemoRenderTypes.latentInfectionSkin(SKIN, shaderTime)), light,
                OverlayTexture.NO_OVERLAY, skinColor);

        VertexConsumer core = buffer.getBuffer(RenderType.entityCutoutNoCull(TENDRIL));
        poseStack.pushPose();
        model.translateToTorso(poseStack);
        PoseStack.Pose torso = poseStack.last();
        float[][] crown = crown(seed, time, motion);
        for (int i = 0; i < crown.length; i++) {
            TendrilTube.core(torso, core, crown[i], crownRadii(i, crown[i], seed), i * 0.31F, light, overlay, 0.9F);
        }
        float[][] maw = maw(seed, time, motion);
        for (int i = 0; i < maw.length; i++) {
            TendrilTube.core(torso, core, maw[i], mawRadii(maw[i], seed + i), i * 0.17F, light, overlay, 0.8F);
        }
        poseStack.popPose();

        poseStack.pushPose();
        model.translateToWhipTip(poseStack);
        PoseStack.Pose whip = poseStack.last();
        float[] lash = lash(seed, time, motion);
        TendrilTube.core(whip, core, lash, lashRadii(lash, seed), 0.5F, light, overlay, 0.85F);
        poseStack.popPose();

        poseStack.pushPose();
        model.translateToCoilBase(poseStack);
        PoseStack.Pose base = poseStack.last();
        float[][] drags = hem(seed, time, motion);
        for (int i = 0; i < drags.length; i++) {
            TendrilTube.core(base, core, drags[i], hemRadii(drags[i], seed + i), i * 0.23F, light, overlay, 0.7F);
        }
        poseStack.popPose();

        renderGlow(poseStack, buffer, model, crown, maw, lash, seed, time, motion);
    }

    private void renderGlow(PoseStack poseStack, MultiBufferSource buffer, LatentAnnettaInfectionModel model,
            float[][] crown, float[][] maw, float[] lash, long seed, float time, Motion motion) {
        if (motion.alive() <= 0.0F) return;
        VertexConsumer glow = buffer.getBuffer(HLRenderTypeInit.TENDRIL_GLOW);
        float strength = motion.alive() * (1.0F + motion.bloom() * 1.6F + motion.thump() * 0.5F);
        poseStack.pushPose();
        model.translateToTorso(poseStack);
        PoseStack.Pose torso = poseStack.last();
        for (int i = 0; i < crown.length; i++) {
            TendrilTube.glow(torso, glow, crown[i], crownRadii(i, crown[i], seed), 1.8F, 0.95F, 0.45F, 0.1F,
                    pulse(crown[i], time + i * 9.0F, strength, 0.02F));
        }
        for (int i = 0; i < maw.length; i++) {
            TendrilTube.glow(torso, glow, maw[i], mawRadii(maw[i], seed + i), 1.9F, 1.0F, 0.2F, 0.12F,
                    pulse(maw[i], time + i * 4.0F, strength * 1.3F, 0.04F));
        }
        // The core: a red bloom that swells with each heartbeat and flares when it blooms the infection.
        float bloom = motion.alive() * motion.grow() * (0.16F + 0.24F * motion.thump() + 0.45F * motion.bloom());
        TendrilTube.halo(torso, glow, 0.0F, -5.5F, -2.35F, 7.5F, 8.5F, 1.0F, 0.16F, 0.12F, bloom);
        TendrilTube.halo(torso, glow, 0.0F, -5.5F, -2.4F, 3.2F, 3.8F, 1.0F, 0.55F, 0.3F, bloom * 0.8F);
        poseStack.popPose();

        poseStack.pushPose();
        model.translateToHead(poseStack);
        PoseStack.Pose head = poseStack.last();
        // An ember deep in the empty hood, never quite a face.
        float ember = motion.alive() * motion.grow() * (0.12F + 0.05F * Mth.sin(time * 0.11F) + 0.2F * motion.bloom());
        TendrilTube.halo(head, glow, 0.0F, -2.4F, -1.6F, 2.6F, 3.4F, 0.9F, 0.12F, 0.1F, ember);
        poseStack.popPose();

        poseStack.pushPose();
        model.translateToWhipTip(poseStack);
        float lashGlow = strength * (0.6F + 1.2F * motion.lash());
        TendrilTube.glow(poseStack.last(), glow, lash, lashRadii(lash, seed), 1.8F, 0.9F, 0.3F, 0.1F,
                pulse(lash, time * 1.5F, lashGlow, 0.02F));
        poseStack.popPose();
    }

    /** A slow pulse of infected blood travelling outward along a tendril. */
    private static float[] pulse(float[] line, float time, float strength, float base) {
        int n = line.length / 3;
        float[] alpha = new float[n];
        for (int i = 0; i < n; i++) {
            float s = i / (n - 1.0F);
            float wave = 0.5F + 0.5F * Mth.sin(time * 0.16F - s * 8.0F);
            alpha[i] = strength * (base + 0.08F * wave * wave * wave) * (1.0F - s * 0.5F);
        }
        return alpha;
    }

    private static Wave wave(long seed, int index, float amplitude, float speed, float droop) {
        long mixed = seed * 0x9E3779B97F4A7C15L + index * 0xBF58476D1CE4E5B9L;
        float phaseA = (mixed & 0xFFFF) / 65535.0F * Mth.TWO_PI;
        float phaseB = ((mixed >>> 16) & 0xFFFF) / 65535.0F * Mth.TWO_PI;
        float jitter = 0.85F + 0.3F * (((mixed >>> 32) & 0xFF) / 255.0F);
        return new Wave(amplitude, speed * jitter, phaseA, phaseB, droop);
    }

    // Torso frame: the upper back block spans y -13..-8.5 and z -2.4..2.4; +z is its back, -z its chest.
    private static final float[][] CROWN_ROOTS = {
            {-2.6F, -12.2F, 2.0F}, {2.6F, -12.2F, 2.0F}, {-1.3F, -10.4F, 2.3F}, {1.3F, -10.4F, 2.3F}, {0.0F, -12.6F, 1.7F},
            {-3.2F, -11.0F, 1.4F}, {3.2F, -11.0F, 1.4F}};
    private static final float[][] CROWN_HEADINGS = {{130.0F, 46.0F}, {50.0F, 46.0F}, {112.0F, 22.0F}, {68.0F, 22.0F},
            {90.0F, 62.0F}, {165.0F, 18.0F}, {15.0F, 18.0F}};
    private static final float[] CROWN_LENGTH = {20.0F, 18.0F, 15.0F, 16.0F, 24.0F, 11.0F, 12.0F};

    /** Vessel tendrils torn out of its back, arching up behind it and rearing high when it blooms. */
    private static float[][] crown(long seed, float time, Motion motion) {
        float alive = motion.alive();
        float[][] lines = new float[CROWN_ROOTS.length][];
        for (int i = 0; i < lines.length; i++) {
            float amplitude = (0.95F + 0.6F * motion.bloom() + 1.1F * motion.spasm()) * alive;
            Heading heading = new Heading(CROWN_HEADINGS[i][0], CROWN_HEADINGS[i][1] + 18.0F * motion.bloom() - 30.0F * (1.0F - alive));
            float length = CROWN_LENGTH[i] * motion.grow() * (1.0F + 0.2F * motion.bloom());
            lines[i] = PriestessTendrilRules.integrate(CROWN_ROOTS[i], heading, length, 14,
                    wave(seed, i, amplitude, 0.06F + 0.05F * motion.bloom(), 0.8F * (1.0F - motion.bloom()) + (1.0F - alive) * 3.0F), time);
        }
        return lines;
    }

    private static float[] crownRadii(int index, float[] line, long seed) {
        float root = index == 4 ? 1.05F : index < 2 ? 0.95F : index < 4 ? 0.75F : 0.6F;
        return TendrilTube.taper(line.length / 3, root, 0.07F, 2.5F, seed + index);
    }

    /** Thin feelers lolling out of the chest maw round the core; they fan out and stiffen when it blooms. */
    private static float[][] maw(long seed, float time, Motion motion) {
        float alive = motion.alive();
        float[][] roots = {{-1.3F, -7.4F, -2.2F}, {1.1F, -6.6F, -2.2F}, {-0.5F, -5.0F, -2.2F}, {0.7F, -4.0F, -2.1F}};
        float[] azimuths = {-120.0F, -60.0F, -100.0F, -75.0F};
        float[][] lines = new float[roots.length][];
        for (int i = 0; i < roots.length; i++) {
            float spread = 1.0F + 0.6F * motion.bloom();
            Heading heading = new Heading(-90.0F + (azimuths[i] + 90.0F) * spread, -38.0F + 45.0F * motion.bloom());
            float length = (7.0F + i) * motion.grow() * (1.0F + 0.6F * motion.bloom());
            lines[i] = PriestessTendrilRules.integrate(roots[i], heading, length, 10,
                    wave(seed, 10 + i, (1.0F + 0.6F * motion.bloom() + motion.spasm()) * alive, 0.14F, 0.5F + (1.0F - alive) * 2.0F), time);
        }
        return lines;
    }

    private static float[] mawRadii(float[] line, long seed) {
        return TendrilTube.taper(line.length / 3, 0.38F, 0.05F, 1.5F, seed);
    }

    /** The whip arm runs out into a fine lash that cracks with the blow. */
    private static float[] lash(long seed, float time, Motion motion) {
        float amplitude = (0.45F + 0.9F * motion.lash() + motion.spasm()) * motion.alive();
        return PriestessTendrilRules.integrate(new float[]{0.0F, 4.4F, 0.0F}, new Heading(0.0F, -84.0F),
                12.0F * motion.grow() * (1.0F + 0.25F * motion.lash()), 14,
                wave(seed, 20, amplitude, 0.12F + 0.3F * motion.lash(), 0.0F), time);
    }

    private static float[] lashRadii(float[] line, long seed) {
        return TendrilTube.taper(line.length / 3, 0.5F, 0.05F, 2.0F, seed + 20);
    }

    /**
     * Roots dragging out from round the coil base: they spill to the floor and spread over it, and dig in when it
     * drives its claws down for the eruption.
     */
    private static float[][] hem(long seed, float time, Motion motion) {
        float[][] lines = new float[HEM_DRAGS][];
        float sink = 1.6F * motion.dig();
        for (int i = 0; i < HEM_DRAGS; i++) {
            float azimuth = i * 360.0F / HEM_DRAGS + 25.0F;
            float a = azimuth * Mth.DEG_TO_RAD;
            float[] root = {Mth.cos(a) * 2.6F, 0.6F, Mth.sin(a) * 2.6F};
            float length = (11.0F + 3.0F * ((seed >>> (i * 4)) & 3)) * motion.grow() * (1.0F + 0.5F * motion.dig());
            float[] line = PriestessTendrilRules.integrate(root, new Heading(azimuth, -50.0F), length, 12,
                    wave(seed, 30 + i, 0.3F * motion.alive(), 0.04F, 1.4F), time);
            for (int k = 1; k < line.length / 3; k++) {
                // Lie along the floor rather than through it, unless it is digging in.
                float floor = HEM_FLOOR - 0.3F + sink * (k / (line.length / 3.0F));
                line[k * 3 + 1] = Math.min(line[k * 3 + 1], floor);
            }
            lines[i] = line;
        }
        return lines;
    }

    private static float[] hemRadii(float[] line, long seed) {
        return TendrilTube.taper(line.length / 3, 0.55F, 0.08F, 3.0F, seed + 30);
    }
}
