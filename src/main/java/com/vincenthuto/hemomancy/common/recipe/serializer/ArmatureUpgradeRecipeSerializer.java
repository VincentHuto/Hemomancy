package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.recipe.ArmatureUpgradeRecipe;
import com.vincenthuto.hemomancy.common.recipe.ArmatureUpgradeRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.stream.Stream;

public class ArmatureUpgradeRecipeSerializer implements RecipeSerializer<ArmatureUpgradeRecipe> {
	private static final MapCodec<ArmatureUpgradeRecipe> CODEC = new MapCodec<>() {
		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of("id", "required_degree", "armor_slot", "base", "reagent",
					"blood_cost", "result", "required_armature_tier", "required_base_data", "result_data",
					"persistent_data_gate")
					.map(ops::createString);
		}

		@Override
		public <T> DataResult<ArmatureUpgradeRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
			try {
				JsonObject json = toJsonObject(ops, input);
				ResourceLocation id = json.has("id")
						? ResourceLocation.parse(json.get("id").getAsString())
						: Hemomancy.rloc("armature_upgrade/unknown");
				return DataResult.success(fromJsonObject(id, json));
			} catch (Exception e) {
				return DataResult.error(() -> "Failed to decode ArmatureUpgradeRecipe: " + e.getMessage());
			}
		}

		@Override
		public <T> RecordBuilder<T> encode(ArmatureUpgradeRecipe recipe, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			prefix.add("id", ops.createString(recipe.getId().toString()));
			prefix.add("required_degree", ops.createInt(recipe.getRequiredDegree()));
			prefix.add("armor_slot", ops.createString(recipe.getArmorSlot().name().toLowerCase()));
			Ingredient.CODEC_NONEMPTY.encodeStart(JsonOps.INSTANCE, recipe.getValidBase()).result()
					.ifPresent(e -> prefix.add("base", JsonOps.INSTANCE.convertTo(ops, e)));
			Ingredient.CODEC_NONEMPTY.encodeStart(JsonOps.INSTANCE, recipe.getReagent()).result()
					.ifPresent(e -> prefix.add("reagent", JsonOps.INSTANCE.convertTo(ops, e)));
			prefix.add("blood_cost", ops.createDouble(recipe.getBloodCost()));
			ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, recipe.getResultItem(null)).result()
					.ifPresent(e -> prefix.add("result", JsonOps.INSTANCE.convertTo(ops, e)));
			prefix.add("required_armature_tier", ops.createString(recipe.getRequiredArmatureTier().serializedName()));
			if (recipe.getRequiredBaseData() != null) {
				prefix.add("required_base_data", NbtOps.INSTANCE.convertTo(ops, recipe.getRequiredBaseData()));
			}
			if (recipe.getResultData() != null) {
				prefix.add("result_data", NbtOps.INSTANCE.convertTo(ops, recipe.getResultData()));
			}
			if (recipe.getPersistentDataGate() != null) {
				JsonObject gate = new JsonObject();
				gate.addProperty("key", recipe.getPersistentDataGate().key());
				gate.addProperty("value", recipe.getPersistentDataGate().value());
				prefix.add("persistent_data_gate", JsonOps.INSTANCE.convertTo(ops, gate));
			}
			return prefix;
		}
	};

	private static final StreamCodec<RegistryFriendlyByteBuf, ArmatureUpgradeRecipe> STREAM_CODEC = StreamCodec.of(
			ArmatureUpgradeRecipeSerializer::toNetwork,
			ArmatureUpgradeRecipeSerializer::fromNetwork);

	@Override
	public MapCodec<ArmatureUpgradeRecipe> codec() {
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ArmatureUpgradeRecipe> streamCodec() {
		return STREAM_CODEC;
	}

	private static <T> JsonObject toJsonObject(DynamicOps<T> ops, MapLike<T> input) {
		JsonObject json = new JsonObject();
		input.entries().forEach(pair -> {
			String key = ops.getStringValue(pair.getFirst()).getOrThrow(IllegalStateException::new);
			JsonElement value = ops.convertTo(JsonOps.INSTANCE, pair.getSecond());
			json.add(key, value);
		});
		return json;
	}

	private static ArmatureUpgradeRecipe fromJsonObject(ResourceLocation id, JsonObject json) {
		int requiredDegree = requiredDegreeFromJson(json);
		ArmatureUpgradeRules.ArmatureSlot armorSlot = slotFromString(GsonHelper.getAsString(json, "armor_slot"));
		Ingredient base = ingredientFromJson(json, "base");
		Ingredient reagent = ingredientFromJson(json, "reagent");
		double bloodCost = json.has("blood_cost")
				? GsonHelper.getAsDouble(json, "blood_cost")
				: GsonHelper.getAsDouble(json, "bloodCost", 0);
		ItemStack result = RecipeResultStackParser.parseResultStack(json, "result");
		ArmatureUpgradeRules.ArmatureTier requiredArmatureTier = json.has("required_armature_tier")
				? ArmatureUpgradeRules.ArmatureTier.byName(GsonHelper.getAsString(json, "required_armature_tier"))
				: ArmatureUpgradeRules.requiredTierForDegree(requiredDegree);
		CompoundTag resultData = json.has("result_data") && json.get("result_data").isJsonObject()
				? compoundFromJson(json.getAsJsonObject("result_data"))
				: null;
		CompoundTag requiredBaseData = json.has("required_base_data") && json.get("required_base_data").isJsonObject()
				? compoundFromJson(json.getAsJsonObject("required_base_data"))
				: null;
		ArmatureUpgradeRecipe.PersistentDataGate gate = json.has("persistent_data_gate")
				? gateFromJson(json.getAsJsonObject("persistent_data_gate"))
				: null;
		return new ArmatureUpgradeRecipe(id, requiredDegree, armorSlot, base, reagent,
				bloodCost, result, requiredArmatureTier, requiredBaseData, resultData, gate);
	}

	private static int requiredDegreeFromJson(JsonObject json) {
		if (json.has("required_degree")) {
			return GsonHelper.getAsInt(json, "required_degree", 0);
		}
		return GsonHelper.getAsInt(json, "requiredDegree", 0);
	}

	private static Ingredient ingredientFromJson(JsonObject json, String key) {
		JsonElement element = json.get(key);
		if (element == null || element.isJsonNull()) {
			throw new JsonSyntaxException("Missing armature ingredient: " + key);
		}
		return Ingredient.CODEC_NONEMPTY.parse(JsonOps.INSTANCE, element)
				.getOrThrow(err -> new JsonSyntaxException("Invalid armature ingredient " + key + ": " + err));
	}

	private static ArmatureUpgradeRules.ArmatureSlot slotFromString(String name) {
		return switch (name.toLowerCase()) {
			case "head", "helmet" -> ArmatureUpgradeRules.ArmatureSlot.HEAD;
			case "chest", "chestplate" -> ArmatureUpgradeRules.ArmatureSlot.CHEST;
			case "legs", "leggings" -> ArmatureUpgradeRules.ArmatureSlot.LEGS;
			case "feet", "boots" -> ArmatureUpgradeRules.ArmatureSlot.FEET;
			default -> throw new JsonSyntaxException("Unknown armature armor slot: " + name);
		};
	}

	private static CompoundTag compoundFromJson(JsonObject object) {
		CompoundTag tag = new CompoundTag();
		for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
			JsonElement element = entry.getValue();
			if (!element.isJsonPrimitive()) {
				throw new JsonSyntaxException("Armature custom data only supports primitive values: " + entry.getKey());
			}
			JsonPrimitive primitive = element.getAsJsonPrimitive();
			if (primitive.isBoolean()) {
				tag.putBoolean(entry.getKey(), primitive.getAsBoolean());
			} else if (primitive.isNumber()) {
				double value = primitive.getAsDouble();
				if (Math.floor(value) == value) {
					tag.putInt(entry.getKey(), primitive.getAsInt());
				} else {
					tag.putDouble(entry.getKey(), value);
				}
			} else {
				tag.putString(entry.getKey(), primitive.getAsString());
			}
		}
		return tag;
	}

	@Nullable
	private static ArmatureUpgradeRecipe.PersistentDataGate gateFromJson(JsonObject object) {
		String key = GsonHelper.getAsString(object, "key", "");
		String value = GsonHelper.getAsString(object, "value", "");
		if (key.isBlank()) {
			return null;
		}
		return new ArmatureUpgradeRecipe.PersistentDataGate(key, value);
	}

	private static ArmatureUpgradeRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
		ResourceLocation id = buffer.readResourceLocation();
		int requiredDegree = buffer.readInt();
		ArmatureUpgradeRules.ArmatureSlot slot = ArmatureUpgradeRules.ArmatureSlot.valueOf(buffer.readUtf());
		Ingredient base = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
		Ingredient reagent = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
		double bloodCost = buffer.readDouble();
		ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
		ArmatureUpgradeRules.ArmatureTier requiredArmatureTier =
				ArmatureUpgradeRules.ArmatureTier.byName(buffer.readUtf());
		CompoundTag requiredBaseData = buffer.readBoolean() ? buffer.readNbt() : null;
		CompoundTag resultData = buffer.readBoolean() ? buffer.readNbt() : null;
		ArmatureUpgradeRecipe.PersistentDataGate gate = null;
		if (buffer.readBoolean()) {
			gate = new ArmatureUpgradeRecipe.PersistentDataGate(buffer.readUtf(), buffer.readUtf());
		}
		return new ArmatureUpgradeRecipe(id, requiredDegree, slot, base, reagent,
				bloodCost, result, requiredArmatureTier, requiredBaseData, resultData, gate);
	}

	private static void toNetwork(RegistryFriendlyByteBuf buffer, ArmatureUpgradeRecipe recipe) {
		buffer.writeResourceLocation(recipe.getId());
		buffer.writeInt(recipe.getRequiredDegree());
		buffer.writeUtf(recipe.getArmorSlot().name());
		Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.getValidBase());
		Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.getReagent());
		buffer.writeDouble(recipe.getBloodCost());
		ItemStack.STREAM_CODEC.encode(buffer, recipe.getResultItem(null));
		buffer.writeUtf(recipe.getRequiredArmatureTier().serializedName());
		CompoundTag requiredBaseData = recipe.getRequiredBaseData();
		buffer.writeBoolean(requiredBaseData != null && !requiredBaseData.isEmpty());
		if (requiredBaseData != null && !requiredBaseData.isEmpty()) {
			buffer.writeNbt(requiredBaseData);
		}
		CompoundTag resultData = recipe.getResultData();
		buffer.writeBoolean(resultData != null && !resultData.isEmpty());
		if (resultData != null && !resultData.isEmpty()) {
			buffer.writeNbt(resultData);
		}
		buffer.writeBoolean(recipe.getPersistentDataGate() != null);
		if (recipe.getPersistentDataGate() != null) {
			buffer.writeUtf(recipe.getPersistentDataGate().key());
			buffer.writeUtf(recipe.getPersistentDataGate().value());
		}
	}
}
