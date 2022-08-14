package com.vincenthuto.hemomancy.init;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.container.JuiceinatorMenu;
import com.vincenthuto.hemomancy.container.LivingStaffMenu;
import com.vincenthuto.hemomancy.container.LivingSyringeMenu;
import com.vincenthuto.hemomancy.container.MenuRunes;
import com.vincenthuto.hemomancy.container.MorphlingJarMenu;
import com.vincenthuto.hemomancy.container.VisceralRecallerMenu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ContainerInit {
	public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS,
			Hemomancy.MOD_ID);
	public static List<MenuType<?>> RUNECONTAINER = new ArrayList<>();

	public static final RegistryObject<MenuType<VisceralRecallerMenu>> visceral_recaller = CONTAINERS
			.register("visceral_recaller", () -> IForgeMenuType.create(VisceralRecallerMenu::new));

	public static final RegistryObject<MenuType<JuiceinatorMenu>> juiceinator = CONTAINERS.register("juiceinator",
			() -> IForgeMenuType.create(JuiceinatorMenu::new));

	public static final RegistryObject<MenuType<MorphlingJarMenu>> morphling_jar = CONTAINERS.register("morphling_jar",
			() -> IForgeMenuType.create(MorphlingJarMenu::new));

	public static final RegistryObject<MenuType<LivingStaffMenu>> living_staff = CONTAINERS.register("living_staff",
			() -> IForgeMenuType.create(LivingStaffMenu::new));

	public static final RegistryObject<MenuType<LivingSyringeMenu>> living_syringe = CONTAINERS
			.register("living_syringe", () -> IForgeMenuType.create(LivingSyringeMenu::new));

	@ObjectHolder("hemomancy:playerrunes")
	public static MenuType<MenuRunes> playerrunes = createRuneContainer("playerrunes",
			(id, inv, data) -> new MenuRunes(id, inv, !inv.player.level.isClientSide));

	private static <T extends AbstractContainerMenu> MenuType<T> createRuneContainer(String name,
			IContainerFactory<T> factory) {
		MenuType<T> containerType = IForgeMenuType.create(factory);
		containerType.setRegistryName(new ResourceLocation(Hemomancy.MOD_ID, name));
		RUNECONTAINER.add(containerType);
		return containerType;
	}

	@SubscribeEvent
	public static void onContainerRegister(final RegistryEvent.Register<MenuType<?>> event) {
		event.getRegistry().registerAll(RUNECONTAINER.toArray(new MenuType[0]));
	}
}
