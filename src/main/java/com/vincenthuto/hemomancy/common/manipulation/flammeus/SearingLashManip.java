package com.vincenthuto.hemomancy.common.manipulation.flammeus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Searing Lash — Flammeus Mediocritas. A whip of boiling blood cracks against one aimed body, scalding it
 * and laying a long Searing. Vitric Combustion's detonation bonus scales with the Searing time left, so the
 * lash primes a full-strength payoff.
 */
public class SearingLashManip extends BloodManipulation {
	public static final double RANGE = 12.0D;
	private static final double AIM_DOT = 0.94D;
	public static final float BASE_DAMAGE = 4.0F;
	private static final int SEARING_TICKS = 120;
	private static final int FIRE_SECONDS = 3;

	public SearingLashManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItem, float ticks) {
		if (target(player, player.level()) == null) {
			player.displayClientMessage(Component.literal("Nothing within the lash's reach."), true);
			return false;
		}
		return super.canPerformAction(player, heldItem, ticks);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {
			if (!(world instanceof ServerLevel level)) return;
			LivingEntity target = target(player, level);
			if (target == null) return;
			float damage = (float) (BASE_DAMAGE * SkillPointHelper.getCrimsonMasteryMultiplier(player));
			ManipulationVisuals.burst(level, ManipulationVisuals.Form.SEARING_LASH, hand(player), target.getEyePosition(), 1,
					SearingLashRules.VISUAL_TICKS, SearingLashRules.encode(player.getMainArm() == net.minecraft.world.entity.HumanoidArm.LEFT));
			if (ManipulationCombatHelper.hurt(this, player, target, level, damage, SEARING_TICKS, 1)
					&& !target.fireImmune()) target.igniteForSeconds(FIRE_SECONDS);
			level.playSound(null, target.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8F, 1.2F);
			level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.6F, 1.3F);
		}
	}

	/** Roughly where the whip leaves the casting hand. */
	static net.minecraft.world.phys.Vec3 hand(Player player) {
		return player.getEyePosition().add(player.getLookAngle().scale(0.4)).subtract(0, 0.45, 0);
	}

	private static LivingEntity target(Player player, Level world) {
		double range = RANGE * SkillPointHelper.getSanguineReachMultiplier(player);
		return ManipulationCombatHelper.aimedTarget(player, world, range, AIM_DOT);
	}
}
