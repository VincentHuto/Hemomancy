package com.vincenthuto.hemomancy.common.station;

import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.server.level.ServerPlayer;

/** Folds the per-station claim records of pre-consolidation saves into StationUpgradeProgress, once. */
public final class StationUpgradeMigration {
    static final String LEGACY_CONSECRATION_KIT_KEY = "hemomancy.vicar_consecration_kit_claimed";
    static final String LEGACY_CORNERSTONE_KEY = "hemomancy.monolithic_cornerstone_claimed";

    private StationUpgradeMigration() {}

    public static void migrate(ServerPlayer player) {
        StationUpgradeProgress progress = HemoCapabilityAccess.stationUpgrades(player);
        if (progress.legacyMigrated()) return;
        if (player.hasData(HemoAttachmentTypes.ADVANCED_BREWING)) {
            var brewing = player.getData(HemoAttachmentTypes.ADVANCED_BREWING);
            if (brewing.distilled()) progress.recordUse(UpgradeStation.ALEMBIC, StationUpgradeCatalog.DISTILL);
            if (brewing.refined()) progress.recordUse(UpgradeStation.ALEMBIC, StationUpgradeCatalog.REFINE);
            if (brewing.compounded()) progress.recordUse(UpgradeStation.ALEMBIC, StationUpgradeCatalog.COMPOUND);
            if (brewing.condenserClaimed()) progress.markClaimed(UpgradeStation.ALEMBIC, 1, brewing.condenserPending());
            if (brewing.athanorClaimed()) progress.markClaimed(UpgradeStation.ALEMBIC, 2, brewing.athanorPending());
            player.removeData(HemoAttachmentTypes.ADVANCED_BREWING);
        }
        if (player.hasData(HemoAttachmentTypes.RESONANT_FORGE)) {
            var forge = player.getData(HemoAttachmentTypes.RESONANT_FORGE);
            if (forge.precisionClaimed()) progress.markClaimed(UpgradeStation.RESONANT_FORGE, 1, forge.precisionPending());
            if (forge.masterClaimed()) progress.markClaimed(UpgradeStation.RESONANT_FORGE, 2, forge.masterPending());
            player.removeData(HemoAttachmentTypes.RESONANT_FORGE);
        }
        var persistent = player.getPersistentData();
        if (persistent.getBoolean(LEGACY_CONSECRATION_KIT_KEY)) progress.markClaimed(UpgradeStation.ARMATURE, 1, false);
        if (persistent.getBoolean(LEGACY_CORNERSTONE_KEY)) progress.markClaimed(UpgradeStation.ARMATURE, 2, false);
        persistent.remove(LEGACY_CONSECRATION_KIT_KEY);
        persistent.remove(LEGACY_CORNERSTONE_KEY);
        for (StationUpgradeTier tier : StationUpgradeCatalog.all())
            if (progress.hasClaimed(tier.station(), tier.tier())) StationUpgradeProgress.awardRecipe(player, tier);
        progress.setLegacyMigrated();
    }
}
