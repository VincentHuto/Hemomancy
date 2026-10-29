package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;
import com.vincenthuto.hemomancy.common.capability.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.*;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
public final class DegreeProgression {
 private DegreeProgression() {}
 /** Degree 8 reached this way is Apotheos; Silentium passes its path explicitly. */
 public static boolean advance(ServerPlayer player, int target) {
  return advance(player, target, target == 8 ? EnumArchonPath.APOTHEOS : null);
 }
 /** Raises the degree and, when {@code path} is given, settles the endgame path before the rewards run. */
 public static boolean advance(ServerPlayer player, int target, EnumArchonPath path) {
  var degree=HemoCapabilityAccess.getInitiatoryDegree(player);
  if (degree == null || degree.getDegreeNumber() >= target) return false;
  degree.setDegreeNumber(target);
  if (path != null) degree.setArchonPath(path);
  PathMutualExclusionHelper.resetUnstainedProgress(player);
  InitiatoryDegreeEvents.syncDegree(player, degree);
  SkillPointGainEvents.onDegreeReached(player, target);
  HarbingerAdvancementGranter.grantDegree(player, target);
  LiberKnowledgeHelper.unlockForDegree(player, target);
  if (KnownManipulationGrantHelper.grantDegreeOneUtilities(player)) KnownManipulationEvents.syncPlayerEvent(player);
  player.displayClientMessage(Component.translatable("hemomancy.initiation.degree", DegreeTitles.title(target, degree.getArchonPath())), false);
  return true;
 }
}
