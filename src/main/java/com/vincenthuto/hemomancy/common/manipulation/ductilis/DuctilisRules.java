package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public final class DuctilisRules {
    public static final int RECOVERY_TICKS = 20;
    public static final int MAX_TRAVERSAL = 32;
    private DuctilisRules() { }

    public record Hop(UUID id, double x, double y, double z, boolean conductive, boolean relay) {
        double distanceSquared(double fromX, double fromY, double fromZ) {
            return (x-fromX)*(x-fromX)+(y-fromY)*(y-fromY)+(z-fromZ)*(z-fromZ);
        }
    }

    public static Hop nextHop(Collection<Hop> candidates, double x, double y, double z,
            boolean first, boolean conductiveSource, Set<UUID> visited, Predicate<Hop> visible) {
        return candidates.stream().filter(n -> !visited.contains(n.id()))
                .filter(n -> {
                    double range = first ? 18 : conductiveSource || n.conductive() ? 8 : 5;
                    return n.distanceSquared(x,y,z) <= range*range && visible.test(n);
                })
                .min(Comparator.comparing((Hop n) -> !n.conductive())
                        .thenComparingDouble(n -> n.distanceSquared(x,y,z)).thenComparing(Hop::id))
                .orElse(null);
    }

    public static boolean canParalyze(long now, boolean active, long recoveryUntil) {
        return !active && now >= recoveryUntil;
    }

    public static int paralysisDuration(int requested, boolean player) {
        return Math.max(1, Math.min(player ? 20 : 60, requested));
    }
}
