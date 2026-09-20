package com.vincenthuto.hemomancy.common.antecedent;

/** One observer in one playback; a missed beat requires another complete replay. */
public final class AntecedentObservation {
    private int previous = -1, witnessed;
    public boolean tick(int elapsed, boolean samplePresent) {
        if (elapsed < 1120 || elapsed > 1300 || !samplePresent) { witnessed = 0; previous = -1; return false; }
        if (elapsed == 1120) witnessed = 1;
        else if (previous == elapsed - 1 && witnessed > 0) witnessed++;
        else witnessed = 0;
        previous = elapsed;
        return elapsed == 1300 && witnessed == 181;
    }
}
