package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.client.screen.dialogue.DialogueLineEditor;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/** Temporary dev tool: lets the in-game dialogue line editor see which keys were drawn. */
@Mixin(value = TranslatableContents.class, remap = false)
public abstract class MixinDialogueLineRecorder {
	@Shadow
	@Final
	private String key;

	@Inject(method = "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;", at = @At("HEAD"))
	private <T> void hemomancy$recordStyled(FormattedText.StyledContentConsumer<T> consumer, Style style,
			CallbackInfoReturnable<Optional<T>> cir) {
		DialogueLineEditor.record(key);
	}

	@Inject(method = "visit(Lnet/minecraft/network/chat/FormattedText$ContentConsumer;)Ljava/util/Optional;", at = @At("HEAD"))
	private <T> void hemomancy$recordPlain(FormattedText.ContentConsumer<T> consumer,
			CallbackInfoReturnable<Optional<T>> cir) {
		DialogueLineEditor.record(key);
	}
}
