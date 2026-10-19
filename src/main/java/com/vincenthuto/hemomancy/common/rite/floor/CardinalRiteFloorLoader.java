package com.vincenthuto.hemomancy.common.rite.floor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.vincenthuto.hemomancy.common.data.JsonResourceReloadListener;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hemomancy.common.recipe.serializer.CardinalRitePatternJson;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public final class CardinalRiteFloorLoader
		extends JsonResourceReloadListener<CardinalRiteFloorLoader.Sourced> {
	private static final Logger LOGGER = LogManager.getLogger();

	/** A parsed floor with the JSON it came from, kept so clients can be sent the same definition. */
	record Sourced(CardinalRiteFloorDefinition definition, String json) {
	}

	public CardinalRiteFloorLoader() {
		super("cardinal_rite_floor", "Cardinal Rite floor", LOGGER,
				(id, root) -> new Sourced(parse(id, root), root.toString()),
				CardinalRiteFloorRegistry::reloadSourced);
	}

	public static CardinalRiteFloorDefinition parse(ResourceLocation id, JsonObject root) {
		String style = root.get("style").getAsString();
		CardinalRiteType tier = CardinalRiteType.byName(root.get("tier").getAsString());
		BlockPos focus = blockPos(root.get("focus"));
		// Braziers stand anywhere along the floor's gash, so the floor derives its sockets instead of authoring them.
		var sockets = CardinalRiteGashRules.cells(Math.max(focus.getX(), focus.getZ()));
		float footprint = root.has("footprint_radius")
				? root.get("footprint_radius").getAsFloat()
				: tier.getSize() * 0.5F + 2.0F;
		var pattern = CardinalRitePatternJson.parse(root);
		validateFocus(id, pattern, focus);
		return new CardinalRiteFloorDefinition(id, style, tier, pattern, focus, sockets, footprint,
				purpose(id, style, root));
	}

	/** A standard style implies its purpose; a special style must declare one, and a declared one must agree. */
	static CardinalRitePurpose purpose(ResourceLocation id, String style, JsonObject root) {
		CardinalRitePurpose implied = CardinalRitePurpose.ofFloorStyle(style);
		CardinalRitePurpose declared = root.has("purpose")
				? CardinalRitePurpose.byId(root.get("purpose").getAsString()) : null;
		if (root.has("purpose") && declared == null) {
			throw new JsonSyntaxException("Cardinal Rite floor " + id + " declares an unknown purpose");
		}
		if (implied == null && declared == null) {
			throw new JsonSyntaxException("Special Cardinal Rite floor " + id + " must declare a purpose");
		}
		if (implied != null && declared != null && implied != declared) {
			throw new JsonSyntaxException("Cardinal Rite floor " + id + " is a " + style
					+ " floor and cannot declare purpose " + declared.id());
		}
		return implied != null ? implied : declared;
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

}
