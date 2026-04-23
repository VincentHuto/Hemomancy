package com.vincenthuto.hemomancy.compat.mna;

import com.mna.api.events.CastingResourceGuiRegistrationEvent;
import com.mna.api.tools.RLoc;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.compat.mna.block.render.BrokenManaTrapazahedronRenderer;
import com.vincenthuto.hemomancy.compat.mna.entity.MnAPluginEntityInit;
import com.vincenthuto.hemomancy.compat.mna.entity.SanguilithModel;
import com.vincenthuto.hemomancy.compat.mna.entity.SanguilithRenderer;
import com.vincenthuto.hemomancy.compat.mna.faction.HarbingerEventHandler;
import com.vincenthuto.hemomancy.compat.mna.item.MnAPluginItemInit;

import com.vincenthuto.hemomancy.compat.mna.tile.MnAPluginBlockEntityInit;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class MnAPluginClientEvents {

	public static void onRegisterSpecialModels(ModelEvent.RegisterAdditional event) {
		event.register(Hemomancy.rloc("item/special/grimoire_harbinger_open"));
		event.register(Hemomancy.rloc("item/special/grimoire_harbinger_closed"));
		event.register(Hemomancy.rloc("item/faction_horn_harbingers"));
		event.register(Hemomancy.rloc("block/broken_mana_trapazahedron"));
		event.register(BrokenManaTrapazahedronRenderer.crystal);
		event.register(BrokenManaTrapazahedronRenderer.runes);

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

	public static void onClientSetupEvent(FMLClientSetupEvent event) {
		BlockEntityRenderers.register(MnAPluginBlockEntityInit.broken_mana_trapazahedron.get(), BrokenManaTrapazahedronRenderer::new);
		// Generate composited spell icons (base icon + bloody border) on the render thread
		event.enqueueWork(HemoSpellIconCompositor::generateAll);
	}

	/**
	 * Advances the animated bloody border on hemomancy spell icons each tick.
	 * Must be registered on the FORGE bus (not MOD bus).
	 */
	public static void onClientTick(ClientTickEvent.Pre event) {
		HemoSpellIconCompositor.tick();
	}

}