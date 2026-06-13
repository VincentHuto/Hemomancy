package com.vincenthuto.hemomancy.common.item.harbinger.memory;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncLastDeathMemory;
import com.vincenthuto.hemomancy.common.network.routing.PacketSpawnVeinSpiderCourier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class HematicMemoryToolEvents {
    public static final String TAG_EFFIGY = "HemomancyHuskEffigy";
    public static final String TAG_OWNER = "HemomancyEffigyOwner";
    public static final String TAG_EXPIRES = "HemomancyEffigyExpires";

    private HematicMemoryToolEvents() {
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HemoCapabilityAccess.getLastDeathMemory(player).remember(player.level().dimension().location().toString(),
                    LastDeathMemory.packPosition(player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ()),
                    player.level().getGameTime());
            syncLastDeathMemory(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncLastDeathMemory(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncLastDeathMemory(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncLastDeathMemory(player);
        }
    }

    public static void syncLastDeathMemory(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player,
                new PacketSyncLastDeathMemory(LastDeathMemorySnapshot.from(HemoCapabilityAccess.getLastDeathMemory(player))));
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof Mob mob) || !mob.getPersistentData().getBoolean(TAG_EFFIGY)
                || !(event.getEntity() instanceof Player player)) {
            return;
        }
        UUID ownerUuid = ownerUuid(mob);
        if (HuskEffigyRules.shouldRejectTarget(ownerUuid, player.getUUID())) {
            mob.setTarget(null);
            mob.setLastHurtByMob(null);
            event.setNewDamage(0.0F);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        long time = level.getGameTime();
        if (time % 20L == 0L) {
            tickVeinSpiders(serverLevel);
        }
        if (time % 10L == 0L) {
            tickEffigies(serverLevel, time);
        }
    }

    private static void tickVeinSpiders(ServerLevel level) {
        if (!VeinSpiderRules.shouldTransferAt(level.getGameTime())) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            if (!HemoCapabilityAccess.getBloodVolume(player).map(v -> v.isActive()
                    && v.getBloodVolume() >= VeinSpiderRules.BLOOD_COST_PER_TRANSFER).orElse(false)) {
                continue;
            }
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.is(ItemInit.vein_spider.get())) {
                    continue;
                }
                Optional<VeinSpiderRules.Link> link = VeinSpiderItem.readLink(stack).filter(VeinSpiderRules::isValidLink);
                if (link.isEmpty() || !link.get().fromDimension().equals(level.dimension().location().toString())) {
                    continue;
                }
                if (tryTransfer(level, link.get())) {
                    HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
                        volume.drain(VeinSpiderRules.BLOOD_COST_PER_TRANSFER);
                        BloodVolumeEvents.syncVolume(player, volume);
                    });
                }
            }
        }
    }

    private static boolean tryTransfer(ServerLevel level, VeinSpiderRules.Link link) {
        BlockPos from = new BlockPos(LastDeathMemory.unpackX(link.fromPackedPos()),
                LastDeathMemory.unpackY(link.fromPackedPos()), LastDeathMemory.unpackZ(link.fromPackedPos()));
        BlockPos to = new BlockPos(LastDeathMemory.unpackX(link.toPackedPos()),
                LastDeathMemory.unpackY(link.toPackedPos()), LastDeathMemory.unpackZ(link.toPackedPos()));
        if (!level.isLoaded(from) || !level.isLoaded(to)) {
            return false;
        }
        for (Direction sourceSide : transferDirections()) {
            IItemHandler source = level.getCapability(Capabilities.ItemHandler.BLOCK, from, sourceSide);
            if (source == null) {
                continue;
            }
            for (Direction targetSide : transferDirections()) {
                IItemHandler target = level.getCapability(Capabilities.ItemHandler.BLOCK, to, targetSide);
                if (target == null) {
                    continue;
                }
                ItemStack moved = tryMoveOne(source, target);
                if (!moved.isEmpty()) {
                    sendVeinSpiderCourier(level, from, to, moved);
                    return true;
                }
            }
        }
        return false;
    }

    private static Direction[] transferDirections() {
        int[] sides = VeinSpiderRules.transferSideProbeOrder();
        Direction[] directions = new Direction[sides.length];
        for (int i = 0; i < sides.length; i++) {
            directions[i] = switch (sides[i]) {
                case VeinSpiderRules.SIDE_DOWN -> Direction.DOWN;
                case VeinSpiderRules.SIDE_UP -> Direction.UP;
                case VeinSpiderRules.SIDE_NORTH -> Direction.NORTH;
                case VeinSpiderRules.SIDE_SOUTH -> Direction.SOUTH;
                case VeinSpiderRules.SIDE_WEST -> Direction.WEST;
                case VeinSpiderRules.SIDE_EAST -> Direction.EAST;
                default -> null;
            };
        }
        return directions;
    }

    private static ItemStack tryMoveOne(IItemHandler source, IItemHandler target) {
        for (int slot = 0; slot < source.getSlots(); slot++) {
            ItemStack extracted = source.extractItem(slot, 1, true);
            if (extracted.isEmpty()) {
                continue;
            }
            ItemStack remainder = insert(target, extracted.copy(), true);
            if (remainder.isEmpty()) {
                ItemStack realExtract = source.extractItem(slot, 1, false);
                insert(target, realExtract.copy(), false);
                return realExtract.copyWithCount(1);
            }
        }
        return ItemStack.EMPTY;
    }

    private static void sendVeinSpiderCourier(ServerLevel level, BlockPos from, BlockPos to, ItemStack moved) {
        double x = (from.getX() + to.getX()) * 0.5D + 0.5D;
        double y = (from.getY() + to.getY()) * 0.5D + 0.5D;
        double z = (from.getZ() + to.getZ()) * 0.5D + 0.5D;
        double radius = Math.min(VeinSpiderRules.MAX_RANGE_BLOCKS + 32.0D, Math.sqrt(from.distSqr(to)) * 0.5D + 48.0D);
        PacketDistributor.sendToPlayersNear(level, null, x, y, z, radius,
                new PacketSpawnVeinSpiderCourier(from, to, moved.copyWithCount(1), level.random.nextInt()));
    }

    private static ItemStack insert(IItemHandler handler, ItemStack stack, boolean simulate) {
        ItemStack remainder = stack;
        for (int slot = 0; slot < handler.getSlots() && !remainder.isEmpty(); slot++) {
            remainder = handler.insertItem(slot, remainder, simulate);
        }
        return remainder;
    }

    private static void tickEffigies(ServerLevel level, long time) {
        Set<UUID> seen = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(96.0D),
                    candidate -> candidate.getPersistentData().getBoolean(TAG_EFFIGY))) {
                if (!seen.add(mob.getUUID())) {
                    continue;
                }
                if (time >= mob.getPersistentData().getLong(TAG_EXPIRES)) {
                    mob.discard();
                    continue;
                }
                LivingEntity currentTarget = mob.getTarget();
                UUID ownerUuid = ownerUuid(mob);
                if (currentTarget instanceof Player targetPlayer
                        && HuskEffigyRules.shouldRejectTarget(ownerUuid, targetPlayer.getUUID())) {
                    mob.setTarget(null);
                    mob.setLastHurtByMob(null);
                    currentTarget = null;
                }
                if (currentTarget == null || !currentTarget.isAlive()) {
                    LivingEntity target = level.getNearestEntity(Monster.class,
                            net.minecraft.world.entity.ai.targeting.TargetingConditions.DEFAULT,
                            mob, mob.getX(), mob.getY(), mob.getZ(), mob.getBoundingBox().inflate(12.0D));
                    if (target != null && !isOwner(mob, target)) {
                        mob.setTarget(target);
                    }
                }
            }
        }
    }

    private static boolean isOwner(Mob mob, LivingEntity target) {
        return target instanceof Player player
                && mob.getPersistentData().hasUUID(TAG_OWNER)
                && HuskEffigyRules.isOwnerTarget(ownerUuid(mob), player.getUUID());
    }

    private static UUID ownerUuid(Mob mob) {
        return mob.getPersistentData().hasUUID(TAG_OWNER) ? mob.getPersistentData().getUUID(TAG_OWNER) : null;
    }
}
