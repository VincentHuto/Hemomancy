package com.vincenthuto.hemomancy.common.init;

import com.mna.gui.containers.block.ContainerOcculus;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.menu.*;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ContainerInit {
	public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES,
			Hemomancy.MOD_ID);

	public static final RegistryObject<MenuType<ChiselStationMenu>> runic_chisel_station = CONTAINERS
			.register("runic_chisel_station", () -> IForgeMenuType.create(ChiselStationMenu::new));

	public static final RegistryObject<MenuType<RuneBinderInventoryMenu>> rune_binder = CONTAINERS
			.register("rune_binder", () -> IForgeMenuType.create(RuneBinderInventoryMenu::new));


	public static final RegistryObject<MenuType<VialCentrifugeMenu>> vial_centrifuge = CONTAINERS
			.register("vial_centrifuge", () -> IForgeMenuType.create(VialCentrifugeMenu::new));

	public static final RegistryObject<MenuType<JuiceinatorMenu>> juiceinator = CONTAINERS.register("juiceinator",
			() -> IForgeMenuType.create(JuiceinatorMenu::new));

	public static final RegistryObject<MenuType<MorphlingJarMenu>> morphling_jar = CONTAINERS.register("morphling_jar",
			() -> IForgeMenuType.create(MorphlingJarMenu::new));

	public static final RegistryObject<MenuType<LivingStaffMenu>> living_staff = CONTAINERS.register("living_staff",
			() -> IForgeMenuType.create(LivingStaffMenu::new));

	public static final RegistryObject<MenuType<LivingSyringeMenu>> living_syringe = CONTAINERS
			.register("living_syringe", () -> IForgeMenuType.create(LivingSyringeMenu::new));

	public static final RegistryObject<MenuType<CharmGourdMenu>> gourd_charm_inventory = CONTAINERS
			.register("gourd_charm_inventory", () -> IForgeMenuType.create(CharmGourdMenu::new));

	public static final RegistryObject<MenuType<SporeImplantMenu>> fungal_implantation = CONTAINERS
			.register("fungal_implantation", () -> IForgeMenuType.create(SporeImplantMenu::new));

	public static final RegistryObject<MenuType<VascularViewMenu>> vascular_view =
			CONTAINERS.register("vascular_view", () -> IForgeMenuType.create(VascularViewMenu::new));

	public static final RegistryObject<MenuType<TendencyViewMenu>> tendency_view =
			CONTAINERS.register("tendency_view", () -> IForgeMenuType.create(TendencyViewMenu::new));

	public static final RegistryObject<MenuType<MorphlingIncubatorMenu>> morphling_incubator = CONTAINERS
			.register("morphling_incubator", () -> IForgeMenuType.create(MorphlingIncubatorMenu::new));

	public static final RegistryObject<MenuType<StructureSpawnerMenu>> structure_spawner = CONTAINERS
			.register("structure_spawner", () -> IForgeMenuType.create(StructureSpawnerMenu::new));

	public static final RegistryObject<MenuType<MnemonicReliquaryMenu>> mnemonic_reliquary = CONTAINERS
			.register("mnemonic_reliquary", () -> IForgeMenuType.create(MnemonicReliquaryMenu::new));

}
