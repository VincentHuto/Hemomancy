package com.vincenthuto.hemomancy.common.routing;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.BloodRoutingRules;

public final class BloodRoutingRulesTest {

    public static void main(String[] args) {
        drawsEquippedGourdBeforePlayer();
        respectsPlayerSafetyFloor();
        maintainsWorkingReserveInsteadOfFillingTarget();
    }

    private static void drawsEquippedGourdBeforePlayer() {
        IBloodVolume gourd = volume(100, 1000);
        IBloodVolume player = volume(5000, 5000);
        IBloodVolume target = volume(0, 4000);

        double transferred = BloodRoutingRules.transferFromGourdThenPlayer(
                gourd, 3.0,
                player, 2500.0,
                target, 10.0,
                10.0);

        assertEquals(10.0, transferred, "total transfer");
        assertEquals(97.0, gourd.getBloodVolume(), "gourd is drained first at its flow rate");
        assertEquals(4993.0, player.getBloodVolume(), "player provides only the remaining request");
        assertEquals(10.0, target.getBloodVolume(), "target receives requested blood");
    }

    private static void respectsPlayerSafetyFloor() {
        IBloodVolume gourd = volume(0, 1000);
        IBloodVolume player = volume(2510, 5000);
        IBloodVolume target = volume(0, 4000);

        double transferred = BloodRoutingRules.transferFromGourdThenPlayer(
                gourd, 5.0,
                player, 2500.0,
                target, 200.0,
                200.0);

        assertEquals(10.0, transferred, "only blood above the safety floor is routed");
        assertEquals(2500.0, player.getBloodVolume(), "player is left at the safety floor");
        assertEquals(10.0, target.getBloodVolume(), "target receives safe amount");
    }

    private static void maintainsWorkingReserveInsteadOfFillingTarget() {
        IBloodVolume target = volume(575, 4000);

        double request = BloodRoutingRules.calculateReserveRequest(target, 600.0, 1000.0);

        assertEquals(25.0, request, "request is capped at the working reserve gap");
    }

    private static IBloodVolume volume(double current, double max) {
        return new TestBloodVolume(current, max);
    }

    private static void assertEquals(double expected, double actual, String message) {
        if (Double.compare(expected, actual) != 0) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }

    private static final class TestBloodVolume implements IBloodVolume {
        private double blood;
        private double max;

        private TestBloodVolume(double blood, double max) {
            this.blood = blood;
            this.max = max;
        }

        @Override
        public void addBloodVolume(double points) {
            blood += points;
        }

        @Override
        public void addMaxBloodVolume(double points) {
            max += points;
        }

        @Override
        public boolean drain(double points) {
            if (wouldOverstrain(points)) {
                blood = 0;
                return false;
            }
            blood -= points;
            return true;
        }

        @Override
        public boolean drainFromSource(IBloodVolume src, double points) {
            return false;
        }

        @Override
        public boolean fill(double points) {
            if (wouldOverflow(points)) {
                blood = max;
                return false;
            }
            blood += points;
            return true;
        }

        @Override
        public boolean fillFromSource(IBloodVolume src, double points) {
            return false;
        }

        @Override
        public Bloodline getBloodLine() {
            return Bloodline.NOBLOODLINE;
        }

        @Override
        public double getBloodVolume() {
            return blood;
        }

        @Override
        public double getMaxBloodVolume() {
            return max;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            return blood <= 0;
        }

        @Override
        public boolean isFull() {
            return blood >= max;
        }

        @Override
        public void setActive(boolean set) {
        }

        @Override
        public void setBloodLine(Bloodline bloodLine) {
        }

        @Override
        public void setBloodVolume(double points) {
            blood = points;
        }

        @Override
        public void setMaxBloodVolume(double points) {
            max = points;
        }

        @Override
        public void subtractBloodVolume(double points) {
            blood -= points;
        }

        @Override
        public void subtractMaxBloodVolume(double points) {
            max -= points;
        }

        @Override
        public void toggleActive() {
        }

        @Override
        public boolean wouldOverflow(double points) {
            return blood + points > max;
        }

        @Override
        public boolean wouldOverstrain(double points) {
            return blood - points < 0;
        }

        @Override
        public void addDamage(double amount) {
        }

        @Override
        public void addBloodSpend(double amount) {
        }

        @Override
        public double consumeDebt() {
            return 0;
        }

        @Override
        public double getBloodDebt() {
            return 0;
        }

        @Override
        public void resetBloodDebt() {
        }

        @Override
        public boolean isTrickleEnabled() {
            return false;
        }

        @Override
        public void setTrickleEnabled(boolean enabled) {
        }

        @Override
        public double getTrickleRate() {
            return 0;
        }

        @Override
        public void setTrickleRate(double rate) {
        }

        @Override
        public boolean isAutoDrawEnabled() {
            return false;
        }

        @Override
        public void setAutoDrawEnabled(boolean enabled) {
        }

        @Override
        public double getAutoDrawThreshold() {
            return 0;
        }

        @Override
        public void setAutoDrawThreshold(double threshold) {
        }

        @Override
        public boolean isBloodRoutingOptInEnabled() {
            return false;
        }

        @Override
        public void setBloodRoutingOptInEnabled(boolean enabled) {
        }
    }
}
