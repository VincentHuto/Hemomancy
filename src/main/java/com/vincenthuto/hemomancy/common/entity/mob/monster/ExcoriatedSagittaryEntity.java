package com.vincenthuto.hemomancy.common.entity.mob.monster;

import com.vincenthuto.hemomancy.common.entity.projectile.RecallBarbEntity;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.worldgen.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

public class ExcoriatedSagittaryEntity extends Monster {
    public static final int IDLE=0,DRAW=1,FIRE=2,REAR=3,BREATH=4;
    private static final EntityDataAccessor<Integer> ATTACK=SynchedEntityData.defineId(ExcoriatedSagittaryEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_TIME=SynchedEntityData.defineId(ExcoriatedSagittaryEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CLOTTED=SynchedEntityData.defineId(ExcoriatedSagittaryEntity.class,EntityDataSerializers.BOOLEAN);
    private static final ResourceLocation CLOT_SPEED=ResourceLocation.fromNamespaceAndPath("hemomancy","sagittary_clotting");
    private BlockPos home;
    private BlockPos river;
    private boolean sentinel;
    private int dryTicks,shotCooldown,breathCooldown,attackTicks,volleyDelay;
    private long lastSeen;

    public ExcoriatedSagittaryEntity(EntityType<? extends ExcoriatedSagittaryEntity> type,Level level) {
        super(type,level);xpReward=12;
        setPathfindingMalus(PathType.WATER,0);
        setPathfindingMalus(PathType.WATER_BORDER,0);
    }
    public static AttributeSupplier.Builder setAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,52).add(Attributes.MOVEMENT_SPEED,.28)
                .add(Attributes.ATTACK_DAMAGE,8).add(Attributes.FOLLOW_RANGE,40)
                .add(Attributes.ARMOR,6).add(Attributes.KNOCKBACK_RESISTANCE,.45);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);builder.define(ATTACK,IDLE);builder.define(ATTACK_TIME,0);builder.define(CLOTTED,false);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(5,new LookAtPlayerGoal(this,Player.class,16));
        goalSelector.addGoal(6,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
    }
    public void setHome(BlockPos pos,boolean sentinel) {this.home=pos.immutable();this.sentinel=sentinel;restrictTo(home,32);}
    public BlockPos home() {return home==null?blockPosition():home;}
    public boolean isSentinel() {return sentinel;}
    public int attackState() {return entityData.get(ATTACK);}
    public int attackTime() {return entityData.get(ATTACK_TIME);}
    public boolean clotted() {return entityData.get(CLOTTED);}
    private void attack(int state) {entityData.set(ATTACK,state);attackTicks=0;entityData.set(ATTACK_TIME,0);}

    @Override protected void customServerAiStep() {
        super.customServerAiStep();
        if(!(level() instanceof ServerLevel server)) return;
        if(home==null) setHome(blockPosition(),false);
        if(shotCooldown>0)shotCooldown--;
        if(breathCooldown>0)breathCooldown--;
        if(tickCount%20==0) river=ExcoriatedSagittarySpawnRules.nearestIchor(level(),blockPosition(),8,5);
        dryTicks=river==null?dryTicks+1:Math.max(0,dryTicks-4);
        boolean clot=dryTicks>=100;
        if(clot!=clotted()) {
            entityData.set(CLOTTED,clot);
            var speed=getAttribute(Attributes.MOVEMENT_SPEED);
            if(speed!=null) {
                speed.removeModifier(CLOT_SPEED);
                if(clot)speed.addTransientModifier(new AttributeModifier(CLOT_SPEED,-.5,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
        if(dryTicks>=200 && tickCount%20==0)hurt(damageSources().dryOut(),1);
        if(getFluidTypeHeight(FluidInit.PHLEGETHONTIC_ICHOR_TYPE.get())>1.15)
            setDeltaMovement(getDeltaMovement().add(0,.045,0));

        LivingEntity target=getTarget();
        if(target!=null && (!target.isAlive() || distanceToSqr(target)>1600 || target instanceof Player p && (p.isCreative()||p.isSpectator()))) {
            setTarget(null);target=null;
        }
        if(tickCount%10==0 && (target==null || target instanceof Player)) {
            Player best=null;double score=Double.NEGATIVE_INFINITY;
            for(Player player:server.players()) {
                if(player.isCreative()||player.isSpectator()||!player.isAlive()||distanceToSqr(player)>1600)continue;
                if(server.getFluidState(BlockPos.containing(player.getEyePosition())).is(PhlegethonticTags.ICHOR)
                        && getLastHurtByMob()!=player)continue;
                boolean visible=hasLineOfSight(player);
                if(!visible && (target!=player || !player.hasEffect(EffectInit.blood_loss) || server.getGameTime()-lastSeen>100))continue;
                long contact=player.getPersistentData().getLong("PhlegethonticContact");
                double priority=-distanceToSqr(player)+(contact>0 && server.getGameTime()-contact<60?1200:0)
                        +(river!=null && player.getY()>river.getY()+4?500:0);
                if(priority>score){score=priority;best=player;}
            }
            setTarget(best);target=best;
        }
        if(target!=null && hasLineOfSight(target))lastSeen=server.getGameTime();
        if(target!=null)getLookControl().setLookAt(target,30,30);
        if(attackState()!=IDLE) {
            attackTicks++;entityData.set(ATTACK_TIME,attackTicks);
            if(attackState()==REAR && attackTicks>=20) {attack(BREATH);breathCooldown=100;}
            else if(attackState()==BREATH) {
                if(attackTicks<=12) breath(server,target);
                else attack(IDLE);
            } else if(attackState()==FIRE && attackTicks>=8)attack(IDLE);
            else if(attackState()==DRAW && attackTicks>=20) {
                if(target!=null && hasLineOfSight(target) && distanceToSqr(target)<=576) {
                    boolean elevated=river!=null && target.getY()>river.getY()+4;
                    shoot(target,!elevated);if(elevated)volleyDelay=8;
                    // Reserve the final twenty ticks of the interval for drawing the next shot.
                    shotCooldown=(PhlegethonticRules.pulsing(server.getGameTime())?40:60)*(clot?2:1)-20;
                }
                attack(FIRE);
            }
        }
        if(volleyDelay>0 && --volleyDelay==0 && target!=null && hasLineOfSight(target))shoot(target,false);
        if(distanceToSqr(Vec3.atBottomCenterOf(home))>32*32 || dryTicks>=100) {
            getNavigation().moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,1);
        } else if(target!=null) {
            double distance=distanceToSqr(target);
            if(distance<9 && breathCooldown==0 && attackState()==IDLE) {getNavigation().stop();attack(REAR);}
            else if(distance<=576 && hasLineOfSight(target)) {
                getNavigation().stop();
                if(shotCooldown==0 && attackState()==IDLE)attack(DRAW);
                if(distance<4 && breathCooldown>0 && tickCount%30==0)doHurtTarget(target);
            } else if(target.distanceToSqr(Vec3.atBottomCenterOf(home))<32*32)
                getNavigation().moveTo(target,1);
        } else if(tickCount%80==0) {
            BlockPos candidate=home.offset(random.nextInt(25)-12,0,random.nextInt(25)-12);
            for(int y=5;y>=-5;y--) if(ExcoriatedSagittarySpawnRules.validShore(server,candidate.above(y))) {
                getNavigation().moveTo(candidate.getX()+.5,candidate.getY()+y,candidate.getZ()+.5,.7);break;
            }
        }
    }

    private void shoot(LivingEntity target,boolean tether) {
        var barb=new RecallBarbEntity(level(),this,tether);
        Vec3 aim=target.getBoundingBox().getCenter().subtract(barb.position());
        barb.shoot(aim.x,aim.y+aim.horizontalDistance()*.12,aim.z,1.6F,2F);
        level().addFreshEntity(barb);playSound(SoundEvents.SKELETON_SHOOT,1,.6F);
    }
    private void breath(ServerLevel server,@Nullable LivingEntity target) {
        Vec3 forward=target==null?getLookAngle():target.position().subtract(position()).normalize();
        for(int i=1;i<=4;i++)PhlegethonticVisuals.breath(server,getX(),getY()+1.5,getZ(),forward.x*i,forward.z*i);
        if(attackTicks!=1 && attackTicks!=11)return;
        for(LivingEntity victim:server.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(4),e -> e!=this && !(e instanceof ExcoriatedSagittaryEntity))) {
            Vec3 toward=victim.getBoundingBox().getCenter().subtract(getEyePosition());
            if(toward.lengthSqr()>20 || toward.normalize().dot(forward)<.65 || !hasLineOfSight(victim))continue;
            if(victim instanceof Player player && (player.isCreative()||player.isSpectator()))continue;
            victim.hurt(damageSources().hotFloor(),3);victim.igniteForSeconds(3);
            if(!com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates.NOBLOOD.test(victim))
                victim.addEffect(new MobEffectInstance(EffectInit.blood_loss,60));
        }
    }
    @Override public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
        return type!=FluidInit.PHLEGETHONTIC_ICHOR_TYPE.get() && super.canDrownInFluidType(type);
    }
    @Override public boolean isPushedByFluid(net.neoforged.neoforge.fluids.FluidType type) {
        return type!=FluidInit.PHLEGETHONTIC_ICHOR_TYPE.get() && super.isPushedByFluid(type);
    }
    @Override protected SoundEvent getAmbientSound() {return SoundEvents.HORSE_BREATHE;}
    @Override protected SoundEvent getHurtSound(DamageSource source) {return SoundEvents.HORSE_HURT;}
    @Override protected SoundEvent getDeathSound() {return SoundEvents.HORSE_DEATH;}
    @Override protected float getSoundVolume() {return .7F;}
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);if(home!=null)tag.putLong("PhlegethonHome",home.asLong());
        tag.putBoolean("PhlegethonSentinel",sentinel);tag.putInt("PhlegethonDryTicks",dryTicks);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if(tag.contains("PhlegethonHome"))setHome(BlockPos.of(tag.getLong("PhlegethonHome")),tag.getBoolean("PhlegethonSentinel"));
        dryTicks=Math.clamp(tag.getInt("PhlegethonDryTicks"),0,200);attack(IDLE);
    }
}
