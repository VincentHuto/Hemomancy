package com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.LiberKnowledge;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.LiberKnowledgeEvents;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hutoslib.common.book.knowledge.CommonDiscoverySource;
import com.vincenthuto.hutoslib.common.book.knowledge.IBookKnowledge;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.*;

public final class MemoHelper {
	private static final BloodAccounting.Source DICTATION = BloodAccounting.Source.of("memo_dictation", Category.OTHER, false, false);
	public static final String EVENT_PREFIX = "memo_capture:";
	public static final int DICTATION_BASE_BLOOD_COST = 100;
	public static final int DICTATION_BLOOD_COST_PER_MEMO = 75;
	public static final int DICTATION_UNSTAINED_XP_LEVEL_COST = 1;

	private static final String TAG_KNOWN_MEMOS = "KnownMemos";
	private static final String TAG_LIBER_ENTRIES = "UnlockedLiberEntries";
	private static final int TAG_STRING = 8;

	private MemoHelper() {
	}

	public static boolean isMemoEvent(String eventId) {
		return eventId != null && eventId.startsWith(EVENT_PREFIX);
	}

	public static boolean handleMemoEvent(ServerPlayer player, String eventId) {
		ResourceLocation memoId = ResourceLocation.tryParse(eventId.substring(EVENT_PREFIX.length()));
		if (memoId == null || MemoDefinitions.get(memoId).isEmpty()) {
			player.displayClientMessage(Component.translatable("message.hemomancy.memo.invalid")
					.withStyle(ChatFormatting.RED), true);
			return true;
		}
		captureMemo(player, memoId);
		return true;
	}

	public static CaptureResult captureMemo(ServerPlayer player, ResourceLocation memoId) {
		if (memoId == null || MemoDefinitions.get(memoId).isEmpty()) {
			player.displayClientMessage(Component.translatable("message.hemomancy.memo.invalid")
					.withStyle(ChatFormatting.RED), true);
			return CaptureResult.INVALID;
		}
		LiberKnowledge knowledge = HemoCapabilityAccess.getLiberKnowledge(player);
		if (knowledge.knowsMemo(memoId)) {
			player.displayClientMessage(Component.translatable("message.hemomancy.memo.already_written")
					.withStyle(ChatFormatting.GRAY), true);
			return CaptureResult.ALREADY_KNOWN;
		}
		if (knowledge.getPendingMemos().contains(memoId)) {
			player.displayClientMessage(Component.translatable("message.hemomancy.memo.already_noted")
					.withStyle(ChatFormatting.GRAY), true);
			return CaptureResult.ALREADY_NOTED;
		}

		knowledge.recordPendingMemo(memoId);
		LiberKnowledgeEvents.sync(player);
		player.displayClientMessage(Component.translatable("message.hemomancy.memo.noted")
				.withStyle(colorForMemo(memoId)), true);
		return CaptureResult.CAPTURED;
	}

	public static DictationResult dictatePendingToLiber(ServerPlayer player, ItemStack liber) {
		if (!isLiber(liber)) {
			return DictationResult.NO_LIBER;
		}

		LiberKnowledge knowledge = HemoCapabilityAccess.getLiberKnowledge(player);
		migrateLegacyLiberStack(liber, knowledge);

		if (knowledge.getPendingMemos().isEmpty()) {
			player.displayClientMessage(Component.translatable("message.hemomancy.memo.no_memos")
					.withStyle(ChatFormatting.GRAY), true);
			return DictationResult.NO_MEMOS;
		}

		MemoDefinition.MemoPath liberPath = pathForLiber(liber);
		List<ResourceLocation> dictatedMemos = getDictatableMemosForPath(knowledge.getPendingMemos(), liberPath);
		if (dictatedMemos.isEmpty()) {
			player.displayClientMessage(Component.translatable("message.hemomancy.memo.wrong_liber")
					.withStyle(ChatFormatting.GRAY), true);
			return DictationResult.WRONG_LIBER;
		}
		int newMemoCount = 0;
		for (ResourceLocation memoId : dictatedMemos) {
			if (!knowledge.knowsMemo(memoId)) {
				newMemoCount++;
			}
		}

		if (newMemoCount == 0) {
			knowledge.removePendingMemos(dictatedMemos);
			LiberKnowledgeEvents.sync(player);
			player.displayClientMessage(Component.translatable("message.hemomancy.memo.no_new_memos")
					.withStyle(ChatFormatting.GRAY), true);
			return DictationResult.NO_NEW_MEMOS;
		}

		if (liber.is(ItemInit.liber_immaculatus.get())) {
			int cost = getUnstainedDictationXpLevelCost();
			if (player.experienceLevel < cost) {
				player.displayClientMessage(Component.translatable("message.hemomancy.memo.not_enough_xp", cost)
						.withStyle(ChatFormatting.AQUA), true);
				return DictationResult.NOT_ENOUGH_EXPERIENCE;
			}

			player.giveExperienceLevels(-cost);
			unlockDictatedMemos(knowledge, dictatedMemos);
			knowledge.removePendingMemos(dictatedMemos);
			LiberKnowledgeEvents.sync(player);

			player.displayClientMessage(Component.translatable("message.hemomancy.memo.dictated_unstained", newMemoCount, cost)
					.withStyle(ChatFormatting.AQUA), true);
			return DictationResult.DICTATED;
		}

		int cost = getDictationBloodCost(knowledge.getKnownMemos().size(), newMemoCount);
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		if (!volume.isActive() || volume.getBloodVolume() < cost) {
			player.displayClientMessage(Component.translatable("message.hemomancy.memo.not_enough_blood", cost)
					.withStyle(ChatFormatting.DARK_RED), true);
			return DictationResult.NOT_ENOUGH_BLOOD;
		}

		BloodAccounting.pay(player, cost, DICTATION);
		unlockDictatedMemos(knowledge, dictatedMemos);
		knowledge.removePendingMemos(dictatedMemos);
		LiberKnowledgeEvents.sync(player);

		player.displayClientMessage(Component.translatable("message.hemomancy.memo.dictated", newMemoCount, cost)
				.withStyle(ChatFormatting.DARK_RED), true);
		return DictationResult.DICTATED;
	}

    public static boolean hasPendingForLiber(net.minecraft.world.entity.player.Player player, ItemStack liber) {
        if (player == null || !isLiber(liber)) return false;
        var knowledge = HemoCapabilityAccess.getLiberKnowledge(player);
        return getDictatableMemosForPath(knowledge.getPendingMemos(), pathForLiber(liber)).stream()
                .anyMatch(memo -> !knowledge.knowsMemo(memo));
    }

	public static List<ResourceLocation> getDictatableMemosForPath(Collection<ResourceLocation> pendingMemos,
			MemoDefinition.MemoPath liberPath) {
		List<ResourceLocation> dictatable = new ArrayList<>();
		if (liberPath == null) {
			return dictatable;
		}
		for (ResourceLocation memoId : pendingMemos) {
			if (pathsCompatible(liberPath, pathForMemo(memoId))) {
				dictatable.add(memoId);
			}
		}
		return dictatable;
	}

	/** A memo with no definition is still written down, but it names no Liber entry to unlock. */
	private static void unlockDictatedMemos(IBookKnowledge knowledge, Collection<ResourceLocation> memos) {
		for (ResourceLocation memoLocation : memos) {
			ResourceLocation liberEntry = liberEntryForMemo(memoLocation);
			if (liberEntry != null) {
				knowledge.unlockMemo(memoLocation, liberEntry);
			} else {
				knowledge.recordMemo(memoLocation);
			}
		}
	}

	/**
	 * Blood cost for dictating a batch of new memos.
	 * The cost scales on the total number of memos that will be known after
	 * dictation (existing + new), so each subsequent dictation session costs more
	 * as the player's record grows. This reflects the increasing complexity of
	 * integrating deeper knowledge into the Liber.
	 */
	public static int getDictationBloodCost(int knownMemoCount, int newMemoCount) {
		return DICTATION_BASE_BLOOD_COST + (knownMemoCount + newMemoCount) * DICTATION_BLOOD_COST_PER_MEMO;
	}

	public static int getUnstainedDictationXpLevelCost() {
		return DICTATION_UNSTAINED_XP_LEVEL_COST;
	}

	public static void migrateLegacyLiberStack(ServerPlayer player, ItemStack liber) {
		var knowledge = HemoCapabilityAccess.getLiberKnowledge(player);
		migrateLegacyLiberStack(liber, knowledge);
		LiberKnowledgeEvents.sync(player);
	}

	public static String memoEvent(ResourceLocation memoId) {
		return EVENT_PREFIX + memoId;
	}

	public static boolean isLiber(ItemStack stack) {
		return !stack.isEmpty() && (stack.is(ItemInit.liber_sanguinum.get()) || stack.is(ItemInit.liber_immaculatus.get()));
	}

	/** Null for an id that is not a memo: an unknown id never names a Liber entry. */
	private static ResourceLocation liberEntryForMemo(ResourceLocation memoId) {
		return MemoDefinitions.get(memoId)
				.map(MemoDefinition::liberEntry)
				.orElse(null);
	}

	private static MemoDefinition.MemoPath pathForLiber(ItemStack liber) {
		if (liber.is(ItemInit.liber_sanguinum.get())) {
			return MemoDefinition.MemoPath.HARBINGER;
		}
		if (liber.is(ItemInit.liber_immaculatus.get())) {
			return MemoDefinition.MemoPath.UNSTAINED;
		}
		return null;
	}

	private static boolean pathsCompatible(MemoDefinition.MemoPath notesPath, MemoDefinition.MemoPath memoPath) {
		return notesPath == MemoDefinition.MemoPath.SHARED
				|| memoPath == MemoDefinition.MemoPath.SHARED
				|| notesPath == memoPath;
	}

	private static MemoDefinition.MemoPath pathForMemo(ResourceLocation memoId) {
		return MemoDefinitions.get(memoId)
				.map(MemoDefinition::path)
				.orElse(MemoDefinition.MemoPath.SHARED);
	}

	private static ChatFormatting colorForMemo(ResourceLocation memoId) {
		MemoDefinition.MemoPath path = pathForMemo(memoId);
		if (path == MemoDefinition.MemoPath.UNSTAINED) {
			return ChatFormatting.AQUA;
		}
		if (path == MemoDefinition.MemoPath.SHARED) {
			return ChatFormatting.GRAY;
		}
		return ChatFormatting.DARK_RED;
	}

	private static void migrateLegacyLiberStack(ItemStack liber, IBookKnowledge knowledge) {
		CompoundTag tag = getCustomData(liber);
		boolean hadLegacyData = tag.contains(TAG_KNOWN_MEMOS) || tag.contains(TAG_LIBER_ENTRIES);
		if (!hadLegacyData) {
			return;
		}
		ListTag memoList = tag.getList(TAG_KNOWN_MEMOS, TAG_STRING);
		for (int i = 0; i < memoList.size(); i++) {
			ResourceLocation memoId = ResourceLocation.tryParse(memoList.getString(i));
			if (memoId != null) {
				knowledge.recordMemo(memoId);
			}
		}
		ListTag entryList = tag.getList(TAG_LIBER_ENTRIES, TAG_STRING);
		for (int i = 0; i < entryList.size(); i++) {
			ResourceLocation entryId = ResourceLocation.tryParse(entryList.getString(i));
			if (entryId != null && legacyEntryMatchesLiber(liber, entryId)) {
				knowledge.unlockEntry(entryId, CommonDiscoverySource.OTHER);
			}
		}
		tag.remove(TAG_KNOWN_MEMOS);
		tag.remove(TAG_LIBER_ENTRIES);
		setCustomData(liber, tag);
	}

	private static boolean legacyEntryMatchesLiber(ItemStack liber, ResourceLocation entryId) {
		String path = entryId.getPath();
		if (path.startsWith("libersanguinium/")) {
			return liber.is(ItemInit.liber_sanguinum.get());
		}
		if (path.startsWith("liberimmaculatus/")) {
			return liber.is(ItemInit.liber_immaculatus.get());
		}
		return true;
	}

	private static CompoundTag getCustomData(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	private static void setCustomData(ItemStack stack, CompoundTag tag) {
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public enum CaptureResult {
		CAPTURED,
		ALREADY_NOTED,
		ALREADY_KNOWN,
		INVALID,
		NO_FIELD_NOTES,
		FIELD_NOTES_FULL,
		NO_INK_PATH,
		WRONG_INK_PATH
	}

	public enum DictationResult {
		DICTATED,
		NO_FIELD_NOTES,
		NO_LIBER,
		NO_MEMOS,
		NO_NEW_MEMOS,
		NOT_ENOUGH_BLOOD,
		NOT_ENOUGH_EXPERIENCE,
		NO_INK_PATH,
		WRONG_LIBER,
		MIXED_NOTES
	}
}
