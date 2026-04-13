package com.vincenthuto.hemomancy.common.item.armor;

import java.util.List;
import java.util.function.Consumer;

import com.vincenthuto.hemomancy.client.model.armor.MarrowCrownModel;
import com.vincenthuto.hemomancy.client.render.item.MarrowCrownItemRenderer;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class MarrowCrownArmorItem extends ArmorItem {

	public MarrowCrownArmorItem(ArmorMaterial materialIn, Type slot) {
		super(materialIn, slot, new Item.Properties().rarity(Rarity.EPIC));
	}
	
	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(Component.translatable("tooltip.hemomancy.marrow_crown_bonus").withStyle(ChatFormatting.GOLD));
	}


	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,
					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				if (itemStack.getItem() == ItemInit.marrow_crown.get()) {
					return MarrowCrownModel.helmet.get();
				}

				return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new MarrowCrownItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels());
			}
		});

	}

	@Override
	public void onArmorTick(ItemStack stack, Level world, Player player) {
//		if (world.isClientSide) {
//			for (int i = 0; i < 1; ++i) {
//				if (i % 2 == 0) {
//					world.addParticle(DustParticleOptions.REDSTONE, player.getRandomX(0.5D), player.getY(),
//							player.getRandomZ(0.5D), (world.random.nextDouble() - 0.5D) * 2.0D,
//							-world.random.nextDouble(), (world.random.nextDouble() - 0.5D) * 2.0D);
//				}
//			}
//		}

		super.onArmorTick(stack, world, player);
	}

}
