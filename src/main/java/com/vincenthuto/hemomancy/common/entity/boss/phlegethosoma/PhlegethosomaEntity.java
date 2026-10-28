package com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma;

import com.vincenthuto.hemomancy.common.entity.boss.*;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.*;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.PhlegethosomaSuctionPacket;
import com.vincenthuto.hemomancy.common.worldgen.terrain.phlegethontic.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import java.util.*;

public final class PhlegethosomaEntity extends Monster {
    public enum Action { DORMANT, SEEKING, SUCTION_WINDUP, SUCTION_ACTIVE, LATCH_ATTACK, CLOT_SPIT, ICHOR_SPRAY, LUNGE, STAGGERED, RECOVERY, DIVE, BREACH, RETURN_TO_ICHOR }
    private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(PhlegethosomaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TIME = SynchedEntityData.defineId(PhlegethosomaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(PhlegethosomaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> AIM = SynchedEntityData.defineId(PhlegethosomaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Long> EPOCH = SynchedEntityData.defineId(PhlegethosomaEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> CROP = SynchedEntityData.defineId(PhlegethosomaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> INTRO = SynchedEntityData.defineId(PhlegethosomaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SWIMMING = SynchedEntityData.defineId(PhlegethosomaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> SWIM_AMOUNT = SynchedEntityData.defineId(PhlegethosomaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SWIM_PITCH = SynchedEntityData.defineId(PhlegethosomaEntity.class, EntityDataSerializers.FLOAT);
    private final BossReplayClock clock = new BossReplayClock();
    private final BossIntroWindow intro = new BossIntroWindow(60);
    private final BossBarBinding bar = new BossBarBinding("entity.hemomancy.phlegethosoma", BossPresentationIds.FRAME_PHLEGETHOSOMA, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
    private final PhlegethosomaBreach breach = new PhlegethosomaBreach(this);
    private final Set<UUID> contacts = new HashSet<>();
    private BlockPos home;
    private boolean introduced;
    private int suctionCooldown, clotCooldown, sprayCooldown, lungeCooldown, breachCooldown, missingTarget;
    private float interruptDamage;

    public PhlegethosomaEntity(EntityType<? extends PhlegethosomaEntity> type, Level level) { super(type,level); xpReward=15;
        setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER,0);getNavigation().setCanFloat(true);bar.setVisible(false); }
    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,120).add(Attributes.ATTACK_DAMAGE,7)
                .add(Attributes.MOVEMENT_SPEED,.20).add(Attributes.FOLLOW_RANGE,24).add(Attributes.KNOCKBACK_RESISTANCE,.85).add(Attributes.ARMOR,6);
    }
    @Override protected void registerGoals() { targetSelector.addGoal(1,new HurtByTargetGoal(this)); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);b.define(ACTION,0);b.define(TIME,0);b.define(PHASE,1);b.define(AIM,0F);b.define(EPOCH,0L);b.define(CROP,0);b.define(INTRO,-1);b.define(SWIMMING,false);b.define(SWIM_AMOUNT,0F);b.define(SWIM_PITCH,0F);
    }
    public Action action() { return Action.values()[entityData.get(ACTION)]; }
    public int phase() { return entityData.get(PHASE); }
    public int actionTime() { return entityData.get(TIME); }
    public long attackEpoch() { return entityData.get(EPOCH); }
    public float animationTime(float partial) { return clock.sample(entityData.get(ACTION),actionTime(),tickCount,partial,1); }
    public int introTime() {return entityData.get(INTRO);}
    public boolean swollen() { return phase()>=2 || entityData.get(CROP)>0; }
    public boolean isIchorSwimming() { return entityData.get(SWIMMING); }
    public boolean isBreachAttack() { return action()==Action.DIVE || action()==Action.BREACH || action()==Action.RETURN_TO_ICHOR; }
    void faceBreach(Vec3 movement) {
        setYRot((float)Math.toDegrees(Math.atan2(-movement.x,movement.z)));
        yBodyRot=getYRot();setYHeadRot(getYRot());entityData.set(AIM,getYRot());
    }
    public float swimAmount() { return entityData.get(SWIM_AMOUNT); }
    public float swimPitch() { return entityData.get(SWIM_PITCH); }
    public Vec3 mouth() { return position().add(forward().scale(.85)).add(0,.35,0); }
    private Vec3 forward() { double a=Math.toRadians(entityData.get(AIM));return new Vec3(-Math.sin(a),0,Math.cos(a)); }
    boolean valid(LivingEntity e) { return e!=null && e.level()==level() && ManipulationCombatHelper.canHarmFor(this,e); }
    void change(Action next) {
        entityData.set(ACTION,next.ordinal());entityData.set(TIME,0);entityData.set(EPOCH,level().getGameTime());contacts.clear();interruptDamage=0;
        if(next!=Action.SEEKING && next!=Action.DORMANT) getNavigation().stop();
        if(next==Action.SUCTION_WINDUP) playSound(SoundEvents.SLIME_SQUISH,1,.45F);
        if(next==Action.CLOT_SPIT || next==Action.ICHOR_SPRAY) playSound(SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT,1,.65F);
    }
    @Override protected void customServerAiStep() {
        super.customServerAiStep();if(!(level() instanceof ServerLevel server))return;
        if(home==null)home=blockPosition();
        entityData.set(SWIMMING, getFluidTypeHeight(FluidInit.PHLEGETHONTIC_ICHOR_TYPE.get()) > .1);
        entityData.set(SWIM_AMOUNT, isIchorSwimming() || isBreachAttack() ? (float)Math.min(1, getDeltaMovement().length() / .4) : 0F);
        Vec3 motion = getDeltaMovement();
        float pitch = isIchorSwimming() || isBreachAttack() ? (float)-Math.atan2(motion.y, Math.max(.001,motion.horizontalDistance())) : 0;
        entityData.set(SWIM_PITCH, entityData.get(SWIM_PITCH) + (pitch-entityData.get(SWIM_PITCH))*.2F);
        bar.trackHealth(this);
        if(entityData.get(CROP)>0)entityData.set(CROP,entityData.get(CROP)-1);
        suctionCooldown=Math.max(0,suctionCooldown-1);clotCooldown=Math.max(0,clotCooldown-1);
        breachCooldown=Math.max(0,breachCooldown-1);
        sprayCooldown=Math.max(0,sprayCooldown-1);lungeCooldown=Math.max(0,lungeCooldown-1);
        if(intro.active()) { if(isIchorSwimming())setDeltaMovement(Vec3.ZERO);intro.tick();entityData.set(INTRO,intro.elapsed());bar.setVisible(!intro.active());return; }
        int nextPhase=PhlegethosomaCombatRules.phase(phase(),getHealth(),getMaxHealth());
        if(nextPhase>phase()) {
            entityData.set(PHASE,nextPhase);bar.setColor(PhlegethosomaCombatRules.barColor(nextPhase));getAttribute(Attributes.ARMOR).setBaseValue(nextPhase==3?0:3);
            if(isBreachAttack())breach.returnToPool();else change(Action.RECOVERY);playSound(SoundEvents.SLIME_DEATH,1,.5F);return;
        }
        LivingEntity target=getTarget();
        if(!valid(target) || target.distanceToSqr(Vec3.atBottomCenterOf(home))>24*24) { setTarget(null);target=null; }
        if(target==null && tickCount%10==0) {
            target=server.players().stream().filter(p->valid(p) && distanceToSqr(p)<=256 && hasLineOfSight(p))
                    .min(Comparator.comparingDouble(p->distanceToSqr(p))).orElse(null);
            setTarget(target);
        }
        if(isBreachAttack()) {
            entityData.set(TIME,actionTime()+1);breach.tick(server,target);return;
        }
        if(target==null) {
            if(action()!=Action.DORMANT && action()!=Action.RECOVERY)change(Action.RECOVERY);
            if(++missingTarget>=40 && action()!=Action.DORMANT) { clearHazards();change(Action.DORMANT);bar.setVisible(false); }
            if(isIchorSwimming()) {
                double angle=server.getGameTime()*.035;
                swimToward(Vec3.atCenterOf(home).add(Math.sin(angle)*5,0,Math.cos(angle)*5),.18);
            } else if(distanceToSqr(Vec3.atBottomCenterOf(home))>4)getNavigation().moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,1);
            return;
        }
        missingTarget=0;
        if(!introduced) { introduced=true;intro.begin();entityData.set(INTRO,0);bar.setVisible(false);BossScenes.play(this,BossPresentationIds.SCENE_PHLEGETHOSOMA);change(Action.RECOVERY);return; }
        bar.setVisible(true);
        int t=actionTime();entityData.set(TIME,t+1);
        if(action()==Action.DORMANT)change(Action.SEEKING);
        if(action()==Action.SEEKING) {
            getLookControl().setLookAt(target,30,30);
            entityData.set(AIM,(float)Math.toDegrees(Math.atan2(target.getZ()-getZ(),target.getX()-getX()))-90);
            if(!hasLineOfSight(target)) { pursue(target);return; }
            double d=distanceTo(target);
            if(breachCooldown==0 && isIchorSwimming() && d>=6 && d<=14) {
                boolean started=breach.begin(target);breachCooldown=started?cooldown(200):40;
                if(started)return;
            }
            if(d<3)change(Action.LATCH_ATTACK);
            else if(suctionCooldown==0 && d<10) { suctionCooldown=PhlegethosomaCombatRules.suctionCooldown(phase());change(Action.SUCTION_WINDUP); }
            else if(phase()>=2 && sprayCooldown==0 && d<8) {sprayCooldown=cooldown(140);change(Action.ICHOR_SPRAY);}
            else if(clotCooldown==0 && d<14) {clotCooldown=cooldown(100);change(Action.CLOT_SPIT);}
            else if(lungeCooldown==0 && d<7) {lungeCooldown=cooldown(100);change(Action.LUNGE);}
            else pursue(target);
            return;
        }
        setYRot(entityData.get(AIM));yBodyRot=getYRot();getNavigation().stop();
        if(action()!=Action.LUNGE)setDeltaMovement(0,isIchorSwimming()?0:getDeltaMovement().y,0);
        switch(action()) {
            case SUCTION_WINDUP -> { effect(server,true);if(t>=PhlegethosomaCombatRules.suction(phase()).windup())change(Action.SUCTION_ACTIVE); }
            case SUCTION_ACTIVE -> {
                effect(server,true);
                for(LivingEntity victim:server.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(10),this::valid)) {
                    if(!inCone(victim,10,50))continue;
                    double force=victim.isInFluidType(FluidInit.PHLEGETHONTIC_ICHOR_TYPE.get())?.06:.04;
                    if(!PhlegethosomaMovement.safe(victim,mouth().subtract(victim.position()).multiply(1,0,1).normalize().scale(force)))continue;
                    if(victim instanceof ServerPlayer p)PacketHandler.sendToPlayer(p,new PhlegethosomaSuctionPacket(getId(),getUUID(),attackEpoch(),force));
                    else applySuction(victim,force);
                }
                if(t>=PhlegethosomaCombatRules.suction(phase()).active())change(Action.RECOVERY);
            }
            case LATCH_ATTACK -> {
                if(t==15 && distanceTo(target)<3.5 && visible(target) && target.hurt(damageSources().mobAttack(this),7)) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,20,1));heal(2);entityData.set(CROP,30);playSound(SoundEvents.GENERIC_EAT,1,.5F);
                }
                if(t>=45)change(Action.RECOVERY);
            }
            case CLOT_SPIT -> { if(t==20)spit(server,target);if(t>=46)change(Action.RECOVERY); }
            case ICHOR_SPRAY -> {
                if(t>=24 && t<36) {
                    effect(server,false);
                    for(LivingEntity v:server.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(8),this::valid))
                        if(inCone(v,8,30) && contacts.add(v.getUUID()))v.hurt(damageSources().mobAttack(this),6);
                }
                if(t>=66)change(Action.RECOVERY);
            }
            case LUNGE -> {
                if(t>=20 && t<28) {
                    Vec3 step=forward().scale(.5);
                    if(isIchorSwimming()) {
                        step=forward().scale(.6);
                        if(swimSpace(step))setDeltaMovement(step);
                    } else if(PhlegethosomaMovement.safe(this,step))setDeltaMovement(step.x,getDeltaMovement().y,step.z);
                    for(LivingEntity v:server.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(.3),this::valid))
                        if(visible(v) && contacts.add(v.getUUID())) {v.hurt(damageSources().mobAttack(this),7);v.knockback(.5,-forward().x,-forward().z);}
                }
                if(t>=28)setDeltaMovement(0,getDeltaMovement().y,0);
                if(t>=58)change(Action.RECOVERY);
            }
            case STAGGERED -> {if(t>=35)change(Action.RECOVERY);}
            case RECOVERY -> {if(t>=25)change(Action.SEEKING);}
            default -> {}
        }
    }
    @Override public boolean checkSpawnObstruction(net.minecraft.world.level.LevelReader level) {
        return level.isUnobstructed(this);
    }

    /** Ichor is a custom fluid, so vanilla water-only swimming controls do not drive this animal. */
    @Override public void travel(Vec3 input) {
        if(isBreachAttack() && isEffectiveAi()) {
            move(MoverType.SELF,getDeltaMovement());calculateEntityAnimation(false);
        } else if(getFluidTypeHeight(FluidInit.PHLEGETHONTIC_ICHOR_TYPE.get()) > .1 && isEffectiveAi()) {
            move(MoverType.SELF,getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(.88));
            calculateEntityAnimation(false);
        } else super.travel(input);
    }

    private void pursue(LivingEntity target) {
        if(!isIchorSwimming()) {getNavigation().moveTo(target,1);return;}
        Vec3 destination=target.position().add(0,target.getBbHeight()*.3,0);
        if(!target.isInFluidType(FluidInit.PHLEGETHONTIC_ICHOR_TYPE.get()))
            destination=new Vec3(destination.x,getY(),destination.z);
        if(distanceTo(target)<5) {
            Vec3 radial=position().subtract(target.position()).multiply(1,0,1).normalize();
            destination=target.position().add(radial.scale(4)).add(-radial.z*3,0,radial.x*3);
            destination=new Vec3(destination.x,getY(),destination.z);
        }
        swimToward(destination,PhlegethosomaSwimmingRules.speed(phase()));
    }

    private void swimToward(Vec3 destination,double speed) {
        getNavigation().stop();
        Vec3 desired=destination.subtract(position());
        if(desired.lengthSqr()<.1)return;
        float yaw=(float)Math.toDegrees(Math.atan2(-desired.x,desired.z));
        yaw=net.minecraft.util.Mth.approachDegrees(getYRot(),yaw,18);
        double vertical=Math.clamp(desired.y*.18,-.14,.14);
        for(float offset:new float[]{0,35,-35,70,-70,110,-110,180}) {
            double angle=Math.toRadians(yaw+offset);
            for(double lift:new double[]{vertical,0}) {
                Vec3 step=new Vec3(-Math.sin(angle)*speed,lift,Math.cos(angle)*speed);
                if(!swimSpace(step))continue;
                setYRot(yaw+offset);yBodyRot=getYRot();setYHeadRot(getYRot());
                entityData.set(AIM,getYRot());setDeltaMovement(step);return;
            }
        }
        setDeltaMovement(Vec3.ZERO);
    }

    private boolean swimSpace(Vec3 movement) {
        for(int i=1;i<=4;i++) {
            Vec3 step=movement.scale(i/4.0);
            var box=getBoundingBox().move(step);
            if(!level().hasChunkAt(BlockPos.containing(box.minX,box.minY,box.minZ))
                    || !level().hasChunkAt(BlockPos.containing(box.maxX,box.maxY,box.maxZ))
                    || !level().noCollision(this,box))return false;
            BlockPos sample=BlockPos.containing(position().add(step).add(0,.2,0));
            if(!level().getFluidState(sample).is(PhlegethonticTags.ICHOR))return false;
        }
        return true;
    }

    private int cooldown(int ticks) {return phase()==3?ticks*4/5:ticks;}
    private void effect(ServerLevel s,boolean inward) {
        if(tickCount%3!=0)return;
        var particle=new com.vincenthuto.hutoslib.client.particle.data.ColorParticleData(com.vincenthuto.hutoslib.common.registry.HLParticleInit.glow.get(),new com.vincenthuto.hutoslib.client.particle.util.ParticleColor(155,25,23));
        for(int i=1;i<=7;i++) {
            Vec3 p=mouth().add(forward().scale(i));Vec3 velocity=forward().scale(inward?-.12:.16);
            s.sendParticles(particle,p.x,p.y,p.z,0,velocity.x,velocity.y,velocity.z,1);
        }
    }
    public boolean visible(LivingEntity target) {
        if(!hasLineOfSight(target))return false;
        if(level().clip(new net.minecraft.world.level.ClipContext(mouth(),target.getBoundingBox().getCenter(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this)).getType()!=HitResult.Type.MISS)return false;
        Vec3 a=mouth(),b=target.getBoundingBox().getCenter();
        return level().getEntitiesOfClass(TemporaryCoagulumEntity.class,new AABB(a,b).inflate(1)).stream()
                .noneMatch(c->c.getBoundingBox().clip(a,b).isPresent());
    }
    public boolean inCone(LivingEntity target,double range,double halfAngle) {
        Vec3 d=target.getBoundingBox().getCenter().subtract(mouth());
        return d.lengthSqr()<=range*range && d.horizontalDistance()>0 && forward().dot(new Vec3(d.x,0,d.z).normalize())>=Math.cos(Math.toRadians(halfAngle)) && visible(target);
    }
    public void applySuction(LivingEntity victim,double force) {
        if(action()!=Action.SUCTION_ACTIVE || !valid(victim) || !inCone(victim,10,50))return;
        Vec3 direction=mouth().subtract(victim.position()).multiply(1,0,1).normalize();
        Vec3 velocity=victim.getDeltaMovement();double inward=velocity.dot(direction);
        Vec3 addition=direction.scale(Math.max(0,Math.min(force,.20-inward)));
        if(PhlegethosomaMovement.safe(victim,velocity.multiply(1,0,1).add(addition))) {
            victim.setDeltaMovement(velocity.add(addition));if(!level().isClientSide)victim.hurtMarked=true;
        }
    }
    private void spit(ServerLevel server,LivingEntity target) {
        int count=PhlegethosomaCombatRules.clotCount(phase(),server.getEntitiesOfClass(TemporaryCoagulumEntity.class,getBoundingBox().inflate(32),c->getUUID().equals(c.ownerId())).size());
        for(int i=0;i<count;i++) {
            var shot=new CoagulumProjectileEntity(EntityInit.coagulum_projectile.get(),level());shot.setOwner(this);shot.setPos(mouth());
            Vec3 d=target.position().add((i-count/2.0)*1.7,.2,0).subtract(mouth());shot.shoot(d.x,d.y+d.horizontalDistance()*.18,d.z,1.05F,2);server.addFreshEntity(shot);
        }
        playSound(SoundEvents.SLIME_JUMP,1,.6F);
    }
    private void clearHazards() {
        if(!(level() instanceof ServerLevel s))return;
        for(var c:s.getEntitiesOfClass(TemporaryCoagulumEntity.class,getBoundingBox().inflate(64),c->getUUID().equals(c.ownerId())))c.discard();
        for(var c:s.getEntitiesOfClass(CoagulumProjectileEntity.class,getBoundingBox().inflate(64),c->c.getOwner()==this))c.discard();
    }
    @Override public boolean hurt(DamageSource source,float amount) {
        if(intro.active())return false;
        boolean exposed=action()==Action.SUCTION_WINDUP || action()==Action.SUCTION_ACTIVE || action()==Action.LATCH_ATTACK || action()==Action.ICHOR_SPRAY;
        boolean direct=source.getDirectEntity() instanceof LivingEntity || source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile;
        boolean result=super.hurt(source,exposed && direct?amount*1.25F:amount);
        if(result && isAlive() && direct && (action()==Action.SUCTION_WINDUP || action()==Action.SUCTION_ACTIVE)) {
            interruptDamage+=amount;
            if(PhlegethosomaCombatRules.interrupts(true,true,interruptDamage)) {change(Action.STAGGERED);playSound(SoundEvents.SLIME_HURT,1,.4F);}
        }
        return result;
    }
    @Override public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
        return type!=FluidInit.PHLEGETHONTIC_ICHOR_TYPE.get() && super.canDrownInFluidType(type);
    }
    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() {return SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT;}
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {return SoundEvents.SLIME_HURT;}
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound() {return SoundEvents.SLIME_DEATH;}
    @Override public void die(DamageSource source) {clearHazards();bar.hideAll();super.die(source);}
    @Override public void remove(RemovalReason reason) {clearHazards();bar.hideAll();super.remove(reason);}
    @Override public void startSeenByPlayer(ServerPlayer p) {super.startSeenByPlayer(p);bar.show(p);if(intro.active())BossScenes.playTo(p,this,BossPresentationIds.SCENE_PHLEGETHOSOMA,intro.elapsed());}
    @Override public void stopSeenByPlayer(ServerPlayer p) {super.stopSeenByPlayer(p);bar.hide(p);}
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);tag.putInt("FlukePhase",phase());tag.putBoolean("FlukeIntroduced",introduced);
        if(home!=null)tag.putLong("FlukeHome",home.asLong());intro.save(tag,"FlukeIntro");
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);entityData.set(PHASE,Math.clamp(tag.getInt("FlukePhase"),1,3));bar.setColor(PhlegethosomaCombatRules.barColor(phase()));introduced=tag.getBoolean("FlukeIntroduced");
        if(tag.contains("FlukeHome"))home=BlockPos.of(tag.getLong("FlukeHome"));intro.load(tag,"FlukeIntro");
        getAttribute(Attributes.ARMOR).setBaseValue(phase()==3?0:phase()==2?3:6);change(Action.RECOVERY);
    }
}
