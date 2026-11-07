package com.vincenthuto.hemomancy.client.model.entity.boss.hemorath;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HemorathPoseBlendTest {
    @Test
    void theFirstPoseAndAnUnchangedTrackAreShownAsTheyAre() {
        HemorathPoseBlend blend = new HemorathPoseBlend();
        float[] first = {1, 2};
        blend.apply(first, 1, 0, 5);
        assertArrayEquals(new float[]{1, 2}, first);
        float[] same = {5, -3};
        blend.apply(same, 1, 1, 5);
        assertArrayEquals(new float[]{5, -3}, same);
    }

    @Test
    void aNewTrackStartsWhereTheLastPoseEndedAndArrivesByTheBlendsEnd() {
        HemorathPoseBlend blend = new HemorathPoseBlend();
        for (int frame = 0; frame <= 10; frame++) blend.apply(new float[]{1}, 1, frame, 5);
        float[] first = {0};
        blend.apply(first, 2, 10.25F, 5);
        assertEquals(1, first[0], .05F, "no jump at the change");
        float[] middle = {0};
        blend.apply(middle, 2, 12.5F, 5);
        assertTrue(middle[0] > 0 && middle[0] < 1);
        float[] end = {0};
        blend.apply(end, 2, 15.5F, 5);
        assertEquals(0, end[0], 1e-6F, "the new track alone once the blend is done");
    }

    @Test
    void itCarriesTheMotionTheBodyHadWhenTheTrackChanged() {
        HemorathPoseBlend blend = new HemorathPoseBlend();
        for (int frame = 0; frame <= 20; frame++) blend.apply(new float[]{frame * .2F}, 1, frame, 6);
        float[] next = {0};
        blend.apply(next, 2, 20.5F, 6);
        assertTrue(next[0] > HemorathPoseBlend.decay(4, 0, .5F, 6) + .05F,
                "a channel rising when the move changed keeps rising a moment before it turns");
    }

    @Test
    void theOffsetLandsFlat() {
        assertEquals(1, HemorathPoseBlend.decay(1, 0, 0, 5));
        assertEquals(.5F, HemorathPoseBlend.decay(1, 0, 2.5F, 5), 1e-6F);
        assertEquals(0, HemorathPoseBlend.decay(1, .3F, 5, 5));
        float nearEnd = HemorathPoseBlend.decay(1, 0, 4.9F, 5);
        assertTrue(nearEnd > 0 && nearEnd < .002F);
    }
}
