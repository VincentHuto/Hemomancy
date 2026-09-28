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
 public static boolean advance(ServerPlayer player, int target) {
  var degree=HemoCapabilityAccess.getInitiatoryDegree(player).orElse(null);
  if (degree == null || degree.getDegreeNumber() >= target) return false;
  degree.setDegreeNumber(target);
  if (target == 8) degree.setArchonPath(EnumArchonPath.APOTHEOS);
  PathMutualExclusionHelper.resetUnstainedProgress(player);
  InitiatoryDegreeEvents.syncDegree(player, degree);
  SkillPointGainEvents.onDegreeReached(player, target);
  HarbingerAdvancementGranter.grantDegree(player, target);
  LiberKnowledgeHelper.unlockForDegree(player, target);
  if (KnownManipulationGrantHelper.grantDegreeOneUtilities(player)) KnownManipulationEvents.syncPlayerEvent(player);
  player.displayClientMessage(Component.translatable("hemomancy.initiation.degree", Component.translatable(degree.getDegree().getLangKey())), false);
  return true;
 }
}
