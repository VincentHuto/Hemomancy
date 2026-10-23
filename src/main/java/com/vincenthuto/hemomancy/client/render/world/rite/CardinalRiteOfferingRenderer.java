package com.vincenthuto.hemomancy.client.render.world.rite;

import com.vincenthuto.hemomancy.client.render.AnimationTime;
import static com.vincenthuto.hemomancy.client.render.world.rite.CardinalRiteVisualRules.*;
import static com.vincenthuto.hemomancy.client.render.world.rite.RiteSurface.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.world.AnimusMortemRenderTypes;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteEventClientState;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteEventClientState.RiteEvent;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Offering draws during the procession. For each {@code OFFERING_DRAWN} beat a vessel reaches from the brazier,
 * dips low over the floor and climbs into the hovering daemon; the offering rides it, spinning, and dissolves into
 * blood cells as it arrives while the vessel withdraws after it. Draws are independent, so several can be in flight.
 * <p>
 * Layers, back to front: a broad translucent shell, two sinew strands twisting about the spine, a pale core line, the
 * brazier flare and the arrival burst, then the item itself.
 */
public final class CardinalRiteOfferingRenderer {
	private static final int SAMPLES = 18;
	private static final double CULL_DISTANCE_SQR = 96.0D * 96.0D;
	private static final ParticleColor EMBER = new ParticleColor(230, 40, 34);
	private static final ParticleColor GLOW = new ParticleColor(180, 12, 28);

	private static final View VIEW = new View();
	private static final Path SPINE = new Path();
	private static final Path STRAND = new Path();
	private static final double[] P = new double[3];
	private static final double[] STATION = new double[3 * (SAMPLES + 1)];
	/** Per draw, the last game tick its particle accents were spawned. */
	private static final Map<RiteEvent, Long> ACCENTED = new IdentityHashMap<>();

	private CardinalRiteOfferingRenderer() {
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		List<RiteEvent> events = CardinalRiteEventClientState.all();
		if (events.isEmpty()) {
			ACCENTED.clear();
			return;
		}
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		double time = AnimationTime.gameTime(mc.level, partialTick);
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		AnimusMortemRenderTypes.begin(time);

		boolean any = false;
		VertexConsumer blood = null;
		for (RiteEvent event : events) {
			if (!visible(event, partialTick, camera)) continue;
			if (blood == null) blood = buffers.getBuffer(AnimusMortemRenderTypes.ANIMUS);
			drawVessel(poseStack, blood, event, camera, time, event.age(partialTick));
			any = true;
		}
		if (!any) return;
		buffers.endBatch(AnimusMortemRenderTypes.ANIMUS);

		long tick = mc.level.getGameTime();
		for (RiteEvent event : events) {
			if (!visible(event, partialTick, camera)) continue;
			float age = event.age(partialTick);
			drawItem(mc, poseStack, buffers, event, camera, time, age);
			Long last = ACCENTED.get(event);
			if (last == null || last != tick) {
				ACCENTED.put(event, tick);
				spawnAccents(mc, event, age);
			}
		}
		buffers.endBatch();
		ACCENTED.keySet().removeIf(event -> !offeringVisible(event.age(0)));
	}

	private static boolean visible(RiteEvent event, float partialTick, Vec3 camera) {
		if (event.kind() != CardinalRiteEventKind.OFFERING_DRAWN || event.points().size() < 2) return false;
		if (!offeringVisible(event.age(partialTick))) return false;
		return event.points().get(0).distanceToSqr(camera) < CULL_DISTANCE_SQR;
	}

	/** Samples the arc into {@link #STATION}, relative to the brazier. */
	private static void sampleArc(RiteEvent event, double time) {
		Vec3 a = event.points().get(0), b = event.points().get(1);
		double floorY = Math.min(event.center().getY() + 1.0D, a.y - 0.5D);
		double bx = b.x - a.x, by = b.y - a.y, bz = b.z - a.z;
		double length = Math.sqrt(bx * bx + bz * bz);
		double sideX = length < 1e-6 ? 1 : -bz / length, sideZ = length < 1e-6 ? 0 : bx / length;
		int seed = event.index() * 17;
		for (int i = 0; i <= SAMPLES; i++) {
			double t = i / (double) SAMPLES;
			offeringArc(0, 0, 0, bx, by, bz, floorY - a.y, t, P);
			double writhe = Math.sin(t * Math.PI) * Math.sin(t * 9 - time * 0.25 + seed) * 0.14;
			STATION[i * 3] = P[0] + sideX * writhe;
			STATION[i * 3 + 1] = P[1];
			STATION[i * 3 + 2] = P[2] + sideZ * writhe;
		}
	}

	/** Linear position along the sampled arc. */
	private static void along(double t, double[] out) {
		double f = HemoEasing.clamp01(t) * SAMPLES;
		int i = Math.min(SAMPLES - 1, (int) f);
		double r = f - i;
		for (int c = 0; c < 3; c++) out[c] = HemoEasing.lerp(STATION[i * 3 + c], STATION[(i + 1) * 3 + c], r);
	}

	private static void drawVessel(PoseStack poseStack, VertexConsumer v, RiteEvent event, Vec3 camera, double time,
			float age) {
		Vec3 a = event.points().get(0);
		VIEW.set(camera.x, camera.y, camera.z, a.x, a.y, a.z,
				Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
		poseStack.pushPose();
		poseStack.translate(a.x - camera.x, a.y - camera.y, a.z - camera.z);
		Matrix4f m = poseStack.last().pose();
		sampleArc(event, time);
		int seed = Math.floorMod(event.index() * 31 + event.center().hashCode(), 113);
		float pulse = heartbeat(time);

		double head = offeringReach(age), tail = offeringTail(age);
		double travel = offeringTravel(age);
		if (head - tail > 0.02) {
			// 1. Shell: a broad translucent sheath.
			SPINE.clear();
			for (int i = 0; i <= 12; i++) {
				double t = HemoEasing.lerp(tail, head, i / 12.0);
				along(t, P);
				SPINE.add(P[0], P[1], P[2], 0.2 * (1 - 0.4 * t));
			}
			ribbon(m, v, VIEW, SPINE, RIBBON, seed, TINT, 0.28F);
			// 2. Sinew: two strands twisting about the spine, swelling where the offering passes.
			for (int strand = 0; strand < 2; strand++) {
				STRAND.clear();
				for (int i = 0; i <= 12; i++) {
					double t = HemoEasing.lerp(tail, head, i / 12.0);
					along(t, P);
					double angle = t * 14 + strand * Math.PI + time * 0.2;
					double swell = 1 + 1.4 * Math.exp(-Math.pow((t - travel) * 9, 2));
					double radius = 0.06 * swell;
					STRAND.add(P[0] + Math.cos(angle) * radius, P[1] + Math.sin(angle) * radius,
							P[2] + Math.sin(angle) * radius * 0.6, (0.075 - 0.03 * t) * swell);
				}
				ribbon(m, v, VIEW, STRAND, RIBBON, seed + 1 + strand, TINT, 0.75F + 0.25F * pulse);
			}
			// 3. Pale core line.
			ribbon(m, v, VIEW, SPINE, 0, SPINE.size(), 0.12, RIBBON, seed + 3, CORE, 0.7F);
		}

		// 4. Brazier flare: the vessel tears out of the coals.
		double flare = offeringFlare(age);
		if (flare > 0) {
			card(m, v, VIEW, 0, 0.1, 0, 0.32 * flare, 0.32 * flare, FOCUS, seed + 4, TINT, (float) flare);
			card(m, v, VIEW, 0, 0.25, 0, 0.6 * flare, 0.75 * flare, WISP, seed + 5, TINT, 0.45F * (float) flare);
			ground(m, v, 0, 0.02, 0, 0.45 + age * 0.06, RIPPLE, seed + 6, TINT, 0.6F * (float) flare);
		}
		// The offering's glow riding the vessel.
		if (age >= OFFERING_DEPART_TICKS && travel < 1) {
			along(travel, P);
			double scale = offeringItemScale(age);
			card(m, v, VIEW, P[0], P[1], P[2], 0.28 * scale + 0.06, 0.28 * scale + 0.06, WISP, seed + 7, TINT, 0.5F);
			card(m, v, VIEW, P[0], P[1], P[2], 0.1, 0.1, FOCUS, seed + 8, CORE, 0.6F * (float) scale);
		}

		// 5. Arrival: the offering bursts into cells at the daemon.
		double arrival = offeringArrival(age);
		if (arrival > 0) {
			along(1, P);
			double since = Math.max(0, age - OFFERING_ARRIVE_TICKS);
			card(m, v, VIEW, P[0], P[1], P[2], 0.35 * arrival, 0.35 * arrival, FOCUS, seed + 9, TINT, (float) arrival);
			for (int k = 0; k < 8; k++) {
				double y = 1 - 2 * (k + 0.5) / 8, ring = Math.sqrt(1 - y * y), angle = k * 2.399963 + seed;
				double reach = 0.12 + since * 0.07;
				card(m, v, VIEW, P[0] + Math.cos(angle) * ring * reach, P[1] + y * reach,
						P[2] + Math.sin(angle) * ring * reach, 0.05, 0.05, FOCUS, seed + 10 + k,
						k % 3 == 0 ? CLOT : TINT, (float) arrival * 0.9F);
			}
		}
		poseStack.popPose();
	}

	private static void drawItem(Minecraft mc, PoseStack poseStack, MultiBufferSource buffers, RiteEvent event,
			Vec3 camera, double time, float age) {
		if (event.item().isEmpty() || age < 0) return;
		double scale = offeringItemScale(age) * 0.42;
		if (scale <= 0.01) return;
		Vec3 a = event.points().get(0);
		sampleArc(event, time);
		along(age < OFFERING_DEPART_TICKS ? 0 : offeringTravel(age), P);
		poseStack.pushPose();
		poseStack.translate(a.x + P[0] - camera.x, a.y + P[1] - camera.y + 0.05, a.z + P[2] - camera.z);
		poseStack.mulPose(Axis.YP.rotationDegrees((float) (time * 14.0 + event.index() * 61.0)));
		poseStack.mulPose(Axis.XP.rotationDegrees((float) (Math.sin(time * 0.2 + event.index()) * 18.0)));
		poseStack.scale((float) scale, (float) scale, (float) scale);
		mc.getItemRenderer().renderStatic(event.item(), ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT,
				OverlayTexture.NO_OVERLAY, poseStack, buffers, mc.level, event.index());
		poseStack.popPose();
	}

	/** Light accents once per game tick: embers at the brazier, then cells of the offering's colour at the daemon. */
	private static void spawnAccents(Minecraft mc, RiteEvent event, float age) {
		Vec3 a = event.points().get(0), b = event.points().get(1);
		var random = mc.level.random;
		if (age < 8) {
			mc.level.addParticle(HemoParticleData.emberSpark(EMBER, 0.12F, 0.8F, 18),
					a.x + (random.nextDouble() - 0.5) * 0.4, a.y + 0.1, a.z + (random.nextDouble() - 0.5) * 0.4,
					0.0, 0.04, 0.0);
			if (age < 2) mc.level.addParticle(HemoParticleData.glow(GLOW), a.x, a.y + 0.2, a.z, 0.0, 0.02, 0.0);
		}
		if (age >= OFFERING_ARRIVE_TICKS - 1 && age < OFFERING_ARRIVE_TICKS + 1) {
			int rgb = CardinalRiteOfferingMemory.colour(event.item());
			ParticleColor colour = new ParticleColor((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255);
			for (int k = 0; k < 4; k++) {
				mc.level.addParticle(HemoParticleData.bloodCell(colour), b.x, b.y, b.z,
						(random.nextDouble() - 0.5) * 0.12, (random.nextDouble() - 0.3) * 0.1,
						(random.nextDouble() - 0.5) * 0.12);
			}
		}
	}
}
