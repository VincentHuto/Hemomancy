package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class MixinChamberInventoryDrops {
    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true, remap = false)
    private void hemomancy$rejectObservationalDrop(int slot, int button, ClickType type,
            Player player, CallbackInfo ci) {
        boolean outsideDrop = slot == -999 && (type == ClickType.PICKUP || type == ClickType.QUICK_MOVE);
        if (player instanceof ServerPlayer serverPlayer && ChamberVisitService.isObservational(serverPlayer)
                && (type == ClickType.THROW || outsideDrop)) {
            // Reject before removal: a full inventory cannot recover an armor or cursor drop.
            ((AbstractContainerMenu) (Object) this).broadcastFullState();
            ci.cancel();
        }
    }
}
