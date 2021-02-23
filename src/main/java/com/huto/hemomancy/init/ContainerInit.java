package com.huto.hemomancy.init;

import java.util.ArrayList;
import java.util.List;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.container.ContainerChiselStation;
import com.huto.hemomancy.container.ContainerLivingStaff;
import com.huto.hemomancy.container.ContainerMorphlingJar;
import com.huto.hemomancy.container.ContainerRuneBinder;
import com.huto.hemomancy.container.PlayerExpandedContainer;
import com.huto.hemomancy.recipes.CopyMorphlingJarDataRecipe;
import com.huto.hemomancy.recipes.CopyRuneBinderDataRecipe;

import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ContainerInit {
	public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister
			.create(ForgeRegistries.CONTAINERS, Hemomancy.MOD_ID);
	public static List<ContainerType<?>> RUNECONTAINER = new ArrayList<>();

	public static final RegistryObject<ContainerType<ContainerChiselStation>> runic_chisel_station = CONTAINERS
			.register("runic_chisel_station", () -> IForgeContainerType.create(ContainerChiselStation::new));

	@ObjectHolder("hemomancy:playerrunes")
	public static ContainerType<PlayerExpandedContainer> playerrunes = createRuneContainer("playerrunes",
			(id, inv, data) -> new PlayerExpandedContainer(id, inv, !inv.player.world.isRemote));

	private static <T extends Container> ContainerType<T> createRuneContainer(String name,
			IContainerFactory<T> factory) {
		ContainerType<T> containerType = IForgeContainerType.create(factory);
		containerType.setRegistryName(new ResourceLocation(Hemomancy.MOD_ID, name));
		RUNECONTAINER.add(containerType);
		return containerType;
	}

	@SubscribeEvent
	public static void onContainerRegister(final RegistryEvent.Register<ContainerType<?>> event) {
		event.getRegistry().registerAll(RUNECONTAINER.toArray(new ContainerType[0]));
		event.getRegistry().register(ContainerRuneBinder.type);
		event.getRegistry().register(ContainerMorphlingJar.type);
		event.getRegistry().register(ContainerLivingStaff.type);

	}

	@SubscribeEvent
	public static void onRecipeRegistry(final RegistryEvent.Register<IRecipeSerializer<?>> event) {
		event.getRegistry().register(new CopyRuneBinderDataRecipe.Serializer()
				.setRegistryName(new ResourceLocation(Hemomancy.MOD_ID, "backpack_upgrade")));
		event.getRegistry().register(new CopyMorphlingJarDataRecipe.Serializer()
				.setRegistryName(new ResourceLocation(Hemomancy.MOD_ID, "morphling_jar_upgrade")));
	}
}
