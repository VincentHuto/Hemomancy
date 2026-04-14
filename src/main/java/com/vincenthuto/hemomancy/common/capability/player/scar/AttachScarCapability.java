package com.vincenthuto.hemomancy.common.capability.player.scar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Hemomancy.MOD_ID)
public class AttachScarCapability {

	private static ResourceLocation cap = Hemomancy.rloc("scar_cap");

	@SubscribeEvent
	public static void attachCaps(AttachCapabilitiesEvent<ItemStack> event) {
		ItemStack stack = event.getObject();
		if (stack.getItem() instanceof IScar && stack.getItem() != Items.AIR) {
			event.addCapability(cap, new ICapabilityProvider() {
				private final LazyOptional<IScar> opt = LazyOptional.of(() -> (IScar) stack.getItem());

				@Nonnull
				@Override
				public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
					return ScarsCapabilities.ITEM_SCAR.orEmpty(cap, opt);
				}
			});
		}
	}
}
