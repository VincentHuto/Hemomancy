package com.vincenthuto.hemomancy.support;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * English text of a component without a Minecraft bootstrap ({@code getString()} needs vanilla's {@code Language}, which
 * a plain JUnit run cannot load). Translation keys resolve against the mod's {@code en_us.json}, with vanilla's
 * {@code %s}, {@code %n$s} and {@code %%} formatting.
 */
public final class EnglishText {
	private static final Pattern FORMAT = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");
	private static Map<String, String> english;

	private EnglishText() {
	}

	/** The English for a key, as {@code en_us.json} gives it; a missing key fails the test. */
	public static String of(String key) {
		String value = english().get(key);
		if (value == null) throw new AssertionError("en_us.json has no " + key);
		return value;
	}

	public static String render(Component component) {
		StringBuilder out = new StringBuilder();
		append(component, out);
		return out.toString();
	}

	private static void append(Component component, StringBuilder out) {
		if (component.getContents() instanceof TranslatableContents translatable) {
			out.append(format(translatable));
		} else if (component.getContents() instanceof PlainTextContents plain) {
			out.append(plain.text());
		}
		for (Component sibling : component.getSiblings()) append(sibling, out);
	}

	private static String format(TranslatableContents translatable) {
		String template = english().get(translatable.getKey());
		if (template == null) {
			template = translatable.getFallback() != null ? translatable.getFallback() : translatable.getKey();
		}
		Object[] args = translatable.getArgs();
		StringBuilder out = new StringBuilder();
		Matcher matcher = FORMAT.matcher(template);
		int next = 0;
		int end = 0;
		while (matcher.find()) {
			out.append(template, end, matcher.start());
			end = matcher.end();
			if ("%".equals(matcher.group(2))) {
				out.append('%');
				continue;
			}
			int index = matcher.group(1) != null ? Integer.parseInt(matcher.group(1)) - 1 : next++;
			Object arg = args[index];
			out.append(arg instanceof Component nested ? render(nested) : String.valueOf(arg));
		}
		return out.append(template.substring(end)).toString();
	}

	private static synchronized Map<String, String> english() {
		if (english == null) {
			JsonObject lang = ResourceReader.asset("lang/en_us.json");
			Map<String, String> loaded = new HashMap<>();
			for (Map.Entry<String, JsonElement> entry : lang.entrySet()) loaded.put(entry.getKey(), entry.getValue().getAsString());
			english = loaded;
		}
		return english;
	}

	/** Every key whose English contains {@code text}. */
	static java.util.List<String> keysShowing(String text) {
		return english().entrySet().stream().filter(entry -> entry.getValue().contains(text)).map(Map.Entry::getKey)
				.sorted().toList();
	}
}
