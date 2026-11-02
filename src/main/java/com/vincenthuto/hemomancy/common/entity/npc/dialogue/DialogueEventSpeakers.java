package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerCicatrixAnchoriteEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerMnemonistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.entity.npc.unstained.UnstainedZealotEntity;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * The progression events only their own speaker may grant. A chosen option names its speaker by entity id, and the
 * server never checks that the event was in the tree it sent, so an event from this table sent to another NPC, or
 * with no speaker at all (entity id 0), is dropped before it is posted. Whisper events are not listed: they keep
 * entity id 0.
 */
public final class DialogueEventSpeakers {
	private static final List<Class<? extends Entity>> VICAR = List.of(HarbingerVicarEntity.class);
	private static final List<Class<? extends Entity>> ALCHEMIST = List.of(HarbingerAlchemistEntity.class);
	private static final List<Class<? extends Entity>> MNEMONIST = List.of(HarbingerMnemonistEntity.class);
	private static final List<Class<? extends Entity>> ANCHORITE = List.of(HarbingerCicatrixAnchoriteEntity.class);
	/** The NPCs a bloodline can recruit (HEMOMANCY_REFERENCE §12). */
	public static final List<Class<? extends Entity>> RECRUITABLE = List.of(HarbingerVicarEntity.class,
			HarbingerAlchemistEntity.class, HarbingerMnemonistEntity.class);

	private static final Map<String, List<Class<? extends Entity>>> EVENTS = Map.ofEntries(
			Map.entry(HarbingerVicarDialogueTrees.EVENT_HERMIT_ROAD_REPORT, VICAR),
			Map.entry(HarbingerVicarDialogueTrees.EVENT_MASONS_RESPITE_DIRECTIVE, VICAR),
			Map.entry("give_sanguine_monolith_blueprint", VICAR),
			// The Zealot hands the same map to the Unstained.
			Map.entry("give_stained_church_map", List.of(HarbingerVicarEntity.class, UnstainedZealotEntity.class)),
			Map.entry(HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_BRIEF, ALCHEMIST),
			Map.entry(HarbingerAlchemistDialogueTrees.EVENT_BODY_ANSWERS_BRIEF, ALCHEMIST),
			Map.entry(HarbingerMnemonistDialogueTrees.EVENT_RELIQUARY_TAUGHT, MNEMONIST),
			Map.entry(HarbingerMnemonistDialogueTrees.EVENT_ANCHORITE_COUNSEL, MNEMONIST),
			Map.entry(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_DIAGNOSIS, ANCHORITE),
			Map.entry(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D5_REWARD, ANCHORITE),
			Map.entry(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REFERRAL, ANCHORITE),
			Map.entry(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REWARD, ANCHORITE),
			Map.entry("recruit_harbinger", RECRUITABLE),
			Map.entry("expel_harbinger", RECRUITABLE));

	/** Event families named by prefix: the red taxonomy samples, the bestiary and the scar-pattern replacements. */
	private static final Map<String, List<Class<? extends Entity>>> PREFIXES = Map.of(
			HarbingerAlchemistDialogueTrees.EVENT_RED_TAXONOMY_PREFIX, ALCHEMIST,
			"alchemist_bestiary_", ALCHEMIST,
			"vein_mason_replace_", ANCHORITE);

	private DialogueEventSpeakers() {
	}

	/** Whether {@code speaker} may grant {@code eventId}; true for an event this table does not list. */
	public static boolean allows(String eventId, @Nullable Entity speaker) {
		List<Class<? extends Entity>> speakers = speakersOf(eventId);
		if (speakers == null) return true;
		if (speaker == null) return false;
		for (Class<? extends Entity> type : speakers) if (type.isInstance(speaker)) return true;
		return false;
	}

	@Nullable
	private static List<Class<? extends Entity>> speakersOf(String eventId) {
		List<Class<? extends Entity>> speakers = EVENTS.get(eventId);
		if (speakers != null) return speakers;
		for (Map.Entry<String, List<Class<? extends Entity>>> prefix : PREFIXES.entrySet()) {
			if (eventId.startsWith(prefix.getKey())) return prefix.getValue();
		}
		return null;
	}
}
