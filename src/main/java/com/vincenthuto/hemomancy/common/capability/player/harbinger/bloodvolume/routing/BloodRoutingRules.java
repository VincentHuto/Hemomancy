package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;

public final class BloodRoutingRules {

    public static final double DEFAULT_WORKING_RESERVE = 600.0;
    public static final double DEFAULT_PLAYER_SAFETY_RATIO = 0.5;

    private BloodRoutingRules() {
    }

    public static double calculateReserveRequest(IBloodVolume target, double workingReserve, double maxRequest) {
        if (target == null || maxRequest <= 0 || workingReserve <= 0) {
            return 0;
        }
        double reserveCap = Math.min(workingReserve, target.getMaxBloodVolume());
        double missingReserve = Math.max(0, reserveCap - target.getBloodVolume());
        return Math.min(maxRequest, missingReserve);
    }

    public static double workingReserve(int sanctumSutureLevel) {
        return DEFAULT_WORKING_RESERVE + Math.max(0, sanctumSutureLevel) * 80.0;
    }

    public static double routingRateMultiplier(int sanctumSutureLevel) {
        return 1.0 + Math.max(0, sanctumSutureLevel) * 0.12;
    }

    public static double bloodlineEfficiencyMultiplier(int bloodlineConcordLevel) {
        return 1.0 + Math.max(0, bloodlineConcordLevel) * 0.10;
    }

    public static double servitorThroughputMultiplier(int servitorTenderLevel) {
        return 1.0 + Math.max(0, servitorTenderLevel) * 0.15;
    }

    public static double servitorCommandRange(double baseRange, int servitorTenderLevel) {
        return baseRange + Math.max(0, servitorTenderLevel) * 4.0;
    }

    public static double ancestralSovereigntyMultiplier(int ancestralSovereigntyLevel) {
        return 1.0 + Math.max(0, ancestralSovereigntyLevel) * 0.06;
    }

    public static double calculateSafetyFloor(IBloodVolume playerVolume) {
        if (playerVolume == null) {
            return 0;
        }
        return playerVolume.getMaxBloodVolume() * DEFAULT_PLAYER_SAFETY_RATIO;
    }

    public static double transferFromGourdThenPlayer(IBloodVolume gourd, double gourdRate,
            IBloodVolume player, double playerSafetyFloor, IBloodVolume target, double requested, double maxTotalRate) {
        if (target == null || requested <= 0 || maxTotalRate <= 0) {
            return 0;
        }

        double remaining = Math.min(requested, maxTotalRate);
        remaining = Math.min(remaining, freeSpace(target));
        if (remaining <= 0) {
            return 0;
        }

        double fromGourd = transfer(gourd, target, remaining, gourdRate);
        remaining -= fromGourd;

        double playerAvailable = availableAboveFloor(player, playerSafetyFloor);
        double fromPlayer = transfer(player, target, Math.min(remaining, playerAvailable), remaining);

        return fromGourd + fromPlayer;
    }

    public static double transfer(IBloodVolume source, IBloodVolume target, double requested, double maxRate) {
        if (source == null || target == null || requested <= 0 || maxRate <= 0) {
            return 0;
        }
        double amount = Math.min(requested, maxRate);
        amount = Math.min(amount, source.getBloodVolume());
        amount = Math.min(amount, freeSpace(target));
        if (amount <= 0) {
            return 0;
        }
        source.drain(amount);
        target.fill(amount);
        return amount;
    }

    public static double availableAboveFloor(IBloodVolume source, double safetyFloor) {
        if (source == null) {
            return 0;
        }
        return Math.max(0, source.getBloodVolume() - Math.max(0, safetyFloor));
    }

    public static double freeSpace(IBloodVolume target) {
        if (target == null) {
            return 0;
        }
        return Math.max(0, target.getMaxBloodVolume() - target.getBloodVolume());
    }
}
