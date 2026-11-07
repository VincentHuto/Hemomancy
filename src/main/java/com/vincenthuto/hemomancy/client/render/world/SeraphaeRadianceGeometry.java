package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.SeraphaeAnimState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRise;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.ContainmentAnchorEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEndingRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeFragmentEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeGrowthRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeOpenings;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeRestraint;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeVisualRules;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Seraphae's standing light in the shared school batch: her heart, her chains from each anchor's crown to its iron,
 * the anchors' accents, her presence ring, the growth her light raises on players, the hold's sinew, and her fragments.
 * White is the LUX material (with the Riposte's hot core and clot accents); gold is only ever the DUCTILIS material and
 * only drawn when a Ductilis consumer is given. Overlays stay subtle and partly clear: no strand wider than 0.019, shells
 * at most 0.28 alpha, slack chains a single ribbon.
 */
final class SeraphaeRadianceGeometry {
	private static final int TINT = 0xFFFFFF, CORE = 0xFFD2C4, CLOT = 0x7A1020, NERVE = 0xFFD678;
	private static final int LINE = 0, GLOW = 1, CRACKLE = 2, SPARK = 3;
	private static final Vec3 UP = new Vec3(0, 1, 0);
	/** Strand and shell ceilings: the subtlety budget. */
	static final double STRAND = 0.019D;
	static final float SHELL = 0.28F;
	static final float PRESENCE_ALPHA = 0.10F;
	private static final double RANGE = 96.0D;

	private SeraphaeRadianceGeometry() {
	}

	static boolean handles(Form form) {
		return form == Form.SERAPHAE_RELIQUARY;
	}

	/** The manipulation pass's hook: white only. */
	static void collect(ClientLevel level, PoseStack p, VertexConsumer lux, Vec3 camera, Vec3 right, Vec3 up,
			double time, float partial) {
		collect(level, p, lux, null, camera, right, up, time, partial);
	}

	/** With the school batch: white and the gold nerve sparks (compile-gate follow-up: the hook passes the batch). */
	static void collect(ClientLevel level, PoseStack p, LuxUmbraBatch flows, Vec3 camera, Vec3 right, Vec3 up,
			double time, float partial) {
		collect(level, p, flows.vertices(LuxUmbraBatch.Material.LUX), flows.vertices(LuxUmbraBatch.Material.DUCTILIS),
				camera, right, up, time, partial);
	}

	static void collect(ClientLevel level, PoseStack p, VertexConsumer v, @Nullable VertexConsumer gold, Vec3 camera,
			Vec3 right, Vec3 up, double time, float partial) {
		AABB near = new AABB(camera, camera).inflate(RANGE);
		Minecraft mc = Minecraft.getInstance();
		for (SeraphaeEntity boss : level.getEntitiesOfClass(SeraphaeEntity.class, near)) {
			if (!boss.isAlive()) continue;
			SeraphaeAnimState motion = SeraphaeAnimState.of(boss);
			Vec3 origin = boss.getPosition(partial);
			float age = AnimationTime.age(boss, partial);
			float rise = boss.riseTime(partial);
			boolean rising = rise >= 0 && rise < SaintRise.TICKS;
			float ending = boss.view().endingTime(partial);
			if (ending >= SeraphaeEndingRules.END_TRIAL) continue;
			float light = ending < 0 ? 1.0F : SeraphaeEndingRules.light(ending);
			Vec3 at = origin.subtract(camera);
			int seed = boss.getId();
			heart(p, v, at.add(motion.heart()), right, up, seed, boss.syncedPhase(), light, boss.view().openingId());
			if (rising) continue;
			if (ending < 0) presence(p, v, at, boss.syncedPresenceRadius(), age, seed);
			for (ContainmentAnchorEntity anchor : level.getEntitiesOfClass(ContainmentAnchorEntity.class,
					boss.getBoundingBox().inflate(48), a -> boss.getUUID().equals(a.getParentId()))) {
				Vec3 crown = anchor.getPosition(partial).subtract(camera).add(0, ContainmentAnchorEntity.CROWN, 0);
				SeraphaeRestraint restraint = anchor.kind() == ContainmentAnchorEntity.Kind.RESERVE
						? SeraphaeRestraint.WAIST : anchor.restraint();
				Vec3 iron = at.add(motion.shackle(restraint));
				float flare = anchor.flare() / (float) ContainmentAnchorEntity.FLARE_TICKS;
				anchor(p, v, anchor.getPosition(partial).subtract(camera), right, up, time, anchor.getId(), anchor.isHeld(),
						Math.min(1.0F, flare));
				double reach = Math.max(1.0D, boss.syncedLeash() + 2.0D);
				float taut = (float) HemoEasing.clamp01(crown.distanceTo(iron) / reach);
				if (ending >= 0) taut = Math.max(taut, SeraphaeEndingRules.braid(ending, 0));
				if (boss.view().openingId() == SeraphaeOpenings.RECOIL_ID) {
					taut = Math.min(1.0F, taut + recoilSpike(boss.view().openingTime(partial)));
				}
				switch (anchor.state()) {
					case HELD -> chain(p, v, crown, iron, right, up, time, anchor.getId(), taut, light);
					case SLIPPED -> frayed(p, v, iron, right, up, time, anchor.getId());
					case BINDING -> {
						Vec3 head = crown.lerp(iron, anchor.progress());
						chain(p, v, crown, head, right, up, time, anchor.getId(), 0.6F, 1.0F);
						if (gold != null) card(p, gold, head, right, up, 0.06, 0.06, SPARK, anchor.getId(), CORE, 0.8F);
						frayed(p, v, iron, right, up, time, anchor.getId());
					}
				}
			}
			int heldId = boss.syncedHoldPlayer();
			Entity held = heldId < 0 ? null : level.getEntity(heldId);
			if (held != null) {
				Vec3 chest = held.getPosition(partial).subtract(camera).add(0, held.getBbHeight() * 0.62, 0);
				for (boolean left : new boolean[]{true, false}) {
					hold(p, v, at.add(motion.hand(left)), chest, time, seed + (left ? 0 : 7));
				}
				if (gold != null && age % 10 < 5) {
					card(p, gold, chest, right, up, 0.07, 0.07, SPARK, seed + 3, CORE, 1.0F - (age % 10) / 5.0F);
				}
			}
			for (Player player : level.getEntitiesOfClass(Player.class, boss.getBoundingBox().inflate(32))) {
				int flags = boss.syncedChallengerFlags(player.getId());
				if ((flags & (SeraphaeGrowthRules.FLAG_SWELLING | SeraphaeGrowthRules.FLAG_KNOTTING)) == 0) continue;
				boolean self = player == mc.player && mc.options.getCameraType().isFirstPerson();
				growth(p, v, player.getPosition(partial).subtract(camera), player.yBodyRot, right, up, time,
						player.getId(), (flags & SeraphaeGrowthRules.FLAG_KNOTTING) != 0, self);
			}
		}
		for (SeraphaeFragmentEntity fragment : level.getEntitiesOfClass(SeraphaeFragmentEntity.class, near)) {
			if (!fragment.isAlive()) continue;
			Vec3 at = fragment.getPosition(partial).subtract(camera).add(0, 0.5, 0);
			fragment(p, v, gold, at, right, up, AnimationTime.age(fragment, partial), fragment.getId(), fragment.drawn());
		}
	}

	/** A spike in every chain's tension through the Recoil's first 6 ticks, overshooting on a sine. */
	static float recoilSpike(float openingTime) {
		if (openingTime < 0 || openingTime > 6) return 0.0F;
		return (float) Math.sin(openingTime / 6.0 * Math.PI) * 0.6F;
	}

	/** Her heart: one white focus card at her chest, brighter by phase; nothing detached floats behind her. */
	static void heart(PoseStack p, VertexConsumer v, Vec3 heart, Vec3 right, Vec3 up, int seed, int phase, float light,
			int opening) {
		float brightness = (0.45F + 0.15F * Math.max(0, phase - 1)) * light;
		if (opening == SeraphaeOpenings.CONDENSING_ID || opening == SeraphaeOpenings.LAPSE_ID) brightness *= 0.6F;
		card(p, v, heart, right, up, 0.16, 0.22, WISP, seed, TINT, 0.08F * light);
		card(p, v, heart, right, up, 0.06 + phase * 0.012, 0.08 + phase * 0.014, FOCUS, seed, CORE, brightness);
	}

	/** One thin white ring on the floor at her presence's radius, breathing on her 20-tick pulse. */
	static void presence(PoseStack p, VertexConsumer v, Vec3 feet, double radius, float age, int seed) {
		if (radius <= 0) return;
		float pulse = HemoEasing.pulse(age % SeraphaeGrowthRules.CADENCE, 2, 6);
		ground(p, v, feet.add(0, 0.03, 0), radius, RIPPLE, seed, TINT, PRESENCE_ALPHA + pulse * 0.06F);
	}

	static void anchor(PoseStack p, VertexConsumer v, Vec3 feet, Vec3 right, Vec3 up, double time, int seed, boolean held,
			float flare) {
		float pulse = 0.7F + 0.2F * (float) Math.sin(time * 0.09 + seed);
		ground(p, v, feet.add(0, 0.035, 0), held ? 1.1 : 0.7, RIPPLE, seed, TINT, held ? 0.22F + flare * 0.3F : 0.1F);
		Vec3 crown = feet.add(0, ContainmentAnchorEntity.CROWN, 0);
		card(p, v, crown, right, up, held ? 0.11 + flare * 0.06 : 0.05, held ? 0.16 + flare * 0.08 : 0.07, FOCUS, seed,
				held ? CORE : CLOT, held ? pulse * (0.55F + flare * 0.4F) : 0.25F);
		if (flare <= 0.01F) return;
		for (int i = 0; i < 3; i++) {
			double a = i * Math.PI * 2 / 3 + time * 0.018;
			Vec3 root = feet.add(Math.cos(a) * 0.22, 0.15, Math.sin(a) * 0.22);
			ribbonPath(p, v, List.of(root, root.add(0, 1.2 * flare, 0)), Vec3.ZERO, STRAND, RIBBON, seed + i, TINT,
					0.5F * flare);
		}
	}

	/**
	 * A chain from an anchor's crown to her iron. Slack, it is one core ribbon with a sag; as it nears taut the braid
	 * shows (a translucent shell, three twisting strands, one of them the clot, travelling knots), brightest when taut.
	 */
	static void chain(PoseStack p, VertexConsumer v, Vec3 from, Vec3 to, Vec3 right, Vec3 up, double time, int seed,
			float taut, float alpha) {
		Vec3 axis = to.subtract(from);
		if (axis.lengthSqr() < 1e-6 || alpha <= 0) return;
		List<Vec3> spine = new ArrayList<>(19);
		double sag = 0.45 * (1 - taut);
		for (int i = 0; i <= 18; i++) {
			double t = i / 18.0;
			spine.add(from.lerp(to, t).add(0, -Math.sin(t * Math.PI) * sag, 0));
		}
		ribbonPath(p, v, spine, Vec3.ZERO, 0.012, RIBBON, seed + 4, CORE, alpha * (0.5F + 0.4F * taut));
		float braid = HemoEasing.smoothstep(HemoEasing.clamp01((taut - 0.55F) / 0.4F)) * alpha;
		if (braid <= 0.01F) return;
		Vec3 side = axis.cross(UP);
		side = side.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : side.normalize();
		Vec3 lift = side.cross(axis.normalize()).normalize();
		ribbonPath(p, v, spine, Vec3.ZERO, 0.11, BEAM, seed, TINT, Math.min(SHELL, braid * 0.15F));
		for (int strand = 0; strand < 3; strand++) {
			List<Vec3> twist = new ArrayList<>(19);
			for (int i = 0; i <= 18; i++) {
				double angle = i * 0.85 - time * 0.13 + strand * Math.PI * 2 / 3;
				twist.add(spine.get(i).add(side.scale(Math.cos(angle) * 0.05)).add(lift.scale(Math.sin(angle) * 0.05)));
			}
			ribbonPath(p, v, twist, Vec3.ZERO, STRAND, RIBBON, seed + strand, strand == 1 ? CLOT : TINT, braid * 0.65F);
		}
		for (int i = 0; i < 3; i++) {
			double t = ((time * 0.025 + i / 3.0 + seed * 0.1) % 1 + 1) % 1;
			card(p, v, from.lerp(to, t), right, up, 0.04, 0.055, FOCUS, seed + i, CORE, braid * 0.7F);
		}
	}

	/** A slipped iron: a short frayed length of chain hanging from it. */
	static void frayed(PoseStack p, VertexConsumer v, Vec3 iron, Vec3 right, Vec3 up, double time, int seed) {
		double sway = Math.sin(time * 0.07 + seed) * 0.08;
		List<Vec3> hang = List.of(iron, iron.add(sway * 0.5, -0.35, 0), iron.add(sway, -0.7, sway * 0.3));
		ribbonPath(p, v, hang, Vec3.ZERO, 0.012, 0.004, RIBBON, seed + 9, TINT, 0.45F);
	}

	/** The hold on the Riposte anatomy: a pale shell from palm to chest, three helical sinew strands, knots at each end. */
	static void hold(PoseStack p, VertexConsumer v, Vec3 palm, Vec3 chest, double time, int seed) {
		Vec3 axis = chest.subtract(palm);
		if (axis.lengthSqr() < 1e-6) return;
		float beat = 0.75F + 0.25F * (float) Math.pow(Math.max(0, Math.sin(time * 0.31)), 4);
		ribbonPath(p, v, List.of(palm, palm.lerp(chest, 0.5), chest), Vec3.ZERO, 0.09, BEAM, seed, TINT, 0.22F * beat);
		Vec3 side = axis.cross(UP);
		side = side.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : side.normalize();
		Vec3 lift = side.cross(axis.normalize()).normalize();
		double[] widths = {STRAND, 0.014, 0.01};
		for (int strand = 0; strand < 3; strand++) {
			List<Vec3> helix = new ArrayList<>(13);
			for (int i = 0; i <= 12; i++) {
				double t = i / 12.0;
				double angle = t * 6.0 - time * 0.2 + strand * Math.PI * 2 / 3;
				double radius = 0.045 * Math.sin(t * Math.PI);
				helix.add(palm.lerp(chest, t).add(side.scale(Math.cos(angle) * radius)).add(lift.scale(Math.sin(angle) * radius)));
			}
			ribbonPath(p, v, helix, Vec3.ZERO, widths[strand], widths[strand] * 0.5, RIBBON, seed + strand, TINT, 0.6F * beat);
		}
		card(p, v, palm, side, lift, 0.04, 0.04, FOCUS, seed + 5, CORE, 0.6F);
		card(p, v, chest, side, lift, 0.05, 0.05, FOCUS, seed + 6, CORE, 0.6F * beat);
	}

	/**
	 * Growth on a player: Swelling shows a few small white sores on arms and torso; Knotting adds short bone-white spurs
	 * at shoulders and knees, a faint ripple under the feet and three short root ribbons into the floor. In first person
	 * only the floor part shows.
	 */
	static void growth(PoseStack p, VertexConsumer v, Vec3 feet, float bodyYaw, Vec3 right, Vec3 up, double time, int seed,
			boolean knotting, boolean firstPerson) {
		double yaw = Math.toRadians(bodyYaw);
		Vec3 fwd = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw)), side = new Vec3(-fwd.z, 0, fwd.x);
		if (!firstPerson) {
			double[][] sores = {{0.32, 1.25, 0.05}, {-0.32, 1.1, 0.05}, {0.12, 1.0, 0.16}, {-0.1, 1.35, 0.16}, {0.3, 0.85, 0.0}};
			int count = knotting ? 5 : 3;
			for (int i = 0; i < count; i++) {
				Vec3 at = feet.add(side.scale(sores[i][0])).add(0, sores[i][1], 0).add(fwd.scale(sores[i][2]));
				float flicker = 0.75F + 0.25F * (float) Math.sin(time * 0.17 + i + seed);
				card(p, v, at, right, up, 0.035, 0.035, FOCUS, seed + i, TINT, 0.35F * flicker);
			}
		}
		if (!knotting) return;
		if (!firstPerson) {
			for (int s = -1; s <= 1; s += 2) {
				Vec3 shoulder = feet.add(side.scale(s * 0.36)).add(0, 1.42, 0);
				Vec3 knee = feet.add(side.scale(s * 0.13)).add(0, 0.5, 0).add(fwd.scale(0.08));
				ribbonPath(p, v, List.of(shoulder, shoulder.add(side.scale(s * 0.12)).add(0, 0.12, 0)), Vec3.ZERO,
						0.018, 0.004, SLASH, seed + 20 + s, TINT, 0.5F);
				ribbonPath(p, v, List.of(knee, knee.add(fwd.scale(0.12)).add(0, 0.04, 0)), Vec3.ZERO, 0.016, 0.004, SLASH,
						seed + 30 + s, TINT, 0.45F);
			}
		}
		ground(p, v, feet.add(0, 0.03, 0), 0.6, RIPPLE, seed + 40, TINT, 0.12F);
		for (int i = 0; i < 3; i++) {
			double a = i * Math.PI * 2 / 3 + seed;
			Vec3 root = feet.add(Math.cos(a) * 0.12, 0.25, Math.sin(a) * 0.12);
			ribbonPath(p, v, List.of(root, root.add(Math.cos(a) * 0.18, -0.24, Math.sin(a) * 0.18)), Vec3.ZERO,
					0.012, 0.004, RIBBON, seed + 41 + i, TINT, 0.4F);
		}
	}

	/** A fragment of her light: a white knot of nerve with orbiting shards; drawn in, it dims and draws together. */
	static void fragment(PoseStack p, VertexConsumer v, @Nullable VertexConsumer gold, Vec3 at, Vec3 right, Vec3 up,
			float age, int seed, float drawn) {
		float beat = 0.75F + 0.25F * (float) Math.sin(age * 0.13 + seed);
		double shrink = 1.0 - drawn * 0.5;
		// a faint halo and a heart large enough to find across the floor: a fragment is a target, not an accent
		card(p, v, at, right, up, 0.5 * shrink, 0.5 * shrink, LUX_SHELL, seed + 7, TINT, 0.2F * beat);
		card(p, v, at, right, up, 0.2 * shrink, 0.36 * shrink, FOCUS, seed, CORE, 0.95F);
		ribbonPath(p, v, List.of(at.add(0, -0.42 * shrink, 0), at, at.add(0, 0.48 * shrink, 0)), Vec3.ZERO,
				new double[]{0.006, STRAND, 0.004}, SLASH, seed, TINT, 0.85F);
		for (int i = 0; i < 3; i++) {
			double angle = age * 0.065 + i * Math.PI * 2 / 3 + seed;
			double r = 0.32 * shrink;
			Vec3 point = at.add(Math.cos(angle) * r, Math.sin(age * 0.1 + i) * 0.16, Math.sin(angle) * r);
			ribbonPath(p, v, List.of(point.add(0, -0.3, 0), point, point.add(0, 0.3, 0)), Vec3.ZERO,
					new double[]{0.006, STRAND, 0.003}, SLASH, seed + i, TINT, 0.7F * beat);
			if (gold != null) {
				// gold reads against her pale radiant stone where white does not: the impulses fire often enough to find her
				// fragments by across the floor
				float impulse = (float) Math.pow(Math.max(0, Math.sin(age * 0.32 - seed * 0.7 - i)), 2);
				card(p, gold, point, right, up, 0.05, 0.05, SPARK, seed + i + 4, NERVE, impulse * 0.85F);
			}
		}
	}

	// ── server cues ──────────────────────────────────────────────────

	/** The manipulation pass's cue hook: white only. */
	static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up,
			double time, float age, float alpha) {
		draw(packet, p, v, null, camera, right, up, time, age, alpha);
	}

	/** Short server-authored cues: wound closings, knots along a chain, captures, bursts, slips and flares. */
	static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, @Nullable VertexConsumer gold,
			Vec3 camera, Vec3 right, Vec3 up, double time, float age, float alpha) {
		int kind = packet.count();
		float fade = alpha * SeraphaeVisualRules.burstOpacity(age, packet.ticks());
		if (fade <= 0) return;
		int seed = Math.floorMod(packet.from().hashCode(), 113);
		double radius = packet.radius();
		Vec3 end = packet.to().subtract(packet.from());
		switch (kind) {
			case SeraphaeVisualRules.CLOSE -> {
				double size = Math.max(0.02, radius * (1 - age / packet.ticks()));
				card(p, v, Vec3.ZERO, right, up, size, size, FOCUS, seed, TINT, fade);
			}
			case SeraphaeVisualRules.KNOT, SeraphaeVisualRules.CAPTURE, SeraphaeVisualRules.WAKE -> {
				float travel = HemoEasing.smoothstep(age / Math.max(1.0F, packet.ticks() * 0.8F));
				Vec3 head = end.scale(travel);
				if (kind != SeraphaeVisualRules.KNOT) {
					chain(p, v, end.scale(Math.max(0, travel - 0.45)), head, right, up, time, seed, 0.9F, fade);
				}
				card(p, v, head, right, up, 0.08 * radius + 0.04, 0.1 * radius + 0.05, FOCUS, seed, CORE, fade);
				if (kind == SeraphaeVisualRules.KNOT) card(p, v, Vec3.ZERO, right, up, 0.12, 0.12, FOCUS, seed + 1, TINT, fade * (1 - travel));
			}
			case SeraphaeVisualRules.TRANSFUSION -> {
				double travel = HemoEasing.smoothstep(age / 12);
				Vec3 head = end.scale(travel), root = end.scale(Math.max(0, travel - 0.4));
				ribbonPath(p, v, List.of(root, root.lerp(head, 0.5).add(0, 0.06, 0), head), camera, STRAND, 0.006,
						RIBBON, seed, TINT, fade);
			}
			case SeraphaeVisualRules.SLIP -> {
				List<Vec3> whip = new ArrayList<>(13);
				for (int i = 0; i <= 12; i++) {
					double t = i / 12.0;
					whip.add(end.scale(t).add(right.scale(Math.sin(t * Math.PI * 2 + age * 0.6) * 0.6 * (1 - age / packet.ticks()))));
				}
				ribbonPath(p, v, whip, camera, STRAND, RIBBON, seed, TINT, fade * 0.8F);
				card(p, v, Vec3.ZERO, right, up, 0.18, 0.18, FOCUS, seed + 2, TINT, fade);
			}
			case SeraphaeVisualRules.FLARE -> {
				float reveal = HemoEasing.smoothstep(age / 6.0F);
				chain(p, v, Vec3.ZERO, end.scale(reveal), right, up, time * 2.0, seed, 1.0F, fade);
			}
			case SeraphaeVisualRules.RECOIL -> {
				ground(p, v, end.add(0, -0.05, 0), 0.25 + age * 0.04, RIPPLE, seed, TINT, fade * 0.4F);
				chain(p, v, Vec3.ZERO, end, right, up, time, seed, 1.0F, fade * 0.6F);
			}
			case SeraphaeVisualRules.PAD -> {
				ground(p, v, new Vec3(0, -0.05, 0), radius, POOL, seed, TINT, fade * 0.25F);
				ground(p, v, new Vec3(0, -0.04, 0), radius * (0.6 + age / packet.ticks() * 0.5), RIPPLE, seed, TINT, fade * 0.3F);
			}
			default -> burst(p, v, gold, kind, camera, right, up, time, age, fade, seed, radius, packet.ticks());
		}
	}

	/** A white ray burst (a hit inside an opening, a growth burst, a Fracture, her sealing), with gold crackle when given. */
	private static void burst(PoseStack p, VertexConsumer v, @Nullable VertexConsumer gold, int kind, Vec3 camera,
			Vec3 right, Vec3 up, double time, float age, float fade, int seed, double radius, int life) {
		boolean sealed = kind == SeraphaeVisualRules.SEALED;
		double grow = HemoEasing.easeOutCubic(age / 9.0);
		double size = radius * (sealed ? 1 - HemoEasing.smoothstep(age / 32.0) * 0.88 : 0.15 + grow * 0.85);
		double core = Math.max(0.04, 0.2 * radius * (1 - age / life));
		card(p, v, Vec3.ZERO, right, up, core, core, FOCUS, seed, CORE, fade * 0.8F);
		int rays = sealed ? 14 : 10;
		for (int i = 0; i < rays; i++) {
			double a = i * 2.39996 + seed;
			Vec3 ray = new Vec3(Math.cos(a), Math.sin(a * 1.3) * 0.65, Math.sin(a)).normalize();
			Vec3 tip = ray.scale(size);
			Vec3 inner = sealed ? tip.scale(0.55) : ray.scale(size * 0.2);
			ribbonPath(p, v, List.of(inner, inner.lerp(tip, 0.5), tip), camera, STRAND, 0.004, RIBBON, seed + i, TINT,
					fade * 0.7F);
		}
		if (gold != null && (kind == SeraphaeVisualRules.GROWTH_BURST || kind == SeraphaeVisualRules.FRACTURE_BURST)
				&& age < 6) {
			for (int i = 0; i < 4; i++) {
				double a = i * Math.PI / 2 + seed;
				Vec3 tip = new Vec3(Math.cos(a), -0.6, Math.sin(a)).scale(size * 0.8);
				ribbonPath(p, gold, List.of(Vec3.ZERO, tip.scale(0.5).add(0, 0.1, 0), tip), camera, 0.012, 0.004,
						CRACKLE, seed + 20 + i, NERVE, fade * (1 - age / 6.0F));
			}
		}
		if (!sealed) ground(p, v, new Vec3(0, -0.7, 0), Math.max(0.05, size), RIPPLE, seed, TINT, fade * 0.25F);
	}
}
