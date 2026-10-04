package com.vincenthuto.hemomancy.common.data.book;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hutoslib.common.data.book.BookCodeModel;
import net.minecraft.network.chat.Component;

/** Reader callbacks use the existing synced progression; the visual theme cannot grant Clarity. */
public final class HemomancyBookPresentation {
    private HemomancyBookPresentation() {}

    public static void configure(BookCodeModel book) {
        book.setRedactionPredicate((player, level) -> player != null && HemoCapabilityAccess.getUnstainedProgress(player)
                .map(progress -> canReveal(progress.hasClarityUnlocked(), progress.getClarity(), level)).orElse(false));
        book.setRevealRequirementLabel(level -> Component.translatable("hemomancy.book.clarity_requirement", roman(level)));
        boolean liber = book.getResourceLocation().getPath().equals("liberimmaculatus");
        book.setShowWashedSearchCount(liber);
        book.setOwnerLine(player -> liber ? Component.translatable("hemomancy.book.owner", player.getName())
                : Component.translatable("hemomancy.book.owner_degree", player.getName(), HemoCapabilityAccess.getPlayerDegreeNumber(player)));
        if (liber) {
            book.setStatusLine(player -> HemoCapabilityAccess.getUnstainedProgress(player)
                    .map(progress -> Component.translatable("hemomancy.book.purity_clarity", Math.round(progress.getPurity()),
                            progress.hasClarityUnlocked() ? roman(EnumClarityStage.byClarity(progress.getClarity()).getLevel() + 1) : "—"))
                    .orElse(Component.translatable("hemomancy.book.purity_clarity", 0, "—")));
        }
    }

    static boolean canReveal(boolean unlocked, float clarity, int requested) {
        return unlocked && requested >= 1 && requested <= 5 && Float.isFinite(clarity)
                && EnumClarityStage.byClarity(clarity).getLevel() + 1 >= requested;
    }

    private static String roman(int level) {
        return switch (level) { case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; case 5 -> "V"; default -> "?"; };
    }
}
