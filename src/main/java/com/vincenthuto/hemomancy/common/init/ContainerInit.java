package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.menu.*;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.*;
import com.vincenthuto.hemomancy.common.menu.tile.functional.MasonsEffigyMenu;
import com.vincenthuto.hemomancy.common.menu.tile.functional.MnemonicReliquaryMenu;
import com.vincenthuto.hemomancy.common.menu.tile.functional.SporeImplantMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ContainerInit {
	public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU,
			Hemomancy.MOD_ID);

	public static final DeferredHolder<MenuType<?>, MenuType<ScarStationMenu>> scar_station = CONTAINERS
			.register("scar_station", () -> IMenuTypeExtension.create(ScarStationMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<ScarBinderInventoryMenu>> scar_binder = CONTAINERS
			.register("scar_binder", () -> IMenuTypeExtension.create(ScarBinderInventoryMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<MnemonicFolioMenu>> mnemonic_folio = CONTAINERS
			.register("mnemonic_folio", () -> IMenuTypeExtension.create(MnemonicFolioMenu::new));


	public static final DeferredHolder<MenuType<?>, MenuType<VialCentrifugeMenu>> vial_centrifuge = CONTAINERS
			.register("vial_centrifuge", () -> IMenuTypeExtension.create(VialCentrifugeMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<GhastlyAlembicMenu>> ghastly_alembic = CONTAINERS.register("ghastly_alembic",
			() -> IMenuTypeExtension.create(GhastlyAlembicMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<PallidRetortMenu>> pallid_retort = CONTAINERS.register("pallid_retort",
			() -> IMenuTypeExtension.create(PallidRetortMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<StillwaterCondenserMenu>> stillwater_condenser = CONTAINERS.register(
			"stillwater_condenser", () -> IMenuTypeExtension.create(StillwaterCondenserMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<MorphlingJarMenu>> morphling_jar = CONTAINERS.register("morphling_jar",
			() -> IMenuTypeExtension.create(MorphlingJarMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<LivingStaffMenu>> living_staff = CONTAINERS.register("living_staff",
			() -> IMenuTypeExtension.create(LivingStaffMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<LivingSyringeMenu>> living_syringe = CONTAINERS
			.register("living_syringe", () -> IMenuTypeExtension.create(LivingSyringeMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<HarbingerEquipmentMenu>> gourd_charm_inventory = CONTAINERS
			.register("gourd_charm_inventory", () -> IMenuTypeExtension.create(HarbingerEquipmentMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<SporeImplantMenu>> fungal_implantation = CONTAINERS
			.register("fungal_implantation", () -> IMenuTypeExtension.create(SporeImplantMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<MasonsEffigyMenu>> mason_effigy = CONTAINERS
			.register("mason_effigy", () -> IMenuTypeExtension.create(MasonsEffigyMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<VascularViewMenu>> vascular_view =
			CONTAINERS.register("vascular_view", () -> IMenuTypeExtension.create(VascularViewMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<TendencyViewMenu>> tendency_view =
			CONTAINERS.register("tendency_view", () -> IMenuTypeExtension.create(TendencyViewMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<ScryingDiagnosticsMenu>> scrying_diagnostics =
			CONTAINERS.register("scrying_diagnostics", () -> IMenuTypeExtension.create(ScryingDiagnosticsMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<MorphlingIncubatorMenu>> morphling_incubator = CONTAINERS
			.register("morphling_incubator", () -> IMenuTypeExtension.create(MorphlingIncubatorMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<StructureSpawnerMenu>> structure_spawner = CONTAINERS
			.register("structure_spawner", () -> IMenuTypeExtension.create(StructureSpawnerMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<PuppeteersSpindleMenu>> puppeteers_spindle = CONTAINERS
			.register("puppeteers_spindle", () -> IMenuTypeExtension.create(PuppeteersSpindleMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<MnemonicReliquaryMenu>> mnemonic_reliquary = CONTAINERS
			.register("mnemonic_reliquary", () -> IMenuTypeExtension.create(MnemonicReliquaryMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<MycelialCrucibleMenu>> mycelial_crucible = CONTAINERS
			.register("mycelial_crucible", () -> IMenuTypeExtension.create(MycelialCrucibleMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<MycelialLanternMenu>> mycelial_lantern = CONTAINERS
			.register("mycelial_lantern", () -> IMenuTypeExtension.create(MycelialLanternMenu::new));

}
