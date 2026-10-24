package com.vincenthuto.hemomancy.common.manipulation.congeatio;

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
 * Cruor Javelin — Congeatio Mediocritas. Blood arrested into a shaft of dark cruor is hurled at one aimed body,
 * driving two Rime levels into it, or three if it is wet with water or rain. A Ferric follow-up can then shatter it.
 */
public class CruorJavelinManip extends BloodManipulation {
	public static final double RANGE = 16.0D;
	private static final double AIM_DOT = 0.96D;
	public static final float BASE_DAMAGE = 4.0F;
	private static final int RIME_TICKS = 120;
	public static final int RIME_LEVELS = 2;
	/** Water already in or on the body freezes along with the shaft. */
	public static final int WET_RIME_BONUS = 1;

	public CruorJavelinManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItem, float ticks) {
		if (target(player, player.level()) == null) {
			player.displayClientMessage(Component.translatable("message.hemomancy.cruor_javelin_manip.no_body_in_line_for_the"), true);
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
			ManipulationVisuals.burst(level, ManipulationVisuals.Form.CRUOR_JAVELIN,
					player.getEyePosition().add(player.getLookAngle().scale(0.4)).subtract(0, 0.4, 0),
					target.position().add(0, target.getBbHeight() * 0.55, 0), .5, CruorJavelinRules.VISUAL_TICKS, 1);
			float damage = (float) (BASE_DAMAGE * SkillPointHelper.getCrimsonMasteryMultiplier(player));
			int rime = rimeLevels(target.isInWaterOrRain());
			if (ManipulationCombatHelper.hurt(this, player, target, level, damage, RIME_TICKS, rime))
				ManipulationVisuals.attached(target, ManipulationVisuals.Form.FROZEN_VEINS, target.getBbWidth(), RIME_TICKS, 1);
			level.playSound(null, target.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8F, 1.4F);
			level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.7F, 0.7F);
		}
	}

	public static int rimeLevels(boolean wet) {
		return RIME_LEVELS + (wet ? WET_RIME_BONUS : 0);
	}

	private static LivingEntity target(Player player, Level world) {
		return ManipulationCombatHelper.aimedTarget(player, world,
				RANGE * SkillPointHelper.getSanguineReachMultiplier(player), AIM_DOT);
	}
}
