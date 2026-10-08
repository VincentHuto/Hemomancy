package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationRankGates;
import com.vincenthuto.hemomancy.common.network.axonal.AxonalStatePacket;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import com.vincenthuto.hutoslib.common.lightning.LightningTesterSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class AxonalTransductionManager {
    public static final String ACTIVE = "hemomancy_axonal_active";
    private static final String RETURN = "hemomancy_axonal_return";
    private static final String EXIT = "hemomancy_axonal_exit";
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static final Map<UUID, BlockPos> EXIT_CONTACTS = new HashMap<>();
    private static long nextToken;
    private static final ParticleColor GOLD = new ParticleColor(255, 232, 90);
    private static final LightningTestConfig ARC = new LightningTestConfig(
            LightningTestConfig.Backend.BOLT, 0xD0FFE84A, 0xD0FFE84A, 0xFFFFFFFF,
            32, 0, 0, 0, 42, 1.4F, 3, 3, 0.035F, 0.006F, false, 0L, false, 6);

    private AxonalTransductionManager() { }
    public static boolean isTraveling(Player player) { return player.getPersistentData().getBoolean(ACTIVE); }
    public static void applyMovement(Player player) {
        if (!isTraveling(player)) return;
        player.noPhysics = true;
        player.setNoGravity(true);
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
    }
    public static AxonalTravelPath.Network network(ServerLevel level) {
        return new AxonalTravelPath.Network() {
            public boolean contains(BlockPos pos) {
                if (!level.hasChunkAt(pos)) return false;
                var state = level.getBlockState(pos);
                return state.is(BlockInit.nerve_fiber.get()) || state.is(BlockInit.nerve_bundle.get())
                        || state.is(BlockInit.synaptic_node.get());
            }
            public boolean node(BlockPos pos) {
                return level.hasChunkAt(pos) && level.getBlockState(pos).is(BlockInit.synaptic_node.get());
            }
        };
    }
    private static boolean eligible(ServerPlayer player) {
        var manip = ManipulationInit.axonal_transduction.get();
        int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
        return manip.isPassiveReady(player) && ManipulationRankGates.playerMeetsRank(degree, manip.getRank())
                && !player.isSpectator() && !player.isPassenger() && !player.isVehicle()
                && !Paralysis.isParalyzed(player);
    }
    public static boolean canEnter(ServerPlayer player) {
        BlockPos node = touchingNode(player);
        return !isTraveling(player) && eligible(player) && !player.isShiftKeyDown() && node != null
                && !EXIT_CONTACTS.containsKey(player.getUUID())
                && player.containerMenu == player.inventoryMenu && player.inventoryMenu.getCarried().isEmpty()
                && !AxonalTravelPath.neighbors(network(player.serverLevel()), node).isEmpty()
                && safeLanding(player, player.serverLevel(), node) != null;
    }
    private static BlockPos touchingNode(ServerPlayer player) {
        AABB bounds = player.getBoundingBox().inflate(0.06);
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ),
                BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ))) {
            if (player.serverLevel().hasChunkAt(pos) && player.serverLevel().getBlockState(pos).is(BlockInit.synaptic_node.get())
                    && bounds.intersects(new AABB(pos))) return pos.immutable();
        }
        return null;
    }
    public static void begin(ServerPlayer player) {
        if (!canEnter(player)) return;
        BlockPos source = touchingNode(player);
        Vec3 home = safeLanding(player, player.serverLevel(), source);
        Session session = new Session(player.serverLevel(), source, ++nextToken, player.isNoGravity());
        SESSIONS.put(player.getUUID(), session);
        CompoundTag recovery = new CompoundTag();
        recovery.putString("dimension", player.level().dimension().location().toString());
        recovery.putDouble("x", home.x); recovery.putDouble("y", home.y); recovery.putDouble("z", home.z);
        recovery.putBoolean("noGravity", session.noGravityBefore);
        recovery.putLong("node", source.asLong());
        player.getPersistentData().put(RETURN, recovery);
        player.getPersistentData().putBoolean(ACTIVE, true);
        for (Mob mob : player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(64)))
            if (mob.getTarget() == player) mob.setTarget(null);
        ManipulationChannelManager.stop(player, false);
        player.stopUsingItem();
        Vec3 signal = session.path.position().subtract(0, player.getEyeHeight(), 0);
        // Supersede any pending physical-position teleport before accepting signal input.
        player.teleportTo(signal.x, signal.y, signal.z);
        session.expected = player.position();
        settledPosition(player);
        applyMovement(player);
        transition(player);
        sync(player, session.token, true);
    }
    public static void input(ServerPlayer player, long token, int direction, boolean precise) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null || session.token != token) return;
        session.input = Math.clamp(direction, -1, 1);
        session.precise = precise;
        session.lastInputTick = player.level().getGameTime();
    }
    @SubscribeEvent public static void beforeTick(PlayerTickEvent.Pre event) { applyMovement(event.getEntity()); }
    @SubscribeEvent public static void afterTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) tick(player);
    }
    public static void tick(ServerPlayer player) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null) {
            BlockPos contact = touchingNode(player);
            if (EXIT_CONTACTS.containsKey(player.getUUID())) {
                BlockPos exit = EXIT_CONTACTS.get(player.getUUID());
                if (player.getBoundingBox().inflate(0.1).intersects(new AABB(exit))) return;
                clearExitContact(player);
            }
            if (contact != null && canEnter(player)) ManipulationInit.axonal_transduction.get().tryPerformPassiveTrigger(player);
            return;
        }
        if (!player.isAlive()) { end(player, false); return; }
        if (player.serverLevel() != session.level || player.position().distanceToSqr(session.expected) > 0.04) {
            end(player, false); return;
        }
        if (!eligible(player)) { end(player, true); return; }
        var graph = network(session.level);
        int direction = player.level().getGameTime() - session.lastInputTick > 4 ? 0 : session.input;
        var result = session.path.advance(graph, session.precise ? AxonalTravelPath.PRECISE_SPEED : AxonalTravelPath.SPEED,
                direction, player.getLookAngle());
        if (result == AxonalTravelPath.Result.LOST) { end(player, true); return; }
        if (result == AxonalTravelPath.Result.NODE) {
            Vec3 landing = safeLanding(player, session.level, session.path.anchor());
            if (landing != null) {
                setExitContact(player, session.path.anchor());
                player.teleportTo(landing.x, landing.y, landing.z);
                end(player, false);
                return;
            }
        }
        Vec3 old = session.path.position();
        player.setPos(old.subtract(0, player.getEyeHeight(), 0));
        session.expected = player.position();
        settledPosition(player);
        applyMovement(player);
        sync(player, session.token, true);
        if (player.tickCount % 4 == 0) session.level.sendParticles(HemoParticleData.glow(GOLD),
                old.x, old.y, old.z, 2, 0.04, 0.04, 0.04, 0);
        if (result == AxonalTravelPath.Result.MOVING && player.tickCount % 6 == 0) {
            LightningTesterSpawner.spawn(session.level, old, old.subtract(player.getLookAngle().scale(0.55)), ARC);
        }
    }
    public static Vec3 safeLanding(ServerPlayer player, ServerLevel level, BlockPos node) {
        // Prefer the familiar standing space above a node; then its immediate surrounding cells.
        Vec3 above = Vec3.atBottomCenterOf(node.above());
        if (safe(player, level, above)) return above;
        for (BlockPos pos : BlockPos.betweenClosed(node.offset(-2, -1, -2), node.offset(2, 2, 2))) {
            Vec3 candidate = Vec3.atBottomCenterOf(pos);
            if (level.hasChunkAt(pos.below()) && !level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty()
                    && safe(player, level, candidate)) return candidate;
        }
        return null;
    }
    private static boolean safe(ServerPlayer player, ServerLevel level, Vec3 feet) {
        AABB box = new AABB(feet.x - 0.3, feet.y, feet.z - 0.3, feet.x + 0.3, feet.y + 1.8, feet.z + 0.3);
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ),
                BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
            if (!level.hasChunkAt(pos) || !level.getFluidState(pos).isEmpty()) return false;
        }
        return level.getWorldBorder().isWithinBounds(box) && level.noCollision(player, box);
    }
    public static void end(ServerPlayer player, boolean recover) {
        Session session = SESSIONS.remove(player.getUUID());
        CompoundTag recovery = player.getPersistentData().getCompound(RETURN);
        if (session == null && recovery.isEmpty() && !isTraveling(player)) return;
        player.getPersistentData().remove(ACTIVE);
        if (recover && player.isAlive() && !recovery.isEmpty()) {
            ResourceLocation dimension = ResourceLocation.tryParse(recovery.getString("dimension"));
            ServerLevel homeLevel = dimension == null ? null : player.server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
            Vec3 landing = null;
            if (homeLevel != null) {
                Vec3 home = new Vec3(recovery.getDouble("x"), recovery.getDouble("y"), recovery.getDouble("z"));
                // Recovery may load the saved entry chunk, never the fiber network ahead.
                homeLevel.getChunkAt(BlockPos.containing(home));
                landing = safe(player, homeLevel, home) ? home : safeLanding(player, homeLevel, BlockPos.containing(home).below());
            }
            if (landing != null) {
                player.teleportTo(homeLevel, landing.x, landing.y, landing.z,
                        java.util.Set.of(), player.getYRot(), player.getXRot());
                setExitContact(player, BlockPos.of(recovery.getLong("node")));
            } else {
                ServerLevel spawnLevel = player.server.overworld();
                BlockPos spawn = spawnLevel.getSharedSpawnPos();
                spawnLevel.getChunkAt(spawn);
                Vec3 fallback = safeLanding(player, spawnLevel, spawn);
                if (fallback == null) {
                    BlockPos surface = spawnLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn);
                    // Air above the terrain also handles obstructed or submerged world spawns.
                    for (int y = surface.getY(); y <= spawnLevel.getMaxBuildHeight(); y++) {
                        Vec3 candidate = Vec3.atBottomCenterOf(new BlockPos(spawn.getX(), y, spawn.getZ()));
                        if (safe(player, spawnLevel, candidate)) { fallback = candidate; break; }
                    }
                }
                if (fallback != null) player.teleportTo(spawnLevel, fallback.x, fallback.y, fallback.z,
                        java.util.Set.of(), player.getYRot(), player.getXRot());
            }
        }
        player.getPersistentData().remove(RETURN);
        player.noPhysics = player.isSpectator();
        player.setNoGravity(session != null ? session.noGravityBefore : recovery.getBoolean("noGravity"));
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        settledPosition(player);
        if (session != null || !recovery.isEmpty()) { sync(player, session == null ? 0 : session.token, false); transition(player); }
    }
    private static void settledPosition(ServerPlayer player) {
        if (player.connection == null) return;
        // The vanilla connection tick restores firstGood after PlayerTick.Post.
        player.connection.resetPosition();
        player.serverLevel().getChunkSource().move(player);
    }
    private static void transition(ServerPlayer player) {
        Vec3 center = player.getEyePosition();
        player.serverLevel().sendParticles(HemoParticleData.glow(GOLD), center.x, center.y, center.z, 10, 0.15, 0.2, 0.15, 0);
        player.serverLevel().playSound(null, player.blockPosition(), SoundInit.ITEM_SYNAPTIC_STEP_USE.get(),
                net.minecraft.sounds.SoundSource.PLAYERS, 0.65F, 1.4F);
    }
    private static void setExitContact(ServerPlayer player, BlockPos node) {
        EXIT_CONTACTS.put(player.getUUID(), node.immutable());
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", player.level().dimension().location().toString());
        tag.putLong("node", node.asLong());
        player.getPersistentData().put(EXIT, tag);
    }
    private static void clearExitContact(ServerPlayer player) {
        EXIT_CONTACTS.remove(player.getUUID());
        player.getPersistentData().remove(EXIT);
    }
    private static void sync(ServerPlayer player, long token, boolean active) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new AxonalStatePacket(
                player.level().dimension().location(), player.getId(), token, active, player.position()));
    }
    @SubscribeEvent public static void incomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player p && isTraveling(p) && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY))
            event.setCanceled(true);
    }
    @SubscribeEvent public static void changeTarget(LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() instanceof Player p && isTraveling(p)) event.setNewAboutToBeSetTarget(null);
    }
    @SubscribeEvent public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            CompoundTag exit = p.getPersistentData().getCompound(EXIT);
            if (exit.getString("dimension").equals(p.level().dimension().location().toString()))
                EXIT_CONTACTS.put(p.getUUID(), BlockPos.of(exit.getLong("node")));
            else clearExitContact(p);
            if (p.getPersistentData().contains(RETURN)) end(p, true);
        }
    }
    @SubscribeEvent public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) { if (isTraveling(p)) end(p, true); EXIT_CONTACTS.remove(p.getUUID()); }
    }
    @SubscribeEvent public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            if (isTraveling(p)) end(p, false);
            clearExitContact(p);
        }
    }
    @SubscribeEvent public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            if (isTraveling(p)) end(p, false);
            clearExitContact(p);
        }
    }
    @SubscribeEvent public static void onTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer observer && event.getTarget() instanceof ServerPlayer traveler) {
            Session session = SESSIONS.get(traveler.getUUID());
            if (session != null) PacketDistributor.sendToPlayer(observer, new AxonalStatePacket(
                    traveler.level().dimension().location(), traveler.getId(), session.token, true, traveler.position()));
        }
    }
    @SubscribeEvent public static void onStopped(ServerStoppedEvent event) { SESSIONS.clear(); EXIT_CONTACTS.clear(); }
    private static final class Session {
        final ServerLevel level;
        final AxonalTravelPath path;
        final long token;
        final boolean noGravityBefore;
        Vec3 expected;
        int input;
        boolean precise;
        long lastInputTick;
        Session(ServerLevel level, BlockPos source, long token, boolean noGravityBefore) {
            this.level = level; path = new AxonalTravelPath(source); this.token = token; this.noGravityBefore = noGravityBefore;
        }
    }
}
