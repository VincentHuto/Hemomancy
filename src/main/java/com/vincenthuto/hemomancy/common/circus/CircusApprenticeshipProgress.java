package com.vincenthuto.hemomancy.common.circus;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonTrialEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** The CircusPlayerProgress clone handler preserves this prefix, including pending rewards. */
public final class CircusApprenticeshipProgress {
    public static final String KEY = "hemomancy.circus_apprenticeship";
    private CircusApprenticeshipProgress() {}

    public static CompoundTag state(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(KEY, CompoundTag.TAG_COMPOUND)) persistent.put(KEY, new CompoundTag());
        return persistent.getCompound(KEY);
    }

    public static boolean knows(Player player, String summon) {
        return HemoCapabilityAccess.getKnownSummons(player)
                .map(known -> known.getKnownSummonNames().contains(summon)).orElse(false);
    }

    public static boolean instructionSatisfied(Player player, String summon) {
        return CircusCurriculum.instructionSatisfied(state(player).getBoolean("taught." + summon), knows(player, summon));
    }

    public static boolean teach(ServerPlayer player, String summon) {
        var lesson = CircusCurriculum.lesson(summon).orElse(null);
        if (lesson == null || !player.isAlive() || player.isSpectator()
                || HemoCapabilityAccess.getPlayerDegreeNumber(player) < lesson.degree()
                || !HemoCapabilityAccess.getBloodVolume(player).map(volume -> volume.isActive()).orElse(false)) return false;
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
