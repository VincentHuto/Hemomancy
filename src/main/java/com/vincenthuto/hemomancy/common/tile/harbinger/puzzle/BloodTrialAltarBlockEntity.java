package com.vincenthuto.hemomancy.common.tile.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.entity.mob.monster.HematicConstructEntity;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class BloodTrialAltarBlockEntity extends BlockEntity {

    private static final double TRIAL_RADIUS = 15.0;
    private static final int STRAIN_DURATION = 200;
    private static final int WAVE_INTERVAL = 300;
    private static final int MAX_CONSTRUCTS = 6;

    private int tickCounter = 0;
    private boolean active = false;
    private boolean completed = false;

    public BloodTrialAltarBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.blood_trial_altar.get(), pos, state);
    }

    public boolean isActive() {
        return active;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void activate() {
        if (!active && !completed) {
            active = true;
            tickCounter = 0;
            setChanged();
        }
    }

    public void deactivate() {
        active = false;
        completed = true;
        // Kill all remaining constructs spawned by this trial
        if (level != null && !level.isClientSide) {
            AABB range = new AABB(worldPosition).inflate(TRIAL_RADIUS);
            for (HematicConstructEntity construct : level.getEntitiesOfClass(HematicConstructEntity.class, range)) {
                construct.discard();
            }
        }
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BloodTrialAltarBlockEntity te) {
        if (!te.active || te.completed) return;

        te.tickCounter++;
        if (te.tickCounter % 20 != 0) return;

        AABB range = new AABB(pos).inflate(TRIAL_RADIUS);
        List<Player> players = level.getEntitiesOfClass(Player.class, range);
        if (players.isEmpty()) return;

        for (Player player : players) {
            player.addEffect(new MobEffectInstance(
                    EffectInit.hematic_strain, STRAIN_DURATION, 0, false, true));
            // Prevent players from mining through mausoleum walls during the trial
            player.addEffect(new MobEffectInstance(
                    MobEffects.DIG_SLOWDOWN, STRAIN_DURATION, 3, false, false));
        }

        if (te.tickCounter % WAVE_INTERVAL == 0 && level instanceof ServerLevel serverLevel) {
            spawnWave(serverLevel, pos, players);
        }
    }

    private static void spawnWave(ServerLevel level, BlockPos pos, List<Player> players) {
        long existing = level.getEntitiesOfClass(HematicConstructEntity.class,
                new AABB(pos).inflate(TRIAL_RADIUS)).size();
        if (existing >= MAX_CONSTRUCTS) return;

        int toSpawn = (int) Math.min(3, MAX_CONSTRUCTS - existing);
        for (int i = 0; i < toSpawn; i++) {
            HematicConstructEntity construct = EntityInit.hematic_construct.get().create(level);
            if (construct == null) continue;
            double angle = (i * 2.0 * Math.PI / toSpawn) + level.random.nextFloat();
            double spawnX = pos.getX() + 0.5 + Math.cos(angle) * 3.0;
            double spawnZ = pos.getZ() + 0.5 + Math.sin(angle) * 3.0;
            construct.moveTo(spawnX, pos.getY() + 1.0, spawnZ, 0, 0);
            construct.finalizeSpawn(level,
                    level.getCurrentDifficultyAt(pos), MobSpawnType.TRIGGERED, null);
            if (!players.isEmpty()) {
                construct.setTarget(players.get(level.random.nextInt(players.size())));
            }
            level.addFreshEntity(construct);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putBoolean("Active", active);
        tag.putBoolean("Completed", completed);
        tag.putInt("TickCounter", tickCounter);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        active = tag.getBoolean("Active");
        completed = tag.getBoolean("Completed");
        tickCounter = tag.getInt("TickCounter");
    }

    /** Find and deactivate any nearby trial altar within the given radius. */
    public static void deactivateNearby(Level level, BlockPos center, int radius) {
        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {
            if (level.getBlockEntity(p) instanceof BloodTrialAltarBlockEntity altar && altar.isActive()) {
                altar.deactivate();
            }
        }
    }
}
