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

final class CircusPavilionStructureProvider implements DataProvider {
	private final Path output;

	CircusPavilionStructureProvider(PackOutput packOutput) {
		output = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "structure")
				.file(Hemomancy.rloc("circus_pavilion"), "nbt");
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		return CompletableFuture.runAsync(() -> {
			try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
				NbtIo.writeCompressed(createTemplate(), bytes);
				byte[] data = bytes.toByteArray();
				cache.writeIfNeeded(output, data, Hashing.sha1().hashBytes(data));
			} catch (IOException exception) {
				throw new UncheckedIOException(exception);
			}
		});
	}

	@Override
	public String getName() {
		return "Circus pavilion structure";
	}

	private static CompoundTag createTemplate() {
		CompoundTag root = new CompoundTag();
		List<CircusPavilionTemplate.BlockPlacement> placements = CircusPavilionTemplate.blocks();
		root.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
		root.put("size", vector(CircusPavilionTemplate.WIDTH, CircusPavilionTemplate.HEIGHT,
				CircusPavilionTemplate.DEPTH));

		Map<String, Integer> stateIds = new LinkedHashMap<>();
		for (CircusPavilionTemplate.BlockPlacement block : placements) {
			stateIds.computeIfAbsent(block.name(), ignored -> stateIds.size());
		}

		ListTag palette = new ListTag();
		stateIds.keySet().forEach(name -> {
			CompoundTag state = new CompoundTag();
			state.putString("Name", name);
			palette.add(state);
		});
		root.put("palette", palette);

		ListTag blocks = new ListTag();
		for (CircusPavilionTemplate.BlockPlacement placement : placements) {
			CompoundTag block = new CompoundTag();
			block.put("pos", vector(placement.x(), placement.y(), placement.z()));
			block.putInt("state", stateIds.get(placement.name()));
			blocks.add(block);
		}
		root.put("blocks", blocks);
		ListTag entities = new ListTag();
		for (CircusPavilionTemplate.PerformerPlacement placement : CircusPavilionTemplate.performers()) {
			entities.add(entity(placement));
		}
		entities.add(entity(CircusPavilionTemplate.carousel()));
		entities.add(entity(CircusPavilionTemplate.ringmaster()));
		root.put("entities", entities);
		return root;
	}

	private static CompoundTag entity(CircusPavilionTemplate.PerformerPlacement placement) {
		CompoundTag entity = new CompoundTag();
		entity.put("pos", vector(placement.x(), placement.y(), placement.z()));
		entity.put("blockPos", vector((int) placement.x(), (int) placement.y(), (int) placement.z()));
		CompoundTag data = new CompoundTag();
		data.putString("id", placement.entityId());
		data.putBoolean("PersistenceRequired", true);
		entity.put("nbt", data);
		return entity;
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
}
