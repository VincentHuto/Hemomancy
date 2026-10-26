package com.vincenthuto.hemomancy.client.render.entity.boss.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.BloodPoolSurface;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.TendrilTubeBrush;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantLastBroodRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Her hazards on the Nursery floor, each in the existing toolkit and her palette: a nectar pool as the brazier blood
 * surface veined with amber ichor under a warm glow; a spore cloud as a faint floor haze (its drifting motes are
 * particles the entity emits); the lattice net as a web of hyphae strands settling from above and lying pulsing where
 * it fell; and a binding tendril as a living tube from her body to its anchor, a pulse running back toward her.
 */
public class MycophantHazardRenderer extends EntityRenderer<MycophantHazardEntity> {
	static final ResourceLocation HYPHAE = Hemomancy.rloc("textures/entity/boss/endgame/mycophant_hyphae.png");
	static final RenderType HYPHAE_CORE = RenderType.entityCutoutNoCull(HYPHAE);
	private static final float PIXELS = 16.0F;
	private static final int NET_SPOKES = 9;
	private static final int NET_RINGS = 3;
	private static final int RING_STATIONS = 28;
	private static final int TENDRIL_STATIONS = 16;

	public MycophantHazardRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.0F;
	}

	@Override
	public void render(MycophantHazardEntity hazard, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
			int light) {
		double gameTime = AnimationTime.gameTime(hazard.level(), (double) partialTick);
		float age = hazard.age(gameTime);
		poseStack.pushPose();
		switch (hazard.kind()) {
			case NECTAR_POOL -> pool(hazard, age, gameTime, poseStack, buffer);
			case SPORE_CLOUD -> cloud(hazard, age, poseStack, buffer);
			case LATTICE_NET -> net(hazard, age, poseStack, buffer, light);
			case BINDING_TENDRIL -> tendril(hazard, age, partialTick, poseStack, buffer, light);
		}
		poseStack.popPose();
		super.render(hazard, yaw, partialTick, poseStack, buffer, light);
	}

	/** Grows in over its first ticks and dries away over its last twenty. */
	private static float presence(float age, int life, float growTicks) {
		float grow = HemoEasing.smoothstep(Mth.clamp(age / growTicks, 0.0F, 1.0F));
		float fade = Mth.clamp((life - age) / 20.0F, 0.0F, 1.0F);
		return grow * fade;
	}

	// ------------------------------------------------------------------ the nectar pool

	private static void pool(MycophantHazardEntity hazard, float age, double gameTime, PoseStack poseStack, MultiBufferSource buffer) {
		float presence = presence(age, hazard.life(), 6.0F);
		if (presence <= 0.0F) return;
		float radius = hazard.radius() * (0.55F + 0.45F * presence) * (1.0F + 0.02F * Mth.sin(age * 0.08F));
		float lift = 0.012F + Math.floorMod(hazard.getId() * 5, 8) * 0.004F;
		poseStack.mulPose(Axis.YP.rotationDegrees(-hazard.getYRot() + age * 0.12F));
		PoseStack.Pose pose = poseStack.last();
		// The living blood surface the braziers and Annetta's pools share, its ichor running amber through it.
		float shaderTime = (float) (gameTime % 24000.0D);
		var blood = buffer.getBuffer(HemoRenderTypes.infectedPool(shaderTime, Math.floorMod(hazard.getId() * 37, 1000)));
		BloodPoolSurface.draw(pose.pose(), blood, 0.0F, lift, 0.0F, radius * 1.25F, 0, 90, 0, Math.round(238 * presence));
		// The nectar's own warmth over it.
		float beat = 0.5F + 0.5F * Mth.sin(age * 0.18F);
		floorHalo(poseStack, buffer, lift + 0.006F, radius * 2.2F, 1.0F, 0.66F, 0.16F, (0.22F + 0.1F * beat) * presence);
		floorHalo(poseStack, buffer, lift + 0.008F, radius * 1.1F, 1.0F, 0.81F, 0.31F, 0.16F * presence);
	}

	// ------------------------------------------------------------------ the spore cloud

	private static void cloud(MycophantHazardEntity hazard, float age, PoseStack poseStack, MultiBufferSource buffer) {
		float presence = presence(age, hazard.life(), 10.0F);
		if (presence <= 0.0F) return;
		float r = hazard.radius();
		float drift = 0.5F + 0.5F * Mth.sin(age * 0.07F);
		floorHalo(poseStack, buffer, 0.03F, r * 2.4F, 0.78F, 0.69F, 0.25F, (0.2F + 0.06F * drift) * presence);
		floorHalo(poseStack, buffer, 0.6F + 0.2F * drift, r * 1.8F, 0.78F, 0.69F, 0.25F, 0.08F * presence);
		floorHalo(poseStack, buffer, 1.3F + 0.25F * drift, r * 1.3F, 1.0F, 0.81F, 0.31F, 0.05F * presence);
	}

	// ------------------------------------------------------------------ the lattice net

	private static void net(MycophantHazardEntity hazard, float age, PoseStack poseStack, MultiBufferSource buffer, int light) {
		float delay = Math.max(1, hazard.landDelay());
		float fall = Mth.clamp(age / delay, 0.0F, 1.0F);
		boolean landed = age >= delay;
		float since = age - delay;
		float fade = Mth.clamp((hazard.life() - age) / 10.0F, 0.0F, 1.0F);
		float r = hazard.radius();
		// The circle on the floor tightens and brightens until it comes down, then flashes.
		float flash = landed ? Math.max(0.0F, 1.0F - since / 6.0F) : 0.0F;
		float tell = landed ? 0.25F + 0.5F * flash : 0.15F + 0.35F * fall * (0.7F + 0.3F * Mth.sin(age * (0.6F + fall)));
		floorHalo(poseStack, buffer, 0.03F, r * 2.2F, 1.0F, 0.66F, 0.16F, tell * fade);
		// It hangs above the circle and drops, slow then fast; after landing it sags flat and breathes.
		float height = landed ? 0.06F : 0.06F + 3.2F * (1.0F - HemoEasing.easeInCubic(fall));
		float spread = landed ? 1.0F : 0.55F + 0.45F * HemoEasing.smoothstep(fall);
		float breathe = landed ? 0.03F * Mth.sin(since * 0.3F) : 0.0F;
		float cut = HemoEasing.smoothstep(fade);
		PoseStack.Pose pose = poseStack.last();
		long seed = hazard.getUUID().getLeastSignificantBits();
		// Spokes from the knot at its centre to the rim, drooping between.
		for (int s = 0; s < NET_SPOKES; s++) {
			float azimuth = s * Mth.TWO_PI / NET_SPOKES + (float) ((seed >>> (s * 3)) & 7) * 0.04F;
			int stations = 10;
			float[] line = new float[(stations + 1) * 3];
			for (int i = 0; i <= stations; i++) {
				float t = i / (float) stations;
				float reach = r * spread * t;
				float sag = landed ? 0.0F : 0.5F * Mth.sin(t * Mth.PI) * (1.0F - fall);
				line[i * 3] = Mth.cos(azimuth) * reach * PIXELS;
				line[i * 3 + 1] = (height - sag + breathe * Mth.sin(t * 6.0F + s)) * PIXELS + 0.6F;
				line[i * 3 + 2] = Mth.sin(azimuth) * reach * PIXELS;
			}
			strand(pose, buffer, line, 0.9F * cut, 0.45F * cut, light, seed + s, (0.25F + 0.4F * flash) * fade);
		}
		// Rings of hyphae strung between the spokes.
		for (int ring = 1; ring <= NET_RINGS; ring++) {
			float reach = r * spread * ring / NET_RINGS;
			float[] line = new float[(RING_STATIONS + 1) * 3];
			for (int i = 0; i <= RING_STATIONS; i++) {
				float a = i * Mth.TWO_PI / RING_STATIONS;
				float wobble = 1.0F + 0.05F * Mth.sin(a * 5.0F + ring * 1.7F);
				float sag = landed ? 0.0F : 0.45F * (ring / (float) NET_RINGS) * (1.0F - fall);
				line[i * 3] = Mth.cos(a) * reach * wobble * PIXELS;
				line[i * 3 + 1] = (height - sag + breathe * Mth.sin(a * 3.0F + ring)) * PIXELS + 0.6F;
				line[i * 3 + 2] = Mth.sin(a) * reach * wobble * PIXELS;
			}
			strand(pose, buffer, line, 0.6F * cut, 0.6F * cut, light, seed + 31 + ring, (0.2F + 0.4F * flash) * fade);
		}
		// The knot at its middle, glowing yolk.
		poseStack.pushPose();
		poseStack.translate(0.0F, height + 0.05F, 0.0F);
		floorHalo(poseStack, buffer, 0.0F, 1.2F, 1.0F, 0.81F, 0.31F, (0.35F + 0.4F * flash) * fade);
		poseStack.popPose();
	}

	// ------------------------------------------------------------------ the binding tendrils

	private static void tendril(MycophantHazardEntity hazard, float age, float partialTick, PoseStack poseStack,
			MultiBufferSource buffer, int light) {
		MycophantEntity boss = hazard.owner();
		if (boss == null) return;
		Vec3 anchor = hazard.getPosition(partialTick);
		Vec3 root = MycophantHazardEntity.tendrilRoot(boss.getPosition(partialTick),
				Mth.rotLerp(partialTick, boss.yBodyRotO, boss.yBodyRot)).subtract(anchor);
		// It whips out from her over its first few ticks.
		float reach = HemoEasing.easeOutCubic(Mth.clamp(age / 6.0F, 0.0F, 1.0F));
		float wounds = 1.0F - hazard.hitsLeft() / (float) MycophantLastBroodRules.HITS_PER_TENDRIL;
		long seed = hazard.getUUID().getLeastSignificantBits();
		float[] line = new float[(TENDRIL_STATIONS + 1) * 3];
		double length = root.length();
		Vec3 side = new Vec3(-root.z, 0.0D, root.x);
		side = side.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D) : side.normalize();
		for (int i = 0; i <= TENDRIL_STATIONS; i++) {
			// Stations run from her body (t = 0) out to the anchor (t = 1).
			float t = i / (float) TENDRIL_STATIONS * reach;
			Vec3 at = root.scale(1.0D - t);
			double arch = Math.sin(t * Math.PI) * Math.min(1.2D, length * 0.12D);
			double writhe = Math.sin(age * 0.35F + t * 7.0F + (seed & 15)) * 0.18D * Math.sin(t * Math.PI);
			at = at.add(side.scale(writhe)).add(0.0D, arch, 0.0D);
			line[i * 3] = (float) at.x * PIXELS;
			line[i * 3 + 1] = (float) at.y * PIXELS;
			line[i * 3 + 2] = (float) at.z * PIXELS;
		}
		PoseStack.Pose pose = poseStack.last();
		float[] radii = TendrilTubeBrush.taper(TENDRIL_STATIONS + 1, 4.2F, 1.8F, 3.0F, seed);
		TendrilTubeBrush.core(pose, buffer.getBuffer(HYPHAE_CORE), line, radii, age * 0.04F, light, OverlayTexture.NO_OVERLAY,
				0.95F - 0.2F * wounds);
		// The pull: a bright pulse runs from the anchor back into her, again and again.
		float[] alpha = new float[TENDRIL_STATIONS + 1];
		float travel = (age * 0.06F) % 1.0F;
		for (int i = 0; i <= TENDRIL_STATIONS; i++) {
			float s = i / (float) TENDRIL_STATIONS;
			float pulse = Math.max(0.0F, 1.0F - Math.abs(s - (1.0F - travel)) * 5.0F);
			alpha[i] = 0.12F + 0.45F * pulse;
		}
		float red = 1.0F, green = Mth.lerp(wounds, 0.66F, 0.2F), blue = Mth.lerp(wounds, 0.16F, 0.12F);
		TendrilTubeBrush.glow(pose, buffer.getBuffer(HLRenderTypeInit.TENDRIL_GLOW), line, radii, 1.9F, red, green, blue, alpha);
		// Where it grips the floor.
		floorHalo(poseStack, buffer, 0.03F, 1.6F, 1.0F, 0.66F, 0.16F, 0.3F + 0.15F * Mth.sin(age * 0.3F));
	}

	// ------------------------------------------------------------------ shared pieces

	/** A hyphae strand: the tendril core thin and tapering, with an amber shell along it. */
	private static void strand(PoseStack.Pose pose, MultiBufferSource buffer, float[] line, float root, float tip, int light,
			long seed, float glowAlpha) {
		int stations = line.length / 3;
		if (root <= 0.02F) return;
		float[] radii = TendrilTubeBrush.taper(stations, root, tip, 2.0F, seed);
		TendrilTubeBrush.core(pose, buffer.getBuffer(HYPHAE_CORE), line, radii, (seed & 7) * 0.2F, light, OverlayTexture.NO_OVERLAY, 0.9F);
		float[] alpha = new float[stations];
		java.util.Arrays.fill(alpha, glowAlpha);
		TendrilTubeBrush.glow(pose, buffer.getBuffer(HLRenderTypeInit.TENDRIL_GLOW), line, radii, 2.4F, 1.0F, 0.66F, 0.16F, alpha);
	}

	/** A soft additive disc lying flat at {@code y} above the entity, {@code size} blocks across. */
	static void floorHalo(PoseStack poseStack, MultiBufferSource buffer, float y, float size, float red, float green, float blue,
			float alpha) {
		if (alpha <= 0.002F) return;
		poseStack.pushPose();
		poseStack.translate(0.0F, y, 0.0F);
		poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
		TendrilTubeBrush.halo(poseStack.last(), buffer.getBuffer(HLRenderTypeInit.TENDRIL_GLOW), 0.0F, 0.0F, 0.0F, size * PIXELS,
				size * PIXELS, red, green, blue, Math.min(1.0F, alpha));
		poseStack.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(MycophantHazardEntity entity) {
		return HYPHAE;
	}
}
