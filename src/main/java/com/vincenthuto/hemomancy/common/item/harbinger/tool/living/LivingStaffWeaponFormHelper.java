package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketLivingStaffMorph;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStaffEscrow;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.PacketDistributor;

public final class LivingStaffWeaponFormHelper {
	public static final String STORED_STAFF_KEY = "HemomancyStoredLivingStaff";
	public static final String FORM_KEY = "HemomancyStaffWeaponForm";
	public static final String SECONDARY_TENDENCY_KEY = "HemomancyStaffWeaponSecondaryTendency";

	private LivingStaffWeaponFormHelper() {
	}

	public static boolean applySelection(Player player, BloodManipulation selectedManip) {
		ItemStack beforeMain = player.getMainHandItem().copy();
		ItemStack beforeOff = player.getOffhandItem().copy();
		String selectedName = selectedManip == null ? "" : selectedManip.getName();
		boolean applied;
		if (!LivingStaffWeaponFormRules.isStaffWeaponFormManip(selectedName)) {
			restoreMainHandNow(player);
			applied = true;
		} else {
			applied = applyWeaponForm(player, selectedName);
		}
		syncMorph(player, beforeMain, beforeOff);
		return applied;
	}

	public static boolean toggleSelectedForm(Player player, BloodManipulation selectedManip) {
		ItemStack beforeMain = player.getMainHandItem().copy();
		ItemStack beforeOff = player.getOffhandItem().copy();
		String selectedName = selectedManip == null ? "" : selectedManip.getName();
		if (!LivingStaffWeaponFormRules.isStaffWeaponFormManip(selectedName)) {
			return false;
		}
		boolean applied;
		if (LivingStaffWeaponFormRules.shouldToggleOffSelectedForm(selectedName, currentFormName(player.getMainHandItem()))) {
			applied = restoreMainHandNow(player);
		} else {
			applied = applyWeaponForm(player, selectedName);
		}
		syncMorph(player, beforeMain, beforeOff);
		return applied;
	}

	public static boolean restoreMainHand(Player player) {
		ItemStack beforeMain = player.getMainHandItem().copy();
		ItemStack beforeOff = player.getOffhandItem().copy();
		boolean restored = restoreMainHandNow(player);
		syncMorph(player, beforeMain, beforeOff);
		return restored;
	}

	private static boolean restoreMainHandNow(Player player) {
		ItemStack held = player.getMainHandItem();
		LivingFlailDeployment.reconcileForRestoration(held, player);
		if (LivingSicklePruning.isTemporarySickle(held)) {
			ItemStack restoredWeapon = LivingSicklePruning.restoredWeaponStack(held, player.registryAccess());
			player.setItemInHand(InteractionHand.MAIN_HAND,
					isTransformedStaffWeapon(restoredWeapon)
							? restoredStaffStack(restoredWeapon, player.registryAccess())
							: restoredWeapon);
			LivingArsenalInventoryGuard.sanitizePlayerInventory(player);
			return true;
		}
		if (!isTransformedStaffWeapon(held)) {
			return false;
		}
		player.setItemInHand(InteractionHand.MAIN_HAND, restoredStaffStack(held, player.registryAccess()));
		LivingArsenalInventoryGuard.sanitizePlayerInventory(player);
		return true;
	}

	public static void removePairedOffhandClaw(Player player, ItemStack mainHandClaw) {
		if (LivingStaffWeaponFormRules.isPairedClawForm(
				currentFormName(mainHandClaw), currentFormName(player.getOffhandItem()))) {
			player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
		}
	}

	public static boolean restoreStaffAfterBloodFailure(ItemStack stack, LivingEntity entity) {
		return restoreStaffAfterBloodFailure(stack, entity, InteractionHand.MAIN_HAND);
	}

	public static boolean restoreStaffAfterBloodFailure(ItemStack stack, LivingEntity entity, InteractionHand hand) {
		if (!(entity instanceof Player player) || !isTransformedStaffWeapon(stack)) {
			return false;
		}
		ItemStack beforeMain = player.getMainHandItem().copy();
		ItemStack beforeOff = player.getOffhandItem().copy();
		LivingFlailDeployment.reconcileForRestoration(stack, player);
		player.setItemInHand(hand, restoredStaffStack(stack, player.registryAccess()));
		LivingArsenalInventoryGuard.sanitizePlayerInventory(player);
		syncMorph(player, beforeMain, beforeOff);
		player.displayClientMessage(Component.literal("The Living Staff recoils back into shape.")
				.withStyle(ChatFormatting.DARK_RED), true);
		return true;
	}

	public static boolean wasRestoredOutOfHand(ItemStack stack, LivingEntity entity) {
		return entity instanceof Player player
				&& isTransformedStaffWeapon(stack)
				&& player.getMainHandItem() != stack;
	}

	public static boolean isTransformedStaffWeapon(ItemStack stack) {
		if (stack.isEmpty() || !LivingStaffWeaponFormRules.isStaffWeaponFormManip(currentFormName(stack))) {
			return false;
		}
		return stack.get(DataComponents.CUSTOM_DATA) != null;
	}

	public static String currentFormName(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data == null) {
			return "";
		}
		return data.copyTag().getString(FORM_KEY);
	}

	public static ItemStack restoredStaffStack(ItemStack transformedStack, HolderLookup.Provider registryAccess) {
		CustomData data = transformedStack.get(DataComponents.CUSTOM_DATA);
		if (data != null) {
			CompoundTag root = data.copyTag();
			if (root.contains(STORED_STAFF_KEY, Tag.TAG_COMPOUND)) {
				ItemStack restored = ItemStack.parseOptional(registryAccess, root.getCompound(STORED_STAFF_KEY));
				if (!restored.isEmpty()) {
					restored.setCount(1);
					return restored;
				}
			}
		}
		return new ItemStack(ItemInit.living_staff.get());
	}

	private static boolean applyWeaponForm(Player player, String formName) {
		if (player instanceof ServerPlayer serverPlayer && CardinalRiteStaffEscrow.isPlanted(serverPlayer)) {
			player.displayClientMessage(Component.literal(
					"The Living Arsenal cannot answer while its staff is planted in a Cardinal Rite.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
			return false;
		}
		ItemStack held = player.getMainHandItem();
		if (LivingSicklePruning.isTemporarySickle(held)) {
			held = LivingSicklePruning.restoredWeaponStack(held, player.registryAccess());
			player.setItemInHand(InteractionHand.MAIN_HAND, held);
		}
		String currentForm = currentFormName(held);
		if (formName.equals(currentForm)) {
			return true;
		}
		if (!(held.getItem() instanceof LivingStaffItem) && !isTransformedStaffWeapon(held)) {
			player.displayClientMessage(Component.literal("The Living Staff must be in hand.")
					.withStyle(ChatFormatting.RED), true);
			return false;
		}
		if (!chargeHotSwapCost(player)) {
			return false;
		}
		ItemStack sourceStaff = isTransformedStaffWeapon(held)
				? restoredStaffStack(held, player.registryAccess())
				: held.copy();
		sourceStaff.setCount(1);
		ItemStack weapon = createWeaponFormStack(formName, sourceStaff, player.registryAccess());
		player.setItemInHand(InteractionHand.MAIN_HAND, weapon);
		if (LivingStaffWeaponFormRules.shouldEquipPairedOffhandClaw(formName, player.getOffhandItem().isEmpty())) {
			player.setItemInHand(InteractionHand.OFF_HAND,
					createWeaponFormStack(formName, sourceStaff, player.registryAccess()));
		}
		LivingArsenalInventoryGuard.sanitizePlayerInventory(player);
		return true;
	}

	private static boolean chargeHotSwapCost(Player player) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null || !volume.isActive()) {
			player.displayClientMessage(Component.literal("The staff finds no active blood in you.")
					.withStyle(ChatFormatting.RED), true);
			return false;
		}
		double cost = SkillPointHelper.getLivingStaffHotSwapCost(player);
		if (volume.getBloodVolume() < cost) {
			player.displayClientMessage(Component.literal("Not enough blood to reshape the Living Staff.")
					.withStyle(ChatFormatting.RED), true);
			return false;
		}
		volume.drain(cost);
		volume.addBloodSpend(cost);
		if (player instanceof ServerPlayer serverPlayer) {
			PacketHandler.sendToPlayer(serverPlayer, new BloodVolumeServerPacket(volume));
		}
		return true;
	}

	private static ItemStack createWeaponFormStack(String formName, ItemStack sourceStaff,
			HolderLookup.Provider registryAccess) {
		ItemStack weapon = new ItemStack(itemForForm(formName));
		CompoundTag root = weapon.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		Tag savedStaff = sourceStaff.save(registryAccess);
		if (savedStaff instanceof CompoundTag staffTag) {
			root.put(STORED_STAFF_KEY, staffTag);
		}
		root.putString(FORM_KEY, formName);
		BloodManipulation formManip = ManipulationInit.getByName(formName);
		EnumBloodTendency secondaryTendency = formManip != null ? formManip.getSecondaryTend() : null;
		if (secondaryTendency != null) {
			root.putString(SECONDARY_TENDENCY_KEY, secondaryTendency.name());
		}
		weapon.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
		return weapon;
	}

	private static Item itemForForm(String formName) {
		return switch (formName) {
			case LivingStaffWeaponFormRules.CONJURE_AXE -> ItemInit.living_axe.get();
			case LivingStaffWeaponFormRules.CONJURE_SPEAR -> ItemInit.living_spear.get();
			case LivingStaffWeaponFormRules.CONJURE_CLAWS -> ItemInit.living_baghnakh.get();
			case LivingStaffWeaponFormRules.CONJURE_CROSSBOW -> ItemInit.living_crossbow.get();
			case LivingStaffWeaponFormRules.CONJURE_TORCH -> ItemInit.living_torch.get();
			case LivingStaffWeaponFormRules.CONJURE_FLAIL -> ItemInit.living_flail.get();
			case LivingStaffWeaponFormRules.CONJURE_SICKLE -> ItemInit.living_sickle.get();
			case LivingStaffWeaponFormRules.CONJURE_BLADE -> ItemInit.living_blade.get();
			default -> ItemInit.living_staff.get();
		};
	}

	public static void syncMorph(Player player, ItemStack beforeMain, ItemStack beforeOff) {
		if (!(player instanceof ServerPlayer serverPlayer)) return;
		ItemStack afterMain = player.getMainHandItem().copy();
		ItemStack afterOff = player.getOffhandItem().copy();
		boolean mainChanged = !ItemStack.isSameItemSameComponents(beforeMain, afterMain);
		boolean offChanged = !ItemStack.isSameItemSameComponents(beforeOff, afterOff);
		boolean mainIsLiving = isLivingArsenalStack(beforeMain) || isLivingArsenalStack(afterMain);
		boolean offIsLiving = isLivingArsenalStack(beforeOff) || isLivingArsenalStack(afterOff);
		if (!LivingStaffMorphSequence.hasChangedStack(mainChanged, mainIsLiving, offChanged, offIsLiving)) return;

		PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer,
				new PacketLivingStaffMorph(serverPlayer.getId(), beforeMain, afterMain, beforeOff, afterOff));
		serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
				SoundEvents.HOGLIN_CONVERTED_TO_ZOMBIFIED, SoundSource.PLAYERS, 0.2F,
				0.9F + serverPlayer.getRandom().nextFloat() * 0.1F);
	}

	private static boolean isLivingArsenalStack(ItemStack stack) {
		return stack.getItem() instanceof LivingStaffItem || isTransformedStaffWeapon(stack);
	}
}
