package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.npc.DrudgeEntity;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Block entity for the Semi-Sentient Construct (SSC).
 * <p>
 * The SSC serves as the hub for Drudge entities. It implements
 * {@link IBloodReservoir} so that it can hold a blood volume (populated by
 * adjacent Dendritic Distributors or filled via the Blood Thrall system).
 * Each tick it refills the blood charge of any nearby Drudge whose
 * {@code homePos} matches this SSC's world position.
 */
public class SemiSentientConstructBlockEntity extends BlockEntity implements IBloodReservoir {

    /** Maximum blood the SSC can hold — generous so many drudges can be fed. */
    public static final double MAX_BLOOD = 30_000.0;

    /**
     * How many game ticks between each Drudge-scan pass.
     * Scanning every tick is cheap (AABB + instanceof), but we throttle
     * to every 10 ticks (0.5 s) for a small performance margin.
     */
    private static final int SCAN_INTERVAL = 10;
    private int scanTimer = 0;

    public SemiSentientConstructBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.semi_sentient_construct.get(), pos, state);
    }

    // ── IBloodReservoir ────────────────────────────────────────────────────────────

    @Override
    public void sendUpdates() {
        if (level == null) return;
        level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        setChanged();
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    public void tick() {
        if (level == null || level.isClientSide) return;

        // Ensure the SSC has a sensible max blood volume on first tick
        IBloodVolume vol = HemoCapabilityAccess.getBloodVolume(this).orElse(null);
        if (vol != null && vol.getMaxBloodVolume() < MAX_BLOOD) {
            vol.setMaxBloodVolume(MAX_BLOOD);
        }

        // Throttled drudge refill scan
        if (--scanTimer > 0) return;
        scanTimer = SCAN_INTERVAL;

        if (vol == null || vol.isEmpty()) return;

        // Find nearby Drudges whose homePos matches this position
        AABB scanBox = new AABB(worldPosition).inflate(3.0);
        List<DrudgeEntity> nearby = level.getEntitiesOfClass(DrudgeEntity.class, scanBox);
        for (DrudgeEntity drudge : nearby) {
            BlockPos home = drudge.getHomePos();
            if (home == null || !home.equals(worldPosition)) continue;
            if (drudge.isRogue()) continue;
            if (drudge.getBloodCharge() >= DrudgeEntity.MAX_BLOOD_CHARGE) continue;

            // Transfer up to REFILL_RATE * SCAN_INTERVAL mL per scan window
            float transferAmount = DrudgeEntity.REFILL_RATE * SCAN_INTERVAL;
            float needed = DrudgeEntity.MAX_BLOOD_CHARGE - drudge.getBloodCharge();
            float toTransfer = Math.min(transferAmount, needed);
            if (toTransfer <= 0) continue;

            if (vol.getBloodVolume() >= toTransfer) {
                vol.drain(toTransfer);
                drudge.fillBloodCharge(toTransfer);
                sendUpdates();
            }
        }
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    }

    // ── Helper: count drudges already bound to this SSC ──────────────────────

    /**
     * Returns the number of living (non-rogue) {@link DrudgeEntity} instances
     * within 64 blocks whose {@code homePos} matches this SSC.
     */
    public int countBoundDrudges() {
        if (level == null) return 0;
        AABB area = new AABB(worldPosition).inflate(64.0);
        List<DrudgeEntity> candidates = level.getEntitiesOfClass(DrudgeEntity.class, area);
        int count = 0;
        for (DrudgeEntity d : candidates) {
            BlockPos home = d.getHomePos();
            if (home != null && home.equals(worldPosition) && !d.isRogue()) {
                count++;
            }
        }
        return count;
    }
}

