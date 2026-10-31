package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;
import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypLayer;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Characterisation of every dialogue-event route before the dialogue handler is split per NPC (step 11, item 2).
 *
 * <p>Every event id the handler routes is posted on the event bus three ways: by a Degree 0 player to the NPC that
 * speaks it, by a Degree 7 player carrying common supplies to that NPC, and by the same Degree 7 player with no
 * speaker at all. Each outcome is fingerprinted: whether a reward was delivered, what the player was told, what
 * changed in the player's saved data (inventory, persistent data and attachments), what dropped and which Hemomancy
 * advancements were earned. The fingerprints were recorded from the handler before the split and must not change.
 *
 * <p>A missing or different fingerprint file writes the actual one to {@code dialogue-route-fingerprints.actual.txt}
 * in the game directory and fails; review it and copy it to {@code src/gameTest/resources/dialogue/}.
 */
@GameTestHolder("dialogue_route_validation")
@PrefixGameTestTemplate(false)
public final class DialogueRouteCharacterisationGameTests {
	private static final String GOLDEN = "/dialogue/route-fingerprints.txt";

	private DialogueRouteCharacterisationGameTests() {
	}

	private enum Speaker {
		NONE(null), WHISPER(null), MONOLITH(null),
		VICAR(EntityInit.harbinger_vicar::get), ALCHEMIST(EntityInit.harbinger_alchemist::get),
		ARTIFICER(EntityInit.harbinger_artificer::get), MNEMONIST(EntityInit.harbinger_mnemonist::get),
		ANCHORITE(EntityInit.harbinger_cicatrix_anchorite::get), HERMIT(EntityInit.harbinger_hermit::get),
		VOYAGER(EntityInit.harbinger_voyager::get), ZEALOT(EntityInit.unstained_zealot::get),
		GUARDIAN(EntityInit.unstained_guardian::get), ACOLYTE(EntityInit.unstained_acolyte::get),
		SCOUT(EntityInit.unstained_scout::get), RINGMASTER(EntityInit.circus_ringmaster::get),
		PERFORMER(EntityInit.circus_acrobat::get), ANNETTA(EntityInit.annetta_knowles::get),
		GARDENER(() -> EntityInit.court_members.get(com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole.GARDENER).get()),
		LIGHTKEEPER(() -> EntityInit.court_members.get(com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole.LIGHTKEEPER).get()),
		TIDEWARDEN(() -> EntityInit.court_members.get(com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole.TIDEWARDEN).get()),
		BRACED_ELDER(() -> EntityInit.court_members.get(com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole.BRACED_ELDER).get()),
		EDGE_SWIMMER(() -> EntityInit.court_members.get(com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole.EDGE_SWIMMER).get()),
		NURSERY_TENDER(() -> EntityInit.court_members.get(com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole.NURSERY_TENDER).get()),
		LINESMAN(() -> EntityInit.court_members.get(com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole.LINESMAN).get()),
		LAMP_COLONY(() -> EntityInit.court_members.get(com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole.LAMP_COLONY).get()),
		HOST(() -> EntityInit.court_members.get(com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole.HOST).get()),
		VEILED_RESIDENT(() -> EntityInit.court_members.get(com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole.VEILED_RESIDENT).get()),
		BELLWARDEN(() -> EntityInit.court_members.get(com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole.BELLWARDEN).get()),
		SLEEPER(() -> EntityInit.court_members.get(com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole.SLEEPER).get()),
		MARROW_TENDER(() -> EntityInit.court_members.get(com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole.MARROW_TENDER).get()),
		ROOTED_ELDER(() -> EntityInit.court_members.get(com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole.ROOTED_ELDER).get()),
		OFFERING_BEARER(() -> EntityInit.court_members.get(com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole.OFFERING_BEARER).get()),
		HEAD_TENDER(() -> EntityInit.court_members.get(com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole.HEAD_TENDER).get());

		private final Supplier<? extends EntityType<?>> type;

		Speaker(Supplier<? extends EntityType<?>> type) {
			this.type = type;
		}
	}

	private record Route(String eventId, Speaker speaker) {
	}

	@GameTest(template = "empty", timeoutTicks = 200, batch = "dialogue_routes")
	public static void everyDialogueRouteKeepsItsOutcome(GameTestHelper helper) {
		List<String> lines = new ArrayList<>();
		for (Route route : routes()) {
			lines.add(fingerprint(helper, route, route.speaker(), false));
			lines.add(fingerprint(helper, route, route.speaker(), true));
			lines.add(fingerprint(helper, route, Speaker.NONE, true));
		}
		PlayerFingerprints.assertGolden(helper, GOLDEN, "dialogue-route-fingerprints.actual.txt",
				String.join("\n", lines) + "\n");
		helper.succeed();
	}

	private static List<Route> routes() {
		List<Route> routes = new ArrayList<>();
		// Routes that match before the per-id switch.
		routes.add(new Route(StationUpgradeDialogue.claimEvent(StationUpgradeCatalog.get(
				com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation.ALEMBIC, 1)), Speaker.ALCHEMIST));
		routes.add(new Route(StationUpgradeDialogue.claimEvent(StationUpgradeCatalog.get(
				com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation.SCRIPTORIUM, 1)), Speaker.MNEMONIST));
		routes.add(new Route(StationUpgradeDialogue.CLAIM_PREFIX + "nonsense", Speaker.ALCHEMIST));
		for (String antecedent : List.of("antecedent_recognition", "antecedent_housing", "antecedent_replay",
				"antecedent_research", "antecedent_vessel", "antecedent_effect")) {
			routes.add(new Route(antecedent, Speaker.ALCHEMIST));
			routes.add(new Route(antecedent, Speaker.VICAR));
		}
		for (ClinicalBloodProgress.Lesson lesson : ClinicalBloodProgress.Lesson.values()) {
			routes.add(new Route(lesson.event(), Speaker.ALCHEMIST));
		}
		routes.add(new Route("clinical_lesson_unknown", Speaker.ALCHEMIST));
		routes.add(new Route(AnnettaDialogueTrees.EVENT_UNSEAL_JAR, Speaker.ANNETTA));
		routes.add(new Route(AnnettaDialogueTrees.EVENT_OFFER_DRAUGHT, Speaker.ANNETTA));
		routes.add(new Route(AnnettaDialogueTrees.EVENT_OFFER_DRAUGHT, Speaker.VICAR));
		routes.add(new Route("circus_school_teach", Speaker.PERFORMER));
		routes.add(new Route("circus_school_inspect", Speaker.PERFORMER));
		routes.add(new Route("circus_reject_pact", Speaker.RINGMASTER));
		routes.add(new Route("circus_reject_pact", Speaker.VICAR));
		for (MnemonistStarterMemoryChoice choice : MnemonistStarterMemoryChoice.values()) {
			routes.add(new Route(choice.eventId(), Speaker.MNEMONIST));
		}
		for (HarbingerAlchemistDialogueTrees.RedTaxonomySample sample : HarbingerAlchemistDialogueTrees.RedTaxonomySample.values()) {
			routes.add(new Route(sample.eventId(), Speaker.ALCHEMIST));
		}
		routes.add(new Route("alchemist_red_taxonomy_unknown", Speaker.ALCHEMIST));
		routes.add(new Route(HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_RECORD, Speaker.ALCHEMIST));
		routes.add(new Route(HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_SURRENDER, Speaker.ALCHEMIST));
		for (MorphlingPolypLayer layer : MorphlingPolypLayer.values()) {
			routes.add(new Route(HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_SURRENDER_MORPHLING_PREFIX
					+ layer.serializedName(), Speaker.ALCHEMIST));
		}
		routes.add(new Route(HarbingerAlchemistDialogueTrees.EVENT_CLAIM_ARMOR_RESEARCH_REWARD, Speaker.ALCHEMIST));
		routes.add(new Route(HarbingerAlchemistDialogueTrees.EVENT_CLAIM_ARMOR_RESEARCH_REWARD, Speaker.ARTIFICER));
		for (ArtificerProgressionRules.ForkFamily family : ArtificerProgressionRules.ForkFamily.values()) {
			routes.add(new Route(HarbingerArtificerDialogueTrees.EVENT_RECOVER_FORK_PREFIX + family.serializedName(),
					Speaker.ARTIFICER));
		}
		for (ArtificerProgressionRules.D7Lineage lineage : ArtificerProgressionRules.D7Lineage.values()) {
			routes.add(new Route(HarbingerArtificerDialogueTrees.EVENT_RECOVER_D7_PREFIX + lineage.serializedName(),
					Speaker.ARTIFICER));
		}
		routes.add(new Route(HarbingerArtificerDialogueTrees.EVENT_BRIEF_WORN_VOW, Speaker.VICAR));
		routes.add(new Route("artificer_unknown_choice", Speaker.ARTIFICER));

		// The per-id switch, each with the NPC that speaks it.
		add(routes, Speaker.MNEMONIST, VagrantMindInquiryDialogue.MEMORY_REPORT, VagrantMindInquiryDialogue.BEARING,
				HarbingerMnemonistDialogueTrees.EVENT_GUIDED_CHAMBER, HarbingerMnemonistDialogueTrees.EVENT_CIRCUS_WAYBILL,
				HarbingerMnemonistDialogueTrees.EVENT_RELIQUARY_TAUGHT, HarbingerMnemonistDialogueTrees.EVENT_ANCHORITE_COUNSEL,
				HarbingerMnemonistDialogueTrees.EVENT_WOVEN_VESSEL_TURN_IN, HarbingerMnemonistDialogueTrees.EVENT_BLANK_MEMORY_RECIPE,
				HarbingerMnemonistDialogueTrees.EVENT_SCRIPTORIUM_LESSON, "recruit_harbinger", "expel_harbinger");
		add(routes, Speaker.ALCHEMIST, VagrantMindInquiryDialogue.BIOLOGY_REPORT, DeepDarkCommissionDialogue.REPORT,
				DeepDarkCommissionDialogue.BEARING, PhlegethonticCommissionDialogue.REPORT,
				PhlegethonticCommissionDialogue.BEARING, MorphlingHandlingDialogue.INSPECT,
				OverworldFungalSurveyDialogue.REPORT, FirstDrawsDialogue.ACCEPT, SpecimenJarLessonDialogue.TEACH,
				"alchemist_replace_concentrated_blood", HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_BRIEF,
				HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_CLAIM,
				HarbingerAlchemistDialogueTrees.EVENT_BODY_ANSWERS_BRIEF,
				HarbingerArtificerDialogueTrees.EVENT_CLAIM_THREE_ANSWERS_REWARD,
				HarbingerArtificerDialogueTrees.EVENT_CLAIM_CRIMSON_VESTMENT_REWARD);
		add(routes, Speaker.VOYAGER, VoyagerIntroductionDialogue.OBSERVE, CourtDiscoveryDialogue.VOYAGER_LEARN,
				CourtDiscoveryDialogue.VOYAGER_LETTER, CourtDiscoveryDialogue.VOYAGER_CHART);
		add(routes, Speaker.ALCHEMIST, CourtDiscoveryDialogue.ALCHEMIST_LEAD, CourtDiscoveryDialogue.ALCHEMIST_LETTER);
		add(routes, Speaker.VICAR, CourtDiscoveryDialogue.VICAR_LETTER);
		add(routes, Speaker.VICAR, VoyagerIntroductionDialogue.REPORT, VoyagerIntroductionDialogue.BEARING,
				"vicar_begin_initiation", "vicar_release_charm", HarbingerVicarDialogueTrees.EVENT_BLOOD_SHOTTING,
				HarbingerVicarDialogueTrees.EVENT_HERMIT_ROAD_REPORT,
				HarbingerVicarDialogueTrees.EVENT_CLAIM_FIRST_BLOODCRAFT_REWARD,
				HarbingerVicarDialogueTrees.EVENT_LIBER_BLUEPRINT, HarbingerVicarDialogueTrees.EVENT_IRON_BLUEPRINT,
				HarbingerVicarDialogueTrees.EVENT_MASONS_RESPITE_DIRECTIVE, HarbingerVicarDialogueTrees.EVENT_CONSECRATION_KIT,
				"give_sanguine_monolith_blueprint", "give_stained_church_map", "recruit_harbinger", "expel_harbinger");
		add(routes, Speaker.ANCHORITE, HarbingerCicatrixAnchoriteDialogueTrees.EVENT_DIAGNOSIS,
				HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D5_REWARD,
				HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REFERRAL,
				HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REWARD,
				HarbingerCicatrixAnchoriteDialogueTrees.EVENT_REPLACE_D4,
				HarbingerCicatrixAnchoriteDialogueTrees.EVENT_REPLACE_D5,
				HarbingerCicatrixAnchoriteDialogueTrees.EVENT_REPLACE_D6,
				HarbingerCicatrixAnchoriteDialogueTrees.EVENT_FIRST_LESSON,
				HarbingerCicatrixAnchoriteDialogueTrees.EVENT_CONTINUATION_REWARD);
		add(routes, Speaker.ACOLYTE, "acolyte_task_gather_ghost_pipe", "acolyte_task_wreath", "acolyte_task_hemolytic",
				"acolyte_task_consecrate", "acolyte_task_chalice");
		add(routes, Speaker.ZEALOT, "zealot_task_still_waters", "zealot_task_pallid_icon", "zealot_begin_novitiate",
				"novitiate_gather_remedies", "novitiate_gentle_separation", "novitiate_stillwater_labor",
				"novitiate_clean_labor", "novitiate_shelter_afflicted", "zealot_accept_church", "zealot_reject_help",
				"zealot_accept_purification", "give_stained_church_map");
		add(routes, Speaker.GUARDIAN, "guardian_task_plating", "guardian_task_bell", "guardian_replace_absolution_dagger");
		add(routes, Speaker.HERMIT, "hermit_accept_guidance", "hermit_heart_offered", "hermit_farewell_die",
				"hermit_archon_wisdom");
		add(routes, Speaker.ARTIFICER, HarbingerArtificerDialogueTrees.EVENT_CONSECRATION_KIT,
				HarbingerArtificerDialogueTrees.EVENT_BRIEF_WORN_VOW, HarbingerArtificerDialogueTrees.EVENT_BRIEF_THREE_ANSWERS,
				HarbingerArtificerDialogueTrees.EVENT_BRIEF_CRIMSON_VESTMENT,
				HarbingerArtificerDialogueTrees.EVENT_BRIEF_ASSUMED_LIMB,
				HarbingerArtificerDialogueTrees.EVENT_BRIEF_WEIGHT_OF_FRAME,
				HarbingerArtificerDialogueTrees.EVENT_INSPECT_THREE_ANSWERS,
				HarbingerArtificerDialogueTrees.EVENT_INSPECT_CRIMSON_VESTMENT,
				HarbingerArtificerDialogueTrees.EVENT_INSPECT_WEIGHT_OF_FRAME,
				HarbingerArtificerDialogueTrees.EVENT_CLAIM_D7_REWARD, HarbingerArtificerDialogueTrees.EVENT_CLAIM_WORN_VOW_REWARD,
				HarbingerArtificerDialogueTrees.EVENT_CLAIM_THREE_ANSWERS_REWARD,
				HarbingerArtificerDialogueTrees.EVENT_CLAIM_ASSUMED_LIMB_REWARD,
				HarbingerArtificerDialogueTrees.EVENT_CLAIM_HEMATIC_IRON_FITTING,
				HarbingerArtificerDialogueTrees.EVENT_CLAIM_FORK_FITTING,
				HarbingerArtificerDialogueTrees.EVENT_CLAIM_BLOOD_LUST_FITTING,
				HarbingerArtificerDialogueTrees.EVENT_CLAIM_D7_FITTING,
				HarbingerArtificerDialogueTrees.EVENT_CLAIM_LIVING_ARSENAL_FITTING);
		add(routes, Speaker.MONOLITH, SanguineMonolithDialogueTrees.EVENT_SHATTER, SanguineMonolithDialogueTrees.EVENT_CORNERSTONE);
		add(routes, Speaker.WHISPER, "qliphoth_communion_done", "archon_choice_silence", "archon_choice_eighth_degree",
				"whisper_dismiss", "whisper_truth_acknowledged");
		add(routes, Speaker.SCOUT, "scout_give_notes");
		// The Brined Court's members (Track 9): each event with a role that offers it, and the Lightkeeper's lesson
		// spoken by a role that doesn't.
		add(routes, Speaker.GARDENER, CourtMemberDialogue.FOOD, CourtMemberDialogue.REST, CourtMemberDialogue.GIFT,
				CourtMemberDialogue.OFFER, CourtMemberDialogue.AMENDS, CourtMemberDialogue.LESSON);
		add(routes, Speaker.LIGHTKEEPER, CourtMemberDialogue.LESSON, CourtMemberDialogue.JOIN);
		add(routes, Speaker.TIDEWARDEN, CourtMemberDialogue.REST, CourtMemberDialogue.OFFER);
		add(routes, Speaker.BRACED_ELDER, CourtMemberDialogue.GIFT, CourtMemberDialogue.JOIN);
		add(routes, Speaker.EDGE_SWIMMER, CourtMemberDialogue.REST);
		add(routes, Speaker.NURSERY_TENDER, CourtMemberDialogue.GIFT, CourtMemberDialogue.OFFER, CourtMemberDialogue.JOIN);
		add(routes, Speaker.LINESMAN, CourtMemberDialogue.OFFER);
		add(routes, Speaker.LAMP_COLONY, CourtMemberDialogue.GIFT, CourtMemberDialogue.JOIN);
		add(routes, Speaker.HOST, CourtMemberDialogue.REST, CourtMemberDialogue.OFFER);
		add(routes, Speaker.VEILED_RESIDENT, CourtMemberDialogue.GIFT, CourtMemberDialogue.JOIN);
		add(routes, Speaker.BELLWARDEN, CourtMemberDialogue.REST, CourtMemberDialogue.GIFT, CourtMemberDialogue.OFFER,
				CourtMemberDialogue.JOIN);
		add(routes, Speaker.SLEEPER, CourtMemberDialogue.GIFT);
		add(routes, Speaker.MARROW_TENDER, CourtMemberDialogue.FOOD, CourtMemberDialogue.REST, CourtMemberDialogue.OFFER);
		add(routes, Speaker.ROOTED_ELDER, CourtMemberDialogue.GIFT, CourtMemberDialogue.JOIN);
		add(routes, Speaker.OFFERING_BEARER, CourtMemberDialogue.OFFER);
		add(routes, Speaker.HEAD_TENDER, CourtMemberDialogue.GIFT, CourtMemberDialogue.JOIN);
		routes.add(new Route("no_route_takes_this_event", Speaker.VICAR));
		return routes;
	}

	private static void add(List<Route> routes, Speaker speaker, String... eventIds) {
		for (String eventId : eventIds) routes.add(new Route(eventId, speaker));
	}

	private static String fingerprint(GameTestHelper helper, Route route, Speaker speaker, boolean adept) {
		ServerLevel level = helper.getLevel();
		BlockPos stand = helper.absolutePos(new BlockPos(8, 2, 8));
		try (CapturedPlayer captured = CapturedPlayer.create(helper).online()) {
			ServerPlayer player = captured.player;
			player.setPos(stand.getX() + 0.5D, stand.getY(), stand.getZ() + 0.5D);
			prepare(player, adept);
			Entity speakerEntity = spawn(level, speaker, stand);
			int entityId = switch (speaker) {
				case NONE -> Integer.MAX_VALUE;
				case WHISPER -> 0;
				case MONOLITH -> SanguineMonolithDialogueTrees.BLOCK_ENTITY_ID;
				default -> speakerEntity.getId();
			};
			CompoundTag before = PlayerFingerprints.saved(player);
			Set<String> earnedBefore = PlayerFingerprints.earned(player);
			DialogueEvent event = new DialogueEvent(player, route.eventId(), entityId);
			NeoForge.EVENT_BUS.post(event);
			CompoundTag after = PlayerFingerprints.saved(player);
			Set<String> earned = PlayerFingerprints.earned(player);
			earned.removeAll(earnedBefore);

			StringBuilder line = new StringBuilder();
			line.append(route.eventId()).append(" @").append(route.speaker()).append(speaker == route.speaker() ? "" : "->NONE")
					.append(adept ? " [D7]" : " [D0]")
					.append(" delivered=").append(event.wasRewardDelivered())
					.append(" said=").append(captured.chat)
					.append(" saved=").append(PlayerFingerprints.diff(before, after))
					.append(" drops=").append(PlayerFingerprints.drops(level, stand))
					.append(" earned=").append(earned);
			String text = line.toString();
			text = PlayerFingerprints.normalise(text, player, speakerEntity);
			if (speakerEntity != null) speakerEntity.discard();
			return text;
		}
	}

	private static void prepare(ServerPlayer player, boolean adept) {
		var blood = HemoCapabilityAccess.getBloodVolume(player);
		HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(adept ? 7 : 0);
		blood.setActive(adept);
		blood.setMaxBloodVolume(5000.0D);
		blood.setBloodVolume(adept ? 5000.0D : 0.0D);
		if (!adept) return;
		List<Item> supplies = List.of(Items.BOOK, Items.INK_SAC, Items.PAPER, Items.IRON_INGOT, Items.GLASS_BOTTLE,
				ItemInit.bloody_vial.get(), ItemInit.bleeding_bulb.get(), ItemInit.vivacious_enzyme.get(),
				ItemInit.hematic_memory.get(), ItemInit.living_staff.get(), ItemInit.hematic_iron_knapper.get(),
				ItemInit.runic_motif_paper.get());
		for (Item item : supplies) player.getInventory().add(new ItemStack(item, item.getDefaultMaxStackSize()));
	}

	private static Entity spawn(ServerLevel level, Speaker speaker, BlockPos stand) {
		if (speaker.type == null) return null;
		Entity entity = speaker.type.get().create(level);
		if (entity == null) throw new IllegalStateException("Could not create " + speaker);
		entity.setPos(stand.getX() + 1.5D, stand.getY(), stand.getZ() + 0.5D);
		level.addFreshEntity(entity);
		return entity;
	}


}
