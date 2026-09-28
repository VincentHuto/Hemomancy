package com.vincenthuto.hemomancy.client.screen.overlay;

import net.minecraft.util.Mth;

final class BloodVesselShape {
    private BloodVesselShape() {
    }

    static int halfWidth(int row, boolean outer) {
        float t = row / 91.0f;
        float width;
        if (t < 0.12f) {
            width = Mth.lerp(t / 0.12f, 13.0f, 16.0f);
        } else if (t < 0.86f) {
            width = 16.0f - Mth.sin((t - 0.12f) / 0.74f * Mth.PI) * 2.0f;
        } else {
            width = Mth.lerp((t - 0.86f) / 0.14f, 14.0f, 4.0f);
        }
        return Math.max(1, Math.round(width) - (outer ? 0 : 4));
    }
}
