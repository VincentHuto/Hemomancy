package com.vincenthuto.hemomancy.common.event.worldevent;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.worldgen.VesperOrdealManager;
import com.vincenthuto.hemomancy.common.worldgen.MycophantEncounterManager;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.client.particle.data.BloodCellData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;

/**
 * Keeps players, placed blocks, and tossed items inside their owner's Chamber of
 * Will cell. V1 chambers are caster-only, so owner and occupant are the same
 * UUID, but the owner tag keeps the recovery path ready for future guests.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ChamberOfWillEvents {
    private static final String OWNER_KEY = "hemomancy:chamber_of_will_owner";
    private static final String HANDLED_KEY = "hemomancy:orb_of_perspective_handled";

    private ChamberOfWillEvents() {}

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!level.dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) return;

        ChamberOfWillManager.refreshProgressionNow(player);
        ChamberOfWillManager manager = ChamberOfWillManager.get(player.getServer());
        BlockPos cell = manager.cellPos(manager.idFor(player.getUUID()));
        int radius = manager.radiusFor(player.getUUID());

        BlockPos pos = event.getPos();
        if (!ChamberBoundaryRules.insidePlatform(pos.getX(), pos.getZ(), cell.getX(), cell.getZ(), radius)
                || pos.getY() < cell.getY()) {
            event.setCanceled(true);
            player.displayClientMessage(Component.translatable("chat.hemomancy.chamber_of_will.block_out_of_bounds"), true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) return;
        ChamberOfWillManager.refreshProgressionNow(serverPlayer);
        if (MycophantEncounterManager.isActive(serverPlayer)
                || !ChamberPlayerSafeguardRules.shouldApply(player.isCreative(), player.isSpectator(),
                VesperOrdealManager.isActive(serverPlayer))) return;

        ChamberOfWillManager manager = ChamberOfWillManager.get(serverPlayer.getServer());
        BlockPos cell = manager.cellPos(manager.idFor(player.getUUID()));
        int radius = manager.radiusFor(player.getUUID());

        double px = player.getX();
        double pz = player.getZ();
        double py = player.getY();

        boolean changed = false;
        double nx = px, nz = pz;

        nx = ChamberBoundaryRules.clampCoordinate(px, cell.getX(), radius);
        nz = ChamberBoundaryRules.clampCoordinate(pz, cell.getZ(), radius);
        changed = nx != px || nz != pz;

        if (ChamberBoundaryRules.belowRescuePlane(py, cell.getY())) {
            ChamberOfWillManager.rescuePlayerToCell(serverPlayer, (ServerLevel) player.level(), cell, radius);
            return;
        }

        if (changed) {
            player.setDeltaMovement(Vec3.ZERO);
            serverPlayer.teleportTo(nx, py, nz);
        }
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        if (event.getEntity() == null || event.getPlayer() == null) return;
        ItemEntity item = event.getEntity();
        if (item.level().isClientSide) return;
        if (!item.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) return;
        item.getPersistentData().putUUID(OWNER_KEY, event.getPlayer().getUUID());
        item.getPersistentData().remove(HANDLED_KEY);
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity item)) return;
        if (item.level().isClientSide) return;
        if (!item.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) return;

        ServerLevel level = (ServerLevel) item.level();
        CompoundTag data = item.getPersistentData();
        if (!data.contains(OWNER_KEY)) return;
        UUID ownerId = data.getUUID(OWNER_KEY);
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);

        ChamberOfWillManager manager = ChamberOfWillManager.get(level.getServer());
        BlockPos cell = manager.cellPos(manager.idFor(ownerId));

        if (item.getItem().is(ItemInit.orb_of_perspective.get())
                && ChamberBoundaryRules.belowOrbPlane(item.getY(), cell.getY())) {
            if (owner == null) {
				data.putBoolean(HANDLED_KEY, true);
				OrbOfPerspectiveRules.SafePosition safe = OrbOfPerspectiveRules.ownerlessReturnPosition(
						cell.getX(), cell.getY(), cell.getZ());
                item.setUnlimitedLifetime();
				item.setNoGravity(false);
                item.setDeltaMovement(Vec3.ZERO);
				item.setPos(safe.x(), safe.y(), safe.z());
				item.setDefaultPickUpDelay();
                return;
            }
            handleOrb(item, owner, manager, cell);
            return;
        }

        if (owner == null) return;

        if (ChamberBoundaryRules.belowRescuePlane(item.getY(), cell.getY())) {
            ItemStack stack = item.getItem().copy();
            if (owner.getInventory().add(stack)) {
                item.discard();
                return;
            }
            item.setPos(owner.getX(), owner.getY() + 1.0, owner.getZ());
            item.setDefaultPickUpDelay();
            item.getPersistentData().putUUID(OWNER_KEY, owner.getUUID());
        }
    }

    private static void handleOrb(ItemEntity item, ServerPlayer owner, ChamberOfWillManager manager, BlockPos cell) {
        CompoundTag data = item.getPersistentData();
        boolean ownerInCell = owner.level() == item.level()
                && ChamberBoundaryRules.insideAllocatedCell(owner.getX(), owner.getZ(), cell.getX(), cell.getZ(),
                        ChamberOfWillManager.CHAMBER_SPACING)
                && ChamberBoundaryRules.insideAllocatedCell(item.getX(), item.getZ(), cell.getX(), cell.getZ(),
                        ChamberOfWillManager.CHAMBER_SPACING);
        boolean beyondPlatform = !ChamberBoundaryRules.insidePlatform(
                item.getX(), item.getZ(), cell.getX(), cell.getZ(), manager.radiusFor(owner.getUUID()));
        OrbOfPerspectiveRules.Activation activation = OrbOfPerspectiveRules.activation(
                data.getBoolean(HANDLED_KEY), true, beyondPlatform, !ownerInCell,
                VesperOrdealManager.isActive(owner), MycophantEncounterManager.isActive(owner),
                manager.availableSkyThemes(owner).size());
        if (activation == OrbOfPerspectiveRules.Activation.HANDLED) return;

        data.putBoolean(HANDLED_KEY, true);
        switch (activation) {
            case CYCLE -> {
                manager.cycleAvailableSkyTheme(owner);
                owner.displayClientMessage(Component.translatable("item.hemomancy.orb_of_perspective.changed"), true);
                playThemeChangeEffect(owner.serverLevel(), owner);
            }
            case NO_OTHER_THEME -> owner.displayClientMessage(
                    Component.translatable("item.hemomancy.orb_of_perspective.no_other_theme"), true);
            case REJECT_ENCOUNTER -> owner.displayClientMessage(
                    Component.translatable("item.hemomancy.orb_of_perspective.encounter_active"), true);
            case REJECT_OUTSIDE_CELL, REJECT_NO_OWNER -> owner.displayClientMessage(
                    Component.translatable("item.hemomancy.orb_of_perspective.outside_cell"), true);
            case REJECT_NOT_BEYOND_PLATFORM -> owner.displayClientMessage(
                    Component.translatable("item.hemomancy.orb_of_perspective.not_beyond_platform"), true);
            default -> {
            }
        }
        returnOrb(item, owner);
    }

    private static void returnOrb(ItemEntity item, ServerPlayer owner) {
        ItemStack returned = item.getItem();
        item.setItem(ItemStack.EMPTY);
        boolean accepted = owner.getInventory().add(returned);
        if (OrbOfPerspectiveRules.returnTarget(accepted) == OrbOfPerspectiveRules.ReturnTarget.INVENTORY) {
            item.discard();
            return;
        }
        item.setItem(returned);
        item.setNoGravity(false);
        item.setPos(owner.getX(), owner.getY() + 0.5D, owner.getZ());
        item.setDeltaMovement(Vec3.ZERO);
        item.setDefaultPickUpDelay();
    }

    private static void playThemeChangeEffect(ServerLevel level, ServerPlayer owner) {
        level.sendParticles(new BloodCellData(131, 0, 0),
                owner.getX(), owner.getY() + 1.0D, owner.getZ(), 12, 0.45D, 0.65D, 0.45D, 0.02D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                owner.getX(), owner.getY() + 1.0D, owner.getZ(), 8, 0.35D, 0.5D, 0.35D, 0.03D);
        level.playSound(null, owner.blockPosition(), SoundEvents.END_PORTAL_FRAME_FILL,
                SoundSource.PLAYERS, 0.7F, 0.75F);
    }
}
