package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.arguments.*;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.organs.EnumOrgan;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.organs.IVisceralOrgans;
import com.vincenthuto.hemomancy.common.capability.player.unstained.*;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

/** /hemo organs: read, set and reset a player's organ levels. */
final class OrgansSubcommand {
	private OrgansSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("organs")
				.then(Commands.literal("get")
						.executes(ctx -> getOrgans(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> getOrgans(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("set")
						.then(Commands.argument("organ", StringArgumentType.word())
								.suggests((ctx, builder) -> {
									for (EnumOrgan o : EnumOrgan.values()) {
										builder.suggest(o.name().toLowerCase());
									}
									return builder.buildFuture();
								})
								.then(Commands.argument("level", IntegerArgumentType.integer(0, 3))
										.executes(ctx -> setOrgan(ctx.getSource(), ctx.getSource().getPlayerOrException(),
												StringArgumentType.getString(ctx, "organ"),
												IntegerArgumentType.getInteger(ctx, "level")))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> setOrgan(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
														StringArgumentType.getString(ctx, "organ"),
														IntegerArgumentType.getInteger(ctx, "level")))))))
				.then(Commands.literal("reset")
						.executes(ctx -> resetOrgans(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> resetOrgans(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));
	}

	private static int getOrgans(CommandSourceStack source, ServerPlayer player) {
		IVisceralOrgans organs = HemoCapabilityAccess.getVisceralOrgans(player);
		source.sendSuccess(() -> {
			MutableComponent msg = Component.literal(player.getName().getString())
					.withStyle(ChatFormatting.GOLD)
					.append(Component.translatable("commands.hemomancy.organs.organs").withStyle(ChatFormatting.GRAY));
			for (EnumOrgan organ : EnumOrgan.values()) {
				int level = organs.getOrganLevel(organ);
				ChatFormatting color = level == 0 ? ChatFormatting.GRAY
						: level >= 3 ? ChatFormatting.GOLD : ChatFormatting.RED;
				msg = msg.append(HemoText.translatable("commands.hemomancy.organs.t_lv_3",
						organ.getDisplayName(),
						organ.getTier(),
						level).withStyle(color));
			}
			return msg;
		}, false);
		return 1;
	}

	private static int setOrgan(CommandSourceStack source, ServerPlayer player, String organName, int level) {
		EnumOrgan organ = EnumOrgan.byName(organName);
		if (organ == null) {
			try {
				organ = EnumOrgan.valueOf(organName.toUpperCase());
			} catch (IllegalArgumentException e) {
				source.sendFailure(HemoText.translatable("commands.hemomancy.organs.unknown_organ_valid_spleen_liver_lungs", organName));
				return 0;
			}
		}
		IVisceralOrgans organs = HemoCapabilityAccess.getVisceralOrgans(player);
		organs.setOrganLevel(organ, level);
		final Component name = organ.getDisplayName();
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.organs.set",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				HemoText.translatable("commands.hemomancy.organs.to_level", name, level).withStyle(ChatFormatting.DARK_RED)),
				true);
		return 1;
	}

	private static int resetOrgans(CommandSourceStack source, ServerPlayer player) {
		IVisceralOrgans organs = HemoCapabilityAccess.getVisceralOrgans(player);
		organs.resetAll();
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.organs.reset",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.translatable("commands.hemomancy.organs.all_organs_to_level_0").withStyle(ChatFormatting.GREEN)),
				true);
		return 1;
	}
}
