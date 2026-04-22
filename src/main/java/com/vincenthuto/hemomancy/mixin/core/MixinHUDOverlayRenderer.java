package com.vincenthuto.hemomancy.mixin.core;

// TODO(MnA-compat): This mixin targets com.mna.gui.HUDOverlayRenderer to replace the
// hardcoded bar texture with the Harbingers faction texture. It cannot be compiled until
// Mana and Artifice publishes a NeoForge 1.21.1 build. The mixin config entry is kept
// with "required": false so the mixin framework ignores the missing target at runtime.
//
// Original implementation swapped the first ResourceLocation arg in renderManaBar's
// blit call to use HarbingerEventHandler.FACTION_HARBINGERS_ID when the player belongs
// to the Harbingers faction, rendering "textures/mna/harbingers_resource_bars.png".
//
// Re-enable by:
// 1. Adding MnA NeoForge JAR as compileOnly dep in build.gradle
// 2. Removing this comment and restoring the import + @Mixin + @ModifyArg body

// @Mixin placeholder — class must exist in JAR for hemomancy.mna.mixins.json reference.
public class MixinHUDOverlayRenderer {
}
