package com.vincenthuto.hemomancy.common.capability;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.event.PendingBloodCraftManager;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Credit a player earned while offline (PT-17, PT-20), kept in the world by UUID and applied once at their next login
 * through the appliers table. Not a migration: nothing old is rewritten, the credit only arrives late.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class OfflineCredits extends SavedData {
	private static final String DATA_NAME = "hemomancy_offline_credits";
	private static final SavedData.Factory<OfflineCredits> FACTORY =
			new SavedData.Factory<>(OfflineCredits::new, OfflineCredits::load, null);

	/** What was earned: a Blood Structure craft closing, or a Loom climax (Ganglionic when the loom counted it so). */
	public enum Kind {
		STRUCTURE_CRAFT, LOOM_WEAVE, LOOM_WEAVE_GANGLIONIC
	}

	/** One owed credit and the item it was earned for. */
	public record Credit(Kind kind, ResourceLocation item) {
	}

	private static final class Appliers {
		static final Map<Kind, BiConsumer<ServerPlayer, ItemStack>> ALL = Map.of(
				Kind.STRUCTURE_CRAFT, PendingBloodCraftManager::credit,
				Kind.LOOM_WEAVE, (player, result) -> SomaticLoomBlockEntity.creditWeave(player, result, false),
				Kind.LOOM_WEAVE_GANGLIONIC, (player, result) -> SomaticLoomBlockEntity.creditWeave(player, result, true));
	}

	private final Map<UUID, List<Credit>> owed = new HashMap<>();

	public static OfflineCredits get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	/** Keeps a {@code kind} credit for {@code result} until {@code player} next logs in. */
	public static void owe(MinecraftServer server, UUID player, Kind kind, ItemStack result) {
		get(server).owe(player, new Credit(kind, BuiltInRegistries.ITEM.getKey(result.getItem())));
	}

	void owe(UUID player, Credit credit) {
		owed.computeIfAbsent(player, id -> new ArrayList<>()).add(credit);
		setDirty();
	}

	/** Removes and returns everything owed to {@code player}, in the order it was earned. */
	List<Credit> take(UUID player) {
		List<Credit> credits = owed.remove(player);
		if (credits == null) return List.of();
		setDirty();
		return credits;
	}

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		for (Credit credit : get(player.server).take(player.getUUID())) {
			ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(credit.item()));
			if (!result.isEmpty()) Appliers.ALL.get(credit.kind()).accept(player, result);
		}
	}

	public static OfflineCredits load(CompoundTag tag, HolderLookup.Provider provider) {
		OfflineCredits data = new OfflineCredits();
		CompoundTag players = tag.getCompound("Owed");
		for (String key : players.getAllKeys()) {
			UUID id;
			try {
				id = UUID.fromString(key);
			} catch (IllegalArgumentException ignored) {
				continue;
			}
			List<Credit> credits = new ArrayList<>();
			ListTag list = players.getList(key, Tag.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				CompoundTag entry = list.getCompound(i);
				Kind kind;
				try {
					kind = Kind.valueOf(entry.getString("Kind").toUpperCase(Locale.ROOT));
				} catch (IllegalArgumentException ignored) {
					continue;
				}
				ResourceLocation item = ResourceLocation.tryParse(entry.getString("Item"));
				if (item != null) credits.add(new Credit(kind, item));
			}
			if (!credits.isEmpty()) data.owed.put(id, credits);
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		CompoundTag players = new CompoundTag();
		for (Map.Entry<UUID, List<Credit>> entry : owed.entrySet()) {
			ListTag list = new ListTag();
			for (Credit credit : entry.getValue()) {
				CompoundTag saved = new CompoundTag();
				saved.putString("Kind", credit.kind().name().toLowerCase(Locale.ROOT));
				saved.putString("Item", credit.item().toString());
				list.add(saved);
			}
			players.put(entry.getKey().toString(), list);
		}
		tag.put("Owed", players);
		return tag;
	}
}
