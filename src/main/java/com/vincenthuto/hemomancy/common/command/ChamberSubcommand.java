package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.arguments.*;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.*;
import com.vincenthuto.hemomancy.common.capability.player.unstained.*;
import com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

/** /hemo chamber: override a player's Chamber of Will sky theme and room size. */
final class ChamberSubcommand {
	private ChamberSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("chamber")
				.then(Commands.literal("theme")
						.then(Commands.literal("cycle")
								.executes(ctx -> cycleChamberTheme(ctx.getSource(),
										ctx.getSource().getPlayerOrException(), 1))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> cycleChamberTheme(ctx.getSource(),
												EntityArgument.getPlayer(ctx, "player"), 1))))
						.then(Commands.literal("next")
								.executes(ctx -> cycleChamberTheme(ctx.getSource(),
										ctx.getSource().getPlayerOrException(), 1))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> cycleChamberTheme(ctx.getSource(),
												EntityArgument.getPlayer(ctx, "player"), 1))))
						.then(Commands.literal("previous")
								.executes(ctx -> cycleChamberTheme(ctx.getSource(),
										ctx.getSource().getPlayerOrException(), -1))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> cycleChamberTheme(ctx.getSource(),
												EntityArgument.getPlayer(ctx, "player"), -1))))
						.then(Commands.literal("prev")
								.executes(ctx -> cycleChamberTheme(ctx.getSource(),
										ctx.getSource().getPlayerOrException(), -1))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> cycleChamberTheme(ctx.getSource(),
												EntityArgument.getPlayer(ctx, "player"), -1))))
						.then(Commands.literal("set")
								.then(Commands.argument("theme", StringArgumentType.word())
										.suggests((ctx, builder) -> {
									for (ResourceLocation id : ChamberOfWillManager.commandSkyThemes()) {
												builder.suggest(id.getPath());
											}
											return builder.buildFuture();
										})
										.executes(ctx -> setChamberTheme(ctx.getSource(),
												ctx.getSource().getPlayerOrException(),
												StringArgumentType.getString(ctx, "theme")))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> setChamberTheme(ctx.getSource(),
														EntityArgument.getPlayer(ctx, "player"),
														StringArgumentType.getString(ctx, "theme"))))))
						.then(Commands.literal("reset")
								.executes(ctx -> resetChamberTheme(ctx.getSource(),
										ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> resetChamberTheme(ctx.getSource(),
										EntityArgument.getPlayer(ctx, "player"))))))
				.then(Commands.literal("size")
						.then(Commands.literal("set")
								.then(Commands.argument("radius", IntegerArgumentType.integer(
										ChamberOfWillManager.BASE_ROOM_RADIUS, ChamberOfWillManager.MAX_ROOM_RADIUS))
										.suggests((ctx, builder) -> {
											for (int radius = ChamberOfWillManager.BASE_ROOM_RADIUS;
													radius <= ChamberOfWillManager.MAX_ROOM_RADIUS; radius++) {
												builder.suggest(radius);
											}
											return builder.buildFuture();
										})
										.executes(ctx -> setChamberSize(ctx.getSource(),
												ctx.getSource().getPlayerOrException(),
												IntegerArgumentType.getInteger(ctx, "radius")))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> setChamberSize(ctx.getSource(),
														EntityArgument.getPlayer(ctx, "player"),
														IntegerArgumentType.getInteger(ctx, "radius"))))))
						.then(Commands.literal("reset")
								.executes(ctx -> resetChamberSize(ctx.getSource(),
										ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> resetChamberSize(ctx.getSource(),
										EntityArgument.getPlayer(ctx, "player"))))));
	}

	private static int cycleChamberTheme(CommandSourceStack source, ServerPlayer player, int direction) {
		ResourceLocation theme = ChamberOfWillManager.get(source.getServer()).cycleSkyThemeOverride(player, direction);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.chamber.chamber_sky_theme_override_for_cycled",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.literal(theme.toString()).withStyle(ChatFormatting.DARK_RED)),
				true);
		return 1;
	}

	private static int setChamberTheme(CommandSourceStack source, ServerPlayer player, String themeName) {
		ResourceLocation theme = parseChamberTheme(themeName);
		if (theme == null || !ChamberOfWillManager.isKnownSkyTheme(theme)) {
			source.sendFailure(HemoText.translatable("commands.hemomancy.chamber.unknown_chamber_sky_theme_valid",
					themeName,
					chamberThemeList()));
			return 0;
		}

		ChamberOfWillManager.get(source.getServer()).setSkyThemeOverride(player, theme);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.chamber.chamber_sky_theme_override_for_set",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.literal(theme.toString()).withStyle(ChatFormatting.DARK_RED)),
				true);
		return 1;
	}

	private static int resetChamberTheme(CommandSourceStack source, ServerPlayer player) {
		ResourceLocation theme = ChamberOfWillManager.get(source.getServer()).clearSkyThemeOverride(player);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.chamber.chamber_sky_theme_override_for_reset",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.literal(theme.toString()).withStyle(ChatFormatting.DARK_RED)),
				true);
		return 1;
	}

	private static int setChamberSize(CommandSourceStack source, ServerPlayer player, int radius) {
		int size = ChamberOfWillManager.get(source.getServer()).setChamberRadiusOverride(player, radius);
		source.sendSuccess(() -> HemoText.translatable("commands.hemomancy.chamber.chamber_size_override_for_set_to",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.literal(Integer.toString(size)).withStyle(ChatFormatting.DARK_RED),
				(size * 2 + 1),
				(size * 2 + 1)),
				true);
		return 1;
	}

	private static int resetChamberSize(CommandSourceStack source, ServerPlayer player) {
		int radius = ChamberOfWillManager.get(source.getServer()).clearChamberRadiusOverride(player);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.chamber.chamber_size_override_for_reset_to",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.literal(Integer.toString(radius)).withStyle(ChatFormatting.DARK_RED)),
				true);
		return 1;
	}

	private static ResourceLocation parseChamberTheme(String themeName) {
		String normalized = themeName.trim().toLowerCase(Locale.ROOT);
		try {
			return normalized.contains(":") ? ResourceLocation.parse(normalized) : Hemomancy.rloc(normalized);
		} catch (RuntimeException ignored) {
			return null;
		}
	}

	private static String chamberThemeList() {
		StringBuilder builder = new StringBuilder();
		for (ResourceLocation id : ChamberOfWillManager.commandSkyThemes()) {
			if (!builder.isEmpty()) {
				builder.append(", ");
			}
			builder.append(id);
		}
		return builder.toString();
	}
}
