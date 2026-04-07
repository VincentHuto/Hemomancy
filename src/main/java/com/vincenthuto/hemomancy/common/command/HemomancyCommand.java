package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.vincenthuto.hemomancy.common.capability.player.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.degree.IInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncSkills;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

/**
 * Console commands for testing hemomancy progression.
 * <pre>
 * /hemomancy blood get [player]
 * /hemomancy blood set &lt;amount&gt; [player]
 * /hemomancy blood setmax &lt;amount&gt; [player]
 * /hemomancy blood fill [player]
 * /hemomancy blood activate [player]
 * /hemomancy degree get [player]
 * /hemomancy degree set &lt;0-7&gt; [player]
 * /hemomancy skills get [player]
 * /hemomancy skills setpoints &lt;amount&gt; [player]
 * /hemomancy skills reset [player]
 * </pre>
 */
public class HemomancyCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("hemomancy")
				.requires(src -> src.hasPermission(2))

				// ── Blood Volume ──
				.then(Commands.literal("blood")
						.then(Commands.literal("get")
								.executes(ctx -> getBlood(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> getBlood(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
						.then(Commands.literal("set")
								.then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
										.executes(ctx -> setBlood(ctx.getSource(), ctx.getSource().getPlayerOrException(),
												DoubleArgumentType.getDouble(ctx, "amount")))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> setBlood(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
														DoubleArgumentType.getDouble(ctx, "amount"))))))
						.then(Commands.literal("setmax")
								.then(Commands.argument("amount", DoubleArgumentType.doubleArg(1))
										.executes(ctx -> setMaxBlood(ctx.getSource(), ctx.getSource().getPlayerOrException(),
												DoubleArgumentType.getDouble(ctx, "amount")))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> setMaxBlood(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
														DoubleArgumentType.getDouble(ctx, "amount"))))))
						.then(Commands.literal("fill")
								.executes(ctx -> fillBlood(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> fillBlood(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
						.then(Commands.literal("activate")
								.executes(ctx -> activateBlood(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> activateBlood(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))))))

				// ── Initiatory Degree ──
				.then(Commands.literal("degree")
						.then(Commands.literal("get")
								.executes(ctx -> getDegree(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> getDegree(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
						.then(Commands.literal("set")
								.then(Commands.argument("degree", IntegerArgumentType.integer(0, 7))
										.executes(ctx -> setDegree(ctx.getSource(), ctx.getSource().getPlayerOrException(),
												IntegerArgumentType.getInteger(ctx, "degree")))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> setDegree(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
														IntegerArgumentType.getInteger(ctx, "degree")))))))

				// ── Skill Points (global static state, not per-player) ──
				.then(Commands.literal("skills")
						.then(Commands.literal("get")
								.executes(ctx -> getSkills(ctx.getSource())))
						.then(Commands.literal("setpoints")
								.then(Commands.argument("amount", IntegerArgumentType.integer(0))
										.executes(ctx -> setSkillPoints(ctx.getSource(), ctx.getSource().getPlayerOrException(),
												IntegerArgumentType.getInteger(ctx, "amount")))))
						.then(Commands.literal("reset")
								.executes(ctx -> resetSkills(ctx.getSource(), ctx.getSource().getPlayerOrException()))))
		);
	}

	// ────────────── Blood Volume ──────────────

	private static int getBlood(CommandSourceStack source, ServerPlayer player) {
		IBloodVolume blood = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(IllegalStateException::new);
		source.sendSuccess(() -> Component.literal("")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" Blood: ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(String.format("%.1f / %.1f", blood.getBloodVolume(), blood.getMaxBloodVolume()))
						.withStyle(ChatFormatting.DARK_RED))
				.append(Component.literal(" Active: ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(String.valueOf(blood.isActive()))
						.withStyle(blood.isActive() ? ChatFormatting.GREEN : ChatFormatting.RED)),
				false);
		return 1;
	}

	private static int setBlood(CommandSourceStack source, ServerPlayer player, double amount) {
		IBloodVolume blood = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(IllegalStateException::new);
		blood.setBloodVolume(Math.min(amount, blood.getMaxBloodVolume()));
		BloodVolumeEvents.syncVolume(player, blood);
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" blood volume to "))
				.append(Component.literal(String.format("%.1f", blood.getBloodVolume())).withStyle(ChatFormatting.DARK_RED)),
				true);
		return 1;
	}

	private static int setMaxBlood(CommandSourceStack source, ServerPlayer player, double amount) {
		IBloodVolume blood = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(IllegalStateException::new);
		blood.setMaxBloodVolume(amount);
		if (blood.getBloodVolume() > amount) {
			blood.setBloodVolume(amount);
		}
		BloodVolumeEvents.syncVolume(player, blood);
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" max blood volume to "))
				.append(Component.literal(String.format("%.1f", amount)).withStyle(ChatFormatting.DARK_RED)),
				true);
		return 1;
	}

	private static int fillBlood(CommandSourceStack source, ServerPlayer player) {
		IBloodVolume blood = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(IllegalStateException::new);
		blood.setBloodVolume(blood.getMaxBloodVolume());
		BloodVolumeEvents.syncVolume(player, blood);
		source.sendSuccess(() -> Component.literal("Filled ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" blood to max ("))
				.append(Component.literal(String.format("%.1f", blood.getMaxBloodVolume())).withStyle(ChatFormatting.DARK_RED))
				.append(Component.literal(")")),
				true);
		return 1;
	}

	private static int activateBlood(CommandSourceStack source, ServerPlayer player) {
		IBloodVolume blood = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(IllegalStateException::new);
		blood.toggleActive();
		BloodVolumeEvents.syncVolume(player, blood);
		source.sendSuccess(() -> Component.literal("Toggled ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" blood active: "))
				.append(Component.literal(String.valueOf(blood.isActive()))
						.withStyle(blood.isActive() ? ChatFormatting.GREEN : ChatFormatting.RED)),
				true);
		return 1;
	}

	// ────────────── Initiatory Degree ──────────────

	private static int getDegree(CommandSourceStack source, ServerPlayer player) {
		IInitiatoryDegree degree = player.getCapability(InitiatoryDegreeProvider.DEGREE_CAPA)
				.orElseThrow(IllegalStateException::new);
		int num = degree.getDegreeNumber();
		EnumInitiatoryDegree enumDeg = EnumInitiatoryDegree.byNumber(num);
		String title = (enumDeg != null) ? enumDeg.getTitle() : "Uninitiated";
		source.sendSuccess(() -> Component.literal("")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" Degree: ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(num + " — " + title).withStyle(ChatFormatting.LIGHT_PURPLE)),
				false);
		return 1;
	}

	private static int setDegree(CommandSourceStack source, ServerPlayer player, int degreeNum) {
		IInitiatoryDegree degree = player.getCapability(InitiatoryDegreeProvider.DEGREE_CAPA)
				.orElseThrow(IllegalStateException::new);
		degree.setDegreeNumber(degreeNum);
		InitiatoryDegreeEvents.syncDegree(player, degree);
		EnumInitiatoryDegree enumDeg = EnumInitiatoryDegree.byNumber(degreeNum);
		String title = (enumDeg != null) ? enumDeg.getTitle() : "Uninitiated";
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" degree to "))
				.append(Component.literal(degreeNum + " — " + title).withStyle(ChatFormatting.LIGHT_PURPLE)),
				true);
		return 1;
	}

	// ────────────── Skill Points ──────────────

	private static int getSkills(CommandSourceStack source) {
		source.sendSuccess(() -> Component.literal("Skill Points: ")
				.append(Component.literal(String.valueOf(SkillPointInit.skillPoints)).withStyle(ChatFormatting.AQUA)),
				false);
		return 1;
	}

	private static int setSkillPoints(CommandSourceStack source, ServerPlayer player, int amount) {
		SkillPointInit.skillPoints = amount;
		syncSkills(player);
		source.sendSuccess(() -> Component.literal("Set skill points to ")
				.append(Component.literal(String.valueOf(amount)).withStyle(ChatFormatting.AQUA)),
				true);
		return 1;
	}

	private static int resetSkills(CommandSourceStack source, ServerPlayer player) {
		SkillPointInit.SKILL_TREE.clear();
		SkillPointInit.BASE.clear();
		SkillPointInit.skillPoints = 0;
		SkillPointInit.init();
		syncSkills(player);
		source.sendSuccess(() -> Component.literal("Reset all skills and skill points").withStyle(ChatFormatting.YELLOW),
				true);
		return 1;
	}

	private static void syncSkills(ServerPlayer player) {
		PacketHandler.CHANNELBLOODVOLUME.send(
				PacketDistributor.PLAYER.with(() -> player),
				new PacketSyncSkills(SkillPointInit.serializeAll()));
	}
}
