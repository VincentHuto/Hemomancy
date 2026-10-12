package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Client-only stand-ins for casts whose live visuals come from real entities (blood shots, needles, coronation
 * swords). Each gets a negative id no server entity can share, flies a fixed line that this class drives (client
 * projectiles otherwise wait on server position updates that never come), lives a fixed number of ticks and is then
 * removed; nothing about it ever reaches the server.
 */
final class EchoDummies {
	private static final int CAP = 48;
	private static final List<Dummy> LIVE = new ArrayList<>();
	private static int nextId = -0x4E43_0000;

	private static final class Dummy {
		final int id;
		final Vec3 start, velocity;
		int age, life;

		Dummy(int id, Vec3 start, Vec3 velocity, int life) {
			this.id = id;
			this.start = start;
			this.velocity = velocity;
			this.life = life;
		}
	}

	private EchoDummies() {
	}

	/**
	 * Adds `entity` to the client level at its position for `lifeTicks`, flying `velocity` blocks per tick; returns
	 * false (and adds nothing) at the cap.
	 */
	static boolean spawn(ClientLevel level, Entity entity, Vec3 velocity, int lifeTicks) {
		if (LIVE.size() >= CAP) return false;
		int id = nextId--;
		entity.setId(id);
		face(entity, velocity);
		entity.setDeltaMovement(velocity);
		level.addEntity(entity);
		LIVE.add(new Dummy(id, entity.position(), velocity, lifeTicks));
		return true;
	}

	static void tick(ClientLevel level) {
		LIVE.removeIf(dummy -> {
			Entity entity = level.getEntity(dummy.id);
			if (entity == null || entity.isRemoved()) return true;
			if (++dummy.age >= dummy.life) {
				level.removeEntity(dummy.id, Entity.RemovalReason.DISCARDED);
				return true;
			}
			Vec3 at = dummy.start.add(dummy.velocity.scale(dummy.age));
			entity.setOldPosAndRot();
			entity.setPos(at);
			entity.setDeltaMovement(dummy.velocity);
			face(entity, dummy.velocity);
			return false;
		});
	}

	/** Points a projectile along its flight, as vanilla arrows face their motion. */
	private static void face(Entity entity, Vec3 velocity) {
		if (velocity.lengthSqr() < 1.0E-8D) return;
		double horizontal = velocity.horizontalDistance();
		entity.setYRot((float) (Mth.atan2(velocity.x, velocity.z) * Mth.RAD_TO_DEG));
		entity.setXRot((float) (Mth.atan2(velocity.y, horizontal) * Mth.RAD_TO_DEG));
	}

	static void clear(@Nullable ClientLevel level) {
		if (level != null)
			for (Dummy dummy : LIVE)
				if (level.getEntity(dummy.id) != null) level.removeEntity(dummy.id, Entity.RemovalReason.DISCARDED);
		LIVE.clear();
	}

	static int live() {
		return LIVE.size();
	}
}
