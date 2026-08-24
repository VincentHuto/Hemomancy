package com.vincenthuto.hemomancy.common.worldgen.carver;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.function.Function;

public class FungalCaveWorldCarver extends CaveWorldCarver {
	public FungalCaveWorldCarver(Codec<CaveCarverConfiguration> codec) {
		super(codec);
	}

	@Override
	protected int getCaveBound() {
		return 28;
	}

	@Override
	protected float getThickness(RandomSource random) {
		return super.getThickness(random) * 1.85F;
	}

	@Override
	protected double getYScale() {
		return 2.25D;
	}

	@Override
	protected void createRoom(CarvingContext context, CaveCarverConfiguration config, ChunkAccess chunk,
			Function<BlockPos, Holder<Biome>> biomeAccessor, Aquifer aquifer, double x, double y, double z,
			float radius, double horizontalVerticalRatio, CarvingMask carvingMask, WorldCarver.CarveSkipChecker skipChecker) {
		super.createRoom(context, config, chunk, biomeAccessor, aquifer, x, y, z, radius * 1.35F,
				horizontalVerticalRatio * 1.1D, carvingMask, skipChecker);
	}

	@Override
	protected boolean carveBlock(CarvingContext context, CaveCarverConfiguration config, ChunkAccess chunk,
			Function<BlockPos, Holder<Biome>> biomeGetter, CarvingMask carvingMask, BlockPos.MutableBlockPos pos,
			BlockPos.MutableBlockPos checkPos, Aquifer aquifer, MutableBoolean reachedSurface) {
		BlockState state = chunk.getBlockState(pos);
		boolean replaceable = this.canReplaceBlock(config, state);
		boolean containsLiquid = !state.getFluidState().isEmpty();
		boolean adjacentLiquid = replaceable && !containsLiquid && touchesLiquid(chunk, pos, checkPos);
		if (!DryFungalCarving.isCandidate(replaceable, containsLiquid, adjacentLiquid)) {
			return false;
		}

		chunk.setBlockState(pos, Blocks.CAVE_AIR.defaultBlockState(), false);
		return true;
	}

	private boolean touchesLiquid(ChunkAccess chunk, BlockPos.MutableBlockPos pos, BlockPos.MutableBlockPos checkPos) {
		for (Direction direction : Direction.values()) {
			checkPos.setWithOffset(pos, direction);
			if (!chunk.getFluidState(checkPos).isEmpty()) {
				return true;
			}
		}
		return false;
	}

}

final class DryFungalCarving {
	private DryFungalCarving() {}

	static boolean isCandidate(boolean replaceable, boolean containsLiquid, boolean adjacentLiquid) {
		return replaceable && !containsLiquid && !adjacentLiquid;
	}
}
