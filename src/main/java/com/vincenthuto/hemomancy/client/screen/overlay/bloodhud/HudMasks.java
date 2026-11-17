package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

import com.vincenthuto.hemomancy.client.screen.overlay.BloodHudLayout.Reserve;

import java.util.function.Function;

/**
 * Everything the HUD reads from its textures: each part's silhouette ({@code vessel} is the back and degree frame
 * together; {@code vesselWindow} is the glass the blood fills), the binding's material tiles, the vial sprites, and
 * any hand-edited baked layout by {@link com.vincenthuto.hemomancy.client.screen.overlay.BloodHudLayout#fileName()}
 * (null where none exists, so the binding is grown from the tiles).
 */
public record HudMasks(HudMask vessel, HudMask gourd, HudMask horn, HudMask rib, HudMask jar, HudMask vesselWindow,
                       BindingArt binding, TemperVialArt vials, Function<String, BindingTile> bakedLayouts,
                       Function<String, BakedVials> bakedVials) {
    public HudMask reserve(Reserve reserve) {
        return switch (reserve) {
            case GOURD -> gourd;
            case HORN -> horn;
            case RIB -> rib;
            case NONE -> null;
        };
    }
}
