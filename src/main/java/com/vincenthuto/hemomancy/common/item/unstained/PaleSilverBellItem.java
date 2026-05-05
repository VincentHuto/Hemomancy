package com.vincenthuto.hemomancy.common.item.unstained;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class PaleSilverBellItem extends Item {
	private static final int COOLDOWN_TICKS = 300;
	private static final double RADIUS = 8.0D;

	public PaleSilverBellItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.literal("A handbell of pale silver, tuned to make blood-magic hesitate.")
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		tooltip.add(Component.literal("Use: Silver Ward and a weakening chime against nearby hostiles.")
				.withStyle(ChatFormatting.WHITE));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		level.playSound(null, player.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.4f, 1.65f);

		if (!level.isClientSide) {
			boolean onPath = HemoCapabilityAccess.getUnstainedProgress(player)
					.map(progress -> progress.hasBegunPurification())
					.orElse(false);
			if (!onPath) {
				player.displayClientMessage(Component.literal("The pale bell gives no answer to unstilled blood.")
						.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
				return InteractionResultHolder.fail(stack);
			}

			player.addEffect(new MobEffectInstance(EffectInit.silver_ward, 600, 0, false, true, true));
			List<Monster> monsters = level.getEntitiesOfClass(Monster.class,
					player.getBoundingBox().inflate(RADIUS), Monster::isAlive);
			for (Monster monster : monsters) {
				monster.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0, false, true, true));
				monster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 0, false, true, true));
			}

			if (level instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(),
						36, 1.4, 0.8, 1.4, 0.03);
			}
			player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
		}

		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
