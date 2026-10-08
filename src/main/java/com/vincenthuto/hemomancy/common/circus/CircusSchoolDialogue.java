package com.vincenthuto.hemomancy.common.circus;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;
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
        options.add(new DialogueOption(root + "ask_history", "history", null));
        options.add(new DialogueOption(root + "ask_colleagues", "colleagues", null));
        options.add(new DialogueOption(root + "ask_preparation", "preparation", null));
        options.add(new DialogueOption(root + "ask_aftermath", "aftermath", null));
        options.add(new DialogueOption(root + "testimony", "testimony", "circus_school_testimony"));
        CircusCurriculum.forTeacher(role).ifPresent(lesson -> {
            options.add(new DialogueOption(root + "take_lesson", "lesson", "circus_school_teach"));
            options.add(new DialogueOption(root + "practical", null, "circus_school_practical"));
            options.add(new DialogueOption(root + "guest", null, "circus_school_guest"));
        });
        if (role.equals("circus_threadkeeper")) {
            options.add(new DialogueOption(root + "inspect", null, "circus_school_inspect"));
            options.add(new DialogueOption(root + "report", null, "circus_school_report"));
            options.add(new DialogueOption(root + "claim", null, "circus_school_claim"));
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

    public static boolean handle(DialogueEvent event) {
        ServerPlayer player = event.getPlayer();
        if (!player.isAlive() || player.isSpectator()
                || !(player.level().getEntity(event.getEntityId()) instanceof CircusPerformerEntity teacher)
                || !teacher.canTeachNow() || player.distanceToSqr(teacher) > 64) return false;
        String role = teacher.facultyRoleId();
        var lesson = CircusCurriculum.forTeacher(role).orElse(null);
        switch (event.getEventId()) {
            case "circus_school_teach" -> {
                if (lesson == null || !CircusSchoolRules.canTeach(role, lesson.summon(),
                        HemoCapabilityAccess.getPlayerDegreeNumber(player),
                        HemoCapabilityAccess.getBloodVolume(player).map(v -> v.isActive()).orElse(false),
                        player.isAlive(), player.isSpectator())) {
                    player.displayClientMessage(Component.translatable("hemomancy.circus.school.not_ready"), false);
                    return false;
                }
                return CircusApprenticeshipProgress.teach(player, lesson.summon());
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
