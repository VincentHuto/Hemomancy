package com.vincenthuto.hemomancy.compat.mna.faction;

import com.mna.Registries;
import com.mna.api.faction.IFaction;
import com.mna.capabilities.playerdata.magic.resources.CastingResourceGuiRegistry;
import com.mna.capabilities.playerdata.magic.resources.CastingResourceRegistry;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;


public class HarbingerEventHandler {

    public static final IFaction HARBINGERS_FACTION = new HarbingersFaction();

    public static final ResourceLocation FACTION_HARBINGERS_ID = Hemomancy.rloc("harbingers_faction");
    public static final ResourceLocation HARBINGERS_MANA = Hemomancy.rloc("harbingers_mana");
    public static final ResourceLocation HARBINGERS_HUD_TEXTURE = Hemomancy.rloc("textures/mna/harbingers_resource_bars.png");



    @SubscribeEvent
    public static void registerFactions(RegisterEvent event) {
        event.register(Registries.Factions.get().key(), (helper) -> {
            helper.register(Hemomancy.rloc("harbingers_faction"), HARBINGERS_FACTION);
        });
    }


    @SubscribeEvent
    public static void registerResources(FMLCommonSetupEvent event) {
        CastingResourceRegistry.Instance.register(HARBINGERS_MANA, HarbingersMana.class);
    }

    public static class HarbingerClientEventHandler {
        @SubscribeEvent
        public static void registerResourceGuis(FMLClientSetupEvent event) {
            CastingResourceGuiRegistry.Instance
                    .registerResourceGui(HARBINGERS_MANA, new HarbingersMana.HarbingersManaGui());
        }
    }

}
