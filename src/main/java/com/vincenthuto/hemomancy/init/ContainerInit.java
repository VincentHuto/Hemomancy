package com.vincenthuto.hemomancy.init;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.container.MenuChiselStation;
import com.vincenthuto.hemomancy.container.MenuJuiceinator;
import com.vincenthuto.hemomancy.container.MenuLivingStaff;
import com.vincenthuto.hemomancy.container.MenuLivingSyringe;
import com.vincenthuto.hemomancy.container.MenuMorphlingJar;
import com.vincenthuto.hemomancy.container.MenuRuneBinder;
import com.vincenthuto.hemomancy.container.MenuRunes;
import com.vincenthuto.hemomancy.container.MenuVisceralRecaller;
import com.vincenthuto.hemomancy.recipe.CopyMorphlingJarDataRecipe;
import com.vincenthuto.hemomancy.recipe.CopyRuneBinderDataRecipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
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

	public static final RegistryObject<MenuType<MenuChiselStation>> runic_chisel_station = CONTAINERS
			.register("runic_chisel_station", () -> IForgeMenuType.create(MenuChiselStation::new));

	public static final RegistryObject<MenuType<MenuVisceralRecaller>> visceral_recaller = CONTAINERS
			.register("visceral_recaller", () -> IForgeMenuType.create(MenuVisceralRecaller::new));

	public static final RegistryObject<MenuType<MenuJuiceinator>> juiceinator = CONTAINERS.register("juiceinator",
			() -> IForgeMenuType.create(MenuJuiceinator::new));

	public static final RegistryObject<MenuType<MenuRuneBinder>> rune_binder = CONTAINERS.register("rune_binder",
			() -> IForgeMenuType.create(MenuRuneBinder::new));

	public static final RegistryObject<MenuType<MenuMorphlingJar>> morphling_jar = CONTAINERS.register("morphling_jar",
			() -> IForgeMenuType.create(MenuMorphlingJar::new));

	public static final RegistryObject<MenuType<MenuLivingStaff>> living_staff = CONTAINERS.register("living_staff",
			() -> IForgeMenuType.create(MenuLivingStaff::new));

	public static final RegistryObject<MenuType<MenuLivingSyringe>> living_syringe = CONTAINERS
			.register("living_syringe", () -> IForgeMenuType.create(MenuLivingSyringe::new));

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

	@SubscribeEvent
	public static void onRecipeRegistry(final RegistryEvent.Register<RecipeSerializer<?>> event) {
		event.getRegistry().register(new CopyRuneBinderDataRecipe.Serializer()
				.setRegistryName(new ResourceLocation(Hemomancy.MOD_ID, "backpack_upgrade")));
		event.getRegistry().register(new CopyMorphlingJarDataRecipe.Serializer()
				.setRegistryName(new ResourceLocation(Hemomancy.MOD_ID, "morphling_jar_upgrade")));
	}
}
