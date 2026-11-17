package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.cling.ClingRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.cling.ClingService;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.cling.ClingState;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.CleatsFitting;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingDispatcher;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncCling;
import com.vincenthuto.hemomancy.common.network.mausoleum.WardStrikePacket;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.HematicArmatureBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Core-loop Track 9: Venous Cling with a real survival player. Projection into a wall the player touches roots them;
 * walking input crawls them up it; a ceiling holds them under it; upkeep is paid each second and its lack lets go, as
 * do a broken face and leaving the face; cleats cut the upkeep; a floor is not a cling; the root reaches the players
 * tracking the clinger. The client's own motion is the pure crawl rule, applied here as the client would.
 */
@GameTestHolder("cling_validation")
@PrefixGameTestTemplate(false)
public final class ClingGameTests {
	private static final String EMPTY = "empty";
	/** The wall's west face is the plane x = 6 in test space; the body stands flush against it. */
	private static final double FLUSH_X = 6.0D - 0.3D - 0.01D;

	private ClingGameTests() {
	}

	// ── Fixtures ──

	/** A floor at y=0, air above, and a stone wall filling x = 6..7 from y=1 to 10, z = 3..13. */
	private static void room(GameTestHelper helper, boolean ceiling) {
		for (int x = 0; x <= 12; x++) {
			for (int z = 0; z <= 16; z++) {
				for (int y = 0; y <= 11; y++) {
					boolean wall = x >= 6 && x <= 7 && y >= 1 && y <= 10 && z >= 3 && z <= 13;
					boolean roof = ceiling && y >= 8 && x <= 5;
					helper.setBlock(new BlockPos(x, y, z), y == 0 || wall || roof ? Blocks.STONE : Blocks.AIR);
				}
			}
		}
	}

	private static void prepare(ServerPlayer player, double blood) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(Math.max(volume.getMaxBloodVolume(), Math.max(blood, 100.0D)));
		volume.setBloodVolume(blood);
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		BloodManipulation cling = ManipulationInit.venous_cling.get();
		known.getKnownManips().put(cling, new ManipLevel(1, 0));
		known.setEquippedManipNames(new ArrayList<>(List.of("venous_cling")));
		known.setSelectedManip(cling);
		TemperFixtures.carry(player);
	}

	private static Vec3 at(GameTestHelper helper, double x, double y, double z) {
		return Vec3.atLowerCornerOf(helper.absolutePos(BlockPos.ZERO)).add(x, y, z);
	}

	/** Stands flush against the wall at height {@code y} and looks into its face. */
	private static void againstWall(GameTestHelper helper, ServerPlayer player, double y) {
		Vec3 feet = at(helper, FLUSH_X, y, 8.5);
		player.setPos(feet.x, feet.y, feet.z);
		player.lookAt(EntityAnchorArgument.Anchor.EYES, at(helper, 6.0, y + 1.3, 8.5));
		player.setOldPosAndRot();
		player.yHeadRotO = player.getYHeadRot();
	}

	private static void project(ServerPlayer player) {
		BloodProjectionItem.projectFromEntity(player.level(), player, 5.0D, 100.0D);
	}

	private static double blood(ServerPlayer player) {
		return HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
	}

	private static ClingState cling(ServerPlayer player) {
		return HemoCapabilityAccess.getCling(player);
	}

	/** Runs the server's cling tick for {@code ticks} ticks, then {@code after}. */
	private static void tickThen(GameTestHelper helper, CapturedPlayer captured, int ticks, Consumer<ServerPlayer> each,
			Consumer<ServerPlayer> after) {
		ServerPlayer player = captured.player;
		int[] tick = {0};
		helper.onEachTick(() -> {
			if (tick[0]++ >= ticks) return;
			if (each != null) each.accept(player);
			ClingService.serverTick(player);
		});
		helper.runAtTickTime(ticks + 1, () -> {
			try {
				after.accept(player);
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	// ── Root ──

	@GameTest(template = EMPTY, timeoutTicks = 40, batch = "cling_validation")
	public static void projectionIntoATouchedWallRootsThePlayer(GameTestHelper helper) {
		room(helper, false);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, 500.0D);
		againstWall(helper, player, 2.0D);
		helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isPresent(), "Venous Cling did not claim the wall");
		project(player);
		ClingState state = cling(player);
		helper.assertTrue(state.surface() == ClingRules.Surface.WALL && state.face() == Direction.WEST,
				"the player was not rooted to the wall's west face: " + state.surface() + " " + state.face());
		helper.assertTrue(blood(player) < 500.0D && blood(player) >= 500.0D - 2.0D * ClingRules.ROOT_BLOOD,
				"the root did not cost about " + ClingRules.ROOT_BLOOD + " mL: " + (500.0D - blood(player)));
		helper.assertTrue(!captured.sent(PacketSyncCling.class).isEmpty(), "the owner was not told");
		helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_CLINGING),
				"the first root did not grant Clinging (core-loop Track 11)");
		double paid = blood(player);
		for (int i = 0; i < 3; i++) project(player);
		helper.assertTrue(blood(player) == paid, "holding Projection while rooted poured blood into the wall");
		captured.close();
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40, batch = "cling_validation")
	public static void aSaintsWardWillNotTakeTheRoots(GameTestHelper helper) {
		room(helper, false);
		BoundingBox ward = new BoundingBox(helper.absolutePos(new BlockPos(6, 3, 8))).inflatedBy(1);
		Runnable unconsecrate = MausoleumWardGameTests.consecrate(helper, ward);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, 500.0D);
			againstWall(helper, player, 2.0D);
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isPresent(), "Venous Cling did not claim the wall");
			project(player);
			helper.assertTrue(!cling(player).isClinging(), "the ward took the player's roots");
			helper.assertTrue(blood(player) == 500.0D, "a refused root was paid for: " + (500.0D - blood(player)));
			helper.assertTrue(captured.said().contains("The ward will not take your roots."),
					"the ward refusal was silent: " + captured.said());
			helper.assertTrue(captured.sent(WardStrikePacket.class).stream()
					.anyMatch(strike -> strike.kind() == WardStrikePacket.Kind.BREAK && ward.isInside(strike.pos())),
					"the ward did not strike the wall it refused");
		} finally {
			unconsecrate.run();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40, batch = "cling_validation")
	public static void aWallTheBodyDoesNotTouchIsNoCling(GameTestHelper helper) {
		room(helper, false);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, 500.0D);
			Vec3 feet = at(helper, 4.0, 1.0, 8.5);
			player.setPos(feet.x, feet.y, feet.z);
			player.lookAt(EntityAnchorArgument.Anchor.EYES, at(helper, 6.0, 2.3, 8.5));
			player.setOldPosAndRot();
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isEmpty(), "a wall two blocks off was claimed");
			Vec3 floor = at(helper, FLUSH_X, 1.0, 8.5);
			player.setPos(floor.x, floor.y, floor.z);
			player.lookAt(EntityAnchorArgument.Anchor.EYES, at(helper, 5.5, 1.0, 8.5));
			player.setOldPosAndRot();
			project(player);
			helper.assertTrue(!cling(player).isClinging(), "the floor under the player became a cling");
		}
		helper.succeed();
	}

	// ── Crawl and hang ──

	@GameTest(template = EMPTY, timeoutTicks = 80, batch = "cling_validation")
	public static void walkingIntoTheWallClimbsIt(GameTestHelper helper) {
		room(helper, false);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, 500.0D);
		againstWall(helper, player, 2.0D);
		project(player);
		double startY = player.getY();
		tickThen(helper, captured, 40, held -> {
			// What the owner's client does with forward held, facing the wall (east).
			Vec3 intent = ClingRules.intent(1.0F, 0.0F, held.getYRot());
			held.move(MoverType.SELF, ClingRules.crawl(cling(held).face(), intent, ClingRules.speed(cling(held).surface(), false)));
		}, held -> {
			helper.assertTrue(cling(held).isClinging(), "the climb let go");
			helper.assertTrue(held.getY() - startY > 2.0D, "forty ticks of crawl climbed only " + (held.getY() - startY));
			helper.assertTrue(held.fallDistance == 0.0F, "the climb banked fall distance");
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 80, batch = "cling_validation")
	public static void aCeilingHoldsThePlayerUnderIt(GameTestHelper helper) {
		room(helper, true);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, 500.0D);
		Vec3 feet = at(helper, 3.5, 8.0 - 1.8 - 0.01, 8.5);
		player.setPos(feet.x, feet.y, feet.z);
		player.lookAt(EntityAnchorArgument.Anchor.EYES, at(helper, 3.6, 8.0, 8.9));
		player.setOldPosAndRot();
		project(player);
		helper.assertTrue(cling(player).surface() == ClingRules.Surface.CEILING, "the ceiling did not take the root");
		double rooted = blood(player);
		tickThen(helper, captured, 41, null, held -> {
			helper.assertTrue(cling(held).isClinging(), "the hang let go on its own");
			double upkeep = rooted - blood(held);
			helper.assertTrue(upkeep >= 2.0D * ClingRules.upkeepPerSecond(ClingRules.Surface.CEILING, false) - 1.0E-6D
					&& upkeep < 3.0D * ClingRules.upkeepPerSecond(ClingRules.Surface.CEILING, false),
					"two seconds under a ceiling cost " + upkeep + " mL");
		});
	}

	// ── What ends it ──

	@GameTest(template = EMPTY, timeoutTicks = 100, batch = "cling_validation")
	public static void runningShortOfBloodLetsGo(GameTestHelper helper) {
		room(helper, false);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, 12.0D);
		againstWall(helper, player, 2.0D);
		project(player);
		helper.assertTrue(cling(player).isClinging(), "the root was refused");
		tickThen(helper, captured, 70, null, held -> {
			helper.assertTrue(!cling(held).isClinging(), "the cling held with no blood to pay for it");
			helper.assertTrue(captured.said().contains("too thin"), "running short was not said: " + captured.said());
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 40, batch = "cling_validation")
	public static void aBrokenFaceLetsGo(GameTestHelper helper) {
		room(helper, false);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, 500.0D);
		againstWall(helper, player, 2.0D);
		project(player);
		BlockPos anchor = cling(player).anchor();
		helper.assertTrue(cling(player).isClinging(), "the root was refused");
		for (BlockPos pos : BlockPos.betweenClosed(anchor.offset(0, -3, -3), anchor.offset(1, 3, 3))) {
			helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		}
		ClingService.serverTick(player);
		helper.assertTrue(!cling(player).isClinging(), "the cling held a wall that was gone");
		helper.assertTrue(captured.said().contains("gave way"), "the broken face was not said: " + captured.said());
		captured.close();
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40, batch = "cling_validation")
	public static void leavingTheFaceLetsGoQuietly(GameTestHelper helper) {
		room(helper, false);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, 500.0D);
		againstWall(helper, player, 3.0D);
		project(player);
		int said = captured.chat.size();
		player.move(MoverType.SELF, ClingRules.jumpOff(cling(player).face()).scale(3.0D));
		ClingService.serverTick(player);
		helper.assertTrue(!cling(player).isClinging(), "the cling held a body that jumped off");
		helper.assertTrue(captured.chat.size() == said, "letting go by jumping said something: " + captured.said());
		helper.assertTrue(player.fallDistance == 0.0F, "letting go kept fall distance");
		captured.close();
		helper.succeed();
	}

	// ── Cleats ──

	@GameTest(template = EMPTY, timeoutTicks = 80, batch = "cling_validation")
	public static void cleatsCutTheUpkeep(GameTestHelper helper) {
		room(helper, false);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, 500.0D);
		player.setItemSlot(EquipmentSlot.FEET, CleatsFixtures.fitted(ItemInit.hematic_iron_boots.get()));
		againstWall(helper, player, 2.0D);
		project(player);
		helper.assertTrue(cling(player).cleats(), "the cleats were not noticed");
		double rooted = blood(player);
		tickThen(helper, captured, 41, null, held -> {
			double upkeep = rooted - blood(held);
			helper.assertTrue(Math.abs(upkeep - 2.0D * ClingRules.upkeepPerSecond(ClingRules.Surface.WALL, true)) < 0.5D,
					"two seconds in cleats cost " + upkeep + " mL, not " + 2.0D * ClingRules.upkeepPerSecond(ClingRules.Surface.WALL, true));
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 80, batch = "cling_validation")
	public static void cleatsTurnedOffCostTheFullUpkeep(GameTestHelper helper) {
		room(helper, false);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, 500.0D);
		ItemStack boots = CleatsFixtures.fitted(ItemInit.hematic_iron_boots.get());
		boots.set(DataComponentInit.CLEATS.get(), false);
		player.setItemSlot(EquipmentSlot.FEET, boots);
		againstWall(helper, player, 2.0D);
		project(player);
		helper.assertTrue(!cling(player).cleats(), "cleats turned off still cut the cling");
		double rooted = blood(player);
		tickThen(helper, captured, 41, null, held -> {
			double upkeep = rooted - blood(held);
			helper.assertTrue(Math.abs(upkeep - 2.0D * ClingRules.upkeepPerSecond(ClingRules.Surface.WALL, false)) < 0.5D,
					"two seconds with the cleats off cost " + upkeep + " mL");
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 20, batch = "cling_validation")
	public static void theRadialToggleTurnsTheWornCleatsOffAndOn(GameTestHelper helper) {
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemInit.hematic_iron_boots.get()));
		helper.assertTrue(!CleatsFitting.toggle(player) && !ClingService.wearsCleats(player),
				"boots without cleats toggled or counted as cleats");
		player.setItemSlot(EquipmentSlot.FEET, CleatsFixtures.fitted(ItemInit.barbed_boots.get()));
		helper.assertTrue(ClingService.wearsCleats(player), "freshly fitted cleats were not on");
		helper.assertTrue(CleatsFitting.toggle(player) && !ClingService.wearsCleats(player), "the toggle did not turn them off");
		helper.assertTrue(CleatsFitting.isFitted(player.getItemBySlot(EquipmentSlot.FEET)), "turning them off took them out");
		helper.assertTrue(CleatsFitting.toggle(player) && ClingService.wearsCleats(player), "the toggle did not turn them back on");
		captured.close();
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 20, batch = "cling_validation")
	public static void theNineBloodSideBootsTakeCleatsAndNoOthers(GameTestHelper helper) {
		List<Item> takes = List.of(ItemInit.hematic_iron_boots.get(), ItemInit.barbed_boots.get(), ItemInit.chitinite_boots.get(),
				ItemInit.prismatic_boots.get(), ItemInit.blood_lust_boots.get(), ItemInit.edacious_blood_lust_boots.get(),
				ItemInit.phantasmal_blood_lust_boots.get(), ItemInit.sheolic_blood_lust_boots.get(),
				ItemInit.silent_archon_boots.get());
		for (Item boots : takes) {
			helper.assertTrue(CleatsFitting.canFit(new ItemStack(boots)), boots + " does not take cleats");
			helper.assertTrue(!CleatsFitting.canFit(CleatsFixtures.fitted(boots)), boots + " takes a second set");
		}
		for (Item boots : List.of(ItemInit.unstained_boots.get(), ItemInit.venous_strider_sabatons.get(),
				Items.IRON_BOOTS, ItemInit.hematic_cleats.get())) {
			helper.assertTrue(!CleatsFitting.canFit(new ItemStack(boots)), boots + " takes cleats");
		}
		helper.assertTrue(BuiltInRegistries.ITEM.getTag(CleatsFitting.TAKES_CLEATS).map(tag -> tag.size()).orElse(0)
				== takes.size(), "the cleats tag is not the nine blood-side boots");
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 200, batch = "cling_validation")
	public static void theArmatureFitsCleatsToTheWornBootsWithoutPractice(GameTestHelper helper) {
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		HematicArmatureBlockEntity armature = armature(helper, 400.0D);
		armature.setItem(HematicArmatureBlockEntity.SLOT_HEAD_REAGENT, new ItemStack(ItemInit.hematic_cleats.get()));
		ItemStack boots = new ItemStack(ItemInit.hematic_iron_boots.get());
		player.setItemSlot(EquipmentSlot.FEET, boots);
		helper.assertTrue(!armature.processRestrainedPlayer(helper.getLevel(), player), "the fitting finished at once");
		helper.runAfterDelay(PROCESS_WAIT, () -> {
			helper.assertTrue(armature.processRestrainedPlayer(helper.getLevel(), player), "the fitting did not finish");
			ItemStack worn = player.getItemBySlot(EquipmentSlot.FEET);
			helper.assertTrue(worn == boots, "the fitting replaced the worn boots: " + worn);
			helper.assertTrue(CleatsFitting.isOn(worn), "the boots carry no cleats that are on");
			helper.assertTrue(armature.getItem(HematicArmatureBlockEntity.SLOT_HEAD_REAGENT).isEmpty(), "the cleats were not used");
			helper.assertTrue(Math.abs(armature.getBloodCapability().getBloodVolume() - (400.0D - CleatsFitting.BLOOD_COST)) < 0.01D,
					"the fitting did not take " + CleatsFitting.BLOOD_COST + " mL");
			helper.assertTrue(!HemoCapabilityAccess.stationUpgrades(player).hasUsed(UpgradeStation.ARMATURE,
					StationUpgradeCatalog.ARMOR_UPGRADE), "a fitting counted as armor-upgrade practice");
			captured.close();
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 200, batch = "cling_validation")
	public static void theArmatureRefusesCleatsForBootsThatTakeNone(GameTestHelper helper) {
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		HematicArmatureBlockEntity armature = armature(helper, 400.0D);
		armature.setItem(HematicArmatureBlockEntity.SLOT_HEAD_REAGENT, new ItemStack(ItemInit.hematic_cleats.get()));
		player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemInit.unstained_boots.get()));
		armature.processRestrainedPlayer(helper.getLevel(), player);
		helper.runAfterDelay(PROCESS_WAIT, () -> {
			helper.assertTrue(!armature.processRestrainedPlayer(helper.getLevel(), player), "the Armature worked on Unstained boots");
			helper.assertTrue(!CleatsFitting.isFitted(player.getItemBySlot(EquipmentSlot.FEET)), "Unstained boots took cleats");
			helper.assertTrue(!armature.getItem(HematicArmatureBlockEntity.SLOT_HEAD_REAGENT).isEmpty(), "the cleats were used");
			captured.close();
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 200, batch = "cling_validation")
	public static void fittedCleatsAndTheirToggleSurviveALineageUpgrade(GameTestHelper helper) {
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(3);
		HematicArmatureBlockEntity armature = armature(helper, 1000.0D);
		armature.setItem(HematicArmatureBlockEntity.SLOT_HEAD_REAGENT, new ItemStack(ItemInit.aculeate_vitriol.get()));
		ItemStack boots = CleatsFixtures.fitted(ItemInit.hematic_iron_boots.get());
		boots.set(DataComponentInit.CLEATS.get(), false);
		player.setItemSlot(EquipmentSlot.FEET, boots);
		armature.processRestrainedPlayer(helper.getLevel(), player);
		helper.runAfterDelay(PROCESS_WAIT, () -> {
			helper.assertTrue(armature.processRestrainedPlayer(helper.getLevel(), player), "the upgrade did not finish");
			ItemStack worn = player.getItemBySlot(EquipmentSlot.FEET);
			helper.assertTrue(worn.is(ItemInit.barbed_boots.get()), "the boots were not upgraded: " + worn);
			helper.assertTrue(CleatsFitting.isFitted(worn) && !CleatsFitting.isOn(worn),
					"the upgrade lost the cleats or their toggle: " + worn.get(DataComponentInit.CLEATS.get()));
			captured.close();
			helper.succeed();
		});
	}

	/** Past one Armature work interval, so the second cycle finishes the work the first one started. */
	private static final int PROCESS_WAIT = HematicArmatureBlockEntity.PROCESS_INTERVAL_TICKS + 2;

	/** A base Hematic Armature at (3, 1, 3) holding {@code blood} mL. */
	private static HematicArmatureBlockEntity armature(GameTestHelper helper, double blood) {
		BlockPos pos = new BlockPos(3, 1, 3);
		helper.setBlock(pos, BlockInit.hematic_armature.get().defaultBlockState());
		HematicArmatureBlockEntity armature = (HematicArmatureBlockEntity) helper.getBlockEntity(pos);
		IBloodVolume tank = armature.getBloodCapability();
		tank.setActive(true);
		tank.setMaxBloodVolume(HematicArmatureBlockEntity.MAX_BLOOD);
		tank.setBloodVolume(blood);
		return armature;
	}
}
