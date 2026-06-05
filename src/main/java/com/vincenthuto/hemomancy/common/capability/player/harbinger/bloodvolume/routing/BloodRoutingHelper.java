package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.IScarsItemHandler;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingSanctumSavedData;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.tile.IBloodTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.UUID;

public final class BloodRoutingHelper {
    public static final double NEARBY_RANGE = 16.0;
    public static final int ROUTE_INTERVAL_TICKS = 10;
    public static final double DEFAULT_MAX_RATE_PER_TICK = 10.0;
    public static final double DEFAULT_PLAYER_RATE_PER_TICK = 8.0;
    public static final double DEFAULT_BLOODLINE_RATE_PER_TICK = 6.0;

    private BloodRoutingHelper() {
    }

    public static double routeLinkedTarget(ServerLevel level, BlockPos targetPos, DirectBloodLinkData link) {
        return routeLinkedTarget(level, targetPos, targetPos, link,
                DEFAULT_MAX_RATE_PER_TICK * ROUTE_INTERVAL_TICKS * routingRateMultiplier(level, link));
    }

    public static double routeAdjacentTargets(ServerLevel level, BlockPos nodePos, DirectBloodLinkData link) {
        double remainingBudget = DEFAULT_MAX_RATE_PER_TICK * ROUTE_INTERVAL_TICKS * routingRateMultiplier(level, link);
        double routed = 0;
        for (Direction direction : Direction.values()) {
            if (remainingBudget <= 0) {
                break;
            }
            BlockPos targetPos = nodePos.relative(direction);
            double moved = routeLinkedTarget(level, nodePos, targetPos, link, remainingBudget);
            routed += moved;
            remainingBudget -= moved;
        }
        return routed;
    }

    public static double routeLinkedTarget(ServerLevel level, BlockPos sourcePos, BlockPos targetPos,
            DirectBloodLinkData link, double maxAmount) {
        if (link == null || maxAmount <= 0) {
            return 0;
        }

        BlockEntity be = level.getBlockEntity(targetPos);
        if (be == null) {
            return 0;
        }

        IBloodVolume targetVolume = targetVolume(be);
        if (targetVolume == null) {
            return 0;
        }

        double request = requestForTarget(be, targetVolume, effectiveWorkingReserve(level, link), maxAmount);
        if (request <= 0) {
            return 0;
        }

        double drawn = drawFromLinkedSources(level, sourcePos, link, request, maxAmount);
        if (drawn <= 0) {
            return 0;
        }

        targetVolume.fill(drawn);
        if (be instanceof IBloodTile tile) {
            tile.sendUpdates();
        }
        if (be instanceof IBloodRoutingTarget routingTarget) {
            routingTarget.onBloodRouted(drawn);
        }
        return drawn;
    }

    public static double drawForCourier(ServerLevel level, BlockPos sourcePos, DirectBloodLinkData link,
            double requested, double maxAmount) {
        return drawFromLinkedSources(level, sourcePos, link, requested, maxAmount);
    }

    public static boolean canUseLink(ServerLevel level, BlockPos sourcePos, DirectBloodLinkData link) {
        ServerPlayer owner = findOwner(level, link);
        if (owner == null || !owner.isAlive()) {
            return false;
        }
        int degree = HemoCapabilityAccess.getPlayerDegreeNumber(owner);
        if (degree < 3) {
            return false;
        }
        IBloodVolume ownerVolume = HemoCapabilityAccess.getBloodVolume(owner).orElse(null);
        if (ownerVolume == null || !ownerVolume.isActive()) {
            return false;
        }
        if (link.mode() == BloodRoutingMode.NEARBY) {
            return owner.level() == level && distanceSqr(owner, sourcePos) <= NEARBY_RANGE * NEARBY_RANGE;
        }
        return degree >= 5 && isInOwnerSanctum(level, link.owner(), sourcePos);
    }

    public static boolean canUseSanctumMode(ServerLevel level, ServerPlayer owner, BlockPos sourcePos) {
        return HemoCapabilityAccess.getPlayerDegreeNumber(owner) >= 5
                && isInOwnerSanctum(level, owner.getUUID(), sourcePos);
    }

    public static boolean isInOwnerSanctum(ServerLevel level, UUID ownerId, BlockPos pos) {
        FoundingSanctumSavedData data = FoundingSanctumSavedData.get(level);
        BlockPos center = data.getAllSanctums().get(ownerId);
        if (center == null) {
            return false;
        }
        double dx = pos.getX() + 0.5 - (center.getX() + 0.5);
        double dz = pos.getZ() + 0.5 - (center.getZ() + 0.5);
        return dx * dx + dz * dz
                <= FoundingSanctumSavedData.SANCTUM_RADIUS * FoundingSanctumSavedData.SANCTUM_RADIUS;
    }

    private static double drawFromLinkedSources(ServerLevel level, BlockPos sourcePos, DirectBloodLinkData link,
            double requested, double maxAmount) {
        if (link == null || requested <= 0 || maxAmount <= 0 || !canUseLink(level, sourcePos, link)) {
            return 0;
        }

        ServerPlayer owner = findOwner(level, link);
        if (owner == null) {
            return 0;
        }

        IBloodVolume ownerVolume = HemoCapabilityAccess.getBloodVolume(owner).orElse(null);
        if (ownerVolume == null || !ownerVolume.isActive()) {
            return 0;
        }

        double remaining = Math.min(requested, maxAmount);
        double drawn = 0;

        GourdHandle gourd = findOpenEquippedGourd(owner);
        if (gourd != null) {
            double gourdDraw = drawFromVolume(gourd.volume(), remaining,
                    Math.min(remaining, gourd.item().getTransferRate() * ROUTE_INTERVAL_TICKS));
            remaining -= gourdDraw;
            drawn += gourdDraw;
        }

        if (remaining > 0) {
            double playerFloor = BloodRoutingRules.calculateSafetyFloor(ownerVolume);
            double safeAvailable = BloodRoutingRules.availableAboveFloor(ownerVolume, playerFloor);
            double playerDraw = Math.min(remaining, DEFAULT_PLAYER_RATE_PER_TICK * ROUTE_INTERVAL_TICKS
                    * BloodRoutingRules.routingRateMultiplier(SkillPointHelper.getSanctumSutureLevel(owner)));
            playerDraw = Math.min(playerDraw, safeAvailable);
            playerDraw = drawFromVolume(ownerVolume, playerDraw, playerDraw);
            remaining -= playerDraw;
            drawn += playerDraw;
        }

        if (remaining > 0 && canUseBloodline(level, owner, ownerVolume, link, sourcePos)) {
            double bloodlineDraw = Math.min(remaining, DEFAULT_BLOODLINE_RATE_PER_TICK * ROUTE_INTERVAL_TICKS
                    * BloodRoutingRules.bloodlineEfficiencyMultiplier(SkillPointHelper.getBloodlineConcordLevel(owner)));
            Bloodline line = ownerVolume.getBloodLine();
            BloodlineSavedData savedData = BloodlineSavedData.get(owner.server.overworld());
            float fromPool = savedData.drawBlood(line.getBloodlineUUID(), (float) bloodlineDraw);
            drawn += fromPool;
        }

        if (drawn > 0) {
            PacketHandler.sendToPlayer(owner, new BloodVolumeServerPacket(ownerVolume));
        }
        return drawn;
    }

    private static double requestForTarget(BlockEntity be, IBloodVolume targetVolume, double reserve, double maxAmount) {
        if (be instanceof IBloodRoutingTarget target) {
            return target.getBloodRoutingRequest(maxAmount);
        }
        return BloodRoutingRules.calculateReserveRequest(targetVolume, reserve, maxAmount);
    }

    private static double effectiveWorkingReserve(ServerLevel level, DirectBloodLinkData link) {
        ServerPlayer owner = findOwner(level, link);
        if (owner == null) {
            return link.workingReserve();
        }
        return Math.max(link.workingReserve(), BloodRoutingRules.workingReserve(SkillPointHelper.getSanctumSutureLevel(owner)));
    }

    private static double routingRateMultiplier(ServerLevel level, DirectBloodLinkData link) {
        ServerPlayer owner = findOwner(level, link);
        if (owner == null) {
            return 1.0;
        }
        return BloodRoutingRules.routingRateMultiplier(SkillPointHelper.getSanctumSutureLevel(owner));
    }

    @Nullable
    private static IBloodVolume targetVolume(BlockEntity be) {
        if (be instanceof IBloodRoutingTarget target) {
            return target.getRoutingBloodVolume();
        }
        return HemoCapabilityAccess.getBloodVolume(be).orElse(null);
    }

    private static boolean canUseBloodline(ServerLevel level, ServerPlayer owner, IBloodVolume ownerVolume,
            DirectBloodLinkData link, BlockPos sourcePos) {
        if (!link.bloodlineEnabled()
                || link.mode() != BloodRoutingMode.SANCTUM
                || !isInOwnerSanctum(level, link.owner(), sourcePos)) {
            return false;
        }
        Bloodline line = ownerVolume.getBloodLine();
        if (line == null || !line.isValid()) {
            return false;
        }
        BloodlineSavedData savedData = BloodlineSavedData.get(owner.server.overworld());
        Bloodline globalLine = savedData.getBloodline(line.getBloodlineUUID());
        if (globalLine == null || globalLine.getBloodVolume() <= 0) {
            return false;
        }
        return owner.getUUID().equals(globalLine.getLeaderUUID()) || ownerVolume.isBloodRoutingOptInEnabled();
    }

    private static double drawFromVolume(IBloodVolume volume, double requested, double maxAmount) {
        if (volume == null || requested <= 0 || maxAmount <= 0) {
            return 0;
        }
        double amount = Math.min(requested, maxAmount);
        amount = Math.min(amount, volume.getBloodVolume());
        if (amount <= 0) {
            return 0;
        }
        volume.drain(amount);
        return amount;
    }

    @Nullable
    private static ServerPlayer findOwner(ServerLevel level, DirectBloodLinkData link) {
        if (link == null || level.getServer() == null) {
            return null;
        }
        return level.getServer().getPlayerList().getPlayer(link.owner());
    }

    private static double distanceSqr(ServerPlayer player, BlockPos pos) {
        double dx = player.getX() - (pos.getX() + 0.5);
        double dy = player.getY() - (pos.getY() + 0.5);
        double dz = player.getZ() - (pos.getZ() + 0.5);
        return dx * dx + dy * dy + dz * dz;
    }

    @Nullable
    private static GourdHandle findOpenEquippedGourd(ServerPlayer owner) {
        GourdHandle scarGourd = findOpenScarGourd(owner);
        if (scarGourd != null) {
            return scarGourd;
        }
        GourdHandle mainHand = gourdHandle(owner.getMainHandItem());
        if (mainHand != null) {
            return mainHand;
        }
        return gourdHandle(owner.getOffhandItem());
    }

    @Nullable
    private static GourdHandle findOpenScarGourd(ServerPlayer owner) {
        IScarsItemHandler scars = HemoCapabilityAccess.getScars(owner).orElse(null);
        if (scars == null) {
            return null;
        }
        for (int slot = 0; slot < scars.getSlots(); slot++) {
            GourdHandle handle = gourdHandle(scars.getStackInSlot(slot));
            if (handle != null) {
                return handle;
            }
        }
        return null;
    }

    @Nullable
    private static GourdHandle gourdHandle(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BloodGourdItem gourd) || !isOpenGourd(stack)) {
            return null;
        }
        IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(stack).orElse(null);
        if (volume == null || volume.getBloodVolume() <= 0) {
            return null;
        }
        return new GourdHandle(stack, gourd, volume);
    }

    private static boolean isOpenGourd(ItemStack stack) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return data.getBoolean(BloodGourdItem.TAG_STATE);
    }

    private record GourdHandle(ItemStack stack, BloodGourdItem item, IBloodVolume volume) {
    }
}
