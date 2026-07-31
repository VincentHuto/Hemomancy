package com.vincenthuto.hemomancy.common.recipe.serializer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertFalse;

final class PatternKeyEntryUnloadedChunkTest {
	@Test
	void patternKeysRejectAnUnloadedBlockWithoutCrashingTheServerTick() {
		BlockInWorld unloaded = new BlockInWorld(unloadedLevel(), BlockPos.ZERO, false);
		PatternKeyEntry blockEntry = PatternKeyEntry.fromJsonLiteral(
				"B", "{\"block\":\"minecraft:polished_basalt\"}");
		PatternKeyEntry tagEntry = PatternKeyEntry.fromJsonLiteral(
				"N", "{\"tag\":\"minecraft:base_stone_overworld\","
						+ "\"fallback\":\"minecraft:stone\"}");

		assertFalse(blockEntry.matches(unloaded), "block key");
		assertFalse(tagEntry.matches(unloaded), "tag key");
	}

	private static LevelReader unloadedLevel() {
		return (LevelReader) Proxy.newProxyInstance(
				LevelReader.class.getClassLoader(),
				new Class<?>[] {LevelReader.class},
				(proxy, method, args) -> {
					Class<?> type = method.getReturnType();
					if (type == boolean.class) return false;
					if (type == byte.class) return (byte) 0;
					if (type == short.class) return (short) 0;
					if (type == int.class) return 0;
					if (type == long.class) return 0L;
					if (type == float.class) return 0.0F;
					if (type == double.class) return 0.0D;
					if (type == char.class) return '\0';
					return null;
				});
	}
}
