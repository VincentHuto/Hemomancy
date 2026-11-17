package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
public class SanguineVigilBlockEntity extends HemoBlockEntity implements IBloodReservoir {

    /** Maximum blood stored by the Vigil. */
    /** A working buffer (core-loop Track 3): kept topped by its owner through a suture, or filled and spent. */
    public static final double MAX_BLOOD = 600.0;

    /** Radius (blocks) in which the ward scans for hostiles. */
    public static final double WARD_RADIUS = 20.0;

    /** How often the ward fires, in ticks. */
    private static final int WARD_TICK_INTERVAL = 20;

    /** Duration of blood_binding applied per ward pulse, in ticks (10 s). */
    private static final int BINDING_DURATION_TICKS = 200;

    /** Blood drained from the Vigil per hostile mob warded per pulse. */
    private static final double BLOOD_PER_APPLICATION = 25.0;

    private static final String TAG_BLOOD_LEVEL = "bloodLevel";
    private static final String TAG_CATALYST = "catalyst";
    private static final String TAG_TENDED = "tendedTicks";

    /** A tending Vigil spends one catalyst in this many ticks of tending (core-loop Track 5). */
    public static final int TICKS_PER_CATALYST = 6000;
    /** The keeper must stand this near for the Vigil to tend, as near as a Degree 3 suture reaches. */
    public static final double TEND_KEEPER_RANGE = 16.0;

    /** Temper catalysts the Vigil burns to keep nearby kept creatures from fading. */
    private ItemStack catalyst = ItemStack.EMPTY;
    private int tendedTicks;
    private boolean tending;

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

        te.tend(level, pos);

        IBloodVolume storage = te.resolveVolume();
        boolean watching = storage != null && storage.getBloodVolume() > 0;
        if (state.getValue(SanguineVigilBlock.LIT) != watching) {
            level.setBlock(pos, state.setValue(SanguineVigilBlock.LIT, watching), Block.UPDATE_ALL);
        }
        if (!watching) return;

        // Scan for hostile mobs in range
        AABB scanBox = new AABB(
                pos.getX() - WARD_RADIUS, pos.getY() - WARD_RADIUS, pos.getZ() - WARD_RADIUS,
                pos.getX() + WARD_RADIUS, pos.getY() + WARD_RADIUS, pos.getZ() + WARD_RADIUS);

        List<Mob> hostiles = level.getEntitiesOfClass(Mob.class, scanBox,
                mob -> mob.getType().getCategory() == MobCategory.MONSTER
                        && !com.vincenthuto.hemomancy.common.entity.menagerie.MenagerieLeash.isLed(mob));

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

    // ── Tending (core-loop Track 5) ─────────────────────────────────────────────

    /**
     * With a catalyst in it and its keeper within suture range, the Vigil keeps every kept creature within
     * {@link com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVessels#VIGIL_REACH} blocks from fading,
     * spending one catalyst every {@link #TICKS_PER_CATALYST} ticks it tends.
     */
    private void tend(Level level, BlockPos pos) {
        boolean was = tending;
        tending = false;
        if (catalyst.isEmpty() || !(level instanceof net.minecraft.server.level.ServerLevel server)) {
            if (was) sendUpdates();
            return;
        }
        java.util.UUID owner = com.vincenthuto.hemomancy.common.tile.ReservoirBlood.settled(this).owner();
        net.minecraft.world.entity.player.Player keeper = owner == null ? null : server.getPlayerByUUID(owner);
        if (keeper == null || !keeper.isAlive()
                || keeper.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > TEND_KEEPER_RANGE * TEND_KEEPER_RANGE) {
            if (was) sendUpdates();
            return;
        }
        tending = true;
        com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVessels.tendAround(server, pos,
                server.getGameTime() + WARD_TICK_INTERVAL * 2L);
        tendedTicks += WARD_TICK_INTERVAL;
        if (tendedTicks >= TICKS_PER_CATALYST) {
            tendedTicks = 0;
            catalyst.shrink(1);
            sendUpdates();
        } else if (!was) {
            sendUpdates();
        } else {
            setChanged();
        }
    }

    /** Takes catalysts from {@code held} into the Vigil's slot; returns how many went in. */
    public int insertCatalyst(ItemStack held) {
        if (held.isEmpty() || com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperCatalysts.of(held) == null) return 0;
        if (!catalyst.isEmpty() && !ItemStack.isSameItemSameComponents(catalyst, held)) return 0;
        int room = held.getMaxStackSize() - catalyst.getCount();
        int moved = Math.min(room, held.getCount());
        if (moved <= 0) return 0;
        if (catalyst.isEmpty()) catalyst = held.copyWithCount(moved);
        else catalyst.grow(moved);
        held.shrink(moved);
        sendUpdates();
        return moved;
    }

    public ItemStack catalyst() {
        return catalyst;
    }

    public boolean tending() {
        return tending;
    }

    /** Drops the catalyst slot, for a broken Vigil. */
    public void dropCatalyst(Level level, BlockPos pos) {
        if (!catalyst.isEmpty()) net.minecraft.world.level.block.Block.popResource(level, pos, catalyst.copy());
        catalyst = ItemStack.EMPTY;
    }

    // ── IBloodReservoir ─────────────────────────────────────────────────────────────

    // ── Helpers ───────────────────────────────────────────────────────────────

    private IBloodVolume resolveVolume() {
        return HemoCapabilityAccess.getBloodVolume(this).orElse(null);
    }

    // ── Serialisation ─────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!catalyst.isEmpty()) tag.put(TAG_CATALYST, catalyst.save(registries));
        tag.putInt(TAG_TENDED, tendedTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        catalyst = tag.contains(TAG_CATALYST) ? ItemStack.parseOptional(registries, tag.getCompound(TAG_CATALYST)) : ItemStack.EMPTY;
        tendedTicks = tag.getInt(TAG_TENDED);
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
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        IBloodVolume vol = resolveVolume();
        if (vol != null) tag.putDouble(TAG_BLOOD_LEVEL, vol.getBloodVolume());
        if (!catalyst.isEmpty()) tag.put(TAG_CATALYST, catalyst.save(registries));
        tag.putBoolean("tending", tending);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        tending = tag.getBoolean("tending");
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
