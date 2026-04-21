package com.vincenthuto.hemomancy.common.item.tool.living;

import java.util.List;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.item.BloodVialItem;
import com.vincenthuto.hemomancy.common.item.VialRackItem;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class LivingSyringeItem extends LivingItemItem {
	public static final String TAG_STATE = "state";
	public static final String TAG_LOADED_RACK = "loaded_rack";

	public LivingSyringeItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		if (hasLoadedRack(stack)) {
			ItemStack rack = getLoadedRack(stack);
			tooltip.add(Component.translatable("item.hemomancy.living_syringe.loaded",
					VialRackItem.countEmptyVials(rack), VialRackItem.MAX_VIALS));
		}
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return hasLoadedRack(stack);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return 0xB80F2A;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		if (!hasLoadedRack(stack)) {
			return 0;
		}
		int emptyCount = VialRackItem.countEmptyVials(getLoadedRack(stack));
		return Math.round(13.0F * (emptyCount / (float) VialRackItem.MAX_VIALS));
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		stack.getOrCreateTag().putBoolean(TAG_STATE, hasLoadedRack(stack));
		if (hasLoadedRack(stack)) {
			ItemStack rack = getLoadedRack(stack);
			VialRackItem.ensureInitialized(rack);
			setLoadedRack(stack, rack);
		}
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
		if (player.isShiftKeyDown()) {
			if (!player.level().isClientSide) {
				ejectRack(player.level(), player, stack);
			}
			return InteractionResult.sidedSuccess(player.level().isClientSide);
		}
		if (!player.level().isClientSide) {
			return fillVialFromTarget(player, target, stack);
		}
		return InteractionResult.sidedSuccess(true);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack syringe = playerIn.getItemInHand(handIn);
		if (!worldIn.isClientSide) {
			if (playerIn.isShiftKeyDown()) {
				ejectRack(playerIn, syringe);
			} else if (!hasLoadedRack(syringe)) {
				if (!loadRackFromInventory(playerIn, syringe)) {
					playerIn.displayClientMessage(Component.translatable("item.hemomancy.living_syringe.no_rack"), true);
				}
			}
		}
		return InteractionResultHolder.sidedSuccess(syringe, worldIn.isClientSide);
	}

	private InteractionResult fillVialFromTarget(Player player, LivingEntity target, ItemStack syringe) {
		if (!hasLoadedRack(syringe) && !loadRackFromInventory(player, syringe)) {
			player.displayClientMessage(Component.translatable("item.hemomancy.living_syringe.no_rack"), true);
			return InteractionResult.FAIL;
		}
		ItemStack rack = getLoadedRack(syringe);
		int emptySlot = VialRackItem.findFirstEmptyVialSlot(rack);
		if (emptySlot < 0) {
			ejectRack(player, syringe);
			player.displayClientMessage(Component.translatable("item.hemomancy.living_syringe.rack_full"), true);
			return InteractionResult.FAIL;
		}
		NonNullList<ItemStack> vials = VialRackItem.getVials(rack);
		ItemStack sampledVial = new ItemStack(ItemInit.bloody_vial.get());
		sampledVial.getOrCreateTag().putString(BloodVialItem.TAG_ENTITY_TYPE,
				ForgeRegistries.ENTITY_TYPES.getKey(target.getType()).toString());
		sampledVial.getOrCreateTag().putBoolean(BloodVialItem.TAG_STATE, true);
		vials.set(emptySlot, sampledVial);
		VialRackItem.setVials(rack, vials);
		setLoadedRack(syringe, rack);
		player.playSound(SoundEvents.BOTTLE_FILL, 1.0F, 1.0F);
		if (VialRackItem.countEmptyVials(rack) <= 0) {
			ejectRack(player, syringe);
		}
		return InteractionResult.SUCCESS;
	}

	private boolean loadRackFromInventory(Player player, ItemStack syringe) {
		if (hasLoadedRack(syringe)) {
			return false;
		}
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack candidate = player.getInventory().getItem(i);
			if (candidate.getItem() instanceof VialRackItem && VialRackItem.hasLoadableCapacity(candidate)) {
				ItemStack loadedRack = candidate.copy();
				loadedRack.setCount(1);
				setLoadedRack(syringe, loadedRack);
				player.getInventory().setItem(i, ItemStack.EMPTY);
				return true;
			}
		}
		return false;
	}

	private boolean ejectRack(Player player, ItemStack syringe) {
		if (!hasLoadedRack(syringe)) {
			return false;
		}
		ItemStack rack = getLoadedRack(syringe);
		clearLoadedRack(syringe);
		if (!player.getInventory().add(rack)) {
			player.drop(rack, false);
		}
		player.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 0.8F, 1.0F);
		return true;
	}

	private boolean hasLoadedRack(ItemStack syringe) {
		CompoundTag tag = syringe.getTag();
		return tag != null && tag.contains(TAG_LOADED_RACK, Tag.TAG_COMPOUND);
	}

	private ItemStack getLoadedRack(ItemStack syringe) {
		if (!hasLoadedRack(syringe)) {
			return ItemStack.EMPTY;
		}
		return ItemStack.of(syringe.getOrCreateTag().getCompound(TAG_LOADED_RACK));
	}

	private void setLoadedRack(ItemStack syringe, ItemStack rack) {
		syringe.getOrCreateTag().put(TAG_LOADED_RACK, rack.save(new CompoundTag()));
		syringe.getOrCreateTag().putBoolean(TAG_STATE, true);
	}

	private void clearLoadedRack(ItemStack syringe) {
		syringe.getOrCreateTag().remove(TAG_LOADED_RACK);
		syringe.getOrCreateTag().putBoolean(TAG_STATE, false);
	}
}
