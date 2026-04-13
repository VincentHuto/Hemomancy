package com.vincenthuto.hemomancy.common.block.functional;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.capability.player.visceral.EnumOrgan;
import com.vincenthuto.hemomancy.common.capability.player.visceral.IVisceralOrgans;
import com.vincenthuto.hemomancy.common.capability.player.visceral.VisceralOrgansProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.item.OrganEchoItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.visceral.OpenVisceralMirrorPacket;
import com.vincenthuto.hemomancy.common.tile.functional.VisceralMirrorBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;

/**
 * The Visceral Mirror — a ritualistic block that allows the player to gaze into
 * their own reflection, "reach" through the mirror, and extract organs for
 * sanguine modification. Requires initiatory degree 3+.
 *
 * <h3>Interaction Flow</h3>
 * <p>Right-clicking the mirror with an empty hand opens a GUI screen where
 * the player can browse all organs, view their status, and initiate or cancel
 * extraction rituals with a visual progress bar.</p>
 */
public class VisceralMirrorBlock extends Block implements EntityBlock {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	// A tall mirror-like shape (2 blocks tall appearance in 1 block)
	private static final VoxelShape SHAPE = Shapes.or(
			Block.box(1, 0, 1, 15, 2, 15),    // Base pedestal
			Block.box(3, 2, 3, 13, 16, 13)     // Mirror frame
	);

	@Nullable
	@SuppressWarnings("unchecked")
	public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
			BlockEntityType<A> candidate, BlockEntityType<E> desired, BlockEntityTicker<? super E> ticker) {
		return desired == candidate ? (BlockEntityTicker<A>) ticker : null;
	}

	public VisceralMirrorBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return SHAPE;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		if (level.isClientSide) {
			return createTickerHelper(type, BlockEntityInit.visceral_mirror.get(),
					VisceralMirrorBlockEntity::clientTick);
		} else {
			return createTickerHelper(type, BlockEntityInit.visceral_mirror.get(),
					VisceralMirrorBlockEntity::serverTick);
		}
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new VisceralMirrorBlockEntity(pos, state);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int id, int param) {
		super.triggerEvent(state, world, pos, id, param);
		BlockEntity be = world.getBlockEntity(pos);
		return be != null && be.triggerEvent(id, param);
	}

	/**
	 * Player interaction with the Visceral Mirror.
	 *
	 * <p>Right-click with an empty hand opens the GUI screen, which sends
	 * the player all organ data needed to make informed extraction choices.</p>
	 */
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player,
			InteractionHand handIn, BlockHitResult result) {
		if (worldIn.isClientSide) return InteractionResult.SUCCESS;

		BlockEntity tile = worldIn.getBlockEntity(pos);
		if (!(tile instanceof VisceralMirrorBlockEntity te)) return InteractionResult.PASS;

		// Only react to empty-hand interactions
		if (!player.getItemInHand(handIn).isEmpty()) {
			return InteractionResult.PASS;
		}

		if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

		// Gather organ data for the screen
		EnumOrgan[] organs = EnumOrgan.values();
		int[] organLevels = new int[organs.length];
		boolean[] hasEcho = new boolean[organs.length];

		IVisceralOrgans organsCap = player.getCapability(VisceralOrgansProvider.ORGANS_CAPA).orElse(null);
		for (int i = 0; i < organs.length; i++) {
			organLevels[i] = organsCap != null ? organsCap.getOrganLevel(organs[i]) : 0;
			hasEcho[i] = playerHasEcho(player, organs[i]);
		}

		double bloodVol = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.map(IBloodVolume::getBloodVolume).orElse(0.0);
		double maxBloodVol = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.map(IBloodVolume::getMaxBloodVolume).orElse(0.0);
		int degree = player.getCapability(InitiatoryDegreeProvider.DEGREE_CAPA)
				.map(d -> d.getDegreeNumber()).orElse(0);

		// Send the packet to open the screen
		PacketHandler.CHANNELBLOODVOLUME.send(
				PacketDistributor.PLAYER.with(() -> serverPlayer),
				new OpenVisceralMirrorPacket(pos, organLevels, hasEcho, bloodVol, maxBloodVol, degree));

		return InteractionResult.SUCCESS;
	}

	private boolean playerHasEcho(Player player, EnumOrgan organ) {
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack invStack = player.getInventory().getItem(i);
			if (!invStack.isEmpty()
					&& invStack.getItem() instanceof OrganEchoItem echoItem
					&& echoItem.getOrgan() == organ) {
				return true;
			}
		}
		return false;
	}
}
