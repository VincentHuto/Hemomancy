package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

/**
 * One editable piece of the binding's art (a {@code textures/gui/blood_hud/binding_*.png} swatch) in its three
 * layers: as painted once the degree red has reached it, before it has, and at Apotheos. A pixel whose dormant
 * colour differs from its painted colour is red: degree holds it back and the heartbeat darkens it.
 */
public record BindingTile(int width, int height, int[] art, int[] dormant, int[] apotheos) {
    public BindingTile {
        int n = width * height;
        if (art.length != n || dormant.length != n || apotheos.length != n) {
            throw new IllegalArgumentException("binding tile layers differ in size");
        }
    }

    int index(int x, int y) {
        return Math.floorMod(y, height) * width + Math.floorMod(x, width);
    }

    boolean empty(int i) {
        return (art[i] >>> 24) == 0 && (dormant[i] >>> 24) == 0;
    }

    boolean red(int i) {
        return art[i] != dormant[i];
    }
}
