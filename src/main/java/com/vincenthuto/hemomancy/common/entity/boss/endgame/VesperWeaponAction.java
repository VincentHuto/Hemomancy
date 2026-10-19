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
	SANGUINE_CRESCENTS(null, 12, 22, 32, 12, 17, 22),
	FLAMMEUS_CONCENTRATION(EnumBloodTendency.FLAMMEUS, 12, 48, 60,
			12, 16, 20, 24, 28, 32, 36, 40, 44, 48),
	// Chained-moveset additions. Appended only: the ordinal is synced and saved.
	BLADE_RISING_CUT(EnumBloodTendency.ANIMUS, 10, 10, 22, 10),
	BLADE_DRAWING_STEP(EnumBloodTendency.ANIMUS, 16, 24, 36, 16, 24),
	AXE_HAFT_SHOVE(EnumBloodTendency.MORTEM, 10, 10, 21, 10),
	AXE_GRAVE_SPLITTER(EnumBloodTendency.MORTEM, 24, 30, 42, 24, 30),
	SPEAR_VAULT_KICK(EnumBloodTendency.LUX, 12, 18, 30, 12, 18),
	SPEAR_PINNING_THRUST(EnumBloodTendency.LUX, 18, 18, 30, 18),
	CLAW_SHADOW_FEINT(EnumBloodTendency.TENEBRIS, 14, 14, 24, 14),
	CLAW_EVISCERATE(EnumBloodTendency.TENEBRIS, 12, 24, 38, 12, 16, 20, 24),
	CROSSBOW_SNAP_SHOT(EnumBloodTendency.DUCTILIS, 10, 10, 20, 10),
	CROSSBOW_BOLT_RAIN(EnumBloodTendency.DUCTILIS, 20, 32, 44, 20, 26, 32),
	TORCH_CINDER_SWEEP(EnumBloodTendency.FLAMMEUS, 14, 14, 26, 14),
	FLAIL_ORBIT(EnumBloodTendency.CONGEATIO, 12, 28, 36, 12, 20, 28),
	FLAIL_SHATTER_SLAM(EnumBloodTendency.CONGEATIO, 22, 22, 34, 22),
	STAFF_LODESTONE_SWEEP(EnumBloodTendency.FERRIC, 14, 14, 28, 14),
	STAFF_PILLAR_VAULT(EnumBloodTendency.FERRIC, 16, 22, 36, 16, 22),
	// The expanded dual-sickle act (VesperSickleRules).
	SICKLE_REAP_L(null, 10, 10, 18, 10),
	SICKLE_REAP_R(null, 10, 10, 18, 10),
	SICKLE_HOOK_REEL(null, 12, 24, 34, 12, 24),
	SICKLE_REEL_CYCLONE(null, 10, 26, 36, 10, 16, 21, 26),
	SICKLE_REAPERS_RISE(null, 12, 12, 24, 12),
	SICKLE_PASS_THROUGH(null, 10, 18, 28, 10, 18),
	SICKLE_GALLOWS_WHEEL(null, 16, 40, 50, 16, 22, 28, 34, 40),
	SICKLE_TRAVELLING_CYCLONE(null, 14, 38, 48, 14, 20, 26, 32, 38),
	SICKLE_REVERSE_CYCLONE(null, 20, 32, 42, 20, 32),
	SICKLE_FEINT_CYCLONE(null, 10, 10, 18, 10),
	SICKLE_POUNCE_FEINT(null, 12, 12, 20, 12),
	SICKLE_CRESCENT_FAN(null, 14, 14, 24, 14),
	SICKLE_CRESCENT_STAGGER(null, 12, 30, 40, 12, 18, 24, 30),
	SICKLE_CRESCENT_SKIM(null, 12, 20, 30, 12, 20),
	SICKLE_CRESCENT_RETURN(null, 12, 30, 40, 12, 30),
	SICKLE_ANCHOR_HOOK(null, 12, 20, 30, 12, 20),
	SICKLE_HARVEST(null, 14, 70, 84, 14, 22, 30, 38, 46, 54, 62, 70),
	// Inherited from the puppets the player failed to kill in phase one (VesperBorrowedAttacks).
	BORROWED_HULK_YANK(null, 14, 24, 36, 14, 24),
	BORROWED_MARROW_VOLLEY(null, 14, 22, 32, 14, 18, 22),
	BORROWED_VULTURE_DIVE(null, 18, 18, 30, 18),
	BORROWED_MUMMER_DECOYS(null, 12, 40, 52, 12, 40),
	BORROWED_HOUND_CURS(null, 14, 14, 26, 14),
	BORROWED_BELLOWS_BREATH(null, 14, 30, 40, 14, 18, 22, 26, 30),
	BORROWED_MNEMONIC_LASH(null, 16, 16, 28, 16);

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

	/** Where this move sits in a string, and the poses it starts and ends in (see {@link VesperChainGraph}). */
	public VesperChainGraph.Role role() { return VesperChainGraph.role(this); }
	public VesperChainGraph.Pose entry() { return VesperChainGraph.entry(this); }
	public VesperChainGraph.Pose exit() { return VesperChainGraph.exit(this); }
}
