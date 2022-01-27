package com.vincenthuto.hemomancy.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.render.layer.LayerBloodAvatar;
import com.vincenthuto.hemomancy.render.layer.LayerBloodGourd;
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

	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void onStitch(TextureStitchEvent.Pre event) {
		if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
			event.addSprite(new ResourceLocation(Hemomancy.MOD_ID, "entity/royal_guard_shield_base"));
			event.addSprite(new ResourceLocation(Hemomancy.MOD_ID, "entity/spiked_shield/model_spiked_shield"));

		}
		if (event.getAtlas().location() == InventoryMenu.BLOCK_ATLAS) {
			event.addSprite(BannerSlot.SLOT_BACKGROUND);
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void addLayerToPlayerSkin(EntityRenderersEvent.AddLayers event, String skinName) {
		EntityRenderer<? extends Player> render = event.getSkin(skinName);
		if (render instanceof LivingEntityRenderer livingRenderer) {
			livingRenderer.addLayer(new LayerBloodGourd<>(livingRenderer));
			livingRenderer.addLayer(new LayerBloodAvatar(livingRenderer));

		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <T extends LivingEntity, M extends HumanoidModel<T>, R extends LivingEntityRenderer<T, M>> void addLayerToEntity(
			EntityRenderersEvent.AddLayers event, EntityType<? extends T> entityType) {
		R renderer = event.getRenderer(entityType);
		if (renderer != null) {
			renderer.addLayer(new LayerBloodGourd(renderer));
			renderer.addLayer(new LayerBloodAvatar(renderer));
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