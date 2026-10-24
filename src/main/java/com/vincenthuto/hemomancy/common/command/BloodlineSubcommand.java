package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.arguments.*;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.*;
import com.vincenthuto.hemomancy.common.capability.player.unstained.*;
import com.vincenthuto.hemomancy.common.util.HemoText;
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
			source.sendFailure(HemoText.translatable("commands.hemomancy.bloodline.has_no_bloodline_to_disband", player.getName().getString()));
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
			source.sendFailure(HemoText.translatable("commands.hemomancy.bloodline.was_already_missing_from_world_data",
					localLine.getName(),
					player.getName().getString()));
			return 0;
		}

		int playerCount = globalLine.getPlayerUUIDS().size();
		int npcCount = globalLine.getNpcMemberCount();
		BloodlineDisbandHelper.removeOwnedFanes(source.getServer(), globalLine);
		savedData.disbandBloodline(globalLine.getBloodlineUUID());
		int onlineReset = BloodlineDisbandHelper.resetOnlineMembers(source.getServer(), globalLine,
				member -> Component.translatable("commands.hemomancy.bloodline.your_bloodline_has_been_disbanded").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));

		source.sendSuccess(() -> Component.translatable("commands.hemomancy.bloodline.disbanded_for",
				Component.literal(globalLine.getName()).withStyle(ChatFormatting.DARK_RED),
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				HemoText.translatable("commands.hemomancy.bloodline.player_npc_online_reset",
		playerCount,
		(playerCount == 1 ? "" : "s"),
		npcCount,
		(npcCount == 1 ? "" : "s"),
		onlineReset).withStyle(ChatFormatting.GRAY)),
				true);
		return 1;
	}
}
