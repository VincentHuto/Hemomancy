package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.PlayerStateSync;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemperamentEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperCatalysts;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperCharge;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperRules;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodTemperamentServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodTemper;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationDiagnosticsSync;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.PacketSyncManipulationCostDiagnostics;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Core loop Track 4, Temper: the spendable pool per school beside Temperament. A cast with Temper costs and hits as
 * before; without Temper or a catalyst it costs more and lands softer, and says why. Drawing from a creature fills
 * its schools; coal in the off hand covers a Pyretic Forge pulse; a tier 3 scar holds its school through a long
 * fight; death keeps half; conjurations charge nothing; and the client hears grown and scar alignment apart.
 */
@GameTestHolder("temper_validation")
@PrefixGameTestTemplate(false)
public final class TemperValidationGameTests {
	private static final String EMPTY = "bastion/mobs/empty";

	private TemperValidationGameTests() {
	}

	// ── Fixtures ──

	/** Active blood, {@code manipulation} known, equipped and selected, and enough alignment for its gate. */
	private static void prepare(ServerPlayer player, BloodManipulation manipulation, double blood) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(Math.max(volume.getMaxBloodVolume(), blood));
		volume.setBloodVolume(blood);
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		if (manipulation == null) return;
		known.getKnownManips().put(manipulation, new ManipLevel(4, 185));
		known.getKnownManips().put(ManipulationInit.getByName(ManipulationFamilyRegistry.baselineId(manipulation.getName())),
				new ManipLevel(4, 185));
		known.setEquippedManipNames(new ArrayList<>(List.of(manipulation.getName())));
		known.setSelectedManip(manipulation);
		HemoCapabilityAccess.getBloodTemperament(player).setTemperAlignment(manipulation.getTend(),
				(float) manipulation.getAlignLevel() + 1.0F);
	}

	private static void fill(ServerPlayer player, EnumBloodTemper school, float amount) {
		HemoCapabilityAccess.getBloodTemper(player).set(school, amount, TemperCharge.cap(player, school));
	}

	private static float held(ServerPlayer player, EnumBloodTemper school) {
		return HemoCapabilityAccess.getBloodTemper(player).get(school);
	}

	private static double blood(ServerPlayer player) {
		return HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
	}

	private static void clear(GameTestHelper helper) {
		for (int x = 0; x <= 7; x++) {
			for (int z = 0; z <= 7; z++) {
				helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
				for (int y = 1; y <= 4; y++) helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
			}
		}
	}

	private static <T extends Mob> T still(GameTestHelper helper, EntityType<T> type, Vec3 relative, float health) {
		T mob = type.create(helper.getLevel());
		mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
		if (mob.getAttribute(Attributes.ARMOR) != null) mob.getAttribute(Attributes.ARMOR).setBaseValue(0);
		mob.setHealth(health);
		mob.setNoAi(true);
		mob.setNoGravity(true);
		mob.setPos(helper.absoluteVec(relative));
		helper.getLevel().addFreshEntity(mob);
		return mob;
	}

	private static void near(GameTestHelper helper, double expected, double actual, double tolerance, String message) {
		helper.assertTrue(Math.abs(expected - actual) <= tolerance, message + ": expected " + expected + ", got " + actual);
	}

	// ── The one rule ──

	/** One Synaptic Storm at full charge, tempered and dry, into identical husks: the dry one costs x1.25 and hits x0.9. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aDryCastCostsMoreAndLandsSofterAndSaysWhy(GameTestHelper helper) {
		clear(helper);
		var storm = ManipulationInit.synaptic_storm.get();
		double[] paid = new double[2];
		float[] damage = new float[2];
		String dryNotice = "";
		for (int run = 0; run < 2; run++) {
			boolean tempered = run == 0;
			try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
				ServerPlayer player = captured.player;
				player.setPos(helper.absoluteVec(new Vec3(2.5, 1, 2.5)));
				prepare(player, storm, 3000.0D);
				if (tempered) fill(player, EnumBloodTemper.DUCTILIS, 50.0F);
				LivingEntity husk = still(helper, EntityType.HUSK, new Vec3(4.5, 1, 2.5), 100.0F);
				var ledger = ManipulationCostLedger.collect(player, storm, 1.0D).contributions().stream()
						.filter(contribution -> contribution.sourceId().equals("temper")).findFirst();
				helper.assertTrue(ledger.isPresent(), "the ledger has no Temper line");
				helper.assertTrue(ledger.get().condition().endsWith(tempered ? ".tempered" : ".dry"),
						"the ledger's Temper line reads " + ledger.get().condition());
				helper.assertTrue(ledger.get().category() == ManipulationCostContribution.Category.TEMPER, "Temper line category");
				helper.assertTrue(storm.tryPerformAction(player, helper.getLevel(), ItemStack.EMPTY, player.blockPosition(),
						storm.getRequiredChargeTicks()), (tempered ? "tempered" : "dry") + " Storm refused: nothing is refused for Temper");
				paid[run] = 3000.0D - blood(player);
				damage[run] = husk.getMaxHealth() - husk.getHealth();
				husk.discard();
				if (tempered) {
					near(helper, 42.0D, held(player, EnumBloodTemper.DUCTILIS), 1.0E-4D, "a Perfectus cast burns eight Ductilis (LP-14)");
				} else {
					near(helper, 0.0D, held(player, EnumBloodTemper.DUCTILIS), 1.0E-4D, "a dry cast leaves the pool as it was");
					helper.assertTrue(HemoCapabilityAccess.getBloodTemper(player).hasSeen(EnumBloodTemper.DUCTILIS),
							"a dry school does not show on the HUD");
					dryNotice = captured.said();
				}
			}
		}
		helper.assertTrue(paid[0] > 0 && damage[0] > 0, "the tempered Storm paid " + paid[0] + " and dealt " + damage[0]);
		near(helper, paid[0] * TemperRules.Kind.CAST.dryCostMultiplier(), paid[1], 1.0E-3D, "the dry Storm's price");
		near(helper, damage[0] * TemperRules.Kind.CAST.dryStrength(), damage[1], 1.0E-3D, "the dry Storm's damage");
		helper.assertTrue(dryNotice.contains("Dry of"), "the dry cast did not say why: " + dryNotice);
		helper.succeed();
	}

	/** Dissolving: the off hand first, then the pack; one item, its worth into the pool, the cost out of it. */
	@GameTest(template = EMPTY)
	public static void aShortPoolDissolvesTheOffHandCatalystFirst(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			var rebuke = ManipulationInit.hematic_rebuke.get();
			prepare(player, rebuke, 2000.0D);
			// 26 plus glowroot and emberpod, the catalyst crops (round two, P7).
			helper.assertTrue(TemperCatalysts.all().size() == 28, "the catalyst table did not load: " + TemperCatalysts.all().size());
			player.getInventory().items.set(5, new ItemStack(ItemInit.neurotic_enzyme.get(), 2));
			player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ItemInit.neurotic_spores.get(), 2));
			TemperCharge.Charge charge = TemperCharge.forCast(player, rebuke, 1.0F, false);
			helper.assertTrue(charge != null && charge.outcome() == TemperRules.Outcome.CATALYST, "a short pool did not plan a catalyst");
			TemperCharge.commit(player, charge);
			helper.assertTrue(player.getOffhandItem().getCount() == 1, "the off-hand spores were not the one dissolved");
			helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_CARRIED),
					"a dissolved catalyst did not grant Carried (core-loop Track 11)");
			helper.assertTrue(player.getInventory().items.get(5).getCount() == 2, "the pack's enzyme went before the off hand");
			near(helper, 5.0D - 3.5D, held(player, EnumBloodTemper.DUCTILIS), 1.0E-4D, "spores (5) less a Mediocritas cast (3.5, LP-14)");
			player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
			TemperCharge.commit(player, TemperCharge.forCast(player, rebuke, 1.0F, false));
			helper.assertTrue(player.getInventory().items.get(5).getCount() == 1, "the pack's enzyme was not dissolved next");
			near(helper, 1.5D + 10.0D - 3.5D, held(player, EnumBloodTemper.DUCTILIS), 1.0E-4D, "an enzyme is worth ten");
			helper.assertTrue(captured.said().contains("dissolves into your"), "the catalyst was not named: " + captured.said());
			// A catalyst of another school is no help.
			fill(player, EnumBloodTemper.DUCTILIS, 0.0F);
			player.getInventory().items.set(5, new ItemStack(ItemInit.fervent_enzyme.get(), 1));
			helper.assertTrue(TemperCharge.forCast(player, rebuke, 1.0F, false).dry(), "a Fervent enzyme paid for Ductilis");
		}
		helper.succeed();
	}

	/** CB-19: the smallest carried catalyst that covers the shortfall is spent, wherever it sits; the off hand wins a tie. */
	@GameTest(template = EMPTY)
	public static void aShortPoolSpendsTheSmallestCatalystThatCovers(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			var rebuke = ManipulationInit.hematic_rebuke.get();
			var storm = ManipulationInit.synaptic_storm.get();
			prepare(player, rebuke, 2000.0D);
			var items = player.getInventory().items;
			items.set(1, new ItemStack(ItemInit.neurotic_enzyme.get(), 1));
			items.set(2, new ItemStack(ItemInit.neurotic_spores.get(), 1));
			items.set(3, new ItemStack(Items.SPIDER_EYE, 1));
			fill(player, EnumBloodTemper.DUCTILIS, 0.0F);
			TemperCharge.commit(player, TemperCharge.forCast(player, rebuke, 1.0F, false));
			helper.assertTrue(items.get(2).isEmpty(), "a shortfall of 3.5 did not spend the spores (5), the smallest that covers");
			helper.assertTrue(items.get(1).getCount() == 1 && items.get(3).getCount() == 1,
					"the enzyme went first, or the spider eye (3), which does not cover, was spent");
			fill(player, EnumBloodTemper.DUCTILIS, 0.0F);
			TemperCharge.commit(player, TemperCharge.forCast(player, storm, 1.0F, false));
			helper.assertTrue(items.get(1).isEmpty(), "a shortfall of 8 did not spend the enzyme (10)");
			helper.assertTrue(items.get(3).getCount() == 1, "the spider eye went where only the enzyme covered");
			fill(player, EnumBloodTemper.DUCTILIS, 0.0F);
			items.set(3, new ItemStack(ItemInit.neurotic_spores.get(), 1));
			player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ItemInit.neurotic_spores.get(), 1));
			TemperCharge.commit(player, TemperCharge.forCast(player, rebuke, 1.0F, false));
			helper.assertTrue(player.getOffhandItem().isEmpty() && items.get(3).getCount() == 1,
					"the off hand did not win a tie between two spores");
		}
		helper.succeed();
	}

	/** The acceptance case: coal in the off hand covers a Pyretic Forge pulse, at full price and full work. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void coalInTheOffHandCoversAPyreticForgePulse(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		Vec3 ground = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 1, 2)));
		List<ItemEntity> ores = new ArrayList<>();
		double[] paid = new double[2];
		int[] smelted = new int[2];
		try {
			for (int run = 0; run < 2; run++) {
				boolean coal = run == 0;
				ItemEntity ore = new ItemEntity(level, ground.x, ground.y, ground.z, new ItemStack(Items.RAW_IRON, 8));
				ore.setDeltaMovement(Vec3.ZERO);
				ore.setNoGravity(true);
				level.addFreshEntity(ore);
				ores.add(ore);
				try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
					ServerPlayer player = captured.player;
					prepare(player, ManipulationInit.pyretic_forge.get(), 2000.0D);
					if (coal) player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.COAL, 3));
					Vec3 feet = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 1, 2)));
					player.setPos(feet.x, feet.y, feet.z);
					player.lookAt(EntityAnchorArgument.Anchor.EYES, ore.getBoundingBox().getCenter());
					player.setOldPosAndRot();
					paid[run] = BloodProjectionItem.projectFromEntity(level, player, 5.0D, 100.0D);
					smelted[run] = 8 - ore.getItem().getCount();
					if (coal) {
						helper.assertTrue(player.getOffhandItem().getCount() == 2, "the pulse did not dissolve one coal");
						near(helper, 1.0D - TemperRules.pulseCost(10, com.vincenthuto.hemomancy.common.manipulation.verb.SculptingRules
								.bloodPerSecond(com.vincenthuto.hemomancy.common.manipulation.flammeus.PyreticForgeRules.BLOOD_PER_PULSE, 10)),
								held(player, EnumBloodTemper.FLAMMEUS), 1.0E-4D, "coal (1) less a 40 mL/s pulse");
					} else {
						helper.assertTrue(captured.said().contains("Dry of"), "the dry pulse did not say why: " + captured.said());
					}
				}
				ore.discard();
			}
			helper.assertTrue(paid[0] > 0.0D, "the coal-covered pulse paid nothing");
			near(helper, paid[0] * TemperRules.Kind.SCULPTING.dryCostMultiplier(), paid[1], 1.0E-6D, "the dry pulse's price");
			helper.assertTrue(smelted[0] == 2, "the covered pulse smelted " + smelted[0] + ", not two");
			// A dry pulse works at three quarters; a bare hand's two items round up, so it still smelts at most two.
			helper.assertTrue(smelted[1] <= smelted[0], "the dry pulse worked harder than the covered one: " + smelted[1]);
		} finally {
			ores.forEach(ItemEntity::discard);
			level.getEntitiesOfClass(ItemEntity.class, new AABB(BlockPos.containing(ground)).inflate(3.0D)).forEach(ItemEntity::discard);
		}
		helper.succeed();
	}

	// ── Filling ──

	/**
	 * Drawing from a creature fills its schools in proportion to the blood drawn. The track doc's example is a blaze,
	 * but a blaze is bloodless to Absorption ({@code HemoEntityPredicates.NOBLOOD}; its blood is a Living Syringe
	 * sample), so it gives nothing here. A creeper, now Ductilis and Flammeus, is the Overworld fire source.
	 */
	@GameTest(template = EMPTY)
	public static void drawingFromACreatureFillsItsSchools(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, null, 100.0D);
			HemoCapabilityAccess.getBloodVolume(player).setMaxBloodVolume(2000.0D);
			// Read kinds, so each draw gives its whole Temper (an unread kind gives half, CL-12).
			for (String kind : new String[]{"minecraft:creeper", "minecraft:cow"})
				HemoCapabilityAccess.clinicalBlood(player).recordExamination(kind);
			player.setPos(helper.absoluteVec(new Vec3(2.5, 1, 2.5)));
			LivingEntity creeper = still(helper, EntityType.CREEPER, new Vec3(3.5, 1, 2.5), 20.0F);
			double drawn = BloodAbsorptionItem.absorbFromTarget(helper.getLevel(), player, creeper, 4.0D);
			creeper.discard();
			helper.assertTrue(drawn > 0.0D, "nothing was drawn from the creeper");
			near(helper, drawn / 2.0D, held(player, EnumBloodTemper.FLAMMEUS), 1.0E-4D, "Flammeus from the creeper");
			near(helper, drawn / 2.0D, held(player, EnumBloodTemper.DUCTILIS), 1.0E-4D, "Ductilis from the creeper");
			near(helper, 0.0D, held(player, EnumBloodTemper.ANIMUS), 1.0E-6D, "a creeper gave Animus");
			LivingEntity cow = still(helper, EntityType.COW, new Vec3(3.5, 1, 4.5), 10.0F);
			double fromCow = BloodAbsorptionItem.absorbFromTarget(helper.getLevel(), player, cow, 2.0D);
			cow.discard();
			near(helper, fromCow, held(player, EnumBloodTemper.ANIMUS), 1.0E-4D, "a cow's whole draw is Animus");
			LivingEntity blaze = still(helper, EntityType.BLAZE, new Vec3(5.5, 1, 4.5), 20.0F);
			helper.assertTrue(BloodAbsorptionItem.absorbFromTarget(helper.getLevel(), player, blaze, 4.0D) == 0.0D
					&& held(player, EnumBloodTemper.LUX) == 0.0F, "a bloodless blaze gave blood or Lux to Absorption");
			blaze.discard();
			BloodTemper temper = HemoCapabilityAccess.getBloodTemper(player);
			helper.assertTrue(temper.hasSeen(EnumBloodTemper.FLAMMEUS) && temper.hasSeen(EnumBloodTemper.DUCTILIS)
					&& !temper.hasSeen(EnumBloodTemper.MORTEM), "the HUD's schools: " + temper.seenMask());
			// The cap: twenty of a school the player has no alignment in.
			LivingEntity big = still(helper, EntityType.CREEPER, new Vec3(3.5, 1, 2.5), 200.0F);
			BloodAbsorptionItem.absorbFromTarget(helper.getLevel(), player, big, 150.0D);
			big.discard();
			near(helper, TemperRules.BASE_CAP, held(player, EnumBloodTemper.FLAMMEUS), 1.0E-4D, "Flammeus past its cap");
		}
		helper.succeed();
	}

	// ── Scars and death ──

	/**
	 * LP-13: a tier 3 scar refills 0.3 a second, less than a Mediocritas cast a second burns after its cut (0.5). Over
	 * two minutes the scar slows the drain a long way but no longer holds the school.
	 */
	@GameTest(template = EMPTY)
	public static void aTierThreeScarSlowsTheDrainButDoesNotHoldIt(GameTestHelper helper) {
		var rebuke = ManipulationInit.hematic_rebuke.get();
		int[] dry = new int[2];
		int[] firstDry = {-1, -1};
		for (int run = 0; run < 2; run++) {
			boolean scarred = run == 0;
			try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
				ServerPlayer player = captured.player;
				prepare(player, rebuke, 2000.0D);
				if (scarred) {
					helper.assertTrue(HemoCapabilityAccess.getScarState(player).activateCerebralScar(
							ResourceLocation.parse("hemomancy:scar_chimera")), "the Chimera scar would not activate");
					helper.assertTrue(TemperCharge.activeScarTier(player, EnumBloodTemper.DUCTILIS) == 3, "the Chimera is not tier 3");
				}
				fill(player, EnumBloodTemper.DUCTILIS, 15.0F);
				for (int second = 0; second < 120; second++) {
					TemperCharge.Charge charge = TemperCharge.forCast(player, rebuke, 1.0F, false);
					if (charge.dry()) {
						dry[run]++;
						if (firstDry[run] < 0) firstDry[run] = second;
					}
					TemperCharge.commit(player, charge);
					TemperEvents.settle(player, 1.0F);
				}
			}
		}
		helper.assertTrue(dry[0] > 0, "the scarred caster never ran dry: the scar still holds its school");
		helper.assertTrue(firstDry[0] > 5 * firstDry[1],
				"the scar did not slow the drain: first dry at " + firstDry[0] + " s against " + firstDry[1] + " s");
		helper.assertTrue(dry[1] > 100, "the unscarred caster should run dry most of the fight: " + dry[1]);
		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void deathKeepsHalfAndTheEndReturnKeepsAll(GameTestHelper helper) {
		try (CapturedPlayer died = CapturedPlayer.create(helper);
			 CapturedPlayer respawned = CapturedPlayer.create(helper, died.player.getGameProfile());
			 CapturedPlayer returned = CapturedPlayer.create(helper, died.player.getGameProfile())) {
			fill(died.player, EnumBloodTemper.LUX, 18.0F);
			// restoreFrom copies the attachments and fires PlayerEvent.Clone, as a real respawn does.
			respawned.player.restoreFrom(died.player, false);
			near(helper, 9.0D, held(respawned.player, EnumBloodTemper.LUX), 1.0E-4D, "death keeps half");
			returned.player.restoreFrom(died.player, true);
			near(helper, 18.0D, held(returned.player, EnumBloodTemper.LUX), 1.0E-4D, "a return from the End keeps it all");
		}
		helper.succeed();
	}

	// ── Exemptions, teaching and sync ──

	@GameTest(template = EMPTY)
	public static void conjurationsChargeNoTemper(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, null, 2000.0D);
			for (BloodManipulation conjuration : List.of(ManipulationInit.blood_projection.get(),
					ManipulationInit.conjure_staff.get(), ManipulationInit.conjure_blade.get())) {
				helper.assertTrue(!conjuration.chargesTemper() && TemperCharge.forCast(player, conjuration, 1.0F, false) == null,
						conjuration.getName() + " charges Temper");
				helper.assertTrue(ManipulationCostLedger.collect(player, conjuration, 1.0D).contributions().stream()
						.noneMatch(contribution -> contribution.sourceId().equals("temper")), conjuration.getName() + " has a Temper line");
			}
			helper.assertTrue(ManipulationInit.blood_shot.get().chargesTemper(), "Blood Shot is exempt");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void theVicarsKitTeachesTheCatalyst(GameTestHelper helper) {
		helper.assertTrue(FirstBloodcraftAssignment.rewardStacks().stream()
						.anyMatch(stack -> TemperCatalysts.of(stack) != null && TemperCatalysts.of(stack).school() == EnumBloodTemper.ANIMUS),
				"the First Bloodcraft kit carries no Animus catalyst");
		helper.succeed();
	}

	/**
	 * The Scrying cost preview reads the pool: emptying the selected memory's school resends the cost snapshot with a dry
	 * Temper line at the next flush, with no blood change behind it; a flush with nothing changed, or a pool change that
	 * leaves the line as it was, sends none.
	 */
	@GameTest(template = EMPTY)
	public static void theCostPreviewFollowsThePoolBetweenBloodSyncs(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			var storm = ManipulationInit.synaptic_storm.get();
			prepare(player, storm, 3000.0D);
			fill(player, storm.getTend(), 50.0F);
			ManipulationDiagnosticsSync.sync(player);
			PlayerStateSync.flushDirty(player);
			int before = captured.sent(PacketSyncManipulationCostDiagnostics.class).size();
			helper.assertTrue(before == 1, "the selection sync and its flush sent " + before + " cost snapshots, not one");
			fill(player, storm.getTend(), 40.0F);
			PlayerStateSync.flushDirty(player);
			helper.assertTrue(captured.sent(PacketSyncManipulationCostDiagnostics.class).size() == before,
					"a pool change that leaves the Temper line as it was resent the cost snapshot");
			fill(player, storm.getTend(), 0.0F);
			PlayerStateSync.flushDirty(player);
			List<PacketSyncManipulationCostDiagnostics> sent = captured.sent(PacketSyncManipulationCostDiagnostics.class);
			helper.assertTrue(sent.size() == before + 1, "an emptied pool sent " + (sent.size() - before) + " cost snapshots, not one");
			var line = sent.getLast().snapshot().contribution("temper");
			helper.assertTrue(line.isPresent() && line.get().condition().endsWith(".dry"),
					"the resent preview's Temper line reads " + line.map(ManipulationCostContribution::condition).orElse("nothing"));
			PlayerStateSync.flushDirty(player);
			helper.assertTrue(captured.sent(PacketSyncManipulationCostDiagnostics.class).size() == before + 1,
					"a flush with nothing changed resent the cost snapshot");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void theClientHearsGrownScarAndPoolApart(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			var temperament = HemoCapabilityAccess.getBloodTemperament(player);
			temperament.setTemperAlignment(EnumBloodTemper.FERRIC, 30.0F);
			temperament.addTransientAlignment(EnumBloodTemper.FERRIC, 12.0F);
			BloodTemperamentEvents.syncTemperament(player, temperament);
			BloodTemperamentServerPacket packet = captured.sent(BloodTemperamentServerPacket.class).getLast();
			near(helper, 30.0D, packet.base().get(EnumBloodTemper.FERRIC), 1.0E-4D, "grown Ferric");
			near(helper, 12.0D, packet.scars().get(EnumBloodTemper.FERRIC), 1.0E-4D, "scar Ferric");
			// The client mirror rebuilds both from the packet.
			var mirror = new com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemperament();
			mirror.setTemper(packet.base());
			mirror.setTransientAlignments(packet.scars());
			near(helper, 30.0D, mirror.getBaseAlignment(EnumBloodTemper.FERRIC), 1.0E-4D, "mirror base");
			near(helper, 42.0D, mirror.getAlignmentByTemper(EnumBloodTemper.FERRIC), 1.0E-4D, "mirror effective");
			// The pool rides its own row and dirty flag.
			fill(player, EnumBloodTemper.FERRIC, 7.0F);
			int before = captured.sent(PacketSyncBloodTemper.class).size();
			PlayerStateSync.flushDirty(player);
			List<PacketSyncBloodTemper> pools = captured.sent(PacketSyncBloodTemper.class);
			helper.assertTrue(pools.size() == before + 1, "a changed pool was not flushed once");
			near(helper, 7.0D, pools.getLast().amounts()[EnumBloodTemper.FERRIC.ordinal()], 1.0E-4D, "the synced pool");
			PlayerStateSync.flushDirty(player);
			helper.assertTrue(captured.sent(PacketSyncBloodTemper.class).size() == before + 1, "an unchanged pool was resent");
		}
		helper.succeed();
	}
}
