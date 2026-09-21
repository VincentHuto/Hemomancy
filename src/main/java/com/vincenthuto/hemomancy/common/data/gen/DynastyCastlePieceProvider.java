package com.vincenthuto.hemomancy.common.data.gen;

import com.google.common.hash.Hashing;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.SharedConstants;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Writes each dynasty castle piece to a structure {@code .nbt} template, mirroring
 * {@link CircusPavilionStructureProvider}. Pieces carry no jigsaw connector blocks; assembly is
 * handled in code by {@code DynastyCastleStructure}.
 */
final class DynastyCastlePieceProvider implements DataProvider {
	private final PackOutput.PathProvider path;

	DynastyCastlePieceProvider(PackOutput packOutput) {
		this.path = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "structure");
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		return CompletableFuture.allOf(DynastyCastlePieces.pieceNames().stream()
				.map(piece -> writePiece(cache, piece))
				.toArray(CompletableFuture[]::new));
	}

	private CompletableFuture<?> writePiece(CachedOutput cache, String piece) {
		return CompletableFuture.runAsync(() -> {
			Path output = path.file(Hemomancy.rloc("dynasty_castle/" + piece), "nbt");
			try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
				NbtIo.writeCompressed(template(piece), bytes);
				byte[] data = bytes.toByteArray();
				cache.writeIfNeeded(output, data, Hashing.sha1().hashBytes(data));
			} catch (IOException exception) {
				throw new UncheckedIOException(exception);
			}
		});
	}

	private static CompoundTag template(String piece) {
		CompoundTag root = new CompoundTag();
		int[] size = DynastyCastlePieces.size(piece);
		root.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
		root.put("size", vector(size[0], size[1], size[2]));

		Map<String, Integer> stateIds = new LinkedHashMap<>();
		List<DynastyCastlePieces.BlockPlacement> placements = DynastyCastlePieces.blocks(piece);
		for (DynastyCastlePieces.BlockPlacement b : placements) {
			stateIds.computeIfAbsent(b.name(), ignored -> stateIds.size());
		}

		ListTag palette = new ListTag();
		stateIds.keySet().forEach(name -> palette.add(paletteEntry(name)));
		root.put("palette", palette);

		ListTag blocks = new ListTag();
		for (DynastyCastlePieces.BlockPlacement b : placements) {
			CompoundTag block = new CompoundTag();
			block.put("pos", vector(b.x(), b.y(), b.z()));
			block.putInt("state", stateIds.get(b.name()));
			blocks.add(block);
		}
		root.put("blocks", blocks);

		ListTag entities = new ListTag();
		for (DynastyCastlePieces.EntityPlacement e : DynastyCastlePieces.entities(piece)) {
			entities.add(entity(e));
		}
		root.put("entities", entities);
		return root;
	}

	private static CompoundTag entity(DynastyCastlePieces.EntityPlacement placement) {
		CompoundTag entity = new CompoundTag();
		entity.put("pos", vector(placement.x(), placement.y(), placement.z()));
		entity.put("blockPos", vector((int) placement.x(), (int) placement.y(), (int) placement.z()));
		CompoundTag data = new CompoundTag();
		data.putString("id", placement.entityId());
		data.putBoolean("PersistenceRequired", true);
		if (placement.fargoneVariant() >= 0) data.putInt("FargoneVariant", placement.fargoneVariant());
		entity.put("nbt", data);
		return entity;
	}

	/**
	 * Builds a palette entry from a state string of the form {@code "namespace:block"} or
	 * {@code "namespace:block[prop=val,prop=val]"}, so furniture can carry facing/half/hanging/etc.
	 */
	private static CompoundTag paletteEntry(String state) {
		CompoundTag tag = new CompoundTag();
		int bracket = state.indexOf('[');
		if (bracket < 0) {
			tag.putString("Name", state);
			return tag;
		}
		tag.putString("Name", state.substring(0, bracket));
		CompoundTag props = new CompoundTag();
		String inner = state.substring(bracket + 1, state.indexOf(']'));
		for (String kv : inner.split(",")) {
			int eq = kv.indexOf('=');
			props.putString(kv.substring(0, eq).trim(), kv.substring(eq + 1).trim());
		}
		tag.put("Properties", props);
		return tag;
	}

	private static ListTag vector(int x, int y, int z) {
		ListTag vector = new ListTag();
		vector.add(IntTag.valueOf(x));
		vector.add(IntTag.valueOf(y));
		vector.add(IntTag.valueOf(z));
		return vector;
	}

	private static ListTag vector(double x, double y, double z) {
		ListTag vector = new ListTag();
		vector.add(DoubleTag.valueOf(x));
		vector.add(DoubleTag.valueOf(y));
		vector.add(DoubleTag.valueOf(z));
		return vector;
	}

	@Override
	public String getName() {
		return "Dynasty castle pieces";
	}
}
