package com.vincenthuto.hemomancy.common.mission.alchemist;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.VialRackItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.UUID;

/** Server-authoritative state and rewards for the Degree-2 First Separation assignment. */
public final class FirstSeparationAssignment {
	private static final String DATA_CENTRIFUGE_ACQUIRED = "hemomancy:first_separation_centrifuge_acquired";
	private static final String DATA_ASSIGNED_SPIN = "hemomancy:first_separation_spin";
	private static final String TAG_SPIN = "first_separation_spin";
	private static final String TAG_PLAYER = "first_separation_player";
	public static final ResourceLocation ADV_BRIEFED =
			Hemomancy.rloc("hemomancy/first_separation_briefed");
	public static final ResourceLocation ADV_REWARD_CLAIMED =
			Hemomancy.rloc("hemomancy/first_separation_reward_claimed");

	private FirstSeparationAssignment() {
	}

	public static boolean canBrief(ServerPlayer player) {
		return HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 2 && !isBriefed(player);
	}

	public static boolean isBriefed(ServerPlayer player) {
		return HarbingerAdvancementGranter.hasAdvancement(player, ADV_BRIEFED);
	}

	public static boolean markBriefed(ServerPlayer player) {
		HarbingerAdvancementGranter.grantIfNotDone(player, ADV_BRIEFED);
		return isBriefed(player);
	}

	public static List<ItemStack> briefingStacks() {
		return List.of(new ItemStack(ItemInit.bloody_vial.get()), new ItemStack(ItemInit.bloody_vial.get()));
	}

	public static void giveBriefingSupplies(ServerPlayer player) {
		for (ItemStack stack : briefingStacks()) {
			if (!player.getInventory().add(stack)) player.drop(stack, false);
		}
	}

	public static void markCentrifugeAcquired(ServerPlayer player) {
		player.getPersistentData().putBoolean(DATA_CENTRIFUGE_ACQUIRED, true);
	}

	public static boolean hasCentrifugeAcquired(ServerPlayer player) {
		return player.getPersistentData().getBoolean(DATA_CENTRIFUGE_ACQUIRED);
	}

	public static UUID beginAssignmentSpin(ServerPlayer player) {
		if (!canBeginAssignmentSpin(player)) return null;
		UUID spinId = UUID.randomUUID();
		player.getPersistentData().putString(DATA_ASSIGNED_SPIN, spinId.toString());
		return spinId;
	}

	public static boolean canBeginAssignmentSpin(ServerPlayer player) {
		return isBriefed(player) && !HarbingerAdvancementGranter.isFirstSeparationComplete(player);
	}

	public static void markAssignmentOutput(ItemStack stack, UUID playerId, UUID spinId) {
		if (stack.isEmpty() || playerId == null || spinId == null) return;
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putString(TAG_PLAYER, playerId.toString());
		tag.putString(TAG_SPIN, spinId.toString());
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static boolean tryRecoverAssignmentOutput(ServerPlayer player, ItemStack stack) {
		UUID expectedSpin = parseUuid(player.getPersistentData().getString(DATA_ASSIGNED_SPIN));
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		UUID outputPlayer = parseUuid(tag.getString(TAG_PLAYER));
		UUID outputSpin = parseUuid(tag.getString(TAG_SPIN));
		if (!FirstSeparationSpinProof.matches(player.getUUID(), expectedSpin, outputPlayer, outputSpin)) return false;
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE);
		return HarbingerAdvancementGranter.isFirstSeparationComplete(player);
	}

	private static UUID parseUuid(String value) {
		if (value == null || value.isBlank()) return null;
		try {
			return UUID.fromString(value);
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	public static boolean canClaim(ServerPlayer player) {
		return HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 2
				&& isBriefed(player)
				&& HarbingerAdvancementGranter.isFirstSeparationStarted(player)
				&& HarbingerAdvancementGranter.isFirstSeparationComplete(player)
				&& !isClaimed(player);
	}

	public static boolean isClaimed(ServerPlayer player) {
		return HarbingerAdvancementGranter.hasAdvancement(player, ADV_REWARD_CLAIMED);
	}

	public static boolean markClaimed(ServerPlayer player) {
		HarbingerAdvancementGranter.grantIfNotDone(player, ADV_REWARD_CLAIMED);
		return isClaimed(player);
	}

	public static List<ItemStack> rewardStacks() {
		ItemStack rack = new ItemStack(ItemInit.vial_rack.get());
		VialRackItem.ensureInitialized(rack);
		return List.of(new ItemStack(ItemInit.living_syringe.get()), rack);
	}
}
