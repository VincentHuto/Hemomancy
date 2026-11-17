package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.world.farm.BloodFedGround;
import com.vincenthuto.hemomancy.common.tile.harbinger.plant.PlantingBasinBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoTickers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.common.util.TriState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Tier 2 of fed ground (core-loop Track 7): a shallow venous-stone bed of soil for one plant, with a small tank under
 * it. Crops, stems and the gourd take root in it as in farmland (it never dries or tramples). Fed by hand or by a
 * worked gutter beside it, it feeds the plant on it, and the soil beside it, at level 2 while its blood is fresh; then
 * it clots like any inert tank. {@link #FED} shows the soaked bed.
 */
public class PlantingBasinBlock extends BaseEntityBlock {
	public static final MapCodec<PlantingBasinBlock> CODEC = simpleCodec(PlantingBasinBlock::new);
	public static final BooleanProperty FED = BooleanProperty.create("fed");
	/** As farmland: the soil stands a pixel below the block's top, so a crop's roots sit in it. */
	private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 15, 16);

	public PlantingBasinBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FED, false));
	}

	@Override
	protected MapCodec<PlantingBasinBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FED);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}

	/** What the basin holds: the crops and stems in {@code hemomancy:planting_basin_plants}, planted on its top. */
	@Override
	public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
		if (facing != Direction.UP) return TriState.DEFAULT;
		return plant.is(BloodFedGround.PLANTING_BASIN_PLANTS) ? TriState.TRUE : TriState.DEFAULT;
	}

	/** Like moist farmland, but only while blood stands in it. */
	@Override
	public boolean isFertile(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getValue(FED);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PlantingBasinBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null
				: HemoTickers.create(type, BlockEntityInit.planting_basin.get(), PlantingBasinBlockEntity::serverTick);
	}
}
