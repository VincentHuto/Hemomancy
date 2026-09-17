package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.Hemomancy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.common.util.TriState;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hutoslib.common.item.ItemKnapper;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CabinetProtectionEvents {
    private CabinetProtectionEvents() {}
    @SubscribeEvent public static void routeCabinetUse(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().getBlockState(event.getPos()).getBlock() instanceof PhlebotomistsFieldCaseBlock
                && !event.getUseBlock().isFalse() && !event.getEntity().isSpectator()) {
            if (event.getItemStack().isEmpty() || event.getItemStack().getItem() instanceof BloodVialItem)
                event.setUseBlock(TriState.TRUE);
            return;
        }
        if (!event.getEntity().isShiftKeyDown() || event.getEntity().isSpectator()
                || event.getUseBlock().isFalse()
                || !(event.getLevel().getBlockState(event.getPos()).getBlock() instanceof PhlebotomistsCabinetBlock)) return;
        var stack = event.getItemStack();
        if (stack.getItem() instanceof BloodVialItem || stack.is(PhlebotomistsCabinetBlock.GLAZING)
                || stack.canPerformAction(ItemKnapper.KNAPPER_DIG)) event.setUseBlock(TriState.TRUE);
    }
    @SubscribeEvent public static void breakCabinet(BlockEvent.BreakEvent event) {
        if (!event.getPlayer().getAbilities().instabuild && event.getState().getBlock() instanceof PhlebotomistsCabinetBlock
                && PhlebotomistsCabinetBlock.hasSpecimens(event.getLevel(), event.getPos())) {
            event.setCanceled(true);
            PhlebotomistsCabinetBlock.warn(event.getPlayer());
        }
    }
    @SubscribeEvent public static void explosion(ExplosionEvent.Detonate event) {
        event.getAffectedBlocks().removeIf(pos -> event.getLevel().getBlockState(pos).getBlock() instanceof PhlebotomistsCabinetBlock
                && PhlebotomistsCabinetBlock.hasSpecimens(event.getLevel(), pos));
    }
}
