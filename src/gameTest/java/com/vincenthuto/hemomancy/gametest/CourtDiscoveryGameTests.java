package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtDiscoveryProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtKnowledge;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtProgress;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CourtDiscoveryDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueEvent;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.RelayChartItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncCourtProgress;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSeatSearch;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Brined Court Track 5: the "Court known" state and its routes. Each route alone, each ordered pair (the second only
 * acknowledges the first), the Votary letter's once-per-unknowing-player drop, structure discovery by a living player
 * only, the Relay Chart's reissue, its off-thread search, and the owner sync the strain text reads.
 */
@GameTestHolder("court_discovery_validation")
@PrefixGameTestTemplate(false)
public final class CourtDiscoveryGameTests {
	private static final String EMPTY = "empty";
	private static final BlockPos STAND = new BlockPos(4, 2, 4);

	private CourtDiscoveryGameTests() {
	}

	// --- Each route alone ----------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void eachRouteAloneMakesTheCourtKnown(GameTestHelper h) {
		for (CourtProgress.Route route : CourtProgress.Route.values()) {
			try (CapturedPlayer captured = player(h, 5)) {
				h.assertTrue(arrive(h, captured.player, route), route + " records the Court");
				CourtProgress progress = CourtDiscoveryProgress.progress(captured.player);
				h.assertTrue(progress.known() && progress.knownBy() == route, route + " is the first route: " + progress.knownBy());
				h.assertTrue(announcements(captured) == 1, route + " announces once: " + captured.said());
			}
		}
		h.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void eachPairKeepsTheFirstRouteInBothOrders(GameTestHelper h) {
		for (CourtProgress.Route first : CourtProgress.Route.values()) {
			for (CourtProgress.Route second : CourtProgress.Route.values()) {
				if (first == second) continue;
				try (CapturedPlayer captured = player(h, 5)) {
					h.assertTrue(arrive(h, captured.player, first), first + " records first");
					arrive(h, captured.player, second);
					h.assertTrue(CourtDiscoveryProgress.knownBy(captured.player) == first,
							first + " then " + second + " keeps " + first + ", got " + CourtDiscoveryProgress.knownBy(captured.player));
					h.assertTrue(announcements(captured) == 1, first + " then " + second + " announces once: " + captured.said());
				}
			}
		}
		h.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void routesRefuseTheirGates(GameTestHelper h) {
		try (CapturedPlayer captured = player(h, 4)) {
			Entity voyager = spawn(h, EntityInit.harbinger_voyager.get());
			h.assertFalse(post(captured.player, CourtDiscoveryDialogue.VOYAGER_LEARN, voyager),
					"the Voyager's explanation waits for the fifth Degree");
			Entity alchemist = spawn(h, EntityInit.harbinger_alchemist.get());
			captured.player.getInventory().add(MorphlingPowerGameTests.stack(MorphlingStrains.SUNBURST, 4));
			h.assertFalse(post(captured.player, CourtDiscoveryDialogue.ALCHEMIST_LEAD, alchemist),
					"a Mature strain doesn't skip the fifth Degree");
			Entity vicar = spawn(h, EntityInit.harbinger_vicar.get());
			h.assertFalse(post(captured.player, CourtDiscoveryDialogue.VICAR_LETTER, vicar), "no letter, nothing to read");
			h.assertFalse(CourtDiscoveryProgress.isKnown(captured.player), "no route recorded");
			discard(voyager, alchemist, vicar);
		}
		try (CapturedPlayer captured = player(h, 5)) {
			Entity alchemist = spawn(h, EntityInit.harbinger_alchemist.get());
			captured.player.getInventory().add(MorphlingPowerGameTests.stack(MorphlingStrains.SUNBURST, 2));
			h.assertFalse(post(captured.player, CourtDiscoveryDialogue.ALCHEMIST_LEAD, alchemist),
					"a Developing strain isn't enough");
			captured.player.getInventory().add(new ItemStack(ItemInit.brine_sealed_letter.get()));
			h.assertFalse(post(captured.player, CourtDiscoveryDialogue.VICAR_LETTER, alchemist),
					"the Vicar's letter event needs the Vicar");
			h.assertFalse(CourtDiscoveryProgress.isKnown(captured.player), "no route recorded");
			discard(alchemist);
		}
		h.succeed();
	}

	// --- The Votary letter ---------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theLetterDropsOnceForAnUnknowingKiller(GameTestHelper h) {
		try (CapturedPlayer captured = player(h, 1)) {
			h.assertTrue(killVotary(h, captured.player) == 1, "the first Votary carries the letter");
			h.assertTrue(CourtDiscoveryProgress.progress(captured.player).letterDropped(), "the drop is recorded, not the pickup");
			h.assertTrue(killVotary(h, captured.player) == 0, "never a second letter");
		}
		try (CapturedPlayer captured = player(h, 1)) {
			CourtDiscoveryProgress.markKnown(captured.player, CourtProgress.Route.FOUND);
			h.assertTrue(killVotary(h, captured.player) == 0, "no letter for a player who knows the Court");
			h.assertFalse(CourtDiscoveryProgress.progress(captured.player).letterDropped(), "and none is recorded");
		}
		LivingEntity votary = votary(h);
		votary.hurt(h.getLevel().damageSources().generic(), 1000.0F);
		h.assertTrue(letters(h).isEmpty(), "no letter without a player killer");
		h.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aLostLetterCanStillBeExplained(GameTestHelper h) {
		try (CapturedPlayer captured = player(h, 1)) {
			h.assertTrue(killVotary(h, captured.player) == 1, "the letter drops");
			letters(h).forEach(Entity::discard);
			h.assertTrue(CourtDiscoveryProgress.letterLost(captured.player), "dropped, never read, not carried");
			Entity voyager = spawn(h, EntityInit.harbinger_voyager.get());
			h.assertTrue(post(captured.player, CourtDiscoveryDialogue.VOYAGER_LETTER, voyager), "a Voyager hears it out");
			h.assertTrue(CourtDiscoveryProgress.knownBy(captured.player) == CourtProgress.Route.LETTER, "the letter is the route");
			h.assertTrue(charts(h).size() == 1, "and the Voyager offers the Relay Chart");
			charts(h).forEach(Entity::discard);
			discard(voyager);
		}
		h.succeed();
	}

	// --- Structure discovery -------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void onlyALivingPlayerDiscoversTheCourt(GameTestHelper h) {
		try (CapturedPlayer captured = player(h, 0)) {
			ServerPlayer player = captured.player;
			h.assertTrue(CourtDiscoveryProgress.mayDiscover(player), "a living player can find the Court before the fifth Degree");
			player.setGameMode(GameType.SPECTATOR);
			h.assertFalse(CourtDiscoveryProgress.mayDiscover(player), "a spectator can't");
			player.setGameMode(GameType.SURVIVAL);
			player.setHealth(0.0F);
			h.assertFalse(CourtDiscoveryProgress.mayDiscover(player), "a dead player can't");
			player.setHealth(player.getMaxHealth());
			h.assertFalse(CourtDiscoveryProgress.insideCourt(player), "the test floor is no Court");
			player.tickCount = 0;
			CourtDiscoveryProgress.onPlayerTick(new PlayerTickEvent.Post(player));
			h.assertFalse(CourtDiscoveryProgress.isKnown(player), "outside the Court, the tick records nothing");
			CourtDiscoveryProgress.markKnown(player, CourtProgress.Route.FOUND);
			h.assertFalse(CourtDiscoveryProgress.mayDiscover(player), "once known, the tick stops looking");
		}
		h.succeed();
	}

	// --- The Relay Chart -----------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void voyagersReissueTheChart(GameTestHelper h) {
		try (CapturedPlayer captured = player(h, 2)) {
			ServerPlayer player = captured.player;
			Entity voyager = spawn(h, EntityInit.harbinger_voyager.get());
			h.assertFalse(post(player, CourtDiscoveryDialogue.VOYAGER_CHART, voyager), "no chart before the Court is known");
			CourtDiscoveryProgress.markKnown(player, CourtProgress.Route.FOUND);
			h.assertTrue(post(player, CourtDiscoveryDialogue.VOYAGER_CHART, voyager), "known: a chart is drawn");
			h.assertTrue(CourtDiscoveryProgress.progress(player).chartIssued(), "the issue is recorded");
			List<ItemEntity> drawn = charts(h);
			h.assertTrue(drawn.size() == 1, "one chart at the Voyager");
			player.getInventory().add(drawn.getFirst().getItem().copy());
			drawn.forEach(Entity::discard);
			h.assertFalse(post(player, CourtDiscoveryDialogue.VOYAGER_CHART, voyager), "not while carrying one");
			h.assertTrue(charts(h).isEmpty(), "no second chart");
			player.getInventory().clearContent();
			h.assertTrue(post(player, CourtDiscoveryDialogue.VOYAGER_CHART, voyager), "a lost chart is reissued");
			h.assertTrue(charts(h).size() == 1, "the reissued chart");
			charts(h).forEach(Entity::discard);
			discard(voyager);
		}
		h.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void theChartSearchRunsOffTheServerThread(GameTestHelper h) {
		CapturedPlayer captured = player(h, 5);
		ServerPlayer player = captured.player;
		ItemStack chart = new ItemStack(ItemInit.relay_chart.get());
		player.setItemInHand(InteractionHand.MAIN_HAND, chart);
		CourtSeatSearch.clearSessionState();
		chart.use(h.getLevel(), player, InteractionHand.MAIN_HAND);
		// On the flat test floor the probe answers at once, so the hand-back may already have run; the proof is the
		// thread the search itself ran on.
		Thread server = h.getLevel().getServer().getRunningThread();
		h.succeedWhen(() -> {
			h.assertFalse(CourtSeatSearch.pending(player.getUUID()), "the search finished");
			Thread searched = CourtSeatSearch.lastSearchThread();
			h.assertTrue(searched != null && searched != server, "the search ran off the server thread: " + searched);
			h.assertTrue(RelayChartItem.foundNothing(chart), "no Lighthouse Garden near the test floor");
			List<Component> tooltip = new ArrayList<>();
			chart.getItem().appendHoverText(chart, Item.TooltipContext.of(h.getLevel()), tooltip, TooltipFlag.NORMAL);
			String none = Component.translatable("item.hemomancy.relay_chart.tooltip.none").getString();
			h.assertTrue(tooltip.stream().anyMatch(line -> line.getString().equals(none)), "the tooltip says so");
			captured.close();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void theLetterTooltipSaysWhoToShow(GameTestHelper h) {
		ItemStack letter = new ItemStack(ItemInit.brine_sealed_letter.get());
		List<Component> tooltip = new ArrayList<>();
		letter.getItem().appendHoverText(letter, Item.TooltipContext.of(h.getLevel()), tooltip, TooltipFlag.NORMAL);
		for (String key : List.of("item.hemomancy.brine_sealed_letter.tooltip", "item.hemomancy.brine_sealed_letter.tooltip.use")) {
			String line = Component.translatable(key).getString();
			h.assertTrue(!line.equals(key) && tooltip.stream().anyMatch(shown -> shown.getString().equals(line)),
					"the letter's tooltip shows " + key);
		}
		h.succeed();
	}

	// --- Owner sync ----------------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void knowingTheCourtSyncsToTheOwner(GameTestHelper h) {
		try (CapturedPlayer captured = player(h, 5)) {
			h.assertFalse(CourtKnowledge.isKnown(captured.player), "the strain house line starts hidden");
			CourtDiscoveryProgress.markKnown(captured.player, CourtProgress.Route.ALCHEMIST);
			h.assertTrue(CourtKnowledge.isKnown(captured.player), "a known player sees the strain house line");
			List<PacketSyncCourtProgress> sent = captured.sent(PacketSyncCourtProgress.class);
			h.assertTrue(!sent.isEmpty() && sent.getLast().data().getBoolean("Known")
					&& "alchemist".equals(sent.getLast().data().getString("KnownBy")), "the owner receives the known flag");
			CourtProgress mirror = new CourtProgress();
			mirror.deserializeNBT(captured.player.registryAccess(), sent.getLast().data());
			h.assertTrue(mirror.known() && mirror.knownBy() == CourtProgress.Route.ALCHEMIST, "the client copy decodes");
		}
		h.succeed();
	}

	// --- Helpers -------------------------------------------------------------------------------------------------

	private static CapturedPlayer player(GameTestHelper h, int degree) {
		CapturedPlayer captured = CapturedPlayer.create(h).online();
		ServerPlayer player = captured.player;
		BlockPos stand = h.absolutePos(STAND);
		player.moveTo(stand.getX() + 0.5D, stand.getY(), stand.getZ() + 0.5D);
		HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(degree);
		var blood = HemoCapabilityAccess.getBloodVolume(player);
		blood.setMaxBloodVolume(5000.0D);
		blood.setBloodVolume(5000.0D);
		blood.setActive(degree > 0);
		return captured;
	}

	/** Takes one route the way a player would; true when it recorded the Court. */
	private static boolean arrive(GameTestHelper h, ServerPlayer player, CourtProgress.Route route) {
		boolean before = CourtDiscoveryProgress.isKnown(player);
		switch (route) {
		case VOYAGER -> {
			Entity voyager = spawn(h, EntityInit.harbinger_voyager.get());
			post(player, CourtDiscoveryDialogue.VOYAGER_LEARN, voyager);
			discard(voyager);
			charts(h).forEach(Entity::discard);
		}
		case LETTER -> {
			player.getInventory().add(new ItemStack(ItemInit.brine_sealed_letter.get()));
			Entity vicar = spawn(h, EntityInit.harbinger_vicar.get());
			post(player, CourtDiscoveryDialogue.VICAR_LETTER, vicar);
			discard(vicar);
		}
		case ALCHEMIST -> {
			player.getInventory().add(MorphlingPowerGameTests.stack(MorphlingStrains.SUNBURST, 3));
			Entity alchemist = spawn(h, EntityInit.harbinger_alchemist.get());
			post(player, CourtDiscoveryDialogue.ALCHEMIST_LEAD, alchemist);
			discard(alchemist);
		}
		case FOUND -> {
			// The discovery tick's own gate, with the structure lookup answered yes.
			if (CourtDiscoveryProgress.mayDiscover(player)) CourtDiscoveryProgress.markKnown(player, CourtProgress.Route.FOUND);
		}
		}
		return !before && CourtDiscoveryProgress.isKnown(player);
	}

	private static boolean post(ServerPlayer player, String eventId, Entity speaker) {
		DialogueEvent event = new DialogueEvent(player, eventId, speaker.getId());
		NeoForge.EVENT_BUS.post(event);
		return event.wasRewardDelivered();
	}

	private static int announcements(CapturedPlayer captured) {
		String known = Component.translatable("hemomancy.court.known").getString();
		String found = Component.translatable("hemomancy.court.discovery").getString();
		return (int) captured.chat.stream().filter(line -> line.equals(known) || line.equals(found)).count();
	}

	private static Entity spawn(GameTestHelper h, EntityType<?> type) {
		Entity entity = type.create(h.getLevel());
		if (entity == null) throw new IllegalStateException("Could not create " + type);
		entity.moveTo(h.absoluteVec(new Vec3(STAND.getX() + 1.5D, STAND.getY(), STAND.getZ() + 0.5D)));
		h.getLevel().addFreshEntity(entity);
		return entity;
	}

	private static LivingEntity votary(GameTestHelper h) {
		return (LivingEntity) spawn(h, EntityInit.brined_votary.get());
	}

	/** Kills a fresh Votary as {@code player} and returns how many letters it dropped. */
	private static int killVotary(GameTestHelper h, ServerPlayer player) {
		LivingEntity votary = votary(h);
		votary.hurt(player.damageSources().playerAttack(player), 1000.0F);
		h.assertTrue(votary.isDeadOrDying(), "the Votary dies");
		List<ItemEntity> dropped = letters(h);
		int count = dropped.size();
		dropped.forEach(Entity::discard);
		h.getLevel().getEntitiesOfClass(ItemEntity.class, area(h)).forEach(Entity::discard);
		return count;
	}

	private static List<ItemEntity> letters(GameTestHelper h) {
		return dropped(h, () -> ItemInit.brine_sealed_letter.get());
	}

	private static List<ItemEntity> charts(GameTestHelper h) {
		return dropped(h, () -> ItemInit.relay_chart.get());
	}

	private static List<ItemEntity> dropped(GameTestHelper h, Supplier<Item> item) {
		return h.getLevel().getEntitiesOfClass(ItemEntity.class, area(h), drop -> drop.getItem().is(item.get()));
	}

	private static AABB area(GameTestHelper h) {
		return new AABB(h.absolutePos(STAND)).inflate(6.0D);
	}

	private static void discard(Entity... entities) {
		for (Entity entity : entities) entity.discard();
	}
}
