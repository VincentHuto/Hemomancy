package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.arguments.*;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.PathMutualExclusionHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.IInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.*;
import com.vincenthuto.hemomancy.common.capability.player.unstained.*;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

/** /hemo degree: read or set a player's Hematic Order degree. */
final class DegreeSubcommand {
	private DegreeSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("degree")
				.then(Commands.literal("get")
						.executes(ctx -> getDegree(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> getDegree(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("set")
						.then(Commands.argument("degree", IntegerArgumentType.integer(0, 8))
								.executes(ctx -> setDegree(ctx.getSource(), ctx.getSource().getPlayerOrException(),
										IntegerArgumentType.getInteger(ctx, "degree")))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> setDegree(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
												IntegerArgumentType.getInteger(ctx, "degree"))))));
	}

	private static int getDegree(CommandSourceStack source, ServerPlayer player) {
		IInitiatoryDegree degree = HemoCapabilityAccess.getInitiatoryDegree(player)
				;
		int num = degree.getDegreeNumber();
		EnumInitiatoryDegree enumDeg = EnumInitiatoryDegree.byNumber(num);
		Component title = enumDeg != null ? enumDeg.displayName() : Component.translatable("hemomancy.degree.0");
		source.sendSuccess(() -> Component.literal("")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.translatable("commands.hemomancy.degree.degree").withStyle(ChatFormatting.GRAY))
				.append(Component.translatable("commands.hemomancy.degree.number_title", num, title)
						.withStyle(ChatFormatting.LIGHT_PURPLE)),
				false);
		return 1;
	}

	private static int setDegree(CommandSourceStack source, ServerPlayer player, int degreeNum) {
		IInitiatoryDegree degree = HemoCapabilityAccess.getInitiatoryDegree(player)
				;
		degree.setDegreeNumber(degreeNum);
		InitiatoryDegreeEvents.syncDegree(player, degree);
		boolean resetUnstained = degreeNum > 0 && PathMutualExclusionHelper.resetUnstainedProgress(player);
		EnumInitiatoryDegree enumDeg = EnumInitiatoryDegree.byNumber(degreeNum);
		Component title = enumDeg != null ? enumDeg.displayName() : Component.translatable("hemomancy.degree.0");
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.degree.set_degree_to",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.translatable("commands.hemomancy.degree.number_title", degreeNum, title)
						.withStyle(ChatFormatting.LIGHT_PURPLE),
				resetUnstained
						? Component.translatable("commands.hemomancy.degree.unstained_progress_reset").withStyle(ChatFormatting.GRAY)
						: Component.empty()),
				true);
		return 1;
	}
}
