package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.Possession;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionAction;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.SurrogateForm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A possessing Silentium is seen as the facsimile their will walks in. Each possessed player gets a client-only
 * stand-in body that follows them and is ticked every client tick, so the mob's own animation runs: the bat hangs,
 * the golem swings, the horse rears, the blaze smoulders. It carries the player's name. On the possessing client the
 * spider form climbs walls while Climb is on, and Still holds the body by dropping its movement input.
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
		place(player, body);
		var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(body);
		renderer.render(body, Mth.lerp(partial, player.yRotO, player.getYRot()), partial, event.getPoseStack(),
				event.getMultiBufferSource(), event.getPackedLight());
	}

	/** The body the possession wheel turns in its centre: the facsimile, or the player themself for the armor stand. */
	public static LivingEntity wheelBody(Player player) {
		SurrogateForm form = drawnForm(player);
		return form == null ? player : body(player, form);
	}

	private static LivingEntity body(Player player, SurrogateForm form) {
		ResourceLocation id = ResourceLocation.parse(form.facsimileId());
		LivingEntity body = BODIES.get(player.getId());
		if (body != null && BuiltInRegistries.ENTITY_TYPE.getKey(body.getType()).equals(id)
				&& body.level() == player.level()) return body;
		Entity created = BuiltInRegistries.ENTITY_TYPE.get(id).create(player.level());
		if (!(created instanceof LivingEntity living)) return null;
		living.setUUID(player.getUUID());
		// Never in the level and never solid: it must not push its own possessor about.
		living.noPhysics = true;
		place(player, living);
		BODIES.put(player.getId(), living);
		return living;
	}

	/** Puts the body exactly where the player is, facing where they face. */
	private static void place(Player player, LivingEntity body) {
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
	}

	/** One client tick of the stand-in: mirror the player, let the mob's own tick animate it, then pin it back. */
	private static void step(Player player, LivingEntity body) {
		place(player, body);
		body.setOnGround(player.onGround());
		body.setShiftKeyDown(player.isShiftKeyDown());
		body.setSprinting(player.isSprinting());
		body.setInvisible(player.isInvisible());
		body.hurtTime = player.hurtTime;
		body.hurtDuration = player.hurtDuration;
		body.swinging = player.swinging;
		body.swingTime = player.swingTime;
		body.oAttackAnim = player.oAttackAnim;
		body.attackAnim = player.attackAnim;
		// The body wears the will's strain: Concentration is its health, so a drained golem shows its cracks.
		Possession possession = HemoCapabilityAccess.getPossession(player);
		if (possession.max() > 0f) {
			body.setHealth(body.getMaxHealth() * Mth.clamp(possession.concentration() / possession.max(), 0.05f, 1f));
		}
		body.setCustomName(player.getDisplayName());
		body.setCustomNameVisible(nameShown(player));
		boolean swingStarts = player.swinging && player.swingTime == 0;
		Vec3 motion = player.getDeltaMovement();

		if (body instanceof Bat bat) bat.setResting(hangs(player));
		if (body instanceof IronGolem golem && swingStarts) golem.handleEntityEvent((byte) 4);
		if (body instanceof AbstractHorse horse) horse.setStanding(player.swinging || !player.onGround() && motion.y > 0.3);

		// Ticked standing still, so the walk cycle it works out comes from the player's own step (x - xo) alone.
		body.setDeltaMovement(Vec3.ZERO);
		body.tickCount = player.tickCount - 1;
		body.tick();
		place(player, body);
		body.setOnGround(player.onGround());
		body.setDeltaMovement(motion);
		body.tickCount = player.tickCount;
	}

	/** A bat body hangs when it is pressed up under something it can grip and has stopped moving. */
	private static boolean hangs(Player player) {
		BlockPos above = BlockPos.containing(player.getX(), player.getBoundingBox().maxY + 0.1, player.getZ());
		Vec3 motion = player.getDeltaMovement();
		return player.level().getBlockState(above).isFaceSturdy(player.level(), above, Direction.DOWN)
				&& motion.horizontalDistanceSqr() < 4.0E-4;
	}

	/** Whether the local viewer should see this player's name over their body, by vanilla's nameplate rules. */
	private static boolean nameShown(Player player) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer local = minecraft.player;
		if (local == null || player == minecraft.getCameraEntity()) return false;
		boolean visible = !player.isInvisibleTo(local);
		if (player == local) return visible;
		Team team = player.getTeam();
		Team mine = local.getTeam();
		if (team == null) return visible;
		return switch (team.getNameTagVisibility()) {
			case ALWAYS -> visible;
			case NEVER -> false;
			case HIDE_FOR_OTHER_TEAMS -> mine == null ? visible : team.isAlliedTo(mine) && (team.canSeeFriendlyInvisibles() || visible);
			case HIDE_FOR_OWN_TEAM -> mine == null ? visible : !team.isAlliedTo(mine) && visible;
		};
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
		if (!minecraft.isPaused()) {
			for (Player player : minecraft.level.players()) {
				SurrogateForm form = drawnForm(player);
				if (form == null) continue;
				LivingEntity body = body(player, form);
				if (body != null) step(player, body);
			}
		}
		for (Iterator<Map.Entry<Integer, LivingEntity>> it = BODIES.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<Integer, LivingEntity> entry = it.next();
			if (!(minecraft.level.getEntity(entry.getKey()) instanceof Player player) || drawnForm(player) == null
					|| entry.getValue().level() != minecraft.level) {
				it.remove();
			}
		}
		Player local = minecraft.player;
		if (local != null && HemoCapabilityAccess.possessionForm(local) == SurrogateForm.SPIDER
				&& HemoCapabilityAccess.getPossession(local).toggled(PossessionAction.CLIMB)
				&& local.horizontalCollision && !local.isShiftKeyDown()) {
			Vec3 motion = local.getDeltaMovement();
			local.setDeltaMovement(motion.x, 0.2, motion.z);
		}
	}

	@SubscribeEvent
	public static void onMovementInput(MovementInputUpdateEvent event) {
		Player player = event.getEntity();
		if (!HemoCapabilityAccess.isPossessing(player)
				|| !HemoCapabilityAccess.getPossession(player).toggled(PossessionAction.STILL)) return;
		var input = event.getInput();
		input.forwardImpulse = 0f;
		input.leftImpulse = 0f;
		input.up = input.down = input.left = input.right = false;
		input.jumping = false;
		input.shiftKeyDown = false;
	}

	public static void clear() {
		BODIES.clear();
	}
}
