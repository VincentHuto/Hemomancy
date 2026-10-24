package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.arguments.*;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.BloodTendencyEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.unstained.*;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

/** /hemo tendency: read, set, max and reset a player's blood tendencies. */
final class TendencySubcommand {
	private TendencySubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("tendency")
				.then(Commands.literal("get")
						.executes(ctx -> getTendency(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> getTendency(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("reset")
						.executes(ctx -> resetTendency(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> resetTendency(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("max")
						.executes(ctx -> maxTendency(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> maxTendency(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.argument("tendency", StringArgumentType.word())
						.suggests((ctx, builder) -> {
							for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
								builder.suggest(tendency.name().toLowerCase(Locale.ROOT));
							}
							return builder.buildFuture();
						})
						.then(Commands.argument("value", FloatArgumentType.floatArg(0))
								.executes(ctx -> setTendency(ctx.getSource(), ctx.getSource().getPlayerOrException(),
										StringArgumentType.getString(ctx, "tendency"),
										FloatArgumentType.getFloat(ctx, "value")))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> setTendency(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
												StringArgumentType.getString(ctx, "tendency"),
												FloatArgumentType.getFloat(ctx, "value"))))))
		;
	}

	private static int getTendency(CommandSourceStack source, ServerPlayer player) {
		IBloodTendency bloodTendency = HemoCapabilityAccess.getBloodTendency(player);
		float total = bloodTendency.getTotalAlignment();

		source.sendSuccess(() -> Component.literal("")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.translatable("commands.hemomancy.tendency.blood_tendencies").withStyle(ChatFormatting.GRAY)), false);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.tendency.total",
				Component.literal(String.format("%.2f", total)).withStyle(ChatFormatting.AQUA)), false);

		for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
			float value = bloodTendency.getAlignmentByTendency(tendency);
			float percent = total > 0.0f ? (value / total) * 100.0f : 0.0f;
			final EnumBloodTendency current = tendency;
			source.sendSuccess(() -> Component.literal("  ")
					.append(Component.literal(current.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.DARK_RED))
					.append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
					.append(Component.literal(String.format("%.2f", value)).withStyle(ChatFormatting.WHITE))
					.append(Component.literal(" (" + String.format("%.1f%%", percent) + ")").withStyle(ChatFormatting.GRAY)),
				false);
		}
		return 1;
	}

	private static int resetTendency(CommandSourceStack source, ServerPlayer player) {
		setAllTendencies(player, 0.0f);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.tendency.reset",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.translatable("commands.hemomancy.tendency.blood_tendencies_to_0").withStyle(ChatFormatting.GREEN)),
				true);
		return 1;
	}

	private static int maxTendency(CommandSourceStack source, ServerPlayer player) {
		setAllTendencies(player, 100.0f);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.tendency.set",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.translatable("commands.hemomancy.tendency.blood_tendencies_to_100").withStyle(ChatFormatting.GREEN)),
				true);
		return 1;
	}

	private static int setTendency(CommandSourceStack source, ServerPlayer player, String tendencyName, float value) {
		EnumBloodTendency tendency = parseBloodTendency(tendencyName);
		if (tendency == null) {
			source.sendFailure(HemoText.translatable("commands.hemomancy.tendency.unknown_tendency_valid", tendencyName, getValidTendencyNames()));
			return 0;
		}

		IBloodTendency bloodTendency = HemoCapabilityAccess.getBloodTendency(player);
		bloodTendency.setTendencyAlignment(tendency, value);
		BloodTendencyEvents.syncTendency(player, bloodTendency);

		source.sendSuccess(() -> Component.translatable("commands.hemomancy.tendency.set_tendency_to",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.literal(tendency.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.DARK_RED),
				Component.literal(String.format("%.2f", bloodTendency.getAlignmentByTendency(tendency)))
						.withStyle(ChatFormatting.AQUA)),
				true);
		return 1;
	}

	private static void setAllTendencies(ServerPlayer player, float value) {
		IBloodTendency bloodTendency = HemoCapabilityAccess.getBloodTendency(player);
		for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
			bloodTendency.setTendencyAlignment(tendency, value);
		}
		BloodTendencyEvents.syncTendency(player, bloodTendency);
	}

	static EnumBloodTendency parseBloodTendency(String tendencyName) {
		try {
			return EnumBloodTendency.valueOf(tendencyName.trim().replace('-', '_').toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	static String getValidTendencyNames() {
		StringBuilder builder = new StringBuilder();
		for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
			if (!builder.isEmpty()) {
				builder.append(", ");
			}
			builder.append(tendency.name().toLowerCase(Locale.ROOT));
		}
		return builder.toString();
	}
}
