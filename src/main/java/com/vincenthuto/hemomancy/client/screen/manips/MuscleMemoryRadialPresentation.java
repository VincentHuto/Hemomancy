package com.vincenthuto.hemomancy.client.screen.manips;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumBloodFlow;

public final class MuscleMemoryRadialPresentation {
    public enum CrackSeverity { NONE, FINE, DENSE, DEAD }

    private MuscleMemoryRadialPresentation() {}

    public static State resolve(int reserveTicks, boolean active, boolean armed,
            EnumBloodFlow flow, long gameTime) {
        int tint = reserveTicks > 0 ? 0xFFFFFFFF : 0xFF777777;
        int background = 0x3F000000;
        if (armed) {
            background = (gameTime / 4L) % 2L == 0L ? 0xDFB51A1A : 0xFFEF5050;
        } else if (active) {
            background = (gameTime / 10L) % 2L == 0L ? 0x9F7A0D0D : 0xCFB02020;
        }
        return new State(tint, background, cracks(flow));
    }

    public static CrackSeverity cracks(EnumBloodFlow flow) {
        if (flow == null) return CrackSeverity.NONE;
        return switch (flow) {
            case VARICOSE -> CrackSeverity.FINE;
            case ClOTTED -> CrackSeverity.DENSE;
            case DEAD -> CrackSeverity.DEAD;
            default -> CrackSeverity.NONE;
        };
    }

    public record State(int iconTint, int backgroundColor, CrackSeverity cracks) {}
}
