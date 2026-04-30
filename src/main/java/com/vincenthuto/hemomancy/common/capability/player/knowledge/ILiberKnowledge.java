package com.vincenthuto.hemomancy.common.capability.player.knowledge;

import com.vincenthuto.hutoslib.common.book.knowledge.IBookKnowledge;

/**
 * Hemomancy-specific book-knowledge interface. All generic methods are
 * inherited from {@link IBookKnowledge}; this interface exists as a typed
 * handle so Hemomancy capability keys and accessors remain strongly typed
 * to the mod's own attachment slot.
 */
public interface ILiberKnowledge extends IBookKnowledge {
}
