package com.vincenthuto.hemomancy.common.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class DataGenParsers {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public static String genBaseBlockState(String modid, String id) {
		JsonObject root = new JsonObject();
		JsonObject variants = new JsonObject();
		JsonObject defaultVariant = new JsonObject();
		defaultVariant.addProperty("model", modid + ":block/" + id);
		variants.add("", defaultVariant);
		root.add("variants", variants);
		return GSON.toJson(root);
	}

	public static String genBasicBlockModel(String modid, String id) {
		JsonObject root = new JsonObject();
		root.addProperty("parent", "block/cube_all");
		JsonObject textures = new JsonObject();
		textures.addProperty("all", modid + ":block/" + id);
		root.add("textures", textures);
		return GSON.toJson(root);
	}

	public static String genColumnHorizontalBlockModel(String modid, String id) {
		JsonObject root = new JsonObject();
		root.addProperty("parent", "minecraft:block/cube_column_horizontal");
		JsonObject textures = new JsonObject();
		textures.addProperty("end", modid + ":block/" + id + "_top");
		textures.addProperty("side", modid + ":block/" + id);
		root.add("textures", textures);
		return GSON.toJson(root);
	}

	public static String genColumnVerticalBlockModel(String modid, String id) {
		JsonObject root = new JsonObject();
		root.addProperty("parent", "block/cube_column");
		JsonObject textures = new JsonObject();
		textures.addProperty("end", modid + ":block/" + id + "_top");
		textures.addProperty("side", modid + ":block/" + id);
		root.add("textures", textures);
		return GSON.toJson(root);
	}

	public static String genCrossBlockModel(String modid, String id) {
		JsonObject root = new JsonObject();
		root.addProperty("parent", "block/cross");
		JsonObject textures = new JsonObject();
		textures.addProperty("cross", modid + ":block/" + id);
		root.add("textures", textures);
		return GSON.toJson(root);
	}

	public static String genObjBlockModel(String modid, String id, String particleId) {
		JsonObject root = new JsonObject();
		root.addProperty("parent", "forge:block/default");
		root.addProperty("loader", "forge:obj");
		root.addProperty("model", modid + ":models/block/" + id + ".obj");
		JsonObject textures = new JsonObject();
		textures.addProperty("particle", modid + ":block/" + particleId);
		root.add("textures", textures);
		return GSON.toJson(root);
	}

	public static String genRotatableBlockstate(String modid, String id) {
		JsonObject root = new JsonObject();
		JsonObject variants = new JsonObject();

		JsonObject north = new JsonObject();
		north.addProperty("model", modid + ":block/" + id);
		variants.add("facing=north", north);

		JsonObject south = new JsonObject();
		south.addProperty("model", modid + ":block/" + id);
		south.addProperty("y", 180);
		variants.add("facing=south", south);

		JsonObject west = new JsonObject();
		west.addProperty("model", modid + ":block/" + id);
		west.addProperty("y", 270);
		variants.add("facing=west", west);

		JsonObject east = new JsonObject();
		east.addProperty("model", modid + ":block/" + id);
		east.addProperty("y", 90);
		variants.add("facing=east", east);

		root.add("variants", variants);
		return GSON.toJson(root);
	}

	public static String genSlabBlockState(String modid, String id, String baseBlockId) {
		JsonObject root = new JsonObject();
		JsonObject variants = new JsonObject();

		JsonObject bottom = new JsonObject();
		bottom.addProperty("model", modid + ":block/" + id);
		variants.add("type=bottom", bottom);

		JsonObject top = new JsonObject();
		top.addProperty("model", modid + ":block/" + id + "_top");
		variants.add("type=top", top);

		JsonObject doubleSlab = new JsonObject();
		doubleSlab.addProperty("model", modid + ":block/" + baseBlockId);
		variants.add("type=double", doubleSlab);

		root.add("variants", variants);
		return GSON.toJson(root);
	}

	public static String genSlabBlockModel(String modid, String id, String baseBlockId) {
		JsonObject root = new JsonObject();
		root.addProperty("parent", "block/slab");
		JsonObject textures = new JsonObject();
		textures.addProperty("bottom", modid + ":block/" + baseBlockId);
		textures.addProperty("top", modid + ":block/" + baseBlockId);
		textures.addProperty("side", modid + ":block/" + baseBlockId);
		root.add("textures", textures);
		return GSON.toJson(root);
	}

	public static String genSlabTopBlockModel(String modid, String id, String baseBlockId) {
		JsonObject root = new JsonObject();
		root.addProperty("parent", "block/slab_top");
		JsonObject textures = new JsonObject();
		textures.addProperty("bottom", modid + ":block/" + baseBlockId);
		textures.addProperty("top", modid + ":block/" + baseBlockId);
		textures.addProperty("side", modid + ":block/" + baseBlockId);
		root.add("textures", textures);
		return GSON.toJson(root);
	}

	public static String genStairBlockModel(String modid, String id, String baseBlockId) {
		JsonObject root = new JsonObject();
		root.addProperty("parent", "block/stairs");
		JsonObject textures = new JsonObject();
		textures.addProperty("bottom", modid + ":block/" + baseBlockId);
		textures.addProperty("top", modid + ":block/" + baseBlockId);
		textures.addProperty("side", modid + ":block/" + baseBlockId);
		root.add("textures", textures);
		return GSON.toJson(root);
	}

	public static String genPottedPlantModel(String modid, String id, String plantId) {
		JsonObject root = new JsonObject();
		root.addProperty("parent", "block/flower_pot_cross");
		JsonObject textures = new JsonObject();
		textures.addProperty("plant", modid + ":block/" + plantId);
		root.add("textures", textures);
		return GSON.toJson(root);
	}

	public static String genMyceliumBlockModel(String modid, String id) {
		JsonObject root = new JsonObject();
		root.addProperty("parent", "block/cube_bottom_top");
		JsonObject textures = new JsonObject();
		textures.addProperty("top", modid + ":block/" + id + "_top");
		textures.addProperty("bottom", modid + ":block/" + id + "_bottom");
		textures.addProperty("side", modid + ":block/" + id + "_side");
		root.add("textures", textures);
		return GSON.toJson(root);
	}

	public static String genBlockItemModel(String modid, String id) {
		JsonObject root = new JsonObject();
		root.addProperty("parent", modid + ":block/" + id);
		return GSON.toJson(root);
	}

	public static String genBasicItemModel(String modid, String id) {
		JsonObject root = new JsonObject();
		root.addProperty("parent", "item/generated");
		JsonObject textures = new JsonObject();
		textures.addProperty("layer0", modid + ":item/" + id);
		root.add("textures", textures);
		return GSON.toJson(root);
	}

	public static String genHandheldItemModel(String modid, String id) {
		JsonObject root = new JsonObject();
		root.addProperty("parent", "item/handheld");
		JsonObject textures = new JsonObject();
		textures.addProperty("layer0", modid + ":item/" + id);
		root.add("textures", textures);
		return GSON.toJson(root);
	}

	public static String genSpawnEggItemModel() {
		JsonObject root = new JsonObject();
		root.addProperty("parent", "item/template_spawn_egg");
		return GSON.toJson(root);
	}

	/**
	 * @deprecated Use {@link #genCrossBlockModel(String, String)} instead.
	 */
	@Deprecated
	public static String genCrosslBlockModel(String modid, String id) {
		return genCrossBlockModel(modid, id);
	}
}
