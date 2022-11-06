package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.container.CharmGourdMenu;
import com.vincenthuto.hemomancy.container.JuiceinatorMenu;
import com.vincenthuto.hemomancy.container.LivingStaffMenu;
import com.vincenthuto.hemomancy.container.LivingSyringeMenu;
import com.vincenthuto.hemomancy.container.MorphlingJarMenu;
import com.vincenthuto.hemomancy.container.VialCentrifugeMenu;
import com.vincenthuto.hemomancy.container.VisceralRecallerMenu;

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

	public static final RegistryObject<MenuType<VisceralRecallerMenu>> visceral_recaller = CONTAINERS
			.register("visceral_recaller", () -> IForgeMenuType.create(VisceralRecallerMenu::new));

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

	public static final  RegistryObject<MenuType<CharmGourdMenu>> gourd_charm_inventory =  CONTAINERS.register("gourd_charm_inventory", () -> IForgeMenuType.create(CharmGourdMenu::new));



}
