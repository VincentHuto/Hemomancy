package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathSpring;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielAnimState;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielFireAnchors;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.world.PutricielTelegraphPass.Scene;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintControllerRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveView;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielBodyFireRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielBodyFireRules.Flame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielBodyFireRules.Moment;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielBodyFireRules.Region;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielDeeds;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielOpenings;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielStage;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The purifier on fire (design revision 2026-11-06, R1): real flame on the body through the cleansing interval, far
 * beyond the ember seams. Each of the body's fire anchors carries a cluster in the revision's layer order (soot over
 * it, the crimson outer flame, the hotter core licking inside, the bed of coals it rises from), and the structure
 * burns as a whole: the staff burns along its length (two tongues wrapping each wrap, a sleeve of thin tongues
 * hugging the shaft from the grip to the head, a three-pronged torch at the head, the foot ember-tipped), the censers
 * roar up through their lids over beds of coals and stream flame behind them, the back colony burns along every
 * shelf's rim under a broad back column with a soot plume, the shoulders and arms smoulder under heavy smoke, the
 * crown is a candelabrum lighting the hood from below, the chest sacs breathe fire and the hem burns low. Everything
 * follows motion (tongues drag back along a swing on springs, stretch with speed and tear off a fast limb), and the
 * body sheds as it burns: ash flakes, burning fragments of shelf at the big commits, ember sparks as accents and
 * scorched prints where it strides. All of it reads {@link PutricielBodyFireRules}: it builds over the Ignition from
 * smoke to the commit's flare, follows the Kindling down, peaks with the moves, draws down to the palm at an Offering
 * and dies back at the Embering. The rot phases carry none of it. Points are camera-relative.
 */
final class PutricielBodyFireGeometry {
	/** The frame's budget for one body (R1.9): tongues (the stream trains inside it), smoke cards and shed pieces. */
	static final int TONGUES = 220, SMOKE = 60, SHED = 40;
	/** Where the grip sits along the staff from its foot. */
	static final double GRIP = 0.42D;
	/**
	 * The body's fire as drawn against R1.1's heights (revision polish, 2026-11-06): at R1.1's numbers the tongues read
	 * as red flecks on a five-block body from the boss camera, so every body tongue stands this much taller and wider.
	 * The rules keep R1.1's numbers; only the drawing scales them.
	 */
	static final double BOLD_HEIGHT = 1.75D, BOLD_WIDTH = 1.6D;
	/** A scorched print is left every half stride and lasts this long (R1.4). */
	static final double STEP = 0.77D;
	static final float PRINT_LIFE = 40.0F, PRINT_FLAME = 8.0F;
	private static final int PRINTS = 16;
	/** The stream behind the staff head and the censers: their last six ticks of path. */
	private static final int STREAM_SAMPLES = 12;
	private static final float STREAM_TICKS = 6.0F;
	/** Ember sparks a second under All particles (Decreased halves them, Minimal shows none). */
	private static final float SPARKS_A_SECOND = 6.0F;
	private static final ParticleColor CRIMSON = new ParticleColor(214, 30, 46), PINK_WHITE = new ParticleColor(255, 168, 190);
	private static final Map<PutricielEntity, Memory> MEMORY = new WeakHashMap<>();

	private PutricielBodyFireGeometry() {
	}

	/** One place fire stands this frame: camera-relative, with its outward normal, its world velocity and its fire's keys. */
	record Site(Vec3 at, Vec3 normal, Vec3 velocity, Region region, double size, double place, boolean palmArm, int key) {
		double speed() {
			return velocity.length();
		}
	}

	/** How much one body may still draw this frame. */
	static final class Budget {
		int tongues = TONGUES, smoke = SMOKE, shed = SHED;

		boolean tongue() {
			return tongues-- > 0;
		}

		boolean smoke() {
			return smoke-- > 0;
		}

		boolean shed() {
			return shed-- > 0;
		}
	}

	/**
	 * The structure the fire burns on, camera-relative: the staff, the censers, the core, the palm and the face, the
	 * body's facing, the floor's height and its velocity (blocks a tick), and the camera itself (world).
	 */
	record Frame(Vec3 staffFoot, Vec3 staffHead, Vec3[] censers, Vec3 core, Vec3 palm, Vec3 face, Vec3 forward, Vec3 right, double floor,
			Vec3 bodyVelocity, Vec3 camera) {
	}

	/** A body's memory between frames: the tips' springs, its scorched prints, the shed bursts and the ember throttle. */
	static final class Memory {
		float lastAge = Float.NaN, dt;
		final Map<Integer, HemorathSpring[]> tips = new HashMap<>();
		final ArrayDeque<double[]> prints = new ArrayDeque<>();
		final ArrayDeque<double[]> bursts = new ArrayDeque<>();
		final HemorathSpring ease = new HemorathSpring();
		double walked;
		Vec3 last;
		boolean left;
		int burstSerial = -1;
		double sparkDebt;

		/** A later age is a new frame; only the first call a frame advances. */
		boolean advance(float age) {
			boolean fresh = Float.isNaN(lastAge) || age > lastAge + 1.0E-4F || age < lastAge - 1.0F;
			if (fresh) {
				dt = Float.isNaN(lastAge) || age < lastAge ? 0.0F : Math.min(4.0F, age - lastAge);
				lastAge = age;
			}
			return fresh;
		}

		/** A tongue's tip direction, leaned on its spring toward {@code want}. */
		Vec3 tip(int key, Vec3 want, boolean fresh) {
			HemorathSpring[] spring = tips.computeIfAbsent(key, k -> new HemorathSpring[]{new HemorathSpring(), new HemorathSpring(),
					new HemorathSpring()});
			float step = fresh ? dt : 0.0F;
			float f = PutricielBodyFireRules.FOLLOW_FREQUENCY, d = PutricielBodyFireRules.FOLLOW_DAMPING;
			Vec3 tip = new Vec3(spring[0].step((float) want.x, step, f, d), spring[1].step((float) want.y, step, f, d),
					spring[2].step((float) want.z, step, f, d));
			return tip.lengthSqr() < 1.0E-6D ? new Vec3(0, 1, 0) : PutricielBodyFireRules.clampLean(tip.normalize());
		}
	}

	// ── the entry ────────────────────────────────────────────────────

	/** Called by the pass for each Putriciel in each of its layers. */
	static void collect(Scene s, PutricielEntity boss) {
		PutricielAnimState motion = PutricielAnimState.of(boss);
		motion.update(boss, s.partial());
		float age = AnimationTime.age(boss, s.partial());
		Memory memory = MEMORY.computeIfAbsent(boss, b -> new Memory());
		boolean fresh = memory.advance(age);
		Moment m = moment(boss, motion, s.partial());
		Vec3 body = boss.getPosition(s.partial());
		Vec3 bodyVelocity = new Vec3(boss.getX() - boss.xo, boss.getY() - boss.yo, boss.getZ() - boss.zo);
		double yaw = Math.toRadians(Mth.rotLerp(s.partial(), boss.yBodyRotO, boss.yBodyRot));
		Vec3 forward = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw)), right = new Vec3(-Math.cos(yaw), 0.0D, -Math.sin(yaw));
		boolean burning = PutricielBodyFireRules.drawsBody(m);
		if (fresh) remember(memory, boss, motion, body, bodyVelocity, forward, right, m, age);
		if (s.ground()) {
			prints(s, memory, age, true);
			return;
		}
		Frame frame = frame(s, motion, body, forward, right, boss.getY(), bodyVelocity);
		Budget budget = new Budget();
		if (s.solid()) {
			fragments(s, memory, age, budget, true);
			return;
		}
		if (!s.spore()) return;
		prints(s, memory, age, false);
		fragments(s, memory, age, budget, false);
		if (!burning) return;
		float ease = memory.ease.step(offeringTarget(m), fresh ? memory.dt : 0.0F, 0.6F, 1.0F);
		float settle = settleFactor(m, ease);
		List<Site> sites = sites(s, boss, motion, body, frame, bodyVelocity);
		draw(s.p(), s.flame(), s.spores(), sites, frame, m, s.time(), age, s.right(), s.up(), budget, memory, fresh, settle, boss);
		if (fresh) sparks(s, boss, memory, sites, frame, m);
	}

	/** The moment the rules read, from the synced state the client already has (no packet). */
	static Moment moment(PutricielEntity boss, PutricielAnimState motion, float partial) {
		SaintMoveView view = boss.view();
		PutricielMove move = PutricielMove.byId(view.moveId());
		int commit = 0, active = 0;
		if (move != null) {
			BossMoveTimeline timeline = SaintControllerRules.effective(move.kind(), move.timeline(view.inputs()));
			commit = timeline.commitTick();
			active = timeline.active();
		}
		float sinceHit = boss.hurtTime > 0 ? boss.hurtDuration - boss.hurtTime + partial : -1.0F;
		return new Moment(motion.stage(), motion.stageTime(), motion.kindling(), PutricielDeeds.repose(boss.syncedDeeds()),
				move == null ? 0 : move.id(), move == null ? -1.0F : motion.moveTime(), commit, active, motion.openingId(), motion.openingTime(),
				motion.sinceAccepted(), sinceHit, motion.endingTime());
	}

	/** The Offering's draw-down as one body-wide value: the spring eases the body back up when an unanswered one closes. */
	private static float offeringTarget(Moment m) {
		boolean open = m.moveId() == PutricielMove.OFFERING.id() || m.openingId() == PutricielOpenings.OFFERING_ID;
		return open ? PutricielBodyFireRules.OFFERING_DRAW : 1.0F;
	}

	/** Once the Offering has closed, the body climbs back to its burn on the eased value instead of at once. */
	private static float settleFactor(Moment m, float ease) {
		boolean open = m.moveId() == PutricielMove.OFFERING.id() || m.openingId() == PutricielOpenings.OFFERING_ID;
		boolean accepted = m.sinceAccepted() >= 0.0F && m.sinceAccepted() < 30.0F;
		return open || accepted ? 1.0F : HemoEasing.clamp01(ease);
	}

	/** The structure in camera space. */
	private static Frame frame(Scene s, PutricielAnimState motion, Vec3 body, Vec3 forward, Vec3 right, double floor, Vec3 velocity) {
		Vec3[] censers = new Vec3[motion.censers().length];
		for (int i = 0; i < censers.length; i++) censers[i] = s.local(body.add(motion.censers()[i]));
		return new Frame(s.local(body.add(motion.staffFoot())), s.local(body.add(motion.staffHead())), censers, s.local(body.add(motion.core())),
				s.local(body.add(motion.palm())), s.local(body.add(motion.face())), forward, right, floor - s.camera().y, velocity, s.camera());
	}

	// ── the sites ────────────────────────────────────────────────────

	/** This frame's fire sites: the generator's anchors as posed, or, before they are recorded, ones built off the points. */
	private static List<Site> sites(Scene s, PutricielEntity boss, PutricielAnimState motion, Vec3 body, Frame f, Vec3 velocity) {
		List<Site> sites = new ArrayList<>(80);
		int i = 0;
		for (PutricielFireAnchors.Anchor anchor : motion.fireAnchors()) {
			Region region = anchor.bodyRegion();
			sites.add(new Site(s.local(anchor.at(body)), anchor.normal(), anchor.velocity(), region, anchor.size(), anchor.place(), anchor.palmArm(),
					region.ordinal() * 1000 + anchor.order() * 7 + i++));
		}
		if (!sites.isEmpty()) return sites;
		return fallback(f, velocity, boss.getId());
	}

	/**
	 * Sites built off the recorded points (the core, the palm, the face, the staff, the censers) for a body whose
	 * anchors are not recorded yet: the same regions, counts and kindle order as the generator's table.
	 */
	static List<Site> fallback(Frame f, Vec3 velocity, int seed) {
		List<Site> out = new ArrayList<>(70);
		Vec3 up = new Vec3(0, 1, 0), fw = f.forward(), rt = f.right();
		Vec3 axis = f.staffHead().subtract(f.staffFoot());
		Vec3 along = axis.lengthSqr() < 1.0E-6D ? up : axis.normalize();
		double palmSide = Math.signum(f.palm().subtract(f.core()).dot(rt));
		// the staff: four wraps up the shaft, three growths, the head-bar's ends and the three prongs
		double[] wraps = {0.55D, 0.64D, 0.73D, 0.82D};
		for (int k = 0; k < 4; k++) {
			out.add(new Site(f.staffFoot().add(axis.scale(wraps[k])), rt.scale(k % 2 == 0 ? 1.0D : -1.0D), velocity, Region.STAFF, 0.4D + 0.15D * k,
					(wraps[k] - GRIP) / (1.0D - GRIP), false, seed + 10 + k));
		}
		for (int k = 0; k < 3; k++) {
			double at = 0.6D + 0.09D * k;
			out.add(new Site(f.staffFoot().add(axis.scale(at)).add(rt.scale(0.12D * (k % 2 == 0 ? 1 : -1))), rt.scale(k % 2 == 0 ? 1 : -1), velocity,
					Region.STAFF, 0.2D, (at - GRIP) / (1.0D - GRIP), false, seed + 20 + k));
		}
		for (int k = -1; k <= 1; k += 2) {
			out.add(new Site(f.staffHead().subtract(along.scale(0.1D)).add(rt.scale(0.22D * k)), rt.scale(k), velocity, Region.STAFF, 0.1D, 0.95D, false,
					seed + 30 + k));
		}
		for (int k = -1; k <= 1; k++) {
			out.add(new Site(f.staffHead().add(along.scale(0.15D)).add(rt.scale(0.12D * k)), up.add(rt.scale(0.3D * k)).normalize(), velocity, Region.STAFF,
					0.6D + 0.3D * (1 - Math.abs(k)), 1.0D, false, seed + 40 + k));
		}
		// the censers
		for (int k = 0; k < f.censers().length; k++) {
			out.add(new Site(f.censers()[k], up, velocity, Region.CENSER, 0.5D + 0.3D * k, k, false, seed + 50 + k));
		}
		// the back colony: eleven shelves fanned across it from the root outward, five mushrooms and spore sacs, the root
		Vec3 root = f.core().subtract(fw.scale(0.45D)).add(0.0D, 0.25D, 0.0D);
		for (int k = 0; k < 19; k++) {
			double t = k / 18.0D;
			double a = (PutricielBloomRules.unit(seed, 2000 + k) - 0.5D) * 2.4D;
			Vec3 out2 = fw.scale(-0.8D).add(rt.scale(Math.sin(a) * 0.9D)).add(0.0D, 0.25D + 0.5D * PutricielBloomRules.unit(seed, 2020 + k), 0.0D)
					.normalize();
			Vec3 at = root.add(out2.scale(0.15D + 0.55D * t)).add(0.0D, 0.6D * t, 0.0D);
			out.add(new Site(at, out2, velocity, Region.BACK, k < 11 ? 0.55D + 0.45D * PutricielBloomRules.unit(seed, 2040 + k) : 0.3D, t, false,
					seed + 100 + k));
		}
		// the shoulders and arms: shelves, mantles, the elbows and the palm's knuckle
		for (int side = -1; side <= 1; side += 2) {
			boolean palm = side == palmSide;
			int count = 7;
			for (int k = 0; k < count; k++) {
				double t = k / (double) (count - 1);
				Vec3 shoulder = f.core().add(rt.scale(side * (0.5D + 0.25D * t))).add(0.0D, 0.45D - 0.75D * t, 0.0D).subtract(fw.scale(0.05D));
				out.add(new Site(shoulder, rt.scale(side).add(0.0D, 0.6D, 0.0D).normalize(), velocity, Region.SHOULDER, 0.3D + 0.5D * (1.0D - t),
						t * 0.85D, palm, seed + 200 + side * 20 + k));
			}
		}
		out.add(new Site(f.palm().subtract(f.palm().subtract(f.core()).scale(0.15D)), up, velocity, Region.SHOULDER, 0.2D, 1.0D, true, seed + 260));
		// the crown: six spire tips, the head's shelves and its mushroom
		Vec3 crown = f.face().add(0.0D, 0.55D, 0.0D).subtract(fw.scale(0.1D));
		for (int k = 0; k < 6; k++) {
			double x = (k / 5.0D - 0.5D) * 0.6D;
			out.add(new Site(crown.add(rt.scale(x)).add(0.0D, 0.1D + 0.15D * (1.0D - Math.abs(x) * 2.0D), 0.0D), up, velocity, Region.CROWN, 0.5D,
					k / 8.0D, false, seed + 300 + k));
		}
		for (int k = -1; k <= 1; k += 2) {
			out.add(new Site(f.face().add(rt.scale(0.38D * k)).add(0.0D, 0.25D, 0.0D), rt.scale(k).add(0, 0.5, 0).normalize(), velocity, Region.CROWN,
					0.9D, 0.8D, false, seed + 310 + k));
		}
		out.add(new Site(crown.add(0.0D, 0.2D, 0.0D), up, velocity, Region.CROWN, 0.7D, 1.0D, false, seed + 320));
		// the chest: the four sac tears and the seams' two upper ends
		for (int k = 0; k < 6; k++) {
			double x = (k / 5.0D - 0.5D) * 0.5D;
			out.add(new Site(f.core().add(fw.scale(0.28D)).add(rt.scale(x)).add(0.0D, 0.1D * (k % 3), 0.0D), fw, velocity, Region.CHEST, 0.5D, k / 5.0D,
					false, seed + 400 + k));
		}
		// the hem: low round the robe's lower edge
		Vec3 feet = new Vec3(f.core().x, f.floor(), f.core().z);
		for (int k = 0; k < 7; k++) {
			double a = Math.PI * 2.0D * k / 7.0D;
			Vec3 outward = fw.scale(Math.cos(a)).add(rt.scale(Math.sin(a)));
			out.add(new Site(feet.add(outward.scale(0.6D)).add(0.0D, 0.3D, 0.0D), outward, velocity, Region.HEM, 0.5D, k / 6.0D, false, seed + 500 + k));
		}
		return out;
	}

	// ── the fire ─────────────────────────────────────────────────────

	/**
	 * The body's fire this frame: each site's cluster, the staff's sleeve and foot, the censers' beds and streams, the
	 * back column and its plume, the pall over the shoulders, the light under the hood, the tear-offs and the ash.
	 * {@code settle} scales the whole while the body climbs back after an Offering.
	 */
	static void draw(PoseStack p, VertexConsumer flame, VertexConsumer spores, List<Site> sites, Frame f, Moment m, double time, float age,
			Vec3 right, Vec3 up, Budget budget, @Nullable Memory memory, boolean fresh, float settle, @Nullable PutricielEntity boss) {
		if (!PutricielBodyFireRules.drawsBody(m)) return;
		Vec3 axis = f.staffHead().subtract(f.staffFoot());
		double staffLength = axis.length();
		Vec3 along = staffLength < 1.0E-4D ? new Vec3(0, 1, 0) : axis.scale(1.0D / staffLength);
		Vec3 nearestHit = null;
		if (boss != null && m.sinceHit() >= 0.0F && m.sinceHit() < 6.0F) nearestHit = struck(boss, f);
		Site hitSite = null;
		double hitDistance = Double.MAX_VALUE;
		for (Site site : sites) {
			Flame fire = PutricielBodyFireRules.anchor(site.region(), site.place(), site.palmArm(), m);
			if (!fire.visible()) continue;
			if (nearestHit != null && site.at().distanceToSqr(nearestHit) < hitDistance) {
				hitDistance = site.at().distanceToSqr(nearestHit);
				hitSite = site;
			}
			cluster(p, flame, site, fire, f, along, staffLength, m, time, age, right, up, budget, memory, fresh, settle);
		}
		staff(p, flame, f, along, staffLength, m, time, right, up, budget, settle);
		censers(p, flame, f, m, time, right, up, budget, settle);
		streams(p, flame, f, m, time, age, right, up, budget, settle, boss);
		column(p, flame, sites, f, m, time, right, up, budget, memory, fresh, settle);
		pall(p, flame, sites, m, time, right, up, budget);
		hood(p, flame, f, m, right, up);
		ash(p, spores, sites, f, m, age, right, up, budget);
		if (hitSite != null) tearBurst(p, flame, spores, hitSite, m.sinceHit(), time, right, up, budget);
	}

	/**
	 * One site's cluster, back to front: the soot over it, the outer flame and its core (one tongue, two round a staff
	 * wrap, three along a big back shelf's rim, three through a censer's lid), the bed of coals, and tear-offs when it
	 * moves fast.
	 */
	private static void cluster(PoseStack p, VertexConsumer v, Site site, Flame fire, Frame f, Vec3 along, double staffLength, Moment m,
			double time, float age, Vec3 right, Vec3 up, Budget budget, @Nullable Memory memory, boolean fresh, float settle) {
		Region region = site.region();
		double speed = site.speed();
		double base = region.height(site.size());
		if (region == Region.SHOULDER) base = PutricielBodyFireRules.armFlare(speed, base);
		float flicker = PutricielBodyFireRules.flicker(time, site.key());
		double height = base * fire.tongue() * flicker * PutricielBodyFireRules.stretch(speed) * settle * BOLD_HEIGHT;
		Vec3 want = PutricielBodyFireRules.tip(site.normal(), site.velocity());
		Vec3 dir = memory == null ? want : memory.tip(site.key(), want, fresh);
		double width = (0.05D + 0.11D * site.size()) * Math.sqrt(Math.max(0.0D, fire.tongue() * settle)) * BOLD_WIDTH;
		int seed = site.key() * 13;
		float heat = fire.heat();
		// the soot over it
		if (fire.smoke() > 0.04F && (region == Region.SHOULDER || region == Region.BACK || site.key() % 3 == 0) && budget.smoke()) {
			double size = region == Region.SHOULDER ? 0.32D : region == Region.BACK ? 0.28D : 0.18D;
			PutricielFlameGeometry.smoke(p, v, site.at().add(dir.scale(height * 1.15D + 0.12D)), right, up, size, time + site.key(), seed + 1,
					fire.smoke() * (region == Region.SHOULDER ? 0.6F : 0.42F));
		}
		if (height > 0.03D) {
			switch (region) {
				case STAFF -> {
					double t = staffLength < 1.0E-4D ? 1.0D : site.at().subtract(f.staffFoot()).dot(along) / staffLength;
					Vec3 off = site.at().subtract(f.staffFoot().add(along.scale(t * staffLength)));
					boolean wrap = t < 0.92D && off.length() < 0.18D;
					if (wrap) {
						// two tongues wrapping round the shaft from opposite sides
						Vec3 side = along.cross(new Vec3(0, 1, 0));
						side = side.lengthSqr() < 1.0E-6D ? f.right() : side.normalize();
						for (int k = -1; k <= 1; k += 2) {
							if (!budget.tongue()) break;
							Vec3 round = PutricielBodyFireRules.clampLean(dir.add(side.scale(0.35D * k)).normalize());
							PutricielFlameGeometry.anchored(p, v, site.at().add(side.scale(0.05D * k)), round, height, width, heat, right, up, time,
									seed + k, 1.0F);
						}
					} else if (budget.tongue()) {
						PutricielFlameGeometry.anchored(p, v, site.at(), dir, height * (t >= 0.92D ? 1.15D : 1.0D), width, heat, right, up, time, seed, 1.0F);
					}
				}
				case BACK -> {
					if (site.size() > 0.5D) {
						// a shelf burns along its rim
						Vec3 rim = site.normal().cross(new Vec3(0, 1, 0));
						rim = rim.lengthSqr() < 1.0E-6D ? f.right() : rim.normalize();
						for (int k = -1; k <= 1; k++) {
							if (!budget.tongue()) break;
							double h = height * (k == 0 ? 1.0D : 0.75D + 0.15D * PutricielBloomRules.unit(seed, k + 2));
							PutricielFlameGeometry.anchored(p, v, site.at().add(rim.scale(0.13D * k)), dir, h, width * (k == 0 ? 1.0D : 0.85D), heat, right,
									up, time, seed + 3 + k, 1.0F);
						}
					} else if (budget.tongue()) {
						PutricielFlameGeometry.anchored(p, v, site.at(), dir, height, width, heat, right, up, time, seed, 1.0F);
					}
				}
				case CENSER -> {
					for (int k = 0; k < 3; k++) {
						if (!budget.tongue()) break;
						double a = k * Math.PI * 2.0D / 3.0D + site.key();
						Vec3 lid = site.at().add(Math.cos(a) * 0.07D, 0.08D, Math.sin(a) * 0.07D);
						double h = height * (0.8D + 0.25D * PutricielBloomRules.unit(seed, 10 + k));
						PutricielFlameGeometry.anchored(p, v, lid, dir, h, width * 0.9D, heat, right, up, time, seed + 10 + k, 1.0F);
					}
				}
				default -> {
					if (budget.tongue()) PutricielFlameGeometry.anchored(p, v, site.at(), dir, height, width, heat, right, up, time, seed, 1.0F);
				}
			}
			// a fast anchor tears tongues off
			if (PutricielBodyFireRules.sheds(speed) && fire.tongue() > 0.3F) {
				for (int k = 0; k < 2; k++) {
					float since = (float) ((age + k * 2.5F + (site.key() & 7) * 0.6F) % PutricielFlameGeometry.TEAR_LIFE);
					if (!budget.tongue()) break;
					PutricielFlameGeometry.tearOff(p, v, site.at().subtract(site.velocity().scale(since)), site.velocity(), since, height * 0.55D, right, up,
							time, seed + 20 + k, 0.9F);
				}
			}
		}
		// the bed of coals it rises from
		if (fire.bed() > 0.02F) {
			double size = region == Region.CENSER ? 0.2D : region == Region.SHOULDER ? 0.16D : 0.11D + 0.08D * site.size();
			PutricielFlameGeometry.glow(p, v, site.at(), right, up, size * (0.8D + 0.4D * fire.bed()), seed + 30, fire.bed() * 0.75F);
		}
	}

	/**
	 * The staff along its length: a sleeve of eight thin tongues hugging the shaft from the grip to the head, between
	 * the wraps, and below the grip half-height tongues down to an ember-tipped foot. The fire climbs it from the grip
	 * at a tenth of its length a tick and runs down to the foot at half that.
	 */
	private static void staff(PoseStack p, VertexConsumer v, Frame f, Vec3 along, double length, Moment m, double time, Vec3 right, Vec3 up,
			Budget budget, float settle) {
		if (length < 0.5D) return;
		Vec3 side = along.cross(new Vec3(0, 1, 0));
		side = side.lengthSqr() < 1.0E-6D ? f.right() : side.normalize();
		Vec3 hug = PutricielBodyFireRules.clampLean(new Vec3(0, 1, 0).scale(0.75D).add(along.scale(0.5D)).normalize());
		for (int i = 0; i < 8; i++) {
			double t = GRIP + (0.9D - GRIP) * (i + 0.5D) / 8.0D;
			Flame fire = PutricielBodyFireRules.anchor(Region.STAFF, (t - GRIP) / (1.0D - GRIP), false, m);
			double h = HemoEasing.lerp(0.2D, 0.35D, PutricielBloomRules.unit(i, 3)) * fire.tongue() * settle
					* PutricielBodyFireRules.flicker(time, 900 + i) * PutricielBodyFireRules.stretch(f.bodyVelocity().length()) * BOLD_HEIGHT;
			if (h < 0.03D || !budget.tongue()) continue;
			Vec3 root = f.staffFoot().add(along.scale(t * length)).add(side.scale(i % 2 == 0 ? 0.045D : -0.045D));
			PutricielFlameGeometry.anchored(p, v, root, hug, h, (0.035D + 0.02D * fire.tongue()) * BOLD_WIDTH, fire.heat(), right, up, time, 7000 + i * 11, 0.95F);
		}
		for (int i = 0; i < 4; i++) {
			double t = GRIP * (1.0D - (i + 0.5D) / 4.0D);
			// down to the foot at half the climb's speed: a twentieth of the staff a tick from the grip
			double place = HemoEasing.clamp01(2.0D * (GRIP - t) / (1.0D - GRIP));
			Flame fire = PutricielBodyFireRules.anchor(Region.STAFF, place, false, m);
			double h = HemoEasing.lerp(0.1D, 0.18D, PutricielBloomRules.unit(i, 5)) * fire.tongue() * settle * PutricielBodyFireRules.flicker(time, 920 + i)
					* BOLD_HEIGHT;
			if (h < 0.03D || !budget.tongue()) continue;
			Vec3 root = f.staffFoot().add(along.scale(t * length)).add(side.scale(i % 2 == 0 ? -0.04D : 0.04D));
			PutricielFlameGeometry.anchored(p, v, root, hug, h, 0.03D * BOLD_WIDTH, fire.heat(), right, up, time, 7100 + i * 11, 0.9F);
		}
		Flame foot = PutricielBodyFireRules.anchor(Region.STAFF, 0.9D, false, m);
		PutricielFlameGeometry.glow(p, v, f.staffFoot().add(along.scale(0.05D)), right, up, 0.1D, 7200, foot.bed() * 0.8F);
	}

	/** The censers: a bed of coals glowing in each bowl, under the tongues the clusters push up through the lids. */
	private static void censers(PoseStack p, VertexConsumer v, Frame f, Moment m, double time, Vec3 right, Vec3 up, Budget budget, float settle) {
		for (int i = 0; i < f.censers().length; i++) {
			Flame fire = PutricielBodyFireRules.anchor(Region.CENSER, i, false, m);
			if (!fire.visible()) continue;
			float coals = Math.max(fire.bed(), Math.min(1.0F, fire.tongue())) * settle;
			PutricielFlameGeometry.glow(p, v, f.censers()[i], right, up, 0.16D + 0.05D * Math.sin(time * 0.4D + i), 7300 + i, coals * 0.9F);
			if (fire.smoke() > 0.1F && budget.smoke()) {
				PutricielFlameGeometry.smoke(p, v, f.censers()[i].add(0.0D, 0.7D + 0.3D * fire.tongue(), 0.0D), right, up, 0.22D, time + i * 9.0D,
						7310 + i, 0.4F * fire.smoke());
			}
		}
	}

	/**
	 * The streams: the staff head and both censers leave a train of overlapping tongues along their last six ticks of
	 * path, tapering 0.35 to 0.05 with age, whenever they move. Fire with body, never a ribbon line.
	 */
	private static void streams(PoseStack p, VertexConsumer v, Frame f, Moment m, double time, float age, Vec3 right, Vec3 up, Budget budget,
			float settle, @Nullable PutricielEntity boss) {
		if (boss == null) return;
		PutricielAnimState motion = PutricielAnimState.of(boss);
		for (int limb = 0; limb < 3; limb++) {
			Flame fire = limb == 2 ? PutricielBodyFireRules.anchor(Region.STAFF, 1.0D, false, m) : PutricielBodyFireRules.anchor(Region.CENSER, limb, false, m);
			if (fire.tongue() < 0.05F) continue;
			Vec3 previous = null;
			for (int j = 0; j <= STREAM_SAMPLES; j++) {
				float back = STREAM_TICKS * j / STREAM_SAMPLES;
				Vec3 world = limb == 2 ? motion.staffHeadAt(age - back) : motion.censerAt(limb, age - back);
				if (world == null) break;
				if (previous != null) {
					Vec3 step = previous.subtract(world);
					double speed = step.length() / (STREAM_TICKS / STREAM_SAMPLES);
					float moving = (float) HemoEasing.clamp01((speed - 0.06D) / 0.12D);
					if (moving > 0.02F && budget.tongue()) {
						double t = j / (double) STREAM_SAMPLES;
						double size = HemoEasing.lerp(0.35D, 0.05D, t) * fire.tongue() * settle * moving * BOLD_HEIGHT;
						Vec3 trail = PutricielBodyFireRules.clampLean(new Vec3(0, 1, 0).add(step.normalize().scale(-0.9D)).normalize());
						Vec3 at = world.subtract(f.camera());
						PutricielFlameGeometry.anchored(p, v, at, trail, size, Math.max(0.03D, size * 0.3D), fire.heat() * (float) (1.0D - t), right, up,
								time, 7400 + limb * 31 + j, (float) (1.0D - t * 0.6D));
					}
				}
				previous = world;
			}
		}
	}

	/**
	 * The back column: three broad tongues 1.2 to 1.8 tall over the colony's centre, leaning back off the shoulders,
	 * with a dark soot plume over them; it leaps at the commit and the Pyre Heart and in the Embering collapses into
	 * two smoke columns.
	 */
	private static void column(PoseStack p, VertexConsumer v, List<Site> sites, Frame f, Moment m, double time, Vec3 right, Vec3 up,
			Budget budget, @Nullable Memory memory, boolean fresh, float settle) {
		Vec3 centre = centroid(sites, Region.BACK);
		if (centre == null) return;
		double height = PutricielBodyFireRules.column(m) * settle;
		Vec3 lean = f.forward().scale(-0.35D).subtract(f.bodyVelocity().scale(PutricielBodyFireRules.DRAG));
		Vec3 want = PutricielBodyFireRules.clampLean(new Vec3(0, 1, 0).add(lean).normalize());
		Vec3 dir = memory == null ? want : memory.tip(-1, want, fresh);
		float heat = PutricielBodyFireRules.heat(m);
		double[] heights = {1.0D, 0.85D, 0.72D};
		for (int k = 0; k < 3 && height > 0.05D; k++) {
			if (!budget.tongue()) break;
			Vec3 root = centre.add(f.right().scale((k - 1) * 0.18D)).add(0.0D, 0.15D, 0.0D);
			double h = height * heights[k] * PutricielBodyFireRules.flicker(time, 7500 + k) * BOLD_HEIGHT;
			PutricielFlameGeometry.anchored(p, v, root, dir, h, (0.2D + 0.06D * (2 - k)) * BOLD_WIDTH, heat, right, up, time, 7500 + k * 17, 1.0F);
		}
		// the soot plume over it
		float plume = (float) HemoEasing.clamp01(height / PutricielBodyFireRules.COLUMN);
		for (int k = 0; k < 3 && plume > 0.05F; k++) {
			if (!budget.smoke()) break;
			PutricielFlameGeometry.smoke(p, v, centre.add(dir.scale(height * 1.15D + 0.4D * k)), right, up, 0.42D + 0.16D * k, time + k * 5.0D,
					7520 + k, 0.55F * plume);
		}
		// the Embering: the column collapses into two columns of smoke
		if (m.stage() == PutricielStage.EMBERING && !m.ending()) {
			float smoke = HemoEasing.window(m.stageTime(), 0.0F, 4.0F, 22.0F, PutricielBodyFireRules.SMOKE_OUT);
			for (int c = -1; c <= 1 && smoke > 0.0F; c += 2) {
				for (int k = 0; k < 4; k++) {
					if (!budget.smoke()) return;
					Vec3 at = centre.add(f.right().scale(0.28D * c)).add(0.0D, 0.4D + 0.5D * k + m.stageTime() * 0.02D, 0.0D);
					PutricielFlameGeometry.smoke(p, v, at, right, up, 0.26D + 0.1D * k, time + k * 6.0D + c * 3.0D, 7540 + k + c * 10, 0.6F * smoke);
				}
			}
		}
	}

	/** A thin pall of smoke over the smouldering shoulders. */
	private static void pall(PoseStack p, VertexConsumer v, List<Site> sites, Moment m, double time, Vec3 right, Vec3 up, Budget budget) {
		Vec3 centre = centroid(sites, Region.SHOULDER);
		if (centre == null) return;
		Flame fire = PutricielBodyFireRules.anchor(Region.SHOULDER, 0.3D, false, m);
		float pall = Math.max(fire.smoke(), Math.min(1.0F, fire.tongue())) * 0.3F;
		for (int k = 0; k < 2 && pall > 0.02F; k++) {
			if (!budget.smoke()) return;
			PutricielFlameGeometry.smoke(p, v, centre.add(0.0D, 0.55D + 0.35D * k, 0.0D), right, up, 0.6D + 0.2D * k, time + k * 11.0D, 7600 + k, pall);
		}
	}

	/** The hood's rim and the purifier's face lit from below by the crown's and the chest's glow. */
	private static void hood(PoseStack p, VertexConsumer v, Frame f, Moment m, Vec3 right, Vec3 up) {
		Flame crown = PutricielBodyFireRules.anchor(Region.CROWN, 0.5D, false, m);
		Flame chest = PutricielBodyFireRules.anchor(Region.CHEST, 0.5D, false, m);
		float light = Math.max(Math.min(1.0F, crown.tongue()), Math.min(1.0F, chest.tongue()) * 0.7F);
		PutricielFlameGeometry.glow(p, v, f.face().add(f.forward().scale(0.2D)).add(0.0D, -0.25D, 0.0D), right, up, 0.32D, 7700, 0.28F * light);
	}

	/**
	 * Ash: charcoal and ash-grey flakes drifting down and out off the back and the shoulders, six a second while it
	 * burns, and twelve falling through the Embering's smoke.
	 */
	private static void ash(PoseStack p, VertexConsumer spores, List<Site> sites, Frame f, Moment m, float age, Vec3 right, Vec3 up, Budget budget) {
		List<Site> shedding = new ArrayList<>();
		for (Site site : sites) if (site.region() == Region.BACK || site.region() == Region.SHOULDER) shedding.add(site);
		if (shedding.isEmpty()) return;
		boolean embering = m.stage() == PutricielStage.EMBERING && !m.ending();
		Flame fire = PutricielBodyFireRules.anchor(Region.BACK, 0.5D, false, m);
		float rate = embering ? 0.0F : PutricielBodyFireRules.drawsBody(m) && fire.tongue() > 0.15F ? 6.0F : 0.0F;
		int seed = 8000;
		for (int i = 0; i < 10 && rate > 0.0F; i++) {
			double[] flake = PutricielBloomRules.flake(seed, i, age, rate);
			if (flake == null || !budget.shed()) continue;
			int born = (int) flake[0];
			Site site = shedding.get(Math.floorMod(born * 31 + i, shedding.size()));
			drift(p, spores, site, f, flake[1], age - (float) flake[0], born, right, up);
		}
		if (embering) {
			float e = m.stageTime();
			for (int i = 0; i < 12; i++) {
				double life = (e - PutricielBodyFireRules.EMBERS_OUT - i * 0.8D) / 14.0D;
				if (life < 0.0D || life >= 1.0D || !budget.shed()) continue;
				Site site = shedding.get(Math.floorMod(i * 7, shedding.size()));
				drift(p, spores, site, f, life, (float) (life * 14.0D), 8100 + i, right, up);
			}
		}
	}

	/** One flake {@code life} of the way through its fall from {@code site}, {@code since} ticks after it left. */
	private static void drift(PoseStack p, VertexConsumer spores, Site site, Frame f, double life, float since, int seed, Vec3 right, Vec3 up) {
		double swirl = PutricielBloomRules.unit(seed, 1) * Math.PI * 2.0D + life * 3.0D;
		Vec3 flat = new Vec3(site.normal().x, 0.0D, site.normal().z);
		flat = flat.lengthSqr() < 1.0E-6D ? Vec3.ZERO : flat.normalize();
		Vec3 at = site.at().subtract(f.bodyVelocity().scale(since))
				.add(flat.scale(0.5D * life)).add(Math.cos(swirl) * 0.12D * life, 0.2D * life - 1.1D * life * life, Math.sin(swirl) * 0.12D * life);
		double size = 0.04D + 0.04D * PutricielBloomRules.unit(seed, 2);
		PutricielSporeGeometry.ash(p, spores, at, right, up, size, PutricielBloomRules.unit(seed, 3) > 0.5D ? 0.85F : 0.2F, (float) life * 0.4F,
				(float) Math.sin(Math.min(1.0D, life * 1.4D) * Math.PI) * 0.9F + 0.1F, seed);
	}

	/** A hit: four tongues torn off the wound's nearest anchor and a burst of eight ash flakes. */
	private static void tearBurst(PoseStack p, VertexConsumer flame, VertexConsumer spores, Site site, float since, double time, Vec3 right, Vec3 up,
			Budget budget) {
		for (int k = 0; k < 4; k++) {
			if (!budget.tongue()) break;
			double a = k * Math.PI / 2.0D + site.key();
			Vec3 burst = site.normal().scale(0.12D).add(Math.cos(a) * 0.08D, 0.05D, Math.sin(a) * 0.08D);
			PutricielFlameGeometry.tearOff(p, flame, site.at(), burst, since, 0.35D, right, up, time, site.key() * 5 + k, 1.0F);
		}
		for (int k = 0; k < 8; k++) {
			if (!budget.shed()) break;
			double a = k * 0.785D + site.key();
			double life = since / 14.0D;
			Vec3 at = site.at().add(Math.cos(a) * 0.4D * life, 0.25D * life - 0.6D * life * life, Math.sin(a) * 0.4D * life);
			PutricielSporeGeometry.ash(p, spores, at, right, up, 0.05D, k % 2 == 0 ? 0.85F : 0.2F, (float) life * 0.5F, 1.0F - (float) life,
					site.key() + k);
		}
	}

	/** Where the last blow came from, camera-relative, or the chest's front when the attacker is gone. */
	@Nullable
	private static Vec3 struck(PutricielEntity boss, Frame f) {
		DamageSource source = boss.getLastDamageSource();
		Entity attacker = source == null ? null : source.getEntity();
		if (attacker == null) return f.core().add(f.forward().scale(0.5D));
		Vec3 eye = attacker.getEyePosition();
		Vec3 coreWorld = boss.position().add(0.0D, 1.8D, 0.0D);
		Vec3 toward = eye.subtract(coreWorld);
		return f.core().add(toward.lengthSqr() < 1.0E-4D ? f.forward() : toward.normalize().scale(0.8D));
	}

	@Nullable
	private static Vec3 centroid(List<Site> sites, Region region) {
		double x = 0, y = 0, z = 0;
		int n = 0;
		for (Site site : sites) {
			if (site.region() != region) continue;
			x += site.at().x;
			y += site.at().y;
			z += site.at().z;
			n++;
		}
		return n == 0 ? null : new Vec3(x / n, y / n, z / n);
	}

	// ── what the body leaves behind ──────────────────────────────────

	/**
	 * Records this frame's footfalls and shed bursts: a scorched print each half stride while the fire burns and the
	 * body walks, and the burning fragments that break off the back at the commits of the Purging Sweep, the Censer
	 * Cast and the Pyre Heart, kept past the move so they can fall, bounce and smoulder.
	 */
	private static void remember(Memory memory, PutricielEntity boss, PutricielAnimState motion, Vec3 body, Vec3 velocity, Vec3 forward,
			Vec3 right, Moment m, float age) {
		while (!memory.prints.isEmpty() && age - memory.prints.peekFirst()[4] > PRINT_LIFE) memory.prints.pollFirst();
		while (!memory.bursts.isEmpty() && age - memory.bursts.peekFirst()[3] > 60.0F) memory.bursts.pollFirst();
		boolean fire = PutricielBodyFireRules.drawsBody(m) && !m.ending() && m.stage() != PutricielStage.EMBERING
				&& PutricielBodyFireRules.ignitionTick(m) >= PutricielBodyFireRules.COMMIT;
		if (memory.last != null && fire) {
			memory.walked += Math.hypot(body.x - memory.last.x, body.z - memory.last.z);
			if (memory.walked >= STEP) {
				memory.walked = 0.0D;
				memory.left = !memory.left;
				Vec3 foot = body.add(right.scale(memory.left ? -0.3D : 0.3D));
				if (memory.prints.size() >= PRINTS) memory.prints.pollFirst();
				memory.prints.addLast(new double[]{foot.x, Math.floor(boss.getY() + 1.0E-3D), foot.z, Math.atan2(forward.z, forward.x), age});
			}
		} else {
			memory.walked = 0.0D;
		}
		memory.last = body;
		PutricielMove move = PutricielMove.byId(m.moveId());
		boolean breaks = move == PutricielMove.PURGING_SWEEP || move == PutricielMove.CENSER_CAST || move == PutricielMove.PYRE_HEART;
		int serial = boss.syncedMoveSerial();
		if (breaks && m.moveTime() >= m.commit() && memory.burstSerial != serial) {
			memory.burstSerial = serial;
			Vec3 back = body.add(motion.core()).subtract(forward.scale(0.5D)).add(0.0D, 0.55D, 0.0D);
			if (memory.bursts.size() >= 3) memory.bursts.pollFirst();
			memory.bursts.addLast(new double[]{back.x, back.y, back.z, age - (m.moveTime() - m.commit()), Math.floor(boss.getY() + 1.0E-3D),
					serial * 31 + boss.getId()});
		}
	}

	/** The scorched prints: SCORCH on the floor, 0.5 by 0.35, cooling over 40 ticks; two low tongues on each for 8. */
	private static void prints(Scene s, Memory memory, float age, boolean ground) {
		int i = 0;
		for (double[] print : memory.prints) {
			float since = age - (float) print[4];
			i++;
			if (since < 0.0F || since > PRINT_LIFE) continue;
			Vec3 at = new Vec3(print[0], print[1], print[2]);
			if (ground) {
				double yaw = print[3];
				PutricielGroundGeometry.Ground g = new PutricielGroundGeometry.Ground(at.x, at.z, Math.cos(yaw), Math.sin(yaw), at.y, s.camera());
				float cool = since / PRINT_LIFE;
				PutricielGroundGeometry.rect(s.p(), s.floor(), g, -0.25D, 0.25D, -0.175D, 0.175D, PutricielGroundGeometry.Front.whole(at),
						new PutricielGroundGeometry.Paint(PutricielGroundGeometry.SCORCH, 0.85F * (1.0F - cool), 0.15F + 0.5F * cool,
								1.0F - HemoEasing.ramp(since, PRINT_LIFE * 0.6F, PRINT_LIFE)), 8200 + i);
			} else if (since < PRINT_FLAME) {
				float burn = 1.0F - since / PRINT_FLAME;
				for (int k = -1; k <= 1; k += 2) {
					Vec3 root = s.local(at).add(Math.cos(print[3]) * 0.12D * k, 0.0D, Math.sin(print[3]) * 0.12D * k);
					PutricielFlameGeometry.tongue(s.p(), s.flame(), root, Vec3.ZERO, 0.22D * burn * PutricielBloomRules.flicker(s.time(), 8300 + i, k + 1),
							0.06D, s.time(), 8300 + i * 3 + k, burn);
				}
			}
		}
	}

	/**
	 * The burning fragments: chunks of shelf broken off the back, falling, bouncing once and smouldering on the floor
	 * for 20 ticks. Solid in the growth material (char 0.8, glowing cracks, cooling to cold black), with a small flame
	 * and its soot while they burn.
	 */
	private static void fragments(Scene s, Memory memory, float age, Budget budget, boolean solid) {
		for (double[] burst : memory.bursts) {
			float since = age - (float) burst[3];
			int seed = (int) burst[5];
			double drop = Math.max(0.2D, burst[1] - burst[4]);
			int count = 3 + Math.floorMod(seed, 3);
			for (int i = 0; i < count; i++) {
				double[] piece = PutricielBloomRules.fragment(seed, i, since, drop);
				if (piece == null || !budget.shed()) continue;
				Vec3 at = s.local(burst[0] + piece[0], burst[1] + piece[1], burst[2] + piece[2]);
				double size = 0.08D + 0.05D * PutricielBloomRules.unit(seed, 970 + i);
				float smoulder = piece[4] < 0.0D ? 0.0F : (float) (piece[4] / PutricielBloomRules.SMOULDER);
				if (solid) {
					float charred = HemoEasing.lerp(0.78F, 0.97F, smoulder);
					PutricielGrowthGeometry.clod(s.p(), s.growth(), at.add(0.0D, size * 0.4D, 0.0D), size, piece[3],
							PutricielGrowthGeometry.withChar(PutricielGrowthGeometry.FRESH, charred), i % 2 == 0 ? PutricielGrowthGeometry.CAP
									: PutricielGrowthGeometry.CLOD, seed + i);
				} else {
					float burn = 1.0F - smoulder;
					if (burn > 0.02F && budget.tongue()) {
						PutricielFlameGeometry.tongue(s.p(), s.flame(), at.add(0.0D, size * 0.5D, 0.0D), Vec3.ZERO, (0.12D + 0.1D * burn) * burn, 0.05D,
								s.time(), seed + 40 + i, burn);
					}
					if (burn > 0.05F && i % 2 == 0 && budget.smoke()) {
						PutricielFlameGeometry.smoke(s.p(), s.flame(), at.add(0.0D, 0.35D, 0.0D), s.right(), s.up(), 0.14D, s.time() + i, seed + 50 + i,
								0.4F * burn);
					}
				}
			}
		}
	}

	/** Ember sparks as accents off the back and the staff head: six a second under All particles, three under Decreased. */
	private static void sparks(Scene s, PutricielEntity boss, Memory memory, List<Site> sites, Frame f, Moment m) {
		ParticleStatus setting = Minecraft.getInstance().options.particles().get();
		if (setting == ParticleStatus.MINIMAL) return;
		Flame back = PutricielBodyFireRules.anchor(Region.BACK, 0.5D, false, m);
		if (back.tongue() < 0.2F) return;
		float rate = SPARKS_A_SECOND * (setting == ParticleStatus.DECREASED ? 0.5F : 1.0F) * Math.min(1.0F, back.tongue());
		memory.sparkDebt += rate / 20.0F * memory.dt;
		Vec3 centre = centroid(sites, Region.BACK);
		Vec3 camera = s.camera();
		var random = boss.getRandom();
		while (memory.sparkDebt >= 1.0D) {
			memory.sparkDebt -= 1.0D;
			Vec3 from = (random.nextBoolean() || centre == null ? f.staffHead() : centre).add(camera);
			s.level().addParticle(HemoParticleData.emberSpark(random.nextInt(3) == 0 ? PINK_WHITE : CRIMSON, 0.05F + random.nextFloat() * 0.03F,
					0.9F, 12 + random.nextInt(10)), from.x + (random.nextDouble() - 0.5D) * 0.4D, from.y + random.nextDouble() * 0.3D,
					from.z + (random.nextDouble() - 0.5D) * 0.4D, (random.nextDouble() - 0.5D) * 0.06D, 0.04D + random.nextDouble() * 0.06D,
					(random.nextDouble() - 0.5D) * 0.06D);
		}
	}
}
