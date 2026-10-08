package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;

/** Pale, haemoglobin-free cold-water fish; shared hemolymph is its current reagent. */
public final class IceFishEntity extends AbstractSchoolingFish {
    public static final TagKey<Biome> SPAWN_BIOMES = TagKey.create(Registries.BIOME,
            Hemomancy.rloc("ice_fish_spawnlist"));

    public IceFishEntity(EntityType<? extends IceFishEntity> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder setAttributes() {
        return createAttributes().add(Attributes.MAX_HEALTH, 6).add(Attributes.MOVEMENT_SPEED, .85);
    }

    public static boolean canSpawnHere(EntityType<? extends IceFishEntity> type, LevelAccessor level,
                                       MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getBiome(pos).is(SPAWN_BIOMES)
                && PelagicHabitat.waterClearance(level, pos, 0, 1, 0)
                && level.noCollision(type.getSpawnAABB(pos.getX()+.5, pos.getY(), pos.getZ()+.5));
    }

    @Override public int getMaxSchoolSize() { return 6; }
    @Override public ItemStack getBucketItemStack() { return ItemStack.EMPTY; }
    @Override protected SoundEvent getFlopSound() { return SoundEvents.COD_FLOP; }

    @Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        // Like Pelagic Herring, capture uses Specimen Jars rather than an empty bucket result.
        if (player.getItemInHand(hand).is(Items.WATER_BUCKET)) return InteractionResult.PASS;
        return super.mobInteract(player, hand);
    }
}
