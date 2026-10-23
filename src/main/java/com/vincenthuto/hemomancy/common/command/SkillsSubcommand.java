package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.arguments.*;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.*;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.HemoMilestone;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.*;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncSkills;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

/** /hemo skills: read, set and reset the executor's skill points. */
final class SkillsSubcommand {
	private SkillsSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("skills")
						.then(Commands.literal("get")
						.executes(ctx -> getSkills(ctx.getSource(), ctx.getSource().getPlayerOrException())))
				.then(Commands.literal("setpoints")
						.then(Commands.argument("amount", IntegerArgumentType.integer(0))
								.executes(ctx -> setSkillPoints(ctx.getSource(), ctx.getSource().getPlayerOrException(),
										IntegerArgumentType.getInteger(ctx, "amount")))))
				.then(Commands.literal("reset")
						.executes(ctx -> resetSkills(ctx.getSource(), ctx.getSource().getPlayerOrException())));
	}

	private static int getSkills(CommandSourceStack source, ServerPlayer player) {
		SkillProgress progress = HemoCapabilityAccess.getSkillProgress(player);
		source.sendSuccess(() -> Component.literal("Skill Points: ")
				.append(Component.literal(String.valueOf(progress.getSkillPoints())).withStyle(ChatFormatting.AQUA)),
				false);
		source.sendSuccess(() -> Component.literal("Milestones: ")
				.append(Component.literal(progress.getCompletedMilestoneCount() + "/" + HemoMilestone.values().length)
						.withStyle(ChatFormatting.GOLD)),
				false);
		source.sendSuccess(() -> Component.literal("  Manip Uses: " + progress.getTotalManipulationUses()
				+ "  Kills: " + progress.getTotalKillsWithBlood()
				+ "  Rites: " + progress.getTotalRitesCompleted()
				+ "  Advancements: " + progress.getTotalHemoAdvancements())
				.withStyle(ChatFormatting.GRAY), false);
		return 1;
	}

	private static int setSkillPoints(CommandSourceStack source, ServerPlayer player, int amount) {
		HemoCapabilityAccess.getSkillProgress(player).setSkillPoints(amount);
		syncSkills(player);
		source.sendSuccess(() -> Component.literal("Set skill points to ")
				.append(Component.literal(String.valueOf(amount)).withStyle(ChatFormatting.AQUA)),
				true);
		return 1;
	}

	private static int resetSkills(CommandSourceStack source, ServerPlayer player) {
		HemoCapabilityAccess.getSkillProgress(player).reset();
		syncSkills(player);
		source.sendSuccess(() -> Component.literal("Reset all skills, milestones, and skill points").withStyle(ChatFormatting.YELLOW),
				true);
		return 1;
	}

	private static void syncSkills(ServerPlayer player) {
		PacketHandler.sendToPlayer(player, new PacketSyncSkills(HemoCapabilityAccess.getSkillProgress(player).toSyncTag()));
	}
}
