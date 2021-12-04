package com.vincenthuto.hemomancy;

import com.vincenthuto.hemomancy.gui.guide.HemoTitlePage;
import com.vincenthuto.hemomancy.gui.manips.ScreenChooseManip;
import com.vincenthuto.hemomancy.gui.manips.ScreenChooseVein;
import com.vincenthuto.hemomancy.gui.mindrunes.ScreenRuneBinderViewer;
import com.vincenthuto.hemomancy.gui.morphlingjar.ScreenMorphlingJarViewer;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hutoslib.client.ClientUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientProxy implements IProxy {

	@Override
	public void openGuideGui() {
		Minecraft.getInstance().setScreen(new HemoTitlePage());
	}

	@Override
	public void openBinderGui() {
		Minecraft.getInstance().setScreen(
				new ScreenRuneBinderViewer(new ItemStack(ItemInit.rune_binder.get()), ClientUtils.getClientPlayer()));
	}

	@Override
	public void openJarGui() {
		Minecraft.getInstance().setScreen(
				new ScreenMorphlingJarViewer(new ItemStack(ItemInit.morphling_jar.get()), ClientUtils.getClientPlayer()));

	}

	@Override
	public void openStaffGui() {
		Minecraft.getInstance().setScreen(
				new ScreenMorphlingJarViewer(new ItemStack(ItemInit.morphling_jar.get()), ClientUtils.getClientPlayer()));
	}

	@Override
	public void openManipGui() {
		Minecraft.getInstance().setScreen(new ScreenChooseManip(ClientUtils.getClientPlayer()));
	}

	@Override
	public void openVeinGui() {
		Minecraft.getInstance().setScreen(new ScreenChooseVein(ClientUtils.getClientPlayer()));
	}
	
	@Override
	public void registerHandlers() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addListener(this::onModelRegister);
		modBus.addListener(this::onModelBake);
	}

	public static BakedModel bloodAbsorptionModel, bloodProjectionModel;

	public void onModelRegister(ModelRegistryEvent evt) {
		ForgeModelBakery.addSpecialModel(new ResourceLocation(Hemomancy.MOD_ID, "item/blood_absorption_texture"));
		ForgeModelBakery.addSpecialModel(new ResourceLocation(Hemomancy.MOD_ID, "item/blood_projection_texture"));

	}

	public void onModelBake(ModelBakeEvent evt) {
		bloodAbsorptionModel = evt.getModelRegistry()
				.get(new ResourceLocation(Hemomancy.MOD_ID, "item/blood_absorption_texture"));
		bloodProjectionModel = evt.getModelRegistry()
				.get(new ResourceLocation(Hemomancy.MOD_ID, "item/blood_projection_texture"));

	}

}
