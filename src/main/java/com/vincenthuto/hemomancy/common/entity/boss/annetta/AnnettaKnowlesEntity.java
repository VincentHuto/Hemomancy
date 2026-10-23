package com.vincenthuto.hemomancy.common.entity.boss.annetta;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaEncounterRules.Move;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.AnnettaDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.projectile.HemolyticVialEntity;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import com.vincenthuto.hemomancy.common.entity.boss.BossBarBinding;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;
import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;

/**
 * Annetta Knowles. Found cowering in the broken church; a Harbinger who unseals a jar of Tooth Pecks drives her into
 * a frantic Phase 1 that ends with her own dagger in the swollen tick at her neck and her body rising as the Stained
 * Priestess. An Unstained with Clarity can instead give her the Draught of Still Mercy: the infection is torn out of
 * her as the Latent Infection, and she fights it beside them, healing them, until it dies.
 */
public class AnnettaKnowlesEntity extends Monster {
    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(AnnettaKnowlesEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTION =
            SynchedEntityData.defineId(AnnettaKnowlesEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTION_TICK =
            SynchedEntityData.defineId(AnnettaKnowlesEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SCENE_TICK =
            SynchedEntityData.defineId(AnnettaKnowlesEntity.class, EntityDataSerializers.INT);

    public static final int ACTION_NONE = 0;
    public static final int ACTION_SCREAM = 1;
    public static final int ACTION_DAGGER_COMBO = 2;
    public static final int ACTION_STILL_DASH = 3;
    public static final int ACTION_STILL_BOLT = 4;
    public static final int ACTION_SHIELD_GUARD = 5;
    public static final int ACTION_SILVER_AURA = 6;
    public static final int ACTION_FLINCH = 7;
    public static final int ACTION_HEAL = 8;
    public static final int ACTION_ALLY_BOLT = 9;
    public static final int ACTION_ALLY_STRIKE = 10;
    public static final int ACTION_SHIELD_BASH = 11;

    private static final int SCREAM_TICKS = 30;
    private static final int FLINCH_TICKS = 8;
    private static final int HEAL_TICKS = 24;
    private static final int ALLY_BOLT_TICKS = 18;
    private static final int ALLY_STRIKE_TICKS = 22;
    private static final int ALLY_DASH_TICKS = 12;
    private static final double SILVER_AURA_RADIUS = 6.0D;
    private static final ParticleColor PALE = new ParticleColor(214, 234, 246);
    private static final ParticleColor ICHOR = new ParticleColor(214, 150, 48);
    private static final ParticleColor BLOOD = new ParticleColor(140, 12, 26);
    private static final ParticleColor HAIR = new ParticleColor(168, 40, 30);
    private static final ParticleColor SKIN = new ParticleColor(226, 196, 184);
    private static final int FRANTIC_LINES = 8;
    private static final int ALLY_LINES = 4;

    private final BossBarBinding bossBar = new BossBarBinding("entity.hemomancy.annetta_knowles", BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);

    private int phaseOneTicks;
    private float damageTaken;
    private int moveCooldown = 20;
    private int lineCooldown = 120;
    private int lastHurtTick = -100;
    private int lastSpillTick = -100;
    private int lastBlockTick = -100;
    private int healCooldown = 60;
    private int boltCooldown = 40;
    @Nullable private UUID infection;
    @Nullable private UUID reanimationPool;
    @Nullable private UUID purifier;
    private final java.util.List<UUID> props = new java.util.ArrayList<>();
    private int foeMissingTicks;
    /** Which way she circles her foe, and how long until she changes her mind. */
    private int strafeDir = 1;
    private int strafeTicks = 40;

    // Client clocks: the synced action and scene ticks arrive on the network's schedule, not every client tick, so
    // the model reads these instead. They advance with the client's own ticks and resync when the server disagrees.
    private final BossReplayClock actionClock = new BossReplayClock(ACTION_NONE);
    private final BossReplayClock sceneClock = new BossReplayClock();
    /** Review fixtures pin a pose: the model then reads the synced ticks exactly. */
    public static boolean reviewHoldClock;

    /** The yaw a staged scene is held at; NaN when no scene is holding her. */
    private float sceneYaw = Float.NaN;

    public AnnettaKnowlesEntity(EntityType<? extends AnnettaKnowlesEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 100;
        this.bossBar.setVisible(false);
        // Her dagger is held for the render; the cure route's dagger reward is its own drop.
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setLeftHanded(false);
    }

    public static AttributeSupplier.Builder setAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 350.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.ARMOR, 8.0D);
    }

    @Override
    protected void registerGoals() {
        // Movement and attacks are driven from aiStep by her encounter state; goals only keep her afloat and looking.
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Idle looking only while she is calm: in a fight these goals pull her head away from her foe every few ticks.
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F) {
            @Override public boolean canUse() { return calm() && super.canUse(); }
            @Override public boolean canContinueToUse() { return calm() && super.canContinueToUse(); }
        });
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this) {
            @Override public boolean canUse() { return calm() && super.canUse(); }
            @Override public boolean canContinueToUse() { return calm() && super.canContinueToUse(); }
        });
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, EncounterState.COWERING.id);
        builder.define(DATA_ACTION, ACTION_NONE);
        builder.define(DATA_ACTION_TICK, 0);
        builder.define(DATA_SCENE_TICK, 0);
    }

    // ---------------------------------------------------------------- state

    private boolean calm() {
        EncounterState state = getEncounterState();
        return state == EncounterState.COWERING || state == EncounterState.RESOLVED;
    }

    /** Ticks into the current action, smooth across frames on the client. */
    public float actionTime(float partialTick) {
        if (!this.level().isClientSide || reviewHoldClock) return getActionTick();
        return actionClock.at(this.tickCount, partialTick);
    }

    /** Ticks into the current staged scene (the break or the purge), smooth across frames on the client. */
    public float sceneTime(float partialTick) {
        if (!this.level().isClientSide || reviewHoldClock) return getSceneTick();
        return sceneClock.at(this.tickCount, partialTick);
    }

    private void tickClientClocks() {
        actionClock.sync(getAction(), getActionTick(), this.tickCount, 1);
        sceneClock.sync(getEncounterState().ordinal(), getSceneTick(), this.tickCount, 1);
    }

    public EncounterState getEncounterState() {
        return EncounterState.byId(this.entityData.get(DATA_STATE));
    }

    private void setEncounterState(EncounterState state) {
        this.entityData.set(DATA_STATE, state.id);
        this.entityData.set(DATA_SCENE_TICK, 0);
        this.bossBar.setVisible(state == EncounterState.PHASE_ONE || state == EncounterState.BREAKING);
    }

    public int getAction() {
        return this.entityData.get(DATA_ACTION);
    }

    public int getActionTick() {
        return this.entityData.get(DATA_ACTION_TICK);
    }

    public int getSceneTick() {
        return this.entityData.get(DATA_SCENE_TICK);
    }

    private void setAction(int action, int tick) {
        this.entityData.set(DATA_ACTION, action);
        this.entityData.set(DATA_ACTION_TICK, tick);
    }

    /** The head is gone from the moment the tick bursts. */
    public boolean isHeadless() {
        return getEncounterState() == EncounterState.BREAKING && AnnettaEncounterRules.headless(getSceneTick());
    }

    public void setCowering() {
        this.setEncounterState(EncounterState.COWERING);
    }

    public boolean isCuredSupport() {
        return this.getEncounterState() == EncounterState.CURED_SUPPORT;
    }

    public boolean isPurging() {
        return this.getEncounterState() == EncounterState.PURGING;
    }

    /** The infection died before the purge finished; she finishes it, then is free. */
    public void infectionDestroyed() {
        this.infection = null;
    }

    public void markResolvedAfterCure() {
        this.setEncounterState(EncounterState.RESOLVED);
        this.setAction(ACTION_NONE, 0);
        this.setTarget(null);
        this.getNavigation().stop();
        if (this.level() instanceof ServerLevel server) {
            broadcastNearby(server, Component.translatable("hemomancy.annetta.event.resolved")
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        }
    }

    /** The infection lashes out at the body it was torn from; she reels but it cannot take her again. */
    public void staggerFrom(LivingEntity attacker) {
        if (getEncounterState() != EncounterState.CURED_SUPPORT) return;
        setAction(ACTION_FLINCH, 0);
        Vec3 away = position().subtract(attacker.position()).multiply(1.0D, 0.0D, 1.0D);
        if (away.lengthSqr() > 1.0E-4D) {
            away = away.normalize().scale(0.6D);
            this.push(away.x, 0.2D, away.z);
            this.hasImpulse = true;
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        EncounterState state = this.getEncounterState();
        if (state == EncounterState.PHASE_ONE || state == EncounterState.BREAKING) {
            this.bossBar.show(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossBar.hide(player);
    }

    // ---------------------------------------------------------------- interaction and dialogue

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        EncounterState state = this.getEncounterState();
        if (state == EncounterState.COWERING) {
            ItemStack held = player.getMainHandItem();
            if (isToothPecksJar(held) && isHarbinger(player)) {
                openDialogue(serverPlayer, AnnettaDialogueTrees.coweringJar(this.getId()));
                return InteractionResult.CONSUME;
            }
            if (held.is(ItemInit.draught_of_still_mercy.get()) && canPurify(player)) {
                openDialogue(serverPlayer, AnnettaDialogueTrees.offerDraught(this.getId()));
                return InteractionResult.CONSUME;
            }
            openDialogue(serverPlayer, chooseCoweringDialogue(player));
            return InteractionResult.CONSUME;
        }

        if (state == EncounterState.CURED_SUPPORT) {
            openDialogue(serverPlayer, AnnettaDialogueTrees.curedSupport(this.getId()));
            return InteractionResult.CONSUME;
        }
        if (state == EncounterState.RESOLVED) {
            openDialogue(serverPlayer, AnnettaDialogueTrees.resolved(this.getId()));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    /** Runs a dialogue choice; re-checks everything the dialogue promised, since the player may have moved on. */
    public boolean handleDialogueChoice(ServerPlayer player, String eventId) {
        if (getEncounterState() != EncounterState.COWERING || player.distanceToSqr(this) > 64.0D) return false;
        ItemStack held = player.getMainHandItem();
        if (AnnettaDialogueTrees.EVENT_UNSEAL_JAR.equals(eventId) && isToothPecksJar(held) && isHarbinger(player)) {
            startHarbingerRoute(player, held);
            return getEncounterState() == EncounterState.PHASE_ONE;
        }
        if (AnnettaDialogueTrees.EVENT_OFFER_DRAUGHT.equals(eventId) && held.is(ItemInit.draught_of_still_mercy.get())
                && canPurify(player)) {
            startCureRoute(player, held);
            return getEncounterState() == EncounterState.PURGING;
        }
        return false;
    }

    private DialogueTree chooseCoweringDialogue(Player player) {
        if (canPurify(player)) {
            return AnnettaDialogueTrees.coweringUnstainedHint(this.getId());
        }
        if (isHarbinger(player)) {
            return AnnettaDialogueTrees.coweringHarbingerHint(this.getId());
        }
        return AnnettaDialogueTrees.coweringNeutral(this.getId());
    }

    private void openDialogue(ServerPlayer player, DialogueTree tree) {
        PacketHandler.sendToPlayer(player, new OpenDialoguePacket(tree));
    }

    // ---------------------------------------------------------------- route starts

    private void startHarbingerRoute(ServerPlayer player, ItemStack jarStack) {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        CompoundTag specimen = SpecimenJarData.getSpecimen(jarStack);
        if (SpecimenJarData.releaseSpecimen(server,
                this.blockPosition().relative(player.getDirection().getOpposite()), specimen).isEmpty()) return;
        SpecimenJarData.clearSpecimen(jarStack);

        this.setEncounterState(EncounterState.PHASE_ONE);
        this.setHealth(this.getMaxHealth());
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.annettas_absolution_dagger.get()));
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ItemInit.unstained_shield.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        this.setTarget(player);
        this.phaseOneTicks = 0;
        this.damageTaken = 0.0F;
        this.moveCooldown = SCREAM_TICKS + 10;
        this.setAction(ACTION_SCREAM, 0);
        addNearbyPlayersToBossBar(server);

        server.playSound(null, this.blockPosition(), SoundEvents.GENERIC_HURT, SoundSource.HOSTILE, 1.2F, 1.3F);
        server.sendParticles(HemoParticleData.bloodCell(BLOOD), this.getX(), this.getEyeY() - 0.3D, this.getZ(),
                16, 0.2D, 0.2D, 0.2D, 0.05D);
        broadcastNearby(server, Component.translatable("hemomancy.annetta.event.bitten")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
    }

    private void startCureRoute(ServerPlayer player, ItemStack brewStack) {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        LatentAnnettaInfectionEntity infection = EntityInit.latent_annetta_infection.get().create(server);
        if (infection == null) return;
        infection.setCuredAnnetta(this.getUUID());
        if (!placeInfection(server, infection)) {
            player.displayClientMessage(Component.translatable("hemomancy.annetta.event.no_room")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            return;
        }
        infection.setTarget(player);
        infection.setPersistenceRequired();
        infection.beginEmerging();
        if (!server.addFreshEntity(infection)) return;

        if (!player.getAbilities().instabuild) {
            brewStack.shrink(1);
        }
        this.infection = infection.getUUID();
        this.purifier = player.getUUID();
        this.setEncounterState(EncounterState.PURGING);
        this.setHealth(this.getMaxHealth());
        this.setTarget(null);
        this.getNavigation().stop();
        this.lookAt(player, 30.0F, 30.0F);

        server.playSound(null, this.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.NEUTRAL, 1.0F, 0.9F);
        speak(server, "hemomancy.annetta.purge.drink", ChatFormatting.AQUA);
    }

    /** The infection towers over her; find it room she can see, inside the church, before the draught is spent. */
    private boolean placeInfection(ServerLevel server, LatentAnnettaInfectionEntity infection) {
        infection.moveTo(this.getX() + 2.0D, this.getY(), this.getZ() + 2.0D, this.getYRot(), 0.0F);
        if (roomFor(server, infection)) return true;
        for (int radius = 2; radius <= 6; radius++) {
            for (int step = 0; step < 8; step++) {
                double angle = step * Math.PI / 4.0D;
                for (int dy = -1; dy <= 1; dy++) {
                    infection.moveTo(this.getX() + Math.cos(angle) * radius, this.getY() + dy,
                            this.getZ() + Math.sin(angle) * radius, this.getYRot(), 0.0F);
                    if (roomFor(server, infection)) return true;
                }
            }
        }
        return false;
    }

    private boolean roomFor(ServerLevel server, LatentAnnettaInfectionEntity infection) {
        if (!server.noCollision(infection)) return false;
        Vec3 to = infection.position().add(0.0D, 1.0D, 0.0D);
        return server.clip(new ClipContext(this.getEyePosition(), to, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    // ---------------------------------------------------------------- ticking

    @Override
    public void aiStep() {
        super.aiStep();
        holdSceneFacing();
        if (this.level().isClientSide) {
            tickClientClocks();
            return;
        }
        ServerLevel server = (ServerLevel) this.level();
        if (getAction() != ACTION_NONE) {
            int tick = getActionTick() + 1;
            if (tick >= actionLength(getAction())) setAction(ACTION_NONE, 0);
            else this.entityData.set(DATA_ACTION_TICK, tick);
        }
        EncounterState state = this.getEncounterState();
        switch (state) {
            case COWERING, RESOLVED -> {
                this.setTarget(null);
                this.getNavigation().stop();
            }
            case PHASE_ONE -> tickPhaseOne(server);
            case BREAKING -> tickBreak(server);
            case PURGING -> tickPurge(server);
            case CURED_SUPPORT -> tickAlly(server);
        }
        this.bossBar.trackHealth(this);
    }

    /** Cowering, the break and the purge are staged: she must not turn to follow players' eyes while they play. */
    static boolean holdsFacing(EncounterState state) {
        return state == EncounterState.COWERING || state == EncounterState.BREAKING || state == EncounterState.PURGING;
    }

    /**
     * Pins body, head and yaw to where she faced when the scene began. Runs after the look goals and look control
     * on the server, and on the client too so the body never eases round toward a stale head yaw.
     */
    private void holdSceneFacing() {
        if (!holdsFacing(getEncounterState())) {
            sceneYaw = Float.NaN;
            return;
        }
        if (Float.isNaN(sceneYaw)) sceneYaw = this.level().isClientSide ? this.getYRot() : this.yBodyRot;
        this.setYRot(sceneYaw);
        this.yRotO = sceneYaw;
        this.yBodyRot = sceneYaw;
        this.yBodyRotO = sceneYaw;
        this.yHeadRot = sceneYaw;
        this.yHeadRotO = sceneYaw;
    }

    private int actionLength(int action) {
        return switch (action) {
            case ACTION_SCREAM -> SCREAM_TICKS;
            case ACTION_DAGGER_COMBO -> AnnettaEncounterRules.duration(Move.DAGGER_COMBO);
            case ACTION_STILL_DASH -> getEncounterState() == EncounterState.CURED_SUPPORT ? ALLY_DASH_TICKS
                    : AnnettaEncounterRules.duration(Move.STILL_DASH);
            case ACTION_STILL_BOLT -> AnnettaEncounterRules.duration(Move.STILL_BOLT);
            case ACTION_SHIELD_GUARD -> AnnettaEncounterRules.duration(Move.SHIELD_GUARD);
            case ACTION_SILVER_AURA -> AnnettaEncounterRules.duration(Move.SILVER_AURA);
            case ACTION_FLINCH -> FLINCH_TICKS;
            case ACTION_HEAL -> HEAL_TICKS;
            case ACTION_ALLY_BOLT -> ALLY_BOLT_TICKS;
            case ACTION_ALLY_STRIKE -> ALLY_STRIKE_TICKS;
            case ACTION_SHIELD_BASH -> AnnettaEncounterRules.SHIELD_BASH_TICKS;
            default -> 1;
        };
    }

    // ---------------------------------------------------------------- Phase 1

    private void tickPhaseOne(ServerLevel server) {
        this.phaseOneTicks++;
        LivingEntity target = acquireTarget(server);
        if (AnnettaEncounterRules.breakReady(phaseOneTicks, damageTaken) && getAction() != ACTION_DAGGER_COMBO) {
            startBreak(server);
            return;
        }
        if (--lineCooldown <= 0) {
            speak(server, "hemomancy.annetta.frantic." + this.random.nextInt(FRANTIC_LINES), ChatFormatting.DARK_RED);
            lineCooldown = 220 + this.random.nextInt(180);
        }
        if (target == null) {
            this.getNavigation().stop();
            return;
        }
        this.getLookControl().setLookAt(target, 40.0F, 40.0F);
        int action = getAction();
        if (action != ACTION_NONE) {
            performAction(server, target, action, getActionTick());
            return;
        }
        double distance = this.distanceTo(target);
        footwork(target, distance, 4.5D, 2.2D, 3.4D, 1.15D);
        if (--moveCooldown > 0) return;
        boolean recentlyHurt = this.tickCount - lastHurtTick < 30;
        Move move = AnnettaEncounterRules.choose(distance, recentlyHurt, this.random.nextInt(100));
        moveCooldown = AnnettaEncounterRules.duration(move)
                + AnnettaEncounterRules.cooldownAfter(move, getHealth() / getMaxHealth());
        setAction(switch (move) {
            case DAGGER_COMBO -> ACTION_DAGGER_COMBO;
            case STILL_DASH -> ACTION_STILL_DASH;
            case STILL_BOLT -> ACTION_STILL_BOLT;
            case SHIELD_GUARD -> ACTION_SHIELD_GUARD;
            case SILVER_AURA -> ACTION_SILVER_AURA;
        }, 0);
        performAction(server, target, getAction(), 0);
    }

    @Nullable
    private LivingEntity acquireTarget(ServerLevel server) {
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && target.distanceToSqr(this) < 48.0D * 48.0D
                && !(target instanceof Player p && (p.isSpectator() || p.isCreative()))) {
            return target;
        }
        Player nearest = server.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 32.0D,
                entity -> entity instanceof Player p && !p.isSpectator() && !p.isCreative() && p.isAlive());
        this.setTarget(nearest);
        return nearest;
    }

    private void performAction(ServerLevel server, LivingEntity target, int action, int tick) {
        switch (action) {
            case ACTION_SCREAM -> {
                this.getNavigation().stop();
                if (tick % 6 == 0) {
                    server.sendParticles(HemoParticleData.bloodCell(BLOOD), getX(), getEyeY() - 0.35D, getZ(), 4, 0.15D, 0.1D, 0.15D, 0.02D);
                }
            }
            case ACTION_DAGGER_COMBO -> {
                this.getNavigation().stop();
                // A lunge step lands each cut; the overhead finisher steps in hardest.
                if (tick == 4 || tick == 12 || tick == 19) stepToward(target, tick == 19 ? 0.45D : 0.3D);
                if (tick == 6 || tick == 14 || tick == 22) daggerStrike(server, target, tick == 22 ? 7.0F : 5.0F, tick == 22);
            }
            case ACTION_STILL_DASH -> {
                if (tick == 2) stillDash(server, target);
                if (tick > 2 && tick < 9) {
                    server.sendParticles(HemoParticleData.glow(PALE), getX(), getY() + 0.9D, getZ(), 3, 0.2D, 0.4D, 0.2D, 0.0D);
                }
            }
            case ACTION_STILL_BOLT -> {
                this.getNavigation().stop();
                if (tick == 9) throwHemolyticVial(server, target);
            }
            case ACTION_SHIELD_GUARD -> this.getNavigation().stop();
            case ACTION_SHIELD_BASH -> {
                this.getNavigation().stop();
                if (tick == AnnettaEncounterRules.SHIELD_BASH_HIT - 2) stepToward(target, 0.5D);
                if (tick == AnnettaEncounterRules.SHIELD_BASH_HIT) shieldBash(server, target);
            }
            case ACTION_SILVER_AURA -> {
                this.getNavigation().stop();
                if (tick == 8) silverAura(server);
            }
            default -> {
            }
        }
    }

    /**
     * Close the gap when far; inside the band she circles, edging in or out, and only changes direction every few
     * seconds, so she reads as stalking rather than jittering on the spot.
     */
    private void footwork(LivingEntity foe, double distance, double approach, double tooClose, double tooFar, double speed) {
        if (--strafeTicks <= 0) {
            strafeDir = -strafeDir;
            strafeTicks = 35 + this.random.nextInt(45);
        }
        if (distance > approach) {
            this.getNavigation().moveTo(foe, speed);
            return;
        }
        this.getNavigation().stop();
        float forward = distance < tooClose ? -0.45F : distance > tooFar ? 0.4F : 0.0F;
        this.getMoveControl().strafe(forward, 0.42F * strafeDir);
        this.lookAt(foe, 30.0F, 30.0F);
    }

    private void stepToward(LivingEntity target, double amount) {
        Vec3 toward = target.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
        if (toward.lengthSqr() < 1.0E-4D) return;
        this.setDeltaMovement(this.getDeltaMovement().add(toward.normalize().scale(amount)));
        this.hasImpulse = true;
    }

    /** The cut's arc is drawn client side from the blade itself; the server adds the bite where it lands. */
    private void daggerStrike(ServerLevel server, LivingEntity target, float damage, boolean finisher) {
        server.playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 0.9F, finisher ? 0.9F : 1.45F);
        Vec3 facing = Vec3.directionFromRotation(0.0F, this.yBodyRot);
        if (target.distanceTo(this) > 3.4D) return;
        Vec3 toTarget = target.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D).normalize();
        if (facing.dot(toTarget) < 0.2D) return;
        if (target.hurt(this.damageSources().mobAttack(this), damage)) {
            cutBite(server, target, toTarget, finisher);
            if (finisher) {
                server.playSound(null, target.blockPosition(), SoundEvents.TRIDENT_HIT, SoundSource.HOSTILE, 1.0F, 1.3F);
                target.knockback(0.6D, -toTarget.x, -toTarget.z);
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, true));
            }
        }
    }

    /** Where a cut lands: a pale flash at the wound and blood thrown on along the blow. */
    private void cutBite(ServerLevel server, LivingEntity target, Vec3 along, boolean heavy) {
        Vec3 at = target.position().add(0.0D, target.getBbHeight() * 0.6D, 0.0D).subtract(along.scale(target.getBbWidth() * 0.5D));
        server.sendParticles(HemoParticleData.hitGlow(PALE), at.x, at.y, at.z, heavy ? 10 : 5, 0.15D, 0.15D, 0.15D, 0.0D);
        spray(server, at, along, HemoParticleData.bloodCell(BLOOD), heavy ? 14 : 7, 0.35D, 0.45D);
    }

    /** The counter after a deflected blow: the shield driven into them, flinging them back. */
    private void shieldBash(ServerLevel server, LivingEntity target) {
        Vec3 facing = Vec3.directionFromRotation(0.0F, this.yBodyRot);
        Vec3 face = position().add(facing.scale(0.7D)).add(0.0D, 1.15D, 0.0D);
        server.playSound(null, blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 1.1F, 0.6F);
        server.playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 1.0F, 1.4F);
        wardRing(server, face, facing, 0.55D, 18);
        server.sendParticles(HemoParticleData.hitGlow(PALE), face.x, face.y, face.z, 4, 0.1D, 0.1D, 0.1D, 0.0D);
        if (target.distanceTo(this) > 2.8D) return;
        Vec3 toTarget = target.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D).normalize();
        if (facing.dot(toTarget) < 0.3D) return;
        if (target.hurt(this.damageSources().mobAttack(this), 4.0F)) {
            target.knockback(1.1D, -toTarget.x, -toTarget.z);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, true));
        }
    }

    /** A ring of still-water motes standing on the shield face and flung outward: the deflection read at a glance. */
    private void wardRing(ServerLevel server, Vec3 centre, Vec3 facing, double radius, int points) {
        Vec3 side = new Vec3(-facing.z, 0.0D, facing.x).normalize();
        for (int i = 0; i < points; i++) {
            double a = i * Math.PI * 2.0D / points;
            Vec3 out = side.scale(Math.cos(a)).add(0.0D, Math.sin(a), 0.0D);
            Vec3 at = centre.add(out.scale(radius));
            server.sendParticles(HemoParticleData.glow(PALE), at.x, at.y, at.z, 0, out.x + facing.x * 0.6D, out.y,
                    out.z + facing.z * 0.6D, 0.12D);
        }
    }

    /** Particles thrown along a direction, fanned by spread; count 0 makes sendParticles read the offset as velocity. */
    private void spray(ServerLevel server, Vec3 at, Vec3 direction, ParticleOptions particle, int count, double speed, double spread) {
        for (int i = 0; i < count; i++) {
            Vec3 d = direction.add((this.random.nextDouble() - 0.5D) * spread * 2.0D, (this.random.nextDouble() - 0.3D) * spread * 2.0D,
                    (this.random.nextDouble() - 0.5D) * spread * 2.0D).normalize();
            server.sendParticles(particle, at.x, at.y, at.z, 0, d.x, d.y, d.z, speed * (0.6D + this.random.nextDouble() * 0.6D));
        }
    }

    /** Still arts for movement: she slips across the floor like water poured out, leaving humor behind. */
    private void stillDash(ServerLevel server, LivingEntity target) {
        Vec3 toTarget = target.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
        double distance = toTarget.length();
        if (distance < 1.0E-3D) return;
        toTarget = toTarget.normalize();
        Vec3 side = new Vec3(-toTarget.z, 0.0D, toTarget.x).scale(this.random.nextBoolean() ? 1.0D : -1.0D);
        Vec3 direction = distance > 6.0D ? toTarget.add(side.scale(0.35D))
                : distance < 3.0D ? side.add(toTarget.scale(-0.6D)) : side.add(toTarget.scale(0.3D));
        if (this.random.nextFloat() < 0.4F) spill(server, AnnettaPoolEntity.Kind.HUMOR, 0.8F);
        this.setDeltaMovement(direction.normalize().scale(1.25D).add(0.0D, 0.18D, 0.0D));
        this.hasImpulse = true;
        server.playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.0F, 1.6F);
    }

    private void throwHemolyticVial(ServerLevel server, LivingEntity target) {
        HemolyticVialEntity vial = new HemolyticVialEntity(server, this);
        vial.setPos(this.getX(), this.getEyeY() - 0.1D, this.getZ());
        Vec3 delta = target.getEyePosition().subtract(vial.position());
        vial.shoot(delta.x, delta.y + delta.horizontalDistance() * 0.08D, delta.z, 0.95F, 4.0F);
        server.addFreshEntity(vial);
        server.playSound(null, this.blockPosition(), SoundEvents.SPLASH_POTION_THROW, SoundSource.HOSTILE, 1.0F, 0.8F);
    }

    private void silverAura(ServerLevel server) {
        ring(server, SILVER_AURA_RADIUS, HemoParticleData.glow(PALE), 36, getY() + 0.1D);
        server.sendParticles(HemoParticleData.glow(PALE), getX(), getY() + 1.0D, getZ(), 24, 1.6D, 0.6D, 1.6D, 0.01D);
        server.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1.2F, 1.6F);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, new AABB(this.blockPosition()).inflate(SILVER_AURA_RADIUS),
                e -> e != this && e.isAlive())) {
            boolean awakeBlood = !(victim instanceof Player player) || ProgressionGates.isBloodActive(player);
            if (awakeBlood) {
                victim.hurt(this.damageSources().magic(), 3.0F);
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, true));
            }
        }
    }

    private void ring(ServerLevel server, double radius, ParticleOptions particle, int points, double y) {
        for (int i = 0; i < points; i++) {
            double angle = i * Math.PI * 2.0D / points;
            server.sendParticles(particle, getX() + Math.cos(angle) * radius, y, getZ() + Math.sin(angle) * radius,
                    1, 0.02D, 0.02D, 0.02D, 0.0D);
        }
    }

    /** Infected blood and Unstained humor are knocked out of her as she is cut and as she moves. */
    private void spill(ServerLevel server, AnnettaPoolEntity.Kind kind, float radius) {
        if (this.tickCount - lastSpillTick < 12) return;
        lastSpillTick = this.tickCount;
        Vec3 at = position().add((this.random.nextDouble() - 0.5D) * 1.4D, 0.0D, (this.random.nextDouble() - 0.5D) * 1.4D);
        AnnettaPoolEntity.spawn(server, groundAt(server, at), kind, radius * (0.8F + this.random.nextFloat() * 0.5F), 900);
    }

    private Vec3 groundAt(ServerLevel server, Vec3 at) {
        var hit = server.clip(new ClipContext(at.add(0.0D, 1.0D, 0.0D), at.add(0.0D, -3.0D, 0.0D),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return hit.getType() == HitResult.Type.MISS ? at : hit.getLocation();
    }

    // ---------------------------------------------------------------- the break

    private void startBreak(ServerLevel server) {
        this.setEncounterState(EncounterState.BREAKING);
        this.setAction(ACTION_NONE, 0);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        broadcastNearby(server, Component.translatable("hemomancy.annetta.event.falter")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
    }

    private void tickBreak(ServerLevel server) {
        int t = getSceneTick() + 1;
        this.entityData.set(DATA_SCENE_TICK, t);
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
        if (t == AnnettaEncounterRules.SHIELD_DROP) dropProp(server, EquipmentSlot.OFFHAND);
        if (t == AnnettaEncounterRules.LAST_WORDS) speak(server, "hemomancy.annetta.break.last_words", ChatFormatting.DARK_RED);
        Vec3 facing = Vec3.directionFromRotation(0.0F, this.yBodyRot);
        Vec3 side = new Vec3(-facing.z, 0.0D, facing.x);
        // Her left side of the neck, where the tick hangs (her left is the facing's right in world space).
        Vec3 tick = position().add(0.0D, 1.48D, 0.0D).subtract(side.scale(0.27D));
        if (t == AnnettaEncounterRules.GRIP) {
            server.playSound(null, blockPosition(), SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.HOSTILE, 0.6F, 1.4F);
        }
        if (t == AnnettaEncounterRules.STAB) {
            server.playSound(null, blockPosition(), SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH, SoundSource.HOSTILE, 1.2F, 0.6F);
            server.playSound(null, blockPosition(), SoundEvents.TRIDENT_HIT, SoundSource.HOSTILE, 0.9F, 0.7F);
            spray(server, tick, side.reverse().add(0.0D, 0.3D, 0.0D), HemoParticleData.bloodCell(ICHOR), 8, 0.18D, 0.4D);
        }
        if (t > AnnettaEncounterRules.STAB && t < AnnettaEncounterRules.BURST) {
            // The tick fills against the blade: ichor wells round it and it creaks as it stretches.
            if (t % 2 == 0) {
                server.sendParticles(HemoParticleData.bloodCell(ICHOR), tick.x, tick.y, tick.z, 0, -side.x * 0.2D, -0.4D, -side.z * 0.2D, 0.08D);
            }
            if (t % 5 == 1) {
                float pitch = 0.5F + 0.6F * (t - AnnettaEncounterRules.STAB) / (float) (AnnettaEncounterRules.BURST - AnnettaEncounterRules.STAB);
                server.playSound(null, blockPosition(), SoundEvents.SLIME_SQUISH_SMALL, SoundSource.HOSTILE, 0.8F, pitch);
            }
        }
        if (t == AnnettaEncounterRules.BURST) burst(server, tick, facing, side);
        if (t > AnnettaEncounterRules.BURST && t < AnnettaEncounterRules.FALL_START) {
            // Blood pumps out of the stump in falling spurts, weaker with each beat.
            float beat = 1.0F - (t - AnnettaEncounterRules.BURST) / (float) (AnnettaEncounterRules.FALL_START - AnnettaEncounterRules.BURST);
            if (t % 4 < 2) {
                Vec3 stump = position().add(0.0D, 1.52D, 0.0D);
                spray(server, stump, new Vec3(0.0D, 1.0D, 0.0D), HemoParticleData.bloodCell(BLOOD), 2 + Math.round(3 * beat), 0.22D + 0.2D * beat, 0.25D);
            }
        }
        if (t == AnnettaEncounterRules.DAGGER_DROP) dropProp(server, EquipmentSlot.MAINHAND);
        if (t == AnnettaEncounterRules.FALL_END) {
            server.playSound(null, blockPosition(), SoundEvents.PLAYER_BIG_FALL, SoundSource.HOSTILE, 1.0F, 0.8F);
            AnnettaPoolEntity pool = AnnettaPoolEntity.spawnGrowing(server, groundAt(server, position()),
                    AnnettaPoolEntity.Kind.REANIMATION, 0.4F, 2.8F, AnnettaEncounterRules.RISE - AnnettaEncounterRules.FALL_END,
                    AnnettaEncounterRules.RISE - AnnettaEncounterRules.FALL_END + 900);
            this.reanimationPool = pool.getUUID();
        }
        if (t > AnnettaEncounterRules.FALL_END && t % 4 == 0) {
            double r = AnnettaEncounterRules.reanimationPoolRadius(t);
            double a = this.random.nextDouble() * Math.PI * 2.0D;
            server.sendParticles(HemoParticleData.bloodCell(BLOOD), getX() + Math.cos(a) * r, getY() + 0.05D,
                    getZ() + Math.sin(a) * r, 2, 0.05D, 0.02D, 0.05D, 0.01D);
        }
        if (t == AnnettaEncounterRules.RISE - 60) {
            broadcastNearby(server, Component.translatable("hemomancy.annetta.event.reanimating")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
        }
        if (t >= AnnettaEncounterRules.RISE) riseAsPriestess(server);
    }

    /**
     * Her own dagger in the swollen tick: it pops, and the pressure takes her head apart from the inside. Blood and
     * ichor blow out sideways from the tick, the head's matter (hair, skin, blood) is flung up and out, spores hang.
     */
    private void burst(ServerLevel server, Vec3 tick, Vec3 facing, Vec3 side) {
        Vec3 head = position().add(0.0D, 1.78D, 0.0D);
        Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);
        spray(server, tick, side.reverse().add(0.0D, 0.25D, 0.0D), HemoParticleData.bloodCell(ICHOR), 30, 0.45D, 0.55D);
        spray(server, head, up, HemoParticleData.bloodCell(BLOOD), 60, 0.5D, 1.1D);
        spray(server, head, up, HemoParticleData.bloodCell(HAIR), 18, 0.42D, 1.0D);
        spray(server, head, up, HemoParticleData.bloodCell(SKIN), 14, 0.4D, 1.0D);
        spray(server, head, facing, HemoParticleData.bloodCell(BLOOD), 20, 0.55D, 0.6D);
        server.sendParticles(HemoParticleData.hitGlow(ICHOR), head.x, head.y, head.z, 6, 0.12D, 0.12D, 0.12D, 0.0D);
        server.sendParticles(HemoParticleData.sporiticSpore(ICHOR), head.x, head.y, head.z, 40, 0.8D, 0.6D, 0.8D, 0.03D);
        server.sendParticles(HemoParticleData.glow(PALE), head.x, head.y, head.z, 24, 0.5D, 0.4D, 0.5D, 0.06D);
        server.playSound(null, blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.6F, 0.5F);
        server.playSound(null, blockPosition(), SoundEvents.TURTLE_EGG_BREAK, SoundSource.HOSTILE, 1.4F, 0.6F);
        server.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 0.5F, 1.6F);
        for (int i = 0; i < 3; i++) {
            double a = i * Math.PI * 2.0D / 3.0D + this.random.nextDouble();
            Vec3 at = position().add(Math.cos(a) * 1.6D, 0.0D, Math.sin(a) * 1.6D);
            AnnettaPoolEntity.spawn(server, groundAt(server, at),
                    i == 1 ? AnnettaPoolEntity.Kind.HUMOR : AnnettaPoolEntity.Kind.INFECTED, 1.1F, 1200);
        }
        broadcastNearby(server, Component.translatable("hemomancy.annetta.event.burst")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
    }

    /**
     * Equipment falls from her hands as a display prop lying on the floor: nothing, not even a hopper, can collect it;
     * the real drops come from the fight's end.
     */
    private void dropProp(ServerLevel server, EquipmentSlot slot) {
        ItemStack held = getItemBySlot(slot);
        if (held.isEmpty()) return;
        Display.ItemDisplay prop = EntityType.ITEM_DISPLAY.create(server);
        if (prop != null) {
            prop.getSlot(0).set(held.copy());
            Vec3 side = Vec3.directionFromRotation(0.0F, this.yBodyRot + (slot == EquipmentSlot.OFFHAND ? 70.0F : -60.0F));
            Vec3 at = groundAt(server, position().add(side.scale(0.8D)));
            prop.moveTo(at.x, at.y + 0.04D, at.z, this.random.nextFloat() * 360.0F, 90.0F);
            server.addFreshEntity(prop);
            props.add(prop.getUUID());
        }
        setItemSlot(slot, ItemStack.EMPTY);
        server.playSound(null, blockPosition(), slot == EquipmentSlot.OFFHAND ? SoundEvents.SHIELD_BREAK : SoundEvents.ITEM_BREAK,
                SoundSource.HOSTILE, 0.8F, 0.8F);
    }

    private void riseAsPriestess(ServerLevel server) {
        StainedPriestessEntity priestess = EntityInit.stained_priestess.get().create(server);
        if (priestess == null) return;
        priestess.moveTo(this.getX(), this.getY(), this.getZ(), this.yBodyRot, 0.0F);
        priestess.setYBodyRot(this.yBodyRot);
        priestess.setYHeadRot(this.yBodyRot);
        if (this.getTarget() != null) priestess.setTarget(this.getTarget());
        priestess.setPersistenceRequired();
        priestess.beginEmerging();
        if (!server.addFreshEntity(priestess)) return;

        broadcastNearby(server, Component.translatable("hemomancy.annetta.event.mutation")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
        server.playSound(null, blockPosition(), SoundEvents.WARDEN_EMERGE, SoundSource.HOSTILE, 1.2F, 1.2F);

        clearProps(server);
        this.bossBar.hideAll();
        this.discard();
    }

    private void clearProps(ServerLevel server) {
        for (UUID id : props) {
            Entity prop = server.getEntity(id);
            if (prop != null) prop.discard();
        }
        props.clear();
    }

    // ---------------------------------------------------------------- the purge

    private void tickPurge(ServerLevel server) {
        int t = getSceneTick() + 1;
        this.entityData.set(DATA_SCENE_TICK, t);
        this.getNavigation().stop();
        if (t > AnnettaEncounterRules.DRINK_END && t < AnnettaEncounterRules.TEAR && t % 3 == 0) {
            // Red bleeding out of her while still water takes its place.
            server.sendParticles(HemoParticleData.bloodCell(BLOOD), getX(), getY() + 1.4D, getZ(), 2, 0.25D, 0.35D, 0.25D, 0.03D);
            server.sendParticles(HemoParticleData.glow(PALE), getX(), getY() + 1.0D, getZ(), 2, 0.3D, 0.5D, 0.3D, 0.01D);
        }
        if (t == 60) speak(server, "hemomancy.annetta.purge.pulling", ChatFormatting.AQUA);
        if (t == AnnettaEncounterRules.TEAR) {
            server.playSound(null, blockPosition(), SoundEvents.WARDEN_EMERGE, SoundSource.HOSTILE, 1.0F, 1.4F);
            server.sendParticles(HemoParticleData.bloodCell(ICHOR), getX(), getY() + 1.2D, getZ(), 50, 0.4D, 0.5D, 0.4D, 0.2D);
            server.sendParticles(HemoParticleData.sporiticSpore(ICHOR), getX(), getY() + 1.2D, getZ(), 30, 0.6D, 0.6D, 0.6D, 0.04D);
            broadcastNearby(server, Component.translatable("hemomancy.annetta.event.cured")
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        }
        if (t >= AnnettaEncounterRules.PURGE_END) {
            this.setEncounterState(EncounterState.CURED_SUPPORT);
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.annettas_absolution_dagger.get()));
            this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
            speak(server, "hemomancy.annetta.ally.0", ChatFormatting.AQUA);
            lineCooldown = 300;
        }
    }

    // ---------------------------------------------------------------- beside the purifier

    private void tickAlly(ServerLevel server) {
        LatentAnnettaInfectionEntity foe = infection != null
                && server.getEntity(infection) instanceof LatentAnnettaInfectionEntity latent && latent.isAlive() ? latent : null;
        // If the infection is gone (killed early, carried off, lost to the void) she does not wait for it forever.
        foeMissingTicks = foe == null ? foeMissingTicks + 1 : 0;
        if (foeMissingTicks > 200) {
            markResolvedAfterCure();
            return;
        }
        Player friend = purifier != null ? server.getPlayerByUUID(purifier) : null;
        if (friend == null || friend.distanceToSqr(this) > 32.0D * 32.0D) {
            friend = server.getNearestPlayer(this, 24.0D);
        }
        if (--healCooldown <= 0 && getAction() == ACTION_NONE) {
            healCooldown = 60;
            setAction(ACTION_HEAL, 0);
            tendWounds(server);
        }
        int action = getAction();
        if (action == ACTION_ALLY_BOLT && getActionTick() == 9 && foe != null) stillLance(server, foe);
        if (action == ACTION_ALLY_STRIKE && foe != null) {
            int at = getActionTick();
            if (at == 5 || at == 12) stepToward(foe, 0.35D);
            if ((at == 7 || at == 14) && foe.distanceTo(this) < 4.6D) {
                foe.hurt(this.damageSources().mobAttack(this), 4.0F);
                server.playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.NEUTRAL, 0.9F, 1.5F);
                cutBite(server, foe, foe.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D).normalize(), at == 14);
            }
            // Cut and away: she slips back out of its reach as the second cut lands.
            if (at == 16) slipFrom(server, foe);
        }
        if (action == ACTION_STILL_DASH && getActionTick() == 2 && foe != null) slipFrom(server, foe);
        if (action == ACTION_STILL_DASH && getActionTick() > 2 && getActionTick() < 9) {
            server.sendParticles(HemoParticleData.glow(PALE), getX(), getY() + 0.9D, getZ(), 2, 0.2D, 0.4D, 0.2D, 0.0D);
        }
        if (foe != null) {
            this.getLookControl().setLookAt(foe, 30.0F, 30.0F);
            double toFoe = this.distanceTo(foe);
            if (action == ACTION_NONE && --boltCooldown <= 0) {
                if (toFoe < 3.6D) {
                    setAction(ACTION_ALLY_STRIKE, 0);
                    boltCooldown = 34;
                } else if (toFoe < 16.0D && this.hasLineOfSight(foe)) {
                    setAction(ACTION_ALLY_BOLT, 0);
                    boltCooldown = 50;
                }
            } else if (action == ACTION_NONE && toFoe < 2.6D) {
                // Too close to that thing: she slips away across the floor rather than walking.
                setAction(ACTION_STILL_DASH, 0);
            }
            action = getAction();
            if (action != ACTION_NONE) {
                this.getNavigation().stop();
            } else if (friend != null && friend.distanceTo(this) > 9.0D) {
                this.getNavigation().moveTo(friend, 1.05D);
            } else {
                // Weakened, she keeps her distance and circles it, edging in only when her blade is ready.
                footwork(foe, toFoe, 9.0D, boltCooldown < 12 ? 3.0D : 5.0D, boltCooldown < 12 ? 3.4D : 7.0D, 1.0D);
            }
        } else if (friend != null && friend.distanceTo(this) > 4.0D) {
            this.getNavigation().moveTo(friend, 0.9D);
        }
        if (--lineCooldown <= 0 && foe != null) {
            speak(server, "hemomancy.annetta.ally." + (1 + this.random.nextInt(ALLY_LINES - 1)), ChatFormatting.AQUA);
            lineCooldown = 300 + this.random.nextInt(200);
        }
    }

    /** Still arts for movement beside the purifier: a slip back and aside, out of the infection's reach. */
    private void slipFrom(ServerLevel server, LivingEntity foe) {
        Vec3 away = position().subtract(foe.position()).multiply(1.0D, 0.0D, 1.0D);
        if (away.lengthSqr() < 1.0E-4D) return;
        away = away.normalize();
        Vec3 side = new Vec3(-away.z, 0.0D, away.x).scale(strafeDir);
        this.setDeltaMovement(away.add(side.scale(0.5D)).normalize().scale(0.95D).add(0.0D, 0.15D, 0.0D));
        this.hasImpulse = true;
        server.playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 0.8F, 1.7F);
    }

    /** Still arts at the infection: a lance of still water that slows it. */
    private void stillLance(ServerLevel server, LatentAnnettaInfectionEntity foe) {
        Vec3 from = getEyePosition();
        Vec3 to = foe.position().add(0.0D, 2.0D, 0.0D);
        Vec3 step = to.subtract(from).scale(1.0D / 14.0D);
        for (int i = 1; i <= 14; i++) {
            Vec3 at = from.add(step.scale(i));
            server.sendParticles(HemoParticleData.glow(PALE), at.x, at.y, at.z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
        }
        server.playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.NEUTRAL, 1.0F, 1.5F);
        foe.hurt(this.damageSources().indirectMagic(this, this), 6.0F);
        foe.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, true));
    }

    private void tendWounds(ServerLevel server) {
        AABB supportArea = new AABB(this.blockPosition()).inflate(18.0D);
        for (ServerPlayer player : server.getEntitiesOfClass(ServerPlayer.class, supportArea,
                p -> p.isAlive() && !p.isSpectator())) {
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(3.0F);
            }
            player.removeEffect(EffectInit.blood_loss);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, true));
            server.sendParticles(HemoParticleData.glow(PALE), player.getX(), player.getY() + 1.0D, player.getZ(),
                    8, 0.3D, 0.5D, 0.3D, 0.01D);
        }
    }

    // ---------------------------------------------------------------- damage

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return super.hurt(source, amount);
        EncounterState state = this.getEncounterState();
        if (state == EncounterState.BREAKING || state == EncounterState.PURGING || state == EncounterState.CURED_SUPPORT) {
            return false;
        }
        if (state == EncounterState.COWERING || state == EncounterState.RESOLVED) {
            if (source.getEntity() instanceof Player player && player.isCreative()) {
                return super.hurt(source, amount);
            }
            return false;
        }
        if (this.level().isClientSide) return super.hurt(source, amount);
        if (getAction() == ACTION_SHIELD_GUARD && blocksFrom(source)) {
            amount *= 0.2F;
            if (this.tickCount - lastBlockTick > 8 && source.getEntity() instanceof LivingEntity attacker) {
                lastBlockTick = this.tickCount;
                this.level().playSound(null, blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 1.0F, 1.2F);
                if (this.level() instanceof ServerLevel server) {
                    Vec3 facing = Vec3.directionFromRotation(0.0F, this.yBodyRot);
                    wardRing(server, position().add(facing.scale(0.6D)).add(0.0D, 1.25D, 0.0D), facing, 0.45D, 14);
                }
                // Turned aside, then answered: the guard breaks into a shield bash.
                setAction(ACTION_SHIELD_BASH, 0);
            }
        }
        float allowed = AnnettaEncounterRules.guardedDamage(getHealth(), amount);
        this.lastHurtTick = this.tickCount;
        // At one heart she still bleeds, but only landed blows count toward the break.
        boolean landed = super.hurt(source, allowed);
        if (landed) this.damageTaken += amount;
        if (this.level() instanceof ServerLevel server && (landed || allowed <= 0.0F)) {
            spill(server, this.random.nextFloat() < 0.55F ? AnnettaPoolEntity.Kind.INFECTED : AnnettaPoolEntity.Kind.HUMOR, 1.0F);
            server.sendParticles(HemoParticleData.bloodCell(ICHOR), getX(), getY() + 1.2D, getZ(), 5, 0.2D, 0.3D, 0.2D, 0.05D);
            server.sendParticles(HemoParticleData.glow(PALE), getX(), getY() + 1.2D, getZ(), 4, 0.2D, 0.3D, 0.2D, 0.02D);
        }
        if (landed && allowed > 4.0F && getAction() == ACTION_NONE) setAction(ACTION_FLINCH, 0);
        return landed;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        // She is a person first; Peaceful does not erase her.
        return false;
    }

    private boolean blocksFrom(DamageSource source) {
        Vec3 from = source.getSourcePosition();
        if (from == null) return false;
        Vec3 facing = Vec3.directionFromRotation(0.0F, this.yBodyRot);
        Vec3 toward = from.subtract(position()).multiply(1.0D, 0.0D, 1.0D);
        return toward.lengthSqr() > 1.0E-4D && facing.dot(toward.normalize()) > 0.3D;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return this.getEncounterState() == EncounterState.PHASE_ONE && super.canAttack(target);
    }

    @Override
    public boolean isPushable() {
        EncounterState state = getEncounterState();
        return state != EncounterState.BREAKING && state != EncounterState.PURGING && super.isPushable();
    }

    private void addNearbyPlayersToBossBar(ServerLevel server) {
        for (ServerPlayer player : server.getPlayers(p -> p.distanceToSqr(this) <= 96.0D * 96.0D)) {
            this.bossBar.show(player);
        }
    }

    private void broadcastNearby(ServerLevel server, Component message) {
        for (ServerPlayer player : server.getPlayers(p -> p.distanceToSqr(this) <= 48.0D * 48.0D)) {
            player.displayClientMessage(message, false);
        }
    }

    private void speak(ServerLevel server, String key, ChatFormatting colour) {
        broadcastNearby(server, Component.translatable("hemomancy.annetta.says",
                Component.translatable(key)).withStyle(colour, ChatFormatting.ITALIC));
    }

    private boolean isHarbinger(Player player) {
        return ProgressionGates.isBloodActive(player) && ProgressionGates.hasDegree(player, 1);
    }

    private boolean canPurify(Player player) {
        return HemoCapabilityAccess.getUnstainedProgress(player).hasClarityUnlocked() && !isHarbinger(player);
    }

    private boolean isToothPecksJar(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(com.vincenthuto.hemomancy.common.init.BlockInit.specimen_jar.get().asItem())) {
            return false;
        }
        CompoundTag specimen = SpecimenJarData.getSpecimen(stack);
        return EntityType.byString(specimen.getString("id"))
                .map(type -> type == EntityInit.tooth_pecks.get())
                .orElse(false);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("EncounterState", this.getEncounterState().name());
        tag.putInt("SceneTick", getSceneTick());
        tag.putInt("PhaseOneTicks", phaseOneTicks);
        tag.putFloat("DamageTaken", damageTaken);
        if (infection != null) tag.putUUID("Infection", infection);
        if (purifier != null) tag.putUUID("Purifier", purifier);
        net.minecraft.nbt.ListTag propList = new net.minecraft.nbt.ListTag();
        for (UUID id : props) propList.add(net.minecraft.nbt.NbtUtils.createUUID(id));
        tag.put("Props", propList);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setEncounterState(EncounterState.byName(tag.getString("EncounterState")));
        this.entityData.set(DATA_SCENE_TICK, tag.getInt("SceneTick"));
        this.phaseOneTicks = tag.getInt("PhaseOneTicks");
        this.damageTaken = tag.getFloat("DamageTaken");
        this.infection = tag.hasUUID("Infection") ? tag.getUUID("Infection") : null;
        this.purifier = tag.hasUUID("Purifier") ? tag.getUUID("Purifier") : null;
        this.props.clear();
        for (net.minecraft.nbt.Tag id : tag.getList("Props", net.minecraft.nbt.Tag.TAG_INT_ARRAY)) {
            this.props.add(net.minecraft.nbt.NbtUtils.loadUUID(id));
        }
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return getEncounterState() == EncounterState.BREAKING ? null : SoundEvents.SKELETON_AMBIENT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }

    public enum EncounterState {
        COWERING(0),
        PHASE_ONE(1),
        CURED_SUPPORT(2),
        RESOLVED(3),
        BREAKING(4),
        PURGING(5);

        private final int id;

        EncounterState(int id) {
            this.id = id;
        }

        private static EncounterState byId(int id) {
            for (EncounterState state : values()) {
                if (state.id == id) {
                    return state;
                }
            }
            return COWERING;
        }

        private static EncounterState byName(String name) {
            for (EncounterState state : values()) {
                if (state.name().equals(name)) {
                    return state;
                }
            }
            return COWERING;
        }
    }
}
