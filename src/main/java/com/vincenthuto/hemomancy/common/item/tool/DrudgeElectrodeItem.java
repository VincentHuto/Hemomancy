package com.vincenthuto.hemomancy.common.item.tool;

import java.util.List;
import java.util.UUID;

import com.vincenthuto.hemomancy.common.entity.npc.DrudgeEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class DrudgeElectrodeItem extends Item {

	public static String TAG_MODE = "mode";
	/** Radius (blocks) within which the electrode's "fire now" signal is broadcast. */
	private static final double SIGNAL_RADIUS = 16.0;

	public DrudgeElectrodeItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(Component.literal("Used to give and reflect commands to drudges"));
		if (stack.has(DataComponents.CUSTOM_DATA)) {
			if (stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(TAG_MODE)) {
				tooltip.add(Component.literal("State: On").withStyle(ChatFormatting.RED));
			} else {
				tooltip.add(Component.literal("State: Off").withStyle(ChatFormatting.GRAY));
			}
		}
		tooltip.add(Component.literal("§8Right-click: toggle mode  §8| Sneak+right-click a Drudge: dissolve").withStyle(ChatFormatting.DARK_GRAY));
		super.appendHoverText(stack, context, tooltip, flagIn);
	}

	/**
	 * When the electrode is in ON mode and used to attack, it broadcasts a
	 * one-shot "execute now" signal to all nearby Drudges owned by the attacker.
	 * In OFF mode it does nothing special.
	 */
	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (!(attacker instanceof Player player)) return true;

		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		boolean modeOn = tag.getBoolean(TAG_MODE);
		if (!modeOn) return true;

		if (!player.level().isClientSide) {
			UUID ownerUUID = player.getUUID();
			AABB signalBox = player.getBoundingBox().inflate(SIGNAL_RADIUS);
			List<DrudgeEntity> nearby = player.level().getEntitiesOfClass(DrudgeEntity.class, signalBox,
					d -> ownerUUID.equals(d.getOwnerUUID()) && !d.isRogue());
			int signalled = 0;
			for (DrudgeEntity drudge : nearby) {
				if (drudge.getEquippedMemory() != null) {
					drudge.queueElectrodeSignal();
					signalled++;
				}
			}
			if (signalled > 0) {
				player.displayClientMessage(Component.literal("§7Signal sent to " + signalled + " construct(s)."), true);
			}
		}
		return true;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getMainHandItem();
		if (stack.getItem() instanceof DrudgeElectrodeItem) {
			CompoundTag compound = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			if (!compound.getBoolean(TAG_MODE)) {
				playerIn.playSound(SoundEvents.BEACON_ACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_MODE, !compound.getBoolean(TAG_MODE));
			} else {
				playerIn.playSound(SoundEvents.BEACON_DEACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_MODE, !compound.getBoolean(TAG_MODE));
			}
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));
		}
		return super.use(worldIn, playerIn, handIn);
	}
}

