package com.vincenthuto.hemomancy.client.data;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.FinalePhase;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * What the client knows of the Ringmaster's finale it is in (2026-11-18), read each tick from his copy on the stage:
 * the copy's synced phase and phase ticks and its health. Only the Chamber holds stage copies, and a player's stage
 * holds only their own finale, so the nearest copy is theirs; the copy's carousel marks the stage's centre.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class RingmasterFinaleClientData {
	private static final double SEARCH = 96.0D;

	private static boolean active;
	private static FinalePhase phase = FinalePhase.ENDED;
	private static int phaseTicks;
	private static float health = 1.0F;
	private static Vec3 centre = Vec3.ZERO;
	private static float veil, lastVeil;
	@Nullable
	private static CircusRingmasterEntity ringmaster;

	private RingmasterFinaleClientData() {
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null || !mc.level.dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) {
			if (active) clear();
			return;
		}
		AABB around = mc.player.getBoundingBox().inflate(SEARCH);
		CircusRingmasterEntity copy = null;
		for (CircusRingmasterEntity found : mc.level.getEntitiesOfClass(CircusRingmasterEntity.class, around,
				CircusRingmasterEntity::stageCopy)) {
			if (copy == null || found.distanceToSqr(mc.player) < copy.distanceToSqr(mc.player)) copy = found;
		}
		if (copy == null) {
			if (active) clear();
			return;
		}
		CircusCarouselEntity wheel = null;
		for (CircusCarouselEntity found : mc.level.getEntitiesOfClass(CircusCarouselEntity.class, around)) {
			if (wheel == null || found.distanceToSqr(copy) < wheel.distanceToSqr(copy)) wheel = found;
		}
		if (wheel == null) {
			if (active) clear();
			return;
		}
		boolean starting = !active;
		active = true;
		ringmaster = copy;
		phase = copy.finalePhase();
		phaseTicks = copy.finaleTick();
		health = Mth.clamp(copy.getHealth() / copy.getMaxHealth(), 0.0F, 1.0F);
		centre = wheel.position();
		lastVeil = starting ? 0.0F : veil;
		veil = FinaleRules.veilStrength(phase, phaseTicks, health);
	}

	public static void clear() {
		active = false;
		phase = FinalePhase.ENDED;
		phaseTicks = 0;
		health = 1.0F;
		centre = Vec3.ZERO;
		veil = lastVeil = 0.0F;
		ringmaster = null;
		FinaleCrossingClientState.clear();
		com.vincenthuto.hemomancy.client.render.world.circus.FinaleTheatre.clear();
		com.vincenthuto.hemomancy.client.render.world.circus.FinaleVeilRenderer.clear();
	}

	public static boolean isActive() {
		return active;
	}

	public static FinalePhase phase() {
		return phase;
	}

	public static int phaseTicks() {
		return phaseTicks;
	}

	public static float health() {
		return health;
	}

	/** How strongly his Chamber covers the tent's faces this frame, eased between the last two ticks. */
	public static float veilStrength(float partial) {
		return active ? Mth.lerp(partial, lastVeil, veil) : 0.0F;
	}

	/** Whether his Chamber has begun to show on the tent. */
	public static boolean merging(float partial) {
		return veilStrength(partial) > 0.001F;
	}

	/** The top of the copy's ring floor at its centre, where the carousel stands. */
	public static Vec3 copyCentre() {
		return centre;
	}

	@Nullable
	public static CircusRingmasterEntity ringmaster() {
		return ringmaster;
	}
}
