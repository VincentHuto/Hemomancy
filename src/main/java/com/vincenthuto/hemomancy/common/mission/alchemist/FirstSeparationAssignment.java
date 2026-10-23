package com.vincenthuto.hemomancy.common.mission.alchemist;

import com.vincenthuto.hemomancy.common.capability.PendingDelivery;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.VialRackItem;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
	private static final String DATA_PENDING_REWARDS = "hemomancy:first_separation_pending_rewards";
	private static final String DATA_BRIEFING_SUPPLIES_ISSUED = "hemomancy:first_separation_supplies_issued";
	private static final String DATA_PENDING_BRIEFING_VIALS = "hemomancy:first_separation_pending_vials";
	private static final String TAG_SPIN = "first_separation_spin";
	private static final String TAG_PLAYER = "first_separation_player";
	public static final ResourceLocation ADV_BRIEFED =
			Hemomancy.rloc("hemomancy/first_separation_briefed");
	public static final ResourceLocation ADV_REWARD_CLAIMED =
			Hemomancy.rloc("hemomancy/first_separation_reward_claimed");
	/** The two loose vials of the briefing, retried until they really land. */
	public static final PendingDelivery.Claim BRIEFING_VIALS = new PendingDelivery.Claim("first_separation_vials",
			player -> {
				int owed = durable(player).getInt(DATA_PENDING_BRIEFING_VIALS);
				return owed > 0 ? List.of(new ItemStack(ItemInit.bloody_vial.get(), owed)) : List.of();
			},
			player -> ClinicalBloodKnowledge.eligible(player) && isBriefed(player),
			(player, left) -> {
				CompoundTag durable = durable(player);
				durable.putInt(DATA_PENDING_BRIEFING_VIALS, left.stream().mapToInt(ItemStack::getCount).sum());
				player.getPersistentData().put(Player.PERSISTED_NBT_TAG, durable);
			});
	/** The earned syringe and initialized rack. Entries that no longer parse stay stored, untouched. */
	public static final PendingDelivery.Claim TOOLS = new PendingDelivery.Claim("first_separation_tools",
			player -> {
				ListTag pending = durable(player).getList(DATA_PENDING_REWARDS, 10);
				List<ItemStack> owed = new java.util.ArrayList<>();
				for (int i = 0; i < pending.size(); i++) {
					ItemStack stack = ItemStack.parseOptional(player.registryAccess(), pending.getCompound(i));
					if (!stack.isEmpty()) owed.add(stack);
				}
				return owed;
			},
			player -> ClinicalBloodKnowledge.eligible(player) && isClaimed(player),
			(player, left) -> {
				CompoundTag durable = durable(player);
				ListTag pending = durable.getList(DATA_PENDING_REWARDS, 10);
				ListTag remaining = new ListTag();
				for (ItemStack stack : left) remaining.add(stack.saveOptional(player.registryAccess()));
				for (int i = 0; i < pending.size(); i++) {
					CompoundTag encoded = pending.getCompound(i);
					if (ItemStack.parseOptional(player.registryAccess(), encoded).isEmpty()) remaining.add(encoded.copy());
				}
				durable.put(DATA_PENDING_REWARDS, remaining);
				player.getPersistentData().put(Player.PERSISTED_NBT_TAG, durable);
			});

	private FirstSeparationAssignment() {
	}

	public static boolean canBrief(ServerPlayer player) {
		return player.isAlive() && ClinicalBloodKnowledge.eligible(player) && !isBriefed(player);
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
		if (!player.isAlive() || !ClinicalBloodKnowledge.eligible(player) || !isBriefed(player)) return;
		CompoundTag root = player.getPersistentData();
		CompoundTag durable = root.getCompound(Player.PERSISTED_NBT_TAG);
		if (durable.getBoolean(DATA_BRIEFING_SUPPLIES_ISSUED)) return;
		durable.putBoolean(DATA_BRIEFING_SUPPLIES_ISSUED, true);
		durable.putInt(DATA_PENDING_BRIEFING_VIALS, briefingStacks().stream().mapToInt(ItemStack::getCount).sum());
		root.put(Player.PERSISTED_NBT_TAG, durable);
		deliverPendingBriefingVials(player);
	}

	static void deliverPendingBriefingVials(ServerPlayer player) {
		PendingDelivery.deliver(player, BRIEFING_VIALS);
	}

	private static CompoundTag durable(ServerPlayer player) {
		return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
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
		return ProgressionGates.hasDegree(player, 2)
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

	public static boolean claimRewards(ServerPlayer player) {
		if (!player.isAlive() || !canClaim(player)) return false;
		savePendingRewards(player, rewardStacks());
		if (!markClaimed(player)) return false;
		deliverPendingRewards(player);
		return true;
	}

	static void deliverPendingRewards(ServerPlayer player) {
		PendingDelivery.deliver(player, TOOLS);
	}

	private static void savePendingRewards(ServerPlayer player, List<ItemStack> stacks) {
		ListTag pending = new ListTag();
		for (ItemStack stack : stacks) pending.add(stack.saveOptional(player.registryAccess()));
		CompoundTag root = player.getPersistentData();
		CompoundTag durable = root.getCompound(Player.PERSISTED_NBT_TAG);
		durable.put(DATA_PENDING_REWARDS, pending);
		root.put(Player.PERSISTED_NBT_TAG, durable);
	}

	public static List<ItemStack> rewardStacks() {
		ItemStack rack = new ItemStack(ItemInit.vial_rack.get());
		VialRackItem.ensureInitialized(rack);
		return List.of(new ItemStack(ItemInit.living_syringe.get()), rack);
	}
}
