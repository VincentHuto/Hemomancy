package com.vincenthuto.hemomancy.common.mission.alchemist;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.VialRackItem;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.UUID;

/** Server-authoritative state and rewards for the Degree-2 First Separation assignment. */
public final class FirstSeparationAssignment {
	private static final String DATA_CENTRIFUGE_ACQUIRED = "hemomancy:first_separation_centrifuge_acquired";
	private static final String DATA_SAMPLE_ACQUIRED = "hemomancy:first_separation_sample_acquired";
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
		if (isBriefed(player)) recognizeInventorySample(player);
		return isBriefed(player);
	}

	public static void recognizeInventorySample(ServerPlayer player) {
		if (!isBriefed(player) || hasSampleAcquired(player)) return;
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (BloodSampleData.isStorableSample(stack) && BloodSampleData.entityType(stack) != null) {
				markSampleAcquired(player);
				return;
			}
		}
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
		putDurableBoolean(player, DATA_CENTRIFUGE_ACQUIRED);
	}

	public static boolean hasCentrifugeAcquired(ServerPlayer player) {
		return durableBoolean(player, DATA_CENTRIFUGE_ACQUIRED);
	}

	public static void markSampleAcquired(ServerPlayer player) {
		if (isBriefed(player)) putDurableBoolean(player, DATA_SAMPLE_ACQUIRED);
	}

	public static boolean hasSampleAcquired(ServerPlayer player) {
		return durableBoolean(player, DATA_SAMPLE_ACQUIRED);
	}

	public static UUID beginAssignmentSpin(ServerPlayer player) {
		if (!canBeginAssignmentSpin(player)) return null;
		UUID spinId = UUID.randomUUID();
		putDurableString(player, DATA_ASSIGNED_SPIN, spinId.toString());
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
		UUID expectedSpin = parseUuid(durableString(player, DATA_ASSIGNED_SPIN));
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

	private static void putDurableBoolean(ServerPlayer player, String key) {
		CompoundTag root = player.getPersistentData();
		CompoundTag durable = root.getCompound(Player.PERSISTED_NBT_TAG);
		durable.putBoolean(key, true);
		root.put(Player.PERSISTED_NBT_TAG, durable);
		root.remove(key);
	}

	private static boolean durableBoolean(ServerPlayer player, String key) {
		CompoundTag root = player.getPersistentData();
		CompoundTag durable = root.getCompound(Player.PERSISTED_NBT_TAG);
		if (durable.contains(key)) return durable.getBoolean(key);
		if (!root.getBoolean(key)) return false;
		putDurableBoolean(player, key);
		return true;
	}

	private static void putDurableString(ServerPlayer player, String key, String value) {
		CompoundTag root = player.getPersistentData();
		CompoundTag durable = root.getCompound(Player.PERSISTED_NBT_TAG);
		durable.putString(key, value);
		root.put(Player.PERSISTED_NBT_TAG, durable);
		root.remove(key);
	}

	private static String durableString(ServerPlayer player, String key) {
		CompoundTag root = player.getPersistentData();
		CompoundTag durable = root.getCompound(Player.PERSISTED_NBT_TAG);
		if (durable.contains(key)) return durable.getString(key);
		String legacy = root.getString(key);
		if (!legacy.isBlank()) putDurableString(player, key, legacy);
		return legacy;
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
