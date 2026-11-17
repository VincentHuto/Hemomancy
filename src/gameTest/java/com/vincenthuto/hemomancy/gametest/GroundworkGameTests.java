package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.VeinMinerHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillOrigin;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillPhase;
import com.vincenthuto.hemomancy.common.entity.summon.BloodThrallEntity;
import com.vincenthuto.hemomancy.common.event.BloodInfusionManager;
import com.vincenthuto.hemomancy.common.event.BloodInfusionRules;
import com.vincenthuto.hemomancy.common.event.SanguineCondenserProjection;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenCaptureRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarBlockItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionChannelRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.manipulation.HematicCommandManager;
import com.vincenthuto.hemomancy.common.manipulation.HematicCommandState;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.TemporaryIceManager;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.TemporaryIceSavedData;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodTemperamentServerPacket;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineCondenserBlockEntity;
import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Core-loop Track 0: the groundwork fixes, each checked in a running world. Block edits a player causes go through
 * {@code PlayerBlockEdits}; the bugs listed in {@code docs/archive/tracks/core_loop/00a-groundwork-cleanup.md} each have a case here
 * (or a unit test where no world is needed).
 */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GroundworkGameTests {
	private static final String EMPTY = "bastion/mobs/empty";

	private GroundworkGameTests() {
	}

	// ── PlayerBlockEdits ──

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void breakBlockDropsOnlyForTheRightTool(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos picked = helper.absolutePos(new BlockPos(1, 1, 1));
		BlockPos bare = helper.absolutePos(new BlockPos(5, 1, 1));
		level.setBlock(picked, Blocks.STONE.defaultBlockState(), 3);
		level.setBlock(bare, Blocks.STONE.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			helper.assertTrue(PlayerBlockEdits.breakBlock(player, picked, new ItemStack(Items.IRON_PICKAXE)),
					"a survival player could not break stone with a pickaxe");
			helper.assertTrue(PlayerBlockEdits.breakBlock(player, bare, ItemStack.EMPTY),
					"a survival player could not break stone by hand");
			helper.assertTrue(level.getBlockState(picked).isAir() && level.getBlockState(bare).isAir(),
					"a broken block was left standing");
			helper.assertTrue(drops(level, picked, Items.COBBLESTONE) == 1, "the pickaxe break did not drop cobblestone");
			helper.assertTrue(drops(level, bare, Items.COBBLESTONE) == 0, "stone broken by hand dropped cobblestone");

			BlockPos guarded = helper.absolutePos(new BlockPos(1, 1, 5));
			level.setBlock(guarded, Blocks.STONE.defaultBlockState(), 3);
			player.setGameMode(GameType.ADVENTURE);
			helper.assertTrue(!PlayerBlockEdits.breakBlock(player, guarded, new ItemStack(Items.IRON_PICKAXE)),
					"an adventure-mode player broke a block");
			helper.assertTrue(level.getBlockState(guarded).is(Blocks.STONE), "a refused break still removed the block");
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void replaceConvertsInPlaceAndRefusesWhereThePlayerMayNotBuild(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
		level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			helper.assertTrue(PlayerBlockEdits.replace(captured.player, pos, Blocks.COBBLESTONE.defaultBlockState()),
					"a survival player could not convert stone");
			helper.assertTrue(level.getBlockState(pos).is(Blocks.COBBLESTONE), "the conversion did not land");
			helper.assertTrue(drops(level, pos, Items.COBBLESTONE) == 0 && drops(level, pos, Items.STONE) == 0,
					"an in-place conversion dropped the old block");
			captured.player.setGameMode(GameType.ADVENTURE);
			helper.assertTrue(!PlayerBlockEdits.replace(captured.player, pos, Blocks.STONE.defaultBlockState()),
					"an adventure-mode player converted a block");
			helper.assertTrue(level.getBlockState(pos).is(Blocks.COBBLESTONE), "a refused conversion changed the block");
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void liftTakesABlockWithoutDropsAndSettlePutsItBack(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos dirt = helper.absolutePos(new BlockPos(1, 1, 1));
		BlockPos chest = helper.absolutePos(new BlockPos(5, 1, 1));
		BlockPos stone = helper.absolutePos(new BlockPos(1, 1, 5));
		level.setBlock(dirt, Blocks.DIRT.defaultBlockState(), 3);
		level.setBlock(chest, Blocks.CHEST.defaultBlockState(), 3);
		level.setBlock(stone, Blocks.STONE.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			BlockState lifted = PlayerBlockEdits.lift(player, dirt);
			helper.assertTrue(lifted != null && lifted.is(Blocks.DIRT), "lift did not hand back the dirt it took");
			helper.assertTrue(level.getBlockState(dirt).isAir(), "lift left the block in the world");
			helper.assertTrue(drops(level, dirt, Items.DIRT) == 0, "lift dropped the block");
			helper.assertTrue(PlayerBlockEdits.lift(player, chest) == null, "lift took a block entity and its contents");
			helper.assertTrue(level.getBlockState(chest).is(Blocks.CHEST), "a refused lift removed the chest");
			helper.assertTrue(!PlayerBlockEdits.settle(player, stone, lifted), "settle overwrote a solid block");
			helper.assertTrue(PlayerBlockEdits.settle(player, dirt, lifted), "settle could not put the dirt back");
			helper.assertTrue(level.getBlockState(dirt).is(Blocks.DIRT), "the settled dirt is not there");
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void aBreakCheckNeverStartsAVein(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos first = helper.absolutePos(new BlockPos(1, 1, 1));
		BlockPos second = first.east();
		level.setBlock(first, Blocks.IRON_ORE.defaultBlockState(), 3);
		level.setBlock(second, Blocks.IRON_ORE.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			HemoCapabilityAccess.getBloodVolume(player).setBloodVolume(
					Math.max(100.0D, HemoCapabilityAccess.getBloodVolume(player).getBloodVolume()));
			HemoCapabilityAccess.getScarState(player).setFungalScar(new ItemStack(ItemInit.talaromyces_minus.get()));
			player.setShiftKeyDown(true);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
			helper.assertTrue(PlayerBlockEdits.mayBreak(player, first), "a survival player may not break iron ore");
			helper.assertTrue(level.getBlockState(first).is(Blocks.IRON_ORE)
					&& level.getBlockState(second).is(Blocks.IRON_ORE), "a break check vein-mined the ore");
			helper.assertTrue(!PlayerBlockEdits.isCheckingBreak(), "the break check did not end");

			// The contrast: the same player's real break event does start the vein.
			net.neoforged.neoforge.common.CommonHooks.fireBlockBreak(level, GameType.SURVIVAL, player, first,
					level.getBlockState(first));
			helper.assertTrue(level.getBlockState(second).isAir(), "a real break by a sneaking scarred player no longer vein-mines");
		} finally {
			level.setBlock(first, Blocks.AIR.defaultBlockState(), 3);
			level.setBlock(second, Blocks.AIR.defaultBlockState(), 3);
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void infusionDoesNotStartWhereThePlayerMayNotBuild(GameTestHelper helper) {
		BloodInfusionManager.clear();
		ServerLevel level = helper.getLevel();
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
		level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(100.0D);
			player.setGameMode(GameType.ADVENTURE);
			for (int i = 0; i < 10; i++) BloodInfusionManager.feedBlock(player, level, pos, 5.0D);
			for (int i = 0; i < BloodInfusionRules.COLLAPSE_TICKS + 1; i++) BloodInfusionManager.tick(level);
			helper.assertTrue(blood.getBloodVolume() == 100.0D, "a refused infusion still took blood");
			helper.assertTrue(level.getBlockState(pos).is(Blocks.STONE), "a refused infusion collapsed the stone");
		} finally {
			BloodInfusionManager.clear();
		}
		helper.succeed();
	}

	// ── Bugs ──

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void specimenJarLiftsARiderOffInsteadOfDeletingIt(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Minecart cart = helper.spawn(EntityType.MINECART, open(helper, new BlockPos(1, 1, 1)));
		Mob doe = helper.spawn(EntityInit.crimson_doe.get(), open(helper, new BlockPos(1, 1, 1)));
		doe.startRiding(cart, true);
		helper.assertTrue(doe.isPassenger(), "the doe did not mount the cart");
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			ItemStack jar = new ItemStack(BlockInit.specimen_jar.get());
			player.setItemInHand(InteractionHand.MAIN_HAND, jar);
			jar.getItem().interactLivingEntity(jar, player, doe, InteractionHand.MAIN_HAND);
			ItemStack held = player.getMainHandItem();
			helper.assertTrue(SpecimenJarData.hasSpecimen(held), "the jar did not take the riding doe");
			helper.assertTrue(SpecimenJarData.getSpecimenEntityId(held)
					.filter(id -> id.equals(Hemomancy.rloc("crimson_doe"))).isPresent(), "the jar holds something else");
			helper.assertTrue(doe.isRemoved() && !cart.isRemoved(), "the capture removed the wrong entity");
		} finally {
			cart.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void everyCapturableKindFitsTheJarAtItsNormalSize(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, Hemomancy.rloc("specimen_jar_capturable"));
		List<String> refused = new ArrayList<>();
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
				if (!type.is(tag) || !(type.create(level) instanceof LivingEntity living)) continue;
				SpecimenCaptureRules.Verdict verdict = SpecimenCaptureRules.judge(
						SpecimenJarBlockItem.candidate(living, captured.player));
				if (verdict != SpecimenCaptureRules.Verdict.CAPTURE) {
					refused.add(BuiltInRegistries.ENTITY_TYPE.getKey(type) + " " + verdict);
				}
				living.discard();
			}
		}
		helper.assertTrue(refused.isEmpty(), "tagged specimens the jar refuses: " + refused);
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void shearsDoNotCarveAGourd(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
		level.setBlock(pos, BlockInit.gourd.get().defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ItemStack shears = new ItemStack(Items.SHEARS);
			captured.player.setItemInHand(InteractionHand.MAIN_HAND, shears);
			ItemInteractionResult result = level.getBlockState(pos).useItemOn(shears, level, captured.player,
					InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false));
			helper.assertTrue(result == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION,
					"shears still act on a gourd: " + result);
			helper.assertTrue(level.getBlockState(pos).is(BlockInit.gourd.get()), "shears turned the gourd into something else");
			helper.assertTrue(drops(level, pos, Items.PUMPKIN_SEEDS) == 0, "shearing a gourd dropped pumpkin seeds");
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 20)
	public static void livingToolsGetDropsFromTheirOwnBlocks(GameTestHelper helper) {
		ItemStack blade = new ItemStack(ItemInit.living_blade.get());
		ItemStack axe = new ItemStack(ItemInit.living_axe.get());
		helper.assertTrue(blade.isCorrectToolForDrops(Blocks.COBWEB.defaultBlockState()),
				"a Living Blade does not get drops from cobweb");
		helper.assertTrue(!blade.isCorrectToolForDrops(Blocks.STONE.defaultBlockState()),
				"a Living Blade counts as a pickaxe");
		helper.assertTrue(axe.isCorrectToolForDrops(Blocks.OAK_LOG.defaultBlockState()),
				"a Living Axe does not count as an axe");
		helper.assertTrue(axe.getDestroySpeed(Blocks.OAK_LOG.defaultBlockState()) > 1.0F,
				"a Living Axe is slow on wood");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 20)
	public static void veinMiningNeverMinesPastTheToolsTier(GameTestHelper helper) {
		BlockState diamond = Blocks.DIAMOND_ORE.defaultBlockState();
		helper.assertTrue(!VeinMinerHelper.canHarvest(diamond, new ItemStack(Items.WOODEN_PICKAXE)),
				"a wooden pickaxe may vein-mine diamond ore");
		helper.assertTrue(VeinMinerHelper.canHarvest(diamond, new ItemStack(Items.IRON_PICKAXE)),
				"an iron pickaxe may not vein-mine diamond ore");
		helper.assertTrue(VeinMinerHelper.canHarvest(Blocks.DIRT.defaultBlockState(), ItemStack.EMPTY),
				"a block that needs no tool is refused");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void willTemperamentShiftsReachTheClient(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			WillEntity will = helper.spawn(EntityInit.will.get(), open(helper, new BlockPos(1, 1, 1)));
			will.configure(WillOrigin.BROKEN, EnumBloodTemper.LUX, 1, null, false);
			will.setPhase(WillPhase.FALTERING);
			float before = HemoCapabilityAccess.getBloodTemperament(player).getAlignmentByTemper(EnumBloodTemper.LUX);
			helper.assertTrue(will.projectBanishWithBlood(player), "the faltering Will could not be banished");
			float after = HemoCapabilityAccess.getBloodTemperament(player).getAlignmentByTemper(EnumBloodTemper.LUX);
			helper.assertTrue(after == before - 3.0F, "banishment did not move Lux by -3: " + before + " to " + after);
			helper.assertTrue(!captured.sent(BloodTemperamentServerPacket.class).isEmpty(),
					"the banishment's Temperament shift was never sent to the player");
			will.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void aDrawKillGivesNoKillShift(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(10.0D);
			var temperament = HemoCapabilityAccess.getBloodTemperament(player);
			float before = temperament.getAlignmentByTemper(EnumBloodTemper.ANIMUS);
			Pig drained = helper.spawn(EntityType.PIG, open(helper, new BlockPos(1, 1, 1)));
			drained.setHealth(0.5F);
			double absorbed = BloodAbsorptionItem.absorbFromTarget(helper.getLevel(), player, drained, 2.0D);
			helper.assertTrue(absorbed > 0.0D && !drained.isAlive(), "the draw did not kill the pig");
			helper.assertTrue(temperament.getAlignmentByTemper(EnumBloodTemper.ANIMUS) == before,
					"a kill by draw also paid a kill shift; the drained creature was counted twice");

			Pig struck = helper.spawn(EntityType.PIG, open(helper, new BlockPos(3, 1, 1)));
			struck.hurt(player.damageSources().playerAttack(player), 100.0F);
			helper.assertTrue(temperament.getAlignmentByTemper(EnumBloodTemper.ANIMUS) > before,
					"an ordinary kill no longer shifts Temperament (the contrast case)");
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void hematicCommandIsSavedWithTheMob(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			Zombie zombie = helper.spawn(EntityType.ZOMBIE, open(helper, new BlockPos(1, 1, 1)));
			helper.assertTrue(HematicCommandManager.rebuke(captured.player, zombie), "Rebuke refused a zombie");
			CompoundTag saved = zombie.saveWithoutId(new CompoundTag());
			Zombie reloaded = EntityType.ZOMBIE.create(helper.getLevel());
			reloaded.load(saved);
			HematicCommandState.Command command = HemoCapabilityAccess.getHematicCommand(reloaded);
			helper.assertTrue(command != null && command.mode() == HematicCommandState.Mode.REBUKED
					&& command.caster().equals(captured.player.getUUID()), "the saved zombie lost its Rebuke");
			zombie.discard();
			helper.assertTrue(HemoCapabilityAccess.getHematicCommand(zombie) == null,
					"a discarded body kept its command");
			reloaded.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 60)
	public static void glacialIceIsSavedWithTheLevelAndStillMelts(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		helper.assertTrue(TemporaryIceManager.place(level, pos, BlockInit.frozen_cruor.get().defaultBlockState(), 10),
				"the ice was not placed");
		TemporaryIceSavedData data = TemporaryIceSavedData.get(level);
		helper.assertTrue(data.get(pos) != null, "the ice is not recorded with the level");
		TemporaryIceSavedData reloaded = TemporaryIceSavedData.load(
				data.save(new CompoundTag(), level.registryAccess()), level.registryAccess());
		helper.assertTrue(reloaded.get(pos) != null && reloaded.get(pos).meltsAt() == data.get(pos).meltsAt(),
				"the ice did not survive a save and load");
		helper.runAfterDelay(20, () -> {
			helper.assertTrue(level.getBlockState(pos).isAir(), "the ice did not melt");
			helper.assertTrue(TemporaryIceSavedData.get(level).get(pos) == null, "melted ice stayed on the record");
			helper.succeed();
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void thrallReabsorbIsLedgeredIncome(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(100.0D);
			BlockPos at = helper.absolutePos(open(helper, new BlockPos(1, 1, 1)));
			BloodThrallEntity thrall = new BloodThrallEntity(helper.getLevel(), player, at, at);
			thrall.moveTo(Vec3.atBottomCenterOf(at));
			helper.getLevel().addFreshEntity(thrall);
			thrall.interact(player, InteractionHand.MAIN_HAND);
			helper.assertTrue(blood.getBloodVolume() > 100.0D, "reabsorbing the thrall returned no blood");
			helper.assertTrue(BloodFlowLedger.collect(player, blood).contribution("thrall_reabsorb").isPresent(),
					"the flow ledger never saw the thrall's blood come back");
			thrall.discard();
		}
		helper.succeed();
	}

	// ── The verb items ──

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 120)
	public static void absorptionPulsesFasterThanTheHurtCooldownLandLessOften(GameTestHelper helper) {
		// The measurement behind the cadence fix: an 8-tick pulse of the same size lands every 16 ticks.
		Pig measured = helper.spawn(EntityType.PIG, open(helper, new BlockPos(1, 1, 1)));
		measured.setNoAi(true);
		AtomicInteger landed8 = new AtomicInteger();
		List<String> trace = new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			long at = 2 + i * 8L;
			helper.runAtTickTime(at, () -> {
				int cooldown = measured.invulnerableTime;
				boolean landed = measured.hurt(helper.getLevel().damageSources().generic(), 0.5F);
				if (landed) landed8.incrementAndGet();
				trace.add(at + ":" + cooldown + (landed ? "+" : "-"));
			});
		}
		// The pulse the rules now use lands every time, through the real drain.
		Pig drained = helper.spawn(EntityType.PIG, open(helper, new BlockPos(3, 1, 1)));
		drained.setNoAi(true);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(captured.player);
		blood.setActive(true);
		blood.setBloodVolume(10.0D);
		int interval = BloodAbsorptionChannelRules.livingTargetPulseInterval();
		AtomicInteger landedRule = new AtomicInteger();
		for (int i = 0; i < 8; i++) {
			helper.runAtTickTime(2 + (long) i * interval, () -> {
				if (BloodAbsorptionItem.absorbFromTarget(helper.getLevel(), captured.player, drained, 0.5D) > 0.0D)
					landedRule.incrementAndGet();
			});
		}
		helper.runAtTickTime(4 + 8L * Math.max(8, interval), () -> {
			try {
				helper.assertTrue(landed8.get() == 4, "an 8-tick pulse landed " + landed8.get() + " of 8 times, not 4 " + trace);
				helper.assertTrue(landedRule.get() == 8,
						"the " + interval + "-tick drain pulse landed " + landedRule.get() + " of 8 times");
			} finally {
				captured.close();
				measured.discard();
				drained.discard();
			}
			helper.succeed();
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 20)
	public static void aDrainingPulseBuildsStrain(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			BloodAbsorptionItem.updateChannelStrain(captured.player, true);
			helper.assertTrue(BloodAbsorptionItem.strainTicks(captured.player)
					== BloodAbsorptionChannelRules.LIVING_TARGET_PULSE_INTERVAL,
					"a draining pulse did not count its interval as strain: " + BloodAbsorptionItem.strainTicks(captured.player));
			BloodAbsorptionItem.logout(captured.player);
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void theCondenserTakesOnlyWhatThePlayerPaid(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
		level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(40.0D);
			double spent = SanguineCondenserProjection.tryProject(level, player, pos, 100.0D).orElse(-1.0D);
			helper.assertTrue(level.getBlockEntity(pos) instanceof SanguineCondenserBlockEntity,
					"projection did not seat a Condenser in the cauldron");
			SanguineCondenserBlockEntity condenser = (SanguineCondenserBlockEntity) level.getBlockEntity(pos);
			helper.assertTrue(spent == 40.0D && blood.getBloodVolume() == 0.0D,
					"the projection spent " + spent + " and left " + blood.getBloodVolume());
			helper.assertTrue(condenser.getBloodVolume() == spent, "the pool holds " + condenser.getBloodVolume()
					+ " for " + spent + " paid");
		}
		helper.succeed();
	}

	// ── Data ──

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 20)
	public static void everyBloodProfileNamesARealCreature(GameTestHelper helper) {
		List<ResourceLocation> orphans = new ArrayList<>();
		for (ResourceLocation id : BloodProfileData.snapshot(false).json().keySet()) {
			boolean ours = id.getNamespace().equals(Hemomancy.MOD_ID) || id.getNamespace().equals("minecraft");
			if (ours && !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) orphans.add(id);
		}
		helper.assertTrue(orphans.isEmpty(), "blood profiles for creatures that do not exist: " + orphans);
		helper.succeed();
	}

	/** Clears a standing space at {@code relative} (the empty template is not empty at every height) over stone. */
	private static BlockPos open(GameTestHelper helper, BlockPos relative) {
		helper.setBlock(relative.below(), Blocks.STONE);
		helper.setBlock(relative, Blocks.AIR);
		helper.setBlock(relative.above(), Blocks.AIR);
		return relative;
	}

	// ── Raw edits converted to PlayerBlockEdits (core-loop round two, P3 task H) ──

	/** Stands the player at {@code feet} looking at {@code at}, the old position and rotation following. */
	private static void look(ServerPlayer player, Vec3 feet, Vec3 at) {
		player.setPos(feet.x, feet.y, feet.z);
		player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, at);
		player.setOldPosAndRot();
		player.yHeadRotO = player.getYHeadRot();
	}

	/** Crimson Flame Conjuration sets its flames as the caster's placement: a claim's place event refuses them. */
	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void crimsonFlamesAreTheCastersPlacement(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos block = helper.absolutePos(new BlockPos(4, 1, 2));
		BlockPos fire = block.above();
		level.setBlock(block, Blocks.STONE.defaultBlockState(), 3);
		level.setBlock(fire, Blocks.AIR.defaultBlockState(), 3);
		level.setBlock(fire.above(), Blocks.AIR.defaultBlockState(), 3);
		var flame = com.vincenthuto.hemomancy.common.init.ManipulationInit.getByName("crimson_flame_conjuration");
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			look(player, Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 1, 2))), Vec3.atCenterOf(block).add(0.0D, 0.5D, 0.0D));
			java.util.function.Consumer<net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent> claim = event -> {
				if (event.getEntity() == player) event.setCanceled(true);
			};
			net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(claim);
			try {
				flame.getAction(player, level, ItemStack.EMPTY, player.blockPosition());
			} finally {
				net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(claim);
			}
			helper.assertTrue(!level.getBlockState(fire).is(BlockInit.crimson_flames.get()), "a claim's place event did not stop the flames");
			flame.getAction(player, level, ItemStack.EMPTY, player.blockPosition());
			helper.assertTrue(level.getBlockState(fire).is(BlockInit.crimson_flames.get()), "the flames did not take where nothing refused them");
		} finally {
			level.setBlock(fire, Blocks.AIR.defaultBlockState(), 3);
		}
		helper.succeed();
	}

	/** Furnace Veins melts ice as the caster's edit: a claim's break event keeps the ice. */
	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void furnaceVeinsMeltsIceAsTheCastersEdit(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos ice = helper.absolutePos(new BlockPos(4, 1, 4));
		level.setBlock(ice, Blocks.FROSTED_ICE.defaultBlockState(), 2);
		var veins = com.vincenthuto.hemomancy.common.init.ManipulationInit.getByName("furnace_veins");
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			look(player, Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(2, 1, 4))), Vec3.atCenterOf(ice));
			java.util.function.Consumer<net.neoforged.neoforge.event.level.BlockEvent.BreakEvent> claim = event -> {
				if (event.getPlayer() == player) event.setCanceled(true);
			};
			net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(claim);
			try {
				veins.getAction(player, level, ItemStack.EMPTY, player.blockPosition(), 1.0F);
			} finally {
				net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(claim);
			}
			helper.assertTrue(level.getBlockState(ice).is(Blocks.FROSTED_ICE), "a claim's break event did not keep the ice");
			veins.getAction(player, level, ItemStack.EMPTY, player.blockPosition(), 1.0F);
			helper.assertTrue(level.getBlockState(ice).isAir(), "Furnace Veins did not melt ice nothing protected");
		} finally {
			level.setBlock(ice, Blocks.AIR.defaultBlockState(), 2);
		}
		helper.succeed();
	}

	/** A Living Flail's impact freezes water as its thrower's edit: a thrower who may not build here freezes nothing. */
	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void aFlailImpactFreezesWaterOnlyWhereItsThrowerMayBuild(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos water = helper.absolutePos(new BlockPos(4, 1, 4));
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 1, 1))));
			for (GameType mode : List.of(GameType.ADVENTURE, GameType.SURVIVAL)) {
				level.setBlock(water, Blocks.WATER.defaultBlockState(), 2);
				player.setGameMode(mode);
				var head = new com.vincenthuto.hemomancy.common.entity.projectile.LivingFlailHeadProjectileEntity(level, player);
				head.configure(1.0F, InteractionHand.MAIN_HAND, EnumBloodTemper.CONGEATIO, null);
				com.vincenthuto.hemomancy.common.entity.projectile.LivingFlailImpactEffects.impact(level, head, Vec3.atCenterOf(water), false);
				boolean frozen = level.getBlockState(water).is(Blocks.ICE);
				helper.assertTrue(frozen == (mode == GameType.SURVIVAL), "a " + mode + " thrower's impact left the water "
						+ level.getBlockState(water));
			}
		} finally {
			for (BlockPos pos : BlockPos.betweenClosed(water.offset(-6, -1, -6), water.offset(6, 2, 6))) {
				BlockState state = level.getBlockState(pos);
				if (state.is(Blocks.SNOW) || state.is(Blocks.ICE) || state.is(Blocks.WATER)) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
			}
		}
		helper.succeed();
	}

	/** The Rite of the Silver Dawn cleanses blood-faction stone as its caster's edit: a caster who may not build cleanses nothing. */
	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void theSilverDawnCleansesOnlyWhereItsCasterMayBuild(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos center = helper.absolutePos(new BlockPos(4, 1, 4));
		BlockPos stone = center.east(2);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer caster = captured.player;
			caster.setPos(Vec3.atBottomCenterOf(center.west(2)));
			HemoCapabilityAccess.getUnstainedProgress(caster).setClarityUnlocked(true);
			for (GameType mode : List.of(GameType.ADVENTURE, GameType.SURVIVAL)) {
				level.setBlock(stone, BlockInit.venous_stone.get().defaultBlockState(), 3);
				caster.setGameMode(mode);
				helper.assertTrue(com.vincenthuto.hemomancy.common.rite.unstained.UnstainedCardinalRiteEvents.completeRite(level, caster,
						center, "cardinal_rite/silver_dawn"), "the Silver Dawn did not complete for a caster with Clarity");
				boolean cleansed = level.getBlockState(stone).is(BlockInit.cleansed_stone.get());
				helper.assertTrue(cleansed == (mode == GameType.SURVIVAL), "a " + mode + " caster's Silver Dawn left "
						+ level.getBlockState(stone));
			}
		} finally {
			level.setBlock(stone, Blocks.AIR.defaultBlockState(), 3);
		}
		helper.succeed();
	}

	private static long drops(ServerLevel level, BlockPos pos, net.minecraft.world.item.Item item) {
		return level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1.0D),
				entity -> entity.getItem().is(item)).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
	}
}
