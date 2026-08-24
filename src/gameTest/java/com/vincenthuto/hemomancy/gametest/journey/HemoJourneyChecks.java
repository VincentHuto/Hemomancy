package com.vincenthuto.hemomancy.gametest.journey;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemory;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment;
import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment;
import com.vincenthuto.hemomancy.common.mission.alchemist.BodyAnswersAssignment;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.D7Lineage;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper;
import com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments;
import com.vincenthuto.hemomancy.common.tile.crafting.VialCentrifugeBlockEntity;
import com.vincenthuto.hemomancy.common.tile.crafting.ScarStationBlockEntity;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.VeinMasonScarLesson;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.util.SpecimenJarData;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hutoslib.common.book.knowledge.CommonDiscoverySource;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.stats.Stats;
import com.vincenthuto.hemomancy.common.init.BlockInit;

/** Reads server-authoritative outcomes and optionally attributes exact output UUIDs. */
public final class HemoJourneyChecks {
	private HemoJourneyChecks() { }

	public static HemoJourneyResult verify(ServerPlayer player, HemoJourneyStage stage, BlockPos origin) {
		return verify(player, stage, origin, false);
	}

	public static HemoJourneyResult verify(ServerPlayer player, HemoJourneyStage stage, BlockPos origin,
			boolean claimOutputs) {
		List<String> unmet = new ArrayList<>();
		switch (stage) {
			case MORTAL_DISPLAY -> verifyMortalDisplay(player, origin, unmet);
			case SANGUINE_INITIATION -> verifyInitiation(player, origin, unmet);
			case FIRST_REMNANT_DISCOVERED -> {
				var entries = HemoCapabilityAccess.requireLiberKnowledge(player).getUnlockedEntries();
				require(unmet, HarbingerAdvancementGranter.hasAdvancement(player,
						HarbingerAdvancementGranter.ADV_HERMIT_ROAD_FIRST_REMNANT),
						"Read the First Remnant blood echo.");
				require(unmet, entries.contains(LiberEntryDefinitions.THE_HARBINGERS)
						&& entries.contains(LiberEntryDefinitions.FIRST_RITE_NOTES),
						"The blood echo did not unlock both expected Liber entries.");
			}
			case VICAR_HERMIT_ROAD_REPORT -> {
				require(unmet, HarbingerAdvancementGranter.hasAdvancement(player,
						HarbingerAdvancementGranter.ADV_HERMIT_ROAD_LEDGER_GRANTED),
						"Speak to the Vicar once to receive the Assignment Ledger.");
				require(unmet, HarbingerAdvancementGranter.hasAdvancement(player,
						HarbingerAdvancementGranter.ADV_HERMIT_ROAD_REPORTED),
						"Report the First Remnant to the Vicar.");
				require(unmet, outputPresent(player, stage, origin, claimOutputs),
						"The Vicar's Assignment Ledger and four Befouling Ash Trail were not received.");
			}
			case VESSEL_FILLED -> {
				require(unmet, HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume() >= 5000.0D,
						"Blood vessel has not reached 5,000 mL.");
				require(unmet, HarbingerAdvancementGranter.isVesselFilled(player),
						"Vessel Filled milestone is incomplete.");
			}
			case FORMATION_PROJECTED -> verifyFormation(player, origin, unmet, claimOutputs);
			case LIBER_CRAFTED -> verifyCraft(player, stage, origin, unmet, claimOutputs,
					HarbingerAdvancementGranter.isLiberSanguinumCrafted(player), "Liber Sanguinum", "Liber Sanguinum");
			case HEMATIC_IRON_CRAFTED -> verifyCraft(player, stage, origin, unmet, claimOutputs,
					HarbingerAdvancementGranter.isHematicIronBlockCrafted(player), "Hematic Iron Block", "Iron in the Blood");
			case LIVING_STAFF_CRAFTED -> {
				require(unmet, outputPresent(player, stage, origin, claimOutputs),
						"No attributable Living Staff was produced after preparation.");
				require(unmet, HemoCapabilityAccess.getLivingStaffProgress(player).orElseThrow().hasLivingStaffBond(),
						"The Living Staff craft did not establish its staff bond.");
			}
			case VICAR_REWARD -> verifyVicarReward(player, origin, unmet, claimOutputs);
			case VOTARY_RITE -> require(unmet, HemoCapabilityAccess.requireInitiatoryDegree(player).getDegreeNumber() == 2
					&& HarbingerAdvancementGranter.hasAdvancement(player, HarbingerAdvancementGranter.ADV_DEGREE_2_VOTARY), "Rite of the Votary is incomplete.");
			case DEGREE_2_REACHED -> require(unmet, HemoCapabilityAccess.requireInitiatoryDegree(player).getDegreeNumber() == 2, "Initiatory degree is not exactly 2.");
			case ALCHEMIST_BRIEFING -> require(unmet, FirstSeparationAssignment.isBriefed(player), "The First Separation briefing was not accepted.");
			case CENTRIFUGE_PREPARED -> require(unmet, player.getStats().getValue(Stats.ITEM_CRAFTED.get(BlockInit.vial_centrifuge.get().asItem())) > 0
					&& centrifuge(player, origin) != null, "Craft and place your own Vial Centrifuge at the fixture center.");
			case SEPARATION_STARTED -> require(unmet, centrifuge(player, origin) != null && centrifuge(player, origin).isSpinning()
					&& HarbingerAdvancementGranter.isFirstSeparationStarted(player), "Vial Centrifuge separation has not started.");
			case ENZYME_RECOVERED -> require(unmet, HarbingerAdvancementGranter.isFirstSeparationComplete(player), "Recover the enzyme from the centrifuge output.");
			case ALCHEMIST_REWARD -> require(unmet, FirstSeparationAssignment.isClaimed(player), "First Separation reward has not been claimed.");
			case BODY_ANSWERS_BRIEFING -> {
				require(unmet, HarbingerAdvancementGranter.hasAdvancement(player, BodyAnswersAssignment.ADV_BRIEFED),
						"The Body Answers briefing was not accepted.");
				require(unmet, player.getRecipeBook().contains(Hemomancy.rloc("distillation/tincture_sanguine_fists")),
						"The Sanguine Fists recipe was not learned from the Alchemist.");
				require(unmet, player.getInventory().countItem(ItemInit.sanguine_formation.get()) >= 1
						&& player.getInventory().countItem(ItemInit.fervent_enzyme.get()) >= 1
						&& player.getInventory().countItem(ItemInit.bloody_flask.get()) >= 1,
						"The Alchemist's three Body Answers ingredients were not received.");
			}
			case BODY_ANSWERS_TINCTURE -> {
				var memory = player.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
				require(unmet, HarbingerAdvancementGranter.hasAdvancement(player, BodyAnswersAssignment.ADV_COMPLETE),
						"The Body Answers assignment is incomplete.");
				require(unmet, memory.knows(MuscleMemory.SANGUINE_FISTS)
						&& memory.reserveTicks(MuscleMemory.SANGUINE_FISTS) > 0,
						"Sanguine Fists was not learned with an active tincture reserve.");
			}
			case RED_TAXONOMY -> {
				require(unmet, HarbingerAdvancementGranter.getRedTaxonomySpecimenCount(player) == 4
						&& HarbingerAdvancementGranter.isRedTaxonomyComplete(player),
						"Submit four distinct Red Taxonomy specimens to the Alchemist.");
				require(unmet, player.getInventory().countItem(BlockInit.specimen_jar.get().asItem()) == 4
						&& player.getInventory().countItem(ItemInit.bloody_vial.get()) == 5,
						"The four specimen jars, first field vial, and four completion vials were not received.");
			}
			case LIVING_BESTIARY_RECORD -> {
				var bestiary = HemoCapabilityAccess.requireSpecimenBestiary(player);
				require(unmet, bestiary.hasRecordedSpecimen(Hemomancy.rloc("crimson_doe"))
						&& !bestiary.hasSurrenderedSpecimen(Hemomancy.rloc("crimson_doe")),
						"Record the captured Crimson Doe with the Alchemist without surrendering it.");
				require(unmet, SpecimenJarData.getSpecimenEntityId(player.getMainHandItem())
						.filter(Hemomancy.rloc("crimson_doe")::equals).isPresent(),
						"Keep the recorded Crimson Doe in the Specimen Jar.");
			}
			case LIVING_BESTIARY_SURRENDER -> {
				var bestiary = HemoCapabilityAccess.requireSpecimenBestiary(player);
				require(unmet, bestiary.hasSurrenderedSpecimen(Hemomancy.rloc("crimson_doe")),
						"Surrender the recorded Crimson Doe to the Alchemist.");
				require(unmet, !SpecimenJarData.hasSpecimen(player.getMainHandItem())
						&& player.getInventory().countItem(ItemInit.enzyme_primer.get()) == 1,
						"The surrendered jar was not emptied or its Enzyme Primer was not received.");
			}
			case HYPHAE_DISCOVERED -> {
				var knowledge = HemoCapabilityAccess.requireLiberKnowledge(player);
				require(unmet, knowledge.hasEntry(LiberEntryDefinitions.HYPHAE),
						"Pick up the supplied Fungal Spine to discover Hyphae.");
				require(unmet, knowledge.getEntrySources().getOrDefault(LiberEntryDefinitions.HYPHAE, java.util.Set.of())
						.contains(CommonDiscoverySource.ITEM_PICKUP),
						"Hyphae was not attributed to the real item-pickup discovery source.");
			}
			case ARTIFICER_WORN_VOW_BRIEFING -> require(unmet,
					HarbingerAdvancementGranter.hasAdvancement(player, ArtificerAssignments.WORN_VOW_BRIEFED),
					"Accept The Worn Vow from the Artificer.");
			case ARTIFICER_ARMATURE_PLACED -> {
				require(unmet, HemoJourneyFixtures.fixtureLevel(player).getBlockState(origin.above())
						.is(BlockInit.hematic_armature.get()), "Place the Hematic Armature at the fixture center.");
				require(unmet, HarbingerAdvancementGranter.isArtificerArmaturePlaced(player),
						"The real Armature placement milestone is incomplete.");
			}
			case ARTIFICER_HEMATIC_UPGRADE -> require(unmet,
					player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET)
							.is(ItemInit.hematic_iron_boots.get())
							&& HarbingerAdvancementGranter.isArtificerFirstHematicUpgrade(player),
					"Let the Hematic Armature upgrade the supplied Iron Boots.");
			case ARTIFICER_WORN_VOW_REWARD -> require(unmet,
					ArtificerAssignments.isArtificerLessonRewardClaimed(player,
							ArtificerAssignments.WORN_VOW_REWARD_CLAIM_KEY)
							&& outputPresent(player, stage, origin, claimOutputs),
					"Claim the four Hematic Iron Scrap from the Artificer.");
			case ARTIFICER_WORN_VOW_FITTING -> require(unmet,
					HarbingerAdvancementGranter.isArtificerHematicIronFitting(player)
							&& outputPresent(player, stage, origin, claimOutputs),
					"Claim The Worn Vow fitting while wearing the complete Hematic Iron set.");
			case ENZYME_MASTERY -> require(unmet,
					HarbingerAdvancementGranter.getEnzymeMasteryCount(player) == 8
							&& HarbingerAdvancementGranter.isEnzymeMasteryComplete(player),
					"The Eightfold Centrifuge has not recorded all eight carried enzymes.");
			case INITIATE_RITE -> verifyRankup(player, 3, HarbingerAdvancementGranter.ADV_DEGREE_3_INITIATE,
					"Rite of the Incarnadine Fane", unmet);
			case FIRST_CULTURE -> require(unmet,
					HarbingerAdvancementGranter.hasAdvancement(player,
							HarbingerAdvancementGranter.ADV_FIRST_CULTURE_COMPLETE),
					"Take a recorded enzyme from the Mycelial Lantern output.");
			case WOVEN_VESSEL_TURN_IN -> {
				boolean outputs = outputPresent(player, stage, origin, claimOutputs);
				require(unmet, HarbingerAdvancementGranter.isMnemonistWovenVesselComplete(player),
						"The Woven Vessel materials have not been accepted by the Mnemonist.");
				require(unmet, outputs, "The Mnemonist's Bleeding Bulb and Vivacious Enzyme were not received.");
			}
			case FIRST_MEMORY_WOVEN -> {
				boolean output = outputPresent(player, stage, origin, claimOutputs);
				require(unmet, HarbingerAdvancementGranter.isMnemonistFirstWeaveComplete(player),
						"The Somatic Loom has not completed its first memory weave.");
				require(unmet, HarbingerAdvancementGranter.isMnemonistWovenVesselFinished(player),
						"The Woven Vessel chapter has not closed after the first weave.");
				require(unmet, output, "No attributable Blood Shot memory was produced by the loom.");
			}
			case NOETIC_MARK_RECOGNIZED -> {
				require(unmet, HarbingerAdvancementGranter.hasAdvancement(player,
						HarbingerAdvancementGranter.ADV_NOETIC_CONDUCTIVE_MARK_RECOGNIZED),
						"Ask the marked Mnemonist to recognize the first Noetic conductive mark.");
				require(unmet, player.getRecipeBook().contains(
						Hemomancy.rloc("memory_weaving/memory_conductive_mark")),
						"The Conductive Mark Loom recipe was not learned.");
			}
			case ARTIFICER_THREE_ANSWERS_BRIEFING -> require(unmet,
					HarbingerAdvancementGranter.hasAdvancement(player, ArtificerAssignments.THREE_ANSWERS_BRIEFED),
					"Accept The Three Answers from the Artificer.");
			case ARTIFICER_FORK_UPGRADE -> require(unmet,
					player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET).is(ItemInit.barbed_boots.get())
							&& HarbingerAdvancementGranter.isArtificerFirstForkUpgrade(player),
					"Step onto the prepared Armature and let it upgrade the Hematic Iron Boots.");
			case ARTIFICER_THREE_ANSWERS_INSPECTION -> require(unmet,
					HarbingerAdvancementGranter.hasAdvancement(player, ArtificerAssignments.THREE_ANSWERS_INSPECTED),
					"Show the first fork upgrade to the Artificer.");
			case ARTIFICER_THREE_ANSWERS_COUNSEL -> require(unmet,
					HarbingerAdvancementGranter.hasAdvancement(player, ArtificerAssignments.THREE_ANSWERS_COUNSELED)
							&& outputPresent(player, stage, origin, claimOutputs),
					"Ask the marked Alchemist for the corresponding Aculeate Vitriol.");
			case ARTIFICER_BARBED_RESEARCH -> {
				var bestiary = HemoCapabilityAccess.requireSpecimenBestiary(player);
				require(unmet, bestiary.hasRecordedSpecimen(Hemomancy.rloc("barbed_urchin"))
						&& bestiary.hasRecordedSpecimen(Hemomancy.rloc("desiccant"))
						&& bestiary.hasRecordedSpecimen(Hemomancy.rloc("venom_rib_centipede")),
						"Capture and ask the Alchemist to record all three Barbed research specimens.");
			}
			case ARTIFICER_BARBED_RESEARCH_REWARD -> require(unmet,
					ArtificerAssignments.isForkResearchRewardClaimed(player)
							&& outputPresent(player, stage, origin, claimOutputs),
					"Claim the completed Barbed research reward from the Alchemist.");
			case ARTIFICER_FORK_DEMONSTRATION -> require(unmet,
					HarbingerAdvancementGranter.hasAdvancement(player, ArtificerAssignments.THREE_ANSWERS_DEMONSTRATED),
					"Let the marked attacker strike the supplied full Barbed set once.");
			case ARTIFICER_FORK_FITTING -> require(unmet,
					HarbingerAdvancementGranter.isArtificerBarbedFitting(player)
							&& outputPresent(player, stage, origin, claimOutputs),
					"Claim the Barbed fitting from the Artificer.");
			case ADEPT_RITE -> verifyRankup(player, 4, HarbingerAdvancementGranter.ADV_DEGREE_4_ADEPT,
					"Rite of the Sanguine Brotherhood", unmet);
			case VEIN_MASON_LESSON -> {
				require(unmet, HarbingerAdvancementGranter.isVeinMasonFirstLesson(player),
						"The Vein-Mason's first lesson was not accepted.");
				require(unmet, outputPresent(player, stage, origin, claimOutputs),
						"The Vein-Mason's complete first scar kit was not received.");
			}
			case FIRST_SCAR_CARVED -> {
				ScarStationBlockEntity station = scarStation(player, origin);
				require(unmet, HarbingerAdvancementGranter.isVeinMasonFirstScarCarved(player),
						"The first scar has not been carved through the station.");
				require(unmet, station != null && station.getItem(2).is(VeinMasonScarLesson.forPlayer(player).scar().get()),
						"The station did not produce the lesson's carved scar.");
			}
			case FIRST_SCAR_LEARNED -> {
				var scarId = VeinMasonScarLesson.forPlayer(player).patternScarId();
				require(unmet, HarbingerAdvancementGranter.isVeinMasonFirstScarLearned(player),
						"The carved scar has not burned into memory.");
				require(unmet, HemoCapabilityAccess.requireScarState(player).knowsCerebralScar(scarId),
						"The lesson's cerebral scar is not known.");
			}
			case FIRST_EFFIGY_PATTERN -> {
				var scarId = VeinMasonScarLesson.forPlayer(player).patternScarId();
				require(unmet, HarbingerAdvancementGranter.isVeinMasonFirstEffigyPattern(player),
						"The Mason's Effigy has not completed its first pattern.");
				require(unmet, outputPresent(player, stage, origin, claimOutputs)
						&& hasPreparedPattern(player, origin, scarId),
						"No attributable Effigy pattern contains the learned scar.");
			}
			case FIRST_EFFIGY_LOADOUT -> {
				var scarId = VeinMasonScarLesson.forPlayer(player).patternScarId();
				require(unmet, HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(player)
						&& HarbingerAdvancementGranter.isVeinMasonContinuationReady(player),
						"The first Effigy loadout has not been committed.");
				require(unmet, HemoCapabilityAccess.requireScarState(player).getActiveCerebralScars().contains(scarId),
						"The lesson's scar is not active in the committed loadout.");
			}
			case VEIN_MASON_REWARD -> {
				require(unmet, HarbingerAdvancementGranter.isVeinMasonRewardClaimed(player),
						"The Vein-Mason continuation reward has not been claimed.");
				require(unmet, outputPresent(player, stage, origin, claimOutputs),
						"The Vein-Mason's complete continuation kit was not received.");
			}
			case ILLUMINATUS_RITE -> verifyRankup(player, 5, HarbingerAdvancementGranter.ADV_DEGREE_5_ILLUMINATUS,
					"Rite of the Crimson Lodge", unmet);
			case VEIN_MASON_D5_STRAIN -> require(unmet,
					VeinMasonAssignments.has(player, VeinMasonAssignments.D5_VARICOSE),
					"Hit the marked target once with Sanguine Fists enabled to strain the arm vessels into Varicose flow.");
			case VEIN_MASON_D5_DIAGNOSIS -> require(unmet,
					VeinMasonAssignments.has(player, VeinMasonAssignments.D5_DIAGNOSED),
					"Speak to the Vein-Mason and accept the vascular diagnosis.");
			case VEIN_MASON_D5_TREATMENT -> require(unmet,
					VeinMasonAssignments.has(player, VeinMasonAssignments.D5_TREATED),
					"Use the supplied Vascular Poultice.");
			case VEIN_MASON_D5_FORTIFICATION -> require(unmet,
					HemoCapabilityAccess.requireInitiatoryDegree(player).hasHematicFortification()
							&& VeinMasonAssignments.has(player, VeinMasonAssignments.D5_READY),
					"Complete the prepared Rite of Hematic Fortification.");
			case VEIN_MASON_D5_REWARD -> require(unmet,
					VeinMasonAssignments.has(player, VeinMasonAssignments.D5_REWARD)
							&& outputPresent(player, stage, origin, claimOutputs),
					"Claim the degree-five continuation kit from the Vein-Mason.");
			case ARTIFICER_ASSUMED_LIMB_BRIEFING -> require(unmet,
					ArtificerAssignments.has(player, ArtificerAssignments.ASSUMED_LIMB_BRIEFED),
					"Accept The Assumed Limb from the Artificer.");
			case ARTIFICER_FIRST_LIVING_GRAFT -> require(unmet,
					HarbingerAdvancementGranter.isArtificerFirstLivingGraft(player)
							&& ArtificerAssignments.knownLivingWeaponFormCount(player) >= 1,
					"Receive the Blade Graft from the lit brazier with the Living Staff.");
			case ARTIFICER_ASSUMED_LIMB_REWARD -> require(unmet,
					ArtificerAssignments.isArtificerLessonRewardClaimed(player,
							ArtificerAssignments.ASSUMED_LIMB_REWARD_CLAIM_KEY)
							&& outputPresent(player, stage, origin, claimOutputs),
					"Claim the Hematic Memory from the Artificer.");
			case ARTIFICER_LIVING_ARSENAL_DEMONSTRATION -> require(unmet,
					ArtificerAssignments.has(player, ArtificerAssignments.ASSUMED_LIMB_DEMONSTRATED),
					"Kill the marked target with the supplied Living Blade.");
			case ARTIFICER_FULL_LIVING_ARSENAL -> require(unmet,
					ArtificerAssignments.knowsFullLivingArsenal(player),
					"Receive all six remaining Living Weapon grafts from their lit braziers.");
			case ARTIFICER_LIVING_ARSENAL_FITTING -> require(unmet,
					HarbingerAdvancementGranter.isArtificerLivingArsenalFitting(player)
							&& outputPresent(player, stage, origin, claimOutputs),
					"Claim The Assumed Limb fitting from the Artificer.");
			case ARTIFICER_CRIMSON_VESTMENT_BRIEFING -> require(unmet,
					ArtificerAssignments.has(player, ArtificerAssignments.CRIMSON_VESTMENT_BRIEFED),
					"The Crimson Vestment briefing was not accepted.");
			case VICAR_CONSECRATION_KIT -> require(unmet,
					outputPresent(player, stage, origin, claimOutputs),
					"The Vicar's Consecration Kit was not received.");
			case ARTIFICER_FRAME_CONSECRATED -> require(unmet,
					HarbingerAdvancementGranter.isArtificerFrameConsecrated(player),
					"Apply the Vicar's Consecration Kit to the Hematic Armature.");
			case ARTIFICER_CRIMSON_VESTMENT_INSPECTION -> require(unmet,
					ArtificerAssignments.has(player, ArtificerAssignments.CRIMSON_VESTMENT_INSPECTED),
					"The Artificer has not inspected the consecrated frame.");
			case ARTIFICER_CRIMSON_VESTMENT_COUNSEL -> require(unmet,
					ArtificerAssignments.has(player, ArtificerAssignments.CRIMSON_VESTMENT_COUNSELED)
							&& outputPresent(player, stage, origin, claimOutputs),
					"The Alchemist's Crimson Lacquer was not received.");
			case ARTIFICER_BLOOD_LUST_UPGRADE -> require(unmet,
					HarbingerAdvancementGranter.isArtificerFirstBloodLustUpgrade(player)
							&& player.getItemBySlot(EquipmentSlot.FEET).is(ItemInit.blood_lust_boots.get()),
					"The consecrated Armature has not produced Blood Lust Boots.");
			case ARTIFICER_BLOOD_LUST_DEMONSTRATION -> require(unmet,
					ArtificerAssignments.has(player, ArtificerAssignments.CRIMSON_VESTMENT_DEMONSTRATED),
					"Hit the marked target once while wearing the full Blood Lust set.");
			case ARTIFICER_BLOOD_LUST_FITTING -> require(unmet,
					HarbingerAdvancementGranter.isArtificerBloodLustFitting(player)
							&& outputPresent(player, stage, origin, claimOutputs),
					"The Crimson Vestment fitting was not received.");
			case FOUNDING_FANE -> {
				require(unmet, HarbingerAdvancementGranter.hasAdvancement(player,
						HarbingerAdvancementGranter.ADV_FOUNDING_FANE_ESTABLISHED),
						"The Founding Fane has not been established.");
				require(unmet, HarbingerAdvancementGranter.hasAdvancement(player,
						HarbingerAdvancementGranter.ADV_COVENANT_WRITTEN_IN_PLACE),
						"The covenant-written-in-place chapter proof is incomplete.");
				require(unmet, origin.above(4).equals(FoundingFaneSavedData.get(player.serverLevel())
						.getHeart(player.getUUID())), "The journey Founding Fane heart was not persisted.");
			}
			case SANCTIFIED_RITE -> verifyRankup(player, 6, HarbingerAdvancementGranter.ADV_DEGREE_6_SANCTIFIED,
					"Rite of the Bloodline Covenant", unmet);
			case VEIN_MASON_D6_REFERRAL -> require(unmet,
					VeinMasonAssignments.has(player, VeinMasonAssignments.D6_REFERRAL),
					"Accept the degree-six referral from the Vein-Mason.");
			case VEIN_MASON_D6_COUNSEL -> require(unmet,
					VeinMasonAssignments.has(player, VeinMasonAssignments.D6_COUNSEL),
					"Receive the Mnemonist's scar-routing counsel.");
			case VEIN_MASON_D6_FIRST_ROUTE -> require(unmet,
					VeinMasonAssignments.has(player, VeinMasonAssignments.D6_FIRST_ROUTE),
					"Cast the selected manipulation through its matching active cerebral scar.");
			case VEIN_MASON_D6_SCAR_CARVED -> {
				var lesson = VeinMasonScarLesson.strongestForPlayer(player, 2);
				ScarStationBlockEntity station = scarStation(player, origin);
				require(unmet, station != null && station.getItem(2).is(lesson.scar().get()),
						"Carve the supplied degree-five scar at the prepared station.");
			}
			case VEIN_MASON_D6_SCAR_LEARNED -> require(unmet,
					HemoCapabilityAccess.requireScarState(player).knowsCerebralScar(
							VeinMasonScarLesson.strongestForPlayer(player, 2).patternScarId()),
					"Burn the newly carved scar into memory at the lit brazier.");
			case VEIN_MASON_D6_LOADOUT -> require(unmet,
					VeinMasonAssignments.has(player, VeinMasonAssignments.D6_LOADOUT)
							&& HemoCapabilityAccess.requireScarState(player).getActiveCerebralScars().contains(
									VeinMasonScarLesson.strongestForPlayer(player, 2).patternScarId()),
					"Commit the supplied pattern as the active cerebral scar loadout.");
			case VEIN_MASON_D6_SECOND_ROUTE -> require(unmet,
					VeinMasonAssignments.has(player, VeinMasonAssignments.D6_SECOND_ROUTE)
							&& VeinMasonAssignments.has(player, VeinMasonAssignments.D6_READY),
					"Cast once more through the newly committed matching scar.");
			case VEIN_MASON_D6_REWARD -> require(unmet,
					VeinMasonAssignments.has(player, VeinMasonAssignments.D6_REWARD)
							&& outputPresent(player, stage, origin, claimOutputs),
					"Claim the degree-six routing reward from the Vein-Mason.");
			case CHAMBER_RETURNED -> require(unmet, HarbingerAdvancementGranter.hasAdvancement(player,
					HarbingerAdvancementGranter.ADV_CHAMBER_RETURNED),
					"The rite-attuned Chamber visit has not returned through its real exit path.");
			case COVENANT_THRONE_BOUND -> require(unmet, HarbingerAdvancementGranter.hasAdvancement(player,
					HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND),
					"The Covenant Throne has not bound the Progenitor's return.");
			case COVENANT_VIGIL -> {
				require(unmet, HarbingerAdvancementGranter.hasAdvancement(player,
						HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED),
						"The Covenant Vigil has not completed.");
				require(unmet, HarbingerAdvancementGranter.hasAdvancement(player,
						HarbingerAdvancementGranter.ADV_LIVING_COVENANT_COMPLETE),
						"The Living Covenant chapter has not closed.");
			}
			case ARCHON_RITE -> verifyRankup(player, 7, HarbingerAdvancementGranter.ADV_DEGREE_7_ARCHON,
					"Rite of the Hematic Order", unmet);
			case ARTIFICER_WEIGHT_OF_FRAME_BRIEFING -> require(unmet,
					ArtificerAssignments.has(player, ArtificerAssignments.WEIGHT_OF_FRAME_BRIEFED),
					"Accept Weight of the Frame from the Artificer.");
			case ARTIFICER_MONOLITHIC_FRAME -> require(unmet,
					HarbingerAdvancementGranter.isArtificerMonolithicFrame(player),
					"Apply the Monolithic Cornerstone to the prepared Armature.");
			case ARTIFICER_D7_UPGRADE -> require(unmet,
					HarbingerAdvancementGranter.isArtificerFirstD7Upgrade(player)
							&& ArtificerAssignments.firstD7Lineage(player) == D7Lineage.EDACIOUS
							&& player.getItemBySlot(EquipmentSlot.FEET).is(ItemInit.edacious_blood_lust_boots.get()),
					"Wait for the prepared Armature to produce Edacious Blood Lust Boots.");
			case ARTIFICER_WEIGHT_OF_FRAME_INSPECTION -> require(unmet,
					ArtificerAssignments.has(player, ArtificerAssignments.WEIGHT_OF_FRAME_INSPECTED)
							&& player.getPersistentData().getBoolean(ArtificerAssignments.D7_REWARD_CLAIM_KEY)
							&& outputPresent(player, stage, origin, claimOutputs),
					"Show the first Edacious piece to the Artificer and receive its lineage material.");
			case ARTIFICER_D7_DEMONSTRATION -> require(unmet,
					ArtificerAssignments.has(player, ArtificerAssignments.WEIGHT_OF_FRAME_DEMONSTRATED)
							&& ArtificerAssignments.hasFullD7Lineage(player, D7Lineage.EDACIOUS),
					"Activate Bloodburst while wearing the complete supplied Edacious set.");
			case ARTIFICER_D7_FITTING -> require(unmet,
					HarbingerAdvancementGranter.isArtificerD7Fitting(player)
							&& outputPresent(player, stage, origin, claimOutputs),
					"Claim the Monolithic Frame fitting from the Artificer.");
			case QLIPHOTH_COMMUNION -> {
				var degree = HemoCapabilityAccess.requireInitiatoryDegree(player);
				require(unmet, degree.getTotalPomesConsumed() == 9 && degree.isQliphothCommunionDone(),
						"Consume all nine supplied Qliphoth pomes from the same bloom.");
			}
			case APOTHEOS_CHOICE -> {
				var degree = HemoCapabilityAccess.requireInitiatoryDegree(player);
				require(unmet, degree.getArchonPath() == EnumArchonPath.APOTHEOS_PENDING
						&& FungalGardenTravelHelper.ARCHON_CHOICE_APOTHEOS.equals(player.getPersistentData()
								.getString(FungalGardenTravelHelper.ARCHON_CHOICE_KEY))
						&& !player.getPersistentData().getBoolean(FungalGardenTravelHelper.REVELATION_CHOICE_PENDING),
						"Choose to pursue the Eighth Degree in the fungal revelation.");
			}
			case APOTHEOS_RITE -> {
				verifyRankup(player, 8, HarbingerAdvancementGranter.ADV_DEGREE_8_APOTHEOS,
						"Rite of Apotheos", unmet);
				require(unmet, HemoCapabilityAccess.requireInitiatoryDegree(player).getArchonPath()
						== EnumArchonPath.APOTHEOS, "The Apotheos path did not finalize.");
			}
			case COMPLETE -> { return new HemoJourneyResult(true, stage, "Journey checkpoints complete; ready to restore the snapshot."); }
		}
		return unmet.isEmpty() ? new HemoJourneyResult(true, stage, "All checkpoint conditions passed.")
				: HemoJourneyResult.fail(stage, String.join("\n", unmet));
	}

	private static void verifyMortalDisplay(ServerPlayer player, BlockPos origin, List<String> unmet) {
		require(unmet, HemoCapabilityAccess.requireBloodVolume(player).isActive(), "Blood magic is not active.");
		require(unmet, HarbingerAdvancementGranter.hasAdvancement(player,
				Hemomancy.rloc("hemomancy/the_first_awakening")), "The First Awakening advancement is incomplete.");
		require(unmet, HemoJourneyFixtures.fixtureLevel(player).getBlockState(origin.above())
				.is(com.vincenthuto.hemomancy.common.init.BlockInit.placed_blood_stained_stone.get()),
				"The Mortal Display has not transformed.");
		boolean equipped = HemoCapabilityAccess.requireEquipment(player).getStackInSlot(5)
				.is(ItemInit.charm_of_vascularium.get());
		require(unmet, equipped || fixtureHasItem(player, origin, ItemInit.charm_of_vascularium.get()),
				"Charm of Vascularium is neither equipped nor dropped in the fixture.");
	}

	private static void verifyInitiation(ServerPlayer player, BlockPos origin, List<String> unmet) {
		require(unmet, HemoCapabilityAccess.requireInitiatoryDegree(player).getDegreeNumber() == 1,
				"Initiatory degree is not exactly 1.");
		require(unmet, HarbingerAdvancementGranter.hasAdvancement(player,
				HarbingerAdvancementGranter.ADV_DEGREE_1_NEOPHYTE), "Degree-1 milestone is incomplete.");
		require(unmet, hasItem(player, origin, ItemInit.sanguine_conduit.get()),
				"Sanguine Conduit reward was not found on the player or in the fixture.");
	}

	private static void verifyFormation(ServerPlayer player, BlockPos origin, List<String> unmet,
			boolean claimOutputs) {
		double start = HemoJourneyFixtures.baselineBlood(player);
		double current = HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume();
		boolean output = outputPresent(player, HemoJourneyStage.FORMATION_PROJECTED, origin, claimOutputs);
		require(unmet, HemoJourneyCheckpointRules.formationPassed(start, current, output),
				"The attuned projection did not spend at least 100 mL and produce its formation.");
		require(unmet, output,
				"No attributable Sanguine Formation output was produced after preparation.");
	}

	private static void verifyCraft(ServerPlayer player, HemoJourneyStage stage, BlockPos origin,
			List<String> unmet, boolean claimOutputs, boolean advancementComplete, String outputLabel,
			String advancementLabel) {
		boolean output = outputPresent(player, stage, origin, claimOutputs);
		require(unmet, output, "No attributable " + outputLabel + " output was produced after preparation.");
		require(unmet, HemoJourneyCheckpointRules.craftPassed(output,
				HemoJourneyFixtures.baselineAdvancementIncomplete(player), advancementComplete),
				advancementLabel + " milestone is incomplete; pick up the crafted " + outputLabel + ".");
	}

	private static void verifyVicarReward(ServerPlayer player, BlockPos origin, List<String> unmet,
			boolean claimOutputs) {
		require(unmet, HarbingerAdvancementGranter.isVesselFilled(player), "Vessel Filled milestone no longer holds.");
		require(unmet, HarbingerAdvancementGranter.isLiberSanguinumCrafted(player), "Fane Sanguinium milestone no longer holds.");
		require(unmet, HarbingerAdvancementGranter.isHematicIronBlockCrafted(player), "Iron in the Blood milestone no longer holds.");
		boolean outputs = outputPresent(player, HemoJourneyStage.VICAR_REWARD, origin, claimOutputs);
		require(unmet, HemoJourneyCheckpointRules.rewardPassed(outputs,
				HemoJourneyFixtures.baselineAdvancementIncomplete(player), FirstBloodcraftAssignment.isClaimed(player)),
				"First Bloodcraft reward has not been newly claimed with its exact kit.");
	}

	private static void verifyRankup(ServerPlayer player, int degree,
			net.minecraft.resources.ResourceLocation advancement, String riteName, List<String> unmet) {
		require(unmet, HemoCapabilityAccess.requireInitiatoryDegree(player).getDegreeNumber() == degree,
				riteName + " has not raised the Initiatory Degree to " + degree + ".");
		require(unmet, HarbingerAdvancementGranter.hasAdvancement(player, advancement),
				riteName + " advancement is incomplete.");
	}

	private static boolean outputPresent(ServerPlayer player, HemoJourneyStage stage, BlockPos origin,
			boolean claimOutputs) {
		return claimOutputs ? HemoJourneyFixtures.captureExpectedOutputs(player, stage, origin)
				: HemoJourneyFixtures.expectedOutputsPresent(player, stage, origin);
	}

	private static boolean hasItem(ServerPlayer player, BlockPos origin, Item item) {
		return inventoryCount(player, item) > 0 || fixtureHasItem(player, origin, item);
	}

	private static boolean fixtureHasItem(ServerPlayer player, BlockPos origin, Item item) {
		return !HemoJourneyFixtures.fixtureLevel(player).getEntitiesOfClass(ItemEntity.class, HemoJourneyFixtures.bounds(origin),
				entity -> entity.getItem().is(item)).isEmpty();
	}

	private static boolean hasPreparedPattern(ServerPlayer player, BlockPos origin,
			net.minecraft.resources.ResourceLocation scarId) {
		for (ItemStack stack : player.getInventory().items) {
			if (ItemScarPattern.hasPreparedLoadout(stack) && ItemScarPattern.getScarIds(stack).contains(scarId)) return true;
		}
		return HemoJourneyFixtures.fixtureLevel(player).getEntitiesOfClass(ItemEntity.class,
				HemoJourneyFixtures.bounds(origin), entity -> ItemScarPattern.hasPreparedLoadout(entity.getItem())
						&& ItemScarPattern.getScarIds(entity.getItem()).contains(scarId)).size() == 1;
	}

	private static int inventoryCount(ServerPlayer player, Item item) {
		int count = 0;
		for (ItemStack stack : player.getInventory().items) if (stack.is(item)) count += stack.getCount();
		for (ItemStack stack : player.getInventory().offhand) if (stack.is(item)) count += stack.getCount();
		for (ItemStack stack : player.getInventory().armor) if (stack.is(item)) count += stack.getCount();
		return count;
	}

	private static void require(List<String> unmet, boolean condition, String message) {
		if (!condition) unmet.add(message);
	}

	private static VialCentrifugeBlockEntity centrifuge(ServerPlayer player, BlockPos origin) {
		return HemoJourneyFixtures.fixtureLevel(player).getBlockEntity(origin.above(2)) instanceof VialCentrifugeBlockEntity value ? value : null;
	}

	private static ScarStationBlockEntity scarStation(ServerPlayer player, BlockPos origin) {
		return HemoJourneyFixtures.fixtureLevel(player).getBlockEntity(origin.above()) instanceof ScarStationBlockEntity value
				? value : null;
	}
}
