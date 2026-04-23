package com.vincenthuto.hemomancy.common.item.armor;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.List;

import javax.annotation.Nullable;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

/**
 * Unstained Shield — a smooth, round shield carried by followers of the
 * Unstained path. Has no spikes or barbs (unlike the BarbedShieldItem) so
 * it never draws blood. While blocking, it passively grants a small purity
 * bonus over time and provides knockback resistance.
 */
public class UnstainedShieldItem extends Item {

	public UnstainedShieldItem(Item.Properties builder) {
		super(builder.durability(1280));
		DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BLOCK;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 72000;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack itemstack = playerIn.getItemInHand(handIn);
		playerIn.startUsingItem(handIn);
		return InteractionResultHolder.consume(itemstack);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		if (!worldIn.isClientSide && entityIn instanceof Player player) {
			if (player.getOffhandItem() == stack || player.getMainHandItem() == stack) {
				if (player.isBlocking()) {
					// Every 40 ticks (2 seconds) while blocking, grant a tiny purity bonus
					if (player.tickCount % 40 == 0) {
						HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(progress -> {
							if (progress.hasBegunPurification() && !progress.isPurified()) {
								progress.addPurity(0.1f);
							}
						});
					}
				}
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
			TooltipFlag flagIn) {
		tooltip.add(Component.translatable("item.hemomancy.unstained_shield.tooltip")
				.withStyle(ChatFormatting.GRAY));
	}

	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
		return repair.getItem() == com.vincenthuto.hemomancy.common.init.ItemInit.cleansed_blood_crystal_shard.get()
				|| super.isValidRepairItem(toRepair, repair);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return false;
	}
}
