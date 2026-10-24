package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.QliphothPomeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.QliphothPomeRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSicklePruning;
import com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomEvents;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.SeveredQliphothState;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.QliphothBloomBlockEntity;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import com.vincenthuto.hemomancy.common.encounter.VesperOrdealManager;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
		return pickPendingPome(level, pos, player, true);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (LivingSicklePruning.interact(level, pos, player, hand)) {
			return ItemInteractionResult.SUCCESS;
		}
		return pickPendingPome(level, pos, player, stack.isEmpty()) == InteractionResult.SUCCESS
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

	private InteractionResult pickPendingPome(Level level, BlockPos pos, Player player, boolean emptyHand) {
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
		SeveredQliphothState bloomState = data.getState(bloom);
		if (!bloom.ownerUUID().equals(player.getUUID())) {
			player.displayClientMessage((bloomState == SeveredQliphothState.LIVING ? Component.translatable("message.hemomancy.qliphoth_bloom_block.the_fruit_tightens_against_another_covenant") : Component.translatable("message.hemomancy.qliphoth_bloom_block.the_wound_has_no_memory_of")).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
			return InteractionResult.SUCCESS;
		}
		if (bloomState.isSealedTrophy()) {
			player.displayClientMessage(Component.translatable("message.hemomancy.qliphoth_bloom_block.the_severed_scar_is_still_your").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
			return InteractionResult.SUCCESS;
		}
		if (bloomState.isPortalOpen()) {
			if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
					|| !(ProgressionGates.isDegree(player, 7)
									&& HemoCapabilityAccess.getInitiatoryDegree(player).getArchonPath() == EnumArchonPath.SILENT_PENDING)) {
				player.displayClientMessage(Component.translatable("message.hemomancy.qliphoth_bloom_block.the_wound_rejects_the_path_you").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
				return InteractionResult.SUCCESS;
			}
			VesperOrdealManager.enter(serverPlayer, bloom);
			return InteractionResult.SUCCESS;
		}
		int huskIndex = data.getPendingPomeHuskIndex(bloom);
		boolean claimed = data.isPendingPomeClaimed(bloom);
		int consumedHere = HemoCapabilityAccess.getInitiatoryDegree(player).getPomesConsumedFromBloom(bloom.bloomId(), pos.asLong(),
						bloom.migratesLegacyProgress());
		if (emptyHand && bloomState == SeveredQliphothState.LIVING && player.isShiftKeyDown()
				&& data.getRemainingPomes(bloom) == 0 && consumedHere >= 9
				&& player instanceof ServerPlayer serverPlayer
				&& HemoCapabilityAccess.getInitiatoryDegree(player).isQliphothCommunionDone()) {
			if (player.getInventory().countItem(ItemInit.fungal_spine.get()) > 0) {
				player.displayClientMessage(Component.translatable("message.hemomancy.qliphoth_bloom_block.your_fungal_spine_is_already_with").withStyle(ChatFormatting.DARK_PURPLE), true);
			} else {
				var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
				boolean delivered;
				if (degree.hasFungalSpineGranted()) {
					delivered = player.getInventory().add(new ItemStack(ItemInit.fungal_spine.get()));
				} else {
					QliphothBloomEvents.deliverPendingFungalSpine(serverPlayer);
					delivered = player.getInventory().countItem(ItemInit.fungal_spine.get()) > 0;
				}
				player.displayClientMessage((delivered ? Component.translatable("message.hemomancy.qliphoth_bloom_block.the_spent_husks_return_your_fungal") : Component.translatable("message.hemomancy.qliphoth_bloom_block.make_room_to_reclaim_your_fungal")).withStyle(ChatFormatting.DARK_PURPLE), !delivered);
			}
			return InteractionResult.SUCCESS;
		}
		if (bloomState == SeveredQliphothState.LIVING && claimed && huskIndex >= 0
				&& consumedHere == huskIndex + 1) {
			data.clearPendingPome(bloom);
			player.displayClientMessage(Component.translatable("message.hemomancy.qliphoth_bloom_block.your_covenant_remembers_eating_this_husk").withStyle(ChatFormatting.DARK_PURPLE), false);
			return InteractionResult.SUCCESS;
		}
		if (!QliphothPomeRules.canPickPendingPome(huskIndex >= 0, claimed)) {
			Component message = claimed
					? Component.translatable("message.hemomancy.qliphoth_bloom_block.the_tree_has_already_given_you")
					: HemoText.translatable("message.hemomancy.qliphoth_bloom_block.the_next_pome_is_growing_return",
							data.getRemainingPomes(bloom));
			if (!claimed && data.getRemainingPomes(bloom) == 0) {
				EnumArchonPath path = HemoCapabilityAccess.getInitiatoryDegree(player).getArchonPath();
				message = Component.translatable(switch (path) {
					case NONE -> "message.hemomancy.qliphoth_bloom_block.spine_visit_the_gardens_lost_it";
					case SILENT_PENDING -> "message.hemomancy.qliphoth_bloom_block.your_refusal_awaits_its_act_sever";
					case APOTHEOS_PENDING -> "message.hemomancy.qliphoth_bloom_block.the_tree_has_fulfilled_its_nine";
					case SILENT_ARCHON, APOTHEOS -> "message.hemomancy.qliphoth_bloom_block.the_nine_husk_covenant_is_complete";
				});
			}
			player.displayClientMessage(message.copy()
					.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
			return InteractionResult.SUCCESS;
		}

		ItemStack pome = QliphothPomeItem.createPickedPomeStack(bloom, huskIndex);
		if (!player.getInventory().add(pome)) {
			player.displayClientMessage(Component.translatable("message.hemomancy.qliphoth_bloom_block.the_ripe_pome_waits_make_room").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
			return InteractionResult.SUCCESS;
		}

		data.markPendingPomeClaimed(bloom);
		player.displayClientMessage(Component.translatable("message.hemomancy.qliphoth_bloom_block.the_qliphoth_pome_comes_away_warm").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
		return InteractionResult.SUCCESS;
	}
}
