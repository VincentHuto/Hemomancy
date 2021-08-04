package com.huto.hemomancy;

import com.huto.hemomancy.gui.manips.GuiChooseManip;
import com.huto.hemomancy.gui.mindrunes.GuiRuneBinderViewer;
import com.huto.hemomancy.gui.morphlingjar.GuiMorphlingJarViewer;
import com.huto.hemomancy.init.ItemInit;
import com.hutoslib.client.ClientUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientProxy implements IProxy {

	@Override
	public void openBinderGui() {
		Minecraft.getInstance().setScreen(new GuiRuneBinderViewer(new ItemStack(ItemInit.rune_binder.get()),
				ClientUtils.getClientPlayer()));
	}

	@Override
	public void openJarGui() {
		Minecraft.getInstance().setScreen(
				new GuiMorphlingJarViewer(new ItemStack(ItemInit.morphling_jar.get()), ClientUtils.getClientPlayer()));

	}

	@Override
	public void openStaffGui() {
		Minecraft.getInstance().setScreen(
				new GuiMorphlingJarViewer(new ItemStack(ItemInit.morphling_jar.get()), ClientUtils.getClientPlayer()));
	}

	@Override
	public void openManipGui() {
		Minecraft.getInstance().setScreen(new GuiChooseManip(ClientUtils.getClientPlayer()));
	}

	@Override
	public void registerHandlers() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addListener(this::onModelRegister);
		modBus.addListener(this::onModelBake);
	}

	public static BakedModel bloodAbsorptionModel, bloodProjectionModel;

	public void onModelRegister(ModelRegistryEvent evt) {
		ModelLoader.addSpecialModel(new ResourceLocation(Hemomancy.MOD_ID, "item/blood_absorption_texture"));
		ModelLoader.addSpecialModel(new ResourceLocation(Hemomancy.MOD_ID, "item/blood_projection_texture"));

	}

	public void onModelBake(ModelBakeEvent evt) {
		bloodAbsorptionModel = evt.getModelRegistry()
				.get(new ResourceLocation(Hemomancy.MOD_ID, "item/blood_absorption_texture"));
		bloodProjectionModel = evt.getModelRegistry()
				.get(new ResourceLocation(Hemomancy.MOD_ID, "item/blood_projection_texture"));

	}

}
