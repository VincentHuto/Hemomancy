package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWeaponAction;

import java.util.EnumSet;
import java.util.Set;

import static com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWeaponAction.*;

/**
 * Which Evening Star actions play an authored Blockbench clip and which are keyed in Java by
 * {@link VesperEveningStarMoves}. Every move is keyed in Java now; the old clips only serve the hit, hood and defeat.
 */
public final class VesperEveningStarMotion {
	public static final Set<VesperWeaponAction> AUTHORED = EnumSet.noneOf(VesperWeaponAction.class);
	public static final Set<VesperWeaponAction> PROCEDURAL = EnumSet.of(SKY_LANCE, SICKLE_CYCLONE, SICKLE_POUNCE,
			SICKLE_CROSS_REND, SICKLE_HOOK, TWIN_REND, PREDATOR_POUNCE, SANGUINE_CRESCENTS, ICHIMONJI, CROSSCUT, LEAPING_CLEAVE,
			REAPER_SWEEP, LANCE_FLURRY, BRANDING_THRUSTS, UPDRAFT_IMPALEMENT, FLAMMEUS_CONCENTRATION,
			CONDUCTIVE_VOLLEY, STORM_LOCK, CHAIN_SWEEP, HOOK_AND_CRUSH, MAGNETIC_AXIS, IRON_RETORT,
			BLADE_RISING_CUT, BLADE_DRAWING_STEP, AXE_HAFT_SHOVE, AXE_GRAVE_SPLITTER, SPEAR_VAULT_KICK,
			SPEAR_PINNING_THRUST, CLAW_SHADOW_FEINT, CLAW_EVISCERATE, CROSSBOW_SNAP_SHOT, CROSSBOW_BOLT_RAIN,
			TORCH_CINDER_SWEEP, FLAIL_ORBIT, FLAIL_SHATTER_SLAM, STAFF_LODESTONE_SWEEP, STAFF_PILLAR_VAULT,
			SICKLE_REAP_L, SICKLE_REAP_R, SICKLE_HOOK_REEL, SICKLE_REEL_CYCLONE, SICKLE_REAPERS_RISE,
			SICKLE_PASS_THROUGH, SICKLE_GALLOWS_WHEEL, SICKLE_TRAVELLING_CYCLONE, SICKLE_REVERSE_CYCLONE,
			SICKLE_FEINT_CYCLONE, SICKLE_POUNCE_FEINT, SICKLE_CRESCENT_FAN, SICKLE_CRESCENT_STAGGER,
			SICKLE_CRESCENT_SKIM, SICKLE_CRESCENT_RETURN, SICKLE_ANCHOR_HOOK, SICKLE_HARVEST,
			BORROWED_HULK_YANK, BORROWED_MARROW_VOLLEY, BORROWED_VULTURE_DIVE, BORROWED_MUMMER_DECOYS,
			BORROWED_HOUND_CURS, BORROWED_BELLOWS_BREATH, BORROWED_MNEMONIC_LASH);

	private VesperEveningStarMotion() {
	}

	public static boolean supports(VesperWeaponAction action) {
		return AUTHORED.contains(action) || PROCEDURAL.contains(action);
	}
}
