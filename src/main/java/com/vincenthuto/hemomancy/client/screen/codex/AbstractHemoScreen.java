package com.vincenthuto.hemomancy.client.screen.codex;

import java.util.function.Supplier;

import com.vincenthuto.hutoslib.common.data.skilltree.SkillTreeCodeModel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class AbstractHemoScreen extends Screen {

	public SkillTreeCodeModel model;

	protected AbstractHemoScreen(SkillTreeCodeModel model, Component pTitle) {
		super(pTitle);
		this.model = model;
	}

	public boolean isHovering(double mouseX, double mouseY, int posX, int posY, int width, int height) {
		return false;

	}

	public Supplier<SoundEvent> getSweetenerSound() {
		return () -> SoundEvents.GENERIC_BIG_FALL;

	}

	public void playPageFlipSound(Supplier<SoundEvent> soundEvent, float pitch) {
		playSound(soundEvent, 1f, Math.max(1, pitch * 0.8f));
		playSound(getSweetenerSound(), 1f, pitch);
	}

	public void playSweetenedSound(Supplier<SoundEvent> soundEvent, float sweetenerPitch) {
		playSound(soundEvent, 1f, 1);
		playSound(getSweetenerSound(), 1f, sweetenerPitch);
	}

	public void playSound(Supplier<SoundEvent> soundEvent, float volume, float pitch) {
		Player playerEntity = Minecraft.getInstance().player;
		playerEntity.playNotifySound(soundEvent.get(), SoundSource.PLAYERS, volume, pitch);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (minecraft.options.keyInventory.matches(keyCode, scanCode)) {
			onClose();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	public SkillTreeCodeModel getModel() {
		return model;
	}
}