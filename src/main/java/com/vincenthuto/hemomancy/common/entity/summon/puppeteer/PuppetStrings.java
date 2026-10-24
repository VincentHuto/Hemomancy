package com.vincenthuto.hemomancy.common.entity.summon.puppeteer;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Puppets arrive lowered on their strings and leave hauled up by them. A departing body stays in the world for
 * {@link PuppetStringsRules#UNSPOOL_TICKS} as an inert shell: no AI, no gravity, invulnerable, skipped by every
 * active-puppet count, its own tick cancelled. The client draws the strings and poses the body from the same rules.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class PuppetStrings {
	private static final String UNSPOOL = "HemomancyUnspool";
	private static final ParticleColor ANIMUS_RED = new ParticleColor(214, 18, 48);
	private static final ParticleColor HOT_CORE = new ParticleColor(255, 120, 110);

	private PuppetStrings() {
	}

	/** Lowers a newly placed body in on its strings. Call after it has been added to the level. */
	public static void descend(Mob body) {
		if (!(body.level() instanceof ServerLevel level)) return;
		ManipulationVisuals.attached(body, ManipulationVisuals.Form.PUPPET_DESCEND, size(body), body.getBbHeight(),
				PuppetStringsRules.DESCEND_TICKS + 2, strings(body));
		level.sendParticles(HemoParticleData.bloodCell(ANIMUS_RED), body.getX(), body.getY() + 0.1, body.getZ(),
				4, body.getBbWidth() * 0.3, 0.05, body.getBbWidth() * 0.3, 0.02);
	}

	public static boolean isUnspooling(Entity entity) {
		return entity.getPersistentData().contains(UNSPOOL);
	}

	/** Turns the body into a shell its strings haul away; it is removed once the haul has finished. */
	public static void unspool(Mob body) {
		unspool(body, true);
	}

	/**
	 * Hauls the body away; {@code ownStrings} false when someone else's strings already hold it (Vesper's Recall), so
	 * the cue carries a negative string count and only the body's peel and flash are drawn.
	 */
	public static void unspool(Mob body, boolean ownStrings) {
		if (body.isRemoved() || isUnspooling(body)) return;
		if (!(body.level() instanceof ServerLevel level)) {
			body.discard();
			return;
		}
		body.getPersistentData().putInt(UNSPOOL, PuppetStringsRules.UNSPOOL_TICKS);
		body.setTarget(null);
		body.getNavigation().stop();
		body.setDeltaMovement(Vec3.ZERO);
		body.setNoAi(true);
		body.setNoGravity(true);
		body.setInvulnerable(true);
		// The missing-Crossbar countdown blinks the body; the haul replaces it.
		if (body instanceof BoundPuppeteerSummon summon) summon.hemomancy$setDismissalTicks(0);
		ManipulationVisuals.attached(body, ManipulationVisuals.Form.PUPPET_UNSPOOL, size(body), body.getBbHeight(),
				PuppetStringsRules.UNSPOOL_TICKS + 6, ownStrings ? strings(body) : -1);
		Vec3 head = body.position().add(0, body.getBbHeight(), 0);
		level.sendParticles(HemoParticleData.hitGlow(HOT_CORE), head.x, head.y, head.z, 3, 0.1, 0.05, 0.1, 0.01);
		level.sendParticles(HemoParticleData.bloodCell(ANIMUS_RED), head.x, head.y - body.getBbHeight() * 0.4, head.z,
				5, body.getBbWidth() * 0.3, 0.2, body.getBbWidth() * 0.3, 0.03);
		body.playSound(SoundEvents.CHAIN_BREAK, 0.35F, 1.35F);
	}

	// Runs before other tick handlers (the Circus stage among them) so a departing body ticks only its countdown.
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void beforeTick(EntityTickEvent.Pre event) {
		if (!(event.getEntity() instanceof Mob body) || body.level().isClientSide() || !isUnspooling(body)) return;
		event.setCanceled(true);
		body.setDeltaMovement(Vec3.ZERO);
		int left = body.getPersistentData().getInt(UNSPOOL) - 1;
		if (left <= 0) body.discard();
		else body.getPersistentData().putInt(UNSPOOL, left);
	}

	/** The cue's {@code to} sits one body-width along X, which the client reads back as the body's width. */
	private static Vec3 size(Mob body) {
		return body.position().add(body.getBbWidth(), 0, 0);
	}

	private static int strings(Mob body) {
		return PuppetStringsRules.MIN_STRINGS
				+ Math.floorMod(body.getId(), PuppetStringsRules.MAX_STRINGS - PuppetStringsRules.MIN_STRINGS + 1);
	}
}
