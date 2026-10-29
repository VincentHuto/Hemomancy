package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MonolithFragmentItem extends Item {

	public MonolithFragmentItem(Properties properties) {
		super(properties);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (level.isClientSide || !(entity instanceof Player player)) {
			return;
		}
		if (level.getGameTime() % MonolithFragmentBurdenRules.BURDEN_CHECK_INTERVAL_TICKS != 0) {
			return;
		}
		if (MonolithFragmentBurdenRules.shouldBurdenCarrier(HemoCapabilityAccess.getInitiatoryDegree(player).getArchonPath())) {
			applyApotheosBurden(player);
		}
	}

	private static void applyApotheosBurden(Player player) {
		player.addEffect(new MobEffectInstance(MobEffects.DARKNESS,
				MonolithFragmentBurdenRules.BURDEN_EFFECT_TICKS, 0, false, true, true));
		player.addEffect(new MobEffectInstance(MobEffects.CONFUSION,
				MonolithFragmentBurdenRules.BURDEN_EFFECT_TICKS, 0, false, true, true));
	}

}
