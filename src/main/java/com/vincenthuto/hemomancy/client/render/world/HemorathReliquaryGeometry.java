package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathAnimState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRise;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathOpenings;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathVisualRules;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Hollow iron and circulating blood (design section 12): the body's inner blood, which climbs his hollow chest as he
 * fills, and every server burst of the {@code HEMORATH_RELIQUARY} form, in Animus blood with the Ferric iron plate,
 * white base tint and the two accents CORE and CLOT. The move, presence and arena geometry that reads his synced move
 * lives in {@link HemorathMoveGeometry}; his footprints, hazards and struck bodies in {@link HemorathTelegraphPass},
 * which {@link #collect} runs first in both passes. Geometry first; particles are only accents. The body's own blood
 * stays a subtle overlay (narrow strands in soft shells); the attacks are bold (visual pass 2).
 */
final class HemorathReliquaryGeometry {
	static final int TINT = 0xFFFFFF, CORE = 0xFFD2C4, CLOT = 0x7A1020;
	static final Vec3 UP = new Vec3(0, 1, 0);
	/** A stream's half-width at rest and its boluses' speed in blocks a tick. */
	static final double STREAM_WIDTH = 0.18D, STREAM_SPEED = 0.32D;

	/** What the body read draws from: the swell, his age, a seed, the phase, an open opening and three attachments. */
	record Frame(float swell, float age, int seed, boolean phase2, int opening, Vec3 core, Vec3 claw, Vec3 staff) {
	}

	private HemorathReliquaryGeometry() {
	}

	static boolean handles(Form form) {
		return form == Form.HEMORATH_RELIQUARY;
	}

	static void collect(ClientLevel level, PoseStack p, LuxUmbraBatch flows, VertexConsumer iron, Vec3 camera,
			Vec3 right, Vec3 up, double time, float partial, boolean solids) {
		HemorathRenderTypes.begin(time);
		HemorathTelegraphPass.collect(level, p, flows, iron, camera, right, up, time, partial, solids);
		for (HemorathEntity boss : level.getEntitiesOfClass(HemorathEntity.class, new AABB(camera, camera).inflate(96))) {
			if (!boss.isAlive() || boss.isInvisible()) continue;
			HemorathAnimState motion = HemorathAnimState.of(boss);
			Vec3 origin = boss.getPosition(partial);
			Vec3 eye = camera.subtract(origin);
			float rise = boss.riseTime(partial);
			boolean rising = rise >= 0 && rise < SaintRise.TICKS;
			p.pushPose();
			p.translate(origin.x - camera.x, origin.y - camera.y, origin.z - camera.z);
			p.translate(0, -SaintRise.depth(rise, boss.getBbHeight()), 0);
			Frame frame = new Frame(motion.swell(), AnimationTime.age(boss, partial), boss.getId(), boss.isInPhase2(),
					boss.syncedOpening(), motion.core(), motion.claw(), motion.staff());
			VertexConsumer blood = flows.vertices(LuxUmbraBatch.Material.ANIMUS);
			VertexConsumer clot = flows.vertices(LuxUmbraBatch.Material.CRUOR);
			if (!solids) body(p, blood, blood, eye, right, up, frame);
			if (!rising) HemorathMoveGeometry.collect(boss, motion, p, blood, clot, iron, origin, eye, right, up, time, partial,
					solids);
			p.popPose();
		}
	}

	/** The blood that climbs his hollow chest, narrow strands in soft shells, and the heart's glow when fed. */
	static void body(PoseStack p, VertexConsumer blood, VertexConsumer shade, Vec3 camera, Vec3 right, Vec3 up, Frame f) {
		float intensity = HemoEasing.clamp01(f.swell());
		if (HemorathOpenings.feeds(f.opening())) intensity = Math.max(intensity, .8F);
		if (intensity <= .02F) return;
		Vec3 core = f.core();
		Vec3 across = new Vec3(f.staff().x - core.x, 0, f.staff().z - core.z);
		across = across.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : across.normalize();
		Vec3 depth = across.cross(UP);
		float beat = HemorathVisualRules.heartbeat(f.age(), f.swell());
		float ribs = HemorathVisualRules.ribSpread(f.swell());
		int strands = 2 + Math.round(intensity * 2);
		for (int i = 0; i < strands; i++) {
			double side = i % 2 == 0 ? -1 : 1;
			Vec3 low = core.add(across.scale(side * .22)).add(0, -.7 + (i / 2) * .25, 0);
			Vec3 middle = core.add(across.scale(side * (.3 + ribs * .3))).add(depth.scale(.04));
			Vec3 high = core.add(across.scale(side * .1)).add(0, .5, 0);
			List<Vec3> path = List.of(low, middle, high);
			ribbonPath(p, shade, path, camera, .08, .03, WISP, f.seed() + i, TINT, intensity * .16F);
			ribbonPath(p, blood, path, camera, .022, .008, RIBBON, f.seed() + i, TINT, intensity * .6F);
		}
		card(p, blood, core, right, up, .07 + intensity * .08, .09 + intensity * .12, FOCUS, f.seed(), CORE,
				intensity * (.3F + beat * .3F));
	}

	/** A dark pressure sheath around two clot strands and a pale moving knot: the debt drawn toward him. */
	static void debt(PoseStack p, VertexConsumer blood, VertexConsumer shade, Vec3 from, Vec3 to, Vec3 camera,
			Vec3 right, Vec3 up, double time, int seed, float pressure) {
		Vec3 axis = to.subtract(from);
		if (axis.lengthSqr() < 1e-6 || pressure <= 0) return;
		Vec3 side = axis.cross(UP);
		side = side.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : side.normalize();
		Vec3 lift = axis.normalize().cross(side).normalize();
		List<Vec3> spine = new ArrayList<>(17);
		for (int i = 0; i <= 16; i++) {
			double t = i / 16.0;
			spine.add(from.lerp(to, t).add(0, -Math.sin(t * Math.PI) * .25 * (1 - pressure), 0));
		}
		ribbonPath(p, shade, spine, camera, .14, .06, WISP, seed, TINT, pressure * .2F);
		for (int strand = 0; strand < 2; strand++) {
			List<Vec3> points = new ArrayList<>(17);
			for (int i = 0; i <= 16; i++) {
				double a = i * .8 + time * .15 + strand * Math.PI;
				points.add(spine.get(i).add(side.scale(Math.cos(a) * .05)).add(lift.scale(Math.sin(a) * .05)));
			}
			ribbonPath(p, blood, points, camera, .04, .02, RIBBON, seed + strand, strand == 0 ? TINT : CLOT, pressure * .75F);
		}
		double knot = 1 - ((time * .045 + seed * .13) % 1 + 1) % 1;
		card(p, blood, from.lerp(to, knot), right, up, .06, .09, FOCUS, seed, CORE, pressure * .7F);
	}

	/** Iron links from one point to another, {@code count} of them, in the Ferric plate. */
	static void debtLinks(PoseStack p, VertexConsumer iron, Vec3 from, Vec3 to, int seed, float pressure) {
		debtLinks(p, iron, from, to, 8, seed, pressure);
	}

	static void debtLinks(PoseStack p, VertexConsumer iron, Vec3 from, Vec3 to, int count, int seed, float pressure) {
		if (pressure <= .05 || count <= 0 || from.distanceToSqr(to) < .01) return;
		Vec3 axis = to.subtract(from).normalize(), side = axis.cross(UP);
		side = side.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : side.normalize();
		double length = from.distanceTo(to);
		for (int i = 0; i < count; i++) {
			Vec3 at = from.add(axis.scale(length * (i + .5) / count));
			Vec3 width = i % 2 == 0 ? side : axis.cross(side).normalize();
			link(p, iron, at, axis.scale(.14 * pressure), width.scale(.085 * pressure), seed + i);
		}
	}

	/** One open iron link: four bars round a gap. */
	static void link(PoseStack p, VertexConsumer iron, Vec3 at, Vec3 along, Vec3 wide, int seed) {
		Vec3[] outer = {at.subtract(along).subtract(wide), at.add(along).subtract(wide), at.add(along).add(wide), at.subtract(along).add(wide)};
		Vec3[] inner = {at.subtract(along.scale(.72)).subtract(wide.scale(.5)), at.add(along.scale(.72)).subtract(wide.scale(.5)),
				at.add(along.scale(.72)).add(wide.scale(.5)), at.subtract(along.scale(.72)).add(wide.scale(.5))};
		for (int i = 0; i < 4; i++) {
			Vec3 a = outer[i], b = outer[(i + 1) % 4], c = inner[(i + 1) % 4], d = inner[i];
			MaterialQuads.quad(iron, p.last().pose(), a.x, a.y, a.z, b.x, b.y, b.z, c.x, c.y, c.z, d.x, d.y, d.z,
					0, MaterialQuads.seedTile(seed), HemorathIronGeometry.IRON, 1, 0, 1, false);
		}
	}

	/** A ring of short ribbon arcs on the floor (relative to the pose). */
	static void ring(PoseStack p, VertexConsumer v, double radius, double y, Vec3 camera, int seed,
			double width, int color, float alpha) {
		int arcs = Math.max(12, (int) (radius * 4));
		for (int i = 0; i < arcs; i++) {
			double a = i * Math.PI * 2 / arcs, b = a + Math.PI * 2 / arcs * .8;
			ribbonPath(p, v, List.of(new Vec3(Math.cos(a) * radius, y, Math.sin(a) * radius),
					new Vec3(Math.cos(b) * radius, y, Math.sin(b) * radius)), camera, width, RIBBON, seed + i, color, alpha);
		}
	}

	/**
	 * A blood stream from {@code from} to {@code to} with body (visual pass 2): {@link HemorathBloodGeometry#stream}'s
	 * shell, glossy dark body, lit rim, twisting sinews, travelling boluses and shedding drops. {@code reach} runs 0 to
	 * 1 along it.
	 */
	static void stream(PoseStack p, VertexConsumer blood, Vec3 from, Vec3 to, Vec3 camera, Vec3 right, Vec3 up,
			double time, int seed, double reach, double lift, float alpha) {
		HemorathBloodGeometry.stream(p, blood, from, to, camera, right, up, time, seed, reach, lift, STREAM_WIDTH, STREAM_SPEED, alpha);
	}

	static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer iron, VertexConsumer blood,
			VertexConsumer shade, boolean solids, Vec3 camera, Vec3 right, Vec3 up, double time, float age, float alpha) {
		float fade = alpha * HemorathVisualRules.fade(age, packet.ticks());
		if (fade <= 0) return;
		int kind = packet.count(), seed = Math.floorMod(packet.from().hashCode(), 113);
		double r = packet.radius();
		Vec3 to = packet.to().subtract(packet.from());
		if (solids) {
			drawIron(p, iron, kind, seed, r, age, fade);
			return;
		}
		switch (kind) {
			case HemorathVisualRules.SIPHON -> {
				double radius = r * (1 - HemoEasing.easeOutCubic(age / packet.ticks()));
				ring(p, blood, Math.max(.04, radius), .08, camera, seed, .02, TINT, fade * .5F);
				for (int i = 0; i < 8; i++) {
					double a = i * Math.PI / 4;
					Vec3 at = new Vec3(Math.cos(a) * radius, .1, Math.sin(a) * radius);
					ribbonPath(p, blood, List.of(at, at.scale(.5).add(0, .25, 0), new Vec3(0, 1.4, 0)),
							camera, .02, .006, RIBBON, seed + i, TINT, fade * .4F);
				}
			}
			case HemorathVisualRules.INTAKE -> HemorathBloodGeometry.drain(p, blood, Vec3.ZERO, to, camera, right, up, time, seed,
					fade);
			// the stream itself is drawn from his synced draw; each pulse is the well's surface breaking into a crown
			case HemorathVisualRules.WELL_STREAM -> HemorathBloodGeometry.crown(p, blood, new Vec3(0, -.3, 0), camera, right, up,
					time, seed, .8, .55, fade);
			case HemorathVisualRules.ROAD_HOME -> {
				double reach = HemoEasing.smoothstep(age / Math.max(1.0, packet.ticks() - 4.0));
				stream(p, blood, Vec3.ZERO, to, camera, right, up, time, seed, reach, .35, fade);
			}
			case HemorathVisualRules.PULSE_RELEASE -> {
				double radius = Math.min(r, .4 + HemoEasing.easeOutCubic(age / 7.0) * (r - .4));
				// a shock ring with body: a bright band, a dark clot wake behind it and spurts thrown off its crest
				ring(p, blood, radius, .12, camera, seed, .12 * fade, TINT, fade * .85F);
				ring(p, shade, radius * .94, .14, camera, seed + 25, .32, CLOT, fade * .3F);
				for (int i = 0; i < 8; i++) {
					double a = i * Math.PI / 4;
					Vec3 tip = new Vec3(Math.cos(a) * radius, .16, Math.sin(a) * radius);
					ribbonPath(p, blood, List.of(tip.scale(.82), tip.add(0, .25, 0), tip), camera,
							.035, .006, SLASH, seed + i, CORE, fade * .5F);
				}
			}
			case HemorathVisualRules.MELEE -> {
				for (double offset : slashOffsets())
					ribbonPath(p, blood, List.of(new Vec3(offset - .3, .4, 0), new Vec3(offset, 0, 0), new Vec3(offset + .1, -.4, 0)),
							camera, .05, .012, SLASH, seed, TINT, fade);
			}
			case HemorathVisualRules.DEBT_HIT -> {
				double squeeze = HemoEasing.easeOutCubic(age / 9.0);
				for (int i = 0; i < 10; i++) {
					double a = i * 2.39996 + seed;
					Vec3 direction = new Vec3(Math.cos(a), Math.sin(a * 1.5) * .65, Math.sin(a)).normalize();
					Vec3 start = direction.scale(r * (1 - squeeze * .8));
					Vec3 end = direction.scale(r * .06);
					List<Vec3> path = List.of(start, start.lerp(end, .5).add(0, .06, 0), end);
					ribbonPath(p, shade, path, camera, .12, .02, WISP, seed + i, TINT, fade * .22F);
					ribbonPath(p, blood, path, camera, .045, .012, RIBBON, seed + i, TINT, fade * .8F);
				}
				card(p, blood, Vec3.ZERO, right, up, .18, .26, FOCUS, seed, CORE, fade);
			}
			case HemorathVisualRules.RIB_JETS -> ribJets(p, blood, shade, camera, right, up, seed, r, age, fade);
			case HemorathVisualRules.STAFF_CRACK -> {
				HemorathBloodGeometry.crown(p, blood, new Vec3(0, .05, 0), camera, right, up, time, seed, 1.0, .7, fade);
				for (int i = 0; i < 5; i++) {
					double a = i * 2.39996 + seed;
					HemorathBloodGeometry.spatter(p, blood, new Vec3(0, .1, 0), new Vec3(Math.cos(a), 0, Math.sin(a)), camera, right, up,
							age, seed + i, 2.5, fade);
				}
			}
			case HemorathVisualRules.PATCH_BLOOM -> HemorathIronGeometry.dust(p, blood, Vec3.ZERO, right, up, age, .5, seed);
			// the crown's ring of iron is drawn by HemorathTelegraphPass from the move itself
			case HemorathVisualRules.CROWN_SPIKES -> {
			}
			default -> finale(p, blood, shade, camera, right, up, kind, seed, r, age, fade);
		}
	}

	/** Iron in the solid pass: links thrown by the phase break and the ending, and a patch bloom's spikes and chips. */
	private static void drawIron(PoseStack p, VertexConsumer iron, int kind, int seed, double r, float age, float fade) {
		if (kind == HemorathVisualRules.PHASE_BREAK || kind == HemorathVisualRules.EXSANGUINATE) {
			for (int i = 0; i < 6; i++) {
				double a = i * Math.PI / 3 + seed, travel = HemoEasing.easeOutCubic(age / 14.0);
				Vec3 at = new Vec3(Math.cos(a) * r * travel, Math.sin(travel * Math.PI) * .65, Math.sin(a) * r * travel);
				link(p, iron, at, new Vec3(.13 * fade, .08 * fade, 0), new Vec3(0, .07 * fade, .06 * fade), seed + i);
			}
		} else if (kind == HemorathVisualRules.PATCH_BLOOM) {
			// a small bloom of iron round the site: three short spikes that punch out and sink within ten ticks
			for (int i = 0; i < 3; i++) {
				double a = i * 2.1 + seed;
				var spikes = HemorathFissureRules.cluster(seed + i * 5, .32, 2);
				HemorathIronGeometry.cluster(p, iron, new Vec3(Math.cos(a) * .18, -.08, Math.sin(a) * .18), spikes, age - i * .5F, 2,
						age, HemorathIronGeometry.IRON, seed + i);
			}
			HemorathIronGeometry.chips(p, iron, Vec3.ZERO, age, .8, 4, seed + 9);
		}
	}

	/** Eight jets out of his ribs in the Riposte layering: shell, three sinews, a knot at the rib, a slash tip. */
	private static void ribJets(PoseStack p, VertexConsumer blood, VertexConsumer shade, Vec3 camera, Vec3 right, Vec3 up,
			int seed, double r, float age, float fade) {
		double grow = HemoEasing.easeOutCubic(age / 5.0);
		for (int i = 0; i < 8; i++) {
			double a = Math.toRadians(22.5 + i * 45);
			Vec3 dir = new Vec3(Math.cos(a), 0, Math.sin(a));
			Vec3 root = dir.scale(.25);
			Vec3 tip = dir.scale(.25 + r * .5 * grow).add(0, -.9 * grow, 0);
			Vec3 middle = root.lerp(tip, .45).add(0, .25, 0);
			List<Vec3> path = List.of(root, middle, tip);
			ribbonPath(p, shade, path, camera, .11, .04, WISP, seed + i, TINT, fade * .26F);
			for (int s = 0; s < 3; s++) {
				double off = (s - 1) * .03;
				Vec3 side = dir.cross(UP).scale(off);
				ribbonPath(p, blood, List.of(root.add(side), middle.add(side), tip.add(side.scale(.5))), camera, .02, .006,
						RIBBON, seed + i * 3 + s, s == 1 ? TINT : CLOT, fade * .75F);
			}
			card(p, blood, root, right, up, .06, .08, FOCUS, seed + i, CORE, fade * .6F);
			ribbonPath(p, blood, List.of(tip.subtract(dir.scale(.2)), tip), camera, .03, .004, SLASH, seed + i, CLOT, fade * .7F);
		}
	}

	/**
	 * The overload (the vessel bursting at his chest) and the ending's exsanguination (blood pouring out of him to the
	 * floor), visual pass 2: a white-hot heart and a dark clot shell, then twelve jets with body (a soft shell, a glossy
	 * dark body swelling and thinning along its arc, a lit rim on top, a clot-dark sinew and a knot at the tip) flung
	 * out on falling arcs, drops raining off their tips, and for the ending a pool spreading over the floor beneath him.
	 * Timeline: flash 0 to 5, jets grow over 6 (overload) or 12 (ending) ticks, hold, then fray as the burst fades.
	 */
	private static void finale(PoseStack p, VertexConsumer blood, VertexConsumer shade, Vec3 camera, Vec3 right, Vec3 up,
			int kind, int seed, double r, float age, float fade) {
		boolean exsanguinate = kind == HemorathVisualRules.EXSANGUINATE;
		double grow = HemoEasing.easeOutCubic(age / (exsanguinate ? 12.0 : 6.0));
		float flash = 1 - HemoEasing.ramp(age, 0, 5);
		card(p, blood, Vec3.ZERO, right, up, .3 + .25 * grow, .3 + .25 * grow, FOCUS, seed, CORE, fade * flash);
		card(p, shade, Vec3.ZERO, right, up, .35 + grow * r * .35, .4 + grow * r * .4, WISP, seed + 1, CLOT, fade * .35F);
		double floor = exsanguinate ? -HemorathEntity.CHEST_HEIGHT + .03 : -HemorathEntity.CHEST_HEIGHT - .57;
		for (int i = 0; i < 12; i++) {
			double a = i * 2.39996 + seed;
			double spread = .75 + .5 * HemorathFissureRules.unit(seed, i);
			Vec3 out = new Vec3(Math.cos(a), 0, Math.sin(a));
			double rise = exsanguinate ? .25 : Math.sin(a * 1.7) * .6 + .2;
			List<Vec3> spine = new ArrayList<>(9);
			double[] body = new double[9], sheath = new double[9], sinew = new double[9];
			for (int k = 0; k <= 8; k++) {
				double t = grow * k / 8.0;
				double fall = (exsanguinate ? 2.4 : 1.4) * t * t;
				spine.add(out.scale(.15 + r * spread * t).add(0, rise * r * t * (1 - t) * 2 - fall, 0));
				double swell = 1 + .45 * Math.sin(k * 1.3 - age * .9 + i);
				body[k] = (.11 - .06 * k / 8.0) * swell * (1 + age * .015);
				sheath[k] = body[k] * 2.3;
				sinew[k] = body[k] * .3;
			}
			ribbonPath(p, shade, spine, camera, sheath, WISP, seed + i, TINT, fade * .22F);
			ribbonPath(p, blood, spine, camera, body, RIBBON, seed + i + 20, HemorathBloodGeometry.BODY, fade * .9F);
			List<Vec3> rim = new ArrayList<>(9);
			for (int k = 0; k <= 8; k++) rim.add(spine.get(k).add(0, body[k] * .5, 0));
			ribbonPath(p, blood, rim, camera, .015, .006, RIBBON, seed + i + 40, CORE, fade * .5F);
			ribbonPath(p, blood, spine, camera, sinew, RIBBON, seed + i + 60, CLOT, fade * .7F);
			Vec3 tip = spine.get(8);
			card(p, blood, tip, right, up, .07, .08, FOCUS, seed + i, TINT, fade);
			if (age > 4 && i % 2 == 0) {
				double phase = ((age - 4) / 10.0 + i * .13) % 1;
				Vec3 drop = tip.add(out.scale(.15 * phase)).add(0, -(tip.y - floor) * phase * phase, 0);
				ribbonPath(p, blood, List.of(drop.add(0, .18, 0), drop), camera, .01, .03, RIBBON, seed + i + 80, TINT,
						fade * (float) (1 - phase));
				card(p, blood, drop, right, up, .04, .05, FOCUS, seed + i + 90, TINT, fade * (float) (1 - phase));
			}
		}
		if (exsanguinate) {
			double pool = .6 + r * .8 * HemoEasing.easeOutCubic(age / 24.0);
			ground(p, blood, new Vec3(0, floor, 0), pool, POOL, seed + 3, TINT, fade * .75F);
			ground(p, blood, new Vec3(0, floor + .005, 0), pool * .6, POOL, seed + 4, CORE, fade * .35F);
		}
	}
}
