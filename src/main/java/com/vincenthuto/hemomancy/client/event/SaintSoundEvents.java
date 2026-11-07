package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.registry.SaintClientStyles;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.SaintMeterClientState;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.SaintMeterContext;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.SaintMeterLayer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Applies the fought Saint's {@code SaintMeterStyle.muffle} (Velorum's darkness) to sounds the Saint did not make: a
 * sound within {@link #OWN_REACH} of its body is its own, and music, the interface and relative sounds are left alone.
 * Ticking sounds keep their own volume, since wrapping them would stop them ticking.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class SaintSoundEvents {
	private static final double OWN_REACH = 3.0D;

	private SaintSoundEvents() {
	}

	@SubscribeEvent
	public static void onPlaySound(PlaySoundEvent event) {
		SoundInstance sound = event.getSound();
		if (sound == null || sound instanceof TickableSoundInstance || sound.isRelative()) return;
		if (sound.getSource() == SoundSource.MUSIC || sound.getSource() == SoundSource.MASTER) return;
		EnumSaintType type = SaintMeterClientState.type();
		SaintMeterContext context = type == null ? null : SaintMeterLayer.queryContext();
		if (context == null) return;
		SaintTrialEntity saint = context.saint();
		if (saint != null && saint.distanceToSqr(sound.getX(), sound.getY(), sound.getZ()) <= OWN_REACH * OWN_REACH) return;
		float factor = SaintClientStyles.meter(type).muffle(context, sound);
		if (factor >= 0.999F) return;
		event.setSound(factor <= 0.0F ? null : new Muffled(sound, factor));
	}

	/** A sound played at a share of its volume. */
	private record Muffled(SoundInstance sound, float factor) implements SoundInstance {
		@Override
		public ResourceLocation getLocation() {
			return sound.getLocation();
		}

		@Override
		public WeighedSoundEvents resolve(SoundManager manager) {
			return sound.resolve(manager);
		}

		@Override
		public Sound getSound() {
			return sound.getSound();
		}

		@Override
		public SoundSource getSource() {
			return sound.getSource();
		}

		@Override
		public boolean isLooping() {
			return sound.isLooping();
		}

		@Override
		public boolean isRelative() {
			return sound.isRelative();
		}

		@Override
		public int getDelay() {
			return sound.getDelay();
		}

		@Override
		public float getVolume() {
			return sound.getVolume() * factor;
		}

		@Override
		public float getPitch() {
			return sound.getPitch();
		}

		@Override
		public double getX() {
			return sound.getX();
		}

		@Override
		public double getY() {
			return sound.getY();
		}

		@Override
		public double getZ() {
			return sound.getZ();
		}

		@Override
		public Attenuation getAttenuation() {
			return sound.getAttenuation();
		}

		@Override
		public boolean canStartSilent() {
			return sound.canStartSilent();
		}

		@Override
		public boolean canPlaySound() {
			return sound.canPlaySound();
		}

		@Override
		public CompletableFuture<AudioStream> getStream(SoundBufferLibrary buffers, Sound sound, boolean looping) {
			return this.sound.getStream(buffers, sound, looping);
		}
	}
}
