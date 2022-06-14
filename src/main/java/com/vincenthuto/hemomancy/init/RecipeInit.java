package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.recipe.ChiselRecipe;
import com.vincenthuto.hemomancy.recipe.CopyBloodGourdRecipe;
import com.vincenthuto.hemomancy.recipe.CopyMorphlingJarRecipe;
import com.vincenthuto.hemomancy.recipe.CopyRuneBinderRecipe;
import com.vincenthuto.hemomancy.recipe.EarthlyTransfuserRecipe;
import com.vincenthuto.hemomancy.recipe.FillBloodGourdRecipe;
import com.vincenthuto.hemomancy.recipe.JuiceinatorRecipe;
import com.vincenthuto.hemomancy.recipe.RecallerRecipe;
import com.vincenthuto.hemomancy.recipe.serializer.ChiselRecipeSerializer;
import com.vincenthuto.hemomancy.recipe.serializer.EarthlyTransfuserSerializer;
import com.vincenthuto.hemomancy.recipe.serializer.JuiceinatorSerializer;
import com.vincenthuto.hemomancy.recipe.serializer.RecallerRecipeSerializer;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RecipeInit {
	public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister
			.create(ForgeRegistries.RECIPE_SERIALIZERS, Hemomancy.MOD_ID);

	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister
			.create(ForgeRegistries.RECIPE_TYPES, Hemomancy.MOD_ID);


	public static final RegistryObject<RecipeSerializer<?>> juiceinator_serializer = SERIALIZERS.register("juiceinator",
			JuiceinatorSerializer::new);
	public static final RegistryObject<RecipeSerializer<?>> earthly_transfuser_serializer = SERIALIZERS
			.register("earthly_transfuser", EarthlyTransfuserSerializer::new);

	public static final RegistryObject<RecipeSerializer<?>> morphling_jar_upgrade_serializer = SERIALIZERS
			.register("morphling_jar_upgrade", CopyMorphlingJarRecipe.Serializer::new);

	public static final RegistryObject<RecipeSerializer<?>> rune_binder_upgrade_serializer = SERIALIZERS
			.register("rune_binder_upgrade", CopyRuneBinderRecipe.Serializer::new);

	public static final RegistryObject<RecipeSerializer<?>> blood_gourd_upgrade_serializer = SERIALIZERS
			.register("blood_gourd_upgrade", CopyBloodGourdRecipe.Serializer::new);

	public static final RegistryObject<RecipeSerializer<?>> blood_gourd_fill_serializer = SERIALIZERS
			.register("blood_gourd_fill", FillBloodGourdRecipe.Serializer::new);

	public static final RegistryObject<RecipeSerializer<?>> chisel_recipe_serializer = SERIALIZERS
			.register("chisel_recipe", ChiselRecipeSerializer::new);

	public static final RegistryObject<RecipeSerializer<?>> recaller_recipe_serializer = SERIALIZERS
			.register("recaller_recipe", RecallerRecipeSerializer::new);

	public static final RegistryObject<RecipeSerializer<?>> binder_upgrade = SERIALIZERS.register("binder_upgrade",
			CopyRuneBinderRecipe.Serializer::new);

	public static final RegistryObject<RecipeType<EarthlyTransfuserRecipe>> earthly_transfuser = RECIPE_TYPES
			.register("earthly_transfuser", () -> new RecipeType<EarthlyTransfuserRecipe>() {
				public String toString() {
					return earthly_transfuser.toString();
				}
			});

	public static final RegistryObject<RecipeType<JuiceinatorRecipe>> juiceinator = RECIPE_TYPES.register("juiceinator",
			() -> new RecipeType<JuiceinatorRecipe>() {
				public String toString() {
					return juiceinator.toString();
				}
			});

	public static final RegistryObject<RecipeType<ChiselRecipe>> chisel_recipe = RECIPE_TYPES
			.register("chisel_recipse", () -> new RecipeType<ChiselRecipe>() {
				public String toString() {
					return chisel_recipe.toString();
				}
			});

	public static final RegistryObject<RecipeType<RecallerRecipe>> recaller_recipe = RECIPE_TYPES
			.register("recaller_recipe", () -> new RecipeType<RecallerRecipe>() {
				public String toString() {
					return recaller_recipe.toString();
				}
			});

}
