package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtChampionRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtLessonRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtLife;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtMembershipRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtProgress;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtHospitality;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole.Offer;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrain;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * What a Court member says, by role and by who the player is to the Court: a stranger, referred, known, a member of
 * the speaker's house, a transgressor at the Garden, or Unstained and shunned. Every role's lines are keyed
 * {@code hemomancy.court.<role>.<node>.lineN} and written to its sheet in {@code docs/NPC_VOICES.md}. The hub offers
 * only what the role gives ({@link CourtRole#offers()}) and what the player may have now; before the fifth Degree
 * members feed and talk but don't teach or take anyone in. The chosen gift, rest, food, offering, lesson or joining is
 * a {@code court_} event the route hands to {@link CourtHospitality}, which checks it again.
 */
public final class CourtMemberDialogue {
	public static final String EVENT_PREFIX = "court_member_";
	public static final String FOOD = EVENT_PREFIX + "food";
	public static final String REST = EVENT_PREFIX + "rest";
	public static final String GIFT = EVENT_PREFIX + "gift";
	public static final String OFFER = EVENT_PREFIX + "offer";
	public static final String AMENDS = EVENT_PREFIX + "amends";
	public static final String LESSON = EVENT_PREFIX + "lesson";
	public static final String JOIN = EVENT_PREFIX + "join";
	public static final List<String> EVENTS = List.of(FOOD, REST, GIFT, OFFER, AMENDS, LESSON, JOIN);
	/**
	 * The Head-tender lets a worthy challenger try to usurp the Kindled Champion (Track 10). It isn't hospitality, so
	 * it is not among {@link #EVENTS}: the route hands it to {@code KindledChampionTrial}, which checks the gate again.
	 */
	public static final String CHALLENGE = EVENT_PREFIX + "challenge";

	private static final String OPTION = "hemomancy.dialogue.court.option.";
	private static final DialogueOption LEAVE = new DialogueOption(OPTION + "leave", null, null);
	private static final DialogueOption BACK = new DialogueOption(OPTION + "back", "root", null);

	/** Who the player is to the speaker, most telling first. */
	public enum Standing {
		SHUNNED, TRANSGRESSOR, MEMBER, KNOWN, REFERRED, STRANGER;

		public String key() {
			return name().toLowerCase(java.util.Locale.ROOT);
		}
	}

	/** Everything a member's tree depends on, read once from the player; holdsOffering is the house's own custom in hand. */
	public record View(Standing standing, boolean teachable, boolean hungry, boolean giftTaken, boolean holdsOffering,
			boolean lessonTaught, CourtMembershipRules.Outcome join, StrainMatch strain) {
		public static View of(ServerPlayer player, CourtHouse house) {
			CourtProgress progress = CourtLife.progress(player);
			boolean teachable = ProgressionGates.hasDegree(player, CourtLessonRules.MINIMUM_DEGREE);
			// The same rule tryJoin applies, previewed so the lead answers before the player asks.
			CourtMembershipRules.Outcome join = CourtMembershipRules.join(progress.house(), progress.switchUsed(), house,
					ProgressionGates.hasDegree(player, CourtMembershipRules.MINIMUM_DEGREE), progress.standing(house),
					CourtLife.isShunned(player), CourtLife.turnsAway(player, house));
			return new View(CourtMemberDialogue.standing(player, house, progress), teachable, player.getFoodData().needsFood(),
					progress.actCounted(house, CourtHospitality.GIFT_ACT), CourtHospitality.holdsCustom(player, house),
					progress.lessonTaught(), join, StrainMatch.of(player, house));
		}
	}

	/** Whether the player wears this house's strain, another house's, or none. */
	public enum StrainMatch {
		NONE, OWN, OTHER;

		static StrainMatch of(ServerPlayer player, CourtHouse house) {
			var worn = HemoCapabilityAccess.getEquippedMorphling(player).getEquippedMorphling();
			return MorphlingStrains.of(worn).map(MorphlingStrain::house).map(h -> h == house ? OWN : OTHER).orElse(NONE);
		}
	}

	private CourtMemberDialogue() {
	}

	public static Standing standing(ServerPlayer player, CourtHouse house, CourtProgress progress) {
		if (CourtLife.isShunned(player)) return Standing.SHUNNED;
		if (CourtLife.turnsAway(player, house)) return Standing.TRANSGRESSOR;
		if (CourtLife.isMember(player, house)) return Standing.MEMBER;
		if (!progress.known()) return Standing.STRANGER;
		CourtProgress.Route route = progress.knownBy();
		return route == null || route == CourtProgress.Route.FOUND ? Standing.KNOWN : Standing.REFERRED;
	}

	public static DialogueTree forMember(CourtMemberEntity member, ServerPlayer player) {
		CourtChampionRules.Gate challenge = member.role() == CourtRole.HEAD_TENDER ? CourtLife.championGate(player) : null;
		return build(member.role(), member.getId(), View.of(player, member.role().house()), challenge);
	}

	/** The role's tree for one view of the player; pure, so the route GameTests can build every state. */
	public static DialogueTree build(CourtRole role, int entityId, View view) {
		return build(role, entityId, view, null);
	}

	/**
	 * The tree with the Head-tender's answer to a challenge for the Champion's title: {@code challenge} is the gate as it
	 * stands, or null for a speaker who has no say over the title.
	 */
	public static DialogueTree build(CourtRole role, int entityId, View view, @Nullable CourtChampionRules.Gate challenge) {
		String speaker = "entity.hemomancy." + role.entityId();
		ResourceLocation icon = Hemomancy.rloc("textures/entity/npc/court/" + role.id() + ".png");
		DialogueTree.Builder tree = DialogueTree.builder(speaker, icon, entityId);
		String base = "hemomancy.court." + role.id() + ".";
		if (view.standing() == Standing.SHUNNED) {
			return tree.addNode(new DialogueNode("root", List.of(base + "shunned.line1", base + "shunned.line2"), List.of(LEAVE)))
					.build();
		}
		List<String> greeting = new ArrayList<>(List.of(base + "greet." + view.standing().key()));
		if (view.strain() != StrainMatch.NONE) greeting.add(base + "strain." + view.strain().name().toLowerCase(java.util.Locale.ROOT));
		boolean welcome = view.standing() != Standing.TRANSGRESSOR;
		List<DialogueOption> options = new ArrayList<>();
		options.add(new DialogueOption(OPTION + "house", "house", null));
		tree.addNode(reply("house", base + "house.line1", base + "house.line2"));
		if (role.offers(Offer.FOOD) && welcome && view.hungry()) {
			options.add(new DialogueOption(OPTION + "food", "fed", FOOD));
			tree.addNode(reply("fed", base + "food.line1"));
		}
		if (role.offers(Offer.REST) && welcome) {
			options.add(new DialogueOption(OPTION + "rest", "rested", REST));
			tree.addNode(reply("rested", base + "rest.line1"));
		}
		if (role.offers(Offer.GIFT) && welcome && !view.giftTaken()) {
			options.add(new DialogueOption(OPTION + "gift", "gifted", GIFT));
			tree.addNode(reply("gifted", base + "gift.line1"));
		}
		if (view.standing() == Standing.TRANSGRESSOR && view.holdsOffering()
				&& (role.offers(Offer.FOOD) || role.offers(Offer.LESSON))) {
			options.add(new DialogueOption(OPTION + "amends", "amends", AMENDS));
			tree.addNode(reply("amends", base + "amends.line1"));
		}
		if (role.offers(Offer.CUSTOM) && welcome && view.holdsOffering()) {
			String offer = CourtHospitality.customOption(role.house());
			options.add(new DialogueOption(OPTION + offer, "offered", OFFER));
			tree.addNode(reply("offered", base + "custom.line1"));
		}
		if (role.offers(Offer.LESSON) && welcome) {
			if (view.lessonTaught()) {
				options.add(new DialogueOption(OPTION + "lesson", "lesson_known", null));
				tree.addNode(reply("lesson_known", base + "lesson.known"));
			} else if (view.teachable()) {
				options.add(new DialogueOption(OPTION + "lesson", "lesson", LESSON));
				tree.addNode(reply("lesson", base + "lesson.line1", base + "lesson.line2"));
			} else {
				options.add(new DialogueOption(OPTION + "lesson", "lesson_early", null));
				tree.addNode(reply("lesson_early", base + "lesson.early"));
			}
		}
		if (role.offers(Offer.JOIN) && welcome && view.join() != CourtMembershipRules.Outcome.ALREADY_MEMBER) {
			if (view.join().changed()) {
				options.add(new DialogueOption(OPTION + "join", "join", JOIN));
				tree.addNode(reply("join", base + "join.line1"));
			} else {
				String refusal = switch (view.join()) {
					case REFUSED_DEGREE -> "early";
					case REFUSED_SWITCH_USED -> "settled";
					default -> "standing";
				};
				options.add(new DialogueOption(OPTION + "join", "join_" + refusal, null));
				tree.addNode(reply("join_" + refusal, base + "join." + refusal));
			}
		}
		if (challenge != null && welcome && challenge != CourtChampionRules.Gate.REFUSED_CHAMPION) {
			if (challenge.open()) {
				options.add(new DialogueOption(OPTION + "challenge", "challenge", CHALLENGE));
				tree.addNode(reply("challenge", base + "challenge.line1", base + "challenge.line2"));
			} else {
				String refusal = challenge.name().toLowerCase(java.util.Locale.ROOT);
				options.add(new DialogueOption(OPTION + "challenge", "challenge_" + refusal, null));
				tree.addNode(reply("challenge_" + refusal, base + "challenge." + refusal));
			}
		}
		if (role.offers(Offer.DIRECTIONS) && welcome) {
			options.add(new DialogueOption(OPTION + "directions", "directions", null));
			tree.addNode(reply("directions", base + "directions.line1", base + "directions.line2"));
		}
		options.add(new DialogueOption(OPTION + "ask_about_item", "item_hint", null));
		tree.addNode(new DialogueNode("item_hint", List.of(base + "item_hint"), List.of(BACK, LEAVE)));
		options.add(LEAVE);
		return tree.addNode(new DialogueNode("root", greeting, options)).startNode("root").build();
	}

	private static DialogueNode reply(String id, String... lines) {
		return new DialogueNode(id, List.of(lines), List.of(BACK, LEAVE));
	}
}
