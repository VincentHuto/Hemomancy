package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.world.chum.ChumPools;
import com.vincenthuto.hemomancy.common.world.chum.ChumRules;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Core-loop Track 7, chum: plain Projection at a water surface pours a pool that grows with the blood and is paid by
 * the tick; a catalyst in the off hand colours it once; a pool fades and is forgotten; a catch inside a pool can be one
 * of its own (by school when coloured) and never outside one; a hook in a pool is lured faster; a strong deep pool
 * draws fish, up to its cap.
 */
@GameTestHolder("chum_validation")
@PrefixGameTestTemplate(false)
public final class ChumGameTests {
	private static final String EMPTY = "empty";
	private static final Set<Item> PLAIN_CATCHES = Set.of(ItemInit.raw_pelagic_herring.get(), ItemInit.erythrocoral_fragment.get(),
			ItemInit.swollen_leech.get());

	private ChumGameTests() {
	}

	/** A walled pond of still water four deep over x 2..12, z 2..12; returns the middle of its surface. */
	private static Vec3 pond(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (int x = 1; x <= 13; x++) {
			for (int z = 1; z <= 13; z++) {
				boolean wall = x == 1 || x == 13 || z == 1 || z == 13;
				level.setBlock(helper.absolutePos(new BlockPos(x, 0, z)), Blocks.STONE.defaultBlockState(), 3);
				for (int y = 1; y <= 4; y++) {
					level.setBlock(helper.absolutePos(new BlockPos(x, y, z)), wall ? Blocks.STONE.defaultBlockState()
							: Blocks.WATER.defaultBlockState(), 3);
				}
				for (int y = 5; y <= 10; y++) level.setBlock(helper.absolutePos(new BlockPos(x, y, z)), Blocks.AIR.defaultBlockState(), 3);
			}
		}
		BlockPos top = helper.absolutePos(new BlockPos(7, 4, 7));
		return new Vec3(top.getX() + 0.5D, top.getY() + level.getFluidState(top).getHeight(level, top), top.getZ() + 0.5D);
	}

	private static void prepare(ServerPlayer player, double blood) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(5_000.0D);
		volume.setBloodVolume(blood);
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.setSelectedManip(BloodManipulation.BLANK);
	}

	/** The player just above the pond, three blocks from the middle, looking at {@code at} on the surface. */
	private static void standAndAim(GameTestHelper helper, ServerPlayer player, Vec3 at) {
		Vec3 feet = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 5, 7)));
		player.setPos(feet.x, feet.y, feet.z);
		player.lookAt(EntityAnchorArgument.Anchor.EYES, at);
		player.setOldPosAndRot();
		player.yHeadRotO = player.getYHeadRot();
	}

	private static double project(ServerPlayer player) {
		return BloodProjectionItem.projectFromEntity(player.level(), player, 5.0D, 100.0D);
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void projectingIntoWaterPoursAPoolThatGrowsWithTheBlood(GameTestHelper helper) {
		Vec3 surface = pond(helper);
		ServerLevel level = helper.getLevel();
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, 1_000.0D);
			standAndAim(helper, player, surface);
			for (int i = 0; i < 25; i++) project(player);
			ChumPools.Pool pool = ChumPools.at(level, surface);
			helper.assertTrue(pool != null, "no pool where blood was poured into the water");
			helper.assertTrue(Math.abs(pool.ml() - 25 * ChumRules.ML_PER_TICK) < 1.0E-6D, "the pool holds " + pool.ml() + " mL");
			helper.assertTrue(Math.abs(pool.radius() - ChumRules.radius(50.0D)) < 1.0E-6D, "the radius is not the blood's");
			double paid = 1_000.0D - HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
			helper.assertTrue(Math.abs(paid - 50.0D) < 1.0E-6D, "the pour cost " + paid + " mL");
			helper.assertTrue(level.getBlockState(BlockPos.containing(surface).above()).isAir(), "chum placed a block");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aCatalystColoursThePoolOnce(GameTestHelper helper) {
		Vec3 surface = pond(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, 1_000.0D);
			player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ItemInit.incandescent_enzyme.get(), 3));
			standAndAim(helper, player, surface);
			for (int i = 0; i < 6; i++) project(player);
			ChumPools.Pool pool = ChumPools.at(helper.getLevel(), surface);
			helper.assertTrue(pool != null && pool.school() == EnumBloodTemper.LUX, "the enzyme did not colour the pool");
			helper.assertTrue(player.getOffhandItem().getCount() == 2, "the pool took " + (3 - player.getOffhandItem().getCount())
					+ " enzymes for one colouring");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aPoolFadesAndIsForgotten(GameTestHelper helper) {
		Vec3 surface = pond(helper);
		ServerLevel level = helper.getLevel();
		ChumPools.pour(level, surface, 400.0D, null);
		ChumPools.backdate(level, surface, ChumRules.HALF_LIFE_TICKS);
		ChumPools.Pool pool = ChumPools.at(level, surface);
		helper.assertTrue(pool != null && Math.abs(pool.ml() - 200.0D) < 1.0E-6D, "a minute did not halve the pool");
		ChumPools.backdate(level, surface, ChumRules.HALF_LIFE_TICKS * 8);
		helper.assertTrue(ChumPools.at(level, surface) == null, "a faded pool was not forgotten");
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aPoolGrowsFromTickToTick(GameTestHelper helper) {
		Vec3 surface = pond(helper);
		ServerLevel level = helper.getLevel();
		ChumPools.pour(level, surface, ChumRules.ML_PER_TICK, null);
		helper.runAfterDelay(1, () -> {
			ChumPools.pour(level, surface, ChumRules.ML_PER_TICK, null);
			helper.runAfterDelay(1, () -> {
				ChumPools.Pool pool = ChumPools.at(level, surface);
				helper.assertTrue(pool != null && pool.ml() > 1.9D * ChumRules.ML_PER_TICK,
						"a held pour did not build a pool from tick to tick: " + (pool == null ? "none" : pool.ml() + " mL"));
				helper.succeed();
			});
		});
	}

	private static List<ItemStack> fish(ServerLevel level, Vec3 origin, int casts) {
		LootTable table = level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING);
		List<ItemStack> caught = new ArrayList<>();
		for (int i = 0; i < casts; i++) {
			LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, origin)
					.withParameter(LootContextParams.TOOL, new ItemStack(Items.FISHING_ROD)).create(LootContextParamSets.FISHING);
			caught.addAll(table.getRandomItems(params));
		}
		return caught;
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void aCatchInAPoolCanBeItsOwnAndNeverOutside(GameTestHelper helper) {
		Vec3 surface = pond(helper);
		ServerLevel level = helper.getLevel();
		Vec3 hook = surface.add(0.0D, -0.2D, 0.0D);
		List<ItemStack> outside = fish(level, hook, 200);
		helper.assertTrue(outside.stream().noneMatch(stack -> PLAIN_CATCHES.contains(stack.getItem())),
				"a chum catch came up with no pool");
		ChumPools.pour(level, surface, ChumRules.FULL_ML, null);
		long own = fish(level, hook, 400).stream().filter(stack -> PLAIN_CATCHES.contains(stack.getItem())).count();
		helper.assertTrue(own >= 60 && own <= 140, own + " of 400 catches in a full pool were its own; expected about 100");
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void aColouredPoolCatchesFromItsSchool(GameTestHelper helper) {
		Vec3 surface = pond(helper);
		ServerLevel level = helper.getLevel();
		ChumPools.pour(level, surface, ChumRules.FULL_ML, EnumBloodTemper.LUX);
		List<ItemStack> caught = fish(level, surface.add(0.0D, -0.2D, 0.0D), 300);
		long chromatophores = caught.stream().filter(stack -> stack.is(ItemInit.cuttlefish_chromatophores.get())).count();
		helper.assertTrue(chromatophores > 0, "a Lux pool never gave its own catch");
		helper.assertTrue(caught.stream().noneMatch(stack -> stack.is(ItemInit.erythrocoral_fragment.get())),
				"a Lux pool caught from the plain table");
		helper.succeed();
	}

	private static int timeUntilLured(FishingHook hook) {
		try {
			Field field = FishingHook.class.getDeclaredField("timeUntilLured");
			field.setAccessible(true);
			return field.getInt(hook);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("cannot read the hook's lure", e);
		}
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void aHookInAPoolIsLuredFaster(GameTestHelper helper) {
		Vec3 surface = pond(helper);
		ServerLevel level = helper.getLevel();
		ChumPools.pour(level, surface, ChumRules.FULL_ML, null);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			standAndAim(helper, player, surface);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FISHING_ROD));
			FishingHook hook = new FishingHook(player, level, 0, 0);
			hook.setPos(surface.x, surface.y - 0.3D, surface.z);
			hook.setDeltaMovement(Vec3.ZERO);
			int start = 0;
			for (int i = 0; i < 60 && start <= 60; i++) {
				hook.tick();
				hook.setPos(surface.x, surface.y - 0.3D, surface.z);
				start = timeUntilLured(hook);
			}
			helper.assertTrue(start > 60, "the hook never began to lure (" + start + ")");
			for (int i = 0; i < 8; i++) {
				hook.tick();
				hook.setPos(surface.x, surface.y - 0.3D, surface.z);
			}
			int pulled = start - timeUntilLured(hook);
			// Vanilla counts at most 2 a tick; a full pool adds 5.
			helper.assertTrue(pulled >= Math.min(start - 20, 8 * (1 + ChumRules.extraLurePerTick(1.0D)) - 8),
					"the lure fell only " + pulled + " in 8 ticks inside a full pool");
			hook.discard();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aStrongDeepPoolDrawsFishUpToItsCap(GameTestHelper helper) {
		Vec3 surface = pond(helper);
		ServerLevel level = helper.getLevel();
		ChumPools.pour(level, surface, ChumRules.FULL_ML, null);
		ChumPools.Pool pool = ChumPools.at(level, surface);
		List<Entity> drawn = new ArrayList<>();
		for (int i = 0; i < ChumRules.DRAW_CAP + 2; i++) {
			Entity fish = ChumPools.draw(level, pool, level.random);
			if (fish != null) drawn.add(fish);
		}
		long near = level.getEntities(EntityInit.pelagic_herring.get(), new AABB(surface, surface).inflate(13.0D, 5.0D, 13.0D), e -> true).size();
		helper.assertTrue(drawn.size() == ChumRules.DRAW_CAP && near == ChumRules.DRAW_CAP,
				"a strong pool drew " + drawn.size() + " fish (" + near + " near); the cap is " + ChumRules.DRAW_CAP);
		drawn.forEach(Entity::discard);
		ChumPools.backdate(level, surface, ChumRules.HALF_LIFE_TICKS * 2);
		helper.assertTrue(ChumPools.draw(level, ChumPools.at(level, surface), level.random) == null, "a weak pool drew a fish");
		helper.succeed();
	}

	/**
	 * Round two P9: landing one of a pool's own catches is "Something Bit" for the fisher whose blood is awake. Vanilla's
	 * hook names its owner as the attacking entity; a fisher whose blood sleeps earns nothing from the same pool.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void aChumCatchIsTheAwakeFishersFirst(GameTestHelper helper) {
		Vec3 surface = pond(helper);
		ServerLevel level = helper.getLevel();
		Vec3 hook = surface.add(0.0D, -0.2D, 0.0D);
		ChumPools.pour(level, surface, ChumRules.FULL_ML, null);
		LootTable table = level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING);
		try (CapturedPlayer sleeping = CapturedPlayer.create(helper); CapturedPlayer awake = CapturedPlayer.create(helper)) {
			HemoCapabilityAccess.getBloodVolume(sleeping.player).setActive(false);
			HemoCapabilityAccess.getBloodVolume(awake.player).setActive(true);
			long own = 0;
			for (int cast = 0; cast < 200; cast++) {
				own += table.getRandomItems(cast(level, hook, sleeping.player)).stream()
						.filter(stack -> PLAIN_CATCHES.contains(stack.getItem())).count();
			}
			helper.assertTrue(own > 0, "two hundred casts in a full pool landed none of its catches");
			helper.assertTrue(!com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(sleeping.player,
					com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_CHUM_CATCH), "a fisher whose blood sleeps earned Something Bit");
			for (int cast = 0; cast < 200; cast++) table.getRandomItems(cast(level, hook, awake.player));
			helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(awake.player,
					com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_CHUM_CATCH), "a chum catch did not grant Something Bit");
		}
		helper.succeed();
	}

	private static LootParams cast(ServerLevel level, Vec3 origin, ServerPlayer fisher) {
		return new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, origin)
				.withParameter(LootContextParams.TOOL, new ItemStack(Items.FISHING_ROD))
				.withParameter(LootContextParams.ATTACKING_ENTITY, fisher).create(LootContextParamSets.FISHING);
	}
}
