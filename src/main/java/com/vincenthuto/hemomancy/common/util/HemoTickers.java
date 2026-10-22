package com.vincenthuto.hemomancy.common.util;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;

import javax.annotation.Nullable;

/** Block entity tickers, typed so a block's {@code getTicker} needs no unchecked cast of its own. */
public final class HemoTickers {
	private HemoTickers() {
	}

	/** {@code ticker} when the block is asking for {@code type}'s ticker, otherwise null. */
	@Nullable
	@SuppressWarnings("unchecked")
	public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> create(
			BlockEntityType<A> requested, BlockEntityType<E> type, BlockEntityTicker<? super E> ticker) {
		return type == requested ? (BlockEntityTicker<A>) ticker : null;
	}
}
