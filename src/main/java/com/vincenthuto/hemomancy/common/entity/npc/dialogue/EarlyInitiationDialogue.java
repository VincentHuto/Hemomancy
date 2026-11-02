package com.vincenthuto.hemomancy.common.entity.npc.dialogue;
import com.vincenthuto.hemomancy.common.mission.alchemist.*;
import com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;
public final class EarlyInitiationDialogue {
 /** The Vicar's initiation choices; only a Vicar may speak them (a founder receives through Blood Projection). */
 public static final String BEGIN = "vicar_begin_initiation";
 public static final String RELEASE = "vicar_release_charm";
 private static final DialogueOptionPresentation MAIN_STEP =
   DialogueOptionPresentation.attention(DialogueAttention.NOTICE).withQuest();
 private EarlyInitiationDialogue() {}
 public static DialogueTree vicar(DialogueTree tree, ServerPlayer player) {
  if (!EarlyInitiation.eligible(player)) return tree;
  // The initiation is the charm-bearer's next Main step: a quest with a sigil over the Vicar.
  return add(tree, "formal_introduction", "hemomancy.initiation.ask", MAIN_STEP, List.of("hemomancy.initiation.offer", "hemomancy.initiation.reason"), List.of(
   new DialogueOption("hemomancy.initiation.accept", null, BEGIN),
   new DialogueOption("hemomancy.initiation.remove", null, RELEASE),
   new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)));
 }
 public static DialogueTree alchemist(DialogueTree tree, ServerPlayer player) {
  // Concentrated Blood takes only through the attached charm, so a player without it is not offered the vial.
  if (!EarlyInitiation.attached(player) || !ClinicalBloodKnowledge.eligible(player) || !ProgressionGates.isDegree(player, 2)
    || !FirstSeparationAssignment.isClaimed(player)
    || !com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.stationUpgrades(player).hasUsed(
            com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation.ALEMBIC,
            com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog.DISTILL)) return tree;
  List<DialogueOption> options = new ArrayList<>();
  if (!ConcentratedBlood.pending(player)) options.add(new DialogueOption("hemomancy.initiation.replace", null, "alchemist_replace_concentrated_blood"));
  options.add(new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null));
  // The sigil marks the Alchemist only while the vial can actually be issued: nothing pending and none carried.
  boolean actionable = !ConcentratedBlood.pending(player) && !ConcentratedBlood.carries(player);
  return add(tree, "concentrated_blood", "hemomancy.initiation.ask_blood",
    actionable ? MAIN_STEP : DialogueOptionPresentation.questWork(),
    List.of("hemomancy.initiation.blood_rest"), options);
 }
 private static DialogueTree add(DialogueTree tree, String id, String label, DialogueOptionPresentation presentation,
   List<String> lines, List<DialogueOption> choices) {
  var nodes = new LinkedHashMap<>(tree.nodes());
  var start = tree.getStartNode();
  var options = new ArrayList<>(start.options());
  options.addFirst(new DialogueOption(label, id, null, presentation));
  nodes.put(start.id(), new DialogueNode(start.id(), start.lines(), options));
  nodes.put(id, new DialogueNode(id, lines, choices));
  return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes, tree.entityId(), tree.theme(), tree.presentation());
 }
}
