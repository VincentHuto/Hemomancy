package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.encounter.VesperOrdealManager;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * /hemo vesper fight: starts the real Vesper ordeal for a test run. The ordeal belongs to an open (pruned) Qliphoth
 * Bloom of the player's and to the pending Silent path, and ends itself without them, so this sets both up the way
 * {@code /hemo qliphoth archon pending} and {@code /hemo qliphoth tree pruned} do: the player is moved to degree 7 on
 * the pending path if they are not there, their open Bloom is reused or a pruned one grows at their feet. Then they
 * enter through the same {@link VesperOrdealManager#enter} the Bloom block uses: the arena, the transfer and the
 * Crowned Refusal.
 */
final class VesperSubcommand {
	private VesperSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("vesper")
				.then(Commands.literal("fight")
						.executes(ctx -> fight(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> fight(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));
	}

	private static int fight(CommandSourceStack source, ServerPlayer player) {
		if (VesperOrdealManager.isActive(player)) {
			source.sendFailure(Component.translatable("commands.hemomancy.vesper.already_active", player.getDisplayName()));
			return 0;
		}
		if (!(ProgressionGates.isDegree(player, 7)
				&& HemoCapabilityAccess.getInitiatoryDegree(player).getArchonPath() == EnumArchonPath.SILENT_PENDING))
			QliphothSubcommand.applySilentArchonState(player, "pending");
		QliphothBloomSavedData blooms = QliphothBloomSavedData.get(player.getServer().overworld());
		QliphothBloomSavedData.BloomEntry bloom = blooms.getBlooms().stream()
				.filter(entry -> entry.ownerUUID().equals(player.getUUID()) && blooms.getState(entry).isPortalOpen())
				.findFirst().orElse(null);
		if (bloom == null) bloom = QliphothSubcommand.placeDebugTree(source, player, QliphothTreeStage.PRUNED);
		if (bloom == null) return 0;
		if (!VesperOrdealManager.enter(player, bloom)) {
			source.sendFailure(Component.translatable("commands.hemomancy.vesper.could_not_enter", player.getDisplayName()));
			return 0;
		}
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.vesper.fight_started", player.getDisplayName()), true);
		return 1;
	}
}
