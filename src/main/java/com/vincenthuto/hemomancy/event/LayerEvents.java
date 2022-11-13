package com.vincenthuto.hemomancy.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.render.layer.player.BloodAvatarLayer;
import com.vincenthuto.hemomancy.render.layer.player.BloodGourdLayer;
import com.vincenthuto.hemomancy.render.layer.player.CellHandLayer;
import com.vincenthuto.hemomancy.render.layer.player.VascCharmLayer;
import com.vincenthuto.hutoslib.common.container.BannerSlot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class LayerEvents {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <T extends LivingEntity, M extends HumanoidModel<T>, R extends LivingEntityRenderer<T, M>> void addLayerToEntity(
			EntityRenderersEvent.AddLayers event, EntityType<? extends T> entityType) {
		R renderer = event.getRenderer(entityType);
		if (renderer != null) {
			renderer.addLayer(new BloodGourdLayer(renderer));
			renderer.addLayer(new BloodAvatarLayer(renderer));
			renderer.addLayer(new CellHandLayer(renderer));
			renderer.addLayer(new VascCharmLayer<>(renderer));

		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void addLayerToPlayerSkin(EntityRenderersEvent.AddLayers event, String skinName) {
		EntityRenderer<? extends Player> render = event.getSkin(skinName);
		if (render instanceof LivingEntityRenderer livingRenderer) {
			livingRenderer.addLayer(new BloodGourdLayer<>(livingRenderer));
			livingRenderer.addLayer(new BloodAvatarLayer(livingRenderer));
			livingRenderer.addLayer(new CellHandLayer(livingRenderer));
			livingRenderer.addLayer(new VascCharmLayer(livingRenderer));

		}
	}

	@SubscribeEvent
	public static void constructLayers(EntityRenderersEvent.AddLayers event) {

		addLayerToEntity(event, EntityType.ARMOR_STAND);
		addLayerToEntity(event, EntityType.ZOMBIE);
		addLayerToEntity(event, EntityType.SKELETON);
		addLayerToEntity(event, EntityType.HUSK);
		addLayerToEntity(event, EntityType.DROWNED);
		addLayerToEntity(event, EntityType.STRAY);
		addLayerToPlayerSkin(event, "default");
		addLayerToPlayerSkin(event, "slim");

	}

	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void onStitch(TextureStitchEvent.Pre event) {
		if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
			event.addSprite(Hemomancy.rloc("entity/royal_guard_shield_base"));
			event.addSprite(Hemomancy.rloc("entity/barbed_shield/model_barbed_shield"));
			event.addSprite(Hemomancy.rloc("entity/chitinite_shield/model_chitinite_shield"));

		}
		if (event.getAtlas().location() == InventoryMenu.BLOCK_ATLAS) {
			event.addSprite(BannerSlot.SLOT_BACKGROUND);
		}
	}

	// For when the horn breaks it shows the custom animation
	public static void playHornAnimation() {
		LocalPlayer ent = Minecraft.getInstance().player;
		Minecraft.getInstance().particleEngine.createTrackingEmitter(ent, ParticleTypes.SOUL, 30);
		Minecraft.getInstance().gameRenderer.displayItemActivation(new ItemStack(ItemInit.curved_horn.get()));
		Minecraft.getInstance().level.playLocalSound(ent.getX(), ent.getY(), ent.getZ(), SoundEvents.TOTEM_USE,
				ent.getSoundSource(), 1F, 0.6F, false);
	}

}
