package com.vincenthuto.hemomancy.gametest;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** Captures the removal caller for an intermittent fixture failure. */
final class FixtureEntityRemovalProbe implements AutoCloseable {
    private final Consumer<EntityLeaveLevelEvent> listener;
    private String trace = "not removed";

    FixtureEntityRemovalProbe(Entity actor) {
        listener = event -> {
            if (event.getEntity() != actor) return;
            trace = actor.getRemovalReason() + " at " + actor.position() + ": "
                    + Arrays.stream(new Throwable().getStackTrace()).limit(24)
                            .map(StackTraceElement::toString).collect(Collectors.joining(" <- "));
        };
        NeoForge.EVENT_BUS.addListener(listener);
    }

    String trace() {
        return trace;
    }

    @Override public void close() {
        NeoForge.EVENT_BUS.unregister(listener);
    }
}
