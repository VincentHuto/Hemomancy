package com.vincenthuto.hemomancy.compat.mna.faction;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.events.CastingResourceGuiRegistrationEvent;
import com.mna.capabilities.playerdata.magic.resources.CastingResourceGuiRegistry;
import com.mna.capabilities.playerdata.magic.resources.CastingResourceRegistry;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class TestModEventBus {
	
	public static final ResourceLocation FACTION_HARBINGERS_ID = Hemomancy.rloc("harbingers_faction");
	public static final ResourceLocation HARBINGERS_MANA = Hemomancy.rloc("harbingers_mana");
	public static final ResourceLocation HARBINGERS_HUD_TEXTURE = Hemomancy
			.rloc("textures/mna/gui_harbingers_manabars.png");
	
	public static void onCastingResourceGuiRegistrationEvent(CastingResourceGuiRegistrationEvent event) {
	}
	
	@SubscribeEvent
	public static void clientSetup(final FMLClientSetupEvent event) {
		CastingResourceGuiRegistry.Instance.registerResourceGui(HARBINGERS_MANA, new HarbingersManaGui());

	}
	@SubscribeEvent
	public static void commonSetup(final FMLCommonSetupEvent event) {
		CastingResourceRegistry.Instance.register(HARBINGERS_MANA, HarbingersMana.class);

	}


}
