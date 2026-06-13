package com.vincenthuto.hemomancy.common.item.shared;

import com.vincenthuto.hemomancy.common.effect.ConstrictorCordRules;
import com.vincenthuto.hemomancy.common.entity.projectile.ConstrictorCordEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ConstrictorCordItem extends Item {
	public ConstrictorCordItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WOOL_PLACE,
				SoundSource.PLAYERS, 0.6F, 0.8F);
		if (!level.isClientSide) {
			ConstrictorCordEntity projectile = new ConstrictorCordEntity(level, player);
			projectile.setItem(stack);
			projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
					(float) ConstrictorCordRules.PROJECTILE_VELOCITY,
					(float) ConstrictorCordRules.PROJECTILE_INACCURACY);
			level.addFreshEntity(projectile);
		}
		player.awardStat(Stats.ITEM_USED.get(this));
		player.getCooldowns().addCooldown(this, ConstrictorCordRules.USE_COOLDOWN_TICKS);
		if (!player.getAbilities().instabuild) {
			stack.shrink(1);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}
}
