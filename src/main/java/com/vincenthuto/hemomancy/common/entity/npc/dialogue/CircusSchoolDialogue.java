package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusApprenticeshipProgress;
import com.vincenthuto.hemomancy.common.mission.circus.CircusCurriculum;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusPlayerProgress;
import com.vincenthuto.hemomancy.common.mission.circus.CircusPracticalController;
import com.vincenthuto.hemomancy.common.mission.circus.CircusSchoolQuests;
import com.vincenthuto.hemomancy.common.mission.circus.CircusSchoolRules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.ArrayList;
import java.util.List;

public final class CircusSchoolDialogue {
    private CircusSchoolDialogue() {}
    public static DialogueTree tree(CircusPerformerEntity teacher, ServerPlayer player, boolean firstVisit) {
        String role = teacher.facultyRoleId();
        String root = "hemomancy.circus.school.";
        var options = new ArrayList<DialogueOption>();
        options.add(new DialogueOption(root + "ask_history", "history", null).withLore());
        options.add(new DialogueOption(root + "ask_colleagues", "colleagues", null).withLore());
        options.add(new DialogueOption(root + "ask_preparation", "preparation", null));
        options.add(new DialogueOption(root + "ask_aftermath", "aftermath", null));
        options.add(new DialogueOption(root + "testimony", "testimony", "circus_school_testimony").withQuest());
        CircusCurriculum.forTeacher(role).ifPresent(lesson -> {
            options.add(new DialogueOption(root + "take_lesson", "lesson", "circus_school_teach").withQuest());
            options.add(new DialogueOption(root + "practical", null, "circus_school_practical").withQuest());
            options.add(new DialogueOption(root + "guest", null, "circus_school_guest").withQuest());
        });
        if (role.equals("circus_threadkeeper")) {
            options.add(new DialogueOption(root + "inspect", null, "circus_school_inspect").withQuest());
            options.add(new DialogueOption(root + "report", null, "circus_school_report").withQuest());
            options.add(new DialogueOption(root + "claim", null, "circus_school_claim").withQuest());
        }
        options.add(new DialogueOption("hemomancy.dialogue.circus_performer.leave", null, null));
        var builder = DialogueTree.builder(teacher.getType().getDescriptionId(),
                Hemomancy.rloc("textures/entity/npc/harbinger/circus/" + (teacher instanceof com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFacultyEntity ? role : role.replace("circus_", "")) + "_0.png"), teacher.getId());
        var greeting = new ArrayList<String>();
        if (firstVisit) greeting.add("hemomancy.dialogue.circus_performer.welcome");
        greeting.add(root + role + ".welcome");
        greeting.add(root + "status." + CircusSchoolQuests.nextStep(player, role));
        builder.addNode(new DialogueNode("greeting", greeting, options));
        for (String topic : List.of("history", "colleagues", "preparation", "aftermath", "testimony", "lesson")) {
            String key = topic.equals("preparation") ? root + "preparation" : root + role + "." + topic;
            builder.addNode(new DialogueNode(topic, topic.equals("aftermath") ? List.of(key, root + "aftermath." + CircusPlayerProgress.route(player).serializedName()) : List.of(key),
                    List.of(new DialogueOption(root + "back", "greeting", null))));
        }
        return builder.build();
    }

    /**
     * The school's hub for {@code player}. The hub reads a claim card as a Turn In and claim, inspect and report cards
     * as Urgent from their ids alone; the Threadkeeper's cards say so only when something can be handed in.
     */
    public static DialogueTree hub(CircusPerformerEntity teacher, ServerPlayer player, boolean firstVisit) {
        DialogueTree hub = DialogueHubFactory.decorate(tree(teacher, player, firstVisit), teacher.facultyRoleId(), player);
        if (!teacher.facultyRoleId().equals("circus_threadkeeper")) return hub;
        var state = CircusApprenticeshipProgress.state(player);
        boolean owed = state.getInt("guest.pending") > 0;
        boolean reportable = CircusSchoolQuests.reportReady(player);
        boolean uninspected = !state.hasUUID("primer.crossbar");
        List<DialogueTopic> topics = new ArrayList<>();
        for (DialogueTopic topic : hub.presentation().topics()) {
            String event = cardEvent(hub, topic);
            if ("circus_school_claim".equals(event)) {
                topic = owed ? topic.withStatus(DialogueTopicState.TURN_IN, DialogueAttention.URGENT)
                        : topic.withStatus(DialogueTopicState.ACTIVE, DialogueAttention.NONE);
            } else if ("circus_school_report".equals(event)) {
                topic = topic.withStatus(topic.state(), reportable ? DialogueAttention.URGENT : DialogueAttention.NONE);
            } else if ("circus_school_inspect".equals(event)) {
                topic = topic.withStatus(topic.state(), uninspected ? DialogueAttention.NOTICE : DialogueAttention.NONE);
            }
            topics.add(topic);
        }
        var presentation = hub.presentation();
        return new DialogueTree(hub.speakerName(), hub.speakerIcon(), hub.startNodeId(), hub.nodes(), hub.entityId(),
                hub.theme(), new DialoguePresentation(presentation.dialogueId(), presentation.styleId(),
                        presentation.mode(), topics, presentation.trade()));
    }

    /** The event a quest card's node sends, or null. */
    private static String cardEvent(DialogueTree hub, DialogueTopic topic) {
        if (topic.category() != DialogueCategory.QUESTS || topic.targetNodeId() == null) return null;
        DialogueNode node = hub.getNode(topic.targetNodeId());
        if (node == null) return null;
        for (DialogueOption option : node.options()) if (option.eventId() != null) return option.eventId();
        return null;
    }

    public static boolean handle(DialogueEvent event) {
        ServerPlayer player = event.getPlayer();
        if (!player.isAlive() || player.isSpectator()
                || !(player.level().getEntity(event.getEntityId()) instanceof CircusPerformerEntity teacher)
                || player.distanceToSqr(teacher) > 64) return false;
        if (!teacher.canTeachNow()) {
            CircusPracticalController.systemMessage(player, "busy");
            return false;
        }
        String role = teacher.facultyRoleId();
        var lesson = CircusCurriculum.forTeacher(role).orElse(null);
        switch (event.getEventId()) {
            case "circus_school_teach" -> {
                if (lesson == null || !CircusSchoolRules.canTeach(role, lesson.summon(),
                        HemoCapabilityAccess.getPlayerDegreeNumber(player),
                        HemoCapabilityAccess.getBloodVolume(player).isActive(),
                        player.isAlive(), player.isSpectator())) {
                    player.displayClientMessage(Component.translatable("hemomancy.circus.school.not_ready"), false);
                    return false;
                }
                if (!CircusApprenticeshipProgress.teach(player, lesson.summon())) return false;
                CircusPracticalController.systemMessage(player, "lesson_taught");
                return true;
            }
            case "circus_school_practical", "circus_school_guest" -> {
                return lesson != null && CircusPracticalController.begin(player, teacher, lesson.summon(),
                        event.getEventId().equals("circus_school_guest"));
            }
            case "circus_school_testimony" -> { CircusSchoolQuests.testimony(player, role); return true; }
            case "circus_school_inspect" -> { return role.equals("circus_threadkeeper") && CircusSchoolQuests.inspect(player); }
            case "circus_school_report" -> { return role.equals("circus_threadkeeper") && CircusSchoolQuests.report(player); }
            case "circus_school_claim" -> { return role.equals("circus_threadkeeper") && CircusSchoolQuests.claim(player); }
            default -> { return false; }
        }
    }
}
