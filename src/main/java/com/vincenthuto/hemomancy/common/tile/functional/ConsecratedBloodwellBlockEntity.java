package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.ConsecratedBloodwellBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.tile.IBloodTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the {@link ConsecratedBloodwellBlock}.
 *
 * <p>Blood is stored via {@link com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes#BLOCK_BLOOD_VOLUME}
 * (the {@link IBloodTile} interface enables this lookup). Max capacity is
 * {@link #MAX_BLOOD} units.</p>
 *
 * <p>The server tick handles optional auto-draw: every
 * {@link #AUTO_DRAW_INTERVAL} ticks, any nearby {@link ServerPlayer} who is
 * standing inside their own Founding Sanctum and has
 * {@code autoDrawEnabled == true} receives blood from the well to top up
 * toward their configured threshold.</p>
 */
public class ConsecratedBloodwellBlockEntity extends BlockEntity implements IBloodTile {

    /** Maximum blood this well can hold. */
    public static final double MAX_BLOOD = 25_000.0;

    /** How many ticks between each auto-draw pass. */
    private static final int AUTO_DRAW_INTERVAL = 40;

    /** Tag key used for manual NBT serialisation of the current blood level. */
    private static final String TAG_BLOOD_LEVEL = "bloodLevel";

    /** Whether the attachment has been initialised yet. */
    private boolean initialised = false;

    public ConsecratedBloodwellBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.consecrated_bloodwell.get(), pos, state);
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            ConsecratedBloodwellBlockEntity te) {

        // Initialise the blood-volume attachment on the first server tick
        if (!te.initialised) {
            IBloodVolume vol = te.resolveVolume();
            if (vol != null) {
                vol.setActive(true);
                vol.setMaxBloodVolume(MAX_BLOOD);
                if (vol.getBloodVolume() > MAX_BLOOD) {
                    vol.setBloodVolume(MAX_BLOOD);
                }
            }
            te.initialised = true;
            te.setChanged();
        }

        // Auto-draw: fill nearby sanctum-resident players from the well
        if (level.getGameTime() % AUTO_DRAW_INTERVAL != 0) return;

        IBloodVolume storage = te.resolveVolume();
        if (storage == null || storage.getBloodVolume() <= 0) return;

        for (ServerPlayer player : ((ServerLevel) level).players()) {
            if (!pos.closerThan(player.blockPosition(), 80)) continue;
            if (!ConsecratedBloodwellBlock.isInOwnSanctum(player)) continue;

            var playerBloodOpt = HemoCapabilityAccess.getBloodVolume(player);
            if (playerBloodOpt.isEmpty() || !playerBloodOpt.get().isActive()) continue;
            IBloodVolume pb = playerBloodOpt.get();
            if (!pb.isAutoDrawEnabled()) continue;

            double target = pb.getMaxBloodVolume() * pb.getAutoDrawThreshold();
            if (pb.getBloodVolume() >= target) continue;

            double deficit = target - pb.getBloodVolume();
            double maxRate = 50.0; // max blood drawn per AUTO_DRAW_INTERVAL
            double draw = Math.min(deficit, Math.min(maxRate, storage.getBloodVolume()));
            if (draw <= 0) continue;

            storage.subtractBloodVolume(draw);
            pb.fill(draw);
            te.sendUpdates();
            PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(pb));
        }
    }

    // ── IBloodTile ─────────────────────────────────────────────────────────────

    @Override
    public void sendUpdates() {
        if (level == null) return;
        level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        setChanged();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private IBloodVolume resolveVolume() {
        return HemoCapabilityAccess.getBloodVolume(this).orElse(null);
    }

    // ── Serialisation ─────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        IBloodVolume vol = resolveVolume();
        if (vol != null) {
            tag.putDouble(TAG_BLOOD_LEVEL, vol.getBloodVolume());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(TAG_BLOOD_LEVEL)) {
            IBloodVolume vol = resolveVolume();
            if (vol != null) {
                vol.setActive(true);
                vol.setMaxBloodVolume(MAX_BLOOD);
                vol.setBloodVolume(Math.min(tag.getDouble(TAG_BLOOD_LEVEL), MAX_BLOOD));
                initialised = true;
            }
        }
    }

    // ── Network sync ──────────────────────────────────────────────────────────

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        IBloodVolume vol = resolveVolume();
        if (vol != null) {
            tag.putDouble(TAG_BLOOD_LEVEL, vol.getBloodVolume());
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        if (tag.contains(TAG_BLOOD_LEVEL)) {
            IBloodVolume vol = resolveVolume();
            if (vol != null) {
                vol.setBloodVolume(tag.getDouble(TAG_BLOOD_LEVEL));
            }
        }
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt,
            HolderLookup.Provider registries) {
        super.onDataPacket(net, pkt, registries);
        CompoundTag tag = pkt.getTag();
        if (tag != null && tag.contains(TAG_BLOOD_LEVEL)) {
            IBloodVolume vol = resolveVolume();
            if (vol != null) {
                vol.setBloodVolume(tag.getDouble(TAG_BLOOD_LEVEL));
            }
        }
    }

    // ── Accessors (for BER / HUD use) ─────────────────────────────────────────

    /** Current stored blood, safe to call client-side after sync. */
    public double getStoredBlood() {
        IBloodVolume vol = resolveVolume();
        return vol != null ? vol.getBloodVolume() : 0.0;
    }

    /** Max storage capacity. Always {@link #MAX_BLOOD}. */
    public double getMaxBlood() {
        return MAX_BLOOD;
    }
}
