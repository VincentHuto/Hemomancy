package com.vincenthuto.hemomancy.common.entity.boss.saint;

import javax.annotation.Nullable;

/**
 * How a move's footprint is shown: fill and edge families (null: the Saint's own), the tick the footprint first shows
 * (clamped so it always shows for {@link SaintMoves#revealFloor} ticks before the commit), a Saint style id for its
 * painter, and whether it stays drawn through the active span.
 */
public record TelegraphSpec(@Nullable TelegraphFamily fill, @Nullable TelegraphFamily edge, int revealTick, int style,
		boolean showWhileActive) {
	public static final TelegraphSpec DEFAULT = new TelegraphSpec(null, null, 0, 0, true);
}
