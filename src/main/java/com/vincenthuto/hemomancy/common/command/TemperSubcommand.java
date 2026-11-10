package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.arguments.*;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemperamentEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.IBloodTemperament;
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

/** /hemo temper: read, set, max and reset a player's blood tempers. */
final class TemperSubcommand {
	private TemperSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("temper")
				.then(Commands.literal("get")
						.executes(ctx -> getTemper(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> getTemper(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("reset")
						.executes(ctx -> resetTemper(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> resetTemper(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("max")
						.executes(ctx -> maxTemper(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> maxTemper(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.argument("temper", StringArgumentType.word())
						.suggests((ctx, builder) -> {
							for (EnumBloodTemper temper : EnumBloodTemper.values()) {
								builder.suggest(temper.name().toLowerCase(Locale.ROOT));
							}
							return builder.buildFuture();
						})
						.then(Commands.argument("value", FloatArgumentType.floatArg(0))
								.executes(ctx -> setTemper(ctx.getSource(), ctx.getSource().getPlayerOrException(),
										StringArgumentType.getString(ctx, "temper"),
										FloatArgumentType.getFloat(ctx, "value")))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> setTemper(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
												StringArgumentType.getString(ctx, "temper"),
												FloatArgumentType.getFloat(ctx, "value"))))))
		;
	}

	private static int getTemper(CommandSourceStack source, ServerPlayer player) {
		IBloodTemperament bloodTemperament = HemoCapabilityAccess.getBloodTemperament(player);
		float total = bloodTemperament.getTotalAlignment();

		source.sendSuccess(() -> Component.literal("")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.translatable("commands.hemomancy.temper.blood_tempers").withStyle(ChatFormatting.GRAY)), false);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.temper.total",
				Component.literal(String.format("%.2f", total)).withStyle(ChatFormatting.AQUA)), false);

		for (EnumBloodTemper temper : EnumBloodTemper.values()) {
			float value = bloodTemperament.getAlignmentByTemper(temper);
			float percent = total > 0.0f ? (value / total) * 100.0f : 0.0f;
			final EnumBloodTemper current = temper;
			source.sendSuccess(() -> Component.literal("  ")
					.append(Component.literal(current.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.DARK_RED))
					.append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
					.append(Component.literal(String.format("%.2f", value)).withStyle(ChatFormatting.WHITE))
					.append(Component.literal(" (" + String.format("%.1f%%", percent) + ")").withStyle(ChatFormatting.GRAY)),
				false);
		}
		return 1;
	}

	private static int resetTemper(CommandSourceStack source, ServerPlayer player) {
		setAllTempers(player, 0.0f);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.temper.reset",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.translatable("commands.hemomancy.temper.blood_tempers_to_0").withStyle(ChatFormatting.GREEN)),
				true);
		return 1;
	}

	private static int maxTemper(CommandSourceStack source, ServerPlayer player) {
		setAllTempers(player, 100.0f);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.temper.set",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.translatable("commands.hemomancy.temper.blood_tempers_to_100").withStyle(ChatFormatting.GREEN)),
				true);
		return 1;
	}

	private static int setTemper(CommandSourceStack source, ServerPlayer player, String temperName, float value) {
		EnumBloodTemper temper = parseBloodTemper(temperName);
		if (temper == null) {
			source.sendFailure(HemoText.translatable("commands.hemomancy.temper.unknown_temper_valid", temperName, getValidTemperNames()));
			return 0;
		}

		IBloodTemperament bloodTemperament = HemoCapabilityAccess.getBloodTemperament(player);
		bloodTemperament.setTemperAlignment(temper, value);
		BloodTemperamentEvents.syncTemperament(player, bloodTemperament);

		source.sendSuccess(() -> Component.translatable("commands.hemomancy.temper.set_temper_to",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.literal(temper.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.DARK_RED),
				Component.literal(String.format("%.2f", bloodTemperament.getAlignmentByTemper(temper)))
						.withStyle(ChatFormatting.AQUA)),
				true);
		return 1;
	}

	private static void setAllTempers(ServerPlayer player, float value) {
		IBloodTemperament bloodTemperament = HemoCapabilityAccess.getBloodTemperament(player);
		for (EnumBloodTemper temper : EnumBloodTemper.values()) {
			bloodTemperament.setTemperAlignment(temper, value);
		}
		BloodTemperamentEvents.syncTemperament(player, bloodTemperament);
	}

	static EnumBloodTemper parseBloodTemper(String temperName) {
		try {
			return EnumBloodTemper.valueOf(temperName.trim().replace('-', '_').toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	static String getValidTemperNames() {
		StringBuilder builder = new StringBuilder();
		for (EnumBloodTemper temper : EnumBloodTemper.values()) {
			if (!builder.isEmpty()) {
				builder.append(", ");
			}
			builder.append(temper.name().toLowerCase(Locale.ROOT));
		}
		return builder.toString();
	}
}
