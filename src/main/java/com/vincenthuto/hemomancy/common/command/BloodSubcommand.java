package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.arguments.*;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
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

/** /hemo blood: read, set, fill and activate a player's blood volume. */
final class BloodSubcommand {
	private BloodSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("blood")
				.then(Commands.literal("get")
						.executes(ctx -> getBlood(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> getBlood(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("set")
						.then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
								.executes(ctx -> setBlood(ctx.getSource(), ctx.getSource().getPlayerOrException(),
										DoubleArgumentType.getDouble(ctx, "amount")))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> setBlood(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
												DoubleArgumentType.getDouble(ctx, "amount"))))))
				.then(Commands.literal("setmax")
						.then(Commands.argument("amount", DoubleArgumentType.doubleArg(1))
								.executes(ctx -> setMaxBlood(ctx.getSource(), ctx.getSource().getPlayerOrException(),
										DoubleArgumentType.getDouble(ctx, "amount")))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> setMaxBlood(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
												DoubleArgumentType.getDouble(ctx, "amount"))))))
				.then(Commands.literal("fill")
						.executes(ctx -> fillBlood(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> fillBlood(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("activate")
						.executes(ctx -> activateBlood(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> activateBlood(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));
	}

	private static int getBlood(CommandSourceStack source, ServerPlayer player) {
		IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player)
				;
		source.sendSuccess(() -> Component.literal("")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" Blood: ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(String.format("%.1f / %.1f", blood.getBloodVolume(), blood.getMaxBloodVolume()))
						.withStyle(ChatFormatting.DARK_RED))
				.append(Component.literal(" Active: ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(String.valueOf(blood.isActive()))
						.withStyle(blood.isActive() ? ChatFormatting.GREEN : ChatFormatting.RED)),
				false);
		return 1;
	}

	private static int setBlood(CommandSourceStack source, ServerPlayer player, double amount) {
		IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player)
				;
		blood.setBloodVolume(Math.min(amount, blood.getMaxBloodVolume()));
		BloodVolumeEvents.syncVolume(player, blood);
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" blood volume to "))
				.append(Component.literal(String.format("%.1f", blood.getBloodVolume())).withStyle(ChatFormatting.DARK_RED)),
				true);
		return 1;
	}

	private static int setMaxBlood(CommandSourceStack source, ServerPlayer player, double amount) {
		IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player)
				;
		blood.setMaxBloodVolume(amount);
		if (blood.getBloodVolume() > amount) {
			blood.setBloodVolume(amount);
		}
		BloodVolumeEvents.syncVolume(player, blood);
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" max blood volume to "))
				.append(Component.literal(String.format("%.1f", amount)).withStyle(ChatFormatting.DARK_RED)),
				true);
		return 1;
	}

	private static int fillBlood(CommandSourceStack source, ServerPlayer player) {
		IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player)
				;
		blood.setBloodVolume(blood.getMaxBloodVolume());
		BloodVolumeEvents.syncVolume(player, blood);
		source.sendSuccess(() -> Component.literal("Filled ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" blood to max ("))
				.append(Component.literal(String.format("%.1f", blood.getMaxBloodVolume())).withStyle(ChatFormatting.DARK_RED))
				.append(Component.literal(")")),
				true);
		return 1;
	}

	private static int activateBlood(CommandSourceStack source, ServerPlayer player) {
		IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player)
				;
		blood.toggleActive();
		BloodVolumeEvents.syncVolume(player, blood);
		source.sendSuccess(() -> Component.literal("Toggled ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" blood active: "))
				.append(Component.literal(String.valueOf(blood.isActive()))
						.withStyle(blood.isActive() ? ChatFormatting.GREEN : ChatFormatting.RED)),
				true);
		return 1;
	}
}
