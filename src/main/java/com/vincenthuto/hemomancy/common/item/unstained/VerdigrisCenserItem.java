package com.vincenthuto.hemomancy.common.item.unstained;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class VerdigrisCenserItem extends Item {
	private static final int COOLDOWN_TICKS = 500;
	private static final double RADIUS = 10.0D;

	public VerdigrisCenserItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.literal("Oxidized copper smoke, carried in a pale silver cage.")
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		tooltip.add(Component.literal("Use: grants Verdigris Aura and reveals blood-active bodies nearby.")
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
				player.displayClientMessage(Component.literal("The censer smoke curls away from you.")
						.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
				return InteractionResultHolder.fail(stack);
			}

			player.addEffect(new MobEffectInstance(EffectInit.verdigris_aura, 600, 0, false, true, true));
			List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class,
					player.getBoundingBox().inflate(RADIUS), LivingEntity::isAlive);
			int marked = 0;
			for (LivingEntity entity : nearby) {
				if (entity == player) continue;
				boolean bloodActive = entity instanceof ServerPlayer other
						&& HemoCapabilityAccess.getBloodVolume(other).map(volume -> volume.isActive()).orElse(false);
				if (bloodActive || entity instanceof Monster) {
					entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, true, true));
					entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, true, true));
					marked++;
				}
			}

			player.displayClientMessage(Component.literal("Verdigris smoke marks " + marked + " suspect bodies.")
					.withStyle(ChatFormatting.AQUA), true);
			level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.9f, 1.6f);
			if (level instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ParticleTypes.SCRAPE, player.getX(), player.getY() + 0.8, player.getZ(),
						48, 1.8, 0.8, 1.8, 0.03);
				serverLevel.sendParticles(ParticleTypes.WAX_OFF, player.getX(), player.getY() + 0.8, player.getZ(),
						24, 1.2, 0.6, 1.2, 0.02);
			}
			player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
