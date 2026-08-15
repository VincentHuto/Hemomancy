package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.PathMutualExclusionHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.IInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationDiagnosticsSync;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationRetirementRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipSlotHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphlingEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.IKnownSummons;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.KnownSummonEvents;
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
import com.vincenthuto.hemomancy.common.block.harbinger.functional.QliphothBloomBlock;
import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.event.LastRiteHelper;
import com.vincenthuto.hemomancy.common.event.worldevent.BloodMoonSavedData;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneEvents;
import com.vincenthuto.hemomancy.common.event.worldevent.FaneBoundaryRelation;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillAnchorEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillCompositionRules;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillOrigin;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.PrimalMorphlingRules;
import com.vincenthuto.hemomancy.common.item.itemhandler.MorphlingJarItemHandler;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodMoon;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncPomeProgress;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncSkills;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.tile.functional.QliphothBloomBlockEntity;
import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

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
 * â”€â”€ Puppeteer Summons â”€â”€
 * /hemo summons list [player]
 * /hemo summons add &lt;summon|all&gt; [player]
 * /hemo summons remove &lt;summon|all&gt; [player]
 * /hemo summons clear [player]
 *
 * â”€â”€ Known Manipulations â”€â”€
 * /hemo manipulations list [player]
 * /hemo manipulations add &lt;manipulation|all&gt; [player]
 * /hemo manipulations remove &lt;manipulation|all&gt; [player]
 * /hemo manipulations clear [player]
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
 *
 * Qliphoth Tree Preview:
 * /hemo qliphoth tree &lt;initial|1-9|fully_grown|pruned|sealed&gt;
 * /hemo qliphoth archon &lt;pending|complete|clear&gt; [player]
 * </pre>
 */
public class HemoCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("hemo")
				.requires(src -> src.hasPermission(2))

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
						.then(Commands.literal("tree")
								.then(Commands.argument("stage", StringArgumentType.word())
										.suggests((ctx, builder) -> {
											for (QliphothTreeStage stage : QliphothTreeStage.values()) {
												builder.suggest(stage.commandName());
											}
											return builder.buildFuture();
										})
										.executes(ctx -> spawnQliphothTree(ctx.getSource(),
												ctx.getSource().getPlayerOrException(),
												StringArgumentType.getString(ctx, "stage")))))
						.then(Commands.literal("archon")
								.then(silentArchonStateCommand("pending"))
								.then(silentArchonStateCommand("complete"))
								.then(silentArchonStateCommand("clear")))
						.then(Commands.literal("pome")
								.then(Commands.literal("set")
										.then(Commands.argument("count", IntegerArgumentType.integer(0, 9))
												.executes(ctx -> setPomeProgress(ctx.getSource(),
														ctx.getSource().getPlayerOrException(),
														IntegerArgumentType.getInteger(ctx, "count")))
												.then(Commands.argument("player", EntityArgument.player())
														.executes(ctx -> setPomeProgress(ctx.getSource(),
																EntityArgument.getPlayer(ctx, "player"),
																IntegerArgumentType.getInteger(ctx, "count"))))))
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

				.then(Commands.literal("bloodmoon")
						.then(Commands.literal("summon")
								.executes(ctx -> summonBloodMoon(ctx.getSource())))
						.then(Commands.literal("cancel")
								.executes(ctx -> cancelBloodMoon(ctx.getSource()))))

				.then(Commands.literal("fane")
						.then(Commands.literal("preview")
								.then(Commands.literal("member")
										.executes(ctx -> setFanePreview(ctx.getSource(), FaneBoundaryRelation.MEMBER)))
								.then(Commands.literal("mundane")
										.executes(ctx -> setFanePreview(ctx.getSource(),
												FaneBoundaryRelation.MUNDANE_OUTSIDER)))
								.then(Commands.literal("outsider")
										.executes(ctx -> setFanePreview(ctx.getSource(), FaneBoundaryRelation.OUTSIDER)))
								.then(Commands.literal("rival")
										.executes(ctx -> setFanePreview(ctx.getSource(), FaneBoundaryRelation.RIVAL_ELDER)))
								.then(Commands.literal("clear")
										.executes(ctx -> clearFanePreview(ctx.getSource())))))

				.then(Commands.literal("chamber")
						.then(Commands.literal("theme")
								.then(Commands.literal("cycle")
										.executes(ctx -> cycleChamberTheme(ctx.getSource(),
												ctx.getSource().getPlayerOrException(), 1))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> cycleChamberTheme(ctx.getSource(),
														EntityArgument.getPlayer(ctx, "player"), 1))))
								.then(Commands.literal("next")
										.executes(ctx -> cycleChamberTheme(ctx.getSource(),
												ctx.getSource().getPlayerOrException(), 1))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> cycleChamberTheme(ctx.getSource(),
														EntityArgument.getPlayer(ctx, "player"), 1))))
								.then(Commands.literal("previous")
										.executes(ctx -> cycleChamberTheme(ctx.getSource(),
												ctx.getSource().getPlayerOrException(), -1))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> cycleChamberTheme(ctx.getSource(),
														EntityArgument.getPlayer(ctx, "player"), -1))))
								.then(Commands.literal("prev")
										.executes(ctx -> cycleChamberTheme(ctx.getSource(),
												ctx.getSource().getPlayerOrException(), -1))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> cycleChamberTheme(ctx.getSource(),
														EntityArgument.getPlayer(ctx, "player"), -1))))
								.then(Commands.literal("set")
										.then(Commands.argument("theme", StringArgumentType.word())
												.suggests((ctx, builder) -> {
											for (ResourceLocation id : ChamberOfWillManager.commandSkyThemes()) {
														builder.suggest(id.getPath());
													}
													return builder.buildFuture();
												})
												.executes(ctx -> setChamberTheme(ctx.getSource(),
														ctx.getSource().getPlayerOrException(),
														StringArgumentType.getString(ctx, "theme")))
												.then(Commands.argument("player", EntityArgument.player())
														.executes(ctx -> setChamberTheme(ctx.getSource(),
																EntityArgument.getPlayer(ctx, "player"),
																StringArgumentType.getString(ctx, "theme"))))))
								.then(Commands.literal("reset")
										.executes(ctx -> resetChamberTheme(ctx.getSource(),
												ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> resetChamberTheme(ctx.getSource(),
												EntityArgument.getPlayer(ctx, "player"))))))
						.then(Commands.literal("size")
								.then(Commands.literal("set")
										.then(Commands.argument("radius", IntegerArgumentType.integer(
												ChamberOfWillManager.BASE_ROOM_RADIUS, ChamberOfWillManager.MAX_ROOM_RADIUS))
												.suggests((ctx, builder) -> {
													for (int radius = ChamberOfWillManager.BASE_ROOM_RADIUS;
															radius <= ChamberOfWillManager.MAX_ROOM_RADIUS; radius++) {
														builder.suggest(radius);
													}
													return builder.buildFuture();
												})
												.executes(ctx -> setChamberSize(ctx.getSource(),
														ctx.getSource().getPlayerOrException(),
														IntegerArgumentType.getInteger(ctx, "radius")))
												.then(Commands.argument("player", EntityArgument.player())
														.executes(ctx -> setChamberSize(ctx.getSource(),
																EntityArgument.getPlayer(ctx, "player"),
																IntegerArgumentType.getInteger(ctx, "radius"))))))
								.then(Commands.literal("reset")
										.executes(ctx -> resetChamberSize(ctx.getSource(),
												ctx.getSource().getPlayerOrException()))
										.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> resetChamberSize(ctx.getSource(),
												EntityArgument.getPlayer(ctx, "player")))))))

				.then(Commands.literal("will")
						.then(Commands.literal("ambush")
								.then(Commands.literal("anchor")
										.then(Commands.argument("school", StringArgumentType.word())
												.suggests((ctx, builder) -> suggestBloodTendencies(builder))
												.then(Commands.argument("tier", IntegerArgumentType.integer(1, 4))
														.then(Commands.argument("broken_count", IntegerArgumentType.integer(0, 8))
																.then(Commands.argument("sent_present", BoolArgumentType.bool())
																		.executes(ctx -> summonWillAmbushAnchor(ctx.getSource(),
																				ctx.getSource().getPlayerOrException(),
																				parseBloodTendency(StringArgumentType.getString(ctx, "school")),
																				IntegerArgumentType.getInteger(ctx, "tier"),
																				IntegerArgumentType.getInteger(ctx, "broken_count"),
																				BoolArgumentType.getBool(ctx, "sent_present")))
																		.then(Commands.argument("player", EntityArgument.player())
																				.executes(ctx -> summonWillAmbushAnchor(ctx.getSource(),
																						EntityArgument.getPlayer(ctx, "player"),
																						parseBloodTendency(StringArgumentType.getString(ctx, "school")),
																						IntegerArgumentType.getInteger(ctx, "tier"),
																						IntegerArgumentType.getInteger(ctx, "broken_count"),
																						BoolArgumentType.getBool(ctx, "sent_present")))))))))
								.then(Commands.literal("immediate")
										.then(Commands.argument("school", StringArgumentType.word())
												.suggests((ctx, builder) -> suggestBloodTendencies(builder))
												.then(Commands.argument("tier", IntegerArgumentType.integer(1, 4))
														.then(Commands.argument("origin", StringArgumentType.word())
																.suggests((ctx, builder) -> suggestWillOrigins(builder))
																.executes(ctx -> summonWillAmbushImmediate(ctx.getSource(),
																		ctx.getSource().getPlayerOrException(),
																		parseBloodTendency(StringArgumentType.getString(ctx, "school")),
																		IntegerArgumentType.getInteger(ctx, "tier"),
																		parseWillOrigin(StringArgumentType.getString(ctx, "origin")),
																		1))
																.then(Commands.argument("count", IntegerArgumentType.integer(1, 8))
																		.executes(ctx -> summonWillAmbushImmediate(ctx.getSource(),
																				ctx.getSource().getPlayerOrException(),
																				parseBloodTendency(StringArgumentType.getString(ctx, "school")),
																				IntegerArgumentType.getInteger(ctx, "tier"),
																				parseWillOrigin(StringArgumentType.getString(ctx, "origin")),
																				IntegerArgumentType.getInteger(ctx, "count")))
																		.then(Commands.argument("player", EntityArgument.player())
																				.executes(ctx -> summonWillAmbushImmediate(ctx.getSource(),
																						EntityArgument.getPlayer(ctx, "player"),
																						parseBloodTendency(StringArgumentType.getString(ctx, "school")),
																						IntegerArgumentType.getInteger(ctx, "tier"),
																						parseWillOrigin(StringArgumentType.getString(ctx, "origin")),
																						IntegerArgumentType.getInteger(ctx, "count")))))))))))

				.then(Commands.literal("summons")
						.then(Commands.literal("list")
								.executes(ctx -> listKnownSummons(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> listKnownSummons(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
						.then(Commands.literal("add")
								.then(Commands.argument("summon", StringArgumentType.word())
										.suggests((ctx, builder) -> suggestPuppeteerSummons(builder))
										.executes(ctx -> addKnownSummon(ctx.getSource(), ctx.getSource().getPlayerOrException(),
												StringArgumentType.getString(ctx, "summon")))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> addKnownSummon(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
														StringArgumentType.getString(ctx, "summon"))))))
						.then(Commands.literal("remove")
								.then(Commands.argument("summon", StringArgumentType.word())
										.suggests((ctx, builder) -> suggestPuppeteerSummons(builder))
										.executes(ctx -> removeKnownSummon(ctx.getSource(), ctx.getSource().getPlayerOrException(),
												StringArgumentType.getString(ctx, "summon")))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> removeKnownSummon(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
														StringArgumentType.getString(ctx, "summon"))))))
						.then(Commands.literal("clear")
								.executes(ctx -> clearKnownSummons(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> clearKnownSummons(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))))))

				.then(Commands.literal("manipulations")
						.then(Commands.literal("list")
								.executes(ctx -> listKnownManipulations(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> listKnownManipulations(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
						.then(Commands.literal("add")
								.then(Commands.argument("manipulation", StringArgumentType.word())
										.suggests((ctx, builder) -> suggestManipulations(builder))
										.executes(ctx -> addKnownManipulation(ctx.getSource(), ctx.getSource().getPlayerOrException(),
												StringArgumentType.getString(ctx, "manipulation")))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> addKnownManipulation(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
														StringArgumentType.getString(ctx, "manipulation"))))))
						.then(Commands.literal("remove")
								.then(Commands.argument("manipulation", StringArgumentType.word())
										.suggests((ctx, builder) -> suggestManipulations(builder))
										.executes(ctx -> removeKnownManipulation(ctx.getSource(), ctx.getSource().getPlayerOrException(),
												StringArgumentType.getString(ctx, "manipulation")))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> removeKnownManipulation(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
														StringArgumentType.getString(ctx, "manipulation"))))))
						.then(Commands.literal("clear")
								.executes(ctx -> clearKnownManipulations(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> clearKnownManipulations(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))))))

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

	private static int listKnownSummons(CommandSourceStack source, ServerPlayer player) {
		List<String> names = HemoCapabilityAccess.requireKnownSummons(player).getKnownSummonNames();
		String display = names.isEmpty() ? "none" : String.join(", ", names);
		source.sendSuccess(() -> Component.literal(player.getName().getString())
				.append(Component.literal(" known puppeteer summons: ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(display).withStyle(names.isEmpty() ? ChatFormatting.DARK_GRAY : ChatFormatting.GREEN)),
				false);
		return Math.max(1, names.size());
	}

	private static int addKnownSummon(CommandSourceStack source, ServerPlayer player, String summonName) {
		IKnownSummons known = HemoCapabilityAccess.requireKnownSummons(player);
		List<String> current = new ArrayList<>(known.getKnownSummonNames());
		int added;
		if ("all".equals(summonName)) {
			List<String> everySummon = PuppeteerSummonDefinitions.all().stream()
					.map(definition -> definition.name()).toList();
			added = (int) everySummon.stream().filter(name -> !current.contains(name)).count();
			known.setKnownSummonNames(everySummon);
		} else {
			var definition = PuppeteerSummonDefinitions.byName(summonName).orElse(null);
			if (definition == null) {
				source.sendFailure(Component.literal("Unknown puppeteer summon '" + summonName
						+ "'. Valid: " + puppeteerSummonList()));
				return 0;
			}
			if (current.contains(definition.name())) {
				source.sendFailure(Component.literal(player.getName().getString() + " already knows "
						+ definition.name() + "."));
				return 0;
			}
			current.add(definition.name());
			known.setKnownSummonNames(current);
			added = 1;
		}
		KnownSummonEvents.sync(player, known);
		int changed = added;
		source.sendSuccess(() -> Component.literal("Added " + changed + " puppeteer summon"
				+ (changed == 1 ? "" : "s") + " for ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD)), true);
		return Math.max(1, added);
	}

	private static int removeKnownSummon(CommandSourceStack source, ServerPlayer player, String summonName) {
		if ("all".equals(summonName)) {
			return clearKnownSummons(source, player);
		}
		var definition = PuppeteerSummonDefinitions.byName(summonName).orElse(null);
		if (definition == null) {
			source.sendFailure(Component.literal("Unknown puppeteer summon '" + summonName
					+ "'. Valid: " + puppeteerSummonList()));
			return 0;
		}
		IKnownSummons known = HemoCapabilityAccess.requireKnownSummons(player);
		List<String> remaining = new ArrayList<>(known.getKnownSummonNames());
		if (!remaining.remove(definition.name())) {
			source.sendFailure(Component.literal(player.getName().getString() + " does not know "
					+ definition.name() + "."));
			return 0;
		}
		known.setKnownSummonNames(remaining);
		KnownSummonEvents.sync(player, known);
		source.sendSuccess(() -> Component.literal("Removed ")
				.append(Component.literal(definition.name()).withStyle(ChatFormatting.RED))
				.append(Component.literal(" from "))
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD)), true);
		return 1;
	}

	private static int clearKnownSummons(CommandSourceStack source, ServerPlayer player) {
		IKnownSummons known = HemoCapabilityAccess.requireKnownSummons(player);
		int removed = known.getKnownSummonNames().size();
		known.setKnownSummonNames(List.of());
		KnownSummonEvents.sync(player, known);
		source.sendSuccess(() -> Component.literal("Cleared " + removed + " known puppeteer summon"
				+ (removed == 1 ? "" : "s") + " from ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD)), true);
		return Math.max(1, removed);
	}

	private static CompletableFuture<Suggestions> suggestPuppeteerSummons(SuggestionsBuilder builder) {
		builder.suggest("all");
		for (var definition : PuppeteerSummonDefinitions.all()) {
			builder.suggest(definition.name());
		}
		return builder.buildFuture();
	}

	private static String puppeteerSummonList() {
		return String.join(", ", PuppeteerSummonDefinitions.all().stream()
				.map(definition -> definition.name()).toList()) + ", all";
	}

	private static int getMorphlingStage(CommandSourceStack source, ServerPlayer player) {
		MorphlingCommandTarget target = getMorphlingCommandTarget(source, player);
		if (target == null) {
			return 0;
		}
		ItemStack stack = target.stack();
		int stage = MorphlingItem.getMaturityLevel(stack);
		source.sendSuccess(() -> Component.literal("")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" " + target.description() + ": ").withStyle(ChatFormatting.GRAY))
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
		MorphlingCommandTarget target = getMorphlingCommandTarget(source, player);
		if (target == null) {
			return 0;
		}
		ItemStack original = target.stack().copy();
		ItemStack updated = original.copy();
		applyMorphlingStage(updated, stage);
		boolean sourceUpdated = false;
		if (target.source() == MorphlingCommandRules.Source.EQUIPPED) {
			var cap = HemoCapabilityAccess.getEquippedMorphling(player)
					.orElseThrow(IllegalStateException::new);
			sourceUpdated = writeBackEquippedMorphlingSource(player, original, updated);
			cap.setEquippedMorphling(updated);
			LastRiteHelper.armForMorphling(player, updated);
			EquippedMorphlingEvents.syncToClient(player);
		} else {
			InteractionHand hand = target.source() == MorphlingCommandRules.Source.MAIN_HAND
					? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			player.setItemInHand(hand, updated);
			player.getInventory().setChanged();
			player.containerMenu.broadcastChanges();
		}
		int actualStage = MorphlingItem.getMaturityLevel(updated);
		boolean jarUpdated = sourceUpdated;
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" " + target.description() + " to stage "))
				.append(Component.literal(actualStage + " - " + MorphlingItem.getMaturityName(actualStage))
						.withStyle(MorphlingItem.MATURITY_COLORS[actualStage]))
				.append(target.source() != MorphlingCommandRules.Source.EQUIPPED
						? Component.empty()
						: jarUpdated
								? Component.literal(" and updated its jar item.").withStyle(ChatFormatting.GRAY)
								: Component.literal("; no matching jar item was found.").withStyle(ChatFormatting.YELLOW)),
				true);
		return 1;
	}

	private static int cycleMorphlingStage(CommandSourceStack source, ServerPlayer player, int delta) {
		MorphlingCommandTarget target = getMorphlingCommandTarget(source, player);
		if (target == null) {
			return 0;
		}
		int current = MorphlingItem.getMaturityLevel(target.stack());
		int next = Math.floorMod(current + delta, PrimalMorphlingRules.PRIMAL_LEVEL + 1);
		return setMorphlingStage(source, player, next);
	}

	private static MorphlingCommandTarget getMorphlingCommandTarget(CommandSourceStack source, ServerPlayer player) {
		ItemStack equipped = HemoCapabilityAccess.getEquippedMorphling(player)
				.filter(cap -> cap.hasMorphling())
				.map(cap -> cap.getEquippedMorphling())
				.filter(HemoCommand::isMorphling)
				.orElse(ItemStack.EMPTY);
		ItemStack mainHand = player.getMainHandItem();
		ItemStack offhand = player.getOffhandItem();
		MorphlingCommandRules.Source selected = MorphlingCommandRules.chooseSource(!equipped.isEmpty(),
				isMorphling(mainHand), isMorphling(offhand));
		MorphlingCommandTarget target = switch (selected) {
			case EQUIPPED -> new MorphlingCommandTarget(equipped, selected, "equipped morphling");
			case MAIN_HAND -> new MorphlingCommandTarget(mainHand, selected, "main-hand morphling");
			case OFF_HAND -> new MorphlingCommandTarget(offhand, selected, "offhand morphling");
			case NONE -> null;
		};
		if (target == null) {
			source.sendFailure(Component.literal(player.getName().getString()
					+ " has no equipped or held morphling."));
		}
		return target;
	}

	private static boolean isMorphling(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof MorphlingItem;
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
		tag.remove(MorphlingItem.WILD_BOUND_KEY);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		MorphlingItem.resetBondingProgress(stack, stage);
	}

	private record MorphlingCommandTarget(ItemStack stack, MorphlingCommandRules.Source source,
			String description) {
	}

	private static boolean writeBackEquippedMorphlingSource(ServerPlayer player, ItemStack original, ItemStack updated) {
		boolean changed = false;
		ItemStack inventoryJar = Hemomancy.findItemInPlayerInv(player, ItemMorphlingJar.class);
		if (!inventoryJar.isEmpty()) {
			changed |= updateMorphlingJarStack(inventoryJar, original, updated);
		}
		ItemStack scarJar = HemoCapabilityAccess.getEquipment(player)
				.map(equipmentItemHandler -> equipmentItemHandler.getStackInSlot(7))
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
		ChamberOfWillManager.syncFor(player);
		source.sendSuccess(() -> Component.literal("Reset ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" Qliphoth pome progress and Communion gate.").withStyle(ChatFormatting.DARK_PURPLE)),
				true);
		return 1;
	}

	private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack>
			silentArchonStateCommand(String state) {
		return Commands.literal(state)
				.executes(ctx -> setSilentArchonState(ctx.getSource(), ctx.getSource().getPlayerOrException(), state))
				.then(Commands.argument("player", EntityArgument.player())
						.executes(ctx -> setSilentArchonState(ctx.getSource(),
								EntityArgument.getPlayer(ctx, "player"), state)));
	}

	private static int setSilentArchonState(CommandSourceStack source, ServerPlayer player, String state) {
		IInitiatoryDegree degree = HemoCapabilityAccess.getInitiatoryDegree(player)
				.orElseThrow(IllegalStateException::new);
		SilentArchonCommandRules.Transition transition =
				SilentArchonCommandRules.transition(state, degree.getDegreeNumber());
		degree.setDegreeNumber(transition.degreeNumber());
		degree.setArchonPath(transition.archonPath());
		if (transition.archonPath() != EnumArchonPath.NONE) {
			degree.setFungalRevelationWitnessed(true);
		}
		if (transition.clearLegacyChoice()) {
			player.getPersistentData().remove(FungalGardenTravelHelper.ARCHON_CHOICE_KEY);
		}
		InitiatoryDegreeEvents.syncDegree(player, degree);
		ChamberOfWillManager.syncFor(player);
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" Silent Archon state to "))
				.append(Component.literal(state).withStyle(ChatFormatting.DARK_PURPLE))
				.append(Component.literal(" (degree " + transition.degreeNumber() + ").")
						.withStyle(ChatFormatting.GRAY)), true);
		return 1;
	}

	private static int setPomeProgress(CommandSourceStack source, ServerPlayer player, int count) {
		int clamped = Math.max(0, Math.min(9, count));
		IInitiatoryDegree degree = HemoCapabilityAccess.getInitiatoryDegree(player)
				.orElseThrow(IllegalStateException::new);
		degree.syncTotalPomesConsumed(clamped);
		degree.setQliphothCommunionDone(clamped >= 9);
		InitiatoryDegreeEvents.syncDegree(player, degree);
		PacketHandler.sendToPlayer(player, new PacketSyncPomeProgress(clamped));
		ChamberOfWillManager.syncFor(player);
		source.sendSuccess(() -> Component.literal("Set ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" Qliphoth pome progress to ").withStyle(ChatFormatting.DARK_PURPLE))
				.append(Component.literal(String.valueOf(clamped)).withStyle(ChatFormatting.LIGHT_PURPLE))
				.append(Component.literal("/9.").withStyle(ChatFormatting.DARK_PURPLE)),
				true);
		return 1;
	}

	private static int spawnQliphothTree(CommandSourceStack source, ServerPlayer player, String rawStage) {
		QliphothTreeStage stage = QliphothTreeStage.parse(rawStage);
		if (stage == null) {
			source.sendFailure(Component.literal("Unknown Qliphoth tree stage. Use initial, 1-9, fully_grown, or pruned."));
			return 0;
		}

		ServerLevel level = player.serverLevel();
		BlockPos center = player.blockPosition();
		String dimension = level.dimension().location().toString();
		QliphothBloomSavedData data = QliphothBloomSavedData.get(level.getServer().overworld());
		QliphothBloomSavedData.BloomEntry overlapping = data.getOverlappingBloom(center, dimension, 3);
		if (overlapping != null && !overlapping.center().equals(center)) {
			source.sendFailure(Component.literal("A Qliphoth Bloom already exists within 3 chunks of your position."));
			return 0;
		}
		if (overlapping != null) {
			removeDebugQliphothTree(level, data, overlapping);
		}

		QliphothBloomBlock bloomBlock = (QliphothBloomBlock) BlockInit.qliphoth_bloom.get();
		IMultiBlock multiBlock = bloomBlock;
		if (!multiBlock.canPlaceMultiBlock(level, center)) {
			source.sendFailure(Component.literal("There is not enough room for the Qliphoth tree here."));
			return 0;
		}

		level.setBlockAndUpdate(center, bloomBlock.defaultBlockState());
		if (level.getBlockEntity(center) instanceof QliphothBloomBlockEntity bloomEntity) {
			bloomEntity.setOwnerUUID(player.getUUID());
			bloomEntity.setChunkRadius(3);
		}
		multiBlock.placeFillers(level, center, bloomBlock.defaultBlockState());

		data.addBloom(new QliphothBloomSavedData.BloomEntry(
				player.getUUID(), center, dimension, 3, level.getGameTime()));
		for (int i = 0; i < stage.pomesDropped(); i++) {
			data.incrementPomesDropped(center);
		}
		if (stage.severedState().isPortalOpen()) {
			data.severBloom(center);
		} else if (stage.severedState().isSealedTrophy()) {
			data.severBloom(center);
			data.sealBloom(center);
		}
		HarbingerCardinalRiteEvents.syncQliphothBlooms(level.getServer());

		source.sendSuccess(() -> Component.literal("Spawned Qliphoth tree stage ")
				.append(Component.literal(rawStage).withStyle(ChatFormatting.LIGHT_PURPLE))
				.append(Component.literal(" at ").withStyle(ChatFormatting.DARK_PURPLE))
				.append(Component.literal(center.toShortString()).withStyle(ChatFormatting.GOLD)), true);
		return 1;
	}

	private static void removeDebugQliphothTree(ServerLevel level, QliphothBloomSavedData data,
			QliphothBloomSavedData.BloomEntry bloom) {
		if (level.getBlockState(bloom.center()).getBlock() instanceof IMultiBlock multiBlock) {
			multiBlock.removeFillers(level, bloom.center());
			level.removeBlock(bloom.center(), false);
		} else {
			data.removeBloomInChunk(bloom.center(), bloom.dimension());
		}
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
		cap.setAcceptedObservances(0);
		cap.setClaimedObservances(0);
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

	private static CompletableFuture<Suggestions> suggestBloodTendencies(SuggestionsBuilder builder) {
		for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
			builder.suggest(tendency.name().toLowerCase(Locale.ROOT));
		}
		return builder.buildFuture();
	}

	private static EnumBloodTendency parseBloodTendency(String tendencyName) {
		try {
			return EnumBloodTendency.valueOf(tendencyName.trim().replace('-', '_').toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	private static CompletableFuture<Suggestions> suggestWillOrigins(SuggestionsBuilder builder) {
		for (WillOrigin origin : WillOrigin.values()) {
			builder.suggest(origin.name().toLowerCase(Locale.ROOT));
		}
		return builder.buildFuture();
	}

	private static WillOrigin parseWillOrigin(String originName) {
		try {
			return WillOrigin.valueOf(originName.trim().replace('-', '_').toUpperCase(Locale.ROOT));
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

	private static int listKnownManipulations(CommandSourceStack source, ServerPlayer player) {
		List<String> names = HemoCapabilityAccess.requireKnownManipulations(player).getKnownManips().keySet().stream()
				.map(BloodManipulation::getName).sorted().toList();
		String display = names.isEmpty() ? "none" : String.join(", ", names);
		source.sendSuccess(() -> Component.literal(player.getName().getString())
				.append(Component.literal(" known manipulations: ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(display).withStyle(names.isEmpty() ? ChatFormatting.DARK_GRAY : ChatFormatting.GREEN)),
				false);
		return Math.max(1, names.size());
	}

	private static int addKnownManipulation(CommandSourceStack source, ServerPlayer player, String manipulationName) {
		IKnownManipulations known = HemoCapabilityAccess.requireKnownManipulations(player);
		LinkedHashMap<BloodManipulation, ManipLevel> updated = new LinkedHashMap<>(known.getKnownManips());
		int added = 0;
		if ("all".equals(manipulationName)) {
			for (BloodManipulation manipulation : availableManipulations()) {
				if (!known.doesListContainName(updated, manipulation)) {
					updated.put(manipulation, new ManipLevel(0, 0));
					added++;
				}
			}
		} else {
			BloodManipulation manipulation = ManipulationInit.getByName(manipulationName);
			if (!isAvailableManipulation(manipulation)) {
				source.sendFailure(Component.literal("Unknown manipulation '" + manipulationName
						+ "'. Use tab completion or 'all'."));
				return 0;
			}
			if (known.doesListContainName(updated, manipulation)) {
				source.sendFailure(Component.literal(player.getName().getString() + " already knows "
						+ manipulation.getName() + "."));
				return 0;
			}
			updated.put(manipulation, new ManipLevel(0, 0));
			added = 1;
		}
		known.setKnownManips(updated);
		syncKnownManipulations(player);
		int changed = added;
		source.sendSuccess(() -> Component.literal("Added " + changed + " manipulation"
				+ (changed == 1 ? "" : "s") + " for ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD)), true);
		return Math.max(1, added);
	}

	private static int removeKnownManipulation(CommandSourceStack source, ServerPlayer player,
			String manipulationName) {
		if ("all".equals(manipulationName)) {
			return clearKnownManipulations(source, player);
		}
		BloodManipulation manipulation = ManipulationInit.getByName(manipulationName);
		if (!isAvailableManipulation(manipulation)) {
			source.sendFailure(Component.literal("Unknown manipulation '" + manipulationName
					+ "'. Use tab completion or 'all'."));
			return 0;
		}
		IKnownManipulations known = HemoCapabilityAccess.requireKnownManipulations(player);
		boolean removed = known.getKnownManips().entrySet().removeIf(entry ->
				manipulation.getName().equals(entry.getKey().getName()));
		if (!removed) {
			source.sendFailure(Component.literal(player.getName().getString() + " does not know "
					+ manipulation.getName() + "."));
			return 0;
		}
		if (known.getSelectedManip() != null
				&& manipulation.getName().equals(known.getSelectedManip().getName())) {
			known.setSelectedManip(BloodManipulation.BLANK);
		}
		List<String> equipped = new ArrayList<>(known.getEquippedManipNames());
		equipped.removeIf(manipulation.getName()::equals);
		known.setEquippedManipNames(equipped);
		syncKnownManipulations(player);
		source.sendSuccess(() -> Component.literal("Removed ")
				.append(Component.literal(manipulation.getName()).withStyle(ChatFormatting.RED))
				.append(Component.literal(" from "))
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD)), true);
		return 1;
	}

	private static int clearKnownManipulations(CommandSourceStack source, ServerPlayer player) {
		IKnownManipulations known = HemoCapabilityAccess.requireKnownManipulations(player);
		int removed = known.getKnownManips().size();
		known.setKnownManips(new LinkedHashMap<>());
		known.setSelectedManip(BloodManipulation.BLANK);
		known.setEquippedManipNames(List.of());
		syncKnownManipulations(player);
		source.sendSuccess(() -> Component.literal("Cleared " + removed + " known manipulation"
				+ (removed == 1 ? "" : "s") + " from ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD)), true);
		return Math.max(1, removed);
	}

	private static List<BloodManipulation> availableManipulations() {
		return ManipulationInit.getAllEntries().stream().filter(HemoCommand::isAvailableManipulation)
				.sorted(Comparator.comparing(BloodManipulation::getName)).toList();
	}

	private static boolean isAvailableManipulation(BloodManipulation manipulation) {
		return manipulation != null && manipulation != BloodManipulation.BLANK
				&& !ManipulationRetirementRules.isRetiredManipulation(manipulation);
	}

	private static CompletableFuture<Suggestions> suggestManipulations(SuggestionsBuilder builder) {
		builder.suggest("all");
		for (BloodManipulation manipulation : availableManipulations()) {
			builder.suggest(manipulation.getName());
		}
		return builder.buildFuture();
	}

	private static void syncKnownManipulations(ServerPlayer player) {
		KnownManipulationEvents.syncPlayerEvent(player);
		ManipulationDiagnosticsSync.sync(player);
	}

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

	private static int summonWillAmbushAnchor(CommandSourceStack source, ServerPlayer player,
			EnumBloodTendency school, int tier, int brokenCount, boolean sentPresent) {
		if (school == null) {
			source.sendFailure(Component.literal("Unknown Will school. Valid: " + getValidTendencyNames()));
			return 0;
		}
		ServerLevel level = source.getLevel();
		WillAnchorEntity anchor = EntityInit.will_anchor.get().create(level);
		if (anchor == null) {
			source.sendFailure(Component.literal("Could not create Will anchor."));
			return 0;
		}
		Vec3 pos = source.getPosition();
		WillCompositionRules.Composition composition =
				new WillCompositionRules.Composition(tier, brokenCount, Math.min(2, tier), sentPresent);
		anchor.moveTo(pos.x, pos.y, pos.z, player.getYRot(), 0.0F);
		anchor.configure(school, composition, player);
		level.addFreshEntity(anchor);
		source.sendSuccess(() -> Component.literal("Summoned Will ambush anchor: ")
				.append(Component.literal(school.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.DARK_RED))
				.append(Component.literal(" tier " + tier + ", broken " + brokenCount
						+ ", sent " + sentPresent + ", target "))
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD)),
				true);
		return 1;
	}

	private static int summonWillAmbushImmediate(CommandSourceStack source, ServerPlayer player,
			EnumBloodTendency school, int tier, WillOrigin origin, int count) {
		if (school == null) {
			source.sendFailure(Component.literal("Unknown Will school. Valid: " + getValidTendencyNames()));
			return 0;
		}
		if (origin == null) {
			source.sendFailure(Component.literal("Unknown Will origin. Valid: broken, sent"));
			return 0;
		}
		ServerLevel level = source.getLevel();
		Vec3 pos = source.getPosition();
		int spawned = 0;
		for (int i = 0; i < count; i++) {
			WillEntity will = EntityInit.will.get().create(level);
			if (will == null) continue;
			double offsetX = (i % 3 - 1) * 1.35D;
			double offsetZ = (i / 3) * 1.35D;
			will.moveTo(pos.x + offsetX, pos.y, pos.z + offsetZ, player.getYRot(), 0.0F);
			will.configure(origin, school, tier, player, true);
			level.addFreshEntity(will);
			spawned++;
		}
		if (spawned <= 0) {
			source.sendFailure(Component.literal("Could not create any Wills."));
			return 0;
		}
		final int spawnedCount = spawned;
		source.sendSuccess(() -> Component.literal("Summoned ")
				.append(Component.literal(String.valueOf(spawnedCount)).withStyle(ChatFormatting.AQUA))
				.append(Component.literal(" immediate "))
				.append(Component.literal(origin.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.GRAY))
				.append(Component.literal(" Will(s): "))
				.append(Component.literal(school.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.DARK_RED))
				.append(Component.literal(" tier " + tier + ", target "))
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD)),
				true);
		return 1;
	}

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

	private static int setFanePreview(CommandSourceStack source, FaneBoundaryRelation relation)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		FoundingFaneEvents.setPreviewRelation(player, relation);
		source.sendSuccess(() -> Component.literal("Fane boundary preview set to ")
				.append(Component.literal(relation.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.DARK_RED))
				.append(Component.literal(". Wait up to two seconds for the next boundary sync.")
						.withStyle(ChatFormatting.GRAY)),
				false);
		return 1;
	}

	private static int clearFanePreview(CommandSourceStack source)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		FoundingFaneEvents.clearPreviewRelation(player);
		source.sendSuccess(() -> Component.literal("Fane boundary preview cleared.").withStyle(ChatFormatting.GRAY),
				false);
		return 1;
	}

	private static int cycleChamberTheme(CommandSourceStack source, ServerPlayer player, int direction) {
		ResourceLocation theme = ChamberOfWillManager.get(source.getServer()).cycleSkyThemeOverride(player, direction);
		source.sendSuccess(() -> Component.literal("Chamber sky theme override for ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" cycled to "))
				.append(Component.literal(theme.toString()).withStyle(ChatFormatting.DARK_RED))
				.append(Component.literal(".")),
				true);
		return 1;
	}

	private static int setChamberTheme(CommandSourceStack source, ServerPlayer player, String themeName) {
		ResourceLocation theme = parseChamberTheme(themeName);
		if (theme == null || !ChamberOfWillManager.isKnownSkyTheme(theme)) {
			source.sendFailure(Component.literal("Unknown chamber sky theme '" + themeName
					+ "'. Valid: " + chamberThemeList()));
			return 0;
		}

		ChamberOfWillManager.get(source.getServer()).setSkyThemeOverride(player, theme);
		source.sendSuccess(() -> Component.literal("Chamber sky theme override for ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" set to "))
				.append(Component.literal(theme.toString()).withStyle(ChatFormatting.DARK_RED))
				.append(Component.literal(". Use /hemo chamber theme reset to return to progression.")),
				true);
		return 1;
	}

	private static int resetChamberTheme(CommandSourceStack source, ServerPlayer player) {
		ResourceLocation theme = ChamberOfWillManager.get(source.getServer()).clearSkyThemeOverride(player);
		source.sendSuccess(() -> Component.literal("Chamber sky theme override for ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" reset to progression theme "))
				.append(Component.literal(theme.toString()).withStyle(ChatFormatting.DARK_RED))
				.append(Component.literal(".")),
				true);
		return 1;
	}

	private static int setChamberSize(CommandSourceStack source, ServerPlayer player, int radius) {
		int size = ChamberOfWillManager.get(source.getServer()).setChamberRadiusOverride(player, radius);
		source.sendSuccess(() -> Component.literal("Chamber size override for ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" set to radius "))
				.append(Component.literal(Integer.toString(size)).withStyle(ChatFormatting.DARK_RED))
				.append(Component.literal(" (" + (size * 2 + 1) + "x" + (size * 2 + 1) + " platform).")),
				true);
		return 1;
	}

	private static int resetChamberSize(CommandSourceStack source, ServerPlayer player) {
		int radius = ChamberOfWillManager.get(source.getServer()).clearChamberRadiusOverride(player);
		source.sendSuccess(() -> Component.literal("Chamber size override for ")
				.append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" reset to progression radius "))
				.append(Component.literal(Integer.toString(radius)).withStyle(ChatFormatting.DARK_RED))
				.append(Component.literal(".")),
				true);
		return 1;
	}

	private static ResourceLocation parseChamberTheme(String themeName) {
		String normalized = themeName.trim().toLowerCase(Locale.ROOT);
		try {
			return normalized.contains(":") ? ResourceLocation.parse(normalized) : Hemomancy.rloc(normalized);
		} catch (RuntimeException ignored) {
			return null;
		}
	}

	private static String chamberThemeList() {
		StringBuilder builder = new StringBuilder();
		for (ResourceLocation id : ChamberOfWillManager.commandSkyThemes()) {
			if (!builder.isEmpty()) {
				builder.append(", ");
			}
			builder.append(id);
		}
		return builder.toString();
	}
}
