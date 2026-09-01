package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.SanguineVigilBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Block entity for the {@link SanguineVigilBlock}.
 *
 * <p>Blood is stored via the {@link IBloodReservoir} attachment up to
 * {@link #MAX_BLOOD} units.  Every {@link #WARD_TICK_INTERVAL} ticks the
 * block entity scans for hostile mobs within {@link #WARD_RADIUS} blocks and
 * applies {@code blood_binding} to them, draining {@link #BLOOD_PER_APPLICATION}
 * from its store.  When blood runs out the ward goes dormant.</p>
 */
public class SanguineVigilBlockEntity extends BlockEntity implements IBloodReservoir {

    /** Maximum blood stored by the Vigil. */
    public static final double MAX_BLOOD = 5_000.0;

    /** Radius (blocks) in which the ward scans for hostiles. */
    public static final double WARD_RADIUS = 20.0;

    /** How often the ward fires, in ticks. */
    private static final int WARD_TICK_INTERVAL = 20;

    /** Duration of blood_binding applied per ward pulse, in ticks (10 s). */
    private static final int BINDING_DURATION_TICKS = 200;

    /** Blood drained from the Vigil per hostile mob warded per pulse. */
    private static final double BLOOD_PER_APPLICATION = 25.0;

    private static final String TAG_BLOOD_LEVEL = "bloodLevel";

    private boolean initialised = false;

    public SanguineVigilBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.sanguine_vigil.get(), pos, state);
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            SanguineVigilBlockEntity te) {

        // Initialise attachment on first tick
        if (!te.initialised) {
            IBloodVolume vol = te.resolveVolume();
            if (vol != null) {
                vol.setActive(true);
                vol.setMaxBloodVolume(MAX_BLOOD);
                if (vol.getBloodVolume() > MAX_BLOOD) vol.setBloodVolume(MAX_BLOOD);
            }
            te.initialised = true;
            te.setChanged();
        }

        if (level.getGameTime() % WARD_TICK_INTERVAL != 0) return;

        IBloodVolume storage = te.resolveVolume();
        if (storage == null || storage.getBloodVolume() <= 0) return;

        // Scan for hostile mobs in range
        AABB scanBox = new AABB(
                pos.getX() - WARD_RADIUS, pos.getY() - WARD_RADIUS, pos.getZ() - WARD_RADIUS,
                pos.getX() + WARD_RADIUS, pos.getY() + WARD_RADIUS, pos.getZ() + WARD_RADIUS);

        List<Mob> hostiles = level.getEntitiesOfClass(Mob.class, scanBox,
                mob -> mob.getType().getCategory() == MobCategory.MONSTER);

        if (hostiles.isEmpty()) return;

        for (Mob mob : hostiles) {
            if (storage.getBloodVolume() < BLOOD_PER_APPLICATION) break;
            mob.addEffect(new MobEffectInstance(
                    EffectInit.blood_binding,
                    BINDING_DURATION_TICKS, 0, false, true, true));
            storage.subtractBloodVolume(BLOOD_PER_APPLICATION);
        }
        te.sendUpdates();
    }

    // ── IBloodReservoir ─────────────────────────────────────────────────────────────

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
        if (vol != null) tag.putDouble(TAG_BLOOD_LEVEL, vol.getBloodVolume());
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
        if (vol != null) tag.putDouble(TAG_BLOOD_LEVEL, vol.getBloodVolume());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        if (tag.contains(TAG_BLOOD_LEVEL)) {
            IBloodVolume vol = resolveVolume();
            if (vol != null) vol.setBloodVolume(tag.getDouble(TAG_BLOOD_LEVEL));
        }
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt,
            HolderLookup.Provider registries) {
        super.onDataPacket(net, pkt, registries);
        CompoundTag tag = pkt.getTag();
        if (tag != null && tag.contains(TAG_BLOOD_LEVEL)) {
            IBloodVolume vol = resolveVolume();
            if (vol != null) vol.setBloodVolume(tag.getDouble(TAG_BLOOD_LEVEL));
        }
    }

    /** Current stored blood, safe to call client-side after sync. */
    public double getStoredBlood() {
        IBloodVolume vol = resolveVolume();
        return vol != null ? vol.getBloodVolume() : 0.0;
    }

    /** Max capacity. Always {@link #MAX_BLOOD}. */
    public double getMaxBlood() {
        return MAX_BLOOD;
    }
}
