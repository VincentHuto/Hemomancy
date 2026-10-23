package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PlaceStructurePacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStationMatcher;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules;
import com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.rite.harbinger.RiteOfferingEscrow;
import com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites;
import com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.station.StationUpgradeTier;
import com.vincenthuto.hemomancy.common.station.UpgradeStation;
import com.vincenthuto.hemomancy.common.succession.SuccessionRites;
import com.vincenthuto.hemomancy.common.succession.SuccessionSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/**
 * Characterisation of the Cardinal Rite tick, kind by kind, before its dispatch is split into strategies (step 11).
 * Every rite here runs through the real level tick ({@code HarbingerCardinalRiteEvents.onLevelTick}), not through a
 * controller called by hand: each kind's start, its completion and the ways it ends, and a save and load in the middle
 * of the rite. The kinds are the Harbinger ceremony, the Unstained countdown, the Working craft, the station upgrade,
 * the Puppeteer trial and the Succession.
 */
@GameTestHolder("rite_kind_validation")
@PrefixGameTestTemplate(false)
public final class RiteKindCharacterisationGameTests {
	static final String NAMESPACE = "rite_kind_validation";
	private static final String ROOM = "empty";
	private static final String WIDE = "wide";
	private static final ResourceLocation CEREMONY = Hemomancy.rloc("cardinal_rite/initiate_rite");
	private static final ResourceLocation COUNTDOWN = Hemomancy.rloc("cardinal_rite/still_waters");
	private static final ResourceLocation CRAFT = Hemomancy.rloc("cardinal_rite/working/somatic_loom");
	private static final ResourceLocation TRIAL = Hemomancy.rloc("cardinal_rite/puppeteer_trial_sanguine_hound");
	private static final ResourceLocation RETIRED = Hemomancy.rloc("cardinal_rite/votary_rite");
	private static final BlockPos FOCUS = new BlockPos(16, 3, 12);

	private RiteKindCharacterisationGameTests() {
	}

	// ---- Harbinger ceremony -------------------------------------------------------------------------------------

	@GameTest(template = ROOM, timeoutTicks = 200, batch = "rite_kind_ceremony_start")
	public static void ceremonyStartsByPlantingTheStaff(GameTestHelper helper) {
		Rite rite = startCeremony(helper);
		ActiveCardinalRite active = rite.active();
		helper.assertTrue(active.getPhase() == CardinalRitePhase.CONSECRATION && active.hasEscrowedStaff()
				&& active.isStaffPlanting() && active.ringsGated() && active.getMatchedFloorId() != null
				&& active.hasCapturedOfferingItinerary() && active.getOfferingItinerary().size() == 1,
				"A ceremony starts consecrating, with the staff planting, rings gated and its offering captured");
		helper.assertTrue(!rite.caster().player.getInventory().contains(new ItemStack(ItemInit.living_staff.get())),
				"The staff is escrowed while planting");
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(active.isStaffImpactReached(), "The staff must finish planting"))
				.thenExecuteAfter(5, () -> helper.assertTrue(active.getPhase() == CardinalRitePhase.CONSECRATION
						&& active.getPhaseTicks() > 0, "The planted ceremony advances its phase clock"))
				.thenExecute(() -> rite.end(helper))
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 1200, batch = "rite_kind_ceremony_complete")
	public static void ceremonyCompletesThroughItsFinale(GameTestHelper helper) {
		Rite rite = startCeremony(helper);
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(rite.active().isStaffImpactReached(), "planted"))
				.thenExecute(() -> sealCeremony(helper, rite.active()))
				.thenWaitUntil(() -> helper.assertTrue(!rite.saved().hasActiveRite(rite.uuid()),
						"The sealed ceremony must end through the tick, in phase " + rite.active().getPhase()))
				.thenExecute(() -> {
					assertCompletedCeremony(helper, rite);
					rite.close();
				})
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 300, batch = "rite_kind_ceremony_broken")
	public static void ceremonyWhoseStructureBreaksLetsGo(GameTestHelper helper) {
		Rite rite = startCeremony(helper);
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(rite.active().isStaffImpactReached(), "planted"))
				.thenExecute(() -> {
					BlockPos pillar = findAbove(helper.getLevel(), rite.focus(), BlockInit.hematic_iron_pillar.get());
					helper.getLevel().setBlock(pillar, Blocks.AIR.defaultBlockState(), 3);
					rite.caster().chat.clear();
				})
				.thenWaitUntil(() -> helper.assertTrue(!rite.saved().hasActiveRite(rite.uuid()), "A broken ceremony ends"))
				.thenExecute(() -> {
					helper.assertTrue(rite.caster().said().contains("The rite's structure was disturbed. The boundary lets go of you."),
							"A safe-retry ceremony lets go gently: " + rite.caster().said());
					helper.assertTrue(rite.active().isEnded() && !rite.active().hasEscrowedStaff(), "Ended, staff released");
					helper.assertTrue(rite.caster().player.getInventory().countItem(ItemInit.living_staff.get()) == 1,
							"The staff comes back to a living caster");
					rite.close();
				})
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 400, batch = "rite_kind_ceremony_cancel")
	public static void ceremonyDrawnBackIntoTheStaffIsCancelled(GameTestHelper helper) {
		Rite rite = startCeremony(helper);
		boolean[] channel = {false};
		helper.onEachTick(() -> {
			if (channel[0] && rite.saved().hasActiveRite(rite.uuid())) {
				rite.active().requestCancellation(helper.getLevel().getGameTime() + 1);
			}
		});
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(rite.active().isStaffImpactReached(), "planted"))
				.thenExecute(() -> channel[0] = true)
				.thenWaitUntil(() -> helper.assertTrue(!rite.saved().hasActiveRite(rite.uuid()), "A recalled ceremony ends"))
				.thenExecute(() -> {
					// The "hold the absorption steady" prompt waits for a daemon, which a fresh ceremony has not called yet.
					helper.assertTrue(rite.caster().said().contains("The daemon folds into the Living Staff"),
							"Recall is announced and resolved: " + rite.caster().said());
					helper.assertTrue(rite.caster().player.getInventory().countItem(ItemInit.living_staff.get()) == 1,
							"A recalled rite returns the staff");
					rite.close();
				})
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 300, batch = "rite_kind_ceremony_leash")
	public static void ceremonyBindsACasterWhoLeavesTheCircle(GameTestHelper helper) {
		Rite rite = startCeremony(helper);
		double[] before = new double[1];
		int[] phaseTicks = new int[1];
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(rite.active().isStaffImpactReached(), "planted"))
				.thenExecute(() -> {
					ServerPlayer player = rite.caster().player;
					player.setPos(Vec3.atCenterOf(rite.focus()).add(40.0D, 1.0D, 0.0D));
					player.setHealth(player.getMaxHealth());
					before[0] = HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
					phaseTicks[0] = rite.active().getPhaseTicks();
					rite.caster().chat.clear();
				})
				.thenExecuteAfter(4, () -> {
					ServerPlayer player = rite.caster().player;
					double drawn = before[0] - HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
					// 25 mL a tick for each of the four ticks, plus whatever the boundary wounds bleed.
					helper.assertTrue(drawn >= 100.0D, "Outside the leash the rite draws 25 mL a tick, drew " + drawn);
					helper.assertTrue(player.getHealth() < player.getMaxHealth(), "and wounds the caster");
					helper.assertTrue(rite.caster().said().contains("The rite binds you! Return to the circle!"),
							"and says so: " + rite.caster().said());
					helper.assertTrue(rite.active().getPhaseTicks() == phaseTicks[0],
							"The rite does not advance while its caster is outside");
					player.setPos(Vec3.atCenterOf(rite.focus()).add(0.0D, 1.0D, 0.0D));
				})
				.thenExecuteAfter(3, () -> helper.assertTrue(rite.active().getPhaseTicks() > phaseTicks[0],
						"Back inside, the rite advances again"))
				.thenExecute(() -> rite.end(helper))
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 300, batch = "rite_kind_ceremony_abandoned")
	public static void ceremonyGuttersOutAfterItsCasterStaysAway(GameTestHelper helper) {
		Rite rite = startCeremony(helper);
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(rite.active().isStaffImpactReached(), "planted"))
				.thenExecute(() -> {
					rite.caster().offline();
					rite.active().setDisconnectTicks(CardinalRiteCeremonyRules.DISCONNECT_GRACE_TICKS - 5);
				})
				.thenExecuteAfter(2, () -> helper.assertTrue(rite.saved().hasActiveRite(rite.uuid())
						&& rite.active().getDisconnectTicks() > CardinalRiteCeremonyRules.DISCONNECT_GRACE_TICKS - 5,
						"An absent caster's rite counts its grace"))
				.thenWaitUntil(() -> helper.assertTrue(!rite.saved().hasActiveRite(rite.uuid()),
						"Past the grace the rite gutters out"))
				.thenExecute(() -> {
					ServerPlayer player = rite.caster().player;
					helper.assertTrue(player.getInventory().countItem(ItemInit.living_staff.get()) == 0,
							"An absent caster's staff goes to the recovery queue, not their inventory");
					CardinalRiteSavedData.get(helper.getLevel().getServer().overworld()).deliverRecovery(player);
					helper.assertTrue(player.getInventory().countItem(ItemInit.living_staff.get()) == 1,
							"and comes back through recovery");
					rite.close();
				})
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 300, batch = "rite_kind_ceremony_collapse")
	public static void ceremonyAtFullInstabilityCollapsesIntoBacklash(GameTestHelper helper) {
		Rite rite = startCeremony(helper);
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(rite.active().isStaffImpactReached(), "planted"))
				.thenExecute(() -> {
					rite.caster().player.setHealth(rite.caster().player.getMaxHealth());
					rite.active().addInstability(CardinalRiteCeremonyRules.COLLAPSE_INSTABILITY);
				})
				.thenWaitUntil(() -> helper.assertTrue(!rite.saved().hasActiveRite(rite.uuid()), "A collapsed rite ends"))
				.thenExecute(() -> {
					helper.assertTrue(rite.caster().said().contains("The cardinal boundary collapses into backlash."),
							"Collapse backlash is announced: " + rite.caster().said());
					helper.assertTrue(rite.caster().player.getInventory().countItem(ItemInit.living_staff.get()) == 1,
							"A collapsed rite returns the staff");
					rite.close();
				})
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 200, batch = "rite_kind_ceremony_death")
	public static void ceremonyEndsWithItsCastersDeath(GameTestHelper helper) {
		Rite rite = startCeremony(helper);
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(rite.active().isStaffImpactReached(), "planted"))
				.thenExecute(() -> {
					ServerPlayer player = rite.caster().player;
					HarbingerCardinalRiteEvents.onPlayerDeath(new LivingDeathEvent(player, player.damageSources().magic()));
					helper.assertTrue(!rite.saved().hasActiveRite(rite.uuid()) && rite.active().isEnded(),
							"Death ends the rite at once");
					helper.assertTrue(rite.caster().said().contains("The rite has been broken by your death..."),
							"and says so: " + rite.caster().said());
					helper.assertTrue(player.getInventory().countItem(ItemInit.living_staff.get()) == 1,
							"The staff returns to the inventory about to drop");
					rite.close();
				})
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 100, batch = "rite_kind_ceremony_retired")
	public static void ceremonyWhoseRecipeIsRetiredDissolves(GameTestHelper helper) {
		CapturedPlayer caster = caster(helper, 2);
		BlockPos center = helper.absolutePos(FOCUS);
		caster.player.setPos(Vec3.atCenterOf(center).add(0.0D, 1.0D, 0.0D));
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(caster.player.getUUID(), center, RETIRED, 1200, 3, 2,
				false, 0, 4);
		CardinalRiteSavedData saved = CardinalRiteSavedData.get(helper.getLevel());
		saved.startRite(rite);
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(!saved.hasActiveRite(caster.player.getUUID()), "A retired rite ends"))
				.thenExecute(() -> {
					helper.assertTrue(caster.said().contains("The rite dissolves harmlessly because its pattern is no longer known."),
							"Retirement is announced: " + caster.said());
					caster.close();
				})
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 1200, batch = "rite_kind_ceremony_reload")
	public static void ceremonySavedAndLoadedMidRiteResumesAndCompletes(GameTestHelper helper) {
		Rite rite = startCeremony(helper);
		ActiveCardinalRite[] resumed = new ActiveCardinalRite[1];
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(rite.active().isStaffImpactReached(), "planted"))
				.thenExecute(() -> {
					rite.active().fillAnchor(0, 100);
					rite.active().fillAnchor(1, 100);
					resumed[0] = restart(helper, rite.active());
					helper.assertTrue(resumed[0].getPhase() == CardinalRitePhase.CONSECRATION
							&& resumed[0].hasEscrowedStaff() && resumed[0].getAnchorBloodMl()[1] > 0,
							"The reloaded ceremony keeps its phase, staff and anchor blood");
				})
				.thenExecuteAfter(3, () -> helper.assertTrue(rite.saved().getRite(rite.uuid()) == resumed[0]
						&& !rite.active().isEnded(), "The reloaded copy ticks in place of the original"))
				.thenExecute(() -> sealCeremony(helper, resumed[0]))
				.thenWaitUntil(() -> helper.assertTrue(!rite.saved().hasActiveRite(rite.uuid()),
						"The reloaded ceremony must still end, in phase " + resumed[0].getPhase()))
				.thenExecute(() -> {
					assertCompletedCeremony(helper, new Rite(rite.caster(), rite.focus(), resumed[0], rite.saved()));
					rite.close();
				})
				.thenSucceed();
	}

	// ---- Unstained countdown ------------------------------------------------------------------------------------

	@GameTest(template = ROOM, timeoutTicks = 200, batch = "rite_kind_countdown")
	public static void countdownTicksDownThenMeetsItsFinalGate(GameTestHelper helper) {
		CapturedPlayer caster = caster(helper, 1);
		ActiveCardinalRite rite = countdown(helper, caster, 30);
		CardinalRiteSavedData saved = CardinalRiteSavedData.get(helper.getLevel());
		helper.startSequence()
				.thenExecuteAfter(5, () -> helper.assertTrue(rite.getPhase() == CardinalRitePhase.LEGACY
						&& rite.getRemainingTicks() < 30 && rite.getRemainingTicks() > 0, "A countdown ticks down"))
				.thenWaitUntil(() -> helper.assertTrue(!saved.hasActiveRite(caster.player.getUUID()), "The countdown ends"))
				.thenExecute(() -> {
					helper.assertTrue(rite.getRemainingTicks() == 0, "It ran to zero");
					helper.assertTrue(caster.said().contains("The rite falls silent. It requires"),
							"An unqualified Unstained caster is refused at the end: " + caster.said());
					caster.close();
				})
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 100, batch = "rite_kind_countdown")
	public static void countdownWaitsHarmlesslyOutsideTheCircle(GameTestHelper helper) {
		CapturedPlayer caster = caster(helper, 1);
		ActiveCardinalRite rite = countdown(helper, caster, 30);
		ServerPlayer player = caster.player;
		player.setPos(Vec3.atCenterOf(rite.getCenterPos()).add(40.0D, 1.0D, 0.0D));
		double blood = HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
		helper.startSequence()
				.thenExecuteAfter(5, () -> {
					helper.assertTrue(rite.getRemainingTicks() == 30, "The countdown waits for its caster");
					helper.assertTrue(HemoCapabilityAccess.getBloodVolume(player).getBloodVolume() == blood
							&& player.getHealth() == player.getMaxHealth(), "An Unstained caster is not bound in blood");
					helper.assertTrue(caster.said().contains("The stillness breaks. Return to the circle."),
							"but is told to return: " + caster.said());
					CardinalRiteSavedData.get(helper.getLevel()).removeRite(player.getUUID());
					caster.close();
				})
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 300, batch = "rite_kind_countdown")
	public static void countdownOutlastsAnAbsentCaster(GameTestHelper helper) {
		CapturedPlayer caster = caster(helper, 1);
		ActiveCardinalRite rite = countdown(helper, caster, 30);
		caster.offline();
		helper.startSequence()
				.thenExecuteAfter(CardinalRiteCeremonyRules.DISCONNECT_GRACE_TICKS + 10, () -> {
					helper.assertTrue(CardinalRiteSavedData.get(helper.getLevel()).getRite(caster.player.getUUID()) == rite
							&& rite.getRemainingTicks() == 30 && rite.getDisconnectTicks() == 0,
							"A countdown neither ticks nor gutters out while its caster is away");
					CardinalRiteSavedData.get(helper.getLevel()).removeRite(caster.player.getUUID());
					caster.close();
				})
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 200, batch = "rite_kind_countdown_reload")
	public static void countdownSavedAndLoadedMidRiteKeepsItsCount(GameTestHelper helper) {
		CapturedPlayer caster = caster(helper, 1);
		ActiveCardinalRite rite = countdown(helper, caster, 30);
		CardinalRiteSavedData saved = CardinalRiteSavedData.get(helper.getLevel());
		ActiveCardinalRite[] resumed = new ActiveCardinalRite[1];
		helper.startSequence()
				.thenExecuteAfter(5, () -> {
					int remaining = rite.getRemainingTicks();
					resumed[0] = restart(helper, rite);
					helper.assertTrue(resumed[0].getPhase() == CardinalRitePhase.LEGACY
							&& resumed[0].getRemainingTicks() == remaining, "The count survives the restart");
				})
				.thenWaitUntil(() -> helper.assertTrue(!saved.hasActiveRite(caster.player.getUUID()), "and runs out"))
				.thenExecute(() -> {
					helper.assertTrue(resumed[0].getRemainingTicks() == 0 && caster.said().contains("The rite falls silent."),
							"The reloaded countdown ends the same way: " + caster.said());
					caster.close();
				})
				.thenSucceed();
	}

	// ---- Working craft ------------------------------------------------------------------------------------------

	@GameTest(template = ROOM, timeoutTicks = 1600, batch = "rite_kind_craft_complete")
	public static void craftStartsFeedingAndCompletesWhenFed(GameTestHelper helper) {
		Rite rite = startCraft(helper);
		ActiveCardinalRite active = rite.active();
		helper.assertTrue(active.isWorkingCraft() && active.getPhase() == CardinalRitePhase.SUBJECT_FEEDING
				&& RiteOfferingEscrow.isEscrowed(active.craft()), "A craft starts feeding its subject with escrow held");
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(active.isStaffImpactReached(), "planted"))
				.thenExecute(() -> helper.assertTrue(active.feedSubject(active.subjectFeedNeeded())
						&& active.getPhase() != CardinalRitePhase.SUBJECT_FEEDING, "A fed subject seals the craft"))
				.thenWaitUntil(() -> helper.assertTrue(!rite.saved().hasActiveRite(rite.uuid()),
						"The sealed craft must end through the tick, in phase " + active.getPhase()))
				.thenExecute(() -> {
					helper.assertTrue(active.isComplete() && RiteOfferingEscrow.isConsumed(active.craft()),
							"The craft completes and spends its escrow");
					helper.assertTrue(itemsNear(helper.getLevel(), rite.focus(), BlockInit.somatic_loom.get().asItem()) == 1,
							"Exactly one Somatic Loom condenses");
					CardinalRiteSavedData.get(helper.getLevel().getServer().overworld()).deliverRecovery(rite.caster().player);
					helper.assertTrue(rite.caster().player.getInventory().countItem(ItemInit.living_staff.get()) == 1,
							"The staff comes back");
					rite.close();
				})
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 200, batch = "rite_kind_craft_focus")
	public static void craftWhoseFocusIsRemovedBreaksAndReturnsItsEscrow(GameTestHelper helper) {
		Rite rite = startCraft(helper);
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(rite.active().isStaffImpactReached(), "planted"))
				.thenExecute(() -> helper.getLevel().setBlock(rite.focus(), Blocks.AIR.defaultBlockState(), 3))
				.thenWaitUntil(() -> helper.assertTrue(!rite.saved().hasActiveRite(rite.uuid()), "A focusless craft ends"))
				.thenExecute(() -> {
					assertCraftEscrowReturned(helper, rite);
					rite.close();
				})
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 200, batch = "rite_kind_craft_subject")
	public static void craftWhoseSubjectBreaksFailsAndReturnsItsEscrow(GameTestHelper helper) {
		Rite rite = startCraft(helper);
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(rite.active().isStaffImpactReached(), "planted"))
				.thenExecute(() -> {
					BlockPos cell = com.vincenthuto.hemomancy.common.rite.harbinger.WorkingCraftRites
							.subjectPositions(rite.active()).getFirst();
					helper.getLevel().setBlock(cell, Blocks.AIR.defaultBlockState(), 3);
					rite.caster().chat.clear();
				})
				.thenWaitUntil(() -> helper.assertTrue(!rite.saved().hasActiveRite(rite.uuid()), "A broken craft ends"))
				.thenExecute(() -> {
					assertCraftEscrowReturned(helper, rite);
					rite.close();
				})
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 400, batch = "rite_kind_craft_absent")
	public static void craftWaitsForAnAbsentCaster(GameTestHelper helper) {
		Rite rite = startCraft(helper);
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(rite.active().isStaffImpactReached(), "planted"))
				.thenExecute(() -> rite.caster().offline())
				.thenExecuteAfter(CardinalRiteCeremonyRules.DISCONNECT_GRACE_TICKS + 10, () -> {
					helper.assertTrue(rite.saved().getRite(rite.uuid()) == rite.active() && rite.active().getDisconnectTicks() == 0,
							"A craft waits for its caster instead of guttering out");
					rite.caster().online();
					rite.end(helper);
				})
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 1600, batch = "rite_kind_craft_reload")
	public static void craftSavedAndLoadedMidRiteStillCompletesOnce(GameTestHelper helper) {
		Rite rite = startCraft(helper);
		ActiveCardinalRite[] resumed = new ActiveCardinalRite[1];
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(rite.active().isStaffImpactReached(), "planted"))
				.thenExecute(() -> {
					rite.active().feedSubject(rite.active().subjectFeedNeeded() / 2);
					resumed[0] = restart(helper, rite.active());
					helper.assertTrue(resumed[0].isWorkingCraft() && resumed[0].subjectFill() > 0.0F
							&& RiteOfferingEscrow.isEscrowed(resumed[0].craft()), "The reloaded craft keeps its feed and escrow");
					resumed[0].feedSubject(resumed[0].subjectFeedNeeded());
				})
				.thenWaitUntil(() -> helper.assertTrue(!rite.saved().hasActiveRite(rite.uuid()), "The reloaded craft ends"))
				.thenExecute(() -> {
					helper.assertTrue(resumed[0].isComplete() && RiteOfferingEscrow.isConsumed(resumed[0].craft()),
							"The reloaded craft completes");
					helper.assertTrue(itemsNear(helper.getLevel(), rite.focus(), BlockInit.somatic_loom.get().asItem()) == 1,
							"Exactly one Somatic Loom condenses");
					rite.close();
				})
				.thenSucceed();
	}

	// ---- Station upgrade ----------------------------------------------------------------------------------------

	@GameTest(template = ROOM, timeoutTicks = 3000, batch = "rite_kind_station_complete")
	public static void stationUpgradeStartsAndCompletesThroughTheTick(GameTestHelper helper) {
		Rite rite = startStation(helper);
		StationUpgradeTier tier = stationTier();
		ActiveCardinalRite active = rite.active();
		helper.assertTrue(active.getRecipeId().equals(tier.rite()) && active.getPhase() == CardinalRitePhase.CONSECRATION
				&& StationUpgradeRites.station(helper.getLevel(), active).isRiteLocked(),
				"A station upgrade starts consecrating with its station locked");
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(active.isStaffImpactReached(), "planted"))
				.thenExecute(() -> sealStation(helper, active, tier))
				.thenWaitUntil(() -> helper.assertTrue(!rite.saved().hasActiveRite(rite.uuid()),
						"The fed upgrade must end through the tick, in phase " + active.getPhase()))
				.thenExecute(() -> {
					var station = StationUpgradeRites.station(helper.getLevel(), active);
					helper.assertTrue(active.isComplete() && station.upgradeTier() == tier.tier() && !station.isRiteLocked()
							&& RiteOfferingEscrow.CONSUMED.equals(active.upgrade().getString("EscrowState")),
							"The station upgrades and spends its escrow");
					CardinalRiteSavedData.get(helper.getLevel().getServer().overworld()).deliverRecovery(rite.caster().player);
					helper.assertTrue(rite.caster().player.getInventory().countItem(ItemInit.living_staff.get()) == 1,
							"The staff comes back");
					rite.close();
				})
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 200, batch = "rite_kind_station_broken")
	public static void stationUpgradeWhoseStationGoesBreaksAndReturnsItsEscrow(GameTestHelper helper) {
		Rite rite = startStation(helper);
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(rite.active().isStaffImpactReached(), "planted"))
				.thenExecute(() -> helper.getLevel().removeBlock(StationUpgradeRites.seat(rite.active()), false))
				.thenWaitUntil(() -> helper.assertTrue(!rite.saved().hasActiveRite(rite.uuid()), "A stationless upgrade ends"))
				.thenExecute(() -> {
					helper.assertTrue(RiteOfferingEscrow.RETURNED.equals(rite.active().upgrade().getString("EscrowState")),
							"Its escrow goes home");
					helper.assertTrue(rite.caster().player.getInventory().countItem(ItemInit.living_staff.get()) == 1,
							"and the staff to a living caster");
					rite.close();
				})
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 400, batch = "rite_kind_station_absent")
	public static void stationUpgradeWaitsForAnAbsentCaster(GameTestHelper helper) {
		Rite rite = startStation(helper);
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(rite.active().isStaffImpactReached(), "planted"))
				.thenExecute(() -> rite.caster().offline())
				.thenExecuteAfter(CardinalRiteCeremonyRules.DISCONNECT_GRACE_TICKS + 10, () -> {
					helper.assertTrue(rite.saved().getRite(rite.uuid()) == rite.active() && rite.active().getDisconnectTicks() == 0,
							"A station upgrade waits for its caster instead of guttering out");
					rite.caster().online();
					rite.end(helper);
				})
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 3000, batch = "rite_kind_station_reload")
	public static void stationUpgradeSavedAndLoadedMidProjectionCompletes(GameTestHelper helper) {
		Rite rite = startStation(helper);
		StationUpgradeTier tier = stationTier();
		ActiveCardinalRite[] resumed = new ActiveCardinalRite[1];
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(rite.active().isStaffImpactReached(), "planted"))
				.thenExecute(() -> {
					ActiveCardinalRite active = rite.active();
					for (int anchor = 0; anchor < active.getAnchorBloodMl().length; anchor++) active.fillAnchor(anchor, 50);
					helper.assertTrue(active.enterInscription() && active.sealAltar(false)
							&& active.fillUpgradeCircuit(0, tier.bloodPerCircuit() / 2), "projection begun");
					resumed[0] = restart(helper, active);
					helper.assertTrue(resumed[0].getPhase() == CardinalRitePhase.STATION_PROJECTION
							&& resumed[0].upgradeBloodNeeded() == active.upgradeBloodNeeded(),
							"The reloaded upgrade keeps its circuit progress");
					resumed[0].fillUpgradeCircuit(0, resumed[0].upgradeBloodNeeded());
					for (int circuit = 1; circuit < tier.circuits(); circuit++)
						resumed[0].fillUpgradeCircuit(circuit, tier.bloodPerCircuit());
					for (int anchor = 0; anchor < resumed[0].getAnchorBloodMl().length; anchor++)
						resumed[0].fillAnchor(anchor, resumed[0].bloodNeededForAnchor(anchor));
				})
				.thenWaitUntil(() -> helper.assertTrue(!rite.saved().hasActiveRite(rite.uuid()),
						"The reloaded upgrade must end, in phase " + resumed[0].getPhase()))
				.thenExecute(() -> {
					helper.assertTrue(resumed[0].isComplete()
							&& StationUpgradeRites.station(helper.getLevel(), resumed[0]).upgradeTier() == tier.tier(),
							"The reloaded upgrade completes");
					rite.close();
				})
				.thenSucceed();
	}

	// ---- Puppeteer trial ----------------------------------------------------------------------------------------

	@GameTest(template = ROOM, timeoutTicks = 200, batch = "rite_kind_trial_unready")
	public static void puppeteerTrialWithoutAReadyCrossbarCollapses(GameTestHelper helper) {
		Rite rite = trial(helper);
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(!rite.saved().hasActiveRite(rite.uuid()), "An unready trial ends"))
				.thenExecute(() -> {
					helper.assertTrue(rite.active().getPhase() == CardinalRitePhase.COLLAPSED
							&& !rite.active().isPuppeteerTrialManifested(), "No puppet manifests; the trial collapses");
					helper.assertTrue(rite.caster().said().contains("The cardinal boundary collapses into backlash."),
							"with the ceremony's backlash: " + rite.caster().said());
					rite.close();
				})
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 400, batch = "rite_kind_trial_vanished")
	public static void puppeteerTrialWhosePuppetVanishesCollapses(GameTestHelper helper) {
		Rite rite = trial(helper);
		rite.active().beginPuppeteerTrial("sanguine_hound", UUID.randomUUID(), UUID.randomUUID());
		helper.startSequence()
				.thenExecuteAfter(3, () -> helper.assertTrue(rite.active().getPuppeteerTrialMissingTicks() > 0,
						"A missing puppet is counted"))
				.thenWaitUntil(() -> helper.assertTrue(!rite.saved().hasActiveRite(rite.uuid()), "A puppetless trial ends"))
				.thenExecute(() -> {
					helper.assertTrue(rite.active().getPhase() == CardinalRitePhase.COLLAPSED, "It collapses");
					rite.close();
				})
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 600, batch = "rite_kind_trial_defeated")
	public static void puppeteerTrialDefeatedMeetsItsCrossbarCheck(GameTestHelper helper) {
		Rite rite = trial(helper);
		UUID puppet = UUID.randomUUID();
		rite.active().beginPuppeteerTrial("sanguine_hound", puppet, UUID.randomUUID());
		helper.assertTrue(rite.active().markPuppeteerTrialDefeated(puppet)
				&& rite.active().getPhase() == CardinalRitePhase.CULMINATION, "A defeated puppet moves the trial to culmination");
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(!rite.saved().hasActiveRite(rite.uuid()),
						"The defeated trial ends through the tick, in phase " + rite.active().getPhase()))
				.thenExecute(() -> {
					helper.assertTrue(rite.active().isComplete(), "It reached completion");
					helper.assertTrue(!HemoCapabilityAccess.getKnownSummons(rite.caster().player).getKnownSummonNames()
							.contains("sanguine_hound"), "A trial whose seated crossbar is not its own grants nothing");
					rite.close();
				})
				.thenSucceed();
	}

	@GameTest(template = ROOM, timeoutTicks = 100, batch = "rite_kind_trial_reload")
	public static void puppeteerTrialSavedAndLoadedKeepsItsPuppet(GameTestHelper helper) {
		Rite rite = trial(helper);
		UUID puppet = UUID.randomUUID();
		UUID crossbar = UUID.randomUUID();
		rite.active().beginPuppeteerTrial("sanguine_hound", puppet, crossbar);
		rite.active().updatePuppeteerTrialHealth(5.0F, 20.0F);
		ActiveCardinalRite resumed = restart(helper, rite.active());
		helper.assertTrue(resumed.isPuppeteerTrialManifested() && puppet.equals(resumed.getPuppeteerTrialEntityId())
				&& crossbar.equals(resumed.getPuppeteerTrialCrossbarId())
				&& "sanguine_hound".equals(resumed.getPuppeteerTrialSummonName())
				&& resumed.getPuppeteerTrialProgress() == 0.75F, "The trial's puppet survives the restart");
		rite.close();
		helper.succeed();
	}

	// ---- Succession ---------------------------------------------------------------------------------------------

	@GameTest(template = WIDE, timeoutTicks = 1600, batch = "rite_kind_succession_complete")
	public static void successionRunsItsCourseThroughTheTick(GameTestHelper helper) {
		Succession succession = succession(helper);
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(!succession.saved().hasActiveRite(succession.uuid()),
						"The succession must end through the tick at " + succession.rite().succession().getInt("Ticks")))
				.thenExecute(() -> {
					var context = succession.rite().succession();
					helper.assertTrue(context.getBoolean("Resolved") && context.getInt("Ticks") == SuccessionRites.DURATION,
							"It runs its whole course and resolves");
					helper.assertTrue(SuccessionSavedData.get(helper.getLevel()).residents.containsKey(context.getUUID("Identity")),
							"and creates its successor");
					succession.caster().close();
				})
				.thenSucceed();
	}

	@GameTest(template = WIDE, timeoutTicks = 300, batch = "rite_kind_succession_collapse")
	public static void successionWhoseBodyIsTakenCollapses(GameTestHelper helper) {
		Succession succession = succession(helper);
		helper.startSequence()
				.thenExecuteAfter(5, () -> {
					helper.assertTrue(succession.rite().succession().getInt("Ticks") > 0, "The succession advances");
					helper.getLevel().setBlock(BlockPos.of(succession.rite().succession().getLong("Body")),
							Blocks.AIR.defaultBlockState(), 3);
				})
				.thenWaitUntil(() -> helper.assertTrue(!succession.saved().hasActiveRite(succession.uuid()),
						"A succession without its body ends"))
				.thenExecute(() -> {
					helper.assertTrue(!succession.rite().succession().getBoolean("Resolved") && !succession.rite().isComplete(),
							"without resolving");
					succession.caster().close();
				})
				.thenSucceed();
	}

	@GameTest(template = WIDE, timeoutTicks = 1600, batch = "rite_kind_succession_reload")
	public static void successionSavedAndLoadedMidRiteResolves(GameTestHelper helper) {
		Succession succession = succession(helper);
		ActiveCardinalRite[] resumed = new ActiveCardinalRite[1];
		helper.startSequence()
				.thenExecuteAfter(250, () -> {
					int ticks = succession.rite().succession().getInt("Ticks");
					resumed[0] = restart(helper, succession.rite());
					helper.assertTrue(SuccessionRites.is(resumed[0]) && resumed[0].succession().getInt("Ticks") == ticks
							&& resumed[0].succession().getBoolean("BloodConsumed"), "The reloaded succession keeps its stage");
				})
				.thenWaitUntil(() -> helper.assertTrue(!succession.saved().hasActiveRite(succession.uuid()),
						"The reloaded succession ends at " + resumed[0].succession().getInt("Ticks")))
				.thenExecute(() -> {
					helper.assertTrue(resumed[0].succession().getBoolean("Resolved"), "and resolves");
					succession.caster().close();
				})
				.thenSucceed();
	}

	// ---- fixtures -----------------------------------------------------------------------------------------------

	private record Rite(CapturedPlayer caster, BlockPos focus, ActiveCardinalRite active, CardinalRiteSavedData saved) {
		UUID uuid() {
			return caster.player.getUUID();
		}

		/** Ends a rite the test left running, returning what it holds. */
		void end(GameTestHelper helper) {
			if (saved.hasActiveRite(uuid())) {
				com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteTermination.end(helper.getLevel(), caster.player,
						saved.getRite(uuid()), com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteTermination.Outcome.CANCELLED);
				saved.removeRite(uuid());
			}
			close();
		}

		void close() {
			saved.removeRite(uuid());
			caster.close();
		}
	}

	private record Succession(CapturedPlayer caster, ActiveCardinalRite rite, CardinalRiteSavedData saved) {
		UUID uuid() {
			return caster.player.getUUID();
		}
	}

	private static CapturedPlayer caster(GameTestHelper helper, int degree) {
		CapturedPlayer caster = CapturedPlayer.create(helper).online();
		HemoCapabilityAccess.getInitiatoryDegree(caster.player).setDegreeNumber(degree);
		var blood = HemoCapabilityAccess.getBloodVolume(caster.player);
		blood.setActive(true);
		blood.setMaxBloodVolume(5000.0D);
		blood.setBloodVolume(5000.0D);
		// A captured player never ticks, so its spawn invulnerability would never wear off and hide rite damage.
		try {
			var field = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
			field.setAccessible(true);
			field.setInt(caster.player, 0);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Could not end the test caster's spawn invulnerability", exception);
		}
		return caster;
	}

	private static void clear(ServerLevel level, BlockPos focus) {
		for (BlockPos pos : BlockPos.betweenClosed(focus.offset(-8, -1, -8), focus.offset(8, 5, 8))) {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
		}
		for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(focus).inflate(9.0D))) item.discard();
	}

	/** Raises the recipe's floor at the room's focus and starts it with a Living Staff, as a player would. */
	private static Rite startLayered(GameTestHelper helper, ResourceLocation recipeId, int degree) {
		ServerLevel level = helper.getLevel();
		BlockPos focus = helper.absolutePos(FOCUS);
		CapturedPlayer caster = caster(helper, degree);
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, recipeId);
		helper.assertTrue(recipe != null, recipeId + " must load");
		clear(level, focus);
		helper.assertTrue(PlaceStructurePacket.placeLayeredCardinalRite(level, focus, recipe, caster.player) > 0,
				recipeId + " floor must place");
		return startAt(helper, caster, focus, recipeId);
	}

	private static Rite startAt(GameTestHelper helper, CapturedPlayer caster, BlockPos focus, ResourceLocation recipeId) {
		ServerPlayer player = caster.player;
		player.setPos(Vec3.atCenterOf(focus).add(0.0D, 1.0D, 0.0D));
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.living_staff.get()));
		caster.chat.clear();
		var start = BloodCraftingKeyPressPacket.tryStartCardinalRite(player, focus,
				CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
		CardinalRiteSavedData saved = CardinalRiteSavedData.get(helper.getLevel());
		ActiveCardinalRite rite = saved.getRite(player.getUUID());
		helper.assertTrue(start == CardinalRiteActivationRules.ActivationAttempt.STARTED && rite != null
				&& rite.getRecipeId().equals(recipeId), recipeId + " must start, got " + start + ": " + caster.said());
		return new Rite(caster, focus, rite, saved);
	}

	private static Rite startCeremony(GameTestHelper helper) {
		return startLayered(helper, CEREMONY, 2);
	}

	/** Feeds every anchor and seals the altar, so the ceremony runs its finale on its own. */
	private static void sealCeremony(GameTestHelper helper, ActiveCardinalRite rite) {
		for (int anchor = 0; anchor < rite.getAnchorBloodMl().length; anchor++) {
			rite.fillAnchor(anchor, rite.bloodNeededForAnchor(anchor));
		}
		helper.assertTrue(rite.enterInscription() && rite.sealAltar(false), "The fed ceremony must seal");
	}

	private static void assertCompletedCeremony(GameTestHelper helper, Rite rite) {
		helper.assertTrue(rite.active().isComplete() && rite.active().isEnded(), "The ceremony completed");
		helper.assertTrue(rite.caster().said().contains("is complete!"),
				"The completion is announced: " + rite.caster().said());
		helper.assertTrue(rite.caster().player.getInventory().countItem(ItemInit.living_staff.get()) == 1,
				"The staff comes back");
	}

	private static ActiveCardinalRite countdown(GameTestHelper helper, CapturedPlayer caster, int ticks) {
		BlockPos center = helper.absolutePos(FOCUS);
		clear(helper.getLevel(), center);
		// Still Waters: calcite in a cross, cleansed stone at the corners, one layer.
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				helper.getLevel().setBlock(center.offset(x, 0, z), (x != 0 && z != 0
						? BlockInit.cleansed_stone.get().defaultBlockState() : Blocks.CALCITE.defaultBlockState()), 3);
			}
		}
		caster.player.setPos(Vec3.atCenterOf(center).add(0.0D, 1.0D, 0.0D));
		ActiveCardinalRite rite = new ActiveCardinalRite(caster.player.getUUID(), center, COUNTDOWN, ticks, 3);
		CardinalRiteSavedData.get(helper.getLevel()).startRite(rite);
		return rite;
	}

	private static Rite startCraft(GameTestHelper helper) {
		return startLayered(helper, CRAFT, 3);
	}

	private static void assertCraftEscrowReturned(GameTestHelper helper, Rite rite) {
		helper.assertTrue(RiteOfferingEscrow.RETURNED.equals(rite.active().craft().getString("EscrowState")),
				"The craft's escrow goes home");
		CardinalRiteSavedData.get(helper.getLevel().getServer().overworld()).deliverRecovery(rite.caster().player);
		var inventory = rite.caster().player.getInventory();
		helper.assertTrue(inventory.countItem(Items.STRING) == 1 && inventory.countItem(ItemInit.neurotic_enzyme.get()) == 1,
				"The Loom's offerings come back");
		helper.assertTrue(inventory.countItem(ItemInit.living_staff.get()) == 1, "and so does the staff");
	}

	private static StationUpgradeTier stationTier() {
		return StationUpgradeCatalog.get(UpgradeStation.RESONANT_FORGE, 1);
	}

	private static Rite startStation(GameTestHelper helper) {
		StationUpgradeTier tier = stationTier();
		CapturedPlayer caster = caster(helper, 7);
		BlockPos center = StationUpgradeActivationGameTests.prepare(helper, tier);
		StationUpgradeActivationGameTests.practice(caster.player, tier);
		return startAt(helper, caster, center, tier.rite());
	}

	/** Feeds the anchors and every circuit, so the upgrade runs its finale on its own. */
	private static void sealStation(GameTestHelper helper, ActiveCardinalRite rite, StationUpgradeTier tier) {
		for (int anchor = 0; anchor < rite.getAnchorBloodMl().length; anchor++) rite.fillAnchor(anchor, 50);
		helper.assertTrue(rite.enterInscription() && rite.sealAltar(false)
				&& rite.getPhase() == CardinalRitePhase.STATION_PROJECTION, "The upgrade must enter projection");
		for (int circuit = 0; circuit < tier.circuits(); circuit++) {
			helper.assertTrue(rite.fillUpgradeCircuit(circuit, tier.bloodPerCircuit()), "circuit " + circuit);
		}
		for (int anchor = 0; anchor < rite.getAnchorBloodMl().length; anchor++) {
			rite.fillAnchor(anchor, rite.bloodNeededForAnchor(anchor));
		}
	}

	/** A Puppeteer trial on its own floor, started directly so no crossbar has to be attuned. */
	private static Rite trial(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos focus = helper.absolutePos(FOCUS);
		CapturedPlayer caster = caster(helper, 4);
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, TRIAL);
		helper.assertTrue(recipe != null && recipe.isPuppeteerTrial(), TRIAL + " must load");
		clear(level, focus);
		helper.assertTrue(PlaceStructurePacket.placeLayeredCardinalRite(level, focus, recipe, caster.player) > 0, "trial floor");
		var station = CardinalRiteStationMatcher.find(level, focus, recipe).orElse(null);
		helper.assertTrue(station != null, "The trial floor must match");
		caster.player.setPos(Vec3.atCenterOf(focus).add(0.0D, 1.0D, 0.0D));
		caster.chat.clear();
		ActiveCardinalRite rite = ActiveCardinalRite.puppeteerTrial(caster.player.getUUID(), focus, TRIAL,
				recipe.getRiteType().getSize());
		rite.setMatchedFloor(station.floor().id(), station.floorMatch().getForwards(), station.floorMatch().getUp());
		CardinalRiteSavedData saved = CardinalRiteSavedData.get(level);
		saved.startRite(rite);
		return new Rite(caster, focus, rite, saved);
	}

	private static Succession succession(GameTestHelper helper) {
		CapturedPlayer caster = CapturedPlayer.create(helper).online();
		var fixture = SuccessionGameTests.fixture(helper, caster.player);
		HemoCapabilityAccess.getBloodVolume(caster.player).setBloodVolume(1000.0D);
		ActiveCardinalRite rite = SuccessionGameTests.setup(helper, fixture, false, null, false);
		CardinalRiteSavedData saved = CardinalRiteSavedData.get(helper.getLevel());
		saved.startRite(rite);
		return new Succession(caster, rite, saved);
	}

	/**
	 * A server restart in the middle of a rite: the level's rite data is written and read back the way the world
	 * saves it, the copy must write the same NBT, and the copy replaces the original in the running level.
	 */
	private static ActiveCardinalRite restart(GameTestHelper helper, ActiveCardinalRite rite) {
		ServerLevel level = helper.getLevel();
		CardinalRiteSavedData saved = CardinalRiteSavedData.get(level);
		CompoundTag written = rite.serialize(level.registryAccess());
		CardinalRiteSavedData single = new CardinalRiteSavedData();
		single.startRite(rite);
		CardinalRiteSavedData reloaded = CardinalRiteSavedData.load(single.save(new CompoundTag(), level.registryAccess()),
				level.registryAccess());
		ActiveCardinalRite copy = reloaded.getRite(rite.getPlayerUUID());
		helper.assertTrue(copy != null && copy != rite, "The rite must reload");
		CompoundTag rewritten = copy.serialize(level.registryAccess());
		helper.assertTrue(rewritten.equals(written), "A reloaded rite must write the same NBT:\n" + written + "\n" + rewritten);
		saved.removeRite(rite.getPlayerUUID());
		saved.startRite(copy);
		return copy;
	}

	private static BlockPos findAbove(ServerLevel level, BlockPos focus, net.minecraft.world.level.block.Block block) {
		for (BlockPos pos : BlockPos.betweenClosed(focus.offset(-4, 1, -4), focus.offset(4, 4, 4))) {
			if (level.getBlockState(pos).is(block)) return pos.immutable();
		}
		throw new IllegalStateException("No " + block + " above " + focus);
	}

	private static int itemsNear(ServerLevel level, BlockPos focus, net.minecraft.world.item.Item item) {
		return level.getEntitiesOfClass(ItemEntity.class, new AABB(focus).inflate(5.0D), entity -> entity.getItem().is(item))
				.stream().mapToInt(entity -> entity.getItem().getCount()).sum();
	}
}
