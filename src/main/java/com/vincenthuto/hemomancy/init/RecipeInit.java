package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.recipe.CopyBloodGourdDataRecipe;
import com.vincenthuto.hemomancy.recipe.CopyMorphlingJarDataRecipe;
import com.vincenthuto.hemomancy.recipe.CopyRuneBinderDataRecipe;
import com.vincenthuto.hemomancy.recipe.EarthlyTransfuserDataRecipe;
import com.vincenthuto.hemomancy.recipe.EarthlyTransfuserSerializer;
import com.vincenthuto.hemomancy.recipe.FillBloodGourdDataRecipe;
import com.vincenthuto.hemomancy.recipe.JuiceinatorDataRecipe;
import com.vincenthuto.hemomancy.recipe.JuiceinatorSerializer;
import com.vincenthuto.hemomancy.recipe.serializer.ChiselRecipe;
import com.vincenthuto.hemomancy.recipe.serializer.ChiselRecipeSerializer;

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

	public static final RecipeType<JuiceinatorDataRecipe> juiceinator_recipe_type = RecipeType
			.register(Hemomancy.MOD_ID + ":juiceinator");
	public static final RegistryObject<RecipeSerializer<?>> juiceinator_serializer = SERIALIZERS.register("juiceinator",
			JuiceinatorSerializer::new);
	public static final RecipeType<EarthlyTransfuserDataRecipe> earthly_transfuser_recipe_type = RecipeType
			.register(Hemomancy.MOD_ID + ":earthly_transfuser");
	public static final RegistryObject<RecipeSerializer<?>> earthly_transfuser_serializer = SERIALIZERS
			.register("earthly_transfuser", EarthlyTransfuserSerializer::new);
	public static final RegistryObject<RecipeSerializer<?>> morphling_jar_upgrade_serializer = SERIALIZERS
			.register("morphling_jar_upgrade", CopyMorphlingJarDataRecipe.Serializer::new);
	public static final RegistryObject<RecipeSerializer<?>> rune_binder_upgrade_serializer = SERIALIZERS
			.register("rune_binder_upgrade", CopyRuneBinderDataRecipe.Serializer::new);
	public static final RegistryObject<RecipeSerializer<?>> blood_gourd_upgrade_serializer = SERIALIZERS
			.register("blood_gourd_upgrade", CopyBloodGourdDataRecipe.Serializer::new);
	public static final RegistryObject<RecipeSerializer<?>> blood_gourd_fill_serializer = SERIALIZERS
			.register("blood_gourd_fill", FillBloodGourdDataRecipe.Serializer::new);

	public static final RegistryObject<RecipeSerializer<?>> chisel_recipe_serializer = SERIALIZERS
			.register("chisel_recipe", ChiselRecipeSerializer::new);

	public static RecipeType<ChiselRecipe> chisel_recipe_type;

	@SubscribeEvent
	public static void setup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			RecipeInit.initRecipeTypes();
		});
	}

	@SuppressWarnings("unchecked")
	private static void initRecipeTypes() {
		final ResourceLocation chisel_recipe = new ResourceLocation(Hemomancy.MOD_ID, "chisel_recipse");
		chisel_recipe_type = (RecipeType<ChiselRecipe>) Registry.register((Registry) Registry.RECIPE_TYPE,
				(ResourceLocation) chisel_recipe, (Object) new RecipeType<ChiselRecipe>() {
					public String toString() {
						return chisel_recipe.toString();
					}
				});
	}

}
