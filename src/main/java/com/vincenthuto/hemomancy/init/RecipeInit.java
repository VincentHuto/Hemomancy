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

	public static RecipeType<EarthlyTransfuserRecipe> earthly_transfuser_recipe_type;
	public static RecipeType<JuiceinatorRecipe> juiceinator_recipe_type;
	public static RecipeType<ChiselRecipe> chisel_recipe_type;
	public static RecipeType<RecallerRecipe> recaller_recipe_type;

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
	public static final RegistryObject<RecipeSerializer<?>> morphling_jar_upgrade = SERIALIZERS
			.register("morphling_jar_upgrade", CopyMorphlingJarRecipe.Serializer::new);

	@SubscribeEvent
	public static void setup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			RecipeInit.initRecipeTypes();
		});
	}

	private static void initRecipeTypes() {

		final ResourceLocation earthly_transfuser = new ResourceLocation(Hemomancy.MOD_ID, "earthly_transfuser");
		earthly_transfuser_recipe_type = (RecipeType<EarthlyTransfuserRecipe>) Registry.register(Registry.RECIPE_TYPE,
				earthly_transfuser, new RecipeType<EarthlyTransfuserRecipe>() {
					public String toString() {
						return earthly_transfuser.toString();
					}
				});

		final ResourceLocation juiceinator_recipe = new ResourceLocation(Hemomancy.MOD_ID, "juiceinator");
		juiceinator_recipe_type = (RecipeType<JuiceinatorRecipe>) Registry.register(Registry.RECIPE_TYPE,
				juiceinator_recipe, new RecipeType<JuiceinatorRecipe>() {
					public String toString() {
						return juiceinator_recipe.toString();
					}
				});

		final ResourceLocation chisel_recipe = new ResourceLocation(Hemomancy.MOD_ID, "chisel_recipse");
		chisel_recipe_type = (RecipeType<ChiselRecipe>) Registry.register(Registry.RECIPE_TYPE, chisel_recipe,
				new RecipeType<ChiselRecipe>() {
					public String toString() {
						return chisel_recipe.toString();
					}
				});

		final ResourceLocation recaller_recipe = new ResourceLocation(Hemomancy.MOD_ID, "recaller_recipe");
		recaller_recipe_type = (RecipeType<RecallerRecipe>) Registry.register(Registry.RECIPE_TYPE, recaller_recipe,
				new RecipeType<RecallerRecipe>() {
					public String toString() {
						return recaller_recipe.toString();
					}
				});
	}

}
