package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.util.ClientBridge;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData;
import com.vincenthuto.hemomancy.common.particle.EntityParticleUtils;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSamplingResult;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSamplingRules;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.item.itemhandler.LivingSyringeItemHandler;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.PacketLivingSyringeDraw;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
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
	private static final int MODEL_LOAD_FILLED_VIAL = 2;

	public LivingSyringeItem(Properties properties) {
		super(properties);
	}

	/** Model override order: bare, empty vial, filled vial, then rack with zero to eight samples. */
	public static int modelLoad(ItemStack syringe, HolderLookup.Provider registries) {
		CompoundTag tag = syringe.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		ItemStack rack = ItemStack.parseOptional(registries, tag.getCompound(TAG_LOADED_RACK));
		if (rack.getItem() instanceof VialRackItem) {
			return 3 + VialRackItem.countFilledVials(rack, registries);
		}
		if (!tag.contains("Inventory", Tag.TAG_COMPOUND)) return 0;
		var inventory = new LivingSyringeItemHandler(syringe, 1);
		inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
		ItemStack vial = inventory.getStackInSlot(0);
		if (!BloodSampleData.isSpecimenVessel(vial)) return 0;
		return BloodSampleData.isFilled(vial) ? 2 : 1;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
	}

	@Override
	public int getEntityLifespan(ItemStack stack, Level level) {
		return 6000;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.translatable("item.hemomancy.living_syringe.restricted_samples").withStyle(ChatFormatting.GRAY));
		if (hasLoadedRack(stack)) {
			ItemStack rack = getLoadedRack(stack);
			tooltip.add(Component.translatable("item.hemomancy.living_syringe.loaded",
					VialRackItem.countEmptyVials(rack), VialRackItem.MAX_VIALS));
			if (ClientBridge.get().hasShiftDown()) {
				VialRackItem.appendContentsTooltip(rack, tooltip);
			} else if (VialRackItem.countEmptyVials(rack) < VialRackItem.MAX_VIALS) {
				tooltip.add(Component.translatable("item.hemomancy.vial_rack.shift_hint")
						.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
			}
		} else if (hasLoadedVial(stack)) {
			tooltip.add(Component.translatable("item.hemomancy.living_syringe.loaded_vial"));
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
			if (!worldIn.isClientSide) VialRackItem.migrateAhaematicSamples(rack);
			setLoadedRack(stack, rack);
		}
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
		if (player.isShiftKeyDown()) {
			if (!player.level().isClientSide) {
				ejectLoaded(player, stack);
			}
			return InteractionResult.CONSUME;
		}
		// Consume rather than succeed: the draw animation replaces the arm swing.
		if (!player.level().isClientSide) {
			InteractionResult result = fillVialFromTarget(player, target, stack, null, "");
			return result.consumesAction() ? InteractionResult.CONSUME : result;
		}
		return InteractionResult.CONSUME;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack syringe = playerIn.getItemInHand(handIn);
		if (!worldIn.isClientSide) {
			if (playerIn.isShiftKeyDown()) {
				ejectLoaded(playerIn, syringe);
			} else if (!hasLoadedRack(syringe) && !hasLoadedVial(syringe)) {
				if (!loadFromInventory(playerIn, syringe)) {
					playerIn.displayClientMessage(Component.translatable("item.hemomancy.living_syringe.no_rack"), true);
				}
			}
		}
		// Consume rather than succeed: an arm swing would hide the hinge and drum motion.
		return InteractionResultHolder.consume(syringe);
	}

    @Override public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        var player = context.getPlayer();
        var level = context.getLevel();
        if (player == null || player.isShiftKeyDown() || !level.getBlockState(context.getClickedPos()).is(net.minecraft.world.level.block.Blocks.SCULK_CATALYST)) return super.useOn(context);
        if (level.isClientSide) return InteractionResult.SUCCESS;
        var syringe = context.getItemInHand();
        if (hasLoadedVial(syringe) || !hasLoadedRack(syringe) && !loadRackFromInventory(player, syringe)) return InteractionResult.FAIL;
        var rack = getLoadedRack(syringe);
        int slot = VialRackItem.findFirstEmptyVialSlot(rack);
        if (slot < 0) return InteractionResult.FAIL;
        var vials = VialRackItem.getVials(rack);
        var vial = com.vincenthuto.hemomancy.common.item.component.AhaematicSample.create("block", "minecraft:sculk_catalyst");
        vials.set(slot, vial);
        VialRackItem.setVials(rack, vials);
        setLoadedRack(syringe, rack);
        level.gameEvent(player, net.minecraft.world.level.gameevent.GameEvent.BLOCK_CHANGE, context.getClickedPos());
        level.playSound(null, context.getClickedPos(), net.minecraft.sounds.SoundEvents.SCULK_CATALYST_BLOOM, net.minecraft.sounds.SoundSource.BLOCKS, .5F, .8F);
        return InteractionResult.CONSUME;
    }
    /** The dialogue validates consent before requesting an authenticated donation. */
    public InteractionResult collectDonation(net.minecraft.server.level.ServerPlayer player,
            com.vincenthuto.hemomancy.common.entity.npc.harbinger.ProfessionalHarbingerEntity donor,
            java.util.UUID bloodline, String profession) {
        var syringe = player.getMainHandItem();
        if (syringe.getItem() != this) return InteractionResult.FAIL;
        return fillVialFromTarget(player, donor, syringe, bloodline, profession);
    }

	private InteractionResult fillVialFromTarget(Player player, LivingEntity target, ItemStack syringe,
            java.util.UUID donorBloodline, String donorProfession) {
		var targetId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        // Original professionals are protected; their consent-based donation is still permitted.
		BloodSamplingResult samplingResult = BloodSamplingRules.evaluate(false, true, target.isAlive(),
				target.isInvulnerable() && donorBloodline == null, targetId != null,
                BloodProfileData.profile(target.getType(), false).requiresLivingSyringe(), true);
		if (samplingResult != BloodSamplingResult.SUCCESS) {
			player.displayClientMessage(Component.translatable(samplingResult.translationKey(), target.getDisplayName()), true);
			return InteractionResult.FAIL;
		}
		if (hasLoadedVial(syringe) && !VialRackItem.isEmptyVial(getLoadedVial(syringe))) ejectLoaded(player, syringe);
		if (!hasLoadedRack(syringe) && !hasLoadedVial(syringe) && !loadFromInventory(player, syringe)) {
			player.displayClientMessage(Component.translatable("item.hemomancy.living_syringe.no_rack"), true);
			return InteractionResult.FAIL;
		}
		boolean single = !hasLoadedRack(syringe);
		ItemStack rack = getLoadedRack(syringe);
		int emptySlot = single ? 0 : VialRackItem.findFirstEmptyVialSlot(rack);
		if (emptySlot < 0) {
			ejectRack(player, syringe);
			player.displayClientMessage(Component.translatable("item.hemomancy.living_syringe.rack_full"), true);
			return InteractionResult.FAIL;
		}
		NonNullList<ItemStack> vials = single ? NonNullList.create() : VialRackItem.getVials(rack);
		ItemStack sampledVial = (single ? getLoadedVial(syringe) : vials.get(emptySlot)).copyWithCount(1);
		CompoundTag vialTag = sampledVial.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		vialTag.putString(BloodVialItem.TAG_ENTITY_TYPE, targetId.toString());
		vialTag.putBoolean(BloodVialItem.TAG_STATE, true);
		sampledVial.set(DataComponents.CUSTOM_DATA, CustomData.of(vialTag));
        if (donorBloodline != null) com.vincenthuto.hemomancy.common.rite.succession.SuccessionSamples.fill(sampledVial, target, donorBloodline, donorProfession);
        else if (target instanceof Player donor) com.vincenthuto.hemomancy.common.rite.succession.SuccessionSamples.fill(sampledVial, donor, null, "");
        sampledVial.remove(com.vincenthuto.hemomancy.common.init.DataComponentInit.BLOOD_SAMPLE_IDENTIFIED.get());
        if (target.getType() == net.minecraft.world.entity.EntityType.WARDEN)
            sampledVial = com.vincenthuto.hemomancy.common.item.component.AhaematicSample.create("entity", "minecraft:warden");
		if (single) {
			setLoadedVial(syringe, ItemStack.EMPTY);
		} else {
			vials.set(emptySlot, sampledVial);
			VialRackItem.setVials(rack, vials);
			setLoadedRack(syringe, rack);
		}
        com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodKnowledge.collected(player, sampledVial);
		player.playSound(SoundEvents.BOTTLE_FILL, 2.0F, 0.8F);
		player.displayClientMessage(Component.translatable(BloodSamplingResult.SUCCESS.translationKey(),
				target.getDisplayName()), true);
		InteractionHand hand = player.getMainHandItem() == syringe ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		PacketHandler.sendTracking(player, new PacketLivingSyringeDraw(player.getId(), target.getId(), hand,
				(hand == InteractionHand.MAIN_HAND) == (player.getMainArm() == HumanoidArm.RIGHT),
				EntityParticleUtils.getColorFromPredicate(EntityParticleUtils.getEntityPredicate(target)).getColor(),
				single ? MODEL_LOAD_FILLED_VIAL : modelLoad(syringe, player.registryAccess())));
		if (single) {
			giveBack(player, sampledVial);
		} else if (VialRackItem.countEmptyVials(rack) <= 0) {
			ejectRack(player, syringe);
		}
		return InteractionResult.SUCCESS;
	}

	/** A rack takes priority; a loose empty vial is only used when no rack has room. */
	private boolean loadFromInventory(Player player, ItemStack syringe) {
		return loadRackFromInventory(player, syringe) || loadVialFromInventory(player, syringe);
	}

	private boolean loadVialFromInventory(Player player, ItemStack syringe) {
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack candidate = player.getInventory().getItem(i);
			if (candidate.getItem() instanceof BloodVialItem && VialRackItem.isEmptyVial(candidate)) {
				setLoadedVial(syringe, candidate.copyWithCount(1));
				candidate.shrink(1);
				player.playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 2.0F, 0.9F);
				return true;
			}
		}
		return false;
	}

	private void ejectLoaded(Player player, ItemStack syringe) {
		if (ejectRack(player, syringe) || !hasLoadedVial(syringe)) return;
		ItemStack vial = getLoadedVial(syringe);
		setLoadedVial(syringe, ItemStack.EMPTY);
		giveBack(player, vial);
		player.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 2.0F, 0.9F);
	}

	private static void giveBack(Player player, ItemStack stack) {
		if (!player.getInventory().add(stack)) player.drop(stack, false);
	}

	private boolean hasLoadedVial(ItemStack syringe) {
		return !getLoadedVial(syringe).isEmpty();
	}

	private ItemStack getLoadedVial(ItemStack syringe) {
		var inventory = new LivingSyringeItemHandler(syringe, 1);
		inventory.load();
		return inventory.getStackInSlot(0);
	}

	private void setLoadedVial(ItemStack syringe, ItemStack vial) {
		var inventory = new LivingSyringeItemHandler(syringe, 1);
		inventory.load();
		inventory.setStackInSlot(0, vial);
		inventory.save();
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
