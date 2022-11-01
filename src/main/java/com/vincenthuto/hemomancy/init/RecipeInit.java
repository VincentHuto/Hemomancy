package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.recipe.CopyBloodGourdRecipe;
import com.vincenthuto.hemomancy.recipe.CopyMorphlingJarRecipe;
import com.vincenthuto.hemomancy.recipe.FillBloodGourdRecipe;
import com.vincenthuto.hemomancy.recipe.JuiceinatorRecipe;
import com.vincenthuto.hemomancy.recipe.RecallerRecipe;
import com.vincenthuto.hemomancy.recipe.serializer.BloodStructureRecipeSerializer;
import com.vincenthuto.hemomancy.recipe.serializer.JuiceinatorSerializer;
import com.vincenthuto.hemomancy.recipe.serializer.RecallerRecipeSerializer;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RecipeInit {
	public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister
			.create(ForgeRegistries.RECIPE_SERIALIZERS, Hemomancy.MOD_ID);

	public static RecipeType<JuiceinatorRecipe> juiceinator_recipe_type;
	public static RecipeType<RecallerRecipe> recaller_recipe_type;
	public static RecipeType<BloodStructureRecipe> blood_structure_recipe_type;

	public static final RegistryObject<RecipeSerializer<?>> juiceinator_serializer = SERIALIZERS.register("juiceinator",
			JuiceinatorSerializer::new);

	public static final RegistryObject<RecipeSerializer<?>> morphling_jar_upgrade_serializer = SERIALIZERS
			.register("morphling_jar_upgrade", CopyMorphlingJarRecipe.Serializer::new);

	public static final RegistryObject<RecipeSerializer<?>> blood_gourd_upgrade_serializer = SERIALIZERS
			.register("blood_gourd_upgrade", CopyBloodGourdRecipe.Serializer::new);

	public static final RegistryObject<RecipeSerializer<?>> blood_gourd_fill_serializer = SERIALIZERS
			.register("blood_gourd_fill", FillBloodGourdRecipe.Serializer::new);

	public static final RegistryObject<RecipeSerializer<?>> recaller_recipe_serializer = SERIALIZERS
			.register("recaller_recipe", RecallerRecipeSerializer::new);

	public static final RegistryObject<RecipeSerializer<?>> blood_structure_recipe_serializer = SERIALIZERS
			.register("blood_structure_recipe", BloodStructureRecipeSerializer::new);

	@SubscribeEvent
	public static void setup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			RecipeInit.initRecipeTypes();
		});
	}

	private static void initRecipeTypes() {

		final ResourceLocation juiceinator_recipe = new ResourceLocation(Hemomancy.MOD_ID, "juiceinator");
		juiceinator_recipe_type = (RecipeType<JuiceinatorRecipe>) Registry.register(Registry.RECIPE_TYPE,
				juiceinator_recipe, new RecipeType<JuiceinatorRecipe>() {
					public String toString() {
						return juiceinator_recipe.toString();
					}
				});

		final ResourceLocation recaller_recipe = new ResourceLocation(Hemomancy.MOD_ID, "recaller_recipe");
		recaller_recipe_type = (RecipeType<RecallerRecipe>) Registry.register(Registry.RECIPE_TYPE, recaller_recipe,
				new RecipeType<RecallerRecipe>() {
					public String toString() {
						return recaller_recipe.toString();
					}
				});

		final ResourceLocation blood_structure_recipe = new ResourceLocation(Hemomancy.MOD_ID,
				"blood_structure_recipe");
		blood_structure_recipe_type = (RecipeType<BloodStructureRecipe>) Registry.register(Registry.RECIPE_TYPE,
				recaller_recipe, new RecipeType<BloodStructureRecipe>() {
					public String toString() {
						return blood_structure_recipe.toString();
					}
				});
	}

}
