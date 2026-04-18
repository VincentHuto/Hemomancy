package com.vincenthuto.hemomancy.common.item.armor;

import java.util.function.Consumer;

import com.vincenthuto.hemomancy.client.model.armor.HemolymphopodaHeadArmorModel;
import com.vincenthuto.hemomancy.client.render.item.HemolymphopodaHeadpieceItemRenderer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class HemolymphopodaHeadpieceArmorItem extends ArmorItem {

	public HemolymphopodaHeadpieceArmorItem(ArmorMaterial materialIn, Type slot) {
		super(materialIn, slot, new Item.Properties().stacksTo(1));
	}

	@Override
	public String getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity,
			net.minecraft.world.entity.EquipmentSlot slot, String type) {
		return "hemomancy:textures/models/armor/empty.png";
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			private BlockEntityWithoutLevelRenderer renderer;

			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,
					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				// Return an empty model to suppress vanilla helmet rendering.
				// HemolymphopodaHeadpieceLayer handles the actual crab rendering.
				return HemolymphopodaHeadArmorModel.HELMET.get();
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				if (renderer == null) {
					net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
					renderer = new HemolymphopodaHeadpieceItemRenderer(
							minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
				}
				return renderer;
			}
		});
	}
}
