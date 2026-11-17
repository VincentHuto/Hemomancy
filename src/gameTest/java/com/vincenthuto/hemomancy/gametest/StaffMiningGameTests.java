package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.ILivingStaffProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.LivingStaffBondHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostLedger;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingDigRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffGrowth;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffGrowthRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormHelper;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.ferric.LodeDrawSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingDispatcher;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingSessions;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbReach;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.block.harbinger.BlockBloodEndpoint;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.EnzymaticScriptoriumBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.MasonsEffigyBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineMonolithBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.RingDoorBlockEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Core-loop Track 8: the Living Staff's growth and its mining, with a real survival player. Rings come from handled
 * blood and are stamped on the Staff and its forms; the pickaxe form comes with the bond, mines like iron and like
 * diamond from the fourth ring, pays blood per block by hardness, digs like a bare hand when dry and takes no
 * enchantment; Lode Draw crawls a lode and breaks it on release at the Staff's tier and not where the player may not
 * build; Hematic Hewing cuts its shapes and never a block entity; the Talaromyces vein uses the same crawl.
 */
@GameTestHolder("staff_mining_validation")
@PrefixGameTestTemplate(false)
public final class StaffMiningGameTests {
	private static final String EMPTY = "empty";
	/** The English of {@code message.hemomancy.player_block_edits.refused}. */
	static final String REFUSED = "That ground is not yours to change.";

	private StaffMiningGameTests() {
	}

	// ── Fixtures ──

	/** A bonded Harbinger with active blood whose Staff has handled {@code handled} mL. */
	private static void bond(ServerPlayer player, double blood, double handled) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(Math.max(volume.getMaxBloodVolume(), Math.max(blood, 100.0D)));
		volume.setBloodVolume(blood);
		ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player);
		progress.setLivingStaffBond(true);
		progress.setBloodHandled(handled);
	}

	/** {@link #bond}, with {@code manipulation} known, equipped and selected, its alignment met and Temper carried. */
	private static void select(ServerPlayer player, String manipulation, double blood, double handled) {
		bond(player, blood, handled);
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
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

	private static ItemStack pickaxe(int ring) {
		ItemStack pickaxe = new ItemStack(ItemInit.living_pickaxe.get());
		LivingStaffGrowth.stamp(pickaxe, ring);
		return pickaxe;
	}

	private static void aim(ServerPlayer player, Vec3 feet, Vec3 at) {
		player.setPos(feet.x, feet.y, feet.z);
		player.lookAt(EntityAnchorArgument.Anchor.EYES, at);
		player.setOldPosAndRot();
		player.yHeadRotO = player.getYHeadRot();
	}

	private static double project(ServerPlayer player) {
		return BloodProjectionItem.projectFromEntity(player.level(), player, 5.0D, 100.0D);
	}

	private static double blood(ServerPlayer player) {
		return HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
	}

	private static double price(ServerPlayer player, String manipulation) {
		VerbSculpting sculpting = SculptingDispatcher.selected(player, Verb.PROJECT).orElseThrow().sculpting();
		return sculpting.bloodPerPulse()
				* ManipulationCostLedger.collect(player, ManipulationInit.getByName(manipulation), 1.0D).totalMultiplier();
	}

	/** Clears the test volume (the template is not empty above its floor) and lays a stone floor. */
	private static void clear(GameTestHelper helper, int sizeX, int sizeZ, int height) {
		for (int x = 0; x <= sizeX; x++) {
			for (int z = 0; z <= sizeZ; z++) {
				helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
				for (int y = 1; y <= height; y++) helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
			}
		}
	}

	private static long drops(ServerLevel level, BlockPos pos, double radius, Item item) {
		return level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(radius),
				entity -> entity.getItem().is(item)).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	private static void sweepDrops(ServerLevel level, BlockPos pos) {
		level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(6.0D)).forEach(ItemEntity::discard);
	}

	// ── Rings ──

	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void ringsComeFromHandledBloodAndGrowTheStaffsReach(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			bond(player, 1000.0D, 0.0D);
			ItemStack staff = new ItemStack(ItemInit.living_staff.get());
			player.setItemInHand(InteractionHand.MAIN_HAND, staff);
			double young = VerbReach.of(player, Verb.PROJECT);
			HemoCapabilityAccess.getLivingStaffProgress(player).setBloodHandled(100_000.0D);
			helper.assertTrue(LivingStaffGrowth.ring(player) == 3, "100,000 mL handled is not three rings");
			double grown = VerbReach.of(player, Verb.PROJECT);
			helper.assertTrue(Math.abs(grown - young - 1.5D * SkillPointHelper.getSanguineReachMultiplier(player)) < 1.0E-6D,
					"three rings did not add a block and a half of reach: " + young + " -> " + grown);
			LivingStaffGrowth.refreshStamp(player, staff);
			helper.assertTrue(LivingStaffGrowth.stampedRing(staff) == 3, "the held Staff was not stamped with its rings");
			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			helper.assertTrue(VerbReach.of(player, Verb.PROJECT) == VerbReach.BASE * SkillPointHelper.getSanguineReachMultiplier(player),
					"rings lengthened a bare hand's reach");
		}
		helper.succeed();
	}

	/** CQ-8: blood the Staff cycles through a tank, in and back out, moves blood but grows no rings. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aTankRoundTripGrowsNoRings(GameTestHelper helper) {
		BlockPos station = new BlockPos(3, 1, 2);
		helper.setBlock(station.below(), Blocks.STONE);
		helper.setBlock(station, com.vincenthuto.hemomancy.common.init.BlockInit.enzymatic_scriptorium.get());
		ServerLevel level = helper.getLevel();
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			bond(player, 1000.0D, 0.0D);
			IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
			known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
			known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
			known.setSelectedManip(BloodManipulation.BLANK);
			ItemStack staff = new ItemStack(ItemInit.living_staff.get());
			player.setItemInHand(InteractionHand.MAIN_HAND, staff);
			aim(player, Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 1, 2))),
					Vec3.atCenterOf(helper.absolutePos(station)));
			for (Verb verb : List.of(Verb.PROJECT, Verb.ABSORB)) {
				known.setActiveVerb(verb);
				double before = blood(player);
				staff.use(level, player, InteractionHand.MAIN_HAND);
				for (int tick = 0; tick < 6; tick++) {
					staff.getItem().onUseTick(level, player, staff, staff.getUseDuration(player) - tick);
				}
				helper.assertTrue(verb == Verb.PROJECT ? blood(player) < before : blood(player) > before,
						"the Staff moved no blood " + (verb == Verb.PROJECT ? "into" : "out of") + " the tank");
				staff.getItem().releaseUsing(staff, level, player, staff.getUseDuration(player) - 6);
				player.stopUsingItem();
			}
			helper.assertTrue(HemoCapabilityAccess.getLivingStaffProgress(player).getBloodHandled() == 0.0D,
					"a tank round trip grew the Staff: " + HemoCapabilityAccess.getLivingStaffProgress(player).getBloodHandled());
		}
		helper.succeed();
	}

	/** CQ-8: only a tank stops the Staff's projection growing rings; a block that spends the blood still grows them. */
	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void onlyATankStopsProjectionGrowingRings(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
		helper.assertTrue(BloodProjectionItem.holdsProjectedBlood(
				new EnzymaticScriptoriumBlockEntity(pos,
						BlockInit.enzymatic_scriptorium.get().defaultBlockState())),
				"a station's tank was not counted as a tank");
		helper.assertTrue(!BloodProjectionItem.holdsProjectedBlood(
				new RingDoorBlockEntity(pos,
						BlockInit.ring_door.get().defaultBlockState())),
				"the Ring Door spends its blood but was counted as a tank");
		helper.assertTrue(!BloodProjectionItem.holdsProjectedBlood(
				new SanguineMonolithBlockEntity(pos,
						BlockInit.sanguine_monolith.get().defaultBlockState())),
				"the Monolith gate spends its blood but was counted as a tank");
		helper.assertTrue(!BloodProjectionItem.holdsProjectedBlood(
				new MasonsEffigyBlockEntity(pos,
						BlockInit.mason_effigy.get().defaultBlockState())),
				"the Mason's Effigy spends its blood but was counted as a tank");
		helper.assertTrue(!BloodProjectionItem.holdsProjectedBlood(
				new SomaticLoomBlockEntity(pos,
						BlockInit.somatic_loom.get().defaultBlockState())),
				"the Somatic Loom keeps no tank but was counted as one");
		for (var endpoint : List.of(BlockInit.mason_effigy,
				BlockInit.heartwood_core)) {
			helper.assertTrue(endpoint.get() instanceof BlockBloodEndpoint spent
					&& !spent.holdsProjectedBlood(), endpoint.getId() + " uses its blood up but was counted as a tank");
		}
		helper.assertTrue(BlockInit.consecrated_bloodwell.get()
				instanceof BlockBloodEndpoint well && well.holdsProjectedBlood(),
				"the Bloodwell keeps its blood but was not counted as a tank");
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void aRingClosesAsBloodMovesThroughTheStaff(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			bond(player, 1000.0D, 4_990.0D);
			ItemStack staff = new ItemStack(ItemInit.living_staff.get());
			player.setItemInHand(InteractionHand.MAIN_HAND, staff);
			LivingStaffGrowth.record(player, 20.0D);
			helper.assertTrue(HemoCapabilityAccess.getLivingStaffProgress(player).getBloodHandled() == 5_010.0D,
					"the Staff did not count the blood it moved");
			helper.assertTrue(LivingStaffGrowth.stampedRing(player.getMainHandItem()) == 1,
					"the first ring did not show on the held Staff when it closed");
			helper.assertTrue(captured.said().contains("1 of 5"), "the first ring was not announced: " + captured.said());
			helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_FIRST_RING),
					"the first ring did not grant The First Ring (core-loop Track 11)");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void theFifthRingCheapensTheSwap(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			bond(player, 1000.0D, 0.0D);
			double young = SkillPointHelper.getLivingStaffHotSwapCost(player);
			HemoCapabilityAccess.getLivingStaffProgress(player).setBloodHandled(LivingStaffGrowthRules.threshold(5));
			double old = SkillPointHelper.getLivingStaffHotSwapCost(player);
			helper.assertTrue(young - old == 25.0D, "the fifth ring did not take 25 mL off a swap: " + young + " -> " + old);
		}
		helper.succeed();
	}

	// ── The pickaxe form ──

	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void theBondTeachesThePickaxeAndTheFormCarriesItsRings(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			bond(player, 1000.0D, 0.0D);
			HemoCapabilityAccess.getLivingStaffProgress(player).setLivingStaffBond(false);
			LivingStaffBondHelper.unlockFromBloodStructureCraft(player, new ItemStack(ItemInit.living_staff.get()));
			IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
			helper.assertTrue(known.doesListContainName(known.getKnownManips(), ManipulationInit.conjure_pickaxe.get()),
					"the bond did not teach Conjure Pickaxe");
			HemoCapabilityAccess.getLivingStaffProgress(player).setBloodHandled(300_000.0D);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.living_staff.get()));
			helper.assertTrue(LivingStaffWeaponFormHelper.applySelection(player, ManipulationInit.conjure_pickaxe.get()),
					"the Staff did not take its pickaxe form");
			ItemStack form = player.getMainHandItem();
			helper.assertTrue(form.is(ItemInit.living_pickaxe.get()), "the Staff became " + form);
			helper.assertTrue(LivingStaffGrowth.stampedRing(form) == 4, "the form did not carry the Staff's four rings");
			helper.assertTrue(form.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()),
					"a four-ring pickaxe cannot take diamond ore");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void thePickaxeTakesIronAtTheBondDiamondsFromTheSecondRingAndObsidianFromTheFourth(GameTestHelper helper) {
		ItemStack young = pickaxe(0);
		ItemStack second = pickaxe(2);
		ItemStack old = pickaxe(4);
		helper.assertTrue(young.isCorrectToolForDrops(Blocks.IRON_ORE.defaultBlockState()), "a bonded pickaxe cannot take iron");
		helper.assertTrue(!young.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()), "a bonded pickaxe takes diamond");
		helper.assertTrue(second.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()), "two rings cannot take diamond");
		helper.assertTrue(!second.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()), "two rings take obsidian");
		helper.assertTrue(old.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()), "four rings are not diamond-equal");
		float bonded = young.getDestroySpeed(Blocks.STONE.defaultBlockState());
		float iron = second.getDestroySpeed(Blocks.STONE.defaultBlockState());
		float diamond = old.getDestroySpeed(Blocks.STONE.defaultBlockState());
		helper.assertTrue(bonded < iron && iron < diamond && diamond < 9.0F,
				"dig speeds did not climb to diamond and stop short of netherite: " + bonded + ", " + iron + ", " + diamond);
		helper.assertTrue(young.getDestroySpeed(Blocks.OAK_LOG.defaultBlockState()) == LivingDigRules.HAND_SPEED,
				"the pickaxe chops wood faster than a hand");
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void livingFormsTakeNoEnchantmentAndHaveNoDurability(GameTestHelper helper) {
		Holder<Enchantment> fortune = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
				.getOrThrow(Enchantments.FORTUNE);
		Holder<Enchantment> efficiency = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
				.getOrThrow(Enchantments.EFFICIENCY);
		for (Item item : List.of(ItemInit.living_pickaxe.get(), ItemInit.living_axe.get(), ItemInit.living_blade.get())) {
			ItemStack stack = new ItemStack(item);
			helper.assertTrue(!stack.isEnchantable(), item + " can go on an enchanting table");
			helper.assertTrue(!stack.supportsEnchantment(fortune) && !stack.supportsEnchantment(efficiency),
					item + " accepts Fortune or Efficiency from an anvil");
			helper.assertTrue(!stack.isDamageableItem(), item + " still wears down");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void eachBlockCostsBloodByHardness(GameTestHelper helper) {
		clear(helper, 6, 2, 3);
		ServerLevel level = helper.getLevel();
		BlockPos stone = helper.absolutePos(new BlockPos(2, 1, 1));
		BlockPos ore = helper.absolutePos(new BlockPos(4, 1, 1));
		level.setBlock(stone, Blocks.STONE.defaultBlockState(), 3);
		level.setBlock(ore, Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			player.setGameMode(GameType.SURVIVAL);
			bond(player, 1000.0D, 0.0D);
			player.setItemInHand(InteractionHand.MAIN_HAND, pickaxe(0));
			helper.assertTrue(player.gameMode.destroyBlock(stone), "the pickaxe could not break stone");
			helper.assertTrue(blood(player) == 997.0D, "stone did not cost 3 mL: " + (1000.0D - blood(player)));
			helper.assertTrue(player.gameMode.destroyBlock(ore), "the pickaxe could not break deepslate iron ore");
			helper.assertTrue(blood(player) == 988.0D, "deepslate iron ore did not cost 9 mL: " + (997.0D - blood(player)));
			helper.assertTrue(drops(level, ore, 1.0D, Items.RAW_IRON) == 1, "a bonded pickaxe got no raw iron, or Fortune");
			helper.assertTrue(HemoCapabilityAccess.getLivingStaffProgress(player).getBloodHandled() == 12.0D,
					"the blood paid for digging did not age the Staff");
		} finally {
			sweepDrops(level, stone);
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void aDryPickaxeDigsLikeABareHand(GameTestHelper helper) {
		clear(helper, 6, 2, 3);
		ServerLevel level = helper.getLevel();
		BlockPos ore = helper.absolutePos(new BlockPos(3, 1, 1));
		level.setBlock(ore, Blocks.IRON_ORE.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			player.setGameMode(GameType.SURVIVAL);
			bond(player, 2.0D, 0.0D);
			player.setItemInHand(InteractionHand.MAIN_HAND, pickaxe(4));
			helper.assertTrue(player.getDigSpeed(Blocks.IRON_ORE.defaultBlockState(), ore) == LivingDigRules.HAND_SPEED,
					"a dry pickaxe still digs at its own speed");
			helper.assertTrue(!player.hasCorrectToolForDrops(Blocks.IRON_ORE.defaultBlockState(), level, ore),
					"a dry pickaxe still harvests iron ore");
			helper.assertTrue(player.gameMode.destroyBlock(ore), "the dry pickaxe could not break the ore at all");
			helper.assertTrue(drops(level, ore, 1.0D, Items.RAW_IRON) == 0, "a dry pickaxe got raw iron");
			helper.assertTrue(blood(player) == 2.0D, "a block the player could not pay for was charged anyway");
		} finally {
			sweepDrops(level, ore);
		}
		helper.succeed();
	}

	// ── Lode Draw ──

	/** Five iron ores in a row (one of them deepslate), a coal ore beside the first, all at y 1. */
	private static List<BlockPos> lode(GameTestHelper helper, Block first) {
		ServerLevel level = helper.getLevel();
		List<BlockPos> lode = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			BlockPos pos = helper.absolutePos(new BlockPos(4 + i, 1, 2));
			level.setBlock(pos, (i == 0 ? first : i == 3 ? Blocks.DEEPSLATE_IRON_ORE : Blocks.IRON_ORE).defaultBlockState(), 3);
			lode.add(pos);
		}
		level.setBlock(helper.absolutePos(new BlockPos(4, 1, 3)), Blocks.COAL_ORE.defaultBlockState(), 3);
		return lode;
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void lodeDrawCrawlsTheLodeAndBreaksWhatItReachedOnRelease(GameTestHelper helper) {
		clear(helper, 10, 4, 3);
		ServerLevel level = helper.getLevel();
		List<BlockPos> lode = lode(helper, Blocks.IRON_ORE);
		BlockPos coal = helper.absolutePos(new BlockPos(4, 1, 3));
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		select(player, "lode_draw", 2000.0D, 0.0D);
		aim(player, Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 1, 2))), Vec3.atCenterOf(lode.getFirst()));
		double pulse = price(player, "lode_draw");
		int[] tick = {0};
		helper.onEachTick(() -> {
			if (tick[0]++ < 25) project(player);
		});
		helper.runAtTickTime(28, () -> {
			try {
				int reached = LodeDrawSculpting.drawSize(player);
				helper.assertTrue(reached == 4, "three pulses after the first claim reached " + reached + " blocks");
				helper.assertTrue(lode.stream().allMatch(pos -> !level.getBlockState(pos).isAir()), "ore broke before release");
				helper.assertTrue(Math.abs(2000.0D - blood(player) - 3 * pulse) < 1.0E-6D,
						"three pulses did not each pay " + pulse + ": paid " + (2000.0D - blood(player)));
				SculptingSessions.end(player, true);
				for (int i = 0; i < lode.size(); i++) {
					boolean standing = !level.getBlockState(lode.get(i)).isAir();
					helper.assertTrue(standing == (i >= 4), "lode block " + i + " standing=" + standing);
				}
				helper.assertTrue(level.getBlockState(coal).is(Blocks.COAL_ORE), "the draw left its lode for coal");
				helper.assertTrue(drops(level, lode.get(2), 4.0D, Items.RAW_IRON) == 4,
						"the drawn lode did not drop one plain raw iron a block");
				helper.assertTrue(LodeDrawSculpting.drawSize(player) == 0, "the draw outlived its release");
			} finally {
				sweepDrops(level, lode.get(2));
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void lodeDrawLeavesDiamondsToTheSecondRing(GameTestHelper helper) {
		clear(helper, 10, 4, 3);
		List<BlockPos> lode = lode(helper, Blocks.DIAMOND_ORE);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			select(player, "lode_draw", 2000.0D, 0.0D);
			aim(player, Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 1, 2))), Vec3.atCenterOf(lode.getFirst()));
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isEmpty(),
					"Lode Draw claimed diamond ore at the bond");
			HemoCapabilityAccess.getLivingStaffProgress(player).setBloodHandled(LivingStaffGrowthRules.threshold(2));
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isPresent(),
					"Lode Draw left diamond ore alone at the second ring");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void lodeDrawBreaksNothingWhereThePlayerMayNotBuild(GameTestHelper helper) {
		clear(helper, 10, 4, 3);
		ServerLevel level = helper.getLevel();
		List<BlockPos> lode = lode(helper, Blocks.IRON_ORE);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			select(player, "lode_draw", 2000.0D, 0.0D);
			player.setGameMode(GameType.ADVENTURE);
			aim(player, Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 1, 2))), Vec3.atCenterOf(lode.getFirst()));
			// The draw still claims the ore, so it is the break guard, not a missed claim, that keeps the lode standing.
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isPresent(),
					"Lode Draw did not claim the ore, so the guard was never reached");
			float[] temper = HemoCapabilityAccess.getBloodTemper(player).snapshot();
			project(player);
			helper.assertTrue(LodeDrawSculpting.drawSize(player) == 0, "a draw began where the player may not build");
			// CQ-2: the refusal comes before the price, so nothing is paid for the refused pulse.
			helper.assertTrue(blood(player) == 2000.0D, "a refused draw was paid for: " + blood(player));
			helper.assertTrue(java.util.Arrays.equals(temper, HemoCapabilityAccess.getBloodTemper(player).snapshot()),
					"a refused draw spent Temper");
			helper.assertTrue(captured.said().contains(REFUSED), "the refusal was silent: " + captured.said());
			SculptingSessions.end(player, true);
			helper.assertTrue(lode.stream().noneMatch(pos -> level.getBlockState(pos).isAir()),
					"Lode Draw broke ore for a player who may not build");
		}
		helper.succeed();
	}

	/** CN-13: protection is asked on every pulse, so a draw already running stops, unpaid, once the player may not build. */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void lodeDrawStopsMidChannelWhenThePlayerMayNoLongerBuild(GameTestHelper helper) {
		clear(helper, 10, 4, 3);
		ServerLevel level = helper.getLevel();
		List<BlockPos> lode = lode(helper, Blocks.IRON_ORE);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		select(player, "lode_draw", 2000.0D, 0.0D);
		aim(player, Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 1, 2))), Vec3.atCenterOf(lode.getFirst()));
		int[] tick = {0};
		double[] afterFirst = {Double.NaN};
		int[] reached = {0};
		helper.onEachTick(() -> {
			if (tick[0]++ >= 25) return;
			double before = blood(player);
			project(player);
			if (Double.isNaN(afterFirst[0]) && blood(player) < before) {
				afterFirst[0] = blood(player);
				reached[0] = LodeDrawSculpting.drawSize(player);
				player.setGameMode(GameType.ADVENTURE);
			}
		});
		helper.runAtTickTime(28, () -> {
			try {
				helper.assertFalse(Double.isNaN(afterFirst[0]), "the draw never paid its first pulse");
				helper.assertTrue(blood(player) == afterFirst[0], "a draw kept paying after the player lost the right to build: "
						+ (afterFirst[0] - blood(player)) + " mL more");
				helper.assertTrue(LodeDrawSculpting.drawSize(player) <= reached[0],
						"the draw grew from " + reached[0] + " to " + LodeDrawSculpting.drawSize(player) + " while refused");
				helper.assertTrue(captured.said().contains(REFUSED), "the refusal was silent: " + captured.said());
				SculptingSessions.end(player, true);
				helper.assertTrue(lode.stream().noneMatch(pos -> level.getBlockState(pos).isAir()),
						"Lode Draw broke ore after the player lost the right to build");
			} finally {
				sweepDrops(level, lode.get(2));
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void talaromycesFollowsTheLodeThroughTheSameCrawl(GameTestHelper helper) {
		clear(helper, 10, 4, 3);
		ServerLevel level = helper.getLevel();
		List<BlockPos> lode = lode(helper, Blocks.IRON_ORE);
		BlockPos coal = helper.absolutePos(new BlockPos(4, 1, 3));
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			player.setGameMode(GameType.SURVIVAL);
			bond(player, 100.0D, 0.0D);
			HemoCapabilityAccess.getScarState(player).setFungalScar(new ItemStack(ItemInit.talaromyces_minus.get()));
			player.setShiftKeyDown(true);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE_PICKAXE));
			helper.assertTrue(player.gameMode.destroyBlock(lode.getFirst()), "the player could not break the first ore");
			helper.assertTrue(lode.stream().allMatch(pos -> level.getBlockState(pos).isAir()),
					"the scar's vein did not follow the whole lode, deepslate included");
			helper.assertTrue(Math.abs(blood(player) - (100.0D - 4 * com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.VeinMinerHelper.BLOOD_PER_EXTRA_ORE)) < 1.0E-6D,
					"the four extra ores did not cost 12 mL each: " + blood(player));
			helper.assertTrue(level.getBlockState(coal).is(Blocks.COAL_ORE), "the scar's vein crossed into coal");
			helper.assertTrue(drops(level, lode.get(2), 4.0D, Items.RAW_IRON) == 5, "the vein did not drop its iron");
			helper.assertTrue(BlockPos.ZERO.equals(HemoCapabilityAccess.getKnownManipulations(player).getLastVeinMineStart()),
					"the vein left its guard set");
		} finally {
			sweepDrops(level, lode.get(2));
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void theScarredVeinStopsWhereTheBloodStops(GameTestHelper helper) {
		clear(helper, 10, 4, 3);
		ServerLevel level = helper.getLevel();
		List<BlockPos> lode = lode(helper, Blocks.IRON_ORE);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			player.setGameMode(GameType.SURVIVAL);
			bond(player, 30.0D, 0.0D);
			HemoCapabilityAccess.getScarState(player).setFungalScar(new ItemStack(ItemInit.talaromyces_minus.get()));
			player.setShiftKeyDown(true);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE_PICKAXE));
			helper.assertTrue(player.gameMode.destroyBlock(lode.getFirst()), "the player could not break the first ore");
			long mined = lode.stream().skip(1).filter(pos -> level.getBlockState(pos).isAir()).count();
			helper.assertTrue(mined == 2, "30 mL should pay for two extra ores, not " + mined);
			helper.assertTrue(Math.abs(blood(player) - 6.0D) < 1.0E-6D, "the vein did not stop with 6 mL left: " + blood(player));
		} finally {
			sweepDrops(level, lode.get(2));
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void aDryLivingPickaxeOpensNoScarredVein(GameTestHelper helper) {
		clear(helper, 10, 4, 3);
		ServerLevel level = helper.getLevel();
		List<BlockPos> lode = lode(helper, Blocks.IRON_ORE);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			player.setGameMode(GameType.SURVIVAL);
			bond(player, 2.0D, 0.0D);
			HemoCapabilityAccess.getScarState(player).setFungalScar(new ItemStack(ItemInit.talaromyces_minus.get()));
			player.setShiftKeyDown(true);
			player.setItemInHand(InteractionHand.MAIN_HAND, pickaxe(4));
			helper.assertTrue(player.gameMode.destroyBlock(lode.getFirst()), "the player could not break the first ore");
			helper.assertTrue(lode.stream().skip(1).noneMatch(pos -> level.getBlockState(pos).isAir()),
					"a dry Living pickaxe, digging like a bare hand, opened a scarred vein");
			helper.assertTrue(drops(level, lode.get(2), 4.0D, Items.RAW_IRON) == 0, "a dry Living pickaxe got raw iron");
			helper.assertTrue(blood(player) == 2.0D, "a block the player could not pay for was charged anyway");
		} finally {
			sweepDrops(level, lode.get(2));
		}
		helper.succeed();
	}

	// ── Hematic Hewing ──

	/** A stone wall three wide and three tall across x = 4, z 1 to 3, y 1 to 3. */
	private static List<BlockPos> wall(GameTestHelper helper) {
		List<BlockPos> wall = new ArrayList<>();
		for (int z = 1; z <= 3; z++) {
			for (int y = 1; y <= 3; y++) {
				BlockPos pos = helper.absolutePos(new BlockPos(4, y, z));
				helper.getLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
				wall.add(pos);
			}
		}
		return wall;
	}

	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void hewingCutsATunnelTwoTallTowardTheBody(GameTestHelper helper) {
		clear(helper, 6, 4, 4);
		ServerLevel level = helper.getLevel();
		wall(helper);
		BlockPos head = helper.absolutePos(new BlockPos(4, 2, 2));
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			select(player, "hematic_hewing", 2000.0D, 0.0D);
			aim(player, Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 1, 2))), Vec3.atCenterOf(head));
			double pulse = price(player, "hematic_hewing");
			project(player);
			helper.assertTrue(level.getBlockState(head).isAir() && level.getBlockState(head.below()).isAir(),
					"the tunnel did not take the struck block and the one at the feet");
			helper.assertTrue(level.getBlockState(head.above()).is(Blocks.STONE), "the tunnel was taller than two");
			helper.assertTrue(level.getBlockState(head.north()).is(Blocks.STONE), "the tunnel was wider than one");
			helper.assertTrue(Math.abs(2000.0D - blood(player) - pulse) < 1.0E-6D, "one cut did not cost one pulse");
		} finally {
			sweepDrops(level, head);
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void hewingSneakingCutsAFaceButNeverABlockEntity(GameTestHelper helper) {
		clear(helper, 6, 4, 4);
		ServerLevel level = helper.getLevel();
		List<BlockPos> wall = wall(helper);
		BlockPos centre = helper.absolutePos(new BlockPos(4, 2, 2));
		BlockPos chest = helper.absolutePos(new BlockPos(4, 1, 1));
		level.setBlock(chest, Blocks.CHEST.defaultBlockState(), 3);
		BlockPos bedrock = helper.absolutePos(new BlockPos(4, 3, 3));
		level.setBlock(bedrock, Blocks.BEDROCK.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			select(player, "hematic_hewing", 2000.0D, 0.0D);
			player.setShiftKeyDown(true);
			aim(player, Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 1, 2))), Vec3.atCenterOf(centre));
			project(player);
			helper.assertTrue(level.getBlockState(chest).is(Blocks.CHEST), "the cut broke a chest");
			helper.assertTrue(level.getBlockState(bedrock).is(Blocks.BEDROCK), "the cut broke bedrock");
			long cut = wall.stream().filter(pos -> level.getBlockState(pos).isAir()).count();
			helper.assertTrue(cut == 7, "a sneaking cut took " + cut + " of the seven stone blocks in the face");
		} finally {
			sweepDrops(level, centre);
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void hewingRefusesBeforeAnyBloodWhereThePlayerMayNotBuild(GameTestHelper helper) {
		clear(helper, 6, 4, 4);
		ServerLevel level = helper.getLevel();
		List<BlockPos> wall = wall(helper);
		BlockPos head = helper.absolutePos(new BlockPos(4, 2, 2));
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			select(player, "hematic_hewing", 2000.0D, 0.0D);
			player.setGameMode(GameType.ADVENTURE);
			aim(player, Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 1, 2))), Vec3.atCenterOf(head));
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isPresent(),
					"Hewing did not claim the stone, so the guard was never reached");
			float[] temper = HemoCapabilityAccess.getBloodTemper(player).snapshot();
			project(player);
			helper.assertTrue(blood(player) == 2000.0D, "a refused cut was paid for: " + blood(player));
			helper.assertTrue(java.util.Arrays.equals(temper, HemoCapabilityAccess.getBloodTemper(player).snapshot()),
					"a refused cut spent Temper");
			helper.assertTrue(captured.said().contains(REFUSED), "the refusal was silent: " + captured.said());
			helper.assertTrue(wall.stream().noneMatch(pos -> level.getBlockState(pos).isAir()),
					"Hewing cut stone for a player who may not build");
		} finally {
			sweepDrops(level, head);
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void hewingTakesOnlyWhatTheStaffsTierCanHarvest(GameTestHelper helper) {
		clear(helper, 6, 4, 4);
		ServerLevel level = helper.getLevel();
		BlockPos obsidian = helper.absolutePos(new BlockPos(4, 2, 2));
		level.setBlock(obsidian, Blocks.OBSIDIAN.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			select(player, "hematic_hewing", 2000.0D, 0.0D);
			aim(player, Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 1, 2))), Vec3.atCenterOf(obsidian));
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isEmpty(),
					"Hewing claimed obsidian before the fourth ring");
			HemoCapabilityAccess.getLivingStaffProgress(player).setBloodHandled(LivingStaffGrowthRules.threshold(4));
			project(player);
			helper.assertTrue(level.getBlockState(obsidian).isAir(), "four rings could not hew obsidian");
			helper.assertTrue(drops(level, obsidian, 2.0D, Items.OBSIDIAN) == 1, "the hewn obsidian dropped nothing");
		} finally {
			sweepDrops(level, obsidian);
		}
		helper.succeed();
	}
}
