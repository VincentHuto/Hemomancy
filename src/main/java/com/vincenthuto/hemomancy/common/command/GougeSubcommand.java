package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * /hemo gouge: {@code light} cuts a {@code CoreLoopFixtures.RUN_LENGTH}-cell gouge run two blocks ahead and pulses
 * it with the light function for the player; {@code clear} fills it in and forgets its clocks. Dev-only.
 */
final class GougeSubcommand {
	private GougeSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("gouge").requires(DevCommands::available)
				.then(CoreLoopFixtures.action("light", CoreLoopFixtures::placeLitRun))
				.then(CoreLoopFixtures.action("clear", player -> CoreLoopFixtures.clear(player, CoreLoopFixtures.Kind.GOUGE)));
	}
}
