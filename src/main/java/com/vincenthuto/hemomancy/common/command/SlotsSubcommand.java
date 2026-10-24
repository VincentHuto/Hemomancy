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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

/** /hemo slots: read, equip and unequip manipulation slots. */
final class SlotsSubcommand {
	private SlotsSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("slots")
				.then(Commands.literal("get")
						.executes(ctx -> getSlots(ctx.getSource(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> getSlots(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("equip")
						.then(Commands.argument("manip", StringArgumentType.word())
								.executes(ctx -> equipManip(ctx.getSource(), ctx.getSource().getPlayerOrException(),
										StringArgumentType.getString(ctx, "manip")))))
				.then(Commands.literal("unequip")
						.then(Commands.argument("manip", StringArgumentType.word())
								.executes(ctx -> unequipManip(ctx.getSource(), ctx.getSource().getPlayerOrException(),
										StringArgumentType.getString(ctx, "manip")))));
	}

	private static int getSlots(CommandSourceStack source, ServerPlayer player) {
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
				;
		int maxSlots = ManipSlotHelper.getMaxSlots(player);
		java.util.List<String> equipped = known.getEquippedManipNames();
		int normalSlots = ManipulationEquipHelper.countNormalEquippedNames(equipped);
		MutableComponent msg = Component.literal("")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.translatable("commands.hemomancy.slots.slots").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(normalSlots + "/" + maxSlots).withStyle(ChatFormatting.DARK_RED));
		source.sendSuccess(() -> msg, false);
		for (int i = 0; i < equipped.size(); i++) {
			final int idx = i;
			source.sendSuccess(() -> Component.literal("  [" + idx + "] " + equipped.get(idx))
					.withStyle(ChatFormatting.AQUA), false);
		}
		return 1;
	}

	private static int equipManip(CommandSourceStack source, ServerPlayer player, String manipName) {
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
				;
		int maxSlots = ManipSlotHelper.getMaxSlots(player);
		if (known.equipManip(manipName, maxSlots)) {
			int normalSlots = ManipulationEquipHelper.countNormalEquippedNames(known.getEquippedManipNames());
			source.sendSuccess(() -> Component.translatable("commands.hemomancy.slots.equipped",
					Component.literal(manipName).withStyle(ChatFormatting.GREEN),
					Component.literal(" (" + normalSlots + "/" + maxSlots + ")")
							.withStyle(ChatFormatting.GRAY)),
					true);
		} else {
			int normalSlots = ManipulationEquipHelper.countNormalEquippedNames(known.getEquippedManipNames());
			source.sendFailure(HemoText.translatable("commands.hemomancy.slots.cannot_equip_no_free_slot_or",
					normalSlots,
					maxSlots));
		}
		return 1;
	}

	private static int unequipManip(CommandSourceStack source, ServerPlayer player, String manipName) {
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
				;
		if (known.unequipManip(manipName)) {
			source.sendSuccess(() -> Component.translatable("commands.hemomancy.slots.unequipped",
					Component.literal(manipName).withStyle(ChatFormatting.YELLOW)),
					true);
		} else {
			source.sendFailure(HemoText.translatable("commands.hemomancy.slots.manipulation_was_not_equipped", manipName));
		}
		return 1;
	}
}
