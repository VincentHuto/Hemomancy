package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * /hemo tank: {@code clot} stands an Enzymatic Scriptorium two blocks ahead whose blood has clotted through (fed
 * {@code CoreLoopFixtures.TANK_FED} mL a full clot ago); {@code clear} takes it away. Dev-only.
 */
final class TankSubcommand {
	private TankSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("tank").requires(DevCommands::available)
				.then(CoreLoopFixtures.action("clot", CoreLoopFixtures::placeClottedTank))
				.then(CoreLoopFixtures.action("clear", player -> CoreLoopFixtures.clear(player, CoreLoopFixtures.Kind.TANK)));
	}
}
