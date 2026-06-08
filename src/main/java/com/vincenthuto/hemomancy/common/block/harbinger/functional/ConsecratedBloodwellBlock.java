package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.block.shared.BlockBloodEndpoint;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketOpenBloodlinePoolScreen;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodlinePool;
import com.vincenthuto.hemomancy.common.tile.functional.ConsecratedBloodwellBlockEntity;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Founding Fane heart and physical link to the owner's shared bloodline pool.
 * The block has no private reservoir: Blood Projection contributes directly to
 * the bloodline pool, Blood Absorption draws directly from it, and right-click
 * opens the bloodline pool monitor screen.
 */
public class ConsecratedBloodwellBlock extends BaseEntityBlock implements BlockBloodEndpoint {

	public static final MapCodec<ConsecratedBloodwellBlock> CODEC = simpleCodec(ConsecratedBloodwellBlock::new);

	public ConsecratedBloodwellBlock(Properties properties) {
		super(properties);
	}

	public ConsecratedBloodwellBlock() {
		this(BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_RED)
				.requiresCorrectToolForDrops()
				.strength(3.0f, 8.0f)
				.sound(SoundType.METAL)
				.noOcclusion());
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		if (context.getPlayer() instanceof ServerPlayer player) {
			player.displayClientMessage(riteOnlyPlacementMessage(), true);
		}
		return null;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ConsecratedBloodwellBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return level.isClientSide ? null
				: createTickerHelper(type, BlockEntityInit.consecrated_bloodwell.get(),
						ConsecratedBloodwellBlockEntity::serverTick);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel) {
			FoundingFaneSavedData data = FoundingFaneSavedData.get(serverLevel);
			UUID owner = data.findOwnerForHeart(pos);
			if (owner != null) {
				List<BlockPos> stakes = data.removeHeartAndGetStakes(owner, pos);
				for (BlockPos stakePos : stakes) {
					if (serverLevel.getBlockState(stakePos).is(BlockInit.hematic_stake.get())) {
						serverLevel.removeBlock(stakePos, false);
					}
				}
			}
		}
		super.onRemove(state, level, pos, newState, movedByPiston);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
			Player player, BlockHitResult result) {
		return handleInteraction(level, pos, player);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
			BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
		if (stack.getItem() instanceof BloodAbsorptionItem
				|| stack.getItem() instanceof BloodProjectionItem
				|| stack.getItem() instanceof LivingStaffItem) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		return handleInteraction(level, pos, player) == InteractionResult.FAIL
				? ItemInteractionResult.FAIL
				: ItemInteractionResult.SUCCESS;
	}

	private InteractionResult handleInteraction(Level level, BlockPos pos, Player player) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			if (!isRegisteredBloodwell(serverLevel, pos)) {
				serverPlayer.displayClientMessage(invalidBloodwellMessage(), true);
				return InteractionResult.FAIL;
			}
			if (!canUseBloodwell(serverPlayer, pos)) {
				serverPlayer.displayClientMessage(Component.translatable(
						"block.hemomancy.consecrated_bloodwell.not_bloodline")
						.withStyle(ChatFormatting.DARK_RED), true);
				return InteractionResult.FAIL;
			}
			syncLinkedPool(serverPlayer, pos);
			PacketHandler.sendToPlayer(serverPlayer, new PacketOpenBloodlinePoolScreen());
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public double absorbBloodFromBlock(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
			double maxAmount) {
		if (!isRegisteredBloodwell(level, pos)) {
			player.displayClientMessage(invalidBloodwellMessage(), true);
			return 0.0D;
		}
		if (!canUseBloodwell(player, pos)) {
			player.displayClientMessage(Component.translatable(
					"block.hemomancy.consecrated_bloodwell.not_bloodline")
					.withStyle(ChatFormatting.DARK_RED), true);
			return 0.0D;
		}

		IBloodVolume playerBlood = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (playerBlood == null || !playerBlood.isActive()) {
			player.displayClientMessage(Component.translatable(
					"block.hemomancy.consecrated_bloodwell.inactive").withStyle(ChatFormatting.GRAY), true);
			return 0.0D;
		}

		Bloodline bloodline = playerBlood.getBloodLine();
		BloodlineSavedData savedData = BloodlineSavedData.get(player.server.overworld());
		Bloodline globalLine = savedData.getBloodline(bloodline.getBloodlineUUID());
		if (globalLine == null) {
			return 0.0D;
		}

		double capacity = Math.max(0.0D, playerBlood.getMaxBloodVolume() - playerBlood.getBloodVolume());
		float requested = (float) Math.min(maxAmount, capacity);
		if (requested <= 0.0F) {
			return 0.0D;
		}

		float drawn = savedData.drawBlood(bloodline.getBloodlineUUID(), requested);
		if (drawn <= 0.0F) {
			return 0.0D;
		}

		playerBlood.fill(drawn);
		BloodVolumeEvents.syncVolume(player, playerBlood);
		syncBloodlinePool(player, globalLine);
		syncWellFromPool(level, pos, globalLine);
		return drawn;
	}

	@Override
	public double projectBloodIntoBlock(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
			double maxAmount) {
		if (!isRegisteredBloodwell(level, pos)) {
			player.displayClientMessage(invalidBloodwellMessage(), true);
			return 0.0D;
		}
		if (!canUseBloodwell(player, pos)) {
			player.displayClientMessage(Component.translatable(
					"block.hemomancy.consecrated_bloodwell.not_bloodline")
					.withStyle(ChatFormatting.DARK_RED), true);
			return 0.0D;
		}

		IBloodVolume playerBlood = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (playerBlood == null || !playerBlood.isActive()) {
			player.displayClientMessage(Component.translatable(
					"block.hemomancy.consecrated_bloodwell.inactive").withStyle(ChatFormatting.GRAY), true);
			return 0.0D;
		}

		Bloodline bloodline = playerBlood.getBloodLine();
		BloodlineSavedData savedData = BloodlineSavedData.get(player.server.overworld());
		Bloodline globalLine = savedData.getBloodline(bloodline.getBloodlineUUID());
		if (globalLine == null) {
			return 0.0D;
		}

		double poolRoom = Math.max(0.0D, globalLine.getMaxBloodVolume() - globalLine.getBloodVolume());
		double toDeposit = Math.min(maxAmount, Math.min(playerBlood.getBloodVolume(), poolRoom));
		if (toDeposit <= 0.0D || !playerBlood.drain(toDeposit)) {
			return 0.0D;
		}

		savedData.contributeBlood(bloodline.getBloodlineUUID(), (float) toDeposit);
		BloodVolumeEvents.syncVolume(player, playerBlood);
		syncBloodlinePool(player, globalLine);
		syncWellFromPool(level, pos, globalLine);
		return toDeposit;
	}

	public static boolean isInOwnFane(ServerPlayer player) {
		FoundingFaneSavedData data = FoundingFaneSavedData.get((ServerLevel) player.level());
		UUID owner = HemoCapabilityAccess.getBloodVolume(player)
				.map(IBloodVolume::getBloodLine)
				.filter(Bloodline::isValid)
				.map(Bloodline::getLeaderUUID)
				.orElse(player.getUUID());
		return data.isWithinFane(owner, player.blockPosition());
	}

	public static boolean canUseBloodwell(ServerPlayer player, BlockPos bloodwellPos) {
		return HemoCapabilityAccess.getBloodVolume(player)
				.map(IBloodVolume::getBloodLine)
				.filter(Bloodline::isValid)
				.filter(bloodline -> bloodline.hasMember(player.getUUID()))
				.map(Bloodline::getLeaderUUID)
				.map(owner -> {
					FoundingFaneSavedData data = FoundingFaneSavedData.get((ServerLevel) player.level());
					BlockPos heart = data.getHeart(owner);
					return heart != null && heart.equals(bloodwellPos);
				})
				.orElse(false);
	}

	private static boolean isRegisteredBloodwell(ServerLevel level, BlockPos pos) {
		return FoundingFaneSavedData.get(level).findOwnerForHeart(pos) != null;
	}

	private static Component riteOnlyPlacementMessage() {
		return Component.literal("Consecrated Bloodwells may only be manifested by the Rite of the Founding Fane.")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC);
	}

	private static Component invalidBloodwellMessage() {
		return Component.literal("This bloodwell is not bound to an active Founding Fane.")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC);
	}

	private static void syncLinkedPool(ServerPlayer player, BlockPos pos) {
		HemoCapabilityAccess.getBloodVolume(player)
				.map(IBloodVolume::getBloodLine)
				.filter(Bloodline::isValid)
				.filter(bloodline -> bloodline.hasMember(player.getUUID()))
				.ifPresent(bloodline -> {
					Bloodline globalLine = BloodlineSavedData.get(player.server.overworld())
							.getBloodline(bloodline.getBloodlineUUID());
					if (globalLine != null) {
						syncBloodlinePool(player, globalLine);
						if (player.level() instanceof ServerLevel level) {
							syncWellFromPool(level, pos, globalLine);
						}
					}
				});
	}

	private static void syncBloodlinePool(ServerPlayer player, Bloodline globalLine) {
		PacketHandler.sendToPlayer(player, new PacketSyncBloodlinePool(globalLine.getBloodVolume(),
				globalLine.getMaxBloodVolume(), globalLine.getPlayerUUIDS().size()));
	}

	private static void syncWellFromPool(ServerLevel level, BlockPos pos, Bloodline globalLine) {
		if (level.getBlockEntity(pos) instanceof ConsecratedBloodwellBlockEntity well) {
			well.syncFromBloodlinePool(globalLine);
		}
	}
}
