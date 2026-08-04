
package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.world.entity.player.Player;

/** Stable player state used while resolving inventory inquiry branches. */
public record ItemInquiryContext(
        int degree,
        float purity,
        float clarity,
        boolean clarityUnlocked,
        boolean activeBlood,
        boolean purifying,
        boolean silentArchon,
        boolean apotheos
) {
    public static ItemInquiryContext legacy(int degree, float purity) {
        return new ItemInquiryContext(degree, purity, 0F, false, degree > 0, purity > 0F, false, degree >= 8);
    }

    public static ItemInquiryContext from(Player player) {
        int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
        float[] unstained = new float[2];
        boolean[] flags = new boolean[2];
        HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(progress -> {
            unstained[0] = progress.getPurity();
            unstained[1] = progress.getClarity();
            flags[0] = progress.hasBegunPurification();
            flags[1] = progress.hasClarityUnlocked();
        });
        boolean activeBlood = HemoCapabilityAccess.getBloodVolume(player).map(volume -> volume.isActive()).orElse(false);
        boolean silent = player.getPersistentData().getBoolean("hemomancy.silent_archon")
                || player.getPersistentData().getBoolean("silent_archon");
        return new ItemInquiryContext(degree, unstained[0], unstained[1], flags[1], activeBlood,
                flags[0], silent, degree >= 8);
    }
}
