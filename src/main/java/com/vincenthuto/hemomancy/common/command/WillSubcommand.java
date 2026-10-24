package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.unstained.*;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillAnchorEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillCompositionRules;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillOrigin;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

/** /hemo will ambush: summon a Will ambush anchor or immediate Wills. */
final class WillSubcommand {
	private WillSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("will")
				.then(Commands.literal("ambush")
						.then(Commands.literal("anchor")
								.then(Commands.argument("school", StringArgumentType.word())
										.suggests((ctx, builder) -> suggestBloodTendencies(builder))
										.then(Commands.argument("tier", IntegerArgumentType.integer(1, 4))
												.then(Commands.argument("broken_count", IntegerArgumentType.integer(0, 8))
														.then(Commands.argument("sent_present", BoolArgumentType.bool())
																.executes(ctx -> summonWillAmbushAnchor(ctx.getSource(),
																		ctx.getSource().getPlayerOrException(),
																		TendencySubcommand.parseBloodTendency(StringArgumentType.getString(ctx, "school")),
																		IntegerArgumentType.getInteger(ctx, "tier"),
																		IntegerArgumentType.getInteger(ctx, "broken_count"),
																		BoolArgumentType.getBool(ctx, "sent_present")))
																.then(Commands.argument("player", EntityArgument.player())
																		.executes(ctx -> summonWillAmbushAnchor(ctx.getSource(),
																				EntityArgument.getPlayer(ctx, "player"),
																				TendencySubcommand.parseBloodTendency(StringArgumentType.getString(ctx, "school")),
																				IntegerArgumentType.getInteger(ctx, "tier"),
																				IntegerArgumentType.getInteger(ctx, "broken_count"),
																				BoolArgumentType.getBool(ctx, "sent_present")))))))))
						.then(Commands.literal("immediate")
								.then(Commands.argument("school", StringArgumentType.word())
										.suggests((ctx, builder) -> suggestBloodTendencies(builder))
										.then(Commands.argument("tier", IntegerArgumentType.integer(1, 4))
												.then(Commands.argument("origin", StringArgumentType.word())
														.suggests((ctx, builder) -> suggestWillOrigins(builder))
														.executes(ctx -> summonWillAmbushImmediate(ctx.getSource(),
																ctx.getSource().getPlayerOrException(),
																TendencySubcommand.parseBloodTendency(StringArgumentType.getString(ctx, "school")),
																IntegerArgumentType.getInteger(ctx, "tier"),
																parseWillOrigin(StringArgumentType.getString(ctx, "origin")),
																1))
														.then(Commands.argument("count", IntegerArgumentType.integer(1, 8))
																.executes(ctx -> summonWillAmbushImmediate(ctx.getSource(),
																		ctx.getSource().getPlayerOrException(),
																		TendencySubcommand.parseBloodTendency(StringArgumentType.getString(ctx, "school")),
																		IntegerArgumentType.getInteger(ctx, "tier"),
																		parseWillOrigin(StringArgumentType.getString(ctx, "origin")),
																		IntegerArgumentType.getInteger(ctx, "count")))
																.then(Commands.argument("player", EntityArgument.player())
																		.executes(ctx -> summonWillAmbushImmediate(ctx.getSource(),
																				EntityArgument.getPlayer(ctx, "player"),
																				TendencySubcommand.parseBloodTendency(StringArgumentType.getString(ctx, "school")),
																				IntegerArgumentType.getInteger(ctx, "tier"),
																				parseWillOrigin(StringArgumentType.getString(ctx, "origin")),
																				IntegerArgumentType.getInteger(ctx, "count"))))))))));
	}

	private static CompletableFuture<Suggestions> suggestBloodTendencies(SuggestionsBuilder builder) {
		for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
			builder.suggest(tendency.name().toLowerCase(Locale.ROOT));
		}
		return builder.buildFuture();
	}

	private static CompletableFuture<Suggestions> suggestWillOrigins(SuggestionsBuilder builder) {
		for (WillOrigin origin : WillOrigin.values()) {
			builder.suggest(origin.name().toLowerCase(Locale.ROOT));
		}
		return builder.buildFuture();
	}

	private static WillOrigin parseWillOrigin(String originName) {
		try {
			return WillOrigin.valueOf(originName.trim().replace('-', '_').toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	private static int summonWillAmbushAnchor(CommandSourceStack source, ServerPlayer player,
			EnumBloodTendency school, int tier, int brokenCount, boolean sentPresent) {
		if (school == null) {
			source.sendFailure(HemoText.translatable("commands.hemomancy.will.unknown_will_school_valid", TendencySubcommand.getValidTendencyNames()));
			return 0;
		}
		ServerLevel level = source.getLevel();
		WillAnchorEntity anchor = EntityInit.will_anchor.get().create(level);
		if (anchor == null) {
			source.sendFailure(Component.translatable("commands.hemomancy.will.could_not_create_will_anchor"));
			return 0;
		}
		Vec3 pos = source.getPosition();
		WillCompositionRules.Composition composition =
				new WillCompositionRules.Composition(tier, brokenCount, Math.min(2, tier), sentPresent);
		anchor.moveTo(pos.x, pos.y, pos.z, player.getYRot(), 0.0F);
		anchor.configure(school, composition, player);
		level.addFreshEntity(anchor);
		source.sendSuccess(() -> HemoText.translatable("commands.hemomancy.will.summoned_will_ambush_anchor_tier_broken",
				Component.literal(school.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.DARK_RED),
				tier,
				brokenCount,
				sentPresent,
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD)),
				true);
		return 1;
	}

	private static int summonWillAmbushImmediate(CommandSourceStack source, ServerPlayer player,
			EnumBloodTendency school, int tier, WillOrigin origin, int count) {
		if (school == null) {
			source.sendFailure(HemoText.translatable("commands.hemomancy.will.unknown_will_school_valid", TendencySubcommand.getValidTendencyNames()));
			return 0;
		}
		if (origin == null) {
			source.sendFailure(Component.translatable("commands.hemomancy.will.unknown_will_origin_valid_broken_sent"));
			return 0;
		}
		ServerLevel level = source.getLevel();
		Vec3 pos = source.getPosition();
		int spawned = 0;
		for (int i = 0; i < count; i++) {
			WillEntity will = EntityInit.will.get().create(level);
			if (will == null) continue;
			double offsetX = (i % 3 - 1) * 1.35D;
			double offsetZ = (i / 3) * 1.35D;
			will.moveTo(pos.x + offsetX, pos.y, pos.z + offsetZ, player.getYRot(), 0.0F);
			will.configure(origin, school, tier, player, true);
			level.addFreshEntity(will);
			spawned++;
		}
		if (spawned <= 0) {
			source.sendFailure(Component.translatable("commands.hemomancy.will.could_not_create_any_wills"));
			return 0;
		}
		final int spawnedCount = spawned;
		source.sendSuccess(() -> HemoText.translatable("commands.hemomancy.will.summoned_immediate_will_s_tier_target",
				Component.literal(String.valueOf(spawnedCount)).withStyle(ChatFormatting.AQUA),
				Component.literal(origin.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.GRAY),
				Component.literal(school.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.DARK_RED),
				tier,
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD)),
				true);
		return 1;
	}
}
