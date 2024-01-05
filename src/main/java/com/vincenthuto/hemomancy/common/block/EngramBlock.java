package com.vincenthuto.hemomancy.common.block;

import com.mna.blocks.tileentities.ChalkRuneTile;
import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hutoslib.client.particle.factory.EmberParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EngramBlock extends WaterloggableBlock {

	protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 0.05, 16.0);
	public static final IntegerProperty CHARACTERINDEX = IntegerProperty.create("character", 0, 25);

	public EngramBlock() {
		super(BlockBehaviour.Properties.of().mapColor(DyeColor.RED).strength(0.1F).noCollission().noOcclusion()
				.sound(SoundType.HONEY_BLOCK), false);
		var r = (int) (Math.random() * 26);
		this.registerDefaultState(this.defaultBlockState().setValue(CHARACTERINDEX, r));
	}

	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(new Property[] { CHARACTERINDEX });
	}

	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.IGNORE;
	}

	public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
		return 3;
	}

	@Override
	public void onPlace(BlockState p_60566_, Level p_60567_, BlockPos p_60568_, BlockState p_60569_, boolean p_60570_) {
		super.onPlace(p_60566_, p_60567_, p_60568_, p_60569_, p_60570_);
		var r = (int) (Math.random() * 26);
		p_60566_.setValue(CHARACTERINDEX, r);
	}

	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (!worldIn.isClientSide && fromPos.equals(pos.below())
				&& !worldIn.getBlockState(fromPos).isSolidRender(worldIn, fromPos)) {
			worldIn.destroyBlock(pos, true);
		}

	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		super.animateTick(state, level, pos, random);
		if (random.nextInt(10) == 0) {
			Vec3 translation = new Vec3(0, .5, 0);
			Vec3 target = pos.above().above().getCenter();
			Vec3 speedVec = new Vec3(target.x, target.y, target.z);

			level.addParticle(BloodCellParticleFactory.createData(ParticleColor.BLOOD),
					(double) pos.getX() + random.nextDouble(), (double) pos.getY() + 0.1D,
					(double) pos.getZ() + random.nextDouble(), 0.0D, 0.0D, 0.0D);
		}

	}

	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
		BlockEntity te = worldIn.getBlockEntity(pos);
		return te != null && te instanceof ChalkRuneTile && !((ChalkRuneTile) te).getDisplayedItem().isEmpty() ? 15 : 0;
	}

	public boolean canPlaceLiquid(BlockGetter worldIn, BlockPos pos, BlockState state, Fluid fluidIn) {
		return super.canPlaceLiquid(worldIn, pos, state, fluidIn);
	}

	public boolean placeLiquid(LevelAccessor worldIn, BlockPos pos, BlockState state, FluidState fluidStateIn) {
		return super.placeLiquid(worldIn, pos, state, fluidStateIn);
	}
}
