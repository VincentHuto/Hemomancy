package com.vincenthuto.hemomancy.client.sound;

import com.vincenthuto.hemomancy.client.player.HematicMicroscopeClientState;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

public final class MicroscopeExaminationSound extends AbstractTickableSoundInstance {
    private final LivingEntity player;
    private final HematicMicroscopeClientState.Animation animation;
    public MicroscopeExaminationSound(LivingEntity player, HematicMicroscopeClientState.Animation animation) {
        super(SoundInit.MICROSCOPE_EXAMINE_LOOP.get(), SoundSource.PLAYERS, player.getRandom());
        this.player = player;
        this.animation = animation;
        looping = true;
        delay = 0;
        volume = .25F;
        pitch = 1;
        updatePosition();
    }
    private void updatePosition() { x = player.getX(); y = player.getEyeY(); z = player.getZ(); }
    public void finish() { stop(); }
    @Override public void tick() {
        if (!player.isUsingItem() || HematicMicroscopeClientState.animation(player) != animation
                || !animation.playback.shouldLoop(player.level().getGameTime())) stop();
        else updatePosition();
    }
}
