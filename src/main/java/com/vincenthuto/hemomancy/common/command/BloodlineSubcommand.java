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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

/** /hemo bloodline disband: dissolve a player's bloodline and its Fanes. */
final class BloodlineSubcommand {
	private BloodlineSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("bloodline")
				.then(Commands.literal("disband")
						.executes(ctx -> disbandBloodline(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> disbandBloodline(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));
	}

	private static int disbandBloodline(CommandSourceStack source, ServerPlayer player) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player)
				;
		Bloodline localLine = volume.getBloodLine();

		if (!localLine.isValid()) {
			source.sendFailure(Component.literal(player.getName().getString() + " has no bloodline to disband."));
			return 0;
		}

		ServerLevel overworld = player.server.overworld();
		BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
		Bloodline globalLine = savedData.getBloodline(localLine.getBloodlineUUID());
		if (globalLine == null) {
			BloodlineDisbandHelper.removeOwnedFanes(source.getServer(), localLine);
			volume.setBloodLine(Bloodline.NOBLOODLINE);
			BloodVolumeEvents.syncVolume(player, volume);
			BloodlineDisbandHelper.burnBloodlineLedgers(player, localLine);
			source.sendFailure(Component.literal(localLine.getName() + " was already missing from world data; cleared "
					+ player.getName().getString() + "'s local bloodline state."));
			return 0;
		}

		int playerCount = globalLine.getPlayerUUIDS().size();
		int npcCount = globalLine.getNpcMemberCount();
		BloodlineDisbandHelper.removeOwnedFanes(source.getServer(), globalLine);
		savedData.disbandBloodline(globalLine.getBloodlineUUID());
		int onlineReset = BloodlineDisbandHelper.resetOnlineMembers(source.getServer(), globalLine,
				member -> Component.literal("Your bloodline has been disbanded.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));

		source.sendSuccess(() -> Component.literal("Disbanded ")
				.append(Component.literal(globalLine.getName()).withStyle(ChatFormatting.DARK_RED))
				.append(Component.literal(" for "))
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" (" + playerCount + " player" + (playerCount == 1 ? "" : "s")
						+ ", " + npcCount + " npc" + (npcCount == 1 ? "" : "s")
						+ "; " + onlineReset + " online reset).")
						.withStyle(ChatFormatting.GRAY)),
				true);
		return 1;
	}
}
