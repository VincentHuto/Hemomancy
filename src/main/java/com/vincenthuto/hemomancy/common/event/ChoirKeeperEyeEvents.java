package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ChoirKeeperEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ChoirKeeperEyeEvents {
    private ChoirKeeperEyeEvents() {}

    @SubscribeEvent
    public static void onEyeUse(PlayerInteractEvent.RightClickItem event) {
        if (!event.getItemStack().is(Items.ENDER_EYE)
                || !event.getLevel().dimension().equals(Level.END)) return;
        var player = event.getEntity();
        boolean keeperNearby = !event.getLevel().getEntitiesOfClass(ChoirKeeperEntity.class,
                player.getBoundingBox().inflate(24.0D)).isEmpty();
        if (!keeperNearby) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (event.getLevel().isClientSide) return;

        Vec3 eyePosition = player.getEyePosition().add(player.getLookAngle().scale(0.6D));
        ItemEntity eye = new ItemEntity(event.getLevel(), eyePosition.x, eyePosition.y, eyePosition.z,
                new ItemStack(Items.ENDER_EYE));
        eye.setDeltaMovement(player.getLookAngle().scale(0.34D).add(0.0D, 0.12D, 0.0D));
        eye.setPickUpDelay(80);
        event.getLevel().addFreshEntity(eye);
        if (!player.getAbilities().instabuild) event.getItemStack().shrink(1);
        player.getCooldowns().addCooldown(Items.ENDER_EYE, 12);
        event.getLevel().playSound(null, player.blockPosition(), SoundEvents.ENDER_EYE_LAUNCH,
                SoundSource.PLAYERS, 0.6F, 1.1F);
    }
}
