package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.item.harbinger.QliphothPomeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.QliphothPomeRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSicklePruning;
import com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.SeveredQliphothState;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.QliphothBloomBlockEntity;
import com.vincenthuto.hemomancy.common.worldgen.VesperOrdealManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * The Qliphoth Bloom block, a 1x1x8 multi-block structure placed by the
 * Bloom of the Qliphoth cardinal rite.
 */
public class QliphothBloomBlock extends BaseEntityBlock implements IMultiBlock {
	public static final MapCodec<QliphothBloomBlock> CODEC = simpleCodec(QliphothBloomBlock::new);

	private static final BlockPos[] FILLER_OFFSETS = new BlockPos[] {
			new BlockPos(0, 1, 0),
			new BlockPos(0, 2, 0),
			new BlockPos(0, 3, 0),
			new BlockPos(0, 4, 0),
			new BlockPos(0, 5, 0),
			new BlockPos(0, 6, 0),
			new BlockPos(0, 7, 0)
	};
	private static final VoxelShape SHAPE = Shapes.block();

	public QliphothBloomBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockPos[] getFillerOffsets() {
		return FILLER_OFFSETS;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = (Level) context.getLevel();
		if (pos.getY() + 7 <= level.getMaxBuildHeight() && canPlaceMultiBlock(level, pos)) {
			return this.defaultBlockState();
		}
		return null;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state,
			@Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!level.isClientSide) {
			placeFillers(level, pos, state);
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new QliphothBloomBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		return pickPendingPome(level, pos, player);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (LivingSicklePruning.interact(level, pos, player, hand)) {
			return ItemInteractionResult.SUCCESS;
		}
		return pickPendingPome(level, pos, player) == InteractionResult.SUCCESS
				? ItemInteractionResult.SUCCESS
				: ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public void attack(BlockState state, Level level, BlockPos pos, Player player) {
		LivingSicklePruning.interact(level, pos, player, InteractionHand.MAIN_HAND);
		super.attack(state, level, pos, player);
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			if (!level.isClientSide) {
				removeFillers(level, pos);
				HarbingerCardinalRiteEvents.removeBloomAt(level, pos);
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return null;
	}

	private InteractionResult pickPendingPome(Level level, BlockPos pos, Player player) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.PASS;
		}

		QliphothBloomSavedData data = QliphothBloomSavedData.get(serverLevel.getServer().overworld());
		QliphothBloomSavedData.BloomEntry bloom = data.getBloomAt(pos, level.dimension().location().toString());
		if (bloom == null || !bloom.center().equals(pos)) {
			return InteractionResult.PASS;
		}
		SeveredQliphothState bloomState = data.getState(pos);
		if (!bloom.ownerUUID().equals(player.getUUID())) {
			player.displayClientMessage(Component.literal(bloomState == SeveredQliphothState.LIVING
					? "The fruit tightens against another covenant."
					: "The wound has no memory of you.")
					.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
			return InteractionResult.SUCCESS;
		}
		if (bloomState.isSealedTrophy()) {
			player.displayClientMessage(Component.literal("The severed scar is still. Your refusal is already written here.")
					.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
			return InteractionResult.SUCCESS;
		}
		if (bloomState.isPortalOpen()) {
			if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
					|| !HemoCapabilityAccess.getInitiatoryDegree(player)
							.map(degree -> degree.getDegreeNumber() == 7
									&& degree.getArchonPath() == EnumArchonPath.SILENT_PENDING)
							.orElse(false)) {
				player.displayClientMessage(Component.literal("The wound rejects the path you carry.")
						.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
				return InteractionResult.SUCCESS;
			}
			VesperOrdealManager.enter(serverPlayer, bloom);
			return InteractionResult.SUCCESS;
		}
		int huskIndex = data.getPendingPomeHuskIndex(pos);
		boolean claimed = data.isPendingPomeClaimed(pos);
		if (!QliphothPomeRules.canPickPendingPome(huskIndex >= 0, claimed)) {
			Component message = claimed
					? Component.literal("The tree has already given you its pome. Eat it before the next can grow.")
					: Component.literal("No Qliphoth pome is ripe enough to be taken.");
			player.displayClientMessage(message.copy()
					.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
			return InteractionResult.SUCCESS;
		}

		ItemStack pome = QliphothPomeItem.createPickedPomeStack(pos.asLong(), huskIndex, bloom.ownerUUID());
		if (!player.getInventory().add(pome)) {
			player.displayClientMessage(Component.literal("The ripe pome waits. Make room before you claim it.")
					.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
			return InteractionResult.SUCCESS;
		}

		data.markPendingPomeClaimed(pos);
		player.displayClientMessage(Component.literal("The Qliphoth pome comes away warm. Eat it before the next can grow.")
				.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
		return InteractionResult.SUCCESS;
	}
}
