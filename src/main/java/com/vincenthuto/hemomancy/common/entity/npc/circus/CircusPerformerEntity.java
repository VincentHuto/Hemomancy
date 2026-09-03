package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.monster.BloodDrunkPuppeteerTuning;
import com.vincenthuto.hemomancy.common.entity.mob.monster.EnthralledDollEntity;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueNode;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueOption;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

public abstract class CircusPerformerEntity extends PathfinderMob {
	private static final EntityDataAccessor<Byte> ACT_STATE = SynchedEntityData.defineId(
			CircusPerformerEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> VARIANT = SynchedEntityData.defineId(
			CircusPerformerEntity.class, EntityDataSerializers.BYTE);
	private BlockPos home;
	private UUID threatId;
	private int phaseTicks;
	private int warningTicks;
	private int outsideThreatTicks;
	private int downedTicks;
	private boolean dollsSummonedForThreat;

	protected CircusPerformerEntity(EntityType<? extends CircusPerformerEntity> type, Level level) {
		super(type, level);
		setPersistenceRequired();
		xpReward = 0;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ACT_STATE, (byte) ActState.REST.ordinal());
		builder.define(VARIANT, (byte) -1);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(8, new RandomLookAroundGoal(this));
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) return;
		initializeHome();
		if (isDowned()) {
			tickDowned();
			return;
		}
		LivingEntity threat = resolveThreat();
		if (threat != null) {
			tickThreat(threat);
			return;
		}
		if (threatId != null) clearThreat();
		setTarget(null);
		warningTicks = 0;
		outsideThreatTicks = 0;
		returnHomeIfNeeded();
		tickPerformanceCycle();
	}

	private void initializeHome() {
		if (home == null) home = blockPosition();
		if (getVariant() < 0) setVariant(Math.floorMod(home.hashCode(), 2));
		restrictTo(home, 8);
	}

	private void tickPerformanceCycle() {
		Player player = level().getNearestPlayer(this, 24.0D);
		double distanceSqr = player == null ? Double.MAX_VALUE : distanceToSqr(player);
		if (!CircusPerformerRules.shouldPerform(distanceSqr, false, false)) {
			setActState(ActState.REST);
			phaseTicks = 0;
			return;
		}
		phaseTicks++;
		switch (getActState()) {
			case REST -> {
				if (phaseTicks >= restDuration()) changeState(ActState.SETUP);
			}
			case SETUP -> {
				if (phaseTicks >= 20) changeState(ActState.PERFORM);
			}
			case PERFORM -> {
				tickPerformance(phaseTicks);
				if (phaseTicks >= performanceDuration()) changeState(ActState.RECOVER);
			}
			case RECOVER -> {
				if (phaseTicks >= 40) changeState(ActState.REST);
			}
			default -> changeState(ActState.REST);
		}
	}

	private int restDuration() {
		return 100 + Math.floorMod(getId() * 31, 81);
	}

	private void tickThreat(LivingEntity threat) {
		setActState(ActState.ALERT);
		getLookControl().setLookAt(threat, 30.0F, 30.0F);
		double distanceSqr = distanceToSqr(threat);
		outsideThreatTicks = distanceSqr > CircusPerformerRules.THREAT_RANGE_SQR
				? outsideThreatTicks + 1 : 0;
		if (CircusPerformerRules.shouldClearThreat(threat.isAlive(), threat.level() == level(), distanceSqr,
				outsideThreatTicks)) {
			clearThreat();
			return;
		}
		if (!CircusPerformerRules.warningComplete(++warningTicks)) return;
		setTarget(threat);
		tickDefense(threat);
	}

	private LivingEntity resolveThreat() {
		if (threatId == null || !(level() instanceof ServerLevel server)) return null;
		return server.getEntity(threatId) instanceof LivingEntity living ? living : null;
	}

	private void tickDowned() {
		setActState(ActState.DOWNED);
		setTarget(null);
		getNavigation().stop();
		LivingEntity threat = resolveThreat();
		if (threat != null && threat.isAlive() && threat.level() == level()
				&& distanceToSqr(threat) <= CircusPerformerRules.THREAT_RANGE_SQR) {
			downedTicks = 0;
			return;
		}
		if (++downedTicks >= CircusPerformerRules.THREAT_CLEAR_TICKS) {
			setHealth(getMaxHealth() * 0.5F);
			clearThreat();
			setActState(ActState.REST);
			downedTicks = 0;
		}
	}

	private void returnHomeIfNeeded() {
		if (home != null && distanceToSqr(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D) > 9.0D) {
			getNavigation().moveTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D, 1.0D);
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		boolean administrative = source.is(DamageTypes.GENERIC_KILL)
				|| source.getEntity() instanceof Player player && player.isCreative();
		if (administrative) return super.hurt(source, amount);
		if (isDowned()) return false;
		if (source.getEntity() instanceof LivingEntity attacker) {
			summonDolls();
			alertTroupe(attacker);
		}
		if (CircusPerformerRules.shouldEnterDowned(getHealth(), amount, false)) {
			setHealth(1.0F);
			setActState(ActState.DOWNED);
			return true;
		}
		return super.hurt(source, amount);
	}

	private void alertTroupe(LivingEntity attacker) {
		AABB area = getBoundingBox().inflate(32.0D);
		for (CircusPerformerEntity performer : level().getEntitiesOfClass(CircusPerformerEntity.class, area)) {
			if (!performer.isDowned()) performer.setThreat(attacker);
		}
	}

	private void summonDolls() {
		if (dollsSummonedForThreat) return;
		dollsSummonedForThreat = true;
		int count = CircusPerformerRules.dollCount(random.nextInt(3));
		for (int i = 0; i < count; i++) {
			EnthralledDollEntity doll = new EnthralledDollEntity(level(), this);
			double[] offset = BloodDrunkPuppeteerTuning.dollSpawnOffset(i);
			doll.setPos(getX() + offset[0], getY() + offset[1], getZ() + offset[2]);
			doll.setSummonedByPuppeteer(true);
			level().addFreshEntity(doll);
		}
	}

	private void setThreat(LivingEntity threat) {
		threatId = threat.getUUID();
		warningTicks = 0;
		outsideThreatTicks = 0;
		phaseTicks = 0;
		setActState(ActState.ALERT);
		getNavigation().stop();
	}

	private void clearThreat() {
		threatId = null;
		dollsSummonedForThreat = false;
		setTarget(null);
		warningTicks = 0;
		outsideThreatTicks = 0;
		phaseTicks = 0;
	}

	private void changeState(ActState state) {
		setActState(state);
		phaseTicks = 0;
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND || isDowned() || threatId != null)
			return InteractionResult.PASS;
		if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
			changeState(ActState.RECOVER);
			PacketHandler.sendToPlayer(serverPlayer, new OpenDialoguePacket(dialogueTree()));
		}
		return InteractionResult.sidedSuccess(level().isClientSide);
	}

	private DialogueTree dialogueTree() {
		String key = "hemomancy.dialogue." + roleId();
		return DialogueTree.builder(getType().getDescriptionId(), Hemomancy.rloc(texturePath()), getId())
				.addNode(new DialogueNode("greeting", List.of(key + ".line1", key + ".line2", key + ".line3"),
						List.of(new DialogueOption("hemomancy.dialogue.circus_performer.leave", null, null))))
				.build();
	}

	public ActState getActState() {
		int index = Byte.toUnsignedInt(entityData.get(ACT_STATE));
		return index < ActState.values().length ? ActState.values()[index] : ActState.REST;
	}

	protected void setActState(ActState state) {
		entityData.set(ACT_STATE, (byte) state.ordinal());
	}

	public int getVariant() {
		return entityData.get(VARIANT);
	}

	private void setVariant(int variant) {
		entityData.set(VARIANT, (byte) Math.floorMod(variant, 2));
	}

	public boolean isDowned() {
		return getActState() == ActState.DOWNED;
	}

	@Override
	public boolean isAttackable() {
		return !isDowned() && super.isAttackable();
	}

	public BlockPos getHome() {
		return home == null ? blockPosition() : home;
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (home != null) tag.putLong("CircusHome", home.asLong());
		tag.putInt("CircusVariant", getVariant());
		tag.putBoolean("CircusDowned", isDowned());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		home = tag.contains("CircusHome") ? BlockPos.of(tag.getLong("CircusHome")) : null;
		setVariant(tag.getInt("CircusVariant"));
		setActState(tag.getBoolean("CircusDowned") ? ActState.DOWNED : ActState.REST);
	}

	protected boolean isTroupeMember(LivingEntity entity) {
		return entity instanceof CircusPerformerEntity
				|| entity instanceof EnthralledDollEntity doll && doll.isOwnedByCircusPerformer();
	}

	protected abstract String roleId();
	protected abstract String texturePath();
	protected abstract int performanceDuration();
	protected abstract void tickPerformance(int actTick);
	protected abstract void tickDefense(LivingEntity target);

	public enum ActState {
		REST, SETUP, PERFORM, RECOVER, ALERT, DOWNED
	}
}
