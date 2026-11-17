package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.saint.BloomOfRotRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Bloom of Rot (D-1 spec 3.3) drawn as Putriciel's rot made the caster's, on their own materials:
 * <ol>
 * <li>the field ({@code BLOOM_FIELD}, keyed to the caster, centred where it was sown): a translucent mould sheet over the
 * real floor cells with a pale cream rim at its growing edge, the colony's mass in the solid pass (shelf rosettes in
 * about one cell of six, spore sacs near the sown palm), and the Kindling as cream cracks brightening and heat shimmer
 * over the field; no red before the fire;</li>
 * <li>the Ignition ({@code BLOOM_IGNITION}, at the centre): a ring of Flammeus tongues running out from the caster over
 * 12 ticks, the floor charring behind it in curled plates to packed ash that lingers to tick 160;</li>
 * <li>colonisation on a body ({@code BLOOM_COLONY}): bracket growths at body points by stage, climbing from the feet;
 * overlay strength.</li>
 * </ol>
 * White base tint, accents CREAM and the rot's CLOT. First person: the colony within 1.5 blocks of the viewer's feet is
 * drawn lower and thinner so the floor stays clear.
 */
final class BloomOfRotGeometry {
	private static final int TINT = 0xFFFFFF, CREAM = 0xE8D8A0, CLOT = 0x2A2A14, ASH = 0x3A3530;

	private BloomOfRotGeometry() {
	}

	static boolean handles(Form form) {
		return form == Form.BLOOM_FIELD || form == Form.BLOOM_IGNITION || form == Form.BLOOM_COLONY;
	}

	static void draw(ManipulationVisualPacket packet, CanonGiftDraw d) {
		if (d.opacity() <= 0) return;
		switch (packet.form()) {
			case BLOOM_FIELD -> field(packet, d);
			case BLOOM_IGNITION -> ignition(packet, d);
			case BLOOM_COLONY -> colony(packet, d);
			default -> {
			}
		}
	}

	/** The sown field round its centre, offsets from the cue's anchor (the caster's feet). */
	private static void field(ManipulationVisualPacket packet, CanonGiftDraw d) {
		PoseStack p = d.poses();
		Vec3 centre = packet.to().subtract(d.anchor());
		float age = d.age();
		double radius = BloomOfRotRules.radius(age);
		double kindling = Math.max(0, Math.min(1, packet.radius()));
		int seed = MaterialQuads.seedTile(Math.floorMod(packet.to().hashCode(), 1 << 20));
		float a = d.opacity();
		List<Vec3> cells = cells(packet.to(), d.anchor());
		Vec3 viewer = d.frame().firstPerson() ? Vec3.ZERO : null;
		if (d.solids()) {
			// the colony's mass: shelf rosettes in about one cell of six, sacs near the sown palm
			var growth = d.growth();
			for (int i = 0; i < cells.size(); i++) {
				Vec3 cell = cells.get(i);
				double out = cell.subtract(centre).horizontalDistance();
				if (out > radius || i % 6 != 2) continue;
				if (viewer != null && cell.horizontalDistanceSqr() < 2.25D) continue;
				float local = (float) (age - out / radius * BloomOfRotRules.GROW_TICKS);
				int state = PutricielGrowthGeometry.state(0.0F, 0.35F, (float) (0.15D + 0.4D * kindling));
				double scale = 0.3D + 0.3D * PutricielBloomRules.unit(seed + i, 7);
				PutricielGrowthGeometry.rosette(p, growth, cell, PutricielBloomRules.rosette(seed + i, scale, 3 + i % 3), local, 1.0E6F,
						d.time(), state, seed + i * 7);
			}
			for (int i = 0; i < 3; i++) {
				double angle = i * 2.1D + seed;
				Vec3 foot = centre.add(Math.cos(angle) * 0.6D, 0.02D, Math.sin(angle) * 0.6D);
				float swell = (float) HemoEasing.clamp01((age - 6.0D - i * 3.0D) / 20.0D);
				PutricielGrowthGeometry.sac(p, growth, foot, 0.12D, 0.22D, swell, 1.0F, PutricielGrowthGeometry.SAC_SKIN, seed + 40 + i);
			}
			return;
		}
		// 1. shell: the mould sheet over each reached floor cell, thinned at the viewer's own feet in first person
		for (int i = 0; i < cells.size(); i++) {
			Vec3 cell = cells.get(i);
			double out = cell.subtract(centre).horizontalDistance();
			if (out > radius) continue;
			double edge = HemoEasing.clamp01((radius - out) / 1.2D);
			float near = viewer != null && cell.horizontalDistanceSqr() < 2.25D ? 0.35F : 1.0F;
			ground(p, d.rot(), cell.add(0, 0.015D * near, 0), 0.62D * near, POOL, seed + i, i % 5 == 0 ? CLOT : TINT,
					a * (float) (0.3D + 0.35D * edge) * near);
			if (i % 4 == 0)
				ground(p, d.rot(), cell.add(0, 0.022D * near, 0), 0.3D * near, RIPPLE, seed + i + 300, CLOT, a * 0.35F * near);
		}
		// 2. sinew: the pale cream rim advancing at the edge, its Bloom tell
		int rimPoints = Math.max(12, (int) (radius * 6));
		List<Vec3> rim = new ArrayList<>(rimPoints + 1);
		for (int i = 0; i <= rimPoints; i++) {
			double angle = i * Math.PI * 2.0D / rimPoints;
			double wobble = 1.0D + 0.05D * Math.sin(angle * 5 + seed + d.time() * 0.05D);
			rim.add(centre.add(Math.cos(angle) * radius * wobble, 0.05D, Math.sin(angle) * radius * wobble));
		}
		ribbonPath(p, d.rot(), rim, d.camera(), 0.09D, 0.09D, RIBBON, seed + 500, CREAM, a * (age < 50 ? 0.7F : 0.4F));
		// 3. knots: the Kindling as cracks in the colony that brighten cream with its value, never red
		int cracks = 6 + (int) Math.round(kindling * 6);
		for (int i = 0; i < cracks; i++) {
			double angle = i * 2.39996D + seed;
			double reach = radius * (0.45D + 0.45D * PutricielBloomRules.unit(seed, 900 + i));
			Vec3 mid = centre.add(Math.cos(angle + 0.25D) * reach * 0.5D, 0.04D, Math.sin(angle + 0.25D) * reach * 0.5D);
			Vec3 tip = centre.add(Math.cos(angle) * reach, 0.04D, Math.sin(angle) * reach);
			ribbonPath(p, d.rot(), List.of(centre.add(0, 0.04D, 0), mid, tip), d.camera(), 0.035D, 0.01D, RIBBON, seed + 600 + i,
					CREAM, a * (float) (0.15D + 0.75D * kindling));
		}
		// 4. business end: heat shimmer above the field as the buried will stirs
		if (kindling > 0.2D) {
			for (int i = 0; i < 6; i++) {
				double angle = i * 1.047D + seed;
				double rise = (d.time() * 0.02D + i * 0.17D) % 1.0D;
				Vec3 at = centre.add(Math.cos(angle) * radius * 0.55D, 0.4D + rise * 1.6D, Math.sin(angle) * radius * 0.55D);
				card(p, d.rot(), at, d.right(), d.up(), 0.5D, 0.8D, WISP, seed + 700 + i, CREAM,
						a * (float) ((kindling - 0.2D) * 0.25D * Math.sin(rise * Math.PI)));
			}
		}
		// 5. motion: spores at the sown palm while the caster kneels
		if (age < 30) {
			float spores = 1.0F - age / 30.0F;
			for (int i = 0; i < 8; i++) {
				double angle = i * 0.785D + seed;
				double lift = HemoEasing.easeOutCubic(age / 30.0D);
				Vec3 at = centre.add(Math.cos(angle) * 0.4D * lift, 0.1D + 0.6D * lift, Math.sin(angle) * 0.4D * lift);
				card(p, d.rot(), at, d.right(), d.up(), 0.04D, 0.04D, FOCUS, seed + 800 + i, CREAM, a * spores * 0.7F);
			}
		}
	}

	/** The purifier's fire run out from the centre, then the char and the ash, offsets from the centre. */
	private static void ignition(ManipulationVisualPacket packet, CanonGiftDraw d) {
		PoseStack p = d.poses();
		float age = d.age();
		double radius = Math.max(1.0D, packet.radius());
		boolean full = packet.count() >= 2;
		int seed = MaterialQuads.seedTile(Math.floorMod(packet.from().hashCode(), 1 << 20));
		double reach = BloomOfRotRules.fireReach(age, radius);
		float a = d.opacity();
		List<Vec3> cells = cells(packet.from(), d.anchor());
		float burning = 1.0F - (float) HemoEasing.smoothstep((age - BloomOfRotRules.IGNITION_SPREAD) / 18.0D);
		float charred = (float) HemoEasing.smoothstep((age - 6.0D) / 24.0D);
		float ash = 1.0F - (float) HemoEasing.smoothstep((age - (BloomOfRotRules.ASH_END - 30)) / 30.0D);
		if (d.solids()) {
			// the colony's last caps curl and blacken where the fire has passed
			var growth = d.growth();
			for (int i = 0; i < cells.size(); i += 6) {
				Vec3 cell = cells.get(i);
				double out = cell.horizontalDistance();
				if (out > reach || charred >= 0.98F) continue;
				float burnt = (float) HemoEasing.clamp01((reach - out) / 2.0D);
				int state = PutricielGrowthGeometry.state(burnt, 0.1F, 0.0F);
				PutricielGrowthGeometry.rosette(p, growth, cell, PutricielBloomRules.rosette(seed + i, 0.4D, 3), 20.0F, 20.0F + 6.0F * (1 - charred),
						d.time(), state, seed + i * 7);
			}
			return;
		}
		// 1. the char eats the rot in curled plates, then packed ash lingers
		for (int i = 0; i < cells.size(); i++) {
			Vec3 cell = cells.get(i);
			double out = cell.horizontalDistance();
			if (out > reach) continue;
			ground(p, d.rot(), cell.add(0, 0.018D, 0), 0.6D, POOL, seed + i, ASH, a * ash * (0.35F + 0.4F * charred));
			if (i % 3 == 0)
				ground(p, d.rot(), cell.add(0, 0.024D, 0), 0.28D, RIPPLE, seed + i + 200, CLOT, a * ash * 0.5F * charred);
		}
		if (burning <= 0.01F) return;
		// 2. the ring of tongues at the running front, crimson with pink-white heat and soot tips
		double height = (full ? 1.6D : 1.2D) * (0.6D + 0.4D * HemoEasing.clamp01(age / 4.0D));
		PutricielFlameGeometry.ring(p, d.flame(), Vec3.ZERO, Math.max(0.3D, reach), 0.45D, height * burning, 0.25D, d.right(), d.up(),
				d.time(), seed, a * burning);
		// 3. the field behind the front still burns low before it chars
		for (int i = 0; i < cells.size(); i += 3) {
			Vec3 cell = cells.get(i);
			double out = cell.horizontalDistance();
			if (out > reach - 0.8D) continue;
			float low = burning * (1.0F - charred);
			if (low <= 0.02F) break;
			PutricielFlameGeometry.tongue(p, d.flame(), cell, Vec3.ZERO, 0.5D * low * (0.6D + 0.6D * PutricielBloomRules.unit(seed, i)),
					0.12D, d.time(), seed + i, a * low);
		}
		// 4. the heart of the fire at the caster as it is lit
		if (age < 10.0F) {
			float flash = 1.0F - age / 10.0F;
			PutricielFlameGeometry.column(p, d.flame(), Vec3.ZERO, 0.4D, 1.8D * flash, d.right(), d.up(), d.time(), seed + 3, a * flash);
		}
	}

	/** Bracket growths climbing from the body's feet by stage, offsets from the body's feet. */
	private static void colony(ManipulationVisualPacket packet, CanonGiftDraw d) {
		if (d.source() == null) return;
		int stage = Math.max(0, Math.min(3, packet.count() - 1));
		double fill = Math.max(0, Math.min(1, packet.radius()));
		if (fill <= 0.01D) return;
		PoseStack p = d.poses();
		int seed = MaterialQuads.seedTile(packet.entityId());
		double height = d.source().getBbHeight(), width = d.source().getBbWidth() * 0.5D;
		double climb = height * (0.15D + 0.7D * fill);
		int points = 2 + stage * 2;
		if (d.solids()) {
			if (d.frame().firstPerson()) return;
			var growth = d.growth();
			for (int i = 0; i < points; i++) {
				double angle = i * 2.39996D + seed;
				double y = climb * (0.15D + 0.85D * i / Math.max(1, points - 1));
				Vec3 foot = new Vec3(Math.cos(angle) * width * 1.05D, y, Math.sin(angle) * width * 1.05D);
				double scale = (0.12D + 0.05D * stage) * Math.min(1.5D, height / 1.8D);
				PutricielGrowthGeometry.rosette(p, growth, foot, PutricielBloomRules.rosette(seed + i, scale, 2 + stage), 20.0F, 1.0E6F,
						d.time(), PutricielGrowthGeometry.FRESH, seed + i * 11);
			}
			return;
		}
		float a = d.opacity() * (float) (0.25D + 0.35D * fill);
		for (int i = 0; i < 3; i++) {
			double angle = i * 2.09D + seed;
			Vec3 low = new Vec3(Math.cos(angle) * width, 0.02D, Math.sin(angle) * width);
			Vec3 high = new Vec3(Math.cos(angle + 0.3D) * width * 0.9D, climb, Math.sin(angle + 0.3D) * width * 0.9D);
			ribbonPath(p, d.rot(), List.of(low, low.lerp(high, 0.5D).add(0, 0, 0.03D), high), d.camera(), 0.035D, 0.012D, WISP,
					seed + i, TINT, a);
		}
		if (!d.frame().firstPerson())
			ground(p, d.rot(), new Vec3(0, 0.02D, 0), width * 2.2D, POOL, seed + 9, CLOT, a * 0.8F);
	}

	private static List<Vec3> cells(Vec3 centre, Vec3 anchor) {
		return GiftFloorCells.cells(centre, anchor, BloomOfRotRules.MAX_RADIUS, BloomOfRotRules.FLOOR_DEPTH);
	}
}
