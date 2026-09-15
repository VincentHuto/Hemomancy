package com.vincenthuto.hemomancy.common.block.harbinger;

import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.worldgen.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.Vec3;

public class PhlegethonticIchorBlock extends LiquidBlock {
    public PhlegethonticIchorBlock(FlowingFluid fluid, Properties properties) { super(fluid, properties); }

    @Override public net.minecraft.world.item.ItemStack pickupBlock(net.minecraft.world.entity.player.Player player,
            net.minecraft.world.level.LevelAccessor level,BlockPos pos,BlockState state) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    @Override protected void entityInside(BlockState state,Level level,BlockPos pos,Entity entity) {
        if (!(level instanceof ServerLevel server) || !(entity instanceof LivingEntity living)
                || entity.getType().is(PhlegethonticTags.RIVER_GUARDIANS)) return;
        CompoundTag data=entity.getPersistentData();
        long now=level.getGameTime();
        if(data.contains("PhlegethonticContact") && data.getLong("PhlegethonticContact")==now) return;
        data.putLong("PhlegethonticContact",now);
        if(!data.contains("PhlegethonticScald") || now-data.getLong("PhlegethonticScald")>=10) {
            data.putLong("PhlegethonticScald",now);
            if(!entity.fireImmune()) {
                entity.hurt(level.damageSources().hotFloor(),3F);
                entity.igniteForSeconds(3);
            }
            if(!HemoEntityPredicates.NOBLOOD.test(entity)) living.addEffect(new MobEffectInstance(EffectInit.blood_loss,60));
        }
        if(now%20==0 || !data.contains("PhlegethonticFlowX")) {
            Vec3 direction=current(server,pos);
            data.putDouble("PhlegethonticFlowX",direction.x);
            data.putDouble("PhlegethonticFlowZ",direction.z);
        }
        double force=PhlegethonticRules.pulsing(now)?.014:.007;
        Vec3 velocity=entity.getDeltaMovement();
        Vec3 addition=new Vec3(data.getDouble("PhlegethonticFlowX"),0,data.getDouble("PhlegethonticFlowZ")).scale(force);
        if(velocity.horizontalDistanceSqr()<.09) {
            Vec3 pushed=velocity.add(addition);
            double speed=pushed.horizontalDistance();
            if(speed>.3)pushed=new Vec3(pushed.x*.3/speed,pushed.y,pushed.z*.3/speed);
            entity.setDeltaMovement(pushed);entity.hurtMarked=true;
        }
    }

    private static Vec3 current(ServerLevel level,BlockPos pos) {
        if(pos.getY()<=level.getMinBuildHeight()+20) {
            var nodes=PhlegethonticVeinPath.nearChunk(level.getSeed(),pos.getX()>>4,pos.getZ()>>4,level.getMinBuildHeight());
            double closest=Double.POSITIVE_INFINITY; Vec3 result=Vec3.ZERO;
            for(int i=1;i<nodes.size();i++) {
                var a=nodes.get(i-1);var b=nodes.get(i);
                if(Math.abs(a.x()-b.x())>2 || Math.abs(a.z()-b.z())>2) continue;
                double distance=pos.distSqr(new BlockPos(a.x(),a.y(),a.z()));
                if(distance<closest) {closest=distance;result=new Vec3(b.x()-a.x(),0,b.z()-a.z()).normalize();}
            }
            return closest<25?result:Vec3.ZERO;
        }
        var column=PhlegethonticTerrainPlan.column(PhlegethonticBasinLayout.nearChunk(
                level.getSeed(),pos.getX()>>4,pos.getZ()>>4,level.getChunkSource().getGenerator().getSeaLevel()),pos.getX(),pos.getZ());
        return column.distance()<0?new Vec3(column.dx(),0,column.dz()).normalize():Vec3.ZERO;
    }

    @Override public void animateTick(BlockState state,Level level,BlockPos pos,RandomSource random) {
        if(PhlegethonticRules.pulsing(level.getGameTime()) && random.nextInt(10)==0 && level.getBlockState(pos.above()).isAir())
            PhlegethonticVisuals.surfacePulse(level,pos.getX()+random.nextDouble(),pos.getY()+.92,pos.getZ()+random.nextDouble());
    }
}
