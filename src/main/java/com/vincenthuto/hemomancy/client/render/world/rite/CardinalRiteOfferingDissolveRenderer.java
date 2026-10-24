package com.vincenthuto.hemomancy.client.render.world.rite;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.data.CardinalRiteEventClientState;
import com.vincenthuto.hemomancy.client.data.CardinalRiteEventClientState.RiteEvent;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import net.minecraft.world.phys.AABB;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * A Working craft's daemon breaking an offering in its brazier. For each
 * {@code OFFERING_DISSOLVED} beat the offering is pulled up out of the bowl
 * toward the daemon, swelling and shuddering, then breaks with the vanilla
 * item-break crumbs and sound, and its blood streams up into the daemon.
 */
public final class CardinalRiteOfferingDissolveRenderer {
	private static final double CULL_DISTANCE_SQR = 96.0D * 96.0D;
	private static final ParticleColor BLOOD = new ParticleColor(150, 6, 16);
	/** Beats whose offering has already broken. */
	private static final Set<RiteEvent> BROKEN = Collections.newSetFromMap(new IdentityHashMap<>());

	private CardinalRiteOfferingDissolveRenderer() {
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			BROKEN.clear();
			return;
		}
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		double time = AnimationTime.gameTime(mc.level, partialTick);
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		boolean any = false;
		for (RiteEvent event : CardinalRiteEventClientState.all()) {
			if (event.kind() != CardinalRiteEventKind.OFFERING_DISSOLVED || event.points().size() < 2
					|| event.item().isEmpty()) continue;
			float age = event.age(partialTick);
			if (age >= OfferingDissolveRules.BREAK_TICKS) {
				// Break once, and only as it happens: a beat seen late (or re-seen) must not shatter again.
				if (BROKEN.add(event) && OfferingDissolveRules.shattersNow(age)) shatter(mc, event);
				continue;
			}
			if (event.points().getFirst().distanceToSqr(camera) > CULL_DISTANCE_SQR) continue;
			Vec3 bowl = event.points().get(0);
			Vec3 at = bowl.lerp(event.points().get(1), OfferingDissolveRules.lift(age));
			float strain = OfferingDissolveRules.strain(age);
			double shakeX = Math.sin(time * 3.7D + event.index()) * strain;
			double shakeZ = Math.cos(time * 4.3D + event.index() * 1.7D) * strain;
			float scale = OfferingDissolveRules.scale(age);
			poseStack.pushPose();
			poseStack.translate(at.x + shakeX - camera.x, at.y - camera.y, at.z + shakeZ - camera.z);
			poseStack.mulPose(Axis.YP.rotationDegrees((float) (time * 22.0D + event.index() * 61.0D)));
			poseStack.mulPose(Axis.ZP.rotationDegrees((float) (Math.sin(time * 2.9D) * 40.0D * strain / 0.08D)));
			poseStack.scale(scale, scale, scale);
			mc.getItemRenderer().renderStatic(event.item(), ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT,
					OverlayTexture.NO_OVERLAY, poseStack, buffers, mc.level, event.index());
			poseStack.popPose();
			any = true;
		}
		any |= renderSwallowed(mc, poseStack, buffers, camera, time, partialTick);
		if (any) buffers.endBatch();
		// Forget a beat only once it has left the event list, so it can never be found unbroken again.
		Set<RiteEvent> live = Collections.newSetFromMap(new IdentityHashMap<>());
		live.addAll(CardinalRiteEventClientState.all());
		BROKEN.retainAll(live);
	}

	/** How many break crumbs of an offering snake around the daemon. */
	private static final int CRUMBS = 24;

	/**
	 * The break crumbs of broken offerings: drawn like vanilla's, a random quarter of the item's own particle
	 * sprite, they stream into the daemon and snake around its body as it moves.
	 */
	private static boolean renderSwallowed(Minecraft mc, PoseStack poseStack, MultiBufferSource buffers, Vec3 camera,
			double time, float partialTick) {
		boolean any = false;
		org.joml.Quaternionf facing = mc.gameRenderer.getMainCamera().rotation();
		var consumer = buffers.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(
				net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS));
		for (RiteEvent event : CardinalRiteEventClientState.all()) {
			if (event.kind() != CardinalRiteEventKind.OFFERING_DISSOLVED || event.points().size() < 2
					|| event.item().isEmpty()) continue;
			float since = event.age(partialTick) - OfferingDissolveRules.BREAK_TICKS;
			if (since < 0.0F) continue;
			HumanitySpriteEntity daemon = daemonFor(mc, event);
			if (daemon == null) continue;
			// The body runs from the daemon's position along its crown; the coil is wound in its frame, so it tips
			// over and turns with the daemon as it flies.
			double[] crown = daemon.flight().crown(partialTick);
			Vec3 axis = new Vec3(crown[0], crown[1], crown[2]);
			Vec3[] frame = SwallowedFragmentRules.frame(axis, Math.toRadians(daemon.renderBodyYaw(partialTick)));
			double height = CardinalRiteVisualRules.DAEMON_HEIGHT * daemon.renderScale(partialTick);
			Vec3 heart = daemon.getPosition(partialTick).add(axis.scale(height * 0.25D));
			if (heart.distanceToSqr(camera) > CULL_DISTANCE_SQR) continue;
			Vec3 broke = event.points().get(0).lerp(event.points().get(1),
					OfferingDissolveRules.lift(OfferingDissolveRules.BREAK_TICKS));
			Vec3 start = SwallowedFragmentRules.toLocal(broke.subtract(heart), frame);
			float size = SwallowedFragmentRules.size(since);
			var sprite = mc.getItemRenderer().getModel(event.item(), mc.level, null, 0).getParticleIcon();
			java.util.Random pick = new java.util.Random(event.index() * 977L);
			for (int i = 0; i < CRUMBS; i++) {
				Vec3 at = heart.add(SwallowedFragmentRules.toWorld(SwallowedFragmentRules.offset(since, i,
						time + event.index() * 7.0D, start, height * 0.2D, height * 0.3D), frame));
				// Like vanilla's crumbs: a random quarter of the sprite, sized a little differently each.
				float uo = pick.nextFloat() * 3.0F, vo = pick.nextFloat() * 3.0F;
				float half = size * (0.7F + 0.6F * pick.nextFloat());
				crumb(poseStack, consumer, facing, at.subtract(camera), half,
						sprite.getU((uo + 1.0F) / 4.0F), sprite.getU(uo / 4.0F),
						sprite.getV(vo / 4.0F), sprite.getV((vo + 1.0F) / 4.0F));
				any = true;
			}
		}
		return any;
	}

	/** One camera-facing crumb quad at {@code at} (relative to the camera). */
	private static void crumb(PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer consumer,
			org.joml.Quaternionf facing, Vec3 at, float half, float u0, float u1, float v0, float v1) {
		var pose = poseStack.last();
		org.joml.Vector3f[] corners = {new org.joml.Vector3f(-1, -1, 0), new org.joml.Vector3f(-1, 1, 0),
				new org.joml.Vector3f(1, 1, 0), new org.joml.Vector3f(1, -1, 0)};
		float[][] uv = {{u1, v1}, {u1, v0}, {u0, v0}, {u0, v1}};
		for (int k = 0; k < 4; k++) {
			org.joml.Vector3f corner = corners[k].rotate(facing).mul(half).add((float) at.x, (float) at.y, (float) at.z);
			consumer.addVertex(pose, corner.x(), corner.y(), corner.z()).setColor(255, 255, 255, 255)
					.setUv(uv[k][0], uv[k][1]).setOverlay(OverlayTexture.NO_OVERLAY)
					.setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
		}
	}

	/** The daemon that broke this offering: the one nearest where it hung over the brazier. */
	private static HumanitySpriteEntity daemonFor(Minecraft mc, RiteEvent event) {
		Vec3 hung = event.points().get(1);
		return mc.level.getEntitiesOfClass(HumanitySpriteEntity.class, new AABB(event.center()).inflate(24.0D)).stream()
				.min(Comparator.comparingDouble(entity -> entity.distanceToSqr(hung))).orElse(null);
	}

	/** The offering breaks: a few crumbs scatter, it cracks audibly, and its pieces are drawn into the daemon. */
	private static void shatter(Minecraft mc, RiteEvent event) {
		var level = mc.level;
		var random = level.random;
		Vec3 bowl = event.points().get(0);
		Vec3 daemon = event.points().get(1);
		Vec3 at = bowl.lerp(daemon, OfferingDissolveRules.lift(OfferingDissolveRules.BREAK_TICKS));
		ItemParticleOption crumbs = new ItemParticleOption(ParticleTypes.ITEM, event.item());
		for (int i = 0; i < 6; i++) {
			level.addParticle(crumbs, at.x, at.y, at.z, (random.nextDouble() - 0.5D) * 0.24D,
					0.05D + random.nextDouble() * 0.18D, (random.nextDouble() - 0.5D) * 0.24D);
		}
		for (int i = 0; i < 8; i++) {
			Vec3 toward = daemon.subtract(at).scale(0.12D + random.nextDouble() * 0.08D);
			level.addParticle(HemoParticleData.bloodCell(BLOOD), at.x + (random.nextDouble() - 0.5D) * 0.3D, at.y,
					at.z + (random.nextDouble() - 0.5D) * 0.3D, toward.x, toward.y, toward.z);
		}
		level.playLocalSound(at.x, at.y, at.z, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.9F,
				0.8F + random.nextFloat() * 0.3F, false);
	}
}
