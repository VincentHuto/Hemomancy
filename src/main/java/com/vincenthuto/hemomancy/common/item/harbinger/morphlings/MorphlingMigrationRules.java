package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

public final class MorphlingMigrationRules {
	private static final Map<String, String> ID_MIGRATIONS = Map.ofEntries(
			entry("morphling_leeches", "morphling_deadmans_purse"),
			entry("morphling_fungal", "morphling_gravecap"),
			entry("morphling_bat", "morphling_witchs_ear"),
			entry("morphling_cuttlefish", "morphling_lumenlace"),
			entry("morphling_foxfire", "morphling_lumenlace"),
			entry("morphling_spider", "morphling_bootlace"),
			entry("morphling_mole", "morphling_irontooth"),
			entry("morphling_serpent", "morphling_emberfang"),
			entry("morphling_centipede", "morphling_winter_shroud"),
			entry("morphling_chitinite", "morphling_winter_shroud"),
			entry("morphling_urchin", "morphling_winter_shroud"),
			entry("morphling_pests", "morphling_gravecap"),
			entry("morphling_tick", "morphling_deadmans_purse"));

	private static final Map<String, String> LAYER_FAMILY_MIGRATIONS = Map.ofEntries(
			entry("leeches", "deadmans_purse"),
			entry("morphling_leeches", "deadmans_purse"),
			entry("fungal", "gravecap"),
			entry("morphling_fungal", "gravecap"),
			entry("bat", "witchs_ear"),
			entry("morphling_bat", "witchs_ear"),
			entry("cuttlefish", "lumenlace"),
			entry("morphling_cuttlefish", "lumenlace"),
			entry("foxfire", "lumenlace"),
			entry("morphling_foxfire", "lumenlace"),
			entry("spider", "bootlace"),
			entry("morphling_spider", "bootlace"),
			entry("mole", "irontooth"),
			entry("morphling_mole", "irontooth"),
			entry("serpent", "emberfang"),
			entry("morphling_serpent", "emberfang"),
			entry("centipede", "winter_shroud"),
			entry("morphling_centipede", "winter_shroud"),
			entry("chitinite", "winter_shroud"),
			entry("morphling_chitinite", "winter_shroud"),
			entry("urchin", "winter_shroud"),
			entry("morphling_urchin", "winter_shroud"),
			entry("pests", "gravecap"),
			entry("morphling_pests", "gravecap"),
			entry("tick", "deadmans_purse"),
			entry("morphling_tick", "deadmans_purse"));

	private MorphlingMigrationRules() {
	}

	public static Optional<ResourceLocation> migrateId(ResourceLocation oldId) {
		if (oldId == null || !Hemomancy.MOD_ID.equals(oldId.getNamespace())) {
			return Optional.empty();
		}
		String targetPath = ID_MIGRATIONS.get(oldId.getPath());
		return targetPath == null ? Optional.empty() : Optional.of(Hemomancy.rloc(targetPath));
	}

	public static ItemStack migrateStack(ItemStack old) {
		if (old.isEmpty()) {
			return old;
		}
		ResourceLocation oldId = BuiltInRegistries.ITEM.getKey(old.getItem());
		Optional<ResourceLocation> targetId = migrateId(oldId);
		if (targetId.isEmpty()) {
			return old;
		}
		Item target = BuiltInRegistries.ITEM.get(targetId.get());
		if (target == Items.AIR) {
			return old;
		}
		ItemStack migrated = new ItemStack(target, old.getCount());
		// Preserve EnzymePower, Primalized, WildBound, feed history, cooldowns, nectar markers, and any future stack data.
		migrated.applyComponents(old.getComponentsPatch());
		return migrated;
	}

	public static String migrateLayerFamily(String old) {
		if (old == null) {
			return null;
		}
		String migrated = LAYER_FAMILY_MIGRATIONS.get(normalizeLayerFamily(old));
		return migrated == null ? old : migrated;
	}

	private static String normalizeLayerFamily(String family) {
		String normalized = family.trim().toLowerCase(Locale.ROOT);
		int namespaceSeparator = normalized.indexOf(':');
		if (namespaceSeparator >= 0 && namespaceSeparator < normalized.length() - 1) {
			normalized = normalized.substring(namespaceSeparator + 1);
		}
		int slashSeparator = normalized.lastIndexOf('/');
		if (slashSeparator >= 0 && slashSeparator < normalized.length() - 1) {
			return normalized.substring(slashSeparator + 1);
		}
		return normalized;
	}
}
