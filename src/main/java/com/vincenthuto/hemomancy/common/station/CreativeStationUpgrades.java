package com.vincenthuto.hemomancy.common.station;

import com.vincenthuto.hemomancy.common.block.harbinger.crafting.GhastlyAlembicBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Creative kit use changes only the installed stage, leaving personal progression and rites alone. */
public final class CreativeStationUpgrades {
    private CreativeStationUpgrades() {}

    public static boolean tryApply(ItemStack stack, Level level, BlockPos pos, Player player) {
        if (!player.isCreative() || stack.isEmpty()
                || !(level.getBlockEntity(pos) instanceof UpgradeableStation station)) return false;
        var itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        var upgrade = StationUpgradeCatalog.all().stream()
                .filter(tier -> tier.station() == station.upgradeStation() && tier.upgradeItem().equals(itemId))
                .findFirst();
        if (upgrade.isEmpty()) return false;
        int target = upgrade.get().tier();
        if (!level.isClientSide && target > station.upgradeTier()) {
            var state = station.getBlockState().setValue(StationTierProperty.STAGE, target);
            level.setBlock(pos, state, 3);
            if (state.getBlock() instanceof GhastlyAlembicBlock alembic) alembic.placeFillers(level, pos, state);
            level.getBlockEntity(pos).setChanged();
        }
        return true;
    }
}
