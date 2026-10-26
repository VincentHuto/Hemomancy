package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * The {@code /hemo} operator command (permission 2): debug and test controls for player progression and world
 * events. Each branch is its own class, in this package, whose {@code build()} returns the branch:
 * {@code /hemo blood}, {@code bloodline}, {@code degree}, {@code morphling}, {@code qliphoth}, {@code skills},
 * {@code unstained}, {@code organs}, {@code tendency}, {@code bloodmoon}, {@code fane}, {@code chamber}, {@code will},
 * {@code summons}, {@code manipulations}, {@code slots}, {@code mycophant} and {@code vesper}. Most paths take an optional trailing player and act on
 * the executor without one. The syntax is pinned node by node by {@code HemoCommandTreeTest}.
 */
public final class HemoCommand {
	private HemoCommand() {
	}

	/** Registers /hemo: one root that operators (permission 2) may use, with one class per subcommand. */
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("hemo")
				.requires(src -> src.hasPermission(2))
				.then(BloodSubcommand.build())
				.then(BloodlineSubcommand.build())
				.then(DegreeSubcommand.build())
				.then(MorphlingSubcommand.build())
				.then(QliphothSubcommand.build())
				.then(SkillsSubcommand.build())
				.then(UnstainedSubcommand.build())
				.then(OrgansSubcommand.build())
				.then(TendencySubcommand.build())
				.then(BloodMoonSubcommand.build())
				.then(FaneSubcommand.build())
				.then(ChamberSubcommand.build())
				.then(WillSubcommand.build())
				.then(SummonsSubcommand.build())
				.then(ManipulationsSubcommand.build())
				.then(SlotsSubcommand.build())
				.then(MycophantSubcommand.build())
				.then(VesperSubcommand.build()));
	}
}
