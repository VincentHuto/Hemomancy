package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.saint.CrimsonTitheRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Crimson Tithe (D-1 spec 3.1) drawn as Hemorath's power made the caster's, on his own iron and blood:
 * <ol>
 * <li>the chain ({@code TITHE_CHAIN}, keyed to the chained body): a translucent sheath of drawn blood along its sag,
 * opaque iron links in the solid pass, a fluid bolus stream running back to the caster's palm, knots at hook and
 * palm;</li>
 * <li>the vessel ({@code TITHE_VESSEL}, on the caster): blood climbing the chest with the fill, the ribs pressing out
 * when Brimming, drips off ribs and hand that spread into small pools, and an iron spur on the forearm or shoulder for
 * each iron set; overlay strength;</li>
 * <li>the Vessel Empties ({@code TITHE_RELEASE}): a star of lanes from the caster's feet, trenches that open and
 * flood with blood, iron spikes erupting near to far, and a ray burst from the chest. Timeline from
 * {@link CrimsonTitheRules}: emerge 0 to 3, spikes 3 to 10, flood 10 to 30, close 30 to 50.</li>
 * </ol>
 * White base tint, accents the Riposte pair (CORE, CLOT). No brass or copper: the iron is Hemorath's plate.
 */
final class CrimsonTitheGeometry {
	private static final int TINT = 0xFFFFFF, CORE = 0xFFD2C4, CLOT = 0x7A1020;
	private static final Vec3 UP = new Vec3(0, 1, 0);
	private static final int MAX_LINKS = 24;

	private CrimsonTitheGeometry() {
	}

	static boolean handles(Form form) {
		return form == Form.TITHE_CHAIN || form == Form.TITHE_VESSEL || form == Form.TITHE_RELEASE;
	}

	static void draw(ManipulationVisualPacket packet, CanonGiftDraw d) {
		if (d.opacity() <= 0) return;
		switch (packet.form()) {
			case TITHE_CHAIN -> chain(packet, d);
			case TITHE_VESSEL -> vessel(packet, d);
			case TITHE_RELEASE -> release(packet, d);
			default -> {
			}
		}
	}

	/** The chain from the caster's palm to the hook in the body, offsets from the chained body's feet. */
	private static void chain(ManipulationVisualPacket packet, CanonGiftDraw d) {
		if (d.caster() == null || d.source() == null) return;
		PoseStack p = d.poses();
		int seed = Math.floorMod(packet.entityId() * 31 + packet.count(), 113);
		Vec3 hook = new Vec3(0, d.source().getBbHeight() * 0.55D, 0);
		Vec3 palm = d.frame().hand();
		double length = hook.distanceTo(palm);
		if (length < 0.3D) return;
		double sag = Math.min(0.6D, length * 0.06D);
		int samples = 16;
		List<Vec3> path = new ArrayList<>(samples + 1);
		for (int i = 0; i <= samples; i++) {
			double t = i / (double) samples;
			path.add(palm.lerp(hook, t).add(0, -sag * 4.0D * t * (1.0D - t), 0));
		}
		// First person keeps the nearest stretch of chain off the screen.
		double near = d.frame().firstPerson() ? 0.4D : 0.0D;
		if (d.solids()) {
			int links = Math.max(3, Math.min(MAX_LINKS, (int) Math.round(length / 0.24D)));
			for (int i = 0; i < links; i++) {
				double t = (i + 0.5D) / links;
				if (t < near) continue;
				Vec3 at = sample(path, t);
				Vec3 tangent = sample(path, Math.min(1, t + 0.02)).subtract(sample(path, Math.max(0, t - 0.02))).normalize();
				Vec3 side = tangent.cross(UP);
				side = side.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : side.normalize();
				Vec3 wide = i % 2 == 0 ? side : tangent.cross(side).normalize();
				HemorathReliquaryGeometry.link(p, d.iron(), at, tangent.scale(0.1D), wide.scale(0.06D), seed + i);
			}
			return;
		}
		float a = d.opacity();
		List<Vec3> shown = near > 0 ? path.subList((int) Math.round(near * samples), path.size()) : path;
		// 1. shell: a sheath of drawn blood round the chain, wisps trailing back toward the body
		ribbonPath(p, d.clot(), shown, d.camera(), 0.16D, 0.12D, WISP, seed, TINT, a * 0.28F);
		for (int i = 1; i < 4; i++) {
			double t = Math.max(near, i / 4.0D);
			card(p, d.clot(), sample(path, t), d.right(), d.up(), 0.12D, 0.16D, WISP, seed + 10 + i, TINT, a * 0.18F);
		}
		// 2. sinew: each pulse sends a bolus of blood back to the caster along the chain
		Vec3 from = hook, to = near > 0 ? sample(path, near) : palm;
		HemorathBloodGeometry.stream(p, d.blood(), from, to, d.camera(), d.right(), d.up(), d.time(), seed, 1.0D, -sag * 0.8D,
				0.055D, 0.32D, a * 0.85F);
		// 3. knots: a clot at the wound where the hook bites, a pale knot at the palm
		card(p, d.blood(), hook, d.right(), d.up(), 0.16D, 0.18D, FOCUS, seed + 20, CLOT, a * 0.9F);
		card(p, d.blood(), hook, d.right(), d.up(), 0.07D, 0.08D, FOCUS, seed + 21, CORE, a * 0.7F);
		if (near <= 0) card(p, d.blood(), palm, d.right(), d.up(), 0.07D, 0.07D, FOCUS, seed + 22, CORE, a * 0.6F);
	}

	/** The vessel in the caster's chest, offsets from the caster's feet. */
	private static void vessel(ManipulationVisualPacket packet, CanonGiftDraw d) {
		PoseStack p = d.poses();
		CasterFrame f = d.frame();
		double fill = Math.max(0, Math.min(1, packet.radius()));
		int iron = Math.floorMod(packet.count() - 1, 8);
		boolean brimming = packet.count() - 1 >= 8;
		int seed = Math.floorMod(packet.entityId(), 113);
		Vec3 forward = f.forward(), side = f.right();
		Vec3 chest = UP.scale(f.chest()).add(forward.scale(0.1D * f.scale()));
		if (d.solids()) {
			// an iron spur for each iron set: forearm first, then the shoulders
			for (int i = 0; i < iron; i++) {
				Vec3 foot = i < 2 ? f.hand().lerp(chest, 0.25D + 0.2D * i)
						: chest.add(side.scale((i == 2 ? 1 : -1) * 0.3D * f.scale())).add(0, 0.2D * f.scale(), 0);
				if (f.firstPerson() && i >= 2) continue;
				Vec3 out = i < 2 ? side : side.scale(i == 2 ? 1 : -1);
				var spike = new HemorathFissureRules.Spike(0, 0, (0.16D + 0.04D * i) * (brimming ? 1.25D : 1.0D) * f.scale(),
						0.035D * f.scale(), out.x * 0.9D, out.z * 0.9D, 5, seed + i);
				HemorathIronGeometry.spike(p, d.iron(), foot, spike, 1.0D, 1.0F, 0.0D, HemorathIronGeometry.IRON, seed + i * 7);
			}
			return;
		}
		float a = d.opacity();
		if (!f.firstPerson()) {
			// the blood mass climbs the chest with the fill: narrow strands in a soft shell, a beating knot at the heart
			double top = -0.35D + 0.75D * fill;
			int strands = 2 + (int) Math.round(fill * 3);
			for (int i = 0; i < strands; i++) {
				double s = i % 2 == 0 ? -1 : 1;
				Vec3 low = chest.add(side.scale(s * 0.12D * f.scale())).add(0, -0.4D * f.scale(), 0);
				Vec3 mid = chest.add(side.scale(s * (0.16D + 0.06D * fill) * f.scale())).add(0, (top - 0.2D) * 0.5D * f.scale(), 0);
				Vec3 high = chest.add(side.scale(s * 0.05D * f.scale())).add(0, top * f.scale(), 0);
				List<Vec3> path = List.of(low, mid, high);
				ribbonPath(p, d.clot(), path, d.camera(), 0.07D, 0.03D, WISP, seed + i, TINT, a * (0.12F + 0.12F * (float) fill));
				ribbonPath(p, d.blood(), path, d.camera(), 0.02D, 0.008D, RIBBON, seed + i, i % 2 == 0 ? TINT : CLOT,
						a * (0.35F + 0.3F * (float) fill));
			}
			double beat = 0.5D + 0.5D * Math.sin(d.time() * (0.25D + 0.35D * fill));
			card(p, d.blood(), chest, d.right(), d.up(), 0.05D + 0.08D * fill, 0.07D + 0.1D * fill, FOCUS, seed, CORE,
					a * (float) (0.25D + 0.3D * beat * fill));
			if (brimming) {
				// the ribs press out against what he cannot hold
				for (int s = -1; s <= 1; s += 2)
					for (int rib = 0; rib < 3; rib++) {
						double y = -0.25D + rib * 0.17D;
						Vec3 back = chest.add(side.scale(s * 0.08D * f.scale())).add(0, y * f.scale(), 0);
						Vec3 bow = chest.add(side.scale(s * 0.24D * f.scale())).add(forward.scale(0.06D)).add(0, (y - 0.04D) * f.scale(), 0);
						Vec3 front = chest.add(side.scale(s * 0.14D * f.scale())).add(forward.scale(0.12D)).add(0, (y - 0.09D) * f.scale(), 0);
						ribbonPath(p, d.blood(), List.of(back, bow, front), d.camera(), 0.012D, 0.006D, RIBBON, seed + 30 + rib, CLOT,
								a * 0.7F);
					}
			}
		}
		// drips fall from the ribs and the hand at the leak rate and spread into pools that fade
		int drips = 2 + (int) Math.round(fill * 4);
		double rate = 0.04D + 0.06D * fill;
		for (int i = 0; i < drips; i++) {
			if (f.firstPerson() && i % 2 == 1) continue;
			Vec3 source = i % 2 == 0 ? f.hand() : chest.add(side.scale((i % 4 == 1 ? 1 : -1) * 0.22D * f.scale())).add(0, -0.25D, 0);
			double phase = ((d.time() * rate + i * 0.37D + seed * 0.01D) % 1.0D + 1.0D) % 1.0D;
			double fall = phase * phase;
			Vec3 drop = source.add(0, -(source.y - 0.02D) * fall, 0);
			ribbonPath(p, d.blood(), List.of(drop.add(0, 0.12D, 0), drop), d.camera(), 0.008D, 0.022D, RIBBON, seed + 40 + i, TINT,
					a * (float) (0.8D - 0.5D * phase));
			card(p, d.blood(), drop, d.right(), d.up(), 0.03D, 0.035D, FOCUS, seed + 50 + i, TINT, a * (float) (1.0D - phase) * 0.8F);
			Vec3 pool = new Vec3(source.x, 0.02D, source.z);
			ground(p, d.blood(), pool, 0.1D + 0.22D * phase, POOL, seed + 60 + i, TINT, a * (float) (1.0D - phase) * 0.45F);
		}
	}

	/** The Vessel Empties, offsets from the caster's feet: a star of flooded trenches and iron. */
	private static void release(ManipulationVisualPacket packet, CanonGiftDraw d) {
		PoseStack p = d.poses();
		int lanes = Math.max(1, Math.min(CrimsonTitheRules.MAX_LANES, packet.count()));
		double length = Math.max(1.0D, packet.radius());
		double fill = Math.max(0, Math.min(1, (length - 3.0D) / 7.0D));
		Vec3 toward = packet.to().subtract(packet.from());
		double start = Math.atan2(toward.z, toward.x);
		int seed = Math.floorMod(packet.from().hashCode(), 113);
		float age = d.age();
		float fade = d.opacity() * (1.0F - (float) HemoEasing.smoothstep((age - 40.0F) / 10.0F));
		if (fade <= 0) return;
		double open = CrimsonTitheRules.trenchOpen(age), flood = CrimsonTitheRules.flood(age);
		for (int i = 0; i < lanes; i++) {
			double angle = CrimsonTitheRules.laneAngle(start, i, lanes);
			Vec3 dir = new Vec3(Math.cos(angle), 0, Math.sin(angle));
			Vec3 across = new Vec3(-dir.z, 0, dir.x);
			double reach = length * HemoEasing.easeOutCubic(Math.min(1.0D, age / (double) CrimsonTitheRules.EMERGE_END));
			if (d.solids()) {
				// iron spikes erupt near to far along the lane and sink as it closes
				int steps = Math.max(1, (int) (length / 1.4D));
				for (int k = 1; k <= steps; k++) {
					double frac = k / (double) steps;
					float startAt = (float) (CrimsonTitheRules.EMERGE_END
							+ (CrimsonTitheRules.SPIKES_END - CrimsonTitheRules.EMERGE_END) * frac * 0.7D);
					float local = age - startAt;
					if (local <= 0) continue;
					double offset = (HemorathFissureRules.unit(seed + i, k) - 0.5D) * 0.5D;
					Vec3 foot = dir.scale(length * frac - 0.3D).add(across.scale(offset)).add(0, -0.05D, 0);
					var spikes = HemorathFissureRules.cluster(seed + i * 31 + k, 0.45D + 0.5D * fill * (1.0D - frac * 0.5D), 2);
					HemorathIronGeometry.cluster(p, d.iron(), foot, spikes, local, CrimsonTitheRules.FLOOD_END - startAt, d.time(),
							HemorathIronGeometry.IRON, seed + i * 13 + k);
				}
				continue;
			}
			if (open <= 0.01D) continue;
			double half = CrimsonTitheRules.LANE_HALF_WIDTH * open;
			int segments = Math.max(1, (int) Math.ceil(reach));
			for (int k = 0; k < segments; k++) {
				double s0 = reach * k / segments, s1 = reach * (k + 1) / segments;
				double taper0 = 1.0D - 0.45D * s0 / length, taper1 = 1.0D - 0.45D * s1 / length;
				Vec3 a0 = dir.scale(s0), a1 = dir.scale(s1);
				// the trench's dark lips and bed
				floor(p, d.clot(), a0, a1, across, half * taper0 * 1.15D, half * taper1 * 1.15D, 0.025D, POOL, seed + i * 17 + k, CLOT,
						fade * 0.7F);
				// blood floods back in and stands, a lit core down its middle
				if (flood > 0.01D) {
					floor(p, d.blood(), a0, a1, across, half * taper0 * flood, half * taper1 * flood, 0.032D, POOL,
							seed + i * 19 + k, TINT, fade * 0.85F);
					floor(p, d.blood(), a0, a1, across, half * taper0 * 0.25D * flood, half * taper1 * 0.25D * flood, 0.036D, RIBBON,
							seed + i * 23 + k, CORE, fade * 0.5F);
				}
			}
			// the lane's running head throws blood ahead of it while it emerges
			if (age < CrimsonTitheRules.SPIKES_END + 4)
				HemorathBloodGeometry.spatter(p, d.blood(), dir.scale(reach).add(0, 0.1D, 0), dir, d.camera(), d.right(), d.up(), age,
						seed + i, 1.0D, fade * 0.8F);
		}
		if (!d.solids() && age < 10.0F) {
			// a Riposte-style ray burst from the chest as the vessel gives way
			Vec3 chest = new Vec3(0, 1.15D, 0);
			float flash = 1.0F - age / 10.0F;
			card(p, d.blood(), chest, d.right(), d.up(), 0.3D + 0.3D * flash, 0.3D + 0.3D * flash, FOCUS, seed, CORE, fade * flash);
			card(p, d.clot(), chest, d.right(), d.up(), 0.55D, 0.6D, WISP, seed + 1, CLOT, fade * flash * 0.4F);
			double grow = HemoEasing.easeOutCubic(age / 6.0D);
			for (int i = 0; i < lanes; i++) {
				double angle = CrimsonTitheRules.laneAngle(start, i, lanes);
				Vec3 out = new Vec3(Math.cos(angle), 0.25D, Math.sin(angle)).normalize();
				Vec3 tip = chest.add(out.scale(0.4D + 1.4D * grow));
				ribbonPath(p, d.blood(), List.of(chest.add(out.scale(0.2D)), tip), d.camera(), 0.06D, 0.008D, SLASH, seed + 70 + i, TINT,
						fade * flash);
			}
		}
	}

	/** A flat strip on the floor from {@code a} to {@code b}, its half-width running {@code w0} to {@code w1}. */
	private static void floor(PoseStack p, VertexConsumer v, Vec3 a, Vec3 b, Vec3 across, double w0, double w1, double lift,
			int shape, int seed, int color, float alpha) {
		Vec3 up = new Vec3(0, lift, 0);
		surface(p, v, a.add(across.scale(-w0)).add(up), b.add(across.scale(-w1)).add(up), b.add(across.scale(w1)).add(up),
				a.add(across.scale(w0)).add(up), shape, seed, color, alpha);
	}

	private static Vec3 sample(List<Vec3> path, double t) {
		double at = Math.max(0, Math.min(1, t)) * (path.size() - 1);
		int i = Math.min(path.size() - 2, (int) at);
		return path.get(i).lerp(path.get(i + 1), at - i);
	}
}
