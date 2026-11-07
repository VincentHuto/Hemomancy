package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.boss.velorum.VelorumAnimState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRise;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEffects;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEndingRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumOpeningRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumPresence;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumVisualRules;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Velorum's overlays and cue accents in the RIME and UMBRA materials: the lanterns' light on his body (shrunk to a
 * point by the Gutter and the Hour, the frost falling out of it at the end), the presence's seam disc, and the beats the
 * server cues that his synced state cannot show ({@link VelorumEffects}: a figure warming and waking, the Apotheos's
 * hyphae, a devotee kneeling home, the Bow's seam and the overheld threads, the broken Hour and the Litany's toll, a
 * guarded blow, the rest burst). Overlays on bodies stay subtle and partly clear. Since visual pass 2 (2026-11-06) every
 * move, the black ice, the struck bodies and the preserving shells are drawn from synced state by
 * {@link VelorumTelegraphPass}, so their cues ({@code TOLL}, {@code FROST_SLASH}, {@code STILL_FLOOR}, {@code LONG_NIGHT},
 * {@code BLACK_ICE}, {@code SHATTER}, {@code PRESERVED}, {@code FREED}, {@code GUTTER}) draw nothing here. Since the Frozen
 * Cruor revision (2026-11-06) the beats the pass draws in its own materials (a figure thawing into warm red veins, a
 * stain, a pulse mark and its beats, a figure's veins, the Shadow Step, the Eclipse, a shape's reach) are handed to
 * {@link VelorumCueLog} here instead, and his lantern's light {@code L} lies on the floor under it.
 */
final class VelorumRimeGeometry {
	private static final int TINT = 0xFFFFFF, CORE = 0xE8F4FF, BONE = 0xD8D2C6;
	private static final double SEARCH = 96.0D;
	private static final int THREADS = 5;

	private VelorumRimeGeometry() {
	}

	static boolean handles(Form form) {
		return form == Form.VELORUM_RIME;
	}

	static void collect(ClientLevel level, PoseStack p, LuxUmbraBatch batch, Vec3 camera, Vec3 right, Vec3 up, float partial) {
		VertexConsumer ice = batch.vertices(LuxUmbraBatch.Material.RIME);
		Player viewer = Minecraft.getInstance().player;
		for (VelorumEntity boss : level.getEntitiesOfClass(VelorumEntity.class, new AABB(camera, camera).inflate(SEARCH))) {
			if (!boss.isAlive() || boss.isInvisible()) continue;
			float rise = boss.riseTime(partial);
			if (rise >= 0 && rise < SaintRise.TICKS) continue;
			VelorumAnimState motion = VelorumAnimState.of(boss);
			VelorumAnimState.Frame frame = motion.frame();
			Vec3 at = boss.getPosition(partial);
			float age = AnimationTime.age(boss, partial);
			p.pushPose();
			p.translate(at.x - camera.x, at.y - camera.y, at.z - camera.z);
			Vec3 local = camera.subtract(at);
			lanterns(p, ice, local, right, up, motion, frame, boss.getId(), age);
			lanternLight(p, batch.vertices(LuxUmbraBatch.Material.LUX), boss, partial);
			float still = viewer == null ? 0.0F : Math.max(0, boss.syncedChallengerValue(viewer.getId())) / 255.0F;
			if (boss.syncedPresenceRadius() > 0 && frame.ending() < 0) {
				// the chill's ground read: a disc of hairline seams that firms as the viewer grows still
				ground(p, ice, new Vec3(0, .03, 0), VelorumPresence.RADIUS, RIPPLE, boss.getId(), TINT, .06F + .10F * still);
			}
			p.popPose();
		}
	}

	/** The lanterns' light: a soft card at each crystal, shrunk to a point by the Gutter and the Hour, fading at the end. */
	private static void lanterns(PoseStack p, VertexConsumer ice, Vec3 camera, Vec3 right, Vec3 up, VelorumAnimState motion,
			VelorumAnimState.Frame frame, int seed, float age) {
		float light = 1.0F;
		if (frame.opening() == VelorumOpeningRules.GUTTER_ID) light *= HemoEasing.lerp(1.0F, .2F, frame.openingWeight());
		if (frame.hour()) light *= HemoEasing.lerp(1.0F, .45F, frame.moveWeight());
		if (frame.ending() >= 0) light *= HemoEasing.lerp(1.0F, .25F, HemoEasing.ramp(frame.ending(), 40, 64));
		Vec3[] lanterns = motion.lanterns();
		for (int i = 0; i < lanterns.length; i++) {
			double size = (i == 0 ? .09 : .06) * light;
			card(p, ice, lanterns[i], right, up, size, size * 1.4, WISP, seed + i, CORE, .55F * light);
			if (frame.ending() >= VelorumEndingRules.HOOD_BOWED) {
				// the frost falls out of the light: a few short fragments sliding down through each lantern's glow
				float fall = (frame.ending() - VelorumEndingRules.HOOD_BOWED) % 24.0F / 24.0F;
				Vec3 drop = lanterns[i].add(Math.sin(i * 2.1) * .08, -fall * .9, Math.cos(i * 2.1) * .08);
				spire(p, ice, drop, .012, .05, new Vec3(0, -.04, 0), seed + i * 3, .5F * (1 - fall));
			}
		}
	}

	/**
	 * His lantern's light {@code L} on the floor under it (revision 2026-11-06): a pool of cold light the dark cannot
	 * hold, the pools torn back at its edge. Shrinks with the Long Night and the Gutter, goes out in the Eclipse.
	 */
	private static void lanternLight(PoseStack p, VertexConsumer lux, VelorumEntity boss, float partial) {
		float radius = VelorumDarkSight.lantern(boss, partial);
		if (radius <= 0.1F || boss.syncedEndingTick() >= 0) return;
		Vec3 foot = VelorumDarkSight.lanternFoot(boss, partial).subtract(boss.getPosition(partial));
		ground(p, lux, foot.add(0, .035, 0), radius, POOL, boss.getId() + 5, CORE, .13F);
	}

	/** Four real tapered faces, with a leaning tip and a dark translucent frost material. */
	static void spire(PoseStack p, VertexConsumer v, Vec3 at, double width, double height, Vec3 lean, int seed, float alpha) {
		if (height <= 0 || width <= 0 || alpha <= 0) return;
		Vec3 tip = at.add(lean).add(0, height, 0);
		for (int i = 0; i < 4; i++) {
			double a = i * Math.PI / 2, b = a + Math.PI / 2;
			Vec3 x = at.add(Math.cos(a) * width, 0, Math.sin(a) * width), y = at.add(Math.cos(b) * width, 0, Math.sin(b) * width);
			MaterialQuads.quad(v, p.last().pose(), x.x, x.y, x.z, y.x, y.y, y.z, tip.x, tip.y, tip.z, tip.x, tip.y, tip.z,
					RIBBON, MaterialQuads.seedTile(seed + i), TINT, alpha, 0, 1, false);
		}
	}

	/** 1 through the beat's life, easing out over its tail; 0 outside it. */
	static float fade(float age, int life) {
		return age < 0 || age >= life ? 0 : 1 - HemoEasing.smoothstep((age - 3) / Math.max(1.0F, life - 3));
	}

	static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer ice, VertexConsumer shade, Vec3 camera,
			Vec3 right, Vec3 up, double time, float age, float alpha) {
		float fade = alpha * fade(age, packet.ticks());
		if (fade <= 0) return;
		int kind = VelorumVisualRules.kind(packet.count());
		if (VelorumCueLog.logs(kind)) {
			// drawn by the pass from the log, in his cruor and liquid-dark materials
			VelorumCueLog.note(packet, time, age);
			if (kind != VelorumEffects.THAW) return;
		}
		int seed = Math.floorMod(packet.from().hashCode(), 113);
		double radius = packet.radius();
		Vec3 to = packet.to().subtract(packet.from());
		switch (kind) {
			case VelorumEffects.THAW, VelorumEffects.WAKING -> {
				// hairline cracks running over the figure as the warm blood pours in, then the rime shedding
				double spread = kind == VelorumEffects.WAKING ? radius : 1.0;
				for (int i = 0; i < THREADS; i++) {
					double a = i * 1.26 + seed;
					Vec3 base = new Vec3(Math.cos(a) * .35, .1 + i * .3, Math.sin(a) * .35);
					Vec3 end = base.add(Math.cos(a + 1) * .25 * spread, .35 * spread, Math.sin(a + 1) * .25 * spread);
					ribbonPath(p, ice, List.of(base, end), camera, .006, .002, RIBBON, seed + i, TINT, .4F * fade);
				}
				if (kind == VelorumEffects.THAW) {
					for (int i = 0; i < 5; i++) {
						double a = i * 2.39996 + seed;
						double fall = HemoEasing.easeInCubic(age / 18.0F);
						Vec3 at = new Vec3(Math.cos(a) * .4, 1.6 - fall * 1.6, Math.sin(a) * .4);
						spire(p, ice, at, .035 * fade, .12 * fade, new Vec3(0, -.06, 0), seed + i, .45F * fade);
					}
				}
			}
			case VelorumEffects.PINNING -> {
				// fine pale hyphae threading into the figure's base, at most five, branching once
				for (int i = 0; i < 5; i++) {
					double a = i * 1.26 + seed;
					Vec3 mid = to.scale(.55).add(Math.cos(a) * .25, -.2, Math.sin(a) * .25);
					Vec3 end = to.add(Math.cos(a) * .3, .05, Math.sin(a) * .3);
					ribbonPath(p, ice, List.of(Vec3.ZERO, mid, end), camera, .005, .002, RIBBON, seed + i, BONE, .32F * fade);
					if (i % 2 == 0) ribbonPath(p, ice, List.of(mid, mid.lerp(end, .5).add(Math.sin(a) * .2, -.1, Math.cos(a) * .2)),
							camera, .004, .0015, RIBBON, seed + i + 11, BONE, .25F * fade);
				}
			}
			case VelorumEffects.KNEEL_HOME -> {
				for (int i = 0; i < 6; i++) {
					double a = i * Math.PI / 3 + seed;
					Vec3 at = new Vec3(Math.cos(a) * .45, 0, Math.sin(a) * .45);
					spire(p, ice, at, .04 * fade, .3 * fade * HemoEasing.ramp(age, 0, 6), new Vec3(0, 0, 0), seed + i, .45F * fade);
				}
			}
			case VelorumEffects.BOW, VelorumEffects.OVERHELD -> {
				// the overheld threads snap back into the palm with a glint each; the Bow's seam a pale flash
				int threads = kind == VelorumEffects.OVERHELD ? THREADS : 1;
				for (int i = 0; i < threads; i++) {
					double a = i * 1.26 + seed;
					double back = HemoEasing.easeOutCubic(age / 6.0F);
					Vec3 far = new Vec3(Math.cos(a) * 2.5 * (1 - back), -.2, Math.sin(a) * 2.5 * (1 - back));
					if (kind == VelorumEffects.OVERHELD) ribbonPath(p, ice, List.of(far, Vec3.ZERO), camera, .005, .002, RIBBON, seed + i, TINT, .4F * fade);
					card(p, ice, Vec3.ZERO, right, up, .06 * fade, .06 * fade, WISP, seed + i, CORE, .5F * fade);
				}
			}
			case VelorumEffects.HOUR_BROKEN, VelorumEffects.LITANY_TOLL -> {
				double reach = radius * HemoEasing.easeOutCubic(age / 10.0F);
				ground(p, ice, new Vec3(0, .05, 0), reach, RIPPLE, seed, TINT, .2F * fade);
			}
			case VelorumEffects.GUARDED -> {
				for (int i = 0; i < 3; i++) {
					double a = i * 2.1 + seed;
					spire(p, ice, new Vec3(Math.cos(a) * .2, -.1, Math.sin(a) * .2), .02 * fade, .1 * fade,
							new Vec3(Math.cos(a) * .05, 0, Math.sin(a) * .05), seed + i, .4F * fade);
				}
			}
			case VelorumEffects.REST -> {
				for (int i = 0; i < 7; i++) {
					double a = i * 2.39996 + seed, travel = HemoEasing.easeOutCubic(age / 22.0F);
					Vec3 at = new Vec3(Math.cos(a) * radius * travel, -1.4 * travel, Math.sin(a) * radius * travel);
					spire(p, ice, at, .045 * fade, .2 * fade, new Vec3(Math.cos(a) * .08, 0, Math.sin(a) * .08), seed + i, .45F * fade);
				}
			}
			default -> {
				// the moves, the black ice, the struck, the preserved and the Gutter are drawn by VelorumTelegraphPass
			}
		}
	}

	/** Points on a ground circle, for tests and the hazard read. */
	static List<Vec3> ring(Vec3 centre, double radius, int count) {
		List<Vec3> points = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			double a = i * Math.PI * 2 / count;
			points.add(centre.add(Math.cos(a) * radius, 0, Math.sin(a) * radius));
		}
		return points;
	}
}
