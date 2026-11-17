package com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.init.BiomeInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.worldgen.terrain.phlegethontic.PhlegethonticTags;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.OptionalDouble;
import java.util.UUID;

/**
 * CR-7: the Phlegethosoma is called, never met by chance. Blood projected into a broad pool of basin ichor is counted
 * for that pool ({@link PhlegethosomaSummons}); at 500 mL the animal rises at the pour, homed there and turned on the
 * one who called it. Any player with awake blood may call it (NQ-06-10). One lives in a basin at a time, and a player
 * has one at a time (NQ-06-8). A link in the Projection chain, ahead of chum.
 */
public final class PhlegethosomaSummoning {
    public static final BloodAccounting.Source SUMMON =
            BloodAccounting.Source.of("phlegethosoma_summon", Category.WORLD, false);
    private static final ParticleColor ICHOR_BLOOM = new ParticleColor(132, 8, 16);
    private static final ParticleColor ICHOR_BRIGHT = new ParticleColor(176, 18, 26);

    public enum Refusal {
        PEACEFUL("peaceful"), NARROW("narrow"), YOURS("yours"), OCCUPIED("occupied");

        private final String key;

        Refusal(String key) {
            this.key = key;
        }

        public String key() {
            return "message.hemomancy.phlegethosoma.summon." + key;
        }
    }

    private PhlegethosomaSummoning() {
    }

    /** The Projection chain's ichor link: empty unless the hit is basin ichor in the Nether. */
    public static OptionalDouble tryProject(ServerPlayer player, BlockHitResult hit) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = hit.getBlockPos();
        if (!level.getFluidState(pos).is(PhlegethonticTags.ICHOR) || !inBasin(level, pos)) return OptionalDouble.empty();
        return pour(player, pos, hit.getLocation());
    }

    public static boolean inBasin(ServerLevel level, BlockPos pos) {
        return level.dimension().equals(Level.NETHER) && level.getBiome(pos).is(BiomeInit.PHLEGETHONTIC_BASIN);
    }

    /**
     * One tick's pour into the ichor at {@code pos}, wherever it lies (the basin check is {@link #tryProject}'s). Pays
     * 2 mL, counts it, and calls the animal at 500 mL. A refused pour costs nothing and says why.
     */
    public static OptionalDouble pour(ServerPlayer player, BlockPos pos, Vec3 at) {
        ServerLevel level = player.serverLevel();
        if (!HemoCapabilityAccess.getBloodVolume(player).isActive()) return OptionalDouble.empty();
        BlockPos surface = surface(level, pos);
        Refusal refusal = refusal(level, player, surface);
        if (refusal != null) {
            player.displayClientMessage(Component.translatable(refusal.key()).withStyle(ChatFormatting.DARK_RED), true);
            return OptionalDouble.of(0.0D);
        }
        double ml = PhlegethosomaSummonRules.ML_PER_TICK;
        if (!BloodAccounting.canPayInFull(player, ml, SUMMON)) return OptionalDouble.of(0.0D);
        BloodAccounting.pay(player, ml, SUMMON);
        Vec3 point = new Vec3(at.x, surface.getY() + level.getFluidState(surface).getHeight(level, surface), at.z);
        PhlegethosomaSummons data = PhlegethosomaSummons.get(level);
        double counted = data.pour(level, point, ml);
        level.sendParticles(HemoParticleData.bloodCell(ICHOR_BRIGHT), point.x, point.y - 0.05D, point.z, 2, 0.15D, 0.02D, 0.15D, 0.01D);
        if (level.getGameTime() % 40L == 0L) {
            ManipulationVisuals.burst(level, ManipulationVisuals.Form.CHUM_BLOOM, point, point,
                    PhlegethosomaSummonRules.POOL_REACH, 50, (int) Math.round(100.0D * counted / PhlegethosomaSummonRules.SUMMON_ML));
        }
        if (level.getGameTime() % 10L == 0L) {
            level.playSound(null, surface, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.PLAYERS, 0.3F, 0.45F);
        }
        if (PhlegethosomaSummonRules.reached(counted)) {
            data.clear(level, point);
            summon(level, player, surface, point);
        }
        return OptionalDouble.of(ml);
    }

    @Nullable
    public static Refusal refusal(ServerLevel level, ServerPlayer player, BlockPos surface) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return Refusal.PEACEFUL;
        if (!PhlegethosomaSpawnRules.validPool(level, surface)) return Refusal.NARROW;
        if (current(level, player.getUUID()) != null) return Refusal.YOURS;
        if (!level.getEntitiesOfClass(PhlegethosomaEntity.class, new AABB(surface).inflate(PhlegethosomaSummonRules.SPACING),
                boss -> boss.isAlive() && !boss.departing()).isEmpty()) return Refusal.OCCUPIED;
        return null;
    }

    /** The player's summoned Phlegethosoma while it is loaded and has not dived, or null. */
    @Nullable
    public static PhlegethosomaEntity current(ServerLevel level, UUID player) {
        UUID boss = PhlegethosomaSummons.get(level).summonedBy(player);
        if (boss == null) return null;
        Entity entity = level.getEntity(boss);
        return entity instanceof PhlegethosomaEntity fluke && fluke.isAlive() && !fluke.departing() ? fluke : null;
    }

    @Nullable
    private static PhlegethosomaEntity summon(ServerLevel level, ServerPlayer player, BlockPos surface, Vec3 point) {
        BlockPos at = level.getFluidState(surface.below()).is(PhlegethonticTags.ICHOR) ? surface.below() : surface;
        PhlegethosomaEntity boss = EntityInit.phlegethosoma.get().create(level);
        if (boss == null) return null;
        boss.moveTo(Vec3.atBottomCenterOf(at).x, at.getY(), Vec3.atBottomCenterOf(at).z, player.getYRot() + 180.0F, 0.0F);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(at), MobSpawnType.MOB_SUMMONED, null);
        boss.answerSummons(player, surface);
        level.addFreshEntity(boss);
        PhlegethosomaSummons.get(level).record(player.getUUID(), boss.getUUID());
        ManipulationVisuals.burst(level, ManipulationVisuals.Form.CHUM_BLOOM, point, point, 4.5D, 80, 100);
        level.sendParticles(HemoParticleData.bloodCell(ICHOR_BLOOM), point.x, point.y, point.z, 24, 1.2D, 0.1D, 1.2D, 0.04D);
        level.playSound(null, surface, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundSource.HOSTILE, 1.4F, 0.4F);
        player.displayClientMessage(Component.translatable("message.hemomancy.phlegethosoma.summon.answered")
                .withStyle(ChatFormatting.DARK_RED), true);
        return boss;
    }

    /** The top ichor block over {@code pos}. */
    private static BlockPos surface(ServerLevel level, BlockPos pos) {
        BlockPos top = pos;
        while (level.getFluidState(top.above()).is(PhlegethonticTags.ICHOR) && top.getY() < pos.getY() + 16) top = top.above();
        return top;
    }
}
