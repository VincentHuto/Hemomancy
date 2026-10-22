package com.vincenthuto.hemomancy.client.registry;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.item.*;
import com.vincenthuto.hemomancy.client.screen.item.living.LivingSyringeScreen;
import com.vincenthuto.hemomancy.client.screen.item.living.MorphlingJarScreen;
import com.vincenthuto.hemomancy.client.screen.tile.crafting.*;
import com.vincenthuto.hemomancy.client.screen.tile.crafting.scar.ScarBinderScreen;
import com.vincenthuto.hemomancy.client.screen.tile.crafting.scar.ScarStationScreen;
import com.vincenthuto.hemomancy.client.screen.tile.functional.HarbingerEquipmentScreen;
import com.vincenthuto.hemomancy.client.screen.tile.functional.MasonsEffigyScreen;
import com.vincenthuto.hemomancy.client.screen.tile.functional.MnemonicReliquaryScreen;
import com.vincenthuto.hemomancy.client.screen.tile.functional.PhlebotomistsCabinetScreen;
import com.vincenthuto.hemomancy.client.screen.tile.functional.SporeImplantScreen;
import com.vincenthuto.hemomancy.common.init.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class MenuScreenRegistrar {

    private MenuScreenRegistrar() {
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ContainerInit.gourd_charm_inventory.get(), HarbingerEquipmentScreen::new);
        event.register(ContainerInit.fungal_implantation.get(), SporeImplantScreen::new);
        event.register(ContainerInit.mason_effigy.get(), MasonsEffigyScreen::new);
        event.register(ContainerInit.vial_centrifuge.get(), VialCentrifugeScreen::new);
        event.register(ContainerInit.enzymatic_scriptorium.get(), EnzymaticScriptoriumScreen::new);
        event.register(ContainerInit.resonant_forge.get(), ResonantForgeScreen::new);
        event.register(ContainerInit.morphling_jar.get(), MorphlingJarScreen::new);
        event.register(ContainerInit.living_syringe.get(), LivingSyringeScreen::new);
        event.register(ContainerInit.ghastly_alembic.get(), GhastlyAlembicScreen::new);
        event.register(ContainerInit.pallid_retort.get(), PallidRetortScreen::new);
        event.register(ContainerInit.stillwater_condenser.get(), StillwaterCondenserScreen::new);
        event.register(ContainerInit.scar_station.get(), ScarStationScreen::new);
        event.register(ContainerInit.scar_binder.get(), ScarBinderScreen::new);
        event.register(ContainerInit.mnemonic_folio.get(), MnemonicFolioScreen::new);
        event.register(ContainerInit.vascular_view.get(), VascularViewScreen::new);
        event.register(ContainerInit.tendency_view.get(), TendencyViewScreen::new);
        event.register(ContainerInit.scrying_diagnostics.get(), ScryingDiagnosticsScreen::new);
        event.register(ContainerInit.morphling_incubator.get(), MorphlingIncubatorScreen::new);
        event.register(ContainerInit.mycelial_crucible.get(), MycelialCrucibleScreen::new);
        event.register(ContainerInit.mycelial_lantern.get(), MycelialLanternScreen::new);
        event.register(ContainerInit.structure_spawner.get(), StructureSpawnerScreen::new);
        event.register(ContainerInit.puppeteers_spindle.get(), PuppeteersSpindleScreen::new);
        event.register(ContainerInit.mnemonic_reliquary.get(), MnemonicReliquaryScreen::new);
        event.register(ContainerInit.clairaudiograph.get(), com.vincenthuto.hemomancy.client.screen.tile.functional.ClairaudiographScreen::new);
        event.register(ContainerInit.phlebotomists_cabinet.get(), PhlebotomistsCabinetScreen::new);
        event.register(ContainerInit.phlebotomists_field_case.get(), PhlebotomistsCabinetScreen::new);
    }
}
