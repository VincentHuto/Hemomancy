package com.vincenthuto.hemomancy.common.entity.boss.saint;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The fight's two text channels. {@link #teach} shows a lesson once per fight per player on the action bar (grey
 * italic unless a Saint colour is given); {@link #voice} sends the Saint's line in chat to its challengers, at most
 * once per 200 ticks per line. Both reset with the trial ({@link #reset}); {@code TransientServerStateEvents} clears
 * them with the session and on logout.
 */
public final class SaintLines {
	public static final int VOICE_GAP = 200;

	private static final Map<UUID, Set<String>> TAUGHT = new HashMap<>();
	private static final Map<UUID, Map<String, Long>> VOICED = new HashMap<>();

	private SaintLines() {
	}

	/** True when the lesson showed (the first time this fight). */
	public static boolean teach(Player player, String key) {
		return teach(player, key, ChatFormatting.GRAY);
	}

	public static boolean teach(Player player, String key, ChatFormatting color) {
		if (!firstTeaching(player.getUUID(), key)) return false;
		player.displayClientMessage(Component.translatable(key).withStyle(color, ChatFormatting.ITALIC), true);
		return true;
	}

	/** True when the line was said (not within 200 ticks of the last time). */
	public static boolean voice(SaintTrialEntity saint, String key) {
		if (!voiceDue(saint.getUUID(), key, saint.level().getGameTime())) return false;
		Component line = Component.translatable(key).withStyle(ChatFormatting.ITALIC);
		SaintArena arena = saint.arena();
		if (arena != null) {
			for (Player player : arena.challengers()) player.sendSystemMessage(line);
		}
		return true;
	}

	/** The pure once-per-fight rule: true the first time, then false until {@link #forget}. */
	public static boolean firstTeaching(UUID player, String key) {
		return TAUGHT.computeIfAbsent(player, ignored -> new HashSet<>()).add(key);
	}

	/** The pure voice-gap rule; records the line when it is due. */
	public static boolean voiceDue(UUID saint, String key, long now) {
		Map<String, Long> lines = VOICED.computeIfAbsent(saint, ignored -> new HashMap<>());
		Long last = lines.get(key);
		if (last != null && now - last < VOICE_GAP) return false;
		lines.put(key, now);
		return true;
	}

	/** The trial ended or reset: its voice record and its challengers' lessons go. */
	public static void reset(SaintTrialEntity saint, Iterable<? extends Player> challengers) {
		VOICED.remove(saint.getUUID());
		for (Player player : challengers) forget(player.getUUID());
	}

	public static void forget(UUID player) {
		TAUGHT.remove(player);
	}

	public static void clearSessionState() {
		TAUGHT.clear();
		VOICED.clear();
	}

	public static void logout(Player player) {
		forget(player.getUUID());
	}
}
