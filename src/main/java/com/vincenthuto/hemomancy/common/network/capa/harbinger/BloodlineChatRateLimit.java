package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bloodline chat bypasses vanilla chat, and with it vanilla's spam kick, so it keeps its own limit (F-7): at most
 * {@link #MESSAGES} messages per {@link #WINDOW_TICKS} server ticks for each sender. Messages over the limit are
 * dropped.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class BloodlineChatRateLimit {
	public static final int MESSAGES = 3;
	public static final long WINDOW_TICKS = 100L;
	/** Each sender's most recent send times, oldest first. */
	private static final Map<UUID, long[]> RECENT = new HashMap<>();

	private BloodlineChatRateLimit() {
	}

	/** Records a message from {@code sender} at {@code tick} if the limit allows it. */
	public static boolean tryAcquire(UUID sender, long tick) {
		return allow(RECENT.computeIfAbsent(sender, ignored -> emptyWindow()), tick);
	}

	/**
	 * Allows a message at {@code tick} when fewer than {@link #MESSAGES} of the recorded times fall within the window,
	 * and records it in place of the oldest.
	 */
	static boolean allow(long[] recent, long tick) {
		if (tick - recent[0] < WINDOW_TICKS) {
			return false;
		}
		System.arraycopy(recent, 1, recent, 0, recent.length - 1);
		recent[recent.length - 1] = tick;
		return true;
	}

	static long[] emptyWindow() {
		long[] recent = new long[MESSAGES];
		Arrays.fill(recent, Long.MIN_VALUE / 2);
		return recent;
	}

	public static void clearSessionState() {
		RECENT.clear();
	}

	@SubscribeEvent
	public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		RECENT.remove(event.getEntity().getUUID());
	}
}
