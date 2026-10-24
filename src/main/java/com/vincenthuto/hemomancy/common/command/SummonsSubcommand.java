package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.IKnownSummons;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.KnownSummonEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.*;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

/** /hemo summons: list, grant and remove Puppeteer summons. */
final class SummonsSubcommand {
	private SummonsSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("summons")
				.then(Commands.literal("list")
						.executes(ctx -> listKnownSummons(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> listKnownSummons(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("add")
						.then(Commands.argument("summon", StringArgumentType.word())
								.suggests((ctx, builder) -> suggestPuppeteerSummons(builder))
								.executes(ctx -> addKnownSummon(ctx.getSource(), ctx.getSource().getPlayerOrException(),
										StringArgumentType.getString(ctx, "summon")))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> addKnownSummon(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
												StringArgumentType.getString(ctx, "summon"))))))
				.then(Commands.literal("remove")
						.then(Commands.argument("summon", StringArgumentType.word())
								.suggests((ctx, builder) -> suggestPuppeteerSummons(builder))
								.executes(ctx -> removeKnownSummon(ctx.getSource(), ctx.getSource().getPlayerOrException(),
										StringArgumentType.getString(ctx, "summon")))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> removeKnownSummon(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
												StringArgumentType.getString(ctx, "summon"))))))
				.then(Commands.literal("clear")
						.executes(ctx -> clearKnownSummons(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> clearKnownSummons(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));
	}

	private static int listKnownSummons(CommandSourceStack source, ServerPlayer player) {
		List<String> names = HemoCapabilityAccess.getKnownSummons(player).getKnownSummonNames();
		String display = names.isEmpty() ? "none" : String.join(", ", names);
		source.sendSuccess(() -> Component.literal(player.getName().getString())
				.append(Component.translatable("commands.hemomancy.summons.known_puppeteer_summons").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(display).withStyle(names.isEmpty() ? ChatFormatting.DARK_GRAY : ChatFormatting.GREEN)),
				false);
		return Math.max(1, names.size());
	}

	private static int addKnownSummon(CommandSourceStack source, ServerPlayer player, String summonName) {
		IKnownSummons known = HemoCapabilityAccess.getKnownSummons(player);
		List<String> current = new ArrayList<>(known.getKnownSummonNames());
		int added;
		if ("all".equals(summonName)) {
			List<String> everySummon = PuppeteerSummonDefinitions.all().stream()
					.map(definition -> definition.name()).toList();
			added = (int) everySummon.stream().filter(name -> !current.contains(name)).count();
			known.setKnownSummonNames(everySummon);
		} else {
			var definition = PuppeteerSummonDefinitions.byName(summonName).orElse(null);
			if (definition == null) {
				source.sendFailure(HemoText.translatable("commands.hemomancy.summons.unknown_puppeteer_summon_valid",
						summonName,
						puppeteerSummonList()));
				return 0;
			}
			if (current.contains(definition.name())) {
				source.sendFailure(HemoText.translatable("commands.hemomancy.summons.already_knows",
						player.getName().getString(),
						definition.name()));
				return 0;
			}
			current.add(definition.name());
			known.setKnownSummonNames(current);
			added = 1;
		}
		KnownSummonEvents.sync(player, known);
		int changed = added;
		source.sendSuccess(() -> HemoText.translatable("commands.hemomancy.summons.added_puppeteer_summon_for",
				changed,
				(changed == 1 ? "" : "s"),
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD)), true);
		return Math.max(1, added);
	}

	private static int removeKnownSummon(CommandSourceStack source, ServerPlayer player, String summonName) {
		if ("all".equals(summonName)) {
			return clearKnownSummons(source, player);
		}
		var definition = PuppeteerSummonDefinitions.byName(summonName).orElse(null);
		if (definition == null) {
			source.sendFailure(HemoText.translatable("commands.hemomancy.summons.unknown_puppeteer_summon_valid",
					summonName,
					puppeteerSummonList()));
			return 0;
		}
		IKnownSummons known = HemoCapabilityAccess.getKnownSummons(player);
		List<String> remaining = new ArrayList<>(known.getKnownSummonNames());
		if (!remaining.remove(definition.name())) {
			source.sendFailure(HemoText.translatable("commands.hemomancy.summons.does_not_know",
					player.getName().getString(),
					definition.name()));
			return 0;
		}
		known.setKnownSummonNames(remaining);
		KnownSummonEvents.sync(player, known);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.summons.removed_from",
				Component.literal(definition.name()).withStyle(ChatFormatting.RED),
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD)), true);
		return 1;
	}

	private static int clearKnownSummons(CommandSourceStack source, ServerPlayer player) {
		IKnownSummons known = HemoCapabilityAccess.getKnownSummons(player);
		int removed = known.getKnownSummonNames().size();
		known.setKnownSummonNames(List.of());
		KnownSummonEvents.sync(player, known);
		source.sendSuccess(() -> HemoText.translatable("commands.hemomancy.summons.cleared_known_puppeteer_summon_from",
				removed,
				(removed == 1 ? "" : "s"),
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD)), true);
		return Math.max(1, removed);
	}

	private static CompletableFuture<Suggestions> suggestPuppeteerSummons(SuggestionsBuilder builder) {
		builder.suggest("all");
		for (var definition : PuppeteerSummonDefinitions.all()) {
			builder.suggest(definition.name());
		}
		return builder.buildFuture();
	}

	private static String puppeteerSummonList() {
		return String.join(", ", PuppeteerSummonDefinitions.all().stream()
				.map(definition -> definition.name()).toList()) + ", all";
	}
}
