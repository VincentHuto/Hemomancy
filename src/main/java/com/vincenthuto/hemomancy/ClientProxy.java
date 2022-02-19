package com.vincenthuto.hemomancy;

import com.vincenthuto.hemomancy.event.ClientTickHandler;
import com.vincenthuto.hemomancy.gui.guide.HemoTitlePage;
import com.vincenthuto.hemomancy.gui.manips.ScreenChooseManip;
import com.vincenthuto.hemomancy.gui.manips.ScreenChooseVein;
import com.vincenthuto.hemomancy.gui.mindrunes.ScreenRuneBinderViewer;
import com.vincenthuto.hemomancy.gui.morphlingjar.ScreenMorphlingJarViewer;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hutoslib.client.HLClientUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientProxy implements IProxy {

	public void lightningFX(Vec3 vectorStart, Vec3 vectorEnd, float ticksPerMeter, long seed, int colorOuter,
			int colorInner) {
		BoltRenderer.INSTANCE.add(new BoltParticleOptions(vectorStart, vectorEnd).size(0.08F),
				ClientTickHandler.partialTicks);
	}

	@Override
	public void openGui(Screen gui) {
		Minecraft.getInstance().setScreen(gui);
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
