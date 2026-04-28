package com.vincenthuto.hemomancy.common.block.functional;

import java.util.List;
import java.util.stream.Stream;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.npc.DrudgeEntity;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.item.tool.DrudgeElectrodeItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.tile.functional.SemiSentientConstructBlockEntity;
import com.vincenthuto.hemomancy.config.HemoServerConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SemiSentientConstructBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE_N = Stream
			.of(Block.box(2.1999999999999993, 0.2999999999999998, 2.1999999999999993, 13.8, 6.2, 13.8),
					Block.box(4, 6, 4, 12, 12, 12),
					Block.box(2.0999999999999996, 5.800000000000001, 2.0999999999999996, 13.9, 15.8, 13.9),
					Block.box(1.6999999999999993, 0, 1.6999999999999993, 14.3, 5.8, 14.3))
			.reduce((v1, v2) -> {
				return Shapes.join(v1, v2, BooleanOp.OR);
			}).get();

	public SemiSentientConstructBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));

	}

	@Override
	public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
		super.attack(state, worldIn, pos, player);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE_N;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@SuppressWarnings("deprecation")
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return type == BlockEntityInit.semi_sentient_construct.get()
				? (lvl, pos, blockState, be) -> ((SemiSentientConstructBlockEntity) be).tick()
				: null;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos arg0, BlockState arg1) {
		return new SemiSentientConstructBlockEntity(arg0, arg1);
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	/**
	 * Handles a bare right-click (no held item) — shows the status of all Drudges
	 * bound to this SSC.
	 */
	private InteractionResult handleInteraction(Level worldIn, BlockPos pos, Player player) {
		worldIn.playSound(player, pos, SoundEvents.ZOMBIE_AMBIENT, SoundSource.BLOCKS, 0.25f, 1f);

		if (!worldIn.isClientSide) {
			// Display status of bound drudges
			AABB searchBox = new AABB(pos).inflate(64.0);
			List<DrudgeEntity> drudges = worldIn.getEntitiesOfClass(DrudgeEntity.class, searchBox,
					d -> pos.equals(d.getHomePos()));

			if (drudges.isEmpty()) {
				player.displayClientMessage(Component.literal("§8No constructs are bound to this station."), false);
			} else {
				player.displayClientMessage(Component.literal("§7=== SSC Status ==="), false);
				for (DrudgeEntity drudge : drudges) {
					String memStr = drudge.getEquippedMemory() != null
							? "§c" + drudge.getEquippedMemory().getProperName()
							: "§8none";
					String modeStr = drudge.isPassiveMode() ? "§aPassive" : "§eCommanded";
					String stateStr = drudge.isRogue() ? "§4ROGUE" : modeStr;
					player.displayClientMessage(Component.literal(
							"§7Construct — Memory: " + memStr
							+ " §7| Mode: " + stateStr
							+ " §7| Charge: §c" + (int) drudge.getBloodCharge()
							+ "/" + (int) DrudgeEntity.MAX_BLOOD_CHARGE + " mL"), false);
				}
			}

			// Also show SSC blood level
			if (worldIn.getBlockEntity(pos) instanceof SemiSentientConstructBlockEntity ssc) {
				IBloodVolume sscVol = HemoCapabilityAccess.getBloodVolume(ssc).orElse(null);
				if (sscVol != null) {
					player.displayClientMessage(Component.literal(
							"§7SSC Blood: §c" + (int) sscVol.getBloodVolume()
							+ "§7/§c" + (int) sscVol.getMaxBloodVolume() + " mL"), false);
				}
			}
		}

		return InteractionResult.SUCCESS;
	}

	/**
	 * Handles a Drudge Electrode used on the SSC while in ON mode.
	 * Spends blood from the player's reserve and spawns a new Drudge.
	 */
	private ItemInteractionResult handleElectrode(ItemStack stack, Level worldIn, BlockPos pos, Player player) {
		if (worldIn.isClientSide) return ItemInteractionResult.SUCCESS;

		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		boolean modeOn = tag.getBoolean(DrudgeElectrodeItem.TAG_MODE);
		if (!modeOn) {
			// Electrode is off — treat as plain right-click
			handleInteraction(worldIn, pos, player);
			return ItemInteractionResult.SUCCESS;
		}

		// ── Degree gate ─────────────────────────────────────────────────────
		int requiredDegree = HemoServerConfig.DRUDGE_REQUIRED_DEGREE.get();
		int playerDegree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		if (playerDegree < requiredDegree) {
			player.displayClientMessage(Component.literal(
					"§cThe Order has not entrusted you with this rite. (Degree " + requiredDegree + " required)"),
					true);
			return ItemInteractionResult.FAIL;
		}

		// ── SSC drudge cap ───────────────────────────────────────────────────
		if (!(worldIn.getBlockEntity(pos) instanceof SemiSentientConstructBlockEntity ssc)) {
			return ItemInteractionResult.FAIL;
		}
		int maxDrudges = HemoServerConfig.DRUDGE_MAX_PER_SSC.get();
		int bound = ssc.countBoundDrudges();
		if (bound >= maxDrudges) {
			player.displayClientMessage(Component.literal(
					"§4The construct's will is divided enough. §7(" + bound + "/" + maxDrudges + " bound)"),
					true);
			return ItemInteractionResult.FAIL;
		}

		// ── Blood cost ───────────────────────────────────────────────────────
		double birthCost = HemoServerConfig.DRUDGE_BIRTH_COST.get();
		IBloodVolume playerVol = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (playerVol == null || !playerVol.isActive() || playerVol.getBloodVolume() < birthCost) {
			player.displayClientMessage(Component.literal(
					"§4Insufficient blood. §7(" + (int) birthCost + " mL required)"),
					true);
			return ItemInteractionResult.FAIL;
		}

		playerVol.drain(birthCost);
		if (player instanceof ServerPlayer sp) {
			PacketHandler.sendToPlayer(sp, new BloodVolumeServerPacket(playerVol));
		}

		// ── Spawn the Drudge ─────────────────────────────────────────────────
		DrudgeEntity drudge = EntityInit.drudge.get().create(worldIn);
		if (drudge == null) return ItemInteractionResult.FAIL;

		// New Drudges start at half charge — enough for a few actions before needing a refill.
		final float INITIAL_CHARGE_RATIO = 0.5f;
		drudge.setOwnerUUID(player.getUUID());
		drudge.setHomePos(pos);
		drudge.setBloodCharge(DrudgeEntity.MAX_BLOOD_CHARGE * INITIAL_CHARGE_RATIO);
		drudge.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0f, 0f);

		worldIn.addFreshEntity(drudge);
		worldIn.playSound(null, pos, SoundEvents.ZOMBIE_AMBIENT, SoundSource.PLAYERS, 0.5f, 0.8f);

		player.displayClientMessage(Component.literal(
				"§7The construct stirs. A new drudge is bound to this station.").withStyle(ChatFormatting.DARK_RED),
				false);

		return ItemInteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player,
			BlockHitResult result) {
		return handleInteraction(worldIn, pos, player);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos,
			Player player, InteractionHand handIn, BlockHitResult result) {
		if (stack.getItem() instanceof DrudgeElectrodeItem) {
			return handleElectrode(stack, worldIn, pos, player);
		}
		handleInteraction(worldIn, pos, player);
		return ItemInteractionResult.SUCCESS;
	}
}

