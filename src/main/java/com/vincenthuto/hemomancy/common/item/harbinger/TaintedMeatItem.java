package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Food a Harbinger's blood has got into: meat off an animal they dosed (core-loop Track 5) and the harvest of a crop
 * grown on over-fed ground (round two, P7). To a Harbinger whose blood is awake it gives blood back (its row in the
 * {@code blood_food} table, paid by the one meal hook in {@code DailyLoopEvents}); anyone else is sickened by it.
 */
public class TaintedMeatItem extends Item {
	private final String tooltipKey;

	public TaintedMeatItem(Properties properties, String tooltipKey) {
		super(properties);
		this.tooltipKey = tooltipKey;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity eater) {
		if (!level.isClientSide && eater instanceof ServerPlayer player) {
			IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
			if (!volume.isActive() || ProgressionGates.isUnstained(player)) {
				player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
				player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 300, 1));
			}
		}
		return super.finishUsingItem(stack, level, eater);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable(tooltipKey).withStyle(ChatFormatting.DARK_RED));
	}
}
