package com.vincenthuto.hemomancy.compat.mna;

import com.mna.api.events.CastingResourceGuiRegistrationEvent;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.compat.mna.entity.MnAPluginEntityInit;
import com.vincenthuto.hemomancy.compat.mna.entity.SanguilithModel;
import com.vincenthuto.hemomancy.compat.mna.entity.SanguilithRenderer;
import com.vincenthuto.hemomancy.compat.mna.faction.HarbingerEventHandler;
import com.vincenthuto.hemomancy.compat.mna.faction.HarbingersManaGui;
import com.vincenthuto.hemomancy.compat.mna.item.MnAPluginItemInit;

import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MnAPluginClientEvents {
	public static void onCastingResourceRegistrationEventClient(CastingResourceGuiRegistrationEvent event) {
		event.getRegistry().registerResourceGui(HarbingerEventHandler.HARBINGERS_MANA, new HarbingersManaGui());
	}

	public static void onRegisterSpecialModels(ModelEvent.RegisterAdditional event) {
		event.register(Hemomancy.rloc("item/special/grimoire_harbinger_open"));
		event.register(Hemomancy.rloc("item/special/grimoire_harbinger_closed"));
		event.register(Hemomancy.rloc("item/faction_horn_harbingers"));

	}

	public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
		event.register((stack, layer) -> layer > 0 ? -1 : ((DyeableLeatherItem) stack.getItem()).getColor(stack),
				new ItemLike[] { (ItemLike) MnAPluginItemInit.living_thread_boots.get(),
						(ItemLike) MnAPluginItemInit.living_thread_robes.get(),
						(ItemLike) MnAPluginItemInit.living_thread_leggings.get(),
						(ItemLike) MnAPluginItemInit.living_thread_hood.get() });
	}

	public static void registerModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(SanguilithModel.sanguilith, SanguilithModel::createBodyLayer);

	}

	public static void renderEntities(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(MnAPluginEntityInit.sanguilith.get(), SanguilithRenderer::new);

	}

}