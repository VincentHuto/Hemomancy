package com.vincenthuto.hemomancy.common.recipe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodStructureOfferingSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private BloodStructureOfferingSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String recipe = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/recipe/BloodStructureRecipe.java"));
		String offering = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/recipe/BloodStructureOffering.java"));
		String serializer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/recipe/serializer/BloodStructureRecipeSerializer.java"));
		String craftingHelper = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/capa/harbinger/BloodStructureCraftingHelper.java"));
		String feedManager = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/event/BloodStructureFeedManager.java"));
		String packetHandler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/PacketHandler.java"));
		String feedPacket = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketBloodStructureFeed.java"));
		String offeringPacket = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketBloodStructureOfferingBurst.java"));

		assertContains("recipe stores offerings", recipe, "List<BloodStructureOffering> offerings");
		assertContains("recipes without offerings stay compatible", recipe, "List.of()");
		assertContains("recipe exposes offerings", recipe, "getOfferings()");
		assertContains("offering uses ingredient matching", offering, "Ingredient ingredient");
		assertContains("offering uses counted separate braziers", offering, "int count");
		assertContains("offering rejects non-positive counts", offering, "count <= 0");
		assertContains("serializer accepts optional offerings array", serializer, "\"offerings\"");
		assertContains("serializer parses ingredient entries", serializer, "Ingredient.CODEC_NONEMPTY");
		assertContains("serializer syncs offerings to clients", serializer, "Ingredient.CONTENTS_STREAM_CODEC");
		assertContains("helper searches for matching brazier offerings", craftingHelper, "findOfferingMatch");
		assertContains("helper uses pattern-sized offering bounds", craftingHelper, "offeringSearchBounds");
		assertContains("offering bounds use matched structure footprint", craftingHelper,
				"offeringSearchBounds(match, recipe.getPattern().getBlockPattern())");
		assertContains("offering bounds expand from structure bounds", craftingHelper,
				"getMatchBounds(match, blockPattern)");
		assertContains("offering bounds expand by pattern dimensions", craftingHelper,
				"bounds.minX - blockPattern.getWidth()");
		assertNotContains("offering bounds are not center-only", craftingHelper,
				"center.offset(-blockPattern.getWidth()");
		assertContains("helper consumes one brazier per required item", craftingHelper, "consumeMatchedOfferings");
		assertContains("helper requires lit braziers for offerings", craftingHelper, "BrazierBlock.isLit(level.getBlockState(pos))");
		assertContains("helper unlights consumed offering braziers", craftingHelper, "unlightOfferingBrazier");
		assertNotContains("crafting does not light offering braziers", craftingHelper,
				"private static void lightOfferingBrazier");
		assertContains("feed manager validates offerings before blood drain", feedManager, "match.offerings()");
		assertContains("feed manager sends separate offering burst", feedManager, "PacketBloodStructureOfferingBurst");
		assertContains("offering packet registered separately", packetHandler, "PacketBloodStructureOfferingBurst.TYPE");
		assertNotContains("structure feed packet remains structure-only", feedPacket, "offering");
		assertContains("offering packet carries item texture source", offeringPacket, "ItemStack.OPTIONAL_STREAM_CODEC");
		assertContains("offering packet carries center target", offeringPacket, "center");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": contained " + unexpected);
		}
	}
}
