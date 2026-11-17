package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.block.harbinger.BlockBloodInteractions;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenVatBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.VatPart;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperCharge;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperRules;
import com.vincenthuto.hemomancy.common.entity.menagerie.AnimalTaint;
import com.vincenthuto.hemomancy.common.entity.menagerie.AnimalTaintState;
import com.vincenthuto.hemomancy.common.entity.menagerie.MenagerieLeash;
import com.vincenthuto.hemomancy.common.entity.menagerie.MenagerieRules;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.mission.alchemist.BloodResearch;
import com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodKnowledge;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineVigilBlockEntity;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSyringeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.VialRackItem;
import com.vincenthuto.hemomancy.common.menu.tile.functional.SpecimenVesselMenu;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.BroodRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.KeptSpecimen;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenBrood;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.VesselView;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVatBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVessels;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.VatAssembly;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.VatVigorRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Core-loop Track 5, the menagerie: a weakened or passive creature caught on the Tendon Line is
 * leashed, follows its keeper, never despawns, costs upkeep and breaks free unpaid; one rule refuses people, pets and
 * bosses; a led creature goes into a vat, lives on its buffer as Vigor, gives its schools' Temper to held Absorption
 * (cut by acclimation), starves to a husk, rattles and breaks out; a Sanguine Vigil keeps it from fading; sampling
 * costs a creature health and a cooldown; blood is unread until its kind is examined, and three individuals study it;
 * plain Projection doses an animal with fertility and taint. The acceptance case leads a weakened blaze, vats it,
 * feeds it and draws Flammeus and Lux from it again and again, until neglect kills it.
 */
@GameTestHolder("menagerie_validation")
@PrefixGameTestTemplate(false)
public final class MenagerieGameTests {
	private static final String EMPTY = "empty";

	private MenagerieGameTests() {
	}

	// ── Fixtures ──

	/** A clear room: a stone floor at y 0 over x 0..20, z 0..12 and air above it to y 8. */
	private static void clear(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (int x = 0; x <= 20; x++) {
			for (int z = 0; z <= 12; z++) {
				level.setBlock(at(helper, x, 0, z), Blocks.STONE.defaultBlockState(), 2);
				for (int y = 1; y <= 8; y++) level.setBlock(at(helper, x, y, z), Blocks.AIR.defaultBlockState(), 2);
			}
		}
	}

	private static BlockPos at(GameTestHelper helper, int x, int y, int z) {
		return helper.absolutePos(new BlockPos(x, y, z));
	}

	/** A D3 Harbinger with active blood, the Tendon Line selected and Temper carried, joined to the level. */
	private static CapturedPlayer keeper(GameTestHelper helper, int degree, double blood) {
		CapturedPlayer captured = CapturedPlayer.create(helper).joinLevel();
		ServerPlayer player = captured.player;
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(Math.max(5_000.0D, blood));
		volume.setBloodVolume(blood);
		HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(degree);
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		var temperament = HemoCapabilityAccess.getBloodTemperament(player);
		for (String name : List.of("blood_binding", "tendon_line")) {
			BloodManipulation memory = ManipulationInit.getByName(name);
			known.getKnownManips().put(memory, new ManipLevel(1, 0));
			if (temperament.getAlignmentByTemper(memory.getTend()) < memory.getAlignLevel()) {
				temperament.setTemperAlignment(memory.getTend(), (float) memory.getAlignLevel() + 1.0F);
			}
		}
		known.setEquippedManipNames(new ArrayList<>(List.of("blood_binding", "tendon_line")));
		known.setSelectedManip(ManipulationInit.getByName("tendon_line"));
		TemperFixtures.carry(player);
		return captured;
	}

	private static void aim(ServerPlayer player, Vec3 feet, Vec3 at) {
		player.setPos(feet.x, feet.y, feet.z);
		player.lookAt(EntityAnchorArgument.Anchor.EYES, at);
		player.setOldPosAndRot();
		player.yHeadRotO = player.getYHeadRot();
	}

	private static Vec3 feet(GameTestHelper helper, int x, int z) {
		return Vec3.atBottomCenterOf(at(helper, x, 1, z));
	}

	private static double project(ServerPlayer player) {
		return BloodProjectionItem.projectFromEntity(player.level(), player, 5.0D, 100.0D);
	}

	private static double blood(ServerPlayer player) {
		return HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
	}

	/** An empty Specimen Vat at (x, 1, z), owned by {@code owner}; returns its block entity. */
	private static SpecimenVatBlockEntity vat(GameTestHelper helper, int x, int z, ServerPlayer owner) {
		BlockPos base = at(helper, x, 1, z);
		BlockState lower = BlockInit.specimen_vat.get().defaultBlockState().setValue(SpecimenVatBlock.FACING, Direction.SOUTH);
		helper.getLevel().setBlock(base, lower, 3);
		helper.getLevel().setBlock(base.above(), lower.setValue(SpecimenVatBlock.HALF, DoubleBlockHalf.UPPER), 3);
		SpecimenVatBlockEntity vat = (SpecimenVatBlockEntity) helper.getLevel().getBlockEntity(base);
		if (owner != null) vat.buffer().setOwner(owner.getUUID());
		return vat;
	}

	/** A Specimen Vat set down at (x, y, z) as a player sets one down: it joins the empty vats it stands among. */
	private static SpecimenVatBlockEntity setDown(GameTestHelper helper, int x, int y, int z) {
		BlockPos base = at(helper, x, y, z);
		BlockState lower = BlockInit.specimen_vat.get().defaultBlockState().setValue(SpecimenVatBlock.FACING, Direction.SOUTH);
		helper.getLevel().setBlock(base, lower, 3);
		helper.getLevel().setBlock(base.above(), lower.setValue(SpecimenVatBlock.HALF, DoubleBlockHalf.UPPER), 3);
		VatAssembly.placed(helper.getLevel(), base);
		return (SpecimenVatBlockEntity) helper.getLevel().getBlockEntity(base);
	}

	private static SpecimenVatBlockEntity vatAt(GameTestHelper helper, int x, int y, int z) {
		return (SpecimenVatBlockEntity) helper.getLevel().getBlockEntity(at(helper, x, y, z));
	}

	/** Puts a fresh creature of {@code type} straight into a vat at {@code vigor}. */
	private static void keep(GameTestHelper helper, SpecimenVatBlockEntity vat, EntityType<? extends Mob> type, double vigor) {
		Mob mob = type.create(helper.getLevel());
		CompoundTag saved = SpecimenJarData.captureEntity(mob);
		vat.kept().keep(saved, mob.getMaxHealth(), vigor, helper.getLevel().getGameTime());
		mob.discard();
	}

	/** Runs the vessel's second {@code seconds} times at the current game time. */
	private static void seconds(GameTestHelper helper, SpecimenVatBlockEntity vat, int seconds) {
		for (int i = 0; i < seconds; i++) vat.kept().tick(helper.getLevel(), vat.getBlockPos(), vat);
	}

	private static void clearTemper(ServerPlayer player) {
		BloodTemper temper = HemoCapabilityAccess.getBloodTemper(player);
		for (EnumBloodTemper school : EnumBloodTemper.values()) temper.set(school, 0.0F, TemperRules.MAX_CAP);
	}

	private static float temper(ServerPlayer player, EnumBloodTemper school) {
		return HemoCapabilityAccess.getBloodTemper(player).get(school);
	}

	private static List<String> keys(List<Component> lines) {
		List<String> keys = new ArrayList<>();
		for (Component line : lines) {
			if (line.getContents() instanceof TranslatableContents translatable) keys.add(translatable.getKey());
		}
		return keys;
	}

	private static boolean despawnDenied(Mob mob) {
		MobDespawnEvent event = new MobDespawnEvent(mob, (ServerLevel) mob.level());
		NeoForge.EVENT_BUS.post(event);
		return event.getResult() == MobDespawnEvent.Result.DENY;
	}

	/** A filled, identified vial of {@code type}, drawn from {@code donor}. */
	private static ItemStack vial(String type, String donor) {
		ItemStack vial = new ItemStack(ItemInit.bloody_vial.get());
		CompoundTag tag = new CompoundTag();
		tag.putString(BloodVialItem.TAG_ENTITY_TYPE, type);
		tag.putBoolean(BloodVialItem.TAG_STATE, true);
		tag.putString(BloodSampleData.TAG_DONOR, donor);
		vial.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		BloodSampleData.identify(vial);
		return vial;
	}

	// ── Leading a creature home ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aTendonLineThrownIntoAWeakenedHostileLeadsIt(GameTestHelper helper) {
		clear(helper);
		Husk husk = helper.spawn(EntityType.HUSK, new BlockPos(6, 1, 4));
		husk.setNoAi(true);
		husk.setHealth(husk.getMaxHealth() / 3.0F - 0.5F);
		CapturedPlayer captured = keeper(helper, 3, 2_000.0D);
		ServerPlayer player = captured.player;
		try {
			aim(player, feet(helper, 2, 4), husk.getBoundingBox().getCenter());
			double spent = project(player);
			helper.assertTrue(MenagerieLeash.leads(player, husk), "the thrown line did not lead the weakened husk");
			helper.assertTrue(spent > 0.0D, "throwing the line cost nothing");
			helper.assertTrue(despawnDenied(husk), "a led creature may despawn");
			helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_KEPT),
					"the first lead did not grant Kept (core-loop Track 11)");
		} finally {
			MenagerieLeash.release(husk, false);
			husk.discard();
			captured.close();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aHealthyHostileIsNotLedPaysNothingAndSaysWhy(GameTestHelper helper) {
		clear(helper);
		Husk husk = helper.spawn(EntityType.HUSK, new BlockPos(6, 1, 4));
		husk.setNoAi(true);
		CapturedPlayer captured = keeper(helper, 3, 2_000.0D);
		ServerPlayer player = captured.player;
		try {
			aim(player, feet(helper, 2, 4), husk.getBoundingBox().getCenter());
			double spent = project(player);
			helper.assertFalse(MenagerieLeash.isLed(husk), "a husk at full health was led");
			helper.assertTrue(Math.abs(spent) < 1.0E-6D, "a refused throw cost " + spent + " mL");
			helper.assertTrue(captured.said().contains("too strong"), "the thrower was not told why: " + captured.said());
		} finally {
			husk.discard();
			captured.close();
		}
		helper.succeed();
	}

	/** Blood Binding holds a creature still; it never turns into a lead, however long it is held. */
	@GameTest(template = EMPTY, timeoutTicks = 120)
	public static void bloodBindingHoldsAWeakenedCreatureButNeverLeadsIt(GameTestHelper helper) {
		clear(helper);
		Husk husk = helper.spawn(EntityType.HUSK, new BlockPos(6, 1, 4));
		husk.setNoAi(true);
		husk.setHealth(husk.getMaxHealth() / 3.0F - 0.5F);
		CapturedPlayer captured = keeper(helper, 3, 2_000.0D);
		ServerPlayer player = captured.player;
		HemoCapabilityAccess.getKnownManipulations(player).setSelectedManip(ManipulationInit.getByName("blood_binding"));
		aim(player, feet(helper, 2, 4), husk.getBoundingBox().getCenter());
		helper.onEachTick(() -> project(player));
		helper.runAtTickTime(80, () -> {
			try {
				helper.assertTrue(husk.hasEffect(EffectInit.blood_binding), "the binding did not hold the husk");
				helper.assertFalse(MenagerieLeash.isLed(husk), "four seconds of binding led the husk");
			} finally {
				husk.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	/** Held Absorption on a creature on the line reels it toward its keeper. */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void heldAbsorptionReelsALedCreatureIn(GameTestHelper helper) {
		clear(helper);
		// Five blocks off: inside a bare hand's reach of 5.5.
		Pig pig = helper.spawn(EntityType.PIG, new BlockPos(8, 1, 4));
		pig.setNoAi(true);
		CapturedPlayer captured = keeper(helper, 3, 2_000.0D);
		ServerPlayer player = captured.player;
		aim(player, feet(helper, 3, 4), pig.getBoundingBox().getCenter());
		MenagerieLeash.leash(player, pig);
		helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.verb.SculptingDispatcher
				.currentClaim(player, com.vincenthuto.hemomancy.common.manipulation.verb.Verb.ABSORB).isPresent(),
				"the haul does not claim a creature on the line");
		// The test reads the pull each pulse gives the pig, toward its keeper, so it does not hang on how the pig travels.
		double[] pull = {Double.MAX_VALUE};
		int[] ticks = {0};
		helper.onEachTick(() -> {
			if (ticks[0]++ >= 8) return;
			aim(player, feet(helper, 3, 4), pig.getBoundingBox().getCenter());
			pig.setDeltaMovement(Vec3.ZERO);
			FirstConversionsGameTests.absorbTick(player);
			Vec3 toward = player.position().subtract(pig.position()).normalize();
			pull[0] = Math.min(pull[0], pig.getDeltaMovement().dot(toward));
		});
		helper.runAtTickTime(12, () -> {
			try {
				player.stopUsingItem();
				helper.assertTrue(pull[0] > 0.05D, "the line did not reel the pig toward its keeper: " + pull[0]);
				helper.assertTrue(MenagerieLeash.leads(player, pig), "reeling the pig in let it go");
			} finally {
				MenagerieLeash.release(pig, false);
				pig.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void oneRuleRefusesPeoplePetsAndBossesForLeashJarAndVat(GameTestHelper helper) {
		clear(helper);
		Villager villager = helper.spawn(EntityType.VILLAGER, new BlockPos(3, 1, 3));
		Wolf wolf = helper.spawn(EntityType.WOLF, new BlockPos(6, 1, 3));
		Pig pig = helper.spawn(EntityType.PIG, new BlockPos(9, 1, 3));
		villager.setNoAi(true);
		wolf.setNoAi(true);
		pig.setNoAi(true);
		try (CapturedPlayer captured = keeper(helper, 7, 1_000.0D)) {
			wolf.tame(captured.player);
			helper.assertTrue(MenagerieLeash.verdict(villager) == MenagerieRules.Verdict.PERSON, "a villager may be kept");
			helper.assertTrue(MenagerieLeash.verdict(wolf) == MenagerieRules.Verdict.OWNED, "a tame wolf may be kept");
			helper.assertTrue(MenagerieLeash.verdict(captured.player) == MenagerieRules.Verdict.PLAYER, "a player may be kept");
			helper.assertTrue(MenagerieLeash.verdict(pig).keeps(), "a pig may not be kept");
			helper.assertFalse(SpecimenVatBlock.fits(villager, 1, 1), "a villager fits the vat");
			helper.assertTrue(SpecimenVatBlock.fits(pig, 1, 1), "a pig does not fit the vat");
			helper.assertTrue(MenagerieLeash.refusal(captured.player, villager) != null, "a villager can be leashed");
			// CR-26: the mod's people and Wills are refused by tag, each with its own line.
			Mob gardener = helper.spawn(EntityInit.court_members.get(CourtRole.GARDENER).get(), new BlockPos(3, 1, 6));
			Mob will = helper.spawn(EntityInit.will.get(), new BlockPos(6, 1, 6));
			try {
				gardener.setNoAi(true);
				will.setNoAi(true);
				helper.assertTrue(gardener.getType().is(MenagerieLeash.UNKEEPABLE), "a Court gardener is not in menagerie_unkeepable");
				helper.assertTrue(MenagerieLeash.verdict(gardener) == MenagerieRules.Verdict.PERSON, "a Court gardener may be kept");
				helper.assertTrue(will.getType().is(MenagerieLeash.UNKEEPABLE), "a Will is not in menagerie_unkeepable");
				helper.assertTrue(MenagerieLeash.verdict(will) == MenagerieRules.Verdict.WILL, "a Will may be kept");
				helper.assertFalse(EntityType.PIG.is(MenagerieLeash.UNKEEPABLE), "a pig is in menagerie_unkeepable");
			} finally {
				gardener.discard();
				will.discard();
			}
		} finally {
			villager.discard();
			wolf.discard();
			pig.discard();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void degreeSetsHowManyCreaturesMayBeLed(GameTestHelper helper) {
		clear(helper);
		Pig first = helper.spawn(EntityType.PIG, new BlockPos(3, 1, 3));
		Pig second = helper.spawn(EntityType.PIG, new BlockPos(6, 1, 3));
		first.setNoAi(true);
		second.setNoAi(true);
		try (CapturedPlayer captured = keeper(helper, 2, 1_000.0D)) {
			ServerPlayer player = captured.player;
			helper.assertTrue(MenagerieLeash.refusal(player, first) != null, "a D2 Harbinger may lead a creature");
			HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(3);
			helper.assertTrue(MenagerieLeash.refusal(player, first) == null, "a D3 Harbinger may not lead one creature");
			MenagerieLeash.leash(player, first);
			helper.assertTrue(MenagerieLeash.refusal(player, second) != null, "a D3 Harbinger may lead two");
			HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(5);
			helper.assertTrue(MenagerieLeash.refusal(player, second) == null, "a D5 Harbinger may not lead a second");
		} finally {
			MenagerieLeash.release(first, false);
			first.discard();
			second.discard();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 120)
	public static void theLeashCostsUpkeepAndBreaksFreeAngryWhenUnpaid(GameTestHelper helper) {
		clear(helper);
		Husk husk = helper.spawn(EntityType.HUSK, new BlockPos(6, 1, 4));
		husk.setHealth(4.0F);
		CapturedPlayer captured = keeper(helper, 3, 1_000.0D);
		ServerPlayer player = captured.player;
		player.setPos(feet(helper, 3, 4));
		helper.assertTrue(MenagerieLeash.refusal(player, husk) == null, "a weakened husk cannot be leashed");
		MenagerieLeash.leash(player, husk);
		helper.runAtTickTime(45, () -> {
			// Two seconds of a 20-health creature's upkeep: 5 mL a second.
			double paid = 1_000.0D - blood(player);
			helper.assertTrue(paid >= 5.0D - 1.0E-6D && paid <= 15.0D + 1.0E-6D, "the leash paid " + paid + " mL in two seconds");
			helper.assertTrue(MenagerieLeash.isLed(husk), "the husk broke free while paid for");
			HemoCapabilityAccess.getBloodVolume(player).setBloodVolume(2.0D);
		});
		helper.runAtTickTime(75, () -> {
			try {
				helper.assertFalse(MenagerieLeash.isLed(husk), "the husk stayed leashed when the upkeep went unpaid");
				helper.assertTrue(husk.getTarget() == player, "the freed husk did not turn on its keeper");
				helper.assertTrue(captured.said().contains("broke free"), "the keeper was not told: " + captured.said());
			} finally {
				husk.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void aLedCreatureFollowsItsKeeper(GameTestHelper helper) {
		clear(helper);
		Pig pig = helper.spawn(EntityType.PIG, new BlockPos(2, 1, 6));
		CapturedPlayer captured = keeper(helper, 3, 1_000.0D);
		ServerPlayer player = captured.player;
		player.setPos(feet(helper, 14, 6));
		MenagerieLeash.leash(player, pig);
		double start = pig.distanceTo(player);
		helper.runAtTickTime(160, () -> {
			try {
				double now = pig.distanceTo(player);
				helper.assertTrue(now < start - 4.0D, "the pig did not follow: " + start + " to " + now);
			} finally {
				MenagerieLeash.release(pig, false);
				pig.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void aLedCreatureGoesThroughAPortalWithItsKeeper(GameTestHelper helper) {
		clear(helper);
		ServerLevel nether = helper.getLevel().getServer().getLevel(Level.NETHER);
		helper.assertTrue(nether != null, "the GameTest server has no Nether");
		Pig pig = helper.spawn(EntityType.PIG, new BlockPos(5, 1, 6));
		pig.setNoAi(true);
		CapturedPlayer captured = keeper(helper, 3, 1_000.0D);
		ServerPlayer player = captured.player;
		player.setPos(feet(helper, 7, 6));
		MenagerieLeash.leash(player, pig);
		java.util.UUID id = pig.getUUID();
		helper.runAtTickTime(5, () -> {
			helper.assertTrue(MenagerieLeash.nearKeeper(pig), "the pig did not count as near its keeper");
			// The keeper arrives in the Nether: stand them inside the Nether's height. The arrival chunk is not loaded
			// here (generating it would stall the server for every test running beside this one), so the arrivals are
			// checked through the returned entity, not a UUID lookup.
			player.setPos(player.getX(), 70.0D, player.getZ());
			NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerChangedDimensionEvent(player, Level.OVERWORLD, Level.NETHER));
			Pig second = helper.spawn(EntityType.PIG, new BlockPos(6, 1, 6));
			second.setNoAi(true);
			MenagerieLeash.leash(player, second);
			var arrived = MenagerieLeash.carry(second, player, nether);
			helper.assertTrue(arrived != null && arrived.level() == nether && !arrived.isRemoved(),
					"a carried pig did not arrive: " + arrived + (arrived == null ? "" : " in " + arrived.level().dimension()
							+ " removed " + arrived.getRemovalReason()));
			helper.assertTrue(MenagerieLeash.leads(player, arrived), "the carried pig lost its leash on the way");
			MenagerieLeash.release((Mob) arrived, false);
			arrived.discard();
		});
		helper.runAtTickTime(40, () -> {
			try {
				// The keeper's change of dimension took the near pig with them. (Its arrival is checked on the second pig
				// above, through the entity the carry returned.)
				helper.assertTrue(pig.getRemovalReason() == net.minecraft.world.entity.Entity.RemovalReason.CHANGED_DIMENSION,
						"the pig stayed behind: " + pig.getRemovalReason());
				helper.assertTrue(helper.getLevel().getEntity(id) == null, "the pig is still in the Overworld");
			} finally {
				// The carried pig waits for its keeper in the Nether; take it away if its chunk is loaded.
				if (nether.getEntity(id) instanceof Mob carried) {
					MenagerieLeash.release(carried, false);
					carried.discard();
				}
				captured.close();
			}
			helper.succeed();
		});
	}

	// ── Vats ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aLedCreatureGoesIntoAVatAndLivesOnItsBuffer(GameTestHelper helper) {
		clear(helper);
		Husk husk = helper.spawn(EntityType.HUSK, new BlockPos(6, 1, 6));
		husk.setNoAi(true);
		husk.setHealth(5.0F);
		try (CapturedPlayer captured = keeper(helper, 3, 2_000.0D)) {
			ServerPlayer player = captured.player;
			player.setPos(feet(helper, 4, 6));
			SpecimenVatBlockEntity vat = vat(helper, 5, 8, player);
			MenagerieLeash.leash(player, husk);
			helper.assertTrue(!com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_IN_GLASS), "the keeper was In Glass before leading anything in");
			helper.assertTrue(SpecimenVessels.useEmptyHand(helper.getLevel(), vat.getBlockPos(), vat, player, vat::fits),
					"the led husk did not go into the vat");
			helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_IN_GLASS), "leading a creature into a vat did not grant In Glass");
			helper.assertTrue(vat.kept().living() && !husk.isAlive(), "the husk is not kept, or still walks");
			helper.assertTrue(Math.abs(vat.kept().vigor() - 25.0D) < 1.0E-6D, "a husk at a quarter health started at " + vat.kept().vigor());
			double paid = SpecimenVessels.project(vat, player, vat.getBlockPos(), 100.0D);
			helper.assertTrue(paid > 0.0D && vat.buffer().getBloodVolume() >= paid - 1.0E-6D, "Projection did not feed the buffer");
			double buffer = vat.buffer().getBloodVolume();
			seconds(helper, vat, 3);
			helper.assertTrue(Math.abs(vat.kept().vigor() - 28.0D) < 1.0E-6D, "three seconds fed " + vat.kept().vigor());
			helper.assertTrue(Math.abs(buffer - vat.buffer().getBloodVolume() - 30.0D) < 1.0E-6D,
					"three points of a 20-health husk cost " + (buffer - vat.buffer().getBloodVolume()) + " mL");
			helper.assertTrue(vat.isLivingVessel(), "the vat's buffer clots");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void heldAbsorptionOnAVatGivesItsSchoolsCutByAcclimation(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = keeper(helper, 3, 1_000.0D);
		ServerPlayer player = captured.player;
		clearTemper(player);
		// A read kind, so the pull gives its whole Temper (an unread kind gives half, CL-12).
		HemoCapabilityAccess.clinicalBlood(player).recordExamination("minecraft:blaze");
		SpecimenVatBlockEntity vat = vat(helper, 6, 6, player);
		keep(helper, vat, EntityType.BLAZE, 100.0D);
		aim(player, feet(helper, 3, 6), Vec3.atCenterOf(vat.getBlockPos()));
		helper.assertTrue(BlockBloodInteractions.tryAbsorbFromLookedAtBlock(helper.getLevel(), player, 5.5D, 1.0D) > 0.0D,
				"Absorption on the vat was not taken by it");
		float flammeus = temper(player, EnumBloodTemper.FLAMMEUS);
		float lux = temper(player, EnumBloodTemper.LUX);
		helper.assertTrue(Math.abs(flammeus - 3.0F) < 1.0E-4F && Math.abs(lux - 3.0F) < 1.0E-4F,
				"one pull at full Vigor gave Flammeus " + flammeus + " and Lux " + lux);
		helper.assertTrue(Math.abs(blood(player) - 1_002.0D) < 1.0E-6D, "one pull gave " + (blood(player) - 1_000.0D) + " mL");
		helper.assertTrue(Math.abs(vat.kept().vigor() - 98.0D) < 1.0E-6D, "one pull cost " + (100.0D - vat.kept().vigor()) + " Vigor");
		helper.runAtTickTime(12, () -> {
			// Fully acclimated to blazes: the next pull gives nothing, as a wild blaze would.
			HemoCapabilityAccess.getPowerGuardrails(player).recordHematicExposure("minecraft:blaze", 1_500.0D,
					helper.getLevel().getGameTime());
			float before = temper(player, EnumBloodTemper.FLAMMEUS);
			BlockBloodInteractions.tryAbsorbFromLookedAtBlock(helper.getLevel(), player, 5.5D, 1.0D);
			try {
				helper.assertTrue(Math.abs(temper(player, EnumBloodTemper.FLAMMEUS) - before) < 1.0E-4F,
						"an acclimated keeper still drew Temper from a blaze");
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aStarvedCreatureLeavesAHuskAndItsLeavings(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = keeper(helper, 3, 1_000.0D)) {
			ServerPlayer player = captured.player;
			SpecimenVatBlockEntity vat = vat(helper, 6, 6, player);
			keep(helper, vat, EntityType.COW, 100.0D);
			long now = helper.getLevel().getGameTime();
			vat.kept().setVigor(1.0D, now - 1_300L);
			seconds(helper, vat, 1);
			helper.assertTrue(vat.kept().husk(), "an unfed cow at 1 Vigor a minute on did not die");
			helper.assertTrue(keys(SpecimenVessels.readout(player, vat)).contains("message.hemomancy.menagerie.readout.husk"),
					"the readout does not say it is a husk");
			player.getInventory().clearContent();
			helper.assertTrue(SpecimenVessels.useEmptyHand(helper.getLevel(), vat.getBlockPos(), vat, player, vat::fits),
					"the husk could not be taken out");
			helper.assertTrue(player.getInventory().countItem(Items.BONE_MEAL) == 3
					&& player.getInventory().countItem(ItemInit.ruinous_enzyme.get()) == 1, "the husk left the wrong leavings");
			helper.assertFalse(vat.kept().occupied(), "the vat still holds something");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aFailingStrongHostileBreaksOutAngry(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = keeper(helper, 3, 1_000.0D)) {
			ServerPlayer player = captured.player;
			player.setPos(feet(helper, 3, 6));
			SpecimenVatBlockEntity vat = vat(helper, 6, 6, player);
			keep(helper, vat, EntityType.BLAZE, 5.0D);
			helper.assertTrue(VatVigorRules.mayBreakOut(vat.kept().vigor(), vat.kept().hostile(), vat.kept().maxHealth()),
					"a blaze at 5 Vigor may not break out");
			var freed = vat.kept().breakOut(helper.getLevel(), vat.getBlockPos(), vat.buffer());
			helper.assertTrue(freed.isPresent() && freed.get() instanceof Blaze, "no blaze came out");
			Blaze blaze = (Blaze) freed.get();
			try {
				helper.assertTrue(blaze.getTarget() == player, "the freed blaze is not angry at its keeper");
				helper.assertFalse(vat.kept().occupied(), "the vat still holds the blaze");
			} finally {
				blaze.discard();
			}
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aBrokenVatLetsItsCreatureGoAndDropsOnlyItself(GameTestHelper helper) {
		clear(helper);
		SpecimenVatBlockEntity vat;
		try (CapturedPlayer captured = keeper(helper, 3, 1_000.0D)) {
			vat = vat(helper, 6, 6, captured.player);
		}
		keep(helper, vat, EntityType.PIG, 60.0D);
		BlockPos base = vat.getBlockPos();
		helper.getLevel().destroyBlock(base, true);
		AABB around = new AABB(base).inflate(3.0D);
		List<Pig> pigs = helper.getLevel().getEntitiesOfClass(Pig.class, around);
		List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, around);
		try {
			helper.assertTrue(pigs.size() == 1, "breaking the vat let out " + pigs.size() + " pigs");
			helper.assertTrue(drops.size() == 1 && drops.get(0).getItem().is(BlockInit.specimen_vat.get().asItem())
					&& !SpecimenJarData.hasSpecimen(drops.get(0).getItem()), "the vat dropped " + drops.stream()
							.map(drop -> drop.getItem().toString()).toList());
			helper.assertTrue(helper.getLevel().getBlockState(base.above()).isAir(), "the vat's top stood alone");
		} finally {
			pigs.forEach(Pig::discard);
			drops.forEach(ItemEntity::discard);
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aVatPickedUpKeepsItsCreatureAndVigor(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = keeper(helper, 3, 1_000.0D)) {
			ServerPlayer player = captured.player;
			SpecimenVatBlockEntity vat = vat(helper, 6, 6, player);
			keep(helper, vat, EntityType.COW, 42.0D);
			ItemStack carried = new ItemStack(BlockInit.specimen_vat.get());
			vat.kept().saveToItem(carried);
			helper.assertTrue(carried.getMaxStackSize() == 1, "a filled vat stacks");
			KeptSpecimen restored = new KeptSpecimen();
			restored.loadFromItem(carried, helper.getLevel().getGameTime());
			helper.assertTrue(restored.living() && Math.abs(restored.vigor() - 42.0D) < 1.0E-6D
					&& restored.type() == EntityType.COW, "the carried vat lost its cow or its Vigor: " + restored.vigor());
			CompoundTag saved = new CompoundTag();
			vat.kept().save(saved);
			KeptSpecimen reloaded = new KeptSpecimen();
			reloaded.load(saved);
			helper.assertTrue(reloaded.living() && Math.abs(reloaded.vigor() - 42.0D) < 1.0E-6D, "a saved vat lost its cow");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void aTendingVigilKeepsNearbyCreaturesFromFading(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = keeper(helper, 3, 1_000.0D);
		ServerPlayer player = captured.player;
		player.setPos(feet(helper, 4, 4));
		SpecimenVatBlockEntity vat = vat(helper, 6, 6, player);
		keep(helper, vat, EntityType.COW, 50.0D);
		BlockPos vigilPos = at(helper, 8, 1, 6);
		helper.getLevel().setBlock(vigilPos, BlockInit.sanguine_vigil.get().defaultBlockState(), 3);
		SanguineVigilBlockEntity vigil = (SanguineVigilBlockEntity) helper.getLevel().getBlockEntity(vigilPos);
		com.vincenthuto.hemomancy.common.tile.ReservoirBlood.settled(vigil).setOwner(player.getUUID());
		vigil.insertCatalyst(new ItemStack(ItemInit.vivacious_enzyme.get(), 2));
		helper.onEachTick(() -> SanguineVigilBlockEntity.serverTick(helper.getLevel(), vigilPos,
				helper.getLevel().getBlockState(vigilPos), vigil));
		helper.runAtTickTime(45, () -> {
			try {
				long now = helper.getLevel().getGameTime();
				helper.assertTrue(vigil.tending() && vat.kept().tended(now), "the Vigil does not tend the vat");
				double before = vat.kept().vigor();
				vat.kept().settle(now + 20L, true);
				helper.assertTrue(Math.abs(vat.kept().vigor() - before) < 1.0E-9D, "a tended cow still faded");
				helper.assertTrue(keys(SpecimenVessels.readout(player, vat)).contains("message.hemomancy.menagerie.readout.tended"),
						"the readout does not say it is tended");
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	// ── Sampling and research ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aDrawCostsTheCreatureHealthAndACooldown(GameTestHelper helper) {
		clear(helper);
		Cow cow = helper.spawn(EntityType.COW, new BlockPos(5, 1, 5));
		cow.setNoAi(true);
		try (CapturedPlayer captured = keeper(helper, 2, 1_000.0D)) {
			ServerPlayer player = captured.player;
			ItemStack vial = new ItemStack(ItemInit.bloody_vial.get());
			player.setItemInHand(InteractionHand.MAIN_HAND, vial);
			((BloodVialItem) vial.getItem()).onLeftClickEntity(vial, player, cow);
			helper.assertTrue(Math.abs(cow.getHealth() - (cow.getMaxHealth() - 2.0F)) < 1.0E-4F, "a vial took " +
					(cow.getMaxHealth() - cow.getHealth()) + " health");
			helper.assertTrue(BloodSampleData.isFilled(player.getMainHandItem()), "the vial was not filled");
			ItemStack second = new ItemStack(ItemInit.bloody_vial.get());
			player.setItemInHand(InteractionHand.MAIN_HAND, second);
			((BloodVialItem) second.getItem()).onLeftClickEntity(second, player, cow);
			helper.assertFalse(BloodSampleData.isFilled(player.getMainHandItem()), "a recently drawn cow gave a second sample");
			helper.assertTrue(Math.abs(cow.getHealth() - (cow.getMaxHealth() - 2.0F)) < 1.0E-4F, "the refused draw hurt the cow");
			helper.assertTrue(captured.said().contains("too recently"), "the drawer was not told: " + captured.said());
		} finally {
			cow.discard();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aVialFromAVatPaysInVigor(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = keeper(helper, 2, 1_000.0D); CapturedPlayer stranger = keeper(helper, 2, 1_000.0D)) {
			ServerPlayer player = captured.player;
			SpecimenVatBlockEntity vat = vat(helper, 6, 6, player);
			keep(helper, vat, EntityType.COW, 50.0D);
			// Someone outside the keeper's bloodline draws nothing from it (CL-8).
			ItemStack strangerVial = new ItemStack(ItemInit.bloody_vial.get());
			stranger.player.setItemInHand(InteractionHand.MAIN_HAND, strangerVial);
			helper.assertTrue(SpecimenVessels.sample(helper.getLevel(), vat.getBlockPos(), vat, stranger.player, strangerVial,
					InteractionHand.MAIN_HAND).isEmpty(), "a stranger sampled someone else's vat");
			helper.assertTrue(Math.abs(vat.kept().vigor() - 50.0D) < 1.0E-6D, "a refused sample still cost Vigor");
			// A preserved specimen (no keeper) gives no sample either.
			SpecimenVatBlockEntity preserved = vat(helper, 10, 6, null);
			keep(helper, preserved, EntityType.COW, 50.0D);
			ItemStack preservedVial = new ItemStack(ItemInit.bloody_vial.get());
			helper.assertTrue(SpecimenVessels.sample(helper.getLevel(), preserved.getBlockPos(), preserved, player, preservedVial,
					InteractionHand.MAIN_HAND).isEmpty(), "a preserved specimen gave a sample");
			helper.assertTrue(Math.abs(preserved.kept().vigor() - 50.0D) < 1.0E-6D, "a preserved specimen lost Vigor to a sample");
			ItemStack vial = new ItemStack(ItemInit.bloody_vial.get());
			player.setItemInHand(InteractionHand.MAIN_HAND, vial);
			ItemStack sample = SpecimenVessels.sample(helper.getLevel(), vat.getBlockPos(), vat, player, vial, InteractionHand.MAIN_HAND);
			helper.assertTrue(BloodSampleData.isFilled(sample) && BloodSampleData.entityType(sample) == EntityType.COW,
					"no cow sample came out of the vat");
			helper.assertTrue(Math.abs(vat.kept().vigor() - 45.0D) < 1.0E-6D, "the sample cost " + (50.0D - vat.kept().vigor()) + " Vigor");
			helper.assertTrue(BloodSampleData.donor(sample).startsWith("vessel:"), "the vat sample names no individual");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void bloodIsUnreadUntilItsKindIsExamined(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer examiner = keeper(helper, 2, 1_000.0D); CapturedPlayer stranger = keeper(helper, 2, 1_000.0D)) {
			ServerPlayer player = examiner.player;
			SpecimenVatBlockEntity vat = vat(helper, 6, 6, player);
			keep(helper, vat, EntityType.COW, 50.0D);
			helper.assertFalse(BloodResearch.knows(player, EntityType.COW), "an unexamined cow is labelled");
			helper.assertTrue(keys(SpecimenVessels.readout(player, vat)).contains("message.hemomancy.menagerie.readout.unread"),
					"the vat reads an unexamined cow");
			ItemStack sample = vial("minecraft:cow", "a");
			ClinicalBloodKnowledge.examined(player, sample);
			helper.assertTrue(BloodResearch.knows(player, EntityType.COW), "examining a cow sample taught nothing");
			List<String> lines = keys(SpecimenVessels.readout(player, vat));
			helper.assertTrue(lines.contains("message.hemomancy.menagerie.readout.schools")
					&& lines.contains("message.hemomancy.menagerie.readout.study"), "the readout after examining: " + lines);
			// The same identified vial, traded, is still unread to someone who has not done the work.
			helper.assertTrue(BloodSampleData.identified(sample), "the examined vial is not identified");
			helper.assertFalse(BloodResearch.knows(stranger.player, EntityType.COW), "a traded sample taught its new owner");
		}
		helper.succeed();
	}

	/** CL-12 (NQ-03-1 A, NQ-03-2): a pull on a kept kind gives half its Temper until read, and the same blood. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anUnreadKeptKindGivesHalfItsTemper(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer unread = keeper(helper, 2, 1_000.0D); CapturedPlayer read = keeper(helper, 2, 1_000.0D)) {
			ClinicalBloodKnowledge.examined(read.player, vial("minecraft:cow", "a"));
			SpecimenVatBlockEntity weak = vat(helper, 6, 6, unread.player);
			SpecimenVatBlockEntity full = vat(helper, 12, 6, read.player);
			keep(helper, weak, EntityType.COW, 80.0D);
			keep(helper, full, EntityType.COW, 80.0D);
			clearTemper(unread.player);
			clearTemper(read.player);
			double unreadBlood = blood(unread.player), readBlood = blood(read.player);
			helper.assertTrue(weak.kept().draw(helper.getLevel(), unread.player, weak) == KeptSpecimen.DrawOutcome.PULLED
					&& full.kept().draw(helper.getLevel(), read.player, full) == KeptSpecimen.DrawOutcome.PULLED,
					"a kept cow could not be drawn");
			float half = temper(unread.player, EnumBloodTemper.ANIMUS), whole = temper(read.player, EnumBloodTemper.ANIMUS);
			helper.assertTrue(whole > 0.0F && Math.abs(half - whole * 0.5F) < 1.0E-4F,
					"an unread pull gave " + half + " Animus beside a read pull's " + whole);
			helper.assertTrue(Math.abs((blood(unread.player) - unreadBlood) - (blood(read.player) - readBlood)) < 1.0E-6D,
					"an unread pull gave different blood");
		}
		helper.succeed();
	}

	/** CL-12's wild twin: Absorption on an unread kind gives half its Temper and the same blood. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anUnreadWildKindGivesHalfItsTemper(GameTestHelper helper) {
		clear(helper);
		Cow first = helper.spawn(EntityType.COW, new BlockPos(4, 1, 4));
		Cow second = helper.spawn(EntityType.COW, new BlockPos(10, 1, 4));
		first.setNoAi(true);
		second.setNoAi(true);
		try (CapturedPlayer unread = keeper(helper, 2, 1_000.0D); CapturedPlayer read = keeper(helper, 2, 1_000.0D)) {
			ClinicalBloodKnowledge.examined(read.player, vial("minecraft:cow", "a"));
			unread.player.setPos(feet(helper, 4, 3));
			read.player.setPos(feet(helper, 10, 3));
			clearTemper(unread.player);
			clearTemper(read.player);
			double drawnUnread = BloodAbsorptionItem.absorbFromTarget(helper.getLevel(), unread.player, first, 4.0D);
			double drawnRead = BloodAbsorptionItem.absorbFromTarget(helper.getLevel(), read.player, second, 4.0D);
			helper.assertTrue(drawnUnread > 0.0D && Math.abs(drawnUnread - drawnRead) < 1.0E-6D,
					"the two draws took " + drawnUnread + " and " + drawnRead);
			float half = temper(unread.player, EnumBloodTemper.ANIMUS), whole = temper(read.player, EnumBloodTemper.ANIMUS);
			helper.assertTrue(whole > 0.0F && Math.abs(half - whole * 0.5F) < 1.0E-4F,
					"an unread draw gave " + half + " Animus beside a read draw's " + whole);
		} finally {
			first.discard();
			second.discard();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void threeIndividualsStudyAKindAndTheVatTellsItsYield(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = keeper(helper, 2, 1_000.0D)) {
			ServerPlayer player = captured.player;
			SpecimenVatBlockEntity vat = vat(helper, 6, 6, player);
			keep(helper, vat, EntityType.COW, 80.0D);
			ClinicalBloodKnowledge.examined(player, vial("minecraft:cow", "a"));
			ClinicalBloodKnowledge.examined(player, vial("minecraft:cow", "a"));
			helper.assertFalse(BloodResearch.studied(player, net.minecraft.resources.ResourceLocation.parse("minecraft:cow")),
					"one individual examined twice studied the kind");
			ClinicalBloodKnowledge.examined(player, vial("minecraft:cow", "b"));
			helper.assertTrue(!com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_STUDIED), "two individuals granted Studied");
			ClinicalBloodKnowledge.examined(player, vial("minecraft:cow", "c"));
			helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_STUDIED), "the third individual did not grant Studied");
			helper.assertTrue(BloodResearch.studied(player, net.minecraft.resources.ResourceLocation.parse("minecraft:cow")),
					"three individuals did not study the kind");
			helper.assertTrue(keys(SpecimenVessels.readout(player, vat)).contains("message.hemomancy.menagerie.readout.yield"),
					"a studied kind's vat does not tell its yield");
		}
		helper.succeed();
	}

	// ── Projection into animals ──

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void plainProjectionDosesAnAnimalWithFertilityAndTaint(GameTestHelper helper) {
		clear(helper);
		Cow cow = helper.spawn(EntityType.COW, new BlockPos(5, 1, 5));
		cow.setNoAi(true);
		CapturedPlayer captured = keeper(helper, 2, 1_000.0D);
		ServerPlayer player = captured.player;
		HemoCapabilityAccess.getKnownManipulations(player).setEquippedManipNames(new ArrayList<>());
		HemoCapabilityAccess.getKnownManipulations(player).setSelectedManip(BloodManipulation.BLANK);
		aim(player, feet(helper, 2, 5), cow.getBoundingBox().getCenter());
		double spent = project(player);
		helper.assertTrue(Math.abs(spent - AnimalTaint.DOSE_ML) < 1.0E-6D, "a dose cost " + spent + " mL");
		helper.assertTrue(AnimalTaint.tainted(cow) && cow.hasEffect(EffectInit.sanguine_fertility), "the cow took no dose");
		helper.assertTrue(Math.abs(project(player)) < 1.0E-6D, "a second dose within two seconds cost blood");
		helper.runAtTickTime(5, () -> {
			try {
				helper.assertTrue(cow.isInLove(), "the dosed cow did not come into season");
			} finally {
				cow.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aTaintedAnimalDropsTaintedMeatAndMayGoFeral(GameTestHelper helper) {
		clear(helper);
		Cow cow = helper.spawn(EntityType.COW, new BlockPos(5, 1, 5));
		Cow wild = helper.spawn(EntityType.COW, new BlockPos(9, 1, 5));
		// Still, so neither wanders into a neighbouring test.
		cow.setNoAi(true);
		wild.setNoAi(true);
		try (CapturedPlayer captured = keeper(helper, 2, 1_000.0D)) {
			AnimalTaintState taint = HemoCapabilityAccess.getAnimalTaint(cow);
			taint.doses = 40;
			for (int i = 0; i < 30 && !AnimalTaint.feral(cow); i++) {
				AnimalTaint.dose(helper.getLevel(), captured.player, cow, EnumBloodTemper.MORTEM);
			}
			helper.assertTrue(AnimalTaint.feral(cow), "a heavily dosed cow never went feral");
			helper.assertTrue(cow.goalSelector.getAvailableGoals().stream().anyMatch(goal -> goal.getGoal() instanceof AnimalTaint.FeralGoal),
					"a feral cow has no feral goal");
			cow.hurt(helper.getLevel().damageSources().playerAttack(captured.player), 100.0F);
			AABB around = new AABB(at(helper, 5, 1, 5)).inflate(3.0D);
			List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, around);
			helper.assertTrue(drops.stream().anyMatch(drop -> drop.getItem().is(ItemInit.tainted_meat.get()))
							&& drops.stream().noneMatch(drop -> drop.getItem().is(Items.BEEF)),
					"the tainted cow dropped " + drops.stream().map(drop -> drop.getItem().toString()).toList());
			drops.forEach(ItemEntity::discard);
			helper.assertFalse(AnimalTaint.tainted(wild), "an undosed cow is tainted");
		} finally {
			cow.discard();
			wild.discard();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void offspringOfATaintedParentMayCarryItsSchool(GameTestHelper helper) {
		clear(helper);
		Cow mother = helper.spawn(EntityType.COW, new BlockPos(5, 1, 5));
		Cow father = helper.spawn(EntityType.COW, new BlockPos(6, 1, 5));
		mother.setNoAi(true);
		father.setNoAi(true);
		HemoCapabilityAccess.getAnimalTaint(mother).doses = 1;
		HemoCapabilityAccess.getAnimalTaint(mother).school = EnumBloodTemper.LUX;
		Cow calf = null;
		for (int i = 0; i < 40 && calf == null; i++) {
			List<Cow> before = helper.getLevel().getEntitiesOfClass(Cow.class, new AABB(at(helper, 5, 1, 5)).inflate(4.0D));
			mother.setAge(0);
			father.setAge(0);
			mother.spawnChildFromBreeding(helper.getLevel(), father);
			for (Cow found : helper.getLevel().getEntitiesOfClass(Cow.class, new AABB(at(helper, 5, 1, 5)).inflate(4.0D))) {
				if (before.contains(found)) continue;
				found.setNoAi(true);
				if (AnimalTaint.inheritedSchool(found) == EnumBloodTemper.LUX) calf = found;
				else found.discard();
			}
		}
		helper.assertTrue(calf != null, "no calf of a tainted cow carried its school in 40 births");
		try (CapturedPlayer captured = keeper(helper, 3, 1_000.0D)) {
			clearTemper(captured.player);
			TemperCharge.fillFromCreature(captured.player, calf, 4.0D);
			helper.assertTrue(temper(captured.player, EnumBloodTemper.LUX) > 0.0F
					&& temper(captured.player, EnumBloodTemper.LUX) < temper(captured.player, EnumBloodTemper.ANIMUS),
					"drawing the calf gave Lux " + temper(captured.player, EnumBloodTemper.LUX) + " beside Animus "
							+ temper(captured.player, EnumBloodTemper.ANIMUS));
			// Kept in a vat, the calf keeps its inherited school: a pull gives it at half a share too.
			clearTemper(captured.player);
			SpecimenVatBlockEntity vat = vat(helper, 12, 6, captured.player);
			vat.kept().keep(SpecimenJarData.captureEntity(calf), calf.getMaxHealth(), 100.0D, helper.getLevel().getGameTime());
			helper.assertTrue(vat.kept().inheritedSchool() == EnumBloodTemper.LUX, "the vatted calf lost its inherited school");
			helper.assertTrue(vat.kept().draw(helper.getLevel(), captured.player, vat) == KeptSpecimen.DrawOutcome.PULLED,
					"the vatted calf could not be drawn");
			float animus = temper(captured.player, EnumBloodTemper.ANIMUS);
			float lux = temper(captured.player, EnumBloodTemper.LUX);
			helper.assertTrue(lux > 0.0F && Math.abs(lux - animus * 0.5F) < 1.0E-4F,
					"a pull on the vatted calf gave Lux " + lux + " beside Animus " + animus);
		} finally {
			calf.discard();
			mother.discard();
			father.discard();
		}
		helper.succeed();
	}

	// ── Acceptance ──

	/**
	 * A weakened blaze is caught on the Tendon Line, led into a vat, fed by Projection, and drawn
	 * for Flammeus and Lux twice over a refeed, the keeper paying blood each time; its blood was unread to the keeper
	 * until examined; left unfed, it dies.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void aBlazeLedHomeFeedsFlammeusAndLuxUntilNeglectKillsIt(GameTestHelper helper) {
		clear(helper);
		Blaze blaze = helper.spawn(EntityType.BLAZE, new BlockPos(6, 1, 4));
		blaze.setNoAi(true);
		blaze.setHealth(5.0F);
		CapturedPlayer captured = keeper(helper, 3, 3_000.0D);
		ServerPlayer player = captured.player;
		aim(player, feet(helper, 2, 4), blaze.getBoundingBox().getCenter());
		SpecimenVatBlockEntity vat = vat(helper, 4, 7, player);
		boolean[] throwing = {true};
		helper.onEachTick(() -> {
			if (throwing[0]) project(player);
			if (MenagerieLeash.isLed(blaze)) throwing[0] = false;
		});
		helper.runAtTickTime(75, () -> {
			throwing[0] = false;
			helper.assertTrue(MenagerieLeash.leads(player, blaze), "the weakened blaze was not leashed");
			helper.assertTrue(despawnDenied(blaze), "the led blaze may despawn");
			helper.assertTrue(SpecimenVessels.useEmptyHand(helper.getLevel(), vat.getBlockPos(), vat, player, vat::fits),
					"the led blaze did not go into the vat");
			helper.assertTrue(keys(SpecimenVessels.readout(player, vat)).contains("message.hemomancy.menagerie.readout.unread"),
					"the keeper knew a blaze's blood before examining one");
			clearTemper(player);
			feed(helper, vat, player);
		});
		float[] first = new float[2];
		helper.runAtTickTime(80, () -> draw(helper, vat, player));
		helper.runAtTickTime(90, () -> draw(helper, vat, player));
		helper.runAtTickTime(100, () -> {
			draw(helper, vat, player);
			first[0] = temper(player, EnumBloodTemper.FLAMMEUS);
			first[1] = temper(player, EnumBloodTemper.LUX);
			helper.assertTrue(first[0] > 0.0F && first[1] > 0.0F, "three pulls gave Flammeus " + first[0] + " and Lux " + first[1]);
			helper.assertTrue(temper(player, EnumBloodTemper.ANIMUS) == 0.0F, "the blaze gave a school it does not have");
			feed(helper, vat, player);
		});
		helper.runAtTickTime(110, () -> draw(helper, vat, player));
		helper.runAtTickTime(120, () -> {
			draw(helper, vat, player);
			helper.assertTrue(temper(player, EnumBloodTemper.FLAMMEUS) > first[0]
					&& temper(player, EnumBloodTemper.LUX) > first[1], "the refed blaze gave nothing more");
			// Neglect: an hour and a half untended and unfed.
			vat.buffer().setBloodVolume(0.0D);
			vat.kept().setVigor(vat.kept().vigor(), helper.getLevel().getGameTime() - 130_000L);
			seconds(helper, vat, 1);
			try {
				helper.assertTrue(vat.kept().husk(), "a neglected blaze did not die");
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	// ── The vessel's screen (round two P6) ──

	private static SpecimenVesselMenu openMenu(ServerPlayer player) {
		return player.containerMenu instanceof SpecimenVesselMenu menu ? menu : null;
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anEmptyHandOpensTheVesselScreenNotChat(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = keeper(helper, 3, 1_000.0D)) {
			ServerPlayer player = captured.player;
			player.setPos(feet(helper, 4, 6));
			SpecimenVatBlockEntity vat = vat(helper, 6, 6, player);
			keep(helper, vat, EntityType.COW, 64.0D);
			vat.buffer().setBloodVolume(345.0D);
			player.getInventory().clearContent();
			int said = captured.chat.size();
			helper.assertTrue(SpecimenVessels.useEmptyHand(helper.getLevel(), vat.getBlockPos(), vat, player, vat::fits),
					"an empty hand on a kept cow did nothing");
			SpecimenVesselMenu menu = openMenu(player);
			try {
				helper.assertTrue(menu != null, "the vessel's screen did not open: " + player.containerMenu);
				helper.assertTrue(captured.chat.size() == said, "the vessel still printed to chat: " + captured.said());
				helper.assertTrue(menu.data(VesselView.VIGOR_X100) == 6400, "Vigor read " + menu.data(VesselView.VIGOR_X100));
				helper.assertTrue(menu.data(VesselView.BUFFER) == 345 && menu.data(VesselView.CAPACITY) == 800,
						"the buffer read " + menu.data(VesselView.BUFFER) + " of " + menu.data(VesselView.CAPACITY));
				helper.assertTrue(menu.flag(VesselView.OWNED) && menu.flag(VesselView.MAY_DRAW) && menu.flag(VesselView.BREEDS),
						"the flags read " + Integer.toBinaryString(menu.data(VesselView.FLAGS)));
				helper.assertTrue(menu.data(VesselView.FEED_PRICE_X10) == 50, "a cow's feed price read " + menu.data(VesselView.FEED_PRICE_X10));
				helper.assertTrue(menu.data(VesselView.BROOD_REFUSAL) == BroodRules.Refusal.NO_MATE.ordinal(),
						"a lone cow's brood refusal read " + menu.data(VesselView.BROOD_REFUSAL));
				helper.assertTrue(menu.stillValid(player), "the screen closes on a vat beside its keeper");
				helper.assertTrue(BloodResearch.met(player, ResourceLocation.parse("minecraft:cow")), "looking at the cow did not meet it");
				// Anything in the hand keeps its own use: no screen.
				player.closeContainer();
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
				helper.assertFalse(SpecimenVessels.useEmptyHand(helper.getLevel(), vat.getBlockPos(), vat, player, vat::fits),
						"a held stick opened the screen");
				helper.assertTrue(openMenu(player) == null, "a held stick opened the screen");
			} finally {
				player.closeContainer();
			}
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aBloodHandOnAVesselFeedsAndDoesNotOpenTheScreen(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = keeper(helper, 3, 1_000.0D)) {
			ServerPlayer player = captured.player;
			SpecimenVatBlockEntity vat = vat(helper, 6, 6, player);
			keep(helper, vat, EntityType.COW, 64.0D);
			aim(player, feet(helper, 3, 6), Vec3.atCenterOf(vat.getBlockPos()));
			for (var item : List.of(ItemInit.blood_projection.get(), ItemInit.blood_absorption.get())) {
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
				player.gameMode.useItemOn(player, helper.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
						new BlockHitResult(Vec3.atCenterOf(vat.getBlockPos()), Direction.WEST, vat.getBlockPos(), false));
				helper.assertTrue(openMenu(player) == null, "a blood hand (" + item + ") opened the vessel's screen");
			}
			// The verbs still reach the vat: Projection feeds its buffer.
			double paid = SpecimenVessels.project(vat, player, vat.getBlockPos(), 50.0D);
			helper.assertTrue(paid > 0.0D, "Projection did not feed the vat");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theVesselMenuShowsYieldOnlyToAStudiedKeeper(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = keeper(helper, 3, 1_000.0D)) {
			ServerPlayer player = captured.player;
			player.setPos(feet(helper, 4, 6));
			SpecimenVatBlockEntity vat = vat(helper, 6, 6, player);
			keep(helper, vat, EntityType.COW, 80.0D);
			player.getInventory().clearContent();
			SpecimenVessels.useEmptyHand(helper.getLevel(), vat.getBlockPos(), vat, player, vat::fits);
			SpecimenVesselMenu menu = openMenu(player);
			try {
				helper.assertTrue(menu != null, "the screen did not open");
				helper.assertTrue(menu.data(VesselView.TEMPER_YIELD_X100) == 0 && menu.data(VesselView.BLOOD_YIELD_X100) == 0,
						"an unstudied kind showed a yield");
				ClinicalBloodKnowledge.examined(player, vial("minecraft:cow", "a"));
				ClinicalBloodKnowledge.examined(player, vial("minecraft:cow", "b"));
				helper.assertTrue(menu.data(VesselView.TEMPER_YIELD_X100) == 0, "two individuals showed a yield");
				ClinicalBloodKnowledge.examined(player, vial("minecraft:cow", "c"));
				int expected = (int) Math.round(VatVigorRules.temperPerDraw(80.0D, 1.0D) * 100.0D);
				int expectedBlood = (int) Math.round(VatVigorRules.bloodPerDraw(80.0D, 1.0D) * 100.0D);
				helper.assertTrue(menu.data(VesselView.TEMPER_YIELD_X100) == expected
						&& menu.data(VesselView.BLOOD_YIELD_X100) == expectedBlood, "a studied cow's yield read "
						+ menu.data(VesselView.TEMPER_YIELD_X100) + " and " + menu.data(VesselView.BLOOD_YIELD_X100)
						+ ", not " + expected + " and " + expectedBlood);
			} finally {
				player.closeContainer();
			}
		}
		helper.succeed();
	}

	// ── Sampling in glass (round two P6) ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aVialIsRefusedOnASyringeOnlyKind(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = keeper(helper, 3, 1_000.0D)) {
			ServerPlayer player = captured.player;
			SpecimenVatBlockEntity vat = vat(helper, 6, 6, player);
			keep(helper, vat, EntityType.BLAZE, 60.0D);
			helper.assertTrue(com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData.profile(EntityType.BLAZE, false)
					.requiresLivingSyringe(), "the fixture needs a kind only the syringe pierces");
			ItemStack vial = new ItemStack(ItemInit.bloody_vial.get());
			player.setItemInHand(InteractionHand.MAIN_HAND, vial);
			ItemStack sample = SpecimenVessels.sample(helper.getLevel(), vat.getBlockPos(), vat, player, vial, InteractionHand.MAIN_HAND);
			helper.assertTrue(sample.isEmpty(), "a vial drew a blaze in glass");
			helper.assertTrue(Math.abs(vat.kept().vigor() - 60.0D) < 1.0E-6D, "the refused vial cost Vigor");
			helper.assertFalse(BloodSampleData.isFilled(player.getMainHandItem()), "the vial was filled anyway");
			helper.assertTrue(captured.said().contains("too tough"), "the drawer was not told why: " + captured.said());
		}
		helper.succeed();
	}

	/** A Living Syringe in the main hand with an empty rack loaded from the inventory. */
	private static ItemStack loadedSyringe(GameTestHelper helper, ServerPlayer player) {
		ItemStack syringe = new ItemStack(ItemInit.living_syringe.get());
		ItemStack rack = new ItemStack(ItemInit.vial_rack.get());
		VialRackItem.ensureInitialized(rack);
		player.getInventory().setItem(1, rack);
		player.setItemInHand(InteractionHand.MAIN_HAND, syringe);
		syringe.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
		return syringe;
	}

	private static List<ItemStack> rackSamples(GameTestHelper helper, ItemStack syringe) {
		CompoundTag tag = syringe.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		ItemStack rack = ItemStack.parseOptional(helper.getLevel().registryAccess(), tag.getCompound(LivingSyringeItem.TAG_LOADED_RACK));
		if (rack.isEmpty()) return List.of();
		return VialRackItem.getVials(rack).stream().filter(BloodSampleData::isFilled).toList();
	}

	private static InteractionResult useOn(GameTestHelper helper, ServerPlayer player, BlockPos pos) {
		return player.gameMode.useItemOn(player, helper.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(pos), Direction.WEST, pos, false));
	}

	/**
	 * CQ-4: only a vessel's owner and their bloodline take its husk, lead a creature into it or lift it. A stranger is
	 * told why and nothing moves; a vessel nobody owns stays open to all.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aStrangerMayNotEmptyFillOrLiftAnotherKeepersVessel(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		Husk wild = helper.spawn(EntityType.HUSK, new BlockPos(6, 1, 3));
		wild.setNoAi(true);
		wild.setHealth(4.0F);
		try (CapturedPlayer owner = keeper(helper, 3, 1_000.0D); CapturedPlayer stranger = keeper(helper, 3, 1_000.0D)) {
			ServerPlayer player = stranger.player;
			player.setPos(feet(helper, 4, 4));
			player.getInventory().clearContent();

			SpecimenVatBlockEntity husked = vat(helper, 6, 6, owner.player);
			keep(helper, husked, EntityType.COW, 100.0D);
			husked.kept().setVigor(1.0D, level.getGameTime() - 1_300L);
			seconds(helper, husked, 1);
			helper.assertTrue(husked.kept().husk(), "the fixture's cow did not die");
			SpecimenVessels.useEmptyHand(level, husked.getBlockPos(), husked, player, husked::fits);
			helper.assertTrue(husked.kept().husk() && player.getInventory().countItem(Items.BONE_MEAL) == 0,
					"a stranger took the owner's husk");
			helper.assertTrue(stranger.said().contains("outside your bloodline"), "the stranger was not told why: " + stranger.said());

			SpecimenVatBlockEntity empty = vat(helper, 6, 2, owner.player);
			MenagerieLeash.leash(player, wild);
			SpecimenVessels.useEmptyHand(level, empty.getBlockPos(), empty, player, empty::fits);
			helper.assertTrue(!empty.kept().occupied() && wild.isAlive() && MenagerieLeash.leads(player, wild),
					"a stranger led a creature into the owner's vat");

			player.setShiftKeyDown(true);
			useOn(helper, player, empty.getBlockPos());
			helper.assertTrue(level.getBlockEntity(empty.getBlockPos()) instanceof SpecimenVatBlockEntity,
					"a stranger lifted the owner's vat");
			BlockPos jarPos = at(helper, 2, 1, 6);
			level.setBlock(jarPos, BlockInit.specimen_jar.get().defaultBlockState(), 3);
			var jar = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.SpecimenJarBlockEntity) level.getBlockEntity(jarPos);
			jar.buffer().setOwner(owner.player.getUUID());
			useOn(helper, player, jarPos);
			helper.assertTrue(level.getBlockState(jarPos).is(BlockInit.specimen_jar.get()), "a stranger picked up the owner's jar");
			jar.buffer().setOwner(null);
			useOn(helper, player, jarPos);
			helper.assertTrue(level.getBlockState(jarPos).isAir() && player.getInventory().countItem(BlockInit.specimen_jar.get().asItem()) == 1,
					"a jar nobody owns could not be picked up");
			player.setShiftKeyDown(false);
		} finally {
			MenagerieLeash.release(wild, false);
			wild.discard();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aSyringeSamplesAKeptCreatureForThreeVigor(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = keeper(helper, 3, 1_000.0D); CapturedPlayer stranger = keeper(helper, 3, 1_000.0D)) {
			ServerPlayer player = captured.player;
			SpecimenVatBlockEntity vat = vat(helper, 6, 6, player);
			keep(helper, vat, EntityType.BLAZE, 60.0D);
			// A syringe with nothing to fill costs the creature nothing.
			player.getInventory().clearContent();
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.living_syringe.get()));
			useOn(helper, player, vat.getBlockPos());
			helper.assertTrue(Math.abs(vat.kept().vigor() - 60.0D) < 1.0E-6D, "an empty syringe cost Vigor");
			helper.assertTrue(openMenu(player) == null, "the syringe opened the vessel's screen");
			ItemStack syringe = loadedSyringe(helper, player);
			InteractionResult used = useOn(helper, player, vat.getBlockPos().above());
			helper.assertTrue(used.consumesAction(), "the syringe's use on the vat's top was not taken: " + used);
			helper.assertTrue(Math.abs(vat.kept().vigor() - 57.0D) < 1.0E-6D, "the syringe cost " + (60.0D - vat.kept().vigor()) + " Vigor");
			List<ItemStack> samples = rackSamples(helper, syringe);
			helper.assertTrue(samples.size() == 1 && BloodSampleData.entityType(samples.get(0)) == EntityType.BLAZE
					&& BloodSampleData.donor(samples.get(0)).startsWith("vessel:"), "the rack did not take a vessel sample: " + samples);
			helper.assertTrue(openMenu(player) == null, "the syringe opened the vessel's screen");
			// A stranger's syringe draws nothing, and nothing is charged.
			ItemStack strangers = loadedSyringe(helper, stranger.player);
			useOn(helper, stranger.player, vat.getBlockPos());
			helper.assertTrue(Math.abs(vat.kept().vigor() - 57.0D) < 1.0E-6D, "a stranger's syringe cost Vigor");
			helper.assertTrue(rackSamples(helper, strangers).isEmpty(), "a stranger drew from someone else's vat");
			// Nor does a preserved specimen give anything.
			SpecimenVatBlockEntity preserved = vat(helper, 10, 6, null);
			keep(helper, preserved, EntityType.BLAZE, 60.0D);
			useOn(helper, player, preserved.getBlockPos());
			helper.assertTrue(Math.abs(preserved.kept().vigor() - 60.0D) < 1.0E-6D, "a preserved specimen paid for a syringe");
			helper.assertTrue(rackSamples(helper, syringe).size() == 1, "a preserved specimen gave a sample");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void samplingOrKeepingMeetsAKindForTheBestiary(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = keeper(helper, 3, 1_000.0D)) {
			ServerPlayer player = captured.player;
			ResourceLocation cow = ResourceLocation.parse("minecraft:cow");
			ResourceLocation pig = ResourceLocation.parse("minecraft:pig");
			helper.assertFalse(BloodResearch.met(player, cow) || BloodResearch.met(player, pig), "a fresh keeper met a cow or pig");
			SpecimenVatBlockEntity vat = vat(helper, 6, 6, player);
			keep(helper, vat, EntityType.COW, 50.0D);
			ItemStack vial = new ItemStack(ItemInit.bloody_vial.get());
			player.setItemInHand(InteractionHand.MAIN_HAND, vial);
			helper.assertFalse(SpecimenVessels.sample(helper.getLevel(), vat.getBlockPos(), vat, player, vial,
					InteractionHand.MAIN_HAND).isEmpty(), "no sample came from the cow's vat");
			helper.assertTrue(BloodResearch.met(player, cow), "sampling a kept cow did not meet it");
			helper.assertFalse(BloodResearch.knows(player, cow), "meeting a cow read its blood");
			// Leading a pig into glass meets the pig.
			Pig led = helper.spawn(EntityType.PIG, new BlockPos(6, 1, 9));
			led.setNoAi(true);
			player.setPos(feet(helper, 4, 9));
			SpecimenVatBlockEntity empty = vat(helper, 8, 9, player);
			MenagerieLeash.leash(player, led);
			player.getInventory().clearContent();
			helper.assertTrue(SpecimenVessels.useEmptyHand(helper.getLevel(), empty.getBlockPos(), empty, player, empty::fits),
					"the led pig did not go in");
			helper.assertTrue(BloodResearch.met(player, pig), "keeping a pig did not meet it");
			CompoundTag saved = HemoCapabilityAccess.clinicalBlood(player).serializeNBT(player.registryAccess());
			helper.assertTrue(saved.getList("Met", 8).size() == 2, "the met kinds were not saved: " + saved);
			// A player who has met nothing writes no Met key at all.
			try (CapturedPlayer fresh = keeper(helper, 3, 1_000.0D)) {
				helper.assertFalse(HemoCapabilityAccess.clinicalBlood(fresh.player).serializeNBT(fresh.player.registryAccess())
						.contains("Met"), "an empty met set was written");
			}
		}
		helper.succeed();
	}

	// ── The brood (round two P6) ──

	/** A kept cow-like creature at full Vigor in a fresh vat at (x, z), its buffer at {@code buffer} mL. */
	private static SpecimenVatBlockEntity parent(GameTestHelper helper, int x, int z, ServerPlayer owner, EntityType<? extends Mob> type,
			double vigor, double buffer) {
		SpecimenVatBlockEntity vat = vat(helper, x, z, owner);
		keep(helper, vat, type, vigor);
		vat.buffer().setBloodVolume(buffer);
		return vat;
	}

	/** Runs the brood's second {@code seconds} times on {@code bearer} at the current game time. */
	private static void broodSeconds(GameTestHelper helper, SpecimenVatBlockEntity bearer, int seconds) {
		for (int i = 0; i < seconds; i++) SpecimenBrood.second(helper.getLevel(), bearer.getBlockPos(), bearer);
	}

	private static double buffers(SpecimenVatBlockEntity... vats) {
		double total = 0.0D;
		for (SpecimenVatBlockEntity vat : vats) total += vat.buffer().getBloodVolume();
		return total;
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void twoFedCowsInAdjacentVatsBearACalfIntoAnEmptyVat(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = keeper(helper, 3, 1_000.0D)) {
			ServerPlayer player = captured.player;
			player.setPos(feet(helper, 6, 3));
			SpecimenVatBlockEntity bearer = parent(helper, 6, 6, player, EntityType.COW, 100.0D, 200.0D);
			SpecimenVatBlockEntity mate = parent(helper, 7, 6, player, EntityType.COW, 100.0D, 200.0D);
			SpecimenVatBlockEntity cradle = vat(helper, 8, 6, null);
			// The keeper pairs them from the bearer's screen: the menu's button dispatches to the vessel.
			player.getInventory().clearContent();
			helper.assertTrue(SpecimenVessels.useEmptyHand(helper.getLevel(), bearer.getBlockPos(), bearer, player, bearer::fits),
					"the bearer's screen did not open");
			SpecimenVesselMenu menu = openMenu(player);
			helper.assertTrue(menu != null && menu.data(VesselView.BROOD_REFUSAL) == BroodRules.Refusal.NONE.ordinal(),
					"the screen did not offer the brood: " + (menu == null ? "no menu" : menu.data(VesselView.BROOD_REFUSAL)));
			helper.assertTrue(menu.clickMenuButton(player, SpecimenVesselMenu.BROOD_BUTTON), "the brood button was not taken");
			player.closeContainer();
			helper.assertTrue(bearer.kept().broodBearer() && mate.kept().paired() && !mate.kept().broodBearer()
					&& mate.getBlockPos().equals(bearer.kept().broodMate()), "the button did not pair them reciprocally: " + captured.said());
			double before = buffers(bearer, mate);
			double cost = BroodRules.broodCost(bearer.kept().maxHealth());
			broodSeconds(helper, bearer, BroodRules.BROOD_SECONDS - 1);
			helper.assertFalse(cradle.kept().occupied(), "the calf came a second early");
			helper.assertTrue(SpecimenBrood.state(helper.getLevel(), bearer) == BroodRules.State.GESTATING,
					"the brood is not quickening: " + SpecimenBrood.state(helper.getLevel(), bearer));
			broodSeconds(helper, bearer, 1);
			helper.assertTrue(Math.abs(before - buffers(bearer, mate) - cost) < 1.0E-6D,
					"the brood took " + (before - buffers(bearer, mate)) + " mL, not " + cost);
			helper.assertTrue(cradle.kept().living() && cradle.kept().type() == EntityType.COW, "no calf in the empty vat");
			helper.assertTrue(cradle.kept().specimen().getInt("Age") < 0, "the calf is not a baby: " + cradle.kept().specimen().getInt("Age"));
			helper.assertTrue(Math.abs(cradle.kept().vigor() - BroodRules.YOUNG_VIGOR) < 1.0E-6D, "the calf's Vigor is " + cradle.kept().vigor());
			helper.assertTrue(player.getUUID().equals(cradle.buffer().owner()), "the cradle is not the keeper's");
			double expected = 100.0D - BroodRules.BROOD_SECONDS / BroodRules.VIGOR_DRAIN_EVERY * BroodRules.VIGOR_DRAIN - BroodRules.BIRTH_VIGOR;
			helper.assertTrue(Math.abs(bearer.kept().vigor() - expected) < 1.0E-6D && Math.abs(mate.kept().vigor() - expected) < 1.0E-6D,
					"the parents are at " + bearer.kept().vigor() + " and " + mate.kept().vigor() + ", not " + expected);
			long now = helper.getLevel().getGameTime();
			helper.assertTrue(!bearer.kept().paired() && !mate.kept().paired(), "the pair stood after the birth");
			helper.assertTrue(bearer.kept().resting(now) && mate.kept().resting(now)
					&& bearer.kept().restUntil() == now + BroodRules.REST_TICKS, "the parents do not rest");
			helper.assertTrue(BloodResearch.met(player, ResourceLocation.parse("minecraft:cow")), "the keeper did not meet the calf's kind");
			helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player,
					com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_FIRST_BROOD), "no first-brood advancement");
			AABB room = AABB.encapsulatingFullBlocks(at(helper, 0, 0, 0), at(helper, 20, 8, 12));
			helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class, room).isEmpty()
					&& helper.getLevel().getEntitiesOfClass(ExperienceOrb.class, room).isEmpty()
					&& helper.getLevel().getEntitiesOfClass(Cow.class, room).isEmpty(), "the birth dropped something or set a cow loose");
			// Resting, they will not pair again.
			helper.assertTrue(SpecimenBrood.press(helper.getLevel(), bearer, player) == BroodRules.Refusal.RESTING,
					"a resting cow paired again");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aBroodPausesUnderTheVigorMarkAndTakesNoBlood(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = keeper(helper, 3, 1_000.0D)) {
			ServerPlayer player = captured.player;
			SpecimenVatBlockEntity bearer = parent(helper, 6, 6, player, EntityType.COW, 100.0D, 400.0D);
			SpecimenVatBlockEntity mate = parent(helper, 7, 6, player, EntityType.COW, 100.0D, 400.0D);
			helper.assertTrue(SpecimenBrood.press(helper.getLevel(), bearer, player) == BroodRules.Refusal.NONE, "the cows would not pair");
			mate.kept().setVigor(BroodRules.BROOD_VIGOR - 1.0D, helper.getLevel().getGameTime());
			double before = buffers(bearer, mate);
			broodSeconds(helper, bearer, 20);
			helper.assertTrue(bearer.kept().broodPaid() == 0.0D && Math.abs(buffers(bearer, mate) - before) < 1.0E-9D,
					"a brood under the mark took " + bearer.kept().broodPaid() + " mL");
			helper.assertTrue(SpecimenBrood.state(helper.getLevel(), bearer) == BroodRules.State.PAUSED, "the brood is not paused");
			helper.assertTrue(bearer.kept().paired() && mate.kept().paired(), "a pause broke the pair");
			mate.kept().setVigor(100.0D, helper.getLevel().getGameTime());
			broodSeconds(helper, bearer, 1);
			helper.assertTrue(bearer.kept().broodPaid() > 0.0D, "the fed brood did not resume");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aBroodWaitsAtTermUntilAnEmptyVesselStandsBeside(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = keeper(helper, 3, 1_000.0D)) {
			ServerPlayer player = captured.player;
			SpecimenVatBlockEntity bearer = parent(helper, 6, 6, player, EntityType.COW, 100.0D, 400.0D);
			SpecimenVatBlockEntity mate = parent(helper, 7, 6, player, EntityType.COW, 100.0D, 400.0D);
			helper.assertTrue(SpecimenBrood.press(helper.getLevel(), bearer, player) == BroodRules.Refusal.NONE, "the cows would not pair");
			broodSeconds(helper, bearer, BroodRules.BROOD_SECONDS);
			helper.assertTrue(SpecimenBrood.state(helper.getLevel(), bearer) == BroodRules.State.AT_TERM, "the brood is not at term");
			double held = buffers(bearer, mate);
			double vigor = bearer.kept().vigor();
			broodSeconds(helper, bearer, 30);
			helper.assertTrue(Math.abs(buffers(bearer, mate) - held) < 1.0E-9D && Math.abs(bearer.kept().vigor() - vigor) < 1.0E-9D,
					"a brood at term kept paying");
			helper.assertTrue(helper.getLevel().getEntitiesOfClass(Cow.class, AABB.encapsulatingFullBlocks(at(helper, 0, 0, 0), at(helper, 20, 8, 12))).isEmpty(),
					"a calf was set loose for want of glass");
			// A vessel owned by someone else is no cradle; an unowned one beside the mate is.
			try (CapturedPlayer other = keeper(helper, 3, 1_000.0D)) {
				SpecimenVatBlockEntity foreign = vat(helper, 5, 6, other.player);
				broodSeconds(helper, bearer, 1);
				helper.assertFalse(foreign.kept().occupied(), "the calf went into another keeper's vat");
			}
			SpecimenVatBlockEntity cradle = vat(helper, 7, 7, null);
			broodSeconds(helper, bearer, 1);
			helper.assertTrue(cradle.kept().living() && cradle.kept().type() == EntityType.COW, "the waiting calf did not go into the new vat");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void strangersTheDeadDifferentKindsAndRestingPairsAreRefused(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = keeper(helper, 3, 1_000.0D); CapturedPlayer stranger = keeper(helper, 3, 1_000.0D)) {
			ServerPlayer player = captured.player;
			var level = helper.getLevel();
			long now = level.getGameTime();
			// A stranger may not pair someone else's cows.
			SpecimenVatBlockEntity cow = parent(helper, 1, 1, player, EntityType.COW, 100.0D, 0.0D);
			parent(helper, 2, 1, player, EntityType.COW, 100.0D, 0.0D);
			helper.assertTrue(SpecimenBrood.press(level, cow, stranger.player) == BroodRules.Refusal.NOT_YOURS, "a stranger paired them");
			// The dead do not breed.
			SpecimenVatBlockEntity zombie = parent(helper, 5, 1, player, EntityType.ZOMBIE, 100.0D, 0.0D);
			parent(helper, 6, 1, player, EntityType.ZOMBIE, 100.0D, 0.0D);
			helper.assertTrue(SpecimenBrood.press(level, zombie, player) == BroodRules.Refusal.UNDEAD, "two zombies paired");
			// Different kinds find no mate.
			SpecimenVatBlockEntity pig = parent(helper, 9, 1, player, EntityType.PIG, 100.0D, 0.0D);
			parent(helper, 10, 1, player, EntityType.COW, 100.0D, 0.0D);
			helper.assertTrue(SpecimenBrood.press(level, pig, player) == BroodRules.Refusal.NO_MATE, "a pig paired with a cow");
			// A resting cow, and a cow beside a resting one.
			SpecimenVatBlockEntity rested = parent(helper, 13, 1, player, EntityType.COW, 100.0D, 0.0D);
			SpecimenVatBlockEntity beside = parent(helper, 14, 1, player, EntityType.COW, 100.0D, 0.0D);
			rested.kept().restUntil(now + 100L);
			helper.assertTrue(SpecimenBrood.press(level, rested, player) == BroodRules.Refusal.RESTING, "a resting cow paired");
			helper.assertTrue(SpecimenBrood.press(level, beside, player) == BroodRules.Refusal.MATE_RESTING, "a cow paired with a resting one");
			// Another keeper's cow beside it.
			SpecimenVatBlockEntity mine = parent(helper, 1, 5, player, EntityType.COW, 100.0D, 0.0D);
			parent(helper, 2, 5, stranger.player, EntityType.COW, 100.0D, 0.0D);
			helper.assertTrue(SpecimenBrood.press(level, mine, player) == BroodRules.Refusal.OTHER_KEEPER, "two keepers' cows paired");
			// A husk beside it.
			SpecimenVatBlockEntity widow = parent(helper, 5, 5, player, EntityType.COW, 100.0D, 0.0D);
			SpecimenVatBlockEntity husk = parent(helper, 6, 5, player, EntityType.COW, 1.0D, 0.0D);
			husk.kept().setVigor(1.0D, now - 2_000L);
			husk.kept().tick(level, husk.getBlockPos(), husk);
			helper.assertTrue(husk.kept().husk(), "the fixture's cow did not die");
			helper.assertTrue(SpecimenBrood.press(level, widow, player) == BroodRules.Refusal.MATE_HUSK, "a cow paired with a husk");
			// A cow already paired is no one else's mate.
			SpecimenVatBlockEntity a = parent(helper, 9, 5, player, EntityType.COW, 100.0D, 0.0D);
			SpecimenVatBlockEntity b = parent(helper, 10, 5, player, EntityType.COW, 100.0D, 0.0D);
			SpecimenVatBlockEntity c = parent(helper, 11, 5, player, EntityType.COW, 100.0D, 0.0D);
			helper.assertTrue(SpecimenBrood.press(level, a, player) == BroodRules.Refusal.NONE, "a and b would not pair");
			helper.assertTrue(SpecimenBrood.press(level, c, player) == BroodRules.Refusal.MATE_PAIRED, "c took b from a");
			// A kind tagged not to breed.
			SpecimenVatBlockEntity polyp = vat(helper, 13, 5, player);
			Mob made = (Mob) com.vincenthuto.hemomancy.common.init.EntityInit.morphling_polyp.get().create(level);
			polyp.kept().keep(SpecimenJarData.captureEntity(made), made.getMaxHealth(), 100.0D, now);
			made.discard();
			helper.assertTrue(SpecimenBrood.press(level, polyp, player) == BroodRules.Refusal.CANNOT_BREED, "a polyp would breed");
			// Pressing again parts a pair, on both sides.
			helper.assertTrue(SpecimenBrood.press(level, a, player) == BroodRules.Refusal.NONE && !a.kept().paired() && !b.kept().paired(),
					"a second press did not part them");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void breakingOrPickingUpAParentLosesTheBrood(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = keeper(helper, 3, 1_000.0D)) {
			ServerPlayer player = captured.player;
			var level = helper.getLevel();
			SpecimenVatBlockEntity bearer = parent(helper, 6, 3, player, EntityType.COW, 100.0D, 400.0D);
			SpecimenVatBlockEntity mate = parent(helper, 7, 3, player, EntityType.COW, 100.0D, 400.0D);
			helper.assertTrue(SpecimenBrood.press(level, bearer, player) == BroodRules.Refusal.NONE, "the cows would not pair");
			broodSeconds(helper, bearer, 30);
			helper.assertTrue(bearer.kept().broodPaid() > 0.0D, "the brood took nothing");
			BlockPos matePos = mate.getBlockPos();
			level.destroyBlock(matePos, false);
			helper.assertTrue(!bearer.kept().paired() && bearer.kept().broodPaid() == 0.0D, "the bearer kept its brood when the mate's vat broke");
			// Picking up a parent's vat by sneaking with an empty hand loses the brood too, and the lifted vat carries no pair.
			SpecimenVatBlockEntity second = parent(helper, 6, 8, player, EntityType.COW, 100.0D, 400.0D);
			SpecimenVatBlockEntity third = parent(helper, 7, 8, player, EntityType.COW, 100.0D, 400.0D);
			helper.assertTrue(SpecimenBrood.press(level, second, player) == BroodRules.Refusal.NONE, "the second pair would not pair");
			broodSeconds(helper, second, 10);
			player.getInventory().clearContent();
			player.setShiftKeyDown(true);
			useOn(helper, player, third.getBlockPos());
			player.setShiftKeyDown(false);
			helper.assertTrue(level.getBlockState(third.getBlockPos()).isAir(), "the sneak use did not lift the vat");
			helper.assertTrue(!second.kept().paired() && second.kept().broodPaid() == 0.0D, "the bearer kept its brood when its mate was lifted");
			ItemStack lifted = player.getInventory().items.stream().filter(stack -> stack.is(BlockInit.specimen_vat.get().asItem()))
					.findFirst().orElse(ItemStack.EMPTY);
			KeptSpecimen carried = new KeptSpecimen();
			carried.loadFromItem(lifted, level.getGameTime());
			helper.assertTrue(carried.living() && !carried.paired(), "the lifted vat lost its cow or kept its pair");
			// Pairs end on both sides when a parent dies too.
			SpecimenVatBlockEntity d = parent(helper, 12, 3, player, EntityType.COW, 100.0D, 400.0D);
			SpecimenVatBlockEntity e = parent(helper, 13, 3, player, EntityType.COW, 100.0D, 0.0D);
			helper.assertTrue(SpecimenBrood.press(level, d, player) == BroodRules.Refusal.NONE, "the third pair would not pair");
			e.kept().setVigor(0.5D, level.getGameTime() - 2_000L);
			e.kept().tick(level, e.getBlockPos(), e);
			helper.assertTrue(e.kept().husk() && !d.kept().paired(), "a parent died and its mate kept the brood");
		} finally {
			// The broken vat let its cow go; it is this test's own.
			helper.getLevel().getEntitiesOfClass(Cow.class, AABB.encapsulatingFullBlocks(at(helper, 0, 0, 0), at(helper, 20, 8, 12))).forEach(Cow::discard);
		}
		helper.succeed();
	}

	/** A cow carrying {@code school} as an inherited school, in a fresh vat. */
	private static SpecimenVatBlockEntity taintedCow(GameTestHelper helper, int x, int z, ServerPlayer owner, EnumBloodTemper school) {
		SpecimenVatBlockEntity vat = vat(helper, x, z, owner);
		Cow cow = EntityType.COW.create(helper.getLevel());
		HemoCapabilityAccess.getAnimalTaint(cow).inherited = school;
		vat.kept().keep(SpecimenJarData.captureEntity(cow), cow.getMaxHealth(), 100.0D, helper.getLevel().getGameTime());
		cow.discard();
		vat.buffer().setBloodVolume(400.0D);
		return vat;
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aCalfOfParentsWithAnInheritedTemperCarriesIt(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = keeper(helper, 3, 1_000.0D)) {
			ServerPlayer player = captured.player;
			SpecimenVatBlockEntity bearer = taintedCow(helper, 6, 6, player, EnumBloodTemper.LUX);
			taintedCow(helper, 7, 6, player, EnumBloodTemper.LUX);
			SpecimenVatBlockEntity cradle = vat(helper, 6, 7, player);
			helper.assertTrue(bearer.kept().inheritedSchool() == EnumBloodTemper.LUX, "the fixture's cow lost its school");
			helper.assertTrue(SpecimenBrood.press(helper.getLevel(), bearer, player) == BroodRules.Refusal.NONE, "the cows would not pair");
			broodSeconds(helper, bearer, BroodRules.BROOD_SECONDS);
			helper.assertTrue(cradle.kept().living(), "no calf was born");
			helper.assertTrue(cradle.kept().inheritedSchool() == EnumBloodTemper.LUX,
					"a calf of two Lux-carrying parents carries " + cradle.kept().inheritedSchool());
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theBroodNeverAdvancesWithoutBloodInTheBuffers(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = keeper(helper, 3, 1_000.0D)) {
			ServerPlayer player = captured.player;
			var level = helper.getLevel();
			// Rule 2: full Vigor, a Vigil's tending, and nothing in either buffer. No blood, no young.
			SpecimenVatBlockEntity bearer = parent(helper, 6, 6, player, EntityType.COW, 100.0D, 0.0D);
			SpecimenVatBlockEntity mate = parent(helper, 7, 6, player, EntityType.COW, 100.0D, 0.0D);
			SpecimenVatBlockEntity cradle = vat(helper, 8, 6, player);
			bearer.kept().tendUntil(level.getGameTime() + 100_000L);
			mate.kept().tendUntil(level.getGameTime() + 100_000L);
			helper.assertTrue(SpecimenBrood.press(level, bearer, player) == BroodRules.Refusal.NONE, "the cows would not pair");
			broodSeconds(helper, bearer, BroodRules.BROOD_SECONDS * 2);
			helper.assertTrue(bearer.kept().broodPaid() == 0.0D && !cradle.kept().occupied(), "a brood grew on nothing");
			// And full buffers with no press grow nothing either.
			SpecimenVatBlockEntity lone = parent(helper, 6, 9, player, EntityType.COW, 100.0D, 400.0D);
			parent(helper, 7, 9, player, EntityType.COW, 100.0D, 400.0D);
			SpecimenVatBlockEntity empty = vat(helper, 8, 9, player);
			broodSeconds(helper, lone, BroodRules.BROOD_SECONDS * 2);
			helper.assertTrue(!empty.kept().occupied() && Math.abs(lone.buffer().getBloodVolume() - 400.0D) < 1.0E-9D,
					"unpaired cows bred on their own");
		}
		helper.succeed();
	}

	/** Projection fills the vat's buffer from the keeper, and the buffer feeds the creature to full. */
	private static void feed(GameTestHelper helper, SpecimenVatBlockEntity vat, ServerPlayer player) {
		double before = blood(player);
		for (int i = 0; i < 10; i++) SpecimenVessels.project(vat, player, vat.getBlockPos(), 100.0D);
		helper.assertTrue(blood(player) < before, "feeding the vat cost the keeper nothing");
		seconds(helper, vat, 100);
		helper.assertTrue(vat.kept().vigor() > 90.0D, "the buffer fed the blaze only to " + vat.kept().vigor());
	}

	private static void draw(GameTestHelper helper, SpecimenVatBlockEntity vat, ServerPlayer player) {
		aim(player, feet(helper, 1, 7), Vec3.atCenterOf(vat.getBlockPos()));
		double vigor = vat.kept().vigor();
		BlockBloodInteractions.tryAbsorbFromLookedAtBlock(helper.getLevel(), player, 5.5D, 1.0D);
		helper.assertTrue(vat.kept().vigor() < vigor, "a held draw on the vat pulled nothing");
	}

	// ── Joined vats ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void fourEmptyVatsSideBySideJoinIntoOneVesselTwoAcross(GameTestHelper helper) {
		clear(helper);
		setDown(helper, 3, 1, 3);
		setDown(helper, 4, 1, 3);
		setDown(helper, 3, 1, 4);
		setDown(helper, 4, 1, 4);
		SpecimenVatBlockEntity corner = vatAt(helper, 3, 1, 3);
		SpecimenVatBlockEntity far = vatAt(helper, 4, 1, 4);
		helper.assertTrue(corner.runsVessel() && !far.runsVessel() && far.runner() == corner, "the lowest corner runs the vessel");
		helper.assertTrue(far.vats() == 4 && far.width() == 2 && far.layers() == 1, "a vessel two across and one high");
		helper.assertTrue(far.bufferCapacity() == VatVigorRules.VAT_BUFFER * 4, "it holds 800 mL a vat");
		helper.assertTrue(helper.getLevel().getBlockState(corner.getBlockPos()).getValue(SpecimenVatBlock.PART) == VatPart.W2_ONLY
				&& helper.getLevel().getBlockState(far.getBlockPos()).getValue(SpecimenVatBlock.PART) == VatPart.HIDDEN,
				"the corner vat draws the whole vessel and the others nothing");
		Mob horse = EntityType.HORSE.create(helper.getLevel());
		helper.assertTrue(far.fits(horse) && !SpecimenVatBlock.fits(horse, 1, 1), "a horse fits two across, not one vat");
		horse.discard();
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void stackedVatsJoinHighAndTakingOneAwayKeepsTheRestsShareOfBlood(GameTestHelper helper) {
		clear(helper);
		SpecimenVatBlockEntity bottom = setDown(helper, 3, 1, 3);
		setDown(helper, 3, 3, 3);
		helper.assertTrue(bottom.vats() == 2 && bottom.layers() == 2 && vatAt(helper, 3, 3, 3).runner() == bottom,
				"two vats stacked are one vessel two high");
		bottom.buffer().setBloodVolume(1000.0D);
		helper.assertTrue(vatAt(helper, 3, 3, 3).buffer().getBloodVolume() == 1000.0D, "either vat reads the one buffer");
		helper.getLevel().setBlock(at(helper, 3, 3, 3), Blocks.AIR.defaultBlockState(), 3);
		helper.assertTrue(bottom.vats() == 1 && bottom.runsVessel(), "the vat left stands alone");
		helper.assertTrue(Math.abs(bottom.buffer().getBloodVolume() - 500.0D) < 1.0E-6D, "it keeps its own half of the blood");
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void breakingAnyVatOfAKeptVesselLetsTheCreatureGoAndTheRestStandApart(GameTestHelper helper) {
		clear(helper);
		setDown(helper, 3, 1, 3);
		setDown(helper, 4, 1, 3);
		setDown(helper, 3, 1, 4);
		setDown(helper, 4, 1, 4);
		SpecimenVatBlockEntity corner = vatAt(helper, 3, 1, 3);
		keep(helper, corner, EntityType.HORSE, 80.0D);
		helper.getLevel().setBlock(at(helper, 4, 1, 4), Blocks.AIR.defaultBlockState(), 3);
		List<Mob> freed = helper.getLevel().getEntitiesOfClass(Mob.class, new AABB(at(helper, 3, 1, 3)).inflate(4.0D),
				mob -> mob.getType() == EntityType.HORSE);
		helper.assertTrue(freed.size() == 1, "the horse is let go");
		helper.assertTrue(!corner.kept().occupied() && corner.vats() == 1 && vatAt(helper, 4, 1, 3).vats() == 1
				&& vatAt(helper, 3, 1, 4).vats() == 1, "three vats cannot make a square; they stand apart");
		helper.assertTrue(helper.getLevel().getBlockState(corner.getBlockPos()).getValue(SpecimenVatBlock.PART) == VatPart.SINGLE,
				"a lone vat draws its own column again");
		freed.forEach(Mob::discard);
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aVesselKeepingACreatureNeverReforms(GameTestHelper helper) {
		clear(helper);
		SpecimenVatBlockEntity kept = setDown(helper, 3, 1, 3);
		keep(helper, kept, EntityType.COW, 80.0D);
		setDown(helper, 4, 1, 3);
		setDown(helper, 3, 1, 4);
		setDown(helper, 4, 1, 4);
		helper.assertTrue(kept.vats() == 1 && kept.kept().occupied(), "the cow's vat stays as it is");
		helper.assertTrue(vatAt(helper, 4, 1, 3).vats() == 1, "three empty vats around it cannot make a square");
		helper.succeed();
	}
}
