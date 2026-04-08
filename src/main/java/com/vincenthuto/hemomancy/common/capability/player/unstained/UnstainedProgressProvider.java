package com.vincenthuto.hemomancy.common.capability.player.unstained;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class UnstainedProgressProvider implements ICapabilitySerializable<Tag> {

    public static final Capability<IUnstainedProgress> UNSTAINED_CAPA =
            CapabilityManager.get(new CapabilityToken<IUnstainedProgress>() {});

    public static IUnstainedProgress getProgress(Player player) {
        return player.getCapability(UNSTAINED_CAPA)
                .orElseThrow(IllegalStateException::new);
    }

    private LazyOptional<IUnstainedProgress> instance = LazyOptional.of(UnstainedProgress::new);


    @Override
    public void deserializeNBT(Tag nbt) {
        readNBT(instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), nbt);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return UNSTAINED_CAPA.orEmpty(cap, instance);
    }

    @Override
    public Tag serializeNBT() {
        return writeNBT(instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")));
    }

    private CompoundTag writeNBT(IUnstainedProgress inst) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("begunPurification", inst.hasBegunPurification());
        tag.putFloat("purity", inst.getPurity());
        tag.putBoolean("clarityUnlocked", inst.hasClarityUnlocked());
        tag.putFloat("clarity", inst.getClarity());
        tag.putLong("lastManipulationTick", inst.getLastManipulationTick());
        // Milestone counters
        tag.putInt("hemoMobKills", inst.getHemoMobKills());
        tag.putInt("undeadKills", inst.getUndeadKills());
        tag.putInt("hostileKills", inst.getHostileKills());
        tag.putInt("flawlessKills", inst.getFlawlessKills());
        tag.putInt("animalsBreed", inst.getAnimalsBreed());
        tag.putInt("cropsPlanted", inst.getCropsPlanted());
        tag.putInt("advancementsEarned", inst.getAdvancementsEarned());
        tag.putInt("nightsSlept", inst.getNightsSlept());
        tag.putInt("petsHealed", inst.getPetsHealed());
        // One-time flags
        tag.putBoolean("sleptWithHemolysis", inst.hasSleptWithHemolysis());
        tag.putBoolean("killedFirstHemoMob", inst.hasKilledFirstHemoMob());
        tag.putBoolean("reachedAbstinence", inst.hasReachedAbstinence());
        tag.putBoolean("emptiedBlood", inst.hasEmptiedBlood());
        tag.putBoolean("earnedAdvancement", inst.hasEarnedAdvancement());
        return tag;
    }

    private void readNBT(IUnstainedProgress inst, Tag nbt) {
        if (nbt instanceof CompoundTag tag) {
            inst.setBegunPurification(tag.getBoolean("begunPurification"));
            inst.setPurity(tag.getFloat("purity"));
            inst.setClarityUnlocked(tag.getBoolean("clarityUnlocked"));
            inst.setClarity(tag.getFloat("clarity"));
            inst.setLastManipulationTick(tag.getLong("lastManipulationTick"));
            // Milestone counters — replay increments from saved counts
            for (int i = 0; i < tag.getInt("hemoMobKills"); i++) inst.addHemoMobKill();
            for (int i = 0; i < tag.getInt("undeadKills"); i++) inst.addUndeadKill();
            for (int i = 0; i < tag.getInt("hostileKills"); i++) inst.addHostileKill();
            for (int i = 0; i < tag.getInt("flawlessKills"); i++) inst.addFlawlessKill();
            for (int i = 0; i < tag.getInt("animalsBreed"); i++) inst.addAnimalBreed();
            for (int i = 0; i < tag.getInt("cropsPlanted"); i++) inst.addCropPlanted();
            for (int i = 0; i < tag.getInt("advancementsEarned"); i++) inst.addAdvancementEarned();
            for (int i = 0; i < tag.getInt("nightsSlept"); i++) inst.addNightSlept();
            for (int i = 0; i < tag.getInt("petsHealed"); i++) inst.addPetHealed();
            // One-time flags
            inst.setSleptWithHemolysis(tag.getBoolean("sleptWithHemolysis"));
            inst.setKilledFirstHemoMob(tag.getBoolean("killedFirstHemoMob"));
            inst.setReachedAbstinence(tag.getBoolean("reachedAbstinence"));
            inst.setEmptiedBlood(tag.getBoolean("emptiedBlood"));
            inst.setEarnedAdvancement(tag.getBoolean("earnedAdvancement"));
        }
    }
}
