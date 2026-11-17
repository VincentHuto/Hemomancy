package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * /hemo vat: {@code fill} stands the player's Specimen Vat two blocks ahead keeping a cow at
 * {@code CoreLoopFixtures.VAT_VIGOR} Vigor over a {@code VAT_BUFFER} mL buffer; {@code clear} takes vat and cow away.
 * Dev-only.
 */
final class VatSubcommand {
	private VatSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("vat").requires(DevCommands::available)
				.then(CoreLoopFixtures.action("fill", CoreLoopFixtures::placeFilledVat))
				.then(CoreLoopFixtures.action("clear", player -> CoreLoopFixtures.clear(player, CoreLoopFixtures.Kind.VAT)));
	}
}
