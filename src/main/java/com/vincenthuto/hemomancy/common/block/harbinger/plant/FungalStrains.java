package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

/**
 * The fungal strains (core-loop round two, P7): the eight spores, planted. One row per strain: the spore that plants
 * it, the block it grows as, its school and the substrate it wants. Every strain needs blood to grow
 * ({@code hemomancy:needs_blood_to_grow}); the substrate only decides where it may stand. Code reads this table and
 * never names a {@code <strain>_strain} block outside it, {@code BlockInit} and {@code ItemInit}.
 */
public enum FungalStrains implements StringRepresentable {
	VIVACIOUS("vivacious", EnumBloodTemper.ANIMUS, Substrate.FLESH, 0xD23C3E),
	FERVENT("fervent", EnumBloodTemper.FLAMMEUS, Substrate.FLESH, 0xF0963A),
	RUINOUS("ruinous", EnumBloodTemper.MORTEM, Substrate.FLESH, 0x8A9460),
	NEUROTIC("neurotic", EnumBloodTemper.DUCTILIS, Substrate.BRINED, 0xF5D84E),
	FRIGID("frigid", EnumBloodTemper.CONGEATIO, Substrate.BRINED, 0xBEE1F5),
	FERRIC("ferric", EnumBloodTemper.FERRIC, Substrate.BRINED, 0xA65040),
	INCANDESCENT("incandescent", EnumBloodTemper.LUX, Substrate.COMMON, 0xFFF0BE),
	UMBRAL("umbral", EnumBloodTemper.TENEBRIS, Substrate.COMMON, 0x8A50B4);

	/** Every block any strain stands on: the three substrate tags together. */
	public static final TagKey<Block> ANY_SUBSTRATE = TagKey.create(Registries.BLOCK, Hemomancy.rloc("strain_substrates"));

	private final String name;
	private final EnumBloodTemper temper;
	private final Substrate substrate;
	private final int moteColor;

	FungalStrains(String name, EnumBloodTemper temper, Substrate substrate, int moteColor) {
		this.name = name;
		this.temper = temper;
		this.substrate = substrate;
		this.moteColor = moteColor;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	/** The colour of the spores a ripe strain sheds, the bright of its caps (RGB). */
	public int moteColor() {
		return moteColor;
	}

	/** The strain's own word, as its spore and block ids begin. */
	public String strainName() {
		return name;
	}

	/** The spore item that plants it. */
	public String sporeId() {
		return name + "_spores";
	}

	/** The block it grows as. */
	public String blockId() {
		return name + "_strain";
	}

	public EnumBloodTemper temper() {
		return temper;
	}

	public Substrate substrate() {
		return substrate;
	}

	/** The row whose spore has the item path {@code sporePath}, or null. */
	@Nullable
	public static FungalStrains bySpore(String sporePath) {
		for (FungalStrains strain : values()) {
			if (strain.sporeId().equals(sporePath)) return strain;
		}
		return null;
	}

	/**
	 * What a strain stands on. The flesh-feeding strains take Ravenous Heartwood; the Brined ones take Salt-Stained
	 * Timber, inoculated from a voyager's salt; the rest take ordinary fungal ground (podzol, mycelium and the mod's
	 * erythrocytic mycelium).
	 */
	public enum Substrate {
		FLESH("flesh"),
		BRINED("brined"),
		COMMON("common");

		private final String id;
		private final TagKey<Block> tag;

		Substrate(String id) {
			this.id = id;
			this.tag = TagKey.create(Registries.BLOCK, Hemomancy.rloc("strain_substrates/" + id));
		}

		public String id() {
			return id;
		}

		/** The blocks this group's strains stand on. */
		public TagKey<Block> tag() {
			return tag;
		}

		/** The tooltip line a spore of this group carries, naming its substrate. */
		public String tooltipKey() {
			return "item.hemomancy.spores.substrate." + id;
		}
	}
}
