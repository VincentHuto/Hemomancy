package com.vincenthuto.hemomancy.client.model.item.book;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.EnumMap;
import java.util.Map;

/** Each openable Hemomancy book: its item, its mesh and atlas, and the shut book's extent for framing it. */
public enum HemoBookBinding {
	LIBER_SANGUINUM(ItemInit.liber_sanguinum, HemoBookLayers.LIBER_SANGUINUM,
			HemoBookLayers.LIBER_SANGUINUM_BOUNDS, "liber_sanguinum"),
	LIBER_IMMACULATUS(ItemInit.liber_immaculatus, HemoBookLayers.LIBER_IMMACULATUS,
			HemoBookLayers.LIBER_IMMACULATUS_BOUNDS, "liber_immaculatus"),
	BOOK_OF_OBSERVANCES(ItemInit.book_of_observances, HemoBookLayers.BOOK_OF_OBSERVANCES,
			HemoBookLayers.BOOK_OF_OBSERVANCES_BOUNDS, "book_of_observances"),
	HARBINGER_ASSIGNMENT_LEDGER(ItemInit.harbinger_assignment_ledger, HemoBookLayers.HARBINGER_ASSIGNMENT_LEDGER,
			HemoBookLayers.HARBINGER_ASSIGNMENT_LEDGER_BOUNDS, "harbinger_assignment_ledger"),
	MNEMONIC_FOLIO(ItemInit.mnemonic_folio, HemoBookLayers.MNEMONIC_FOLIO,
			HemoBookLayers.MNEMONIC_FOLIO_BOUNDS, "mnemonic_folio");

	private final DeferredHolder<Item, Item> item;
	public final ModelLayerLocation layer;
	public final ResourceLocation texture;
	/** The shut book's extent in model units: min x, y, z, then max x, y, z. */
	private final float[] bounds;
	private final RenderType solid;
	private final RenderType glow;

	HemoBookBinding(DeferredHolder<Item, Item> item, ModelLayerLocation layer, float[] bounds, String name) {
		this.item = item;
		this.layer = layer;
		this.bounds = bounds;
		this.texture = Hemomancy.rloc("textures/entity/book/" + name + ".png");
		this.solid = RenderType.entityCutoutNoCull(texture);
		this.glow = RenderType.entityTranslucentEmissive(texture);
	}

	public RenderType solid() {
		return solid;
	}

	/** The Dictation Table's pending-dictation glow over the book. */
	public RenderType glow() {
		return glow;
	}

	/** Centre of the shut book along an axis (0 x, 1 y, 2 z), in model units. */
	public float centre(int axis) {
		return (bounds[axis] + bounds[axis + 3]) / 2;
	}

	/** The shut book's largest extent across the cover (width or head to foot), in model units. */
	public float span() {
		return Math.max(bounds[3] - bounds[0], bounds[4] - bounds[1]);
	}

	/** The binding drawn for an item, or null when the item is not one of these books. */
	public static HemoBookBinding of(Item item) {
		for (HemoBookBinding binding : values()) {
			if (binding.item.get() == item) return binding;
		}
		return null;
	}

	public static Map<HemoBookBinding, HemoBookModel> bakeAll(EntityModelSet models) {
		Map<HemoBookBinding, HemoBookModel> baked = new EnumMap<>(HemoBookBinding.class);
		for (HemoBookBinding binding : values()) baked.put(binding, new HemoBookModel(models.bakeLayer(binding.layer)));
		return baked;
	}
}
