package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.vincenthuto.hemomancy.common.capability.player.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.degree.IInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressProvider;
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
 * Unified console commands for testing all progression systems.
 * Everything lives under the {@code /hemo} root.
 * <pre>
 * ── Blood ──
 * /hemo blood get [player]
 * /hemo blood set &lt;amount&gt; [player]
 * /hemo blood setmax &lt;amount&gt; [player]
 * /hemo blood fill [player]
 * /hemo blood activate [player]
 *
 * ── Initiatory Degree ──
 * /hemo degree get [player]
 * /hemo degree set &lt;0-7&gt; [player]
 *
 * ── Skill Points (global static state) ──
 * /hemo skills get
 * /hemo skills setpoints &lt;amount&gt;
 * /hemo skills reset
 *
 * ── Unstained Progression ──
 * /hemo unstained get [player]
 * /hemo unstained begin [player]
 * /hemo unstained purity get [player]
 * /hemo unstained purity set &lt;0-100&gt; [player]
 * /hemo unstained clarity unlock [player]
 * /hemo unstained clarity get [player]
 * /hemo unstained clarity set &lt;0-100&gt; [player]
 * /hemo unstained reset [player]
 * /hemo unstained max [player]
 * </pre>
 */
public class HemoCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("hemo")
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

				// ── Unstained Progression ──
				.then(Commands.literal("unstained")
						.then(Commands.literal("get")
								.executes(ctx -> getUnstainedOverview(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> getUnstainedOverview(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
						.then(Commands.literal("begin")
								.executes(ctx -> toggleBegun(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> toggleBegun(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
						.then(Commands.literal("purity")
								.then(Commands.literal("get")
										.executes(ctx -> getPurity(ctx.getSource(), ctx.getSource().getPlayerOrException()))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> getPurity(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
								.then(Commands.literal("set")
										.then(Commands.argument("value", FloatArgumentType.floatArg(0, 100))
												.executes(ctx -> setPurity(ctx.getSource(), ctx.getSource().getPlayerOrException(),
														FloatArgumentType.getFloat(ctx, "value")))
												.then(Commands.argument("player", EntityArgument.player())
														.executes(ctx -> setPurity(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
																FloatArgumentType.getFloat(ctx, "value")))))))
						.then(Commands.literal("clarity")
								.then(Commands.literal("unlock")
										.executes(ctx -> toggleClarityUnlock(ctx.getSource(), ctx.getSource().getPlayerOrException()))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> toggleClarityUnlock(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
								.then(Commands.literal("get")
										.executes(ctx -> getClarity(ctx.getSource(), ctx.getSource().getPlayerOrException()))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> getClarity(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
								.then(Commands.literal("set")
										.then(Commands.argument("value", FloatArgumentType.floatArg(0, 100))
												.executes(ctx -> setClarity(ctx.getSource(), ctx.getSource().getPlayerOrException(),
														FloatArgumentType.getFloat(ctx, "value")))
												.then(Commands.argument("player", EntityArgument.player())
														.executes(ctx -> setClarity(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
																FloatArgumentType.getFloat(ctx, "value")))))))
						.then(Commands.literal("reset")
								.executes(ctx -> resetUnstained(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> resetUnstained(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
						.then(Commands.literal("max")
								.executes(ctx -> maxUnstained(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> maxUnstained(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))))))
		);
	}

	// ═══════════════════ Blood Volume ═══════════════════

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

	// ═══════════════════ Initiatory Degree ═══════════════════

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

	// ═══════════════════ Skill Points ═══════════════════

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

	// ═══════════════════ Unstained Progression ═══════════════════

	private static int getUnstainedOverview(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
				.orElseThrow(IllegalStateException::new);
		EnumPurityStage pStage = EnumPurityStage.byPurity(cap.getPurity());
		EnumClarityStage cStage = EnumClarityStage.byClarity(cap.getClarity());

		source.sendSuccess(() -> Component.literal("")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" Unstained Progress:").withStyle(ChatFormatting.WHITE)), false);
		source.sendSuccess(() -> Component.literal("  Begun: ")
				.append(Component.literal(String.valueOf(cap.hasBegunPurification()))
						.withStyle(cap.hasBegunPurification() ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
		source.sendSuccess(() -> Component.literal("  Purity: ")
				.append(Component.literal(String.format("%.1f", cap.getPurity())).withStyle(ChatFormatting.WHITE))
				.append(Component.literal(" (" + pStage.getTitle() + ")").withStyle(ChatFormatting.GRAY)), false);
		source.sendSuccess(() -> Component.literal("  Clarity Unlocked: ")
				.append(Component.literal(String.valueOf(cap.hasClarityUnlocked()))
						.withStyle(cap.hasClarityUnlocked() ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
		source.sendSuccess(() -> Component.literal("  Clarity: ")
				.append(Component.literal(String.format("%.1f", cap.getClarity())).withStyle(ChatFormatting.AQUA))
				.append(Component.literal(" (" + cStage.getTitle() + ")").withStyle(ChatFormatting.GRAY)), false);
		source.sendSuccess(() -> Component.literal("  Silver Ward: ")
				.append(Component.literal(String.format("%.0f%%", cap.getSilverWardStrength() * 100))
						.withStyle(ChatFormatting.YELLOW)), false);
		source.sendSuccess(() -> Component.literal("  Verdigris Aura: ")
				.append(Component.literal(String.format("%.0f%%", cap.getVerdigrisAura() * 100))
						.withStyle(ChatFormatting.DARK_AQUA)), false);
		return 1;
	}

	private static int toggleBegun(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
				.orElseThrow(IllegalStateException::new);
		cap.setBegunPurification(!cap.hasBegunPurification());
		UnstainedProgressEvents.syncProgress(player, cap);
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" begunPurification: "))
				.append(Component.literal(String.valueOf(cap.hasBegunPurification()))
						.withStyle(cap.hasBegunPurification() ? ChatFormatting.GREEN : ChatFormatting.RED)),
				true);
		return 1;
	}

	private static int getPurity(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
				.orElseThrow(IllegalStateException::new);
		EnumPurityStage stage = EnumPurityStage.byPurity(cap.getPurity());
		source.sendSuccess(() -> Component.literal("")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" Purity: ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(String.format("%.1f", cap.getPurity())).withStyle(ChatFormatting.WHITE))
				.append(Component.literal(" (" + stage.getTitle() + ")").withStyle(ChatFormatting.GRAY)),
				false);
		return 1;
	}

	private static int setPurity(CommandSourceStack source, ServerPlayer player, float value) {
		IUnstainedProgress cap = player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
				.orElseThrow(IllegalStateException::new);
		cap.setPurity(value);
		UnstainedProgressEvents.syncProgress(player, cap);
		EnumPurityStage stage = EnumPurityStage.byPurity(cap.getPurity());
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" purity to "))
				.append(Component.literal(String.format("%.1f", cap.getPurity())).withStyle(ChatFormatting.WHITE))
				.append(Component.literal(" (" + stage.getTitle() + ")").withStyle(ChatFormatting.GRAY)),
				true);
		return 1;
	}

	private static int toggleClarityUnlock(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
				.orElseThrow(IllegalStateException::new);
		cap.setClarityUnlocked(!cap.hasClarityUnlocked());
		UnstainedProgressEvents.syncProgress(player, cap);
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" clarityUnlocked: "))
				.append(Component.literal(String.valueOf(cap.hasClarityUnlocked()))
						.withStyle(cap.hasClarityUnlocked() ? ChatFormatting.GREEN : ChatFormatting.RED)),
				true);
		return 1;
	}

	private static int getClarity(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
				.orElseThrow(IllegalStateException::new);
		EnumClarityStage stage = EnumClarityStage.byClarity(cap.getClarity());
		source.sendSuccess(() -> Component.literal("")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" Clarity: ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(String.format("%.1f", cap.getClarity())).withStyle(ChatFormatting.AQUA))
				.append(Component.literal(" (" + stage.getTitle() + ")").withStyle(ChatFormatting.GRAY)),
				false);
		return 1;
	}

	private static int setClarity(CommandSourceStack source, ServerPlayer player, float value) {
		IUnstainedProgress cap = player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
				.orElseThrow(IllegalStateException::new);
		cap.setClarity(value);
		UnstainedProgressEvents.syncProgress(player, cap);
		EnumClarityStage stage = EnumClarityStage.byClarity(cap.getClarity());
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" clarity to "))
				.append(Component.literal(String.format("%.1f", cap.getClarity())).withStyle(ChatFormatting.AQUA))
				.append(Component.literal(" (" + stage.getTitle() + ")").withStyle(ChatFormatting.GRAY)),
				true);
		return 1;
	}

	private static int resetUnstained(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
				.orElseThrow(IllegalStateException::new);
		cap.setBegunPurification(false);
		cap.setPurity(0);
		cap.setClarityUnlocked(false);
		cap.setClarity(0);
		UnstainedProgressEvents.syncProgress(player, cap);
		source.sendSuccess(() -> Component.literal("Reset ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" unstained progress to zero").withStyle(ChatFormatting.YELLOW)),
				true);
		return 1;
	}

	private static int maxUnstained(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
				.orElseThrow(IllegalStateException::new);
		cap.setBegunPurification(true);
		cap.setPurity(100);
		cap.setClarityUnlocked(true);
		cap.setClarity(100);
		UnstainedProgressEvents.syncProgress(player, cap);
		source.sendSuccess(() -> Component.literal("Maxed ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" unstained progress (Purified + Enlightened)").withStyle(ChatFormatting.GREEN)),
				true);
		return 1;
	}
}
