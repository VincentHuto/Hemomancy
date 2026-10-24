package com.vincenthuto.hemomancy.common.capability.player.harbinger.station;

import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.HemomancyDiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeTier;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Per-player machine use, upgrade-item claims and undelivered claims for every upgradeable station. */
public final class StationUpgradeProgress implements INBTSerializable<CompoundTag> {
    private final EnumMap<UpgradeStation, Set<String>> used = new EnumMap<>(UpgradeStation.class);
    private final EnumMap<UpgradeStation, Integer> claimed = new EnumMap<>(UpgradeStation.class);
    private final EnumMap<UpgradeStation, Integer> pending = new EnumMap<>(UpgradeStation.class);
    private boolean legacyMigrated;
    /** Changed since the owner last received it. Never saved. */
    private transient boolean syncDirty;

    private static int bit(int tier) { return 1 << (tier - 1); }

    public void recordUse(UpgradeStation station, String usage) {
        if (used.computeIfAbsent(station, ignored -> new HashSet<>()).add(usage)) syncDirty = true;
    }

    public boolean hasUsed(UpgradeStation station, String usage) {
        return used.getOrDefault(station, Set.of()).contains(usage);
    }

    public boolean hasClaimed(UpgradeStation station, int tier) {
        return (claimed.getOrDefault(station, 0) & bit(tier)) != 0;
    }

    public boolean hasPending(UpgradeStation station, int tier) {
        return (pending.getOrDefault(station, 0) & bit(tier)) != 0;
    }

    public boolean legacyMigrated() { return legacyMigrated; }
    void setLegacyMigrated() {
        if (!legacyMigrated) syncDirty = true;
        legacyMigrated = true;
    }

    void markClaimed(UpgradeStation station, int tier, boolean deliverItem) {
        if (!hasClaimed(station, tier) || deliverItem && !hasPending(station, tier)) syncDirty = true;
        claimed.merge(station, bit(tier), (a, b) -> a | b);
        if (deliverItem) pending.merge(station, bit(tier), (a, b) -> a | b);
    }

    public StationUpgradeRules.ClaimState claimState(ServerPlayer player, StationUpgradeTier tier) {
        boolean usageComplete = usageComplete(player, tier);
        boolean activeBlood = HemoCapabilityAccess.getBloodVolume(player).isActive();
        return StationUpgradeRules.claimState(HemoCapabilityAccess.getPlayerDegreeNumber(player), activeBlood,
                hasClaimed(tier.station(), tier.tier()),
                tier.tier() == 1 || eligible(player, StationUpgradeCatalog.get(tier.station(), tier.tier() - 1)),
                usageComplete, tier.requiredDegree());
    }

    private boolean usageComplete(ServerPlayer player, StationUpgradeTier tier) {
        return tier.requiredUsage().stream().allMatch(usage -> hasUsed(tier.station(), usage)
                || tier.station() == UpgradeStation.CENTRIFUGE && usage.equals(StationUpgradeCatalog.SEPARATE)
                && com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.isFirstSeparationComplete(player));
    }

    /** Eligibility is personal and independent of whether the player accepted the free item. */
    public boolean eligible(ServerPlayer player, StationUpgradeTier tier) {
        return HemoCapabilityAccess.getPlayerDegreeNumber(player) >= tier.requiredDegree()
                && HemoCapabilityAccess.getBloodVolume(player).isActive()
                && usageComplete(player, tier)
                && (tier.tier() == 1 || eligible(player, StationUpgradeCatalog.get(tier.station(), tier.tier() - 1)));
    }

    /** Unlocks the Liber lessons of every tier the player is eligible for. */
    public void unlockEligibleLessons(ServerPlayer player) {
        Set<net.minecraft.resources.ResourceLocation> lessons = new java.util.LinkedHashSet<>();
        for (StationUpgradeTier tier : StationUpgradeCatalog.all()) {
            if (eligible(player, tier)) lessons.addAll(LiberEntryDefinitions.forRite(tier.ritePath()));
        }
        LiberKnowledgeHelper.unlockEntries(player, lessons, HemomancyDiscoverySource.PRACTICE);
    }

    public boolean isSyncDirty() {
        return syncDirty;
    }

    /** Unlocks earned lessons, then sends the state if it changed since the last send, or always when forced. */
    public void sync(ServerPlayer player, boolean force) {
        unlockEligibleLessons(player);
        if (!force && !syncDirty) return;
        PacketHandler.sendToPlayer(player, new com.vincenthuto.hemomancy.common.network.capa.harbinger
                .PacketSyncStationUpgrades(serializeNBT(player.registryAccess())));
        syncDirty = false;
    }

    /** Gives the upgrade item once, unlocks its repeat-craft recipe, and retries delivery if the inventory is full. */
    public boolean claim(ServerPlayer player, StationUpgradeTier tier) {
        if (claimState(player, tier) != StationUpgradeRules.ClaimState.READY) return false;
        markClaimed(tier.station(), tier.tier(), true);
        awardRecipe(player, tier);
        deliver(player);
        sync(player, true);
        return true;
    }

    public void deliver(ServerPlayer player) {
        for (StationUpgradeTier tier : StationUpgradeCatalog.all()) {
            if (!hasPending(tier.station(), tier.tier())) continue;
            ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(tier.upgradeItem()));
            player.getInventory().add(stack);
            if (stack.isEmpty()) {
                pending.merge(tier.station(), bit(tier.tier()), (a, b) -> a & ~b);
                syncDirty = true;
            }
        }
    }

    static void awardRecipe(ServerPlayer player, StationUpgradeTier tier) {
        player.serverLevel().getRecipeManager().byKey(tier.craftingRecipe())
                .ifPresent(recipe -> player.awardRecipes(List.of(recipe)));
    }

    @Override public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        for (UpgradeStation station : UpgradeStation.values()) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("Claimed", claimed.getOrDefault(station, 0));
            entry.putInt("Pending", pending.getOrDefault(station, 0));
            ListTag usage = new ListTag();
            used.getOrDefault(station, Set.of()).forEach(key -> usage.add(StringTag.valueOf(key)));
            entry.put("Used", usage);
            tag.put(station.serializedName(), entry);
        }
        tag.putBoolean("LegacyMigrated", legacyMigrated);
        return tag;
    }

    @Override public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        used.clear();
        claimed.clear();
        pending.clear();
        for (UpgradeStation station : UpgradeStation.values()) {
            if (!tag.contains(station.serializedName(), Tag.TAG_COMPOUND)) continue;
            CompoundTag entry = tag.getCompound(station.serializedName());
            claimed.put(station, entry.getInt("Claimed"));
            pending.put(station, entry.getInt("Pending"));
            ListTag usage = entry.getList("Used", Tag.TAG_STRING);
            for (int i = 0; i < usage.size(); i++) recordUse(station, usage.getString(i));
        }
        legacyMigrated = tag.getBoolean("LegacyMigrated");
        syncDirty = true;
    }
}
