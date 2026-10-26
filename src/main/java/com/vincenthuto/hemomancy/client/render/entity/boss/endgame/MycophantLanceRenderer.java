package com.vincenthuto.hemomancy.client.render.entity.boss.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.PriestessTendrilRules;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.PriestessTendrilRules.Heading;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.PriestessTendrilRules.Wave;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.TendrilTubeBrush;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantHazardRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantLanceEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantStrikeRules;
import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * A Hyphae Lance, drawn the way the Priestess's eruptions are, in the Spawn Mother's tissue: through the crack the
 * floor glows amber and fibre tips worm at the surface, then a tight sheaf of pale hyphae spars spears straight up,
 * shivers at the top and sinks back. Thinner and straighter than the Priestess's thorns, so it reads as her wing spars'
 * fibre and not as blood.
 */
public class MycophantLanceRenderer extends EntityRenderer<MycophantLanceEntity> {
	private static final int SEGMENTS = 12;
	private static final int SPARS = 7;

	public MycophantLanceRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.0F;
	}

	@Override
	public void render(MycophantLanceEntity lance, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
			int light) {
		float age = AnimationTime.age(lance, partialTick) - lance.delay();
		if (age < 0.0F) return;
		float warn = MycophantStrikeRules.LANCE_WARN_TICKS;
		float life = MycophantHazardRules.LANCE_LIFE;
		float since = age - warn;
		long seed = lance.getUUID().getLeastSignificantBits();
		poseStack.pushPose();

		// The crack glows: a pulse that tightens and brightens through the warning, flashes on the eruption, then cools.
		float tell = Mth.clamp(age / warn, 0.0F, 1.0F);
		float flash = since < 0 ? 0.0F : Math.max(0.0F, 1.0F - since / 5.0F);
		float cool = since < 0 ? 1.0F : 1.0F - Mth.clamp(since / life, 0.0F, 1.0F);
		float size = 2.2F * (since < 0 ? 0.5F + 0.5F * tell : 1.0F + 0.3F * flash);
		float pulse = 0.5F + 0.5F * Mth.sin(age * (0.9F + tell));
		float glow = (since < 0 ? (0.18F + 0.3F * tell) * (0.7F + 0.3F * pulse) : 0.5F * flash + 0.2F) * cool;
		MycophantHazardRenderer.floorHalo(poseStack, buffer, 0.03F, size, 1.0F, 0.66F, 0.16F, glow);
		MycophantHazardRenderer.floorHalo(poseStack, buffer, 0.035F, size * 0.45F, 1.0F, 0.81F, 0.31F, glow * 0.8F);

		// Tendril space: model pixels with +Y down, as PriestessTendrilRules integrates.
		poseStack.scale(1.0F, -1.0F, 1.0F);
		PoseStack.Pose pose = poseStack.last();
		for (int i = 0; i < SPARS; i++) {
			long hash = seed * 0x9E3779B97F4A7C15L + i * 0xBF58476D1CE4E5B9L;
			float azimuth = i * 360.0F / SPARS + ((hash >>> 8) & 63) - 32.0F;
			float r = 3.5F * (0.3F + 0.7F * (((hash >>> 16) & 255) / 255.0F));
			float[] root = { Mth.cos(azimuth * Mth.DEG_TO_RAD) * r, 0.5F, Mth.sin(azimuth * Mth.DEG_TO_RAD) * r };
			// The middle spars stand tallest, so the sheaf comes to a point.
			float full = (i % 3 == 0 ? 44.0F : 32.0F) * (0.75F + 0.25F * (((hash >>> 24) & 255) / 255.0F));
			float length;
			float droop;
			if (since < 0) {
				length = 3.0F * tell * (0.6F + 0.4F * Mth.sin(age * 0.9F + i));
				droop = 0.3F;
			} else {
				float shoot = 1.0F - (float) Math.pow(1.0F - Math.min(1.0F, since / 2.5F), 3);
				float sink = Mth.clamp((since - life + 10.0F) / 10.0F, 0.0F, 1.0F);
				length = full * shoot * (1.0F - sink);
				droop = 0.05F + 1.4F * Mth.clamp((since - 10.0F) / 14.0F, 0.0F, 1.0F);
			}
			if (length < 0.5F) continue;
			float elevation = 80.0F + ((hash >>> 32) & 7);
			Wave wave = new Wave(0.25F + 0.4F * flash, 0.22F, (hash & 0xFF) / 40.0F, ((hash >>> 40) & 0xFF) / 40.0F, droop);
			float[] line = PriestessTendrilRules.integrate(root, new Heading(azimuth, elevation), length, SEGMENTS, wave, age);
			float thick = 1.1F * (0.8F + 0.2F * Math.min(1.0F, length / 10.0F));
			float[] radii = TendrilTubeBrush.taper(SEGMENTS + 1, thick, 0.03F, 2.0F, hash);
			// Fetch each buffer as it is used: asking for another render type closes the last one.
			TendrilTubeBrush.core(pose, buffer.getBuffer(MycophantHazardRenderer.HYPHAE_CORE), line, radii, i * 0.31F, light,
					OverlayTexture.NO_OVERLAY, 0.95F);
			float[] alpha = new float[SEGMENTS + 1];
			for (int k = 0; k <= SEGMENTS; k++) {
				float s = k / (float) SEGMENTS;
				alpha[k] = (0.08F + 0.35F * flash) * (1.0F - 0.5F * s) * cool;
			}
			TendrilTubeBrush.glow(pose, buffer.getBuffer(HLRenderTypeInit.TENDRIL_GLOW), line, radii, 1.9F, 1.0F, 0.72F, 0.22F, alpha);
		}
		poseStack.popPose();
		super.render(lance, yaw, partialTick, poseStack, buffer, light);
	}

	@Override
	public ResourceLocation getTextureLocation(MycophantLanceEntity entity) {
		return MycophantHazardRenderer.HYPHAE;
	}
}
