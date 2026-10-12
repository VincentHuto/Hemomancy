package com.vincenthuto.hemomancy.common.manipulation.tenebris;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Eclipse Well — Tenebris Perfectus. A charged pit of dark drags enemies inward and keeps them Obscured; every
 * second it bites the Obscured bodies it holds. A well charged to the full collapses when it lapses, flinging
 * whatever stands at its heart elsewhere, as a chorus fruit would.
 */
public class EclipseWellManip extends BloodManipulation {
	private static final int CHARGE_TICKS = 80;
	/** Ticks between the well's damage pulses. */
	public static final int PULSE_TICKS = 20;
	/** Damage each pulse deals to an Obscured body inside the well, before Crimson Mastery. */
	public static final float PULSE_DAMAGE = 1.5F;
	/** On a full-charge collapse, enemies within this distance of the centre are scattered. */
	public static final double COLLAPSE_RADIUS = 2.0D;
	/** The scatter's reach along each axis, matching a chorus fruit. */
	public static final double SCATTER_RANGE = 8.0D;
	private static final int SCATTER_ATTEMPTS = 16;

	public EclipseWellManip(String name, double cost, double alignment, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignment, xpCost, type, rank, tendency, section);
	}

	@Override public int getRequiredChargeTicks() { return CHARGE_TICKS; }

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, getRequiredChargeTicks() <= 0 ? 1 : heldTicks / getRequiredChargeTicks())) {

		if (!(world instanceof ServerLevel level)) return;
		double radius = ManipulationScalingRules.scaled(2, 7, heldTicks, CHARGE_TICKS);
		int duration = ManipulationScalingRules.scaledInt(40, 200, heldTicks, CHARGE_TICKS);
		float pulseDamage = PULSE_DAMAGE * (float) SkillPointHelper.getCrimsonMasteryMultiplier(player);
		boolean collapses = collapses(heldTicks);
		Vec3 center = ManipulationCombatHelper.clipToGeometry(player, player.getEyePosition().add(player.getLookAngle().scale(12)));
        HemomancyTendrilEffects.umbraRelease(player, center);
		ManipulationReactiveEvents.createEclipseWell(level, center, radius, duration, player.getUUID(), pulseDamage, collapses);
	        }
    }

	/** Only a well charged to the full collapses on its heart when it lapses. */
	public static boolean collapses(float heldTicks) {
		return ManipulationCastingRules.chargeFraction(heldTicks, CHARGE_TICKS) >= 1.0F;
	}

	/**
	 * Chorus-fruit scatter: up to sixteen tries at a random safe spot within {@link #SCATTER_RANGE} on each axis.
	 * Honours the NeoForge chorus teleport event, so protection mods can refuse it.
	 */
	public static boolean scatter(ServerLevel level, LivingEntity target) {
		Vec3 from = target.position();
		for (int attempt = 0; attempt < SCATTER_ATTEMPTS; attempt++) {
			double x = target.getX() + (target.getRandom().nextDouble() - 0.5D) * 2 * SCATTER_RANGE;
			double y = Mth.clamp(target.getY() + (target.getRandom().nextInt((int) (2 * SCATTER_RANGE)) - SCATTER_RANGE),
					level.getMinBuildHeight(), level.getMinBuildHeight() + level.getLogicalHeight() - 1);
			double z = target.getZ() + (target.getRandom().nextDouble() - 0.5D) * 2 * SCATTER_RANGE;
			if (target.isPassenger()) target.stopRiding();
			var event = EventHooks.onChorusFruitTeleport(target, x, y, z);
			if (event.isCanceled()) return false;
			// No vanilla portal-particle broadcast: the umbral tear forms carry the departure and arrival.
			if (target.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), false)) {
				level.gameEvent(GameEvent.TELEPORT, from, GameEvent.Context.of(target));
				target.resetFallDistance();
				ManipulationVisuals.burst(level, ManipulationVisuals.Form.TELEPORT, from, from, 1, 18);
				ManipulationVisuals.burst(level, ManipulationVisuals.Form.UMBRA_ARRIVAL, target.position(), target.position(), 1, 22);
				level.playSound(null, from.x, from.y, from.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.8F);
				level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.CHORUS_FRUIT_TELEPORT,
						SoundSource.PLAYERS, 1.0F, 0.8F);
				return true;
			}
		}
		return false;
	}
}
