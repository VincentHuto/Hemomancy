package com.vincenthuto.hemomancy.common.data.gen;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class HemoBlockTagProvider extends BlockTagsProvider {

	public HemoBlockTagProvider(PackOutput output, CompletableFuture<Provider> lookupProvider,
			@Nullable ExistingFileHelper existingFileHelper) {
		super(output, lookupProvider, Hemomancy.MOD_ID, existingFileHelper);
	}

	@Override
	public String getName() {
		return super.getName() + ": " + Hemomancy.MOD_ID;
	}



	@Override
	protected void addTags(Provider p_256380_) {
	}
}
