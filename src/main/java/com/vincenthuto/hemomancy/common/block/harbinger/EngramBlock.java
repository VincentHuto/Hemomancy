package com.vincenthuto.hemomancy.common.block.harbinger;

import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggableBlock;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
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

	protected static final VoxelShape FLOOR_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 0.05, 16.0);
	protected static final VoxelShape WALL_NORTH_SHAPE = Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape WALL_SOUTH_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0);
	protected static final VoxelShape WALL_EAST_SHAPE = Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0);
	protected static final VoxelShape WALL_WEST_SHAPE = Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	public static final IntegerProperty CHARACTERINDEX = IntegerProperty.create("character", 0, 25);
	public static final BooleanProperty LIT = BooleanProperty.create("lit");
	public static final DirectionProperty FACING = BlockStateProperties.FACING;

	public EngramBlock() {
		super(BlockBehaviour.Properties.of().mapColor(DyeColor.RED).strength(0.1F).noCollission().noOcclusion()
				.sound(SoundType.HONEY_BLOCK), false);
		var r = (int) (Math.random() * 26);
		this.registerDefaultState(this.defaultBlockState().setValue(CHARACTERINDEX, r).setValue(LIT, false)
				.setValue(FACING, Direction.UP));
	}

	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(new Property[] { CHARACTERINDEX });
		builder.add(LIT);
		builder.add(FACING);

	}

	private InteractionResult handleInteraction(BlockState state, Level worldIn, BlockPos pos, Player player,
			ItemStack stack) {
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

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player,
			BlockHitResult result) {
		return handleInteraction(state, worldIn, pos, player, ItemStack.EMPTY);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos,
			Player player, InteractionHand handIn, BlockHitResult result) {
		handleInteraction(state, worldIn, pos, player, stack);
		return ItemInteractionResult.SUCCESS;
	}

	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case NORTH -> WALL_NORTH_SHAPE;
			case SOUTH -> WALL_SOUTH_SHAPE;
			case EAST -> WALL_EAST_SHAPE;
			case WEST -> WALL_WEST_SHAPE;
			default -> FLOOR_SHAPE;
		};
	}

	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.IGNORE;
	}

	public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
		return 15;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction facing = context.getClickedFace();
		if (facing == Direction.DOWN) {
			return null;
		}
		if (!facing.getAxis().isHorizontal()) {
			facing = Direction.UP;
		}
		BlockState base = super.getStateForPlacement(context);
		if (base == null) {
			return null;
		}
		return base.setValue(LIT, false).setValue(FACING, facing).setValue(CHARACTERINDEX, randomCharacterIndex());
	}

	@Override
	public BlockState mirror(BlockState pState, Mirror pMirror) {
		return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
	}

	@Override
	public BlockState rotate(BlockState pState, Rotation pRotation) {
		return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
	}

	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {

	}

	@Override
	protected boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
		Direction facing = state.getValue(FACING);
		if (facing == Direction.UP) {
			BlockPos supportPos = pos.below();
			return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, Direction.UP);
		}
		if (!facing.getAxis().isHorizontal()) {
			return false;
		}
		BlockPos supportPos = pos.relative(facing.getOpposite());
		return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		BlockState updated = super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
		return updated.canSurvive(worldIn, currentPos) ? updated : Blocks.AIR.defaultBlockState();
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		super.animateTick(state, level, pos, random);
		if (random.nextInt(10) == 0) {
			Vec3 particlePos = randomSurfacePos(state, pos, random);

			level.addParticle(BloodCellParticleFactory.createData(ParticleColor.BLOOD),
					particlePos.x, particlePos.y, particlePos.z, 0.0D, 0.0D, 0.0D);
		}
		if (state.getValue(LIT)) {
			Vec3 particlePos = randomSurfacePos(state, pos, random);
			level.addParticle(GlowParticleFactory.createData(ParticleColor.BLOOD),
					particlePos.x, particlePos.y, particlePos.z, 0.0D, 0.0D, 0.0D);

		}

	}

	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
		// Lit engrams emit full signal; unlit emit nothing.
		// (MnA-compat note: when MnA NeoForge 1.21.1 ships, also return 15 for a
		// non-empty ChalkRuneTile regardless of the LIT state.)
		return blockState.getValue(LIT) ? 15 : 0;
	}

	public static int randomCharacterIndex() {
		return (int) Math.floor(Math.random() * CHARACTERINDEX.getPossibleValues().size());
	}

	private static Vec3 randomSurfacePos(BlockState state, BlockPos pos, RandomSource random) {
		double x = pos.getX() + random.nextDouble();
		double y = pos.getY() + random.nextDouble();
		double z = pos.getZ() + random.nextDouble();
		double offset = 0.0125D;
		return switch (state.getValue(FACING)) {
			case NORTH -> new Vec3(x, y, pos.getZ() + 1.0D - offset);
			case SOUTH -> new Vec3(x, y, pos.getZ() + offset);
			case EAST -> new Vec3(pos.getX() + offset, y, z);
			case WEST -> new Vec3(pos.getX() + 1.0D - offset, y, z);
			default -> new Vec3(x, pos.getY() + 0.1D, z);
		};
	}
}
