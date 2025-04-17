package com.vincenthuto.hemomancy.compat.mna.tile;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.*;
import com.vincenthuto.hemomancy.compat.mna.block.MnAPluginBlockInit;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MnAPluginBlockEntityInit {
	public static final DeferredRegister<BlockEntityType<?>> MNATILES = DeferredRegister
			.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Hemomancy.MOD_ID);

	public static final RegistryObject<BlockEntityType<BrokenManaTrapazahedronBlockEntity>> broken_mana_trapazahedron = MNATILES
			.register("broken_mana_trapazahedron", () -> BlockEntityType.Builder
					.of(BrokenManaTrapazahedronBlockEntity::new, MnAPluginBlockInit.broken_mana_trapazahedron.get()).build(null));

}
