package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

/** Where each of Annetta's pools lies above the floor. Pure, so it can be tested without Minecraft. */
final class AnnettaPoolLayers {
    private AnnettaPoolLayers() {}

    /**
     * Overlapping pools must never share a plane, or their surfaces z-fight: each lies on one of eight layers 4 mm
     * apart, picked by entity id so pools spilled one after another always differ.
     */
    static float layer(int id) {
        return 0.008F + Math.floorMod(id * 5, 8) * 0.004F;
    }
}
