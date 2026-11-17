package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.ICellHand;
import com.vincenthuto.hemomancy.common.world.ground.GroundNetwork;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostLedger;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.impact.CastImpactRules.Delivery;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingDispatcher;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingSessions;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbChannel;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.LashAimSyncPacket;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbReach;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.util.ClientBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Runs every open strand (core-loop Track 12). A strand opens on its Sculpting's first paid pulse, works every tick the
 * channel stays open, pays on its cadence through {@code tryPerformVerbPulse}, and ends on release or when a pulse is
 * refused. Every hit is the server's decision ({@link StrandHits}); clients only draw. A snap ({@link #snap}) forces
 * the form's accent on demand for extra blood. Session state keyed by player UUID; cleared through
 * {@code TransientServerStateEvents}. Names no manipulation: a strand's school is its manipulation's.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class StrandService {
	/** A snap's extra blood: a one-off spend on the cast it accents. */
	public static final BloodAccounting.Source SNAP = BloodAccounting.Source.of("strand_snap", Category.CAST, false);

	/** A tap's blood: a one-off spend on the lash it is struck with. */
	public static final BloodAccounting.Source TAP = BloodAccounting.Source.of("lash_tap", Category.CAST, false);

	private static final Map<UUID, StrandSession> SESSIONS = new HashMap<>();
	/** Player to the game time the attack button last cracked their lash: it outlives the session it was cracked in. */
	private static final Map<UUID, Long> LAST_SNAP = new HashMap<>();
	/** The most whip aims taken between two lash steps, and the steps an aim may be late before the hand counts as still. */
	private static final int AIMS_PER_STEP = 4, AIM_GRACE_STEPS = 3;
	/** Player to the game time of their last tap: a session cooldown, gone on logout. */
	private static final Map<UUID, Long> LAST_TAP = new HashMap<>();
	/** Player to how many taps of a chain they have struck in a row. */
	private static final Map<UUID, Integer> TAP_CHAIN = new HashMap<>();
	/** Plants set off and waiting out their beat. */
	private static final List<Eruption> ERUPTIONS = new ArrayList<>();
	/** The later tails of many-tailed blows, waiting their turn. */
	private static final List<TailHit> TAILS = new ArrayList<>();

	/**
	 * One tail's blow still to land: a many-tailed lash strikes a body with each tip in quick succession.
	 * {@code tapStateTicks} is under 0 for a crack's tail, else the state a tap's tail lays.
	 */
	private record TailHit(ServerPlayer player, StrandSession session, LivingEntity target, float damage, Vec3 travel, long at,
			double stateScale, int tapStateTicks) {
	}

	/**
	 * One eruption waiting out its beat: where, how wide and how hard, and the bodies its run has already struck (a run
	 * along a stain shares one set, so a creature over several cells is hit once). {@code stain} is the stain cell the
	 * eruption spends, or null for a plant's ring.
	 */
	private record Eruption(UUID caster, ServerLevel level, Vec3 point, double radius, float damage, long at,
			StrandSession session, Set<Integer> struck, @Nullable BlockPos stain) {
	}

	private StrandService() {
	}

	/** The player's open strand, or null. */
	@Nullable
	public static StrandSession current(Player player) {
		return SESSIONS.get(player.getUUID());
	}

	public static boolean isOpen(Player player) {
		return SESSIONS.containsKey(player.getUUID());
	}

	/**
	 * Whether {@code player} has a strand out now, on either side: the server's open session, or on a client the strand
	 * its last cue drew and has not yet released. A strand's pace applies only while one is out.
	 */
	public static boolean isOut(Player player) {
		if (player.level().isClientSide) return ClientBridge.get().isStrandOut(player);
		return isOpen(player);
	}

	/** The phase of {@code player}'s open lash on the server; the orb's when none is open. */
	public static LashRules.Phase lashPhase(Player player) {
		StrandSession session = SESSIONS.get(player.getUUID());
		return session == null ? LashRules.Phase.CHARGING : session.lash.phase();
	}

	/**
	 * Whether {@code player}'s strand pours a twin from the off hand (Second Strand, CL-27): the open strand's own
	 * answer on the server, else whether one opened now would. Safe on both sides.
	 */
	public static boolean twinFor(Player player) {
		if (!player.level().isClientSide) {
			StrandSession session = SESSIONS.get(player.getUUID());
			if (session != null) return session.twin;
		}
		return twinReady(player);
	}

	/**
	 * Whether a strand opened now would have a twin: Second Strand unlocked, and the hand opposite the one channelling
	 * (the main hand when nothing is in use) empty or a cell hand. Vanilla uses one item at a time, so the twin is a
	 * second copy of the open strand from the other hand, not a second channel.
	 */
	public static boolean twinReady(Player player) {
		if (!SkillPointHelper.hasSecondStrand(player)) return false;
		InteractionHand using = player.isUsingItem() ? player.getUsedItemHand() : InteractionHand.MAIN_HAND;
		ItemStack other = player.getItemInHand(using == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
		return other.isEmpty() || other.getItem() instanceof ICellHand;
	}

	// ── The Sculpting's hooks ──

	static void begin(ServerPlayer player, StrandSculpting sculpting, VerbTarget target) {
		SculptingSessions.Session channel = SculptingSessions.current(player);
		BloodManipulation manipulation = channel == null ? null : ManipulationInit.getByName(channel.manipulation());
		if (manipulation == null) return;
		StrandSession session = new StrandSession(sculpting, manipulation, player.level().getGameTime(), leftArm(player),
				twinReady(player), SkillPointHelper.getLongStrandLevel(player), SkillPointHelper.getTautLineLevel(player),
				SkillPointHelper.getWhipcrackLevel(player), SkillPointHelper.getDeepBrandLevel(player));
		if (sculpting.form() == StrandForm.PLANT && target instanceof VerbTarget.Block block) plant(player, session, block);
		SESSIONS.put(player.getUUID(), session);
		cue(player, session);
	}

	static void pulse(ServerPlayer player, StrandSculpting sculpting, VerbTarget target, SculptingPulse pulse) {
		StrandSession session = SESSIONS.get(player.getUUID());
		if (session == null) return;
		double full = SculptingDispatcher.workScale(VerbReach.staffFocus(player), Verb.PROJECT);
		session.strength = (float) Math.max(0.0D, Math.min(1.0D, full <= 0.0D ? 1.0D : pulse.workScale() / full));
		// A paid pulse poured from both hands is the twin's first (Second Strand).
		if (session.twin) HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_TWIN_STRAND);
		// This pulse paid for the lash's ticks since the last one.
		session.chargingTicks = 0;
		session.outTicks = 0;
		session.fadingTicks = 0;
		switch (session.form()) {
			case JET -> jetPulse(player, session, target, false);
			case SPRAY -> sprayPulse(player, session, false);
			case PLANT -> plantPulse(player, session, target);
			default -> {
			}
		}
		cue(player, session);
	}

	static void hold(ServerPlayer player, StrandSculpting sculpting, VerbTarget aimed) {
		StrandSession session = SESSIONS.get(player.getUUID());
		if (session == null) return;
		switch (session.form()) {
			case LASH -> lashTick(player, session);
			case JET -> {
				// Pressure builds only on a body the jet may wound, as a brand sets only on one. Taut Line fills it faster.
				LivingEntity on = harmable(player, aimed);
				JetRules.tick(session.jet, on == null ? -1 : on.getId(), StrandSkills.quicken(session.tautLine));
			}
			case PLANT -> {
				if (session.plantPoint != null && player.getEyePosition().distanceTo(session.plantPoint)
						> length(player, session) * PlantRules.LEASH) trigger(player, session);
			}
			case BRAND -> brandTick(player, session, aimed);
			default -> {
			}
		}
	}

	static void end(ServerPlayer player, boolean released) {
		StrandSession session = SESSIONS.remove(player.getUUID());
		if (session == null) return;
		if (session.form() == StrandForm.PLANT) trigger(player, session);
		ManipulationVisuals.attached(player, ManipulationVisuals.Form.STRAND, player.position(), 0, 0, 0);
	}

	// ── The snap (CL-22) ──

	/**
	 * Forces the open strand's accent on demand: the lash cracks straight along the look, the jet spends its pressure in
	 * one pulse, the spray drenches, the plant erupts early, the brand sets at once. Costs the spec's snap blood on top
	 * of the upkeep, through the cost ledger. Returns false, paying nothing, when no strand is open or its accent
	 * cannot act now. A lash still gathering lets its charge go for nothing more than the charge already paid; a lash
	 * that is out pays, cracks, and has its whip aim brought back to the look.
	 */
	public static boolean snap(ServerPlayer player) {
		StrandSession session = SESSIONS.get(player.getUUID());
		if (session == null || !player.isAlive()) return false;
		long now = player.level().getGameTime();
		VerbTarget aimed = aim(player, session);
		boolean able = switch (session.form()) {
			case LASH -> LashRules.maySnap(session.lash, now) && sinceSnap(player, now) >= LashRules.SNAP_COOLDOWN;
			case SPRAY -> true;
			case PLANT -> PlantRules.primed(session.sunkPulses) && session.plantPoint != null;
			// The jet spends its pressure into a body and the brand sets on one: aimed at nothing, a snap does nothing.
			case JET, BRAND -> harmable(player, aimed) != null;
		};
		if (!able) return false;
		boolean letGo = session.form() == StrandForm.LASH && session.lash.phase() == LashRules.Phase.CHARGING;
		double price = letGo ? 0.0D : session.sculpting.spec().snapBlood()
				* ManipulationCostLedger.collect(player, session.manipulation, 1.0D, null).totalMultiplier();
		if (price > 0.0D) {
			if (!BloodAccounting.canPayInFull(player, price, SNAP)) return false;
			if (!BloodAccounting.pay(player, price, SNAP).satisfied()) return false;
		}
		switch (session.form()) {
			case LASH -> {
				LAST_SNAP.put(player.getUUID(), now);
				double charge = LashRules.release(session.lash, player.getYRot(), player.getXRot(), now);
				if (session.twin) LashRules.release(session.twinLash, player.getYRot(), player.getXRot(), now);
				session.aimYaw = player.getYRot();
				session.aimPitch = player.getXRot();
				session.aimBaseYaw = session.aimYaw;
				session.aimBasePitch = session.aimPitch;
				// Watchers see the whip brought back to the look too.
				PacketHandler.sendToTrackers(player, new LashAimSyncPacket(player.getId(), session.aimYaw, session.aimPitch));
				Vec3 hand = hand(player, session);
				Vec3 tip = clip(player, hand, hand.add(player.getLookAngle().scale(length(player, session))));
				LivingEntity met = nearest(hand, ManipulationCombatHelper.swept(player, List.<Vec3[]>of(new Vec3[] {hand, tip}),
						LashRules.STRAND_RADIUS));
				crack(player, session, met == null ? tip : met.getBoundingBox().getCenter(), player.getLookAngle(), charge);
				cue(player, session);
			}
			case JET -> jetPulse(player, session, aimed, true);
			case SPRAY -> sprayPulse(player, session, true);
			case PLANT -> trigger(player, session);
			case BRAND -> setBrand(player, session, harmable(player, aimed));
		}
		return true;
	}

	// ── Lash ──

	private static long sinceSnap(Player player, long now) {
		Long last = LAST_SNAP.get(player.getUUID());
		return last == null ? Long.MAX_VALUE / 2 : now - last;
	}

	private static void lashTick(ServerPlayer player, StrandSession session) {
		long now = player.level().getGameTime();
		Vec3 hand = hand(player, session);
		double length = length(player, session);
		double taut = StrandSkills.taut(session.tautLine);
		LashRules.State lash = session.lash;
		LashRules.Phase before = lash.phase();
		switch (before) {
			case CHARGING -> session.chargingTicks++;
			case THROWING, LIVE -> session.outTicks++;
			case FADING -> session.fadingTicks++;
		}
		// An aim that has not come this step is late, not still, until it has been late a while.
		boolean aimKnown = session.aimAge == 0 || session.aimAge > AIM_GRACE_STEPS;
		LashProfile profile = session.sculpting.spec().lash();
		LashStyle style = profile.style();
		double thrown = lash.throwCharge();
		LashRules.Step step = LashRules.step(lash, hand, player.getYRot(), player.getXRot(), session.aimYaw, session.aimPitch,
				length, now, (from, to) -> clip(player, from, to), taut, profile, aimKnown);
		boolean met = false;
		if (before == LashRules.Phase.THROWING && style.pierces(thrown)) {
			// A charged throw that pierces does not stop at a body: it cuts each one on its arc once and cracks at the
			// arc's end, or at a block.
			Vec3 travel = lash.tip().subtract(lash.previous(LashRules.NODES - 1));
			session.stateScale = profile.chargeScale(thrown);
			try {
				for (LivingEntity body : ManipulationCombatHelper.swept(player, LashRules.throwSweeps(lash), LashRules.STRAND_RADIUS)) {
					if (!session.pierced.add(body.getId())) continue;
					cut(player, session, body, LashRules.crackDamage(thrown, profile), travel, -1);
					if (player.level() instanceof ServerLevel level) {
						Vec3 at = body.getBoundingBox().getCenter();
						accent(level, player, session, StrandCue.Accent.PIERCE, at, at.add(travel.lengthSqr() < 1e-6 ? player.getLookAngle() : travel.normalize()), 1.0D);
					}
				}
			} finally {
				session.stateScale = 1.0D;
			}
			if (step.tipBlocked() || step.crack()) {
				double charge = step.crack() ? step.crackCharge() : LashRules.struck(lash, now);
				met = !step.crack();
				crack(player, session, lash.tip(), travel, charge, session.pierced);
				session.pierced.clear();
			}
		} else if (before == LashRules.Phase.THROWING) {
			// The throw cracks at the first body or block the strand meets on its arc, else at the arc's end.
			LivingEntity body = nearest(hand, ManipulationCombatHelper.swept(player, LashRules.throwSweeps(lash), LashRules.STRAND_RADIUS));
			if (body != null || step.tipBlocked() || step.crack()) {
				double charge = step.crack() ? step.crackCharge() : LashRules.struck(lash, now);
				met = !step.crack();
				Vec3 travel = lash.tip().subtract(lash.previous(LashRules.NODES - 1));
				crack(player, session, body == null ? lash.tip() : body.getBoundingBox().getCenter(), travel, charge);
			}
		} else if (step.phase() == LashRules.Phase.LIVE) {
			float damage = LashRules.sweepDamage(step.tipSpeed(), step.tired());
			Vec3 travel = lash.tip().subtract(lash.previous(LashRules.NODES - 1));
			if (damage > 0.0F) {
				boolean landed = false;
				for (LivingEntity target : ManipulationCombatHelper.swept(player, LashRules.sweeps(lash), LashRules.STRAND_RADIUS)) {
					landed |= StrandHits.strike(player, session, target, damage, false, Delivery.SWEEP, travel);
				}
				// A sweep that lands gives a little back (the tether), once a tick however many it struck.
				if (landed && style.sweepReturn() > 0.0D) giveBack(player, style.sweepReturn());
			}
			if (step.crack()) crack(player, session, lash.tip(), travel, 0.0D);
		}
		// The rules own the aim while the lash charges and through the throw; the caster's whip aim takes over after.
		if (before != LashRules.Phase.LIVE && before != LashRules.Phase.FADING) {
			session.aimYaw = lash.aimYaw();
			session.aimPitch = lash.aimPitch();
		}
		if (session.twin) twinLashTick(player, session, length, taut, now, met, aimKnown);
		session.aimAge = Math.min(session.aimAge + 1, Integer.MAX_VALUE / 2);
		session.aimBaseYaw = session.aimYaw;
		session.aimBasePitch = session.aimPitch;
		session.aimsThisStep = 0;
		if (lash.phase() != before) cue(player, session);
	}

	/**
	 * The twin lash (Second Strand): the same stroke from the off hand, a little slacker so it trails the lead. Its
	 * sweeps hit for half against their own lockouts, and it never cracks; the lead's crack is the crack.
	 */
	private static void twinLashTick(ServerPlayer player, StrandSession session, double length, double taut, long now,
			boolean leadMet, boolean aimKnown) {
		Vec3 off = StrandCue.hand(player.getEyePosition(), player.getLookAngle(), player.getYRot(), !session.leftArm);
		LashRules.State twin = session.twinLash;
		LashRules.Step step = LashRules.step(twin, off, player.getYRot(), player.getXRot(), session.aimYaw, session.aimPitch,
				length, now, (from, to) -> clip(player, from, to), taut * StrandSkills.TWIN_TAUT, session.sculpting.spec().lash(),
				aimKnown);
		if (leadMet) LashRules.struck(twin, now);
		if (step.phase() != LashRules.Phase.LIVE || twin.phase() != LashRules.Phase.LIVE) return;
		float damage = LashRules.sweepDamage(step.tipSpeed(), step.tired()) * StrandSkills.TWIN_SWEEP;
		if (damage <= 0.0F) return;
		Vec3 travel = twin.tip().subtract(twin.previous(LashRules.NODES - 1));
		for (LivingEntity target : ManipulationCombatHelper.swept(player, LashRules.sweeps(twin), LashRules.STRAND_RADIUS)) {
			StrandHits.strike(player, session, target, damage, false, Delivery.SWEEP, travel, session.twinLastHit);
		}
	}

	/** The body nearest {@code from} among {@code bodies}, or null. */
	@Nullable
	private static LivingEntity nearest(Vec3 from, List<LivingEntity> bodies) {
		return bodies.stream().min(Comparator.comparingDouble(body -> body.getBoundingBox().getCenter().distanceToSqr(from)))
				.orElse(null);
	}

	private static void crack(ServerPlayer player, StrandSession session, Vec3 at, Vec3 travel, double charge) {
		crack(player, session, at, travel, charge, Set.of());
	}

	/**
	 * A crack at {@code at}: everything the caster may harm in its radius, bar the {@code spared}, harder and for longer
	 * by the charge it carries. What else the charge buys, and what a row of cracks is worth, is the lash's style.
	 */
	private static void crack(ServerPlayer player, StrandSession session, Vec3 at, Vec3 travel, double charge, Set<Integer> spared) {
		if (!(player.level() instanceof ServerLevel level)) return;
		LashProfile profile = session.sculpting.spec().lash();
		LashStyle style = profile.style();
		long now = level.getGameTime();
		if (now - session.lastHeatCrack > style.heatRest()) session.heat = 0;
		int row = session.heat;
		double radius = style.crackRadius(charge);
		int root = LashStyle.charged(style.rootTicks(), charge), fire = LashStyle.charged(style.fireTicks(), charge);
		session.stateScale = profile.chargeScale(charge);
		try {
			for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(radius),
					target -> ManipulationCombatHelper.canHarm(player, target) && !spared.contains(target.getId())
							&& target.getBoundingBox().getCenter().distanceTo(at) <= radius + target.getBbWidth() * 0.5D
							&& open(level, player, at, target))) {
				if (cut(player, session, target, LashRules.crackDamage(charge, profile) * style.crackHeat(row), travel, -1)) {
					HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_FIRST_CRACK);
					// A charged tether holds what it cracks on where it stands.
					if (root > 0) target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, root, 6, false, false, true));
					if (fire > 0) target.igniteForTicks(fire);
				}
			}
		} finally {
			session.stateScale = 1.0D;
		}
		if (style.heat() > 0.0F) {
			session.heat = Math.min(style.heatStacks(), row + 1);
			session.lastHeatCrack = now;
		}
		accent(level, player, session, StrandCue.Accent.CRACK, at, at.add(travel.lengthSqr() < 1e-6 ? player.getLookAngle() : travel.normalize()),
				(1.0D + 0.5D * charge) * (radius / LashRules.CRACK_RADIUS) * (1.0D + 0.12D * row));
		level.playSound(null, at.x, at.y, at.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.9F, 1.5F);
		level.playSound(null, at.x, at.y, at.z, SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.7F, 0.8F);
	}

	/**
	 * A lash's blow on one body: one hit, or one for each of the lash's tails in quick succession, and harder from
	 * ambush. {@code tapStateTicks} under 0 is a crack's blow (an accent, laying the long state); otherwise a tap's,
	 * laying that much state. Returns whether the first hit landed.
	 */
	private static boolean cut(ServerPlayer player, StrandSession session, LivingEntity target, float damage, Vec3 travel,
			int tapStateTicks) {
		if (!(player.level() instanceof ServerLevel level)) return false;
		LashStyle style = session.sculpting.spec().lash().style();
		boolean ambush = style.ambush() > 1.0F && ambushed(level, player, target);
		float each = style.tailDamage(ambush ? damage * style.ambush() : damage);
		boolean landed = tapStateTicks < 0 ? StrandHits.strike(player, session, target, each, true, Delivery.SWEEP, travel)
				: StrandHits.tap(player, session, target, each, tapStateTicks, travel);
		long now = level.getGameTime();
		for (int tail = 1; tail < style.tails(); tail++) {
			TAILS.add(new TailHit(player, session, target, each, travel, now + (long) tail * LashStyle.TAIL_GAP, session.stateScale,
					tapStateTicks));
		}
		if (landed && ambush) {
			Vec3 at = target.getBoundingBox().getCenter();
			accent(level, player, session, StrandCue.Accent.PIERCE, at, at.add(travel.lengthSqr() < 1e-6 ? player.getLookAngle() : travel.normalize()), 1.0D);
			level.playSound(null, at.x, at.y, at.z, SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 0.8F, 0.6F);
		}
		return landed;
	}

	/** Whether a blow on {@code target} is an ambush: from outside its front, or in the dark. */
	public static boolean ambushed(ServerLevel level, Player player, LivingEntity target) {
		return level.getMaxLocalRawBrightness(target.blockPosition()) <= LashStyle.AMBUSH_LIGHT
				|| LashStyle.behind(Vec3.directionFromRotation(0.0F, target.getYHeadRot()), player.position().subtract(target.position()));
	}

	/** Lands the tails whose turn has come. */
	private static void tails() {
		if (TAILS.isEmpty()) return;
		List<TailHit> due = new ArrayList<>();
		for (Iterator<TailHit> it = TAILS.iterator(); it.hasNext(); ) {
			TailHit tail = it.next();
			if (tail.player().level().getGameTime() >= tail.at()) {
				due.add(tail);
				it.remove();
			}
		}
		for (TailHit tail : due) {
			if (!tail.player().isAlive() || !tail.target().isAlive() || tail.target().level() != tail.player().level()) continue;
			StrandSession session = tail.session();
			double before = session.stateScale;
			session.stateScale = tail.stateScale();
			try {
				if (tail.tapStateTicks() < 0) StrandHits.strike(tail.player(), session, tail.target(), tail.damage(), true, Delivery.SWEEP, tail.travel());
				else StrandHits.tap(tail.player(), session, tail.target(), tail.damage(), tail.tapStateTicks(), tail.travel());
			} finally {
				session.stateScale = before;
			}
		}
	}

	/** Gives {@code player} back up to {@code blood}, as far as they have room for it. */
	private static void giveBack(ServerPlayer player, double blood) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		double back = Math.min(blood, volume.getMaxBloodVolume() - volume.getBloodVolume());
		if (back <= 0.0D) return;
		volume.addBloodVolume(back);
		BloodVolumeEvents.syncVolume(player, volume);
	}

	/** Whether nothing solid stands between a crack at {@code at} and {@code target}: a crack does not reach through a wall. */
	private static boolean open(ServerLevel level, Player player, Vec3 at, LivingEntity target) {
		if (target.getBoundingBox().inflate(0.05D).contains(at)) return true;
		for (Vec3 to : new Vec3[] {target.getBoundingBox().getCenter(), target.getEyePosition(), target.position().add(0.0D, 0.1D, 0.0D)}) {
			if (level.clip(new ClipContext(at, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType()
					== HitResult.Type.MISS) return true;
		}
		return false;
	}

	/**
	 * Takes the caster's whip aim for a live or fading lash, as their client sent it: never further than the tuning
	 * allows from the server's own copy of their look, and never moved further between two lash steps than it allows in
	 * a tick, however many aims arrive in between. Anything else (no lash out, a lash still charging or mid-throw, a
	 * value that is not a number, a flood) is ignored. Watchers are told; the caster, whose own copy is ahead, is not.
	 */
	public static void aim(ServerPlayer player, float yaw, float pitch) {
		StrandSession session = SESSIONS.get(player.getUUID());
		if (session == null || session.form() != StrandForm.LASH || !Float.isFinite(yaw) || !Float.isFinite(pitch)) return;
		LashRules.Phase phase = session.lash.phase();
		if (phase != LashRules.Phase.LIVE && phase != LashRules.Phase.FADING) return;
		if (++session.aimsThisStep > AIMS_PER_STEP) return;
		session.aimAge = 0;
		LashTuning tuning = LashRules.tuning();
		double stepYaw = LashRules.wrapDegrees(yaw - session.aimBaseYaw), stepPitch = pitch - session.aimBasePitch;
		double step = Math.sqrt(stepYaw * stepYaw + stepPitch * stepPitch);
		if (step > tuning.maxAimStep()) {
			stepYaw *= tuning.maxAimStep() / step;
			stepPitch *= tuning.maxAimStep() / step;
		}
		double offYaw = LashRules.wrapDegrees(session.aimBaseYaw + stepYaw - player.getYRot());
		double offPitch = session.aimBasePitch + stepPitch - player.getXRot();
		double off = Math.sqrt(offYaw * offYaw + offPitch * offPitch);
		if (off > tuning.maxAimOffset()) {
			offYaw *= tuning.maxAimOffset() / off;
			offPitch *= tuning.maxAimOffset() / off;
		}
		session.aimYaw = (float) (player.getYRot() + offYaw);
		session.aimPitch = (float) Math.max(-90.0D, Math.min(90.0D, player.getXRot() + offPitch));
		PacketHandler.sendToTrackers(player, new LashAimSyncPacket(player.getId(), session.aimYaw, session.aimPitch));
	}

	/** How far from the server's copy of the look a throw's centre may sit; further and the server's look is used. */
	private static final double FLICK_CENTRE_SLACK = 45.0D;

	/**
	 * Throws the caster's charging lash: their client read a flick of the mouse and says which way it turned and what
	 * look the arc is centred on. The centre is held near the server's own copy of the look. Returns false when no lash
	 * of theirs is charging.
	 */
	public static boolean flick(ServerPlayer player, float centreYaw, float centrePitch, float turnYaw, float turnPitch) {
		StrandSession session = SESSIONS.get(player.getUUID());
		if (session == null || session.form() != StrandForm.LASH) return false;
		if (!Float.isFinite(centreYaw) || !Float.isFinite(centrePitch) || !Float.isFinite(turnYaw) || !Float.isFinite(turnPitch))
			return false;
		double offYaw = LashRules.wrapDegrees(centreYaw - player.getYRot()), offPitch = centrePitch - player.getXRot();
		if (Math.sqrt(offYaw * offYaw + offPitch * offPitch) > FLICK_CENTRE_SLACK) {
			centreYaw = player.getYRot();
			centrePitch = player.getXRot();
		}
		centrePitch = Math.max(-90.0F, Math.min(90.0F, centrePitch));
		double length = length(player, session);
		LashRules.Clipper clipper = (from, to) -> clip(player, from, to);
		if (!LashRules.flick(session.lash, hand(player, session), centreYaw, centrePitch, turnYaw, turnPitch, length, clipper))
			return false;
		session.pierced.clear();
		if (session.twin) {
			Vec3 off = StrandCue.hand(player.getEyePosition(), player.getLookAngle(), player.getYRot(), !session.leftArm);
			LashRules.flick(session.twinLash, off, centreYaw, centrePitch, turnYaw, turnPitch, length, clipper);
		}
		session.aimYaw = session.lash.aimYaw();
		session.aimPitch = session.lash.aimPitch();
		session.aimBaseYaw = session.aimYaw;
		session.aimBasePitch = session.aimPitch;
		cue(player, session);
		return true;
	}

	// ── The tap ──

	/** A lash selected on a held Projection tool: what the attack button taps with. */
	public record Tap(BloodManipulation manipulation, StrandSculpting sculpting) {
	}

	/**
	 * The lash {@code player}'s attack button would tap with now, or null: they hold the Projection hand or a Staff set
	 * to Projection, are not channelling, and the Projection Sculpting their selected manipulation would claim on this aim
	 * is a lash. Safe on both sides.
	 */
	@Nullable
	public static Tap tapLash(Player player) {
		if (player.isUsingItem()) return null;
		if (VerbChannel.verbOf(player, player.getMainHandItem()).orElse(null) != Verb.PROJECT
				&& VerbChannel.verbOf(player, player.getOffhandItem()).orElse(null) != Verb.PROJECT) return null;
		return SculptingDispatcher.currentClaim(player, Verb.PROJECT).map(SculptingDispatcher.Claim::selected)
				.filter(selected -> selected.sculpting() instanceof StrandSculpting strand && strand.form() == StrandForm.LASH)
				.map(selected -> new Tap(selected.manipulation(), (StrandSculpting) selected.sculpting())).orElse(null);
	}

	/**
	 * The tap: one quick lash from the hand straight along the look, striking the first body on the line and no other.
	 * Costs the profile's tap blood through the cost ledger and waits out the profile's cooldown. It wounds only, unless
	 * the profile gives its tap a state. A lash with a tap chain keeps that state for the chain's last tap, which is a
	 * small crack: harder, and on everything round where it lands. Returns false, paying nothing, when it cannot be
	 * struck now.
	 */
	public static boolean tap(ServerPlayer player) {
		if (!player.isAlive() || !(player.level() instanceof ServerLevel level)) return false;
		Tap tap = tapLash(player);
		if (tap == null) return false;
		LashProfile profile = tap.sculpting().spec().lash();
		long now = level.getGameTime();
		Long last = LAST_TAP.get(player.getUUID());
		if (last != null && now - last < profile.tapCooldown()) return false;
		double price = profile.tapBlood() * ManipulationCostLedger.collect(player, tap.manipulation(), 1.0D, null).totalMultiplier();
		if (price > 0.0D) {
			if (!BloodAccounting.canPayInFull(player, price, TAP)) return false;
			if (!BloodAccounting.pay(player, price, TAP).satisfied()) return false;
		}
		LAST_TAP.put(player.getUUID(), now);
		LashStyle style = profile.style();
		boolean finisher = false;
		if (style.tapChain() > 1) {
			int struck = last != null && now - last <= LashStyle.CHAIN_WINDOW ? TAP_CHAIN.getOrDefault(player.getUUID(), 0) + 1 : 1;
			finisher = struck >= style.tapChain();
			TAP_CHAIN.put(player.getUUID(), finisher ? 0 : struck);
		}
		int state = style.tapChain() > 1 && !finisher ? 0 : profile.tapStateTicks();
		// A tap opens no channel: it is struck with a strand of its own that lasts the one blow.
		StrandSession session = new StrandSession(tap.sculpting(), tap.manipulation(), now, leftArm(player), false,
				SkillPointHelper.getLongStrandLevel(player), SkillPointHelper.getTautLineLevel(player),
				SkillPointHelper.getWhipcrackLevel(player), SkillPointHelper.getDeepBrandLevel(player));
		Vec3 hand = hand(player, session);
		Vec3 end = clip(player, hand, hand.add(player.getLookAngle().scale(length(player, session))));
		LivingEntity met = nearest(hand, ManipulationCombatHelper.swept(player, List.<Vec3[]>of(new Vec3[] {hand, end}),
				LashRules.STRAND_RADIUS));
		if (met != null) {
			end = met.getBoundingBox().getCenter();
			if (finisher) {
				Vec3 at = end;
				for (LivingEntity body : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(LashRules.CRACK_RADIUS),
						body -> ManipulationCombatHelper.canHarm(player, body)
								&& body.getBoundingBox().getCenter().distanceTo(at) <= LashRules.CRACK_RADIUS + body.getBbWidth() * 0.5D
								&& open(level, player, at, body))) {
					cut(player, session, body, profile.tapDamage() * LashStyle.FINISHER, player.getLookAngle(), state);
				}
			} else {
				cut(player, session, met, profile.tapDamage(), player.getLookAngle(), state);
			}
		}
		accent(level, player, session, StrandCue.Accent.TAP, hand, end, hand.distanceTo(end));
		if (finisher) accent(level, player, session, StrandCue.Accent.CRACK, end, end.add(player.getLookAngle()), 0.8D);
		level.playSound(null, hand.x, hand.y, hand.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, 1.7F);
		if (met != null) level.playSound(null, end.x, end.y, end.z, SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.8F, 1.3F);
		return true;
	}

	// ── Jet ──

	private static void jetPulse(ServerPlayer player, StrandSession session, VerbTarget target, boolean snap) {
		if (!(target instanceof VerbTarget.Creature creature)) {
			if (snap) JetRules.spend(session.jet);
			return;
		}
		LivingEntity first = creature.living();
		double pressure = session.jet.pressure;
		// Two jets held on one body hit harder (Second Strand).
		float twin = session.twin ? StrandSkills.TWIN_JET : 1.0F;
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle();
		Vec3 contact = first.getBoundingBox().getCenter();
		if (snap) {
			boolean state = pressure >= JetRules.SNAP_STATE_PRESSURE;
			StrandHits.strike(player, session, first, JetRules.snapDamage(pressure) * twin, state, Delivery.PROJECTILE, look);
			JetRules.spend(session.jet);
			if (player.level() instanceof ServerLevel level)
				accent(level, player, session, StrandCue.Accent.PIERCE, contact, contact.add(look), 1.0D + pressure);
			return;
		}
		boolean pierce = JetRules.pierces(pressure);
		StrandHits.strike(player, session, first, JetRules.pulseDamage(pressure) * twin, pierce, Delivery.PROJECTILE, look);
		if (!pierce) return;
		// At full pressure the jet passes through to the next creature behind, along the same line to its full length.
		Vec3 end = clip(player, eye, eye.add(look.scale(length(player, session))));
		double firstDistance = eye.distanceToSqr(contact);
		List<Vec3[]> line = List.<Vec3[]>of(new Vec3[] {eye, end});
		ManipulationCombatHelper.swept(player, line, 0.3D).stream()
				.filter(other -> other != first && eye.distanceToSqr(other.getBoundingBox().getCenter()) > firstDistance)
				.min(Comparator.comparingDouble(other -> eye.distanceToSqr(other.getBoundingBox().getCenter())))
				.ifPresent(behind -> StrandHits.strike(player, session, behind, JetRules.pulseDamage(pressure) * twin, true,
						Delivery.PROJECTILE, look));
		if (player.level() instanceof ServerLevel level)
			accent(level, player, session, StrandCue.Accent.PIERCE, contact, contact.add(look), 1.0D);
	}

	// ── Spray ──

	private static void sprayPulse(ServerPlayer player, StrandSession session, boolean drench) {
		double range = length(player, session) * (drench ? SprayRules.DRENCH_REACH : 1.0D);
		Vec3 eye = player.getEyePosition();
		float damage = SprayRules.PULSE_DAMAGE * (drench ? SprayRules.DRENCH_MULTIPLIER : 1.0F);
		// Two hands fan a wider cone (Second Strand).
		double minDot = session.twin ? SprayRules.TWIN_MIN_DOT : SprayRules.MIN_DOT;
		for (LivingEntity target : ManipulationCombatHelper.cone(player, eye, player.getLookAngle(), range, minDot)) {
			// The spray's job is the state: every pulse lays it, at the long duration.
			StrandHits.strike(player, session, target, damage, true, Delivery.AREA, target.position().subtract(eye));
		}
		if (drench && player.level() instanceof ServerLevel level) {
			Vec3 at = eye.add(player.getLookAngle().scale(range * 0.6D));
			accent(level, player, session, StrandCue.Accent.DRENCH, eye, at, range);
		}
	}

	// ── Plant ──

	private static void plant(Player player, StrandSession session, VerbTarget.Block block) {
		session.plantPos = block.pos();
		session.plantPoint = block.point();
		session.plantStain = GroundNetwork.wetStainAt(player.level(), block.hit());
		session.sunkPulses = 0;
	}

	private static void plantPulse(ServerPlayer player, StrandSession session, VerbTarget target) {
		if (session.plantPoint == null) {
			if (target instanceof VerbTarget.Block block && !StrandSculpting.takesBlood(player.level(), block))
				plant(player, session, block);
			else return;
		}
		// Two hands sink twice the blood for one paid pulse (Second Strand); the cap still holds.
		session.sunkPulses = Math.min(PlantRules.CAP_PULSES,
				session.sunkPulses + (session.twin ? StrandSkills.TWIN_PLANT_PULSES : 1));
		if (PlantRules.full(session.sunkPulses)) trigger(player, session);
		else if (player.level() instanceof ServerLevel level && session.sunkPulses == 1)
			accent(level, player, session, StrandCue.Accent.TELEGRAPH, session.plantPoint, session.plantPoint.add(0, 1, 0), PlantRules.MIN_RADIUS);
	}

	/**
	 * Sets the plant off: it erupts after its beat. A plant with nothing sunk fizzles. A plant sunk into a wet stain runs
	 * along it instead: one small eruption a wet cell, each a beat after the last.
	 */
	private static void trigger(ServerPlayer player, StrandSession session) {
		if (session.plantPoint == null) return;
		if (PlantRules.primed(session.sunkPulses) && player.level() instanceof ServerLevel level) {
			long at = level.getGameTime() + PlantRules.ERUPTION_DELAY;
			float damage = PlantRules.damage(session.sunkPulses);
			List<BlockPos> run = session.plantStain == null ? List.of()
					: PlantRules.stainRun(session.plantStain, pos -> GroundNetwork.isWetStain(level, pos),
							PlantRules.stainCells(session.sunkPulses));
			if (run.isEmpty()) {
				ERUPTIONS.add(new Eruption(player.getUUID(), level, session.plantPoint, PlantRules.radius(session.sunkPulses),
						damage, at, session, new HashSet<>(), null));
			} else {
				HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_STAIN_RUN);
				Set<Integer> struck = new HashSet<>();
				for (int i = 0; i < run.size(); i++) {
					BlockPos cell = run.get(i);
					ERUPTIONS.add(new Eruption(player.getUUID(), level, Vec3.atBottomCenterOf(cell), PlantRules.STAIN_CELL_RADIUS,
							damage, at + (long) i * PlantRules.STAIN_STEP_TICKS, session, struck, cell));
				}
			}
		}
		session.plantPoint = null;
		session.plantPos = null;
		session.plantStain = null;
		session.sunkPulses = 0;
	}

	private static void erupt(Eruption eruption) {
		ServerLevel level = eruption.level();
		if (!(level.getPlayerByUUID(eruption.caster()) instanceof ServerPlayer player) || !player.isAlive()) return;
		double radius = eruption.radius();
		Vec3 point = eruption.point();
		boolean run = eruption.stain() != null;
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(point, point).inflate(radius, 2.5D, radius),
				target -> ManipulationCombatHelper.canHarm(player, target)
						&& horizontal(target.position(), point) <= radius + target.getBbWidth() * 0.5D)) {
			if (!eruption.struck().add(target.getId())) continue;
			StrandHits.strike(player, eruption.session(), target, eruption.damage(), true, Delivery.AREA, new Vec3(0, 1, 0));
		}
		// A run along a stain spends the stain's wet blood as it passes.
		if (run) GroundNetwork.spendStain(player, eruption.stain());
		accent(level, player, eruption.session(), StrandCue.Accent.ERUPTION, point, point.add(0, 1, 0), radius);
		level.playSound(null, point.x, point.y, point.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS,
				run ? 0.25F : 0.6F, run ? 1.8F : 1.4F);
	}

	private static double horizontal(Vec3 a, Vec3 b) {
		double dx = a.x - b.x, dz = a.z - b.z;
		return Math.sqrt(dx * dx + dz * dz);
	}

	@SubscribeEvent
	public static void tick(ServerTickEvent.Post event) {
		tails();
		if (ERUPTIONS.isEmpty()) return;
		List<Eruption> due = new ArrayList<>();
		for (Iterator<Eruption> it = ERUPTIONS.iterator(); it.hasNext(); ) {
			Eruption eruption = it.next();
			if (eruption.level().getGameTime() >= eruption.at()) {
				due.add(eruption);
				it.remove();
			}
		}
		due.forEach(StrandService::erupt);
	}

	// ── Brand ──

	private static void brandTick(ServerPlayer player, StrandSession session, VerbTarget aimed) {
		LivingEntity on = harmable(player, aimed);
		if (BrandRules.tick(session.brand, on == null ? -1 : on.getId(), session.brandSetTicks()) && on != null)
			setBrand(player, session, on);
	}

	/** The creature aimed at when the caster may harm it, else null. */
	@Nullable
	private static LivingEntity harmable(ServerPlayer player, VerbTarget aimed) {
		return aimed instanceof VerbTarget.Creature creature && ManipulationCombatHelper.canHarm(player, creature.living())
				? creature.living() : null;
	}

	private static void setBrand(ServerPlayer player, StrandSession session, LivingEntity target) {
		if (!(player.level() instanceof ServerLevel level)) return;
		brandOne(level, player, session, target);
		if (!session.twin) return;
		// The off hand's strand takes on the nearest other body the caster may harm (Second Strand).
		double reach = StrandSkills.TWIN_BRAND_RADIUS;
		Vec3 centre = target.getBoundingBox().getCenter();
		level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(reach),
						other -> other != target && other != player && ManipulationCombatHelper.canHarm(player, other)
								&& other.getBoundingBox().getCenter().distanceTo(centre) <= reach)
				.stream().min(Comparator.comparingDouble(other -> other.getBoundingBox().getCenter().distanceToSqr(centre)))
				.ifPresent(other -> brandOne(level, player, session, other));
	}

	private static void brandOne(ServerLevel level, ServerPlayer player, StrandSession session, LivingEntity target) {
		StrandHits.mark(player, session, target);
		HemoCapabilityAccess.setStrandBrand(target, new StrandBrand(player.getUUID(), session.manipulation.getTend(),
				level.getGameTime() + StrandSkills.brandTicks(session.deepBrand)));
		Vec3 at = target.getBoundingBox().getCenter();
		accent(level, player, session, StrandCue.Accent.BRAND, at, at.add(at.subtract(player.getEyePosition()).normalize()),
				Math.max(0.6D, target.getBbWidth()));
		level.playSound(null, at.x, at.y, at.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.5F, 0.6F);
	}

	/**
	 * Lays a brand naming {@code player} in {@code school} on {@code target} without a strand: Blood Binding is the
	 * Animus brand, and a binding that takes brands what it holds. Lasts as long as the caster's Deep Brand allows; the
	 * set flourish shows only when the creature carried no live brand of theirs. Returns whether it took.
	 */
	public static boolean brand(ServerPlayer player, EnumBloodTemper school, LivingEntity target) {
		if (!(player.level() instanceof ServerLevel level) || !ManipulationCombatHelper.canHarm(player, target)) return false;
		boolean fresh = brandFor(target, player.getUUID()) == null;
		HemoCapabilityAccess.setStrandBrand(target, new StrandBrand(player.getUUID(), school,
				level.getGameTime() + StrandSkills.brandTicks(SkillPointHelper.getDeepBrandLevel(player))));
		if (fresh) {
			HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_BOUND_AND_BRANDED);
			Vec3 at = target.getBoundingBox().getCenter();
			ManipulationVisuals.burst(level, ManipulationVisuals.Form.STRAND_ACCENT, at,
					at.add(at.subtract(player.getEyePosition()).normalize()), Math.max(0.6D, target.getBbWidth()),
					accentLife(StrandCue.Accent.BRAND), StrandCue.encodeAccent(StrandCue.Accent.BRAND, school, null));
		}
		return true;
	}

	/** The brand {@code target} carries for {@code caster}, if it still holds; lapsed brands are taken off. */
	@Nullable
	public static StrandBrand brandFor(LivingEntity target, UUID caster) {
		StrandBrand brand = HemoCapabilityAccess.findStrandBrand(target);
		if (brand == null) return null;
		long now = target.level().getGameTime();
		if (!BrandRules.holds(brand.until(), now)) {
			if (!target.level().isClientSide) HemoCapabilityAccess.setStrandBrand(target, null);
			return null;
		}
		return brand.caster().equals(caster) ? brand : null;
	}

	// ── Shared ──

	/** The strand's length now: the verb's reach on the hand or the Staff, by the form, lengthened by Long Strand. */
	public static double length(Player player, StrandSession session) {
		LashProfile lash = session.sculpting.spec().lash();
		return session.form().length(VerbReach.of(player, Verb.PROJECT)) * StrandSkills.lengthScale(session.longStrand)
				* (lash == null ? 1.0D : lash.style().length());
	}

	private static Vec3 hand(ServerPlayer player, StrandSession session) {
		return StrandCue.hand(player.getEyePosition(), player.getLookAngle(), player.getYRot(), session.leftArm);
	}

	private static boolean leftArm(Player player) {
		// Not channelling (a tap): the arm of whichever hand holds the Projection tool.
		InteractionHand held = player.isUsingItem() ? player.getUsedItemHand()
				: VerbChannel.verbOf(player, player.getMainHandItem()).orElse(null) == Verb.PROJECT ? InteractionHand.MAIN_HAND
				: VerbChannel.verbOf(player, player.getOffhandItem()).orElse(null) == Verb.PROJECT ? InteractionHand.OFF_HAND
				: InteractionHand.MAIN_HAND;
		HumanoidArm arm = held == InteractionHand.MAIN_HAND ? player.getMainArm()
				: player.getMainArm().getOpposite();
		return arm == HumanoidArm.LEFT;
	}

	private static Vec3 clip(Player player, Vec3 from, Vec3 to) {
		BlockHitResult hit = player.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE, player));
		if (hit.getType() != HitResult.Type.BLOCK) return to;
		Vec3 back = from.subtract(to);
		return hit.getLocation().add(back.lengthSqr() < 1e-8 ? Vec3.ZERO : back.normalize().scale(0.05D));
	}

	private static VerbTarget aim(ServerPlayer player, StrandSession session) {
		double reach = VerbReach.scaled(VerbReach.of(player, Verb.PROJECT), session.sculpting.reachScale(player));
		return com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargeting
				.pick(player.level(), player, reach, session.sculpting.wants())
				.orElseGet(() -> new VerbTarget.Air(player.getEyePosition().add(player.getLookAngle().scale(reach))));
	}

	/** The keyed cue every client draws its own copy of the strand from; refreshed each pulse. */
	private static void cue(ServerPlayer player, StrandSession session) {
		double param = switch (session.form()) {
			case LASH -> session.lash.charge();
			case JET -> session.jet.pressure;
			case PLANT -> PlantRules.fraction(session.sunkPulses);
			case BRAND -> session.brandProgress();
			default -> 0.0D;
		};
		// The skill every client needs to run this caster's motion rather than its own: Taut Line for a lash or jet,
		// Deep Brand for a brand.
		int skill = switch (session.form()) {
			case LASH, JET -> session.tautLine;
			case BRAND -> session.deepBrand;
			default -> 0;
		};
		Vec3 to = session.plantPoint != null ? session.plantPoint : player.position();
		ManipulationVisuals.attached(player, ManipulationVisuals.Form.STRAND, to, length(player, session), StrandCue.LIFE,
				StrandCue.encode(session.form(), session.manipulation.getTend(), session.manipulation.getSecondaryTend(),
						session.leftArm, param, skill, session.twin, session.lash.phase()));
	}

	private static void accent(ServerLevel level, ServerPlayer player, StrandSession session, StrandCue.Accent accent,
			Vec3 at, Vec3 toward, double radius) {
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.STRAND_ACCENT, at, toward, radius, accentLife(accent),
				StrandCue.encodeAccent(accent, session.manipulation.getTend(), session.manipulation.getSecondaryTend()));
	}

	private static int accentLife(StrandCue.Accent accent) {
		return switch (accent) {
			case ERUPTION -> 24;
			case TELEGRAPH -> PlantRules.ERUPTION_DELAY;
			case BRAND -> 20;
			case TAP -> 11;
			default -> 14;
		};
	}

	public static void clearSessionState() {
		SESSIONS.clear();
		LAST_TAP.clear();
		LAST_SNAP.clear();
		TAP_CHAIN.clear();
		ERUPTIONS.clear();
		TAILS.clear();
	}

	/** A tap's cooldown and chain belong to the body that struck them: a death or a change of dimension ends them. */
	public static void forgetTaps(Player player) {
		LAST_TAP.remove(player.getUUID());
		TAP_CHAIN.remove(player.getUUID());
	}

	@SubscribeEvent
	public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
		forgetTaps(event.getEntity());
	}

	@SubscribeEvent
	public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		forgetTaps(event.getEntity());
	}

	public static void logout(Player player) {
		SESSIONS.remove(player.getUUID());
		LAST_TAP.remove(player.getUUID());
		LAST_SNAP.remove(player.getUUID());
		TAP_CHAIN.remove(player.getUUID());
		TAILS.removeIf(tail -> tail.player().getUUID().equals(player.getUUID()));
		ERUPTIONS.removeIf(eruption -> eruption.caster().equals(player.getUUID()));
	}

	/** Test hook: a lash's state, to script a stroke. */
	@Nullable
	public static LashRules.State lashOf(Player player) {
		StrandSession session = SESSIONS.get(player.getUUID());
		return session == null ? null : session.lash;
	}

	/** Test hook: the wet stain cell an open plant sank into, or null when it sank into bare ground. */
	@Nullable
	public static BlockPos plantStain(Player player) {
		StrandSession session = SESSIONS.get(player.getUUID());
		return session == null ? null : session.plantStain;
	}

	/** A position helper for tests and the HUD: the block a plant is sinking into, or null. */
	@Nullable
	public static BlockPos plantPos(Player player) {
		StrandSession session = SESSIONS.get(player.getUUID());
		return session == null ? null : session.plantPos;
	}
}
