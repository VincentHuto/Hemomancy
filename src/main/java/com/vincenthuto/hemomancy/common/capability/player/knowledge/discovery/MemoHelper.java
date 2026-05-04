package com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery;

import java.util.HashSet;
import java.util.Set;

import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hutoslib.common.book.knowledge.IBookKnowledge;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.LiberKnowledgeEvents;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hutoslib.common.book.knowledge.CommonDiscoverySource;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class MemoHelper {
	public static final String EVENT_PREFIX = "memo_capture:";
	public static final int MAX_FIELD_NOTES_MEMOS = 15;
	public static final int DICTATION_BASE_BLOOD_COST = 100;
	public static final int DICTATION_BLOOD_COST_PER_MEMO = 75;
	public static final int DICTATION_UNSTAINED_XP_LEVEL_COST = 1;

	private static final String TAG_REMAINING = "RemainingMemos";
	private static final String TAG_MEMOS = "Memos";
	private static final String TAG_INK_PATH = "InkPath";
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
		if (memoId == null) {
			player.displayClientMessage(Component.translatable("message.hemomancy.memo.invalid")
					.withStyle(ChatFormatting.RED), true);
			return true;
		}
		captureMemo(player, memoId);
		return true;
	}

	public static CaptureResult captureMemo(ServerPlayer player, ResourceLocation memoId) {
		ItemStack notes = findFieldNotes(player);
		if (notes.isEmpty()) {
			player.displayClientMessage(Component.translatable("message.hemomancy.memo.no_field_notes")
					.withStyle(ChatFormatting.DARK_RED), true);
			return CaptureResult.NO_FIELD_NOTES;
		}
		MemoDefinition definition = MemoDefinitions.get(memoId).orElse(null);
		MemoDefinition.MemoPath memoPath = definition != null ? definition.path() : MemoDefinition.MemoPath.SHARED;
		MemoDefinition.MemoPath notePath = getInkPath(notes);
		if (notePath == null) {
			player.displayClientMessage(Component.translatable("message.hemomancy.memo.no_ink_path")
					.withStyle(ChatFormatting.GRAY), true);
			return CaptureResult.NO_INK_PATH;
		}
		if (!pathsCompatible(notePath, memoPath)) {
			player.displayClientMessage(Component.translatable("message.hemomancy.memo.wrong_ink_path")
					.withStyle(ChatFormatting.GRAY), true);
			return CaptureResult.WRONG_INK_PATH;
		}
		if (hasMemo(notes, memoId)) {
			player.displayClientMessage(Component.translatable("message.hemomancy.memo.already_noted")
					.withStyle(ChatFormatting.GRAY), true);
			return CaptureResult.ALREADY_NOTED;
		}
		if (getRemainingMemos(notes) <= 0) {
			player.displayClientMessage(Component.translatable("message.hemomancy.memo.field_notes_full")
					.withStyle(ChatFormatting.DARK_RED), true);
			return CaptureResult.FIELD_NOTES_FULL;
		}

		CompoundTag tag = getCustomData(notes);
		ListTag memos = tag.getList(TAG_MEMOS, TAG_STRING);
		memos.add(StringTag.valueOf(memoId.toString()));
		tag.put(TAG_MEMOS, memos);
		tag.putInt(TAG_REMAINING, Math.max(0, getRemainingMemos(notes) - 1));
		setCustomData(notes, tag);

		player.displayClientMessage(Component.translatable("message.hemomancy.memo.noted")
				.withStyle(ChatFormatting.DARK_RED), true);
		return CaptureResult.CAPTURED;
	}

	public static DictationResult dictateToLiber(ServerPlayer player, ItemStack notes, ItemStack liber) {
		if (!isFieldNotes(notes)) {
			return DictationResult.NO_FIELD_NOTES;
		}
		if (!isLiber(liber)) {
			return DictationResult.NO_LIBER;
		}
		MemoDefinition.MemoPath notePath = getInkPath(notes);
		if (notePath == null) {
			player.displayClientMessage(Component.translatable("message.hemomancy.memo.no_ink_path")
					.withStyle(ChatFormatting.GRAY), true);
			return DictationResult.NO_INK_PATH;
		}
		if (!canDictateInto(liber, notePath)) {
			player.displayClientMessage(Component.translatable("message.hemomancy.memo.wrong_liber")
					.withStyle(ChatFormatting.GRAY), true);
			return DictationResult.WRONG_LIBER;
		}

		ListTag memos = getMemoList(notes);
		if (memos.isEmpty()) {
			player.displayClientMessage(Component.translatable("message.hemomancy.memo.no_memos")
					.withStyle(ChatFormatting.GRAY), true);
			return DictationResult.NO_MEMOS;
		}

		IBookKnowledge knowledge = HemoCapabilityAccess.requireLiberKnowledge(player);
		migrateLegacyLiberStack(liber, knowledge);

		int newMemoCount = 0;
		for (int i = 0; i < memos.size(); i++) {
			String memoId = memos.getString(i);
			ResourceLocation memoLocation = ResourceLocation.tryParse(memoId);
			if (memoLocation == null) {
				continue;
			}
			MemoDefinition.MemoPath memoPath = MemoDefinitions.get(memoLocation)
					.map(MemoDefinition::path)
					.orElse(MemoDefinition.MemoPath.SHARED);
			if (!pathsCompatible(notePath, memoPath)) {
				player.displayClientMessage(Component.translatable("message.hemomancy.memo.mixed_notes")
						.withStyle(ChatFormatting.GRAY), true);
				return DictationResult.MIXED_NOTES;
			}
			if (!knowledge.knowsMemo(memoLocation)) {
				newMemoCount++;
			}
		}

		if (newMemoCount == 0) {
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
			unlockDictatedMemos(knowledge, memos);
			LiberKnowledgeEvents.sync(player);

			player.displayClientMessage(Component.translatable("message.hemomancy.memo.dictated_unstained", newMemoCount, cost)
					.withStyle(ChatFormatting.AQUA), true);
			return DictationResult.DICTATED;
		}

		int cost = getDictationBloodCost(knowledge.getKnownMemos().size(), newMemoCount);
		IBloodVolume volume = player.getData(HemoAttachmentTypes.BLOOD_VOLUME);
		if (!volume.isActive() || volume.getBloodVolume() < cost) {
			player.displayClientMessage(Component.translatable("message.hemomancy.memo.not_enough_blood", cost)
					.withStyle(ChatFormatting.DARK_RED), true);
			return DictationResult.NOT_ENOUGH_BLOOD;
		}

		volume.drain(cost);
		unlockDictatedMemos(knowledge, memos);
		LiberKnowledgeEvents.sync(player);

		player.displayClientMessage(Component.translatable("message.hemomancy.memo.dictated", newMemoCount, cost)
				.withStyle(ChatFormatting.DARK_RED), true);
		return DictationResult.DICTATED;
	}

	private static void unlockDictatedMemos(IBookKnowledge knowledge, ListTag memos) {
		for (int i = 0; i < memos.size(); i++) {
			String memoId = memos.getString(i);
			ResourceLocation memoLocation = ResourceLocation.tryParse(memoId);
			ResourceLocation liberEntry = liberEntryForMemo(memoId);
			if (memoLocation != null && liberEntry != null) {
				knowledge.unlockMemo(memoLocation, liberEntry);
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
		HemoCapabilityAccess.getLiberKnowledge(player).ifPresent(knowledge -> {
			migrateLegacyLiberStack(liber, knowledge);
			LiberKnowledgeEvents.sync(player);
		});
	}

	public static Set<String> getLegacyUnlockedLiberEntries(ItemStack stack) {
		return readStringSet(stack, TAG_LIBER_ENTRIES);
	}

	public static Set<String> getLegacyKnownMemos(ItemStack stack) {
		return readStringSet(stack, TAG_KNOWN_MEMOS);
	}

	private static Set<String> readStringSet(ItemStack stack, String tagName) {
		Set<String> values = new HashSet<>();
		ListTag list = getCustomData(stack).getList(tagName, TAG_STRING);
		for (int i = 0; i < list.size(); i++) {
			values.add(list.getString(i));
		}
		return values;
	}

	public static String memoEvent(ResourceLocation memoId) {
		return EVENT_PREFIX + memoId;
	}

	public static int getRemainingMemos(ItemStack stack) {
		CompoundTag tag = getCustomData(stack);
		if (!tag.contains(TAG_REMAINING)) {
			return 0;
		}
		return Math.max(0, Math.min(MAX_FIELD_NOTES_MEMOS, tag.getInt(TAG_REMAINING)));
	}

	public static int getMemoCount(ItemStack stack) {
		return getMemoList(stack).size();
	}

	public static RefillResult refillFieldNotes(ItemStack stack, ItemStack ink) {
		MemoDefinition.MemoPath inkPath = pathForInk(ink);
		if (inkPath == null) {
			return RefillResult.NO_INK;
		}
		MemoDefinition.MemoPath currentPath = getInkPath(stack);
		if (getMemoCount(stack) > 0 && currentPath != null && currentPath != inkPath) {
			return RefillResult.WRONG_INK_PATH;
		}
		CompoundTag tag = getCustomData(stack);
		tag.putInt(TAG_REMAINING, MAX_FIELD_NOTES_MEMOS);
		tag.putString(TAG_INK_PATH, inkPath.name());
		setCustomData(stack, tag);
		return RefillResult.REFILLED;
	}

	public static MemoDefinition.MemoPath getInkPath(ItemStack stack) {
		CompoundTag tag = getCustomData(stack);
		if (!tag.contains(TAG_INK_PATH, TAG_STRING)) {
			return null;
		}
		try {
			return MemoDefinition.MemoPath.valueOf(tag.getString(TAG_INK_PATH));
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	public static boolean isInk(ItemStack stack) {
		return pathForInk(stack) != null;
	}

	public static boolean isFieldNotes(ItemStack stack) {
		return !stack.isEmpty() && stack.is(ItemInit.field_notes.get());
	}

	public static boolean isLiber(ItemStack stack) {
		return !stack.isEmpty() && (stack.is(ItemInit.liber_sanguinum.get()) || stack.is(ItemInit.liber_immaculatus.get()));
	}

	public static boolean canDictateInto(ItemStack liber, MemoDefinition.MemoPath path) {
		if (path == MemoDefinition.MemoPath.SHARED) {
			return isLiber(liber);
		}
		if (path == MemoDefinition.MemoPath.HARBINGER) {
			return !liber.isEmpty() && liber.is(ItemInit.liber_sanguinum.get());
		}
		if (path == MemoDefinition.MemoPath.UNSTAINED) {
			return !liber.isEmpty() && liber.is(ItemInit.liber_immaculatus.get());
		}
		return false;
	}

	private static ItemStack findFieldNotes(ServerPlayer player) {
		if (isFieldNotes(player.getMainHandItem())) {
			return player.getMainHandItem();
		}
		if (isFieldNotes(player.getOffhandItem())) {
			return player.getOffhandItem();
		}

		Inventory inventory = player.getInventory();
		for (ItemStack stack : inventory.items) {
			if (isFieldNotes(stack)) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}

	private static boolean hasMemo(ItemStack stack, ResourceLocation memoId) {
		return containsString(getMemoList(stack), memoId.toString());
	}

	private static ListTag getMemoList(ItemStack stack) {
		return getCustomData(stack).getList(TAG_MEMOS, TAG_STRING);
	}

	private static boolean containsString(ListTag list, String value) {
		for (int i = 0; i < list.size(); i++) {
			if (value.equals(list.getString(i))) {
				return true;
			}
		}
		return false;
	}

	private static ResourceLocation liberEntryForMemo(String memoId) {
		ResourceLocation id = ResourceLocation.tryParse(memoId);
		if (id == null) {
			return null;
		}
		return MemoDefinitions.get(id)
				.map(MemoDefinition::liberEntry)
				.orElse(id);
	}

	private static MemoDefinition.MemoPath pathForInk(ItemStack ink) {
		if (ink.is(ItemInit.hematic_field_ink.get())) {
			return MemoDefinition.MemoPath.HARBINGER;
		}
		if (ink.is(ItemInit.pale_field_ink.get())) {
			return MemoDefinition.MemoPath.UNSTAINED;
		}
		return null;
	}

	private static boolean pathsCompatible(MemoDefinition.MemoPath notesPath, MemoDefinition.MemoPath memoPath) {
		return notesPath == MemoDefinition.MemoPath.SHARED
				|| memoPath == MemoDefinition.MemoPath.SHARED
				|| notesPath == memoPath;
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
		if (path.startsWith("sanctumsanguinium/")) {
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
		NO_FIELD_NOTES,
		FIELD_NOTES_FULL,
		NO_INK_PATH,
		WRONG_INK_PATH
	}

	public enum RefillResult {
		REFILLED,
		NO_INK,
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
