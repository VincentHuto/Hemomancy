package com.vincenthuto.hemomancy.common.entity.npc.dialogue;
import com.vincenthuto.hemomancy.common.mission.alchemist.*;
import com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;
public final class EarlyInitiationDialogue {
 private EarlyInitiationDialogue() {}
 public static DialogueTree vicar(DialogueTree tree, ServerPlayer player) {
  if (!EarlyInitiation.eligible(player)) return tree;
  return add(tree, "formal_introduction", "hemomancy.initiation.ask", List.of("hemomancy.initiation.offer", "hemomancy.initiation.reason"), List.of(
   new DialogueOption("hemomancy.initiation.accept", null, "vicar_begin_initiation"),
   new DialogueOption("hemomancy.initiation.remove", null, "vicar_release_charm"),
   new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)));
 }
 public static DialogueTree alchemist(DialogueTree tree, ServerPlayer player) {
  if (!ClinicalBloodKnowledge.eligible(player) || com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getPlayerDegreeNumber(player) != 2
    || !FirstSeparationAssignment.isClaimed(player)
    || !com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.stationUpgrades(player).hasUsed(
            com.vincenthuto.hemomancy.common.station.UpgradeStation.ALEMBIC,
            com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog.DISTILL)) return tree;
  List<DialogueOption> options = new ArrayList<>();
  if (!ConcentratedBlood.pending(player)) options.add(new DialogueOption("hemomancy.initiation.replace", null, "alchemist_replace_concentrated_blood"));
  options.add(new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null));
  return add(tree, "concentrated_blood", "hemomancy.initiation.ask_blood", List.of("hemomancy.initiation.blood_rest"), options);
 }
 private static DialogueTree add(DialogueTree tree, String id, String label, List<String> lines, List<DialogueOption> choices) {
  var nodes = new LinkedHashMap<>(tree.nodes());
  var start = tree.getStartNode();
  var options = new ArrayList<>(start.options());
  options.addFirst(new DialogueOption(label, id, null));
  nodes.put(start.id(), new DialogueNode(start.id(), start.lines(), options));
  nodes.put(id, new DialogueNode(id, lines, choices));
  return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes, tree.entityId(), tree.theme(), tree.presentation());
 }
}
