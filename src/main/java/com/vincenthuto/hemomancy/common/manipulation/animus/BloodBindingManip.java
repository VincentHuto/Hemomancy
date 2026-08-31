package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;

public class BloodBindingManip extends BloodManipulation {
	public static final double RANGE = 8.0D;
	public static final int DURATION_TICKS = 120;
	public static final int BOSS_DURATION_TICKS = DURATION_TICKS / 2;
	private static final double AIM_DOT = 0.975D;

	public BloodBindingManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		LivingEntity target = target(player, world);
		if (target == null) return;
		MobEffectInstance binding = bindingFor(target);
		boolean applied;
		if (ManipulationReactiveEvents.isBoss(target)) {
			target.forceAddEffect(binding, player);
			applied = target.getEffect(EffectInit.blood_binding) == binding;
		} else {
			applied = target.addEffect(binding, player);
		}
		if (applied) {
			world.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL,
					SoundSource.PLAYERS, 0.7F, 0.8F);
			PacketHandler.sendBloodBindingTendril(player, target, binding.getDuration(), world.random.nextLong());
		}
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItemMainhand, float chargeTicks) {
		if (target(player, player.level()) != null) return true;
		player.displayClientMessage(Component.literal("No unbound vessel in sight."), true);
		return false;
	}

	private static LivingEntity target(Player player, Level world) {
		LivingEntity target = ManipulationCombatHelper.aimedTarget(player, world, RANGE, AIM_DOT);
		if (target == null || target.isSpectator()) return null;
		if (target instanceof Player targetPlayer
				&& (targetPlayer.isCreative() || !player.canHarmPlayer(targetPlayer))) return null;
		MobEffectInstance binding = bindingFor(target);
		MobEffectInstance current = target.getEffect(EffectInit.blood_binding);
		if (current != null && current.getDuration() >= binding.getDuration()) return null;
		return CommonHooks.canMobEffectBeApplied(target, binding) ? target : null;
	}

	private static MobEffectInstance bindingFor(LivingEntity target) {
		int duration = ManipulationReactiveEvents.isBoss(target) ? BOSS_DURATION_TICKS : DURATION_TICKS;
		return new MobEffectInstance(EffectInit.blood_binding, duration, 0, false, false, true);
	}
}
