package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.arguments.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.*;
import com.vincenthuto.hemomancy.common.capability.player.unstained.*;
import com.vincenthuto.hemomancy.common.event.worldevent.FaneBoundaryRelation;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

/** /hemo fane preview: preview a Fane boundary as another relation would see it. */
final class FaneSubcommand {
	private FaneSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("fane")
				.then(Commands.literal("preview")
						.then(Commands.literal("member")
								.executes(ctx -> setFanePreview(ctx.getSource(), FaneBoundaryRelation.MEMBER)))
						.then(Commands.literal("mundane")
								.executes(ctx -> setFanePreview(ctx.getSource(),
										FaneBoundaryRelation.MUNDANE_OUTSIDER)))
						.then(Commands.literal("outsider")
								.executes(ctx -> setFanePreview(ctx.getSource(), FaneBoundaryRelation.OUTSIDER)))
						.then(Commands.literal("rival")
								.executes(ctx -> setFanePreview(ctx.getSource(), FaneBoundaryRelation.RIVAL_ELDER)))
						.then(Commands.literal("clear")
								.executes(ctx -> clearFanePreview(ctx.getSource()))));
	}

	private static int setFanePreview(CommandSourceStack source, FaneBoundaryRelation relation)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		FoundingFaneEvents.setPreviewRelation(player, relation);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.fane.fane_boundary_preview_set_to",
				Component.literal(relation.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.DARK_RED),
				Component.translatable("commands.hemomancy.fane.wait_up_to_two_seconds_for").withStyle(ChatFormatting.GRAY)),
				false);
		return 1;
	}

	private static int clearFanePreview(CommandSourceStack source)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		FoundingFaneEvents.clearPreviewRelation(player);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.fane.fane_boundary_preview_cleared").withStyle(ChatFormatting.GRAY),
				false);
		return 1;
	}
}
