package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.CirculationIncomeHelper;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoDefinitions;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.FungalWhisperDialogueTrees;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.item.harbinger.QliphothPomeRules;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.tile.FillerBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.List;

/**
 * Handles tick-based effects for Qliphoth Blooms established by the Bloom of
 * the Qliphoth rite. Manipulation cost reduction is handled directly in
 * {@link com.vincenthuto.hemomancy.common.manipulation.BloodManipulation#performAction}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class QliphothBloomEvents {

	private static final int EFFECT_APPLICATION_INTERVAL_TICKS = 40;
	private static final int REGEN_DURATION = 50;
	private static final double BLOOD_REGEN_PER_TICK = 5.0;
	private static final int POME_RIPEN_CHANCE = 80;

	@SubscribeEvent
	public static void onQliphothBloomBreak(BreakEvent event) {
		if (!isProtectedQliphothPart(event.getLevel(), event.getPos(), event.getState())) return;

		event.setCanceled(true);
		if (event.getPlayer() instanceof ServerPlayer player) {
			player.displayClientMessage(Component.literal("The Qliphoth will not yield to ordinary hands.")
					.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
		}
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel sLevel)) return;
		if (sLevel.getGameTime() % EFFECT_APPLICATION_INTERVAL_TICKS != 0) return;

		QliphothBloomSavedData data = QliphothBloomSavedData.get(sLevel.getServer().overworld());
		String dimension = sLevel.dimension().location().toString();

		for (QliphothBloomSavedData.BloomEntry bloom : data.getBlooms()) {
			if (!bloom.dimension().equals(dimension)) continue;

			int blockRadius = bloom.chunkRadius() * 16;
			AABB bloomBounds = new AABB(bloom.center()).inflate(blockRadius, 64, blockRadius);
			List<ServerPlayer> players = sLevel.getEntitiesOfClass(ServerPlayer.class, bloomBounds, ServerPlayer::isAlive);

			for (ServerPlayer player : players) {
				player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
						REGEN_DURATION, 0, true, false, true));

				HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
					BloodFlowLedger.applyCirculationIncome(player, volume, "qliphoth_bloom",
							"Qliphoth Bloom", Category.WORLD, BLOOD_REGEN_PER_TICK,
							EFFECT_APPLICATION_INTERVAL_TICKS, CirculationIncomeHelper.IncomeChannel.OTHER);
				});
			}

			tryRipenPome(sLevel, bloom);
		}
	}

	private static void tryRipenPome(ServerLevel level, QliphothBloomSavedData.BloomEntry bloom) {
		RandomSource rand = level.getRandom();
		if (rand.nextInt(POME_RIPEN_CHANCE) != 0) return;

		BlockPos center = bloom.center();
		QliphothBloomSavedData data = QliphothBloomSavedData.get(level.getServer().overworld());
		int alreadyRipened = data.getPomesDropped(center);
		if (!QliphothPomeRules.canRipenPome(alreadyRipened, data.hasPendingPome(center))) return;

		int huskIndex = QliphothPomeRules.nextRipeHuskIndex(alreadyRipened);
		data.incrementPomesDropped(center);
		data.setPendingPome(center, huskIndex);
		HarbingerCardinalRiteEvents.syncQliphothBlooms(level.getServer());

		ServerPlayer owner = level.getServer().getPlayerList().getPlayer(bloom.ownerUUID());
		if (owner != null) {
			boolean offerMemo = FungalWhisperDialogueTrees.shouldOfferMemoWhisper(owner, MemoDefinitions.QLIPHOTH_COMMUNION);
			PacketHandler.sendToPlayer(owner, new OpenDialoguePacket(FungalWhisperDialogueTrees.pomeDropped(huskIndex, offerMemo)));
		}
	}

	public static boolean isInQliphothBloom(ServerPlayer player) {
		ServerLevel overworld = player.server.overworld();
		QliphothBloomSavedData data = QliphothBloomSavedData.get(overworld);
		String dimension = player.level().dimension().location().toString();
		return data.isInBloomRange(player.blockPosition(), dimension);
	}

	private static boolean isProtectedQliphothPart(LevelAccessor level, BlockPos pos, BlockState state) {
		if (state.is(BlockInit.qliphoth_bloom.get())) {
			return true;
		}
		if (!state.is(BlockInit.filler_block.get())) {
			return false;
		}

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof FillerBlockEntity filler && filler.getMainBlockPos() != null) {
			return level.getBlockState(filler.getMainBlockPos()).is(BlockInit.qliphoth_bloom.get());
		}
		return false;
	}
}
