package com.vincenthuto.hemomancy;

import com.vincenthuto.hemomancy.gui.guide.HemoTitlePage;
import com.vincenthuto.hemomancy.gui.living.MorphlingJarViewerScreen;
import com.vincenthuto.hemomancy.gui.manips.ChooseManipScreen;
import com.vincenthuto.hemomancy.gui.manips.ChooseVeinScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class ClientProxy implements IProxy {

	public static BakedModel bloodAbsorptionModel, bloodProjectionModel;

	@SubscribeEvent
	public static void modelRegisterEvent(ModelEvent.RegisterAdditional event) {
		event.register(new ResourceLocation(Hemomancy.MOD_ID, "item/blood_absorption_texture"));
		event.register(new ResourceLocation(Hemomancy.MOD_ID, "item/blood_projection_texture"));

	}

	@SubscribeEvent
	public static void onModelBake(BakingCompleted evt) {
		bloodAbsorptionModel = evt.getModels()
				.get(new ResourceLocation(Hemomancy.MOD_ID, "item/blood_absorption_texture"));
		bloodProjectionModel = evt.getModels()
				.get(new ResourceLocation(Hemomancy.MOD_ID, "item/blood_projection_texture"));

	}

	@Override
	public void openGuideGui() {
		Minecraft.getInstance().setScreen(new HemoTitlePage());
	}

	@Override
	public void openJarGui() {
		Minecraft.getInstance().setScreen(new MorphlingJarViewerScreen());

	}

	@Override
	public void openManipGui() {
		Minecraft.getInstance().setScreen(new ChooseManipScreen());
	}

	@Override
	public void openStaffGui() {
		Minecraft.getInstance().setScreen(new MorphlingJarViewerScreen());
	}
	@Override
	public void openVeinGui() {
		Minecraft.getInstance().setScreen(new ChooseVeinScreen());
	}

}
