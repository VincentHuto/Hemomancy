package com.vincenthuto.hemomancy.common.manipulation.mortem;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.MortemStatusVisuals;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Fruiting Contagion — Mortem Summa. Necrosis already rotting in nearby bodies fruits at once: each carrier is
 * wracked and withered, and the pale contamination seeds Necrosis into every other enemy standing close to it.
 * It turns one wound into a field for Funeral Bell and Carrion Communion.
 */
public class FruitingContagionManip extends BloodManipulation {
	public static final double RADIUS = 10.0D;
	public static final double SPREAD_RADIUS = 4.0D;
	public static final float CARRIER_DAMAGE = 3.0F;
	private static final int WITHER_TICKS = 80;
	private static final int NECROSIS_TICKS = 160;

	public FruitingContagionManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItem, float ticks) {
		if (carriers(player, player.level()).isEmpty()) {
			player.displayClientMessage(Component.translatable("message.hemomancy.fruiting_contagion_manip.no_rot_has_taken_root_nearby"), true);
			return false;
		}
		return super.canPerformAction(player, heldItem, ticks);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {
			if (!(world instanceof ServerLevel level)) return;
			List<LivingEntity> carriers = carriers(player, level);
			float damage = (float) (CARRIER_DAMAGE * SkillPointHelper.getCrimsonMasteryMultiplier(player));
			for (LivingEntity carrier : carriers) {
				for (LivingEntity near : ManipulationCombatHelper.hostileTargets(player, level, RADIUS + SPREAD_RADIUS)) {
					if (near == carrier || near.distanceTo(carrier) > SPREAD_RADIUS
							|| SchoolStates.has(near, SchoolState.NECROSIS)) continue;
					if (SchoolStates.apply(player, near, SchoolState.NECROSIS, NECROSIS_TICKS)) {
						MortemStatusVisuals.infect(near, NECROSIS_TICKS);
						ManipulationVisuals.burst(level, ManipulationVisuals.Form.CONTAGION, carrier.position(), near.position(),
								carrier.getBbHeight(), FruitingContagionRules.VISUAL_TICKS, FruitingContagionRules.SPREADING);
					}
				}
				ManipulationVisuals.burst(level, ManipulationVisuals.Form.CONTAGION, carrier.position(), carrier.position(),
						carrier.getBbHeight(), FruitingContagionRules.VISUAL_TICKS, FruitingContagionRules.FRUITING);
				if (ManipulationCombatHelper.hurt(this, player, carrier, level, damage))
					carrier.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHER_TICKS, 0, false, true));
			}
			level.playSound(null, player.blockPosition(), SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.PLAYERS, 0.9F, 0.6F);
		}
	}

	private static List<LivingEntity> carriers(Player player, Level world) {
		return ManipulationCombatHelper.hostileTargets(player, world, RADIUS).stream()
				.filter(target -> target.distanceTo(player) <= RADIUS)
				.filter(target -> SchoolStates.has(target, SchoolState.NECROSIS))
				.toList();
	}
}
