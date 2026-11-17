package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * /hemo temperpool: the spendable Temper pool (core-loop Track 4), not the Temperament that {@code /hemo temper}
 * sets. {@code carried} fills every school to the largest cap, {@code dry} empties every school, {@code restore} puts
 * back the pool the first of them replaced. Dev-only.
 */
final class TemperPoolSubcommand {
	private TemperPoolSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("temperpool").requires(DevCommands::available)
				.then(CoreLoopFixtures.action("carried", CoreLoopFixtures::carryTemper))
				.then(CoreLoopFixtures.action("dry", CoreLoopFixtures::dryTemper))
				.then(CoreLoopFixtures.action("restore", CoreLoopFixtures::restoreTemper));
	}
}
