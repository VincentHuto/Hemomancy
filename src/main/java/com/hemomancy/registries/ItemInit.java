package com.hemomancy.registries;

import com.hemomancy.Hemomancy;
import com.hemomancy.Hemomancy.HemomancyItemGroup;

import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class ItemInit {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Hemomancy.MOD_ID);
	public static final DeferredRegister<Item> BASEITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Hemomancy.MOD_ID);
	
	//Base Items
	public static final RegistryObject<Item> grey_ingot = BASEITEMS.register("grey_ingot",
			() -> new Item(new Item.Properties().group(HemomancyItemGroup.instance)));
	
}
