package com.vincenthuto.hemomancy.common.entity.utility;

import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingAllies;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingAnchorState;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingFeeding;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingHealing;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MarrowplumeRules;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.SunburstRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A short-lived Morphling field patch: Sunburst's Bloom or Marrowplume's Whale Fall. It places no blocks, ticks its
 * aura every two seconds for its owner and the owner's allies inside, despawns on time and is never saved, so a
 * reload leaves nothing behind. Track 3 draws it from the synced kind, radius and age.
 */
public class MorphlingFieldEntity extends Entity {
	public enum Kind {
		BLOOM, WHALE_FALL
	}

	private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(MorphlingFieldEntity.class,
			EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(MorphlingFieldEntity.class,
			EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(MorphlingFieldEntity.class,
			EntityDataSerializers.INT);
	public static final int PULSE_TICKS = 40;

	private UUID owner;
	private final Set<UUID> fed = new HashSet<>();

	public MorphlingFieldEntity(EntityType<? extends MorphlingFieldEntity> type, Level level) {
		super(type, level);
		this.noPhysics = true;
		this.setNoGravity(true);
	}

	public static MorphlingFieldEntity spawn(ServerLevel level, Player owner, Kind kind, double x, double y, double z,
			float radius, int lifetime) {
		MorphlingFieldEntity field = new MorphlingFieldEntity(EntityInit.morphling_field.get(), level);
		field.owner = owner.getUUID();
		field.entityData.set(KIND, kind.ordinal());
		field.entityData.set(RADIUS, radius);
		field.entityData.set(LIFETIME, lifetime);
		field.setPos(x, y, z);
		level.addFreshEntity(field);
		return field;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(KIND, 0);
		builder.define(RADIUS, 1.0F);
		builder.define(LIFETIME, 1);
	}

	public Kind kind() {
		int ordinal = entityData.get(KIND);
		return Kind.values()[Math.max(0, Math.min(Kind.values().length - 1, ordinal))];
	}

	public float radius() {
		return entityData.get(RADIUS);
	}

	public int lifetime() {
		return entityData.get(LIFETIME);
	}

	public UUID owner() {
		return owner;
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) return;
		if (tickCount >= lifetime() || owner == null) {
			discard();
			return;
		}
		if (tickCount % PULSE_TICKS != 1) return;
		Player host = level().getPlayerByUUID(owner);
		if (host == null || !host.isAlive()) {
			discard();
			return;
		}
		double r = radius();
		AABB area = getBoundingBox().inflate(r, Math.max(2.0D, r * 0.5D), r);
		for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, area,
				other -> other.distanceToSqr(this) <= r * r * 1.5D)) {
			boolean friend = living == host || MorphlingAllies.isAlly(host, living);
			if (friend) {
				pulseFriend(host, living);
			} else if (kind() == Kind.WHALE_FALL && MorphlingAllies.isHostile(host, living)) {
				living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, PULSE_TICKS + 5, 0, false, true, true), host);
				living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, PULSE_TICKS + 5, 1, false, true, true),
						host);
			}
		}
	}

	private void pulseFriend(Player host, LivingEntity friend) {
		if (kind() == Kind.BLOOM) {
			friend.addEffect(new MobEffectInstance(MobEffects.REGENERATION, PULSE_TICKS + 5, 0, false, true, true));
			if (friend instanceof Player player) {
				MorphlingFeeding.feed(player, SunburstRules.BLOOM_HUNGER, true);
			}
			return;
		}
		friend.addEffect(new MobEffectInstance(EffectInit.mycorrhizal_mending, PULSE_TICKS + 5, 1, false, true, true));
		if (fed.add(friend.getUUID())) {
			float heal = MarrowplumeRules.WHALE_FALL_HEAL;
			if (friend instanceof Player player && MorphlingAnchorState.isAnchored(player, 1)) {
				heal *= (float) MarrowplumeRules.WHALE_FALL_ANCHOR_BONUS;
			}
			if (friend == host) MorphlingHealing.healHost(host, heal);
			else friend.heal(heal);
		}
	}

	@Override
	public boolean shouldBeSaved() {
		return false;
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
	}
}
