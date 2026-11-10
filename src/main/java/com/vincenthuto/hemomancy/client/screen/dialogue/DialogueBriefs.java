package com.vincenthuto.hemomancy.client.screen.dialogue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * Writing aid behind the {@code debug.dialogueBriefMode} client option: each dialogue line and player response
 * shows what it is meant to say and in what tone, instead of its text, so a writer can rewrite the line from
 * its intent. Briefs live in {@code assets/hemomancy/dialogue_briefs/*.json} as
 * {@code {"<lang key>": {"about": "...", "tone": "..."}}}; a key with no brief shows its key instead.
 */
public final class DialogueBriefs implements ResourceManagerReloadListener {
	public static final DialogueBriefs INSTANCE = new DialogueBriefs();
	private static final String DIRECTORY = "dialogue_briefs";

	private record Brief(String about, String tone) {
	}

	private Map<String, Brief> briefs = Map.of();

	private DialogueBriefs() {
	}

	public static boolean active() {
		return HemoClientConfig.dialogueBriefMode();
	}

	/** The brief of an NPC line, or {@code null} when brief mode is off. */
	public static Component line(String key) {
		return active() ? INSTANCE.describe(key, "hemomancy.dialogue.brief.line") : null;
	}

	/** The brief of a player response, or {@code null} when brief mode is off. */
	public static Component option(String key) {
		return active() ? INSTANCE.describe(key, "hemomancy.dialogue.brief.option") : null;
	}

	private Component describe(String key, String frame) {
		Brief brief = briefs.get(key);
		MutableComponent text = brief == null
				? Component.translatable("hemomancy.dialogue.brief.missing")
				: Component.translatable(frame, brief.about(), brief.tone());
		return text.append(Component.translatable("hemomancy.dialogue.brief.key", key).withStyle(ChatFormatting.DARK_GRAY));
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		Map<String, Brief> loaded = new HashMap<>();
		for (Map.Entry<ResourceLocation, Resource> file : manager.listResources(DIRECTORY,
				path -> path.getNamespace().equals(Hemomancy.MOD_ID) && path.getPath().endsWith(".json")).entrySet()) {
			try (Reader reader = file.getValue().openAsReader()) {
				for (Map.Entry<String, JsonElement> entry : JsonParser.parseReader(reader).getAsJsonObject().entrySet()) {
					JsonObject brief = entry.getValue().getAsJsonObject();
					loaded.put(entry.getKey(), new Brief(brief.get("about").getAsString(), brief.get("tone").getAsString()));
				}
			} catch (Exception e) {
				Hemomancy.LOGGER.warn("Skipping dialogue brief file {}: {}", file.getKey(), e.getMessage());
			}
		}
		briefs = Map.copyOf(loaded);
	}
}
