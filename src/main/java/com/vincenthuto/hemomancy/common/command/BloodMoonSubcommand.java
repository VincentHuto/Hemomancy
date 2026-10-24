package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.arguments.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.*;
import com.vincenthuto.hemomancy.common.capability.player.unstained.*;
import com.vincenthuto.hemomancy.common.event.worldevent.BloodMoonSavedData;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodMoon;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

/** /hemo bloodmoon: summon or cancel the Blood Moon. */
final class BloodMoonSubcommand {
	private BloodMoonSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("bloodmoon")
				.then(Commands.literal("summon")
						.executes(ctx -> summonBloodMoon(ctx.getSource())))
				.then(Commands.literal("cancel")
						.executes(ctx -> cancelBloodMoon(ctx.getSource())));
	}

	private static int summonBloodMoon(CommandSourceStack source) {
		ServerLevel overworld = source.getServer().overworld();
		BloodMoonSavedData data = BloodMoonSavedData.get(overworld);
		if (data.isActive()) {
			source.sendFailure(Component.translatable("commands.hemomancy.blood_moon.a_blood_moon_is_already_active"));
			return 0;
		}
		data.start(overworld.getGameTime() + 11900L);
		for (ServerPlayer p : overworld.getPlayers(ServerPlayer::isAlive)) {
			p.sendSystemMessage(Component.translatable("hemomancy.blood_moon.start")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
		}
		PacketHandler.sendToAll(new PacketSyncBloodMoon(true));
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.blood_moon.blood_moon_summoned").withStyle(ChatFormatting.DARK_RED), true);
		return 1;
	}

	private static int cancelBloodMoon(CommandSourceStack source) {
		ServerLevel overworld = source.getServer().overworld();
		BloodMoonSavedData data = BloodMoonSavedData.get(overworld);
		if (!data.isActive()) {
			source.sendFailure(Component.translatable("commands.hemomancy.blood_moon.no_blood_moon_is_currently_active"));
			return 0;
		}
		data.stop();
		for (ServerPlayer p : overworld.getPlayers(ServerPlayer::isAlive)) {
			p.sendSystemMessage(Component.translatable("hemomancy.blood_moon.end")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		}
		PacketHandler.sendToAll(new PacketSyncBloodMoon(false));
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.blood_moon.blood_moon_cancelled").withStyle(ChatFormatting.GRAY), true);
		return 1;
	}
}
