package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A catalyst crop (core-loop round two, P7): a four-stage crop whose harvest is its own seed, like a potato, and a weak
 * Temper catalyst of one school. It is an ordinary {@link CropBlock} (farmland, light, the crop hook, the cloche) but it
 * is tagged {@code needs_blood_to_grow}, so it advances only on fed ground.
 */
public class CatalystCropBlock extends CropBlock {
	public static final int MAX_AGE = 3;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
	public static final MapCodec<CatalystCropBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("seed").forGetter(block -> block.seed),
			propertiesCodec()).apply(instance, CatalystCropBlock::new));
	private static final VoxelShape[] SHAPE_BY_AGE = {
			Block.box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D),
			Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D),
			Block.box(0.0D, 0.0D, 0.0D, 16.0D, 9.0D, 16.0D),
			Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D)};

	private final ResourceLocation seed;

	public CatalystCropBlock(ResourceLocation seed, BlockBehaviour.Properties properties) {
		super(properties);
		this.seed = seed;
	}

	@Override
	public MapCodec<CatalystCropBlock> codec() {
		return CODEC;
	}

	@Override
	protected IntegerProperty getAgeProperty() {
		return AGE;
	}

	@Override
	public int getMaxAge() {
		return MAX_AGE;
	}

	@Override
	protected ItemLike getBaseSeedId() {
		return BuiltInRegistries.ITEM.get(seed);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE_BY_AGE[getAge(state)];
	}
}
