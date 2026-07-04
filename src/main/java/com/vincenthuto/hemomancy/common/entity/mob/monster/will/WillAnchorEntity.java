package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.mob.monster.BloodDrunkPuppeteerEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.will.WillPresenceCuePacket;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public class WillAnchorEntity extends Entity {
	private static final EntityDataAccessor<Integer> DATA_SCHOOL =
			SynchedEntityData.defineId(WillAnchorEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_TIER =
			SynchedEntityData.defineId(WillAnchorEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_BROKEN_COUNT =
			SynchedEntityData.defineId(WillAnchorEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_SENT_PRESENT =
			SynchedEntityData.defineId(WillAnchorEntity.class, EntityDataSerializers.BOOLEAN);

	@Nullable
	private UUID targetId;
	private int lifetimeTicks;

	public WillAnchorEntity(EntityType<?> type, Level level) {
		super(type, level);
		noPhysics = true;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_SCHOOL, EnumBloodTendency.ANIMUS.ordinal());
		builder.define(DATA_TIER, 1);
		builder.define(DATA_BROKEN_COUNT, 1);
		builder.define(DATA_SENT_PRESENT, false);
	}

	public void configure(EnumBloodTendency school, WillCompositionRules.Composition composition, @Nullable Player target) {
		entityData.set(DATA_SCHOOL, school.ordinal());
		entityData.set(DATA_TIER, composition.tier());
		entityData.set(DATA_BROKEN_COUNT, composition.brokenCount());
		entityData.set(DATA_SENT_PRESENT, composition.sentPresent());
		targetId = target == null ? null : target.getUUID();
		lifetimeTicks = anchorLifetime();
	}

	public EnumBloodTendency getSchool() {
		return EnumBloodTendency.values()[Math.max(0, Math.min(EnumBloodTendency.values().length - 1, entityData.get(DATA_SCHOOL)))];
	}

	public int getTier() {
		return Math.max(1, entityData.get(DATA_TIER));
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) return;
		if (lifetimeTicks <= 0) lifetimeTicks = anchorLifetime();
		if (--lifetimeTicks > 0) return;
		materialize();
		discard();
	}

	private void materialize() {
		if (!(level() instanceof ServerLevel server)) return;
		Player target = resolveTarget(server).orElse(null);
		int brokenCount = Math.max(0, entityData.get(DATA_BROKEN_COUNT));
		for (int i = 0; i < brokenCount; i++) {
			if (i == 0 && shouldSubstitutePuppeteer()) {
				spawnPuppeteer(server, target, i);
			} else {
				spawnWill(server, WillOrigin.BROKEN, getSchool(), Math.min(2, getTier()), target, true, i);
			}
		}
		if (entityData.get(DATA_SENT_PRESENT)) {
			spawnWill(server, WillOrigin.SENT, getSchool(), getTier(), target, true, brokenCount);
		}
		for (ServerPlayer player : server.getEntitiesOfClass(ServerPlayer.class, new AABB(blockPosition()).inflate(48.0D))) {
			PacketHandler.sendToPlayer(player, new WillPresenceCuePacket(position()));
		}
	}

	private void spawnWill(ServerLevel server, WillOrigin origin, EnumBloodTendency school, int tier,
			@Nullable Player target, boolean drifting, int index) {
		WillEntity will = EntityInit.will.get().create(server);
		if (will == null) return;
		double offsetX = (index % 3 - 1) * 1.35D;
		double offsetZ = (index / 3) * 1.35D;
		will.moveTo(getX() + offsetX, getY(), getZ() + offsetZ, getYRot(), getXRot());
		will.configure(origin, school, tier, target, drifting);
		server.addFreshEntity(will);
	}

	private void spawnPuppeteer(ServerLevel server, @Nullable Player target, int index) {
		BloodDrunkPuppeteerEntity puppeteer = EntityInit.blood_drunk_puppeteer.get().create(server);
		if (puppeteer == null) return;
		double offsetX = (index % 3 - 1) * 1.35D;
		double offsetZ = (index / 3) * 1.35D;
		puppeteer.moveTo(getX() + offsetX, getY(), getZ() + offsetZ, getYRot(), getXRot());
		if (target != null) {
			puppeteer.setTarget(target);
		}
		server.addFreshEntity(puppeteer);
	}

	private boolean shouldSubstitutePuppeteer() {
		double chance = HemoServerConfig.WILL_PUPPETEER_SPAWN_CHANCE == null
				? 0.2D
				: HemoServerConfig.WILL_PUPPETEER_SPAWN_CHANCE.get();
		return chance > 0.0D && random.nextDouble() < chance;
	}

	private Optional<Player> resolveTarget(ServerLevel server) {
		if (targetId != null) {
			Player player = server.getPlayerByUUID(targetId);
			if (player != null) return Optional.of(player);
		}
		return server.getEntitiesOfClass(Player.class, new AABB(blockPosition()).inflate(48.0D),
						player -> player.isAlive() && !player.isSpectator() && !player.isCreative())
				.stream().min(Comparator.comparingDouble(this::distanceToSqr));
	}

	private static int anchorLifetime() {
		return HemoServerConfig.WILL_ANCHOR_LIFETIME_TICKS == null ? 80 : HemoServerConfig.WILL_ANCHOR_LIFETIME_TICKS.get();
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		if (tag.hasUUID("Target")) targetId = tag.getUUID("Target");
		entityData.set(DATA_SCHOOL, tag.getInt("School"));
		entityData.set(DATA_TIER, tag.getInt("Tier"));
		entityData.set(DATA_BROKEN_COUNT, tag.getInt("BrokenCount"));
		entityData.set(DATA_SENT_PRESENT, tag.getBoolean("SentPresent"));
		lifetimeTicks = tag.getInt("LifetimeTicks");
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		if (targetId != null) tag.putUUID("Target", targetId);
		tag.putInt("School", entityData.get(DATA_SCHOOL));
		tag.putInt("Tier", entityData.get(DATA_TIER));
		tag.putInt("BrokenCount", entityData.get(DATA_BROKEN_COUNT));
		tag.putBoolean("SentPresent", entityData.get(DATA_SENT_PRESENT));
		tag.putInt("LifetimeTicks", lifetimeTicks);
	}
}
