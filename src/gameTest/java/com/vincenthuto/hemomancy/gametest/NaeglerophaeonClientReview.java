package com.vincenthuto.hemomancy.gametest;

import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.encounter.VagrantMindEncounterData;
import com.vincenthuto.hemomancy.common.worldgen.structure.vagrantmind.VagrantMindPiece;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Explicit opt-in fixture. Every mutation is restricted to its disposable review directory. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class NaeglerophaeonClientReview {
    private static volatile UUID bossId;
    private static ArmorStand camera;
    private static double angle = .65, distance = 22;
    private static boolean firstPerson;
    private static boolean testGrip;
    private static int gripHits;
    private static long lastGripHit;
    private static volatile VagrantMindPiece dormantPiece;
    private static int miningTicks;
    private static volatile int captureCountdown;
    private static String captureLabel;
    private static final Set<Integer> captured = new HashSet<>();

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.naeglerophaeonReview")) return;
        var mc = Minecraft.getInstance();
        var root = mc.gameDirectory.toPath().toAbsolutePath().normalize();
        if (!root.endsWith("naeglerophaeon-client") || mc.player == null || mc.getSingleplayerServer() == null) return;
        try {
            if (captureCountdown > 0 && --captureCountdown == 0)
                Screenshot.grab(mc.gameDirectory, "ganglion-" + captureLabel + ".png", mc.getMainRenderTarget(), message -> {});
            if (miningTicks > 0 && mc.screen == null && dormantPiece != null) {
                BlockPos knot = dormantPiece.awakeningPosition();
                if (!mc.level.getBlockState(knot).is(BlockInit.dormant_ganglion.get())) {
                    miningTicks = 0;
                    captureLabel = "client-mine";
                    captureCountdown = 20;
                    Hemomancy.LOGGER.info("GANGLION_REVIEW client mining completed through normal destroy packets");
                } else if (++miningTicks > 600) {
                    miningTicks = 0;
                    Hemomancy.LOGGER.error("GANGLION_REVIEW client mining timed out");
                } else {
                    mc.gameMode.continueDestroyBlock(knot, net.minecraft.core.Direction.NORTH);
                    mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                }
            }
            NaeglerophaeonEntity boss = null;
            for (var entity : mc.level.entitiesForRendering())
                if (entity.getUUID().equals(bossId) && entity instanceof NaeglerophaeonEntity found) boss = found;
            if (boss != null && !firstPerson) {
                if (camera == null || camera.level() != mc.level) camera = new ArmorStand(mc.level, 0, 0, 0);
                Vec3 target = boss.position().add(0, .85, 0);
                Vec3 at = target.add(Math.sin(angle) * distance, distance * .32, Math.cos(angle) * distance);
                Vec3 delta = target.subtract(at);
                camera.setPos(at.add(0, -camera.getEyeHeight(), 0));
                camera.setYRot((float) Math.toDegrees(Math.atan2(-delta.x, delta.z)));
                camera.setYHeadRot(camera.getYRot());
                camera.yHeadRotO = camera.getYRot();
                camera.setXRot((float) -Math.toDegrees(Math.atan2(delta.y, delta.horizontalDistance())));
                camera.setOldPosAndRot();
                mc.setCameraEntity(camera);
            }
            if (boss != null && mc.screen == null && mc.level.getGameTime() - boss.animationStart()
                    >= (boss.animationPhase() == NaeglerophaeonEntity.GRAB ? 12 : 3) && captured.add(boss.animationPhase())) {
                Screenshot.grab(mc.gameDirectory, "membrane-phase-" + boss.animationPhase() + ".png", mc.getMainRenderTarget(), message -> {});
                Hemomancy.LOGGER.info("MEMBRANE_REVIEW client phase={} victim={} anchor={} previous={}",
                        boss.animationPhase(), boss.grabbedEntityId(), boss.position(), boss.captureStart());
            }
            if(testGrip && boss!=null && boss.isGrabbing() && mc.level.getGameTime()-boss.animationStart()>15) {
                firstPerson=true;
                mc.setCameraEntity(mc.player);
                mc.player.setXRot(57.6F);
                if(mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit
                        && hit.getEntity() instanceof net.neoforged.neoforge.entity.PartEntity<?> part
                        && part.getParent()==boss && mc.level.getGameTime()-lastGripHit>=16) {
                    mc.gameMode.attack(mc.player,part);
                    mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                    lastGripHit=mc.level.getGameTime(); gripHits++;
                    Hemomancy.LOGGER.info("AXON_GRIP_NETWORK_HIT count={} health={}",gripHits,boss.getHealth());
                }
            } else if(testGrip && gripHits>0 && boss!=null && !boss.isGrabbing()) {
                Hemomancy.LOGGER.info("AXON_GRIP_NETWORK_RELEASE hits={} health={}",gripHits,boss.getHealth());
                testGrip=false;
            }
            var request = root.resolve("membrane-review.json");
            if (!Files.exists(request)) return;
            var data = JsonParser.parseString(Files.readString(request)).getAsJsonObject();
            Files.delete(request);
            String op = data.get("op").getAsString();
            if (op.equals("mine") && dormantPiece != null) {
                BlockPos knot = dormantPiece.awakeningPosition();
                Hemomancy.LOGGER.info("GANGLION_REVIEW client mining progress/tick={} grounded={}",
                        mc.level.getBlockState(knot).getDestroyProgress(mc.player, mc.level, knot), mc.player.onGround());
                mc.gameMode.startDestroyBlock(knot, net.minecraft.core.Direction.NORTH);
                miningTicks = 1;
                return;
            }
            if (op.equals("dormant") || op.equals("awaken") || op.equals("retreat") || op.equals("return")) {
                firstPerson = true;
                mc.setCameraEntity(mc.player);
                mc.options.hideGui = false;
                mc.options.pauseOnLostFocus = false;
                UUID playerId = mc.player.getUUID();
                mc.getSingleplayerServer().execute(() -> {
                    var player = mc.getSingleplayerServer().getPlayerList().getPlayer(playerId);
                    try {
                        if (op.equals("dormant")) setupDormant(player);
                        else if (dormantPiece != null) {
                            BlockPos knot = dormantPiece.awakeningPosition();
                            if (op.equals("awaken")) {
                                player.gameMode.destroyBlock(knot);
                                bossId = VagrantMindEncounterData.get(player.serverLevel()).encounter(dormantPiece.origin()).entity();
                            } else if (op.equals("retreat")) {
                                BlockPos far = dormantPiece.origin().offset(400, 0, 0);
                                player.serverLevel().setBlockAndUpdate(far.below(), net.minecraft.world.level.block.Blocks.BARRIER.defaultBlockState());
                                player.teleportTo(player.serverLevel(), far.getX() + .5, far.getY(), far.getZ() + .5, 0, 0);
                            } else standAtKnot(player, knot);
                        }
                        var entry = VagrantMindEncounterData.get(player.serverLevel()).encounter(dormantPiece.origin());
                        Hemomancy.LOGGER.info("GANGLION_REVIEW {} entity={} defeated={} knot={}", op,
                                entry == null ? null : entry.entity(), entry != null && entry.defeated(), dormantPiece.awakeningPosition());
                        captureLabel = op;
                        captureCountdown = 20;
                    } catch (Exception failure) { Hemomancy.LOGGER.error("GANGLION_REVIEW failure", failure); }
                });
                return;
            }
            if(op.equals("griptest")) { testGrip=true; gripHits=0; return; }
            if (op.equals("camera")) {
                angle = data.get("angle").getAsDouble();
                distance = data.has("distance") ? data.get("distance").getAsDouble() : 22;
                firstPerson = false;
                return;
            }
            if (op.equals("firstperson")) {
                firstPerson = true;
                mc.setCameraEntity(mc.player);
                captured.remove(NaeglerophaeonEntity.GRAB);
                return;
            }
            mc.options.hideGui = true;
            mc.options.renderDistance().set(10);
            mc.options.pauseOnLostFocus = false;
            mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(0.0);
            var playerId = mc.player.getUUID();
            mc.getSingleplayerServer().execute(() -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(playerId);
                try {
                    if (op.equals("setup")) setup(player);
                    else if (op.equals("resume")) {
                        var residents = player.serverLevel().getEntitiesOfClass(NaeglerophaeonEntity.class,
                                player.getBoundingBox().inflate(100));
                        if (!residents.isEmpty()) bossId = residents.getFirst().getUUID();
                        player.setGameMode(GameType.CREATIVE);
                    }
                    else {
                        var entity = player.serverLevel().getEntity(bossId);
                        if (!(entity instanceof NaeglerophaeonEntity resident)) return;
                        if (op.equals("combat")) {
                            if (!player.isAlive()) throw new IllegalStateException("Respawn before starting a combat review");
                            player.setGameMode(GameType.SURVIVAL);
                            player.setNoGravity(true);
                            player.setHealth(20);
                            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                    net.minecraft.world.effect.MobEffects.REGENERATION, 1200, 4, false, false));
                            for(int attempt=0;attempt<12;attempt++) {
                                double theta=attempt*Math.PI/6;
                                Vec3 point=resident.position().add(Math.cos(theta)*8,0,Math.sin(theta)*8);
                                if(!player.serverLevel().noCollision(player,player.getBoundingBox().move(point.subtract(player.position())))) continue;
                                player.teleportTo(player.serverLevel(),point.x,point.y,point.z,90,0);
                                if(resident.hasLineOfSight(player)) break;
                            }
                            set(resident, "zapCooldown", 0);
                            set(resident, "grabCooldown", 0);
                        }
                        // Stage shortcuts: the boss reacts on its next tick (transition / ascent to the web's centre).
                        if (op.equals("phase2")) resident.setHealth(resident.getMaxHealth() * .5F);
                        if (op.equals("overload")) resident.setHealth(resident.getMaxHealth() * .1F);
                        if (op.equals("release")) {
                            resident.hurt(player.serverLevel().damageSources().playerAttack(player), 12);
                            player.setGameMode(GameType.CREATIVE);
                        }
                        if (op.equals("blink")) {
                            var piece = new VagrantMindPiece(new BlockPos(0, 150, 0), 37L);
                            for (BlockPos node : piece.nodePositions()) {
                                if (node.distSqr(resident.blockPosition()) < 36) continue;
                                var box = resident.getBoundingBox().move(Vec3.atBottomCenterOf(node.above()).subtract(resident.position()));
                                if (player.serverLevel().noCollision(resident, box)) {
                                    
                                    resident.teleportTo(node.getX()+.5, node.getY()+2, node.getZ()+.5);
                                    break;
                                }
                            }
                        }
                        if (op.equals("multiple")) for (int i = 0; i < 3; i++) {
                            var extra = EntityInit.naeglerophaeon.get().create(player.serverLevel());
                            extra.moveTo(resident.getX() + (i - 1) * 3, resident.getY() + 2, resident.getZ() - 3, 0, 0);
                            extra.setPersistenceRequired();
                            player.serverLevel().addFreshEntity(extra);
                        }
                    }
                    Hemomancy.LOGGER.info("MEMBRANE_REVIEW {}", op);
                } catch (Exception failure) { Hemomancy.LOGGER.error("MEMBRANE_REVIEW failure", failure); }
            });
        } catch (Exception failure) { Hemomancy.LOGGER.error("MEMBRANE_REVIEW client failure", failure); }
    }

    private static void setup(ServerPlayer player) {
        var level = player.getServer().getLevel(Level.END);
        player.setGameMode(GameType.CREATIVE);
        player.teleportTo(level, 0, 150, 0, 0, 0);
        var piece = new VagrantMindPiece(new BlockPos(0, 150, 0), 37L);
        var box = new BoundingBox(-128, 22, -128, 128, 255, 128);
        piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(),
                level.random, box, new ChunkPos(0, 0), new BlockPos(0, 150, 0));
        var boss = EntityInit.naeglerophaeon.get().create(level);
        for (BlockPos node : piece.nodePositions()) {
            boss.moveTo(node.getX() + .5, node.getY() + 2, node.getZ() + .5, 0, 0);
            if (level.noCollision(boss)) break;
        }
        boss.bindMind(piece.origin(), piece.seed(), piece.nodePositions());
        boss.setPersistenceRequired();
        level.addFreshEntity(boss);
        bossId = boss.getUUID();
        player.teleportTo(level, boss.getX() + 2, boss.getY() + 1, boss.getZ() + 3, 0, 0);
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
    }

    /** Native activation fixture with real structure references; ordinary mining still starts the fight. */
    private static void setupDormant(ServerPlayer player) {
        var level = player.getServer().getLevel(Level.END);
        var origin = new BlockPos(2048 + (int) (level.getGameTime() / 20) * 512, 150, 0);
        dormantPiece = new VagrantMindPiece(origin, 37L);
        player.setGameMode(GameType.CREATIVE);
        player.teleportTo(level, origin.getX(), origin.getY(), origin.getZ(), 0, 0);
        var box = dormantPiece.getBoundingBox();
        dormantPiece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), level.random,
                box, new ChunkPos(origin), origin);
        var structure = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .get(Hemomancy.rloc("vagrant_mind"));
        var startChunk = level.getChunkAt(origin);
        startChunk.setStartForStructure(structure, new net.minecraft.world.level.levelgen.structure.StructureStart(
                structure, startChunk.getPos(), 0, new net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer(java.util.List.of(dormantPiece))));
        for (int x = box.minX() >> 4; x <= box.maxX() >> 4; x++)
            for (int z = box.minZ() >> 4; z <= box.maxZ() >> 4; z++)
                level.getChunk(x, z).addReferenceForStructure(structure, startChunk.getPos().toLong());
        BlockPos knot = dormantPiece.awakeningPosition();
        level.setBlockAndUpdate(knot, BlockInit.dormant_ganglion.get().defaultBlockState());
        player.setGameMode(GameType.SURVIVAL);
        player.getAbilities().invulnerable = true;
        player.onUpdateAbilities();
        player.setNoGravity(true);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.REGENERATION, 7200, 4, false, false));
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 7200, 4, false, false));
        standAtKnot(player, knot);
        bossId = null;
    }

    private static void standAtKnot(ServerPlayer player, BlockPos knot) {
        var level = player.getServer().getLevel(Level.END);
        level.setBlockAndUpdate(knot.offset(0, -1, 4), net.minecraft.world.level.block.Blocks.BARRIER.defaultBlockState());
        player.teleportTo(level, knot.getX() + .5, knot.getY(), knot.getZ() + 4.5, 180, 16);
        player.setNoGravity(false);
    }

    private static void set(Object object, String name, Object value) throws ReflectiveOperationException {
        var field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(object, value);
    }
}
