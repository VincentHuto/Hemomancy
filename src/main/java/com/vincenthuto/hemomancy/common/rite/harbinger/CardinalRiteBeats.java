package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.network.capa.harbinger.CardinalRiteEventPacket;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Supplier;

/**
 * Server side of the rite's discrete beats: one call sends the client event and
 * plays its sound, so every moment the floor, boundary and daemon react to is
 * also heard.
 */
public final class CardinalRiteBeats {
	private CardinalRiteBeats() {
	}

	public static void send(ServerLevel level, ActiveCardinalRite rite, CardinalRiteEventKind kind) {
		send(level, rite, kind, 0, "", List.of(), ItemStack.EMPTY);
	}

	public static void send(ServerLevel level, ActiveCardinalRite rite, CardinalRiteEventKind kind, String style) {
		send(level, rite, kind, 0, style, List.of(), ItemStack.EMPTY);
	}

	public static void send(ServerLevel level, ActiveCardinalRite rite, CardinalRiteEventKind kind, int index,
			String style, List<Vec3> points, ItemStack item) {
		new CardinalRiteEventPacket(rite.getCenterPos(), kind, index, style, points, item).broadcast(level);
	}

	/** An anchor beat carries the anchor's aim point so clients need no recipe lookup. */
	public static void anchor(ServerLevel level, ActiveCardinalRite rite, CardinalRiteRecipe recipe,
			CardinalRiteEventKind kind, int anchorIndex) {
		List<Vec3> points = List.of();
		if (recipe != null && recipe.getCeremony() != null
				&& anchorIndex >= 0 && anchorIndex < recipe.getCeremony().anchors().size()) {
			points = List.of(CardinalRiteTargetGeometry.anchorAimPoint(rite.getCenterPos(),
					recipe.getCeremony().anchors().get(anchorIndex).offset()));
		}
		send(level, rite, kind, anchorIndex, "", points, ItemStack.EMPTY);
	}

	public static void sound(ServerLevel level, BlockPos pos, Supplier<? extends SoundEvent> sound,
			float volume, float pitch) {
		level.playSound(null, pos, sound.get(), SoundSource.BLOCKS, volume, pitch);
	}
}
