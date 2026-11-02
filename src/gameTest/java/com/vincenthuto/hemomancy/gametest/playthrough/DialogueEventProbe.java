package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Records every {@link DialogueEvent} the server posts, per player, after every route has seen it, so an
 * {@link Outcome} can say which event a choice dispatched and whether its reward was delivered. The listener is added
 * to the game bus once, the first time a playthrough player is made.
 */
final class DialogueEventProbe {
	private static final Map<UUID, List<DialogueEvent>> POSTED = new HashMap<>();
	private static boolean listening;

	private DialogueEventProbe() {
	}

	static synchronized void listen() {
		if (listening) return;
		listening = true;
		NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, DialogueEvent.class, event -> {
			if (event.getPlayer() != null) {
				POSTED.computeIfAbsent(event.getPlayer().getUUID(), id -> new ArrayList<>()).add(event);
			}
		});
	}

	static int mark(UUID player) {
		return POSTED.getOrDefault(player, List.of()).size();
	}

	static List<DialogueEvent> since(UUID player, int mark) {
		List<DialogueEvent> events = POSTED.getOrDefault(player, List.of());
		return mark >= events.size() ? List.of() : List.copyOf(events.subList(mark, events.size()));
	}

	static void forget(UUID player) {
		POSTED.remove(player);
	}
}
