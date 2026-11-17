package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

/** One school's pool as the HUD shows it: what is held, against what cap, and the liquid's colour. */
public record TemperReading(int school, float held, float cap, float fraction, boolean dry, int colour) {
    public boolean full() {
        return cap > 0 && held >= cap - 0.001F;
    }
}
