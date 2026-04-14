package com.vincenthuto.hemomancy.mixin.core;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.capabilities.resource.ICastingResourceGuiProvider;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.magic.resources.CastingResourceGuiRegistry;
import com.mna.capabilities.playerdata.progression.PlayerProgression;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.gui.HUDOverlayRenderer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.compat.mna.faction.HarbingerEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * MnA's HUDOverlayRenderer.renderManaBar hardcodes Hud.BARS as the texture
 * for the frame blit, ignoring ICastingResourceGuiProvider.getTexture().
 * This mixin modifies the first argument (ResourceLocation) of the blit call
 * to use the provider's texture instead, allowing custom factions like
 * Harbingers to render their own bar frame.
 */
@Mixin(value = HUDOverlayRenderer.class, remap = false)
public class MixinHUDOverlayRenderer {

    @ModifyArg(
        method = "renderManaBar",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V",
            ordinal = 0,
            remap = true
        ),
        index = 0
    )
    private ResourceLocation hemomancy$useProviderTexture(ResourceLocation originalTexture) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            IPlayerMagic magic = player.getCapability(PlayerMagicProvider.MAGIC).orElse(null);
            IPlayerProgression progression = player.getCapability(PlayerProgressionProvider.PROGRESSION).orElse(null);
            if (magic.isMagicUnlocked()) {
                ICastingResourceGuiProvider provider = CastingResourceGuiRegistry.Instance
                        .getGuiProvider(magic.getCastingResource().getRegistryName());
                if (provider != null) {
                    if(progression.getAlliedFaction() != null && progression.getAlliedFaction() .is(HarbingerEventHandler.FACTION_HARBINGERS_ID)) {
                        return Hemomancy.rloc("textures/mna/harbingers_resource_bars.png");
                    }
                }
            }
        }
        return originalTexture;
    }
}
