package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.HashSet;
import java.util.Set;

/** Observers receive explicit cue removal; their local entities do not carry full effect maps. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID)
public final class ManipulationStatusVisuals {
    private record Pending(LivingEntity entity,Holder<MobEffect> effect,ManipulationVisuals.Form form) {}
    private static final Set<Pending> PENDING=new HashSet<>();
    private ManipulationStatusVisuals() {}

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void removed(MobEffectEvent.Remove event) {queue(event.getEntity(),event.getEffect());}

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void expired(MobEffectEvent.Expired event) {
        if(event.getEffectInstance()!=null)queue(event.getEntity(),event.getEffectInstance().getEffect());
    }

    private static void queue(LivingEntity entity,Holder<MobEffect> effect) {
        if(!(entity.level() instanceof ServerLevel))return;
        var value=effect.value();
        ManipulationVisuals.Form form=value==EffectInit.conductive_mark.get()?ManipulationVisuals.Form.MARK:
                value==EffectInit.paralysis.get()?ManipulationVisuals.Form.PARALYSIS:
                value==EffectInit.iron_retort.get()?ManipulationVisuals.Form.RETORT:
                value==EffectInit.insatiable_hunger.get()?ManipulationVisuals.Form.HUNGER:
                value==EffectInit.grave_debt.get()?ManipulationVisuals.Form.GRAVE:
                value==EffectInit.blood_loss.get()?ManipulationVisuals.Form.WOUND:null;
        if(form!=null)PENDING.add(new Pending(entity,effect,form));
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post event) {
        // Verify after removal/cure has completed, including cancellation or same-tick reapplication.
        for(var pending:PENDING)
            if(!pending.entity.hasEffect(pending.effect))
                ManipulationVisuals.attached(pending.entity,pending.form,1,0,0);
        PENDING.clear();
    }

    @SubscribeEvent public static void stopped(ServerStoppedEvent event) {PENDING.clear();}
}
