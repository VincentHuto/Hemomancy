package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.ModelEvent.BakingCompleted;

public final class LowtideRuinObjModels {
	public enum Piece {
		FOUNDATION_RUBBLE("world/lowtide_ruin/foundation_rubble"),
		TOWER_SHORT("world/lowtide_ruin/tower_short"),
		ARCH_FRAGMENT("world/lowtide_ruin/arch_fragment"),
		DOME_CHAPEL("world/lowtide_ruin/dome_chapel"),
		SPIRE_FRAGMENT("world/lowtide_ruin/spire_fragment");

		private final ModelResourceLocation location;
		private BakedModel model;

		Piece(String path) {
			this.location = ModelResourceLocation.standalone(Hemomancy.rloc(path));
		}
	}

	private LowtideRuinObjModels() {
	}

	public static void register(ModelEvent.RegisterAdditional event) {
		for (Piece piece : Piece.values()) {
			event.register(piece.location);
		}
	}

	public static void cache(BakingCompleted event) {
		for (Piece piece : Piece.values()) {
			piece.model = event.getModels().get(piece.location);
		}
		MnemonicLowtideChamberEffects.invalidateLowtideObjRuinCache();
	}

	static BakedModel model(Piece piece) {
		return piece.model;
	}
}
