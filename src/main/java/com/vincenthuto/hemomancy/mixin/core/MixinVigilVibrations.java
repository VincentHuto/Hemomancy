package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.antecedent.VigilSites;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=VibrationSystem.Listener.class,remap=false)
public abstract class MixinVigilVibrations {
    @Shadow @Final private VibrationSystem system;
    @Inject(method="handleGameEvent",at=@At("HEAD"),cancellable=true)
    private void hemomancy$fixture(ServerLevel level,Holder<GameEvent> event,GameEvent.Context context,Vec3 origin,CallbackInfoReturnable<Boolean> callback) {
        var position=system.getVibrationUser().getPositionSource().getPosition(level);
        if(position.isPresent() && VigilSites.authored(level,BlockPos.containing(position.get()))) callback.setReturnValue(false);
    }
}
