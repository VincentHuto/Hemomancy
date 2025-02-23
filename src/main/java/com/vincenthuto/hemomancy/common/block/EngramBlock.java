package com.vincenthuto.hemomancy.common.block;

import com.mna.blocks.tileentities.ChalkRuneTile;
import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EngramBlock extends WaterloggableBlock {

	protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 0.05, 16.0);
	public static final IntegerProperty CHARACTERINDEX = IntegerProperty.create("character", 0, 25);
	public static final BooleanProperty LIT = BooleanProperty.create("lit");

	public EngramBlock() {
		super(BlockBehaviour.Properties.of().mapColor(DyeColor.RED).strength(0.1F).noCollission().noOcclusion()
				.sound(SoundType.HONEY_BLOCK), false);
		var r = (int) (Math.random() * 26);
		this.registerDefaultState(this.defaultBlockState().setValue(CHARACTERINDEX, r).setValue(LIT, false));
	}

	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(new Property[] { CHARACTERINDEX });
		builder.add(LIT);

	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
			BlockHitResult result) {
		ItemStack stack = player.getItemInHand(handIn);
		if (!state.getValue(LIT)) {
			if (stack.getItem() == Items.FLINT_AND_STEEL || stack.getItem() == Items.FIRE_CHARGE) {
				BlockState newState = state.setValue(LIT, true);
				worldIn.setBlock(pos, newState, 10);
			}
			if (stack.isEmpty()) {
				BlockState newState = state.setValue(LIT, true);
				worldIn.setBlock(pos, newState, 10);
				player.hurt(player.damageSources().generic(), 1.5f);
				if (!worldIn.isClientSide) {
					HLParticleUtils.spawnPoof((ServerLevel) worldIn, pos,
							BloodCellParticleFactory.createData(ParticleColor.BLOOD));
				}
			}
		} else {
			if (stack.isEmpty()) {
				BlockState newState = state.setValue(LIT, false);
				worldIn.setBlock(pos, newState, 10);
				player.hurt(player.damageSources().generic(), 1f);
				if (!worldIn.isClientSide) {
					HLParticleUtils.spawnPoof((ServerLevel) worldIn, pos, ParticleTypes.SMOKE);
					
				}
			}
		}

		return InteractionResult.SUCCESS;

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
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(LIT, false);
	}

	@Override
	public BlockState mirror(BlockState pState, Mirror pMirror) {
		return super.mirror(pState, pMirror).setValue(LIT, false);
	}

	@Override
	public BlockState rotate(BlockState pState, Rotation pRotation) {
		return super.rotate(pState, pRotation).setValue(LIT, false);
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
		double d0 = (double) pos.getX() + 0.5D;
		double d1 = (double) pos.getY() + 1.2D;
		double d2 = (double) pos.getZ() + 0.5D;
		if (state.getValue(LIT)) {
		//	level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
			level.addParticle(GlowParticleFactory.createData(ParticleColor.BLOOD),
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
