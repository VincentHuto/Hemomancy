package com.vincenthuto.hemomancy.client.screen.util;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public final class AbocipherText {
	public static final ResourceLocation FONT = Hemomancy.rloc("abocipher");

	private AbocipherText() {
	}

	public static boolean canRead(@Nullable Player player) {
		return player != null && LiberKnowledgeHelper.hasEntry(player, LiberEntryDefinitions.ABOCIPHER_LITERACY);
	}

	public static Component veilIfUnread(@Nullable Player player, Component text) {
		return canRead(player) ? text : veil(text);
	}

	public static Component veil(Component text) {
		return veil(text.getString(), text.getStyle());
	}

	public static Component veil(String text) {
		return veil(text, Style.EMPTY);
	}

	public static Component veil(String text, Style baseStyle) {
		MutableComponent result = Component.empty();
		if (text == null || text.isEmpty()) {
			return result.setStyle(baseStyle);
		}

		int index = 0;
		while (index < text.length()) {
			boolean cipherRun = isCipherLetter(text.charAt(index));
			int start = index++;
			while (index < text.length() && isCipherLetter(text.charAt(index)) == cipherRun) {
				index++;
			}
			Style style = cipherRun ? baseStyle.withFont(FONT) : baseStyle;
			result.append(Component.literal(text.substring(start, index)).setStyle(style));
		}
		return result;
	}

	private static boolean isCipherLetter(char value) {
		return value >= 'a' && value <= 'z' || value >= 'A' && value <= 'Z';
	}
}
