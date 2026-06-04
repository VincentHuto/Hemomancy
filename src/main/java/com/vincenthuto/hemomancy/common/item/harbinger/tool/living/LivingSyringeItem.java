package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.client.particle.util.EntityParticleUtils;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

public class LivingSyringeItem extends LivingItem {
	public static final String TAG_STATE = "state";
	public static final String TAG_LOADED_RACK = "loaded_rack";

	public LivingSyringeItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
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
				ejectRack(player, stack);
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
		CompoundTag vialTag = sampledVial.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		vialTag.putString(BloodVialItem.TAG_ENTITY_TYPE,
				BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString());
		vialTag.putBoolean(BloodVialItem.TAG_STATE, true);
		sampledVial.set(DataComponents.CUSTOM_DATA, CustomData.of(vialTag));
		vials.set(emptySlot, sampledVial);
		VialRackItem.setVials(rack, vials);
		setLoadedRack(syringe, rack);
		player.playSound(SoundEvents.BOTTLE_FILL, 2.0F, 0.8F);
		if (player.level() instanceof ServerLevel serverLevel) {
			PacketHandler.sendLivingToolBreakParticles(target.position().add(0, target.getBbHeight() * 0.5, 0),
					EntityParticleUtils.getColorFromPredicate(EntityParticleUtils.getEntityPredicate(target)),
					16, serverLevel);
		}
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
				candidate.shrink(1);
				if (candidate.isEmpty()) {
					player.getInventory().setItem(i, ItemStack.EMPTY);
				}
				player.playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 2.0F, 0.7F);
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
		player.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 2.0F, 0.7F);
		return true;
	}

	private boolean hasLoadedRack(ItemStack syringe) {
		CustomData customData = syringe.get(DataComponents.CUSTOM_DATA);
		return customData != null && customData.copyTag().contains(TAG_LOADED_RACK, Tag.TAG_COMPOUND);
	}

	private ItemStack getLoadedRack(ItemStack syringe) {
		if (!hasLoadedRack(syringe)) {
			return ItemStack.EMPTY;
		}
		return ItemStack.parseOptional(RegistryAccess.EMPTY,
				syringe.get(DataComponents.CUSTOM_DATA).copyTag().getCompound(TAG_LOADED_RACK));
	}

	private void setLoadedRack(ItemStack syringe, ItemStack rack) {
		CompoundTag tag = syringe.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.put(TAG_LOADED_RACK, rack.save(RegistryAccess.EMPTY));
		tag.putBoolean(TAG_STATE, true);
		syringe.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	private void clearLoadedRack(ItemStack syringe) {
		CompoundTag tag = syringe.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.remove(TAG_LOADED_RACK);
		tag.putBoolean(TAG_STATE, false);
		syringe.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}
}
