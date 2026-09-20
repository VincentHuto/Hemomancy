package com.vincenthuto.hemomancy.common.succession;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class SuccessionLedgerTest {
    @Test void restartPreservesBothClaimAndIdentityLocks() {
        var ledger = new SuccessionLedger();
        UUID id = UUID.randomUUID(), rite = UUID.randomUUID();
        ledger.birth(id); ledger.die(id);
        var place = new SuccessionLedger.Workplace("minecraft:overworld", 71);
        assertTrue(ledger.reserve(rite, place, id, 1));
        var loaded = SuccessionLedger.load(ledger.save());
        assertFalse(loaded.reserve(UUID.randomUUID(), place, null, 0));
        assertEquals(-1, loaded.reissue(id));
        assertTrue(loaded.restore(rite, id));
        var alive = SuccessionLedger.load(loaded.save());
        assertFalse(alive.available(place));
        assertTrue(alive.owns(id, place));
    }

    @Test void independentBloodlineBequestsAndSampleReceiptsSurviveReload() {
        var data = new SuccessionSavedData();
        UUID donor = UUID.randomUUID(), first = UUID.randomUUID(), second = UUID.randomUUID();
        data.bequeath(donor, first, "alchemist", "Teacher");
        data.bequeath(donor, second, "alchemist", "Teacher");
        var sample = new net.minecraft.nbt.CompoundTag(); sample.putUUID("Donor", donor); sample.putUUID("Bloodline", first);
        sample.putString("Profession", "alchemist"); sample.putUUID("Token", data.issueSample(sample));
        var loaded = SuccessionSavedData.load(data.save(new net.minecraft.nbt.CompoundTag(), null), null);
        assertTrue(loaded.hasBequest(donor, first, "alchemist"));
        assertTrue(loaded.hasBequest(donor, second, "alchemist"));
        assertTrue(loaded.authentic(sample));
        var forged = sample.copy(); forged.putUUID("Bloodline", second);
        assertFalse(loaded.authentic(forged));
        loaded.spendSample(sample);
        assertFalse(loaded.authentic(sample));
    }
    @Test void twoRitesCannotReserveOneWorkplace() {
        var ledger = new SuccessionLedger();
        var place = new SuccessionLedger.Workplace("minecraft:overworld", 12L);
        UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        assertTrue(ledger.reserve(first, place, null, 0));
        assertFalse(ledger.reserve(second, place, null, 0));
        ledger.release(first);
        assertTrue(ledger.reserve(second, place, null, 0));
    }

    @Test void restorationLocksIdentityEvenAtDifferentWorkplaces() {
        var ledger = new SuccessionLedger();
        UUID identity = UUID.randomUUID(), rite = UUID.randomUUID();
        ledger.birth(identity);
        int generation = ledger.die(identity);
        assertTrue(ledger.reserve(rite, new SuccessionLedger.Workplace("minecraft:overworld", 1), identity, generation));
        assertFalse(ledger.reserve(UUID.randomUUID(), new SuccessionLedger.Workplace("minecraft:overworld", 2), identity, generation));
        assertEquals(-1, ledger.reissue(identity));
        assertTrue(ledger.restore(rite, identity));
        assertFalse(ledger.restore(rite, identity));
        assertFalse(ledger.reserve(UUID.randomUUID(), new SuccessionLedger.Workplace("minecraft:overworld", 2), identity, generation));
    }

    @Test void copiedRemnantsCannotSurviveReissueOrAnotherDeath() {
        var ledger = new SuccessionLedger();
        UUID id = UUID.randomUUID(), rite = UUID.randomUUID();
        ledger.birth(id);
        assertEquals(1, ledger.die(id));
        assertEquals(2, ledger.reissue(id));
        var place = new SuccessionLedger.Workplace("minecraft:overworld", 1);
        assertFalse(ledger.reserve(rite, place, id, 1));
        assertTrue(ledger.reserve(rite, place, id, 2));
        ledger.restore(rite, id);
        assertEquals(3, ledger.die(id));
        assertFalse(ledger.reserve(rite, place, id, 2));
    }

    @Test void failedRestorationReleasesReservationWithoutErasingIdentity() {
        var ledger = new SuccessionLedger();
        UUID id = UUID.randomUUID(), rite = UUID.randomUUID();
        ledger.birth(id);
        ledger.die(id);
        var place = new SuccessionLedger.Workplace("minecraft:overworld", 1);
        assertTrue(ledger.reserve(rite, place, id, 1));
        ledger.release(rite);
        assertTrue(ledger.reserve(UUID.randomUUID(), place, id, 1));
    }
}
