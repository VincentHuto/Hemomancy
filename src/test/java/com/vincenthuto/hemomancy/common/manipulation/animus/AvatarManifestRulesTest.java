package com.vincenthuto.hemomancy.common.manipulation.animus;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AvatarManifestRulesTest {
    @Test void countsCarryStageAndDirection() {
        for (int stage = 0; stage < AvatarManifestRules.STAGES; stage++) {
            int up = AvatarManifestRules.manifest(stage), down = AvatarManifestRules.dismiss(stage);
            assertTrue(up >= 1 && down >= 1);
            assertFalse(AvatarManifestRules.dismissing(up));
            assertTrue(AvatarManifestRules.dismissing(down));
            assertEquals(stage, AvatarManifestRules.stage(up));
            assertEquals(stage, AvatarManifestRules.stage(down));
        }
        assertEquals(4, AvatarManifestRules.stage(AvatarManifestRules.manifest(99)));
    }

    @Test void theStagesMatchTheAvatarStats() {
        String[] forms = { "summon_avatar", "summon_avatar_arms", "summon_avatar_armor", "summon_avatar_legs", "summon_avatar_complete" };
        for (int i = 0; i < forms.length; i++)
            assertEquals(i, AvatarManifestationRules.stats(forms[i]).orElseThrow().stage());
    }

    @Test void theTimelineFitsTheLayerTransition() {
        assertTrue(AvatarManifestRules.EMERGE_END <= AvatarManifestRules.TRANSITION_TICKS);
        assertTrue(AvatarManifestRules.VISUAL_TICKS >= AvatarManifestRules.TRANSITION_TICKS);
        assertEquals(1, AvatarManifestRules.rise(AvatarManifestRules.RISE_END), 1e-9);
        assertEquals(1, AvatarManifestRules.grow(AvatarManifestRules.GROW_END), 1e-9);
        double peak = 0;
        for (double age = AvatarManifestRules.RISE_END; age <= AvatarManifestRules.GROW_END; age += .1)
            peak = Math.max(peak, AvatarManifestRules.grow(age));
        assertTrue(peak > 1, "the sinew lashes past its mark before settling");
        assertEquals(0, AvatarManifestRules.emerge(AvatarManifestRules.GROW_END - 1), 1e-9);
        assertTrue(AvatarManifestRules.emerge((AvatarManifestRules.GROW_END + AvatarManifestRules.EMERGE_END) / 2.0) > .9);
        assertEquals(0, AvatarManifestRules.melt(AvatarManifestRules.MELT_END), 1e-9);
        assertEquals(1, AvatarManifestRules.drain(AvatarManifestRules.VISUAL_TICKS), 1e-9);
        for (int ring = 0; ring < 4; ring++)
            assertEquals(1, AvatarManifestRules.ringHeight(AvatarManifestRules.RISE_END + 2, ring, 4), 1e-9);
    }
}
