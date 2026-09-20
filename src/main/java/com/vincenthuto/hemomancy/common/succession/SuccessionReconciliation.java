package com.vincenthuto.hemomancy.common.succession;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.*;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class SuccessionReconciliation {
    private SuccessionReconciliation() {}
    @SubscribeEvent public static void tick(ServerTickEvent.Post event) {
        var server = event.getServer();
        if (server.getTickCount() % 100 != 0) return;
        var data = SuccessionSavedData.get(server.overworld());
        if (data.ledger.reservations().isEmpty()) return;
        var active = new HashSet<UUID>();
        for (var level : server.getAllLevels()) for (var rite : CardinalRiteSavedData.get(level).getActiveRites().values())
            if (SuccessionRites.is(rite)) active.add(rite.succession().getUUID("Transaction"));
        for (UUID transaction : List.copyOf(data.ledger.reservations().keySet())) if (!active.contains(transaction)) {
            data.ledger.release(transaction); data.setDirty();
        }
    }
}
