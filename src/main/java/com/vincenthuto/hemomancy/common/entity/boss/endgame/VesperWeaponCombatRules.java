package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;

import java.util.EnumMap;
import java.util.Map;

import static com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWeaponAction.*;

public final class VesperWeaponCombatRules {
	private static final Map<EnumBloodTemper, RangeBand> RANGES = createRanges();

	private VesperWeaponCombatRules() { }

	public static VesperWeaponAction coreAction(EnumBloodTemper temper) {
		return switch (temper) {
			case ANIMUS -> ICHIMONJI;
			case MORTEM -> LEAPING_CLEAVE;
			case LUX -> SKY_LANCE;
			case TENEBRIS -> TWIN_REND;
			case DUCTILIS -> CONDUCTIVE_VOLLEY;
			case FLAMMEUS -> BRANDING_THRUSTS;
			case CONGEATIO -> CHAIN_SWEEP;
			case FERRIC -> MAGNETIC_AXIS;
		};
	}

	public static VesperWeaponAction advancedAction(EnumBloodTemper temper) {
		return switch (temper) {
			case ANIMUS -> CROSSCUT;
			case MORTEM -> REAPER_SWEEP;
			case LUX -> LANCE_FLURRY;
			case TENEBRIS -> PREDATOR_POUNCE;
			case DUCTILIS -> STORM_LOCK;
			case FLAMMEUS -> FLAMMEUS_CONCENTRATION;
			case CONGEATIO -> HOOK_AND_CRUSH;
			case FERRIC -> IRON_RETORT;
		};
	}

	public static VesperWeaponAction selectAction(EnumBloodTemper temper, boolean advancedUnlocked,
			VesperWeaponAction previous, double distance) {
		return selectAction(temper, advancedUnlocked, previous, distance, 0.0D, 0.0D, true);
	}

	public static VesperWeaponAction selectAction(EnumBloodTemper temper, boolean advancedUnlocked,
			VesperWeaponAction previous, double distance, double attackAngle, double elevation,
			boolean hasLineOfSight) {
		if (!hasLineOfSight) return NONE;
		VesperWeaponAction core = coreAction(temper);
		if (!advancedUnlocked) return core;
		VesperWeaponAction advanced = advancedAction(temper);
		if (previous == core) return advanced;
		if (previous == advanced) return core;
		RangeBand band = rangeBand(temper);
		VesperWeaponAction selected;
		if (temper == EnumBloodTemper.LUX && Math.abs(elevation) >= 2.5D) selected = core;
		else if (temper == EnumBloodTemper.TENEBRIS && attackAngle >= 70.0D) selected = core;
		else selected = distance < band.minimum() || distance <= (band.minimum() + band.maximum()) * 0.5D
					? advanced : core;
		return selected == previous ? (selected == core ? advanced : core) : selected;
	}

	public static RangeBand rangeBand(EnumBloodTemper temper) { return RANGES.get(temper); }

	public static int recoveryTicks(int normalTicks, float healthFraction) {
		return healthFraction <= 0.25F ? Math.max(1, Math.round(normalTicks * 0.8F)) : normalTicks;
	}

	public static boolean canApplyHit(int hitMask, int hitIndex) {
		return (hitMask & (1 << hitIndex)) == 0;
	}

	public static int recordHit(int hitMask, int hitIndex) {
		return hitMask | (1 << hitIndex);
	}

	public static boolean mayAdvanceStance(int stanceTick, int stanceDuration, VesperWeaponAction action) {
		return stanceTick >= stanceDuration && action == NONE;
	}

	public static boolean withinLane(double along, double lateralDistance, double length, double width) {
		return along >= 0.0D && along <= length && lateralDistance <= width;
	}

	public static boolean withinArc(double distance, double angleDegrees, double range, double arcDegrees) {
		return distance <= range && angleDegrees <= arcDegrees * 0.5D;
	}

	private static Map<EnumBloodTemper, RangeBand> createRanges() {
		EnumMap<EnumBloodTemper, RangeBand> ranges = new EnumMap<>(EnumBloodTemper.class);
		ranges.put(EnumBloodTemper.ANIMUS, new RangeBand(3.0D, 6.0D));
		ranges.put(EnumBloodTemper.MORTEM, new RangeBand(3.0D, 7.0D));
		ranges.put(EnumBloodTemper.LUX, new RangeBand(7.0D, 14.0D));
		ranges.put(EnumBloodTemper.TENEBRIS, new RangeBand(2.0D, 5.0D));
		ranges.put(EnumBloodTemper.DUCTILIS, new RangeBand(12.0D, 20.0D));
		ranges.put(EnumBloodTemper.FLAMMEUS, new RangeBand(2.0D, 5.0D));
		ranges.put(EnumBloodTemper.CONGEATIO, new RangeBand(5.0D, 9.0D));
		ranges.put(EnumBloodTemper.FERRIC, new RangeBand(8.0D, 14.0D));
		return Map.copyOf(ranges);
	}

	public record RangeBand(double minimum, double maximum) { }
}
