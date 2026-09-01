package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.CovenantThroneBlock;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.shared.IMultiBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

/**
 * Block entity for the {@link CovenantThroneBlock}.
 *
 * <p>Stores only the {@code lastTranceTime} field (the game tick at which the
 * Covenant Trance was last triggered), which drives the per-player cooldown
 * check.</p>
 *
 * <p>{@link IBloodReservoir} is implemented only to satisfy internal capability
 * lookup conventions; no blood is stored in this block entity itself.</p>
 */
public class CovenantThroneBlockEntity extends BlockEntity implements IBloodReservoir, IMultiBlockEntity {

    private static final String TAG_LAST_TRANCE = "lastTranceTime";
    private static final String TAG_SEATED_PLAYER = "seatedPlayer";
    private static final String TAG_TRANCE_VISUAL_START = "tranceVisualStart";
    public static final int TRANCE_VISUAL_DURATION_TICKS = 120;

    /**
     * The {@link net.minecraft.world.level.Level#getGameTime() game time} at
     * which the Covenant Trance was last successfully triggered.  {@code -1}
     * means it has never been used.
     */
    private long lastTranceTime = -1L;
    private UUID seatedPlayer;
    private long tranceVisualStart = -1L;

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

    public UUID getSeatedPlayer() {
        return seatedPlayer;
    }

    public boolean hasSeatedPlayer() {
        return seatedPlayer != null;
    }

    public void setSeatedPlayer(UUID seatedPlayer) {
        this.seatedPlayer = seatedPlayer;
        sendUpdates();
    }

    public void clearSeatedPlayer(UUID playerId) {
        if (seatedPlayer == null) return;
        if (playerId == null || seatedPlayer.equals(playerId)) {
            seatedPlayer = null;
            sendUpdates();
        }
    }

    public void startTranceVisual(long gameTime) {
        tranceVisualStart = gameTime;
        sendUpdates();
    }

    public float getTranceVisualProgress(float partialTicks) {
        if (level == null || tranceVisualStart < 0L) {
            return 0.0F;
        }
        float age = level.getGameTime() + partialTicks - tranceVisualStart;
        if (age < 0.0F || age > TRANCE_VISUAL_DURATION_TICKS) {
            return 0.0F;
        }
        return 1.0F - age / TRANCE_VISUAL_DURATION_TICKS;
    }

    public float getWakeIntensity(float partialTicks) {
        float seated = hasSeatedPlayer() ? 1.0F : 0.0F;
        return Math.max(seated, getTranceVisualProgress(partialTicks));
    }

    public AABB getRenderBoundingBox() {
        return AABB.INFINITE;
    }

    // ── IBloodReservoir ─────────────────────────────────────────────────────────────

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
        if (seatedPlayer != null) {
            tag.putUUID(TAG_SEATED_PLAYER, seatedPlayer);
        }
        tag.putLong(TAG_TRANCE_VISUAL_START, tranceVisualStart);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        lastTranceTime = tag.getLong(TAG_LAST_TRANCE);
        seatedPlayer = tag.hasUUID(TAG_SEATED_PLAYER) ? tag.getUUID(TAG_SEATED_PLAYER) : null;
        tranceVisualStart = tag.getLong(TAG_TRANCE_VISUAL_START);
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
        if (seatedPlayer != null) {
            tag.putUUID(TAG_SEATED_PLAYER, seatedPlayer);
        }
        tag.putLong(TAG_TRANCE_VISUAL_START, tranceVisualStart);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        lastTranceTime = tag.getLong(TAG_LAST_TRANCE);
        seatedPlayer = tag.hasUUID(TAG_SEATED_PLAYER) ? tag.getUUID(TAG_SEATED_PLAYER) : null;
        tranceVisualStart = tag.getLong(TAG_TRANCE_VISUAL_START);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt,
            HolderLookup.Provider registries) {
        super.onDataPacket(net, pkt, registries);
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            lastTranceTime = tag.getLong(TAG_LAST_TRANCE);
            seatedPlayer = tag.hasUUID(TAG_SEATED_PLAYER) ? tag.getUUID(TAG_SEATED_PLAYER) : null;
            tranceVisualStart = tag.getLong(TAG_TRANCE_VISUAL_START);
        }
    }
}
