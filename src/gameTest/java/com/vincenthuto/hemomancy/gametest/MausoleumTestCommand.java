package com.vincenthuto.hemomancy.gametest;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Locale;

/**
 * {@code /hemo test mausoleum <saint> [seed]}: builds that Saint's complex with its entrance on the ground at the
 * caller's feet, as natural generation would lay it out for the seed (the world seed by default). For ad-hoc checks in
 * any world; the review client and the assembly GameTests use the same {@link MausoleumAssembly}.
 */
final class MausoleumTestCommand {
	private MausoleumTestCommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("mausoleum")
				.then(Commands.argument("saint", StringArgumentType.word())
						.suggests((context, builder) -> {
							Arrays.stream(EnumSaintType.values())
									.forEach(saint -> builder.suggest(saint.name().toLowerCase(Locale.ROOT)));
							return builder.buildFuture();
						})
						.executes(context -> spawn(context.getSource(), StringArgumentType.getString(context, "saint"),
								context.getSource().getLevel().getSeed()))
						.then(Commands.argument("seed", LongArgumentType.longArg())
								.executes(context -> spawn(context.getSource(),
										StringArgumentType.getString(context, "saint"),
										LongArgumentType.getLong(context, "seed")))));
	}

	private static int spawn(CommandSourceStack source, String name, long seed) {
		EnumSaintType saint = Arrays.stream(EnumSaintType.values())
				.filter(type -> type.name().equalsIgnoreCase(name)).findFirst().orElse(null);
		if (saint == null) {
			source.sendFailure(Component.literal("No Saint named " + name));
			return 0;
		}
		BlockPos surface = BlockPos.containing(source.getPosition());
		var layout = MausoleumAssembly.layout(source.getLevel(), saint, surface, seed);
		if (layout.isEmpty()) {
			source.sendFailure(Component.literal("The " + name + " start piece did not place"));
			return 0;
		}
		MausoleumAssembly.build(source.getLevel(), layout.get());
		source.sendSuccess(() -> Component.literal("Built mausoleum_" + name + " (" + layout.get().pieces().pieces().size()
				+ " pieces: " + String.join(", ", layout.get().byTemplate().keySet()) + ") at " + surface.toShortString()
				+ ", seed " + seed), true);
		return 1;
	}
}
