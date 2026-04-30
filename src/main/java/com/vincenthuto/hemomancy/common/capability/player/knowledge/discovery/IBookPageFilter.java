package com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery;

import com.vincenthuto.hutoslib.common.data.book.BookCodeModel;

import net.minecraft.world.entity.player.Player;

/**
 * Strategy interface for filtering a {@link BookCodeModel} based on what the
 * player has unlocked. Inject an implementation when opening a Liber book
 * screen to control which chapters and pages are visible.
 *
 * <p>The no-op {@link #PASSTHROUGH} constant can be used when no filtering is
 * desired (e.g. in creative/debug contexts).
 */
public interface IBookPageFilter {

    /** A no-op filter that returns the source model unchanged. */
    IBookPageFilter PASSTHROUGH = (source, player) -> source;

    /**
     * Returns a (possibly new) {@link BookCodeModel} that only contains
     * chapters and pages the given player is permitted to see.
     *
     * @param source the full book model loaded from data
     * @param player the player opening the book (may be a client-side
     *               {@link net.minecraft.client.player.LocalPlayer})
     * @return the filtered model, or {@code source} if no filtering is needed
     */
    BookCodeModel filter(BookCodeModel source, Player player);
}
