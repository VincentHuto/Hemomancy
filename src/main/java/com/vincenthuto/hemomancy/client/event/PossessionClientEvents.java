package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.SurrogateForm;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A possessing Silentium is seen as the facsimile their will walks in: the player render is swapped for that
 * facsimile's, posed from the player each frame. The possessed spider form climbs walls on its own client.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class PossessionClientEvents {
	private static final Map<Integer, LivingEntity> BODIES = new HashMap<>();

	private PossessionClientEvents() {}

	private static SurrogateForm drawnForm(Player player) {
		SurrogateForm form = HemoCapabilityAccess.possessionForm(player);
		return form == null || form.facsimileId().isEmpty() ? null : form;
	}

	@SubscribeEvent
	public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
		Player player = event.getEntity();
		SurrogateForm form = drawnForm(player);
		if (form == null) return;
		LivingEntity body = body(player, form);
		if (body == null) return;
		event.setCanceled(true);
		float partial = event.getPartialTick();
		body.setPos(player.getX(), player.getY(), player.getZ());
		body.xo = player.xo;
		body.yo = player.yo;
		body.zo = player.zo;
		body.setYRot(player.getYRot());
		body.yRotO = player.yRotO;
		body.setXRot(player.getXRot());
		body.xRotO = player.xRotO;
		body.yBodyRot = player.yBodyRot;
		body.yBodyRotO = player.yBodyRotO;
		body.yHeadRot = player.yHeadRot;
		body.yHeadRotO = player.yHeadRotO;
		body.tickCount = player.tickCount;
		body.setOnGround(player.onGround());
		body.setDeltaMovement(player.getDeltaMovement());
		var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(body);
		renderer.render(body, Mth.lerp(partial, player.yRotO, player.getYRot()), partial, event.getPoseStack(),
				event.getMultiBufferSource(), event.getPackedLight());
	}

	private static LivingEntity body(Player player, SurrogateForm form) {
		ResourceLocation id = ResourceLocation.parse(form.facsimileId());
		LivingEntity body = BODIES.get(player.getId());
		if (body != null && BuiltInRegistries.ENTITY_TYPE.getKey(body.getType()).equals(id)
				&& body.level() == player.level()) return body;
		Entity created = BuiltInRegistries.ENTITY_TYPE.get(id).create(player.level());
		if (!(created instanceof LivingEntity living)) return null;
		living.setUUID(player.getUUID());
		BODIES.put(player.getId(), living);
		return living;
	}

	@SubscribeEvent
	public static void onRenderHand(RenderHandEvent event) {
		Player player = Minecraft.getInstance().player;
		if (player != null && drawnForm(player) != null) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) {
			BODIES.clear();
			return;
		}
		for (Iterator<Map.Entry<Integer, LivingEntity>> it = BODIES.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<Integer, LivingEntity> entry = it.next();
			if (!(minecraft.level.getEntity(entry.getKey()) instanceof Player player) || drawnForm(player) == null) {
				it.remove();
				continue;
			}
			entry.getValue().walkAnimation.update(player.walkAnimation.speed(), 1.0F);
		}
		Player local = minecraft.player;
		if (local != null && HemoCapabilityAccess.possessionForm(local) == SurrogateForm.SPIDER
				&& local.horizontalCollision && !local.isShiftKeyDown()) {
			Vec3 motion = local.getDeltaMovement();
			local.setDeltaMovement(motion.x, 0.2, motion.z);
		}
	}

	public static void clear() {
		BODIES.clear();
	}
}
