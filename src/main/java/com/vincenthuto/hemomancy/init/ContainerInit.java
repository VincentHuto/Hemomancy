package com.vincenthuto.hemomancy.init;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.container.MenuJuiceinator;
import com.vincenthuto.hemomancy.container.MenuLivingStaff;
import com.vincenthuto.hemomancy.container.MenuLivingSyringe;
import com.vincenthuto.hemomancy.container.MenuMorphlingJar;
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

	public static final RegistryObject<MenuType<MenuVisceralRecaller>> visceral_recaller = CONTAINERS
			.register("visceral_recaller", () -> IForgeMenuType.create(MenuVisceralRecaller::new));

	public static final RegistryObject<MenuType<MenuJuiceinator>> juiceinator = CONTAINERS.register("juiceinator",
			() -> IForgeMenuType.create(MenuJuiceinator::new));

	public static final RegistryObject<MenuType<MenuMorphlingJar>> morphling_jar = CONTAINERS.register("morphling_jar",
			() -> IForgeMenuType.create(MenuMorphlingJar::new));

	public static final RegistryObject<MenuType<MenuLivingStaff>> living_staff = CONTAINERS.register("living_staff",
			() -> IForgeMenuType.create(MenuLivingStaff::new));

	public static final RegistryObject<MenuType<MenuLivingSyringe>> living_syringe = CONTAINERS
			.register("living_syringe", () -> IForgeMenuType.create(MenuLivingSyringe::new));
	
}


