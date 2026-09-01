package com.vincenthuto.hemomancy.gametest.journey;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.armor.ability.ArmorSetAbilityRegistry;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.HematicArmatureBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.CardinalFocusBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.block.inscription.DiscoveryInscriptionDefinition;
import com.vincenthuto.hemomancy.common.block.inscription.DiscoveryInscriptionRegistry;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemoryActivationService;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.HemomancyDiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.*;
import com.vincenthuto.hemomancy.common.event.ArmorSetBonusHandler;
import com.vincenthuto.hemomancy.common.event.BloodStructureFeedManager;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.event.PendingBloodCraftManager;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.QliphothPomeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.LivingWeaponGraftRite;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.MycelialLanternMenu;
import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment;
import com.vincenthuto.hemomancy.common.mission.shared.NoeticDiscoveryProgression;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.StartCentrifugeButtonPacket;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureOfferingPlacement;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.ScarBrazierRite;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules;
import com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.*;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.MasonsEffigyBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import com.vincenthuto.hemomancy.common.tile.inscription.DiscoveryInscriptionBlockEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.lang.reflect.Method;
import java.util.List;

/** Performs one real server-side action for each Harbinger operator checkpoint. */
public final class HarbingerJourneyAutomation {
	private HarbingerJourneyAutomation() { }

	public static void perform(ServerPlayer player, String stageId, BlockPos origin) {
		HemoJourneyStage stage = stage(stageId);
		switch (stage) {
			case MORTAL_DISPLAY -> useBlock(player, origin.above());
			case SANGUINE_INITIATION -> sanguineInitiation(player, origin);
			case FIRST_REMNANT_DISCOVERED -> discoverFirstRemnant(player, origin.above());
			case VICAR_HERMIT_ROAD_REPORT -> vicarHermitRoadReport(player, origin);
			case VESSEL_FILLED -> fillVessel(player);
			case FORMATION_PROJECTED -> projectFormation(player, origin);
			case LIBER_CRAFTED -> craftStructure(player, origin, "liber_sanguinum", origin.above(), null);
			case HEMATIC_IRON_CRAFTED -> craftStructure(player, origin, "hematic_iron_block", origin.above(), null);
			case LIVING_STAFF_CRAFTED -> craftStructure(player, origin, "living_staff", origin.above(2), null);
			case VICAR_REWARD -> dialogue(player, origin, HarbingerVicarEntity.class,
					HarbingerVicarDialogueTrees.EVENT_CLAIM_FIRST_BLOODCRAFT_REWARD);
			case VOTARY_RITE -> completeRankRite(player, origin, CardinalRiteActivationRules.Trigger.HEMATIC_MEDIUM_BLOCK_USE);
			case DEGREE_2_REACHED -> { }
			case ALCHEMIST_BRIEFING -> dialogue(player, origin, HarbingerAlchemistEntity.class,
					HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_BRIEF);
			case CENTRIFUGE_PREPARED -> craftStructure(player, origin, "vial_centrifuge", origin.above(2),
					BlockInit.vial_centrifuge.get().asItem());
			case SEPARATION_STARTED -> startSeparation(player, origin);
			case ENZYME_RECOVERED -> recoverEnzyme(player, origin);
			case ALCHEMIST_REWARD -> dialogue(player, origin, HarbingerAlchemistEntity.class,
					HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_CLAIM);
			case BODY_ANSWERS_BRIEFING -> dialogue(player, origin, HarbingerAlchemistEntity.class,
					HarbingerAlchemistDialogueTrees.EVENT_BODY_ANSWERS_BRIEF);
			case BODY_ANSWERS_TINCTURE -> drinkBodyAnswers(player, origin);
			case RED_TAXONOMY -> redTaxonomy(player, origin);
			case LIVING_BESTIARY_RECORD -> recordBestiary(player, origin);
			case LIVING_BESTIARY_SURRENDER -> dialogue(player, origin, HarbingerAlchemistEntity.class,
					HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_SURRENDER);
			case HYPHAE_DISCOVERED -> touchDrop(player, origin, ItemInit.fungal_spine.get());
			case ARTIFICER_WORN_VOW_BRIEFING -> artificer(player, origin,
					HarbingerArtificerDialogueTrees.EVENT_BRIEF_WORN_VOW);
			case ARTIFICER_ARMATURE_PLACED -> placeArmature(player, origin);
			case ARTIFICER_HEMATIC_UPGRADE, ARTIFICER_FORK_UPGRADE, ARTIFICER_BLOOD_LUST_UPGRADE,
					ARTIFICER_D7_UPGRADE -> runArmature(player, origin);
			case ARTIFICER_WORN_VOW_REWARD -> artificer(player, origin,
					HarbingerArtificerDialogueTrees.EVENT_CLAIM_WORN_VOW_REWARD);
			case ARTIFICER_WORN_VOW_FITTING -> artificer(player, origin,
					HarbingerArtificerDialogueTrees.EVENT_CLAIM_HEMATIC_IRON_FITTING);
			case ENZYME_MASTERY -> BloodVolumeEvents.playerTick(new PlayerTickEvent.Post(player));
			case INITIATE_RITE, ADEPT_RITE, ILLUMINATUS_RITE, SANCTIFIED_RITE, ARCHON_RITE ->
					completeRankRite(player, origin, CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
			case FIRST_CULTURE -> firstCulture(player, origin);
			case WOVEN_VESSEL_TURN_IN -> dialogue(player, origin, HarbingerMnemonistEntity.class,
					HarbingerMnemonistDialogueTrees.EVENT_WOVEN_VESSEL_TURN_IN);
			case FIRST_MEMORY_WOVEN -> weaveMemory(player, origin);
			case NOETIC_MARK_RECOGNIZED -> NoeticDiscoveryProgression.recognizeFromMnemonist(player);
			case ARTIFICER_THREE_ANSWERS_BRIEFING -> artificer(player, origin,
					HarbingerArtificerDialogueTrees.EVENT_BRIEF_THREE_ANSWERS);
			case ARTIFICER_THREE_ANSWERS_INSPECTION -> artificer(player, origin,
					HarbingerArtificerDialogueTrees.EVENT_INSPECT_THREE_ANSWERS);
			case ARTIFICER_THREE_ANSWERS_COUNSEL -> dialogue(player, origin, HarbingerAlchemistEntity.class,
					HarbingerArtificerDialogueTrees.EVENT_CLAIM_THREE_ANSWERS_REWARD);
			case ARTIFICER_BARBED_RESEARCH -> barbedResearch(player, origin);
			case ARTIFICER_BARBED_RESEARCH_REWARD -> dialogue(player, origin, HarbingerAlchemistEntity.class,
					HarbingerAlchemistDialogueTrees.EVENT_CLAIM_ARMOR_RESEARCH_REWARD);
			case ARTIFICER_FORK_DEMONSTRATION -> demonstrateFork(player, origin);
			case ARTIFICER_FORK_FITTING -> artificer(player, origin,
					HarbingerArtificerDialogueTrees.EVENT_CLAIM_FORK_FITTING);
			case VEIN_MASON_LESSON -> mason(player, origin, HarbingerCicatrixAnchoriteDialogueTrees.EVENT_FIRST_LESSON);
			case FIRST_SCAR_CARVED, VEIN_MASON_D6_SCAR_CARVED ->
					((ScarStationBlockEntity) player.serverLevel().getBlockEntity(origin.above())).craftEvent();
			case FIRST_SCAR_LEARNED, FIRST_EFFIGY_LOADOUT, VEIN_MASON_D6_SCAR_LEARNED,
					VEIN_MASON_D6_LOADOUT -> absorbScar(player, origin.above());
			case FIRST_EFFIGY_PATTERN -> createEffigyPattern(player, origin);
			case VEIN_MASON_REWARD -> mason(player, origin,
					HarbingerCicatrixAnchoriteDialogueTrees.EVENT_CONTINUATION_REWARD);
			case VEIN_MASON_D5_STRAIN -> MuscleMemoryActivationService.tryTrigger(player, MuscleMemory.SANGUINE_FISTS, 20);
			case VEIN_MASON_D5_DIAGNOSIS -> mason(player, origin,
					HarbingerCicatrixAnchoriteDialogueTrees.EVENT_DIAGNOSIS);
			case VEIN_MASON_D5_TREATMENT -> player.getMainHandItem().getItem()
					.finishUsingItem(player.getMainHandItem(), player.serverLevel(), player);
			case VEIN_MASON_D5_FORTIFICATION, FOUNDING_FANE, COVENANT_VIGIL, APOTHEOS_RITE ->
					startRite(player, origin, CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
			case VEIN_MASON_D5_REWARD -> mason(player, origin, HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D5_REWARD);
			case ARTIFICER_ASSUMED_LIMB_BRIEFING -> artificer(player, origin,
					HarbingerArtificerDialogueTrees.EVENT_BRIEF_ASSUMED_LIMB);
			case ARTIFICER_FIRST_LIVING_GRAFT -> absorbGraft(player, origin.above());
			case ARTIFICER_ASSUMED_LIMB_REWARD -> artificer(player, origin,
					HarbingerArtificerDialogueTrees.EVENT_CLAIM_ASSUMED_LIMB_REWARD);
			case ARTIFICER_LIVING_ARSENAL_DEMONSTRATION -> killFixtureTarget(player, origin);
			case ARTIFICER_FULL_LIVING_ARSENAL -> {
				for (int index = 0; index < 6; index++) absorbGraft(player, origin.offset(index % 3 - 1, 1, index / 3));
			}
			case ARTIFICER_LIVING_ARSENAL_FITTING -> artificer(player, origin,
					HarbingerArtificerDialogueTrees.EVENT_CLAIM_LIVING_ARSENAL_FITTING);
			case ARTIFICER_CRIMSON_VESTMENT_BRIEFING -> artificer(player, origin,
					HarbingerArtificerDialogueTrees.EVENT_BRIEF_CRIMSON_VESTMENT);
			case VICAR_CONSECRATION_KIT -> dialogue(player, origin, HarbingerVicarEntity.class,
					HarbingerVicarDialogueTrees.EVENT_CONSECRATION_KIT);
			case ARTIFICER_FRAME_CONSECRATED, ARTIFICER_MONOLITHIC_FRAME ->
					armature(player, origin).applyArmatureUpgradeItem(player, InteractionHand.MAIN_HAND);
			case ARTIFICER_CRIMSON_VESTMENT_INSPECTION -> artificer(player, origin,
					HarbingerArtificerDialogueTrees.EVENT_INSPECT_CRIMSON_VESTMENT);
			case ARTIFICER_CRIMSON_VESTMENT_COUNSEL -> dialogue(player, origin, HarbingerAlchemistEntity.class,
					HarbingerArtificerDialogueTrees.EVENT_CLAIM_CRIMSON_VESTMENT_REWARD);
			case ARTIFICER_BLOOD_LUST_DEMONSTRATION -> demonstrateBloodLust(player, origin);
			case ARTIFICER_BLOOD_LUST_FITTING -> artificer(player, origin,
					HarbingerArtificerDialogueTrees.EVENT_CLAIM_BLOOD_LUST_FITTING);
			case VEIN_MASON_D6_REFERRAL -> mason(player, origin,
					HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REFERRAL);
			case VEIN_MASON_D6_COUNSEL -> dialogue(player, origin, HarbingerMnemonistEntity.class,
					HarbingerMnemonistDialogueTrees.EVENT_ANCHORITE_COUNSEL);
			case VEIN_MASON_D6_FIRST_ROUTE, VEIN_MASON_D6_SECOND_ROUTE -> castSelectedManipulation(player);
			case VEIN_MASON_D6_REWARD -> mason(player, origin, HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REWARD);
			case CHAMBER_RETURNED -> { }
			case COVENANT_THRONE_BOUND -> useBlock(player, origin.above());
			case ARTIFICER_WEIGHT_OF_FRAME_BRIEFING -> artificer(player, origin,
					HarbingerArtificerDialogueTrees.EVENT_BRIEF_WEIGHT_OF_FRAME);
			case ARTIFICER_WEIGHT_OF_FRAME_INSPECTION -> artificer(player, origin,
					HarbingerArtificerDialogueTrees.EVENT_INSPECT_WEIGHT_OF_FRAME);
			case ARTIFICER_D7_DEMONSTRATION -> ArmorSetAbilityRegistry.tryActivate(player,
					ArmorSetAbilityRegistry.EDACIOUS_BLOODBURST);
			case ARTIFICER_D7_FITTING -> artificer(player, origin,
					HarbingerArtificerDialogueTrees.EVENT_CLAIM_D7_FITTING);
			case QLIPHOTH_COMMUNION -> consumePomes(player, origin);
			case APOTHEOS_CHOICE -> NeoForge.EVENT_BUS.post(new DialogueEvent(player,
					"archon_choice_eighth_degree", 0));
			case COMPLETE -> { }
		}
	}

	private static HemoJourneyStage stage(String id) {
		for (HemoJourneyStage stage : HemoJourneyStage.values()) if (stage.id().equals(id)) return stage;
		throw new IllegalArgumentException("Unknown Harbinger journey stage: " + id);
	}

	private static void discoverFirstRemnant(ServerPlayer player, BlockPos pos) {
		if (!(player.serverLevel().getBlockEntity(pos) instanceof DiscoveryInscriptionBlockEntity inscription)) {
			throw new IllegalStateException("First Remnant inscription is missing");
		}
		DiscoveryInscriptionDefinition definition = DiscoveryInscriptionRegistry.get(inscription.getInscriptionId())
				.orElseThrow(() -> new IllegalStateException("First Remnant inscription definition is missing"));
		if (!definition.isReadable(HemoCapabilityAccess.getPlayerDegreeNumber(player))) {
			throw new IllegalStateException("First Remnant inscription is not readable");
		}
		HemomancyDiscoverySource source = definition.kind() == DiscoveryInscriptionDefinition.Kind.BLOOD_ECHO
				? HemomancyDiscoverySource.BLOOD_ECHO : HemomancyDiscoverySource.RITE_FRAGMENT;
		LiberKnowledgeHelper.unlockEntries(player, definition.liberEntries(), source);
		definition.advancements().forEach(advancement ->
				HarbingerAdvancementGranter.grantIfNotDone(player, advancement));
	}

	private static void vicarHermitRoadReport(ServerPlayer player, BlockPos origin) {
		HarbingerVicarEntity vicar = entity(player, origin, HarbingerVicarEntity.class);
		try {
			Method ledger = HarbingerVicarEntity.class.getDeclaredMethod(
					"grantOrRecoverAssignmentLedger", ServerPlayer.class);
			ledger.setAccessible(true);
			ledger.invoke(vicar, player);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Vicar Assignment Ledger hook is unavailable", exception);
		}
		NeoForge.EVENT_BUS.post(new DialogueEvent(player,
				HarbingerVicarDialogueTrees.EVENT_HERMIT_ROAD_REPORT, vicar.getId()));
	}

	private static void sanguineInitiation(ServerPlayer player, BlockPos origin) {
		BlockPos focusPos = origin.above();
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(focusPos), Direction.UP, focusPos, false);
		try {
			Method interaction = CardinalFocusBlock.class.getDeclaredMethod("useItemOn", ItemStack.class,
					net.minecraft.world.level.block.state.BlockState.class, net.minecraft.world.level.Level.class,
					BlockPos.class, net.minecraft.world.entity.player.Player.class, InteractionHand.class,
					BlockHitResult.class);
			interaction.setAccessible(true);
			interaction.invoke(player.serverLevel().getBlockState(focusPos).getBlock(), player.getMainHandItem(),
					player.serverLevel().getBlockState(focusPos), player.serverLevel(), focusPos, player,
					InteractionHand.MAIN_HAND, hit);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Sanguine Initiation interaction hook is unavailable", exception);
		}
		if (CardinalRiteSavedData.get(player.serverLevel()).getRite(player.getUUID()) == null) {
			var focus = (CardinalFocusBlockEntity) player.serverLevel().getBlockEntity(focusPos);
			throw new IllegalStateException("Sanguine Initiation interaction did not start a rite: item="
					+ player.getMainHandItem() + ", health=" + player.getHealth() + ", medium="
					+ focus.getMediumDisplayStack() + ", display=" + focus.getTempleDisplay());
		}
		completeActiveRite(player);
	}

	private static void fillVessel(ServerPlayer player) {
		ItemStack jug = player.getOffhandItem();
		jug.getItem().use(player.serverLevel(), player, InteractionHand.OFF_HAND);
		BloodVolumeEvents.playerTick(new PlayerTickEvent.Post(player));
	}

	private static void projectFormation(ServerPlayer player, BlockPos origin) {
		player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(origin.above()));
		ItemStack projection = player.getMainHandItem();
		projection.getItem().use(player.serverLevel(), player, InteractionHand.MAIN_HAND);
		projection.getItem().onUseTick(player.serverLevel(), player, projection,
				projection.getUseDuration(player) - 1);
		player.stopUsingItem();
	}

	private static void craftStructure(ServerPlayer player, BlockPos origin, String recipePath, BlockPos hit,
			Item placeResult) {
		BloodStructureRecipe recipe = BloodStructureRecipe.getStructureByLocation(player.serverLevel(),
				Hemomancy.rloc("blood_structure/" + recipePath));
		if (recipe == null) throw new IllegalStateException("Missing blood structure recipe: " + recipePath);
		prepareOfferings(player, origin, hit, recipe);
		if (!BloodStructureFeedManager.feedStructure(player, player.serverLevel(), hit,
				player.getOffhandItem(), recipe.getBloodCost() + 1.0D)) {
			throw new IllegalStateException("Blood structure did not accept projection: " + recipePath);
		}
		for (int tick = 0; tick < 40; tick++) PendingBloodCraftManager.tick();
		touchDrop(player, origin, recipe.getResult().getItem());
		if (placeResult != null) {
			selectItem(player, placeResult);
			BlockPos support = origin.above();
			BlockHitResult placement = new BlockHitResult(Vec3.atCenterOf(support), Direction.UP, support, false);
			player.gameMode.useItemOn(player, player.serverLevel(), player.getMainHandItem(),
					InteractionHand.MAIN_HAND, placement);
		}
	}

	private static void prepareOfferings(ServerPlayer player, BlockPos origin, BlockPos hit,
			BloodStructureRecipe recipe) {
		if (recipe.getOfferings().isEmpty()) return;
		var slots = BloodStructureOfferingPlacement.plan(hit, 1, 1, 1, recipe.getOfferings());
		for (var slot : slots) {
			if (!(player.serverLevel().getBlockEntity(slot.pos()) instanceof IronBrazierBlockEntity brazier)) {
				throw new IllegalStateException("Missing offering brazier at " + slot.pos());
			}
			if (!brazier.hasOffering() && !brazier.insertOffering(player, slot.representativeStack().copy())) {
				throw new IllegalStateException("Offering was rejected");
			}
			if (player.serverLevel().getBlockState(slot.pos()).getValue(BrazierBlock.RITUAL_PHASE) == 0) {
				((BrazierBlock) player.serverLevel().getBlockState(slot.pos()).getBlock()).projectBloodIntoBlock(
						player.serverLevel(), slot.pos(), player.serverLevel().getBlockState(slot.pos()), player,
						BrazierBlock.BLOOD_TO_LIGHT);
			}
		}
	}

	private static void startSeparation(ServerPlayer player, BlockPos origin) {
		VialCentrifugeBlockEntity station = centrifuge(player, origin);
		List<Entity> cows = player.serverLevel().getEntitiesOfClass(Entity.class, HemoJourneyFixtures.bounds(origin),
				entity -> entity.getType() == net.minecraft.world.entity.EntityType.COW);
		for (int i = 0; i < 2; i++) {
			selectItem(player, ItemInit.bloody_vial.get());
			ItemStack vial = player.getMainHandItem();
			vial.getItem().onLeftClickEntity(vial, player, cows.get(i));
			station.setItem(i == 0 ? 2 : 6, vial.copy());
			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		}
		VialCentrifugeStartupResult result = StartCentrifugeButtonPacket.start(player, station);
		if (result != VialCentrifugeStartupResult.SUCCESS) {
			throw new IllegalStateException("Prepared Vial Centrifuge did not start: " + result);
		}
	}

	private static void recoverEnzyme(ServerPlayer player, BlockPos origin) {
		VialCentrifugeBlockEntity station = centrifuge(player, origin);
		for (int tick = 0; tick <= VialCentrifugeBlockEntity.SPIN_TOTAL_TIME; tick++) {
			VialCentrifugeBlockEntity.serverTick(player.serverLevel(), origin.above(2),
					player.serverLevel().getBlockState(origin.above(2)), station);
		}
		for (int slot = 10; slot < 18; slot++) {
			ItemStack output = station.removeItemNoUpdate(slot);
			if (!output.isEmpty() && FirstSeparationAssignment.tryRecoverAssignmentOutput(player, output)) {
				player.addItem(output);
				return;
			}
		}
		throw new IllegalStateException("Centrifuge produced no assignment enzyme");
	}

	private static void drinkBodyAnswers(ServerPlayer player, BlockPos origin) {
		BlockPos pos = origin.above();
		GhastlyAlembicBlockEntity alembic = (GhastlyAlembicBlockEntity) player.serverLevel().getBlockEntity(pos);
		for (int tick = 0; tick < 200; tick++) GhastlyAlembicBlockEntity.serverTick(
				player.serverLevel(), pos, player.serverLevel().getBlockState(pos), alembic);
		ItemStack tincture = alembic.removeItemNoUpdate(GhastlyAlembicBlockEntity.SLOT_RESULT);
		tincture.getItem().finishUsingItem(tincture, player.serverLevel(), player);
	}

	private static void redTaxonomy(ServerPlayer player, BlockPos origin) {
		HarbingerAlchemistEntity alchemist = entity(player, origin, HarbingerAlchemistEntity.class);
		for (var sample : java.util.Arrays.stream(HarbingerAlchemistDialogueTrees.RedTaxonomySample.values()).limit(4).toList()) {
			selectItem(player, sample.block().asItem());
			NeoForge.EVENT_BUS.post(new DialogueEvent(player, sample.eventId(), alchemist.getId()));
		}
	}

	private static void recordBestiary(ServerPlayer player, BlockPos origin) {
		Mob specimen = player.serverLevel().getEntitiesOfClass(Mob.class, HemoJourneyFixtures.bounds(origin),
				mob -> mob.getType() == EntityInit.crimson_doe.get()).getFirst();
		ItemStack jar = player.getMainHandItem();
		jar.getItem().interactLivingEntity(jar, player, specimen, InteractionHand.MAIN_HAND);
		dialogue(player, origin, HarbingerAlchemistEntity.class, HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_RECORD);
	}

	private static void placeArmature(ServerPlayer player, BlockPos origin) {
		BlockPos pos = origin.above();
		var state = BlockInit.hematic_armature.get().defaultBlockState().setValue(HematicArmatureBlock.FACING, Direction.SOUTH);
		player.serverLevel().setBlock(pos, state, Block.UPDATE_ALL);
		BlockInit.hematic_armature.get().setPlacedBy(player.serverLevel(), pos, state, player,
				new ItemStack(BlockInit.hematic_armature.get()));
	}

	private static void runArmature(ServerPlayer player, BlockPos origin) {
		BlockPos pos = origin.above();
		HematicArmatureBlockEntity armature = armature(player, origin);
		BlockInit.hematic_armature.get().stepOn(player.serverLevel(), pos, player.serverLevel().getBlockState(pos), player);
		for (int tick = 0; tick < 103; tick++) HematicArmatureBlockEntity.serverTick(
				player.serverLevel(), pos, player.serverLevel().getBlockState(pos), armature);
		player.stopRiding();
	}

	private static void firstCulture(ServerPlayer player, BlockPos origin) {
		BlockPos pos = origin.above();
		MycelialLanternBlockEntity lantern = (MycelialLanternBlockEntity) player.serverLevel().getBlockEntity(pos);
		for (int tick = 0; tick < 2400; tick++) MycelialLanternBlockEntity.serverTick(
				player.serverLevel(), pos, player.serverLevel().getBlockState(pos), lantern);
		MycelialLanternMenu menu = new MycelialLanternMenu(0, player.getInventory(), lantern, lantern.dataAccess);
		ItemStack output = menu.getSlot(MycelialLanternMenu.OUTPUT_SLOT).remove(1);
		menu.getSlot(MycelialLanternMenu.OUTPUT_SLOT).onTake(player, output);
		player.addItem(output);
	}

	private static void weaveMemory(ServerPlayer player, BlockPos origin) {
		SomaticLoomBlockEntity loom = (SomaticLoomBlockEntity) player.serverLevel().getBlockEntity(origin.above());
		if (!loom.startRitual(player) || !loom.tryChargeRitualBlood(player, 10_000.0D, true)) {
			throw new IllegalStateException("Somatic Loom did not start");
		}
		while (loom.getRitualOrbs().stream().anyMatch(orb -> !orb.completed())) {
			SomaticLoomBlockEntity.RitualOrb orb = loom.getRitualOrbs().stream().filter(value -> !value.completed()).findFirst().orElseThrow();
			for (int pull = 0; pull < 200 && !orb.completed(); pull++) {
				player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(origin.above()).add(orb.offset()));
				loom.dragSelectedOrb(player, 1.0D);
			}
			if (!orb.completed()) throw new IllegalStateException("Somatic Loom strand could not be drawn home");
		}
	}

	private static void barbedResearch(ServerPlayer player, BlockPos origin) {
		for (var type : List.of(EntityInit.barbed_urchin.get(), EntityInit.desiccant.get(), EntityInit.venom_rib_centipede.get())) {
			selectEmptyJar(player);
			Mob specimen = player.serverLevel().getEntitiesOfClass(Mob.class, HemoJourneyFixtures.bounds(origin),
					mob -> mob.getType() == type).getFirst();
			ItemStack jar = player.getMainHandItem();
			jar.getItem().interactLivingEntity(jar, player, specimen, InteractionHand.MAIN_HAND);
			dialogue(player, origin, HarbingerAlchemistEntity.class, HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_RECORD);
		}
	}

	private static void demonstrateFork(ServerPlayer player, BlockPos origin) {
		Zombie attacker = entity(player, origin, Zombie.class);
		ArmorSetBonusHandler.onPlayerHurt(new LivingDamageEvent.Pre(player,
				new DamageContainer(player.damageSources().mobAttack(attacker), 8.0F)));
	}

	private static void createEffigyPattern(ServerPlayer player, BlockPos origin) {
		MasonsEffigyBlockEntity effigy = (MasonsEffigyBlockEntity) player.serverLevel().getBlockEntity(origin.above());
		if (!effigy.beginMotifRitual(player, player.getMainHandItem())
				|| !effigy.tryReceiveProjectedBlood(player, 10_000.0D, false)) {
			throw new IllegalStateException("Mason's Effigy did not complete its motif");
		}
	}

	private static void absorbScar(ServerPlayer player, BlockPos pos) {
		player.startUsingItem(InteractionHand.MAIN_HAND);
		for (int tick = 0; tick < 100; tick++) ScarBrazierRite.tryAbsorb(
				player.serverLevel(), pos, player.serverLevel().getBlockState(pos), player, 1.0D);
		player.stopUsingItem();
	}

	private static void absorbGraft(ServerPlayer player, BlockPos pos) {
		player.startUsingItem(InteractionHand.MAIN_HAND);
		for (int tick = 0; tick < LivingWeaponGraftRite.REQUIRED_CHANNEL_TICKS; tick++) LivingWeaponGraftRite.tryAbsorb(
				player.serverLevel(), pos, player.serverLevel().getBlockState(pos), player, 1.0D);
		player.stopUsingItem();
	}

	private static void killFixtureTarget(ServerPlayer player, BlockPos origin) {
		Entity target = player.serverLevel().getEntitiesOfClass(Entity.class, HemoJourneyFixtures.bounds(origin),
				entity -> entity instanceof net.minecraft.world.entity.LivingEntity && !(entity instanceof ServerPlayer)).getFirst();
		target.hurt(player.damageSources().playerAttack(player), 1000.0F);
	}

	private static void demonstrateBloodLust(ServerPlayer player, BlockPos origin) {
		Zombie target = entity(player, origin, Zombie.class);
		ArmorSetBonusHandler.onLivingDamage(new LivingDamageEvent.Post(target,
				new DamageContainer(target.damageSources().playerAttack(player), 8.0F)));
	}

	private static void castSelectedManipulation(ServerPlayer player) {
		BloodManipulation.clearSessionState();
		HemoCapabilityAccess.requireKnownManipulations(player).getSelectedManip()
				.performAction(player, player.serverLevel(), ItemStack.EMPTY, player.blockPosition());
	}

	private static void consumePomes(ServerPlayer player, BlockPos origin) {
		for (int slot = 0; slot < 9; slot++) {
			player.getInventory().selected = slot;
			ItemStack pome = player.getMainHandItem();
			if (!QliphothPomeItem.isBoundPomeFromBloom(pome, origin.asLong())) {
				throw new IllegalStateException("Qliphoth pome is not bound to the journey bloom");
			}
			pome.getItem().finishUsingItem(pome, player.serverLevel(), player);
		}
	}

	private static void completeRankRite(ServerPlayer player, BlockPos origin,
			CardinalRiteActivationRules.Trigger trigger) {
		startRite(player, origin, trigger);
		completeActiveRite(player);
	}

	private static void startRite(ServerPlayer player, BlockPos origin, CardinalRiteActivationRules.Trigger trigger) {
		var result = BloodCraftingKeyPressPacket.tryStartCardinalRite(player, origin.above(), trigger);
		if (result != CardinalRiteActivationRules.ActivationAttempt.STARTED) {
			throw new IllegalStateException("Prepared Cardinal Rite did not start: " + result);
		}
	}

	private static void completeActiveRite(ServerPlayer player) {
		try {
			ActiveCardinalRite rite = CardinalRiteSavedData.get(player.serverLevel()).getRite(player.getUUID());
			if (rite == null) throw new IllegalStateException("No active Cardinal Rite");
			Method complete = HarbingerCardinalRiteEvents.class.getDeclaredMethod("completeRite",
					net.minecraft.server.level.ServerLevel.class, ServerPlayer.class, ActiveCardinalRite.class);
			complete.setAccessible(true);
			if (!(boolean) complete.invoke(null, player.serverLevel(), player, rite)) {
				throw new IllegalStateException("Cardinal Rite completion was rejected");
			}
			CardinalRiteSavedData.get(player.serverLevel()).removeRite(player.getUUID());
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Cardinal Rite completion hook is unavailable", exception);
		}
	}

	private static void useBlock(ServerPlayer player, BlockPos pos) {
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
		if (player.getMainHandItem().isEmpty()) {
			player.serverLevel().getBlockState(pos).useWithoutItem(player.serverLevel(), player, hit);
		} else {
			player.serverLevel().getBlockState(pos).useItemOn(player.getMainHandItem(), player.serverLevel(),
					player, InteractionHand.MAIN_HAND, hit);
		}
	}

	private static <T extends Entity> T entity(ServerPlayer player, BlockPos origin, Class<T> type) {
		return player.serverLevel().getEntitiesOfClass(type, HemoJourneyFixtures.bounds(origin)).getFirst();
	}

	private static <T extends Entity> void dialogue(ServerPlayer player, BlockPos origin, Class<T> type, String event) {
		NeoForge.EVENT_BUS.post(new DialogueEvent(player, event, entity(player, origin, type).getId()));
	}

	private static void artificer(ServerPlayer player, BlockPos origin, String event) {
		dialogue(player, origin, HarbingerArtificerEntity.class, event);
	}

	private static void mason(ServerPlayer player, BlockPos origin, String event) {
		dialogue(player, origin, HarbingerCicatrixAnchoriteEntity.class, event);
	}

	private static void touchDrop(ServerPlayer player, BlockPos origin, Item item) {
		player.serverLevel().getEntitiesOfClass(ItemEntity.class, HemoJourneyFixtures.bounds(origin),
				entity -> entity.getItem().is(item)).getFirst().playerTouch(player);
	}

	private static void selectItem(ServerPlayer player, Item item) {
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			if (player.getInventory().getItem(slot).is(item)) {
				if (slot >= 9) {
					ItemStack selected = player.getInventory().getItem(player.getInventory().selected);
					player.getInventory().setItem(player.getInventory().selected, player.getInventory().getItem(slot));
					player.getInventory().setItem(slot, selected);
				} else player.getInventory().selected = slot;
				return;
			}
		}
		throw new IllegalStateException("Missing supplied item: " + item);
	}

	private static void selectEmptyJar(ServerPlayer player) {
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (stack.is(BlockInit.specimen_jar.get().asItem()) && !SpecimenJarData.hasSpecimen(stack)) {
				selectItem(player, stack.getItem());
				return;
			}
		}
		throw new IllegalStateException("No empty Specimen Jar remains");
	}

	private static HematicArmatureBlockEntity armature(ServerPlayer player, BlockPos origin) {
		return (HematicArmatureBlockEntity) player.serverLevel().getBlockEntity(origin.above());
	}

	private static VialCentrifugeBlockEntity centrifuge(ServerPlayer player, BlockPos origin) {
		return (VialCentrifugeBlockEntity) player.serverLevel().getBlockEntity(origin.above(2));
	}
}
