package com.vincenthuto.hemomancy.init;

import java.util.Locale;

import com.vincenthuto.hemomancy.world.structure.BloodTempleStructure;

import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public class PieceInit {

	public static StructurePieceType setPieceId(StructurePieceType p_67164_, String p_67165_) {
		return Registry.register(Registry.STRUCTURE_PIECE, p_67165_.toLowerCase(Locale.ROOT), p_67164_);
	}

	public static StructurePieceType blood_temple_piece = setPieceId(BloodTempleStructure.BloodTemplePiece::new,
			"hemomancy:blood_temple_piece");

}
