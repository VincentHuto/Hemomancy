package com.vincenthuto.hemomancy.client.liber;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.vincenthuto.hemomancy.common.capability.player.knowledge.ILiberKnowledge;

import net.minecraft.resources.ResourceLocation;

/**
 * Client-side, session-scoped tracker that knows which Liber entries a player
 * has already "seen" (i.e. the book was opened while those entries were
 * unlocked). Nothing is persisted to NBT or synced across the network.
 *
 * <p>Entries that were unlocked while the player was offline will therefore
 * always appear as "unread" during the first session where they open the book,
 * which is the intended behaviour.
 */
public final class LiberReadTracker {

    private static final Map<UUID, Set<ResourceLocation>> ACKNOWLEDGED = new HashMap<>();

    private LiberReadTracker() {
    }

    /**
     * Mark a collection of entry IDs as acknowledged (read) for this player.
     * Called when the player opens a liber book.
     */
    public static void acknowledge(UUID playerId, Collection<ResourceLocation> entryIds) {
        ACKNOWLEDGED.computeIfAbsent(playerId, id -> new HashSet<>()).addAll(entryIds);
    }

    /**
     * Returns {@code true} if the player has at least one unlocked entry for
     * {@code bookPrefix} that has not yet been acknowledged.
     *
     * @param playerId    the player's UUID
     * @param knowledge   the player's ILiberKnowledge capability
     * @param bookPrefix  path prefix used to scope entries to a single book,
     *                    e.g. {@code "sanctumsanguinium/"} or
     *                    {@code "liberimmaculatus/"}
     */
    public static boolean hasUnread(UUID playerId, ILiberKnowledge knowledge, String bookPrefix) {
        return countUnread(playerId, knowledge, bookPrefix) > 0;
    }

    /**
     * Returns the number of unlocked entries for {@code bookPrefix} that have
     * not yet been acknowledged by this player.
     */
    public static int countUnread(UUID playerId, ILiberKnowledge knowledge, String bookPrefix) {
        Set<ResourceLocation> seen = ACKNOWLEDGED.getOrDefault(playerId, Collections.emptySet());
        int count = 0;
        for (ResourceLocation entry : knowledge.getUnlockedEntries()) {
            if (entry.getPath().startsWith(bookPrefix) && !seen.contains(entry)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Remove all tracked data for this player. Called on logout so stale data
     * does not bleed into the next session.
     */
    public static void clear(UUID playerId) {
        ACKNOWLEDGED.remove(playerId);
    }
}
