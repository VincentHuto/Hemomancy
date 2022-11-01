package com.vincenthuto.hemomancy.block;

import java.util.stream.Stream;

import com.vincenthuto.hemomancy.tile.DendriticDistributorBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DendriticDistributorBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE_N = Stream
			.of(Block.box(5, 0, 5, 11, 2, 11), Block.box(7, 2, 7, 9, 16, 9), Block.box(4.5, 0, 4.5, 11.5, 1, 11.5),
					Block.box(6, 12.7, 6, 10, 16.7, 10), Block.box(6.5, 2.7, 6.5, 9.5, 11.8, 9.5),
					Block.box(5, 13.25, 6.5, 6, 16.25, 9.5), Block.box(10, 13.25, 6.5, 11, 16.25, 9.5),
					Block.box(6.5, 16.75, 6.5, 9.5, 17.75, 9.5), Block.box(6.5, 13.25, 5, 9.5, 16.25, 6),
					Block.box(6.5, 13.25, 10, 9.5, 16.25, 11))
			.reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

	public DendriticDistributorBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));

	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
			BlockHitResult result) {
//		if (worldIn.isClientSide)
//			return InteractionResult.PASS;
//		worldIn.playSound(player, pos, SoundEvents.ZOMBIE_AMBIENT, SoundSource.BLOCKS, 0.25f, 1f);
//		List<LivingEntity> ents = worldIn.getEntitiesOfClass(LivingEntity.class,
//				worldIn.getBlockEntity(pos).getRenderBoundingBox().inflate(5));
//		if (!ents.isEmpty()) {
//			for (LivingEntity ent : ents) {
//				if (ent instanceof EntityDrudge) {
//					EntityDrudge drudge = (EntityDrudge) ent;
//					ServerPlayer sPlay = (ServerPlayer) player;
//					player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 100));
//					sPlay.setCamera(drudge);
//					player.displayClientMessage(Component.literal(drudge.getRoleTitle().name()), false);
//
//				}
//			}
//		}

		return InteractionResult.SUCCESS;

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
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@SuppressWarnings("deprecation")
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
		super.attack(state, worldIn, pos, player);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos arg0, BlockState arg1) {
		return new DendriticDistributorBlockEntity(arg0, arg1);
	}

}
