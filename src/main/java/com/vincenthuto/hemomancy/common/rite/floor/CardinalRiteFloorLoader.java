package com.vincenthuto.hemomancy.common.rite.floor;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hemomancy.common.recipe.serializer.CardinalRitePatternJson;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class CardinalRiteFloorLoader
		extends SimplePreparableReloadListener<Map<ResourceLocation, CardinalRiteFloorDefinition>> {
	private static final String FOLDER = "cardinal_rite_floor";
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new Gson();

	@Override
	protected Map<ResourceLocation, CardinalRiteFloorDefinition> prepare(
			ResourceManager manager, ProfilerFiller profiler) {
		Map<ResourceLocation, CardinalRiteFloorDefinition> loaded = new HashMap<>();
		manager.listResources(FOLDER, id -> id.getPath().endsWith(".json")).forEach((file, resource) -> {
			String path = file.getPath();
			ResourceLocation id = ResourceLocation.fromNamespaceAndPath(file.getNamespace(),
					path.substring(FOLDER.length() + 1, path.length() - 5));
			try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
				CardinalRiteFloorDefinition definition = parse(id, GSON.fromJson(reader, JsonObject.class));
				if (loaded.put(id, definition) != null) {
					throw new JsonSyntaxException("Duplicate cardinal rite floor " + id);
				}
			} catch (Exception exception) {
				LOGGER.error("Failed to load Cardinal Rite floor {}: {}", file, exception.getMessage());
			}
		});
		return loaded;
	}

	public static CardinalRiteFloorDefinition parse(ResourceLocation id, JsonObject root) {
		String style = root.get("style").getAsString();
		CardinalRiteType tier = CardinalRiteType.byName(root.get("tier").getAsString());
		BlockPos focus = blockPos(root.get("focus"));
		var sockets = new ArrayList<BlockPos>();
		for (JsonElement socket : root.getAsJsonArray("brazier_sockets")) {
			sockets.add(blockPos(socket));
		}
		float footprint = root.has("footprint_radius")
				? root.get("footprint_radius").getAsFloat()
				: tier.getSize() * 0.5F + 2.0F;
		var pattern = CardinalRitePatternJson.parse(root);
		validateFocus(id, pattern, focus);
		return new CardinalRiteFloorDefinition(id, style, tier, pattern, focus, sockets, footprint);
	}

	private static void validateFocus(ResourceLocation id,
			com.vincenthuto.hutoslib.math.MultiblockPattern pattern, BlockPos focus) {
		var blockPattern = pattern.getBlockPattern();
		if (focus.getX() != blockPattern.getWidth() / 2 || focus.getZ() != blockPattern.getDepth() / 2
				|| focus.getY() < 0 || focus.getY() >= blockPattern.getHeight()) {
			throw new JsonSyntaxException("Cardinal Rite floor " + id + " must center its focus");
		}
		int count = 0;
		String[][] layout = pattern.getPatternArray();
		for (int z = 0; z < layout.length; z++) {
			for (int y = 0; y < layout[z].length; y++) {
				for (int x = 0; x < layout[z][y].length(); x++) {
					String symbol = String.valueOf(layout[z][y].charAt(x));
					if (pattern.getSymbolList().get(symbol) == BlockInit.cardinal_focus.get()) count++;
				}
			}
		}
		if (count != 1 || pattern.getSymbolList().get(String.valueOf(
				layout[focus.getZ()][focus.getY()].charAt(focus.getX()))) != BlockInit.cardinal_focus.get()) {
			throw new JsonSyntaxException("Cardinal Rite floor " + id
					+ " must contain exactly one Cardinal Focus at its declared focus");
		}
	}

	private static BlockPos blockPos(JsonElement element) {
		var values = element.getAsJsonArray();
		if (values.size() != 3) throw new JsonSyntaxException("Block position must have three integers");
		return new BlockPos(values.get(0).getAsInt(), values.get(1).getAsInt(), values.get(2).getAsInt());
	}

	@Override
	protected void apply(Map<ResourceLocation, CardinalRiteFloorDefinition> prepared,
			ResourceManager manager, ProfilerFiller profiler) {
		CardinalRiteFloorRegistry.reload(prepared);
		LOGGER.info("Loaded {} Cardinal Rite floors", prepared.size());
	}
}
