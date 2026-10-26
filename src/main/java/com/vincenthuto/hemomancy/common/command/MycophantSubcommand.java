package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.vincenthuto.hemomancy.common.encounter.MycophantEncounterManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * /hemo mycophant: starts the real Mycophant encounter for a test run, skipping only the Gardens hunt. {@code fight}
 * plays the root capture, the Nursery build and transfer, and the full waking; {@code rematch} the short rematch
 * opening. Neither checks degree, path or the retry cooldown.
 */
final class MycophantSubcommand {
	private MycophantSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("mycophant")
				.then(Commands.literal("fight")
						.executes(ctx -> begin(ctx.getSource(), ctx.getSource().getPlayerOrException(), false))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> begin(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), false))))
				.then(Commands.literal("rematch")
						.executes(ctx -> begin(ctx.getSource(), ctx.getSource().getPlayerOrException(), true))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> begin(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), true))));
	}

	private static int begin(CommandSourceStack source, ServerPlayer player, boolean rematch) {
		if (MycophantEncounterManager.isActive(player)) {
			source.sendFailure(Component.translatable("commands.hemomancy.mycophant.already_active", player.getDisplayName()));
			return 0;
		}
		MycophantEncounterManager.beginClaim(player, rematch);
		source.sendSuccess(() -> Component.translatable(rematch ? "commands.hemomancy.mycophant.rematch_started"
				: "commands.hemomancy.mycophant.fight_started", player.getDisplayName()), true);
		return 1;
	}
}
