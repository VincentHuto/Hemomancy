package com.vincenthuto.hemomancy.common.circus;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.HemomancyDiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import net.minecraft.server.level.ServerPlayer;

public final class CircusSchoolKnowledge {
    private CircusSchoolKnowledge() {}
    public static void discover(ServerPlayer player, String page) {
        LiberKnowledgeHelper.unlockEntry(player, Hemomancy.rloc("libersanguinium/the_hematic_order/pages/troupe_" + page), HemomancyDiscoverySource.DIALOGUE);
    }
}
