package com.vincenthuto.hemomancy.client.render.outpouring;

import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringRules.Stage;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPurpose;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastStyle;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArtEnding;
import com.vincenthuto.hemomancy.common.network.CastingAnimationPacket;
import net.minecraft.world.InteractionHand;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OutpouringTrackerTest {
    private static final UUID PLAYER = UUID.randomUUID();
    /** Registered ids resolve to ARMS here; preview ids go through the rules, like the live resolver. */
    private static final OutpouringTracker.Resolver RESOLVER = new OutpouringTracker.Resolver() {
        public EnumVeinSections section(String id) {
            var preview = OutpouringRules.previewSection(id);
            return preview == null ? EnumVeinSections.ARMS : preview;
        }
        public EnumManipulationType type(CastingAnimationPacket p) {
            return p.required() > 0 ? EnumManipulationType.CHARGED : EnumManipulationType.QUICK;
        }
    };

    private static CastingAnimationPacket packet(String id, long sequence, CastPhase phase, long at, int held, int required) {
        return CastingAnimationPacket.harbinger(7, PLAYER, id, sequence, EnumBloodTendency.ANIMUS, null,
                EnumManipulationRank.SUMMA, CastPurpose.AREA, CastStyle.SCHOOL, phase, at, at, held, required,
                InteractionHand.MAIN_HAND);
    }

    @Test void chargeBuildsThenPeaks() {
        var tracker = new OutpouringTracker(RESOLVER);
        tracker.accept(packet("blood_shot", 1, CastPhase.OPENING, 100, 20, 40), 100);
        var cast = tracker.current(PLAYER);
        assertEquals(Stage.BUILD, cast.pulse(100, 0).stage());
        tracker.accept(packet("blood_shot", 1, CastPhase.RELEASE, 110, 40, 40), 110);
        assertSame(cast, tracker.current(PLAYER));
        assertEquals(Stage.PEAK, cast.pulse(113, 0).stage());
    }

    @Test void recastDuringAftermathStartsNewCast() {
        var tracker = new OutpouringTracker(RESOLVER);
        tracker.accept(packet("blood_shot", 1, CastPhase.RELEASE, 100, 0, 0), 100);
        var first = tracker.current(PLAYER);
        tracker.accept(packet(OutpouringRules.previewId(EnumVeinSections.HEAD), 2, CastPhase.RELEASE, 115, 0, 0), 115);
        var second = tracker.current(PLAYER);
        assertNotSame(first, second);
        assertEquals(EnumVeinSections.HEAD, second.section());
        assertTrue(tracker.fading().contains(first));
    }

    @Test void heartbeatsUpdateTheSameCast() {
        var tracker = new OutpouringTracker(RESOLVER);
        tracker.accept(packet("blood_shot", 1, CastPhase.OPENING, 100, 10, 40), 100);
        var cast = tracker.current(PLAYER);
        tracker.accept(packet("blood_shot", 1, CastPhase.HOLD, 100, 30, 40), 120);
        assertSame(cast, tracker.current(PLAYER));
        assertTrue(cast.pulse(120, 0).strength() > .5f);
    }

    @Test void cancelWithdraws() {
        var tracker = new OutpouringTracker(RESOLVER);
        tracker.accept(packet("blood_shot", 1, CastPhase.OPENING, 100, 20, 40), 100);
        var cast = tracker.current(PLAYER);
        tracker.accept(packet("blood_shot", 1, CastPhase.CANCEL, 105, 20, 40), 105);
        assertEquals(Stage.WITHDRAW, cast.pulse(106, 0).stage());
        assertTrue(cast.finished(105 + 7));
    }

    @Test void silentChannelEndsAfterFortyTicks() {
        var tracker = new OutpouringTracker(RESOLVER);
        tracker.accept(packet("blood_shot", 1, CastPhase.HOLD, 100, 0, 0), 100);
        var cast = tracker.current(PLAYER);
        assertFalse(cast.finished(130));
        tracker.tick(200);
        assertNull(tracker.current(PLAYER));
    }

    @Test void stillArtIgnored() {
        var tracker = new OutpouringTracker(RESOLVER);
        tracker.accept(CastingAnimationPacket.stillArt(7, PLAYER, "still", 1, EnumClarityStage.values()[0],
                StillArtEnding.values()[0], CastPhase.RELEASE, 100, 100, InteractionHand.MAIN_HAND), 100);
        assertNull(tracker.current(PLAYER));
        assertTrue(tracker.casts().isEmpty());
    }

    @Test void levelChangeClearsEverything() {
        var tracker = new OutpouringTracker(RESOLVER);
        tracker.accept(packet("blood_shot", 1, CastPhase.RELEASE, 100, 0, 0), 100);
        tracker.accept(packet("blood_shot", 2, CastPhase.RELEASE, 120, 0, 0), 120);
        tracker.clear();
        assertTrue(tracker.casts().isEmpty());
        assertTrue(tracker.fading().isEmpty());
    }

    @Test void previewSectionResolves() {
        var tracker = new OutpouringTracker(RESOLVER);
        tracker.accept(packet(OutpouringRules.previewId(EnumVeinSections.LEGS), 0, CastPhase.RELEASE, 100, 0, 0), 100);
        assertEquals(EnumVeinSections.LEGS, tracker.current(PLAYER).section());
    }

    @Test void finishedCastsArePruned() {
        var tracker = new OutpouringTracker(RESOLVER);
        tracker.accept(packet("blood_shot", 1, CastPhase.RELEASE, 100, 0, 0), 100);
        tracker.tick(100 + 8 + 40 + 1);
        assertNull(tracker.current(PLAYER));
    }

    @Test void castWaitsForItsBloodToSettle() {
        var tracker = new OutpouringTracker(RESOLVER);
        tracker.accept(packet("blood_shot", 1, CastPhase.RELEASE, 100, 0, 0), 100);
        var cast = tracker.current(PLAYER);
        cast.sim = new OutpouringSim(EnumBloodTendency.MORTEM, null, 1);
        cast.sim.emit(new double[]{0, 65, 0}, 1, new double[]{0, 0, 1}, 1,
                OutpouringRules.profile(EnumManipulationRank.SUMMA), 2, true, false);
        assertFalse(cast.finished(100 + 8 + 40 + 1), "the pulse is over but sludge is still on the ground");
        tracker.clear();
    }

    @Test void clearAndPruneReleaseTheMoteBudget() {
        int before = OutpouringSim.live();
        var tracker = new OutpouringTracker(RESOLVER);
        tracker.accept(packet("blood_shot", 1, CastPhase.RELEASE, 100, 0, 0), 100);
        var cast = tracker.current(PLAYER);
        cast.sim = new OutpouringSim(EnumBloodTendency.ANIMUS, null, 1);
        cast.sim.emit(new double[]{0, 65, 0}, 1, new double[]{0, 0, 1}, 1,
                OutpouringRules.profile(EnumManipulationRank.PERFECTUS), 4, true, false);
        assertTrue(OutpouringSim.live() > before);
        tracker.clear();
        assertEquals(before, OutpouringSim.live());
    }

    @Test void everyCastIsVisitedOnce() {
        var tracker = new OutpouringTracker(RESOLVER);
        tracker.accept(packet("blood_shot", 1, CastPhase.RELEASE, 100, 0, 0), 100);
        tracker.accept(packet("blood_shot", 2, CastPhase.RELEASE, 110, 0, 0), 110);
        assertEquals(2, tracker.all().size());
    }

    @Test void deadOrUntrackedCasterWithdraws() {
        var tracker = new OutpouringTracker(RESOLVER);
        tracker.accept(packet("blood_shot", 1, CastPhase.HOLD, 100, 0, 0), 100);
        var cast = tracker.current(PLAYER);
        tracker.end(PLAYER, 110);
        assertEquals(Stage.WITHDRAW, cast.pulse(111, 0).stage());
        assertFalse(cast.emitting(111));
        assertTrue(cast.finished(117));
    }

    @Test void previewWithANewSchoolRestarts() {
        var tracker = new OutpouringTracker(RESOLVER);
        tracker.accept(packet("preview", 0, CastPhase.OPENING, 100, 10, 40), 100);
        var first = tracker.current(PLAYER);
        tracker.accept(CastingAnimationPacket.harbinger(7, PLAYER, "preview", 0, EnumBloodTendency.LUX, null,
                EnumManipulationRank.SUMMA, CastPurpose.AREA, CastStyle.SCHOOL, CastPhase.OPENING, 105, 105, 10, 40,
                InteractionHand.MAIN_HAND), 105);
        assertNotSame(first, tracker.current(PLAYER));
        assertEquals(EnumBloodTendency.LUX, tracker.current(PLAYER).school);
    }

    @Test void everyCastGrowsItsOwnVeins() {
        var tracker = new OutpouringTracker(RESOLVER);
        java.util.Set<Integer> seeds = new java.util.HashSet<>();
        for (int i = 0; i < 6; i++) {
            // Previews repeat the same id and sequence; each still gets a fresh vein pattern.
            tracker.accept(packet("preview", 0, CastPhase.RELEASE, 100 + i * 80, 0, 0), 100 + i * 80);
            seeds.add(tracker.current(PLAYER).seed);
        }
        assertEquals(6, seeds.size());
    }
}
