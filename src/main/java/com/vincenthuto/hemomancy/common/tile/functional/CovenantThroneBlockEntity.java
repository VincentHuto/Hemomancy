package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.CovenantThroneBlock;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.IBloodTile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the {@link CovenantThroneBlock}.
 *
 * <p>Stores only the {@code lastTranceTime} field (the game tick at which the
 * Covenant Trance was last triggered), which drives the per-player cooldown
 * check.</p>
 *
 * <p>{@link IBloodTile} is implemented only to satisfy internal capability
 * lookup conventions; no blood is stored in this block entity itself.</p>
 */
public class CovenantThroneBlockEntity extends BlockEntity implements IBloodTile {

    private static final String TAG_LAST_TRANCE = "lastTranceTime";

    /**
     * The {@link net.minecraft.world.level.Level#getGameTime() game time} at
     * which the Covenant Trance was last successfully triggered.  {@code -1}
     * means it has never been used.
     */
    private long lastTranceTime = -1L;

    public CovenantThroneBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.covenant_throne.get(), pos, state);
    }

    // ── Trance cooldown accessors ──────────────────────────────────────────────

    public long getLastTranceTime() {
        return lastTranceTime;
    }

    public void setLastTranceTime(long time) {
        this.lastTranceTime = time;
        setChanged();
    }

    // ── IBloodTile ─────────────────────────────────────────────────────────────

    @Override
    public void sendUpdates() {
        if (level == null) return;
        level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        setChanged();
    }

    // ── Serialisation ─────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong(TAG_LAST_TRANCE, lastTranceTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        lastTranceTime = tag.getLong(TAG_LAST_TRANCE);
    }

    // ── Network sync ──────────────────────────────────────────────────────────

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putLong(TAG_LAST_TRANCE, lastTranceTime);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        lastTranceTime = tag.getLong(TAG_LAST_TRANCE);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt,
            HolderLookup.Provider registries) {
        super.onDataPacket(net, pkt, registries);
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            lastTranceTime = tag.getLong(TAG_LAST_TRANCE);
        }
    }
}
