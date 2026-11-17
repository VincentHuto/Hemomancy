package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.saint.UnclosingEyeRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * The Unclosing Eye (D-1 spec 3.2) drawn as Seraphae's own eye opened in the caster's chest, on the Lux material with
 * her bone for the Knotting spurs:
 * <ol>
 * <li>the aperture ({@code EYE_CHARGE}): swollen lids parting over the charge round a bright core, a soft glow on the
 * chest at overlay strength;</li>
 * <li>the beam ({@code EYE_BEAM}): an outer sheath widening with range and wisps trailing against the sweep, the dense
 * white core, a gold nerve bead running out with every pulse, a white splash where it ends and a shallow lit furrow
 * where it runs low over the floor;</li>
 * <li>the Lapse and the slit ({@code EYE_APERTURE}): the eye folding to a flickering slit that never closes;</li>
 * <li>growth ({@code EYE_GROWTH}) on a struck body: luminous sores by stage, bone spurs at shoulders and knees from
 * Knotting, and the Burst's white rays with a gold crackle along the limbs.</li>
 * </ol>
 * White base tint, accents hot pale core and nerve gold; gold is only nerve light. First person: the beam starts below
 * the crosshair, its sheath fades within two blocks of the camera, and the aperture is not drawn.
 */
final class UnclosingEyeGeometry {
	private static final int TINT = 0xFFFAEF, CORE = 0xFFF4E8, GOLD = 0xFFD27A;
	private static final Vec3 UP = new Vec3(0, 1, 0);

	private UnclosingEyeGeometry() {
	}

	static boolean handles(Form form) {
		return form == Form.EYE_CHARGE || form == Form.EYE_BEAM || form == Form.EYE_APERTURE || form == Form.EYE_GROWTH;
	}

	static void draw(ManipulationVisualPacket packet, CanonGiftDraw d) {
		if (d.opacity() <= 0) return;
		switch (packet.form()) {
			case EYE_CHARGE -> {
				if (!d.solids() && !d.frame().firstPerson())
					aperture(d, chestOf(d).subtract(UP.scale(eyeHeight(d))), Math.max(0, Math.min(1, packet.radius())), 0);
			}
			case EYE_BEAM -> beam(packet, d);
			case EYE_APERTURE -> {
				if (!d.solids() && !d.frame().firstPerson()) aperture(d, chestOf(d), 1.0F, packet.count());
			}
			case EYE_GROWTH -> {
				if (packet.count() >= 9) burst(packet, d);
				else growth(packet, d);
			}
			default -> {
			}
		}
	}

	private static Vec3 chestOf(CanonGiftDraw d) {
		return UP.scale(d.frame().chest()).add(d.frame().forward().scale(0.14D * d.frame().scale()));
	}

	/** The charge cue is anchored at the eyes, this high above the feet. */
	private static double eyeHeight(CanonGiftDraw d) {
		return (d.frame().crouching() ? 1.27D : 1.62D) * d.frame().scale();
	}

	/**
	 * The eye on the chest: lids of swollen tissue that part with {@code open} round a bright core. {@code mode} 1 is
	 * the Lapse (it folds to a slit), 2 the slit that stays.
	 */
	private static void aperture(CanonGiftDraw d, Vec3 chest, float open, int mode) {
		PoseStack p = d.poses();
		Vec3 across = d.frame().right(), up = UP;
		float a = d.opacity();
		double width = 0.17D * d.frame().scale();
		double gape;
		if (mode == 1) {
			double fold = HemoEasing.smoothstep(d.age() / 8.0D);
			gape = 0.09D * (1.0D - 0.8D * fold);
		} else if (mode == 2) {
			gape = 0.016D + 0.008D * Math.sin(d.time() * 0.9D);
		} else {
			gape = 0.1D * HemoEasing.easeOutCubic(open);
		}
		double flicker = mode == 0 ? 1.0D : 0.75D + 0.25D * Math.sin(d.time() * 1.7D);
		card(p, d.lux(), chest, d.right(), d.up(), 0.32D * d.frame().scale(), 0.34D * d.frame().scale(), LUX_SHELL, 31, TINT,
				a * 0.22F * (float) flicker);
		// the core, bright through the parted lids
		card(p, d.lux(), chest, across, up, width * 0.8D, Math.max(0.01D, gape * 0.9D), EYE, 33, CORE, a * (float) (0.9D * flicker));
		// two lids of swollen tissue, bowed apart by the gape
		for (int s = -1; s <= 1; s += 2) {
			List<Vec3> lid = new ArrayList<>(7);
			for (int i = 0; i <= 6; i++) {
				double t = i / 6.0D, x = (t - 0.5D) * 2.0D;
				lid.add(chest.add(across.scale(x * width)).add(up.scale(s * gape * (1.0D - x * x) + s * 0.012D)));
			}
			ribbonPath(p, d.lux(), lid, d.camera(), 0.03D, 0.03D, RIBBON, 35 + s, TINT, a * 0.75F);
		}
	}

	/** The beam from the caster's chest to where it ends, swept by the server. */
	private static void beam(ManipulationVisualPacket packet, CanonGiftDraw d) {
		if (d.solids()) return;
		PoseStack p = d.poses();
		float a = d.opacity();
		boolean anchored = packet.entityId() >= 0;
		Vec3 start = anchored ? chestOf(d) : Vec3.ZERO;
		Vec3 end = packet.to().subtract(packet.from());
		if (anchored && d.source() != null) end = packet.to().subtract(d.anchor());
		if (d.frame().firstPerson()) {
			// below the crosshair, so the beam leaves from under the view rather than walling it off
			Vec3 look = d.frame().look().normalize();
			start = d.camera().add(look.scale(0.7D)).add(0, -0.32D, 0);
		}
		Vec3 axis = end.subtract(start);
		double length = axis.length();
		if (length < 0.2D) return;
		Vec3 dir = axis.scale(1.0D / length);
		int seed = Math.floorMod(packet.entityId() >= 0 ? packet.entityId() : packet.from().hashCode(), 113);
		float charge = Math.max(0, Math.min(1, packet.radius()));
		double open = HemoEasing.easeOutCubic(Math.min(1.0D, d.age() / 4.0D));
		double grown = length * open;
		Vec3 tip = start.add(dir.scale(grown));
		// 1. shell: the outer sheath widening with range, faded near the camera so the view is never walled off
		double sheathFrom = 0;
		while (sheathFrom < grown && start.add(dir.scale(sheathFrom)).distanceTo(d.camera()) < 2.0D) sheathFrom += 0.25D;
		if (sheathFrom < grown) {
			int n = 10;
			List<Vec3> sheath = new ArrayList<>(n + 1);
			double[] widths = new double[n + 1];
			for (int i = 0; i <= n; i++) {
				double t = sheathFrom + (grown - sheathFrom) * i / n;
				sheath.add(start.add(dir.scale(t)));
				widths[i] = (0.14D + 0.035D * t) * (0.85D + 0.3D * charge);
			}
			ribbonPath(p, d.lux(), sheath, d.camera(), widths, WISP, seed, TINT, a * 0.28F);
			for (int i = 1; i <= 4; i++) {
				double t = sheathFrom + (grown - sheathFrom) * i / 5.0D;
				double drift = Math.sin(d.time() * 0.3D + i) * 0.15D;
				card(p, d.lux(), start.add(dir.scale(t)).add(0, drift, 0), d.right(), d.up(), 0.2D + 0.03D * t, 0.16D + 0.02D * t,
						WISP, seed + 10 + i, TINT, a * 0.16F);
			}
		}
		// 2. sinew: the dense white core, and a gold nerve bead out along it with each pulse
		double core = 0.13D + 0.05D * charge;
		ribbon(p, d.lux(), start, tip, d.camera(), core, WHITE_BEAM, seed, TINT, a, 0, 1);
		ribbon(p, d.lux(), start, tip, d.camera(), core * 0.35D, RIBBON, seed + 1, CORE, a * 0.9F, 0, 1);
		// offset sheets and soft cross-sections keep the beam whole when it runs straight away from the camera
		Vec3 sheetSide = VisceralGeometry.side(dir), lift = dir.cross(sheetSide).normalize();
		for (int i = 0; i < 4; i++) {
			double angle = i * Math.PI / 2;
			Vec3 offset = sheetSide.scale(Math.cos(angle) * core * 0.6D).add(lift.scale(Math.sin(angle) * core * 0.6D));
			ribbon(p, d.lux(), start.add(offset), tip.add(offset), d.camera(), core * 0.4D, WHITE_BEAM, seed + 5 + i, TINT, a * 0.4F, 0, 1);
		}
		for (int i = 1; i <= 5; i++) {
			Vec3 station = start.add(dir.scale(grown * i / 5.0D));
			if (station.distanceTo(d.camera()) < 2.0D) continue;
			card(p, d.lux(), station, d.right(), d.up(), core * 1.6D, core * 1.6D, FOCUS, seed + 40 + i, TINT, a * 0.35F);
		}
		double bead = (d.age() % UnclosingEyeRules.PULSE_TICKS) / UnclosingEyeRules.PULSE_TICKS;
		if (bead * length <= grown)
			card(p, d.gold(), start.add(dir.scale(bead * length)), d.right(), d.up(), 0.09D, 0.09D, FOCUS, seed + 2, GOLD,
					a * (float) (1.0D - bead * 0.5D));
		float flare = (float) Math.max(0, 1.0D - (d.age() % UnclosingEyeRules.PULSE_TICKS) / 2.0D);
		// 3. knots: the source at the chest
		if (!d.frame().firstPerson())
			card(p, d.lux(), start, d.right(), d.up(), 0.16D + 0.06D * flare, 0.16D + 0.06D * flare, FOCUS, seed + 3, CORE, a * 0.9F);
		// 4. business end: a white splash with radiating rays where the beam stops
		if (open >= 0.99D) {
			card(p, d.lux(), tip, d.right(), d.up(), 0.35D + 0.15D * flare, 0.35D + 0.15D * flare, FOCUS, seed + 4, TINT, a * 0.85F);
			for (int i = 0; i < 6; i++) {
				double angle = i * Math.PI / 3 + d.time() * 0.05D;
				Vec3 ray = d.right().scale(Math.cos(angle)).add(d.up().scale(Math.sin(angle))).subtract(dir.scale(0.4D)).normalize();
				ribbonPath(p, d.lux(), List.of(tip, tip.add(ray.scale(0.5D + 0.3D * flare))), d.camera(), 0.035D, 0.004D, SLASH,
						seed + 20 + i, CORE, a * 0.7F);
			}
			// the floor cut open in a shallow furrow under a low sweep, milky light standing in it
			if (anchored && tip.y < 0.5D) {
				Vec3 flat = new Vec3(dir.x, 0, dir.z);
				if (flat.lengthSqr() > 1e-4) {
					flat = flat.normalize();
					Vec3 across = new Vec3(-flat.z, 0, flat.x);
					Vec3 b = new Vec3(tip.x, tip.y + 0.03D, tip.z), f0 = b.subtract(flat.scale(1.6D));
					double w = 0.18D + 0.1D * charge;
					surface(p, d.lux(), f0.add(across.scale(-w * 0.5D)), b.add(across.scale(-w)), b.add(across.scale(w)),
							f0.add(across.scale(w * 0.5D)), RIPPLE, seed + 30, TINT, a * 0.45F);
				}
			}
		}
	}

	/** Growth on a struck body, offsets from its feet: luminous sores, and spurs of bone from Knotting. */
	private static void growth(ManipulationVisualPacket packet, CanonGiftDraw d) {
		if (d.source() == null) return;
		PoseStack p = d.poses();
		int stage = packet.count();
		double grown = Math.max(0, Math.min(1, packet.radius()));
		double height = d.source().getBbHeight(), half = d.source().getBbWidth() * 0.5D;
		int seed = Math.floorMod(packet.entityId(), 113);
		float a = d.opacity();
		if (d.solids()) {
			if (stage < 2) return;
			for (int s = -1; s <= 1; s += 2) {
				for (int j = 0; j < 2; j++) {
					double y = j == 0 ? height * 0.78D : height * 0.3D;
					Vec3 foot = d.frame().right().scale(s * half * 0.9D).add(0, y, 0);
					var spurs = SeraphaeOvergrowthRules.cluster(seed + s * 7 + j, 0.25D + 0.2D * grown, 2);
					SeraphaeBoneGeometry.cluster(p, d.bone(), foot, spurs, 10.0F, 1000.0F, d.time(), SeraphaeBoneGeometry.SPUR,
							seed + j * 5 + s);
				}
			}
			return;
		}
		int sores = 2 + stage * 2;
		for (int i = 0; i < sores; i++) {
			double angle = i * 2.39996D + seed;
			double y = height * (0.25D + 0.6D * ((i * 0.37D) % 1.0D));
			Vec3 at = new Vec3(Math.cos(angle) * half * 1.02D, y, Math.sin(angle) * half * 1.02D);
			double pulse = 0.85D + 0.15D * Math.sin(d.time() * 0.4D + i);
			double size = (0.06D + 0.05D * grown) * pulse;
			card(p, d.lux(), at, d.right(), d.up(), size, size, FOCUS, seed + i, i % 3 == 0 ? CORE : TINT, a * 0.4F);
		}
	}

	/** A Burst: white rays from the body's middle and a gold crackle down the limbs. Offsets from the body's feet. */
	private static void burst(ManipulationVisualPacket packet, CanonGiftDraw d) {
		if (d.solids()) return;
		PoseStack p = d.poses();
		double height = Math.max(0.5D, packet.radius());
		Vec3 middle = new Vec3(0, height * 0.55D, 0);
		int seed = Math.floorMod(packet.from().hashCode(), 113);
		float age = d.age();
		float flash = 1.0F - HemoEasing.clamp01(age / 6.0F);
		float fade = d.opacity() * (1.0F - HemoEasing.clamp01((age - 8.0F) / 12.0F));
		if (fade <= 0) return;
		double grow = HemoEasing.easeOutCubic(Math.min(1.0D, age / 5.0D));
		card(p, d.lux(), middle, d.right(), d.up(), 0.4D + 0.5D * flash, 0.4D + 0.5D * flash, FOCUS, seed, TINT, fade * Math.max(flash, 0.3F));
		for (int i = 0; i < 10; i++) {
			double a = i * 2.39996D + seed;
			Vec3 out = new Vec3(Math.cos(a), Math.sin(a * 1.3D) * 0.7D, Math.sin(a)).normalize();
			ribbonPath(p, d.lux(), List.of(middle.add(out.scale(0.15D)), middle.add(out.scale(0.4D + 1.3D * grow))), d.camera(),
					0.07D, 0.006D, SLASH, seed + 10 + i, i % 2 == 0 ? CORE : TINT, fade);
		}
		// the gold nerve crackle runs down the limbs, jagged and brief
		for (int limb = 0; limb < 4; limb++) {
			double side = limb % 2 == 0 ? -1 : 1;
			boolean leg = limb >= 2;
			List<Vec3> path = new ArrayList<>(6);
			for (int k = 0; k <= 5; k++) {
				double t = k / 5.0D;
				double jag = Math.sin(d.time() * 1.9D + k * 2.1D + limb) * 0.06D;
				double y = leg ? height * (0.45D - 0.42D * t) : height * (0.75D - 0.3D * t);
				path.add(new Vec3(side * (0.18D + (leg ? 0.02D : 0.15D) * t) + jag, y, jag));
			}
			ribbonPath(p, d.gold(), path, d.camera(), 0.025D, 0.012D, RIBBON, seed + 30 + limb, GOLD, fade * 0.85F);
		}
	}
}
