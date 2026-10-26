package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.arguments.*;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.QliphothBloomBlock;
import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.IInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.*;
import com.vincenthuto.hemomancy.common.capability.player.unstained.*;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncPomeProgress;
import com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.QliphothBloomBlockEntity;
import com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.encounter.FungalGardenTravelHelper;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import javax.annotation.Nullable;

/** /hemo qliphoth: raise a debug Qliphoth tree, set the Silent Archon state and pome progress. */
final class QliphothSubcommand {
	private QliphothSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("qliphoth")
				.then(Commands.literal("tree")
						.then(Commands.argument("stage", StringArgumentType.word())
								.suggests((ctx, builder) -> {
									for (QliphothTreeStage stage : QliphothTreeStage.values()) {
										builder.suggest(stage.commandName());
									}
									return builder.buildFuture();
								})
								.executes(ctx -> spawnQliphothTree(ctx.getSource(),
										ctx.getSource().getPlayerOrException(),
										StringArgumentType.getString(ctx, "stage")))))
				.then(Commands.literal("archon")
						.then(silentArchonStateCommand("pending"))
						.then(silentArchonStateCommand("complete"))
						.then(silentArchonStateCommand("clear")))
				.then(Commands.literal("pome")
						.then(Commands.literal("set")
								.then(Commands.argument("count", IntegerArgumentType.integer(0, 9))
										.executes(ctx -> setPomeProgress(ctx.getSource(),
												ctx.getSource().getPlayerOrException(),
												IntegerArgumentType.getInteger(ctx, "count")))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> setPomeProgress(ctx.getSource(),
														EntityArgument.getPlayer(ctx, "player"),
														IntegerArgumentType.getInteger(ctx, "count"))))))
						.then(Commands.literal("reset")
								.executes(ctx -> resetPomeProgress(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> resetPomeProgress(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))))));
	}

	private static int resetPomeProgress(CommandSourceStack source, ServerPlayer player) {
		IInitiatoryDegree degree = HemoCapabilityAccess.getInitiatoryDegree(player)
				;
		degree.resetPomeCommunion();
		degree.setQliphothCommunionDone(false);
		InitiatoryDegreeEvents.syncDegree(player, degree);
		PacketHandler.sendToPlayer(player, new PacketSyncPomeProgress(0));
		ChamberOfWillManager.syncFor(player);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.qliphoth.reset",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.translatable("commands.hemomancy.qliphoth.qliphoth_pome_progress_and_communion_gate").withStyle(ChatFormatting.DARK_PURPLE)),
				true);
		return 1;
	}

	private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack>
			silentArchonStateCommand(String state) {
		return Commands.literal(state)
				.executes(ctx -> setSilentArchonState(ctx.getSource(), ctx.getSource().getPlayerOrException(), state))
				.then(Commands.argument("player", EntityArgument.player())
						.executes(ctx -> setSilentArchonState(ctx.getSource(),
								EntityArgument.getPlayer(ctx, "player"), state)));
	}

	private static int setSilentArchonState(CommandSourceStack source, ServerPlayer player, String state) {
		SilentArchonCommandRules.Transition transition = applySilentArchonState(player, state);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.qliphoth.set_silent_archon_state_to",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.literal(state).withStyle(ChatFormatting.DARK_PURPLE),
				HemoText.translatable("commands.hemomancy.qliphoth.degree",
		transition.degreeNumber()).withStyle(ChatFormatting.GRAY)), true);
		return 1;
	}

	/** Moves the player to a Silent Archon state ("pending", "complete" or "clear") and syncs it; shared with /hemo vesper. */
	static SilentArchonCommandRules.Transition applySilentArchonState(ServerPlayer player, String state) {
		IInitiatoryDegree degree = HemoCapabilityAccess.getInitiatoryDegree(player);
		SilentArchonCommandRules.Transition transition =
				SilentArchonCommandRules.transition(state, degree.getDegreeNumber());
		degree.setDegreeNumber(transition.degreeNumber());
		degree.setArchonPath(transition.archonPath());
		if (transition.archonPath() != EnumArchonPath.NONE) {
			degree.setFungalRevelationWitnessed(true);
		}
		if (transition.clearLegacyChoice()) {
			player.getPersistentData().remove(FungalGardenTravelHelper.ARCHON_CHOICE_KEY);
		}
		InitiatoryDegreeEvents.syncDegree(player, degree);
		ChamberOfWillManager.syncFor(player);
		return transition;
	}

	private static int setPomeProgress(CommandSourceStack source, ServerPlayer player, int count) {
		int clamped = Math.max(0, Math.min(9, count));
		IInitiatoryDegree degree = HemoCapabilityAccess.getInitiatoryDegree(player)
				;
		degree.syncTotalPomesConsumed(clamped);
		degree.setQliphothCommunionDone(clamped >= 9);
		InitiatoryDegreeEvents.syncDegree(player, degree);
		PacketHandler.sendToPlayer(player, new PacketSyncPomeProgress(clamped));
		ChamberOfWillManager.syncFor(player);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.qliphoth.set",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.translatable("commands.hemomancy.qliphoth.qliphoth_pome_progress_to").withStyle(ChatFormatting.DARK_PURPLE),
				Component.literal(String.valueOf(clamped)).withStyle(ChatFormatting.LIGHT_PURPLE),
				Component.literal("/9.").withStyle(ChatFormatting.DARK_PURPLE)),
				true);
		return 1;
	}

	private static int spawnQliphothTree(CommandSourceStack source, ServerPlayer player, String rawStage) {
		QliphothTreeStage stage = QliphothTreeStage.parse(rawStage);
		if (stage == null) {
			source.sendFailure(Component.translatable("commands.hemomancy.qliphoth.unknown_qliphoth_tree_stage_use_initial"));
			return 0;
		}
		QliphothBloomSavedData.BloomEntry bloom = placeDebugTree(source, player, stage);
		if (bloom == null) return 0;
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.qliphoth.spawned_qliphoth_tree_stage",
				Component.literal(rawStage).withStyle(ChatFormatting.LIGHT_PURPLE),
				Component.translatable("commands.hemomancy.qliphoth.at").withStyle(ChatFormatting.DARK_PURPLE),
				Component.literal(bloom.center().toShortString()).withStyle(ChatFormatting.GOLD)), true);
		return 1;
	}

	/**
	 * Grows a debug Qliphoth owned by the player at their feet, at {@code stage}; null (with the failure already sent)
	 * when another Bloom is too close or there is no room. Shared with /hemo vesper.
	 */
	@Nullable
	static QliphothBloomSavedData.BloomEntry placeDebugTree(CommandSourceStack source, ServerPlayer player, QliphothTreeStage stage) {
		ServerLevel level = player.serverLevel();
		BlockPos center = player.blockPosition();
		String dimension = level.dimension().location().toString();
		QliphothBloomSavedData data = QliphothBloomSavedData.get(level.getServer().overworld());
		QliphothBloomSavedData.BloomEntry overlapping = data.getOverlappingBloom(center, dimension, 3);
		if (overlapping != null && !overlapping.center().equals(center)) {
			source.sendFailure(Component.translatable("commands.hemomancy.qliphoth.a_qliphoth_bloom_already_exists_within"));
			return null;
		}
		if (overlapping != null) {
			removeDebugQliphothTree(level, data, overlapping);
		}

		QliphothBloomBlock bloomBlock = (QliphothBloomBlock) BlockInit.qliphoth_bloom.get();
		IMultiBlock multiBlock = bloomBlock;
		if (!multiBlock.canPlaceMultiBlock(level, center)) {
			source.sendFailure(Component.translatable("commands.hemomancy.qliphoth.the_qliphoth_tree_needs_more_room"));
			return null;
		}

		level.setBlockAndUpdate(center, bloomBlock.defaultBlockState());
		if (level.getBlockEntity(center) instanceof QliphothBloomBlockEntity bloomEntity) {
			bloomEntity.setOwnerUUID(player.getUUID());
			bloomEntity.setChunkRadius(3);
		}
		multiBlock.placeFillers(level, center, bloomBlock.defaultBlockState());

		var debugBloom = new QliphothBloomSavedData.BloomEntry(
				player.getUUID(), center, dimension, 3, level.getGameTime());
		data.addBloom(debugBloom);
		for (int i = 0; i < stage.pomesDropped(); i++) {
			data.incrementPomesDropped(debugBloom);
		}
		if (stage.severedState().isPortalOpen()) {
			data.severBloom(debugBloom);
		} else if (stage.severedState().isSealedTrophy()) {
			data.severBloom(debugBloom);
			data.sealBloom(debugBloom);
		}
		HarbingerCardinalRiteEvents.syncQliphothBlooms(level.getServer());
		return debugBloom;
	}

	private static void removeDebugQliphothTree(ServerLevel level, QliphothBloomSavedData data,
			QliphothBloomSavedData.BloomEntry bloom) {
		if (level.getBlockState(bloom.center()).getBlock() instanceof IMultiBlock multiBlock) {
			multiBlock.removeFillers(level, bloom.center());
			level.removeBlock(bloom.center(), false);
		} else {
			data.removeBloomInChunk(bloom.center(), bloom.dimension());
		}
	}
}
