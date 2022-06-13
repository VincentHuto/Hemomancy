package com.vincenthuto.hemomancy.init;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.container.MenuChiselStation;
import com.vincenthuto.hemomancy.container.MenuEarthlyTransfuser;
import com.vincenthuto.hemomancy.container.MenuJuiceinator;
import com.vincenthuto.hemomancy.container.MenuLivingStaff;
import com.vincenthuto.hemomancy.container.MenuLivingSyringe;
import com.vincenthuto.hemomancy.container.MenuMorphlingJar;
import com.vincenthuto.hemomancy.container.MenuRuneBinderInventory;
import com.vincenthuto.hemomancy.container.MenuRuneBinderViewer;
import com.vincenthuto.hemomancy.container.MenuRunes;
import com.vincenthuto.hemomancy.container.MenuVisceralRecaller;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ContainerInit {
	public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS,
			Hemomancy.MOD_ID);
	public static List<MenuType<?>> RUNECONTAINER = new ArrayList<>();

	public static final RegistryObject<MenuType<MenuChiselStation>> runic_chisel_station = CONTAINERS
			.register("runic_chisel_station", () -> IForgeMenuType.create(MenuChiselStation::new));

	public static final RegistryObject<MenuType<MenuVisceralRecaller>> visceral_recaller = CONTAINERS
			.register("visceral_recaller", () -> IForgeMenuType.create(MenuVisceralRecaller::new));

	public static final RegistryObject<MenuType<MenuJuiceinator>> juiceinator = CONTAINERS.register("juiceinator",
			() -> IForgeMenuType.create(MenuJuiceinator::new));

	public static final RegistryObject<MenuType<MenuEarthlyTransfuser>> earthly_transfuser = CONTAINERS
			.register("earthly_transfuser", () -> IForgeMenuType.create(MenuEarthlyTransfuser::new));

	public static final RegistryObject<MenuType<MenuRuneBinderInventory>> rune_binder = CONTAINERS
			.register("rune_binder", () -> IForgeMenuType.create(MenuRuneBinderInventory::new));

	public static final RegistryObject<MenuType<MenuMorphlingJar>> morphling_jar = CONTAINERS.register("morphling_jar",
			() -> IForgeMenuType.create(MenuMorphlingJar::new));

	public static final RegistryObject<MenuType<MenuLivingStaff>> living_staff = CONTAINERS.register("living_staff",
			() -> IForgeMenuType.create(MenuLivingStaff::new));

	public static final RegistryObject<MenuType<MenuLivingSyringe>> living_syringe = CONTAINERS
			.register("living_syringe", () -> IForgeMenuType.create(MenuLivingSyringe::new));

	public static final RegistryObject<MenuType<MenuRuneBinderViewer>> rune_binder_container = CONTAINERS
			.register("rune_binder_container", () -> IForgeMenuType.create(MenuRuneBinderViewer::new));

	public static final RegistryObject<MenuType<MenuRunes>> playerrunes = CONTAINERS.register("playerrunes",
			() -> IForgeMenuType.create(MenuRunes::new));

}
