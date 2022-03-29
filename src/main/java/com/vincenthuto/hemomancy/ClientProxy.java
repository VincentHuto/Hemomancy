package com.vincenthuto.hemomancy;

import com.vincenthuto.hemomancy.gui.guide.HemoTitlePage;
import com.vincenthuto.hemomancy.gui.manips.ScreenChooseManip;
import com.vincenthuto.hemomancy.gui.manips.ScreenChooseVein;
import com.vincenthuto.hemomancy.gui.mindrunes.ScreenRuneBinderViewer;
import com.vincenthuto.hemomancy.gui.mindrunes.ScreenRunePattern;
import com.vincenthuto.hemomancy.gui.morphlingjar.ScreenMorphlingJarViewer;
import com.vincenthuto.hemomancy.recipe.ChiselRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;

public class ClientProxy implements IProxy {

	@Override
	public void openGuideGui() {
		Minecraft.getInstance().setScreen(new HemoTitlePage());
	}

	@Override
	public void openBinderGui() {
		Minecraft.getInstance().setScreen(new ScreenRuneBinderViewer());
	}

	@Override
	public void openJarGui() {
		Minecraft.getInstance().setScreen(new ScreenMorphlingJarViewer());

	}

	@Override
	public void openStaffGui() {
		Minecraft.getInstance().setScreen(new ScreenMorphlingJarViewer());
	}

	@Override
	public void openManipGui() {
		Minecraft.getInstance().setScreen(new ScreenChooseManip());
	}

	@Override
	public void openVeinGui() {
		Minecraft.getInstance().setScreen(new ScreenChooseVein());
	}

	@Override
	public void openPatternGui(RegistryObject<Item> rune, ChiselRecipe recipe) {
		Minecraft.getInstance().setScreen(new ScreenRunePattern(rune, recipe,
				I18n.get(Hemomancy.MOD_ID + "." + rune.get().getRegistryName().getPath() + ".pattern.text")));
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
