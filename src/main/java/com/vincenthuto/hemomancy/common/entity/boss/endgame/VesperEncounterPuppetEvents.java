package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.summon.PuppetStrings;
import com.vincenthuto.hemomancy.common.worldgen.VesperOrdealManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.UUID;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class VesperEncounterPuppetEvents {
	public static final String PUPPET_KEY = "HemomancyVesperEncounterPuppet";
	public static final String BOSS_KEY = "HemomancyVesperEncounterBoss";
	public static final String SINCE_KEY = "HemomancyVesperPuppetSince";
	private static final String ORPHAN_KEY = "HemomancyVesperPuppetOrphan";
	private static final int LEASH_INTERVAL = 10;

	private VesperEncounterPuppetEvents() {
	}

	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		if (!event.getEntity().getPersistentData().getBoolean(PUPPET_KEY)
				|| !(event.getEntity().level() instanceof ServerLevel server)
				|| !event.getEntity().getPersistentData().hasUUID(BOSS_KEY)) return;
		Entity boss = server.getEntity(event.getEntity().getPersistentData().getUUID(BOSS_KEY));
		if (!(boss instanceof VesperTheCrownedRefusalEntity crowned)) return;
		crowned.applyPuppetBacklash();
		// Only a kill by the player (or something of theirs) strikes the shape; other deaths return it to the roster.
		if (killedByPlayer(event.getSource().getEntity())
				&& event.getEntity() instanceof BoundPuppeteerSummon bound) {
			crowned.strikePuppet(bound.hemomancy$getSummonName());
		}
	}

	private static boolean killedByPlayer(Entity killer) {
		if (killer instanceof Player) return true;
		if (killer instanceof OwnableEntity ownable && ownable.getOwner() instanceof Player) return true;
		return killer instanceof BoundPuppeteerSummon summon && summon.hemomancy$getOwnerUUID() != null
				&& killer.level() instanceof ServerLevel level
				&& level.getPlayerByUUID(summon.hemomancy$getOwnerUUID()) != null;
	}

	/**
	 * His puppets stay on his strings: they hunt the ordeal owner while the owner is in the arena, turn back if they
	 * stray past it, and unspool once their conductor is gone (dead, discarded, or replaced by the Evening Star).
	 */
	@SubscribeEvent
	public static void leash(EntityTickEvent.Post event) {
		if (!(event.getEntity() instanceof Mob puppet) || puppet.tickCount % LEASH_INTERVAL != 0
				|| !(puppet.level() instanceof ServerLevel server)) return;
		CompoundTag data = puppet.getPersistentData();
		if (!data.getBoolean(PUPPET_KEY) || !data.hasUUID(BOSS_KEY)) return;
		VesperTheCrownedRefusalEntity boss = server.getEntity(data.getUUID(BOSS_KEY))
				instanceof VesperTheCrownedRefusalEntity crowned && crowned.isAlive() ? crowned : null;
		int orphan = boss == null ? data.getInt(ORPHAN_KEY) + LEASH_INTERVAL : 0;
		data.putInt(ORPHAN_KEY, orphan);
		Vec3 center = boss == null ? puppet.position() : VesperOrdealManager.flightArena(boss)
				.map(arena -> new Vec3(arena.centerX(), arena.floorY() + 1.0D, arena.centerZ()))
				.orElse(boss.position());
		switch (VesperPuppetLeashRules.order(boss != null, orphan, horizontal(puppet.position(), center))) {
			case UNSPOOL -> {
				PuppetStrings.unspool(puppet);
				return;
			}
			case RETURN -> {
				puppet.setTarget(null);
				puppet.getNavigation().moveTo(center.x, center.y, center.z, 1.2D);
				return;
			}
			case HOLD -> { }
		}
		if (boss == null) return;
		UUID ownerId = boss.getOrdealOwner();
		Player owner = ownerId == null ? null : server.getPlayerByUUID(ownerId);
		boolean valid = owner != null && owner.isAlive() && !owner.isCreative() && !owner.isSpectator();
		if (VesperPuppetLeashRules.prefersOwner(valid, valid ? horizontal(owner.position(), center) : Double.MAX_VALUE)
				&& puppet.getTarget() != owner) {
			puppet.setTarget(owner);
		}
	}

	private static double horizontal(Vec3 a, Vec3 b) {
		double dx = a.x - b.x;
		double dz = a.z - b.z;
		return Math.sqrt(dx * dx + dz * dz);
	}

	@SubscribeEvent
	public static void suppressDrops(LivingDropsEvent event) {
		if (event.getEntity().getPersistentData().getBoolean(PUPPET_KEY)) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void suppressExperience(LivingExperienceDropEvent event) {
		if (event.getEntity().getPersistentData().getBoolean(PUPPET_KEY)) event.setDroppedExperience(0);
	}
}
