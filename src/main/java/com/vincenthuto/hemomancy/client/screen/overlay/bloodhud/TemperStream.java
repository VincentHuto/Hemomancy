package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

import com.vincenthuto.hemomancy.client.screen.overlay.BloodHudLayout.VialSeat;

import java.util.ArrayList;
import java.util.List;

/** The thin constant trickle from a Temper vial into the blood vessel. */
public final class TemperStream {
    private static final double TILT = Math.toRadians(50);

    private TemperStream() {
    }

    /** Only a vial with usable Temper pours, and only into a shown vessel. */
    public static boolean flows(boolean vessel, float held, float cap, boolean dry) {
        return vessel && cap > 0 && held > 0 && !dry;
    }

    /** An arc out of the mouth to the glass, then a fall down the inner wall to just above the blood. */
    public static int[][] path(VialSeat seat, int surfaceY) {
        double ax = seat.pointsRight() ? -Math.sin(TILT) : Math.sin(TILT), ay = -Math.cos(TILT);
        double x0 = seat.mouthX() - ax, y0 = seat.mouthY() - ay;
        double x1 = seat.mouthX() - ax * 6, y1 = seat.mouthY() - ay * 6 + 1;
        List<int[]> out = new ArrayList<>();
        for (int step = 0; step <= 40; step++) {
            double s = step / 40.0, q = 1 - s;
            add(out, (int) Math.round(q * q * x0 + 2 * q * s * x1 + s * s * seat.entryX()),
                    (int) Math.round(q * q * y0 + 2 * q * s * y1 + s * s * seat.entryY()));
        }
        for (int y = seat.entryY() + 1; y < surfaceY; y++) add(out, seat.entryX(), y);
        return out.toArray(int[][]::new);
    }

    private static void add(List<int[]> out, int x, int y) {
        if (!out.isEmpty()) {
            int[] last = out.get(out.size() - 1);
            if (last[0] == x && last[1] == y) return;
        }
        out.add(new int[]{x, y});
    }
}
