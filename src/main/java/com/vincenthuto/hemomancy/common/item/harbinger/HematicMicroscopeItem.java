package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public final class HematicMicroscopeItem extends Item {
    public static final int EXAMINATION_TICKS = 40;
    public static final int USE_DURATION = 72000;

    public HematicMicroscopeItem(Properties properties) { super(properties.stacksTo(1)); }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return USE_DURATION; }
    // Custom two-hand rendering provides the viewing pose without spyglass FOV zoom.
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.NONE; }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var instrument = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !player.isAlive() || player.isSpectator()) return InteractionResultHolder.fail(instrument);
        var sample = player.getOffhandItem();
        if (!BloodSampleData.isSpecimenVessel(sample) || !BloodSampleData.isFilled(sample) || sample.getCount() != 1) {
            feedback(player, sample.isEmpty() ? "offhand" : "empty");
            return InteractionResultHolder.fail(instrument);
        }
        if (!level.isClientSide && sample.getItem() instanceof BloodVialItem && com.vincenthuto.hemomancy.common.antecedent.AhaematicSample.is(sample)) {
            sample = com.vincenthuto.hemomancy.common.antecedent.AhaematicSample.migrate(sample);
            player.setItemInHand(InteractionHand.OFF_HAND, sample);
        }
        player.startUsingItem(hand);
        if (!level.isClientSide) {
            MicroscopeExamination.begin(player, instrument, sample);
            level.playSound(null, player.blockPosition(), SoundInit.MICROSCOPE_USE.get(), SoundSource.PLAYERS, 0.3F, 1F);
            if (!BloodSampleData.examinable(sample)) feedback(player, "provenance");
        }
        return InteractionResultHolder.consume(instrument);
    }

    @Override public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (!level.isClientSide && entity instanceof Player player) MicroscopeExamination.tick(player, remaining);
    }
    @Override public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (!level.isClientSide && entity instanceof Player player) MicroscopeExamination.release(player);
    }
    private static void feedback(Player player, String key) {
        if (!player.level().isClientSide) player.displayClientMessage(Component.translatable("message.hemomancy.microscope." + key), true);
    }
}
