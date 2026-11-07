package com.vincenthuto.hemomancy.client.model.entity.boss.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielOpenings;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielPhase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PutricielTracksTest {
    private static final SaintMoveInputs ROT_I = new SaintMoveInputs(PutricielPhase.ROT_I.number(), 0, 0, 1);
    private static final SaintMoveInputs ROT_III = new SaintMoveInputs(PutricielPhase.ROT_III.number(), 0, 0, 1);
    private static final SaintMoveInputs FIRE = new SaintMoveInputs(PutricielPhase.INTERVAL.number(), 0, 0, 1);

    private static PutricielTracks.Track select(PutricielMove move, float t, int opening, float openingTime,
            SaintMoveInputs in) {
        return PutricielTracks.select(move == null ? 0 : move.id(), move == null ? -1 : t, opening, openingTime,
                opening == 0 ? 0 : 50, -1, -1, 1000, in, 77);
    }

    @Test
    void theWindupStretchesTheActiveSpanKeepsAndTheRecoveryStretches() {
        var actual = new BossMoveTimeline(10, 6, 7);
        var authored = new BossMoveTimeline(16, 6, 14);
        assertEquals(0.0F, PutricielTracks.authored(0, actual, authored), 1e-5F);
        assertEquals(8.0F, PutricielTracks.authored(5, actual, authored), 1e-5F);
        assertEquals(16.0F, PutricielTracks.authored(10, actual, authored), 1e-5F, "the commit lands on the authored commit");
        assertEquals(19.0F, PutricielTracks.authored(13, actual, authored), 1e-5F);
        assertEquals(22.0F, PutricielTracks.authored(16, actual, authored), 1e-5F);
        assertEquals(36.0F, PutricielTracks.authored(23, actual, authored), 1e-5F, "the cut recovery plays the whole settle");
        assertEquals(-1.0F, PutricielTracks.authored(-1, actual, authored), 1e-5F);
    }

    @Test
    void aRotIiiWindupPlaysTheAuthoredKeysAndStrikesOnTheCommit() {
        var actual = PutricielMove.CENSER_SWING.timeline(ROT_III);
        var authored = PutricielTracks.authoredTimeline(PutricielMove.CENSER_SWING, ROT_III);
        assertTrue(actual.windup() < authored.windup(), "Rot III cuts the wind-up");
        var track = select(PutricielMove.CENSER_SWING, actual.windup(), 0, -1, ROT_III);
        assertEquals(PutricielTracks.MOVE + PutricielMove.CENSER_SWING.id(), track.id());
        assertEquals(authored.windup(), track.time(), 1e-4F);
        assertEquals(authored.total(), track.length(), 1e-4F);
    }

    @Test
    void theEndingTheRiseAndTheRecoilOutrankTheMove() {
        var in = FIRE;
        int offering = PutricielMove.OFFERING.id();
        var ending = PutricielTracks.select(offering, 20, PutricielOpenings.OFFERING_ID, 8, 50, 30, 10, 2, in, 0);
        assertEquals(PutricielTracks.ENDING, ending.id());
        assertEquals(30.0F, ending.time(), 1e-5F);
        var rise = PutricielTracks.select(0, -1, 0, -1, 0, -1, 12, 1000, ROT_I, 0);
        assertEquals(PutricielTracks.RISE, rise.id());
        var recoil = PutricielTracks.select(offering, 20, PutricielOpenings.OFFERING_ID, 8, 50, -1, -1, 3, in, 0);
        assertEquals(PutricielTracks.RECOIL, recoil.id());
        assertEquals(3.0F, recoil.time(), 1e-5F);
        var after = PutricielTracks.select(0, -1, 0, -1, 0, -1, -1, PutricielTracks.RECOIL_TICKS + 1, in, 55);
        assertEquals(PutricielTracks.IDLE, after.id());
        assertEquals(55.0F, after.time(), 1e-5F);
    }

    @Test
    void aRecoveryOpeningTakesTheTrackButTheKneelingBeatsKeepTheirOwn() {
        var sacs = PutricielMove.SAC_SWELL.timeline(ROT_I);
        var spent = select(PutricielMove.SAC_SWELL, sacs.windup() + sacs.active() + 4, PutricielOpenings.SPENT_SACS_ID, 4, ROT_I);
        assertTrue(spent.opening());
        assertEquals(PutricielTracks.OPENING + PutricielOpenings.SPENT_SACS_ID, spent.id());
        assertEquals(4.0F, spent.time(), 1e-5F);
        var swelling = select(PutricielMove.SAC_SWELL, 10, PutricielOpenings.SPENT_SACS_ID, 4, ROT_I);
        assertTrue(swelling.move(), "an opening never cuts a wind-up short");
        var offering = PutricielTracks.select(PutricielMove.OFFERING.id(), 40, PutricielOpenings.OFFERING_ID, 27, 60, -1, -1,
                1000, FIRE, 0);
        assertTrue(offering.move(), "the Offering kneels through its own opening");
        assertEquals(40.0F, offering.time(), 1e-5F);
        assertEquals(12 + 1 + 60, offering.length(), 1e-5F, "it holds to the opening's length");
        var broken = PutricielTracks.select(0, -1, PutricielOpenings.BROKEN_COMMUNION_ID, 6, 40, -1, -1, 1000, ROT_I, 0);
        assertEquals(PutricielTracks.OPENING + PutricielOpenings.BROKEN_COMMUNION_ID, broken.id());
        assertEquals(40.0F, broken.length(), 1e-5F);
    }

    @Test
    void theVolleyCarriesItsDiscCount() {
        var track = select(PutricielMove.SHELF_VOLLEY, 30, 0, -1, ROT_III);
        assertEquals(PutricielMove.volleyDiscs(ROT_III), track.count());
        assertTrue(track.count() > PutricielMove.volleyDiscs(ROT_I));
    }
}
