package com.vincenthuto.hemomancy.client.render.item.harbinger.crossbar;

import com.vincenthuto.hemomancy.common.entity.projectile.CircusKnifeProjectileEntity;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.summon.CinderBellowsEntity;
import com.vincenthuto.hemomancy.common.entity.summon.MarrowSpitterEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Client-only motion for one holder's Crossbar, advanced once per holder tick from what the client can already see:
 * the holder's puppets for this Crossbar (synced owner and Crossbar ids), their swings, the Cinder Bellows breath
 * cycle and new Marrow Juggler knives. Nothing here is saved or synced.
 */
final class CrossbarMotionState {
	private record Key(UUID holder, UUID crossbar) {
	}

	private static final Map<Key, CrossbarMotionState> STATES = new HashMap<>();
	private static final double PUPPET_RANGE = 128.0D;
	private static final int FORGET_TICKS = 200;
	private static final int NEW_KNIFE_TICKS = 5;

	private int lastTick = Integer.MIN_VALUE;
	private long lastSeen;
	private int count = CrossbarMotionRules.UNSEEN;
	private float engage;
	private float previousEngage;
	private int tugAge = CrossbarMotionRules.TUG_TICKS + 1;
	private int liftAge = CrossbarMotionRules.LIFT_TICKS + 1;
	private Set<Integer> swinging = new HashSet<>();
	private final Map<Integer, Integer> breathCycles = new HashMap<>();
	private final Set<Integer> knives = new HashSet<>();

	static CrossbarMotionRules.Pose pose(LivingEntity holder, UUID crossbar, float partialTick) {
		long now = holder.level().getGameTime();
		if (STATES.size() > 16) STATES.values().removeIf(state -> now - state.lastSeen > FORGET_TICKS);
		CrossbarMotionState state = STATES.computeIfAbsent(new Key(holder.getUUID(), crossbar), key -> new CrossbarMotionState());
		state.advance(holder, crossbar);
		return CrossbarMotionRules.pose(holder.tickCount + partialTick,
				Mth.lerp(partialTick, state.previousEngage, state.engage),
				CrossbarMotionRules.tug(state.tugAge + partialTick),
				CrossbarMotionRules.lift(state.liftAge + partialTick));
	}

	/** Puppets driven by this Crossbar; Vesper's carry no Crossbar id, so a null id matches on owner alone. */
	static List<Mob> puppets(LivingEntity holder, UUID crossbar) {
		return holder.level().getEntitiesOfClass(Mob.class, holder.getBoundingBox().inflate(PUPPET_RANGE),
				mob -> mob.isAlive() && mob instanceof BoundPuppeteerSummon bound
						&& holder.getUUID().equals(bound.hemomancy$getOwnerUUID())
						&& (crossbar == null || crossbar.equals(bound.hemomancy$getCrossbarUUID())));
	}

	private void advance(LivingEntity holder, UUID crossbar) {
		if (holder.tickCount == lastTick) return;
		lastTick = holder.tickCount;
		lastSeen = holder.level().getGameTime();
		tugAge++;
		liftAge++;
		List<Mob> puppets = puppets(holder, crossbar);
		if (CrossbarMotionRules.isCallOrRecall(count, puppets.size())) liftAge = 0;
		count = puppets.size();

		boolean attacked = false;
		Set<Integer> nowSwinging = new HashSet<>();
		Set<Integer> jugglers = new HashSet<>();
		List<Integer> ids = new ArrayList<>();
		for (Mob puppet : puppets) {
			int id = puppet.getId();
			ids.add(id);
			if (puppet.swinging) {
				nowSwinging.add(id);
				attacked |= CrossbarMotionRules.isNewSwing(swinging.contains(id), true);
			}
			if (puppet instanceof CinderBellowsEntity bellows) {
				Integer previous = breathCycles.put(id, bellows.getBreathCycle());
				attacked |= previous != null && CrossbarMotionRules.isBreathStart(previous, bellows.getBreathCycle());
			}
			if (puppet instanceof MarrowSpitterEntity) jugglers.add(id);
		}
		swinging = nowSwinging;
		breathCycles.keySet().retainAll(ids);
		if (!jugglers.isEmpty()) {
			Set<Integer> live = new HashSet<>();
			for (CircusKnifeProjectileEntity knife : holder.level().getEntitiesOfClass(CircusKnifeProjectileEntity.class,
					holder.getBoundingBox().inflate(PUPPET_RANGE),
					knife -> knife.getOwner() != null && jugglers.contains(knife.getOwner().getId()))) {
				live.add(knife.getId());
				attacked |= knife.tickCount <= NEW_KNIFE_TICKS && knives.add(knife.getId());
			}
			knives.retainAll(live);
		} else {
			knives.clear();
		}
		if (attacked) tugAge = 0;
		previousEngage = engage;
		engage = CrossbarMotionRules.approachEngage(engage, count > 0);
	}
}
