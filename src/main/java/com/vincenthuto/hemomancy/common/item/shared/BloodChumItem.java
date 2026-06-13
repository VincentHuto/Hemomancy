package com.vincenthuto.hemomancy.common.item.shared;

import com.vincenthuto.hemomancy.common.effect.ChummedWatersRules;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodChumEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BloodChumItem extends Item {
	public BloodChumItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
			BloodChumEntity projectile = new BloodChumEntity(level, serverPlayer);
			projectile.setItem(stack);
			projectile.shootFromRotation(serverPlayer, serverPlayer.getXRot(), serverPlayer.getYRot(), 0.0F,
					(float) ChummedWatersRules.PROJECTILE_VELOCITY,
					(float) ChummedWatersRules.PROJECTILE_INACCURACY);
			level.addFreshEntity(projectile);
			serverPlayer.awardStat(Stats.ITEM_USED.get(this));
			level.playSound(null, serverPlayer.blockPosition(), SoundEvents.SLIME_BLOCK_PLACE,
					SoundSource.PLAYERS, 0.55F, 0.8F + level.random.nextFloat() * 0.25F);
			if (!serverPlayer.getAbilities().instabuild) {
				stack.shrink(1);
			}
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}
}
