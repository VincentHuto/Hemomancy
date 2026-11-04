package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.shared.StrippableLogBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * One family of wood with the vanilla set of building pieces: log, bark, both stripped, planks, slab, stairs, fence,
 * gate, door, trapdoor, button, pressure plate, and standing and hanging signs. Ids follow vanilla's pattern with the
 * family's name where vanilla says "oak", except that vanilla's "oak_wood" is {@code <family>_bark}. Boats are not
 * part of a set: they are not building pieces, and each needs a Boat.Type extension and its own entity textures.
 * The tree-only blocks (leaves, the core) stay with their owners in {@link BlockInit}.
 */
public record WoodSet(String name, WoodType woodType,
		DeferredHolder<Block, Block> log, DeferredHolder<Block, Block> bark,
		DeferredHolder<Block, Block> strippedLog, DeferredHolder<Block, Block> strippedBark,
		DeferredHolder<Block, Block> planks, DeferredHolder<Block, Block> slab, DeferredHolder<Block, Block> stairs,
		DeferredHolder<Block, Block> fence, DeferredHolder<Block, Block> fenceGate,
		DeferredHolder<Block, Block> door, DeferredHolder<Block, Block> trapdoor,
		DeferredHolder<Block, Block> button, DeferredHolder<Block, Block> pressurePlate,
		DeferredHolder<Block, Block> sign, DeferredHolder<Block, Block> wallSign,
		DeferredHolder<Block, Block> hangingSign, DeferredHolder<Block, Block> wallHangingSign) {
	private static final List<WoodSet> ALL = new ArrayList<>();

	public static List<WoodSet> all() {
		return List.copyOf(ALL);
	}

	/** Registers the family's whole set, in the block registers the rest of the mod uses. */
	public static WoodSet register(String name, MapColor color) {
		String id = Hemomancy.rloc(name).toString();
		BlockSetType setType = BlockSetType.register(new BlockSetType(id));
		WoodType woodType = WoodType.register(new WoodType(id, setType));

		DeferredHolder<Block, Block> strippedLog = BlockInit.COLUMNBLOCKS.register("stripped_" + name + "_log",
				() -> new RotatedPillarBlock(wood(color)));
		DeferredHolder<Block, Block> strippedBark = BlockInit.COLUMNBLOCKS.register("stripped_" + name + "_bark",
				() -> new RotatedPillarBlock(wood(color)));
		DeferredHolder<Block, Block> log = BlockInit.COLUMNBLOCKS.register(name + "_log",
				() -> new StrippableLogBlock(wood(color), strippedLog));
		DeferredHolder<Block, Block> bark = BlockInit.COLUMNBLOCKS.register(name + "_bark",
				() -> new StrippableLogBlock(wood(color), strippedBark));
		DeferredHolder<Block, Block> planks = BlockInit.BASEBLOCKS.register(name + "_planks", () -> new Block(wood(color)));
		DeferredHolder<Block, Block> slab = BlockInit.SLABBLOCKS.register(name + "_slab", () -> new SlabBlock(wood(color)));
		DeferredHolder<Block, Block> stairs = BlockInit.STAIRBLOCKS.register(name + "_stairs",
				() -> new StairBlock(planks.get().defaultBlockState(), wood(color)));
		DeferredHolder<Block, Block> fence = BlockInit.SPECIALBLOCKS.register(name + "_fence",
				() -> new FenceBlock(wood(color).forceSolidOn()));
		DeferredHolder<Block, Block> fenceGate = BlockInit.SPECIALBLOCKS.register(name + "_fence_gate",
				() -> new FenceGateBlock(woodType, wood(color).forceSolidOn()));
		DeferredHolder<Block, Block> door = BlockInit.SPECIALBLOCKS.register(name + "_door",
				() -> new DoorBlock(setType, wood(color).strength(3.0F).noOcclusion().pushReaction(PushReaction.DESTROY)));
		DeferredHolder<Block, Block> trapdoor = BlockInit.SPECIALBLOCKS.register(name + "_trapdoor",
				() -> new TrapDoorBlock(setType, wood(color).strength(3.0F).noOcclusion()
						.isValidSpawn((state, level, pos, type) -> false)));
		DeferredHolder<Block, Block> button = BlockInit.SPECIALBLOCKS.register(name + "_button",
				() -> new ButtonBlock(setType, 30, BlockBehaviour.Properties.of().noCollission().strength(0.5F)
						.pushReaction(PushReaction.DESTROY)));
		DeferredHolder<Block, Block> pressurePlate = BlockInit.SPECIALBLOCKS.register(name + "_pressure_plate",
				() -> new PressurePlateBlock(setType, wood(color).forceSolidOn().noCollission().strength(0.5F)
						.pushReaction(PushReaction.DESTROY)));
		DeferredHolder<Block, Block> sign = BlockInit.SPECIALBLOCKS.register(name + "_sign",
				() -> new StandingSignBlock(woodType, signProperties(color)));
		DeferredHolder<Block, Block> wallSign = BlockInit.SPECIALBLOCKS.register(name + "_wall_sign",
				() -> new WallSignBlock(woodType, signProperties(color).dropsLike(sign.get())));
		DeferredHolder<Block, Block> hangingSign = BlockInit.SPECIALBLOCKS.register(name + "_hanging_sign",
				() -> new CeilingHangingSignBlock(woodType, signProperties(color)));
		DeferredHolder<Block, Block> wallHangingSign = BlockInit.SPECIALBLOCKS.register(name + "_wall_hanging_sign",
				() -> new WallHangingSignBlock(woodType, signProperties(color).dropsLike(hangingSign.get())));

		WoodSet set = new WoodSet(name, woodType, log, bark, strippedLog, strippedBark, planks, slab, stairs, fence, fenceGate,
				door, trapdoor, button, pressurePlate, sign, wallSign, hangingSign, wallHangingSign);
		ALL.add(set);
		return set;
	}

	private static BlockBehaviour.Properties wood(MapColor color) {
		return BlockBehaviour.Properties.of().mapColor(color).instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F).sound(SoundType.WOOD);
	}

	private static BlockBehaviour.Properties signProperties(MapColor color) {
		return BlockBehaviour.Properties.of().mapColor(color).forceSolidOn().instrument(NoteBlockInstrument.BASS)
				.noCollission().strength(1.0F);
	}

	/** A wall sign is placed by its standing sign's item and has none of its own. */
	public static boolean isItemless(String blockPath) {
		return ALL.stream().anyMatch(set -> blockPath.equals(set.name + "_wall_sign") || blockPath.equals(set.name + "_wall_hanging_sign"));
	}

	/** The item a set's door or sign needs in place of a plain block item, or null for an ordinary piece. */
	@Nullable
	public static Function<Block, BlockItem> itemFactory(String blockPath) {
		for (WoodSet set : ALL) {
			if (blockPath.equals(set.name + "_door")) {
				return block -> new DoubleHighBlockItem(block, new Item.Properties());
			}
			if (blockPath.equals(set.name + "_sign")) {
				return block -> new SignItem(new Item.Properties().stacksTo(16), block, set.wallSign.get());
			}
			if (blockPath.equals(set.name + "_hanging_sign")) {
				return block -> new HangingSignItem(block, set.wallHangingSign.get(), new Item.Properties().stacksTo(16));
			}
		}
		return null;
	}
}
