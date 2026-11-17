package com.vincenthuto.hemomancy.common.world.ground;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Optional;

/**
 * The vanilla lights blood can switch: campfires, candles and candle cakes. One place answers what each looks like lit
 * and put out, for a hot gouge run, Crimson Flame Conjuration's kindling and Blood Eclipse's snuff (whose light taken
 * hangs as a field of dark, {@link DarkFields}). A waterlogged light cannot be kindled. Torches, lanterns and glowstone
 * have no unlit state and are beyond all three. Furnaces burn from outside through {@link FurnaceHeat}, not here.
 */
public final class SwitchableLights {
	private SwitchableLights() {
	}

	/** The state {@code state} takes when kindled, when it is an unlit light that can take fire. */
	public static Optional<BlockState> kindled(BlockState state) {
		if (state.getBlock() instanceof CampfireBlock) {
			return CampfireBlock.canLight(state) ? Optional.of(state.setValue(BlockStateProperties.LIT, true)) : Optional.empty();
		}
		if (!candle(state) || state.getValue(AbstractCandleBlock.LIT)) return Optional.empty();
		if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
			return Optional.empty();
		}
		return Optional.of(state.setValue(AbstractCandleBlock.LIT, true));
	}

	/** The state {@code state} takes when put out, when it is a lit campfire, candle or candle cake. */
	public static Optional<BlockState> snuffed(BlockState state) {
		if (state.getBlock() instanceof CampfireBlock) {
			return state.getValue(CampfireBlock.LIT) ? Optional.of(state.setValue(CampfireBlock.LIT, false)) : Optional.empty();
		}
		return candle(state) && state.getValue(AbstractCandleBlock.LIT)
				? Optional.of(state.setValue(AbstractCandleBlock.LIT, false))
				: Optional.empty();
	}

	private static boolean candle(BlockState state) {
		return (state.is(BlockTags.CANDLES) || state.is(BlockTags.CANDLE_CAKES)) && state.hasProperty(AbstractCandleBlock.LIT);
	}
}
