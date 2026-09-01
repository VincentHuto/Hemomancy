package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;

import java.util.*;

public final class VesperCombatRules {
	public static final int DEFEAT_ANIMATION_TICKS = 40;
	public static final int WEAPON_DISSOLVE_TICKS = 28;
	public static final float DEFEAT_ABSORPTION_REQUIRED = 100.0F;
	public static final float ANCHOR_MAX_DAMAGE = 40.0F;
	public static final float ANCHOR_HITBOX_WIDTH = 2.0F;
	public static final float ANCHOR_HITBOX_HEIGHT = 2.2F;
	public static final int ANCHOR_HIT_FLASH_TICKS = 4;
	private static final double ANCHOR_FORWARD_DISTANCE = 2.0D;
	private static final double ANCHOR_BASE_Y_OFFSET = 2.0D;
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

	public static float defeatRecoilProgress(float downedTicks) {
		float phase = clamp(downedTicks / 6.0F);
		return 1.0F - Math.abs(phase * 2.0F - 1.0F);
	}

	public static float defeatKneelProgress(float downedTicks) {
		return smoothstep(clamp((downedTicks - 6.0F) / 26.0F));
	}

	public static float weaponDissolveProgress(float downedTicks) {
		return smoothstep(clamp(downedTicks / WEAPON_DISSOLVE_TICKS));
	}

	public static float sigilFizzleProgress(float downedTicks, int sigilIndex) {
		int reverseIndex = 7 - Math.max(0, Math.min(7, sigilIndex));
		float startTick = 4.0F + reverseIndex * 4.0F;
		return smoothstep(clamp((downedTicks - startTick) / 8.0F));
	}

	public static boolean isDefeatAnimationComplete(int downedTicks) {
		return downedTicks >= DEFEAT_ANIMATION_TICKS;
	}

	public static float advanceDefeatAbsorption(float progress, float amount) {
		return Math.min(DEFEAT_ABSORPTION_REQUIRED,
				Math.max(0.0F, progress) + Math.max(0.0F, amount));
	}

	public static boolean isDefeatAbsorptionComplete(float progress) {
		return progress >= DEFEAT_ABSORPTION_REQUIRED;
	}

	private static float clamp(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}

	private static float smoothstep(float value) {
		return value * value * (3.0F - 2.0F * value);
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
		float damage = clampAnchorDamage(Math.max(0.0F, accumulatedDamage) + Math.max(0.0F, incomingDamage));
		return new AnchorHit(damage, damage >= ANCHOR_MAX_DAMAGE);
	}

	public static float clampAnchorDamage(float damage) {
		return Math.max(0.0F, Math.min(ANCHOR_MAX_DAMAGE, damage));
	}

	public static AnchorDamageBand anchorDamageBand(float accumulatedDamage) {
		float ratio = clampAnchorDamage(accumulatedDamage) / ANCHOR_MAX_DAMAGE;
		if (ratio >= 0.70F) return AnchorDamageBand.HIGH;
		if (ratio >= 0.40F) return AnchorDamageBand.MEDIUM;
		return AnchorDamageBand.LOW;
	}

	public static float anchorFlashStrength(int remainingTicks) {
		return Math.max(0.0F, Math.min(1.0F, remainingTicks / (float) ANCHOR_HIT_FLASH_TICKS));
	}

	public static float anchorPulseSpeed(float accumulatedDamage) {
		return switch (anchorDamageBand(accumulatedDamage)) {
			case LOW -> 0.08F;
			case MEDIUM -> 0.14F;
			case HIGH -> 0.22F;
		};
	}

	public static float anchorSurfaceAgitation(float accumulatedDamage) {
		return switch (anchorDamageBand(accumulatedDamage)) {
			case LOW -> 0.35F;
			case MEDIUM -> 0.70F;
			case HIGH -> 1.15F;
		};
	}

	public static float anchorHitboxScale(int anchorIndex, int activeAnchorIndex) {
		return anchorIndex == activeAnchorIndex ? 1.0F : 0.0F;
	}

	public static AnchorOffset anchorForwardOffset(float yawDegrees, double distance) {
		double yawRadians = Math.toRadians(yawDegrees);
		return new AnchorOffset(-Math.sin(yawRadians) * distance, Math.cos(yawRadians) * distance);
	}

	public static AnchorCenter anchorCenter(double x, double y, double z, float yawDegrees) {
		AnchorOffset forward = anchorForwardOffset(yawDegrees, ANCHOR_FORWARD_DISTANCE);
		return new AnchorCenter(x + forward.x(), y + ANCHOR_BASE_Y_OFFSET + ANCHOR_HITBOX_HEIGHT * 0.5D,
				z + forward.z());
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

	public record AnchorOffset(double x, double z) {
	}

	public record AnchorCenter(double x, double y, double z) {
	}

	public enum AnchorDamageBand {
		LOW,
		MEDIUM,
		HIGH
	}
}
