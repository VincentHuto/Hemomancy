package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.IVascularSystem;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

public class VascularPoulticeItem extends Item {
	private final float healAmount;

	public VascularPoulticeItem(Properties properties) {
		this(properties, VascularPoulticeRules.DEFAULT_HEAL_AMOUNT);
	}

	public VascularPoulticeItem(Properties properties, float healAmount) {
		super(properties.stacksTo(16));
		this.healAmount = healAmount;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.literal("Repairs the most strained vein section").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("Restores +" + (int) healAmount + " vascular health").withStyle(ChatFormatting.DARK_RED));
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 24;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		boolean bloodActive = HemoCapabilityAccess.getBloodVolume(player)
				.map(volume -> volume.isActive())
				.orElse(false);
		if (!bloodActive || player.getCooldowns().isOnCooldown(this) || !hasDamagedSection(player)) {
			return InteractionResultHolder.fail(stack);
		}
		player.startUsingItem(hand);
		return InteractionResultHolder.consume(stack);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (!(entity instanceof ServerPlayer player)) {
			return stack;
		}
		HemoCapabilityAccess.getVascularSystem(player).ifPresent(vascular -> {
			VascularPoulticeRules.healMostDamaged(vascular.getVascularSystem(), healAmount).ifPresent(result -> {
				vascular.setVascularSystem(vascular.getVascularSystem());
				VascularSystemEvents.syncVascular(player, vascular);
				player.displayClientMessage(Component.literal("Poultice sealed " + result.section().name().toLowerCase()
						+ ": +" + (int) result.amountHealed()).withStyle(ChatFormatting.DARK_RED), true);
				level.playSound(null, player.getX(), player.getY(), player.getZ(),
						SoundEvents.HONEY_DRINK, SoundSource.PLAYERS, 0.7F, 0.85F);
				if (!player.getAbilities().instabuild) {
					stack.shrink(1);
				}
				player.getCooldowns().addCooldown(this, VascularPoulticeRules.COOLDOWN_TICKS);
			});
		});
		return stack;
	}

	private boolean hasDamagedSection(Player player) {
		return HemoCapabilityAccess.getVascularSystem(player)
				.map(IVascularSystem::getVascularSystem)
				.flatMap(VascularPoulticeRules::mostDamagedSection)
				.isPresent();
	}
}
