package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Console commands for testing unstained (anti-hemomancy) progression.
 * <pre>
 * /unstained get [player]
 * /unstained begin [player]              — toggle begunPurification
 * /unstained purity get [player]
 * /unstained purity set &lt;0-100&gt; [player]
 * /unstained clarity unlock [player]     — toggle clarityUnlocked
 * /unstained clarity get [player]
 * /unstained clarity set &lt;0-100&gt; [player]
 * /unstained reset [player]              — reset all unstained progress
 * /unstained max [player]                — max out all unstained progress
 * </pre>
 */
public class UnstainedCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("unstained")
				.requires(src -> src.hasPermission(2))

				// ── Overview ──
				.then(Commands.literal("get")
						.executes(ctx -> getOverview(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> getOverview(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))

				// ── Begin Purification Toggle ──
				.then(Commands.literal("begin")
						.executes(ctx -> toggleBegun(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> toggleBegun(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))

				// ── Purity ──
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

				// ── Clarity ──
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

				// ── Reset ──
				.then(Commands.literal("reset")
						.executes(ctx -> resetAll(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> resetAll(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))

				// ── Max Out ──
				.then(Commands.literal("max")
						.executes(ctx -> maxAll(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> maxAll(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
		);
	}

	// ────────────── Overview ──────────────

	private static int getOverview(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
				.orElseThrow(IllegalStateException::new);
		EnumPurityStage pStage = EnumPurityStage.byPurity(cap.getPurity());
		EnumClarityStage cStage = EnumClarityStage.byClarity(cap.getClarity());

		source.sendSuccess(() -> Component.literal("")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" Unstained Progress:").withStyle(ChatFormatting.WHITE)), false);
		source.sendSuccess(() -> Component.literal("  Begun: ")
				.append(Component.literal(String.valueOf(cap.hasBegunPurification()))
						.withStyle(cap.hasBegunPurification() ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
		source.sendSuccess(() -> Component.literal("  Purity: ")
				.append(Component.literal(String.format("%.1f", cap.getPurity())).withStyle(ChatFormatting.WHITE))
				.append(Component.literal(" (" + pStage.getTitle() + ")").withStyle(ChatFormatting.GRAY)), false);
		source.sendSuccess(() -> Component.literal("  Clarity Unlocked: ")
				.append(Component.literal(String.valueOf(cap.hasClarityUnlocked()))
						.withStyle(cap.hasClarityUnlocked() ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
		source.sendSuccess(() -> Component.literal("  Clarity: ")
				.append(Component.literal(String.format("%.1f", cap.getClarity())).withStyle(ChatFormatting.AQUA))
				.append(Component.literal(" (" + cStage.getTitle() + ")").withStyle(ChatFormatting.GRAY)), false);
		source.sendSuccess(() -> Component.literal("  Silver Ward: ")
				.append(Component.literal(String.format("%.0f%%", cap.getSilverWardStrength() * 100))
						.withStyle(ChatFormatting.YELLOW)), false);
		source.sendSuccess(() -> Component.literal("  Verdigris Aura: ")
				.append(Component.literal(String.format("%.0f%%", cap.getVerdigrisAura() * 100))
						.withStyle(ChatFormatting.DARK_AQUA)), false);
		return 1;
	}

	// ────────────── Begin Purification ──────────────

	private static int toggleBegun(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
				.orElseThrow(IllegalStateException::new);
		cap.setBegunPurification(!cap.hasBegunPurification());
		UnstainedProgressEvents.syncProgress(player, cap);
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" begunPurification: "))
				.append(Component.literal(String.valueOf(cap.hasBegunPurification()))
						.withStyle(cap.hasBegunPurification() ? ChatFormatting.GREEN : ChatFormatting.RED)),
				true);
		return 1;
	}

	// ────────────── Purity ──────────────

	private static int getPurity(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
				.orElseThrow(IllegalStateException::new);
		EnumPurityStage stage = EnumPurityStage.byPurity(cap.getPurity());
		source.sendSuccess(() -> Component.literal("")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" Purity: ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(String.format("%.1f", cap.getPurity())).withStyle(ChatFormatting.WHITE))
				.append(Component.literal(" (" + stage.getTitle() + ")").withStyle(ChatFormatting.GRAY)),
				false);
		return 1;
	}

	private static int setPurity(CommandSourceStack source, ServerPlayer player, float value) {
		IUnstainedProgress cap = player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
				.orElseThrow(IllegalStateException::new);
		cap.setPurity(value);
		UnstainedProgressEvents.syncProgress(player, cap);
		EnumPurityStage stage = EnumPurityStage.byPurity(cap.getPurity());
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" purity to "))
				.append(Component.literal(String.format("%.1f", cap.getPurity())).withStyle(ChatFormatting.WHITE))
				.append(Component.literal(" (" + stage.getTitle() + ")").withStyle(ChatFormatting.GRAY)),
				true);
		return 1;
	}

	// ────────────── Clarity ──────────────

	private static int toggleClarityUnlock(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
				.orElseThrow(IllegalStateException::new);
		cap.setClarityUnlocked(!cap.hasClarityUnlocked());
		UnstainedProgressEvents.syncProgress(player, cap);
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" clarityUnlocked: "))
				.append(Component.literal(String.valueOf(cap.hasClarityUnlocked()))
						.withStyle(cap.hasClarityUnlocked() ? ChatFormatting.GREEN : ChatFormatting.RED)),
				true);
		return 1;
	}

	private static int getClarity(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
				.orElseThrow(IllegalStateException::new);
		EnumClarityStage stage = EnumClarityStage.byClarity(cap.getClarity());
		source.sendSuccess(() -> Component.literal("")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" Clarity: ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(String.format("%.1f", cap.getClarity())).withStyle(ChatFormatting.AQUA))
				.append(Component.literal(" (" + stage.getTitle() + ")").withStyle(ChatFormatting.GRAY)),
				false);
		return 1;
	}

	private static int setClarity(CommandSourceStack source, ServerPlayer player, float value) {
		IUnstainedProgress cap = player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
				.orElseThrow(IllegalStateException::new);
		cap.setClarity(value);
		UnstainedProgressEvents.syncProgress(player, cap);
		EnumClarityStage stage = EnumClarityStage.byClarity(cap.getClarity());
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" clarity to "))
				.append(Component.literal(String.format("%.1f", cap.getClarity())).withStyle(ChatFormatting.AQUA))
				.append(Component.literal(" (" + stage.getTitle() + ")").withStyle(ChatFormatting.GRAY)),
				true);
		return 1;
	}

	// ────────────── Reset / Max ──────────────

	private static int resetAll(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
				.orElseThrow(IllegalStateException::new);
		cap.setBegunPurification(false);
		cap.setPurity(0);
		cap.setClarityUnlocked(false);
		cap.setClarity(0);
		UnstainedProgressEvents.syncProgress(player, cap);
		source.sendSuccess(() -> Component.literal("Reset ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" unstained progress to zero").withStyle(ChatFormatting.YELLOW)),
				true);
		return 1;
	}

	private static int maxAll(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
				.orElseThrow(IllegalStateException::new);
		cap.setBegunPurification(true);
		cap.setPurity(100);
		cap.setClarityUnlocked(true);
		cap.setClarity(100);
		UnstainedProgressEvents.syncProgress(player, cap);
		source.sendSuccess(() -> Component.literal("Maxed ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" unstained progress (Purified + Enlightened)").withStyle(ChatFormatting.GREEN)),
				true);
		return 1;
	}
}
