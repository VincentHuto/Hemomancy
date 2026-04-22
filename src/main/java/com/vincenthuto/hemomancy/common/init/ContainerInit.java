package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.menu.*;

import com.vincenthuto.hemomancy.common.menu.tile.crafting.*;
import com.vincenthuto.hemomancy.common.menu.tile.functional.MnemonicReliquaryMenu;
import com.vincenthuto.hemomancy.common.menu.tile.functional.SporeImplantMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IForgeMenuType;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ContainerInit {
	public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES,
			Hemomancy.MOD_ID);

	public static final DeferredHolder<MenuType<?>, MenuType<ScarStationMenu>> scar_station = CONTAINERS
			.register("scar_station", () -> IForgeMenuType.create(ScarStationMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<ScarBinderInventoryMenu>> scar_binder = CONTAINERS
			.register("scar_binder", () -> IForgeMenuType.create(ScarBinderInventoryMenu::new));


	public static final DeferredHolder<MenuType<?>, MenuType<VialCentrifugeMenu>> vial_centrifuge = CONTAINERS
			.register("vial_centrifuge", () -> IForgeMenuType.create(VialCentrifugeMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<GhastlyAlembicMenu>> ghastly_alembic = CONTAINERS.register("ghastly_alembic",
			() -> IForgeMenuType.create(GhastlyAlembicMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<PallidRetortMenu>> pallid_retort = CONTAINERS.register("pallid_retort",
			() -> IForgeMenuType.create(PallidRetortMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<MorphlingJarMenu>> morphling_jar = CONTAINERS.register("morphling_jar",
			() -> IForgeMenuType.create(MorphlingJarMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<LivingStaffMenu>> living_staff = CONTAINERS.register("living_staff",
			() -> IForgeMenuType.create(LivingStaffMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<LivingSyringeMenu>> living_syringe = CONTAINERS
			.register("living_syringe", () -> IForgeMenuType.create(LivingSyringeMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<CharmGourdMenu>> gourd_charm_inventory = CONTAINERS
			.register("gourd_charm_inventory", () -> IForgeMenuType.create(CharmGourdMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<SporeImplantMenu>> fungal_implantation = CONTAINERS
			.register("fungal_implantation", () -> IForgeMenuType.create(SporeImplantMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<VascularViewMenu>> vascular_view =
			CONTAINERS.register("vascular_view", () -> IForgeMenuType.create(VascularViewMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<TendencyViewMenu>> tendency_view =
			CONTAINERS.register("tendency_view", () -> IForgeMenuType.create(TendencyViewMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<MorphlingIncubatorMenu>> morphling_incubator = CONTAINERS
			.register("morphling_incubator", () -> IForgeMenuType.create(MorphlingIncubatorMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<StructureSpawnerMenu>> structure_spawner = CONTAINERS
			.register("structure_spawner", () -> IForgeMenuType.create(StructureSpawnerMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<MnemonicReliquaryMenu>> mnemonic_reliquary = CONTAINERS
			.register("mnemonic_reliquary", () -> IForgeMenuType.create(MnemonicReliquaryMenu::new));

}
