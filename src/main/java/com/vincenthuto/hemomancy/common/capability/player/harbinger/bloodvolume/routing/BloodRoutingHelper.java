package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.IHarbingerEquipmentItemHandler;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;
import com.vincenthuto.hemomancy.common.tile.CoagulationRules;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.ReservoirReadout;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
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
    private static final BloodAccounting.Source OWNER_DRAW = BloodAccounting.Source.of("blood_routing", Category.STATION, false);
    private static final BloodAccounting.Source KIN_DRAW = BloodAccounting.Source.of("blood_routing_kin", Category.STATION, false);
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
        if (link == null || maxAmount <= 0 || !level.isLoaded(targetPos) || !canUseLink(level, sourcePos, link)) {
            return 0;
        }

        BlockEntity be = level.getBlockEntity(targetPos);
        if (be == null || be instanceof IBloodReservoir closed && !closed.canReceiveBlood()) {
            return 0;
        }

        IBloodVolume targetVolume = targetVolume(be);
        if (targetVolume == null) {
            return 0;
        }

        double reserve = effectiveWorkingReserve(level, link);
        double request = BloodRoutingRules.calculateReserveRequest(targetVolume, reserve, maxAmount);
        // The buffer clots like any tank; the suture's draw revives its clot from the owner, cheaply (LP-4, NQ-01-7).
        BlockBloodVolume block = targetVolume instanceof BlockBloodVolume tank ? tank : null;
        if (block != null) {
            request = block.pourOf(request, CoagulationRules.reviveShare(), CoagulationRules.suturedReviveYield()).taken();
        }
        if (request <= 0) {
            return 0;
        }

        double drawn = drawFromLinkedSources(level, sourcePos, link, request, maxAmount);
        if (drawn <= 0) {
            return 0;
        }

        if (block != null) {
            block.revive(drawn, CoagulationRules.reviveShare(), CoagulationRules.suturedReviveYield());
        } else {
            targetVolume.fill(drawn);
        }
        if (be instanceof IBloodReservoir tile) {
            tile.sendUpdates();
        }
        return drawn;
    }

    public static double drawForCourier(ServerLevel level, BlockPos sourcePos, DirectBloodLinkData link,
            double requested, double maxAmount) {
        return drawFromLinkedSources(level, sourcePos, link, requested, maxAmount);
    }

    public static boolean canUseLink(ServerLevel level, BlockPos sourcePos, DirectBloodLinkData link) {
        return linkStatus(level, sourcePos, link).drawing();
    }

    /**
     * Whether a suture can draw for the block at {@code sourcePos} now, and when it cannot, why: the owner is offline
     * or dead, below the third Degree (the fifth for a fane link), their blood is asleep, they are out of a nearby
     * link's reach, or the block is outside their fane. {@code NONE} for no link. With the owner offline, a member of
     * their bloodline nearby stands in ({@code KIN_DRAWING}, CN-9).
     */
    public static ReservoirReadout.Link linkStatus(ServerLevel level, BlockPos sourcePos, @Nullable DirectBloodLinkData link) {
        if (link == null) {
            return ReservoirReadout.Link.NONE;
        }
        ServerPlayer owner = findOwner(level, link);
        if (owner == null) {
            return findStandIn(level, sourcePos, link) != null ? ReservoirReadout.Link.KIN_DRAWING
                    : ReservoirReadout.Link.OWNER_OFFLINE;
        }
        if (!owner.isAlive()) {
            return ReservoirReadout.Link.OWNER_DEAD;
        }
        if (!ProgressionGates.hasDegree(owner, 3)) {
            return ReservoirReadout.Link.BELOW_DEGREE;
        }
        IBloodVolume ownerVolume = HemoCapabilityAccess.getBloodVolume(owner);
        if (ownerVolume == null || !ownerVolume.isActive()) {
            return ReservoirReadout.Link.BLOOD_DORMANT;
        }
        if (link.mode() == BloodRoutingMode.NEARBY) {
            return owner.level() == level && distanceSqr(owner, sourcePos) <= NEARBY_RANGE * NEARBY_RANGE
                    ? ReservoirReadout.Link.DRAWING : ReservoirReadout.Link.OUT_OF_REACH;
        }
        if (!ProgressionGates.hasDegree(owner, 5)) {
            return ReservoirReadout.Link.BELOW_FANE_DEGREE;
        }
        return isInOwnerFane(level, link.owner(), sourcePos) ? ReservoirReadout.Link.DRAWING
                : ReservoirReadout.Link.OUTSIDE_FANE;
    }

    public static boolean canUseFaneMode(ServerLevel level, ServerPlayer owner, BlockPos sourcePos) {
        return ProgressionGates.hasDegree(owner, 5)
                && isInOwnerFane(level, owner.getUUID(), sourcePos);
    }

    public static boolean isInOwnerFane(ServerLevel level, UUID ownerId, BlockPos pos) {
        return FoundingFaneSavedData.get(level).isWithinFane(ownerId, pos);
    }

    private static double drawFromLinkedSources(ServerLevel level, BlockPos sourcePos, DirectBloodLinkData link,
            double requested, double maxAmount) {
        if (link == null || requested <= 0 || maxAmount <= 0 || !canUseLink(level, sourcePos, link)) {
            return 0;
        }

        ServerPlayer owner = findOwner(level, link);
        if (owner == null) {
            ServerPlayer kin = findStandIn(level, sourcePos, link);
            return kin == null ? 0 : drawFromStandIn(kin, Math.min(requested, maxAmount));
        }

        IBloodVolume ownerVolume = HemoCapabilityAccess.getBloodVolume(owner);
        if (ownerVolume == null || !ownerVolume.isActive()) {
            return 0;
        }

        double remaining = Math.min(requested, maxAmount);
        double drawn = 0;

        GourdHandle gourd = findOpenWornGourd(owner);
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
                    * BloodRoutingRules.routingRateMultiplier(SkillPointHelper.getFaneSutureLevel(owner)));
            playerDraw = Math.min(Math.min(playerDraw, safeAvailable), ownerVolume.getBloodVolume());
            // A sutured machine drinks from its owner as upkeep, every route interval (core-loop Track 3).
            playerDraw = playerDraw > 0
                    ? BloodAccounting.payUpkeep(owner, playerDraw, OWNER_DRAW, ROUTE_INTERVAL_TICKS, false).paid()
                    : 0;
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

        return drawn;
    }

    /**
     * CN-9, NQ-01-16: the member of the offline owner's bloodline who stands in at a sutured machine, nearest first:
     * within {@link BloodRoutingRules#STAND_IN_RANGE} blocks, alive, at the third Degree or past it, blood awake.
     */
    @Nullable
    private static ServerPlayer findStandIn(ServerLevel level, BlockPos sourcePos, DirectBloodLinkData link) {
        Bloodline line = BloodlineSavedData.get(level.getServer().overworld()).getBloodlineForPlayer(link.owner());
        if (line == null) {
            return null;
        }
        ServerPlayer nearest = null;
        double best = Double.MAX_VALUE;
        for (ServerPlayer candidate : level.players()) {
            IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(candidate);
            double distance = distanceSqr(candidate, sourcePos);
            if (!candidate.getUUID().equals(link.owner()) && line.hasMember(candidate.getUUID())
                    && BloodRoutingRules.mayStandIn(candidate.isAlive(), ProgressionGates.hasDegree(candidate, 3),
                            volume != null && volume.isActive(), distance)
                    && distance < best) {
                nearest = candidate;
                best = distance;
            }
        }
        return nearest;
    }

    /** A stand-in pays from their own blood, as upkeep, never below the same safety floor an owner keeps. */
    private static double drawFromStandIn(ServerPlayer kin, double requested) {
        IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(kin);
        double floor = BloodRoutingRules.calculateSafetyFloor(volume);
        double draw = Math.min(Math.min(requested, DEFAULT_PLAYER_RATE_PER_TICK * ROUTE_INTERVAL_TICKS),
                Math.min(BloodRoutingRules.availableAboveFloor(volume, floor), volume.getBloodVolume()));
        return draw > 0 ? BloodAccounting.payUpkeep(kin, draw, KIN_DRAW, ROUTE_INTERVAL_TICKS, false).paid() : 0;
    }

    private static double effectiveWorkingReserve(ServerLevel level, DirectBloodLinkData link) {
        ServerPlayer owner = findOwner(level, link);
        if (owner == null) {
            return link.workingReserve();
        }
        return Math.max(link.workingReserve(), BloodRoutingRules.workingReserve(SkillPointHelper.getFaneSutureLevel(owner)));
    }

    private static double routingRateMultiplier(ServerLevel level, DirectBloodLinkData link) {
        ServerPlayer owner = findOwner(level, link);
        if (owner == null) {
            return 1.0;
        }
        return BloodRoutingRules.routingRateMultiplier(SkillPointHelper.getFaneSutureLevel(owner));
    }

    @Nullable
    private static IBloodVolume targetVolume(BlockEntity be) {
        return HemoCapabilityAccess.getBloodVolume(be).orElse(null);
    }

    private static boolean canUseBloodline(ServerLevel level, ServerPlayer owner, IBloodVolume ownerVolume,
            DirectBloodLinkData link, BlockPos sourcePos) {
        if (!link.bloodlineEnabled()
                || link.mode() != BloodRoutingMode.FANE
                || !isInOwnerFane(level, link.owner(), sourcePos)) {
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

    /** FX-1, NQ-01-11: a suture draws only from the worn gourd, never from one in the hand. */
    @Nullable
    private static GourdHandle findOpenWornGourd(ServerPlayer owner) {
        IHarbingerEquipmentItemHandler scars = HemoCapabilityAccess.getEquipment(owner);
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
