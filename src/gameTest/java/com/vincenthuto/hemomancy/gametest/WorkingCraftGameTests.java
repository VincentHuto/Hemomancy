package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.BloodStructureFeedManager;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.mnemonist.MnemonicReliquaryProgression;
import com.vincenthuto.hemomancy.common.network.PlaceStructurePacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStationMatcher;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteInteractionHandler;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteTermination;
import com.vincenthuto.hemomancy.common.rite.harbinger.RiteOfferingEscrow;
import com.vincenthuto.hemomancy.common.rite.harbinger.WorkingCraftRites;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Working crafts: machines raised beside a Cardinal Focus and swallowed by the Working floor. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WorkingCraftGameTests {
	/** 32 x 12 x 24: room for a grand floor, its sockets and a raised subject. */
	private static final String TEMPLATE = "empty";
	private static final ResourceLocation LOOM = Hemomancy.rloc("cardinal_rite/working/somatic_loom");
	private static final ResourceLocation RELIQUARY = Hemomancy.rloc("cardinal_rite/working/mnemonic_reliquary");
	/** Chat and action-bar text sent to each fake player. */
	private static final Map<UUID, List<String>> SENT = new ConcurrentHashMap<>();
	private static final List<String> REHOMED = List.of("vascular_mending", "hematic_fortification", "crimson_beacon",
			"eternal_covenant", "sanguine_attunement", "chamber_of_will", "rooted_vein", "hungering_earth",
			"sanguine_fervor", "sanguine_eclipse", "pallid_shadow", "pallid_vessel_rite", "crimson_vessel_rite",
			"ashen_vessel_rite", "horn_of_culmination_rite", "hematic_unbinding", "scarlet_summons");

	private WorkingCraftGameTests() {
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 400, batch = "working_craft")
	public static void everyWorkingCraftSpawnsReadyToRun(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos focus = helper.absolutePos(new BlockPos(16, 3, 12));
		ServerPlayer player = player(helper);
		List<CardinalRiteRecipe> all = CardinalRiteRecipe.getAllRecipes(level);
		List<CardinalRiteRecipe> crafts = all.stream().filter(CardinalRiteRecipe::isWorkingCraft).toList();
		helper.assertTrue(crafts.size() == 20, "All twenty Working crafts must load; got " + crafts.size());
		for (CardinalRiteRecipe craft : crafts) {
			clear(level, focus);
			helper.assertTrue(PlaceStructurePacket.placeLayeredCardinalRite(level, focus, craft, player) > 0,
					craft.getId() + " did not spawn");
			var alone = CardinalRiteStationMatcher.resolve(level, focus, List.of(craft));
			helper.assertTrue(alone.status() == CardinalRiteStationMatcher.Status.MATCHED
					&& alone.matches().getFirst().station().subject() != null,
					craft.getId() + " did not find its subject: " + alone.status());
			var everyRite = CardinalRiteStationMatcher.resolve(level, focus, all);
			helper.assertTrue(everyRite.status() == CardinalRiteStationMatcher.Status.MATCHED
					&& everyRite.matches().getFirst().recipe().getId().equals(craft.getId()),
					craft.getId() + " is ambiguous among all rites: " + everyRite.status());
		}
		player.discard();
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 400, batch = "working_craft")
	public static void rehomedRitesResolveOnTheirPurposeFloors(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos focus = helper.absolutePos(new BlockPos(16, 3, 12));
		ServerPlayer player = player(helper);
		List<CardinalRiteRecipe> all = CardinalRiteRecipe.getAllRecipes(level);
		for (String name : REHOMED) {
			CardinalRiteRecipe rite = CardinalRiteRecipe.getRiteByLocation(level, Hemomancy.rloc("cardinal_rite/" + name));
			helper.assertTrue(rite != null, name + " must load");
			clear(level, focus);
			PlaceStructurePacket.placeLayeredCardinalRite(level, focus, rite, player);
			var resolution = CardinalRiteStationMatcher.resolve(level, focus, all);
			helper.assertTrue(resolution.status() == CardinalRiteStationMatcher.Status.MATCHED
					&& resolution.matches().getFirst().recipe().getId().equals(rite.getId()),
					name + " must resolve alone on " + rite.getFloorId() + ": " + resolution.status());
		}
		player.discard();
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "working_craft")
	public static void somaticLoomCraftRunsEndToEnd(GameTestHelper helper) {
		Fixture craft = prepareLoom(helper);
		ServerLevel level = helper.getLevel();
		helper.assertTrue(craft.rite().getPhase() == CardinalRitePhase.SUBJECT_FEEDING, "A craft starts by feeding");
		helper.assertTrue(RiteOfferingEscrow.isEscrowed(craft.rite().craft()), "Offerings must be escrowed at start");
		helper.assertTrue(craft.rite().feedSubject(craft.recipe().getSubjectFeedMl()), "The subject must take its feed");
		helper.assertTrue(craft.rite().getPhase() == CardinalRitePhase.OFFERING_PROCESSION, "A fed craft seals");
		helper.assertTrue(WorkingCraftRites.complete(level, craft.player(), craft.rite(), craft.recipe()),
				"The craft must complete");
		for (BlockPos pos : craft.subject()) {
			helper.assertTrue(level.getBlockState(pos).isAir(), "The floor must swallow the subject at " + pos);
		}
		helper.assertTrue(loomsNear(level, craft.focus()) == 1, "Exactly one Somatic Loom must condense on the Focus");
		helper.assertTrue(craft.player().getStats().getValue(Stats.ITEM_CRAFTED.get(BlockInit.somatic_loom.get().asItem())) > 0,
				"Completing the craft must grant machine access");
		helper.assertTrue(level.getBlockEntity(craft.focus()) instanceof CardinalFocusBlockEntity focus
				&& focus.getMediumForMatching().isEmpty(), "The seated medium must be consumed");
		craft.player().discard();
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "working_craft")
	public static void completionIsOnceOnly(GameTestHelper helper) {
		Fixture craft = prepareLoom(helper);
		ServerLevel level = helper.getLevel();
		craft.rite().feedSubject(craft.recipe().getSubjectFeedMl());
		helper.assertTrue(WorkingCraftRites.complete(level, craft.player(), craft.rite(), craft.recipe()), "First completion");
		helper.assertTrue(WorkingCraftRites.complete(level, craft.player(), craft.rite(), craft.recipe()),
				"A settled craft reports success again");
		helper.assertTrue(loomsNear(level, craft.focus()) == 1, "A repeated completion must not drop a second Loom");
		craft.player().discard();
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "working_craft")
	public static void recallReturnsEscrowAndLeavesSubject(GameTestHelper helper) {
		Fixture craft = prepareLoom(helper);
		ServerLevel level = helper.getLevel();
		craft.rite().feedSubject(40);
		CardinalRiteTermination.end(level, null, craft.rite(), CardinalRiteTermination.Outcome.CANCELLED);
		assertEscrowReturned(helper, craft);
		for (BlockPos pos : craft.subject()) {
			helper.assertTrue(!level.getBlockState(pos).isAir(), "A recalled craft leaves its structure standing");
		}
		helper.assertTrue(loomsNear(level, craft.focus()) == 0, "A recalled craft makes nothing");
		craft.player().discard();
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "working_craft")
	public static void brokenSubjectReturnsEscrow(GameTestHelper helper) {
		Fixture craft = prepareLoom(helper);
		ServerLevel level = helper.getLevel();
		level.setBlockAndUpdate(craft.subject().getFirst(), Blocks.AIR.defaultBlockState());
		helper.assertTrue(!WorkingCraftRites.subjectIntact(level, craft.rite()), "A broken subject must be noticed");
		helper.assertTrue(!WorkingCraftRites.complete(level, craft.player(), craft.rite(), craft.recipe()),
				"A broken subject must refuse completion");
		CardinalRiteTermination.end(level, null, craft.rite(), CardinalRiteTermination.Outcome.BROKEN);
		assertEscrowReturned(helper, craft);
		helper.assertTrue(loomsNear(level, craft.focus()) == 0, "A broken craft makes nothing");
		craft.player().discard();
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "working_craft")
	public static void floorDamageIsDetectedForThePause(GameTestHelper helper) {
		Fixture craft = prepareLoom(helper);
		ServerLevel level = helper.getLevel();
		var rite = craft.rite();
		helper.assertTrue(CardinalRiteStationMatcher.findCapturedStructure(level, craft.focus(), craft.recipe(),
				rite.getMatchedFloorId(), rite.getFloorForwards(), rite.getFloorUp()).isPresent(),
				"An intact floor validates without its escrowed braziers");
		level.setBlockAndUpdate(craft.focus().east(), Blocks.DIRT.defaultBlockState());
		helper.assertTrue(CardinalRiteStationMatcher.findCapturedStructure(level, craft.focus(), craft.recipe(),
				rite.getMatchedFloorId(), rite.getFloorForwards(), rite.getFloorUp()).isEmpty(),
				"A damaged floor must fail validation so the craft pauses");
		helper.assertTrue(WorkingCraftRites.subjectIntact(level, rite), "Floor damage alone leaves the subject intact");
		craft.player().discard();
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "working_craft")
	public static void projectionIntoIdleSubjectOnlyHints(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos focus = helper.absolutePos(new BlockPos(16, 3, 12));
		ServerPlayer player = player(helper);
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, LOOM);
		clear(level, focus);
		PlaceStructurePacket.placeLayeredCardinalRite(level, focus, recipe, player);
		var subject = CardinalRiteStationMatcher.resolve(level, focus, List.of(recipe)).matches().getFirst()
				.station().subject();
		BlockPos target = subject.positions().getFirst();
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setBloodVolume(1000.0D);
		double before = blood.getBloodVolume();
		helper.assertTrue(BloodStructureFeedManager.feedStructure(player, level, target, ItemStack.EMPTY),
				"Projection into an idle Working frame must be handled by its hint");
		helper.assertTrue(blood.getBloodVolume() == before, "The hint must not take blood");
		helper.assertTrue(!level.getBlockState(target).isAir(), "The hint must not touch the frame");
		helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, new AABB(target).inflate(3.0D)).isEmpty(),
				"The hint must not condense anything from the frame");
		player.discard();
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 100, batch = "working_craft")
	public static void reliquaryCraftWaitsForTheMnemonistsLesson(GameTestHelper helper) {
		ServerPlayer player = claimant(helper, 3);
		CardinalRiteRecipe reliquary = CardinalRiteRecipe.getRiteByLocation(helper.getLevel(), RELIQUARY);
		helper.assertTrue(reliquary != null && reliquary.isWorkingCraft(), "The Mnemonic Reliquary craft must load");
		helper.assertTrue(!RecipeDegreeGates.playerMayAttempt(player, reliquary),
				"A Degree 3 Harbinger must not raise the Reliquary before the Mnemonist teaches it");
		helper.assertTrue(RecipeDegreeGates.requirementLabel(reliquary).contains("Reliquary lesson"),
				"The refusal must name the Mnemonist's lesson");
		MnemonicReliquaryProgression.teach(player);
		helper.assertTrue(RecipeDegreeGates.playerMayAttempt(player, reliquary), "The lesson opens the craft");
		player.discard();
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "working_craft")
	public static void aRaisedSubjectIsReportedMissing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos focus = helper.absolutePos(new BlockPos(16, 3, 12));
		ServerPlayer player = player(helper);
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, LOOM);
		clear(level, focus);
		PlaceStructurePacket.placeLayeredCardinalRite(level, focus, recipe, player);
		var subject = CardinalRiteStationMatcher.resolve(level, focus, List.of(recipe)).matches().getFirst()
				.station().subject();
		// Lift the whole frame one block off the floor, top cells first.
		subject.positions().stream().sorted(java.util.Comparator.comparingInt((BlockPos pos) -> pos.getY()).reversed())
				.forEach(pos -> {
					level.setBlock(pos.above(), level.getBlockState(pos), 2);
					level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
				});
		var resolution = CardinalRiteStationMatcher.resolve(level, focus, CardinalRiteRecipe.getAllRecipes(level));
		helper.assertTrue(resolution.status() == CardinalRiteStationMatcher.Status.NO_SUBJECT,
				"A floating frame must be named as the missing subject, got " + resolution.status());
		player.discard();
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 1600, batch = "working_craft_engine")
	public static void somaticLoomCraftRunsThroughTheRealEngine(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos focus = helper.absolutePos(new BlockPos(16, 3, 12));
		ServerPlayer player = claimant(helper, 3);
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, LOOM);
		CardinalRiteSavedData saved = CardinalRiteSavedData.get(level);
		clear(level, focus);
		PlaceStructurePacket.placeLayeredCardinalRite(level, focus, recipe, player);
		var station = CardinalRiteStationMatcher.resolve(level, focus, List.of(recipe)).matches().getFirst().station();
		List<BlockPos> subject = station.subject().positions();
		player.setPos(Vec3.atCenterOf(focus).add(0.0D, 1.0D, 0.0D));
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.living_staff.get()));
		setServerPlayerLookup(player, true);
		var start = BloodCraftingKeyPressPacket.tryStartCardinalRite(player, focus,
				CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
		ActiveCardinalRite rite = saved.getRite(player.getUUID());
		helper.assertTrue(start == CardinalRiteActivationRules.ActivationAttempt.STARTED && rite != null
				&& rite.isWorkingCraft() && rite.hasEscrowedStaff(), "The Loom craft must start with the staff planted");
		helper.assertTrue(rite.getPhase() == CardinalRitePhase.SUBJECT_FEEDING, "A craft starts by feeding");
		for (var offering : station.braziers()) {
			var brazier = (IronBrazierBlockEntity) level.getBlockEntity(offering.pos());
			helper.assertTrue(brazier.getOfferingForMatching().isEmpty(), "Activation must escrow the brazier offerings");
			helper.assertTrue(!brazier.getRenderedOfferingStack().isEmpty(),
					"An escrowed offering stays visible in its brazier until the daemon breaks it");
		}
		HemoCapabilityAccess.requireBloodVolume(player).setBloodVolume(1000.0D);
		// Follow the daemon: it must swoop over each brazier to absorb its offering, then dive into the structure.
		List<BlockPos> braziers = station.braziers().stream().map(offering -> offering.pos()).toList();
		double[] nearestBrazier = new double[braziers.size()];
		java.util.Arrays.fill(nearestBrazier, Double.MAX_VALUE);
		double[] nearestHeart = {Double.MAX_VALUE};
		Vec3 heart = subject.stream().map(Vec3::atCenterOf).reduce(Vec3.ZERO, Vec3::add).scale(1.0D / subject.size());
		helper.onEachTick(() -> {
			var daemon = com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity.findBoundToRite(
					level, player.getUUID(), focus);
			if (daemon == null) return;
			if (rite.getPhase() == CardinalRitePhase.OFFERING_PROCESSION) {
				for (int i = 0; i < braziers.size(); i++) {
					nearestBrazier[i] = Math.min(nearestBrazier[i],
							daemon.position().distanceTo(Vec3.atCenterOf(braziers.get(i))));
				}
			} else if (rite.getPhase() == CardinalRitePhase.CULMINATION) {
				nearestHeart[0] = Math.min(nearestHeart[0], daemon.position().distanceTo(heart));
			}
		});
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(!rite.isStaffPlanting(), "The staff must finish planting"))
				.thenExecute(() -> {
					aim(player, Vec3.atCenterOf(subject.stream()
							.min(java.util.Comparator.comparingDouble(pos -> pos.distToCenterSqr(player.getEyePosition())))
							.orElseThrow()));
					for (int pass = 0; pass < 10 && rite.getPhase() == CardinalRitePhase.SUBJECT_FEEDING; pass++) {
						helper.assertTrue(CardinalRiteInteractionHandler.tryProject(level, player, 50.0D).bloodSpent() > 0,
								"Projection into the subject must feed it");
					}
					helper.assertTrue(rite.getPhase() != CardinalRitePhase.SUBJECT_FEEDING, "A fed subject seals the craft");
				})
				.thenWaitUntil(() -> helper.assertTrue(!saved.hasActiveRite(player.getUUID()),
						"The engine must carry the sealed craft to its end"))
				.thenExecute(() -> {
					try {
						helper.assertTrue(rite.isComplete() && RiteOfferingEscrow.isConsumed(rite.craft()),
								"The craft must complete and consume its escrow");
						helper.assertTrue(loomsNear(level, focus) == 1, "Exactly one Somatic Loom must condense");
						for (BlockPos pos : subject) {
							helper.assertTrue(level.getBlockState(pos).isAir(), "The floor must swallow " + pos);
						}
						saved.deliverRecovery(player);
						var inventory = player.getInventory();
						helper.assertTrue(inventory.countItem(ItemInit.living_staff.get()) == 1, "The staff must come back");
						helper.assertTrue(inventory.countItem(Items.STRING) == 0
								&& inventory.countItem(ItemInit.neurotic_enzyme.get()) == 0,
								"Consumed offerings must not come back");
						helper.assertTrue(player.getStats().getValue(
								Stats.ITEM_CRAFTED.get(BlockInit.somatic_loom.get().asItem())) > 0,
								"The craft must grant machine access");
						for (int i = 0; i < braziers.size(); i++) {
							helper.assertTrue(nearestBrazier[i] < 1.6D, "The daemon must fly to brazier " + braziers.get(i)
									+ " to absorb its offering; it came within " + nearestBrazier[i]);
						}
						helper.assertTrue(nearestHeart[0] < 0.6D,
								"The daemon must dive into the structure; it came within " + nearestHeart[0]);
						for (BlockPos pos : braziers) {
							helper.assertTrue(((IronBrazierBlockEntity) level.getBlockEntity(pos)).getRenderedOfferingStack().isEmpty(),
									"The daemon must have broken the offering at " + pos);
						}
					} finally {
						setServerPlayerLookup(player, false);
						player.discard();
					}
				})
				.thenSucceed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "working_craft")
	public static void aStaleCraftReloadedAfterSettlingTakesNothingTwice(GameTestHelper helper) {
		Fixture craft = prepareLoom(helper);
		ServerLevel level = helper.getLevel();
		// The rite data as last saved, before the craft settled; the chunk kept the settled world.
		var beforeSettling = craft.rite().serialize(level.registryAccess());
		craft.rite().feedSubject(craft.recipe().getSubjectFeedMl());
		helper.assertTrue(WorkingCraftRites.complete(level, craft.player(), craft.rite(), craft.recipe()), "First completion");

		ActiveCardinalRite stale = ActiveCardinalRite.deserialize(beforeSettling.copy(), level.registryAccess());
		helper.assertTrue(RiteOfferingEscrow.isEscrowed(stale.craft()), "The stale copy still holds its escrow");
		helper.assertTrue(WorkingCraftRites.complete(level, craft.player(), stale, craft.recipe()),
				"A stale copy of a settled craft reports it settled");
		helper.assertTrue(loomsNear(level, craft.focus()) == 1, "A stale copy must not condense a second Loom");

		ActiveCardinalRite broken = ActiveCardinalRite.deserialize(beforeSettling.copy(), level.registryAccess());
		CardinalRiteTermination.end(level, null, broken, CardinalRiteTermination.Outcome.BROKEN);
		helper.assertTrue(RiteOfferingEscrow.isConsumed(broken.craft()),
				"A stale copy whose structure is gone must not return offerings the Loom already took");
		CardinalRiteSavedData.get(level.getServer().overworld()).deliverRecovery(craft.player());
		var inventory = craft.player().getInventory();
		helper.assertTrue(inventory.countItem(Items.STRING) == 0 && inventory.countItem(ItemInit.neurotic_enzyme.get()) == 0,
				"Offerings spent on a settled craft must not come back");
		craft.player().discard();
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "working_craft")
	public static void aCraftWhoseEscrowWentHomeCannotComplete(GameTestHelper helper) {
		Fixture craft = prepareLoom(helper);
		ServerLevel level = helper.getLevel();
		craft.rite().feedSubject(craft.recipe().getSubjectFeedMl());
		RiteOfferingEscrow.returnEscrow(level, craft.rite(), craft.rite().craft());
		helper.assertTrue(!WorkingCraftRites.complete(level, craft.player(), craft.rite(), craft.recipe()),
				"A craft whose offerings were returned must not complete");
		helper.assertTrue(loomsNear(level, craft.focus()) == 0, "No Loom without its offerings");
		for (BlockPos pos : craft.subject()) {
			helper.assertTrue(!level.getBlockState(pos).isAir(), "The frame must still stand at " + pos);
		}
		craft.player().discard();
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "working_craft")
	public static void theIdleFrameHintNamesTheDegree(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos focus = helper.absolutePos(new BlockPos(16, 3, 12));
		ServerPlayer player = player(helper);
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, LOOM);
		clear(level, focus);
		PlaceStructurePacket.placeLayeredCardinalRite(level, focus, recipe, player);
		BlockPos target = CardinalRiteStationMatcher.resolve(level, focus, List.of(recipe)).matches().getFirst()
				.station().subject().positions().getFirst();
		SENT.remove(player.getUUID());
		BloodStructureFeedManager.feedStructure(player, level, target, ItemStack.EMPTY);
		String said = String.join(" | ", SENT.getOrDefault(player.getUUID(), List.of()));
		helper.assertTrue(said.contains("A frame for the") && said.contains("Degree 3"),
				"The idle hint must say how to raise the Loom and from which degree: " + said);
		player.discard();
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "working_craft")
	public static void projectionIntoARunningCraftGivesNoIdleHint(GameTestHelper helper) {
		Fixture craft = prepareLoom(helper);
		ServerLevel level = helper.getLevel();
		CardinalRiteSavedData saved = CardinalRiteSavedData.get(level);
		ServerPlayer onlooker = player(helper);
		saved.startRite(craft.rite());
		try {
			var blood = HemoCapabilityAccess.requireBloodVolume(onlooker);
			blood.setBloodVolume(1000.0D);
			SENT.remove(onlooker.getUUID());
			helper.assertTrue(BloodStructureFeedManager.feedStructure(onlooker, level, craft.subject().getFirst(), ItemStack.EMPTY),
					"Projection into a running craft's structure must be taken");
			String said = String.join(" | ", SENT.getOrDefault(onlooker.getUUID(), List.of()));
			helper.assertTrue(!said.contains("A frame for the") && said.contains("already"),
					"An onlooker must hear the frame is already being worked, not how to start it: " + said);
			helper.assertTrue(blood.getBloodVolume() == 1000.0D, "The onlooker's blood must not be taken");
		} finally {
			saved.removeRite(craft.player().getUUID());
			onlooker.discard();
			craft.player().discard();
		}
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 400, batch = "working_craft")
	public static void noWorkingStructureHidesAFloorlessOne(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos focus = helper.absolutePos(new BlockPos(16, 3, 12));
		ServerPlayer player = player(helper);
		for (CardinalRiteRecipe craft : CardinalRiteRecipe.getAllRecipes(level).stream()
				.filter(CardinalRiteRecipe::isWorkingCraft).toList()) {
			clear(level, focus);
			PlaceStructurePacket.placeWorkingSubject(level, focus, craft);
			for (BlockPos pos : BlockPos.betweenClosed(focus.offset(-5, 1, -5), focus.offset(5, 4, 5))) {
				if (level.getBlockState(pos).isAir()) continue;
				var floorless = com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodStructureCraftingHelper
						.findProjectionCraftMatch(player, level, pos.immutable(), ItemStack.EMPTY);
				helper.assertTrue(floorless.isEmpty(), craft.getId() + " contains the floorless "
						+ floorless.map(match -> match.recipe().getId().toString()).orElse("") + " at " + pos);
			}
		}
		player.discard();
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 100, batch = "working_craft")
	public static void theWorkingCatalogueListsEveryMakingRiteWithAResult(GameTestHelper helper) {
		var catalogued = CardinalRiteRecipe.getAllRecipes(helper.getLevel()).stream()
				.filter(CardinalRiteRecipe::isCataloguedAsMaking).map(recipe -> recipe.getId().getPath()).toList();
		for (String rite : List.of("pallid_vessel_rite", "crimson_vessel_rite", "ashen_vessel_rite",
				"horn_of_culmination_rite", "exsanguination", "working/somatic_loom", "working/vascular_effigy")) {
			helper.assertTrue(catalogued.contains("cardinal_rite/" + rite), rite + " must appear among Working crafts");
		}
		helper.assertTrue(catalogued.stream().filter(path -> path.startsWith("cardinal_rite/working/")).count() == 20,
				"All twenty Working crafts must be catalogued");
		for (String rite : List.of("sanguine_athanor", "true_groove", "rooted_vein", "sanguine_brotherhood")) {
			helper.assertTrue(!catalogued.contains("cardinal_rite/" + rite),
					rite + " makes no item (or is not a making rite) and must stay out");
		}
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "working_craft")
	public static void aFeedingCraftLeavesProjectionPastItsFloorAlone(GameTestHelper helper) {
		Fixture craft = prepareLoom(helper);
		ServerLevel level = helper.getLevel();
		CardinalRiteSavedData saved = CardinalRiteSavedData.get(level);
		saved.startRite(craft.rite());
		try {
			// An Alembic-like target nine blocks east, past the lesser floor's 5.5-block footprint.
			BlockPos beyond = craft.focus().offset(9, 1, 0);
			level.setBlockAndUpdate(beyond, Blocks.STONE.defaultBlockState());
			craft.player().setPos(Vec3.atBottomCenterOf(craft.focus().offset(6, 1, 0)));
			aim(craft.player(), Vec3.atCenterOf(beyond));
			helper.assertTrue(!CardinalRiteInteractionHandler.tryProject(level, craft.player(), 50.0D).handled(),
					"Projection at a block past the Working floor must be left to its own use");
			level.setBlock(beyond, Blocks.AIR.defaultBlockState(), 2);
		} finally {
			saved.removeRite(craft.player().getUUID());
			craft.player().discard();
		}
		helper.succeed();
	}

	// ---- fixtures ----

	private static ServerPlayer claimant(GameTestHelper helper, int degree) {
		ServerPlayer player = player(helper);
		HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(degree);
		HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> volume.setActive(true));
		return player;
	}

	private static void aim(ServerPlayer player, Vec3 target) {
		Vec3 delta = target.subtract(player.getEyePosition());
		player.setYRot((float) Math.toDegrees(Math.atan2(-delta.x, delta.z)));
		player.setXRot((float) -Math.toDegrees(Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z))));
	}

	/** The rite engine finds its caster through the server's player list. */
	@SuppressWarnings("unchecked")
	private static void setServerPlayerLookup(ServerPlayer player, boolean present) {
		try {
			var field = net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");
			field.setAccessible(true);
			var players = (java.util.Map<UUID, ServerPlayer>) field.get(player.server.getPlayerList());
			if (present) players.put(player.getUUID(), player);
			else players.remove(player.getUUID(), player);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Could not register the Working craft GameTest player", exception);
		}
	}

	private record Fixture(ServerPlayer player, BlockPos focus, CardinalRiteRecipe recipe, ActiveCardinalRite rite,
			List<BlockPos> subject) {
	}

	private static Fixture prepareLoom(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos focus = helper.absolutePos(new BlockPos(16, 3, 12));
		ServerPlayer player = player(helper);
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, LOOM);
		helper.assertTrue(recipe != null && recipe.isWorkingCraft(), "The Somatic Loom craft must load");
		clear(level, focus);
		PlaceStructurePacket.placeLayeredCardinalRite(level, focus, recipe, player);
		var resolution = CardinalRiteStationMatcher.resolve(level, focus, List.of(recipe));
		helper.assertTrue(resolution.status() == CardinalRiteStationMatcher.Status.MATCHED, "Loom must resolve");
		var station = resolution.matches().getFirst().station();
		ActiveCardinalRite rite = ActiveCardinalRite.workingCraft(player.getUUID(), focus, recipe.getId(),
				recipe.getRiteType().getSize(), recipe.getSubjectFeedMl());
		rite.setMatchedFloor(station.floor().id(), station.floorMatch().getForwards(), station.floorMatch().getUp());
		rite.captureOfferingItinerary(station.braziers().stream()
				.map(offering -> new ActiveCardinalRite.RiteOffering(offering.pos(), offering.stack(), true)).toList());
		helper.assertTrue(WorkingCraftRites.prepare(level, rite, station.subject()), "Loom preparation failed");
		return new Fixture(player, focus, recipe, rite, station.subject().positions());
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "working_craft")
	public static void aRecalledCraftEmptiesItsBraziersDisplay(GameTestHelper helper) {
		Fixture craft = prepareLoom(helper);
		ServerLevel level = helper.getLevel();
		for (var offering : craft.rite().getOfferingItinerary()) {
			helper.assertTrue(!((IronBrazierBlockEntity) level.getBlockEntity(offering.pos())).getRenderedOfferingStack().isEmpty(),
					"The escrowed offering shows in its brazier while the craft runs");
		}
		CardinalRiteTermination.end(level, null, craft.rite(), CardinalRiteTermination.Outcome.CANCELLED);
		for (var offering : craft.rite().getOfferingItinerary()) {
			helper.assertTrue(((IronBrazierBlockEntity) level.getBlockEntity(offering.pos())).getRenderedOfferingStack().isEmpty(),
					"A recalled craft's offerings go home, so its braziers must not keep showing them");
		}
		craft.player().discard();
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 120, batch = "working_craft")
	public static void aBrazierForgetsTheOfferingOfARiteThatNoLongerExists(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(4, 3, 4));
		helper.getLevel().setBlockAndUpdate(pos, BlockInit.iron_brazier.get().defaultBlockState());
		var brazier = (IronBrazierBlockEntity) helper.getLevel().getBlockEntity(pos);
		brazier.showEscrowed(new ItemStack(Items.STRING), UUID.randomUUID());
		helper.assertTrue(!brazier.getRenderedOfferingStack().isEmpty() && !brazier.insertOffering(null, new ItemStack(Items.BONE)),
				"An escrow display holds the bowl");
		helper.succeedWhen(() -> helper.assertTrue(brazier.getRenderedOfferingStack().isEmpty(),
				"A display left by a vanished rite (crash, lost data) must clear itself"));
	}

	private static void assertEscrowReturned(GameTestHelper helper, Fixture craft) {
		helper.assertTrue(RiteOfferingEscrow.RETURNED.equals(craft.rite().craft().getString("EscrowState")),
				"The escrow must be returned");
		CardinalRiteSavedData.get(helper.getLevel().getServer().overworld()).deliverRecovery(craft.player());
		var inventory = craft.player().getInventory();
		helper.assertTrue(inventory.countItem(Items.STRING) == 1
				&& inventory.countItem(ItemInit.neurotic_enzyme.get()) == 1,
				"The Loom's string and neurotic enzyme must come back through recovery");
	}

	private static int loomsNear(ServerLevel level, BlockPos focus) {
		return level.getEntitiesOfClass(ItemEntity.class, new AABB(focus).inflate(4.0D),
				entity -> entity.getItem().is(BlockInit.somatic_loom.get().asItem()))
				.stream().mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	private static void clear(ServerLevel level, BlockPos focus) {
		for (BlockPos pos : BlockPos.betweenClosed(focus.offset(-7, -1, -7), focus.offset(7, 4, 7))) {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
		}
		for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(focus).inflate(8.0D))) {
			item.discard();
		}
	}

	private static ServerPlayer player(GameTestHelper helper) {
		var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "working-craft"), false);
		var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(),
				cookie.clientInformation());
		var connection = new Connection(PacketFlow.SERVERBOUND);
		new EmbeddedChannel(connection);
		new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
			@Override
			public void send(net.minecraft.network.protocol.Packet<?> packet) {
				send(packet, null);
			}

			@Override
			public void send(net.minecraft.network.protocol.Packet<?> packet,
					net.minecraft.network.PacketSendListener listener) {
				String text = packet instanceof net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket bar
						? bar.text().getString()
						: packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket chat
								? chat.content().getString() : null;
				if (text != null) SENT.computeIfAbsent(cookie.gameProfile().getId(), id -> new ArrayList<>()).add(text);
			}
		};
		return player;
	}
}
