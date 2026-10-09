package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonCombatRules.Move;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.FiberRepair;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.AxonalTransductionManager;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.AxonalTravelPath;
import com.vincenthuto.hemomancy.common.worldgen.VagrantMindEncounterData;
import com.vincenthuto.hemomancy.common.worldgen.structure.VagrantMindFiberWeb;
import com.vincenthuto.hemomancy.common.worldgen.structure.VagrantMindGeometry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.entity.PartEntity;
import org.joml.Vector3f;

/** Free-swimming resident predator. Attacks and victim motion are server authoritative. */
public final class NaeglerophaeonEntity extends Monster {
    public static final int IDLE=0, CHARGE=1, CAPTURE=2, GRAB=3, DISCHARGE=4, RECOVERY=5,
            LUNGE_WINDUP=6, LUNGE=7, LASH=8, VOLLEY=9, NOVA=10, CONDUCT=11, NERVE_DIVE=12, NERVE_TRANSIT=13,
            NERVE_EMERGE=14, TRANSITION=15, ASCEND=16, OVERLOAD=17, DRAINED=18;
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(NaeglerophaeonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> PHASE_START = SynchedEntityData.defineId(NaeglerophaeonEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> VICTIM = SynchedEntityData.defineId(NaeglerophaeonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HUNTING = SynchedEntityData.defineId(NaeglerophaeonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Vector3f> CAPTURE_OFFSET = SynchedEntityData.defineId(NaeglerophaeonEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Boolean> ENRAGED = SynchedEntityData.defineId(NaeglerophaeonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DRAINED_STATE = SynchedEntityData.defineId(NaeglerophaeonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Vector3f> MOVE_VECTOR = SynchedEntityData.defineId(NaeglerophaeonEntity.class, EntityDataSerializers.VECTOR3);
    private final ServerBossEvent bossEvent = new ServerBossEvent(Component.translatable("entity.hemomancy.naeglerophaeon"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
    private final NaeglerophaeonGripPart[] parts = {new NaeglerophaeonGripPart(this)};
    private BlockPos mindOrigin;
    private long mindSeed;
    private List<VagrantMindGeometry.Lobe> lobes = List.of();
    private List<BlockPos> nodes = List.of();
    private UUID attackTarget;
    private Vec3 grabStart;
    private int zapCooldown, grabCooldown, blockedTicks, routeTicks;
    private int lashCooldown, lungeCooldown=NaeglerophaeonCombatRules.openingCooldown(Move.LUNGE),
            volleyCooldown=NaeglerophaeonCombatRules.openingCooldown(Move.VOLLEY),
            conductCooldown=NaeglerophaeonCombatRules.openingCooldown(Move.CONDUCT),
            novaCooldown=NaeglerophaeonCombatRules.openingCooldown(Move.NOVA), blinkCooldown=200, retaliateCooldown;
    private float grabBreakDamage;
    private Vec3 routeSample;
    private BlockPos pendingGap, repairTarget;
    private int repairTicks, repairTravelTicks, repairCooldown;
    // Stage state
    private boolean enraged, overloading, drained, bypassDamage;
    private int recoveryLength=40;
    // Movement
    private int orbitDirection=1, orbitRepaths, noSightTicks, stalledRepaths;
    private double orbitPhase, orbitRadius=10;
    private float recentDamage;
    private long recentDamageStart;
    // Lunge / dart
    private Vec3 lungeDirection;
    private int lungesLeft;
    private boolean dashDamaging;
    private final Set<UUID> struck=new HashSet<>();
    // Volley
    private int sparksLeft;
    private record PendingSpark(Vec3 from,Vec3 aim,long at) {}
    private final List<PendingSpark> pendingSparks=new ArrayList<>();
    // Conduction
    private BlockPos conductNode;
    private static final class Conduction {
        final List<BlockPos> cells=new ArrayList<>(); final Map<BlockPos,BlockPos> parents=new HashMap<>();
        final Map<BlockPos,Integer> depth=new HashMap<>(); final Set<UUID> hit=new HashSet<>();
        int maxDepth, age;
    }
    private final List<Conduction> conductions=new ArrayList<>();
    // Nerve blink
    private BlockPos diveNode, emergeNode;
    private Vec3 diveFrom, emergeAt;
    private int transitTicks;
    // Fiber retaliation
    private UUID retaliateTarget;
    private long retaliateAt;
    private BlockPos retaliateGap;
    // Overload / drained
    private Vec3 overloadCenter, driftTarget;
    private int overloadClock, twitchTicks;
    private BlockPos blastTarget;
    private long blastAt;
    private static final class Homing { Vec3 pos, velocity; int age; Homing(Vec3 p,Vec3 v) { pos=p; velocity=v; } }
    private final List<Homing> homing=new ArrayList<>();

    public NaeglerophaeonEntity(EntityType<? extends NaeglerophaeonEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        setId(ENTITY_COUNTER.getAndAdd(parts.length + 1) + 1);
        moveControl = new FlyingMoveControl(this, 12, true);
    }
    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,NaeglerophaeonCombatRules.MAX_HEALTH).add(Attributes.ARMOR,4)
                .add(Attributes.MOVEMENT_SPEED,.22).add(Attributes.FLYING_SPEED,.3)
                .add(Attributes.FOLLOW_RANGE,32).add(Attributes.ATTACK_DAMAGE,4);
    }
    @Override protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this,level);
        nav.setCanFloat(true);
        nav.setCanOpenDoors(false);
        return nav;
    }
    @Override protected void registerGoals() {}
    private static boolean swimsDuring(int phase) {
        return phase==IDLE || phase==RECOVERY || phase==VOLLEY || phase==ASCEND || phase==DRAINED;
    }
    @Override public void travel(Vec3 input) {
        if(!swimsDuring(animationPhase())) { setDeltaMovement(Vec3.ZERO); return; }
        super.travel(input);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(PHASE,IDLE); b.define(PHASE_START,0L); b.define(VICTIM,-1); b.define(HUNTING,false);
        b.define(CAPTURE_OFFSET,new Vector3f()); b.define(ENRAGED,false); b.define(DRAINED_STATE,false);
        b.define(MOVE_VECTOR,new Vector3f());
    }
    public void bindMind(BlockPos origin,long seed,List<BlockPos> mindNodes) {
        mindOrigin=origin.immutable(); mindSeed=seed; lobes=VagrantMindGeometry.lobes(seed);
        nodes=mindNodes.stream().map(BlockPos::immutable).toList();
    }
    /** Break events arrive before removal; the next server tick verifies the resulting gap. */
    public void noticeFiberBreak(BlockPos pos,Player cutter) {
        if(!(level() instanceof ServerLevel) || !isAlive() || overloading || drained
                || position().distanceToSqr(Vec3.atCenterOf(pos))>96*96
                || !inMind(Vec3.atCenterOf(pos))) return;
        if(cutter!=null && valid(cutter) && retaliateCooldown==0 && retaliateTarget==null
                && animationPhase()!=NERVE_TRANSIT && distanceToSqr(cutter)<=32*32) {
            retaliateTarget=cutter.getUUID();
            retaliateAt=level().getGameTime()+NaeglerophaeonCombatRules.RETALIATE_WINDUP;
            retaliateGap=pos.immutable();
            retaliateCooldown=NaeglerophaeonCombatRules.RETALIATE_COOLDOWN;
            NaeglerophaeonEffects.telegraph((ServerLevel)level(),core(),cutter.getEyePosition());
            return;
        }
        if(pendingGap==null) pendingGap=pos.immutable();
    }
    public BlockPos mindOrigin() { return mindOrigin; }
    public int animationPhase() { return entityData.get(PHASE); }
    public long animationStart() { return entityData.get(PHASE_START); }
    public int grabbedEntityId() { return entityData.get(VICTIM); }
    public boolean isGrabbing() { return animationPhase()==GRAB; }
    public boolean isHunting() { return entityData.get(HUNTING); }
    public boolean isEnraged() { return entityData.get(ENRAGED); }
    public boolean isDrained() { return entityData.get(DRAINED_STATE); }
    public boolean isOverloading() { return animationPhase()==ASCEND || animationPhase()==OVERLOAD; }
    public Vec3 moveVector() { return new Vec3(entityData.get(MOVE_VECTOR)); }
    public boolean isCharging() { return animationPhase()==CHARGE || animationPhase()==CAPTURE; }
    public Vec3 captureStart() { return position().add(new Vec3(entityData.get(CAPTURE_OFFSET))); }
    public Vec3 gripPosition(float partial) {
        Entity victim=level().getEntity(grabbedEntityId());
        if (victim==null) return getPosition(partial);
        return NaeglerophaeonCombatRules.gripPosition(victim.getPosition(partial),victim.getBbHeight(),victim.getYRot());
    }
    @Override public boolean isMultipartEntity() { return true; }
    @Override public PartEntity<?>[] getParts() { return parts; }
    @Override public void setId(int id) { super.setId(id); if(parts!=null) parts[0].setId(id+1); }
    @Override public AABB getBoundingBoxForCulling() { return getBoundingBox().inflate(34); }
    @Override protected SoundEvent getAmbientSound() { return drained?null:SoundInit.ENTITY_NAEGLEROPHAEON_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundInit.ENTITY_NAEGLEROPHAEON_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return SoundInit.ENTITY_NAEGLEROPHAEON_DEATH.get(); }
    @Override public boolean isPickable() { return super.isPickable() && animationPhase()!=NERVE_TRANSIT; }
    @Override public boolean isInvulnerableTo(DamageSource source) {
        int state=animationPhase();
        return super.isInvulnerableTo(source) || (state==NERVE_TRANSIT || state==TRANSITION)
                && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }
    /** Health never skips the Overload finale and cannot reach zero during it. */
    @Override public void setHealth(float health) {
        if(level()!=null && !level().isClientSide)
            health=NaeglerophaeonCombatRules.allowedHealth(health,getMaxHealth(),overloading,drained,bypassDamage);
        super.setHealth(health);
    }

    private void phase(int phase) {
        entityData.set(PHASE,phase); entityData.set(PHASE_START,level().getGameTime());
    }
    private void recover(int ticks) { recoveryLength=ticks; phase(RECOVERY); }
    private boolean valid(Player p) {
        return p.isAlive() && !p.isCreative() && !p.isSpectator() && p.level()==level()
                && !AxonalTransductionManager.isTraveling(p);
    }
    private Player victim(ServerLevel level) {
        Entity e=attackTarget==null ? null : level.getEntity(attackTarget);
        return e instanceof Player p && valid(p) ? p : null;
    }
    private Player player(ServerLevel level,UUID id) {
        Entity e=id==null ? null : level.getEntity(id);
        return e instanceof Player p && valid(p) ? p : null;
    }
    @Override public void tick() {
        super.tick(); setNoGravity(true);
        parts[0].setOldPosAndRot();
        parts[0].setPos(gripPosition(1).add(0,-.3,0));
        if (!(level() instanceof ServerLevel server) || !isAlive()) return;
        if (server.getDifficulty()==Difficulty.PEACEFUL) { release(); discard(); return; }
        bossEvent.setProgress(getHealth()/getMaxHealth());
        if (mindOrigin!=null && tickCount%40==0) VagrantMindEncounterData.get(server).position(mindOrigin,getUUID(),blockPosition());
        tickCooldowns();
        tickPendingSparks(server);
        tickConductions(server);
        tickHoming(server);
        tickBlast(server);
        tickRetaliation(server);
        if(NaeglerophaeonCombatRules.startsOverload(getHealth(),getMaxHealth(),overloading,drained)) { beginOverload(server); return; }
        if(!enraged && !overloading && !drained && NaeglerophaeonCombatRules.phaseTwo(getHealth(),getMaxHealth())) { beginTransition(server); return; }
        acceptPendingGap(server);
        int state=animationPhase();
        long age=server.getGameTime()-animationStart();
        switch(state) {
            case DRAINED -> { tickDrained(server); return; }
            case ASCEND -> { tickAscend(server,age); return; }
            case OVERLOAD -> { tickOverload(server,age); return; }
            case TRANSITION -> { tickTransition(server,age); return; }
            case NERVE_DIVE, NERVE_TRANSIT, NERVE_EMERGE -> { tickBlink(server,state,age); return; }
            case RECOVERY -> {
                if(age>=recoveryLength) phase(IDLE);
                else swim(nearestTarget(server),.6);
                face(getDeltaMovement());
                return;
            }
            case DISCHARGE -> { if(age>=6) recover(NaeglerophaeonCombatRules.recoveryTicks(enraged)); return; }
            case LUNGE_WINDUP, LUNGE -> { tickLunge(server,state,age); return; }
            case LASH -> { tickLash(server,age); return; }
            case NOVA -> { tickNova(server,age); return; }
            case CONDUCT -> { tickConduct(server,age); return; }
            case VOLLEY -> { tickVolley(server,age); return; }
            case CHARGE, CAPTURE, GRAB -> { tickCaptureOrCharge(server,state,age); return; }
            default -> {}
        }
        Player target=nearestTarget(server);
        if(target==null && repairTarget==null && repairCooldown==0 && tickCount%100==0)
            scanForGap(server);
        if(repairTarget!=null && target==null) {
            setTarget(null);
            entityData.set(HUNTING,false);
            if(tickRepair(server)) return;
        }
        setTarget(target);
        entityData.set(HUNTING,target!=null);
        swim(target,1);
        face(getDeltaMovement());
        if(target==null) { noSightTicks=0; return; }
        boolean sight=hasLineOfSight(target);
        noSightTicks=sight?0:noSightTicks+1;
        if(tryEvade(server,target)) return;
        if(blinkCooldown==0 && (enraged || noSightTicks>60 || stalled()) && beginBlink(server,target)) return;
        if(!sight) return;
        double distance=Math.sqrt(distanceToSqr(target));
        int crowd=0;
        for(ServerPlayer p:server.players()) if(valid(p) && distanceToSqr(p)<=64) crowd++;
        BlockPos node=nodeNear(target);
        Move move=NaeglerophaeonCombatRules.choose(distance,true,enraged,crowd,node!=null,this::ready);
        beginMove(server,move,target,node);
    }
    private void tickCooldowns() {
        if(zapCooldown>0) zapCooldown--;
        if(grabCooldown>0) grabCooldown--;
        if(repairCooldown>0) repairCooldown--;
        if(lashCooldown>0) lashCooldown--;
        if(lungeCooldown>0) lungeCooldown--;
        if(volleyCooldown>0) volleyCooldown--;
        if(conductCooldown>0) conductCooldown--;
        if(novaCooldown>0) novaCooldown--;
        if(blinkCooldown>0) blinkCooldown--;
        if(retaliateCooldown>0) retaliateCooldown--;
    }
    private boolean ready(Move move) {
        return switch(move) {
            case LASH -> lashCooldown==0;
            case CAPTURE -> grabCooldown==0;
            case NOVA -> novaCooldown==0;
            case LUNGE -> lungeCooldown==0;
            case CONDUCT -> conductCooldown==0;
            case VOLLEY -> volleyCooldown==0;
            case CHARGE -> zapCooldown==0;
            case NONE -> false;
        };
    }
    private void beginMove(ServerLevel server,Move move,Player target,BlockPos node) {
        int cooldown=NaeglerophaeonCombatRules.cooldown(move,enraged);
        switch(move) {
            case CAPTURE -> { beginAttack(target,CAPTURE); grabCooldown=cooldown; }
            case CHARGE -> { beginAttack(target,CHARGE); zapCooldown=cooldown; }
            case LASH -> {
                attackTarget=target.getUUID(); stopMoving(); phase(LASH); lashCooldown=cooldown;
                NaeglerophaeonEffects.coil(server,core());
            }
            case NOVA -> {
                attackTarget=target.getUUID(); stopMoving(); struck.clear(); phase(NOVA); novaCooldown=cooldown;
            }
            case LUNGE -> {
                lungesLeft=NaeglerophaeonCombatRules.lungeCount(enraged); lungeCooldown=cooldown;
                attackTarget=target.getUUID(); startLunge(server,target.position().add(0,target.getBbHeight()*.5,0),true);
            }
            case CONDUCT -> {
                attackTarget=target.getUUID(); conductNode=node; stopMoving(); phase(CONDUCT); conductCooldown=cooldown;
            }
            case VOLLEY -> {
                attackTarget=target.getUUID(); sparksLeft=NaeglerophaeonCombatRules.volleyCount(enraged);
                phase(VOLLEY); volleyCooldown=cooldown;
            }
            case NONE -> {}
        }
    }
    private void stopMoving() { navigation.stop(); setDeltaMovement(Vec3.ZERO); }
    private void face(Vec3 motion) {
        if(motion.lengthSqr()<=.0004) return;
        setYRot(Mth.rotLerp(.15F,getYRot(),(float)Math.toDegrees(Math.atan2(-motion.x,motion.z))));
        setXRot(Mth.rotLerp(.12F,getXRot(),(float)-Math.toDegrees(Math.atan2(motion.y,motion.horizontalDistance()))));
        setYHeadRot(getYRot());
    }
    private void faceToward(Vec3 point) {
        Vec3 look=point.subtract(core());
        setYRot((float)Math.toDegrees(Math.atan2(-look.x,look.z)));
        setXRot((float)-Math.toDegrees(Math.atan2(look.y,look.horizontalDistance())));
        setYHeadRot(getYRot());
    }
    private Player nearestTarget(ServerLevel server) {
        Player target=null;
        double best=32*32;
        for(ServerPlayer p:server.players()) if(valid(p) && inMind(p.position()) && distanceToSqr(p)<best) { best=distanceToSqr(p); target=p; }
        return target;
    }
    private boolean stalled() { return stalledRepaths>=3; }

    // ---- Charge and capture (original attacks) ----
    private void tickCaptureOrCharge(ServerLevel server,int state,long age) {
        stopMoving();
        Player player=victim(server);
        if(player==null || distanceToSqr(player)>32*32) { release(); return; }
        if(state==CHARGE && age>=20) {
            if(distanceToSqr(player)<=256 && hasLineOfSight(player)) {
                NaeglerophaeonEffects.zap(server,core(),player.getEyePosition());
                player.hurt(damageSources().mobAttack(this),NaeglerophaeonCombatRules.ZAP_DAMAGE);
            }
            clearVictim(); phase(IDLE);
        } else if(state==CAPTURE && age>=20) {
            if(distanceToSqr(player)>144 || !hasLineOfSight(player)) { release(); return; }
            grabStart=player.position(); blockedTicks=0; grabBreakDamage=0;
            entityData.set(CAPTURE_OFFSET,grabStart.subtract(position()).toVector3f());
            phase(GRAB);
        } else if(state==GRAB) tickReel(server,player,age);
    }
    private Vec3 core() { return position().add(0,.8,0); }
    private void beginAttack(Player target,int state) {
        attackTarget=target.getUUID(); entityData.set(VICTIM,target.getId());
        entityData.set(CAPTURE_OFFSET,target.position().subtract(position()).toVector3f());
        stopMoving(); phase(state);
        NaeglerophaeonEffects.telegraph((ServerLevel)level(),core(),target.getEyePosition());
    }
    private void tickReel(ServerLevel server,Player player,long age) {
        if(grabStart==null || player.position().distanceToSqr(grabStart)>32*32) { release(); return; }
        Vec3 direction=grabStart.subtract(position()).multiply(1,0,1).normalize();
        if(direction.lengthSqr()<.01) direction=Vec3.directionFromRotation(0,getYRot());
        Vec3 destination=position().add(direction.scale(1.8));
        Vec3 desired=grabStart.lerp(destination,Math.min(1,age/100.0));
        Vec3 delta=desired.subtract(player.position());
        if(delta.length()>.35) delta=delta.normalize().scale(.35);
        Vec3 before=player.position();
        player.move(MoverType.SELF,delta);
        player.setDeltaMovement(Vec3.ZERO); player.fallDistance=0; player.hurtMarked=true;
        if(player instanceof ServerPlayer sp) sp.connection.teleport(player.getX(),player.getY(),player.getZ(),player.getYRot(),player.getXRot(),
                java.util.Set.of(RelativeMovement.X_ROT,RelativeMovement.Y_ROT));
        if(delta.lengthSqr()>.0004 && player.position().distanceToSqr(before)<delta.lengthSqr()*.1) blockedTicks++; else blockedTicks=0;
        if(blockedTicks>=10) { release(); return; }
        if(age>0 && age<100 && age%20==0) {
            Vec3 chest=player.position().add(0,player.getBbHeight()*.55,0);
            NaeglerophaeonEffects.drain(server,chest,core());
            player.hurt(damageSources().indirectMagic(this,this),NaeglerophaeonCombatRules.DRAIN_DAMAGE);
            heal(NaeglerophaeonCombatRules.drainHeal(enraged));
        }
        if(age>=100) {
            if(player.position().distanceToSqr(destination)<1) {
                NaeglerophaeonEffects.zap(server,core(),player.getEyePosition());
                NaeglerophaeonEffects.releaseBurst(player);
                player.hurt(damageSources().mobAttack(this),NaeglerophaeonCombatRules.DISCHARGE_DAMAGE);
                player.push(direction.x*.65,.25,direction.z*.65); player.hurtMarked=true;
                clearVictim(); phase(DISCHARGE);
            } else release();
        }
    }

    // ---- Lunge, evade dart and lash ----
    private void startLunge(ServerLevel server,Vec3 aim,boolean damaging) {
        Vec3 direction=aim.subtract(core());
        if(direction.lengthSqr()<1.0E-4) direction=Vec3.directionFromRotation(0,getYRot());
        lungeDirection=direction.normalize();
        dashDamaging=damaging;
        struck.clear();
        entityData.set(MOVE_VECTOR,lungeDirection.toVector3f());
        stopMoving();
        faceToward(core().add(lungeDirection));
        if(damaging) {
            phase(LUNGE_WINDUP);
            double reach=NaeglerophaeonCombatRules.LUNGE_SPEED*NaeglerophaeonCombatRules.LUNGE_TICKS;
            NaeglerophaeonEffects.lungeFlare(server,core(),core().add(lungeDirection.scale(reach)));
        } else phase(LUNGE);
    }
    private void tickLunge(ServerLevel server,int state,long age) {
        if(state==LUNGE_WINDUP) {
            stopMoving();
            if(age>=NaeglerophaeonCombatRules.LUNGE_WINDUP) phase(LUNGE);
            return;
        }
        int duration=dashDamaging?NaeglerophaeonCombatRules.LUNGE_TICKS:5;
        double speed=dashDamaging?NaeglerophaeonCombatRules.LUNGE_SPEED:.9;
        Vec3 before=position(), coreBefore=core();
        Vec3 step=lungeDirection.scale(speed);
        boolean blocked=!inMind(before.add(step));
        if(!blocked) {
            move(MoverType.SELF,step);
            blocked=position().distanceToSqr(before)<step.lengthSqr()*.25;
        }
        setDeltaMovement(Vec3.ZERO);
        if(dashDamaging) {
            NaeglerophaeonEffects.lungeTrail(server,coreBefore,core());
            AABB swept=getBoundingBox().minmax(getBoundingBox().move(before.subtract(position()))).inflate(.6);
            for(ServerPlayer p:server.players()) if(valid(p) && swept.intersects(p.getBoundingBox()) && struck.add(p.getUUID())) {
                p.hurt(damageSources().mobAttack(this),NaeglerophaeonCombatRules.LUNGE_DAMAGE);
                p.push(lungeDirection.x*.9,.35,lungeDirection.z*.9); p.hurtMarked=true;
            }
        }
        if(blocked) {
            lungesLeft=0;
            NaeglerophaeonEffects.coil(server,core());
            recover(NaeglerophaeonCombatRules.recoveryTicks(enraged)+NaeglerophaeonCombatRules.LUNGE_STUN);
            return;
        }
        if(age+1<duration) return;
        if(dashDamaging && --lungesLeft>0) {
            Player target=victim(server);
            if(target!=null && hasLineOfSight(target)) { startLunge(server,target.position().add(0,target.getBbHeight()*.5,0),true); return; }
        }
        attackTarget=null;
        if(dashDamaging) recover(NaeglerophaeonCombatRules.recoveryTicks(enraged));
        else phase(IDLE);
    }
    private boolean tryEvade(ServerLevel server,Player target) {
        if(recentDamage<10 || level().getGameTime()-recentDamageStart>40) return false;
        recentDamage=0;
        Vec3 away=position().subtract(target.position()).multiply(1,0,1);
        Vec3 side=new Vec3(-away.z,0,away.x);
        if(side.lengthSqr()<1.0E-4) side=new Vec3(1,0,0);
        side=side.normalize().scale(random.nextBoolean()?1:-1).add(0,.25,0);
        if(!open(position().add(side.scale(3)))) side=side.scale(-1);
        if(!open(position().add(side.scale(3)))) return false;
        startLunge(server,core().add(side),false);
        return true;
    }
    private void tickLash(ServerLevel server,long age) {
        stopMoving();
        if(age%4==0 && age<NaeglerophaeonCombatRules.LASH_WINDUP) NaeglerophaeonEffects.coil(server,core());
        if(age<NaeglerophaeonCombatRules.LASH_WINDUP) return;
        // The tendrils stay flung open for a few ticks after the strike so the starburst reads.
        if(age>NaeglerophaeonCombatRules.LASH_WINDUP) {
            if(age>=NaeglerophaeonCombatRules.LASH_WINDUP+6) { attackTarget=null; recover(14); }
            return;
        }
        Vec3 core=core();
        NaeglerophaeonEffects.lashSweep(server,core);
        double radius=NaeglerophaeonCombatRules.LASH_RADIUS;
        for(ServerPlayer p:server.players()) {
            Vec3 center=p.position().add(0,p.getBbHeight()*.5,0);
            if(!valid(p) || center.distanceToSqr(core)>radius*radius || !clear(server,core,center)) continue;
            p.hurt(damageSources().mobAttack(this),NaeglerophaeonCombatRules.LASH_DAMAGE);
            Vec3 push=center.subtract(core).multiply(1,0,1).normalize();
            p.push(push.x*1.1,.4,push.z*1.1); p.hurtMarked=true;
            p.addEffect(new MobEffectInstance(EffectInit.neural_overload,60,0),this);
        }
    }

    // ---- Tip-spark volley ----
    private Vec3 tipAnchor(int index) {
        double angle=index*2.39996+level().getGameTime()*.05;
        Vec3 local=new Vec3(Math.cos(angle)*2.2,Math.sin(angle)*2.2,-1.5)
                .xRot((float)Math.toRadians(getXRot())).yRot((float)-Math.toRadians(getYRot()));
        return core().add(local);
    }
    private void tickVolley(ServerLevel server,long age) {
        Player target=victim(server);
        if(target==null || sparksLeft<=0) { attackTarget=null; sparksLeft=0; phase(IDLE); return; }
        swim(target,1);
        face(target.position().subtract(position()));
        if(age%NaeglerophaeonCombatRules.VOLLEY_INTERVAL!=0) return;
        Vec3 tip=openPoint(server,core(),tipAnchor(sparksLeft));
        Vec3 aim=target.position().add(0,target.getBbHeight()*.5,0);
        NaeglerophaeonEffects.sparkLaunch(server,tip,aim);
        pendingSparks.add(new PendingSpark(tip,aim,server.getGameTime()+NaeglerophaeonCombatRules.SPARK_TRAVEL));
        sparksLeft--;
    }
    private void tickPendingSparks(ServerLevel server) {
        var iterator=pendingSparks.iterator();
        while(iterator.hasNext()) {
            PendingSpark spark=iterator.next();
            if(server.getGameTime()<spark.at) continue;
            iterator.remove();
            Vec3 end=clip(server,spark.from,spark.aim);
            NaeglerophaeonEffects.sparkStrike(server,spark.from,end);
            if(end.distanceToSqr(spark.aim)>.01) continue;
            for(ServerPlayer p:server.players())
                if(valid(p) && p.getBoundingBox().inflate(.3).contains(spark.aim))
                    p.hurt(damageSources().mobAttack(this),NaeglerophaeonCombatRules.SPARK_DAMAGE);
        }
    }

    // ---- Conduction along the nerve web ----
    private boolean usable(ServerLevel server,BlockPos node) {
        return server.hasChunkAt(node) && server.getBlockState(node).is(BlockInit.synaptic_node.get());
    }
    private List<BlockPos> usableNodes(ServerLevel server) {
        List<BlockPos> result=new ArrayList<>();
        for(BlockPos node:nodes) if(usable(server,node)) result.add(node);
        return result;
    }
    private BlockPos nodeNear(Player target) {
        if(!(level() instanceof ServerLevel server)) return null;
        BlockPos best=null;
        double bestDistance=100;
        for(BlockPos node:nodes) {
            double toTarget=target.position().distanceToSqr(Vec3.atCenterOf(node));
            if(toTarget<bestDistance && core().distanceToSqr(Vec3.atCenterOf(node))<=400 && usable(server,node)) {
                bestDistance=toTarget; best=node;
            }
        }
        return best;
    }
    private void tickConduct(ServerLevel server,long age) {
        stopMoving();
        if(conductNode==null || !usable(server,conductNode)) { conductNode=null; attackTarget=null; phase(IDLE); return; }
        faceToward(Vec3.atCenterOf(conductNode));
        if(age%5==0) NaeglerophaeonEffects.conductCharge(server,core(),Vec3.atCenterOf(conductNode));
        if(age<NaeglerophaeonCombatRules.CONDUCT_WINDUP) return;
        startConduction(server,conductNode);
        conductNode=null; attackTarget=null;
        phase(IDLE);
    }
    private void startConduction(ServerLevel server,BlockPos origin) {
        if(conductions.size()>=6) return;
        AxonalTravelPath.Network network=AxonalTransductionManager.network(server);
        Conduction conduction=new Conduction();
        ArrayDeque<BlockPos> queue=new ArrayDeque<>();
        queue.add(origin); conduction.depth.put(origin,0); conduction.cells.add(origin);
        while(!queue.isEmpty() && conduction.cells.size()<NaeglerophaeonCombatRules.CONDUCT_CELLS) {
            BlockPos cell=queue.poll();
            for(BlockPos next:AxonalTravelPath.neighbors(network,cell)) {
                if(conduction.depth.containsKey(next) || conduction.cells.size()>=NaeglerophaeonCombatRules.CONDUCT_CELLS) continue;
                int depth=conduction.depth.get(cell)+1;
                conduction.depth.put(next,depth); conduction.parents.put(next,cell); conduction.cells.add(next);
                conduction.maxDepth=Math.max(conduction.maxDepth,depth);
                queue.add(next);
            }
        }
        conductions.add(conduction);
    }
    private void tickConductions(ServerLevel server) {
        var iterator=conductions.iterator();
        while(iterator.hasNext()) {
            Conduction conduction=iterator.next();
            int front=conduction.age*2;
            for(BlockPos cell:conduction.cells) {
                int depth=conduction.depth.get(cell);
                if(depth<front || depth>=front+2) continue;
                Vec3 center=Vec3.atCenterOf(cell);
                BlockPos parent=conduction.parents.get(cell);
                NaeglerophaeonEffects.conductCell(server,parent==null?center.add(0,.6,0):Vec3.atCenterOf(parent),center);
                AABB charged=new AABB(cell).inflate(NaeglerophaeonCombatRules.CONDUCT_REACH-.5);
                for(ServerPlayer p:server.players())
                    if(valid(p) && charged.intersects(p.getBoundingBox()) && conduction.hit.add(p.getUUID()))
                        p.hurt(damageSources().indirectMagic(this,this),NaeglerophaeonCombatRules.CONDUCT_DAMAGE);
            }
            if(++conduction.age*2>conduction.maxDepth+1) iterator.remove();
        }
    }

    // ---- Core nova ----
    private void tickNova(ServerLevel server,long age) {
        stopMoving();
        Vec3 core=core();
        int windup=NaeglerophaeonCombatRules.NOVA_WINDUP;
        if(age<windup) {
            if(age%5==0) NaeglerophaeonEffects.novaCharge(server,core,age/(double)windup);
            return;
        }
        if(age==windup) NaeglerophaeonEffects.novaBurst(server,core);
        double radius=NaeglerophaeonCombatRules.NOVA_RADIUS*Math.min(1,(age-windup+1)/(double)NaeglerophaeonCombatRules.NOVA_SPREAD);
        NaeglerophaeonEffects.novaRing(server,core,radius);
        for(ServerPlayer p:server.players()) {
            Vec3 center=p.position().add(0,p.getBbHeight()*.5,0);
            if(!valid(p) || center.distanceTo(core)>radius || struck.contains(p.getUUID())) continue;
            struck.add(p.getUUID());
            if(clear(server,core,center)) p.hurt(damageSources().indirectMagic(this,this),NaeglerophaeonCombatRules.NOVA_DAMAGE);
        }
        if(age>=windup+NaeglerophaeonCombatRules.NOVA_SPREAD) { attackTarget=null; recover(NaeglerophaeonCombatRules.recoveryTicks(enraged)); }
    }

    // ---- Nerve blink ----
    private Vec3 emergence(ServerLevel server,BlockPos node) {
        for(Vec3 offset:new Vec3[]{new Vec3(0,1.5,0),new Vec3(0,2.5,0),new Vec3(1.5,.5,0),new Vec3(-1.5,.5,0),
                new Vec3(0,.5,1.5),new Vec3(0,.5,-1.5),new Vec3(0,-2.5,0)}) {
            Vec3 feet=Vec3.atBottomCenterOf(node).add(offset);
            if(server.hasChunkAt(BlockPos.containing(feet)) && inMind(feet)
                    && server.noCollision(this,getBoundingBox().move(feet.subtract(position())))) return feet;
        }
        return null;
    }
    private boolean beginBlink(ServerLevel server,Player target) {
        BlockPos dive=null;
        double nearest=144;
        for(BlockPos node:usableNodes(server)) {
            double d=core().distanceToSqr(Vec3.atCenterOf(node));
            if(d<nearest && sees(server,core(),node)) { nearest=d; dive=node; }
        }
        if(dive==null) { blinkCooldown=60; return false; }
        List<BlockPos> options=new ArrayList<>();
        for(BlockPos node:usableNodes(server)) {
            if(node.equals(dive)) continue;
            double d=target.position().distanceTo(Vec3.atCenterOf(node));
            if(d>=12 && d<=28) options.add(node);
        }
        java.util.Collections.shuffle(options,new java.util.Random(random.nextLong()));
        for(BlockPos node:options) {
            Vec3 feet=emergence(server,node);
            if(feet==null || !clear(server,feet.add(0,.8,0),target.getEyePosition())) continue;
            diveNode=dive; emergeNode=node; emergeAt=feet; diveFrom=position();
            attackTarget=target.getUUID();
            stopMoving(); noPhysics=true;
            entityData.set(MOVE_VECTOR,Vec3.atCenterOf(dive).subtract(core()).toVector3f());
            phase(NERVE_DIVE);
            NaeglerophaeonEffects.dive(server,core(),Vec3.atCenterOf(dive));
            blinkCooldown=enraged?160:300;
            return true;
        }
        blinkCooldown=60;
        return false;
    }
    private void tickBlink(ServerLevel server,int state,long age) {
        stopMoving();
        if(diveNode==null || emergeAt==null) { endBlink(); phase(IDLE); return; }
        if(state==NERVE_DIVE) {
            Vec3 node=Vec3.atCenterOf(diveNode).subtract(0,.8,0);
            setPos(diveFrom.lerp(node,Math.min(1,(age+1)/(double)NaeglerophaeonCombatRules.DIVE_TICKS)));
            if(age+1>=NaeglerophaeonCombatRules.DIVE_TICKS) {
                transitTicks=Math.max(6,Mth.ceil(Vec3.atCenterOf(diveNode).distanceTo(Vec3.atCenterOf(emergeNode))
                        /NaeglerophaeonCombatRules.BLINK_SPEED));
                noPhysics=true; setInvisible(true);
                phase(NERVE_TRANSIT);
            }
            return;
        }
        if(state==NERVE_TRANSIT) {
            Vec3 from=Vec3.atCenterOf(diveNode), to=Vec3.atCenterOf(emergeNode);
            Vec3 at=from.lerp(to,Math.min(1,(age+1)/(double)transitTicks));
            setPos(at.subtract(0,.8,0));
            NaeglerophaeonEffects.transit(server,at);
            if(transitTicks-age==NaeglerophaeonCombatRules.EMERGE_WARNING) NaeglerophaeonEffects.coil(server,to);
            if(age+1<transitTicks) return;
            if(!server.noCollision(this,getBoundingBox().move(emergeAt.subtract(position())))) {
                Vec3 feet=emergence(server,emergeNode);
                emergeAt=feet!=null?feet:diveFrom;
            }
            noPhysics=false; setInvisible(false);
            moveTo(emergeAt.x,emergeAt.y,emergeAt.z,getYRot(),0);
            NaeglerophaeonEffects.blink(server,to,core());
            phase(NERVE_EMERGE);
            return;
        }
        if(age>=6) {
            endBlink();
            Player target=victim(server);
            attackTarget=null;
            if(target!=null && hasLineOfSight(target)) {
                if(distanceToSqr(target)>=36 && distanceToSqr(target)<=400) lungeCooldown=0; else volleyCooldown=0;
            }
            phase(IDLE);
        }
    }
    private void endBlink() {
        noPhysics=false; setInvisible(false);
        diveNode=null; emergeNode=null; emergeAt=null; diveFrom=null;
    }

    // ---- Fiber-cut retaliation ----
    private void tickRetaliation(ServerLevel server) {
        if(retaliateTarget==null || server.getGameTime()<retaliateAt) return;
        Player cutter=player(server,retaliateTarget);
        BlockPos gap=retaliateGap;
        retaliateTarget=null; retaliateGap=null;
        if(cutter!=null && distanceToSqr(cutter)<=32*32 && hasLineOfSight(cutter) && animationPhase()!=NERVE_TRANSIT) {
            NaeglerophaeonEffects.zap(server,core(),cutter.getEyePosition());
            cutter.hurt(damageSources().mobAttack(this),NaeglerophaeonCombatRules.ZAP_DAMAGE);
            cutter.addEffect(new MobEffectInstance(EffectInit.neural_overload,100,0),this);
        }
        if(gap==null) return;
        Vec3 center=Vec3.atCenterOf(gap);
        if(core().distanceToSqr(center)<=24*24 && clearRepairLine(server,core(),center) && repairable(server,gap)) {
            NaeglerophaeonEffects.repairing(server,core(),center);
            if(FiberRepair.repair(server,gap)) NaeglerophaeonEffects.repaired(server,center);
        } else if(pendingGap==null) pendingGap=gap;
    }

    // ---- Stage transitions ----
    private void beginTransition(ServerLevel server) {
        release();
        enraged=true; entityData.set(ENRAGED,true);
        bossEvent.setColor(BossEvent.BossBarColor.PURPLE);
        lungesLeft=0; sparksLeft=0; conductNode=null; endBlink();
        stopMoving();
        phase(TRANSITION);
        NaeglerophaeonEffects.transition(server,core());
    }
    private void tickTransition(ServerLevel server,long age) {
        stopMoving();
        if(age%8==0) NaeglerophaeonEffects.coil(server,core());
        if(age==10) for(BlockPos node:usableNodes(server))
            if(core().distanceToSqr(Vec3.atCenterOf(node))<=16*16) startConduction(server,node);
        if(age>=NaeglerophaeonCombatRules.TRANSITION_TICKS) phase(IDLE);
    }
    private void beginOverload(ServerLevel server) {
        release();
        if(!enraged) { enraged=true; entityData.set(ENRAGED,true); }
        overloading=true;
        bossEvent.setColor(BossEvent.BossBarColor.YELLOW);
        lungesLeft=0; sparksLeft=0; conductNode=null; endBlink();
        overloadCenter=findCenter();
        overloadClock=0; routeTicks=0;
        stopMoving();
        phase(ASCEND);
        NaeglerophaeonEffects.transition(server,core());
    }
    private Vec3 findCenter() {
        if(nodes.isEmpty()) return position();
        Vec3 sum=Vec3.ZERO;
        for(BlockPos node:nodes) sum=sum.add(Vec3.atCenterOf(node));
        Vec3 centroid=sum.scale(1.0/nodes.size());
        for(int radius=0;radius<=6;radius++)
            for(int i=0;i<(radius==0?1:16);i++) {
                double angle=i*Math.PI/8;
                Vec3 point=centroid.add(Math.cos(angle)*radius,(i%3-1)*radius*.5,Math.sin(angle)*radius);
                if(open(point)) return point;
            }
        return position();
    }
    private void tickAscend(ServerLevel server,long age) {
        entityData.set(HUNTING,false);
        setTarget(null);
        boolean arrived=position().distanceToSqr(overloadCenter)<2.25;
        if(!arrived && age<NaeglerophaeonCombatRules.ASCEND_LIMIT) {
            if(--routeTicks<=0) {
                routeTicks=20;
                var path=navigation.createPath(BlockPos.containing(overloadCenter),0);
                if(path!=null) navigation.moveTo(path,1.2);
                else moveControl.setWantedPosition(overloadCenter.x,overloadCenter.y,overloadCenter.z,1.2);
            }
            face(getDeltaMovement());
            return;
        }
        stopMoving();
        if(!arrived) {
            BlockPos closest=null;
            double best=Double.MAX_VALUE;
            for(BlockPos node:usableNodes(server)) {
                double d=Vec3.atCenterOf(node).distanceToSqr(overloadCenter);
                if(d<best) { best=d; closest=node; }
            }
            Vec3 feet=closest==null?null:emergence(server,closest);
            if(feet!=null) {
                Vec3 from=core();
                moveTo(feet.x,feet.y,feet.z,getYRot(),getXRot());
                NaeglerophaeonEffects.blink(server,from,core());
            }
        }
        overloadCenter=position();
        phase(OVERLOAD);
        if(usableNodes(server).isEmpty()) becomeDrained(server);
    }
    private void tickOverload(ServerLevel server,long age) {
        stopMoving();
        if(position().distanceToSqr(overloadCenter)>.01) setPos(overloadCenter);
        setXRot(-90); xRotO=-90;
        setYRot(getYRot()+1.5F); setYHeadRot(getYRot());
        entityData.set(HUNTING,false);
        boolean audience=false;
        for(ServerPlayer p:server.players()) if(valid(p) && distanceToSqr(p)<=64*64) { audience=true; break; }
        if(!audience) return;
        overloadClock++;
        if(overloadClock%NaeglerophaeonCombatRules.OVERLOAD_PULSE!=0 || blastTarget!=null) return;
        List<BlockPos> remaining=usableNodes(server);
        if(remaining.isEmpty()) { becomeDrained(server); return; }
        blastTarget=remaining.get(random.nextInt(remaining.size()));
        blastAt=server.getGameTime()+NaeglerophaeonCombatRules.BLAST_TRAVEL;
        NaeglerophaeonEffects.blast(server,core().add(0,2,0),Vec3.atCenterOf(blastTarget));
    }
    private void tickBlast(ServerLevel server) {
        if(blastTarget==null || server.getGameTime()<blastAt) return;
        BlockPos node=blastTarget;
        blastTarget=null;
        if(!usable(server,node)) return;
        server.destroyBlock(node,false,this);
        Vec3 center=Vec3.atCenterOf(node);
        NaeglerophaeonEffects.nodeBurst(server,center);
        int count=NaeglerophaeonCombatRules.homingSparks(random.nextInt(),homing.size());
        for(int i=0;i<count;i++) {
            Vec3 out=new Vec3(random.nextDouble()-.5,random.nextDouble()*.6,random.nextDouble()-.5);
            if(out.lengthSqr()<1.0E-4) out=new Vec3(0,1,0);
            homing.add(new Homing(center,out.normalize().scale(NaeglerophaeonCombatRules.HOMING_SPEED)));
        }
    }
    private void tickHoming(ServerLevel server) {
        var iterator=homing.iterator();
        while(iterator.hasNext()) {
            Homing spark=iterator.next();
            Player target=null;
            double best=48*48;
            for(ServerPlayer p:server.players()) {
                double d=p.position().add(0,p.getBbHeight()*.5,0).distanceToSqr(spark.pos);
                if(valid(p) && d<best) { best=d; target=p; }
            }
            if(target!=null) spark.velocity=NaeglerophaeonCombatRules.steer(spark.velocity,
                    target.position().add(0,target.getBbHeight()*.5,0).subtract(spark.pos));
            Vec3 next=spark.pos.add(spark.velocity);
            boolean expired=++spark.age>NaeglerophaeonCombatRules.HOMING_LIFETIME;
            if(spark.age>3 && server.clip(new ClipContext(spark.pos,next,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this))
                    .getType()!=HitResult.Type.MISS) expired=true;
            if(!expired) for(ServerPlayer p:server.players()) {
                AABB box=p.getBoundingBox().inflate(.3);
                if(valid(p) && (box.contains(next) || box.clip(spark.pos,next).isPresent())) {
                    p.hurt(damageSources().indirectMagic(this,this),NaeglerophaeonCombatRules.HOMING_DAMAGE);
                    NaeglerophaeonEffects.homingHit(server,next);
                    expired=true;
                    break;
                }
            }
            if(spark.age%2==0 || expired) NaeglerophaeonEffects.homing(server,spark.pos,next);
            spark.pos=next;
            if(expired) iterator.remove();
        }
    }
    private void becomeDrained(ServerLevel server) {
        overloading=false; drained=true; entityData.set(DRAINED_STATE,true);
        blastTarget=null; driftTarget=null; twitchTicks=20;
        bossEvent.setColor(BossEvent.BossBarColor.WHITE);
        setXRot(0);
        phase(DRAINED);
        NaeglerophaeonEffects.twitch(server,core());
    }
    private void tickDrained(ServerLevel server) {
        entityData.set(HUNTING,false);
        setTarget(null);
        if(twitchTicks>0) {
            twitchTicks--;
            stopMoving();
            if(twitchTicks%7==0 || random.nextInt(12)==0) {
                setYRot(getYRot()+(random.nextFloat()-.5F)*40); setYHeadRot(getYRot());
                setXRot(Mth.clamp(getXRot()+(random.nextFloat()-.5F)*30,-45,45));
                Vec3 jitter=new Vec3(random.nextDouble()-.5,random.nextDouble()-.5,random.nextDouble()-.5).scale(.12);
                if(open(position().add(jitter))) move(MoverType.SELF,jitter);
                if(random.nextInt(3)==0) NaeglerophaeonEffects.twitch(server,core());
            }
            if(twitchTicks==0) driftTarget=nextDriftSite(server);
            return;
        }
        if(driftTarget==null || position().distanceToSqr(driftTarget)<2.25) {
            twitchTicks=40+random.nextInt(41);
            driftTarget=null;
            return;
        }
        if(--routeTicks<=0) {
            routeTicks=30;
            var path=navigation.createPath(BlockPos.containing(driftTarget),0);
            if(path!=null && path.canReach()) navigation.moveTo(path,.4);
            else driftTarget=null;
        }
        face(getDeltaMovement());
    }
    private Vec3 nextDriftSite(ServerLevel server) {
        List<Vec3> sites=new ArrayList<>();
        for(BlockPos node:nodes) {
            if(!server.hasChunkAt(node) || usable(server,node)) continue;
            Vec3 site=Vec3.atCenterOf(node).add(0,.7,0);
            if(site.distanceToSqr(position())>9 && open(site)) sites.add(site);
        }
        if(!sites.isEmpty()) return sites.get(random.nextInt(sites.size()));
        for(int i=0;i<8;i++) {
            Vec3 point=position().add((random.nextDouble()-.5)*12,(random.nextDouble()-.5)*6,(random.nextDouble()-.5)*12);
            if(open(point)) return point;
        }
        return null;
    }

    // ---- Swimming and nerve repair ----
    private boolean inMind(Vec3 point) {
        return mindOrigin==null || VagrantMindGeometry.field(lobes,point.x-mindOrigin.getX(),point.y-mindOrigin.getY(),point.z-mindOrigin.getZ())>=1.1;
    }
    private boolean open(Vec3 point) {
        return level().hasChunkAt(BlockPos.containing(point)) && inMind(point)
                && level().noCollision(this,getBoundingBox().move(point.subtract(position())));
    }
    private boolean clear(ServerLevel server,Vec3 from,Vec3 to) {
        return server.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS;
    }
    /** The furthest open point on the way from {@code from} to {@code to}, kept just short of any terrain. */
    private Vec3 openPoint(ServerLevel server,Vec3 from,Vec3 to) {
        Vec3 end=clip(server,from,to);
        return end==to?to:end.add(from.subtract(end).normalize().scale(.15));
    }
    /** A clear line to a block, where striking that block itself still counts as reaching it. */
    private boolean sees(ServerLevel server,Vec3 from,BlockPos block) {
        BlockHitResult hit=server.clip(new ClipContext(from,Vec3.atCenterOf(block),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
        return hit.getType()==HitResult.Type.MISS || hit.getBlockPos().equals(block);
    }
    private Vec3 clip(ServerLevel server,Vec3 from,Vec3 to) {
        var hit=server.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
        return hit.getType()==HitResult.Type.MISS?to:hit.getLocation();
    }
    private boolean loadedBreak(ServerLevel server,BlockPos gap) {
        if(!inMind(Vec3.atCenterOf(gap)) || !server.hasChunkAt(gap)) return false;
        for(Direction direction:Direction.values())
            if(!server.hasChunkAt(gap.relative(direction))) return false;
        return server.getBlockState(gap).isAir();
    }
    private boolean repairable(ServerLevel server,BlockPos gap) {
        return loadedBreak(server,gap) && FiberRepair.gapAxis(server,gap)>=0;
    }
    private void acceptPendingGap(ServerLevel server) {
        BlockPos gap=pendingGap;
        if(gap==null) return;
        pendingGap=null;
        if(!repairable(server,gap) || repairTarget!=null || repairCooldown>0) return;
        repairTarget=gap;
        repairTicks=0;
        repairTravelTicks=0;
        routeTicks=0;
    }
    private void scanForGap(ServerLevel server) {
        BlockPos center=blockPosition(), nearest=null;
        double best=Double.POSITIVE_INFINITY;
        for(BlockPos pos:BlockPos.betweenClosed(center.offset(-10,-6,-10),center.offset(10,6,10))) {
            if(!server.hasChunkAt(pos) || !server.getBlockState(pos).isAir() || !repairable(server,pos)) continue;
            double distance=pos.distSqr(center);
            if(distance<best) { best=distance; nearest=pos.immutable(); }
        }
        if(nearest!=null) {
            repairTarget=nearest;
            repairTicks=0;
            repairTravelTicks=0;
            routeTicks=0;
        }
    }
    private boolean clearRepairLine(ServerLevel server,Vec3 from,Vec3 gap) {
        return server.clip(new ClipContext(from,gap,ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS;
    }
    private void clearRepairTarget(int cooldown) {
        repairTarget=null;
        repairTicks=0;
        repairTravelTicks=0;
        repairCooldown=cooldown;
        routeTicks=0;
        navigation.stop();
    }
    private boolean tickRepair(ServerLevel server) {
        BlockPos gap=repairTarget;
        if(!repairable(server,gap)) { clearRepairTarget(40); return false; }
        Vec3 center=Vec3.atCenterOf(gap);
        if(++repairTravelTicks>240) { clearRepairTarget(100); return false; }
        if(core().distanceToSqr(center)<=25 && clearRepairLine(server,core(),center)) {
            stopMoving();
            faceToward(center);
            repairTicks++;
            if(repairTicks%8==0) NaeglerophaeonEffects.repairing(server,core(),center);
            if(repairTicks>=40) {
                if(FiberRepair.repair(server,gap)) NaeglerophaeonEffects.repaired(server,center);
                clearRepairTarget(100);
            }
            return true;
        }
        repairTicks=0;
        if(repairTravelTicks%20==1) navigateToRepair(server,center);
        return true;
    }
    private void navigateToRepair(ServerLevel server,Vec3 center) {
        for(int i=0;i<16;i++) {
            double angle=(i%8)*Math.PI/4+tickCount*.07;
            Vec3 point=center.add(Math.cos(angle)*3.5,i<8?1.5:-1.5,Math.sin(angle)*3.5);
            if(!open(point) || !clearRepairLine(server,point.add(0,.8,0),center)) continue;
            var path=navigation.createPath(BlockPos.containing(point),0);
            if(path!=null && path.canReach()) { navigation.moveTo(path,1); return; }
        }
    }
    private void swim(Player target,double speed) {
        if(--routeTicks>0) return;
        boolean stalled=routeSample!=null && position().distanceToSqr(routeSample)<.25;
        routeSample=position(); routeTicks=20;
        stalledRepaths=stalled?stalledRepaths+1:0;
        if(++orbitRepaths%4==0) {
            if(random.nextFloat()<.3F) orbitDirection=-orbitDirection;
            orbitRadius=enraged?6+random.nextDouble()*4:8+random.nextDouble()*4;
        }
        orbitPhase+=orbitDirection*.36;
        for(int attempt=0;attempt<12;attempt++) {
            Vec3 point;
            if(target!=null) {
                double angle=orbitPhase+attempt*2.39996;
                double rise=enraged?1+random.nextDouble()*3:2+random.nextDouble()*4;
                point=target.position().add(Math.cos(angle)*orbitRadius,rise,Math.sin(angle)*orbitRadius);
                if(attempt==0 && distanceToSqr(target)>256 && !stalled) point=target.position().add(0,2,0);
            } else point=position().add((random.nextDouble()-.5)*20,(random.nextDouble()-.5)*12,(random.nextDouble()-.5)*20);
            if(!open(point)) continue;
            var path=navigation.createPath(BlockPos.containing(point),0);
            if(path!=null && path.canReach()) { navigation.moveTo(path,speed); return; }
        }
        // A short reachable detour gives the pathfinder a new origin instead of retrying an island forever.
        for(int i=0;i<12;i++) {
            Vec3 point=position().add((random.nextDouble()-.5)*8,(random.nextDouble()-.3)*8,(random.nextDouble()-.5)*8);
            if(!open(point)) continue;
            Vec3 delta=point.subtract(position());
            boolean clear=true;
            for(int step=1;step<=8;step++) if(!open(position().add(delta.scale(step/8.0)))) { clear=false; break; }
            if(clear) { moveControl.setWantedPosition(point.x,point.y,point.z,speed); return; }
        }
    }

    // ---- Lifecycle ----
    private void clearVictim() { attackTarget=null; grabStart=null; entityData.set(VICTIM,-1); blockedTicks=0; grabBreakDamage=0; }
    private void releaseBurst() {
        if(isGrabbing() && level() instanceof ServerLevel server) {
            Player player=victim(server);
            if(player!=null) NaeglerophaeonEffects.releaseBurst(player);
        }
    }
    private void release() { releaseBurst(); clearVictim(); recover(NaeglerophaeonCombatRules.recoveryTicks(enraged)); }
    @Override public boolean hurt(DamageSource source,float amount) {
        float before=getHealth();
        bypassDamage=source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        boolean hit;
        try { hit=super.hurt(source,amount); } finally { bypassDamage=false; }
        float dealt=Math.max(0,before-getHealth());
        if(hit && level() instanceof ServerLevel server) {
            if(server.getGameTime()-recentDamageStart>40) { recentDamageStart=server.getGameTime(); recentDamage=0; }
            recentDamage+=dealt;
        }
        if(hit && isGrabbing()) {
            grabBreakDamage+=dealt;
            if(NaeglerophaeonCombatRules.breaksGrab(grabBreakDamage)) release();
        }
        return hit;
    }
    private void clearTransient() {
        pendingSparks.clear(); conductions.clear(); homing.clear();
        blastTarget=null; retaliateTarget=null; retaliateGap=null;
    }
    @Override public void die(DamageSource source) {
        if(mindOrigin!=null && level() instanceof ServerLevel server) VagrantMindEncounterData.get(server).defeated(mindOrigin,getUUID());
        releaseBurst();
        clearVictim(); clearTransient(); super.die(source);
    }
    @Override public void remove(RemovalReason reason) {
        if(level() instanceof ServerLevel server && mindOrigin!=null) VagrantMindEncounterData.get(server).position(mindOrigin,getUUID(),blockPosition());
        releaseBurst();
        clearVictim(); clearTransient(); bossEvent.removeAllPlayers(); super.remove(reason);
    }
    @Override public void startSeenByPlayer(ServerPlayer player) { super.startSeenByPlayer(player); bossEvent.addPlayer(player); }
    @Override public void stopSeenByPlayer(ServerPlayer player) { super.stopSeenByPlayer(player); bossEvent.removePlayer(player); }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag); if(mindOrigin!=null) tag.putLong("MindOrigin",mindOrigin.asLong());
        tag.putLong("MindSeed",mindSeed); tag.putInt("ZapCooldown",zapCooldown); tag.putInt("GrabCooldown",grabCooldown);
        tag.putLongArray("MindNodes",nodes.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putBoolean("Enraged",enraged); tag.putBoolean("Overloading",overloading); tag.putBoolean("Drained",drained);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        // Stage flags first: LivingEntity restores health through setHealth, which honours them.
        drained=tag.getBoolean("Drained");
        overloading=!drained && tag.getBoolean("Overloading");
        enraged=drained || overloading || tag.getBoolean("Enraged");
        super.readAdditionalSaveData(tag); mindOrigin=tag.contains("MindOrigin")?BlockPos.of(tag.getLong("MindOrigin")):null;
        mindSeed=tag.getLong("MindSeed"); lobes=VagrantMindGeometry.lobes(mindSeed);
        if(tag.contains("MindNodes")) nodes=java.util.Arrays.stream(tag.getLongArray("MindNodes")).mapToObj(BlockPos::of).toList();
        else nodes=mindOrigin==null?List.of():generatedNodes(mindOrigin,mindSeed);
        zapCooldown=Math.clamp(tag.getInt("ZapCooldown"),0,90); grabCooldown=Math.clamp(tag.getInt("GrabCooldown"),0,200);
        pendingGap=null; repairTarget=null;
        repairTicks=0; repairTravelTicks=0; repairCooldown=0;
        clearVictim(); clearTransient(); endBlink(); entityData.set(HUNTING,false);
        entityData.set(ENRAGED,enraged); entityData.set(DRAINED_STATE,drained);
        bossEvent.setColor(drained?BossEvent.BossBarColor.WHITE:overloading?BossEvent.BossBarColor.YELLOW
                :enraged?BossEvent.BossBarColor.PURPLE:BossEvent.BossBarColor.RED);
        if(drained) { twitchTicks=20; phase(DRAINED); }
        else if(overloading) { overloadCenter=findCenter(); phase(ASCEND); }
        else phase(IDLE);
    }
    /** Minds saved before node lists were persisted regenerate the same deterministic web. */
    private static List<BlockPos> generatedNodes(BlockPos origin,long seed) {
        return VagrantMindFiberWeb.nodes(VagrantMindFiberWeb.strands(seed,VagrantMindGeometry.lobes(seed))).stream()
                .map(p->origin.offset(Mth.floor(p[0]),Mth.floor(p[1]),Mth.floor(p[2]))).toList();
    }
}
