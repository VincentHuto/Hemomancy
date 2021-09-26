package com.vincenthuto.hemomancy.block;

import java.util.stream.Stream;

import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.tile.BlockEntityMortalDisplay;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.network.HLPacketHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockMortalDisplay extends Block implements EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE_N = Stream.of(Block.box(5, 2, 5, 11, 7, 11), Block.box(11, 2, 5, 12, 6, 11),
			Block.box(5, 2, 4, 11, 6, 5), Block.box(4, 2, 5, 5, 6, 11), Block.box(5, 2, 11, 11, 6, 12),
			Block.box(4, 0, 4, 12, 2, 12), Block.box(5.5, 6.75, 5.5, 10.5, 7.75, 10.5)).reduce((v1, v2) -> {
				return Shapes.join(v1, v2, BooleanOp.OR);
			}).get();

	public BlockMortalDisplay(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));

	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
			BlockHitResult result) {

			IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			worldIn.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			if (!volume.isActive()) {
				volume.setActive(true);
				player.displayClientMessage(
						new TextComponent("Activated Blood Control!").withStyle(ChatFormatting.DARK_RED), true);

				for (int i = 0; i < 10; i++) {
					Vec3 startVec = new Vec3(pos.getX(), pos.getY(), pos.getZ()).add(0.5, 0.5, 0.5);
					Vec3 endVec = player.position().add(0, player.getBbHeight() - worldIn.random.nextDouble(), 0).add(
							worldIn.random.nextDouble() - worldIn.random.nextDouble(), 0,
							worldIn.random.nextDouble() - worldIn.random.nextDouble());
					PacketHandler.sendClawParticles(endVec, ParticleColor.BLOOD, 64f, worldIn.dimension());
					HLPacketHandler.sendLightningSpawn(startVec, endVec, 64.0f, player.level.dimension(),
							ParticleColor.RED, 2, 20, 9, 1.2f);
				}

			} else {
				ItemEntity drops = new ItemEntity(worldIn, pos.getX(), pos.getY(), pos.getZ(),
						new ItemStack(ItemInit.bloody_flask.get(), worldIn.random.nextInt(4)));
				worldIn.explode(player, pos.getX(), pos.getY(), pos.getZ(), 4.0F, Explosion.BlockInteraction.BREAK);
				worldIn.addFreshEntity(drops);
			}

		

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

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
		super.attack(state, worldIn, pos, player);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
		return new BlockEntityMortalDisplay(p_153215_, p_153216_);
	}

}
