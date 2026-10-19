package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWeaponAction.*;

/**
 * The Evening Star's chained movesets. Every weapon has an opener (or two: a close one and a gap closer), follow-ups
 * and a finisher, each ending in a pose the next move starts from, so a string flows instead of resetting to idle.
 * In the Open act a follow-up may hand the string to another school whose move starts in the same pose: that is the
 * blend point where his weapon and school swap mid-animation.
 */
public final class VesperChainGraph {
	public static final int MAX_CHAIN = 5;

	public enum Pose { NEUTRAL, HIGH_GUARD, LOW_GUARD, EXTENDED, AIRBORNE, SPINNING }

	public enum Role { OPENER, FOLLOW_UP, FINISHER }

	private record Node(Role role, Pose entry, Pose exit, boolean gapCloser) {
	}

	public record Context(double distance, boolean lastHitLanded, boolean targetShielding, int chainLength,
			RandomSource random) {
	}

	private static final Map<VesperWeaponAction, Node> NODES = new EnumMap<>(VesperWeaponAction.class);
	private static final Map<EnumBloodTendency, List<VesperWeaponAction>> MOVESETS = new EnumMap<>(EnumBloodTendency.class);

	static {
		// Weapons that share a mid-string pose can hand a string to each other in the Open act:
		// blade <-> torch (high guard), axe <-> flail (low guard), spear <-> claws (airborne),
		// crossbow <-> staff (extended).
		weapon(EnumBloodTendency.ANIMUS, Pose.EXTENDED, Pose.HIGH_GUARD,
				List.of(ICHIMONJI), List.of(), CROSSCUT, BLADE_RISING_CUT, BLADE_DRAWING_STEP);
		weapon(EnumBloodTendency.FLAMMEUS, Pose.EXTENDED, Pose.HIGH_GUARD,
				List.of(BRANDING_THRUSTS), List.of(), TORCH_CINDER_SWEEP, UPDRAFT_IMPALEMENT, FLAMMEUS_CONCENTRATION);
		weapon(EnumBloodTendency.MORTEM, Pose.HIGH_GUARD, Pose.LOW_GUARD,
				List.of(AXE_HAFT_SHOVE), List.of(LEAPING_CLEAVE), REAPER_SWEEP, null, AXE_GRAVE_SPLITTER);
		weapon(EnumBloodTendency.CONGEATIO, Pose.SPINNING, Pose.LOW_GUARD,
				List.of(CHAIN_SWEEP), List.of(), HOOK_AND_CRUSH, FLAIL_ORBIT, FLAIL_SHATTER_SLAM);
		weapon(EnumBloodTendency.LUX, Pose.EXTENDED, Pose.AIRBORNE,
				List.of(), List.of(SKY_LANCE), LANCE_FLURRY, SPEAR_VAULT_KICK, SPEAR_PINNING_THRUST);
		weapon(EnumBloodTendency.TENEBRIS, Pose.LOW_GUARD, Pose.AIRBORNE,
				List.of(CLAW_SHADOW_FEINT), List.of(PREDATOR_POUNCE), TWIN_REND, null, CLAW_EVISCERATE);
		weapon(EnumBloodTendency.DUCTILIS, Pose.EXTENDED, Pose.EXTENDED,
				List.of(CROSSBOW_SNAP_SHOT), List.of(), CONDUCTIVE_VOLLEY, CROSSBOW_BOLT_RAIN, STORM_LOCK);
		weapon(EnumBloodTendency.FERRIC, Pose.LOW_GUARD, Pose.EXTENDED,
				List.of(MAGNETIC_AXIS), List.of(), STAFF_LODESTONE_SWEEP, STAFF_PILLAR_VAULT, IRON_RETORT);
	}

	private VesperChainGraph() {
	}

	/** opener exit = openerPose; follow-ups run openerPose -> stringPose -> stringPose; finisher stringPose -> neutral. */
	private static void weapon(EnumBloodTendency tendency, Pose openerPose, Pose stringPose,
			List<VesperWeaponAction> closeOpeners, List<VesperWeaponAction> gapClosers, VesperWeaponAction first,
			VesperWeaponAction second, VesperWeaponAction finisher) {
		List<VesperWeaponAction> moves = new ArrayList<>();
		for (VesperWeaponAction opener : closeOpeners) {
			NODES.put(opener, new Node(Role.OPENER, Pose.NEUTRAL, openerPose, false));
			moves.add(opener);
		}
		for (VesperWeaponAction opener : gapClosers) {
			NODES.put(opener, new Node(Role.OPENER, Pose.NEUTRAL, openerPose, true));
			moves.add(opener);
		}
		NODES.put(first, new Node(Role.FOLLOW_UP, openerPose, stringPose, false));
		moves.add(first);
		if (second != null) {
			NODES.put(second, new Node(Role.FOLLOW_UP, stringPose, stringPose, false));
			moves.add(second);
		}
		NODES.put(finisher, new Node(Role.FINISHER, stringPose, Pose.NEUTRAL, false));
		moves.add(finisher);
		MOVESETS.put(tendency, List.copyOf(moves));
	}

	public static List<VesperWeaponAction> moveset(EnumBloodTendency tendency) {
		return MOVESETS.get(tendency);
	}

	public static boolean inGraph(VesperWeaponAction action) {
		return NODES.containsKey(action);
	}

	public static Role role(VesperWeaponAction action) {
		Node node = NODES.get(action);
		return node == null ? Role.OPENER : node.role();
	}

	public static Pose entry(VesperWeaponAction action) {
		Node node = NODES.get(action);
		return node == null ? Pose.NEUTRAL : node.entry();
	}

	public static Pose exit(VesperWeaponAction action) {
		Node node = NODES.get(action);
		return node == null ? Pose.NEUTRAL : node.exit();
	}

	/** The next move of the string, or an opener when the string ends. */
	public static VesperWeaponAction next(VesperWeaponAction previous, EnumBloodTendency tendency,
			VesperEveningStarAct act, Context ctx) {
		boolean ends = previous == NONE || !inGraph(previous) || role(previous) == Role.FINISHER
				|| (ctx.chainLength() >= MAX_CHAIN && role(previous) != Role.OPENER);
		// A whiffed opener at range gives up on the string and closes the gap again.
		if (!ends && !ctx.lastHitLanded() && ctx.distance() > 9.0D && role(previous) == Role.OPENER) ends = true;
		if (ends) return opener(tendency, previous, ctx);
		List<VesperWeaponAction> candidates = new ArrayList<>();
		List<Integer> weights = new ArrayList<>();
		for (Map.Entry<EnumBloodTendency, List<VesperWeaponAction>> set : MOVESETS.entrySet()) {
			boolean sameSchool = set.getKey() == tendency;
			if (!sameSchool && !act.castsAnySchool()) continue;
			for (VesperWeaponAction move : set.getValue()) {
				if (move == previous || role(move) == Role.OPENER || entry(move) != exit(previous)) continue;
				int weight = sameSchool ? 4 : 2;
				if (role(move) == Role.FINISHER) {
					weight += ctx.chainLength();
					if (ctx.targetShielding()) weight += 4;
				}
				candidates.add(move);
				weights.add(weight);
			}
		}
		if (candidates.isEmpty()) return opener(tendency, previous, ctx);
		int total = weights.stream().mapToInt(Integer::intValue).sum();
		int roll = ctx.random().nextInt(total);
		for (int i = 0; i < candidates.size(); i++) {
			roll -= weights.get(i);
			if (roll < 0) return candidates.get(i);
		}
		return candidates.getLast();
	}

	private static VesperWeaponAction opener(EnumBloodTendency tendency, VesperWeaponAction previous, Context ctx) {
		List<VesperWeaponAction> openers = moveset(tendency).stream().filter(move -> role(move) == Role.OPENER).toList();
		boolean far = ctx.distance() > 8.0D;
		VesperWeaponAction pick = openers.stream().filter(move -> NODES.get(move).gapCloser() == far).findFirst()
				.orElse(openers.getFirst());
		if (pick != previous) return pick;
		// Never the same opener twice running: take the other opener, or flow straight into the first follow-up.
		for (VesperWeaponAction other : openers) if (other != previous) return other;
		return moveset(tendency).stream().filter(move -> role(move) == Role.FOLLOW_UP).findFirst().orElse(pick);
	}
}
