package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;

public enum VesperWeaponAction {
	NONE(null, 0, 0, 0),
	ICHIMONJI(EnumBloodTendency.ANIMUS, 18, 18, 30, 18),
	CROSSCUT(EnumBloodTendency.ANIMUS, 12, 26, 38, 15, 26),
	LEAPING_CLEAVE(EnumBloodTendency.MORTEM, 22, 22, 34, 22),
	REAPER_SWEEP(EnumBloodTendency.MORTEM, 18, 18, 30, 18),
	SKY_LANCE(EnumBloodTendency.LUX, 16, 25, 37, 17),
	LANCE_FLURRY(EnumBloodTendency.LUX, 12, 28, 40, 12, 20, 28),
	TWIN_REND(EnumBloodTendency.TENEBRIS, 12, 20, 34, 12, 20),
	PREDATOR_POUNCE(EnumBloodTendency.TENEBRIS, 20, 20, 32, 20),
	CONDUCTIVE_VOLLEY(EnumBloodTendency.DUCTILIS, 20, 20, 32, 20),
	STORM_LOCK(EnumBloodTendency.DUCTILIS, 18, 44, 52, 28, 36, 44),
	BRANDING_THRUSTS(EnumBloodTendency.FLAMMEUS, 12, 26, 38, 12, 19, 26),
	UPDRAFT_IMPALEMENT(EnumBloodTendency.FLAMMEUS, 16, 24, 36, 16, 24),
	CHAIN_SWEEP(EnumBloodTendency.CONGEATIO, 18, 18, 30, 18),
	HOOK_AND_CRUSH(EnumBloodTendency.CONGEATIO, 14, 28, 40, 14, 28),
	MAGNETIC_AXIS(EnumBloodTendency.FERRIC, 18, 18, 30, 18),
	IRON_RETORT(EnumBloodTendency.FERRIC, 16, 32, 46, 16),
	SICKLE_CYCLONE(null, 12, 24, 34, 12, 18, 24),
	SICKLE_POUNCE(null, 14, 14, 24, 14),
	SICKLE_CROSS_REND(null, 10, 18, 28, 10, 14, 18),
	SICKLE_HOOK(null, 14, 14, 28, 14),
	SANGUINE_CRESCENTS(null, 12, 22, 32, 12, 17, 22);

	private final EnumBloodTendency tendency;
	private final int impactTick;
	private final int lastImpactTick;
	private final int durationTicks;
	private final int[] contactTicks;

	VesperWeaponAction(EnumBloodTendency tendency, int impactTick, int lastImpactTick, int durationTicks,
			int... contactTicks) {
		this.tendency = tendency;
		this.impactTick = impactTick;
		this.lastImpactTick = lastImpactTick;
		this.durationTicks = durationTicks;
		this.contactTicks = contactTicks;
	}

	public EnumBloodTendency tendency() { return tendency; }
	public int impactTick() { return impactTick; }
	public int lastImpactTick() { return lastImpactTick; }
	public int durationTicks() { return durationTicks; }
	public int contactCount() { return contactTicks.length; }
	public int contactTick(int index) { return contactTicks[index]; }
}
