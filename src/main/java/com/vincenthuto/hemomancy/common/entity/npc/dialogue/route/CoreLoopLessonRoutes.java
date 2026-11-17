package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.HemomancyDiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CoreLoopLessons;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueEvent;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.function.BiConsumer;

/**
 * The core-loop lessons' events (round two P10), shared by the six outpost speakers' routes. A lesson's event opens its
 * Liber pages: no proof, advancement or blood. It is heard from its own speaker, alive and within eight blocks, by a
 * player whose blood is awake, who is not Unstained, at a Degree the lesson is offered at. A speaker's route may hand
 * over something on a telling through {@code heard}; only the Alchemist's Temper lesson does (the Titration Bandolier,
 * CQ-63).
 */
final class CoreLoopLessonRoutes {
	private static final double REACH_SQR = 64.0D;

	private CoreLoopLessonRoutes() {
	}

	/** True when the event is one of {@code speaker}'s lessons, whether or not this telling opened anything. */
	static boolean teach(DialogueEvent event, String speaker, Class<? extends Entity> type) {
		return teach(event, speaker, type, (player, lesson) -> {
		});
	}

	/** As {@link #teach(DialogueEvent, String, Class)}; {@code heard} runs after each telling the player could hear. */
	static boolean teach(DialogueEvent event, String speaker, Class<? extends Entity> type,
			BiConsumer<ServerPlayer, CoreLoopLessons.Lesson> heard) {
		CoreLoopLessons.Lesson lesson = CoreLoopLessons.byEvent(event.getEventId());
		if (lesson == null || !lesson.speaker().equals(speaker)) return false;
		ServerPlayer player = event.getPlayer();
		Entity teacher = player.level().getEntity(event.getEntityId());
		if (type.isInstance(teacher) && teacher.isAlive() && player.isAlive() && player.distanceToSqr(teacher) <= REACH_SQR
				&& ProgressionGates.isBloodActive(player) && !ProgressionGates.isUnstained(player)
				&& lesson.offeredAt(HemoCapabilityAccess.getPlayerDegreeNumber(player))) {
			event.setRewardDelivered(LiberKnowledgeHelper.unlockEntries(player, lesson.pages(),
					HemomancyDiscoverySource.DIALOGUE));
			heard.accept(player, lesson);
		}
		return true;
	}
}
