package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.PathMutualExclusionHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.IInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipSlotHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphlingEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.BloodTendencyEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.HemoMilestone;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.organs.EnumOrgan;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.organs.IVisceralOrgans;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineDisbandHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.event.worldevent.BloodMoonSavedData;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.PrimalMorphlingRules;
import com.vincenthuto.hemomancy.common.item.itemhandler.MorphlingJarItemHandler;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodMoon;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncPomeProgress;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncSkills;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Locale;

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
 * /hemo bloodline disband [player]
 *
 * ── Initiatory Degree ──
 * /hemo degree get [player]
 * /hemo degree set &lt;0-8&gt; [player]
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
 *
 * ── Visceral Organs ──
 * /hemo organs get [player]
 * /hemo organs set &lt;organ&gt; &lt;0-3&gt; [player]
 * /hemo organs reset [player]
 *
 * ── Blood Tendency ──
 * /hemo tendency get [player]
 * /hemo tendency reset [player]
 * /hemo tendency max [player]
 * /hemo tendency &lt;tendency&gt; &lt;value&gt; [player]
 *
 * ── Blood Moon ──
 * /hemo bloodmoon summon
 * /hemo bloodmoon cancel
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
				.then(Commands.literal("bloodline")
						.then(Commands.literal("disband")
								.executes(ctx -> disbandBloodline(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> disbandBloodline(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))))))

				// ── Initiatory Degree ──
				.then(Commands.literal("degree")
						.then(Commands.literal("get")
								.executes(ctx -> getDegree(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> getDegree(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
						.then(Commands.literal("set")
								.then(Commands.argument("degree", IntegerArgumentType.integer(0, 8))
										.executes(ctx -> setDegree(ctx.getSource(), ctx.getSource().getPlayerOrException(),
												IntegerArgumentType.getInteger(ctx, "degree")))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> setDegree(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
														IntegerArgumentType.getInteger(ctx, "degree")))))))

				// ── Skill Points (global static state, not per-player) ──
				.then(Commands.literal("morphling")
						.then(Commands.literal("stage")
								.then(Commands.literal("get")
										.executes(ctx -> getMorphlingStage(ctx.getSource(), ctx.getSource().getPlayerOrException()))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> getMorphlingStage(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
								.then(Commands.literal("set")
										.then(Commands.argument("stage", StringArgumentType.word())
												.suggests((ctx, builder) -> {
													for (int i = 0; i < MorphlingItem.MATURITY_NAMES.length; i++) {
														builder.suggest(String.valueOf(i));
														builder.suggest(MorphlingItem.MATURITY_NAMES[i].toLowerCase(Locale.ROOT));
													}
													return builder.buildFuture();
												})
												.executes(ctx -> setMorphlingStage(ctx.getSource(), ctx.getSource().getPlayerOrException(),
														StringArgumentType.getString(ctx, "stage")))
												.then(Commands.argument("player", EntityArgument.player())
														.executes(ctx -> setMorphlingStage(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
																StringArgumentType.getString(ctx, "stage"))))))
								.then(Commands.literal("next")
										.executes(ctx -> cycleMorphlingStage(ctx.getSource(), ctx.getSource().getPlayerOrException(), 1))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> cycleMorphlingStage(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), 1))))
								.then(Commands.literal("previous")
										.executes(ctx -> cycleMorphlingStage(ctx.getSource(), ctx.getSource().getPlayerOrException(), -1))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> cycleMorphlingStage(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), -1))))
								.then(Commands.literal("prev")
										.executes(ctx -> cycleMorphlingStage(ctx.getSource(), ctx.getSource().getPlayerOrException(), -1))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> cycleMorphlingStage(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), -1))))))

				.then(Commands.literal("qliphoth")
						.then(Commands.literal("pome")
								.then(Commands.literal("reset")
										.executes(ctx -> resetPomeProgress(ctx.getSource(), ctx.getSource().getPlayerOrException()))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> resetPomeProgress(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))))

				.then(Commands.literal("skills")
								.then(Commands.literal("get")
								.executes(ctx -> getSkills(ctx.getSource(), ctx.getSource().getPlayerOrException())))
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

				// ── Visceral Organs ──
				.then(Commands.literal("organs")
						.then(Commands.literal("get")
								.executes(ctx -> getOrgans(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> getOrgans(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
						.then(Commands.literal("set")
								.then(Commands.argument("organ", StringArgumentType.word())
										.suggests((ctx, builder) -> {
											for (EnumOrgan o : EnumOrgan.values()) {
												builder.suggest(o.name().toLowerCase());
											}
											return builder.buildFuture();
										})
										.then(Commands.argument("level", IntegerArgumentType.integer(0, 3))
												.executes(ctx -> setOrgan(ctx.getSource(), ctx.getSource().getPlayerOrException(),
														StringArgumentType.getString(ctx, "organ"),
														IntegerArgumentType.getInteger(ctx, "level")))
												.then(Commands.argument("player", EntityArgument.player())
														.executes(ctx -> setOrgan(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
																StringArgumentType.getString(ctx, "organ"),
																IntegerArgumentType.getInteger(ctx, "level")))))))
						.then(Commands.literal("reset")
								.executes(ctx -> resetOrgans(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> resetOrgans(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))))))

				// ── Blood Tendency ──
				.then(Commands.literal("tendency")
						.then(Commands.literal("get")
								.executes(ctx -> getTendency(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> getTendency(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
						.then(Commands.literal("reset")
								.executes(ctx -> resetTendency(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> resetTendency(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
						.then(Commands.literal("max")
								.executes(ctx -> maxTendency(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> maxTendency(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
						.then(Commands.argument("tendency", StringArgumentType.word())
								.suggests((ctx, builder) -> {
									for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
										builder.suggest(tendency.name().toLowerCase(Locale.ROOT));
									}
									return builder.buildFuture();
								})
								.then(Commands.argument("value", FloatArgumentType.floatArg(0))
										.executes(ctx -> setTendency(ctx.getSource(), ctx.getSource().getPlayerOrException(),
												StringArgumentType.getString(ctx, "tendency"),
												FloatArgumentType.getFloat(ctx, "value")))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> setTendency(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
														StringArgumentType.getString(ctx, "tendency"),
														FloatArgumentType.getFloat(ctx, "value"))))))
				)

				// ── Blood Moon ──
				.then(Commands.literal("bloodmoon")
						.then(Commands.literal("summon")
								.executes(ctx -> summonBloodMoon(ctx.getSource())))
						.then(Commands.literal("cancel")
								.executes(ctx -> cancelBloodMoon(ctx.getSource()))))

				// ── Manipulation Slots ──
				.then(Commands.literal("slots")
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
												StringArgumentType.getString(ctx, "manip"))))))
		);
	}

	// ═══════════════════ Blood Volume ═══════════════════

	private static int getBlood(CommandSourceStack source, ServerPlayer player) {
		IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player)
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
		IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player)
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
		IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player)
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
		IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player)
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
		IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player)
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

	private static int disbandBloodline(CommandSourceStack source, ServerPlayer player) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player)
				.orElseThrow(IllegalStateException::new);
		Bloodline localLine = volume.getBloodLine();

		if (!localLine.isValid()) {
			source.sendFailure(Component.literal(player.getName().getString() + " has no bloodline to disband."));
			return 0;
		}

		ServerLevel overworld = player.server.overworld();
		BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
		Bloodline globalLine = savedData.getBloodline(localLine.getBloodlineUUID());
		if (globalLine == null) {
			BloodlineDisbandHelper.removeOwnedSanctums(source.getServer(), localLine);
			volume.setBloodLine(Bloodline.NOBLOODLINE);
			BloodVolumeEvents.syncVolume(player, volume);
			BloodlineDisbandHelper.burnBloodlineLedgers(player, localLine);
			source.sendFailure(Component.literal(localLine.getName() + " was already missing from world data; cleared "
					+ player.getName().getString() + "'s local bloodline state."));
			return 0;
		}

		int playerCount = globalLine.getPlayerUUIDS().size();
		int npcCount = globalLine.getNpcMemberCount();
		BloodlineDisbandHelper.removeOwnedSanctums(source.getServer(), globalLine);
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

	// ═══════════════════ Initiatory Degree ═══════════════════

	private static int getDegree(CommandSourceStack source, ServerPlayer player) {
		IInitiatoryDegree degree = HemoCapabilityAccess.getInitiatoryDegree(player)
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
		IInitiatoryDegree degree = HemoCapabilityAccess.getInitiatoryDegree(player)
				.orElseThrow(IllegalStateException::new);
		degree.setDegreeNumber(degreeNum);
		InitiatoryDegreeEvents.syncDegree(player, degree);
		boolean resetUnstained = degreeNum > 0 && PathMutualExclusionHelper.resetUnstainedProgress(player);
		EnumInitiatoryDegree enumDeg = EnumInitiatoryDegree.byNumber(degreeNum);
		String title = (enumDeg != null) ? enumDeg.getTitle() : "Uninitiated";
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" degree to "))
				.append(Component.literal(degreeNum + " — " + title).withStyle(ChatFormatting.LIGHT_PURPLE))
				.append(resetUnstained
						? Component.literal("; unstained progress reset.").withStyle(ChatFormatting.GRAY)
						: Component.empty()),
				true);
		return 1;
	}

	// ═══════════════════ Skill Points ═══════════════════

	private static int getMorphlingStage(CommandSourceStack source, ServerPlayer player) {
		ItemStack stack = getEquippedMorphlingStack(source, player);
		if (stack.isEmpty()) {
			return 0;
		}
		int stage = MorphlingItem.getMaturityLevel(stack);
		source.sendSuccess(() -> Component.literal("")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" equipped morphling: ").withStyle(ChatFormatting.GRAY))
				.append(stack.getHoverName().copy().withStyle(ChatFormatting.DARK_GREEN))
				.append(Component.literal(" stage "))
				.append(Component.literal(stage + " - " + MorphlingItem.getMaturityName(stage))
						.withStyle(MorphlingItem.MATURITY_COLORS[stage])),
				false);
		return 1;
	}

	private static int setMorphlingStage(CommandSourceStack source, ServerPlayer player, String rawStage) {
		int stage = parseMorphlingStage(rawStage);
		if (stage < 0) {
			source.sendFailure(Component.literal("Unknown morphling stage: " + rawStage
					+ ". Use 0-5, unfed, fledgling, developing, mature, apex, or primal."));
			return 0;
		}
		return setMorphlingStage(source, player, stage);
	}

	private static int setMorphlingStage(CommandSourceStack source, ServerPlayer player, int stage) {
		stage = Math.max(0, Math.min(stage, PrimalMorphlingRules.PRIMAL_LEVEL));
		var cap = HemoCapabilityAccess.getEquippedMorphling(player)
				.orElseThrow(IllegalStateException::new);
		if (!cap.hasMorphling() || cap.getEquippedMorphling().isEmpty()) {
			source.sendFailure(Component.literal(player.getName().getString() + " has no equipped morphling."));
			return 0;
		}
		ItemStack updated = cap.getEquippedMorphling().copy();
		if (!(updated.getItem() instanceof MorphlingItem)) {
			source.sendFailure(Component.literal("Equipped stack is not a morphling: ")
					.append(updated.getHoverName()));
			return 0;
		}

		ItemStack original = cap.getEquippedMorphling().copy();
		applyMorphlingStage(updated, stage);
		boolean sourceUpdated = writeBackEquippedMorphlingSource(player, original, updated);
		cap.setEquippedMorphling(updated);
		EquippedMorphlingEvents.syncToClient(player);
		int actualStage = MorphlingItem.getMaturityLevel(updated);
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" equipped morphling to stage "))
				.append(Component.literal(actualStage + " - " + MorphlingItem.getMaturityName(actualStage))
						.withStyle(MorphlingItem.MATURITY_COLORS[actualStage]))
				.append(sourceUpdated
						? Component.literal(" and updated its jar item.").withStyle(ChatFormatting.GRAY)
						: Component.literal("; no matching jar item was found.").withStyle(ChatFormatting.YELLOW)),
				true);
		return 1;
	}

	private static int cycleMorphlingStage(CommandSourceStack source, ServerPlayer player, int delta) {
		ItemStack stack = getEquippedMorphlingStack(source, player);
		if (stack.isEmpty()) {
			return 0;
		}
		int current = MorphlingItem.getMaturityLevel(stack);
		int next = Math.floorMod(current + delta, PrimalMorphlingRules.PRIMAL_LEVEL + 1);
		return setMorphlingStage(source, player, next);
	}

	private static ItemStack getEquippedMorphlingStack(CommandSourceStack source, ServerPlayer player) {
		var cap = HemoCapabilityAccess.getEquippedMorphling(player)
				.orElseThrow(IllegalStateException::new);
		if (!cap.hasMorphling() || cap.getEquippedMorphling().isEmpty()) {
			source.sendFailure(Component.literal(player.getName().getString() + " has no equipped morphling."));
			return ItemStack.EMPTY;
		}
		ItemStack stack = cap.getEquippedMorphling();
		if (!(stack.getItem() instanceof MorphlingItem)) {
			source.sendFailure(Component.literal("Equipped stack is not a morphling: ")
					.append(stack.getHoverName()));
			return ItemStack.EMPTY;
		}
		return stack;
	}

	private static void applyMorphlingStage(ItemStack stack, int stage) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		int enzymeStage = Math.min(stage, PrimalMorphlingRules.APEX_LEVEL);
		tag.putFloat("EnzymePower", MorphlingItem.MATURITY_THRESHOLDS[enzymeStage]);
		tag.putInt("EnzymeFeedings", stage == 0 ? 0 : Math.max(tag.getInt("EnzymeFeedings"), 1));
		if (stage >= PrimalMorphlingRules.PRIMAL_LEVEL) {
			tag.putBoolean(MorphlingItem.PRIMALIZED_KEY, true);
		} else {
			tag.remove(MorphlingItem.PRIMALIZED_KEY);
		}
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	private static boolean writeBackEquippedMorphlingSource(ServerPlayer player, ItemStack original, ItemStack updated) {
		boolean changed = false;
		ItemStack inventoryJar = Hemomancy.findItemInPlayerInv(player, ItemMorphlingJar.class);
		if (!inventoryJar.isEmpty()) {
			changed |= updateMorphlingJarStack(inventoryJar, original, updated);
		}
		ItemStack scarJar = HemoCapabilityAccess.getScars(player)
				.map(scars -> scars.getStackInSlot(7))
				.filter(stack -> stack.getItem() instanceof ItemMorphlingJar)
				.orElse(ItemStack.EMPTY);
		if (!scarJar.isEmpty() && scarJar != inventoryJar) {
			changed |= updateMorphlingJarStack(scarJar, original, updated);
		}
		if (changed) {
			player.getInventory().setChanged();
			player.containerMenu.broadcastChanges();
		}
		return changed;
	}

	private static boolean updateMorphlingJarStack(ItemStack jarStack, ItemStack original, ItemStack updated) {
		var rawHandler = jarStack.getCapability(Capabilities.ItemHandler.ITEM);
		if (!(rawHandler instanceof MorphlingJarItemHandler handler)) {
			return false;
		}
		handler.load();

		int fallbackSlot = -1;
		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack candidate = handler.getStackInSlot(i);
			if (candidate.isEmpty()) {
				continue;
			}
			if (ItemStack.isSameItemSameComponents(candidate, original)) {
				handler.setStackInSlot(i, updated.copy());
				handler.save();
				return true;
			}
			if (fallbackSlot == -1 && candidate.is(original.getItem())) {
				fallbackSlot = i;
			} else if (fallbackSlot >= 0 && candidate.is(original.getItem())) {
				fallbackSlot = -2;
			}
		}

		if (fallbackSlot >= 0) {
			handler.setStackInSlot(fallbackSlot, updated.copy());
			handler.save();
			return true;
		}
		return false;
	}

	private static int parseMorphlingStage(String rawStage) {
		String stage = rawStage.toLowerCase(Locale.ROOT);
		try {
			int parsed = Integer.parseInt(stage);
			return parsed >= 0 && parsed <= PrimalMorphlingRules.PRIMAL_LEVEL ? parsed : -1;
		} catch (NumberFormatException ignored) {
		}
		for (int i = 0; i < MorphlingItem.MATURITY_NAMES.length; i++) {
			if (MorphlingItem.MATURITY_NAMES[i].equalsIgnoreCase(stage)) {
				return i;
			}
		}
		return switch (stage) {
			case "develop", "dev" -> 2;
			case "max" -> PrimalMorphlingRules.PRIMAL_LEVEL;
			default -> -1;
		};
	}

	private static int resetPomeProgress(CommandSourceStack source, ServerPlayer player) {
		IInitiatoryDegree degree = HemoCapabilityAccess.getInitiatoryDegree(player)
				.orElseThrow(IllegalStateException::new);
		degree.resetPomeCommunion();
		degree.setQliphothCommunionDone(false);
		InitiatoryDegreeEvents.syncDegree(player, degree);
		PacketHandler.sendToPlayer(player, new PacketSyncPomeProgress(0));
		source.sendSuccess(() -> Component.literal("Reset ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" Qliphoth pome progress and Communion gate.").withStyle(ChatFormatting.DARK_PURPLE)),
				true);
		return 1;
	}

	private static int getSkills(CommandSourceStack source, ServerPlayer player) {
		SkillProgress progress = HemoCapabilityAccess.requireSkillProgress(player);
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
		HemoCapabilityAccess.requireSkillProgress(player).setSkillPoints(amount);
		syncSkills(player);
		source.sendSuccess(() -> Component.literal("Set skill points to ")
				.append(Component.literal(String.valueOf(amount)).withStyle(ChatFormatting.AQUA)),
				true);
		return 1;
	}

	private static int resetSkills(CommandSourceStack source, ServerPlayer player) {
		HemoCapabilityAccess.requireSkillProgress(player).reset();
		syncSkills(player);
		source.sendSuccess(() -> Component.literal("Reset all skills, milestones, and skill points").withStyle(ChatFormatting.YELLOW),
				true);
		return 1;
	}

	private static void syncSkills(ServerPlayer player) {
		PacketHandler.sendToPlayer(player, new PacketSyncSkills(HemoCapabilityAccess.requireSkillProgress(player).toSyncTag()));
	}

	// ═══════════════════ Unstained Progression ═══════════════════

	private static int getUnstainedOverview(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = HemoCapabilityAccess.getUnstainedProgress(player)
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
		IUnstainedProgress cap = HemoCapabilityAccess.getUnstainedProgress(player)
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
		IUnstainedProgress cap = HemoCapabilityAccess.getUnstainedProgress(player)
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
		IUnstainedProgress cap = HemoCapabilityAccess.getUnstainedProgress(player)
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
		IUnstainedProgress cap = HemoCapabilityAccess.getUnstainedProgress(player)
				.orElseThrow(IllegalStateException::new);
		cap.setClarityUnlocked(!cap.hasClarityUnlocked());
		boolean resetHarbinger = PathMutualExclusionHelper.enforceHarbingerResetOnClarity(player, cap);
		UnstainedProgressEvents.syncProgress(player, cap);
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" clarityUnlocked: "))
				.append(Component.literal(String.valueOf(cap.hasClarityUnlocked()))
						.withStyle(cap.hasClarityUnlocked() ? ChatFormatting.GREEN : ChatFormatting.RED))
				.append(resetHarbinger
						? Component.literal("; Harbinger progress reset.").withStyle(ChatFormatting.GRAY)
						: Component.empty()),
				true);
		return 1;
	}

	private static int getClarity(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = HemoCapabilityAccess.getUnstainedProgress(player)
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
		IUnstainedProgress cap = HemoCapabilityAccess.getUnstainedProgress(player)
				.orElseThrow(IllegalStateException::new);
		cap.setClarity(value);
		boolean unlockedBySet = value > 0.0f && !cap.hasClarityUnlocked();
		if (unlockedBySet) {
			cap.setClarityUnlocked(true);
		}
		boolean resetHarbinger = PathMutualExclusionHelper.enforceHarbingerResetOnClarity(player, cap);
		UnstainedProgressEvents.syncProgress(player, cap);
		EnumClarityStage stage = EnumClarityStage.byClarity(cap.getClarity());
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" clarity to "))
				.append(Component.literal(String.format("%.1f", cap.getClarity())).withStyle(ChatFormatting.AQUA))
				.append(Component.literal(" (" + stage.getTitle() + ")").withStyle(ChatFormatting.GRAY))
				.append(unlockedBySet
						? Component.literal("; clarity unlocked.").withStyle(ChatFormatting.GRAY)
						: Component.empty())
				.append(resetHarbinger
						? Component.literal("; Harbinger progress reset.").withStyle(ChatFormatting.GRAY)
						: Component.empty()),
				true);
		return 1;
	}

	private static int resetUnstained(CommandSourceStack source, ServerPlayer player) {
		IUnstainedProgress cap = HemoCapabilityAccess.getUnstainedProgress(player)
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
		IUnstainedProgress cap = HemoCapabilityAccess.getUnstainedProgress(player)
				.orElseThrow(IllegalStateException::new);
		cap.setBegunPurification(true);
		cap.setPurity(100);
		cap.setClarityUnlocked(true);
		cap.setClarity(100);
		boolean resetHarbinger = PathMutualExclusionHelper.enforceHarbingerResetOnClarity(player, cap);
		UnstainedProgressEvents.syncProgress(player, cap);
		source.sendSuccess(() -> Component.literal("Maxed ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" unstained progress (Purified + Enlightened)").withStyle(ChatFormatting.GREEN))
				.append(resetHarbinger
						? Component.literal("; Harbinger progress reset.").withStyle(ChatFormatting.GRAY)
						: Component.empty()),
				true);
		return 1;
	}

	// ═══════════════════ Visceral Organs ═══════════════════

	private static int getOrgans(CommandSourceStack source, ServerPlayer player) {
		IVisceralOrgans organs = HemoCapabilityAccess.requireVisceralOrgans(player);
		source.sendSuccess(() -> {
			MutableComponent msg = Component.literal(player.getName().getString())
					.withStyle(ChatFormatting.GOLD)
					.append(Component.literal(" Organs:").withStyle(ChatFormatting.GRAY));
			for (EnumOrgan organ : EnumOrgan.values()) {
				int level = organs.getOrganLevel(organ);
				ChatFormatting color = level == 0 ? ChatFormatting.GRAY
						: level >= 3 ? ChatFormatting.GOLD : ChatFormatting.RED;
				msg = msg.append(Component.literal("\n  " + organ.getName() + " (T"
						+ organ.getTier() + "): Lv." + level + "/3").withStyle(color));
			}
			return msg;
		}, false);
		return 1;
	}

	private static int setOrgan(CommandSourceStack source, ServerPlayer player, String organName, int level) {
		EnumOrgan organ = EnumOrgan.byName(organName);
		if (organ == null) {
			// Try uppercase enum name
			try {
				organ = EnumOrgan.valueOf(organName.toUpperCase());
			} catch (IllegalArgumentException e) {
				source.sendFailure(Component.literal("Unknown organ: " + organName + ". Valid: spleen, liver, lungs, kidneys, heart"));
				return 0;
			}
		}
		IVisceralOrgans organs = HemoCapabilityAccess.requireVisceralOrgans(player);
		organs.setOrganLevel(organ, level);
		final String name = organ.getName();
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" " + name + " to level " + level).withStyle(ChatFormatting.DARK_RED)),
				true);
		return 1;
	}

	private static int resetOrgans(CommandSourceStack source, ServerPlayer player) {
		IVisceralOrgans organs = HemoCapabilityAccess.requireVisceralOrgans(player);
		organs.resetAll();
		source.sendSuccess(() -> Component.literal("Reset ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" all organs to level 0").withStyle(ChatFormatting.GREEN)),
				true);
		return 1;
	}

	// ═══════════════════ Blood Tendency ═══════════════════

	private static int getTendency(CommandSourceStack source, ServerPlayer player) {
		IBloodTendency bloodTendency = HemoCapabilityAccess.requireBloodTendency(player);
		float total = bloodTendency.getTotalAlignment();

		source.sendSuccess(() -> Component.literal("")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" Blood Tendencies:").withStyle(ChatFormatting.GRAY)), false);
		source.sendSuccess(() -> Component.literal("  Total: ")
				.append(Component.literal(String.format("%.2f", total)).withStyle(ChatFormatting.AQUA)), false);

		for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
			float value = bloodTendency.getAlignmentByTendency(tendency);
			float percent = total > 0.0f ? (value / total) * 100.0f : 0.0f;
			final EnumBloodTendency current = tendency;
			source.sendSuccess(() -> Component.literal("  ")
					.append(Component.literal(current.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.DARK_RED))
					.append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
					.append(Component.literal(String.format("%.2f", value)).withStyle(ChatFormatting.WHITE))
					.append(Component.literal(" (" + String.format("%.1f%%", percent) + ")").withStyle(ChatFormatting.GRAY)),
				false);
		}
		return 1;
	}

	private static int resetTendency(CommandSourceStack source, ServerPlayer player) {
		setAllTendencies(player, 0.0f);
		source.sendSuccess(() -> Component.literal("Reset ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" blood tendencies to 0").withStyle(ChatFormatting.GREEN)),
				true);
		return 1;
	}

	private static int maxTendency(CommandSourceStack source, ServerPlayer player) {
		setAllTendencies(player, 100.0f);
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" blood tendencies to 100").withStyle(ChatFormatting.GREEN)),
				true);
		return 1;
	}

	private static int setTendency(CommandSourceStack source, ServerPlayer player, String tendencyName, float value) {
		EnumBloodTendency tendency = parseBloodTendency(tendencyName);
		if (tendency == null) {
			source.sendFailure(Component.literal("Unknown tendency: " + tendencyName + ". Valid: " + getValidTendencyNames()));
			return 0;
		}

		IBloodTendency bloodTendency = HemoCapabilityAccess.requireBloodTendency(player);
		bloodTendency.setTendencyAlignment(tendency, value);
		BloodTendencyEvents.syncTendency(player, bloodTendency);

		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" "))
				.append(Component.literal(tendency.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.DARK_RED))
				.append(Component.literal(" tendency to "))
				.append(Component.literal(String.format("%.2f", bloodTendency.getAlignmentByTendency(tendency)))
						.withStyle(ChatFormatting.AQUA)),
				true);
		return 1;
	}

	private static void setAllTendencies(ServerPlayer player, float value) {
		IBloodTendency bloodTendency = HemoCapabilityAccess.requireBloodTendency(player);
		for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
			bloodTendency.setTendencyAlignment(tendency, value);
		}
		BloodTendencyEvents.syncTendency(player, bloodTendency);
	}

	private static EnumBloodTendency parseBloodTendency(String tendencyName) {
		try {
			return EnumBloodTendency.valueOf(tendencyName.trim().replace('-', '_').toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	private static String getValidTendencyNames() {
		StringBuilder builder = new StringBuilder();
		for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
			if (!builder.isEmpty()) {
				builder.append(", ");
			}
			builder.append(tendency.name().toLowerCase(Locale.ROOT));
		}
		return builder.toString();
	}

	// ═══════════════════ Manipulation Slots ═══════════════════

	private static int getSlots(CommandSourceStack source, ServerPlayer player) {
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
				.orElseThrow(IllegalStateException::new);
		int maxSlots = ManipSlotHelper.getMaxSlots(player);
		java.util.List<String> equipped = known.getEquippedManipNames();
		int normalSlots = ManipulationEquipHelper.countNormalEquippedNames(equipped);
		MutableComponent msg = Component.literal("")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" Slots: ").withStyle(ChatFormatting.GRAY))
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
				.orElseThrow(IllegalStateException::new);
		int maxSlots = ManipSlotHelper.getMaxSlots(player);
		if (known.equipManip(manipName, maxSlots)) {
			int normalSlots = ManipulationEquipHelper.countNormalEquippedNames(known.getEquippedManipNames());
			source.sendSuccess(() -> Component.literal("Equipped ")
					.append(Component.literal(manipName).withStyle(ChatFormatting.GREEN))
					.append(Component.literal(" (" + normalSlots + "/" + maxSlots + ")")
							.withStyle(ChatFormatting.GRAY)),
					true);
		} else {
			int normalSlots = ManipulationEquipHelper.countNormalEquippedNames(known.getEquippedManipNames());
			source.sendFailure(Component.literal("Cannot equip: no free slot or already equipped (" +
					normalSlots + "/" + maxSlots + ")"));
		}
		return 1;
	}

	private static int unequipManip(CommandSourceStack source, ServerPlayer player, String manipName) {
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
				.orElseThrow(IllegalStateException::new);
		if (known.unequipManip(manipName)) {
			source.sendSuccess(() -> Component.literal("Unequipped ")
					.append(Component.literal(manipName).withStyle(ChatFormatting.YELLOW)),
					true);
		} else {
			source.sendFailure(Component.literal("Manipulation '" + manipName + "' was not equipped."));
		}
		return 1;
	}

	// ═══════════════════ Blood Moon ═══════════════════

	private static int summonBloodMoon(CommandSourceStack source) {
		ServerLevel overworld = source.getServer().overworld();
		BloodMoonSavedData data = BloodMoonSavedData.get(overworld);
		if (data.isActive()) {
			source.sendFailure(Component.literal("A Blood Moon is already active."));
			return 0;
		}
		data.start(overworld.getGameTime() + 11900L);
		for (ServerPlayer p : overworld.getPlayers(ServerPlayer::isAlive)) {
			p.sendSystemMessage(Component.translatable("hemomancy.blood_moon.start")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
		}
		PacketDistributor.sendToAllPlayers(new PacketSyncBloodMoon(true));
		source.sendSuccess(() -> Component.literal("Blood Moon summoned.")
				.withStyle(ChatFormatting.DARK_RED), true);
		return 1;
	}

	private static int cancelBloodMoon(CommandSourceStack source) {
		ServerLevel overworld = source.getServer().overworld();
		BloodMoonSavedData data = BloodMoonSavedData.get(overworld);
		if (!data.isActive()) {
			source.sendFailure(Component.literal("No Blood Moon is currently active."));
			return 0;
		}
		data.stop();
		for (ServerPlayer p : overworld.getPlayers(ServerPlayer::isAlive)) {
			p.sendSystemMessage(Component.translatable("hemomancy.blood_moon.end")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		}
		PacketDistributor.sendToAllPlayers(new PacketSyncBloodMoon(false));
		source.sendSuccess(() -> Component.literal("Blood Moon cancelled.")
				.withStyle(ChatFormatting.GRAY), true);
		return 1;
	}
}
