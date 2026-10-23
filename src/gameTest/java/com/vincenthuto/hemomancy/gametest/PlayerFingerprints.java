package com.vincenthuto.hemomancy.gametest;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Fingerprints of what an action did to a player, for golden-master characterisation tests: what changed in the
 * player's saved data, what dropped, which Hemomancy advancements were earned, with the identities that differ
 * from run to run taken out. The golden file lives on the GameTest classpath; a missing or different one writes the
 * actual text to the game directory and fails.
 */
final class PlayerFingerprints {
	/** Volatile entity keys that say nothing about an action's outcome. */
	private static final Set<String> IGNORED = Set.of("Pos", "Motion", "Rotation", "FallDistance", "OnGround", "Air",
			"Fire", "PortalCooldown", "HurtTime", "HurtByTimestamp", "DeathTime", "AbsorptionAmount", "FallFlying",
			"recipeBook", "seenCredits", "enteredNetherPosition", "warden_spawn_tracker", "current_explosion_impact_pos",
			"ignore_fall_damage_from_current_explosion", "current_impulse_context_reset_grace_time");
	private static final Pattern ANY_UUID_ARRAY = Pattern.compile("\\[I;-?\\d+,-?\\d+,-?\\d+,-?\\d+]");
	private static final Pattern ANY_UUID = Pattern.compile(
			"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
	private static final Pattern BEARING = Pattern.compile(
			"\\d+ blocks(?: (?:northeast|northwest|southeast|southwest|north|south|east|west))?");

	private PlayerFingerprints() {
	}

	static CompoundTag saved(ServerPlayer player) {
		return player.saveWithoutId(new CompoundTag());
	}

	static Set<String> earned(ServerPlayer player) {
		Set<String> earned = new TreeSet<>();
		for (AdvancementHolder advancement : player.server.getAdvancements().getAllAdvancements()) {
			if (!advancement.id().getNamespace().equals("hemomancy")) continue;
			if (player.getAdvancements().getOrStartProgress(advancement).isDone()) earned.add(advancement.id().getPath());
		}
		return earned;
	}

	/** What changed in the player's saved data, two levels deep, in key order. */
	static String diff(CompoundTag before, CompoundTag after) {
		TreeMap<String, String> changes = new TreeMap<>();
		Set<String> keys = new TreeSet<>(before.getAllKeys());
		keys.addAll(after.getAllKeys());
		for (String key : keys) {
			if (IGNORED.contains(key)) continue;
			Tag old = before.get(key);
			Tag now = after.get(key);
			if (Objects.equals(old, now)) continue;
			if (old instanceof CompoundTag oldCompound && now instanceof CompoundTag nowCompound) {
				Set<String> inner = new TreeSet<>(oldCompound.getAllKeys());
				inner.addAll(nowCompound.getAllKeys());
				for (String innerKey : inner) {
					Tag a = oldCompound.get(innerKey);
					Tag b = nowCompound.get(innerKey);
					if (!Objects.equals(a, b)) changes.put(key + "." + innerKey, String.valueOf(b));
				}
			} else {
				changes.put(key, String.valueOf(now));
			}
		}
		return changes.toString();
	}

	/** Item drops near a position, by item id; the drops are removed. */
	static String drops(ServerLevel level, BlockPos near) {
		TreeMap<String, Integer> drops = new TreeMap<>();
		for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(near).inflate(8.0D))) {
			drops.merge(BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString(), item.getItem().getCount(),
					Integer::sum);
			item.discard();
		}
		return drops.toString();
	}

	/** Takes out what differs from run to run: generated identities and names, and room-dependent bearings. */
	static String normalise(String text, ServerPlayer player, Entity other) {
		text = replaceIdentity(text, player.getUUID(), "<player>");
		text = text.replace(player.getGameProfile().getName(), "<player-name>");
		if (other != null) {
			text = replaceIdentity(text, other.getUUID(), "<speaker>");
			text = text.replace(other.getName().getString(), "<speaker-name>");
		}
		// Fresh attachments roll random identities, and a bearing depends on where the test room was placed.
		text = ANY_UUID_ARRAY.matcher(text).replaceAll("<uuid>");
		text = ANY_UUID.matcher(text).replaceAll("<uuid>");
		return BEARING.matcher(text).replaceAll("<n> blocks <direction>");
	}

	private static String replaceIdentity(String text, UUID uuid, String placeholder) {
		text = text.replace(uuid.toString(), placeholder);
		return text.replace(new IntArrayTag(UUIDUtil.uuidToIntArray(uuid)).toString(), placeholder);
	}

	/** Compares the fingerprints with the golden file, failing with the first difference. */
	static void assertGolden(GameTestHelper helper, String resource, String actualFile, String actual) {
		String golden;
		try (InputStream stream = PlayerFingerprints.class.getResourceAsStream(resource)) {
			golden = stream == null ? null : new String(stream.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
		} catch (IOException exception) {
			throw new IllegalStateException(exception);
		}
		if (actual.equals(golden)) return;
		try {
			Files.writeString(Path.of(actualFile), actual, StandardCharsets.UTF_8);
		} catch (IOException exception) {
			throw new IllegalStateException(exception);
		}
		helper.fail(golden == null ? "No fingerprints recorded yet; wrote " + actualFile
				: "Fingerprints changed; first difference: " + firstDifference(golden, actual));
	}

	private static String firstDifference(String golden, String actual) {
		String[] expected = golden.split("\n");
		String[] got = actual.split("\n");
		for (int i = 0; i < Math.max(expected.length, got.length); i++) {
			String e = i < expected.length ? expected[i] : "<none>";
			String g = i < got.length ? got[i] : "<none>";
			if (!e.equals(g)) return "line " + (i + 1) + "\n expected " + e + "\n      got " + g;
		}
		return "none";
	}
}
