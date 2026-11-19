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

import com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenVatBlock;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVessel;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVessels;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.UnaryOperator;

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
        // A kept creature in a jar or vat: the syringe draws it for a little Vigor, whatever its hide (P6).
        SpecimenVessel vessel = player == null || player.isShiftKeyDown() ? null : vesselAt(level, context.getClickedPos());
        if (vessel != null && vessel.kept().living()) {
            if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer drawer)) return InteractionResult.SUCCESS;
            return sampleVessel(server, drawer, context.getItemInHand(), vessel);
        }
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

    /** The vessel a click on {@code pos} reaches: a jar, or a vat by either half. */
    @Nullable
    private static SpecimenVessel vesselAt(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof SpecimenVessel vessel) return vessel.vessel();
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof SpecimenVatBlock ? SpecimenVatBlock.vat(level, pos, state) : null;
    }

    /**
     * Draws a kept creature into the loaded rack or vial. The rack or vial is found first, so a syringe with nothing to
     * fill costs the creature nothing; the vessel then charges its Vigor under its keeper's rights.
     */
    private InteractionResult sampleVessel(ServerLevel level, ServerPlayer player, ItemStack syringe, SpecimenVessel vessel) {
        Slot slot = prepare(player, syringe);
        if (slot == null) return InteractionResult.FAIL;
        SpecimenVessels.Drawn drawn = SpecimenVessels.syringeSample(level, ((BlockEntity) vessel).getBlockPos(), vessel, player);
        if (drawn == null) return InteractionResult.FAIL;
        ItemStack sampled = store(player, syringe, slot, drawn.type(), drawn.donor(), vial -> {
            vial.remove(com.vincenthuto.hemomancy.common.init.DataComponentInit.BLOOD_SAMPLE_IDENTIFIED.get());
            return vial;
        });
        finishDraw(player, syringe, slot, sampled);
        return InteractionResult.CONSUME;
    }

	private InteractionResult fillVialFromTarget(Player player, LivingEntity target, ItemStack syringe,
            java.util.UUID donorBloodline, String donorProfession) {
		var targetId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
		// A troupe professor's blood is the key to their mind: only their gift fills a vial with it (2026-11-17).
		if (target instanceof com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFacultyEntity) {
			player.displayClientMessage(Component.translatable("hemomancy.circus.ordeal.key.no_draw"), true);
			return InteractionResult.FAIL;
		}
        // Original professionals are protected; their consent-based donation is still permitted.
		BloodSamplingResult samplingResult = BloodSamplingRules.evaluate(false, true, target.isAlive(),
				target.isInvulnerable() && donorBloodline == null, targetId != null,
                BloodProfileData.profile(target.getType(), false).requiresLivingSyringe(), true,
                donorBloodline == null && com.vincenthuto.hemomancy.common.item.harbinger.BloodSamplingCost.recentlyDrawn(target));
		if (samplingResult != BloodSamplingResult.SUCCESS) {
			player.displayClientMessage(Component.translatable(samplingResult.translationKey(), target.getDisplayName()), true);
			return InteractionResult.FAIL;
		}
		Slot slot = prepare(player, syringe);
		if (slot == null) return InteractionResult.FAIL;
		boolean single = slot.single();
		ItemStack sampledVial = store(player, syringe, slot, targetId, target.getStringUUID(), vial -> {
            if (donorBloodline != null) com.vincenthuto.hemomancy.common.rite.succession.SuccessionSamples.fill(vial, target, donorBloodline, donorProfession);
            else if (target instanceof Player donor) com.vincenthuto.hemomancy.common.rite.succession.SuccessionSamples.fill(vial, donor, null, "");
            vial.remove(com.vincenthuto.hemomancy.common.init.DataComponentInit.BLOOD_SAMPLE_IDENTIFIED.get());
            return target.getType() == net.minecraft.world.entity.EntityType.WARDEN
                    ? com.vincenthuto.hemomancy.common.item.component.AhaematicSample.create("entity", "minecraft:warden") : vial;
        });
        com.vincenthuto.hemomancy.common.mission.alchemist.BloodResearch.recordMet(player, target.getType());
        // A consenting donor gives freely; any other creature pays for its sample (core-loop Track 5).
        if (donorBloodline == null) com.vincenthuto.hemomancy.common.item.harbinger.BloodSamplingCost.charge(player, target, true);
		player.playSound(SoundEvents.BOTTLE_FILL, 2.0F, 0.8F);
		player.displayClientMessage(Component.translatable(BloodSamplingResult.SUCCESS.translationKey(),
				target.getDisplayName()), true);
		InteractionHand hand = player.getMainHandItem() == syringe ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		PacketHandler.sendTracking(player, new PacketLivingSyringeDraw(player.getId(), target.getId(), hand,
				(hand == InteractionHand.MAIN_HAND) == (player.getMainArm() == HumanoidArm.RIGHT),
				EntityParticleUtils.getColorFromPredicate(EntityParticleUtils.getEntityPredicate(target)).getColor(),
				single ? MODEL_LOAD_FILLED_VIAL : modelLoad(syringe, player.registryAccess())));
		finishDraw(player, syringe, slot, sampledVial);
		return InteractionResult.SUCCESS;
	}

	/** Where a draw goes: the loaded vial, or the first empty slot of the loaded rack. */
	private record Slot(boolean single, ItemStack rack, NonNullList<ItemStack> vials, int index) {
	}

	/**
	 * Readies the syringe for a draw: a filled loose vial is handed back, a rack (or else an empty vial) is loaded from
	 * the inventory, and a full rack is ejected. Null, with the reason said, when there is nothing to fill.
	 */
	@Nullable
	private Slot prepare(Player player, ItemStack syringe) {
		if (hasLoadedVial(syringe) && !VialRackItem.isEmptyVial(getLoadedVial(syringe))) ejectLoaded(player, syringe);
		if (!hasLoadedRack(syringe) && !hasLoadedVial(syringe) && !loadFromInventory(player, syringe)) {
			player.displayClientMessage(Component.translatable("item.hemomancy.living_syringe.no_rack"), true);
			return null;
		}
		boolean single = !hasLoadedRack(syringe);
		ItemStack rack = getLoadedRack(syringe);
		int emptySlot = single ? 0 : VialRackItem.findFirstEmptyVialSlot(rack);
		if (emptySlot < 0) {
			ejectRack(player, syringe);
			player.displayClientMessage(Component.translatable("item.hemomancy.living_syringe.rack_full"), true);
			return null;
		}
		return new Slot(single, rack, single ? NonNullList.create() : VialRackItem.getVials(rack), emptySlot);
	}

	/**
	 * Writes a sample of {@code typeId} from {@code donor} into {@code slot}, after {@code finish} has marked or replaced
	 * it, and records the collection. Returns the stored vial.
	 */
	private ItemStack store(Player player, ItemStack syringe, Slot slot, ResourceLocation typeId, String donor,
			UnaryOperator<ItemStack> finish) {
		ItemStack sampledVial = (slot.single() ? getLoadedVial(syringe) : slot.vials().get(slot.index())).copyWithCount(1);
		CompoundTag vialTag = sampledVial.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		vialTag.putString(BloodVialItem.TAG_ENTITY_TYPE, typeId.toString());
		vialTag.putBoolean(BloodVialItem.TAG_STATE, true);
		vialTag.putString(BloodSampleData.TAG_DONOR, donor);
		sampledVial.set(DataComponents.CUSTOM_DATA, CustomData.of(vialTag));
		sampledVial = finish.apply(sampledVial);
		if (slot.single()) {
			setLoadedVial(syringe, ItemStack.EMPTY);
		} else {
			slot.vials().set(slot.index(), sampledVial);
			VialRackItem.setVials(slot.rack(), slot.vials());
			setLoadedRack(syringe, slot.rack());
		}
		com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodKnowledge.collected(player, sampledVial);
		return sampledVial;
	}

	/** A loose vial goes back to the player; a rack with no empty vial left is ejected. */
	private void finishDraw(Player player, ItemStack syringe, Slot slot, ItemStack sampledVial) {
		if (slot.single()) {
			giveBack(player, sampledVial);
		} else if (VialRackItem.countEmptyVials(slot.rack()) <= 0) {
			ejectRack(player, syringe);
		}
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
