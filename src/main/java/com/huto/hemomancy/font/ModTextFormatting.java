package com.huto.hemomancy.font;

import net.minecraft.item.Rarity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.StringUtils;

@OnlyIn(Dist.CLIENT)
public class ModTextFormatting {

	/***
	 * 
	 * @param parString
	 * @param parShineLocation
	 * @param parReturnToBlack
	 * @return
	 */

	public static Rarity SANGUINE = Rarity.create("Sanguine", TextFormatting.DARK_RED);

	@OnlyIn(Dist.CLIENT)
	public static String stringToRedObf(String parString, int parShineLocation, boolean parReturnToBlack) {
		int stringLength = parString.length();
		if (stringLength < 1) {
			return "";
		}
		String outputString = "";
		for (int i = 0; i < stringLength; i++) {
			if ((i + parShineLocation + System.nanoTime() / 20) % 88 == 0) {
				outputString = outputString + TextFormatting.OBFUSCATED + parString.substring(i, i + 1);
			} else if ((i + parShineLocation + System.nanoTime() / 20) % 88 == 1) {
				outputString = outputString + TextFormatting.RED + parString.substring(i, i + 1);
			} else if ((i + parShineLocation + System.nanoTime() / 20) % 88 == 87) {
				outputString = outputString + TextFormatting.OBFUSCATED + parString.substring(i, i + 1);
			} else {
				outputString = outputString + TextFormatting.BLACK + parString.substring(i, i + 1);
			}
		}
		// return color to a common one after (most chat is white, but for other GUI
		// might want black)
		if (parReturnToBlack) {
			return outputString + TextFormatting.BLACK;
		}
		return outputString + TextFormatting.WHITE;
	}

	/***
	 * 
	 * @param input a string to be formated
	 * @return a string formated as such hello world = Hello world || teSt = Test
	 */

	public static String toProperCase(String input) {
		String newString = "";
		String culledString = input.replaceAll("_", " ");
		input = culledString;
		for (int i = 0; i < input.length(); i++) {

			if (i == 0) {
				String temp = StringUtils.toUpperCase(String.valueOf(input.charAt(i)));
				newString = newString + temp;
			} else {
				String temp = StringUtils.toLowerCase(String.valueOf(input.charAt(i)));
				newString = newString + temp;

			}
		}
		return newString;
	}

	@OnlyIn(Dist.CLIENT)
	public static String stringToBloody(String parString) {

		String outputString = "";
		TextFormatting[] karmicColors = { TextFormatting.RED, TextFormatting.DARK_RED };
		for (int i = 0; i < parString.length(); i++) {
			outputString = TextFormatting.ITALIC + outputString + karmicColors[i % 2] + parString.substring(i, i + 1);
		}
		return outputString;

	}

	public static String convertInitToLang(String text) {
		if (text == null || text.isEmpty()) {
			return text;
		}

		StringBuilder converted = new StringBuilder();
		boolean convertNext = true;
		text.replace("_trail", "");
		for (char ch : text.toCharArray()) {
			if (ch == '_') {
				ch = ' ';
				convertNext = true;
			} else if (convertNext) {
				ch = Character.toTitleCase(ch);
				convertNext = false;
			} else {
				ch = Character.toLowerCase(ch);
			}
			converted.append(ch);
		}

		return converted.toString();
	}

}
