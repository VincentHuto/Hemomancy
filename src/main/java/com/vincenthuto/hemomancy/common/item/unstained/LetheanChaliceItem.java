package com.vincenthuto.hemomancy.common.item.unstained;

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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.stream.Collectors;

public class LetheanChaliceItem extends Item {
	private static final int COOLDOWN_TICKS = 400;

	public LetheanChaliceItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.literal("A chalice for still water, meant to rinse away what the blood remembers.")
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		tooltip.add(Component.literal("Use: clears one harmful effect and steadies the bearer.")
				.withStyle(ChatFormatting.WHITE));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide) {
			boolean onPath = HemoCapabilityAccess.getUnstainedProgress(player)
					.map(progress -> progress.hasBegunPurification())
					.orElse(false);
			if (!onPath) {
				player.displayClientMessage(Component.literal("The chalice remains cold and empty.")
						.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
				return InteractionResultHolder.fail(stack);
			}

			List<MobEffectInstance> harmful = player.getActiveEffects().stream()
					.filter(effect -> !effect.getEffect().value().isBeneficial())
					.collect(Collectors.toList());
			if (!harmful.isEmpty()) {
				player.removeEffect(harmful.get(0).getEffect());
			}

			player.clearFire();
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0, false, true, true));
			if (HemoCapabilityAccess.getUnstainedProgress(player)
					.map(progress -> progress.hasClarityUnlocked())
					.orElse(false)) {
				player.addEffect(new MobEffectInstance(EffectInit.verdigris_aura, 300, 0, false, true, true));
			}

			level.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 0.8f, 1.4f);
			level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7f, 1.2f);
			if (level instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ParticleTypes.SPLASH, player.getX(), player.getY() + 1.0, player.getZ(),
						24, 0.4, 0.6, 0.4, 0.02);
			}
			player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
