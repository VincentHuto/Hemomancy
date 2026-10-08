package com.vincenthuto.hemomancy.client.screen.item;

import com.vincenthuto.hemomancy.common.circus.CircusCurriculum;
import net.minecraft.nbt.CompoundTag;

public final class CircusSchoolLedgerState {
    private static CompoundTag state = new CompoundTag();
    private CircusSchoolLedgerState() {}
    public static void set(CompoundTag progress) { state = progress == null ? new CompoundTag() : progress.copy(); }
    public static boolean complete(String key) { return state.getBoolean(key); }
    public static int completedCount() {
        int count = 0;
        for (var lesson : CircusCurriculum.lessons()) if (complete("practical." + lesson.summon())) count++;
        for (String quest : new String[]{"empty_ring", "wrong_audience", "missing_understudy"}) if (complete("quest." + quest)) count++;
        return count;
    }
    public static String next(String summon) {
        if (complete("practical." + summon)) return "mastered";
        if (complete("known." + summon)) return "practical";
        return complete("taught." + summon) ? "ordeal" : "lesson";
    }
    public static String storyNext(String quest) {
        if (complete("quest." + quest)) return "complete";
        if (quest.equals("empty_ring")) {
            if (!state.hasUUID("primer.crossbar")) return "inspect";
            if (!complete("primer.call")) return "call";
            if (!complete("primer.command")) return "command";
            return complete("primer.recall") ? "report" : "recall";
        }
        if (quest.equals("wrong_audience")) {
            if (!complete("testimony.circus_stilt_walker") || !complete("testimony.circus_knife_thrower")) return "testimony";
            return complete("practical.scarlet_mummer") && complete("practical.marrow_spitter") ? "report" : "protect";
        }
        for (String clue : new String[]{"stage", "loft", "quarters", "carousel"}) if (!complete("clue." + clue)) return clue;
        return complete("testimony.circus_understudy") ? "report" : "testimony";
    }
    public static boolean rewardPending() { return state.getInt("guest.pending") > 0; }
}
