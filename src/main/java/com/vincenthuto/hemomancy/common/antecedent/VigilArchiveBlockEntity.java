package com.vincenthuto.hemomancy.common.antecedent;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.phys.Vec3;
import java.util.*;
import static com.vincenthuto.hemomancy.common.antecedent.AntecedentResearch.Evidence.*;

public final class VigilArchiveBlockEntity extends BlockEntity {
    private record Response(long time,BlockPos pos,int kind) {}
    private final PriorityQueue<Response> responses=new PriorityQueue<>(Comparator.comparingLong(Response::time));
    private final Set<Long> retired=new HashSet<>();
    private final Map<UUID,Integer> passage=new HashMap<>();
    private final Map<UUID,Long> secondClicks=new HashMap<>();
    private final Set<Long> resetFixtures=new HashSet<>();
    private long lastDisturbance=-1000,lastDemo=-1000;
    private int growthBudget;
    private long signalUntil;
    private boolean demonstrationDropped;
    private VigilLayout.Plan layout=VigilLayout.LEGACY;
    public VigilLayout.Plan layout() { return layout; }
    private float animation,previousAnimation;
    public VigilArchiveBlockEntity(BlockPos pos,BlockState state) { super(BlockEntityInit.antecedent_vessel.get(),pos,state); }
    public Rotation rotation() { return switch(getBlockState().getValue(VigilArchiveBlock.FACING)) {
        case EAST->Rotation.CLOCKWISE_90;case SOUTH->Rotation.CLOCKWISE_180;case WEST->Rotation.COUNTERCLOCKWISE_90;default->Rotation.NONE;}; }
    private Rotation inverse() { return switch(rotation()) {case CLOCKWISE_90->Rotation.COUNTERCLOCKWISE_90;case COUNTERCLOCKWISE_90->Rotation.CLOCKWISE_90;default->rotation();}; }
    public BlockPos origin() { return worldPosition.subtract(layout.vessel().rotate(rotation())); }
    public BlockPos world(BlockPos local) { return origin().offset(local.rotate(rotation())); }
    public BlockPos local(BlockPos world) { return world.subtract(origin()).rotate(inverse()); }
    public boolean isFixture(BlockPos pos) {
        if(retired.contains(pos.asLong()) || level==null || !VigilSites.authored(level,pos)) return false;
        var local=local(pos);
        return local.equals(layout.catalyst()) || local.equals(layout.shrieker()) || layout.sensors().contains(local);
    }
    public void retire(BlockPos pos) { retired.add(pos.asLong()); responses.removeIf(response->response.pos.equals(pos)); setChanged(); }
    public void signal(int elapsed) { signalUntil=level.getGameTime()+15; }
    public boolean responding() { return level!=null && level.getGameTime()<signalUntil; }
    public float response(float partial) { return previousAnimation+(animation-previousAnimation)*partial; }
    @Override public void onLoad() {
        super.onLoad(); VigilSites.add(this);
        for(var pos:layout.sensors())resetFixtures.add(world(pos).asLong());
        resetFixtures.add(world(layout.shrieker()).asLong());
    }
    @Override public void onChunkUnloaded() { VigilSites.remove(this); responses.clear(); super.onChunkUnloaded(); }
    @Override public void setRemoved() { VigilSites.remove(this); super.setRemoved(); }
    public void disturbance(Vec3 position) {
        if(!(level instanceof ServerLevel server) || level.getGameTime()-lastDisturbance<40) return;
        var local=local(BlockPos.containing(position));
        if(!layout.gallery().isInside(local)) return;
        lastDisturbance=level.getGameTime();
        int delay=80+server.random.nextInt(41);
        for(int i=0;i<3;i++) {
            var sensor=world(layout.sensors().get(i));
            if(!isFixture(sensor) || !level.getBlockState(sensor).is(Blocks.SCULK_SENSOR) || i==2 && server.random.nextBoolean()) continue;
            int wait=i==0?delay:i==1?delay+40+server.random.nextInt(41):delay+20;
            responses.add(new Response(level.getGameTime()+wait,sensor,0));
            if(i==0) server.sendParticles(new VibrationParticleOption(new BlockPositionSource(sensor),20),position.x,position.y,position.z,1,0,0,0,0);
        }
    }
    public void nourish(int experience) { growthBudget=Math.min(32,growthBudget+experience);setChanged(); }
    public void failShriek() {
        var pos=world(layout.shrieker());
        if(!(level instanceof ServerLevel server) || !isFixture(pos) || !level.getBlockState(pos).is(Blocks.SCULK_SHRIEKER)) return;
        level.setBlock(pos,level.getBlockState(pos).setValue(SculkShriekerBlock.SHRIEKING,true).setValue(SculkShriekerBlock.CAN_SUMMON,false),3);
        server.sendParticles(ParticleTypes.SCULK_SOUL,pos.getX()+.5,pos.getY()+.4,pos.getZ()+.5,3,.15,.05,.15,0);
        level.playSound(null,pos,SoundEvents.SCULK_CLICKING_STOP,SoundSource.BLOCKS,.12F,.6F);
        responses.add(new Response(level.getGameTime()+12,pos,2));
    }
    public static void tick(Level level,BlockPos pos,BlockState state,VigilArchiveBlockEntity site) {
        if(level.isClientSide) {site.previousAnimation=site.animation;site.animation=Math.clamp(site.animation+(site.responding()?1F/180:-1F/80),0,1);return;}
        if(!(level instanceof ServerLevel server)) return;
        long now=level.getGameTime();
        site.resetFixtures.removeIf(encoded->{
            var fixture=BlockPos.of(encoded);if(!level.hasChunkAt(fixture))return false;
            if(site.isFixture(fixture)) {
                var block=level.getBlockState(fixture);
                if(block.is(Blocks.SCULK_SENSOR))level.setBlock(fixture,block.setValue(SculkSensorBlock.PHASE,SculkSensorPhase.INACTIVE).setValue(SculkSensorBlock.POWER,0),3);
                if(block.is(Blocks.SCULK_SHRIEKER))level.setBlock(fixture,block.setValue(SculkShriekerBlock.SHRIEKING,false).setValue(SculkShriekerBlock.CAN_SUMMON,false),3);
            }
            return true;
        });
        while(!site.responses.isEmpty() && site.responses.peek().time<=now) {
            var response=site.responses.remove();
            if(!site.isFixture(response.pos) || !level.hasChunkAt(response.pos)) continue;
            var block=level.getBlockState(response.pos);
            if(response.kind==2 && block.is(Blocks.SCULK_SHRIEKER)) level.setBlock(response.pos,block.setValue(SculkShriekerBlock.SHRIEKING,false),3);
            else if(block.is(Blocks.SCULK_SENSOR)) {
                boolean active=response.kind==0;
                level.setBlock(response.pos,block.setValue(SculkSensorBlock.PHASE,active?SculkSensorPhase.ACTIVE:SculkSensorPhase.INACTIVE).setValue(SculkSensorBlock.POWER,active?2:0),3);
                if(active) {
                    AntecedentEvents.accepted(server,response.pos);
                    level.playSound(null,response.pos,SoundEvents.SCULK_CLICKING,SoundSource.BLOCKS,.3F,.65F);
                    site.responses.add(new Response(now+30,response.pos,1));
                    site.failShriek();
                    for(var player:server.players()) {
                        var local=site.local(player.blockPosition());
                        if(!player.isSpectator() && site.layout.gallery().isInside(local))
                            AntecedentKnowledge.record(player,DEGRADED_NETWORK);
                    }
                }
            }
        }
        if(site.growthBudget>0 && now%80==0 && site.isFixture(site.world(site.layout.catalyst()))) {
            // Unequal horizontal reach is intentional; never generate replacement sensors or shriekers.
            var spread=site.world(site.layout.bed().offset(server.random.nextInt(site.layout.bedWidth()),0,server.random.nextInt(site.layout.bedDepth())));
            if(level.getBlockState(spread).is(Blocks.DEEPSLATE)) level.setBlock(spread,Blocks.SCULK.defaultBlockState(),3);
            site.growthBudget--;site.setChanged();
        }
        if(now%5!=0) return;
        var present=new HashSet<UUID>();
        for(var player:server.players()) {
            if(player.distanceToSqr(pos.getCenter())>6400 || player.isSpectator()) continue;
            var local=site.local(player.blockPosition());
            if(!site.layout.contains(local)) continue;
            int depth=site.layout.passageDepth(local);
            present.add(player.getUUID());
            int previous=site.passage.getOrDefault(player.getUUID(),depth);
            site.passage.put(player.getUUID(),depth);
            if(previous<8 && depth>=8 && depth<=10 && HemoCapabilityAccess.antecedent(player).has(VICAR_RECOGNITION))
                com.vincenthuto.hemomancy.common.network.PacketHandler.sendToPlayer(player,new com.vincenthuto.hemomancy.common.network.AntecedentEffectPacket(7,pos));
            if(previous<=10 && depth>10) server.playSound(null,player.blockPosition(),SoundEvents.DEEPSLATE_STEP,SoundSource.BLOCKS,.65F,.75F);
            if(previous<14 && depth>=14 && depth<33) AntecedentKnowledge.record(player,CROSSED_WOOL_TERMINUS);
            if(previous<19 && depth>=19 && depth<33) {
                server.playSound(null,site.world(site.layout.firstClick()),SoundEvents.SCULK_CLICKING,SoundSource.AMBIENT,.2F,.8F);
                site.secondClicks.put(player.getUUID(),now+100+server.random.nextInt(61));
            }
            if(site.secondClicks.getOrDefault(player.getUUID(),Long.MAX_VALUE)<=now) {
                site.secondClicks.remove(player.getUUID());
                server.playSound(null,site.world(site.layout.secondClick()),SoundEvents.SCULK_CLICKING_STOP,SoundSource.AMBIENT,.2F,.7F);
            }
            if(site.layout.gallery().isInside(local) && local.getY()>=site.layout.balcony() && !HemoCapabilityAccess.antecedent(player).has(DEGRADED_NETWORK) && now-site.lastDemo>400) {
                site.lastDemo=now;
                var impact=site.world(site.layout.demonstration().atY(1)).getCenter();
                if(!site.demonstrationDropped) {
                    site.demonstrationDropped=true;site.setChanged();
                    var falling=net.minecraft.world.entity.item.FallingBlockEntity.fall(server,site.world(site.layout.demonstration()),Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState());
                    falling.dropItem=false;
                } else {
                    server.playSound(null,BlockPos.containing(impact),SoundEvents.DEEPSLATE_BREAK,SoundSource.BLOCKS,.25F,.6F);
                    site.disturbance(impact);
                }
            }
        }
        site.passage.keySet().retainAll(present);
        site.secondClicks.keySet().retainAll(present);
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries) {
        super.saveAdditional(tag,registries); tag.putInt("VigilLayout",layout.version()); tag.putLongArray("Retired",retired.stream().mapToLong(Long::longValue).toArray()); tag.putInt("GrowthBudget",growthBudget);tag.putBoolean("DemonstrationDropped",demonstrationDropped);
    }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries) {
        super.loadAdditional(tag,registries); layout=VigilLayout.forVersion(tag.getInt("VigilLayout")); retired.clear();for(long pos:tag.getLongArray("Retired"))retired.add(pos);growthBudget=Math.clamp(tag.getInt("GrowthBudget"),0,32);demonstrationDropped=tag.getBoolean("DemonstrationDropped");
    }
}
