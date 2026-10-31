package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtChampionRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtDiscoveryProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtLessonRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtLife;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtMembershipRules.Outcome;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtStandingRules.ActKind;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtStandingRules.Standing;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphlingEvents;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncCourtProgress;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSite;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Brined Court Track 11: membership (join, the one switch, the refused third change and every refusal), standing (once
 * per act, a visit by the site check, a transgression and amends), the Lightkeeper's lesson (only after it is taught
 * and only inside the strain's own house site, through the real upkeep tick), the Giving Light offering, the Kindled
 * Champion title and retry wait across death, and an Unstained visitor refused by every service.
 */
@GameTestHolder("court_life_validation")
@PrefixGameTestTemplate(false)
public final class CourtLifeGameTests {
	private static final String EMPTY = "empty";
	private static final BlockPos STAND = new BlockPos(2, 2, 2);
	private static final CourtHouse STONE = CourtHouse.HOLDING_STONE;
	private static final CourtHouse FOLD = CourtHouse.VEILED_FOLD;

	private CourtLifeGameTests() {
	}

	// --- Membership ----------------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void joinSwitchThenTheThirdChangeIsRefused(GameTestHelper h) {
		try (CapturedPlayer captured = player(h, 5)) {
			ServerPlayer player = captured.player;
			CourtLife.recordAct(player, STONE, "visit", ActKind.VISIT);
			CourtLife.recordAct(player, STONE, "custom_promise", ActKind.CUSTOM);
			CourtLife.recordAct(player, FOLD, "visit", ActKind.VISIT);
			h.assertTrue(CourtLife.standing(player, STONE) == Standing.FRIEND, "a visit and a custom make a friend");

			h.assertTrue(CourtLife.tryJoin(player, STONE) == Outcome.JOINED, "the first entry is free");
			h.assertTrue(CourtLife.house(player) == STONE && !CourtLife.progress(player).switchUsed(), "joined, switch unspent");
			h.assertTrue(CourtLife.tryJoin(player, STONE) == Outcome.ALREADY_MEMBER, "joining twice changes nothing");

			h.assertTrue(CourtLife.tryJoin(player, FOLD) == Outcome.SWITCHED, "the one free switch");
			h.assertTrue(CourtLife.house(player) == FOLD && CourtLife.progress(player).switchUsed(), "switched and spent");
			h.assertTrue(CourtLife.standing(player, STONE) == Standing.GUEST, "the house left behind drops to guest, never lower");

			h.assertTrue(CourtLife.tryJoin(player, STONE) == Outcome.REFUSED_SWITCH_USED, "a third change is refused");
			h.assertTrue(CourtLife.house(player) == FOLD, "membership is fixed");
			h.assertTrue(captured.chat.contains(Component.translatable(Outcome.REFUSED_SWITCH_USED.translationKey()).getString()),
					"the refusal is a lang line: " + captured.said());
			List<PacketSyncCourtProgress> sent = captured.sent(PacketSyncCourtProgress.class);
			h.assertTrue(!sent.isEmpty() && "veiled_fold".equals(sent.getLast().data().getString("House"))
					&& sent.getLast().data().getBoolean("SwitchUsed"), "the owner receives the house");
		}
		h.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void joiningRefusesBelowTheFifthDegreeAndWithoutGuestStanding(GameTestHelper h) {
		try (CapturedPlayer captured = player(h, 4)) {
			CourtLife.recordAct(captured.player, STONE, "visit", ActKind.VISIT);
			h.assertTrue(CourtLife.tryJoin(captured.player, STONE) == Outcome.REFUSED_DEGREE, "the fifth Degree first");
			h.assertTrue(CourtLife.house(captured.player) == null, "no house");
		}
		try (CapturedPlayer captured = player(h, 5)) {
			h.assertTrue(CourtLife.tryJoin(captured.player, STONE) == Outcome.REFUSED_STANDING, "a stranger can't join");
			h.assertTrue(CourtLife.house(captured.player) == null, "no house");
			h.assertTrue(MorphlingStrains.SUNBURST.house() != STONE, "joining needs no strain of the house");
			CourtLife.recordAct(captured.player, STONE, "visit", ActKind.VISIT);
			h.assertTrue(CourtLife.tryJoin(captured.player, STONE) == Outcome.JOINED, "guest standing is enough, with no strain");
		}
		h.succeed();
	}

	// --- Standing ------------------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void standingChangesOncePerAct(GameTestHelper h) {
		try (CapturedPlayer captured = player(h, 2)) {
			ServerPlayer player = captured.player;
			h.assertTrue(CourtLife.recordAct(player, STONE, "visit", ActKind.VISIT), "the first visit makes a guest");
			h.assertFalse(CourtLife.recordAct(player, STONE, "visit", ActKind.VISIT), "the same visit never counts again");
			h.assertFalse(CourtLife.recordAct(player, STONE, "visit_again", ActKind.VISIT), "a visit raises no higher than guest");
			h.assertTrue(CourtLife.recordAct(player, STONE, "custom_promise", ActKind.CUSTOM), "the house's custom raises a step");
			h.assertFalse(CourtLife.recordAct(player, STONE, "custom_promise", ActKind.CUSTOM), "the same custom counts once");
			h.assertTrue(CourtLife.standing(player, STONE) == Standing.FRIEND, "friend");
			h.assertTrue(CourtLife.standing(player, FOLD) == Standing.STRANGER, "standing is per house");
		}
		h.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theVisitCheckCountsNothingOutsideACourtSite(GameTestHelper h) {
		try (CapturedPlayer captured = player(h, 0)) {
			ServerPlayer player = captured.player;
			CourtDiscoveryProgress.markKnown(player, CourtProgress.Route.FOUND);
			h.assertTrue(CourtLife.houseAt(h.getLevel(), player.blockPosition()) == null, "the test floor is no Court site");
			player.tickCount = 0;
			CourtLife.onPlayerTick(new PlayerTickEvent.Post(player));
			for (CourtHouse house : CourtHouse.values()) {
				h.assertTrue(CourtLife.standing(player, house) == Standing.STRANGER, "nothing counted for " + house);
			}
		}
		h.succeed();
	}

	// --- Transgression and the offering --------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aTransgressionDropsStandingAndAnOfferingMakesAmends(GameTestHelper h) {
		try (CapturedPlayer captured = player(h, 5); CapturedPlayer clone = player(h, 5)) {
			ServerPlayer player = captured.player;
			CourtHouse light = CourtHouse.GIVING_LIGHT;
			CourtLife.recordAct(player, light, "visit", ActKind.VISIT);
			CourtLife.recordAct(player, light, "custom_food", ActKind.CUSTOM);
			h.assertTrue(CourtLife.standing(player, light) == Standing.FRIEND, "a friend of the Garden");

			CourtLife.recordTransgression(player, "broke_growth");
			h.assertTrue(CourtLife.hasTransgression(player) && CourtLife.standing(player, light) == Standing.STRANGER,
					"a wrong drops Giving Light standing");
			h.assertTrue(CourtLife.turnsAway(player, light) && !CourtLife.turnsAway(player, STONE), "only the Garden turns them away");
			h.assertFalse(CourtLife.recordAct(player, light, "custom_more_food", ActKind.CUSTOM), "no standing while turned away");
			h.assertTrue(CourtLife.tryJoin(player, light) == Outcome.REFUSED_TRANSGRESSION, "and no membership");
			h.assertTrue(CourtLife.teachLesson(player) == CourtLessonRules.Outcome.REFUSED_TRANSGRESSION, "and no lesson");

			clone.player.restoreFrom(player, false);
			h.assertTrue(CourtLife.hasTransgression(clone.player)
					&& "broke_growth".equals(CourtLife.progress(clone.player).transgressionAct()), "the record survives death");

			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE, 3));
			h.assertFalse(CourtLife.holdsOffering(player), "stone is no offering");
			h.assertFalse(CourtLife.reconcile(player), "no amends without an offering");
			h.assertTrue(player.getMainHandItem().getCount() == 3, "nothing taken");

			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 3));
			h.assertTrue(CourtLife.holdsOffering(player) && CourtLife.isOffering(new ItemStack(Items.COOKED_COD)),
					"foods are offerings (c:foods)");
			h.assertTrue(CourtLife.reconcile(player), "amends");
			h.assertTrue(player.getMainHandItem().getCount() == 2, "one offering consumed");
			h.assertTrue(!CourtLife.hasTransgression(player) && CourtLife.standing(player, light) == Standing.GUEST,
					"the record clears and standing returns to guest");
			h.assertTrue(CourtLife.progress(player).transgressionCount() == 1, "the count of past wrongs stays");
			h.assertFalse(CourtLife.reconcile(player), "nothing left to reconcile");
			h.assertTrue(player.getMainHandItem().getCount() == 2, "and nothing more taken");
		}
		h.succeed();
	}

	// --- The Champion --------------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theChampionGateTitleAndRetryWaitSurviveDeath(GameTestHelper h) {
		try (CapturedPlayer captured = player(h, 6); CapturedPlayer clone = player(h, 6)) {
			ServerPlayer player = captured.player;
			h.assertTrue(CourtLife.championGate(player) == CourtChampionRules.Gate.REFUSED_UNKNOWN, "the Court first");
			CourtDiscoveryProgress.markKnown(player, CourtProgress.Route.VOYAGER);
			h.assertTrue(CourtLife.championGate(player) == CourtChampionRules.Gate.REFUSED_STRAIN, "an Apex strain next");
			player.getInventory().add(MorphlingPowerGameTests.stack(MorphlingStrains.KINBUD, 4));
			h.assertTrue(CourtLife.championGate(player) == CourtChampionRules.Gate.REFUSED_STANDING, "then the Kindling's friendship");
			CourtLife.recordAct(player, CourtHouse.KINDLING, "visit", ActKind.VISIT);
			CourtLife.recordAct(player, CourtHouse.KINDLING, "custom_vent_offering", ActKind.CUSTOM);
			h.assertTrue(CourtLife.championGate(player).open(), "the usurpation opens");

			CourtLife.recordUsurpLoss(player);
			long now = h.getLevel().getGameTime();
			h.assertTrue(CourtLife.progress(player).usurpRetryAt() == now + CourtChampionRules.USURP_RETRY_TICKS, "a day's wait");
			h.assertTrue(CourtLife.championGate(player) == CourtChampionRules.Gate.REFUSED_WAIT, "no retry yet");
			clone.player.restoreFrom(player, false);
			h.assertTrue(CourtLife.progress(clone.player).usurpRetryAt() == now + CourtChampionRules.USURP_RETRY_TICKS,
					"the wait survives death");
			CourtProgress reloaded = new CourtProgress();
			reloaded.deserializeNBT(player.registryAccess(), CourtLife.progress(player).serializeNBT(player.registryAccess()));
			h.assertTrue(reloaded.usurpRetryAt() == now + CourtChampionRules.USURP_RETRY_TICKS, "and a save");

			CourtLife.grantChampion(player);
			h.assertTrue(CourtLife.isChampion(player) && CourtLife.progress(player).usurpRetryAt() == 0L, "the victor holds the title");
			h.assertTrue(CourtLife.championGate(player) == CourtChampionRules.Gate.REFUSED_CHAMPION, "and can't challenge themself");
			clone.player.restoreFrom(player, false);
			h.assertTrue(CourtLife.isChampion(clone.player), "the title survives death");
			CourtLife.revokeChampion(player);
			h.assertFalse(CourtLife.isChampion(player), "a later challenge can take it back");
		}
		h.succeed();
	}

	// --- Unstained visitors --------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anUnstainedVisitorIsShunnedByEveryService(GameTestHelper h) {
		try (CapturedPlayer captured = player(h, 0)) {
			ServerPlayer player = captured.player;
			HemoCapabilityAccess.getBloodVolume(player).setActive(false);
			HemoCapabilityAccess.getUnstainedProgress(player).setBegunPurification(true);
			HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(6);
			CourtDiscoveryProgress.markKnown(player, CourtProgress.Route.FOUND);
			h.assertTrue(CourtDiscoveryProgress.isKnown(player), "discovery still records the Court");
			h.assertTrue(CourtLife.isShunned(player), "the Unstained path is shunned");
			for (CourtHouse house : CourtHouse.values()) {
				h.assertFalse(CourtLife.recordAct(player, house, "visit", ActKind.VISIT), "no standing with " + house);
				h.assertTrue(CourtLife.tryJoin(player, house) == Outcome.REFUSED_SHUNNED, "no membership with " + house);
			}
			h.assertTrue(CourtLife.teachLesson(player) == CourtLessonRules.Outcome.REFUSED_SHUNNED, "no lesson");
			player.getInventory().add(MorphlingPowerGameTests.stack(MorphlingStrains.KINBUD, 4));
			h.assertTrue(CourtLife.championGate(player) == CourtChampionRules.Gate.REFUSED_SHUNNED, "no challenge");
			h.assertFalse(CourtLife.progress(player).lessonTaught(), "nothing recorded");
		}
		try (CapturedPlayer captured = player(h, 5)) {
			h.assertFalse(CourtLife.isShunned(captured.player), "a Harbinger isn't shunned");
		}
		h.succeed();
	}

	// --- The lesson, inside real site pieces ---------------------------------------------------------------------

	/**
	 * Builds a Holding Stone (Tide Refuge) and then a Veiled Fold (Screened Cleft) piece round the test floor in turn and
	 * runs the real upkeep tick. Its own batch: the pieces sit in the test's chunk.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = "court_life_site")
	public static void theLessonBondsFasterOnlyInsideTheStrainsOwnHouseSite(GameTestHelper h) {
		BlockPos stand = h.absolutePos(STAND);
		try (CapturedPlayer captured = player(h, 5)) {
			ServerPlayer player = captured.player;
			ItemStack holdfast = MorphlingPowerGameTests.stack(MorphlingStrains.HOLDFAST, 1);
			HemoCapabilityAccess.getEquippedMorphling(player).setEquippedMorphling(holdfast);
			ItemStack worn = HemoCapabilityAccess.getEquippedMorphling(player).getEquippedMorphling();

			double outside = bond(player);
			h.assertTrue(outside > 0.0D, "the upkeep tick credits bonding: " + outside);
			h.assertTrue(CourtLife.lessonMultiplier(player, worn) == 1.0D, "no multiplier outside the Court");

			try (FakeSite site = new FakeSite(h.getLevel(), CourtSite.TIDE_REFUGE, stand)) {
				h.assertTrue(CourtLife.houseAt(h.getLevel(), player.blockPosition()) == STONE, "inside the Holding Stone's site");
				h.assertTrue(Math.abs(bond(player) - outside) < 1.0E-6D, "untaught: plain bonding at home");

				h.assertTrue(CourtLife.teachLesson(player).taught(), "the Lightkeeper teaches a fifth-Degree player");
				h.assertTrue(CourtLife.teachLesson(player) == CourtLessonRules.Outcome.ALREADY_TAUGHT, "once");
				double home = bond(player);
				h.assertTrue(Math.abs(home - outside * CourtLessonRules.MULTIPLIER) < 1.0E-6D,
						"taught: x" + CourtLessonRules.MULTIPLIER + " at home, got " + home + " against " + outside);

				h.assertTrue(CourtLife.progress(player).house() == null, "the lesson works without membership");
				player.tickCount = 0;
				CourtLife.onPlayerTick(new PlayerTickEvent.Post(player));
				h.assertTrue(CourtLife.standing(player, STONE) == Standing.STRANGER, "the visit check waits for the Court to be known");
				CourtDiscoveryProgress.markKnown(player, CourtProgress.Route.FOUND);
				CourtLife.onPlayerTick(new PlayerTickEvent.Post(player));
				h.assertTrue(CourtLife.standing(player, STONE) == Standing.GUEST, "standing in the site makes a guest");
			}
			try (FakeSite site = new FakeSite(h.getLevel(), CourtSite.SCREENED_CLEFT, stand)) {
				h.assertTrue(CourtLife.houseAt(h.getLevel(), player.blockPosition()) == FOLD, "inside the Veiled Fold's site");
				h.assertTrue(Math.abs(bond(player) - outside) < 1.0E-6D, "another house's site gives no multiplier");
			}
			h.assertTrue(CourtLife.houseAt(h.getLevel(), player.blockPosition()) == null, "the pieces are gone");
			h.assertTrue(Math.abs(bond(player) - outside) < 1.0E-6D, "outside again: plain bonding");
			HemoCapabilityAccess.getEquippedMorphling(player).clearMorphling();
		}
		h.succeed();
	}

	// --- Helpers -------------------------------------------------------------------------------------------------

	/** Runs one real upkeep tick from empty bonding and returns the blood it credited. */
	private static double bond(ServerPlayer player) {
		ItemStack worn = HemoCapabilityAccess.getEquippedMorphling(player).getEquippedMorphling();
		MorphlingItem.resetBondingProgress(worn, MorphlingItem.getMaturityLevel(worn));
		var blood = HemoCapabilityAccess.getBloodVolume(player);
		blood.setBloodVolume(blood.getMaxBloodVolume());
		player.tickCount = HemoServerConfig.MORPHLING_DRAIN_INTERVAL.get();
		EquippedMorphlingEvents.playerTick(new PlayerTickEvent.Post(player));
		return MorphlingItem.getBondingBlood(HemoCapabilityAccess.getEquippedMorphling(player).getEquippedMorphling());
	}

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

	/**
	 * One piece of a registered Court site's structure round {@code centre}, written into its chunk's starts and
	 * references the way generation would; closing puts the chunk's own structure data back.
	 */
	static final class FakeSite implements AutoCloseable {
		private final LevelChunk chunk;
		private final Map<Structure, StructureStart> starts;
		private final Map<Structure, LongSet> references;

		FakeSite(ServerLevel level, CourtSite site, BlockPos centre) {
			Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(Hemomancy.rloc(site.id()));
			if (structure == null) throw new IllegalStateException("No structure for " + site.id());
			chunk = level.getChunkAt(centre);
			starts = new HashMap<>(chunk.getAllStarts());
			references = new HashMap<>(chunk.getAllReferences());
			StructurePiece piece = new StructurePiece(StructurePieceType.JIGSAW, 0,
					BoundingBox.fromCorners(centre.offset(-2, -1, -2), centre.offset(2, 3, 2))) {
				@Override
				protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
				}

				@Override
				public void postProcess(WorldGenLevel worldGenLevel, StructureManager manager, ChunkGenerator generator,
						RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
				}
			};
			ChunkPos pos = chunk.getPos();
			chunk.setStartForStructure(structure, new StructureStart(structure, pos, 0, new PiecesContainer(List.of(piece))));
			chunk.addReferenceForStructure(structure, pos.toLong());
		}

		@Override
		public void close() {
			chunk.setAllStarts(starts);
			chunk.setAllReferences(references);
		}
	}
}
