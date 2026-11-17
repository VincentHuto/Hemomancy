package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.data.StrandClientState;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.BrandRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.JetRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.LashRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.PlantRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.SprayRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandCue;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandForm;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSkills;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * Draws every strand (core-loop Track 12) from {@link StrandClientState}, and the accents the server fires. A strand
 * is a focal effect, drawn bold from the mod's own materials: the school's flow material for the body, a hot core down
 * its middle, and for Flammeus the Searing Lash's flame sheath and molten core. The lash leaves a short swish behind
 * its tip; the jet thickens with its pressure; the spray fans; the plant runs into the ground and rings its telegraph;
 * the brand pulls taut and cinches a knot. Accents (crack, pierce, drench, eruption, brand, tap) are server bursts.
 * A lash held but not yet thrown is an orb of blood turning in the hand, tighter and quicker as it charges; the throw
 * unspools it into the strand, and a lash left still thins and beads back down its length into the hand.
 *
 * <p>The second wave (core-loop round two P5) adds each school's own shape where the shared one would not read: a dark
 * Tenebris lash edged in pale light, a Congeatio jet carrying cruor shards, Ferric jets and sprays of blood-iron
 * flechettes and an eruption of iron spikes (drawn in the solid pass, {@link #collectIron}), Ductilis sprays and
 * brands of jagged arcs, and a Lux spray split into prism rays ({@link StrandSchoolGeometry}). A strand with a twin
 * (Second Strand) is drawn again from the off hand.
 */
public final class StrandRenderer {
	private static final int TINT = 0xFFFFFF, CORE = 0xFFD2C4, CLOT = 0x7A1020;
	/** The pale edge round a dark Tenebris strand: a cold violet-white. */
	private static final int EDGE = 0xE6DDF2;
	private static final double GOLDEN = 2.39996;

	private StrandRenderer() {
	}

	static void tick() {
		var level = Minecraft.getInstance().level;
		if (level != null) StrandClientState.tick(level);
	}

	static boolean handlesAccent(Form form) {
		return form == Form.STRAND_ACCENT;
	}

	/** The body's flow material for a school. */
	static LuxUmbraBatch.Material material(EnumBloodTemper school) {
		return CastImpactGeometry.material(school);
	}

	/**
	 * The material a school's cards (glows, knots, billows) draw in. Cruor glass has no soft card shapes, so Congeatio's
	 * cards are the pale cold light, as its cast impacts already are.
	 */
	static LuxUmbraBatch.Material cardMaterial(EnumBloodTemper school) {
		return school == EnumBloodTemper.CONGEATIO ? LuxUmbraBatch.Material.LUX : material(school);
	}

	// ── Held strands ──

	static void collect(PoseStack poses, LuxUmbraBatch flows, Vec3 camera, Vec3 right, Vec3 up, double time, float partial) {
		var level = Minecraft.getInstance().level;
		if (level == null) return;
		for (StrandClientState.Strand strand : StrandClientState.all()) {
			if (strand.cue == null || !(level.getEntity(strand.entityId) instanceof LivingEntity caster)) continue;
			if (caster.isInvisible()) continue;
			float alpha = strand.presence(time);
			if (alpha <= 0.003F) continue;
			Vec3 palm = palm(caster, strand, partial);
			if (palm.distanceToSqr(camera) > 96 * 96) continue;
			poses.pushPose();
			poses.translate(palm.x - camera.x, palm.y - camera.y, palm.z - camera.z);
			Vec3 eye = camera.subtract(palm);
			int seed = Math.floorMod(strand.entityId * 31, 113);
			EnumBloodTemper school = strand.cue.primary();
			body(poses, flows, strand, caster, false, palm, eye, right, up, school, seed, time, partial, alpha);
			poses.popPose();
			if (strand.cue.twin()) {
				// The twin pours from the off hand: the same strand again, a little fainter, from the other palm.
				Vec3 off = twinPalm(caster, strand, partial);
				poses.pushPose();
				poses.translate(off.x - camera.x, off.y - camera.y, off.z - camera.z);
				body(poses, flows, strand, caster, true, off, camera.subtract(off), right, up, school, seed + 17, time, partial,
						alpha * 0.85F);
				poses.popPose();
			}
		}
	}

	private static void body(PoseStack poses, LuxUmbraBatch flows, StrandClientState.Strand strand, LivingEntity caster,
			boolean twin, Vec3 palm, Vec3 eye, Vec3 right, Vec3 up, EnumBloodTemper school, int seed, double time,
			float partial, float alpha) {
		switch (strand.form()) {
			case LASH -> {
				LashRules.State state = twin ? strand.twinLash : strand.lash;
				orb(poses, flows, state, right, up, eye, school, seed, time, partial, alpha);
				var style = StrandClientState.style(caster);
				// A faint lash is its caster's to see: anyone else gets a shadow of it.
				float shown = style.faint() && caster != Minecraft.getInstance().player ? alpha * FAINT : alpha;
				lash(poses, flows, strand, state, twin ? strand.twinTrail : strand.trail,
						twin != strand.cue.leftArm(), palm, eye, right, up, school, seed, time, partial, shown, style.tails());
			}
			case JET -> jet(poses, flows, strand, palm, eye, right, up, school, seed, time, partial, alpha);
			case SPRAY -> spray(poses, flows, strand, caster, palm, eye, right, up, school, seed, time, partial, alpha);
			case PLANT -> plant(poses, flows, strand, palm, eye, right, up, school, seed, time, alpha);
			case BRAND -> brand(poses, flows, strand, palm, eye, right, up, school, seed, time, partial, alpha);
		}
	}

	/**
	 * Where the twin leaves the body: the off hand's drawn palm in first person when a cell hand is drawn there, else
	 * the shared rules' hand on that side.
	 */
	private static Vec3 twinPalm(LivingEntity caster, StrandClientState.Strand strand, float partial) {
		Minecraft mc = Minecraft.getInstance();
		boolean leftOff = !strand.cue.leftArm();
		if (caster == mc.player && mc.getCameraEntity() == caster && mc.options.getCameraType().isFirstPerson()) {
			Vec3 drawn = com.vincenthuto.hemomancy.client.render.item.harbinger.FirstPersonPalm.world(
					leftOff ? HumanoidArm.LEFT : HumanoidArm.RIGHT);
			if (drawn != null) return drawn;
		}
		return StrandCue.hand(caster.getEyePosition(partial), caster.getViewVector(partial), caster.getViewYRot(partial), leftOff);
	}

	/** Where the strand leaves the body this frame: the drawn hand (first or third person) or the Staff's head. */
	private static Vec3 palm(LivingEntity caster, StrandClientState.Strand strand, float partial) {
		return CellHandStreams.palm(caster, strand.cue.leftArm(), partial);
	}

	/**
	 * The orb a held lash gathers in the hand: a core of blood with beads turning round it on two tilted rings, each
	 * dragging a short thread. It swells, tightens and quickens with the charge, beats once a second when full, and
	 * shrinks into the strand through the throw. Nothing once the lash is out.
	 */
	private static void orb(PoseStack p, LuxUmbraBatch flows, LashRules.State state, Vec3 right, Vec3 up, Vec3 eye,
			EnumBloodTemper school, int seed, double time, float partial, float alpha) {
		LashRules.Phase phase = state.phase();
		if (phase != LashRules.Phase.CHARGING && phase != LashRules.Phase.THROWING) return;
		double charge = phase == LashRules.Phase.CHARGING ? state.charge() : 1.0D;
		double shrink = phase == LashRules.Phase.THROWING ? 1.0D - state.throwProgress() : 1.0D;
		if (shrink <= 0.01D) return;
		double beat = charge >= 1.0D ? 1.0D + 0.14D * Math.sin(time * Math.PI / 10.0D) : 1.0D;
		double core = (0.1D + 0.14D * charge) * beat * shrink;
		double orbit = (0.3D - 0.09D * charge) * beat * shrink;
		Vec3 toward = right.cross(up).normalize();
		VertexConsumer cards = flows.vertices(cardMaterial(school));
		VertexConsumer threads = flows.vertices(school == EnumBloodTemper.FLAMMEUS ? LuxUmbraBatch.Material.MOLTEN : material(school));
		float glow = alpha * (float) (0.55D + 0.45D * charge);
		card(p, cards, Vec3.ZERO, right, up, core * 2.2D, core * 2.2D, WISP, seed + 20, TINT, glow * 0.5F);
		card(p, cards, Vec3.ZERO, right, up, core, core, FOCUS, seed + 21, TINT, glow);
		card(p, cards, Vec3.ZERO, right, up, core * 0.5D, core * 0.5D, FOCUS, seed + 22, CORE, glow * (float) charge);
		int beads = 3 + (int) Math.round(3.0D * charge);
		double spin = time * (0.22D + 0.5D * charge);
		for (int i = 0; i < beads; i++) {
			// Alternate beads ride two rings tilted against each other, so the orb reads as a ball and not a wheel.
			double tilt = i % 2 == 0 ? 0.6D : -0.6D;
			double phaseAt = spin * (i % 2 == 0 ? 1.0D : -0.85D) + i * GOLDEN;
			List<Vec3> thread = new ArrayList<>(6);
			for (int k = 0; k < 6; k++) thread.add(orbitPoint(right, up, toward, phaseAt - k * 0.22D * Math.signum(tilt), orbit, tilt));
			double[] widths = new double[thread.size()];
			for (int k = 0; k < widths.length; k++) widths[k] = 0.04D * shrink * (1.0D - k / (double) widths.length);
			ribbonPath(p, threads, thread, eye, widths, RIBBON, seed + 23 + i, TINT, glow * 0.9F);
			card(p, cards, thread.getFirst(), right, up, 0.05D * shrink, 0.05D * shrink, FOCUS, seed + 30 + i, CORE, glow);
		}
	}

	private static Vec3 orbitPoint(Vec3 right, Vec3 up, Vec3 toward, double angle, double radius, double tilt) {
		double c = Math.cos(angle) * radius, s = Math.sin(angle) * radius;
		return right.scale(c).add(up.scale(s * Math.cos(tilt))).add(toward.scale(s * Math.sin(tilt)));
	}

	private static void lash(PoseStack p, LuxUmbraBatch flows, StrandClientState.Strand strand, LashRules.State state,
			Deque<Vec3> trail, boolean leftArm, Vec3 palm, Vec3 eye, Vec3 right, Vec3 up, EnumBloodTemper school, int seed,
			double time, float partial, float alpha, int tails) {
		if (!state.seeded() || state.phase() == LashRules.Phase.CHARGING) return;
		// Through the throw the strand pays out of the hand; through a fade it thins as it shortens.
		double paid = 0.3D + 0.7D * state.throwProgress();
		boolean fading = state.phase() == LashRules.Phase.FADING;
		double thin = fading ? 0.3D + 0.7D * Math.max(0.0D, state.extent()) : 1.0D;
		// The chain is worked out from where the shared rules put the hand; the drawn hand (first person above all) sits
		// elsewhere, so the offset is eased out along the chain: the strand leaves the palm and its tip stays true.
		Vec3 shift = palm.subtract(ruleHand(strand, partial, leftArm));
		List<Vec3> points = new ArrayList<>(LashRules.NODES + 1);
		points.add(Vec3.ZERO);
		for (int i = 0; i < LashRules.NODES; i++) {
			double ease = Math.pow(1.0D - (i + 1) / (double) LashRules.NODES, 1.5D);
			points.add(state.previous(i).lerp(state.node(i), partial).add(shift.scale(ease)).subtract(palm).scale(paid));
		}
		Vec3 tip = points.getLast();
		double speed = state.tip().distanceTo(state.previous(LashRules.NODES - 1));
		float hot = (float) HemoEasing.clamp01((speed - LashRules.tuning().slackSpeed() * 0.5D) / LashRules.tuning().fullSpeed());
		double[] body = new double[points.size()], core = new double[points.size()];
		for (int i = 0; i < points.size(); i++) {
			double t = i / (points.size() - 1.0D);
			double wave = 1.0D + 0.18D * Math.sin(time * 0.9D - t * 9.0D + seed);
			body[i] = (0.13D * (1.0D - t) + 0.065D * t) * wave * thin;
			core[i] = body[i] * 0.3D;
		}
		// A Tenebris cut keeps its width out to the tip (the other lashes taper): seen from the side, a tapered umbra
		// body thinned to a thread past the hand.
		if (school == EnumBloodTemper.TENEBRIS) {
			for (int i = 0; i < body.length; i++) body[i] *= 1.0D + 1.1D * i / (body.length - 1.0D);
		}
		List<Vec3> tailTips = dressTailed(p, flows, school, strand.cue.secondary(), points, eye, body, core, seed, time, alpha, hot,
				tails, 0.55D);
		VertexConsumer tipGlow = flows.vertices(cardMaterial(school));
		for (Vec3 tailTip : tailTips) {
			card(p, tipGlow, tailTip, right, up, 0.1D + 0.12D * hot, 0.1D + 0.12D * hot, FOCUS, seed + 4, TINT, alpha * (0.55F + 0.45F * hot));
		}
		if (fading) {
			// The lash is leaving: beads of it run back down the strand into the hand.
			VertexConsumer beads = flows.vertices(cardMaterial(school));
			for (int k = 0; k < 4; k++) {
				double along = 1.0D - ((time * 0.045D + k * 0.25D) % 1.0D);
				double at = along * (points.size() - 1);
				int lower = Math.min(points.size() - 2, (int) at);
				Vec3 bead = points.get(lower).lerp(points.get(lower + 1), at - lower);
				double size = 0.05D + 0.05D * (1.0D - along);
				card(p, beads, bead, right, up, size, size, FOCUS, seed + 40 + k, k % 2 == 0 ? TINT : CORE, alpha * 0.85F);
			}
		}
		// The tip glows with its speed, and a moving tip drags a short swish behind it.
		// Molten has no soft card shapes (its cards draw as hard squares), so a Flammeus tip glows in flame.
		VertexConsumer glow = flows.vertices(cardMaterial(school));
		double knot = 0.14D + 0.2D * hot;
		card(p, glow, tip, right, up, knot, knot, FOCUS, seed + 4, TINT, alpha * (0.55F + 0.45F * hot));
		card(p, glow, tip, right, up, knot * 0.45D, knot * 0.45D, FOCUS, seed + 6, CORE, alpha * hot);
		if (trail.size() >= 3 && hot > 0.05F) {
			List<Vec3> swish = new ArrayList<>(trail.size() + 1);
			swish.add(tip);
			Iterator<Vec3> behind = trail.iterator();
			behind.next();
			while (behind.hasNext()) swish.add(behind.next().subtract(palm));
			double[] fade = new double[swish.size()];
			for (int i = 0; i < fade.length; i++) fade[i] = 0.11D * (1.0D - i / (double) fade.length) * (0.4D + hot);
			ribbonPath(p, glow, swish, eye, fade, SLASH, seed + 7, TINT, alpha * 0.55F * hot);
		}
	}

	/** How much of a faint lash anyone but its caster sees. */
	private static final float FAINT = 0.4F;
	/** How far along a many-tailed lash its tails part. */
	private static final double KNOT = 0.6D;

	/**
	 * Dresses a lash along {@code path}: whole for one tail; for more, the trunk as far as the knot and then each tail
	 * fanned out from it, thinner. Returns the tips of the tails that are not the path's own end.
	 */
	private static List<Vec3> dressTailed(PoseStack p, LuxUmbraBatch flows, EnumBloodTemper school, EnumBloodTemper secondary,
			List<Vec3> path, Vec3 eye, double[] body, double[] core, int seed, double time, float alpha, float hot, int tails,
			double spread) {
		List<Vec3> tips = new ArrayList<>();
		int knot = (int) Math.round((path.size() - 1) * KNOT);
		if (tails <= 1 || knot < 1 || knot >= path.size() - 1) {
			dress(p, flows, school, secondary, path, eye, body, core, seed, time, alpha, hot);
			return tips;
		}
		dress(p, flows, school, secondary, path.subList(0, knot + 1), eye, java.util.Arrays.copyOfRange(body, 0, knot + 1),
				java.util.Arrays.copyOfRange(core, 0, knot + 1), seed, time, alpha, hot);
		double[] tailBody = StrandSchoolGeometry.scaled(java.util.Arrays.copyOfRange(body, knot, body.length), 0.7D);
		double[] tailCore = StrandSchoolGeometry.scaled(java.util.Arrays.copyOfRange(core, knot, core.length), 0.7D);
		for (int tail = 0; tail < tails; tail++) {
			double lean = tails == 1 ? 0.0D : tail * 2.0D / (tails - 1.0D) - 1.0D;
			List<Vec3> thong = tail(path, knot, lean, eye, spread, time, seed + tail * 13);
			dress(p, flows, school, null, thong, eye, tailBody, tailCore, seed + 17 * (tail + 1), time, alpha, hot);
			if (Math.abs(lean) > 1.0E-3D) tips.add(thong.getLast());
		}
		return tips;
	}

	/** One tail of a many-tailed lash: the path from its knot outward, leaning to one side ({@code lean} -1 to 1) across the view. */
	private static List<Vec3> tail(List<Vec3> path, int knot, double lean, Vec3 eye, double spread, double time, int seed) {
		Vec3 root = path.get(knot);
		Vec3 along = path.getLast().subtract(root);
		Vec3 dir = along.lengthSqr() < 1e-8 ? new Vec3(0, 0, 1) : along.normalize();
		Vec3 side = dir.cross(eye.subtract(root));
		side = side.lengthSqr() < 1e-8 ? VisceralGeometry.side(dir) : side.normalize();
		int links = path.size() - 1 - knot;
		List<Vec3> thong = new ArrayList<>(links + 1);
		for (int i = 0; i <= links; i++) {
			double u = links == 0 ? 0.0D : i / (double) links;
			double sway = 0.07D * Math.sin(time * 0.8D + lean * 2.3D + u * 5.0D + seed) * u;
			thong.add(path.get(knot + i).add(side.scale(lean * spread * Math.pow(u, 0.8D) + sway)));
		}
		return thong;
	}

	/** One lash body in its school's dress along {@code path}, {@code body} and {@code core} its half-widths. */
	private static void dress(PoseStack p, LuxUmbraBatch flows, EnumBloodTemper school, EnumBloodTemper secondary, List<Vec3> path,
			Vec3 eye, double[] body, double[] core, int seed, double time, float alpha, float hot) {
		if (school == EnumBloodTemper.FLAMMEUS) {
			ThermalGeometry.flameRibbon(p, flows.vertices(LuxUmbraBatch.Material.FLAME), path, eye, 0.2D, seed, alpha * 0.85F);
			ribbonPath(p, flows.vertices(LuxUmbraBatch.Material.MOLTEN), path, eye, body, RIBBON, seed + 1, TINT, alpha);
			ribbonPath(p, flows.vertices(LuxUmbraBatch.Material.MOLTEN), path, eye, core, RIBBON, seed + 2, CORE, alpha);
		} else if (school == EnumBloodTemper.TENEBRIS) {
			// A dark cut with a pale edge: a wide halo of cold light laid under the umbra body, so the lash reads on a dark
			// floor as well as a bright one, and a thin pale seam down its middle that brightens with speed.
			VertexConsumer pale = flows.vertices(LuxUmbraBatch.Material.LUX);
			VertexConsumer dark = flows.vertices(LuxUmbraBatch.Material.UMBRA);
			ribbonPath(p, pale, path, eye, StrandSchoolGeometry.scaled(body, 3.0D), WISP, seed + 3, EDGE, alpha * 0.8F);
			ribbonPath(p, pale, path, eye, StrandSchoolGeometry.scaled(body, 2.1D), RIBBON, seed + 8, EDGE, alpha);
			ribbonPath(p, dark, path, eye, StrandSchoolGeometry.scaled(body, 3.2D), WISP, seed + 9, TINT, alpha * 0.9F);
			ribbonPath(p, dark, path, eye, StrandSchoolGeometry.scaled(body, 1.8D), RIBBON, seed, TINT, alpha);
			ribbonPath(p, dark, path, eye, StrandSchoolGeometry.scaled(body, 1.8D), RIBBON, seed + 10, TINT, alpha);
			ribbonPath(p, pale, path, eye, StrandSchoolGeometry.scaled(core, 0.7D), RIBBON, seed + 1, EDGE,
					alpha * (0.55F + 0.45F * hot));
		} else {
			VertexConsumer v = flows.vertices(material(school));
			ribbonPath(p, v, path, eye, StrandSchoolGeometry.scaled(body, 1.9D), WISP, seed + 3, TINT, alpha * 0.55F);
			ribbonPath(p, v, path, eye, body, RIBBON, seed, TINT, alpha);
			ribbonPath(p, v, path, eye, core, RIBBON, seed + 1, CORE, alpha * (0.6F + 0.4F * hot));
			if (secondary != null && secondary != school) {
				ribbonPath(p, flows.vertices(material(secondary)), wound(path, 0.045D, time, seed), eye,
						StrandSchoolGeometry.scaled(body, 0.22D), RIBBON, seed + 5, TINT, alpha * 0.8F);
			}
		}
	}

	/** Where the shared rules put a hand this frame: the point the server works that chain from. */
	private static Vec3 ruleHand(StrandClientState.Strand strand, float partial, boolean leftArm) {
		var level = Minecraft.getInstance().level;
		if (level == null || !(level.getEntity(strand.entityId) instanceof LivingEntity caster)) return Vec3.ZERO;
		return StrandCue.hand(caster.getEyePosition(partial), caster.getViewVector(partial), caster.getViewYRot(partial),
				leftArm);
	}

	/** A thread wound round a path: the secondary school's colour coiled along the strand. */
	private static List<Vec3> wound(List<Vec3> path, double radius, double time, int seed) {
		List<Vec3> coil = new ArrayList<>(path.size());
		for (int i = 0; i < path.size(); i++) {
			Vec3 at = path.get(i);
			Vec3 along = path.get(Math.min(i + 1, path.size() - 1)).subtract(path.get(Math.max(0, i - 1)));
			Vec3 side = VisceralGeometry.side(along.lengthSqr() < 1e-8 ? new Vec3(0, 0, 1) : along.normalize());
			Vec3 normal = along.lengthSqr() < 1e-8 ? new Vec3(0, 1, 0) : along.normalize().cross(side).normalize();
			double angle = i * 1.4D + time * 0.35D + seed;
			double fade = i / (path.size() - 1.0D);
			coil.add(at.add(side.scale(Math.cos(angle) * radius * (1 - fade * 0.6D)))
					.add(normal.scale(Math.sin(angle) * radius * (1 - fade * 0.6D))));
		}
		return coil;
	}

	private static void jet(PoseStack p, LuxUmbraBatch flows, StrandClientState.Strand strand, Vec3 palm, Vec3 eye,
			Vec3 right, Vec3 up, EnumBloodTemper school, int seed, double time, float partial, float alpha) {
		VertexConsumer cards = flows.vertices(cardMaterial(school));
		if (strand.end == null) return;
		Vec3 end = strand.endPrevious.lerp(strand.end, partial).subtract(palm);
		double length = end.length();
		if (length < 0.2D) return;
		double pressure = strand.jet.pressure();
		boolean lux = school == EnumBloodTemper.LUX;
		VertexConsumer v = flows.vertices(material(school));
		int shape = lux ? WHITE_BEAM : RIBBON;
		double width = 0.045D + 0.11D * pressure;
		double throb = 1.0D + 0.12D * Math.sin(time * 1.6D + seed);
		// Body, then three offset sheets so the jet reads as a column from any side, then a white-hot core.
		ribbon(p, v, Vec3.ZERO, end, eye, width * throb, shape, seed, TINT, alpha, 0, 1);
		Vec3 axis = end.scale(1.0D / length);
		Vec3 across = VisceralGeometry.side(axis), lift = axis.cross(across).normalize();
		for (int i = 0; i < 3; i++) {
			double angle = i * Math.PI * 2 / 3 + time * 0.25D;
			Vec3 offset = across.scale(Math.cos(angle) * width * 0.35D).add(lift.scale(Math.sin(angle) * width * 0.35D));
			ribbon(p, v, offset, end.add(offset), eye, width * 0.35D, shape, seed + 1 + i, TINT, alpha * 0.5F, 0, 1);
		}
		ribbon(p, flows.vertices(lux ? LuxUmbraBatch.Material.LUX : material(school)), Vec3.ZERO, end, eye,
				width * 0.3D, shape, seed + 5, lux ? WHITE : CORE, alpha * (0.5F + 0.5F * (float) pressure), 0, 1);
		if (school == EnumBloodTemper.CONGEATIO) {
			// Cruor clots into shards that ride the jet and set as a spearhead in what it strikes.
			StrandSchoolGeometry.cruorShards(p, flows.vertices(LuxUmbraBatch.Material.GLASS), end, pressure, seed + 30, time, alpha);
		} else if (school == EnumBloodTemper.FERRIC) {
			// The iron flechettes ride the solid pass; here, the blood line that carries them.
			ribbon(p, flows.vertices(LuxUmbraBatch.Material.ANIMUS), Vec3.ZERO, end, eye, width * 0.55D, RIBBON, seed + 31,
					TINT, alpha * 0.8F, 0, 1);
		}
		// The contact boils with the pressure; at full it opens to pierce.
		double splash = 0.15D + 0.35D * pressure;
		card(p, cards, end, right, up, splash * throb, splash * throb, FOCUS, seed + 6, TINT, alpha);
		card(p, cards, end, right, up, splash * 0.4D, splash * 0.4D, FOCUS, seed + 7, lux ? WHITE : CORE, alpha * (float) pressure);
		if (strand.onCreature && pressure > 0.05D) {
			for (int i = 0; i < 5; i++) {
				Vec3 spray = cone(axis.scale(-1), across, lift, i, seed, 1.0D).scale(splash * (1.2D + 0.6D * Math.sin(time + i)));
				stream(p, v, end, end.add(spray), eye, 0.02D + 0.03D * pressure, 0.1D, time * 0.3D + i, SLASH, seed + 10 + i,
						TINT, alpha * (float) (0.4D + 0.6D * pressure));
			}
		}
	}

	private static void spray(PoseStack p, LuxUmbraBatch flows, StrandClientState.Strand strand, LivingEntity caster,
			Vec3 palm, Vec3 eye, Vec3 right, Vec3 up, EnumBloodTemper school, int seed, double time, float partial, float alpha) {
		VertexConsumer cards = flows.vertices(cardMaterial(school));
		Vec3 look = caster.getViewVector(partial);
		Vec3 across = VisceralGeometry.side(look), lift = look.cross(across).normalize();
		VertexConsumer v = flows.vertices(material(school));
		double spread = Math.tan(Math.acos(strand.cue.twin() ? SprayRules.TWIN_MIN_DOT : SprayRules.MIN_DOT));
		if (school == EnumBloodTemper.DUCTILIS) {
			StrandSchoolGeometry.arcSpray(p, v, look, across, lift, spread, strand.length, eye, right, up, seed, time, alpha);
			return;
		}
		if (school == EnumBloodTemper.LUX) {
			StrandSchoolGeometry.prismSpray(p, v, look, across, lift, spread, strand.length, eye, right, up, seed, time, alpha);
			return;
		}
		// Ferric's flechettes fly in the solid pass; its plumes thin to a fine blood mist behind them.
		float plumes = school == EnumBloodTemper.FERRIC ? 0.45F : 1.0F;
		// A fan of plumes thrown from the palm: each a stream that swells into billows as it leaves the hand, the billows
		// running outward with time so the spray reads as poured, not drawn.
		for (int i = 0; i < 9; i++) {
			double angle = i * GOLDEN + Math.sin(time * 0.07D + i) * 0.3D;
			double r = spread * Math.sqrt((i + 0.5D) / 9.0D);
			Vec3 dir = look.add(across.scale(Math.cos(angle) * r)).add(lift.scale(Math.sin(angle) * r)).normalize();
			double reach = strand.length * (0.75D + 0.25D * (0.5D + 0.5D * Math.sin(time * 0.8D + i * 1.9D)));
			// A soft swelling plume, not a line: a billowed core that widens as it leaves the hand.
			List<Vec3> plume = new ArrayList<>(9);
			double[] widths = new double[9];
			for (int k = 0; k <= 8; k++) {
				double u = k / 8.0D;
				double sway = Math.sin(time * 0.35D + i * 1.3D + u * 4.0D) * 0.12D * u;
				plume.add(dir.scale(reach * 0.9D * u).add(across.scale(sway)).add(lift.scale(sway * 0.6D)));
				widths[k] = 0.05D + 0.3D * u;
			}
			ribbonPath(p, v, plume, eye, widths, WISP, seed + i, TINT, alpha * 0.7F * plumes);
			for (int k = 0; k < 4; k++) {
				double run = (time * 0.12D + k * 0.25D + i * 0.11D) % 1.0D;
				Vec3 at = dir.scale(reach * (0.2D + 0.85D * run));
				double size = 0.2D + 0.6D * run;
				card(p, cards, at, right, up, size, size, WISP, seed + 20 + i * 4 + k, TINT,
						alpha * plumes * (float) (0.8D * (1.0D - run * 0.75D)));
			}
		}
		card(p, cards, look.scale(0.3D), right, up, 0.22D, 0.22D, FOCUS, seed + 60, CORE, alpha * 0.8F);
	}

	private static void plant(PoseStack p, LuxUmbraBatch flows, StrandClientState.Strand strand, Vec3 palm, Vec3 eye,
			Vec3 right, Vec3 up, EnumBloodTemper school, int seed, double time, float alpha) {
		VertexConsumer cards = flows.vertices(cardMaterial(school));
		if (strand.point == null) return;
		Vec3 ground = strand.point.subtract(palm);
		// Ferric's pour is the blood the iron is drawn from; the pale Ductilis flow would read as a nerve.
		VertexConsumer v = flows.vertices(school == EnumBloodTemper.FERRIC ? LuxUmbraBatch.Material.ANIMUS : material(school));
		double fill = strand.cue.param();
		// The strand runs from the hand down into the ground, throbbing as blood sinks.
		List<Vec3> pour = new ArrayList<>(13);
		Vec3 control = ground.scale(0.5D).add(0, 0.6D, 0);
		double[] widths = new double[13];
		for (int i = 0; i <= 12; i++) {
			double t = i / 12.0D;
			Vec3 a = Vec3.ZERO.lerp(control, t), b = control.lerp(ground, t);
			pour.add(a.lerp(b, t));
			widths[i] = (0.1D - 0.04D * t) * (1.0D + 0.25D * Math.sin(time * 1.2D - t * 8.0D));
		}
		ribbonPath(p, v, pour, eye, widths, RIBBON, seed, TINT, alpha);
		double[] seam = new double[widths.length];
		for (int i = 0; i < seam.length; i++) seam[i] = widths[i] * 0.3D;
		ribbonPath(p, v, pour, eye, seam, RIBBON, seed + 7, CORE, alpha * 0.8F);
		card(p, cards, ground, right, up, 0.18D + 0.15D * fill, 0.18D + 0.15D * fill, FOCUS, seed + 1, CORE, alpha * 0.8F);
		telegraph(p, v, flows.vertices(LuxUmbraBatch.Material.ANIMUS), ground, PlantRules.MIN_RADIUS + (PlantRules.MAX_RADIUS - PlantRules.MIN_RADIUS) * fill, eye, seed, time,
				alpha * (0.45F + 0.35F * (float) fill), fill);
	}

	/** The plant's telegraph on the ground: a thin ring at the eruption's radius and short spokes, partly clear. */
	private static void telegraph(PoseStack p, VertexConsumer v, VertexConsumer pool, Vec3 centre, double radius, Vec3 eye,
			int seed, double time, float alpha, double fill) {
		List<Vec3> ring = new ArrayList<>(33);
		for (int i = 0; i <= 32; i++) {
			double a = i * Math.PI / 16;
			ring.add(centre.add(Math.cos(a) * radius, 0.06D, Math.sin(a) * radius));
		}
		ribbonPath(p, v, ring, eye, 0.04D, RIBBON, seed + 2, TINT, alpha);
		for (int i = 0; i < 8; i++) {
			double a = i * Math.PI / 4 + time * 0.02D;
			Vec3 outer = centre.add(Math.cos(a) * radius, 0.06D, Math.sin(a) * radius);
			Vec3 inner = centre.add(Math.cos(a) * radius * (1 - 0.35D * fill), 0.06D, Math.sin(a) * radius * (1 - 0.35D * fill));
			ribbon(p, v, inner, outer, eye, 0.025D, SLASH, seed + 3 + i, TINT, alpha * 0.8F, 0, 1);
		}
		// The pool shape lives in the Animus material; other schools' materials would draw the bare card.
		ground(p, pool, centre.add(0, 0.05D, 0), radius, POOL, seed + 12, TINT, alpha * 0.18F * (float) fill);
	}

	private static void brand(PoseStack p, LuxUmbraBatch flows, StrandClientState.Strand strand, Vec3 palm, Vec3 eye,
			Vec3 right, Vec3 up, EnumBloodTemper school, int seed, double time, float partial, float alpha) {
		VertexConsumer cards = flows.vertices(cardMaterial(school));
		if (strand.end == null) return;
		Vec3 end = strand.endPrevious.lerp(strand.end, partial).subtract(palm);
		double progress = BrandRules.progress(strand.brand, StrandSkills.brandSetTicks(strand.cue.skill()));
		VertexConsumer v = flows.vertices(material(school));
		if (school == EnumBloodTemper.DUCTILIS) {
			// A conductive mark is an arc held on the body, re-jagging as it takes, thickening as the brand sets.
			StrandSchoolGeometry.arc(p, v, Vec3.ZERO, end, eye, 0.03D + 0.03D * progress, seed + 5, time, alpha);
			StrandSchoolGeometry.arc(p, v, Vec3.ZERO, end, eye, 0.018D, seed + 6, time + 1.0D, alpha * 0.6F);
		}
		// A reaching tether that pulls taut as the brand takes.
		double sag = 0.35D * (1.0D - progress) * (strand.onCreature ? 1.0D : 1.4D);
		List<Vec3> tether = new ArrayList<>(13);
		for (int i = 0; i <= 12; i++) {
			double t = i / 12.0D;
			tether.add(end.scale(t).add(0, -Math.sin(t * Math.PI) * sag, 0)
					.add(0, Math.sin(t * 7.0D + time * 1.1D) * 0.03D * (1 - progress), 0));
		}
		float tetherAlpha = school == EnumBloodTemper.DUCTILIS ? 0.35F : 1.0F;
		ribbonPath(p, v, tether, eye, 0.09D + 0.04D * progress, 0.06D + 0.06D * progress, WISP, seed + 5, TINT, alpha * 0.5F * tetherAlpha);
		ribbonPath(p, v, tether, eye, 0.06D + 0.03D * progress, 0.04D + 0.05D * progress, RIBBON, seed, TINT, alpha * tetherAlpha);
		ribbonPath(p, v, tether, eye, 0.02D, 0.025D, RIBBON, seed + 1, CORE, alpha * (0.5F + 0.5F * (float) progress));
		if (!strand.onCreature) return;
		// The knot cinches and a ring tightens round the body as the brand takes.
		double knot = 0.14D + 0.22D * progress;
		card(p, cards, end, right, up, knot, knot, FOCUS, seed + 2, CLOT, alpha);
		card(p, cards, end, right, up, knot * 0.5D, knot * 0.5D, FOCUS, seed + 3, CORE, alpha * (float) progress);
		double radius = 0.9D - 0.45D * progress;
		List<Vec3> ring = new ArrayList<>(25);
		for (int i = 0; i <= 24; i++) {
			double a = i * Math.PI / 12 + time * 0.08D;
			ring.add(end.add(right.scale(Math.cos(a) * radius)).add(up.scale(Math.sin(a) * radius)));
		}
		ribbonPath(p, v, ring, eye, 0.035D + 0.04D * progress, RIBBON, seed + 4, TINT, alpha * (float) (0.4D + 0.6D * progress));
	}

	/**
	 * The solid pass: the blood-iron of Ferric strands, flechettes streaming down a jet or thrown across a spray, from
	 * the palm (and the off hand's, with a twin). The blood that carries them is drawn in the flow pass.
	 */
	static void collectIron(PoseStack poses, VertexConsumer iron, Vec3 camera, double time, float partial) {
		var level = Minecraft.getInstance().level;
		if (level == null) return;
		for (StrandClientState.Strand strand : StrandClientState.all()) {
			if (strand.cue == null || strand.cue.primary() != EnumBloodTemper.FERRIC) continue;
			if (strand.form() != StrandForm.JET && strand.form() != StrandForm.SPRAY) continue;
			if (!(level.getEntity(strand.entityId) instanceof LivingEntity caster) || caster.isInvisible()) continue;
			float alpha = strand.presence(time);
			if (alpha <= 0.003F) continue;
			int seed = Math.floorMod(strand.entityId * 31, 113);
			Vec3 palm = palm(caster, strand, partial);
			if (palm.distanceToSqr(camera) > 96 * 96) continue;
			ironBody(poses, iron, strand, caster, palm, camera, seed, time, partial, alpha);
			if (strand.cue.twin())
				ironBody(poses, iron, strand, caster, twinPalm(caster, strand, partial), camera, seed + 17, time, partial, alpha * 0.85F);
		}
	}

	private static void ironBody(PoseStack poses, VertexConsumer iron, StrandClientState.Strand strand, LivingEntity caster,
			Vec3 palm, Vec3 camera, int seed, double time, float partial, float alpha) {
		poses.pushPose();
		poses.translate(palm.x - camera.x, palm.y - camera.y, palm.z - camera.z);
		if (strand.form() == StrandForm.JET) {
			if (strand.end != null) {
				Vec3 end = strand.endPrevious.lerp(strand.end, partial).subtract(palm);
				// The lance's barbs: a Ferric jet whose manipulation is the lance; both needle jets share this shape.
				StrandSchoolGeometry.ironJet(poses, iron, end, strand.jet.pressure(), true, seed, time, alpha);
			}
		} else {
			Vec3 look = caster.getViewVector(partial);
			Vec3 across = VisceralGeometry.side(look), lift = look.cross(across).normalize();
			double spread = Math.tan(Math.acos(strand.cue.twin() ? SprayRules.TWIN_MIN_DOT : SprayRules.MIN_DOT));
			StrandSchoolGeometry.ironFan(poses, iron, look, across, lift, spread, strand.length, seed, time, alpha);
		}
		poses.popPose();
	}

	// ── Accents ──

	/**
	 * One accent, in the flow pass ({@code solids} false, {@code iron} discarding) or the solid pass ({@code solids}
	 * true): the solid pass draws only the iron a Ferric eruption drives up.
	 */
	static void drawAccent(ManipulationVisualPacket packet, PoseStack p, LuxUmbraBatch flows, VertexConsumer iron, boolean solids,
			Vec3 eye, Vec3 right, Vec3 up, double time, float age, float alpha) {
		if (alpha <= 0.0F) return;
		StrandCue.DecodedAccent cue = StrandCue.decodeAccent(packet.count());
		if (cue.accent() == StrandCue.Accent.BEAM) {
			if (!solids) beam(p, flows, packet, eye, right, up, time, age, alpha);
			return;
		}
		if (cue.accent() == StrandCue.Accent.VOLLEY) {
			if (!solids) volley(p, flows, packet, eye, right, up, time, age, alpha);
			return;
		}
		if (cue.accent() == StrandCue.Accent.DOME) {
			if (!solids) dome(p, flows, packet, eye, time, age, alpha);
			return;
		}
		if (cue.accent() == StrandCue.Accent.JAVELIN) {
			if (!solids) javelin(p, flows, packet, eye, right, up, time, age, alpha);
			return;
		}
		if (cue.accent() == StrandCue.Accent.GRASP) {
			// A held cue, kept alive by its Sculpting: it draws by its own age, not by a burst's rise and fall.
			if (!solids) grasp(p, flows, packet, eye, right, up, time, age, alpha);
			return;
		}
		if (solids) {
			if (cue.accent() != StrandCue.Accent.ERUPTION || cue.primary() != EnumBloodTemper.FERRIC) return;
			float life = Math.max(1, packet.ticks());
			float grow = HemoEasing.smoothstep(Math.min(1.0F, age / Math.max(2.0F, life * 0.35F)));
			float fade = alpha * (1.0F - HemoEasing.smoothstep(Math.max(0.0F, (age - life * 0.4F) / (life * 0.6F))));
			if (fade > 0.003F)
				StrandSchoolGeometry.ironSpikes(p, iron, Math.max(0.5D, packet.radius()), Math.floorMod(packet.from().hashCode(), 113),
						age, grow, fade);
			return;
		}
		Vec3 dir = packet.to().subtract(packet.from());
		dir = dir.lengthSqr() < 1e-8 ? new Vec3(0, 1, 0) : dir.normalize();
		Vec3[] basis = BloodShotGeometry.basis(dir);
		int seed = Math.floorMod(packet.from().hashCode(), 113);
		float life = Math.max(1, packet.ticks());
		float grow = HemoEasing.smoothstep(Math.min(1.0F, age / Math.max(2.0F, life * 0.35F)));
		float fade = alpha * (1.0F - HemoEasing.smoothstep(Math.max(0.0F, (age - life * 0.4F) / (life * 0.6F))));
		if (fade <= 0.003F) return;
		EnumBloodTemper school = cue.primary();
		VertexConsumer v = flows.vertices(material(school));
		VertexConsumer cards = flows.vertices(cardMaterial(school));
		switch (cue.accent()) {
			case CRACK -> crack(p, flows, v, cards, school, dir, basis, eye, right, up, seed, time, age, grow, fade);
			case PIERCE -> pierce(p, v, cards, school, dir, basis, eye, right, up, seed, time, grow, fade, packet.radius());
			case DRENCH -> drench(p, v, cards, packet, eye, right, up, seed, time, grow, fade);
			case ERUPTION -> eruption(p, flows, v, cards, school, packet.radius(), eye, right, up, seed, time, age, grow, fade);
			case BRAND -> brandSet(p, v, cards, packet.radius(), eye, right, up, seed, time, grow, fade);
			case GRASP, JAVELIN, DOME, BEAM, VOLLEY -> {
			}
			case TAP -> tap(p, flows, v, cards, school, packet, eye, right, up, seed, time, age, life, alpha);
			case TELEGRAPH -> telegraph(p, v, flows.vertices(LuxUmbraBatch.Material.ANIMUS), Vec3.ZERO, packet.radius() * (0.4D + 0.6D * grow), eye, seed, time, fade, 0.2D);
		}
	}

	/**
	 * Hematic Flare's searchlight, drawn from its caster's palm along their look to the first block in the way, worked
	 * out here every frame so it turns with them without a tick's lag. A wide soft cone of pale light with a hard white
	 * core; where it lands, a pool of light. As heat builds on a body the core thickens and reddens toward the flash.
	 */
	private static void beam(PoseStack p, LuxUmbraBatch flows, ManipulationVisualPacket packet, Vec3 eye, Vec3 right, Vec3 up,
			double time, float age, float alpha) {
		var level = Minecraft.getInstance().level;
		if (alpha <= 0.003F || level == null || !(level.getEntity(packet.entityId()) instanceof LivingEntity caster)) return;
		float partial = com.vincenthuto.hemomancy.client.render.AnimationTime.partialTick();
		Vec3 feet = caster.getPosition(partial);
		Vec3 from = caster.getEyePosition(partial), look = caster.getViewVector(partial);
		Vec3 far = from.add(look.scale(Math.max(2.0D, packet.radius())));
		Vec3 end = level.clip(new net.minecraft.world.level.ClipContext(from, far, net.minecraft.world.level.ClipContext.Block.COLLIDER,
				net.minecraft.world.level.ClipContext.Fluid.NONE, caster)).getLocation().subtract(feet);
		Vec3 start = CellHandStreams.palm(caster, false, partial).subtract(feet);
		double heat = Math.max(0.0D, Math.min(1.0D, packet.to().x));
		int seed = Math.floorMod(packet.entityId() * 31, 113);
		float open = HemoEasing.smoothstep(Math.min(1.0F, age / 6.0F)) * alpha;
		VertexConsumer light = flows.vertices(LuxUmbraBatch.Material.LUX);
		double flicker = 1.0D + 0.06D * Math.sin(time * 1.3D + seed);
		// The cone widens toward its end; the core does not.
		ribbon(p, light, start, end, eye, 1.0D * flicker, WISP, seed + 1, WHITE, open * 0.5F, 0, 1);
		ribbon(p, light, start, end, eye, 0.5D * flicker, WHITE_BEAM, seed + 2, WHITE, open * 0.85F, 0, 1);
		ribbon(p, light, start, end, eye, (0.16D + 0.16D * heat) * flicker, WHITE_BEAM, seed + 3, WHITE, open, 0, 1);
		if (heat > 0.02D) {
			ribbon(p, flows.vertices(LuxUmbraBatch.Material.FLAME), start.lerp(end, 0.55D), end, eye, 0.2D * heat, RIBBON, seed + 4, TINT,
					open * (float) heat, 0, 1);
		}
		card(p, light, end, right, up, 1.3D * flicker, 1.3D * flicker, WISP, seed + 5, WHITE, open * 0.45F);
		card(p, light, end, right, up, 0.5D + 0.6D * heat, 0.5D + 0.6D * heat, FOCUS, seed + 6, WHITE, open);
		card(p, light, start, right, up, 0.22D, 0.22D, FOCUS, seed + 7, WHITE, open);
	}

	/**
	 * Blood Needle Lance's hovering volley, drawn round its caster's head: one barbed iron needle for each that has
	 * formed, in a ring, all pointing where the caster looks and bobbing a little out of step. Each new needle draws in
	 * from a bead of blood.
	 */
	private static void volley(PoseStack p, LuxUmbraBatch flows, ManipulationVisualPacket packet, Vec3 eye, Vec3 right, Vec3 up,
			double time, float age, float alpha) {
		var level = Minecraft.getInstance().level;
		int count = (int) Math.round(packet.radius());
		if (alpha <= 0.003F || count <= 0 || level == null || !(level.getEntity(packet.entityId()) instanceof LivingEntity caster)) return;
		float partial = com.vincenthuto.hemomancy.client.render.AnimationTime.partialTick();
		Vec3 feet = caster.getPosition(partial);
		Vec3 head = caster.getEyePosition(partial).subtract(feet), look = caster.getViewVector(partial);
		Vec3 side = look.cross(new Vec3(0.0D, 1.0D, 0.0D));
		side = side.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D) : side.normalize();
		Vec3 over = side.cross(look).normalize();
		int seed = Math.floorMod(packet.entityId() * 31, 113);
		VertexConsumer iron = flows.vertices(material(EnumBloodTemper.FERRIC));
		VertexConsumer blood = flows.vertices(LuxUmbraBatch.Material.ANIMUS);
		int most = com.vincenthuto.hemomancy.common.manipulation.ferric.NeedleVolleySculpting.MAX_NEEDLES;
		for (int i = 0; i < Math.min(count, most); i++) {
			double angle = (i + 0.5D) * Math.PI * 2.0D / most;
			double bob = 0.03D * Math.sin(time * 0.35D + i * 1.7D);
			Vec3 out = side.scale(Math.cos(angle)).add(over.scale(Math.sin(angle)));
			// A crown ahead of the face, every point leaning in toward the aim: seen from behind them the needles are
			// spokes round the crosshair, not dots end-on.
			Vec3 tail = head.add(look.scale(0.7D)).add(out.scale(0.56D + bob)).add(over.scale(0.1D));
			Vec3 point = head.add(look.scale(1.5D)).add(out.scale(0.3D + bob)).add(over.scale(0.06D));
			Vec3 along = point.subtract(tail).normalize();
			ribbon(p, blood, tail, point, eye, 0.09D, WISP, seed + i, CLOT, alpha * 0.6F, 0, 1);
			ribbon(p, iron, tail, point, eye, 0.04D, RIBBON, seed + 10 + i, TINT, alpha, 0, 1);
			ribbon(p, iron, tail.lerp(point, 0.6D), point, eye, 0.016D, RIBBON, seed + 20 + i, CORE, alpha, 0, 1);
			// The barb: two short cuts back from the point.
			for (int barb = -1; barb <= 1; barb += 2) {
				Vec3 back = point.subtract(along.scale(0.2D)).add(out.scale(0.07D * barb));
				ribbon(p, iron, point, back, eye, 0.022D, RIBBON, seed + 30 + i + barb, TINT, alpha, 0, 1);
			}
			card(p, blood, tail, right, up, 0.07D, 0.07D, FOCUS, seed + 40 + i, CLOT, alpha * 0.9F);
		}
	}

	/**
	 * Blood Eclipse's carried dome, drawn round its caster: ribs of dark rising from a ring on the ground and meeting
	 * overhead, turning slowly, with a pale-edged rim where the dome meets the floor. It is a shell, not a wall: the
	 * ribs are spaced so the room and what stands inside read through them from either side. Its size is the cue's,
	 * eased as the dome grows.
	 */
	private static void dome(PoseStack p, LuxUmbraBatch flows, ManipulationVisualPacket packet, Vec3 eye, double time, float age,
			float alpha) {
		double radius = packet.radius();
		if (alpha <= 0.003F || radius < 0.5D) return;
		int seed = Math.floorMod(packet.entityId() * 31, 113);
		float open = HemoEasing.smoothstep(Math.min(1.0F, age / 10.0F)) * alpha;
		VertexConsumer dark = flows.vertices(LuxUmbraBatch.Material.UMBRA);
		VertexConsumer pale = flows.vertices(LuxUmbraBatch.Material.LUX);
		double turn = time * 0.012D;
		int ribs = 12;
		for (int rib = 0; rib < ribs; rib++) {
			double angle = turn + rib * Math.PI * 2.0D / ribs;
			List<Vec3> path = new ArrayList<>(13);
			for (int k = 0; k <= 12; k++) {
				double t = k / 12.0D;
				// Up the dome's side to its crown, leaning with the turn so the ribs spiral a little.
				double lift = t * Math.PI * 0.5D, around = angle + t * 0.6D;
				path.add(new Vec3(Math.cos(around) * Math.cos(lift) * radius, Math.sin(lift) * radius + 0.05D,
						Math.sin(around) * Math.cos(lift) * radius));
			}
			double[] width = new double[path.size()];
			for (int k = 0; k < width.length; k++) width[k] = radius * 0.07D * (1.0D - 0.75D * k / (width.length - 1.0D));
			ribbonPath(p, dark, path, eye, StrandSchoolGeometry.scaled(width, 2.2D), WISP, seed + rib, TINT, open * 0.45F);
			ribbonPath(p, dark, path, eye, width, RIBBON, seed + 20 + rib, TINT, open * 0.85F);
		}
		// The rim: a dark band on the floor with a thin pale edge, and a second pale ring a third of the way up.
		for (int ring = 0; ring < 2; ring++) {
			double lift = ring == 0 ? 0.0D : Math.PI / 6.0D;
			List<Vec3> loop = new ArrayList<>(49);
			for (int k = 0; k <= 48; k++) {
				double a = k * Math.PI / 24.0D - turn * (ring == 0 ? 1.0D : -1.5D);
				loop.add(new Vec3(Math.cos(a) * Math.cos(lift) * radius, Math.sin(lift) * radius + 0.06D, Math.sin(a) * Math.cos(lift) * radius));
			}
			if (ring == 0) ribbonPath(p, dark, loop, eye, radius * 0.09D, RIBBON, seed + 40, TINT, open);
			ribbonPath(p, pale, loop, eye, radius * (ring == 0 ? 0.02D : 0.012D), RIBBON, seed + 41 + ring, EDGE, open * (ring == 0 ? 0.9F : 0.45F));
		}
	}

	/**
	 * A Cruor Javelin, drawn from a cue keyed to what carries it. Keyed to a player it is the shaft being drawn into
	 * their hand: it grows out of the palm along their look as the cue's size climbs through the stages. Keyed to the
	 * thrown javelin it is the shaft in flight, pointing the way it travels with a tail of frost behind it, and then
	 * standing in the block it stuck in. A shaft of dark cruor glass, a pale cold edge, a bright point.
	 */
	private static void javelin(PoseStack p, LuxUmbraBatch flows, ManipulationVisualPacket packet, Vec3 eye, Vec3 right, Vec3 up,
			double time, float age, float alpha) {
		var level = Minecraft.getInstance().level;
		if (alpha <= 0.003F || level == null) return;
		var carrier = level.getEntity(packet.entityId());
		if (carrier == null) return;
		float partial = com.vincenthuto.hemomancy.client.render.AnimationTime.partialTick();
		int seed = Math.floorMod(packet.entityId() * 31, 113);
		double length = Math.max(0.6D, packet.radius());
		Vec3 butt, dir;
		double trail = 0.0D;
		if (carrier instanceof net.minecraft.world.entity.player.Player caster) {
			// In the hand: the shaft grows from behind the palm forward along the look.
			Vec3 feet = caster.getPosition(partial);
			dir = caster.getViewVector(partial);
			double grown = HemoEasing.smoothstep(Math.min(1.0F, age / 8.0F));
			length *= 0.35D + 0.65D * grown;
			butt = CellHandStreams.palm(caster, false, partial).subtract(feet).subtract(dir.scale(length * 0.3D));
		} else {
			Vec3 motion = carrier.getDeltaMovement();
			if (motion.lengthSqr() > 1.0E-4D) {
				dir = motion.normalize();
				trail = Math.min(1.0D, motion.length() / 2.0D);
			} else {
				// Stuck: an arrow keeps the way it flew in its rotation (yaw from x over z, pitch from y over the run).
				double yaw = Math.toRadians(carrier.getYRot()), pitch = Math.toRadians(carrier.getXRot());
				dir = new Vec3(Math.sin(yaw) * Math.cos(pitch), Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch));
			}
			butt = dir.scale(-length);
		}
		Vec3 point = butt.add(dir.scale(length));
		VertexConsumer glass = flows.vertices(LuxUmbraBatch.Material.CRUOR);
		VertexConsumer pale = flows.vertices(LuxUmbraBatch.Material.RIME);
		List<Vec3> shaft = new ArrayList<>(9);
		for (int i = 0; i <= 8; i++) shaft.add(butt.lerp(point, i / 8.0D));
		double[] body = new double[shaft.size()], core = new double[shaft.size()];
		for (int i = 0; i < body.length; i++) {
			double t = i / (body.length - 1.0D);
			// Thickest a third of the way up, drawn to a point: a spear, not a rod.
			body[i] = 0.2D * Math.sin(Math.PI * Math.min(1.0D, 0.25D + t * 0.75D)) + 0.02D;
			core[i] = body[i] * 0.3D;
		}
		ribbonPath(p, pale, shaft, eye, StrandSchoolGeometry.scaled(body, 1.7D), WISP, seed + 3, TINT, alpha * 0.6F);
		ribbonPath(p, glass, shaft, eye, body, RIBBON, seed, TINT, alpha);
		ribbonPath(p, glass, shaft, eye, StrandSchoolGeometry.scaled(body, 0.7D), RIBBON, seed + 6, TINT, alpha);
		ribbonPath(p, pale, shaft, eye, core, RIBBON, seed + 1, TINT, alpha);
		card(p, pale, point, right, up, 0.22D, 0.22D, FOCUS, seed + 2, TINT, alpha);
		if (trail > 0.02D) {
			// Frost torn off the shaft as it flies.
			List<Vec3> wake = new ArrayList<>(7);
			for (int i = 0; i <= 6; i++) {
				double t = i / 6.0D;
				wake.add(butt.subtract(dir.scale(t * 2.2D * trail)).add(0.0D, Math.sin(time * 0.6D + t * 7.0D + seed) * 0.04D * t, 0.0D));
			}
			ribbonPath(p, pale, wake, eye, 0.16D, 0.02D, WISP, seed + 5, TINT, alpha * 0.7F * (float) trail);
		}
	}

	/**
	 * Glacial Grasp's hand of ice, drawn at the feet of the creature it holds. Four fingers and a thumb of dark cruor
	 * rise out of a frozen palm on the ground and curl in over the creature as the hand forms; once formed they stay
	 * closed, breathing slightly, edged in pale cold light with frost running up them.
	 */
	private static void grasp(PoseStack p, LuxUmbraBatch flows, ManipulationVisualPacket packet, Vec3 eye, Vec3 right, Vec3 up,
			double time, float age, float alpha) {
		if (alpha <= 0.003F) return;
		var level = Minecraft.getInstance().level;
		double height = level != null && level.getEntity(packet.entityId()) instanceof LivingEntity held ? held.getBbHeight() : 1.8D;
		double girth = Math.max(0.5D, packet.radius());
		int seed = Math.floorMod(packet.entityId() * 31, 113);
		double formed = com.vincenthuto.hemomancy.common.manipulation.congeatio.GlacialGraspRules.formed(age);
		double closed = HemoEasing.smoothstep((float) formed);
		double breath = 1.0D + 0.03D * Math.sin(time * 0.25D);
		VertexConsumer glass = flows.vertices(LuxUmbraBatch.Material.CRUOR);
		VertexConsumer pale = flows.vertices(LuxUmbraBatch.Material.RIME);
		// The palm: frost spreading on the ground under the creature.
		ground(p, pale, new Vec3(0.0D, 0.04D, 0.0D), girth * (1.0D + 0.8D * closed), POOL, seed, TINT, alpha * 0.8F);
		ground(p, pale, new Vec3(0.0D, 0.05D, 0.0D), girth * (1.3D + 0.9D * closed), RIPPLE, seed + 1, TINT, alpha * 0.5F);
		// The wrist faces the caster, so the fingers come up on the far side and the thumb on the near one.
		Vec3 toCaster = packet.to().subtract(packet.from()).multiply(1.0D, 0.0D, 1.0D);
		double facing = toCaster.lengthSqr() < 1.0E-6D ? 0.0D : Math.atan2(toCaster.z, toCaster.x);
		double[] spread = {Math.PI - 0.75D, Math.PI - 0.25D, Math.PI + 0.25D, Math.PI + 0.75D, 0.0D};
		for (int finger = 0; finger < spread.length; finger++) {
			boolean thumb = finger == spread.length - 1;
			double angle = facing + spread[finger];
			double reach = height * (thumb ? 0.75D : 1.15D) * (0.25D + 0.75D * closed);
			List<Vec3> path = new ArrayList<>(10);
			for (int k = 0; k <= 9; k++) {
				double t = k / 9.0D;
				// Out from the palm, up the creature's side, and in over it: further in the more the hand has closed.
				double out = girth * breath * (1.15D - 0.8D * closed * t * t);
				double lift = reach * Math.sin(t * Math.PI * 0.5D);
				path.add(new Vec3(Math.cos(angle) * out, lift, Math.sin(angle) * out));
			}
			double[] width = new double[path.size()], core = new double[path.size()];
			for (int k = 0; k < width.length; k++) {
				double t = k / (width.length - 1.0D);
				width[k] = (thumb ? 0.44D : 0.36D) * (1.0D - 0.6D * t) * (0.5D + 0.5D * closed);
				core[k] = width[k] * 0.3D;
			}
			ribbonPath(p, pale, path, eye, StrandSchoolGeometry.scaled(width, 1.5D), WISP, seed + 10 + finger, TINT, alpha * 0.6F);
			ribbonPath(p, glass, path, eye, width, RIBBON, seed + 2 + finger, TINT, alpha);
			ribbonPath(p, glass, path, eye, StrandSchoolGeometry.scaled(width, 0.7D), RIBBON, seed + 40 + finger, TINT, alpha);
			ribbonPath(p, pale, path, eye, core, RIBBON, seed + 20 + finger, TINT, alpha * (float) (0.5D + 0.5D * closed));
			// A knuckle of frost at each fingertip once it has closed.
			Vec3 tip = path.getLast();
			card(p, pale, tip, right, up, 0.2D * closed, 0.2D * closed, FOCUS, seed + 30 + finger, TINT, alpha * (float) closed);
		}
	}

	/**
	 * The tap: a lash that unfurls from the caster's hand to its mark. It leaves the palm folded under itself and opens
	 * upward and outward, the straight part growing from the hand while the rest is still a coil hanging below the aim;
	 * the coil lifts into line as it goes and the tip snaps straight onto its mark, where it cracks. Then it is drawn
	 * back in from the hand end. It is as thick as a held lash and wears its school the same way.
	 *
	 * <p>The burst is sent from where the shared rules put the hand; it is drawn from the hand the client actually
	 * shows, in first person or third, so the lash leaves the palm and follows it while it is out.
	 */
	private static void tap(PoseStack p, LuxUmbraBatch flows, VertexConsumer v, VertexConsumer cards, EnumBloodTemper school,
			ManipulationVisualPacket packet, Vec3 eye, Vec3 right, Vec3 up, int seed, double time, float age, float life,
			float alpha) {
		float out = HemoEasing.smoothstep(Math.min(1.0F, age / Math.max(1.0F, life * 0.4F)));
		float back = HemoEasing.smoothstep(Math.max(0.0F, (age - life * 0.6F) / (life * 0.4F)));
		float fade = alpha * (1.0F - back);
		if (fade <= 0.003F) return;
		// Everything here is drawn relative to the burst's origin; the lash starts at the drawn palm instead.
		Vec3 start = tapPalm(packet).subtract(packet.from());
		Vec3 span = packet.to().subtract(packet.from()).subtract(start);
		double reach = span.length();
		if (reach < 1.0E-3D) return;
		Vec3 dir = span.scale(1.0D / reach);
		// The coil turns in the plane of the aim and straight down: folded below the line, it opens upward into it.
		Vec3 below = new Vec3(0.0D, -1.0D, 0.0D);
		below = below.subtract(dir.scale(below.dot(dir)));
		below = below.lengthSqr() < 1.0E-6D ? right : below.normalize();
		int segments = 28;
		double step = reach / segments;
		// How much of the lash has straightened, and how tightly the rest is still folded.
		double straight = out;
		double roll = (1.0D - out) * Math.PI * 3.2D;
		List<Vec3> path = new ArrayList<>(segments + 1);
		Vec3 at = start;
		path.add(at);
		for (int i = 1; i <= segments; i++) {
			double u = i / (double) segments;
			double curled = Math.max(0.0D, (u - straight) / Math.max(1.0E-3D, 1.0D - straight));
			double angle = roll * curled * curled;
			// The folded part is wound tight: its links are drawn short so the coil sits ahead of the hand, not across the room.
			double length = step * (1.0D - 0.75D * (1.0D - out) * curled);
			at = at.add(dir.scale(Math.cos(angle) * length)).add(below.scale(Math.sin(angle) * length));
			path.add(at);
		}
		// Drawn back in: the hand end lets go first.
		int from = Math.min(segments - 1, (int) (back * segments));
		List<Vec3> drawn = path.subList(from, path.size());
		double[] body = new double[drawn.size()], core = new double[drawn.size()];
		for (int i = 0; i < body.length; i++) {
			double t = i / Math.max(1.0D, body.length - 1.0D);
			double wave = 1.0D + 0.18D * Math.sin(time * 0.9D - t * 9.0D + seed);
			body[i] = (0.13D * (1.0D - t) + 0.07D * t) * wave;
			core[i] = body[i] * 0.3D;
		}
		// A many-tailed lash taps with every tail: they part as it straightens.
		var level = Minecraft.getInstance().level;
		var tapper = level == null ? null : level.getNearestPlayer(packet.from().x, packet.from().y, packet.from().z, 2.5D, false);
		var style = StrandClientState.style(tapper);
		if (style.faint() && tapper != Minecraft.getInstance().player) fade *= FAINT;
		for (Vec3 tailTip : dressTailed(p, flows, school, null, drawn, eye, body, core, seed, time, fade, 1.0F, style.tails(), 0.4D * out)) {
			card(p, cards, tailTip, right, up, 0.12D, 0.12D, FOCUS, seed + 2, TINT, fade);
		}
		Vec3 tip = path.getLast();
		// The tip glows hot as it runs out, and cracks where it lands.
		float lead = out * (1.0F - back);
		card(p, cards, tip, right, up, 0.16D + 0.2D * lead, 0.16D + 0.2D * lead, FOCUS, seed + 2, TINT, fade);
		card(p, cards, tip, right, up, 0.07D + 0.1D * lead, 0.07D + 0.1D * lead, FOCUS, seed + 4, CORE, fade * lead);
		float landed = age - life * 0.4F;
		if (landed < 0.0F) return;
		float burst = HemoEasing.smoothstep(Math.min(1.0F, landed / Math.max(1.0F, life * 0.45F)));
		float spent = alpha * (1.0F - HemoEasing.smoothstep(Math.max(0.0F, (landed - life * 0.2F) / (life * 0.4F))));
		if (spent <= 0.003F) return;
		p.pushPose();
		p.translate(tip.x, tip.y, tip.z);
		p.scale(0.7F, 0.7F, 0.7F);
		crack(p, flows, v, cards, school, dir, BloodShotGeometry.basis(dir), eye.subtract(tip).scale(1.0D / 0.7D), right, up, seed + 60,
				time, landed, burst, spent);
		p.popPose();
	}

	/**
	 * Where a tap leaves the body: the drawn palm of the player who struck it, found as the player standing where the
	 * burst was sent from, on the side that burst's origin sits. The burst's own origin when no such player is in view.
	 */
	private static Vec3 tapPalm(ManipulationVisualPacket packet) {
		var level = Minecraft.getInstance().level;
		Vec3 sent = packet.from();
		if (level == null) return sent;
		var caster = level.getNearestPlayer(sent.x, sent.y, sent.z, 2.5D, false);
		if (caster == null) return sent;
		float partial = com.vincenthuto.hemomancy.client.render.AnimationTime.partialTick();
		Vec3 eyes = caster.getEyePosition(partial), look = caster.getViewVector(partial);
		float yaw = caster.getViewYRot(partial);
		boolean left = StrandCue.hand(eyes, look, yaw, true).distanceToSqr(sent) < StrandCue.hand(eyes, look, yaw, false).distanceToSqr(sent);
		return CellHandStreams.palm(caster, left, partial);
	}

	/** The crack: a hot flash at the tip, a ring that snaps out and spurs flung along the stroke, then the school's flourish. */
	private static void crack(PoseStack p, LuxUmbraBatch flows, VertexConsumer v, VertexConsumer cards, EnumBloodTemper school, Vec3 dir, Vec3[] basis,
			Vec3 eye, Vec3 right, Vec3 up, int seed, double time, float age, float grow, float fade) {
		float flash = 1.0F - HemoEasing.smoothstep(Math.min(1.0F, age / 4.0F));
		card(p, cards, Vec3.ZERO, right, up, 0.45D + 0.95D * flash, 0.45D + 0.95D * flash, FOCUS, seed, TINT, fade);
		card(p, cards, Vec3.ZERO, right, up, 0.2D + 0.45D * flash, 0.2D + 0.45D * flash, FOCUS, seed + 1, CORE,
				fade * (0.3F + 0.7F * flash));
		double radius = 0.25D + 1.3D * grow;
		List<Vec3> ring = new ArrayList<>(25);
		for (int i = 0; i <= 24; i++) {
			double a = i * Math.PI / 12;
			ring.add(basis[0].scale(Math.cos(a) * radius).add(basis[1].scale(Math.sin(a) * radius)));
		}
		ribbonPath(p, v, ring, eye, 0.1D * (1.0D - grow) + 0.025D, RIBBON, seed + 2, TINT, fade);
		ribbonPath(p, v, ring, eye, 0.03D * (1.0D - grow) + 0.008D, RIBBON, seed + 8, CORE, fade * (1.0F - grow));
		for (int i = 0; i < 7; i++) {
			Vec3 spur = cone(dir, basis[0], basis[1], i, seed, 1.3D);
			stream(p, v, spur.scale(0.1D), spur.scale(0.35D + 1.5D * grow), eye, 0.08D * (1.0D - 0.5D * grow), 0.15D,
					time * 0.4D + i, SLASH, seed + 3 + i, TINT, fade);
		}
		if (school == EnumBloodTemper.FLAMMEUS) {
			VertexConsumer flame = flows.vertices(LuxUmbraBatch.Material.FLAME);
			for (int k = 0; k < 5; k++) {
				double a = k * GOLDEN + seed;
				Vec3 lean = dir.scale(0.4D).add(basis[0].scale(Math.cos(a) * 0.6D)).add(new Vec3(0, 0.35D, 0));
				ThermalGeometry.flameRibbon(p, flame, ThermalGeometry.tongue(Vec3.ZERO, lean, 0.5D * grow + 0.1D, time, seed + 10 + k),
						eye, 0.06D, seed + 10 + k, fade * 0.9F);
			}
		} else if (school == EnumBloodTemper.CONGEATIO) {
			shards(p, flows.vertices(LuxUmbraBatch.Material.GLASS), dir, basis, seed, age, grow, fade, 4, 0.18D);
		} else if (school == EnumBloodTemper.TENEBRIS) {
			// The gloam's crack: a dark tear edged in cold light, its cuts flung wide along the stroke.
			VertexConsumer pale = flows.vertices(LuxUmbraBatch.Material.LUX);
			card(p, pale, Vec3.ZERO, right, up, 0.9D * (1.0D - 0.5D * grow), 0.9D * (1.0D - 0.5D * grow), WISP, seed + 30, EDGE, fade * 0.6F);
			ribbonPath(p, pale, ring, eye, 0.06D * (1.0D - grow) + 0.02D, RIBBON, seed + 31, EDGE, fade);
			for (int i = 0; i < 5; i++) {
				Vec3 cut = cone(dir, basis[0], basis[1], i, seed + 40, 1.6D).scale(0.4D + 1.8D * grow);
				stream(p, pale, cut.scale(0.15D), cut, eye, 0.07D * (1.0D - 0.4D * grow), 0.12D, time * 0.4D + i, SLASH, seed + 41 + i,
						EDGE, fade * 0.9F);
				stream(p, v, cut.scale(0.15D), cut, eye, 0.045D, 0.12D, time * 0.4D + i, SLASH, seed + 51 + i, TINT, fade);
			}
		}
	}

	/** The pierce: a white-hot spear driven through the point along the jet, and a ring blown open where it entered. */
	private static void pierce(PoseStack p, VertexConsumer v, VertexConsumer cards, EnumBloodTemper school, Vec3 dir, Vec3[] basis, Vec3 eye,
			Vec3 right, Vec3 up, int seed, double time, float grow, float fade, double power) {
		double reach = 0.6D + 2.2D * grow * Math.min(2.0D, power);
		ribbon(p, v, dir.scale(-0.6D), dir.scale(reach), eye, 0.16D * (1.0D - grow) + 0.03D, school == EnumBloodTemper.LUX
				? WHITE_BEAM : RIBBON, seed, TINT, fade, 0, 1);
		ribbon(p, v, dir.scale(-0.3D), dir.scale(reach * 0.9D), eye, 0.05D * (1.0D - grow) + 0.01D, RIBBON, seed + 1, CORE, fade, 0, 1);
		double ring = 0.15D + 0.7D * grow;
		for (int i = 0; i < 10; i++) {
			double a = i * Math.PI / 5 + seed;
			Vec3 point = basis[0].scale(Math.cos(a) * ring).add(basis[1].scale(Math.sin(a) * ring));
			card(p, cards, point, right, up, 0.07D, 0.07D, FOCUS, seed + 2 + i, TINT, fade * (1.0F - 0.5F * grow));
		}
		card(p, cards, Vec3.ZERO, right, up, 0.35D * (1.0D - grow) + 0.1D, 0.35D * (1.0D - grow) + 0.1D, FOCUS, seed + 20, CORE, fade);
	}

	/** The drench: the spray thrown out at once, twice as thick and half again as far. */
	private static void drench(PoseStack p, VertexConsumer v, VertexConsumer cards, ManipulationVisualPacket packet, Vec3 eye, Vec3 right, Vec3 up,
			int seed, double time, float grow, float fade) {
		Vec3 axis = packet.to().subtract(packet.from());
		if (axis.lengthSqr() < 1e-6) return;
		Vec3 look = axis.normalize();
		Vec3 across = VisceralGeometry.side(look), lift = look.cross(across).normalize();
		double spread = Math.tan(Math.acos(SprayRules.MIN_DOT));
		for (int i = 0; i < 14; i++) {
			double angle = i * GOLDEN + seed;
			double r = spread * Math.sqrt((i + 0.5D) / 14.0D);
			Vec3 dir = look.add(across.scale(Math.cos(angle) * r)).add(lift.scale(Math.sin(angle) * r)).normalize();
			Vec3 end = dir.scale(packet.radius() * (0.3D + 0.7D * grow));
			stream(p, v, Vec3.ZERO, end, eye, 0.06D, 0.4D, time * 0.4D + i, WISP, seed + i, TINT, fade * 0.9F);
			card(p, cards, end, right, up, 0.2D, 0.2D, WISP, seed + 20 + i, TINT, fade * 0.7F);
		}
	}

	/** The eruption: a ring thrown out over the ground and the school's shape bursting up around it. */
	private static void eruption(PoseStack p, LuxUmbraBatch flows, VertexConsumer v, VertexConsumer cards, EnumBloodTemper school, double radius,
			Vec3 eye, Vec3 right, Vec3 up, int seed, double time, float age, float grow, float fade) {
		double r = Math.max(0.5D, radius);
		List<Vec3> ring = new ArrayList<>(33);
		for (int i = 0; i <= 32; i++) {
			double a = i * Math.PI / 16;
			ring.add(new Vec3(Math.cos(a) * r * grow, 0.08D, Math.sin(a) * r * grow));
		}
		ribbonPath(p, v, ring, eye, 0.12D * (1.0D - grow) + 0.03D, RIBBON, seed, TINT, fade);
		ground(p, flows.vertices(LuxUmbraBatch.Material.ANIMUS), new Vec3(0, 0.06D, 0), r * grow, POOL, seed + 1, TINT, fade * 0.35F);
		// A stain run's small cells throw a few spikes each, a full ring twelve: a twenty-cell run is not 240 spikes.
		int spikes = StrandSchoolGeometry.spikeCount(radius);
		if (school == EnumBloodTemper.FLAMMEUS) {
			// The fire column: a tall tongue out of the middle, as wide as the ring is.
			VertexConsumer flame = flows.vertices(LuxUmbraBatch.Material.FLAME);
			double tall = (1.2D + 1.6D * Math.min(1.0D, radius / 4.0D)) * grow * (1.0D - 0.4D * Math.max(0, (age - 12) / 12.0D));
			for (int k = 0; k < 3; k++) {
				Vec3 lean = new Vec3(Math.cos(seed + k * 2.1D) * 0.12D, 0, Math.sin(seed + k * 2.1D) * 0.12D);
				ThermalGeometry.flameRibbon(p, flame, ThermalGeometry.tongue(lean.scale(0.5D), lean, tall * (1.0D - k * 0.18D), time,
						seed + 40 + k), eye, 0.16D * Math.max(0.5D, Math.min(1.0D, r / 2.0D)), seed + 40 + k, fade);
			}
		}
		for (int i = 0; i < spikes; i++) {
			double a = i * Math.PI * 2 / spikes + seed;
			double at = r * (0.35D + 0.6D * ((i * 0.618D) % 1.0D));
			Vec3 root = new Vec3(Math.cos(a) * at, 0.0D, Math.sin(a) * at);
			double height = (0.6D + 0.9D * ((i * 0.37D) % 1.0D)) * grow * (1.0D - 0.35D * Math.max(0, (age - 12) / 12.0D));
			if (school == EnumBloodTemper.CONGEATIO) {
				p.pushPose();
				p.translate(root.x, root.y + height * 0.45D, root.z);
				p.mulPose(Axis.YP.rotation((float) a));
				p.mulPose(Axis.ZP.rotation((float) (Math.sin(a * 3) * 0.25D)));
				ThermalGeometry.shard(p, flows.vertices(LuxUmbraBatch.Material.GLASS), Math.max(0.05D, height * 0.55D), seed + i, fade);
				p.popPose();
			} else if (school == EnumBloodTemper.FLAMMEUS) {
				ThermalGeometry.flameRibbon(p, flows.vertices(LuxUmbraBatch.Material.FLAME),
						ThermalGeometry.tongue(root, root.normalize().scale(0.2D), height, time, seed + i), eye, 0.09D, seed + i, fade);
			} else if (school == EnumBloodTemper.FERRIC) {
				// The spikes themselves are iron in the solid pass; blood wells up round each root.
				Vec3 tip = root.add(0, 0.25D * height, 0);
				stream(p, v, root, tip, eye, 0.05D, 0.08D, time * 0.3D + i, SLASH, seed + 2 + i, TINT, fade * 0.8F);
			} else {
				Vec3 tip = root.add(root.normalize().scale(0.15D * height)).add(0, height, 0);
				stream(p, v, root, tip, eye, 0.07D * (1.0D - 0.5D * grow) + 0.02D, 0.1D, time * 0.3D + i, SLASH, seed + 2 + i,
						TINT, fade);
			}
		}
		card(p, cards, new Vec3(0, 0.4D * grow, 0), right, up, 0.6D * (1.0D - grow) + 0.15D, 0.6D * (1.0D - grow) + 0.15D, FOCUS,
				seed + 30, CORE, fade);
	}

	/** The brand setting: a glyph ring that contracts into the body, three cuts across it, and a hot core. */
	private static void brandSet(PoseStack p, VertexConsumer v, VertexConsumer cards, double size, Vec3 eye, Vec3 right, Vec3 up, int seed, double time,
			float grow, float fade) {
		double radius = size * (1.3D - 0.8D * grow);
		List<Vec3> ring = new ArrayList<>(25);
		for (int i = 0; i <= 24; i++) {
			double a = i * Math.PI / 12 + time * 0.1D;
			ring.add(right.scale(Math.cos(a) * radius).add(up.scale(Math.sin(a) * radius)));
		}
		ribbonPath(p, v, ring, eye, 0.08D, RIBBON, seed, TINT, fade);
		for (int i = 0; i < 3; i++) {
			double a = i * Math.PI / 3 + seed;
			Vec3 cut = right.scale(Math.cos(a)).add(up.scale(Math.sin(a))).scale(radius * 0.9D);
			stream(p, v, cut.scale(-1), cut, eye, 0.04D, 0.08D, time * 0.2D + i, SLASH, seed + 1 + i, TINT, fade);
		}
		card(p, cards, Vec3.ZERO, right, up, 0.3D * (1.0D - grow) + 0.12D, 0.3D * (1.0D - grow) + 0.12D, FOCUS, seed + 5, CLOT, fade);
		card(p, cards, Vec3.ZERO, right, up, 0.12D, 0.12D, FOCUS, seed + 6, CORE, fade * (1.0F - grow * 0.5F));
	}

	private static void shards(PoseStack p, VertexConsumer glass, Vec3 dir, Vec3[] basis, int seed, float age, float grow,
			float fade, int count, double size) {
		for (int i = 0; i < count; i++) {
			Vec3 point = cone(dir, basis[0], basis[1], i, seed + 3, 1.0D).scale((0.15D + 0.8D * grow));
			p.pushPose();
			p.translate(point.x, point.y, point.z);
			p.mulPose(Axis.YP.rotation((float) (seed + i * 1.3D + age * 0.2D)));
			p.mulPose(Axis.ZP.rotation((float) (i * 0.9D + age * 0.15D)));
			ThermalGeometry.shard(p, glass, size, seed + i, fade);
			p.popPose();
		}
	}

	/** A unit direction spread around {@code axis}: {@code spread} 0 is the axis itself, 1 roughly a hemisphere. */
	private static Vec3 cone(Vec3 axis, Vec3 across, Vec3 lift, int i, int seed, double spread) {
		double a = i * GOLDEN + seed, r = spread * Math.sqrt((i + 0.5D) / 8.0D);
		return axis.add(across.scale(Math.cos(a) * r)).add(lift.scale(Math.sin(a) * r)).normalize();
	}
}
