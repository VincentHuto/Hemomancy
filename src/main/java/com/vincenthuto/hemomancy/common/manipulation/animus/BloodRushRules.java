package com.vincenthuto.hemomancy.common.manipulation.animus;

/**
 * Presentation timeline for Blood Rush, free of Minecraft types so it can be tested on the JVM. The heart pumps blood
 * down into the legs, the body launches inside a streaming sheath of it, and the sheath frays off behind. The server
 * dash and effect never wait on these ticks.
 */
public final class BloodRushRules {
    /** Client cue length; the server sends the cue once per cast. */
    public static final int VISUAL_TICKS = 20;
    /** Heartbeats push blood from the chest down into the legs. */
    public static final int SURGE_END = 3;
    /** The kick-off: the sheath streams out behind at full length. */
    public static final int LAUNCH_END = 8;
    /** Number of wake strands peeling back from the shoulders and hips. */
    public static final int WAKE_STRANDS = 5;

    private BloodRushRules() {
    }

    /** 0 → 1 over the surge. */
    public static double surge(double age) {
        return smooth(age / SURGE_END);
    }

    /** Wake length factor: grows through the launch, then holds while the sheath frays. */
    public static double wake(double age) {
        if (age < SURGE_END) return 0;
        double t = (age - SURGE_END) / (LAUNCH_END - SURGE_END);
        return t >= 1 ? 1 : 1 - Math.pow(1 - t, 3);
    }

    /** 0 → 1 as the sheath frays apart after the launch. */
    public static double fray(double age) {
        return smooth((age - LAUNCH_END) / (VISUAL_TICKS - LAUNCH_END));
    }

    /**
     * Height fraction (1 chest, 0 feet) of heartbeat ring {@code ring} during the surge, or a negative value once it
     * has reached the feet. Rings leave the chest one beat apart.
     */
    public static double beatHeight(double age, int ring) {
        double t = (age - ring * 0.8) / 2.4;
        if (t < 0) return 1;
        return t > 1 ? -1 : 1 - t;
    }

    static double smooth(double t) {
        double c = Math.max(0, Math.min(1, t));
        return c * c * (3 - 2 * c);
    }
}
