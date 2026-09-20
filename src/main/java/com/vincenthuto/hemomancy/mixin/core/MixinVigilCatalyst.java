package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.antecedent.VigilSites;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import net.minecraft.world.level.gameevent.*;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=SculkCatalystBlockEntity.CatalystListener.class,remap=false)
public abstract class MixinVigilCatalyst {
    @Inject(method="handleGameEvent",at=@At("HEAD"),cancellable=true)
    private void hemomancy$boundedGrowth(ServerLevel level,Holder<GameEvent> event,GameEvent.Context context,Vec3 position,CallbackInfoReturnable<Boolean> callback) {
        var source=((GameEventListener)(Object)this).getListenerSource().getPosition(level);
        var site=source.isEmpty()?null:VigilSites.fixture(level,BlockPos.containing(source.get()));
        if(site==null) {
            if(source.isPresent() && VigilSites.authored(level,BlockPos.containing(source.get())))callback.setReturnValue(false);
            return;
        }
        if(event.is(GameEvent.ENTITY_DIE) && context.sourceEntity() instanceof LivingEntity entity && !entity.wasExperienceConsumed()) {
            var damage=entity.getLastDamageSource();
            int xp=entity.shouldDropExperience()?entity.getExperienceReward(level,damage==null?null:damage.getEntity()):0;
            entity.skipDropExperience();site.nourish(xp);callback.setReturnValue(true);
        } else callback.setReturnValue(false);
    }
}
