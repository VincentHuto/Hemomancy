package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;

public final class BloodRoutingRules {

    public static final double DEFAULT_WORKING_RESERVE = 600.0;
    public static final double DEFAULT_PLAYER_SAFETY_RATIO = 0.5;
    /** How near a sutured machine kin must stand to feed it for an offline owner (CN-9, NQ-01-16). */
    public static final double STAND_IN_RANGE = 16.0;

    private BloodRoutingRules() {
    }

    /**
     * How much fresh blood a sutured buffer is short of its working reserve. A block tank's reserve is measured against
     * its whole size, clot included, so a buffer whose clot fills its room still asks to be revived (LP-4).
     */
    public static double calculateReserveRequest(IBloodVolume target, double workingReserve, double maxRequest) {
        if (target == null || maxRequest <= 0 || workingReserve <= 0) {
            return 0;
        }
        double size = target instanceof BlockBloodVolume block ? block.capacity() : target.getMaxBloodVolume();
        double reserveCap = Math.min(workingReserve, size);
        double missingReserve = Math.max(0, reserveCap - target.getBloodVolume());
        return Math.min(maxRequest, missingReserve);
    }

    /** Whether a member of the owner's bloodline may stand in: alive, at the third Degree, blood awake, near enough. */
    public static boolean mayStandIn(boolean alive, boolean thirdDegree, boolean bloodActive, double distanceSqr) {
        return alive && thirdDegree && bloodActive && distanceSqr <= STAND_IN_RANGE * STAND_IN_RANGE;
    }

    public static double workingReserve(int faneSutureLevel) {
        return DEFAULT_WORKING_RESERVE + Math.max(0, faneSutureLevel) * 80.0;
    }

    public static double routingRateMultiplier(int faneSutureLevel) {
        return 1.0 + Math.max(0, faneSutureLevel) * 0.12;
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
