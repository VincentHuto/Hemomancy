package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.EnumBloodGourdTiers;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BodyFormationDraw;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.BloodRoutingEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.BloodRoutingHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.BloodRoutingSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.DirectBloodLinkData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.ThrallCourierSource;
import com.vincenthuto.hemomancy.common.entity.summon.BloodThrallEntity;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodyFlaskItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;
import com.vincenthuto.hemomancy.common.tile.CoagulationRules;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.BloodDrawRights;
import com.vincenthuto.hemomancy.common.tile.ReservoirBlood;
import com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.vincenthuto.hemomancy.common.tile.ReservoirReading;
import com.vincenthuto.hemomancy.common.tile.ReservoirReadout;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.EnzymaticScriptoriumBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Core-loop Track 3, living blood: an inert tank keeps blood fresh for five minutes after feeding, then clots; clot
 * comes out as Formations through Absorption; a living vessel and a station held by a rite never clot; a sutured
 * machine drinks from its owner nearby and stalls when the owner is away; only the owner and their bloodline may
 * draw; carried vessels clot too; and every reservoir the mod registers either clots, is living, or keeps no tank.
 * Time is moved by backdating a tank's feeding stamp, since a GameTest cannot wait an hour.
 */
@GameTestHolder("living_blood_validation")
@PrefixGameTestTemplate(false)
public final class LivingBloodGameTests {
	private static final String EMPTY = "bastion/mobs/empty";

	private LivingBloodGameTests() {
	}

	// ── Fixtures ──

	private static EnzymaticScriptoriumBlockEntity scriptorium(GameTestHelper helper, BlockPos relative) {
		helper.setBlock(relative.below(), Blocks.STONE);
		helper.setBlock(relative, BlockInit.enzymatic_scriptorium.get());
		return (EnzymaticScriptoriumBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(relative));
	}

	private static BlockBloodVolume tank(BlockEntity blockEntity) {
		return ReservoirBlood.settled(blockEntity);
	}

	private static void backdate(BlockEntity blockEntity, long ticksAgo) {
		tank(blockEntity).backdate(blockEntity.getLevel().getGameTime(), ticksAgo);
	}

	private static IBloodVolume activeBlood(ServerPlayer player, double amount) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(5_000.0D);
		volume.setBloodVolume(amount);
		return volume;
	}

	/** Both verbs known, nothing selected: the plain verbs. */
	private static void plainVerbs(ServerPlayer player) {
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		known.setSelectedManip(BloodManipulation.BLANK);
	}

	private static void aimAt(GameTestHelper helper, ServerPlayer player, BlockPos standRelative, BlockPos targetRelative) {
		Vec3 feet = Vec3.atBottomCenterOf(helper.absolutePos(standRelative));
		player.setPos(feet.x, feet.y, feet.z);
		player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(helper.absolutePos(targetRelative)));
		player.setOldPosAndRot();
		player.yHeadRotO = player.getYHeadRot();
	}

	private static void absorbTick(ServerPlayer player) {
		if (!player.isUsingItem() || !player.getUseItem().is(ItemInit.blood_absorption.get())) {
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_absorption.get()));
			player.startUsingItem(InteractionHand.MAIN_HAND);
		}
		ItemStack hand = player.getUseItem();
		hand.getItem().onUseTick(player.level(), player, hand, player.getUseItemRemainingTicks());
	}

	private static int count(ServerPlayer player, net.minecraft.world.item.Item item) {
		int total = 0;
		for (ItemStack stack : player.getInventory().items) if (stack.is(item)) total += stack.getCount();
		return total;
	}

	// ── The clock ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aFilledTankStaysFreshThroughTheWindowThenClots(GameTestHelper helper) {
		var station = scriptorium(helper, new BlockPos(2, 1, 2));
		tank(station).fill(500.0D);
		backdate(station, CoagulationRules.FRESH_WINDOW_TICKS);
		helper.assertTrue(Math.abs(tank(station).getBloodVolume() - 500.0D) < 1e-6 && tank(station).clotted() == 0.0D,
				"blood clotted inside the five-minute window");
		backdate(station, CoagulationRules.FRESH_WINDOW_TICKS + CoagulationRules.MINUTE_TICKS);
		double fresh = tank(station).getBloodVolume();
		helper.assertTrue(Math.abs(fresh - 450.0D) < 1e-6 && Math.abs(tank(station).clotted() - 50.0D) < 1e-6,
				"a minute past the window should leave 450 fresh and 50 clot, read " + fresh);
		backdate(station, CoagulationRules.FULL_CLOT_TICKS);
		helper.assertTrue(tank(station).getBloodVolume() == 0.0D && Math.abs(tank(station).clotted() - 500.0D) < 1e-6,
				"an hour after feeding the tank still held fresh blood");
		helper.assertTrue(station.getBloodVolume() == 0.0D, "the station still reads clot as fuel");
		helper.succeed();
	}

	/** NQ-01-3: a top-up no longer makes the whole tank fresh; the tank's age blends by volume. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void projectionBlendsTheAgeOfACleanTank(GameTestHelper helper) {
		var station = scriptorium(helper, new BlockPos(2, 1, 2));
		tank(station).fill(200.0D);
		long aged = CoagulationRules.FRESH_WINDOW_TICKS - 20L;
		backdate(station, aged);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			IBloodVolume blood = activeBlood(player, 1_000.0D);
			double taken = station.takeProjectedBlood(player, station.getBlockPos(), 50.0D, false);
			helper.assertTrue(taken == 50.0D && blood.getBloodVolume() == 950.0D,
					"projection did not fill the station, took " + taken);
			long age = tank(station).age();
			helper.assertTrue(age == Math.round(aged * 200.0D / 250.0D),
					"the tank's age did not blend by volume: " + age);
		}
		helper.succeed();
	}

	/** CQ-6, LP-3, NQ-01-2: while a tank holds clot, half of a projection wakes it, two mL of clot for each mL spent. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void projectionIntoAClottedTankWakesTheClot(GameTestHelper helper) {
		var station = scriptorium(helper, new BlockPos(2, 1, 2));
		tank(station).fill(400.0D);
		backdate(station, CoagulationRules.FULL_CLOT_TICKS);
		tank(station).scrapeClot(300.0D);
		helper.assertTrue(Math.abs(tank(station).clotted() - 100.0D) < 1e-6 && tank(station).getBloodVolume() == 0.0D,
				"the fixture did not leave 100 mL of clot");
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			IBloodVolume blood = activeBlood(player, 1_000.0D);
			double taken = station.takeProjectedBlood(player, station.getBlockPos(), 80.0D, false);
			helper.assertTrue(taken == 80.0D && blood.getBloodVolume() == 920.0D, "the player paid " + taken);
			// 40 mL wakes 80 of clot; 40 goes in fresh.
			helper.assertTrue(Math.abs(tank(station).clotted() - 20.0D) < 1e-6
							&& Math.abs(tank(station).getBloodVolume() - 120.0D) < 1e-6,
					"read " + tank(station).getBloodVolume() + " fresh and " + tank(station).clotted() + " clot");
		}
		helper.succeed();
	}

	/** NQ-01-8: a tank with no room left spends the whole projection on its clot and charges only what it used. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aFullClottedTankRevivesUnderProjection(GameTestHelper helper) {
		var station = scriptorium(helper, new BlockPos(2, 1, 2));
		tank(station).setBloodVolume(station.getMaxBloodVolume());
		double size = tank(station).capacity();
		backdate(station, CoagulationRules.FULL_CLOT_TICKS);
		helper.assertTrue(tank(station).getMaxBloodVolume() == 0.0D, "the fixture tank still has room");
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			IBloodVolume blood = activeBlood(player, 1_000.0D);
			double taken = station.takeProjectedBlood(player, station.getBlockPos(), 50.0D, false);
			helper.assertTrue(taken == 50.0D && blood.getBloodVolume() == 950.0D, "a full clotted tank took " + taken);
			helper.assertTrue(Math.abs(tank(station).getBloodVolume() - 100.0D) < 1e-6
							&& Math.abs(tank(station).clotted() - (size - 100.0D)) < 1e-6,
					"50 mL did not wake 100 of clot: " + tank(station).getBloodVolume());
			helper.assertTrue(ReservoirReadout.lines(helper.getLevel(), station).stream()
							.anyMatch(line -> line.getString().contains("wakes the clot")),
					"the Gauge does not say a pour wakes the clot");
		}
		helper.succeed();
	}

	/** NQ-01-5: a Blood Thrall's load keeps the age of the tank it came from and wakes no clot where it lands. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aCourierDeliveryKeepsTheAgeOfItsSourceTank(GameTestHelper helper) {
		var source = scriptorium(helper, new BlockPos(1, 1, 2));
		var dest = scriptorium(helper, new BlockPos(4, 1, 2));
		tank(source).fill(300.0D);
		long aged = CoagulationRules.FRESH_WINDOW_TICKS - 100L;
		backdate(source, aged);
		tank(dest).addClot(50.0D);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			BloodThrallEntity thrall = new BloodThrallEntity(helper.getLevel(), captured.player,
					source.getBlockPos(), dest.getBlockPos());
			tank(source).drain(100.0D);
			thrall.takeBlood(100.0F, tank(source).age());
			net.minecraft.nbt.CompoundTag saved = new net.minecraft.nbt.CompoundTag();
			thrall.addAdditionalSaveData(saved);
			BloodThrallEntity reloaded = new BloodThrallEntity(helper.getLevel(), captured.player,
					source.getBlockPos(), dest.getBlockPos());
			reloaded.readAdditionalSaveData(saved);
			helper.assertTrue(reloaded.getCarriedAge() == aged, "the thrall lost its load's age: " + reloaded.getCarriedAge());
			ThrallCourierSource courier = new ThrallCourierSource(reloaded.getCarriedBlood(), 100.0D,
					reloaded.getCarriedAge());
			double delivered = courier.transferTo(tank(dest), 100.0D);
			helper.assertTrue(delivered == 100.0D, "the courier delivered " + delivered);
			helper.assertTrue(tank(dest).age() == aged, "the delivery came out younger: " + tank(dest).age());
			helper.assertTrue(Math.abs(tank(dest).clotted() - 50.0D) < 1e-6, "a courier's delivery woke clot");
		}
		helper.succeed();
	}

	// ── Living vessels and rites ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theGourdvineTapNeverClots(GameTestHelper helper) {
		helper.setBlock(new BlockPos(2, 0, 2), Blocks.STONE);
		helper.setBlock(new BlockPos(2, 1, 2), BlockInit.gourdvine_tap.get());
		BlockEntity tap = helper.getLevel().getBlockEntity(helper.absolutePos(new BlockPos(2, 1, 2)));
		helper.assertTrue(tap instanceof IBloodReservoir reservoir && reservoir.isLivingVessel(), "the Tap is not living");
		tank(tap).setMaxBloodVolume(2_000.0D);
		tank(tap).fill(1_500.0D);
		backdate(tap, CoagulationRules.FULL_CLOT_TICKS * 2);
		helper.assertTrue(tank(tap).getBloodVolume() == 1_500.0D && tank(tap).clotted() == 0.0D,
				"the living Tap clotted");
		helper.assertTrue(ReservoirReadout.state(tap) == ReservoirReadout.State.LIVING, "the gauge does not read it as living");
		helper.succeed();
	}

	/** NQ-01-6: a rite-held station's clock stops; its blood comes out as old as it went in, not freshly fed. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aStationHeldByAnUpgradeRiteDoesNotClot(GameTestHelper helper) {
		var station = scriptorium(helper, new BlockPos(2, 1, 2));
		tank(station).fill(300.0D);
		long aged = CoagulationRules.FRESH_WINDOW_TICKS - 20L;
		backdate(station, aged);
		helper.assertTrue(tank(station).age() == aged, "the fixture did not age the tank");
		station.setRiteLocked(true);
		helper.runAfterDelay(20, () -> {
			helper.assertTrue(tank(station).age() == aged, "a rite hold restamped the tank fresh: age " + tank(station).age());
			backdate(station, CoagulationRules.FULL_CLOT_TICKS);
			helper.assertTrue(tank(station).getBloodVolume() == 300.0D, "blood clotted while a rite held the station");
			station.setRiteLocked(false);
			helper.assertTrue(tank(station).getBloodVolume() == 300.0D, "releasing the rite clotted the blood at once");
			helper.succeed();
		});
	}

	// ── Clot out as Formations ──

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void absorptionScrapesClotOutAsFormations(GameTestHelper helper) {
		var station = scriptorium(helper, new BlockPos(3, 1, 2));
		tank(station).fill(600.0D);
		backdate(station, CoagulationRules.FULL_CLOT_TICKS);
		helper.assertTrue(Math.abs(tank(station).clotted() - 600.0D) < 1e-6, "the tank did not clot through");
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			activeBlood(player, 1_000.0D);
			plainVerbs(player);
			aimAt(helper, player, new BlockPos(1, 1, 2), new BlockPos(3, 1, 2));
			for (int i = 0; i < 4; i++) absorbTick(player);
			helper.assertTrue(count(player, ItemInit.sanguine_formation.get()) == 1,
					"600 mL of clot should scrape out as one Formation, got "
							+ count(player, ItemInit.sanguine_formation.get()));
			helper.assertTrue(tank(station).clotted() == 0.0D, "the smear under a Formation was left in the tank");
			helper.assertTrue(HemoCapabilityAccess.getBloodVolume(player).getBloodVolume() == 1_000.0D,
					"clot was drunk as blood");
			helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_SET_THROUGH),
					"scraping clot into a Formation did not grant Set Through (core-loop Track 11)");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void absorptionDrinksFreshBloodBeforeTouchingClot(GameTestHelper helper) {
		var station = scriptorium(helper, new BlockPos(3, 1, 2));
		tank(station).fill(500.0D);
		backdate(station, CoagulationRules.FRESH_WINDOW_TICKS + 3 * CoagulationRules.MINUTE_TICKS);
		double fresh = tank(station).getBloodVolume();
		double clot = tank(station).clotted();
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			IBloodVolume blood = activeBlood(player, 1_000.0D);
			plainVerbs(player);
			aimAt(helper, player, new BlockPos(1, 1, 2), new BlockPos(3, 1, 2));
			absorbTick(player);
			helper.assertTrue(blood.getBloodVolume() > 1_000.0D, "absorption took no fresh blood");
			helper.assertTrue(Math.abs(tank(station).clotted() - clot) < 1e-6, "absorption scraped clot while fresh blood remained");
			helper.assertTrue(tank(station).getBloodVolume() < fresh, "the fresh blood did not go down");
		}
		helper.succeed();
	}

	// ── Ownership (CL-8) ──

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void onlyTheOwnerMayDrawFromAPlacedMachine(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		var station = scriptorium(helper, new BlockPos(3, 1, 2));
		try (CapturedPlayer owner = CapturedPlayer.create(helper); CapturedPlayer stranger = CapturedPlayer.create(helper)) {
			NeoForge.EVENT_BUS.post(new BlockEvent.EntityPlaceEvent(
					BlockSnapshot.create(level.dimension(), level, station.getBlockPos()),
					Blocks.STONE.defaultBlockState(), owner.player));
			helper.assertTrue(owner.player.getUUID().equals(tank(station).owner()), "placing did not record the owner");
			tank(station).fill(400.0D);
			for (CapturedPlayer captured : List.of(stranger, owner)) {
				activeBlood(captured.player, 1_000.0D);
				plainVerbs(captured.player);
				aimAt(helper, captured.player, new BlockPos(1, 1, 2), new BlockPos(3, 1, 2));
				absorbTick(captured.player);
			}
			helper.assertTrue(HemoCapabilityAccess.getBloodVolume(stranger.player).getBloodVolume() == 1_000.0D,
					"a stranger drew from someone else's machine");
			helper.assertTrue(stranger.said().contains("outside your bloodline"), "the stranger was not told why: " + stranger.said());
			helper.assertTrue(HemoCapabilityAccess.getBloodVolume(owner.player).getBloodVolume() > 1_000.0D,
					"the owner could not draw from their own machine");
		}
		helper.succeed();
	}

	/**
	 * A multiblock's filler keeps no tank of its own: every read of its blood reaches the main block's tank, so draw
	 * rights at a filler are the main block's owner's (a stranger may not draw there, the owner may), and a filler with
	 * no main block reads as no tank at all.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aFillerReadsItsMainBlocksTankAndOwner(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos main = helper.absolutePos(new BlockPos(3, 1, 3));
		for (BlockPos cell : BlockPos.betweenClosed(main.offset(-1, 0, -1), main.offset(1, 2, 1))) {
			level.setBlockAndUpdate(cell, Blocks.AIR.defaultBlockState());
		}
		level.setBlockAndUpdate(main.below(), Blocks.STONE.defaultBlockState());
		BlockState alembic = BlockInit.ghastly_alembic.get().defaultBlockState();
		level.setBlockAndUpdate(main, alembic);
		alembic.getBlock().setPlacedBy(level, main, alembic, null, new ItemStack(alembic.getBlock()));
		BlockPos upper = main.above();
		BlockPos orphanPos = helper.absolutePos(new BlockPos(6, 1, 3));
		try (CapturedPlayer owner = CapturedPlayer.create(helper); CapturedPlayer stranger = CapturedPlayer.create(helper)) {
			BlockEntity station = level.getBlockEntity(main);
			helper.assertTrue(station instanceof IBloodReservoir reservoir && reservoir.holdsBlood(), "the Alembic keeps no tank");
			helper.assertTrue(level.getBlockEntity(upper) instanceof FillerBlockEntity, "the Alembic set no filler above itself");
			BlockEntity filler = level.getBlockEntity(upper);
			tank(station).setOwner(owner.player.getUUID());
			tank(station).fill(400.0D);
			helper.assertTrue(ReservoirBlood.tank(filler) == station, "the filler does not resolve to its main block");
			helper.assertTrue(tank(filler) == tank(station) && tank(filler).getBloodVolume() == 400.0D,
					"the filler read its own tank: " + tank(filler).getBloodVolume() + " mL");
			helper.assertTrue(((IBloodReservoir) filler).holdsBlood(), "a filler of a station that holds blood says it holds none");
			helper.assertTrue(!BloodDrawRights.mayDraw(stranger.player, filler), "a stranger may draw from the owner's station through a filler");
			helper.assertTrue(BloodDrawRights.mayDraw(owner.player, filler), "the owner may not draw from their station through a filler");
			level.setBlockAndUpdate(orphanPos, BlockInit.filler_block.get().defaultBlockState());
			BlockEntity orphan = level.getBlockEntity(orphanPos);
			helper.assertTrue(orphan instanceof FillerBlockEntity && !((IBloodReservoir) orphan).holdsBlood(), "an orphan filler holds blood");
			helper.assertTrue(tank(orphan).capacity() == 0.0D && tank(orphan).getBloodVolume() == 0.0D,
					"an orphan filler read a tank of " + tank(orphan).capacity());
		} finally {
			level.setBlockAndUpdate(orphanPos, Blocks.AIR.defaultBlockState());
			level.setBlockAndUpdate(main, Blocks.AIR.defaultBlockState());
		}
		helper.succeed();
	}

	// ── Owner draw through a suture (CL-7) ──

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void aSuturedMachineDrinksFromItsOwnerNearbyAndStallsWhenTheyLeave(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		var station = scriptorium(helper, new BlockPos(2, 1, 2));
		BlockPos pos = station.getBlockPos();
		try (CapturedPlayer captured = CapturedPlayer.create(helper).online()) {
			ServerPlayer owner = captured.player;
			IBloodVolume blood = activeBlood(owner, 5_000.0D);
			HemoCapabilityAccess.getInitiatoryDegree(owner).setDegreeNumber(3);
			Vec3 near = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 1, 2)));
			owner.setPos(near.x, near.y, near.z);
			DirectBloodLinkData link = DirectBloodLinkData.nearby(owner.getUUID());
			BloodRoutingSavedData.get(level).setLink(pos, link);
			try {
				backdate(station, CoagulationRules.FRESH_WINDOW_TICKS - 1L);
				double routed = BloodRoutingHelper.routeLinkedTarget(level, pos, link);
				helper.assertTrue(routed > 0.0D && blood.getBloodVolume() == 5_000.0D - routed,
						"the owner nearby did not feed the machine, routed " + routed);
				helper.assertTrue(tank(station).lastFed() == level.getGameTime(), "owner draw did not stamp the tank fresh");
				helper.assertTrue(ReservoirReadout.feed(level, station) == ReservoirReadout.Feed.DRAWING,
						"the gauge does not read the suture as drawing");
				// The routing pass keeps the suture's state on the tank, for the machine's own screen (round two P11).
				helper.assertTrue(BloodRoutingEvents.keepLinkStatus(level, pos, link)
						&& tank(station).link() == ReservoirReadout.Link.DRAWING, "the tank does not carry its drawing suture");
				helper.assertTrue(!BloodRoutingEvents.keepLinkStatus(level, pos, link),
						"an unchanged suture resent its block");
				helper.assertTrue(ReservoirReading.of(station).link() == ReservoirReadout.Link.DRAWING,
						"the screen's reading does not carry the suture");

				// A full buffer needs nothing and is not restamped for free (LP-4): it ages like any tank.
				// The station's own read sets its 600 mL buffer (a GameTest runs before the block's onLoad).
				tank(station).setBloodVolume(station.getMaxBloodVolume());
				backdate(station, CoagulationRules.FRESH_WINDOW_TICKS - 1L);
				double ownerBefore = blood.getBloodVolume();
				helper.assertTrue(BloodRoutingHelper.routeLinkedTarget(level, pos, link) == 0.0D
						&& blood.getBloodVolume() == ownerBefore, "a full buffer drew from its owner");
				helper.assertTrue(tank(station).age() == CoagulationRules.FRESH_WINDOW_TICKS - 1L,
						"a full sutured buffer was restamped fresh for free");
				// Once it clots, the suture revives the clot from its owner, 4 mL of clot for each mL (NQ-01-7).
				backdate(station, CoagulationRules.FRESH_WINDOW_TICKS + CoagulationRules.MINUTE_TICKS);
				double clot = tank(station).clotted();
				helper.assertTrue(clot > 1.0D, "the idle buffer did not clot");
				ownerBefore = blood.getBloodVolume();
				double revived = BloodRoutingHelper.routeLinkedTarget(level, pos, link);
				helper.assertTrue(Math.abs(revived - clot / 4.0D) < 1e-6
								&& Math.abs(blood.getBloodVolume() - (ownerBefore - revived)) < 1e-6,
						"the suture should spend " + clot / 4.0D + " to wake " + clot + " of clot, spent " + revived);
				helper.assertTrue(tank(station).clotted() == 0.0D
								&& Math.abs(tank(station).getBloodVolume() - station.getMaxBloodVolume()) < 1e-6,
						"the suture did not wake the buffer's clot");

				Vec3 far = near.add(40.0D, 0.0D, 0.0D);
				owner.setPos(far.x, far.y, far.z);
				double before = tank(station).getBloodVolume();
				helper.assertTrue(BloodRoutingHelper.routeLinkedTarget(level, pos, link) == 0.0D
								&& tank(station).getBloodVolume() == before,
						"a machine drew from an owner 40 blocks away");
				helper.assertTrue(ReservoirReadout.feed(level, station) == ReservoirReadout.Feed.STALLED,
						"the gauge does not say the machine has stalled");
				helper.assertTrue(BloodRoutingEvents.keepLinkStatus(level, pos, link)
						&& tank(station).link() == ReservoirReadout.Link.OUT_OF_REACH,
						"the tank does not say its owner is out of reach: " + tank(station).link());
				helper.assertTrue(tank(station).serializeNBT(level.registryAccess()).getByte("Link")
						== ReservoirReadout.Link.OUT_OF_REACH.ordinal(), "the stalled state is not in the tank's synced tag");
				HemoCapabilityAccess.getInitiatoryDegree(owner).setDegreeNumber(2);
				owner.setPos(near.x, near.y, near.z);
				helper.assertTrue(BloodRoutingHelper.linkStatus(level, pos, link) == ReservoirReadout.Link.BELOW_DEGREE,
						"an owner below the third Degree is not the reason given");
				HemoCapabilityAccess.getInitiatoryDegree(owner).setDegreeNumber(3);
				owner.setPos(far.x, far.y, far.z);
				backdate(station, CoagulationRules.FULL_CLOT_TICKS);
				helper.assertTrue(tank(station).getBloodVolume() == 0.0D && tank(station).clotted() > 0.0D,
						"a stalled machine's buffer did not clot");
			} finally {
				BloodRoutingSavedData.get(level).removeLink(pos);
			}
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anOfflineOwnerFeedsNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		var station = scriptorium(helper, new BlockPos(2, 1, 2));
		BlockPos pos = station.getBlockPos();
		DirectBloodLinkData link = DirectBloodLinkData.nearby(UUID.randomUUID());
		BloodRoutingSavedData.get(level).setLink(pos, link);
		try {
			helper.assertTrue(BloodRoutingHelper.routeLinkedTarget(level, pos, link) == 0.0D
					&& tank(station).getBloodVolume() == 0.0D, "an offline owner fed the machine");
			helper.assertTrue(BloodRoutingHelper.linkStatus(level, pos, link) == ReservoirReadout.Link.OWNER_OFFLINE,
					"an offline owner is not the reason given");
		} finally {
			BloodRoutingSavedData.get(level).removeLink(pos);
		}
		helper.succeed();
	}

	/**
	 * CN-9, NQ-01-16: with the owner offline, a member of their bloodline within 16 blocks, at the third Degree and blood
	 * awake, stands in and pays from their own blood above the half floor. A stranger near the machine feeds nothing.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void kinStandsInForAnOfflineOwner(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		var station = scriptorium(helper, new BlockPos(2, 1, 2));
		BlockPos pos = station.getBlockPos();
		UUID ownerId = UUID.randomUUID();
		var lines = com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData
				.get(level.getServer().overworld());
		try (CapturedPlayer kin = CapturedPlayer.create(helper).joinLevel(); CapturedPlayer stranger = CapturedPlayer.create(helper).joinLevel()) {
			var line = new com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline(
					"Stand-in", ownerId, UUID.randomUUID(), new ArrayList<>(List.of(ownerId, kin.player.getUUID())));
			lines.registerBloodline(line);
			DirectBloodLinkData link = DirectBloodLinkData.nearby(ownerId);
			BloodRoutingSavedData.get(level).setLink(pos, link);
			try {
				Vec3 near = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 1, 2)));
				for (CapturedPlayer captured : List.of(kin, stranger)) {
					activeBlood(captured.player, 5_000.0D);
					HemoCapabilityAccess.getInitiatoryDegree(captured.player).setDegreeNumber(3);
				}
				stranger.player.setPos(near.x, near.y, near.z);
				kin.player.setPos(near.x + 100.0D, near.y, near.z);
				helper.assertTrue(BloodRoutingHelper.linkStatus(level, pos, link) == ReservoirReadout.Link.OWNER_OFFLINE
								&& BloodRoutingHelper.routeLinkedTarget(level, pos, link) == 0.0D,
						"a stranger near the machine stood in for its offline owner");

				kin.player.setPos(near.x, near.y, near.z);
				helper.assertTrue(BloodRoutingHelper.linkStatus(level, pos, link) == ReservoirReadout.Link.KIN_DRAWING,
						"kin nearby do not stand in: " + BloodRoutingHelper.linkStatus(level, pos, link));
				double routed = BloodRoutingHelper.routeLinkedTarget(level, pos, link);
				IBloodVolume kinBlood = HemoCapabilityAccess.getBloodVolume(kin.player);
				helper.assertTrue(routed > 0.0D && Math.abs(kinBlood.getBloodVolume() - (5_000.0D - routed)) < 1e-6
								&& HemoCapabilityAccess.getBloodVolume(stranger.player).getBloodVolume() == 5_000.0D,
						"kin did not pay for the machine: routed " + routed + ", kin " + kinBlood.getBloodVolume());
				helper.assertTrue(ReservoirReadout.feed(level, station) == ReservoirReadout.Feed.DRAWING,
						"the gauge does not read the stand-in as drawing");

				kinBlood.setBloodVolume(2_500.0D);
				tank(station).setBloodVolume(0.0D);
				helper.assertTrue(BloodRoutingHelper.routeLinkedTarget(level, pos, link) == 0.0D
						&& kinBlood.getBloodVolume() == 2_500.0D, "a stand-in was drawn below the half floor");

				kinBlood.setBloodVolume(5_000.0D);
				HemoCapabilityAccess.getInitiatoryDegree(kin.player).setDegreeNumber(2);
				helper.assertTrue(BloodRoutingHelper.linkStatus(level, pos, link) == ReservoirReadout.Link.OWNER_OFFLINE,
						"kin below the third Degree stood in");
			} finally {
				BloodRoutingSavedData.get(level).removeLink(pos);
				lines.disbandBloodline(line.getBloodlineUUID());
			}
		}
		helper.succeed();
	}

	// ── Carried blood (CL-6) ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aFlaskClotsAndBreaksOutAsFormations(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			IBloodVolume blood = activeBlood(player, 1_000.0D);
			long now = helper.getLevel().getGameTime();
			ItemStack fresh = new ItemStack(ItemInit.bloody_flask.get());
			BloodyFlaskItem.stampBottled(fresh, now - 100L);
			helper.assertTrue(BloodyFlaskItem.freshShare(fresh, now) == 1.0D, "a just-bottled flask is not fresh");

			ItemStack old = new ItemStack(ItemInit.bloody_flask.get());
			old.set(DataComponentInit.BOTTLED_AT.get(), now - CoagulationRules.FULL_CLOT_TICKS);
			player.setItemInHand(InteractionHand.MAIN_HAND, old);
			old.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
			helper.assertTrue(blood.getBloodVolume() == 1_000.0D, "a clotted flask gave blood");
			helper.assertTrue(count(player, ItemInit.sanguine_formation.get()) == 6,
					"2,500 mL of clot should break out as six Formations, got "
							+ count(player, ItemInit.sanguine_formation.get()));

			ItemStack rock = new ItemStack(ItemInit.blood_rock.get());
			BloodyFlaskItem.stampBottled(rock, now - CoagulationRules.FULL_CLOT_TICKS);
			helper.assertTrue(BloodyFlaskItem.freshShare(rock, now) == 1.0D, "Blood Rock clots like a flask");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aPouredFlaskPutsItsClotInTheTank(GameTestHelper helper) {
		helper.setBlock(new BlockPos(2, 0, 2), Blocks.STONE);
		helper.setBlock(new BlockPos(2, 1, 2), BlockInit.mycelial_crucible.get());
		var station = (IBloodReservoir) helper.getLevel().getBlockEntity(helper.absolutePos(new BlockPos(2, 1, 2)));
		tank((BlockEntity) station).setMaxBloodVolume(8_000.0D);
		long now = helper.getLevel().getGameTime();
		ItemStack flask = new ItemStack(ItemInit.bloody_flask.get());
		flask.set(DataComponentInit.BOTTLED_AT.get(), now - CoagulationRules.FRESH_WINDOW_TICKS - 5 * CoagulationRules.MINUTE_TICKS);
		double share = BloodyFlaskItem.freshShare(flask, now);
		var container = new net.minecraft.world.SimpleContainer(2);
		container.setItem(0, flask);
		// The Scriptorium is not a flask station, so pour through the shared transfer every flask station uses.
		boolean poured = com.vincenthuto.hemomancy.common.tile.BloodContainerTransfer.fillReservoirFromSlot(
				container, station, new com.vincenthuto.hemomancy.common.tile.IBloodContainerSlotAccess() {
					@Override public int getBloodContainerInputSlot() { return 0; }
					@Override public int getEmptyBloodContainerOutputSlot() { return 1; }
				});
		helper.assertTrue(poured, "the flask did not pour");
		BlockBloodVolume volume = tank((BlockEntity) station);
		helper.assertTrue(share > 0.0D && share < 1.0D, "the flask was not part-clotted, share " + share);
		helper.assertTrue(Math.abs(volume.getBloodVolume() - 2_500.0D * share) < 1e-6
						&& Math.abs(volume.clotted() - 2_500.0D * (1.0D - share)) < 1e-6,
				"the flask did not split into fresh blood and clot");
		helper.succeed();
	}

	/** CL-6, NQ-01-9: five Blood Rocks in a row give 250, 200, 150, 100 and 62.5 mL; sober again, a Rock gives all. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void repeatedDrinksGiveLessAndLess(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			IBloodVolume blood = activeBlood(player, 0.0D);
			double[] expected = {250.0D, 200.0D, 150.0D, 100.0D, 62.5D};
			for (double gain : expected) {
				double before = blood.getBloodVolume();
				player.getCooldowns().removeCooldown(ItemInit.blood_rock.get());
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_rock.get()));
				player.getMainHandItem().getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
				helper.assertTrue(Math.abs(blood.getBloodVolume() - before - gain) < 1e-6,
						"a Blood Rock gave " + (blood.getBloodVolume() - before) + " mL, expected " + gain);
			}
			player.removeEffect(com.vincenthuto.hemomancy.common.init.EffectInit.blood_drunkenness);
			player.getCooldowns().removeCooldown(ItemInit.blood_rock.get());
			double before = blood.getBloodVolume();
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_rock.get()));
			player.getMainHandItem().getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
			helper.assertTrue(blood.getBloodVolume() - before == 250.0D, "a sober drinker did not get the whole Rock");
		}
		helper.succeed();
	}

	/** FX-2: chest loot stamps a Flask when it is made, so none sits in a chest ageless. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void lootedFlasksCarryABottledStamp(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		var table = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,
				Hemomancy.rloc("chests/blood_temple")));
		int flasks = 0;
		for (int roll = 0; roll < 64; roll++) {
			var params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN,
					Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 1, 2)))).create(LootContextParamSets.CHEST);
			for (ItemStack stack : table.getRandomItems(params)) {
				if (!stack.is(ItemInit.bloody_flask.get())) continue;
				flasks++;
				helper.assertTrue(stack.get(DataComponentInit.BOTTLED_AT.get()) != null, "a looted flask has no bottled stamp");
			}
		}
		helper.assertTrue(flasks > 0, "64 Blood Temple chests held no flask");
		helper.succeed();
	}

	/** FX-2: an unstamped flask a hopper sets in a machine starts its clock there; the rest of its stack keeps it. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anUnstampedFlaskIsStampedBeforeItPours(GameTestHelper helper) {
		helper.setBlock(new BlockPos(2, 0, 2), Blocks.STONE);
		helper.setBlock(new BlockPos(2, 1, 2), BlockInit.mycelial_crucible.get());
		var station = (IBloodReservoir) helper.getLevel().getBlockEntity(helper.absolutePos(new BlockPos(2, 1, 2)));
		tank((BlockEntity) station).setMaxBloodVolume(8_000.0D);
		long now = helper.getLevel().getGameTime();
		var container = new net.minecraft.world.SimpleContainer(2);
		container.setItem(0, new ItemStack(ItemInit.bloody_flask.get(), 2));
		boolean poured = com.vincenthuto.hemomancy.common.tile.BloodContainerTransfer.fillReservoirFromSlot(
				container, station, new com.vincenthuto.hemomancy.common.tile.IBloodContainerSlotAccess() {
					@Override public int getBloodContainerInputSlot() { return 0; }
					@Override public int getEmptyBloodContainerOutputSlot() { return 1; }
				});
		helper.assertTrue(poured && Math.abs(tank((BlockEntity) station).getBloodVolume() - 2_500.0D) < 1e-6,
				"a just-stamped flask did not pour fresh");
		Long stamp = container.getItem(0).get(DataComponentInit.BOTTLED_AT.get());
		helper.assertTrue(stamp != null && stamp == CoagulationRules.bottledStamp(now),
				"the flask left in the slot was not stamped, to the minute, when the first poured: " + stamp);
		helper.succeed();
	}

	/** FX-2: a mob's jug, rolled through the global loot modifiers on a player kill, carries a stamp to the minute. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void mobDropJugsCarryABottledStamp(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		var thirster = helper.spawn(com.vincenthuto.hemomancy.common.init.EntityInit.thirster.get(), new BlockPos(2, 1, 2));
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			var table = level.getServer().reloadableRegistries().getLootTable(thirster.getLootTable());
			int jugs = 0;
			int flasks = 0;
			for (int roll = 0; roll < 400; roll++) {
				var params = new LootParams.Builder(level)
						.withParameter(LootContextParams.THIS_ENTITY, thirster)
						.withParameter(LootContextParams.ORIGIN, thirster.position())
						.withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().playerAttack(captured.player))
						.withParameter(LootContextParams.ATTACKING_ENTITY, captured.player)
						.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, captured.player)
						.create(LootContextParamSets.ENTITY);
				for (ItemStack stack : table.getRandomItems(params)) {
					boolean jug = stack.is(ItemInit.bloody_jug.get());
					if (!jug && !stack.is(ItemInit.bloody_flask.get())) continue;
					if (jug) jugs++;
					else flasks++;
					Long stamp = stack.get(DataComponentInit.BOTTLED_AT.get());
					helper.assertTrue(stamp != null && stamp == CoagulationRules.bottledStamp(level.getGameTime()),
							"a dropped " + stack.getHoverName().getString() + " has no stamp to the minute: " + stamp);
				}
			}
			helper.assertTrue(jugs > 0 && flasks > 0, "400 Thirster kills dropped " + jugs + " jugs and " + flasks + " flasks");
		} finally {
			thirster.discard();
		}
		helper.succeed();
	}

	/** FX-2: the Alchemist's Body Answers flask is stamped when it is given. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theBodyAnswersFlaskIsStampedWhenGiven(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			com.vincenthuto.hemomancy.common.mission.alchemist.BodyAnswersAssignment.giveBriefingSupplies(captured.player);
			int found = 0;
			for (ItemStack stack : captured.player.getInventory().items) {
				if (!stack.is(ItemInit.bloody_flask.get())) continue;
				found += stack.getCount();
				helper.assertTrue(stack.get(DataComponentInit.BOTTLED_AT.get()) != null, "the Body Answers flask has no stamp");
			}
			helper.assertTrue(found == 1, "the briefing gave " + found + " flasks");
		}
		helper.succeed();
	}

	/** FX-2: an Alembic distils a clotting Flask by its fresh share, so it does not revive clot. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aHalfClottedFlaskDistilsToOneVial(GameTestHelper helper) {
		long now = helper.getLevel().getGameTime();
		ItemStack fresh = new ItemStack(ItemInit.bloody_flask.get());
		BloodyFlaskItem.stampBottled(fresh, now);
		ItemStack half = new ItemStack(ItemInit.bloody_flask.get());
		long halfLife = (long) (CoagulationRules.MINUTE_TICKS * Math.log(0.5D) / Math.log(1.0D - CoagulationRules.CLOT_SHARE_PER_MINUTE));
		half.set(DataComponentInit.BOTTLED_AT.get(), now - CoagulationRules.FRESH_WINDOW_TICKS - halfLife);
		ItemStack dead = new ItemStack(ItemInit.bloody_flask.get());
		dead.set(DataComponentInit.BOTTLED_AT.get(), now - CoagulationRules.FULL_CLOT_TICKS);
		int[] vials = new int[3];
		ItemStack[] inputs = {fresh, half, dead};
		for (int i = 0; i < inputs.length; i++) vials[i] = distilledVials(helper, inputs[i]);
		helper.assertTrue(vials[0] == 2 && vials[1] == 1 && vials[2] == 0,
				"fresh, half-clotted and clotted flasks distilled to " + vials[0] + ", " + vials[1] + ", " + vials[2] + " vials");
		helper.succeed();
	}

	private static int distilledVials(GameTestHelper helper, ItemStack flask) {
		BlockPos relative = new BlockPos(3, 1, 3);
		BlockPos main = helper.absolutePos(relative);
		ServerLevel level = helper.getLevel();
		for (BlockPos cell : BlockPos.betweenClosed(main.offset(-1, 0, -1), main.offset(1, 2, 1))) {
			level.setBlockAndUpdate(cell, Blocks.AIR.defaultBlockState());
		}
		level.setBlockAndUpdate(main.below(), Blocks.CAMPFIRE.defaultBlockState());
		BlockState alembic = BlockInit.ghastly_alembic.get().defaultBlockState();
		level.setBlockAndUpdate(main, alembic);
		alembic.getBlock().setPlacedBy(level, main, alembic, null, new ItemStack(alembic.getBlock()));
		try {
			var station = (GhastlyAlembicBlockEntity) level.getBlockEntity(main);
			station.setItem(GhastlyAlembicBlockEntity.SLOT_INPUT, flask.copy());
			for (int tick = 0; tick < 400 && !station.getItem(GhastlyAlembicBlockEntity.SLOT_INPUT).isEmpty(); tick++) {
				GhastlyAlembicBlockEntity.serverTick(level, main, level.getBlockState(main), station);
			}
			if (!station.getItem(GhastlyAlembicBlockEntity.SLOT_INPUT).isEmpty()) return -1;
			ItemStack result = station.getItem(GhastlyAlembicBlockEntity.SLOT_RESULT);
			return result.is(ItemInit.bloody_vial.get()) ? result.getCount() : 0;
		} finally {
			for (BlockPos cell : BlockPos.betweenClosed(main.offset(-1, 0, -1), main.offset(1, 2, 1))) {
				level.setBlockAndUpdate(cell, Blocks.AIR.defaultBlockState());
			}
		}
	}

	// ── The worn gourd (FX-1, CN-12) ──

	private static ItemStack rib(boolean open, double blood) {
		ItemStack rib = new ItemStack(ItemInit.hemorath_rib.get());
		CompoundTag data = new CompoundTag();
		data.putBoolean(BloodGourdItem.TAG_STATE, open);
		rib.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
		HemoCapabilityAccess.getBloodVolume(rib).orElseThrow().setBloodVolume(blood);
		return rib;
	}

	private static double gourdBlood(ItemStack gourd) {
		return HemoCapabilityAccess.getBloodVolume(gourd).orElseThrow().getBloodVolume();
	}

	/** Only the gourd in the worn slot grows its marrow or feeds its wearer; one carried or held does neither. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void onlyTheWornGourdGrowsOrFeeds(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			IBloodVolume blood = activeBlood(player, 1_000.0D);
			ItemStack held = rib(true, 500.0D);
			ItemStack packed = rib(false, 0.0D);
			player.getInventory().setItem(player.getInventory().selected, held);
			player.getInventory().setItem(9, packed);
			for (int tick = 0; tick < 20; tick++) player.getInventory().tick();
			helper.assertTrue(gourdBlood(packed) == 0.0D, "a Rib in the pack grew " + gourdBlood(packed) + " mL");
			helper.assertTrue(gourdBlood(held) == 500.0D && blood.getBloodVolume() == 1_000.0D,
					"an open Rib in the hand fed its holder: gourd " + gourdBlood(held) + ", body " + blood.getBloodVolume());

			var equipment = HemoCapabilityAccess.getEquipment(player);
			equipment.setStackInSlot(HarbingerEquipmentMenu.GOURD_SLOT_INDEX, rib(false, 0.0D));
			for (int tick = 0; tick < 20; tick++) equipment.tick();
			double grown = gourdBlood(equipment.getStackInSlot(HarbingerEquipmentMenu.GOURD_SLOT_INDEX));
			helper.assertTrue(Math.abs(grown - 20 * EnumBloodGourdTiers.RIB.getPassiveGenerationRate()) < 1e-6,
					"a worn Rib grew " + grown + " mL in 20 ticks");

			equipment.setStackInSlot(HarbingerEquipmentMenu.GOURD_SLOT_INDEX, rib(true, 500.0D));
			for (int tick = 0; tick < 20; tick++) equipment.tick();
			helper.assertTrue(blood.getBloodVolume() > 1_000.0D, "an open worn Rib did not feed its wearer");
		}
		helper.succeed();
	}

	/** FX-1, NQ-01-11: a suture draws from the worn gourd, never from an open gourd in the hand. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aSutureDrawsOnlyFromTheWornGourd(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		var station = scriptorium(helper, new BlockPos(2, 1, 2));
		BlockPos pos = station.getBlockPos();
		try (CapturedPlayer captured = CapturedPlayer.create(helper).online()) {
			ServerPlayer owner = captured.player;
			IBloodVolume blood = activeBlood(owner, 5_000.0D);
			HemoCapabilityAccess.getInitiatoryDegree(owner).setDegreeNumber(3);
			Vec3 near = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 1, 2)));
			owner.setPos(near.x, near.y, near.z);
			DirectBloodLinkData link = DirectBloodLinkData.nearby(owner.getUUID());
			BloodRoutingSavedData.get(level).setLink(pos, link);
			try {
				ItemStack held = rib(true, 500.0D);
				owner.setItemInHand(InteractionHand.MAIN_HAND, held);
				double routed = BloodRoutingHelper.routeLinkedTarget(level, pos, link);
				helper.assertTrue(routed > 0.0D && gourdBlood(held) == 500.0D && Math.abs(blood.getBloodVolume() - (5_000.0D - routed)) < 1e-6,
						"a suture drew from a held gourd: gourd " + gourdBlood(held) + ", body " + blood.getBloodVolume());

				owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
				var equipment = HemoCapabilityAccess.getEquipment(owner);
				equipment.setStackInSlot(HarbingerEquipmentMenu.GOURD_SLOT_INDEX, rib(true, 500.0D));
				tank(station).setBloodVolume(0.0D);
				double bodyBefore = blood.getBloodVolume();
				double again = BloodRoutingHelper.routeLinkedTarget(level, pos, link);
				double worn = gourdBlood(equipment.getStackInSlot(HarbingerEquipmentMenu.GOURD_SLOT_INDEX));
				helper.assertTrue(again > 0.0D && worn < 500.0D, "a suture did not draw from the worn gourd: " + worn);
				helper.assertTrue(Math.abs(blood.getBloodVolume() - (bodyBefore - (again - (500.0D - worn)))) < 1e-6,
						"the body paid what the worn gourd covered");
			} finally {
				BloodRoutingSavedData.get(level).removeLink(pos);
			}
		}
		helper.succeed();
	}

	// ── Side doors closed ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theFormationKeyCostsMoreThanTheCondenser(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			IBloodVolume blood = activeBlood(player, 1_000.0D);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
			player.setPos(helper.absolutePos(new BlockPos(2, 1, 2)).getCenter());
			{
				BodyFormationDraw.draw(player);
				helper.assertTrue(blood.getBloodVolume() == 1_000.0D - BodyFormationDraw.COST,
						"the key did not take its price, left " + blood.getBloodVolume());
				helper.assertTrue(BodyFormationDraw.COST > CoagulationRules.condenserMlPerFormation(0),
						"the key is not dearer than the cauldron");
				List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
						new AABB(player.blockPosition()).inflate(3.0D), e -> e.getItem().is(ItemInit.sanguine_formation.get()));
				helper.assertTrue(drops.size() == 1 && drops.get(0).getItem().getCount() == 1,
						"the key did not make exactly one Formation");
				drops.forEach(ItemEntity::discard);
				blood.setBloodVolume(BodyFormationDraw.COST - 1.0D);
				BodyFormationDraw.draw(player);
				helper.assertTrue(blood.getBloodVolume() == BodyFormationDraw.COST - 1.0D, "the key took a price it could not meet");
			}
		}
		helper.succeed();
	}

	// ── Acceptance: every reservoir clots, is living, or has no tank ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void everyReservoirClotsOrIsLivingOrKeepsNoTank(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
		List<String> checked = new ArrayList<>();
		List<String> wrong = new ArrayList<>();
		for (Block block : BuiltInRegistries.BLOCK) {
			if (!BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("hemomancy")) continue;
			if (!(block instanceof EntityBlock entityBlock)) continue;
			BlockEntity blockEntity;
			try {
				blockEntity = entityBlock.newBlockEntity(pos, block.defaultBlockState());
			} catch (RuntimeException ignored) {
				continue;
			}
			if (!(blockEntity instanceof IBloodReservoir reservoir)) continue;
			blockEntity.setLevel(level);
			String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
			checked.add(name);
			if (!reservoir.holdsBlood()) {
				BlockBloodVolume volume = ReservoirBlood.settled(blockEntity);
				if (volume.getMaxBloodVolume() > 0.0D || volume.fill(100.0D) && volume.getBloodVolume() > 0.0D)
					wrong.add(name + " says it keeps no tank but holds blood");
				continue;
			}
			BlockBloodVolume volume = ReservoirBlood.settled(blockEntity);
			volume.setMaxBloodVolume(1_000.0D);
			volume.fill(500.0D);
			volume.backdate(level.getGameTime(), CoagulationRules.FULL_CLOT_TICKS);
			double fresh = ReservoirBlood.settled(blockEntity).getBloodVolume();
			if (reservoir.isLivingVessel() ? fresh != 500.0D : fresh != 0.0D)
				wrong.add(name + (reservoir.isLivingVessel() ? " is living but clotted" : " held fresh blood an hour after feeding"));
		}
		helper.assertTrue(checked.size() >= 10, "found only " + checked.size() + " reservoirs: " + checked);
		helper.assertTrue(checked.contains("gourdvine_tap") && checked.contains("somatic_loom")
				&& checked.contains("enzymatic_scriptorium"), "the walk missed a known reservoir: " + checked);
		helper.assertTrue(wrong.isEmpty(), String.join("; ", wrong));
		helper.succeed();
	}
}
