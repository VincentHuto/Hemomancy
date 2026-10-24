package com.vincenthuto.hemomancy.common.capability.player.harbinger.circus;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonTrialEvents;
import com.vincenthuto.hemomancy.common.mission.circus.CircusCurriculum;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** The apprenticeship record lives in the CircusProgress attachment, so it survives death with pending rewards. */
public final class CircusApprenticeshipProgress {
    private CircusApprenticeshipProgress() {}

    public static CompoundTag state(Player player) {
        return HemoCapabilityAccess.circusProgress(player).apprenticeship();
    }

    public static boolean knows(Player player, String summon) {
        return HemoCapabilityAccess.getKnownSummons(player).getKnownSummonNames().contains(summon);
    }

    public static boolean instructionSatisfied(Player player, String summon) {
        return CircusCurriculum.instructionSatisfied(state(player).getBoolean("taught." + summon), knows(player, summon));
    }

    public static boolean teach(ServerPlayer player, String summon) {
        var lesson = CircusCurriculum.lesson(summon).orElse(null);
        if (lesson == null || !player.isAlive() || player.isSpectator()
                || HemoCapabilityAccess.getPlayerDegreeNumber(player) < lesson.degree()
                || !HemoCapabilityAccess.getBloodVolume(player).isActive()) return false;
        state(player).putBoolean("taught." + summon, true);
        CircusSchoolKnowledge.discover(player, summon);
        PuppeteerSummonTrialEvents.awardOrdealRecipes(player, HemoCapabilityAccess.getPlayerDegreeNumber(player));
        return true;
    }

    public static boolean practicalComplete(Player player, String summon) {
        return state(player).getBoolean("practical." + summon);
    }

    public static boolean completePractical(ServerPlayer player, String summon) {
        if (!knows(player, summon) || CircusCurriculum.lesson(summon).isEmpty() || practicalComplete(player, summon)) return false;
        state(player).putBoolean("practical." + summon, true);
        CircusPlayerProgress.awardMilestone(player, "practical." + summon, 100);
        return true;
    }
}
