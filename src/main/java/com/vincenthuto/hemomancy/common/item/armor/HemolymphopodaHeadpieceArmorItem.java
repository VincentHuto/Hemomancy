package com.vincenthuto.hemomancy.common.item.armor;

import java.util.function.Consumer;

import com.vincenthuto.hemomancy.client.render.item.HemolymphopodaHeadpieceItemRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class HemolymphopodaHeadpieceArmorItem extends ArmorItem {

	public HemolymphopodaHeadpieceArmorItem(ArmorMaterial materialIn, Type slot) {
		super(materialIn, slot, new Item.Properties().stacksTo(1));
	}

	@Override
	public String getArmorTexture(net.minecraft.world.item.ItemStack stack, net.minecraft.world.entity.Entity entity,
			net.minecraft.world.entity.EquipmentSlot slot, String type) {
		return "hemomancy:textures/models/armor/empty.png";
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		Minecraft minecraft = Minecraft.getInstance();
		consumer.accept(new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new HemolymphopodaHeadpieceItemRenderer(minecraft.getBlockEntityRenderDispatcher(),
						minecraft.getEntityModels());
			}
		});
	}
}
