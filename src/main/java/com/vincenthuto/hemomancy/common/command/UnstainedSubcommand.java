package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.arguments.*;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.PathMutualExclusionHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.*;
import com.vincenthuto.hemomancy.common.capability.player.unstained.*;
import com.vincenthuto.hemomancy.common.capability.player.unstained.stillart.KnownStillArtEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

/** /hemo unstained: begin the path and set purity and clarity. */
final class UnstainedSubcommand {
	private UnstainedSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("unstained")
				.then(Commands.literal("get")
						.executes(ctx -> getUnstainedOverview(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> getUnstainedOverview(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("begin")
						.executes(ctx -> toggleBegun(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> toggleBegun(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("purity")
						.then(Commands.literal("get")
								.executes(ctx -> getPurity(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> getPurity(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
						.then(Commands.literal("set")
								.then(Commands.argument("value", FloatArgumentType.floatArg(0, 100))
										.executes(ctx -> setPurity(ctx.getSource(), ctx.getSource().getPlayerOrException(),
												FloatArgumentType.getFloat(ctx, "value")))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> setPurity(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
														FloatArgumentType.getFloat(ctx, "value")))))))
				.then(Commands.literal("clarity")
						.then(Commands.literal("unlock")
								.executes(ctx -> toggleClarityUnlock(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> toggleClarityUnlock(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
						.then(Commands.literal("get")
								.executes(ctx -> getClarity(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> getClarity(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
						.then(Commands.literal("set")
								.then(Commands.argument("value", FloatArgumentType.floatArg(0, 100))
										.executes(ctx -> setClarity(ctx.getSource(), ctx.getSource().getPlayerOrException(),
												FloatArgumentType.getFloat(ctx, "value")))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> setClarity(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
														FloatArgumentType.getFloat(ctx, "value")))))))
				.then(Commands.literal("reset")
						.executes(ctx -> resetUnstained(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> resetUnstained(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("max")
						.executes(ctx -> maxUnstained(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> maxUnstained(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));
	}

	private static int getUnstainedOverview(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = HemoCapabilityAccess.getUnstainedProgress(player)
				;
		EnumPurityStage pStage = EnumPurityStage.byPurity(cap.getPurity());
		EnumClarityStage cStage = EnumClarityStage.byClarity(cap.getClarity());

		source.sendSuccess(() -> Component.literal("")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.translatable("commands.hemomancy.unstained.unstained_progress").withStyle(ChatFormatting.WHITE)), false);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.unstained.phase",
				Component.literal(UnstainedAccessRules.phase(cap).name()).withStyle(ChatFormatting.AQUA)), false);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.unstained.novitiate_vows",
				Component.literal(UnstainedAccessRules.completedNovitiateVows(cap.getClaimedObservances()) + "/5")
						.withStyle(ChatFormatting.WHITE)), false);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.unstained.begun",
				Component.literal(String.valueOf(cap.hasBegunPurification()))
						.withStyle(cap.hasBegunPurification() ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.unstained.purity",
				Component.literal(String.format("%.1f", cap.getPurity())).withStyle(ChatFormatting.WHITE),
				Component.translatable("commands.hemomancy.unstained.stage_suffix", pStage.getDisplayName()).withStyle(ChatFormatting.GRAY)), false);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.unstained.clarity_unlocked",
				Component.literal(String.valueOf(cap.hasClarityUnlocked()))
						.withStyle(cap.hasClarityUnlocked() ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.unstained.clarity",
				Component.literal(String.format("%.1f", cap.getClarity())).withStyle(ChatFormatting.AQUA),
				Component.translatable("commands.hemomancy.unstained.stage_suffix", cStage.getDisplayName()).withStyle(ChatFormatting.GRAY)), false);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.unstained.silver_ward",
				Component.literal(String.format("%.0f%%", cap.getSilverWardStrength() * 100))
						.withStyle(ChatFormatting.YELLOW)), false);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.unstained.verdigris_aura",
				Component.literal(String.format("%.0f%%", cap.getVerdigrisAura() * 100))
						.withStyle(ChatFormatting.DARK_AQUA)), false);
		return 1;
	}

	private static int toggleBegun(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = HemoCapabilityAccess.getUnstainedProgress(player)
				;
		cap.setBegunPurification(!cap.hasBegunPurification());
		UnstainedProgressEvents.syncProgress(player, cap);
		KnownStillArtEvents.grantEligibleArts(player, cap);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.unstained.set_begunpurification",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.literal(String.valueOf(cap.hasBegunPurification()))
						.withStyle(cap.hasBegunPurification() ? ChatFormatting.GREEN : ChatFormatting.RED)),
				true);
		return 1;
	}

	private static int getPurity(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = HemoCapabilityAccess.getUnstainedProgress(player)
				;
		EnumPurityStage stage = EnumPurityStage.byPurity(cap.getPurity());
		source.sendSuccess(() -> Component.literal("")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.translatable("commands.hemomancy.unstained.purity_2").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(String.format("%.1f", cap.getPurity())).withStyle(ChatFormatting.WHITE))
				.append(Component.translatable("commands.hemomancy.unstained.stage_suffix", stage.getDisplayName()).withStyle(ChatFormatting.GRAY)),
				false);
		return 1;
	}

	private static int setPurity(CommandSourceStack source, ServerPlayer player, float value) {
		IUnstainedProgress cap = HemoCapabilityAccess.getUnstainedProgress(player)
				;
		cap.setPurity(value);
		UnstainedProgressEvents.syncProgress(player, cap);
		EnumPurityStage stage = EnumPurityStage.byPurity(cap.getPurity());
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.unstained.set_purity_to",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.literal(String.format("%.1f", cap.getPurity())).withStyle(ChatFormatting.WHITE),
				Component.translatable("commands.hemomancy.unstained.stage_suffix", stage.getDisplayName()).withStyle(ChatFormatting.GRAY)),
				true);
		return 1;
	}

	private static int toggleClarityUnlock(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = HemoCapabilityAccess.getUnstainedProgress(player)
				;
		cap.setClarityUnlocked(!cap.hasClarityUnlocked());
		boolean resetHarbinger = PathMutualExclusionHelper.enforceHarbingerResetOnClarity(player, cap);
		UnstainedProgressEvents.syncProgress(player, cap);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.unstained.set_clarityunlocked",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.literal(String.valueOf(cap.hasClarityUnlocked()))
						.withStyle(cap.hasClarityUnlocked() ? ChatFormatting.GREEN : ChatFormatting.RED),
				resetHarbinger
						? Component.translatable("commands.hemomancy.unstained.harbinger_progress_reset").withStyle(ChatFormatting.GRAY)
						: Component.empty()),
				true);
		return 1;
	}

	private static int getClarity(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = HemoCapabilityAccess.getUnstainedProgress(player)
				;
		EnumClarityStage stage = EnumClarityStage.byClarity(cap.getClarity());
		source.sendSuccess(() -> Component.literal("")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.translatable("commands.hemomancy.unstained.clarity_2").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(String.format("%.1f", cap.getClarity())).withStyle(ChatFormatting.AQUA))
				.append(Component.translatable("commands.hemomancy.unstained.stage_suffix", stage.getDisplayName()).withStyle(ChatFormatting.GRAY)),
				false);
		return 1;
	}

	private static int setClarity(CommandSourceStack source, ServerPlayer player, float value) {
		IUnstainedProgress cap = HemoCapabilityAccess.getUnstainedProgress(player)
				;
		cap.setClarity(value);
		boolean unlockedBySet = value > 0.0f && !cap.hasClarityUnlocked();
		if (unlockedBySet) {
			cap.setClarityUnlocked(true);
		}
		boolean resetHarbinger = PathMutualExclusionHelper.enforceHarbingerResetOnClarity(player, cap);
		UnstainedProgressEvents.syncProgress(player, cap);
		EnumClarityStage stage = EnumClarityStage.byClarity(cap.getClarity());
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.unstained.set_clarity_to",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.literal(String.format("%.1f", cap.getClarity())).withStyle(ChatFormatting.AQUA),
				Component.translatable("commands.hemomancy.unstained.stage_suffix", stage.getDisplayName()).withStyle(ChatFormatting.GRAY),
				unlockedBySet
						? Component.translatable("commands.hemomancy.unstained.clarity_unlocked_2").withStyle(ChatFormatting.GRAY)
						: Component.empty(),
				resetHarbinger
						? Component.translatable("commands.hemomancy.unstained.harbinger_progress_reset").withStyle(ChatFormatting.GRAY)
						: Component.empty()),
				true);
		return 1;
	}

	private static int maxUnstained(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = HemoCapabilityAccess.getUnstainedProgress(player)
				;
		cap.setBegunPurification(false);
		cap.setPurity(100);
		cap.setBaselineRestored(true);
		cap.setClarityUnlocked(true);
		cap.setClarity(100);
		cap.setInfectionSuppressed(false);
		cap.setClarityPrepared(false);
		cap.setAcceptedObservances(UnstainedAccessRules.NOVITIATE_MASK);
		cap.setClaimedObservances(UnstainedAccessRules.NOVITIATE_MASK);
		cap.setNovitiateRetortComplete(true);
		cap.setNovitiateDewProduced(4);
		cap.setNovitiateBlocksConsecrated(8);
		cap.setNovitiateProtectionComplete(true);
		boolean resetHarbinger = PathMutualExclusionHelper.enforceHarbingerResetOnClarity(player, cap);
		UnstainedProgressEvents.syncProgress(player, cap);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.unstained.maxed",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.translatable("commands.hemomancy.unstained.unstained_progress_purified_enlightened").withStyle(ChatFormatting.GREEN),
				resetHarbinger
						? Component.translatable("commands.hemomancy.unstained.harbinger_progress_reset").withStyle(ChatFormatting.GRAY)
						: Component.empty()),
				true);
		return 1;
	}

	private static int resetUnstained(CommandSourceStack source, ServerPlayer player) {
		PathMutualExclusionHelper.resetUnstainedProgress(player);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.unstained.reset",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.translatable("commands.hemomancy.unstained.unstained_progress_to_zero").withStyle(ChatFormatting.YELLOW)),
				true);
		return 1;
	}
}
