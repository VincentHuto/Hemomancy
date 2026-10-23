package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;
import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypLayer;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules;
import com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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
	/** Volatile entity keys that say nothing about a dialogue's outcome. */
	private static final Set<String> IGNORED = Set.of("Pos", "Motion", "Rotation", "FallDistance", "OnGround", "Air",
			"Fire", "PortalCooldown", "HurtTime", "HurtByTimestamp", "DeathTime", "AbsorptionAmount", "FallFlying",
			"recipeBook", "seenCredits", "enteredNetherPosition", "warden_spawn_tracker", "current_explosion_impact_pos",
			"ignore_fall_damage_from_current_explosion", "current_impulse_context_reset_grace_time");

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
		PERFORMER(EntityInit.circus_acrobat::get), ANNETTA(EntityInit.annetta_knowles::get);

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
		String actual = String.join("\n", lines) + "\n";
		String golden = golden();
		if (!actual.equals(golden)) {
			try {
				Files.writeString(Path.of("dialogue-route-fingerprints.actual.txt"), actual, StandardCharsets.UTF_8);
			} catch (IOException exception) {
				throw new IllegalStateException(exception);
			}
			helper.fail(golden == null ? "No dialogue fingerprints recorded yet; wrote dialogue-route-fingerprints.actual.txt"
					: "Dialogue routes changed; first difference: " + firstDifference(golden, actual));
		}
		helper.succeed();
	}

	private static List<Route> routes() {
		List<Route> routes = new ArrayList<>();
		// Routes that match before the per-id switch.
		routes.add(new Route(StationUpgradeDialogue.claimEvent(StationUpgradeCatalog.get(
				com.vincenthuto.hemomancy.common.station.UpgradeStation.ALEMBIC, 1)), Speaker.ALCHEMIST));
		routes.add(new Route(StationUpgradeDialogue.claimEvent(StationUpgradeCatalog.get(
				com.vincenthuto.hemomancy.common.station.UpgradeStation.SCRIPTORIUM, 1)), Speaker.MNEMONIST));
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
		add(routes, Speaker.VOYAGER, VoyagerIntroductionDialogue.OBSERVE);
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
			CompoundTag before = player.saveWithoutId(new CompoundTag());
			Set<String> earnedBefore = earned(player);
			DialogueEvent event = new DialogueEvent(player, route.eventId(), entityId);
			NeoForge.EVENT_BUS.post(event);
			CompoundTag after = player.saveWithoutId(new CompoundTag());
			Set<String> earned = earned(player);
			earned.removeAll(earnedBefore);

			StringBuilder line = new StringBuilder();
			line.append(route.eventId()).append(" @").append(route.speaker()).append(speaker == route.speaker() ? "" : "->NONE")
					.append(adept ? " [D7]" : " [D0]")
					.append(" delivered=").append(event.wasRewardDelivered())
					.append(" said=").append(captured.chat)
					.append(" saved=").append(diff(before, after))
					.append(" drops=").append(drops(level, stand))
					.append(" earned=").append(earned);
			String text = line.toString();
			text = normalise(text, player, speakerEntity);
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

	private static Set<String> earned(ServerPlayer player) {
		Set<String> earned = new TreeSet<>();
		for (AdvancementHolder advancement : player.server.getAdvancements().getAllAdvancements()) {
			if (!advancement.id().getNamespace().equals("hemomancy")) continue;
			if (player.getAdvancements().getOrStartProgress(advancement).isDone()) earned.add(advancement.id().getPath());
		}
		return earned;
	}

	/** What changed in the player's saved data, two levels deep, in key order. */
	private static String diff(CompoundTag before, CompoundTag after) {
		TreeMap<String, String> changes = new TreeMap<>();
		Set<String> keys = new TreeSet<>(before.getAllKeys());
		keys.addAll(after.getAllKeys());
		for (String key : keys) {
			if (IGNORED.contains(key)) continue;
			Tag old = before.get(key);
			Tag now = after.get(key);
			if (java.util.Objects.equals(old, now)) continue;
			if (old instanceof CompoundTag oldCompound && now instanceof CompoundTag nowCompound) {
				Set<String> inner = new TreeSet<>(oldCompound.getAllKeys());
				inner.addAll(nowCompound.getAllKeys());
				for (String innerKey : inner) {
					Tag a = oldCompound.get(innerKey);
					Tag b = nowCompound.get(innerKey);
					if (!java.util.Objects.equals(a, b)) changes.put(key + "." + innerKey, String.valueOf(b));
				}
			} else {
				changes.put(key, String.valueOf(now));
			}
		}
		return changes.toString();
	}

	private static String drops(ServerLevel level, BlockPos stand) {
		TreeMap<String, Integer> drops = new TreeMap<>();
		for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(stand).inflate(8.0D))) {
			drops.merge(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString(),
					item.getItem().getCount(), Integer::sum);
			item.discard();
		}
		return drops.toString();
	}

	/** Takes out what differs from run to run: the generated player and speaker identities and names. */
	private static String normalise(String text, ServerPlayer player, Entity speaker) {
		text = replaceIdentity(text, player.getUUID(), "<player>");
		text = text.replace(player.getGameProfile().getName(), "<player-name>");
		if (speaker != null) {
			text = replaceIdentity(text, speaker.getUUID(), "<speaker>");
			text = text.replace(speaker.getName().getString(), "<speaker-name>");
		}
		// Fresh attachments roll random identities, and a bearing depends on where the test room was placed.
		text = ANY_UUID_ARRAY.matcher(text).replaceAll("<uuid>");
		text = ANY_UUID.matcher(text).replaceAll("<uuid>");
		return BEARING.matcher(text).replaceAll("<n> blocks <direction>");
	}

	private static final java.util.regex.Pattern ANY_UUID_ARRAY = java.util.regex.Pattern.compile(
			"\\[I;-?\\d+,-?\\d+,-?\\d+,-?\\d+]");
	private static final java.util.regex.Pattern ANY_UUID = java.util.regex.Pattern.compile(
			"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
	private static final java.util.regex.Pattern BEARING = java.util.regex.Pattern.compile(
			"\\d+ blocks(?: (?:northeast|northwest|southeast|southwest|north|south|east|west))?");

	private static String replaceIdentity(String text, java.util.UUID uuid, String placeholder) {
		text = text.replace(uuid.toString(), placeholder);
		return text.replace(new IntArrayTag(UUIDUtil.uuidToIntArray(uuid)).toString(), placeholder);
	}

	private static String golden() {
		try (InputStream stream = DialogueRouteCharacterisationGameTests.class.getResourceAsStream(GOLDEN)) {
			return stream == null ? null : new String(stream.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
		} catch (IOException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private static String firstDifference(String golden, String actual) {
		String[] expected = golden.split("\n");
		String[] got = actual.split("\n");
		for (int i = 0; i < Math.max(expected.length, got.length); i++) {
			String e = i < expected.length ? expected[i] : "<none>";
			String g = i < got.length ? got[i] : "<none>";
			if (!e.equals(g)) return "line " + (i + 1) + "\n expected " + e + "\n      got " + g;
		}
		return "none";
	}
}
