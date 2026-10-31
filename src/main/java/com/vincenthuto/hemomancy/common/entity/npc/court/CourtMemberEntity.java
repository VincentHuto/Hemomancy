package com.vincenthuto.hemomancy.common.entity.npc.court;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtLife;
import com.vincenthuto.hemomancy.common.entity.npc.ProtectedNpc;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CourtMemberDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueHubFactory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueItemInquiryNodes;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.ProgressionDialogueNpc;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A member of the Brined Court. The role fixes house, strain stage, body and what the member offers
 * ({@link CourtRole}); the rest is shared. Members breathe water, are never pushed by currents and path on land or the
 * sea floor. A mobile member walks a leash round its home mark; an anchored one holds the mark and only turns to face.
 * Members are protected ({@link ProtectedNpc}) and never fight players: a defending role turns on monsters near home.
 * A shunned (Unstained) player is kept at a distance: mobile members step away, anchored ones close up. The Shared
 * Wake's swimmers move through open water on a swimming stroke; an Edge-swimmer bound to a school swims its loop with
 * the others ({@link CourtSchoolGoal}), and a Nursery tender launches its nursery's school when a player first comes
 * near ({@link CourtSchools}).
 */
public class CourtMemberEntity extends PathfinderMob implements ProgressionDialogueNpc, ProtectedNpc {
	public static final int LEASH = 8;
	public static final int SHUN_DISTANCE = 7;
	public static final int TALK_TICKS = 80;
	/** Ticks a withdrawing arbor holds each way. */
	public static final int WITHDRAW_CYCLE = 400;
	public static final int REST_COOLDOWN = 20 * 60;
	/** Ticks a Veiled resident stands still before it folds away out of sight. */
	public static final int FOLD_TICKS = 60;
	/** A Nursery tender launches its nursery's school once a player is this close. */
	public static final double NURSERY_LAUNCH_RANGE = 64.0D;

	private static final EntityDataAccessor<Integer> TALK = SynchedEntityData.defineId(CourtMemberEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> WITHDRAWN = SynchedEntityData.defineId(CourtMemberEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> WARY = SynchedEntityData.defineId(CourtMemberEntity.class, EntityDataSerializers.BOOLEAN);

	private final CourtRole role;
	@Nullable
	private BlockPos home;
	private float homeYaw;
	private String mark = "";
	private boolean withdraws;
	// Client-side easing of the synced poses.
	private float withdrawO, withdraw, waryO, wary;
	/** When each player last rested here; not saved (a member's own short memory). */
	private final Map<UUID, Long> restedAt = new HashMap<>();
	// The Shared Wake school this member swims with, if any: its id, this member's station, and the loop.
	@Nullable
	private UUID school;
	private int schoolSlot;
	private BlockPos schoolCentre = BlockPos.ZERO;
	private int schoolRadius = CourtSchoolRules.LOOP_RADIUS;
	private int schoolWaypoint;
	/** Game time before which a Head-tender with no crown near doesn't search for one again (not saved). */
	private long nextCrownScan;
	// When a Veiled resident last moved, and where it stood then (server side, not saved).
	private long lastMoved;
	@Nullable
	private Vec3 lastPosition;

	public CourtMemberEntity(EntityType<? extends CourtMemberEntity> type, Level level, CourtRole role) {
		super(type, level);
		this.role = role;
		this.setInvulnerable(true);
		this.setPersistenceRequired();
		if (role.behaviour() == CourtRole.Behaviour.SWIMMER) {
			// Mob builds its controls before the role is set; swimmers get a stroke through open water instead.
			this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, role.defends() ? 0.55F : 0.4F, 1.0F, false);
			this.lookControl = new SmoothSwimmingLookControl(this, 10);
		}
		roleGoals();
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 30.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.22D)
				.add(Attributes.ATTACK_DAMAGE, 4.0D)
				.add(Attributes.FOLLOW_RANGE, 16.0D);
	}

	public CourtRole role() {
		return role;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(TALK, 0);
		builder.define(WITHDRAWN, false);
		builder.define(WARY, false);
	}

	/** Mob's constructor registers goals before the role is set, so the role's goals are added after it. */
	private void roleGoals() {
		this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
		if (role.anchored()) return;
		this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, SHUN_DISTANCE, 0.8D, 1.0D,
				living -> living instanceof Player player && CourtLife.isShunned(player)));
		if (role.defends()) {
			this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1D, false));
			this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
					this::defendsAgainst));
		}
		if (role.behaviour() == CourtRole.Behaviour.SWIMMER) {
			this.goalSelector.addGoal(3, new CourtSchoolGoal(this));
			this.goalSelector.addGoal(4, new RandomSwimmingGoal(this, 0.8D, 60) {
				@Override
				public boolean canUse() {
					return school == null && isInWater() && super.canUse();
				}
			});
		}
		this.goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 0.8D));
		this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.6D, 160) {
			@Override
			public boolean canUse() {
				return school == null && super.canUse();
			}
		});
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
	}

	/**
	 * Whether a defending member turns on {@code target}: a monster it may harm that no player owns or fights beside.
	 * Members never fight players, so a player's pets, puppets and team-mates are left alone too.
	 */
	public boolean defendsAgainst(LivingEntity target) {
		if (!(target instanceof Monster) || !ManipulationCombatHelper.canHarmFor(this, target)) return false;
		if (target instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) return false;
		if (target instanceof BoundPuppeteerSummon puppet && puppet.hemomancy$getOwnerUUID() != null) return false;
		var team = target.getTeam();
		return team == null || level().players().stream().noneMatch(player -> player.getTeam() == team);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new AmphibiousPathNavigation(this, level);
	}

	/** Swimmers stroke through water without sinking (as the axolotl does); everyone else walks the floor. */
	@Override
	public void travel(Vec3 input) {
		if (role.behaviour() == CourtRole.Behaviour.SWIMMER && isControlledByLocalInstance() && isInWater()) {
			moveRelative(getSpeed(), input);
			move(MoverType.SELF, getDeltaMovement());
			setDeltaMovement(getDeltaMovement().scale(0.9D));
			return;
		}
		super.travel(input);
	}

	@Override
	public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
		return false;
	}

	@Override
	public boolean isPushedByFluid() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return !role.anchored();
	}

	/** Protected, but a member that falls out of the world dies there rather than falling forever. */
	@Override
	public boolean hurt(DamageSource source, float amount) {
		boolean admitted = admitsDamage(source) || source.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD);
		// Residents are protected, so the attempt itself is the wrong (Track 10's Bloom Warden answers it).
		if (!admitted && !level().isClientSide() && source.getEntity() instanceof ServerPlayer player) {
			CourtTransgressions.harmAttempted(this, player);
		}
		return admitted && super.hurt(source, amount);
	}

	/** Ordinary living damage, for the Court's followers who fight (the Bloom Warden, the Kindled Champion). */
	protected final boolean hurtAsFighter(DamageSource source, float amount) {
		return super.hurt(source, amount);
	}

	/** A member lost to {@code /kill} or the void leaves its mark vacant, to be filled again later. */
	@Override
	public void die(DamageSource source) {
		boolean first = !this.dead && !this.isRemoved();
		super.die(source);
		if (first && opensVacancy() && level() instanceof net.minecraft.server.level.ServerLevel server
				&& CourtVacancies.replaces(source)) {
			CourtVacancies.get(server).open(this);
		}
	}

	/** Whether losing this member leaves a resident mark to fill; a follower in a fight stands on none. */
	protected boolean opensVacancy() {
		return true;
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	/** Sets the member down on its mark: home, facing, and the leash a mobile member walks. */
	public void settle(BlockPos home, float yaw, String mark) {
		this.home = home.immutable();
		this.homeYaw = yaw;
		this.mark = mark;
		this.withdraws = mark.endsWith("withdrawing");
		// The suspended roles hang where they are set: the Lamp-colony in the lines, the Sleeper from its hook.
		this.setNoGravity(role == CourtRole.LAMP_COLONY || role == CourtRole.SLEEPER);
		this.setYRot(yaw);
		this.setYHeadRot(yaw);
		this.yBodyRot = yaw;
		if (!role.anchored()) this.restrictTo(this.home, LEASH);
	}

	@Nullable
	public BlockPos home() {
		return home;
	}

	public String mark() {
		return mark;
	}

	/** The way the member's mark faces. */
	public float homeYaw() {
		return homeYaw;
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			withdrawO = withdraw;
			waryO = wary;
			withdraw = Mth.approach(withdraw, isWithdrawn() ? 1.0F : 0.0F, 0.05F);
			wary = Mth.approach(wary, isWary() ? 1.0F : 0.0F, 0.1F);
			return;
		}
		int talk = entityData.get(TALK);
		if (talk > 0) entityData.set(TALK, talk - 1);
		if (lastPosition == null || position().distanceToSqr(lastPosition) > 0.0004D || talk > 0) {
			lastPosition = position();
			lastMoved = tickCount;
		}
		if (role == CourtRole.NURSERY_TENDER && role.marks().contains(mark) && tickCount % 200 == 0
				&& level() instanceof net.minecraft.server.level.ServerLevel server
				&& server.getNearestPlayer(this, NURSERY_LAUNCH_RANGE) != null) {
			CourtSchools.get(server).launchFromNursery(server, this);
		}
		if (home != null && role.anchored()) {
			// Anchored: held on the mark, turning only to face.
			if (distanceToSqr(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D) > 0.01D) {
				moveTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D, getYRot(), getXRot());
			}
			setDeltaMovement(getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
			if (getTarget() == null && entityData.get(TALK) == 0 && tickCount % 20 == 0) yBodyRot = homeYaw;
		}
		if (tickCount % 10 == 0) {
			boolean wary = !level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(SHUN_DISTANCE + 1),
					player -> player.isAlive() && !player.isSpectator() && CourtLife.isShunned(player)).isEmpty();
			entityData.set(WARY, wary);
			boolean cycle = withdraws && (tickCount / WITHDRAW_CYCLE) % 2 == 1;
			entityData.set(WITHDRAWN, role.anchored() && (wary || cycle) || foldsAway());
		}
	}

	/** 0 to 1 while the member is speaking to someone, easing in and out. */
	public float talkAmount(float partialTick) {
		float left = Math.max(0.0F, entityData.get(TALK) - partialTick);
		return Mth.clamp(Math.min(left, TALK_TICKS - left) / 10.0F, 0.0F, 1.0F);
	}

	/** Eased 0 to 1 toward the withdrawn pose, for the renderer. */
	public float withdrawAmount(float partialTick) {
		return Mth.lerp(partialTick, withdrawO, withdraw);
	}

	/** Eased 0 to 1 toward the closed posture kept near a shunned visitor, for the renderer. */
	public float waryAmount(float partialTick) {
		return Mth.lerp(partialTick, waryO, wary);
	}

	/**
	 * A Veiled resident that has stood still a while and is speaking to no one folds away into its drape, nearly out
	 * of sight; it unfolds when it moves or is spoken to.
	 */
	public boolean foldsAway() {
		return role == CourtRole.VEILED_RESIDENT && tickCount - lastMoved >= FOLD_TICKS && entityData.get(TALK) == 0;
	}

	/** Counts as moving for the next {@code ticks}, so a Veiled resident unfolds and stays unfolded that long. */
	public void stir(int ticks) {
		lastMoved = tickCount + ticks;
	}

	public boolean isWithdrawn() {
		return entityData.get(WITHDRAWN);
	}

	/** A shunned player is near: the member closes up and keeps its distance. */
	public boolean isWary() {
		return entityData.get(WARY);
	}

	// ---------------------------------------------------------------- the Shared Wake school

	/** Binds this member to a school: slot 0 leads; the loop runs round {@code centre} at its height. */
	public void joinSchool(UUID school, int slot, BlockPos centre, int radius) {
		this.school = school;
		this.schoolSlot = slot;
		this.schoolCentre = centre.immutable();
		this.schoolRadius = radius;
		this.schoolWaypoint = 0;
	}

	@Nullable
	public UUID school() {
		return school;
	}

	public boolean isSchoolLeader() {
		return school != null && schoolSlot == 0;
	}

	/** Takes the lead of its school (its leader is gone). */
	public void leadSchool() {
		this.schoolSlot = 0;
	}

	/** Gives up the lead and swims at {@code slot} behind the school's leader. */
	public void followSchool(int slot) {
		this.schoolSlot = slot;
	}

	public int schoolSlot() {
		return schoolSlot;
	}

	public BlockPos schoolCentre() {
		return schoolCentre;
	}

	public int schoolRadius() {
		return schoolRadius;
	}

	public int schoolWaypoint() {
		return schoolWaypoint;
	}

	public void advanceWaypoint() {
		schoolWaypoint = (schoolWaypoint + 1) % CourtSchoolRules.waypoints(schoolRadius);
	}

	/** Whether a player rested here within the cooldown; records the rest when not. */
	public boolean tryRest(Player player) {
		long now = level().getGameTime();
		Long last = restedAt.get(player.getUUID());
		if (last != null && now - last < REST_COOLDOWN) return false;
		restedAt.put(player.getUUID(), now);
		return true;
	}

	/** Whether a Head-tender may search for a crown to call now (it backs off after finding none). */
	public boolean crownScanDue(long gameTime) {
		return gameTime >= nextCrownScan;
	}

	public void deferCrownScan(long until) {
		nextCrownScan = until;
	}

	/** Turns to {@code player} and speaks for a few seconds (the talk pose). */
	public void speakTo(Player player) {
		getLookControl().setLookAt(player);
		entityData.set(TALK, TALK_TICKS);
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (!player.level().isClientSide && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer serverPlayer) {
			if (!CourtLife.isShunned(player)) speakTo(player);
			DialogueTree tree = progressionDialogue(serverPlayer);
			if (!CourtLife.isShunned(player)) {
				tree = DialogueItemInquiryNodes.withInventoryItemInquiries(tree, serverPlayer, progressionDialogueId(),
						HemoCapabilityAccess.getPlayerDegreeNumber(player), 0f);
			}
			tree = DialogueHubFactory.decorate(tree, progressionDialogueId(), serverPlayer);
			PacketHandler.sendToPlayer(serverPlayer, new OpenDialoguePacket(tree));
		}
		return InteractionResult.sidedSuccess(player.level().isClientSide);
	}

	@Override
	public DialogueTree progressionDialogue(ServerPlayer player) {
		return CourtMemberDialogue.forMember(this, player);
	}

	@Override
	public String progressionDialogueId() {
		return role.entityId();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (home != null) tag.put("Home", NbtUtils.writeBlockPos(home));
		tag.putFloat("HomeYaw", homeYaw);
		tag.putString("Mark", mark);
		if (school != null) {
			CompoundTag wake = new CompoundTag();
			wake.putUUID("Id", school);
			wake.putInt("Slot", schoolSlot);
			wake.put("Centre", NbtUtils.writeBlockPos(schoolCentre));
			wake.putInt("Radius", schoolRadius);
			wake.putInt("Waypoint", schoolWaypoint);
			tag.put("School", wake);
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		BlockPos saved = NbtUtils.readBlockPos(tag, "Home").orElse(null);
		if (saved != null) settle(saved, tag.getFloat("HomeYaw"), tag.getString("Mark"));
		if (tag.contains("School", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
			CompoundTag wake = tag.getCompound("School");
			joinSchool(wake.getUUID("Id"), wake.getInt("Slot"), NbtUtils.readBlockPos(wake, "Centre").orElse(blockPosition()),
					wake.getInt("Radius"));
			this.schoolWaypoint = wake.getInt("Waypoint");
		}
	}
}
