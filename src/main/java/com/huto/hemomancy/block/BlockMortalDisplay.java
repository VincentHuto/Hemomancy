package com.huto.hemomancy.block;

import java.util.stream.Stream;

import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.huto.hemomancy.tile.TileEntityMortalDisplay;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.common.network.HutosLibPacketHandler;
import com.mojang.math.Vector3d;

import net.minecraft.block.HorizontalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class BlockMortalDisplay extends Block {
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	private static final VoxelShape SHAPE_N = Stream.of(Block.makeCuboidShape(5, 2, 5, 11, 7, 11),
			Block.makeCuboidShape(11, 2, 5, 12, 6, 11), Block.makeCuboidShape(5, 2, 4, 11, 6, 5),
			Block.makeCuboidShape(4, 2, 5, 5, 6, 11), Block.makeCuboidShape(5, 2, 11, 11, 6, 12),
			Block.makeCuboidShape(4, 0, 4, 12, 2, 12), Block.makeCuboidShape(5.5, 6.75, 5.5, 10.5, 7.75, 10.5))
			.reduce((v1, v2) -> {
				return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);
			}).get();

	public BlockMortalDisplay(Properties properties) {
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.SOUTH));

	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult result) {
		if (!worldIn.isClientSide) {

			IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
			if (!volume.isActive()) {
				volume.setActive(true);
				PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
						new PacketBloodVolumeServer(volume));
				player.sendStatusMessage(
						new StringTextComponent("Activated Blood Control!").mergeStyle(TextFormatting.DARK_RED), true);

				for (int i = 0; i < 10; i++) {
					Vector3d startVec = new Vector3d(pos.getX(), pos.getY(), pos.getZ()).add(0.5, 0.5, 0.5);
					Vector3d endVec = player.getPositionVec().add(0, player.getHeight() - worldIn.rand.nextDouble(), 0)
							.add(worldIn.rand.nextDouble() - worldIn.rand.nextDouble(), 0,
									worldIn.rand.nextDouble() - worldIn.rand.nextDouble());
					PacketHandler.sendClawParticles(endVec, ParticleColor.BLOOD, 64f,
							(RegistryKey<World>) worldIn.getDimensionKey());
					HutosLibPacketHandler.sendLightningSpawn(startVec, endVec, 64.0f,
							(RegistryKey<World>) player.world.getDimensionKey(), ParticleColor.RED, 2, 20, 9, 1.2f);
				}

			} else {
				ItemEntity drops = new ItemEntity(worldIn, pos.getX(), pos.getY(), pos.getZ(),
						new ItemStack(ItemInit.bloody_flask.get(), worldIn.rand.nextInt(4)));
				worldIn.createExplosion(player, pos.getX(), pos.getY(), pos.getZ(), 4.0F, Explosion.Mode.BREAK);
				worldIn.addEntity(drops);
			}

		}

		return ActionResultType.SUCCESS;

	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE_N;
	}

	@Override
	public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.with(FACING, rot.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.toRotation(state.get(FACING)));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
		super.onBlockClicked(state, worldIn, pos, player);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new TileEntityMortalDisplay();
	}

}
