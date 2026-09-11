package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

public final class DialogueRewardClaims {
    private DialogueRewardClaims() {}

    public static boolean requiresAcknowledgement(String eventId) {
        if (eventId == null) return false;
        if (MnemonistStarterMemoryChoice.fromEventId(eventId).isPresent()) return true;
        return switch (eventId) {
            case HarbingerMnemonistDialogueTrees.EVENT_WOVEN_VESSEL_TURN_IN,
                 HarbingerCicatrixAnchoriteDialogueTrees.EVENT_FIRST_LESSON,
                 HarbingerCicatrixAnchoriteDialogueTrees.EVENT_CONTINUATION_REWARD,
                 HarbingerVicarDialogueTrees.EVENT_CLAIM_FIRST_BLOODCRAFT_REWARD,
                 HarbingerVicarDialogueTrees.EVENT_CONSECRATION_KIT,
                 HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_CLAIM,
                 SanguineMonolithDialogueTrees.EVENT_CORNERSTONE,
                 SanguineMonolithDialogueTrees.EVENT_SHATTER -> true;
            default -> false;
        };
    }
}
