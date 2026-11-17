package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

/**
 * The binding's material: a strip stamped along the band (columns run along it, rows from its inner edge out), a
 * lattice tiled over graft fills and trapped sky, and a collar centred on the band at intervals.
 */
public record BindingArt(BindingTile band, BindingTile lattice, BindingTile collar) {
}
