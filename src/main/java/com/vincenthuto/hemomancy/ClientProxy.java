package com.vincenthuto.hemomancy;

import com.vincenthuto.hemomancy.gui.guide.HemoTitlePage;
import com.vincenthuto.hemomancy.gui.living.MorphlingJarViewerScreen;
import com.vincenthuto.hemomancy.gui.manips.ChooseManipScreen;
import com.vincenthuto.hemomancy.gui.manips.ChooseVeinScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
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
	public void openJarGui() {
		Minecraft.getInstance().setScreen(new MorphlingJarViewerScreen());

	}

	@Override
	public void openStaffGui() {
		Minecraft.getInstance().setScreen(new MorphlingJarViewerScreen());
	}

	@Override
	public void openManipGui() {
		Minecraft.getInstance().setScreen(new ChooseManipScreen());
	}

	@Override
	public void openVeinGui() {
		Minecraft.getInstance().setScreen(new ChooseVeinScreen());
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
