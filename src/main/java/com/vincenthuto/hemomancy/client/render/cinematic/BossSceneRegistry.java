package com.vincenthuto.hemomancy.client.render.cinematic;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.render.cinematic.scene.BossSceneCatalog;
import net.minecraft.resources.ResourceLocation;

/**
 * The client's scene for each id the server can play. The boss a scene frames may reach the client after the scene
 * does, so a scene reads it when it draws ({@link BossScene#card}), never when it is made.
 */
public final class BossSceneRegistry {
	private static final Map<ResourceLocation, Supplier<BossScene>> SCENES = new HashMap<>();

	static {
		BossSceneCatalog.registerAll();
	}

	private BossSceneRegistry() {
	}

	public static void register(ResourceLocation id, Supplier<BossScene> factory) {
		SCENES.put(id, factory);
	}

	@Nullable
	public static Supplier<BossScene> get(ResourceLocation id) {
		return SCENES.get(id);
	}
}
