package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.command.CoreLoopFixtures;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperRules;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillOrigin;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillPhase;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Core-loop Track 11, the closing track: the rows of its gate and tangle tables that no earlier track pinned. The
 * ninth-pome power lock holds a Sculpting as it holds a cast and spends no Temper; a Will taken whole by Absorption fills
 * its school's Temper; and the core loop's {@code /hemo test} fixtures set up, verify and clear.
 */
@GameTestHolder("core_loop_validation")
@PrefixGameTestTemplate(false)
public final class CoreLoopClosingGameTests {
	private static final String EMPTY = "empty";
	private static final String BATCH = "core_loop";

	private CoreLoopClosingGameTests() {
	}

	/** Active blood, the two verbs, {@code manipulation} selected and its alignment met, every school carried. */
	private static void prepare(ServerPlayer player, String manipulation, double blood) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(Math.max(volume.getMaxBloodVolume(), blood));
		volume.setBloodVolume(blood);
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		BloodManipulation registered = ManipulationInit.getByName(manipulation);
		known.getKnownManips().put(registered, new ManipLevel(1, 0));
		known.setEquippedManipNames(new ArrayList<>(List.of(manipulation)));
		known.setSelectedManip(registered);
		var temperament = HemoCapabilityAccess.getBloodTemperament(player);
		if (temperament.getAlignmentByTemper(registered.getTend()) < registered.getAlignLevel()) {
			temperament.setTemperAlignment(registered.getTend(), (float) registered.getAlignLevel() + 1.0F);
		}
		TemperFixtures.carry(player);
	}

	private static void clear(GameTestHelper helper, int x0, int x1, int z0, int z1) {
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
				for (int y = 1; y <= 4; y++) helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
			}
		}
	}

	private static void aim(ServerPlayer player, Vec3 feet, Vec3 at) {
		player.setPos(feet.x, feet.y, feet.z);
		player.lookAt(EntityAnchorArgument.Anchor.EYES, at);
		player.setOldPosAndRot();
		player.yHeadRotO = player.getYHeadRot();
	}

	private static double blood(ServerPlayer player) {
		return HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
	}

	private static double project(ServerPlayer player) {
		return BloodProjectionItem.projectFromEntity(player.level(), player, 5.0D, 100.0D);
	}

	// ── The D8 power lock ──

	/**
	 * The gate table's D8 row: the lock after the ninth pome must cover Sculptings and Temper. A Pyretic Forge Sculpting
	 * held on sand by a player whose ninth-pome choice is unresolved pays nothing, burns no Temper, fires nothing and
	 * earns no first; resolved (no pomes), the same hold pays its pulse and earns "Shaped".
	 */
	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 40)
	public static void theNinthPomeLockHoldsASculptingAndSpendsNoTemper(GameTestHelper helper) {
		clear(helper, 1, 6, 2, 3);
		BlockPos sand = helper.absolutePos(new BlockPos(4, 1, 2));
		helper.getLevel().setBlock(sand, Blocks.SAND.defaultBlockState(), 3);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "pyretic_forge", 2_000.0D);
		var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
		degree.setArchonPath(EnumArchonPath.NONE);
		degree.syncTotalPomesConsumed(9);
		aim(player, Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 1, 2))), Vec3.atCenterOf(sand));
		float flammeus = HemoCapabilityAccess.getBloodTemper(player).get(EnumBloodTemper.FLAMMEUS);
		project(player);
		helper.assertTrue(blood(player) == 2_000.0D, "a locked Sculpting paid " + (2_000.0D - blood(player)) + " mL");
		helper.assertTrue(HemoCapabilityAccess.getBloodTemper(player).get(EnumBloodTemper.FLAMMEUS) == flammeus,
				"a locked Sculpting burned Temper");
		helper.assertTrue(helper.getLevel().getBlockState(sand).is(Blocks.SAND), "a locked Sculpting worked the sand");
		helper.assertTrue(!HarbingerAdvancementGranter.hasAdvancement(player, HarbingerAdvancementGranter.ADV_SCULPTED),
				"a refused pulse granted Shaped");
		helper.assertTrue(captured.said().contains("no longer use this power"), "the lock did not say why: " + captured.said());
		// A refused pulse waits a cadence like any other; resolve the choice and hold again once it has passed.
		helper.runAtTickTime(25, () -> {
			try {
				degree.syncTotalPomesConsumed(0);
				aim(player, Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 1, 2))), Vec3.atCenterOf(sand));
				double projected = project(player);
				helper.assertTrue(blood(player) < 2_000.0D, "with the choice resolved the Sculpting still paid nothing: projection "
						+ projected + ", aim hit " + player.pick(6.0D, 0.0F, false).getType() + ", sand still "
						+ helper.getLevel().getBlockState(sand).getBlock() + ", said " + captured.said());
				helper.assertTrue(HemoCapabilityAccess.getBloodTemper(player).get(EnumBloodTemper.FLAMMEUS) < flammeus,
						"a paid pulse burned no Temper");
				helper.assertTrue(HarbingerAdvancementGranter.hasAdvancement(player, HarbingerAdvancementGranter.ADV_SCULPTED),
						"the first paid pulse did not grant Shaped");
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	// ── Wills and Temper ──

	/** The tangle table's Will row: taking a Will whole by Absorption moves Temperament and fills its school's Temper. */
	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 40)
	public static void aWillTakenWholeFillsItsSchoolsTemper(GameTestHelper helper) {
		clear(helper, 1, 6, 1, 4);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
			volume.setActive(true);
			volume.setBloodVolume(500.0D);
			WillEntity will = helper.spawn(EntityInit.will.get(), new BlockPos(3, 1, 2));
			will.configure(WillOrigin.BROKEN, EnumBloodTemper.LUX, 2, null, false);
			will.setPhase(WillPhase.FALTERING);
			var temper = HemoCapabilityAccess.getBloodTemper(player);
			helper.assertTrue(temper.get(EnumBloodTemper.LUX) == 0.0F, "the fixture player already carries Lux");
			float alignment = HemoCapabilityAccess.getBloodTemperament(player).getAlignmentByTemper(EnumBloodTemper.LUX);
			for (int pull = 0; pull < 200 && will.isAlive() && !will.isRemoved(); pull++) {
				will.absorbWithBlood(player, 1_000.0F);
			}
			helper.assertTrue(will.isRemoved(), "the Will was never taken whole");
			helper.assertTrue(HemoCapabilityAccess.getBloodTemperament(player).getAlignmentByTemper(EnumBloodTemper.LUX)
					== alignment + 3.0F, "taking the Will did not move Lux alignment by +3");
			helper.assertTrue(Math.abs(temper.get(EnumBloodTemper.LUX) - TemperRules.fromWill(2)) < 1.0E-4F,
					"a tier 2 Lux Will should leave " + TemperRules.fromWill(2) + " Lux Temper, left "
							+ temper.get(EnumBloodTemper.LUX));
			helper.assertTrue(temper.get(EnumBloodTemper.ANIMUS) == 0.0F, "the Will filled a school it does not carry");
		}
		helper.succeed();
	}

	// ── The Liber pages the core loop wrote ──

	/**
	 * Before this track the core loop's 24 Liber pages were never registered, so {@code MemoBookFilter} hid them from
	 * everyone. Now every one is registered, the second Degree opens the ground floor (the two verbs, living blood,
	 * Temper), a later Degree's page stays shut, and the core-loop first that marks it (Kept) opens it early.
	 */
	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 20)
	public static void theCoreLoopPagesOpenByDegreeAndByTheirFirsts(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			helper.assertTrue(LiberEntryDefinitions
					.CORE_LOOP_PAGES.stream().allMatch(page -> LiberEntryDefinitions.get(page).isPresent()), "a core-loop page is not registered");
			helper.assertTrue(!LiberKnowledgeHelper
					.hasEntry(player, LiberEntryDefinitions.TEMPER),
					"a fresh player already knows the Temper page");
			LiberKnowledgeHelper.unlockForDegree(player, 2);
			for (var page : List.of(
					LiberEntryDefinitions.THE_TWO_VERBS,
					LiberEntryDefinitions.LIVING_BLOOD,
					LiberEntryDefinitions.TEMPER)) {
				helper.assertTrue(LiberKnowledgeHelper
						.hasEntry(player, page), "the second Degree did not open " + page);
			}
			helper.assertTrue(!LiberKnowledgeHelper
					.hasEntry(player, LiberEntryDefinitions.MENAGERIE),
					"the second Degree opened the third Degree's menagerie page");
			// A first opens the page about what was just done, ahead of its Degree.
			HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_KEPT);
			helper.assertTrue(LiberKnowledgeHelper.hasEntry(player, LiberEntryDefinitions.MENAGERIE),
					"earning Kept did not open the menagerie page");
			helper.assertTrue(!LiberKnowledgeHelper.hasEntry(player, LiberEntryDefinitions.VENOUS_CLING),
					"earning Kept opened a page it does not teach");
			// Round two P9's firsts do the same: The Ground Rose opens Living Land, and nothing else.
			helper.assertTrue(!LiberKnowledgeHelper.hasEntry(player, LiberEntryDefinitions.LIVING_LAND),
					"the second Degree opened the Living Land page");
			HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_LAND_STRIKE);
			helper.assertTrue(LiberKnowledgeHelper.hasEntry(player, LiberEntryDefinitions.LIVING_LAND),
					"earning The Ground Rose did not open the Living Land page");
			helper.assertTrue(!LiberKnowledgeHelper.hasEntry(player, LiberEntryDefinitions.SECOND_STRANDS),
					"earning The Ground Rose opened a page it does not teach");
		}
		helper.succeed();
	}

	/**
	 * CQ-1 (round two P9): every page of both loaded books is registered, so none is hidden without a reason. The older
	 * pages open by their Degree floor, ahead of it by the item that teaches them (a school's enzyme opens that school's
	 * pages and no other) or by a memory of the school being learned, and Liber Immaculatus's open by purity stage.
	 */
	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 20)
	public static void theOlderPagesAreRegisteredAndOpenByDegreeItemAndMemory(GameTestHelper helper) {
		List<String> unregistered = new ArrayList<>();
		for (String title : List.of("libersanguinium", "liberimmaculatus")) {
			var book = com.vincenthuto.hutoslib.common.data.book.BookPlaceboReloadListener.INSTANCE
					.getBookByTitle(com.vincenthuto.hemomancy.Hemomancy.rloc(title));
			helper.assertTrue(book != null, title + " did not load");
			book.getChapters().forEach(chapter -> chapter.getPages().forEach(page -> {
				if (page.getId() != null && LiberEntryDefinitions.get(page.getId()).isEmpty()) unregistered.add(page.getId().toString());
			}));
		}
		helper.assertTrue(unregistered.isEmpty(), "pages MemoBookFilter hides from everyone: " + unregistered);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			helper.assertTrue(LiberEntryDefinitions.OLDER_PAGES.stream().noneMatch(page -> LiberKnowledgeHelper.hasEntry(player, page)),
					"a fresh player already reads an older page");
			com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberDiscoveryEvents.unlockForItem(player,
					new net.minecraft.world.item.ItemStack(com.vincenthuto.hemomancy.common.init.ItemInit.fervent_enzyme.get()));
			helper.assertTrue(LiberKnowledgeHelper.hasEntry(player, LiberEntryDefinitions.FLAMMEUS_MANIPULATIONS)
					&& LiberKnowledgeHelper.hasEntry(player, LiberEntryDefinitions.FLAMMEUS_TEMPERAMENT),
					"a Fervent Enzyme did not open the Flammeus pages");
			helper.assertTrue(!LiberKnowledgeHelper.hasEntry(player, LiberEntryDefinitions.ANIMUS_MANIPULATIONS),
					"a Fervent Enzyme opened the Animus page");
			LiberKnowledgeHelper.unlockForMemory(player, ManipulationInit.hemorrhage.get());
			helper.assertTrue(LiberKnowledgeHelper.hasEntry(player, LiberEntryDefinitions.MORTEM_MANIPULATIONS)
					&& LiberKnowledgeHelper.hasEntry(player, LiberEntryDefinitions.MANIPULATIONS_OVERVIEW),
					"learning Hemorrhage did not open the Mortem page and the overview");
			helper.assertTrue(!LiberKnowledgeHelper.hasEntry(player, LiberEntryDefinitions.SAINT_MEMORIES),
					"learning a memory opened the Canon Memories page");
			// A learned memory also opens what its memory item would: Pyretic Forge's own page and the brazier's.
			LiberKnowledgeHelper.unlockForMemory(player, ManipulationInit.pyretic_forge.get());
			helper.assertTrue(LiberKnowledgeHelper.hasEntry(player, LiberEntryDefinitions.PYRETIC_FORGE)
					&& LiberKnowledgeHelper.hasEntry(player, LiberEntryDefinitions.BRAZIER_TRANSMUTATION),
					"learning Pyretic Forge did not open its own page and Brazier Transmutation");
			LiberKnowledgeHelper.unlockForDegree(player, 6);
			helper.assertTrue(!LiberKnowledgeHelper.hasEntry(player, LiberEntryDefinitions.SAINT_MEMORIES),
					"the sixth Degree opened the seventh's Canon Memories page");
			LiberKnowledgeHelper.unlockForDegree(player, 8);
			List<net.minecraft.resources.ResourceLocation> shut = LiberEntryDefinitions.OLDER_PAGES.stream()
					.filter(page -> !LiberKnowledgeHelper.hasEntry(player, page)).toList();
			helper.assertTrue(shut.isEmpty(), "the eighth Degree left older pages shut: " + shut);
		}
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			var progress = HemoCapabilityAccess.getUnstainedProgress(player);
			helper.assertTrue(!LiberKnowledgeHelper.unlockForUnstained(player, progress), "a player off the path opened Immaculatus pages");
			progress.setBegunPurification(true);
			progress.setPurity(30.0F);
			LiberKnowledgeHelper.unlockForUnstained(player, progress);
			helper.assertTrue(LiberKnowledgeHelper.hasEntry(player, LiberEntryDefinitions.IMMACULATUS_WHAT_PURITY_GRANTS),
					"the Tainted stage did not open What Purity Grants");
			helper.assertTrue(!LiberKnowledgeHelper.hasEntry(player, LiberEntryDefinitions.IMMACULATUS_STILL_ARTS),
					"the Tainted stage opened Using Still Arts");
			progress.setClarityUnlocked(true);
			progress.setClarity(80.0F);
			LiberKnowledgeHelper.unlockForUnstained(player, progress);
			List<net.minecraft.resources.ResourceLocation> shut = LiberEntryDefinitions.OLDER_IMMACULATUS_PAGES.stream()
					.filter(page -> !LiberKnowledgeHelper.hasEntry(player, page)).toList();
			helper.assertTrue(shut.isEmpty(), "Resolute Clarity left Immaculatus pages shut: " + shut);
		}
		helper.succeed();
	}

	// ── The /hemo test fixtures ──

	/** Stands the fixture player on the test floor facing south, so a fixture lands two blocks ahead inside the test. */
	private static CapturedPlayer fixturePlayer(GameTestHelper helper) {
		clear(helper, 1, 7, 1, 12);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		Vec3 feet = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 1, 2)));
		player.setPos(feet.x, feet.y, feet.z);
		player.setYRot(0.0F);
		player.setYHeadRot(0.0F);
		return captured;
	}

	/** Runs {@code /hemo <command>} as an operator for {@code player}; the command's return code. */
	private static int hemo(GameTestHelper helper, ServerPlayer player, String command) {
		var dispatcher = helper.getLevel().getServer().getCommands().getDispatcher();
		try {
			return dispatcher.execute("hemo " + command, player.createCommandSourceStack().withPermission(2).withSuppressedOutput());
		} catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
			throw new AssertionError("/hemo " + command + " did not parse: " + exception.getMessage(), exception);
		}
	}

	/**
	 * Places a world fixture by its {@code /hemo} branch, checks it with {@code check} after {@code verifyAt} ticks, then
	 * clears it by the same branch and checks its anchor is air again.
	 */
	private static void placeCheckAndClear(GameTestHelper helper, String branch, String place, int verifyAt,
			java.util.function.BiConsumer<ServerPlayer, CoreLoopFixtures.Fixture> check) {
		CapturedPlayer captured = fixturePlayer(helper);
		ServerPlayer player = captured.player;
		HemoCapabilityAccess.getBloodVolume(player).setActive(true);
		int placed = hemo(helper, player, branch + " " + place);
		CoreLoopFixtures.Fixture fixture = CoreLoopFixtures.fixture(player);
		helper.runAtTickTime(verifyAt, () -> {
			try {
				helper.assertTrue(placed == 1, branch + " " + place + ": the command's own check failed");
				helper.assertTrue(fixture != null, branch + " " + place + ": no fixture was remembered");
				check.accept(player, fixture);
				helper.assertTrue(hemo(helper, player, branch + " clear") == 1, branch + " clear: nothing was cleared");
				helper.assertTrue(helper.getLevel().getBlockState(fixture.anchor()).isAir(),
						branch + ": clear left its fixture standing");
				helper.assertTrue(CoreLoopFixtures.fixture(player) == null, branch + ": clear left the fixture remembered");
			} finally {
				CoreLoopFixtures.clear(player);
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 20)
	public static void theTemperFixturesCarryAndRunDry(GameTestHelper helper) {
		for (String state : List.of("carried", "dry")) {
			try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
				ServerPlayer player = captured.player;
				HemoCapabilityAccess.getBloodTemper(player).set(EnumBloodTemper.MORTEM, 7.0F, TemperRules.MAX_CAP);
				helper.assertTrue(hemo(helper, player, "temperpool " + state) == 1, "temperpool " + state + ": its check failed");
				float expected = state.equals("carried") ? TemperRules.MAX_CAP : 0.0F;
				for (EnumBloodTemper school : EnumBloodTemper.values()) {
					helper.assertTrue(HemoCapabilityAccess.getBloodTemper(player).get(school) == expected,
							"temperpool " + state + ": " + school + " holds " + HemoCapabilityAccess.getBloodTemper(player).get(school));
				}
				helper.assertTrue(hemo(helper, player, "temperpool restore") == 1, "temperpool restore: nothing to restore");
				helper.assertTrue(HemoCapabilityAccess.getBloodTemper(player).get(EnumBloodTemper.MORTEM) == 7.0F,
						"temperpool " + state + ": restore did not put the pool back");
				helper.assertTrue(hemo(helper, player, "temperpool restore") == 0, "a second restore found a pool to put back");
			}
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 30)
	public static void theClottedTankFixtureHoldsOnlyClot(GameTestHelper helper) {
		placeCheckAndClear(helper, "tank", "clot", 2, (player, fixture) -> {
			var tank = com.vincenthuto.hemomancy.common.tile.ReservoirBlood.settled(
					helper.getLevel().getBlockEntity(fixture.anchor()));
			helper.assertTrue(tank.getBloodVolume() == 0.0D && tank.clotted() >= CoreLoopFixtures.TANK_FED - 1.0D,
					"clotted tank: fresh " + tank.getBloodVolume() + " mL, clot " + tank.clotted() + " mL");
		});
	}

	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 30)
	public static void theFilledVatFixtureKeepsACow(GameTestHelper helper) {
		placeCheckAndClear(helper, "vat", "fill", 2, (player, fixture) -> {
			if (!(helper.getLevel().getBlockEntity(fixture.anchor())
					instanceof com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVatBlockEntity vat)) {
				throw new net.minecraft.gametest.framework.GameTestAssertException("no Specimen Vat stands at the fixture");
			}
			helper.assertTrue(vat.kept().occupied() && vat.kept().living()
					&& vat.kept().type() == net.minecraft.world.entity.EntityType.COW, "the vat keeps no living cow");
			helper.assertTrue(Math.abs(vat.kept().vigor() - CoreLoopFixtures.VAT_VIGOR) < 1.0D, "Vigor " + vat.kept().vigor());
			helper.assertTrue(vat.buffer().getBloodVolume() > 0.0D, "the vat's buffer is empty");
		});
	}

	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 60)
	public static void theLitRunFixtureLightsEveryCell(GameTestHelper helper) {
		placeCheckAndClear(helper, "gouge", "light", 30, (player, fixture) -> helper.assertTrue(
				CoreLoopFixtures.litCells(helper.getLevel(), fixture.anchor(), fixture.facing()) == CoreLoopFixtures.RUN_LENGTH,
				"not every cell of the run carries a light clock"));
	}
}
