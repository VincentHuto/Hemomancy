package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.FiberRepair;
import com.vincenthuto.hemomancy.common.worldgen.VagrantMindEncounterData;
import com.vincenthuto.hemomancy.common.worldgen.structure.VagrantMindGeometry;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
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
    public static final int IDLE=0, CHARGE=1, CAPTURE=2, GRAB=3, DISCHARGE=4, RECOVERY=5;
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(NaeglerophaeonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> PHASE_START = SynchedEntityData.defineId(NaeglerophaeonEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> VICTIM = SynchedEntityData.defineId(NaeglerophaeonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HUNTING = SynchedEntityData.defineId(NaeglerophaeonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Vector3f> CAPTURE_OFFSET = SynchedEntityData.defineId(NaeglerophaeonEntity.class, EntityDataSerializers.VECTOR3);
    private final ServerBossEvent bossEvent = new ServerBossEvent(Component.translatable("entity.hemomancy.naeglerophaeon"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
    private final NaeglerophaeonGripPart[] parts = {new NaeglerophaeonGripPart(this)};
    private BlockPos mindOrigin;
    private long mindSeed;
    private List<VagrantMindGeometry.Lobe> lobes = List.of();
    private UUID attackTarget;
    private Vec3 grabStart;
    private int zapCooldown, grabCooldown, blockedTicks, routeTicks;
    private float grabBreakDamage;
    private Vec3 routeSample;
    private BlockPos pendingGap, repairTarget;
    private boolean pendingUrgent, repairUrgent;
    private int repairTicks, repairTravelTicks, repairCooldown;

    public NaeglerophaeonEntity(EntityType<? extends NaeglerophaeonEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        setId(ENTITY_COUNTER.getAndAdd(parts.length + 1) + 1);
        moveControl = new FlyingMoveControl(this, 12, true);
    }
    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,80).add(Attributes.ARMOR,4)
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
    @Override public void travel(Vec3 input) {
        if(animationPhase()!=IDLE) { setDeltaMovement(Vec3.ZERO); return; }
        super.travel(input);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(PHASE,IDLE); b.define(PHASE_START,0L); b.define(VICTIM,-1); b.define(HUNTING,false);
        b.define(CAPTURE_OFFSET,new Vector3f());
    }
    public void bindMind(BlockPos origin,long seed,List<BlockPos> unusedNodes) {
        mindOrigin=origin.immutable(); mindSeed=seed; lobes=VagrantMindGeometry.lobes(seed);
    }
    /** Break events arrive before removal; the next server tick verifies the resulting gap. */
    public void noticeFiberBreak(BlockPos pos,boolean playerCut) {
        if(!(level() instanceof ServerLevel) || !isAlive()
                || position().distanceToSqr(Vec3.atCenterOf(pos))>96*96
                || !inMind(Vec3.atCenterOf(pos)) || repairUrgent && repairTarget!=null) return;
        if(pendingGap==null || playerCut || !pendingUrgent) {
            pendingGap=pos.immutable();
            pendingUrgent=playerCut;
        }
    }
    public BlockPos mindOrigin() { return mindOrigin; }
    public int animationPhase() { return entityData.get(PHASE); }
    public long animationStart() { return entityData.get(PHASE_START); }
    public int grabbedEntityId() { return entityData.get(VICTIM); }
    public boolean isGrabbing() { return animationPhase()==GRAB; }
    public boolean isHunting() { return entityData.get(HUNTING); }
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
    @Override protected SoundEvent getAmbientSound() { return SoundInit.ENTITY_NAEGLEROPHAEON_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundInit.ENTITY_NAEGLEROPHAEON_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return SoundInit.ENTITY_NAEGLEROPHAEON_DEATH.get(); }

    private void phase(int phase) {
        entityData.set(PHASE,phase); entityData.set(PHASE_START,level().getGameTime());
    }
    private boolean valid(Player p) {
        return p.isAlive() && !p.isCreative() && !p.isSpectator() && p.level()==level();
    }
    private Player victim(ServerLevel level) {
        Entity e=attackTarget==null ? null : level.getEntity(attackTarget);
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
        if(zapCooldown>0) zapCooldown--;
        if(grabCooldown>0) grabCooldown--;
        if(repairCooldown>0) repairCooldown--;
        acceptPendingGap(server);
        int state=animationPhase();
        long age=server.getGameTime()-animationStart();
        if(state!=IDLE) {
            navigation.stop(); setDeltaMovement(Vec3.ZERO);
            Player player=victim(server);
            if(state==RECOVERY) { if(age>=40) phase(IDLE); return; }
            if(state==DISCHARGE) { if(age>=6) phase(RECOVERY); return; }
            if(player==null || distanceToSqr(player)>32*32) { release(); return; }
            if(state==CHARGE && age>=20) {
                if(distanceToSqr(player)<=256 && hasLineOfSight(player)) {
                    NaeglerophaeonEffects.zap(server,core(),player.getEyePosition());
                    player.hurt(damageSources().mobAttack(this),4);
                }
                clearVictim(); phase(IDLE);
            } else if(state==CAPTURE && age>=20) {
                if(distanceToSqr(player)>144 || !hasLineOfSight(player)) { release(); return; }
                grabStart=player.position(); blockedTicks=0; grabBreakDamage=0;
                entityData.set(CAPTURE_OFFSET,grabStart.subtract(position()).toVector3f());
                phase(GRAB);
            } else if(state==GRAB) tickReel(server,player,age);
            return;
        }
        Player target=null;
        double best=32*32;
        for(ServerPlayer p:server.players()) if(valid(p) && inMind(p.position()) && distanceToSqr(p)<best) { best=distanceToSqr(p); target=p; }
        if(target==null && repairTarget==null && repairCooldown==0 && tickCount%100==0)
            scanForGap(server);
        if(repairTarget!=null && (repairUrgent || target==null)) {
            setTarget(null);
            entityData.set(HUNTING,false);
            if(tickRepair(server)) return;
        }
        setTarget(target);
        entityData.set(HUNTING,target!=null);
        swim(target);
        Vec3 motion=getDeltaMovement();
        if(motion.lengthSqr()>.0004) {
            setYRot(Mth.rotLerp(.15F,getYRot(),(float)Math.toDegrees(Math.atan2(-motion.x,motion.z))));
            setXRot(Mth.rotLerp(.12F,getXRot(),(float)-Math.toDegrees(Math.atan2(motion.y,motion.horizontalDistance()))));
            setYHeadRot(getYRot());
        }
        if(target==null || !hasLineOfSight(target)) return;
        if(grabCooldown==0 && distanceToSqr(target)<=144) {
            beginAttack(target,CAPTURE); grabCooldown=200;
        } else if(zapCooldown==0 && distanceToSqr(target)<=256) {
            beginAttack(target,CHARGE); zapCooldown=90;
        }
    }
    private Vec3 core() { return position().add(0,.8,0); }
    private void beginAttack(Player target,int state) {
        attackTarget=target.getUUID(); entityData.set(VICTIM,target.getId());
        entityData.set(CAPTURE_OFFSET,target.position().subtract(position()).toVector3f());
        navigation.stop(); setDeltaMovement(Vec3.ZERO); phase(state);
        NaeglerophaeonEffects.telegraph((ServerLevel)level(),core(),target.getEyePosition());
    }
    private boolean inMind(Vec3 point) {
        return mindOrigin==null || VagrantMindGeometry.field(lobes,point.x-mindOrigin.getX(),point.y-mindOrigin.getY(),point.z-mindOrigin.getZ())>=1.1;
    }
    private boolean open(Vec3 point) {
        return level().hasChunkAt(BlockPos.containing(point)) && inMind(point)
                && level().noCollision(this,getBoundingBox().move(point.subtract(position())));
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
        boolean urgent=pendingUrgent;
        pendingGap=null; pendingUrgent=false;
        if(!loadedBreak(server,gap) || !urgent && (!repairable(server,gap)
                || repairTarget!=null || repairCooldown>0)) return;
        repairTarget=gap;
        repairUrgent=urgent;
        repairTicks=0;
        repairTravelTicks=0;
        routeTicks=0;
        navigation.stop();
        if(urgent && animationPhase()!=IDLE && animationPhase()!=RECOVERY) release();
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
            repairUrgent=false;
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
        repairUrgent=false;
        repairTicks=0;
        repairTravelTicks=0;
        repairCooldown=cooldown;
        routeTicks=0;
        navigation.stop();
    }
    private boolean tickRepair(ServerLevel server) {
        BlockPos gap=repairTarget;
        if(!loadedBreak(server,gap)) { clearRepairTarget(40); return false; }
        boolean mendable=repairable(server,gap);
        if(!mendable && !repairUrgent) { clearRepairTarget(40); return false; }
        Vec3 center=Vec3.atCenterOf(gap);
        if(++repairTravelTicks>240) { clearRepairTarget(100); return false; }
        if(core().distanceToSqr(center)<=25 && clearRepairLine(server,core(),center)) {
            navigation.stop();
            setDeltaMovement(Vec3.ZERO);
            Vec3 look=center.subtract(core());
            setYRot((float)Math.toDegrees(Math.atan2(-look.x,look.z)));
            setXRot((float)-Math.toDegrees(Math.atan2(look.y,look.horizontalDistance())));
            setYHeadRot(getYRot());
            repairTicks++;
            if(repairTicks%8==0) NaeglerophaeonEffects.repairing(server,core(),center);
            if(repairTicks>=(mendable?40:20)) {
                if(mendable && FiberRepair.repair(server,gap)) NaeglerophaeonEffects.repaired(server,center);
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
    private void swim(Player target) {
        if(--routeTicks>0) return;
        boolean stalled=routeSample!=null && position().distanceToSqr(routeSample)<.25;
        routeSample=position(); routeTicks=20;
        for(int attempt=0;attempt<12;attempt++) {
            Vec3 point;
            if(target!=null) {
                double angle=tickCount*.018+attempt*2.39996;
                double radius=8+random.nextDouble()*4;
                point=target.position().add(Math.cos(angle)*radius,2+random.nextDouble()*4,Math.sin(angle)*radius);
                if(attempt==0 && distanceToSqr(target)>256 && !stalled) point=target.position().add(0,2,0);
            } else point=position().add((random.nextDouble()-.5)*20,(random.nextDouble()-.5)*12,(random.nextDouble()-.5)*20);
            if(!open(point)) continue;
            var path=navigation.createPath(BlockPos.containing(point),0);
            if(path!=null && path.canReach()) { navigation.moveTo(path,1); return; }
        }
        // A short reachable detour gives the pathfinder a new origin instead of retrying an island forever.
        for(int i=0;i<12;i++) {
            Vec3 point=position().add((random.nextDouble()-.5)*8,(random.nextDouble()-.3)*8,(random.nextDouble()-.5)*8);
            if(!open(point)) continue;
            Vec3 delta=point.subtract(position());
            boolean clear=true;
            for(int step=1;step<=8;step++) if(!open(position().add(delta.scale(step/8.0)))) { clear=false; break; }
            if(clear) { moveControl.setWantedPosition(point.x,point.y,point.z,1); return; }
        }
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
        if(age>=100) {
            if(player.position().distanceToSqr(destination)<1) {
                NaeglerophaeonEffects.zap(server,core(),player.getEyePosition());
                NaeglerophaeonEffects.releaseBurst(player);
                player.hurt(damageSources().mobAttack(this),8);
                player.push(direction.x*.65,.25,direction.z*.65); player.hurtMarked=true;
                clearVictim(); phase(DISCHARGE);
            } else release();
        }
    }
    private void clearVictim() { attackTarget=null; grabStart=null; entityData.set(VICTIM,-1); blockedTicks=0; grabBreakDamage=0; }
    private void releaseBurst() {
        if(isGrabbing() && level() instanceof ServerLevel server) {
            Player player=victim(server);
            if(player!=null) NaeglerophaeonEffects.releaseBurst(player);
        }
    }
    private void release() { releaseBurst(); clearVictim(); phase(RECOVERY); }
    @Override public boolean hurt(DamageSource source,float amount) {
        float before=getHealth(); boolean hit=super.hurt(source,amount);
        if(hit && isGrabbing()) {
            grabBreakDamage+=Math.max(0,before-getHealth());
            if(NaeglerophaeonCombatRules.breaksGrab(grabBreakDamage)) release();
        }
        return hit;
    }
    @Override public void die(DamageSource source) {
        if(mindOrigin!=null && level() instanceof ServerLevel server) VagrantMindEncounterData.get(server).defeated(mindOrigin,getUUID());
        releaseBurst();
        clearVictim(); super.die(source);
    }
    @Override public void remove(RemovalReason reason) {
        if(level() instanceof ServerLevel server && mindOrigin!=null) VagrantMindEncounterData.get(server).position(mindOrigin,getUUID(),blockPosition());
        releaseBurst();
        clearVictim(); bossEvent.removeAllPlayers(); super.remove(reason);
    }
    @Override public void startSeenByPlayer(ServerPlayer player) { super.startSeenByPlayer(player); bossEvent.addPlayer(player); }
    @Override public void stopSeenByPlayer(ServerPlayer player) { super.stopSeenByPlayer(player); bossEvent.removePlayer(player); }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag); if(mindOrigin!=null) tag.putLong("MindOrigin",mindOrigin.asLong());
        tag.putLong("MindSeed",mindSeed); tag.putInt("ZapCooldown",zapCooldown); tag.putInt("GrabCooldown",grabCooldown);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag); mindOrigin=tag.contains("MindOrigin")?BlockPos.of(tag.getLong("MindOrigin")):null;
        mindSeed=tag.getLong("MindSeed"); lobes=VagrantMindGeometry.lobes(mindSeed);
        zapCooldown=Math.clamp(tag.getInt("ZapCooldown"),0,90); grabCooldown=Math.clamp(tag.getInt("GrabCooldown"),0,200);
        pendingGap=null; repairTarget=null; pendingUrgent=false; repairUrgent=false;
        repairTicks=0; repairTravelTicks=0; repairCooldown=0;
        clearVictim(); entityData.set(HUNTING,false); phase(IDLE);
    }
}
