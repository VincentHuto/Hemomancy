package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodKnowledge;
import com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import static com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress.Lesson;

public final class ClinicalBloodDialogue {
    private ClinicalBloodDialogue() {}

    public static DialogueTree append(DialogueTree tree, ServerPlayer player, String teacher) {
        if (!ClinicalBloodKnowledge.eligible(player)) return tree;
        var progress = HemoCapabilityAccess.clinicalBlood(player);
        var nodes = new LinkedHashMap<>(tree.nodes());
        var root = tree.getStartNode();
        var options = new ArrayList<>(root.options());
        for (Lesson lesson : Lesson.values()) {
            if (!lesson.teacher.equals(teacher)) continue;
            boolean ready = ClinicalBloodKnowledge.canLearn(player, lesson);
            if (!ready && !progress.knows(lesson)) continue;
            String id = "clinical_lesson_" + lesson.key();
            options.add(0, new DialogueOption("hemomancy.clinical." + lesson.key() + ".title", id,
                    ready ? lesson.event() : null, ready ? DialogueOptionPresentation.attention(DialogueAttention.NOTICE)
                    : DialogueOptionPresentation.normal()));
            nodes.put(id, new DialogueNode(id, List.of("hemomancy.clinical." + lesson.key() + ".lesson"),
                    List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))));
        }
        String next = nextStep(progress, HemoCapabilityAccess.getPlayerDegreeNumber(player), teacher);
        if (next != null) {
            String id = "clinical_assignment_next";
            options.add(new DialogueOption("hemomancy.clinical.next.title", id, null));
            nodes.put(id, new DialogueNode(id, List.of("hemomancy.clinical.next." + next),
                    List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))));
        }
        nodes.put(root.id(), new DialogueNode(root.id(), root.lines(), options));
        return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
                tree.entityId(), tree.theme(), tree.presentation());
    }

    public static String nextStep(ClinicalBloodProgress p, int degree, String teacher) {
        if (teacher.equals("artificer")) return p.knows(Lesson.FIELD_CASE) ? "field_case" : "field_referral";
        if (teacher.equals("mnemonist")) return p.knows(Lesson.CLAIRAUDIOGRAPH) ? "recording"
                : p.knows(Lesson.ECHO_REFERRAL) ? "formal_study" : "echo_referral";
        if (!teacher.equals("alchemist")) return null;
        if (!p.collected) return "collect";
        if (!p.knows(Lesson.MICROSCOPE)) return "show_vial";
        if (p.sourceCount() == 0) return "examine";
        if (!p.knows(Lesson.INJECTION)) return "injection";
        if (p.sourceCount() < 3) return "three_sources";
        if (!p.knows(Lesson.CABINET)) return "cabinet";
        if (!p.cabinetCrafted || !p.cabinetInserted || !p.cabinetWithdrawn) return "storage";
        if (degree < 2) return "degree_two";
        if (!p.hematicIronObtained || !p.artificerMet) return "iron_artificer";
        if (!p.knows(Lesson.FIELD_REFERRAL)) return "referral";
        return "later_study";
    }

    public static Optional<List<String>> inquiry(Player player, String teacher, ItemStack stack) {
        var p = HemoCapabilityAccess.clinicalBlood(player);
        int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        String key = null;
        if (teacher.equals("alchemist")) {
            key = switch (path) {
                case "hemomancy:hematic_microscope" -> p.knows(Lesson.MICROSCOPE) ? "microscope.lesson" : "next.show_vial";
                case "hemomancy:phlebotomists_cabinet", "minecraft:glass_pane" -> p.knows(Lesson.CABINET) ? "cabinet.lesson" : "next.three_sources";
                case "hemomancy:phlebotomists_field_case" -> "inquiry.alchemist_field_case";
                case "hemomancy:clairaudiograph", "hemomancy:ambergris_cylinder", "minecraft:echo_shard" -> degree >= 2 ? "inquiry.alchemist_echo" : "inquiry.later";
                case "hemomancy:living_syringe", "hemomancy:vial_rack" -> "inquiry.sampling";
                case "hemomancy:vial_centrifuge" -> "inquiry.centrifuge";
                case "hemomancy:ghastly_alembic" -> degree >= 2 ? "inquiry.alembic" : "inquiry.later";
                default -> null;
            };
            if (stack.getItem() instanceof BloodVialItem) key = !BloodSampleData.isFilled(stack) ? "inquiry.empty_vial"
                    : !BloodSampleData.identified(stack) ? "inquiry.unidentified_vial"
                    : p.knows(Lesson.INJECTION) ? "inquiry.identified_vial" : "next.injection";
        } else if (teacher.equals("artificer")) {
            key = switch (path) {
                case "hemomancy:phlebotomists_field_case" -> p.knows(Lesson.FIELD_CASE) ? "field_case.lesson" : "next.field_referral";
                case "hemomancy:phlebotomists_cabinet" -> "inquiry.artificer_cabinet";
                case "hemomancy:hematic_microscope" -> "inquiry.artificer_microscope";
                case "hemomancy:clairaudiograph" -> degree >= 2 ? "inquiry.artificer_recording" : "inquiry.later";
                default -> null;
            };
        } else if (teacher.equals("mnemonist")) {
            key = switch (path) {
                case "hemomancy:clairaudiograph", "hemomancy:ambergris_cylinder", "minecraft:echo_shard" ->
                        p.knows(Lesson.CLAIRAUDIOGRAPH) ? "clairaudiograph.lesson" : "inquiry.echo_hint";
                default -> null;
            };
        }
        if (teacher.equals("mnemonist") && path.equals("hemomancy:ambergris_cylinder") && p.knows(Lesson.CLAIRAUDIOGRAPH)) {
            var recording = stack.get(com.vincenthuto.hemomancy.common.init.DataComponentInit.CLAIRAUDIOGRAPH_RECORDING.get());
            key = recording == null ? "inquiry.blank_cylinder" : recording.readable() ? "inquiry.carved_cylinder" : "inquiry.unreadable_cylinder";
        }
        if (key == null) return Optional.empty();
        return Optional.of(List.of("hemomancy.clinical." + (degree < 1 ? "inquiry.later" : key)));
    }
}
