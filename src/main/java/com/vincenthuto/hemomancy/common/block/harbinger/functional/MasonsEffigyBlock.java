package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.IScars;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarType;
import com.vincenthuto.hemomancy.common.block.shared.BlockBloodEndpoint;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ScarInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.common.menu.tile.functional.MasonsEffigyMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.scars.PacketSyncScarsState;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import com.vincenthuto.hemomancy.common.tile.functional.MasonsEffigyBlockEntity;
import com.vincenthuto.hutoslib.common.network.VanillaPacketDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class MasonsEffigyBlock extends Block implements EntityBlock, SimpleWaterloggedBlock, BlockBloodEndpoint {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private static final VoxelShape SHAPE = Shapes.or(Block.box(2, 0, 2, 14, 3, 14),
			Block.box(4, 3, 4, 12, 13, 12), Block.box(3, 13, 3, 13, 16, 13));

	public MasonsEffigyBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(WATERLOGGED, false));
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState()
				.setValue(FACING, context.getHorizontalDirection().getOpposite())
				.setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(context));
	}

	private InteractionResult open(Level level, BlockPos pos, Player player) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof MasonsEffigyBlockEntity effigy && player instanceof ServerPlayer serverPlayer) {
			effigy.updateStateFor(player);
			HemoCapabilityAccess.getScarState(player)
					.ifPresent(scars -> PacketHandler.sendToPlayer(serverPlayer, new PacketSyncScarsState(serverPlayer, scars)));
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(level, pos);
			serverPlayer.openMenu(effigy, pos);
		}
		return InteractionResult.CONSUME;
	}

	private InteractionResult placeMotifForPattern(Level level, BlockPos pos, Player player, ItemStack stack) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof MasonsEffigyBlockEntity effigy)) {
			return InteractionResult.PASS;
		}
		List<ResourceLocation> selected = new ArrayList<>(effigy.getSelectedScarIds());
		if (selected.isEmpty()) {
			message(player, "Select scars on the Effigy before pressing motif paper to it.", ChatFormatting.RED);
			return InteractionResult.CONSUME;
		}
		if (effigy.hasPendingMotif()) {
			message(player, "The Effigy is already holding a motif. Feed it with Blood Projection.", ChatFormatting.GOLD);
			return InteractionResult.CONSUME;
		}
		int max = Math.min(MasonsEffigyMenu.MAX_SELECTED_SCARS,
				com.vincenthuto.hemomancy.common.rite.ScarBrazierRite.getMaxActiveScars(player));
		if (selected.size() > max) {
			message(player, "That pattern exceeds your current scar capacity.", ChatFormatting.RED);
			return InteractionResult.CONSUME;
		}
		IScars scars = HemoCapabilityAccess.getScarState(player).orElse(null);
		if (scars == null) {
			message(player, "Your scar memory is silent.", ChatFormatting.RED);
			return InteractionResult.CONSUME;
		}
		for (ResourceLocation id : selected) {
			ScarDefinition definition = ScarInit.getByName(id.toString());
			if (definition == null || definition.getScarType() != ScarType.CEREBRAL || !scars.knowsCerebralScar(id)) {
				message(player, "The Effigy rejects an unknown scar: " + id.getPath(), ChatFormatting.RED);
				return InteractionResult.CONSUME;
			}
		}
		if (!effigy.beginMotifRitual(player, stack)) {
			message(player, "The Effigy will not take the motif yet.", ChatFormatting.RED);
			return InteractionResult.CONSUME;
		}
		message(player, "The motif settles on the Effigy. Feed it with Blood Projection.", ChatFormatting.DARK_RED);
		return InteractionResult.CONSUME;
	}

	private static void message(Player player, String text, ChatFormatting color) {
		player.displayClientMessage(Component.literal(text).withStyle(color), true);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult result) {
		return open(level, pos, player);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult result) {
		if (isBloodTool(stack)) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (stack.is(ItemInit.runic_motif_paper.get())) {
			return placeMotifForPattern(level, pos, player, stack) == InteractionResult.PASS
					? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
					: ItemInteractionResult.SUCCESS;
		}
		open(level, pos, player);
		return ItemInteractionResult.SUCCESS;
	}

	private static boolean isBloodTool(ItemStack stack) {
		return stack.getItem() instanceof BloodAbsorptionItem
				|| stack.getItem() instanceof BloodProjectionItem
				|| stack.getItem() instanceof LivingStaffItem;
	}

	@Override
	public double absorbBloodFromBlock(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
			double maxAmount) {
		return 0.0D;
	}

	@Override
	public double projectBloodIntoBlock(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
			double maxAmount) {
		BlockEntity be = level.getBlockEntity(pos);
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (!(be instanceof MasonsEffigyBlockEntity effigy) || volume == null) {
			return 0.0D;
		}
		double beforeBlood = volume.getBloodVolume();
		boolean livingStaff = player.getMainHandItem().getItem() instanceof LivingStaffItem
				|| player.getOffhandItem().getItem() instanceof LivingStaffItem;
		double accepted = effigy.receiveProjectedBlood(player, maxAmount, livingStaff);
		if (accepted > 0.0D) {
			BloodVolumeEvents.syncVolume(player, volume);
			return Math.max(accepted, beforeBlood - volume.getBloodVolume());
		}
		return 0.0D;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MasonsEffigyBlockEntity(pos, state);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return WaterloggedBlockSupport.fluidState(state);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		WaterloggedBlockSupport.scheduleWaterTick(state, level, pos);
		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED);
	}
}
