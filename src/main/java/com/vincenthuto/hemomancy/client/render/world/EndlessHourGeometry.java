package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.saint.EndlessHourRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The Endless Hour (D-1 spec 3.4) drawn as Velorum's stillness made the caster's, on his Frozen Cruor:
 * <ol>
 * <li>the planted field ({@code HOUR_FIELD} state 1, keyed to the caster): the floor's block seams filling with dark
 * oxblood cruor ring by ring from the planted point, frost on their lips, a partly clear rime glaze over them, the
 * liquid dark lying in torn pools between 6 and 9 blocks, and frost motes drifting at a tenth of their speed;</li>
 * <li>the close ({@code HOUR_FIELD} state 2, at the point): leaning cruor blades erupting from the seams, fewer and
 * bigger than his Still Floor's; the broken Hour (state 3): the seams cracking across and the glaze shattering out;</li>
 * <li>a body set in its own cruor ({@code HOUR_PRESERVE}): the oxblood-glass shell of his fight forming from the feet
 * up over 6 ticks with short blades heaving round it; the caster's own set (count 2) is drawn only low in first
 * person;</li>
 * <li>stillness on a body ({@code HOUR_STILL}): cruor and frost climbing from the feet by its value; overlay
 * strength.</li>
 * </ol>
 * White base tint, accents FROST and CRUOR. Red stays dark, never arterial. The rim and seams are floor geometry and
 * never rise into the view.
 */
final class EndlessHourGeometry {
	private static final int TINT = 0xFFFFFF, FROST = 0xE8F0F4, CRUOR = 0x4A0E14, DARK = 0x0A0608;

	private EndlessHourGeometry() {
	}

	static boolean handles(Form form) {
		return form == Form.HOUR_FIELD || form == Form.HOUR_PRESERVE || form == Form.HOUR_STILL;
	}

	static void draw(ManipulationVisualPacket packet, CanonGiftDraw d) {
		if (d.opacity() <= 0) return;
		switch (packet.form()) {
			case HOUR_FIELD -> {
				if (packet.count() <= 1) field(packet, d);
				else close(packet, d, packet.count() >= 3);
			}
			case HOUR_PRESERVE -> preserve(packet, d);
			case HOUR_STILL -> still(packet, d);
			default -> {
			}
		}
	}

	/** The held Hour round its planted point, offsets from the cue's anchor. */
	private static void field(ManipulationVisualPacket packet, CanonGiftDraw d) {
		if (d.solids()) return;
		PoseStack p = d.poses();
		Vec3 point = packet.to().subtract(d.anchor());
		double radius = Math.max(1.0D, packet.radius());
		float age = d.age();
		double seams = EndlessHourRules.seams(age) * radius;
		double inner = radius * EndlessHourRules.DARK_INNER / EndlessHourRules.RADIUS;
		int seed = MaterialQuads.seedTile(Math.floorMod(packet.to().hashCode(), 1 << 20));
		float a = d.opacity();
		List<Vec3> cells = GiftFloorCells.cells(packet.to(), d.anchor(), radius, 2.0D);
		for (int i = 0; i < cells.size(); i++) {
			Vec3 cell = cells.get(i);
			double out = cell.subtract(point).horizontalDistance();
			if (out > seams) continue;
			// the seam welling at the cell's own edges, the ring nearest the point first
			float well = (float) HemoEasing.clamp01((seams - out) / 1.5D);
			seam(p, d.clot(), d.rime(), cell, well, a, seed + i);
			// a partly clear rime glaze over the seams, so the floor reads through
			ground(p, d.rime(), cell.add(0, 0.03D, 0), 0.5D, POOL, seed + i + 400, TINT, a * 0.14F * well);
			if (out >= inner) {
				// the liquid dark: torn pools between 6 and 9 blocks
				float depth = (float) HemoEasing.clamp01((out - inner) / 1.0D) * well;
				ground(p, d.umbra(), cell.add(0, 0.036D, 0), 0.72D + 0.2D * PutricielBloomRules.unit(seed, i), POOL, seed + i + 800,
						DARK, a * 0.55F * depth);
				if (i % 5 == 0)
					card(p, d.umbra(), cell.add(0, 0.25D + 0.1D * Math.sin(d.time() * 0.03D + i), 0), d.right(), d.up(), 0.35D, 0.3D, WISP,
							seed + i + 900, DARK, a * 0.25F * depth);
			}
		}
		// motes inside the Hour drift at a tenth of their speed and never stop
		for (int i = 0; i < 14; i++) {
			double angle = i * 2.39996D + seed;
			double reach = radius * (0.2D + 0.7D * PutricielBloomRules.unit(seed, 1000 + i));
			double drift = (d.time() * 0.002D + i * 0.07D) % 1.0D;
			Vec3 at = point.add(Math.cos(angle + drift) * reach, 0.3D + 1.6D * drift, Math.sin(angle + drift) * reach);
			card(p, d.rime(), at, d.right(), d.up(), 0.035D, 0.035D, FOCUS, seed + 1100 + i, FROST,
					a * 0.5F * (float) Math.sin(drift * Math.PI));
		}
	}

	/** The close's blades out of the seams, or the broken Hour's cracks and shattered glaze, offsets from the point. */
	private static void close(ManipulationVisualPacket packet, CanonGiftDraw d, boolean broken) {
		PoseStack p = d.poses();
		double radius = Math.max(1.0D, packet.radius());
		float age = d.age();
		int seed = MaterialQuads.seedTile(Math.floorMod(packet.from().hashCode(), 1 << 20));
		float fade = d.opacity() * (1.0F - (float) HemoEasing.smoothstep((age - (EndlessHourRules.SET_END - 8)) / 8.0D));
		if (fade <= 0) return;
		if (d.solids()) {
			VertexConsumer ice = d.ice();
			if (broken) {
				VelorumIceGeometry.chips(p, ice, new Vec3(0, 0.1D, 0), age, radius * 0.6D, 18, VelorumIceGeometry.HOARFROST, seed);
				return;
			}
			int blades = 12;
			for (int i = 0; i < blades; i++) {
				double angle = i * Math.PI * 2.0D / blades + seed * 0.37D;
				double reach = radius * (0.3D + 0.6D * PutricielBloomRules.unit(seed, 1200 + i));
				float rise = (float) HemoEasing.easeOutCubic(HemoEasing.clamp01((age - reach / radius * 6.0D) / 5.0D));
				if (rise <= 0.01F) continue;
				Vec3 foot = new Vec3(Math.cos(angle) * reach, -0.05D, Math.sin(angle) * reach);
				Vec3 lean = new Vec3(Math.cos(angle) * 0.35D, 1.0D, Math.sin(angle) * 0.35D);
				double length = (1.1D + 0.7D * PutricielBloomRules.unit(seed, 1300 + i)) * rise;
				VelorumIceGeometry.shard(p, ice, foot, lean, length, 0.16D, 0.55D, angle, 5, fade, VelorumIceGeometry.CRUOR_BLADE,
						VelorumIceGeometry.SHARD, seed + i * 7);
			}
			return;
		}
		// the seams flare frost as they set, or crack across from the point when the Hour breaks
		int lines = broken ? 10 : 6;
		for (int i = 0; i < lines; i++) {
			double angle = i * Math.PI * 2.0D / lines + seed;
			double grow = HemoEasing.easeOutCubic(HemoEasing.clamp01(age / (broken ? 4.0D : 10.0D)));
			Vec3 tip = new Vec3(Math.cos(angle) * radius * grow, 0.05D, Math.sin(angle) * radius * grow);
			Vec3 bend = tip.scale(0.5D).add(Math.sin(angle * 3) * 0.4D, 0, Math.cos(angle * 3) * 0.4D);
			ribbonPath(p, d.rime(), List.of(new Vec3(0, 0.05D, 0), bend, tip), d.camera(), 0.05D, 0.01D, RIBBON, seed + i,
					FROST, fade * (broken ? 0.85F : 0.5F));
		}
		if (!broken && age < 8.0F)
			ground(p, d.rime(), new Vec3(0, 0.05D, 0), radius * 0.5D * (age / 8.0D), RIPPLE, seed + 50, FROST, fade * (1 - age / 8.0F) * 0.6F);
	}

	/** A body set in its own blood frozen to cruor, offsets from its feet. */
	private static void preserve(ManipulationVisualPacket packet, CanonGiftDraw d) {
		PoseStack p = d.poses();
		float age = d.age();
		double height = Math.max(0.5D, packet.radius());
		boolean self = packet.count() >= 2;
		int seed = MaterialQuads.seedTile(Math.floorMod(packet.from().hashCode(), 1 << 20));
		float formed = (float) HemoEasing.clamp01(age / 6.0D);
		float life = Math.max(12.0F, packet.ticks());
		float crack = (float) HemoEasing.smoothstep((age - (life - 8.0F)) / 8.0D);
		var mc = Minecraft.getInstance();
		boolean viewerInside = self && mc.player != null && mc.options.getCameraType().isFirstPerson()
				&& mc.player.position().subtract(d.anchor()).horizontalDistanceSqr() < 0.36D;
		if (d.solids()) {
			VertexConsumer ice = d.ice();
			double width = Math.min(0.9D, 0.35D + height * 0.18D);
			VelorumIceGeometry.collar(p, ice, Vec3.ZERO, width * 1.3D, 0.18D * formed, 1.0F - crack, VelorumIceGeometry.CRUOR_COLLAR,
					VelorumIceGeometry.SHARD, seed);
			if (viewerInside) return;
			// the shell: panes leaning in round the body, grown from the feet up
			int panes = 7;
			for (int i = 0; i < panes; i++) {
				double angle = i * Math.PI * 2.0D / panes + seed * 0.21D;
				Vec3 foot = new Vec3(Math.cos(angle) * width, 0.0D, Math.sin(angle) * width);
				Vec3 axis = new Vec3(-Math.cos(angle) * 0.12D, 1.0D, -Math.sin(angle) * 0.12D);
				VelorumIceGeometry.shard(p, ice, foot, axis, height * 1.02D * formed, 0.2D, 0.35D, angle + Math.PI / 2, 4,
						1.0F - crack, VelorumIceGeometry.CRUOR_SPLINTER, VelorumIceGeometry.SHARD, seed + i * 5);
			}
			// short cruor blades heaving up round it
			if (age < 14.0F && !self) {
				for (int i = 0; i < 4; i++) {
					double angle = i * 1.57D + seed;
					Vec3 foot = new Vec3(Math.cos(angle) * (width + 0.4D), -0.05D, Math.sin(angle) * (width + 0.4D));
					Vec3 lean = new Vec3(Math.cos(angle) * 0.5D, 1.0D, Math.sin(angle) * 0.5D);
					float rise = (float) HemoEasing.easeOutCubic(HemoEasing.clamp01((age - 2.0D) / 4.0D));
					VelorumIceGeometry.shard(p, ice, foot, lean, 0.6D * rise, 0.1D, 0.5D, angle, 5, 1.0F - crack,
							VelorumIceGeometry.CRUOR_BLADE, VelorumIceGeometry.SHARD, seed + 40 + i);
				}
			}
			if (crack > 0.01F) VelorumIceGeometry.chips(p, ice, new Vec3(0, height * 0.5D, 0), age - (life - 8.0F), 0.9D, 10,
					VelorumIceGeometry.HOARFROST, seed + 60);
			return;
		}
		float a = d.opacity() * (1.0F - crack);
		if (viewerInside) {
			ground(p, d.rime(), new Vec3(0, 0.03D, 0), 0.7D, POOL, seed, FROST, a * 0.4F);
			return;
		}
		// the shell's glassy body over the frozen blood, and frost on its lips
		card(p, d.clot(), new Vec3(0, height * 0.5D * formed, 0), d.right(), d.up(), height * 0.42D, height * 0.55D * formed, WISP,
				seed + 1, CRUOR, a * 0.35F);
		card(p, d.rime(), new Vec3(0, height * formed, 0), d.right(), d.up(), height * 0.3D, 0.15D, FOCUS, seed + 2, FROST, a * 0.6F);
		ground(p, d.rime(), new Vec3(0, 0.03D, 0), 0.9D, RIPPLE, seed + 3, FROST, a * 0.45F);
	}

	/** Cruor and frost climbing a body from its feet by its stillness, offsets from the body's feet. */
	private static void still(ManipulationVisualPacket packet, CanonGiftDraw d) {
		if (d.solids() || d.source() == null) return;
		double still = Math.max(0, Math.min(1, packet.radius()));
		if (still <= 0.02D) return;
		PoseStack p = d.poses();
		int seed = MaterialQuads.seedTile(packet.entityId());
		double height = d.source().getBbHeight(), width = d.source().getBbWidth() * 0.5D;
		double climb = height * (0.1D + 0.85D * still);
		float a = d.opacity() * (float) (0.2D + 0.4D * still);
		boolean self = d.frame().firstPerson();
		for (int i = 0; i < (self ? 2 : 4); i++) {
			double angle = i * 1.57D + seed;
			Vec3 low = new Vec3(Math.cos(angle) * width, 0.02D, Math.sin(angle) * width);
			Vec3 high = new Vec3(Math.cos(angle + 0.2D) * width * 0.95D, self ? Math.min(climb, 0.4D) : climb,
					Math.sin(angle + 0.2D) * width * 0.95D);
			ribbonPath(p, d.clot(), List.of(low, low.lerp(high, 0.5D), high), d.camera(), 0.03D, 0.008D, RIBBON, seed + i, CRUOR, a);
			card(p, d.rime(), high, d.right(), d.up(), 0.05D, 0.05D, FOCUS, seed + 10 + i, FROST, a * 0.8F);
		}
		ground(p, d.rime(), new Vec3(0, 0.02D, 0), width * 2.0D + 0.2D * still, POOL, seed + 20, FROST, a * 0.5F);
	}

	/** One welling seam round a floor cell: four dark strips at its edges with frost on their lips. */
	private static void seam(PoseStack p, VertexConsumer cruor, VertexConsumer rime, Vec3 cell, float well, float alpha, int seed) {
		double half = 0.5D, width = 0.035D + 0.03D * well;
		for (int side = 0; side < 4; side++) {
			boolean alongX = side < 2;
			double sign = side % 2 == 0 ? -1.0D : 1.0D;
			Vec3 mid = alongX ? cell.add(0, 0.02D, sign * half) : cell.add(sign * half, 0.02D, 0);
			Vec3 run = alongX ? new Vec3(half * well + 0.05D, 0, 0) : new Vec3(0, 0, half * well + 0.05D);
			Vec3 across = alongX ? new Vec3(0, 0, width) : new Vec3(width, 0, 0);
			surface(p, cruor, mid.subtract(run).subtract(across), mid.add(run).subtract(across), mid.add(run).add(across),
					mid.subtract(run).add(across), RIBBON, seed + side, CRUOR, alpha * (0.4F + 0.5F * well));
			Vec3 lip = across.scale(1.8D);
			surface(p, rime, mid.subtract(run).subtract(lip).add(0, 0.004D, 0), mid.add(run).subtract(lip).add(0, 0.004D, 0),
					mid.add(run).add(lip).add(0, 0.004D, 0), mid.subtract(run).add(lip).add(0, 0.004D, 0), RIBBON, seed + side + 4,
					FROST, alpha * 0.22F * well);
		}
	}
}
