package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal.ItemFungalScar;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Recipe type for the {@code MycelialCrucible}.
 *
 * <p>Each recipe describes a single fungal scar cultivation run:
 * <ul>
 *   <li>{@code tendency}               — the scar's alignment; only aligned enzymes count.</li>
 *   <li>{@code bloodCostPhase1}        — flat blood drained when Phase 1 begins.</li>
 *   <li>{@code phase1Duration}         — crafting ticks for Phase 1 implantation.</li>
 *   <li>{@code maturationThreshold}    — total enzyme-power needed in Phase 2.</li>
 *   <li>{@code seed}                   — the center-slot Phase 1 input consumed to start the culture.</li>
 *   <li>{@code immatureResult}         — the immature fungal scar produced at Phase 1 end.</li>
 *   <li>{@code result}                 — the finished {@link ItemFungalScar}.</li>
 * </ul>
 */
public class FungalScarCultivationRecipe extends CustomRecipe {

    private final ResourceLocation id;
    private final EnumBloodTendency tendency;
    private final float bloodCostPhase1;
    private final int phase1Duration;
    private final int maturationThreshold;
    private final ItemStack seed;
    private final ItemStack immatureResult;
    private final ItemStack result;

    public FungalScarCultivationRecipe(ResourceLocation id,
            EnumBloodTendency tendency,
            float bloodCostPhase1,
            int phase1Duration,
            int maturationThreshold,
            ItemStack seed,
            ItemStack immatureResult,
            ItemStack result) {
        super(CraftingBookCategory.MISC);
        this.id = id;
        this.tendency = tendency;
        this.bloodCostPhase1 = bloodCostPhase1;
        this.phase1Duration = phase1Duration;
        this.maturationThreshold = maturationThreshold;
        this.seed = seed;
        this.immatureResult = immatureResult;
        this.result = result;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public ResourceLocation getId()               { return id; }
    public EnumBloodTendency getTendency()        { return tendency; }
    public float getBloodCostPhase1()             { return bloodCostPhase1; }
    public int getPhase1Duration()                { return phase1Duration; }
    public int getMaturationThreshold()           { return maturationThreshold; }
    public ItemStack getSeedItemStack()           { return seed.copy(); }
    public ItemStack getImmatureResult()          { return immatureResult.copy(); }
    public ItemStack getResultItemStack()         { return result; }

    // ── Registry lookup ───────────────────────────────────────────────────────

    public static List<FungalScarCultivationRecipe> getAllRecipes(Level world) {
        return world.getRecipeManager()
                .getAllRecipesFor(RecipeInit.fungal_scar_cultivation_type.get())
                .stream().map(RecipeHolder::value).collect(Collectors.toList());
    }

    // ── CustomRecipe boilerplate ──────────────────────────────────────────────

    @Override
    public boolean matches(CraftingInput container, Level level) { return false; }

    @Override
    public ItemStack assemble(CraftingInput container, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return false; }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeInit.fungal_scar_cultivation_serializer.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeInit.fungal_scar_cultivation_type.get();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(BlockInit.mycelial_crucible.get());
    }
}
