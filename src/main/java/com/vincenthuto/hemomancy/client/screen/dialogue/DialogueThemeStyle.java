package com.vincenthuto.hemomancy.client.screen.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueCategory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTheme;

import net.minecraft.resources.ResourceLocation;

public record DialogueThemeStyle(ResourceLocation frameSprite, ResourceLocation portraitFrameSprite,
		ResourceLocation cardSprite, ResourceLocation cardSelectedSprite, ResourceLocation cardDisabledSprite,
		ResourceLocation buttonSprite, ResourceLocation buttonSelectedSprite, ResourceLocation buttonDisabledSprite,
		int speakerColor, int textColor, int optionColor, int disabledColor, int separatorColor,
		int progressBackColor, int progressColor, int scrollTrackColor, int scrollThumbColor) {
	/** One flat, vanilla-style sprite set per theme (tools/oneoff/dialogue_flat_sprites.py). */
	public static DialogueThemeStyle forTheme(DialogueTheme theme) {
		String name = switch (theme) {
			case BLOOD -> "blood";
			case UNSTAINED -> "unstained";
			case FUNGAL -> "fungal";
			case STILL, STILL_MATERIAL -> "still";
		};
		return switch (theme) {
			case UNSTAINED -> create(name, 0xFFD9E3F4, 0xFFC4C8D4, 0xFFAABBE0, 0xFF687080,
					0x665E7098, 0xFF242936, 0xFF8EA9E8, 0x663A435A, 0xFFDDE8FF);
			case FUNGAL -> create(name, 0xFFE0A95C, 0xFFD0B99E, 0xFFE2A050, 0xFF75624E,
					0x668D5B28, 0xFF2B1A0E, 0xFFD17B32, 0x66472A15, 0xFFFFC66D);
			case STILL, STILL_MATERIAL -> create(name, 0xFFD7F2F1, 0xFFC7D9DC, 0xFFB9E2E5, 0xFF65777B,
					0x66758F94, 0xFF101B20, 0xFF75B9C0, 0x6632474C, 0xFFE2FAF8);
			default -> create(name, 0xFFE35A68, 0xFFC8B8B8, 0xFFE7A66F, 0xFF765F62,
					0x668B2735, 0xFF26090D, 0xFFB93447, 0x6643151D, 0xFFFFC07E);
		};
	}

	private static DialogueThemeStyle create(String theme, int speaker, int text, int option, int disabled,
			int separator, int progressBack, int progress, int track, int thumb) {
		String root = "dialogue/" + theme + "/";
		return new DialogueThemeStyle(sprite(root + "frame"), sprite(root + "portrait_frame"),
				sprite(root + "card"), sprite(root + "card_selected"), sprite(root + "card_disabled"),
				sprite(root + "button"), sprite(root + "button_selected"), sprite(root + "button_disabled"),
				speaker, text, option, disabled, separator, progressBack, progress, track, thumb);
	}

	/** A refusal under a trade card. */
	public int dangerColor() {
		return 0xFFE0504A;
	}

	/** Slot wells in the Trade box's strip: the frame, the floor and the lip, from the theme's own colours. */
	public int wellDarkColor() {
		return 0xFF000000 | ((progressBackColor >> 1) & 0x7F7F7F);
	}

	public int wellBackgroundColor() {
		return progressBackColor;
	}

	public int wellLightColor() {
		return 0xFF000000 | (separatorColor & 0xFFFFFF);
	}

	public static ResourceLocation categoryIcon(DialogueCategory category) {
		return sprite("dialogue/icons/" + category.name().toLowerCase());
	}

	public static ResourceLocation categoryCard(DialogueCategory category, boolean selected) {
		return sprite("dialogue/categories/" + category.name().toLowerCase() + (selected ? "_selected" : ""));
	}

	public static ResourceLocation statusIcon(String state) {
		return sprite("dialogue/icons/" + state);
	}

	public static ResourceLocation crest(ResourceLocation styleId) {
		String name = styleId == null ? "default" : styleId.getPath();
		return sprite("dialogue/crests/" + name);
	}

	private static ResourceLocation sprite(String path) {
		return Hemomancy.rloc(path);
	}
}
