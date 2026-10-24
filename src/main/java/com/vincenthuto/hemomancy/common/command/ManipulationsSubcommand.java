package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.*;
import com.vincenthuto.hemomancy.common.capability.player.unstained.*;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry;
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

/** /hemo manipulations: list, grant and remove manipulations, and set the selected one's level and xp. */
final class ManipulationsSubcommand {
	private ManipulationsSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("manipulations")
				.then(Commands.literal("list")
						.executes(ctx -> listKnownManipulations(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> listKnownManipulations(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("add")
						.then(Commands.argument("manipulation", StringArgumentType.word())
								.suggests((ctx, builder) -> suggestManipulations(builder))
								.executes(ctx -> addKnownManipulation(ctx.getSource(), ctx.getSource().getPlayerOrException(),
										StringArgumentType.getString(ctx, "manipulation")))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> addKnownManipulation(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
												StringArgumentType.getString(ctx, "manipulation"))))))
				.then(Commands.literal("remove")
						.then(Commands.argument("manipulation", StringArgumentType.word())
								.suggests((ctx, builder) -> suggestManipulations(builder))
								.executes(ctx -> removeKnownManipulation(ctx.getSource(), ctx.getSource().getPlayerOrException(),
										StringArgumentType.getString(ctx, "manipulation")))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> removeKnownManipulation(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
												StringArgumentType.getString(ctx, "manipulation"))))))
				.then(Commands.literal("clear")
						.executes(ctx -> clearKnownManipulations(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> clearKnownManipulations(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("level")
						.then(Commands.argument("level", IntegerArgumentType.integer(0, ManipLevel.MAX_LEVEL))
								.executes(ctx -> setSelectedManipulationLevel(ctx.getSource(),
										ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx, "level")))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> setSelectedManipulationLevel(ctx.getSource(),
												EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "level"))))))
				.then(Commands.literal("xp")
						.then(Commands.argument("xp", DoubleArgumentType.doubleArg(0.0D))
								.executes(ctx -> setSelectedManipulationXp(ctx.getSource(),
										ctx.getSource().getPlayerOrException(), DoubleArgumentType.getDouble(ctx, "xp")))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> setSelectedManipulationXp(ctx.getSource(),
												EntityArgument.getPlayer(ctx, "player"), DoubleArgumentType.getDouble(ctx, "xp"))))));
	}

	private static int listKnownManipulations(CommandSourceStack source, ServerPlayer player) {
		List<String> names = HemoCapabilityAccess.getKnownManipulations(player).getKnownManips().keySet().stream()
				.map(BloodManipulation::getName).sorted().toList();
		String display = names.isEmpty() ? "none" : String.join(", ", names);
		source.sendSuccess(() -> Component.literal(player.getName().getString())
				.append(Component.translatable("commands.hemomancy.manipulations.known_manipulations").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(display).withStyle(names.isEmpty() ? ChatFormatting.DARK_GRAY : ChatFormatting.GREEN)),
				false);
		return Math.max(1, names.size());
	}

	private static int addKnownManipulation(CommandSourceStack source, ServerPlayer player, String manipulationName) {
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		LinkedHashMap<BloodManipulation, ManipLevel> updated = new LinkedHashMap<>(known.getKnownManips());
		int added = 0;
		if ("all".equals(manipulationName)) {
			for (BloodManipulation manipulation : availableManipulations()) {
				ManipLevel mastery = findManipulationLevel(updated, manipulation);
				if (mastery == null) {
					updated.put(manipulation, maxMastery());
					added++;
				} else {
					mastery.setCurrentLevel(ManipLevel.MAX_LEVEL);
					mastery.setXp(maxMasteryXp());
				}
			}
		} else {
			BloodManipulation manipulation = ManipulationInit.getByName(manipulationName);
			if (!isAvailableManipulation(manipulation)) {
				source.sendFailure(HemoText.translatable("commands.hemomancy.manipulations.unknown_manipulation_use_tab_completion_or",
						manipulationName));
				return 0;
			}
			if (known.doesListContainName(updated, manipulation)) {
				source.sendFailure(HemoText.translatable("commands.hemomancy.manipulations.already_knows",
						player.getName().getString(),
						manipulation.getName()));
				return 0;
			}
			updated.put(manipulation, new ManipLevel(0, 0));
			added = 1;
		}
		known.setKnownManips(updated);
		syncKnownManipulations(player);
		int changed = added;
		source.sendSuccess(() -> HemoText.translatable("commands.hemomancy.manipulations.added_manipulation_for",
				changed,
				(changed == 1 ? "" : "s"),
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD)), true);
		return Math.max(1, added);
	}

	private static ManipLevel findManipulationLevel(LinkedHashMap<BloodManipulation, ManipLevel> knownManips,
			BloodManipulation manipulation) {
		for (Map.Entry<BloodManipulation, ManipLevel> entry : knownManips.entrySet()) {
			if (entry.getKey() != null && entry.getKey().getName().equals(manipulation.getName())) {
				return entry.getValue();
			}
		}
		return null;
	}

	private static ManipLevel maxMastery() {
		return new ManipLevel(ManipLevel.MAX_LEVEL, maxMasteryXp());
	}

	private static double maxMasteryXp() {
		return Arrays.stream(ManipLevel.XP_THRESHOLDS).sum();
	}

	private static int removeKnownManipulation(CommandSourceStack source, ServerPlayer player,
			String manipulationName) {
		if ("all".equals(manipulationName)) {
			return clearKnownManipulations(source, player);
		}
		BloodManipulation manipulation = ManipulationInit.getByName(manipulationName);
		if (!isAvailableManipulation(manipulation)) {
			source.sendFailure(HemoText.translatable("commands.hemomancy.manipulations.unknown_manipulation_use_tab_completion_or",
					manipulationName));
			return 0;
		}
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		boolean removed = known.getKnownManips().entrySet().removeIf(entry ->
				manipulation.getName().equals(entry.getKey().getName()));
		if (!removed) {
			source.sendFailure(HemoText.translatable("commands.hemomancy.manipulations.does_not_know",
					player.getName().getString(),
					manipulation.getName()));
			return 0;
		}
		if (known.getSelectedManip() != null
				&& manipulation.getName().equals(known.getSelectedManip().getName())) {
			known.setSelectedManip(BloodManipulation.BLANK);
		}
		List<String> equipped = new ArrayList<>(known.getEquippedManipNames());
		equipped.removeIf(manipulation.getName()::equals);
		known.setEquippedManipNames(equipped);
		syncKnownManipulations(player);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.manipulations.removed_from",
				Component.literal(manipulation.getName()).withStyle(ChatFormatting.RED),
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD)), true);
		return 1;
	}

	private static int clearKnownManipulations(CommandSourceStack source, ServerPlayer player) {
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		int removed = known.getKnownManips().size();
		known.setKnownManips(new LinkedHashMap<>());
		known.setSelectedManip(BloodManipulation.BLANK);
		known.setEquippedManipNames(List.of());
		syncKnownManipulations(player);
		source.sendSuccess(() -> HemoText.translatable("commands.hemomancy.manipulations.cleared_known_manipulation_from",
				removed,
				(removed == 1 ? "" : "s"),
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD)), true);
		return Math.max(1, removed);
	}

	private static int setSelectedManipulationLevel(CommandSourceStack source, ServerPlayer player, int level) {
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		ManipLevel mastery = selectedEquippedManipulationLevel(known);
		if (mastery == null) {
			source.sendFailure(HemoText.translatable("commands.hemomancy.manipulations.does_not_have_a_selected_equipped",
					player.getName().getString()));
			return 0;
		}
		BloodManipulation selected = known.getSelectedManip();
		known.setSelectedManipLevel(level);
		syncKnownManipulations(player);
		source.sendSuccess(() -> HemoText.translatable("commands.hemomancy.manipulations.set_s_mastery_level_to",
				player.getName().getString(),
				selected.getName(),
				mastery.getCurrentLevel()), true);
		return 1;
	}

	private static int setSelectedManipulationXp(CommandSourceStack source, ServerPlayer player, double xp) {
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		ManipLevel mastery = selectedEquippedManipulationLevel(known);
		if (mastery == null) {
			source.sendFailure(HemoText.translatable("commands.hemomancy.manipulations.does_not_have_a_selected_equipped",
					player.getName().getString()));
			return 0;
		}
		BloodManipulation selected = known.getSelectedManip();
		mastery.setXp(xp);
		syncKnownManipulations(player);
		source.sendSuccess(() -> HemoText.translatable("commands.hemomancy.manipulations.set_s_raw_mastery_xp_to",
				player.getName().getString(),
				selected.getName(),
				mastery.getXp()), true);
		return 1;
	}

	private static ManipLevel selectedEquippedManipulationLevel(IKnownManipulations known) {
		if (known.getKnownManips().isEmpty()) return null;
		BloodManipulation selected = known.getSelectedManip();
		return selected != null && known.isManipEquipped(selected) ? known.getManipLevel(selected) : null;
	}

	private static List<BloodManipulation> availableManipulations() {
		Map<String, BloodManipulation> available = new LinkedHashMap<>();
		for (BloodManipulation manipulation : ManipulationInit.getAllEntries()) {
			addAvailableManipulation(available, manipulation);
		}
		for (var family : ManipulationFamilyRegistry.families()) {
			addAvailableManipulation(available, ManipulationInit.getByName(family.baselineId()));
			for (var form : family.forms()) {
				addAvailableManipulation(available, ManipulationInit.getByName(form.id()));
			}
		}
		return available.values().stream()
				.sorted(Comparator.comparing(BloodManipulation::getName)).toList();
	}

	private static void addAvailableManipulation(Map<String, BloodManipulation> available,
			BloodManipulation manipulation) {
		if (isAvailableManipulation(manipulation)) {
			available.putIfAbsent(manipulation.getName(), manipulation);
		}
	}

	private static boolean isAvailableManipulation(BloodManipulation manipulation) {
		return manipulation != null && manipulation != BloodManipulation.BLANK
				&& !ManipulationRetirementRules.isRetiredManipulation(manipulation);
	}

	private static CompletableFuture<Suggestions> suggestManipulations(SuggestionsBuilder builder) {
		builder.suggest("all");
		for (BloodManipulation manipulation : availableManipulations()) {
			builder.suggest(manipulation.getName());
		}
		return builder.buildFuture();
	}

	private static void syncKnownManipulations(ServerPlayer player) {
		KnownManipulationEvents.syncPlayerEvent(player);
		ManipulationDiagnosticsSync.sync(player);
	}
}
