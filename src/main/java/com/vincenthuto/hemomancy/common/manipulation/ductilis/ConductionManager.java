package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.BloodConstructEntity;
import com.vincenthuto.hemomancy.common.entity.summon.FerricConstructEntity;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.SchoolHitHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.network.ConductionPathPacket;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import java.util.*;

@EventBusSubscriber(modid=Hemomancy.MOD_ID)
public final class ConductionManager {
    public static final TagKey<Block> CONDUCTORS = TagKey.create(Registries.BLOCK,Hemomancy.rloc("ductilis_conductors"));
    public static final int ENERGIZED_TICKS=40;
    private static final int MAX_OWNER_NETWORKS=4, MAX_NETWORKS=64, PULSE_TICKS=10;
    private static final Map<ServerLevel,LinkedHashMap<UUID,Network>> NETWORKS=new WeakHashMap<>();
    private static final Map<FerricConstructEntity,Relay> RELAYS=new WeakHashMap<>();
    private static final Map<LivingEntity,Discharge> OWNER_PULSES=new WeakHashMap<>();
    private ConductionManager() { }

    public static boolean canHarm(LivingEntity owner, LivingEntity target) {
        if (target instanceof BloodConstructEntity || target==owner || !target.isAlive() || target.isSpectator()) return false;
        LivingEntity controller=controller(owner);
        if (controller instanceof Player player) return ManipulationCombatHelper.canHarm(player,target);
        if (target instanceof Player player && player.isCreative()) return false;
        if (owner.isAlliedTo(target) || target.isAlliedTo(owner)) return false;
        return !(owner instanceof net.minecraft.world.entity.Mob mob) || mob.getTarget()==target || mob.canAttack(target);
    }

    private static Discharge ownerLedger(LivingEntity owner) {
        return OWNER_PULSES.computeIfAbsent(owner,id->new Discharge());
    }

    public static boolean canClaimHit(LivingEntity owner,LivingEntity victim,Discharge discharge) {
        long now=owner.level().getGameTime();
        return discharge.canClaim(victim.getUUID(),now) && ownerLedger(owner).canClaim(victim.getUUID(),now);
    }

    public static boolean claimHit(LivingEntity owner,LivingEntity victim,Discharge discharge) {
        if (!canClaimHit(owner,victim,discharge)) return false;
        long now=owner.level().getGameTime();
        return ownerLedger(owner).claim(victim.getUUID(),now) && discharge.claim(victim.getUUID(),now);
    }

    public static boolean isConductor(Level level,BlockPos pos) {
        return level.hasChunkAt(pos) && !level.isOutsideBuildHeight(pos)
                && (level.getFluidState(pos).is(FluidTags.WATER) || level.getBlockState(pos).is(CONDUCTORS));
    }

    public static List<BlockPos> graph(ServerLevel level,BlockPos seed) {
        return ConductorGraph.collect(seed,p -> level.hasChunkAt(p) && !level.isOutsideBuildHeight(p),
                p -> level.getFluidState(p).is(FluidTags.WATER) || level.getBlockState(p).is(CONDUCTORS));
    }

    public static boolean energizeAt(LivingEntity owner,BlockPos seed,Discharge discharge) {
        if (!(owner.level() instanceof ServerLevel level)) return false;
        List<BlockPos> nodes=graph(level,seed);
        if (nodes.isEmpty()) return false;
        var networks=NETWORKS.computeIfAbsent(level,l -> new LinkedHashMap<>());
        long now=level.getGameTime();
        for (Network network:networks.values()) {
            if (!network.owner.equals(owner.getUUID())) continue;
            network.nodes=graph(level,network.seed);
            if (network.nodes.contains(seed)) {
                network.expiresAt=now+ENERGIZED_TICKS;
                network.discharge=discharge;
                // Existing seed remains authoritative so a broken bridge never joins disconnected remnants.
                sync(level,network); return true;
            }
        }
        while (networks.values().stream().filter(n -> n.owner.equals(owner.getUUID())).count()>=MAX_OWNER_NETWORKS) {
            Network oldest=networks.values().stream().filter(n -> n.owner.equals(owner.getUUID())).findFirst().orElseThrow();
            remove(level,oldest); networks.remove(oldest.id);
        }
        if (NETWORKS.values().stream().mapToInt(Map::size).sum()>=MAX_NETWORKS) return false;
        Network network=new Network(owner.getUUID(),seed.immutable(),nodes,now+ENERGIZED_TICKS,now+PULSE_TICKS,discharge);
        networks.put(network.id,network); sync(level,network); return true;
    }

    public static void energizeTouching(LivingEntity owner,LivingEntity target,Discharge discharge) {
        energizeBounds(owner,target.getBoundingBox().inflate(0.08),discharge);
        for (FerricConstructEntity relay:owner.level().getEntitiesOfClass(FerricConstructEntity.class,
                target.getBoundingBox().inflate(1.5),r -> usableRelay(owner,r)
                        && owner.level() instanceof ServerLevel level
                        && visible(level,target.getEyePosition(),r.getEyePosition(),owner))) energizeRelay(owner,relay,discharge);
    }

    public static void energizeBounds(LivingEntity owner,AABB bounds,Discharge discharge) {
        energizeBounds(owner,bounds,discharge,false);
    }

    public static void energizeVisibleBounds(LivingEntity owner,AABB bounds,Discharge discharge) {
        energizeBounds(owner,bounds,discharge,true);
        if (!(owner.level() instanceof ServerLevel level)) return;
        for (FerricConstructEntity relay:level.getEntitiesOfClass(FerricConstructEntity.class,bounds,
                r->usableRelay(owner,r) && visible(level,owner.getEyePosition(),r.getEyePosition(),owner))) {
            energizeRelay(owner,relay,discharge);
        }
    }

    private static void energizeBounds(LivingEntity owner,AABB bounds,Discharge discharge,boolean requireVisible) {
        int seeds=0;
        for (BlockPos pos:BlockPos.betweenClosed(BlockPos.containing(bounds.minX,bounds.minY,bounds.minZ),
                BlockPos.containing(bounds.maxX,bounds.maxY,bounds.maxZ))) {
            if (touchesConductor(owner.level(),pos,bounds) && (!requireVisible || visibleSurface(owner,pos))
                    && energizeAt(owner,pos,discharge) && ++seeds>=16) break;
        }
    }

    private static boolean visibleSurface(LivingEntity owner,BlockPos pos) {
        Vec3 surface=surfacePoint(owner.level(),pos,owner.getEyePosition());
        var hit=owner.level().clip(new ClipContext(owner.getEyePosition(),surface,
                ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,owner));
        return hit.getType()==HitResult.Type.MISS || hit.getBlockPos().equals(pos);
    }

    public static boolean usableRelay(LivingEntity caster,FerricConstructEntity relay) {
        LivingEntity owner=relay.getCreator();
        return relay.isAlive() && owner!=null && (owner==caster || owner.isAlliedTo(caster)
                || caster.isAlliedTo(owner) || caster instanceof BloodConstructEntity construct && construct.getCreator()==owner);
    }

    public static boolean energized(FerricConstructEntity relay) {
        return relay.isAlive() && relay.energizedUntil()>relay.level().getGameTime();
    }

    public static void energizeRelay(LivingEntity owner,FerricConstructEntity relay,Discharge discharge) {
        if (!(owner.level() instanceof ServerLevel level) || relay.level()!=level || !usableRelay(owner,relay)) return;
        updateRelay(owner,relay,discharge,level.getGameTime()+ENERGIZED_TICKS);
    }

    private static void updateRelay(LivingEntity owner,FerricConstructEntity relay,Discharge discharge,long until) {
        long deadline=Math.min(relay.expiresAt(),until);
        if (deadline<relay.energizedUntil()) return;
        relay.energize(deadline);
        RELAYS.put(relay,new Relay(owner.getUUID(),discharge));
    }

    public static void tickFerric(FerricConstructEntity relay) {
        if (!(relay.level() instanceof ServerLevel level) || !energized(relay)) { RELAYS.remove(relay); return; }
        if (level.getGameTime()%PULSE_TICKS!=0) return;
        Relay state=RELAYS.get(relay);
        if (state==null) return;
        LivingEntity owner=resolveOwner(level,state.owner);
        if (owner==null || !owner.isAlive()) return;
        state.discharge.beginPulse(level.getGameTime());
        int hits=0;
        boolean pillar=relay instanceof com.vincenthuto.hemomancy.common.entity.summon.EntityIronPillar;
        AABB contact=relay.getBoundingBox().inflate(pillar ? 5 : .08);
        for (LivingEntity victim:level.getEntitiesOfClass(LivingEntity.class,contact,
                e -> canHarm(owner,e) && (!pillar || e.distanceToSqr(relay)<=25)
                        && visible(level,relay.getEyePosition(),e.getEyePosition(),owner))
                .stream().sorted(Comparator.comparingDouble((LivingEntity e)->e.distanceToSqr(relay)).thenComparing(LivingEntity::getUUID)).toList()) {
            if (pulseHit(owner,victim,state.discharge)) {
                DuctilisLightningEffects.conductiveArc(relay,victim,hits++);
                if (hits>=3) break;
            }
        }
    }

    public static boolean conductive(LivingEntity entity) {
        if (entity.hasEffect(EffectInit.conductive_mark) || entity.hasEffect(EffectInit.lodestone)) return true;
        if (entity instanceof FerricConstructEntity ferric && energized(ferric)) return true;
        AABB bounds=entity.getBoundingBox().inflate(0.08);
        for (BlockPos pos:BlockPos.betweenClosed(BlockPos.containing(bounds.minX,bounds.minY,bounds.minZ),
                BlockPos.containing(bounds.maxX,bounds.maxY,bounds.maxZ))) {
            if (touchesConductor(entity.level(),pos,bounds)) return true;
        }
        return false;
    }

    public static boolean visible(ServerLevel level,Vec3 start,Vec3 end,LivingEntity owner) {
        return level.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,owner)).getType()==HitResult.Type.MISS;
    }

    /** Cast ray stops at geometry; water participates even though it has no collision box. */
    public static boolean energizeAimed(LivingEntity owner,double range,Discharge discharge) {
        if (!(owner.level() instanceof ServerLevel level)) return false;
        Vec3 start=owner.getEyePosition(),end=start.add(owner.getLookAngle().scale(range));
        var hit=level.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.ANY,owner));
        Vec3 clipped=hit.getLocation();
        var relay=level.getEntitiesOfClass(FerricConstructEntity.class,owner.getBoundingBox().expandTowards(clipped.subtract(start)).inflate(1),
                r -> usableRelay(owner,r) && r.getBoundingBox().inflate(0.25).clip(start,clipped).isPresent())
                .stream().min(Comparator.comparingDouble(owner::distanceToSqr)).orElse(null);
        if (relay!=null) {
            energizeRelay(owner,relay,discharge); DuctilisLightningEffects.synapticJolt(owner,relay); return true;
        }
        if (hit.getType()!=HitResult.Type.MISS && energizeAt(owner,hit.getBlockPos(),discharge)) {
            DuctilisLightningEffects.conductorStrike(owner,hit.getLocation()); return true;
        }
        return false;
    }

    private static boolean pulseHit(LivingEntity owner,LivingEntity victim,Discharge discharge) {
        ServerLevel level=(ServerLevel)owner.level();
        if (!canHarm(owner,victim) || !claimHit(owner,victim,discharge)) return false;
        var hit = discharge.pulseContext(owner);
        try (var scope = com.vincenthuto.hemomancy.common.damage.SchoolDamage.scope(hit, owner)) {
            if (!victim.hurt(com.vincenthuto.hemomancy.common.damage.SchoolDamage.attributed(
                    level.damageSources().indirectMagic(owner, owner), hit, owner), 2)) return false;
            SchoolHitHelper.tryTriggerConductiveArc(owner, victim, EnumBloodTendency.DUCTILIS, null, 2, discharge);
            return true;
        }
    }

    @SubscribeEvent public static void onTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        var networks=NETWORKS.get(level); if (networks==null) return;
        long now=level.getGameTime();
        for (var iterator=networks.values().iterator();iterator.hasNext();) {
            Network network=iterator.next(); LivingEntity owner=resolveOwner(level,network.owner);
            if (now>=network.expiresAt || owner==null || !owner.isAlive() || !isConductor(level,network.seed)) {
                remove(level,network); iterator.remove(); continue;
            }
            if (now<network.nextPulse) continue;
            network.nextPulse=now+PULSE_TICKS;
            network.discharge.beginPulse(now);
            network.nodes=graph(level,network.seed);
            sync(level,network);
            int hits=0;
            for (LivingEntity target:level.getEntitiesOfClass(LivingEntity.class,new AABB(network.seed).inflate(13),
                    e -> canHarm(owner,e) && touches(e,network.nodes))
                    .stream().sorted(Comparator.comparingDouble((LivingEntity e)->e.distanceToSqr(Vec3.atCenterOf(network.seed)))
                            .thenComparing(LivingEntity::getUUID)).toList()) {
                if (pulseHit(owner,target,network.discharge)) {
                    BlockPos closest=network.nodes.stream()
                            .filter(p->touchesConductor(level,p,target.getBoundingBox().inflate(.08)))
                            .min(Comparator.comparingDouble(p->target.distanceToSqr(Vec3.atCenterOf(p)))).orElse(network.seed);
                    DuctilisLightningEffects.conductorDischarge(owner,surfacePoint(level,closest,target.getEyePosition()),target);
                    if (++hits>=3) break;
                }
            }
            for (FerricConstructEntity relay:level.getEntitiesOfClass(FerricConstructEntity.class,new AABB(network.seed).inflate(13),
                    r -> usableRelay(owner,r) && touches(r,network.nodes))) {
                updateRelay(owner,relay,network.discharge,network.expiresAt);
            }
        }
    }

    private static boolean touches(LivingEntity entity,List<BlockPos> nodes) {
        AABB box=entity.getBoundingBox().inflate(0.08);
        return nodes.stream().anyMatch(p -> touchesConductor(entity.level(),p,box));
    }

    private static boolean touchesConductor(Level level,BlockPos pos,AABB body) {
        if (!isConductor(level,pos)) return false;
        var fluid=level.getFluidState(pos);
        if (fluid.is(FluidTags.WATER) && body.intersects(new AABB(pos.getX(),pos.getY(),pos.getZ(),
                pos.getX()+1,pos.getY()+fluid.getHeight(level,pos),pos.getZ()+1))) return true;
        var state=level.getBlockState(pos);
        if (!state.is(CONDUCTORS)) return false;
        var shape=state.getCollisionShape(level,pos);
        // Rails and pressure plates have no collision but still have a thin physical surface.
        if (shape.isEmpty()) shape=state.getShape(level,pos);
        return shape.toAabbs().stream().anyMatch(box->body.intersects(box.move(pos)));
    }

    private static Vec3 surfacePoint(Level level,BlockPos pos,Vec3 target) {
        var fluid=level.getFluidState(pos);
        if (fluid.is(FluidTags.WATER)) {
            return new Vec3(Math.clamp(target.x,pos.getX(),pos.getX()+1),pos.getY()+fluid.getHeight(level,pos),
                    Math.clamp(target.z,pos.getZ(),pos.getZ()+1));
        }
        var state=level.getBlockState(pos);
        var shape=state.getCollisionShape(level,pos);
        if (shape.isEmpty()) shape=state.getShape(level,pos);
        return shape.toAabbs().stream().map(box->box.move(pos)).map(box->new Vec3(
                        Math.clamp(target.x,box.minX,box.maxX),Math.clamp(target.y,box.minY,box.maxY),
                        Math.clamp(target.z,box.minZ,box.maxZ)))
                .min(Comparator.comparingDouble(target::distanceToSqr)).orElse(Vec3.atCenterOf(pos));
    }

    private static void sync(ServerLevel level,Network network) {
        var packet=new ConductionPathPacket(network.id,network.nodes,network.expiresAt);
        Set<UUID> viewers=new HashSet<>();
        for (ServerPlayer player:level.players()) if (player.distanceToSqr(Vec3.atCenterOf(network.seed))<=80*80) {
            PacketHandler.sendToPlayer(player,packet); viewers.add(player.getUUID());
        }
        for (UUID id:network.viewers) if (!viewers.contains(id)) {
            ServerPlayer player=level.getServer().getPlayerList().getPlayer(id);
            if (player!=null) PacketHandler.sendToPlayer(player,new ConductionPathPacket(network.id,List.of(),0));
        }
        network.viewers=viewers;
    }

    private static void remove(ServerLevel level,Network network) {
        for (UUID id:network.viewers) {
            ServerPlayer player=level.getServer().getPlayerList().getPlayer(id);
            if (player!=null) PacketHandler.sendToPlayer(player,new ConductionPathPacket(network.id,List.of(),0));
        }
    }

    public static void clearOwner(UUID owner) {
        NETWORKS.forEach((level,networks)->networks.values().removeIf(n->{
            if (!n.owner.equals(owner)) return false; remove(level,n); return true;
        }));
        RELAYS.entrySet().removeIf(e -> {
            if (!e.getValue().owner.equals(owner)) return false; e.getKey().energize(0); return true;
        });
        OWNER_PULSES.keySet().removeIf(e->e.getUUID().equals(owner));
    }
    @SubscribeEvent public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) { clearOwner(e.getEntity().getUUID()); }
    @SubscribeEvent public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent e) { clearOwner(e.getEntity().getUUID()); }
    @SubscribeEvent public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) { clearOwner(e.getEntity().getUUID()); }
    @SubscribeEvent public static void onDeath(LivingDeathEvent e) {
        clearOwner(e.getEntity().getUUID());
    }
    @SubscribeEvent public static void onUnload(LevelEvent.Unload e) {
        NETWORKS.remove(e.getLevel()); OWNER_PULSES.keySet().removeIf(owner->owner.level()==e.getLevel());
        RELAYS.keySet().removeIf(relay->relay.level()==e.getLevel());
    }
    @SubscribeEvent public static void onStop(ServerStoppedEvent e) { NETWORKS.clear(); RELAYS.clear(); OWNER_PULSES.clear(); }

    private static LivingEntity resolveOwner(ServerLevel level,UUID id) {
        return level.getEntity(id) instanceof LivingEntity living ? living : null;
    }

    private static LivingEntity controller(LivingEntity entity) {
        if (entity instanceof BloodConstructEntity construct && construct.getCreator()!=null) return construct.getCreator();
        UUID id=entity instanceof net.minecraft.world.entity.OwnableEntity ownable ? ownable.getOwnerUUID()
                : entity instanceof com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon bound
                ? bound.hemomancy$getOwnerUUID() : null;
        if (id!=null && entity.level() instanceof ServerLevel level && resolveOwner(level,id) instanceof Player player) return player;
        return entity;
    }

    private record Relay(UUID owner,Discharge discharge) { }
    private static final class Network {
        final UUID id=UUID.randomUUID(),owner;
        final BlockPos seed;
        List<BlockPos> nodes;
        long expiresAt,nextPulse;
        Discharge discharge;
        Set<UUID> viewers=new HashSet<>();
        Network(UUID owner,BlockPos seed,List<BlockPos> nodes,long expiresAt,long nextPulse,Discharge discharge) {
            this.owner=owner; this.seed=seed; this.nodes=nodes; this.expiresAt=expiresAt;
            this.nextPulse=nextPulse; this.discharge=discharge;
        }
    }
}
