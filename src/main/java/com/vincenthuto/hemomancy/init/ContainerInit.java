package com.vincenthuto.hemomancy.init;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.container.ContainerChiselStation;
import com.vincenthuto.hemomancy.container.ContainerLivingStaff;
import com.vincenthuto.hemomancy.container.ContainerLivingSyringe;
import com.vincenthuto.hemomancy.container.ContainerMorphlingJar;
import com.vincenthuto.hemomancy.container.ContainerRuneBinder;
import com.vincenthuto.hemomancy.container.ContainerVisceralRecaller;
import com.vincenthuto.hemomancy.container.PlayerExpandedContainer;
import com.vincenthuto.hemomancy.recipe.CopyMorphlingJarDataRecipe;
import com.vincenthuto.hemomancy.recipe.CopyRuneBinderDataRecipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.fmllegacy.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ContainerInit {
	public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS,
			Hemomancy.MOD_ID);
	public static List<MenuType<?>> RUNECONTAINER = new ArrayList<>();

	public static final RegistryObject<MenuType<ContainerChiselStation>> runic_chisel_station = CONTAINERS
			.register("runic_chisel_station", () -> IForgeContainerType.create(ContainerChiselStation::new));

	public static final RegistryObject<MenuType<ContainerVisceralRecaller>> visceral_recaller = CONTAINERS
			.register("visceral_recaller", () -> IForgeContainerType.create(ContainerVisceralRecaller::new));

	public static final RegistryObject<MenuType<ContainerRuneBinder>> rune_binder = CONTAINERS.register("rune_binder",
			() -> IForgeContainerType.create(ContainerRuneBinder::new));

	public static final RegistryObject<MenuType<ContainerMorphlingJar>> morphling_jar = CONTAINERS
			.register("morphling_jar", () -> IForgeContainerType.create(ContainerMorphlingJar::new));

	public static final RegistryObject<MenuType<ContainerLivingStaff>> living_staff = CONTAINERS
			.register("living_staff", () -> IForgeContainerType.create(ContainerLivingStaff::new));
	
	public static final RegistryObject<MenuType<ContainerLivingSyringe>> living_syringe = CONTAINERS
			.register("living_syringe", () -> IForgeContainerType.create(ContainerLivingSyringe::new));

	@ObjectHolder("hemomancy:playerrunes")
	public static MenuType<PlayerExpandedContainer> playerrunes = createRuneContainer("playerrunes",
			(id, inv, data) -> new PlayerExpandedContainer(id, inv, !inv.player.level.isClientSide));

	private static <T extends AbstractContainerMenu> MenuType<T> createRuneContainer(String name,
			IContainerFactory<T> factory) {
		MenuType<T> containerType = IForgeContainerType.create(factory);
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
