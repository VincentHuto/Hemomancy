package com.vincenthuto.hemomancy.compat.mna.faction;

import com.mna.Registries;
import com.mna.api.events.CastingResourceRegistrationEvent;
import com.mna.api.faction.BaseFaction;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.RegisterEvent;

public class HarbingerEventHandler {

	public static final ResourceLocation FACTION_HARBINGERS_ID = Hemomancy.rloc("harbingers_faction");
	public static final ResourceLocation HARBINGERS_MANA = Hemomancy.rloc("harbingers_mana");
	public static final ResourceLocation HARBINGERS_HUD_TEXTURE = Hemomancy
			.rloc("textures/mna/gui_harbingers_manabars.png");
	public static final BaseFaction HARBINGERS_FACTION = new HarbingersFaction();

	// Faction
	public static void registerFactions(RegisterEvent event) {
		event.register(Registries.Factions.get().getRegistryKey(), (helper) -> {
			helper.register(FACTION_HARBINGERS_ID, HARBINGERS_FACTION);
		});
	}

	public static void onCastingResourceRegistrationEvent(CastingResourceRegistrationEvent event) {
//		event.getRegistry().register(HARBINGERS_MANA, HarbingersMana.class);
	}

	public static void loadCompleteEventHandler(FMLLoadCompleteEvent event) {
//		FactionRaidRegistry.registerSoldier(HARBINGERS_FACTION,
//				(EntityType<? extends IFactionEnemy<? extends Mob>>) ((EntityType) EntityInit.fungling.get()),
//				new HashMap<Integer, Integer>() {
//					{
//						this.put(25, 0);
//						this.put(35, 1);
//						this.put(45, 2);
//					}
//
//				});
//		FactionRaidRegistry.registerSoldier(HARBINGERS_FACTION,
//				(EntityType<? extends IFactionEnemy<? extends Mob>>) ((EntityType) EntityInit.abhorent_thought.get()),
//				new HashMap<Integer, Integer>() {
//					{
//						this.put(30, 0);
//						this.put(65, 1);
//						this.put(100, 2);
//					}
//				});
	}

}
