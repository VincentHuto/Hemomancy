package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class VesperCombatRules {
	public static final int SIGIL_DISSOLVE_TICKS = 40;
	public static final float DEFEAT_ABSORPTION_REQUIRED = 100.0F;
	public static final float ANCHOR_MAX_DAMAGE = 40.0F;
	private static final float[] ANCHOR_THRESHOLDS = { 0.72F, 0.42F, 0.12F };
	private static final Map<EnumBloodTendency, StanceProfile> STANCES = createStances();

	private VesperCombatRules() {
	}

	public static int lockedAnchorIndex(float health, float maxHealth, int brokenAnchorMask) {
		if (maxHealth <= 0.0F) return -1;
		for (int anchor = 0; anchor < ANCHOR_THRESHOLDS.length; anchor++) {
			if ((brokenAnchorMask & (1 << anchor)) == 0
					&& health <= maxHealth * ANCHOR_THRESHOLDS[anchor] + 0.001F) {
				return anchor;
			}
		}
		return -1;
	}

	public static float healthFloor(float maxHealth, int brokenAnchorMask) {
		for (int anchor = 0; anchor < ANCHOR_THRESHOLDS.length; anchor++) {
			if ((brokenAnchorMask & (1 << anchor)) == 0) {
				return maxHealth * ANCHOR_THRESHOLDS[anchor];
			}
		}
		return 0.0F;
	}

	public static List<EnumBloodTendency> tendencyTour(long seed) {
		List<EnumBloodTendency> result = new ArrayList<>(List.of(EnumBloodTendency.values()));
		Collections.shuffle(result, new Random(seed));
		return List.copyOf(result);
	}

	public static EnumBloodTendency tendencyAt(long seed, int stanceIndex) {
		List<EnumBloodTendency> tour = tendencyTour(seed);
		return tour.get(Math.floorMod(stanceIndex, tour.size()));
	}

	public static boolean isMorphTelegraph(int stanceTick) {
		return stanceTick >= 0 && stanceTick < 30;
	}

	public static int primaryAttackTick() { return 40; }

	public static int secondaryAttackTick() { return 118; }

	public static StanceProfile profile(EnumBloodTendency tendency) {
		return STANCES.get(tendency);
	}

	public static int stanceDuration(float health, float maxHealth) {
		if (maxHealth > 0.0F && health <= maxHealth * 0.25F) return 140;
		if (mayUseSecondary(health, maxHealth)) return 180;
		return 240;
	}

	public static boolean mayUseSecondary(float health, float maxHealth) {
		return maxHealth > 0.0F && health <= maxHealth * 0.60F;
	}

	public static float sigilDissolveAlpha(int downedTicks) {
		return Math.max(0.0F, 1.0F - Math.max(0, downedTicks) / (float) SIGIL_DISSOLVE_TICKS);
	}

	public static float advanceDefeatAbsorption(float progress, float amount) {
		return Math.min(DEFEAT_ABSORPTION_REQUIRED,
				Math.max(0.0F, progress) + Math.max(0.0F, amount));
	}

	public static boolean isDefeatAbsorptionComplete(float progress) {
		return progress >= DEFEAT_ABSORPTION_REQUIRED;
	}

	public static VesperPhaseOneAttack phaseOneAttack(int attackStep) {
		VesperPhaseOneAttack[] cycle = {
				VesperPhaseOneAttack.ROYAL_SCUTTLE,
				VesperPhaseOneAttack.PINCER_VICE,
				VesperPhaseOneAttack.STINGER_SCRIPT,
				VesperPhaseOneAttack.BROOD_TRAMPLE,
				VesperPhaseOneAttack.PUPPET_MUSTER
		};
		return cycle[Math.floorMod(attackStep, cycle.length)];
	}

	public static AnchorHit hitAnchor(float accumulatedDamage, float incomingDamage) {
		float damage = Math.min(ANCHOR_MAX_DAMAGE,
				Math.max(0.0F, accumulatedDamage) + Math.max(0.0F, incomingDamage));
		return new AnchorHit(damage, damage >= ANCHOR_MAX_DAMAGE);
	}

	public static float anchorHitboxScale(int anchorIndex, int activeAnchorIndex) {
		return anchorIndex == activeAnchorIndex ? 1.0F : 0.0F;
	}

	private static Map<EnumBloodTendency, StanceProfile> createStances() {
		EnumMap<EnumBloodTendency, StanceProfile> result = new EnumMap<>(EnumBloodTendency.class);
		result.put(EnumBloodTendency.ANIMUS, new StanceProfile("blade", "blood_rush", "blood_aneurysm"));
		result.put(EnumBloodTendency.MORTEM, new StanceProfile("axe", "exsanguinate", "grave_debt"));
		result.put(EnumBloodTendency.LUX, new StanceProfile("spear", "prismatic_reproof", "hematic_flare"));
		result.put(EnumBloodTendency.TENEBRIS, new StanceProfile("claws", "umbral_step", "gloam_laceration"));
		result.put(EnumBloodTendency.DUCTILIS, new StanceProfile("crossbow", "conductive_mark", "synaptic_jolt"));
		result.put(EnumBloodTendency.FLAMMEUS, new StanceProfile("torch", "sanguine_ignition", "scalding_updraft"));
		result.put(EnumBloodTendency.CONGEATIO, new StanceProfile("flail", "glacial_grasp", "glacial_rampart"));
		result.put(EnumBloodTendency.FERRIC, new StanceProfile("staff", "sanguine_magnetism", "iron_retort"));
		return Map.copyOf(result);
	}

	public record StanceProfile(String weapon, String primaryManipulation, String secondaryManipulation) {
	}

	public record AnchorHit(float accumulatedDamage, boolean broken) {
	}
}
