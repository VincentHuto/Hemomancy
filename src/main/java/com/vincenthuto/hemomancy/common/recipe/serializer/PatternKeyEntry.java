package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.vincenthuto.hutoslib.math.MultiblockPatternKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

final class PatternKeyEntry {
	private final String symbol;
	private final ResourceLocation blockId;
	private final ResourceLocation tagId;
	private final ResourceLocation fallbackId;

	private PatternKeyEntry(String symbol, ResourceLocation blockId, ResourceLocation tagId,
			ResourceLocation fallbackId) {
		this.symbol = symbol;
		this.blockId = blockId;
		this.tagId = tagId;
		this.fallbackId = fallbackId;
	}

	static PatternKeyEntry fromJsonLiteral(String symbol, String json) {
		return fromJson(symbol, JsonParser.parseString(json).getAsJsonObject());
	}

	static PatternKeyEntry fromJson(String symbol, JsonObject json) {
		if (GsonHelper.isStringValue(json, "tag")) {
			ResourceLocation tagId = ResourceLocation.parse(GsonHelper.getAsString(json, "tag"));
			ResourceLocation fallbackId = ResourceLocation.parse(GsonHelper.getAsString(json, "fallback"));
			return new PatternKeyEntry(symbol, null, tagId, fallbackId);
		}
		ResourceLocation blockId = ResourceLocation.parse(GsonHelper.getAsString(json, "block"));
		return new PatternKeyEntry(symbol, blockId, null, blockId);
	}

	boolean matches(BlockInWorld blockInWorld) {
		if (isTag()) {
			TagKey<Block> tag = TagKey.create(Registries.BLOCK, tagId);
			return blockInWorld.getState().is(tag);
		}
		return blockInWorld.getState().getBlock() == fallbackBlock();
	}

	Block fallbackBlock() {
		Block block = BuiltInRegistries.BLOCK.get(fallbackId);
		if (block == Blocks.AIR) {
			throw new JsonSyntaxException("Invalid fallback block for key '" + symbol + "': " + fallbackId);
		}
		return block;
	}

	MultiblockPatternKey toMultiblockPatternKey() {
		if (isTag()) {
			return MultiblockPatternKey.tag(symbol, tagId, fallbackBlock());
		}
		return MultiblockPatternKey.block(symbol, fallbackBlock());
	}

	boolean isTag() {
		return tagId != null;
	}

	ResourceLocation blockId() {
		return blockId;
	}

	ResourceLocation tagId() {
		return tagId;
	}

	ResourceLocation fallbackId() {
		return fallbackId;
	}

	String blockIdString() {
		return blockId == null ? "" : blockId.toString();
	}

	String tagIdString() {
		return tagId == null ? "" : tagId.toString();
	}

	String fallbackIdString() {
		return fallbackId.toString();
	}
}
