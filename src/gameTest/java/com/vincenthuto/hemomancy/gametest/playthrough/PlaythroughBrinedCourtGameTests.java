package com.vincenthuto.hemomancy.gametest.playthrough;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtDiscoveryProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtLessonRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtLife;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtStandingRules.Standing;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtChampionRules;
import com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenEntity;
import com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenRules;
import com.vincenthuto.hemomancy.common.entity.boss.court.KindledChampionEntity;
import com.vincenthuto.hemomancy.common.entity.boss.court.KindledChampionRules;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.BrinedVotaryEntity;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtHospitality;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtPresences;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.entity.npc.court.HydraRisingHead;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CourtDiscoveryDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtVacancies;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CourtMemberDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueEventSpeakers;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueNode;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVoyagerEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.RelayChartItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrain;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import com.vincenthuto.hemomancy.common.item.itemhandler.MorphlingJarItemHandler;
import com.vincenthuto.hemomancy.common.network.trade.NpcTradeResultPacket;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSite;
import com.vincenthuto.hemomancy.config.HemoCommonConfig;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The Brined Court band of the late playthrough: learning of the Court by each of its routes, the Relay Chart, walking
 * into a house's site, hospitality, the house custom by dialogue and by trade card, the Lightkeeper's lesson and its
 * bonding, joining and the one switch, every inner house, wronging the Giving Light and making amends, the Kindled
 * Champion's usurpation won and lost, Hydra and Marimo, every role swept by state, and the Primal step. Every step under
 * test goes through what a player triggers (right-click, the option and trade payloads, item use, block breaking, real
 * damage, death and respawn); state that is not under test is {@code // ARRANGE}.
 *
 * <p>Harness gaps worked around here, for the harness owner: a strain stack built by hand ({@code MorphlingPowerGameTests
 * .stack} is package-private), an English text reader for lines ({@code LangKeys} keeps keys only), clearing the Court's
 * own spawns (Wardens, Champions, returned elders, Hydra's head, drops) that no harness helper registers, an attack-damage
 * modifier for a Champion fight the test does not balance (a captured player is never ticked, so a held weapon's modifiers
 * never apply), and an entity-lookup check that turns {@code getEntitiesOfClass(ServerPlayer)} scenes into NOT RUN.
 */
@GameTestHolder("playthrough_validation")
@PrefixGameTestTemplate(false)
public final class PlaythroughBrinedCourtGameTests {
	private static final String EMPTY = "empty";
	private static final String BATCH = "playthrough_brined_court";
	/** Each site test has a batch to itself: a synthetic StructureStart sits in its chunk's starts. */
	private static final String SITE_VISIT = "playthrough_brined_court_site_visit";
	private static final String SITE_LESSON = "playthrough_brined_court_site_lesson";
	private static final String SITE_WRONGS = "playthrough_brined_court_site_wrongs";
	private static final String SITE_ORDER = "playthrough_brined_court_site_order";
	private static final String SITE_HOUSES = "playthrough_brined_court_site_houses";
	/** Game-time skips change the whole server: one test per clock batch. */
	private static final String CLOCK_REST = "playthrough_brined_court_clock";
	private static final String CLOCK_CHAMPION = "playthrough_brined_court_clock_champion";

	private static final String SAID_KNOWN = "hemomancy.court.known";
	private static final String SAID_DISCOVERY = "hemomancy.court.discovery";
	private static final String SAID_RAISED = "hemomancy.court.standing.raised";
	private static final String SAID_REFUSED = CourtHospitality.REFUSED;
	private static final String SAID_REST_RECENT = "hemomancy.court.rest.recent";
	private static final String SAID_AMENDS = "hemomancy.court.amends";
	private static final String SAID_CHART_ISSUED = "hemomancy.dialogue.event.relay_chart_issued";
	private static final String SAID_CHART_CARRYING = "hemomancy.dialogue.event.relay_chart_carrying";
	private static final String CHART_SEARCHING = "item.hemomancy.relay_chart.searching";
	private static final String CHART_NOT_FOUND = "item.hemomancy.relay_chart.not_found";
	private static final String CHAMPION_WON = "hemomancy.court.champion.won";
	private static final String CHAMPION_REFUSED_WAIT = CourtChampionRules.Gate.REFUSED_WAIT.translationKey();
	private static final String HYDRA_CALL = "hemomancy.court.presence.hydra.call";
	private static final String MARIMO_CHORUS = "hemomancy.court.presence.marimo.chorus.";
	private static final ResourceLocation GARDENER_CUSTOM = Hemomancy.rloc("court_gardener/custom");
	private static final String LANG = "/assets/hemomancy/lang/en_us.json";
	private static final float[] POWER = {0.0F, 10.0F, 30.0F, 60.0F, 100.0F};
	private static final int MATURE = 3;
	private static final int APEX = 4;
	private static Map<String, String> english;

	private PlaythroughBrinedCourtGameTests() {
	}

	// =================================================================================================================
	// Learning of the Court
	// =================================================================================================================

	/**
	 * Three players learn of the Court three ways. A asks the Vicar about the sea, then the Voyager: one Relay Chart,
	 * none while carried, reissued once lost. B kills a Brined Votary, carries its letter to the Vicar and has it read.
	 * C wears a Mature strain to the Alchemist. Whichever route came first stays the route: A shown the Alchemist's
	 * lead later is acknowledged, not recorded again.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void courtDiscoveryRoutes(GameTestHelper helper) {
		clearCourtActors(helper);
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 4, 4));
		HarbingerVoyagerEntity voyager = PlaythroughNpcs.voyager(helper, PlaythroughSites.stand(helper, 16, 4));
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, PlaythroughSites.stand(helper, 28, 4));

		// A: the Vicar's sea topic, then the Voyager.
		PlaythroughPlayer a = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 10));
		PlaythroughStates.illuminatusD5(a); // ARRANGE
		a.approach(vicar);
		DialogueView atVicar = a.talkTo(vicar);
		atVicar.assertLangKeys(helper, "the D5 Vicar").assertNoDeadEnds(helper, "the D5 Vicar");
		helper.assertTrue(atVicar.hasNode("court"), "the D5 Vicar has the Court topic: " + atVicar);
		PlaythroughLog.note(helper, "Vicar's sea topic: " + english(atVicar.node("court").map(DialogueNode::lines).orElse(List.of())));
		a.approach(voyager);
		DialogueView atVoyager = a.talkTo(voyager);
		atVoyager.assertLangKeys(helper, "the Voyager").assertNoDeadEnds(helper, "the Voyager");
		helper.assertTrue(atVoyager.offers(CourtDiscoveryDialogue.VOYAGER_LEARN), "the Voyager explains the Court: " + atVoyager);
		Outcome learn = a.choose(voyager, CourtDiscoveryDialogue.VOYAGER_LEARN).said(SAID_KNOWN).said(SAID_CHART_ISSUED);
		a.pickUpNear(voyager.blockPosition(), ItemInit.relay_chart.get());
		helper.assertTrue(CourtDiscoveryProgress.knownBy(a.player()) == CourtProgress.Route.VOYAGER, "A knows the Court by the Voyager");
		helper.assertTrue(a.count(ItemInit.relay_chart.get()) == 1, "one Relay Chart; " + learn);
		DialogueView carrying = a.talkTo(voyager);
		helper.assertFalse(carrying.offers(CourtDiscoveryDialogue.VOYAGER_CHART), "no chart offered while one is carried: " + carrying);
		a.forge(CourtDiscoveryDialogue.VOYAGER_CHART, voyager.getId()).said(SAID_CHART_CARRYING);
		a.pickUpNear(voyager.blockPosition(), ItemInit.relay_chart.get());
		helper.assertTrue(a.count(ItemInit.relay_chart.get()) == 1, "asking while carrying gives nothing");
		removeAll(a, ItemInit.relay_chart.get()); // ARRANGE: the chart is lost at sea
		DialogueView lost = a.talkTo(voyager);
		helper.assertTrue(lost.offers(CourtDiscoveryDialogue.VOYAGER_CHART), "a lost chart is offered again: " + lost);
		a.choose(voyager, CourtDiscoveryDialogue.VOYAGER_CHART).said(SAID_CHART_ISSUED).didNotSay(SAID_KNOWN);
		a.pickUpNear(voyager.blockPosition(), ItemInit.relay_chart.get());
		helper.assertTrue(a.count(ItemInit.relay_chart.get()) == 1, "the reissued chart arrives");

		// B: a real kill, the letter carried to the Vicar.
		PlaythroughPlayer b = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 16));
		PlaythroughStates.illuminatusD5(b); // ARRANGE
		BrinedVotaryEntity votary = PlaythroughNpcs.brinedVotary(helper, PlaythroughSites.stand(helper, 10, 19));
		BlockPos fell = killWithFists(b, votary);
		b.pickUpNear(fell, ItemInit.brine_sealed_letter.get());
		helper.assertTrue(b.count(ItemInit.brine_sealed_letter.get()) == 1, "the Votary carried one letter for B");
		helper.assertFalse(CourtDiscoveryProgress.isKnown(b.player()), "carrying the letter is not knowing the Court");
		b.approach(vicar);
		DialogueView letterAtVicar = b.talkTo(vicar);
		letterAtVicar.assertLangKeys(helper, "the Vicar to a letter-carrier");
		helper.assertTrue(letterAtVicar.offers(CourtDiscoveryDialogue.VICAR_LETTER), "the Vicar offers to read the letter: " + letterAtVicar);
		b.choose(vicar, CourtDiscoveryDialogue.VICAR_LETTER).said(SAID_KNOWN);
		helper.assertTrue(CourtDiscoveryProgress.knownBy(b.player()) == CourtProgress.Route.LETTER, "B knows the Court by the letter");
		b.approach(voyager);
		DialogueView bAtVoyager = b.talkTo(voyager);
		helper.assertFalse(bAtVoyager.offers(CourtDiscoveryDialogue.VOYAGER_LEARN), "the Voyager does not explain it again: " + bAtVoyager);
		helper.assertTrue(bAtVoyager.lineKeys().contains("hemomancy.voyager.court.known.letter"),
				"the Voyager acknowledges the letter route: " + bAtVoyager.lineKeys());

		// C: the Alchemist's lead, with a Mature strain worn.
		PlaythroughPlayer c = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 26, 12));
		PlaythroughStates.illuminatusD5(c); // ARRANGE
		wear(c, strain(MorphlingStrains.SUNBURST, MATURE)); // ARRANGE: grown elsewhere
		c.approach(alchemist);
		DialogueView lead = c.talkTo(alchemist);
		lead.assertLangKeys(helper, "the Alchemist with a Mature strain").assertNoDeadEnds(helper, "the Alchemist");
		helper.assertTrue(lead.offers(CourtDiscoveryDialogue.ALCHEMIST_LEAD), "the Alchemist gives her lead: " + lead);
		c.choose(alchemist, CourtDiscoveryDialogue.ALCHEMIST_LEAD).said(SAID_KNOWN);
		helper.assertTrue(CourtDiscoveryProgress.knownBy(c.player()) == CourtProgress.Route.ALCHEMIST, "C knows the Court by the Alchemist");

		// The first route stays: A wears a Mature strain to the Alchemist after the Voyager.
		wear(a, strain(MorphlingStrains.SUNBURST, MATURE)); // ARRANGE
		a.approach(alchemist);
		DialogueView acknowledged = a.talkTo(alchemist);
		helper.assertFalse(acknowledged.offers(CourtDiscoveryDialogue.ALCHEMIST_LEAD), "no lead for a player who knows: " + acknowledged);
		helper.assertTrue(acknowledged.lineKeys().contains("hemomancy.alchemist.court.known.voyager"),
				"the Alchemist names the Voyager's route: " + acknowledged.lineKeys());
		a.forge(CourtDiscoveryDialogue.ALCHEMIST_LEAD, alchemist.getId()).didNotSay(SAID_KNOWN);
		helper.assertTrue(CourtDiscoveryProgress.knownBy(a.player()) == CourtProgress.Route.VOYAGER, "the first route is kept");
		unwear(a);
		unwear(c);
		helper.succeed();
	}

	/** The lead needs the strain worn or in the pack: one in a Morphling Jar or the offhand gives none. */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void alchemistLeadNeedsAWornStrain(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, PlaythroughSites.stand(helper, 16, 6));
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 16, 12));
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		ItemStack jar = new ItemStack(ItemInit.morphling_jar.get());
		helper.assertTrue(putInJar(jar, strain(MorphlingStrains.SUNBURST, MATURE)), "the strain sits in the jar"); // ARRANGE
		p.give(jar);
		p.approach(alchemist);
		DialogueView jarred = p.talkTo(alchemist);
		helper.assertFalse(jarred.offers(CourtDiscoveryDialogue.ALCHEMIST_LEAD), "a jarred strain gives no lead: " + jarred);
		p.player().setItemInHand(InteractionHand.OFF_HAND, strain(MorphlingStrains.SUNBURST, MATURE)); // ARRANGE
		DialogueView offhand = p.talkTo(alchemist);
		helper.assertFalse(offhand.offers(CourtDiscoveryDialogue.ALCHEMIST_LEAD), "an offhand strain gives no lead: " + offhand);
		ItemStack moved = p.player().getOffhandItem().copy();
		p.player().setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
		p.give(moved); // ARRANGE: the strain carried in the pack
		boolean packed = p.talkTo(alchemist).offers(CourtDiscoveryDialogue.ALCHEMIST_LEAD);
		PlaythroughLog.note(helper, "a Mature strain carried in the pack gives the lead: " + packed);
		removeAll(p, moved.getItem());
		wear(p, strain(MorphlingStrains.SUNBURST, MATURE)); // ARRANGE
		DialogueView worn = p.talkTo(alchemist);
		helper.assertTrue(worn.offers(CourtDiscoveryDialogue.ALCHEMIST_LEAD), "the worn strain gives the lead: " + worn);
		unwear(p);
		helper.succeed();
	}

	/**
	 * A Degree 4 player kills a Votary and carries its Brine-sealed letter to a Voyager, the speaker the letter points
	 * to. The Voyager takes up the letter: the reading records the Court (the Garden question waits for D5).
	 */
	// Was decision BC-11. The runs disproved both forms of its premise: a D4 is never shown the Garden option, and the
	// letter reading (voyager_court_letter) is offered to a D4 and records the Court with a Relay Chart. Pinned here.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void subD5LetterCarrierAtVoyager(GameTestHelper helper) {
		clearCourtActors(helper);
		HarbingerVoyagerEntity voyager = PlaythroughNpcs.voyager(helper, PlaythroughSites.stand(helper, 16, 6));
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 16, 14));
		PlaythroughStates.adeptD4(p); // ARRANGE
		BrinedVotaryEntity votary = PlaythroughNpcs.brinedVotary(helper, PlaythroughSites.stand(helper, 16, 18));
		BlockPos fell = killWithFists(p, votary);
		p.pickUpNear(fell, ItemInit.brine_sealed_letter.get());
		helper.assertTrue(p.count(ItemInit.brine_sealed_letter.get()) == 1, "the letter dropped for the D4 killer");
		p.approach(voyager);
		DialogueView view = p.talkTo(voyager);
		view.assertLangKeys(helper, "the Voyager to a D4 letter-carrier");
		List<String> court = view.shownEvents().stream().filter(event -> event.startsWith("voyager_")).toList();
		PlaythroughLog.note(helper, "D4 letter-carrier at the Voyager: court options " + court + "; " + view);
		helper.assertFalse(view.offers(CourtDiscoveryDialogue.VOYAGER_LEARN), "the Garden question is not offered below D5");
		helper.assertTrue(view.offers(CourtDiscoveryDialogue.VOYAGER_LETTER), "the Voyager offers to read a D4 player's letter; "
				+ court);
		Outcome taken = p.choose(voyager, CourtDiscoveryDialogue.VOYAGER_LETTER);
		PlaythroughLog.note(helper, "the letter reading for a D4 letter-carrier: " + taken.describe());
		helper.assertTrue(CourtDiscoveryProgress.isKnown(p.player()), "the reading records the Court; " + taken);
		helper.succeed();
	}

	/** The Relay Chart says it is searching, then that no Garden lies within reach of the flat test world. */
	@GameTest(template = EMPTY, timeoutTicks = 300, batch = BATCH)
	public static void relayChartFindsNothing(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 16, 12));
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		PlaythroughStates.courtKnown(p, CourtProgress.Route.VOYAGER); // ARRANGE
		p.give(new ItemStack(ItemInit.relay_chart.get())); // ARRANGE: the Voyager's chart
		int mark = p.mark();
		PlaythroughClock.start(helper)
				.then(() -> {
					p.wield(ItemInit.relay_chart.get());
					p.use(InteractionHand.MAIN_HAND).said(CHART_SEARCHING);
				})
				.until(250, () -> p.chatSince(mark).stream().anyMatch(line -> line.has(CHART_NOT_FOUND)),
						"the chart answers that no Garden is near")
				.then(() -> {
					ItemStack chart = p.player().getMainHandItem();
					helper.assertTrue(chart.is(ItemInit.relay_chart.get()) && RelayChartItem.foundNothing(chart),
							"the chart remembers that it found nothing");
					List<Component> tooltip = new ArrayList<>();
					chart.getItem().appendHoverText(chart, Item.TooltipContext.of(helper.getLevel()), tooltip, TooltipFlag.NORMAL);
					String none = english("item.hemomancy.relay_chart.tooltip.none");
					helper.assertTrue(tooltip.stream().anyMatch(line -> line.getString().equals(none)), "and its tooltip says so: " + tooltip);
					PlaythroughLog.note(helper, "relay chart on the flat world: " + p.chatSince(mark));
				})
				.run();
	}

	/** Finding a real Garden needs Seats generated in a seeded Pelagic world. */
	@GameTest(template = EMPTY, timeoutTicks = 20, batch = BATCH, required = false)
	public static void relayChartFindsAGarden(GameTestHelper helper) {
		PlaythroughNotRun.notRun(helper, "the Relay Chart's bearing to a Lighthouse Garden needs Court Seats generated in a"
				+ " seeded Pelagic world; the GameTest world is flat and has none", PlaythroughNotRun.COURT_WORLDGEN,
				PlaythroughNotRun.BRINED_SEAT);
	}

	// =================================================================================================================
	// A house's site and its hospitality
	// =================================================================================================================

	/**
	 * Walking into a Tide Refuge (a synthetic start over the floor) makes the Court known, as found, and the walker a
	 * guest of the Holding Stone, each once. A dead and a spectating player standing in the same place record nothing.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = SITE_VISIT)
	public static void siteVisitDiscoversAndMakesGuest(GameTestHelper helper) {
		BlockPos centre = PlaythroughSites.stand(helper, 16, 12);
		PlaythroughSites.courtSite(helper, CourtSite.TIDE_REFUGE, centre); // ARRANGE: the outpost as generated
		PlaythroughPlayer dead = PlaythroughPlayer.create(helper, centre);
		PlaythroughPlayer spectator = PlaythroughPlayer.create(helper, centre);
		PlaythroughPlayer walker = PlaythroughPlayer.create(helper, centre);
		dead.die();
		spectator.player().setGameMode(GameType.SPECTATOR); // ARRANGE
		for (PlaythroughPlayer still : List.of(dead, spectator)) {
			still.player().tickCount = 0;
			Outcome ticked = tickTwice(still);
			ticked.didNotSay(SAID_DISCOVERY).didNotSay(SAID_RAISED);
			helper.assertFalse(CourtDiscoveryProgress.isKnown(still.player()), "a dead or spectating player finds nothing");
			helper.assertTrue(CourtLife.standing(still.player(), CourtHouse.HOLDING_STONE) == Standing.STRANGER,
					"and is no guest");
		}
		walker.player().tickCount = 0;
		Outcome arrived = tickTwice(walker);
		PlaythroughLog.note(helper, "walking into the Tide Refuge: " + arrived.describe());
		arrived.said(SAID_DISCOVERY).said(SAID_RAISED);
		helper.assertTrue(CourtDiscoveryProgress.knownBy(walker.player()) == CourtProgress.Route.FOUND, "found by visiting");
		helper.assertTrue(CourtLife.standing(walker.player(), CourtHouse.HOLDING_STONE) == Standing.GUEST, "a guest of the Holding Stone");
		helper.assertTrue(CourtLife.standing(walker.player(), CourtHouse.GIVING_LIGHT) == Standing.STRANGER, "and of no other house");
		tickTwice(walker).didNotSay(SAID_DISCOVERY).didNotSay(SAID_RAISED);
		helper.succeed();
	}

	/**
	 * The Gardener feeds a hungry guest, lets them rest, and hands the house's welcome gift once, after which the gift
	 * is no longer shown. A second rest inside the minute is refused with a line; after a minute (a game-time skip)
	 * it is given again.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = CLOCK_REST)
	public static void gardenerHospitality(GameTestHelper helper) {
		CourtMemberEntity gardener = member(helper, CourtRole.GARDENER, 16, 6);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 16, 12));
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		PlaythroughStates.courtStanding(p, CourtHouse.GIVING_LIGHT, Standing.GUEST); // ARRANGE: visited the Garden
		p.player().getFoodData().setFoodLevel(20); // ARRANGE: arrives fed
		p.approach(gardener);
		DialogueView fed = p.talkTo(gardener);
		fed.assertLangKeys(helper, "the Gardener").assertNoDeadEnds(helper, "the Gardener");
		helper.assertFalse(fed.offers(CourtMemberDialogue.FOOD), "no food offered to a fed guest: " + fed);
		p.player().getFoodData().setFoodLevel(10); // ARRANGE: hungry after the swim
		DialogueView hungry = p.talkTo(gardener);
		helper.assertTrue(hungry.offers(CourtMemberDialogue.FOOD), "food offered to a hungry guest: " + hungry);
		p.choose(gardener, CourtMemberDialogue.FOOD).didNotSay(SAID_REFUSED);
		helper.assertTrue(p.player().getFoodData().getFoodLevel() == 10 + CourtHospitality.FOOD_HUNGER,
				"the guest is fed: " + p.player().getFoodData().getFoodLevel());

		p.damage(10.0F);
		float hurt = p.player().getHealth();
		p.talkTo(gardener);
		p.choose(gardener, CourtMemberDialogue.REST).didNotSay(SAID_REFUSED);
		helper.assertTrue(p.player().getHealth() >= Math.min(p.player().getMaxHealth(), hurt + CourtHospitality.REST_HEAL) - 0.01F,
				"the rest heals: " + hurt + " -> " + p.player().getHealth());
		p.damage(4.0F);
		float again = p.player().getHealth();
		DialogueView restAgain = p.talkTo(gardener);
		helper.assertTrue(restAgain.offers(CourtMemberDialogue.REST), "rest is still shown inside the minute: " + restAgain);
		Outcome recent = p.choose(gardener, CourtMemberDialogue.REST).said(SAID_REST_RECENT);
		helper.assertTrue(p.player().getHealth() <= again + 0.01F, "no second rest inside the minute");
		// D-28 family: the client still steps to the "rested" reply node while the server refuses the rest.
		PlaythroughLog.note(helper, "D-28 family: a second rest inside the minute shows "
				+ english(restAgain.node("rested").map(DialogueNode::lines).orElse(List.of())) + " while the server says "
				+ recent.saidKeys());

		DialogueView gift = p.talkTo(gardener);
		helper.assertTrue(gift.offers(CourtMemberDialogue.GIFT), "the welcome gift is offered: " + gift);
		ItemStack expected = CourtHospitality.gift(CourtRole.GARDENER);
		p.choose(gardener, CourtMemberDialogue.GIFT).gave(expected.getItem(), expected.getCount());
		DialogueView given = p.talkTo(gardener);
		helper.assertFalse(given.offers(CourtMemberDialogue.GIFT), "the gift is hidden once taken: " + given);
		Outcome forged = p.forge(CourtMemberDialogue.GIFT, gardener.getId()).said(SAID_REFUSED);
		helper.assertTrue(forged.delta(expected.getItem()) == 0 && forged.drops().isEmpty(), "and never given twice; " + forged);

		PlaythroughTime.skipGameTime(helper, CourtMemberEntity.REST_COOLDOWN + 1L); // ARRANGE: a minute passes
		float later = p.player().getHealth();
		p.talkTo(gardener);
		p.choose(gardener, CourtMemberDialogue.REST).didNotSay(SAID_REST_RECENT);
		helper.assertTrue(p.player().getHealth() > later, "after a minute the guest may rest again");
		helper.succeed();
	}

	/**
	 * The Garden's custom (food) raises standing one step whether it is offered by dialogue or through the Gardener's
	 * custom trade card. Given again either way, the food is still taken and standing does not rise.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void customByDialogueAndByTrade(GameTestHelper helper) {
		CourtMemberEntity gardener = member(helper, CourtRole.GARDENER, 16, 6);
		PlaythroughPlayer byWord = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 12, 12));
		PlaythroughPlayer byCard = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 20, 12));
		for (PlaythroughPlayer p : List.of(byWord, byCard)) {
			PlaythroughStates.illuminatusD5(p); // ARRANGE
			PlaythroughStates.courtStanding(p, CourtHouse.GIVING_LIGHT, Standing.GUEST); // ARRANGE
			p.give(new ItemStack(Items.BREAD, 2)); // ARRANGE
		}
		byWord.wield(Items.BREAD);
		byWord.approach(gardener);
		DialogueView offering = byWord.talkTo(gardener);
		helper.assertTrue(offering.offers(CourtMemberDialogue.OFFER), "the custom is offered with food in hand: " + offering);
		byWord.choose(gardener, CourtMemberDialogue.OFFER).gave(Items.BREAD, -1).said(SAID_RAISED);
		helper.assertTrue(CourtLife.standing(byWord.player(), CourtHouse.GIVING_LIGHT) == Standing.FRIEND, "a friend of the Garden");
		byWord.talkTo(gardener);
		byWord.choose(gardener, CourtMemberDialogue.OFFER).gave(Items.BREAD, -1).didNotSay(SAID_RAISED);
		helper.assertTrue(CourtLife.standing(byWord.player(), CourtHouse.GIVING_LIGHT) == Standing.FRIEND, "the custom counts once");

		byCard.approach(gardener);
		DialogueView board = byCard.talkTo(gardener);
		helper.assertTrue(board.tradeCard(GARDENER_CUSTOM).isPresent(), "the Gardener shows the custom card: " + board.tradeCards());
		NpcTradeResultPacket first = byCard.trade(gardener, GARDENER_CUSTOM, slotOf(byCard, Items.BREAD));
		helper.assertTrue(first != null && !"REFUSED".equals(first.outcome().name()), "the card takes the custom: " + first);
		byCard.last().gave(Items.BREAD, -1).said(SAID_RAISED);
		helper.assertTrue(CourtLife.standing(byCard.player(), CourtHouse.GIVING_LIGHT) == Standing.FRIEND, "the card makes a friend too");
		NpcTradeResultPacket second = byCard.trade(gardener, GARDENER_CUSTOM, slotOf(byCard, Items.BREAD));
		PlaythroughLog.note(helper, "custom card again: " + second + "; " + byCard.last().describe());
		helper.assertTrue(second != null && !"REFUSED".equals(second.outcome().name()), "the card accepts it again: " + second);
		byCard.last().gave(Items.BREAD, -1).didNotSay(SAID_RAISED);
		helper.assertTrue(CourtLife.standing(byCard.player(), CourtHouse.GIVING_LIGHT) == Standing.FRIEND, "and raises nothing");
		helper.succeed();
	}

	/** The same custom given by dialogue and by the trade card should earn the same rapport with the Gardener. */
	// DECISION NPC-2: The Court custom trade card adds rapport (2 with the Gardener); the dialogue offering adds none,
	// though NPC_DIALOGUE_AND_TRADE.md says Court cards pay "exactly as the dialogue offering does".
	// Options: both add rapport / neither does / fix the doc.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void tradeCardPaysRapportDialogueDoesNot(GameTestHelper helper) {
		CourtMemberEntity gardener = member(helper, CourtRole.GARDENER, 16, 6);
		PlaythroughPlayer byWord = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 12, 12));
		PlaythroughPlayer byCard = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 20, 12));
		for (PlaythroughPlayer p : List.of(byWord, byCard)) {
			PlaythroughStates.illuminatusD5(p); // ARRANGE
			PlaythroughStates.courtStanding(p, CourtHouse.GIVING_LIGHT, Standing.GUEST); // ARRANGE
			p.give(new ItemStack(Items.BREAD, 1)); // ARRANGE
		}
		byWord.wield(Items.BREAD);
		byWord.approach(gardener);
		byWord.talkTo(gardener);
		byWord.choose(gardener, CourtMemberDialogue.OFFER);
		byCard.approach(gardener);
		byCard.talkTo(gardener);
		byCard.trade(gardener, GARDENER_CUSTOM, slotOf(byCard, Items.BREAD));
		String speaker = CourtRole.GARDENER.entityId();
		int wordRapport = HemoCapabilityAccess.npcTrades(byWord.player()).rapport(speaker);
		int cardRapport = HemoCapabilityAccess.npcTrades(byCard.player()).rapport(speaker);
		PlaythroughLog.note(helper, "Gardener rapport after the custom: by dialogue " + wordRapport + ", by card " + cardRapport
				+ "; standing " + CourtLife.standing(byWord.player(), CourtHouse.GIVING_LIGHT) + " / "
				+ CourtLife.standing(byCard.player(), CourtHouse.GIVING_LIGHT));
		helper.assertTrue(wordRapport == cardRapport, "the dialogue offering and the trade card pay the same rapport: "
				+ wordRapport + " vs " + cardRapport);
		helper.succeed();
	}

	/**
	 * A player brings the Kindling's custom to its Offering Bearer before ever visiting the Vent Sanctum, then visits.
	 * A player would expect the two acts to make them a friend, whatever the order, and so open the Champion.
	 */
	// DECISION BC-1: Court standing depends on act order: a custom given before any visit lifts a stranger to guest,
	// the later visit then adds nothing, and FRIEND (which the Kindled Champion needs) is out of reach for good.
	// Options: order-independent standing / allow re-earning / keep.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = SITE_ORDER, required = false)
	public static void customBeforeVisitLocksGuest(GameTestHelper helper) {
		clearCourtActors(helper);
		CourtMemberEntity bearer = member(helper, CourtRole.OFFERING_BEARER, 4, 6);
		CourtMemberEntity tender = member(helper, CourtRole.HEAD_TENDER, 28, 6);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 12));
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		PlaythroughStates.courtKnown(p, CourtProgress.Route.VOYAGER); // ARRANGE
		p.give(strain(MorphlingStrains.KINBUD, APEX)); // ARRANGE: an Apex strain grown at home
		p.give(new ItemStack(Items.BREAD, 1)); // ARRANGE
		p.wield(Items.BREAD);
		p.approach(bearer);
		DialogueView offering = p.talkTo(bearer);
		helper.assertTrue(offering.offers(CourtMemberDialogue.OFFER), "the Offering Bearer takes the custom: " + offering);
		Outcome custom = p.choose(bearer, CourtMemberDialogue.OFFER);
		Standing afterCustom = CourtLife.standing(p.player(), CourtHouse.KINDLING);
		BlockPos sanctum = PlaythroughSites.stand(helper, 24, 16);
		PlaythroughSites.courtSite(helper, CourtSite.VENT_SANCTUM, sanctum); // ARRANGE: the Seat as generated
		p.moveTo(sanctum);
		p.player().tickCount = 0;
		Outcome visit = tickTwice(p);
		Standing afterVisit = CourtLife.standing(p.player(), CourtHouse.KINDLING);
		p.approach(tender);
		DialogueView head = p.talkTo(tender);
		PlaythroughLog.note(helper, "custom then visit: " + custom.saidKeys() + " -> " + afterCustom + "; visit "
				+ visit.saidKeys() + " -> " + afterVisit + "; Head-tender " + head);
		helper.assertTrue(afterVisit == Standing.FRIEND, "a custom and a visit make a friend in either order: " + afterVisit);
		helper.assertTrue(head.offers(CourtMemberDialogue.CHALLENGE), "and the Head-tender lets them challenge: " + head);
		helper.succeed();
	}

	// =================================================================================================================
	// The lesson, membership and the inner houses
	// =================================================================================================================

	/**
	 * The Lightkeeper holds the lesson back at Degree 4 and teaches it at Degree 5, once. Taught, a worn Holdfast
	 * bonds x1.5 inside a Holding Stone site, and at the plain rate outside the Court and inside another house's site.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = SITE_LESSON)
	public static void lightkeeperLessonAndBonding(GameTestHelper helper) {
		CourtMemberEntity keeper = member(helper, CourtRole.LIGHTKEEPER, 4, 6);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 12));
		PlaythroughStates.adeptD4(p); // ARRANGE
		PlaythroughStates.courtStanding(p, CourtHouse.GIVING_LIGHT, Standing.GUEST); // ARRANGE
		p.approach(keeper);
		DialogueView early = p.talkTo(keeper);
		early.assertLangKeys(helper, "the Lightkeeper at D4").assertNoDeadEnds(helper, "the Lightkeeper at D4");
		helper.assertTrue(early.hasNode("lesson_early") && !early.offers(CourtMemberDialogue.LESSON),
				"at D4 the lesson waits: " + early);
		p.forge(CourtMemberDialogue.LESSON, keeper.getId()).said(CourtLessonRules.Outcome.REFUSED_DEGREE.translationKey());
		helper.assertFalse(CourtLife.hasLesson(p.player()), "nothing taught at D4");

		PlaythroughStates.illuminatusD5(p); // ARRANGE: the Crimson Lodge is its own band
		DialogueView ready = p.talkTo(keeper);
		helper.assertTrue(ready.offers(CourtMemberDialogue.LESSON), "at D5 the Lightkeeper teaches: " + ready);
		p.choose(keeper, CourtMemberDialogue.LESSON).said(CourtLessonRules.Outcome.TAUGHT.translationKey());
		helper.assertTrue(CourtLife.hasLesson(p.player()), "the lesson is taught");
		// DECISION BC-7 (logged): "Feed it there" reads as Incubator feeding, but the x1.5 applies to worn upkeep only.
		PlaythroughLog.note(helper, "DECISION BC-7 lesson wording: " + english(ready.node("lesson").map(DialogueNode::lines)
				.orElse(List.of())));
		DialogueView known = p.talkTo(keeper);
		helper.assertTrue(known.hasNode("lesson_known") && !known.offers(CourtMemberDialogue.LESSON), "taught once: " + known);

		BlockPos stand = PlaythroughSites.stand(helper, 20, 12);
		p.moveTo(stand);
		wear(p, strain(MorphlingStrains.HOLDFAST, 1)); // ARRANGE: a Holding Stone strain
		double outside = bond(p);
		helper.assertTrue(outside > 0.0D, "upkeep credits bonding outside the Court: " + outside);
		Runnable refuge = PlaythroughSites.courtSite(helper, CourtSite.TIDE_REFUGE, stand); // ARRANGE
		double home = bond(p);
		refuge.run();
		Runnable cleft = PlaythroughSites.courtSite(helper, CourtSite.SCREENED_CLEFT, stand); // ARRANGE
		double away = bond(p);
		cleft.run();
		PlaythroughLog.note(helper, "bonding per upkeep tick: outside " + outside + ", Holding Stone " + home + ", Veiled Fold " + away);
		helper.assertTrue(Math.abs(home - outside * CourtLessonRules.MULTIPLIER) < 1.0E-6D,
				"x" + CourtLessonRules.MULTIPLIER + " in the strain's own house site: " + home + " against " + outside);
		helper.assertTrue(Math.abs(away - outside) < 1.0E-6D, "the plain rate in another house's site: " + away);
		unwear(p);
		helper.succeed();
	}

	/**
	 * A D5 guest joins the Holding Stone through its Braced elder, switches once to the Woven Current through its Lamp
	 * colony (the house left drops to guest), and is refused a third change. A stranger and a Degree 4 guest are
	 * refused with lines that say why.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void joinSwitchAndThirdChange(GameTestHelper helper) {
		CourtMemberEntity braced = member(helper, CourtRole.BRACED_ELDER, 4, 6);
		CourtMemberEntity lamp = member(helper, CourtRole.LAMP_COLONY, 16, 6);
		CourtMemberEntity bell = member(helper, CourtRole.BELLWARDEN, 28, 6);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 12));
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		for (CourtHouse house : List.of(CourtHouse.HOLDING_STONE, CourtHouse.WOVEN_CURRENT, CourtHouse.STILL_BELL)) {
			PlaythroughStates.courtStanding(p, house, Standing.GUEST); // ARRANGE: each site visited
		}
		p.approach(braced);
		DialogueView join = p.talkTo(braced);
		join.assertLangKeys(helper, "the Braced elder").assertNoDeadEnds(helper, "the Braced elder");
		helper.assertTrue(join.offers(CourtMemberDialogue.JOIN), "the Braced elder asks the guest in: " + join);
		p.choose(braced, CourtMemberDialogue.JOIN).said("hemomancy.court.membership.joined");
		helper.assertTrue(CourtLife.house(p.player()) == CourtHouse.HOLDING_STONE, "a member of the Holding Stone");

		p.approach(lamp);
		DialogueView switching = p.talkTo(lamp);
		helper.assertTrue(switching.offers(CourtMemberDialogue.JOIN), "the Lamp colony asks them in: " + switching);
		// DECISION BC-3 (logged): asking to join spends the one free switch, and nothing shown before says so.
		PlaythroughLog.note(helper, "DECISION BC-3 the switch is offered as an ordinary join: "
				+ english(switching.node("join").map(DialogueNode::lines).orElse(List.of())));
		p.choose(lamp, CourtMemberDialogue.JOIN).said("hemomancy.court.membership.switched");
		helper.assertTrue(CourtLife.house(p.player()) == CourtHouse.WOVEN_CURRENT && CourtLife.progress(p.player()).switchUsed(),
				"switched, and the switch is spent");
		helper.assertTrue(CourtLife.standing(p.player(), CourtHouse.HOLDING_STONE) == Standing.GUEST, "the house left keeps them as a guest");

		p.approach(bell);
		DialogueView settled = p.talkTo(bell);
		helper.assertTrue(settled.hasNode("join_settled") && !settled.offers(CourtMemberDialogue.JOIN),
				"the Bellwarden says the move is used: " + settled);
		p.forge(CourtMemberDialogue.JOIN, bell.getId()).said("hemomancy.court.membership.refused_switch_used");
		helper.assertTrue(CourtLife.house(p.player()) == CourtHouse.WOVEN_CURRENT, "membership is fixed");

		PlaythroughPlayer stranger = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 16));
		PlaythroughStates.illuminatusD5(stranger); // ARRANGE
		PlaythroughStates.courtKnown(stranger, CourtProgress.Route.VOYAGER); // ARRANGE
		stranger.approach(braced);
		DialogueView unknown = stranger.talkTo(braced);
		helper.assertTrue(unknown.hasNode("join_standing") && !unknown.offers(CourtMemberDialogue.JOIN), "a stranger is not asked in: " + unknown);
		stranger.forge(CourtMemberDialogue.JOIN, braced.getId()).said("hemomancy.court.membership.refused_standing");
		helper.assertTrue(CourtLife.house(stranger.player()) == null, "no house for a stranger");

		PlaythroughPlayer adept = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 22, 16));
		PlaythroughStates.adeptD4(adept); // ARRANGE
		PlaythroughStates.courtStanding(adept, CourtHouse.HOLDING_STONE, Standing.GUEST); // ARRANGE
		adept.approach(braced);
		DialogueView early = adept.talkTo(braced);
		helper.assertTrue(early.hasNode("join_early") && !early.offers(CourtMemberDialogue.JOIN), "a D4 guest is told to wait: " + early);
		adept.forge(CourtMemberDialogue.JOIN, braced.getId()).said("hemomancy.court.membership.refused_degree");
		helper.assertTrue(CourtLife.house(adept.player()) == null, "no house below the fifth Degree");
		helper.succeed();
	}

	/**
	 * Each inner house in turn, at its own outpost: the visit makes a guest, its custom item (stone, a fish, string,
	 * ink, wool, a bone) offered to its custom role makes a friend, its gift role hands its gift, and its join role asks
	 * the guest in. The Still Bell's Sleeper offers nothing but its house, the item question and leave.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = SITE_HOUSES)
	public static void innerHouseSweep(GameTestHelper helper) {
		clearCourtActors(helper);
		BlockPos centre = PlaythroughSites.stand(helper, 16, 12);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, centre);
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		PlaythroughStates.courtKnown(p, CourtProgress.Route.VOYAGER); // ARRANGE
		Map<CourtHouse, CourtSite> outposts = new EnumMap<>(CourtHouse.class);
		outposts.put(CourtHouse.HOLDING_STONE, CourtSite.TIDE_REFUGE);
		outposts.put(CourtHouse.SHARED_WAKE, CourtSite.WAKE_NURSERY);
		outposts.put(CourtHouse.WOVEN_CURRENT, CourtSite.LINE_CAMP);
		outposts.put(CourtHouse.VEILED_FOLD, CourtSite.SCREENED_CLEFT);
		outposts.put(CourtHouse.STILL_BELL, CourtSite.STILL_BELL);
		outposts.put(CourtHouse.LAST_MARROW, CourtSite.WHALE_FALL);
		Map<CourtHouse, Item> customs = Map.of(CourtHouse.HOLDING_STONE, Items.STONE, CourtHouse.SHARED_WAKE, Items.COD,
				CourtHouse.WOVEN_CURRENT, Items.STRING, CourtHouse.VEILED_FOLD, Items.INK_SAC, CourtHouse.STILL_BELL,
				Items.WHITE_WOOL, CourtHouse.LAST_MARROW, Items.BONE);
		List<String> findings = new ArrayList<>();
		for (Map.Entry<CourtHouse, CourtSite> entry : outposts.entrySet()) {
			CourtHouse house = entry.getKey();
			Runnable site = PlaythroughSites.courtSite(helper, entry.getValue(), centre); // ARRANGE: the outpost
			List<CourtMemberEntity> residents = new ArrayList<>();
			int x = 6;
			for (CourtRole role : CourtRole.of(house)) {
				residents.add(member(helper, role, x, 8));
				x += 8;
			}
			p.moveTo(centre);
			p.player().tickCount = 0;
			tickTwice(p);
			if (CourtLife.standing(p.player(), house) != Standing.GUEST) findings.add(house + ": the visit made no guest");
			for (CourtMemberEntity resident : residents) {
				CourtRole role = resident.role();
				p.approach(resident);
				// ARRANGE: an empty hand. A held Lead (the Lamp-colony's gift) leashes a member instead (BC-12).
				PlaythroughRites.emptyHand(p);
				DialogueView view = p.talkTo(resident);
				findings.addAll(view.missingLangKeys().stream().map(key -> role + ": " + key).toList());
				findings.addAll(view.deadEnds().stream().map(end -> role + ": " + end).toList());
				if (role == CourtRole.SLEEPER) {
					if (!view.reachableEvents().isEmpty()) findings.add("the Sleeper offers events " + view.reachableEvents());
					Set<String> allowed = Set.of("hemomancy.dialogue.court.option.house",
							"hemomancy.dialogue.court.option.ask_about_item", "hemomancy.dialogue.court.option.leave");
					DialogueNode root = view.tree().getNode(view.tree().startNodeId());
					if (root != null) {
						for (var option : root.options()) {
							if (!allowed.contains(option.text())) findings.add("the Sleeper offers " + option.text());
						}
					}
					PlaythroughLog.note(helper, "the Sleeper's nodes: " + view.nodes().keySet());
					continue;
				}
				if (role.offers(CourtRole.Offer.CUSTOM)) {
					p.give(new ItemStack(customs.get(house))); // ARRANGE: carried in
					p.wield(customs.get(house));
					DialogueView holding = p.talkTo(resident);
					if (!holding.offers(CourtMemberDialogue.OFFER)) {
						findings.add(role + ": the custom " + customs.get(house) + " is not offered: " + holding);
					} else {
						Outcome given = p.choose(resident, CourtMemberDialogue.OFFER);
						if (given.delta(customs.get(house)) != -1) findings.add(role + ": the custom took " + given.inventoryDelta());
						if (CourtLife.standing(p.player(), house) != Standing.FRIEND) {
							findings.add(role + ": the custom left standing " + CourtLife.standing(p.player(), house));
						}
					}
				}
				if (role.offers(CourtRole.Offer.GIFT) && !CourtLife.progress(p.player()).actCounted(house, CourtHospitality.GIFT_ACT)) {
					DialogueView gift = p.talkTo(resident);
					ItemStack expected = CourtHospitality.gift(role);
					if (!gift.offers(CourtMemberDialogue.GIFT)) {
						findings.add(role + ": no gift offered: " + gift);
					} else {
						Outcome taken = p.choose(resident, CourtMemberDialogue.GIFT);
						if (taken.delta(expected.getItem()) != expected.getCount()) {
							findings.add(role + ": the gift gave " + taken.inventoryDelta() + ", not " + expected);
						}
					}
				}
				PlaythroughRites.emptyHand(p); // ARRANGE: the gift goes to the pack (BC-12)
				if (role.offers(CourtRole.Offer.JOIN) && !p.talkTo(resident).offers(CourtMemberDialogue.JOIN)) {
					findings.add(role + ": a D5 guest is not asked in");
				}
			}
			PlaythroughLog.note(helper, house + " at " + entry.getValue() + ": standing " + CourtLife.standing(p.player(), house));
			residents.forEach(Entity::discard);
			site.run();
		}
		PlaythroughLog.note(helper, "inner houses: " + (findings.isEmpty() ? "no findings" : findings));
		helper.assertTrue(findings.isEmpty(), "every inner house welcomes, takes its custom and gives its gift: " + findings);
		helper.succeed();
	}

	/**
	 * The Lamp-colony's gift is two Leads, and a gift lands in the empty hand the player talked with. Their next
	 * right-click on any Court member then leashes it (vanilla {@code Mob#interact}) instead of opening its dialogue.
	 */
	// DECISION BC-12: Court members can be leashed, so a held Lead (the Lamp-colony's own gift) ties a member up rather
	// than talking to it, and lets a player drag a mobile member off its mark. Options: canBeLeashed false for Court
	// members, as for villagers / keep leashing (a deliberate trick) but open the dialogue first / keep.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void heldLeadLeashesACourtMember(GameTestHelper helper) {
		clearCourtActors(helper);
		CourtMemberEntity colony = member(helper, CourtRole.LAMP_COLONY, 10, 8);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 12));
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		PlaythroughStates.courtKnown(p, CourtProgress.Route.VOYAGER); // ARRANGE
		p.give(new ItemStack(Items.LEAD, 2)); // ARRANGE: the Lamp-colony's gift, still in hand
		p.wield(Items.LEAD);
		p.approach(colony);
		Optional<DialogueView> view = p.tryTalkTo(colony);
		PlaythroughLog.note(helper, "a held Lead: dialogue " + view.isPresent() + ", leashed " + colony.isLeashed());
		helper.assertTrue(view.isPresent() && !colony.isLeashed(),
				"right-clicking a Court member with a Lead talks to it rather than leashing it");
		helper.succeed();
	}

	// =================================================================================================================
	// Wronging the Giving Light
	// =================================================================================================================

	/**
	 * In a Lighthouse Garden a guest breaks its moss, reaches for one of the petrified, and swings at a Gardener: each
	 * is a recorded wrong, the one Warden hunting them takes each new wrong, and the Garden turns them away. Holding
	 * bread out to the Warden makes amends. A new wrong, a death, and the record is still there after the respawn;
	 * the Gardener takes the amends this time.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = SITE_WRONGS)
	public static void transgressionWardenAndAmends(GameTestHelper helper) {
		clearCourtActors(helper);
		BlockPos centre = PlaythroughSites.stand(helper, 16, 12);
		PlaythroughSites.courtSite(helper, CourtSite.LIGHTHOUSE_GARDEN, centre); // ARRANGE: the Seat as generated
		BlockPos moss = PlaythroughSites.stand(helper, 12, 10);
		BlockPos secondMoss = PlaythroughSites.stand(helper, 12, 14);
		BlockPos petrified = PlaythroughSites.stand(helper, 20, 10);
		PlaythroughRites.set(helper, moss, Blocks.MOSS_BLOCK); // ARRANGE: the Garden's growth
		PlaythroughRites.set(helper, secondMoss, Blocks.MOSS_BLOCK);
		PlaythroughRites.set(helper, petrified, BlockInit.petrified_follower.get()); // ARRANGE: the undercroft
		CourtMemberEntity gardener = member(helper, CourtRole.GARDENER, 16, 6);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, centre);
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		PlaythroughStates.courtStanding(p, CourtHouse.GIVING_LIGHT, Standing.GUEST); // ARRANGE

		p.breakBlock(moss);
		helper.assertTrue(CourtLife.progress(p.player()).transgressionCount() == 1, "breaking the moss is a wrong");
		helper.assertTrue(hunting(helper, p).size() == 1, "one Warden wakes: " + hunting(helper, p).size());
		helper.assertTrue(CourtLife.standing(p.player(), CourtHouse.GIVING_LIGHT) == Standing.STRANGER, "the Garden forgets the guest");
		p.breakBlock(petrified);
		helper.assertTrue(helper.getLevel().getBlockState(petrified).is(BlockInit.petrified_follower.get()), "the petrified stay");
		helper.assertTrue(CourtLife.progress(p.player()).transgressionCount() == 2
				&& BloomWardenRules.UNDERCROFT.equals(CourtLife.progress(p.player()).transgressionAct()), "reaching for them is a wrong");
		helper.assertTrue(hunting(helper, p).size() == 1, "still one Warden");
		p.approach(gardener);
		float before = gardener.getHealth();
		p.attack(gardener);
		helper.assertTrue(gardener.getHealth() == before, "the Gardener is protected");
		helper.assertTrue(CourtLife.progress(p.player()).transgressionCount() == 3, "but the swing is a wrong");
		helper.assertTrue(hunting(helper, p).size() == 1, "and the same Warden answers it");

		DialogueView turned = p.talkTo(gardener);
		turned.assertLangKeys(helper, "the Gardener to a transgressor").assertNoDeadEnds(helper, "the Gardener to a transgressor");
		helper.assertTrue(turned.lineKeys().contains("hemomancy.court.gardener.greet.transgressor"), "greeted as a transgressor: " + turned.lineKeys());
		for (String refused : List.of(CourtMemberDialogue.REST, CourtMemberDialogue.GIFT, CourtMemberDialogue.FOOD)) {
			helper.assertFalse(turned.offers(refused), "no " + refused + " while turned away: " + turned);
		}
		p.forge(CourtMemberDialogue.REST, gardener.getId()).said(SAID_REFUSED);

		BloomWardenEntity warden = hunting(helper, p).getFirst();
		p.give(new ItemStack(Items.BREAD, 2)); // ARRANGE
		p.wield(Items.BREAD);
		Outcome amends = p.interactOn(warden).said(SAID_AMENDS).gave(Items.BREAD, -1);
		PlaythroughLog.note(helper, "amends at the Warden: " + amends.describe());
		helper.assertFalse(CourtLife.hasTransgression(p.player()), "the wrong is cleared");
		helper.assertTrue(warden.outcome() == BloomWardenRules.Outcome.RECONCILED, "the Warden's fight is over");
		helper.assertTrue(CourtLife.standing(p.player(), CourtHouse.GIVING_LIGHT) == Standing.GUEST, "a guest again");

		p.breakBlock(secondMoss);
		helper.assertTrue(CourtLife.hasTransgression(p.player()), "a new wrong is recorded");
		p.die();
		p.respawn();
		helper.assertTrue(CourtLife.hasTransgression(p.player()), "the record survives death");
		p.give(new ItemStack(Items.BREAD, 1)); // ARRANGE: food brought back after the respawn
		p.wield(Items.BREAD);
		p.approach(gardener);
		DialogueView owed = p.talkTo(gardener);
		helper.assertTrue(owed.offers(CourtMemberDialogue.AMENDS), "the Gardener offers amends to a transgressor with food: " + owed);
		p.choose(gardener, CourtMemberDialogue.AMENDS).said(SAID_AMENDS).gave(Items.BREAD, -1);
		helper.assertFalse(CourtLife.hasTransgression(p.player()), "the Gardener's amends clear the wrong");
		helper.assertTrue(CourtLife.standing(p.player(), CourtHouse.GIVING_LIGHT) == Standing.GUEST, "and the guest is welcome");
		helper.succeed();
	}

	// =================================================================================================================
	// The Kindled Champion
	// =================================================================================================================

	/**
	 * The Head-tender names each closed gate (the Court unknown, no Apex strain, not yet a friend of the Kindling)
	 * and offers the challenge once it opens. A wins: the Champion yields, the title moves and Hydra's head rises over
	 * the victor. B dies to it: the day's wait is set, kept through the respawn, and ends when the day has passed.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = CLOCK_CHAMPION)
	public static void kindledChampionWinAndLoss(GameTestHelper helper) {
		clearCourtActors(helper);
		CourtMemberEntity tender = member(helper, CourtRole.HEAD_TENDER, 14, 8);
		member(helper, CourtRole.KINDLED_ELDER, 18, 8);
		member(helper, CourtRole.KINDLED_ELDER, 22, 8);
		BlockPos crown = helper.absolutePos(new BlockPos(18, PlaythroughSites.STAND_Y + 4, 4));
		PlaythroughRites.set(helper, crown, BlockInit.hydra_crown.get()); // ARRANGE: a crowned chimney
		PlaythroughPlayer a = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 12));
		PlaythroughPlayer b = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 16));
		PlaythroughStates.illuminatusD5(a); // ARRANGE
		PlaythroughStates.illuminatusD5(b); // ARRANGE
		KindledChampionEntity[] fight = new KindledChampionEntity[2];
		long[] lostAt = new long[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					a.approach(tender);
					gateNode(helper, a, tender, "challenge_refused_unknown");
					PlaythroughStates.courtKnown(a, CourtProgress.Route.VOYAGER); // ARRANGE
					gateNode(helper, a, tender, "challenge_refused_strain");
					a.give(strain(MorphlingStrains.KINBUD, APEX)); // ARRANGE: an Apex strain
					gateNode(helper, a, tender, "challenge_refused_standing");
					PlaythroughStates.courtStanding(a, CourtHouse.KINDLING, Standing.FRIEND); // ARRANGE
					DialogueView open = a.talkTo(tender);
					open.assertLangKeys(helper, "the Head-tender, gate open").assertNoDeadEnds(helper, "the Head-tender");
					helper.assertTrue(open.offers(CourtMemberDialogue.CHALLENGE), "the challenge opens: " + open);
					a.choose(tender, CourtMemberDialogue.CHALLENGE);
					fight[0] = facing(helper, a);
					helper.assertTrue(fight[0] != null, "the Champion steps out for A");
				})
				.until(200, () -> fight[0].fighting(), "the Champion's opening ends")
				.then(() -> {
					heavyBlow(helper, a); // ARRANGE: the fight's balance is not under test
					Outcome won = a.attack(fight[0]).said(CHAMPION_WON);
					PlaythroughLog.note(helper, "the usurpation won: " + won.describe());
					helper.assertTrue(fight[0].outcome() == KindledChampionRules.Outcome.YIELDED, "the Champion yields alive");
					helper.assertTrue(CourtLife.isChampion(a.player()), "A holds the title");
					helper.assertFalse(helper.getLevel().getEntitiesOfClass(HydraRisingHead.class, new AABB(crown).inflate(3.0D)).isEmpty(),
							"Hydra's head rises over the victor");
					helper.assertFalse(a.talkTo(tender).offers(CourtMemberDialogue.CHALLENGE), "the holder is not offered it again");

					PlaythroughStates.courtKnown(b, CourtProgress.Route.VOYAGER); // ARRANGE
					b.give(strain(MorphlingStrains.KINBUD, APEX)); // ARRANGE
					PlaythroughStates.courtStanding(b, CourtHouse.KINDLING, Standing.FRIEND); // ARRANGE
					b.approach(tender);
					b.talkTo(tender);
					b.choose(tender, CourtMemberDialogue.CHALLENGE);
					fight[1] = facing(helper, b);
					helper.assertTrue(fight[1] != null, "the Champion steps out for B");
					BlockPos fell = b.player().blockPosition();
					lostAt[0] = helper.getLevel().getGameTime();
					b.die();
					helper.assertTrue(fight[1].outcome() == KindledChampionRules.Outcome.CHALLENGER_DIED, "B has lost");
					b.respawn();
					long retry = CourtLife.progress(b.player()).usurpRetryAt();
					helper.assertTrue(retry == lostAt[0] + CourtChampionRules.USURP_RETRY_TICKS, "a day's wait, kept through death: " + retry);
					helper.assertFalse(CourtLife.isChampion(b.player()), "B holds no title");
					b.pickUpNear(fell, MorphlingStrains.KINBUD.item().get());
					if (b.count(MorphlingStrains.KINBUD.item().get()) == 0) {
						PlaythroughLog.note(helper, "B's strain scattered out of reach on death; carried again");
						b.give(strain(MorphlingStrains.KINBUD, APEX)); // ARRANGE: the strain recovered
					}
					b.approach(tender);
					DialogueView waiting = b.talkTo(tender);
					PlaythroughLog.note(helper, "B after the loss carries " + b.count(MorphlingStrains.KINBUD.item().get())
							+ " Kinbud; Head-tender " + waiting);
					helper.assertFalse(waiting.offers(CourtMemberDialogue.CHALLENGE), "no challenge inside the day: " + waiting);
					PlaythroughTime.skipGameTime(helper, CourtChampionRules.USURP_RETRY_TICKS + 1L); // ARRANGE: a day passes
					DialogueView again = b.talkTo(tender);
					helper.assertTrue(again.offers(CourtMemberDialogue.CHALLENGE), "after the day the challenge opens again: " + again);
				})
				.run();
	}

	/**
	 * B wears an Apex strain and carries an empty Morphling Jar, challenges and dies: the strain goes into the jar.
	 * Back from the respawn, the Head-tender should name the day's wait, which is the reason, not ask for a strain.
	 */
	// FIX: LF-13: CourtChampionRules.challenge checks the strain before the retry wait, so after a lost fight the
	// player is told to bring an Apex strain (the death moved theirs into the Jar, which the gate does not read) rather
	// than that the day must pass. The fix is to check retryElapsed before the strain (and standing) in challenge().
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void championRefusalNamesTheWait(GameTestHelper helper) {
		clearCourtActors(helper);
		CourtMemberEntity tender = member(helper, CourtRole.HEAD_TENDER, 10, 8);
		member(helper, CourtRole.KINDLED_ELDER, 16, 8);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 14));
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		PlaythroughStates.courtKnown(p, CourtProgress.Route.VOYAGER); // ARRANGE
		PlaythroughStates.courtStanding(p, CourtHouse.KINDLING, Standing.FRIEND); // ARRANGE
		wear(p, strain(MorphlingStrains.KINBUD, APEX)); // ARRANGE
		p.give(new ItemStack(ItemInit.morphling_jar.get())); // ARRANGE: an empty jar in the pack
		p.approach(tender);
		helper.assertTrue(p.talkTo(tender).offers(CourtMemberDialogue.CHALLENGE), "the gate is open");
		p.choose(tender, CourtMemberDialogue.CHALLENGE);
		helper.assertTrue(facing(helper, p) != null, "the Champion steps out");
		BlockPos fell = p.player().blockPosition();
		p.die();
		p.respawn();
		p.pickUpNear(fell, ItemInit.morphling_jar.get());
		PlaythroughLog.note(helper, "after the loss: jar carried " + p.count(ItemInit.morphling_jar.get()) + ", worn "
				+ HemoCapabilityAccess.getEquippedMorphling(p.player()).hasMorphling() + ", gate " + CourtLife.championGate(p.player()));
		p.approach(tender);
		DialogueView view = p.talkTo(tender);
		helper.assertTrue(view.hasNode("challenge_refused_wait"), "the Head-tender names the wait: " + view.nodes().keySet());
		p.forge(CourtMemberDialogue.CHALLENGE, tender.getId()).said(CHAMPION_REFUSED_WAIT);
		helper.succeed();
	}

	/**
	 * A logs out mid-fight and B swims more than 64 blocks off. A player would expect running from the usurpation to
	 * cost the same day as dying to it.
	 */
	// DECISION BC-2: Logging out or leaving 64 blocks ends a usurpation (Outcome GONE after 100 ticks) with no retry
	// wait, while dying costs a day. Options: the same wait for escape / pause and resume / keep.
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = BATCH, required = false)
	public static void usurpationEscapedByLogout(GameTestHelper helper) {
		clearCourtActors(helper);
		CourtMemberEntity tender = member(helper, CourtRole.HEAD_TENDER, 12, 8);
		member(helper, CourtRole.KINDLED_ELDER, 16, 8);
		member(helper, CourtRole.KINDLED_ELDER, 20, 8);
		PlaythroughPlayer a = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 14));
		PlaythroughPlayer b = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 16, 14));
		KindledChampionEntity[] fights = new KindledChampionEntity[2];
		List<PlaythroughPlayer> both = List.of(a, b);
		PlaythroughClock.start(helper)
				.then(() -> {
					for (int i = 0; i < 2; i++) {
						PlaythroughPlayer p = both.get(i);
						PlaythroughStates.illuminatusD5(p); // ARRANGE
						openChampionGate(p); // ARRANGE
						p.approach(tender);
						p.talkTo(tender);
						p.choose(tender, CourtMemberDialogue.CHALLENGE);
						fights[i] = facing(helper, p);
						helper.assertTrue(fights[i] != null, "the Champion steps out for " + i);
					}
					a.offline(); // logs out mid-fight
					b.moveTo(fights[1].blockPosition().east(70)); // swims off
				})
				.until(300, () -> fights[0].outcome() != null && fights[1].outcome() != null, "both usurpations end")
				.then(() -> {
					a.online();
					long now = helper.getLevel().getGameTime();
					long aWait = CourtLife.progress(a.player()).usurpRetryAt();
					long bWait = CourtLife.progress(b.player()).usurpRetryAt();
					PlaythroughLog.note(helper, "escaped usurpations: logout " + fights[0].outcome() + " wait " + aWait
							+ ", 70 blocks " + fights[1].outcome() + " wait " + bWait + ", now " + now);
					helper.assertTrue(aWait > now && bWait > now, "running from the usurpation costs the day dying does: "
							+ aWait + " / " + bWait);
				})
				.run();
	}

	/** Choosing the challenge should ask once more before the Champion steps out. */
	// DECISION BC-4: The Kindled Champion challenge fires the moment the option is chosen; there is no confirm node.
	// Options: a confirm node / keep.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void challengeStartsWithoutConfirm(GameTestHelper helper) {
		clearCourtActors(helper);
		CourtMemberEntity tender = member(helper, CourtRole.HEAD_TENDER, 12, 8);
		member(helper, CourtRole.KINDLED_ELDER, 18, 8);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 12, 14));
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		openChampionGate(p); // ARRANGE
		p.approach(tender);
		DialogueView view = p.talkTo(tender);
		helper.assertTrue(view.offers(CourtMemberDialogue.CHALLENGE), "the gate is open: " + view);
		Outcome chosen = p.choose(tender, CourtMemberDialogue.CHALLENGE);
		KindledChampionEntity champion = facing(helper, p);
		PlaythroughLog.note(helper, "choosing the challenge: " + chosen.describe() + "; Champion out " + (champion != null)
				+ "; the node shown after it: " + english(view.node("challenge").map(DialogueNode::lines).orElse(List.of())));
		helper.assertTrue(champion == null, "the Champion waits for a confirmation before stepping out");
		helper.succeed();
	}

	/** A Degree 4 player with an Apex strain from a shared Incubator should not be offered the usurpation. */
	// DECISION BC-8: The Kindled Champion has no degree gate, while joining a house and the lesson need D5.
	// Options: a D5 gate / keep.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void championHasNoDegreeGate(GameTestHelper helper) {
		CourtMemberEntity tender = member(helper, CourtRole.HEAD_TENDER, 12, 8);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 12, 14));
		PlaythroughStates.adeptD4(p); // ARRANGE
		openChampionGate(p); // ARRANGE: the Apex strain was grown by a D5 partner in a shared Incubator
		p.approach(tender);
		DialogueView view = p.talkTo(tender);
		PlaythroughLog.note(helper, "a D4 at the Head-tender with an Apex strain and the Kindling's friendship: " + view
				+ "; gate " + CourtLife.championGate(p.player()));
		helper.assertFalse(view.offers(CourtMemberDialogue.CHALLENGE), "a D4 is not offered the usurpation: " + view);
		helper.succeed();
	}

	// =================================================================================================================
	// Pointers past the Garden
	// =================================================================================================================

	/**
	 * A D5 guest of the Garden who carries the Relay Chart asks everyone on the way (the Vicar, the Voyager, the three
	 * Giving Light roles) and reads the chart. Something should place the Vent Sanctum before the player stands at it.
	 * BC-5, BC-9 and BC-10 are logged from the same pass.
	 */
	// DECISION BC-6: The Relay Chart points only at the nearest Lighthouse Garden and no line places the Vent Sanctum
	// or the outposts (the residents' directions name depths, not places). Options: chart modes / resident directions
	// name places / keep.
	// DECISION BC-5 (logged): joining the Shared Wake needs its rare nursery, and nothing points to it.
	// DECISION BC-9 (logged): with Pelagic worldgen off, the Vicar, Voyager and Alchemist still send the player seaward.
	// DECISION BC-10 (logged): no Court NPC speaks past D5 or about Primal strains.
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH, required = false)
	public static void courtPointersPastTheGarden(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 4, 4));
		HarbingerVoyagerEntity voyager = PlaythroughNpcs.voyager(helper, PlaythroughSites.stand(helper, 10, 4));
		List<CourtMemberEntity> garden = List.of(member(helper, CourtRole.GARDENER, 16, 4),
				member(helper, CourtRole.ARBOR, 22, 4), member(helper, CourtRole.LIGHTKEEPER, 28, 4));
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 10));
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		PlaythroughStates.courtKnown(p, CourtProgress.Route.VOYAGER); // ARRANGE
		PlaythroughStates.courtStanding(p, CourtHouse.GIVING_LIGHT, Standing.GUEST); // ARRANGE: arrived at the Garden
		p.give(new ItemStack(ItemInit.relay_chart.get())); // ARRANGE
		Map<String, List<String>> heard = new LinkedHashMap<>();
		List<Entity> speakers = new ArrayList<>(List.of(vicar, voyager));
		speakers.addAll(garden);
		for (Entity speaker : speakers) {
			p.approach(speaker);
			DialogueView view = p.talkTo(speaker);
			heard.put(PlaythroughPlayer.describe(speaker), english(view.lineKeys()));
		}
		ItemStack chart = new ItemStack(ItemInit.relay_chart.get());
		List<Component> tooltip = new ArrayList<>();
		chart.getItem().appendHoverText(chart, Item.TooltipContext.of(helper.getLevel()), tooltip, TooltipFlag.NORMAL);
		heard.put("relay chart tooltip", tooltip.stream().map(Component::getString).toList());
		List<String> all = heard.values().stream().flatMap(List::stream).toList();
		boolean sanctum = all.stream().anyMatch(line -> line.contains("Vent Sanctum"));
		boolean nursery = all.stream().anyMatch(line -> line.toLowerCase(Locale.ROOT).contains("nursery"));
		PlaythroughLog.note(helper, "on the way past the Garden: Vent Sanctum named " + sanctum + ", nursery named " + nursery
				+ " (BC-5); heard " + heard);

		boolean pelagic = true;
		try {
			pelagic = HemoCommonConfig.ENABLE_PELAGIC_WORLDGEN.get();
		} catch (RuntimeException exception) {
			PlaythroughLog.note(helper, "the Pelagic worldgen config could not be read: " + exception);
		}
		PlaythroughLog.note(helper, "DECISION BC-9: Pelagic worldgen is " + (pelagic ? "on" : "off")
				+ " here; the Vicar's and Voyager's Court topics do not read it (shown: "
				+ heard.keySet().stream().limit(2).toList() + ")");

		// Its default Fane heart (home, four east) stays inside the template and clear of the members.
		PlaythroughPlayer late = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 20, 18));
		PlaythroughStates.illuminatusD5(late); // ARRANGE
		PlaythroughStates.courtStanding(late, CourtHouse.GIVING_LIGHT, Standing.GUEST); // ARRANGE
		CourtMemberEntity gardener = garden.getFirst();
		late.approach(gardener);
		List<String> atFive = late.talkTo(gardener).lineKeys();
		PlaythroughStates.apotheosD8(late); // ARRANGE: the late path is its own band
		wear(late, strain(MorphlingStrains.SUNBURST, 5)); // ARRANGE: a Primal strain
		List<String> atEight = late.talkTo(gardener).lineKeys();
		List<String> added = atEight.stream().filter(key -> !atFive.contains(key)).toList();
		PlaythroughLog.note(helper, "DECISION BC-10: the Gardener at D8 Apotheos with a Primal strain adds " + added
				+ " to its D5 lines");
		unwear(late);

		helper.assertTrue(sanctum, "something on the way places the Vent Sanctum before the player reaches it: " + heard);
		helper.succeed();
	}

	// =================================================================================================================
	// The presences
	// =================================================================================================================

	/**
	 * Coming near the Head-tender the first time calls Hydra's largest head once; coming near again calls nothing.
	 * Touching Marimo's Heart gives one of the chorus's lines.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void hydraPresenceOnce(GameTestHelper helper) {
		clearCourtActors(helper);
		CourtMemberEntity tender = member(helper, CourtRole.HEAD_TENDER, 10, 10);
		BlockPos crown = helper.absolutePos(new BlockPos(10, PlaythroughSites.STAND_Y + 4, 20));
		PlaythroughRites.set(helper, crown, BlockInit.hydra_crown.get()); // ARRANGE: the threshold chimney
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 16));
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		p.approach(tender);
		p.player().tickCount = 0;
		Outcome first = tickOnce(p).said(HYDRA_CALL);
		int heads = heads(helper, crown);
		helper.assertTrue(heads == 1, "the largest head rises once: " + heads + "; " + first);
		helper.assertTrue(CourtLife.hasSeenPresence(p.player(), CourtPresences.HYDRA), "the first sight is recorded");
		tickOnce(p).didNotSay(HYDRA_CALL);
		helper.assertTrue(heads(helper, crown) == 1, "and not again");

		BlockPos heart = PlaythroughSites.stand(helper, 24, 12);
		PlaythroughRites.set(helper, heart, BlockInit.marimo_heart.get()); // ARRANGE: Marimo's Heart in the undercroft
		p.moveTo(heart.west(2));
		PlaythroughRites.emptyHand(p);
		Outcome touch = p.useOn(heart);
		boolean chorus = touch.saidKeys().stream().anyMatch(key -> key.startsWith(MARIMO_CHORUS));
		PlaythroughLog.note(helper, "touching Marimo's Heart: " + touch.describe());
		helper.assertTrue(chorus, "the anchored speak a chorus line for Marimo; " + touch);
		helper.succeed();
	}

	/**
	 * Marimo notices a living player within reach of his Heart once. His sight finds players through the entity
	 * lookup ({@code CourtPresences.livingNear}), so the player enters the world ({@code enterWorld}).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void marimoFirstSight(GameTestHelper helper) {
		BlockPos heart = PlaythroughSites.stand(helper, 16, 12);
		PlaythroughRites.set(helper, heart, BlockInit.marimo_heart.get()); // ARRANGE
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, heart.west(3));
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		p.enterWorld();
		helper.assertTrue(visibleToEntityLookup(p), "the player is in the level's entity lookup");
		int mark = p.mark();
		int[] seen = new int[1];
		PlaythroughClock.start(helper)
				.until(60, () -> CourtLife.hasSeenPresence(p.player(), CourtPresences.MARIMO), "Marimo notices the player")
				.then(() -> {
					helper.assertTrue(p.chatSince(mark).stream().anyMatch(line -> line.has("hemomancy.court.presence.marimo.first")),
							"his first line is said: " + p.chatSince(mark));
					seen[0] = p.mark();
				})
				.idle(25)
				.then(() -> helper.assertTrue(p.chatSince(seen[0]).stream()
						.noneMatch(line -> line.has("hemomancy.court.presence.marimo.first")), "and only once"))
				.run();
	}

	// =================================================================================================================
	// Every role, every standing
	// =================================================================================================================

	/** Who the player is to the Court in the sweep. */
	private enum Sweep { STRANGER, KNOWN, GUEST, MEMBER, TRANSGRESSOR }

	/**
	 * All 18 roles, each talked to by a stranger, a player who found the Court, a guest of every house, a member of
	 * the role's own house and a Giving Light transgressor, at Degree 4 and Degree 5 (a D4 member cannot exist). Every
	 * tree shows only translated keys, has no dead end or hidden root option, and offers only Court events its role
	 * gives and the route takes; before D5 no role teaches or takes anyone in; the transgressor greeting appears only
	 * in the Giving Light.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void courtMemberSweep(GameTestHelper helper) {
		Map<CourtRole, CourtMemberEntity> members = new EnumMap<>(CourtRole.class);
		int index = 0;
		for (CourtRole role : CourtRole.values()) {
			int x = 2 + 4 * (index % 7);
			int z = 3 + 8 * (index / 7);
			members.put(role, member(helper, role, x, z));
			index++;
		}
		Set<String> owned = new java.util.HashSet<>(CourtMemberDialogue.EVENTS);
		owned.add(CourtMemberDialogue.CHALLENGE);
		List<String> findings = new ArrayList<>();
		int talks = 0;
		for (int degree : List.of(4, 5)) {
			for (Sweep state : Sweep.values()) {
				if (state == Sweep.MEMBER && degree < 5) {
					PlaythroughLog.note(helper, "sweep: no D4 member (joining needs Degree 5)");
					continue;
				}
				List<CourtHouse> houses = new ArrayList<>();
				if (state == Sweep.MEMBER) houses.addAll(List.of(CourtHouse.values()));
				else houses.add(null);
				for (CourtHouse house : houses) {
					PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 16, 7));
					arrange(p, degree, state, house);
					String label = "D" + degree + " " + state + (house == null ? "" : " of " + house);
					for (CourtMemberEntity member : members.values()) {
						CourtRole role = member.role();
						if (house != null && role.house() != house) continue;
						p.approach(member);
						Optional<DialogueView> opened = p.tryTalkTo(member);
						talks++;
						if (opened.isEmpty()) {
							findings.add(label + " " + role + ": opened no dialogue");
							continue;
						}
						DialogueView view = opened.get();
						String at = label + " " + role + ": ";
						view.missingLangKeys().forEach(key -> findings.add(at + "missing " + key));
						view.deadEnds().forEach(end -> findings.add(at + "dead end " + end));
						view.rootOptionsWithoutCard().forEach(hidden -> findings.add(at + "no card for " + hidden));
						for (String event : view.reachableEvents()) {
							if (!owned.contains(event)) findings.add(at + "offers " + event + ", which no Court route takes");
							else if (!offeredByRole(role, event)) findings.add(at + "offers " + event + " the role does not give");
							if (!DialogueEventSpeakers.allows(event, member)) findings.add(at + "offers " + event + " it may not grant");
							if (degree < 5 && (event.equals(CourtMemberDialogue.LESSON) || event.equals(CourtMemberDialogue.JOIN))) {
								findings.add(at + "offers " + event + " below Degree 5");
							}
						}
						List<String> greeting = view.node(view.tree().startNodeId()).map(DialogueNode::lines).orElse(List.of())
								.stream().map(line -> Dialogue.lineKey(line).orElse("")).toList();
						String transgressor = "hemomancy.court." + role.id() + ".greet.transgressor";
						boolean turned = greeting.contains(transgressor);
						if (state == Sweep.TRANSGRESSOR && role.house() == CourtHouse.GIVING_LIGHT && !turned) {
							findings.add(at + "a transgressor is not greeted as one: " + greeting);
						}
						if (role.house() != CourtHouse.GIVING_LIGHT && turned) findings.add(at + "greets as a transgressor outside the Garden");
						if (turned) {
							for (String event : List.of(CourtMemberDialogue.FOOD, CourtMemberDialogue.REST, CourtMemberDialogue.GIFT,
									CourtMemberDialogue.OFFER)) {
								if (view.offers(event)) findings.add(at + "offers " + event + " while turning the player away");
							}
						}
						if (role == CourtRole.SLEEPER && !view.reachableEvents().isEmpty()) {
							findings.add(at + "the Sleeper offers " + view.reachableEvents());
						}
						if (!view.unreachableNodes().isEmpty()) {
							PlaythroughLog.note(helper, at + "unreachable nodes " + view.unreachableNodes());
						}
					}
					p.offline();
				}
			}
		}
		PlaythroughLog.note(helper, "court member sweep: " + talks + " talks, " + findings.size() + " findings"
				+ (findings.isEmpty() ? "" : ": " + findings));
		helper.assertTrue(findings.isEmpty(), "every Court role reads and routes in every standing: " + findings);
		helper.succeed();
	}

	/**
	 * A Seat's two unique leads, the Lightkeeper (lesson, joining the Giving Light) and the Head-tender (joining the
	 * Kindling, the Champion's challenge), each lost two ways: removed with {@code /kill}-style void damage, and struck
	 * down by a Creative player. A game day later (ARRANGE: the time skipped) the marks are looked at and a D5 guest
	 * talks to whatever lead stands there. A player would expect each lead back, so the lesson, joining and the
	 * challenge stay reachable at the Seat.
	 */
	// DECISION: D-10 (late instance; not re-decided here). CourtVacancies refills a mark a day after a /kill or void
	// loss, but a member a Creative player strikes down leaves no vacancy, which permanently closes the lesson, joining
	// and the Champion challenge at that Seat (and a Champion /killed mid-fight never returns its elder). Options as
	// D-10: a respawn rule for unique NPCs / another member takes the lead's role / keep.
	@GameTest(template = EMPTY, timeoutTicks = 600, batch = "playthrough_brined_court_lead_loss", required = false)
	public static void courtLeadLostForGood(GameTestHelper helper) {
		clearCourtActors(helper);
		Map<String, CourtMemberEntity> leads = new LinkedHashMap<>();
		leads.put("Lightkeeper (void)", PlaythroughNpcs.court(helper, CourtRole.LIGHTKEEPER, PlaythroughSites.stand(helper, 4, 6)));
		leads.put("Head-tender (void)", PlaythroughNpcs.court(helper, CourtRole.HEAD_TENDER, PlaythroughSites.stand(helper, 4, 18)));
		leads.put("Lightkeeper (Creative blow)", PlaythroughNpcs.court(helper, CourtRole.LIGHTKEEPER, PlaythroughSites.stand(helper, 26, 6)));
		leads.put("Head-tender (Creative blow)", PlaythroughNpcs.court(helper, CourtRole.HEAD_TENDER, PlaythroughSites.stand(helper, 26, 18)));
		Map<String, BlockPos> homes = new LinkedHashMap<>();
		Map<String, CourtRole> roles = new LinkedHashMap<>();
		leads.forEach((label, member) -> {
			homes.put(label, member.home() != null ? member.home() : member.blockPosition());
			roles.put(label, member.role());
		});
		PlaythroughPlayer creative = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 16, 12));
		creative.creative();
		creative.give(new ItemStack(Items.NETHERITE_SWORD));
		creative.wield(Items.NETHERITE_SWORD);
		int vacanciesBefore = CourtVacancies.get(helper.getLevel()).vacancies().size();
		leads.forEach((label, member) -> {
			if (label.contains("void")) {
				PlaythroughNpcs.kill(member); // the /kill path: void damage with no attacker
			} else {
				creative.approach(member);
				for (int swing = 0; swing < 40 && member.isAlive(); swing++) {
					member.invulnerableTime = 0;
					creative.attack(member);
				}
			}
			PlaythroughLog.note(helper, "courtLeadLostForGood", label + " lost: alive " + member.isAlive());
		});
		int opened = CourtVacancies.get(helper.getLevel()).vacancies().size() - vacanciesBefore;
		PlaythroughPlayer guest = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 16, 12));
		PlaythroughStates.illuminatusD5(guest); // ARRANGE
		PlaythroughStates.courtKnown(guest, CourtProgress.Route.VOYAGER); // ARRANGE
		PlaythroughStates.courtStanding(guest, CourtHouse.GIVING_LIGHT, Standing.FRIEND); // ARRANGE
		PlaythroughStates.courtStanding(guest, CourtHouse.KINDLING, Standing.FRIEND); // ARRANGE
		PlaythroughClock.start(helper)
				.then(() -> PlaythroughTime.skipGameTime(helper, CourtVacancies.REFILL_TICKS + 2000L)) // ARRANGE: a day passes
				.idle(2 * CourtVacancies.CHECK_INTERVAL + 40)
				.then(() -> {
					List<String> gone = new ArrayList<>();
					homes.forEach((label, home) -> {
						CourtRole role = roles.get(label);
						List<CourtMemberEntity> there = helper.getLevel().getEntitiesOfClass(CourtMemberEntity.class,
								new AABB(home).inflate(4.0D), member -> member.isAlive() && member.role() == role);
						String reachable = "nothing";
						if (!there.isEmpty()) {
							CourtMemberEntity lead = there.getFirst();
							PlaythroughCleanup.onEnd(helper, lead::discard);
							Optional<DialogueView> view = guest.approach(lead).tryTalkTo(lead);
							reachable = view.map(v -> List.of(CourtMemberDialogue.LESSON, CourtMemberDialogue.JOIN,
									CourtMemberDialogue.CHALLENGE).stream().filter(v::offers).toList().toString()).orElse("no dialogue");
						} else {
							gone.add(label);
						}
						PlaythroughLog.note(helper, "courtLeadLostForGood", label + " a day later: " + there.size()
								+ " at the mark; offers " + reachable);
					});
					ServerLevel level = helper.getLevel();
					PlaythroughLog.note(helper, "courtLeadLostForGood", "vacancies opened by the four losses: " + opened
							+ "; still open " + CourtVacancies.get(level).vacancies() + " at game time " + level.getGameTime()
							+ "; nearest player to each home " + homes.values().stream().map(home -> {
								var near = level.getNearestPlayer(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D,
										CourtVacancies.CLEAR_RADIUS, false);
								return near == null ? "none" : near.getName().getString();
							}).toList());
					helper.assertTrue(gone.isEmpty(), "DECISION D-10 (late): every Seat lead is back a day after its loss; still"
							+ " gone: " + gone);
				})
				.run();
	}

	// =================================================================================================================
	// The Primal step
	// =================================================================================================================

	/**
	 * An Apex strain left in Morphic Nectar beside a D8 Apotheos player turns Primal; beside a D8 Silentium player it
	 * does not, nor does a Mature strain beside the Apotheos player.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = BATCH)
	public static void primalizeOnlyForApotheos(GameTestHelper helper) {
		PlaythroughPlayer apotheos = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 8));
		// MorphicNectarEvents.findEligiblePrimalizer finds players through the entity lookup: both enter the world.
		apotheos.enterWorld();
		helper.assertTrue(visibleToEntityLookup(apotheos), "the player is in the level's entity lookup");
		PlaythroughPlayer silent = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 26, 8));
		silent.enterWorld();
		PlaythroughStates.apotheosD8(apotheos); // ARRANGE
		PlaythroughStates.silentiumD8(silent); // ARRANGE
		BlockPos apexPool = PlaythroughSites.stand(helper, 4, 4);
		BlockPos maturePool = PlaythroughSites.stand(helper, 4, 12);
		BlockPos silentPool = PlaythroughSites.stand(helper, 26, 4);
		for (BlockPos pool : List.of(apexPool, maturePool, silentPool)) {
			// ARRANGE: a column of nectar three deep in a glass well with a lid, so the fluid does not flow away and the
			// strain stays in it however it bobs.
			for (int dy = 0; dy <= 1; dy++) {
				for (Direction side : Direction.Plane.HORIZONTAL) PlaythroughRites.set(helper, pool.above(dy).relative(side), Blocks.GLASS);
			}
			PlaythroughRites.set(helper, pool.above(2), Blocks.GLASS);
			PlaythroughSites.morphicNectarPool(helper, BoundingBox.fromCorners(pool.below(), pool.above()));
		}
		clearCourtActors(helper);
		drop(helper, apexPool, strain(MorphlingStrains.SUNBURST, APEX)); // the strains are dropped into the pools
		drop(helper, maturePool, strain(MorphlingStrains.SUNBURST, MATURE));
		drop(helper, silentPool, strain(MorphlingStrains.SUNBURST, APEX));
		PlaythroughClock.start(helper)
				.idle(230)
				.then(() -> {
					boolean apex = primalNear(helper, apexPool);
					boolean mature = primalNear(helper, maturePool);
					boolean hushed = primalNear(helper, silentPool);
					PlaythroughLog.note(helper, "Morphic Nectar: Apex by Apotheos " + apex + ", Mature by Apotheos " + mature
							+ ", Apex by Silentium " + hushed + "; strains " + helper.getLevel().getEntitiesOfClass(ItemEntity.class,
							new AABB(apexPool).inflate(12.0D)).stream().map(drop -> drop.getItem().getItem() + "@" + drop.blockPosition().toShortString()
									+ " fluid " + helper.getLevel().getFluidState(drop.blockPosition()).getType()
									+ " apex " + (MorphlingItem.getMaturityLevel(drop.getItem())) + " primal " + MorphlingItem.isPrimal(drop.getItem())
									+ " ticks " + drop.getPersistentData()).toList()
							+ "; the Apotheos is found by the lookup " + visibleToEntityLookup(apotheos) + " path "
							+ HemoCapabilityAccess.getInitiatoryDegree(apotheos.player()).getArchonPath());
					helper.assertTrue(apex, "an Apex strain turns Primal beside a D8 Apotheos");
					helper.assertFalse(mature, "a Mature strain does not");
					helper.assertFalse(hushed, "nor an Apex strain beside a D8 Silentium");
				})
				.run();
	}

	// =================================================================================================================
	// Helpers
	// =================================================================================================================

	/** A Court member on its mark at ({@code x}, {@code z}), held there (ARRANGE: so it stays within talking reach). */
	private static CourtMemberEntity member(GameTestHelper helper, CourtRole role, int x, int z) {
		CourtMemberEntity member = PlaythroughNpcs.court(helper, role, PlaythroughSites.stand(helper, x, z));
		member.setNoAi(true);
		return member;
	}

	/** A sweep player's standing with the Court, arranged on the band's degree state. */
	private static void arrange(PlaythroughPlayer p, int degree, Sweep state, CourtHouse house) {
		if (degree >= 5) PlaythroughStates.illuminatusD5(p); // ARRANGE
		else PlaythroughStates.adeptD4(p); // ARRANGE
		switch (state) {
			case STRANGER -> { }
			case KNOWN -> PlaythroughStates.courtKnown(p, CourtProgress.Route.FOUND); // ARRANGE
			case GUEST, MEMBER, TRANSGRESSOR -> {
				PlaythroughStates.courtKnown(p, CourtProgress.Route.VOYAGER); // ARRANGE
				for (CourtHouse each : CourtHouse.values()) PlaythroughStates.courtStanding(p, each, Standing.GUEST); // ARRANGE
				if (state == Sweep.MEMBER) {
					p.helper().assertTrue(CourtLife.tryJoin(p.player(), house).changed(), "ARRANGE: a member of " + house);
				}
				if (state == Sweep.TRANSGRESSOR) CourtLife.recordTransgression(p.player(), BloomWardenRules.GROWTH); // ARRANGE
			}
		}
	}

	/** Whether {@code role} gives what {@code event} asks for (the Head-tender alone takes the challenge). */
	private static boolean offeredByRole(CourtRole role, String event) {
		return switch (event) {
			case CourtMemberDialogue.FOOD -> role.offers(CourtRole.Offer.FOOD);
			case CourtMemberDialogue.REST -> role.offers(CourtRole.Offer.REST);
			case CourtMemberDialogue.GIFT -> role.offers(CourtRole.Offer.GIFT);
			case CourtMemberDialogue.OFFER -> role.offers(CourtRole.Offer.CUSTOM);
			case CourtMemberDialogue.AMENDS -> role.offers(CourtRole.Offer.FOOD) || role.offers(CourtRole.Offer.LESSON);
			case CourtMemberDialogue.LESSON -> role.offers(CourtRole.Offer.LESSON);
			case CourtMemberDialogue.JOIN -> role.offers(CourtRole.Offer.JOIN);
			case CourtMemberDialogue.CHALLENGE -> role == CourtRole.HEAD_TENDER;
			default -> false;
		};
	}

	/** The Head-tender's tree shows {@code node} (a gate's refusal) and offers no challenge. */
	private static void gateNode(GameTestHelper helper, PlaythroughPlayer p, CourtMemberEntity tender, String node) {
		DialogueView view = p.talkTo(tender);
		view.assertLangKeys(helper, "the Head-tender at " + node);
		helper.assertTrue(view.hasNode(node) && !view.offers(CourtMemberDialogue.CHALLENGE),
				"the Head-tender shows " + node + ": " + view.nodes().keySet());
	}

	/** ARRANGE: the Court known, an Apex Kinbud carried and friendship with the Kindling. */
	private static void openChampionGate(PlaythroughPlayer p) {
		PlaythroughStates.courtKnown(p, CourtProgress.Route.VOYAGER);
		p.give(strain(MorphlingStrains.KINBUD, APEX));
		PlaythroughStates.courtStanding(p, CourtHouse.KINDLING, Standing.FRIEND);
	}

	/** The Champion facing {@code p} in the test's ground, if any. */
	private static KindledChampionEntity facing(GameTestHelper helper, PlaythroughPlayer p) {
		return helper.getLevel().getEntitiesOfClass(KindledChampionEntity.class, helper.getBounds().inflate(8.0D),
				champion -> champion.isFacing(p.player())).stream().findFirst().orElse(null);
	}

	/** The Bloom Wardens hunting {@code p}. */
	private static List<BloomWardenEntity> hunting(GameTestHelper helper, PlaythroughPlayer p) {
		return helper.getLevel().getEntitiesOfClass(BloomWardenEntity.class, helper.getBounds().inflate(16.0D),
				warden -> warden.isHunting(p.player()));
	}

	private static int heads(GameTestHelper helper, BlockPos crown) {
		return helper.getLevel().getEntitiesOfClass(HydraRisingHead.class, new AABB(crown).inflate(4.0D)).size();
	}

	/**
	 * ARRANGE: a blow the Champion yields to at once, as an attack-damage modifier worth four times its health: the
	 * fight's length and balance are not under test (the harness's attack already applies a held weapon's own damage;
	 * this is more than any weapon gives). Taken off at the end.
	 */
	private static void heavyBlow(GameTestHelper helper, PlaythroughPlayer p) {
		ResourceLocation id = Hemomancy.rloc("playthrough_heavy_blow");
		ServerPlayer player = p.player();
		var attribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
		helper.assertTrue(attribute != null, "the player has an attack damage attribute");
		attribute.addOrReplacePermanentModifier(new AttributeModifier(id, KindledChampionRules.MAX_HEALTH * 4.0D,
				AttributeModifier.Operation.ADD_VALUE));
		PlaythroughCleanup.onEnd(helper, () -> {
			var held = p.player().getAttribute(Attributes.ATTACK_DAMAGE);
			if (held != null) held.removeModifier(id);
		});
	}

	/**
	 * Swings at {@code votary} until it dies (ARRANGE between swings: its hurt cooldown is waited out) and returns where
	 * it fell.
	 */
	private static BlockPos killWithFists(PlaythroughPlayer p, BrinedVotaryEntity votary) {
		p.approach(votary);
		BlockPos at = votary.blockPosition();
		for (int swing = 0; swing < 80 && votary.isAlive(); swing++) {
			at = votary.blockPosition();
			votary.invulnerableTime = 0;
			p.attack(votary);
		}
		p.helper().assertTrue(!votary.isAlive(), "the Brined Votary falls to the player");
		return at;
	}

	/** One player tick, as an outcome. */
	private static Outcome tickOnce(PlaythroughPlayer p) {
		Outcome.Probe probe = Outcome.begin(p, null, "tick");
		p.tickOnce();
		return probe.finish();
	}

	/** Two player ticks (the discovery and visit checks may run in either order on the first), as one outcome. */
	private static Outcome tickTwice(PlaythroughPlayer p) {
		Outcome.Probe probe = Outcome.begin(p, null, "tick x2");
		p.tickOnce();
		p.tickOnce();
		return probe.finish();
	}

	/**
	 * One real upkeep tick from empty bonding (ARRANGE: bonding reset and blood full), returning the bonding blood it
	 * credited to the worn strain.
	 */
	private static double bond(PlaythroughPlayer p) {
		ServerPlayer player = p.player();
		ItemStack worn = HemoCapabilityAccess.getEquippedMorphling(player).getEquippedMorphling();
		MorphlingItem.resetBondingProgress(worn, MorphlingItem.getMaturityLevel(worn));
		var blood = HemoCapabilityAccess.getBloodVolume(player);
		blood.setBloodVolume(blood.getMaxBloodVolume());
		player.tickCount = HemoServerConfig.MORPHLING_DRAIN_INTERVAL.get();
		p.tickOnce();
		player.tickCount = 0;
		return MorphlingItem.getBondingBlood(HemoCapabilityAccess.getEquippedMorphling(player).getEquippedMorphling());
	}

	/** A house strain at {@code stage} (0 Unfed .. 4 Apex, 5 Primal), as {@code MorphlingPowerGameTests.stack} builds it. */
	private static ItemStack strain(MorphlingStrain strain, int stage) {
		ItemStack stack = new ItemStack(strain.item().get());
		CompoundTag tag = new CompoundTag();
		tag.putFloat("EnzymePower", POWER[Math.min(stage, 4)]);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		if (stage >= 5) MorphlingItem.setPrimalized(stack);
		return stack;
	}

	/** ARRANGE: {@code stack} worn as the equipped Morphling. */
	private static void wear(PlaythroughPlayer p, ItemStack stack) {
		HemoCapabilityAccess.getEquippedMorphling(p.player()).setEquippedMorphling(stack);
	}

	private static void unwear(PlaythroughPlayer p) {
		HemoCapabilityAccess.getEquippedMorphling(p.player()).clearMorphling();
	}

	/** ARRANGE: puts {@code strain} into the jar's first slot. */
	private static boolean putInJar(ItemStack jar, ItemStack strain) {
		if (!(jar.getCapability(Capabilities.ItemHandler.ITEM) instanceof MorphlingJarItemHandler handler)) return false;
		handler.load();
		handler.setStackInSlot(0, strain);
		handler.save();
		return true;
	}

	/** ARRANGE: {@code stack} as a drop resting in the pool at {@code pos}. */
	private static void drop(GameTestHelper helper, BlockPos pos, ItemStack stack) {
		ItemEntity entity = new ItemEntity(helper.getLevel(), pos.getX() + 0.5D, pos.getY() + 0.2D, pos.getZ() + 0.5D, stack);
		entity.setDeltaMovement(Vec3.ZERO);
		entity.setPickUpDelay(32767);
		helper.getLevel().addFreshEntity(entity);
	}

	private static boolean primalNear(GameTestHelper helper, BlockPos pos) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.0D),
				drop -> drop.getItem().getItem() instanceof MorphlingItem && MorphlingItem.isPrimal(drop.getItem())).size() > 0;
	}

	/** Whether {@code getEntitiesOfClass(ServerPlayer)} round the player finds them (only after {@code enterWorld}). */
	private static boolean visibleToEntityLookup(PlaythroughPlayer p) {
		ServerPlayer player = p.player();
		return !player.serverLevel().getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(2.0D),
				other -> other.getUUID().equals(p.uuid())).isEmpty();
	}

	/**
	 * ARRANGE: the Court's own spawns in the test's ground (Wardens, Champions, elders a Champion returns as, Hydra's
	 * head, drops) are removed when the test ends, as the harness removes the members it placed.
	 */
	private static void clearCourtActors(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		AABB area = helper.getBounds().inflate(6.0D);
		PlaythroughCleanup.onEnd(helper, () -> {
			for (Entity entity : level.getEntities((Entity) null, area, entity -> entity instanceof CourtMemberEntity
					|| entity instanceof HydraRisingHead || entity instanceof ItemEntity)) {
				entity.discard();
			}
		});
	}

	private static int slotOf(PlaythroughPlayer p, ItemLike item) {
		var inventory = p.player().getInventory();
		for (int slot = 0; slot < inventory.items.size(); slot++) if (inventory.items.get(slot).is(item.asItem())) return slot;
		p.helper().fail("the player carries no " + item.asItem());
		return -1;
	}

	private static void removeAll(PlaythroughPlayer p, ItemLike item) {
		var inventory = p.player().getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (inventory.getItem(slot).is(item.asItem())) inventory.setItem(slot, ItemStack.EMPTY);
		}
	}

	/** The English each line key shows ({@code @translate:} arguments dropped), for the log. */
	private static List<String> english(List<String> lines) {
		List<String> text = new ArrayList<>();
		for (String line : lines) Dialogue.lineKey(line).ifPresent(key -> text.add(english(key)));
		return text;
	}

	/** The shipped English for {@code key}, or the key itself when it has none. */
	private static synchronized String english(String key) {
		if (english == null) {
			Map<String, String> loaded = new HashMap<>();
			try (InputStream in = PlaythroughBrinedCourtGameTests.class.getResourceAsStream(LANG)) {
				if (in != null) {
					JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
					for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
						if (entry.getValue().isJsonPrimitive()) loaded.put(entry.getKey(), entry.getValue().getAsString());
					}
				}
			} catch (Exception exception) {
				PlaythroughLog.note(null, "lang", "could not read " + LANG + ": " + exception);
			}
			english = loaded;
		}
		return english.getOrDefault(key, key);
	}
}
