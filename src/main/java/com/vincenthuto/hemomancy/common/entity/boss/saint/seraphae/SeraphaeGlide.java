package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCombatController;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintLocomotion;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMotor;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A chained glide (design section 8): she does not walk; she hovers over the floor and glides toward the most hurt
 * challenger as far as her chain allows, strains at its end, and is reeled in when it shortens. The numbers are
 * {@link SeraphaeLeashRules}; the motion is {@link SeraphaeEntity}'s, which owns the leash.
 */
public final class SeraphaeGlide implements SaintLocomotion {
	public static final float TURN_ROAMING = 6.0F;
	public static final float TURN_TRACKING = 10.0F;

	@Override
	public void roam(SaintTrialEntity saint, SaintMotor motor, @Nullable LivingEntity target) {
		if (saint instanceof SeraphaeEntity seraphae) seraphae.glideRoam(motor, target);
	}

	@Override
	public void inMove(SaintTrialEntity saint, SaintMotor motor, SaintMoveContext ctx) {
		if (saint instanceof SeraphaeEntity seraphae) seraphae.glideInMove(motor, ctx);
	}

	@Override
	public boolean mayPick(SaintTrialEntity saint) {
		return !(saint instanceof SeraphaeEntity seraphae) || seraphae.mayPick();
	}

	@Override
	@Nullable
	public LivingEntity chooseTarget(SaintTrialEntity saint, List<Player> challengers, @Nullable LivingEntity current) {
		if (saint instanceof SeraphaeEntity seraphae) return seraphae.chooseHealTarget(challengers);
		return SaintCombatController.defaultTarget(saint, challengers);
	}

	/** 6 degrees a tick roaming, 10 while a wind-up tracks, none once a move has committed. */
	@Override
	public float turnRate(SaintTrialEntity saint, boolean trackingWindup) {
		if (trackingWindup) return TURN_TRACKING;
		SaintCombatController controller = saint.controller();
		if (controller.move() != null && controller.stage() != BossMoveTimeline.Stage.WINDUP) return 0.0F;
		return TURN_ROAMING;
	}

	@Override
	public int decisionDelay(SaintTrialEntity saint) {
		return SeraphaeSelectionRules.decisionDelay(saint.phases().phase(), saint.deeds().mask());
	}
}
