package com.vincenthuto.hemomancy.block;

import java.util.stream.Stream;

import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.tile.BlockEntityIronBrazier;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockBrazier extends Block implements EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty LIT = BooleanProperty.create("lit");

	private static final VoxelShape SHAPE_N = Stream.of(
			Block.box(4, 0, 4, 12, 2, 12),
			Block.box(5, 2, 5, 11, 4, 11),
			Block.box(6, 4, 6, 10, 13, 10),
			Block.box(5, 13, 5, 11, 16, 11),
			Block.box(6, 15, 6, 10, 17, 10),
			Block.box(5, 14, 11, 11, 17, 12),
			Block.box(11, 14, 5, 12, 17, 11),
			Block.box(5, 14, 4, 11, 17, 5),
			Block.box(4, 14, 5, 5, 17, 11)
			).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

	public BlockBrazier(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(LIT, false));

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
				player.hurt(ItemInit.bloodLoss, 1.5f);
				if (!worldIn.isClientSide) {
					HLParticleUtils.spawnPoof((ServerLevel) worldIn, pos,
							BloodCellParticleFactory.createData(ParticleColor.BLOOD));
				}
			}
		} else {
			if (stack.isEmpty()) {
				BlockState newState = state.setValue(LIT, false);
				worldIn.setBlock(pos, newState, 10);
				player.hurt(ItemInit.bloodLoss, 1f);
				if (!worldIn.isClientSide) {
					HLParticleUtils.spawnPoof((ServerLevel) worldIn, pos, ParticleTypes.SMOKE);
				}
			}
		}

		return InteractionResult.SUCCESS;

	}

	@Override
	public RenderShape getRenderShape(BlockState p_60550_) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE_N;
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(LIT,
				false);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING))).setValue(LIT, false);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING))).setValue(LIT, false);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}

	@Override
	public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
		super.attack(state, worldIn, pos, player);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos arg0, BlockState arg1) {
		return new BlockEntityIronBrazier(arg0, arg1);
	}

}
