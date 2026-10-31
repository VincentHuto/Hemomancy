package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * A Brined Court boundary post (Track 8): carved blackstone capped in a house's grown matter, the house's mark inlaid
 * on its plaque. One block for all eight houses through {@link #HOUSE}; one stands at the edge of every outpost and a
 * few stand alone inside each house's water. Models from tools/model_export/house_marker.py.
 */
public class HouseMarkerBlock extends CourtFacingFixtureBlock {
	public static final EnumProperty<CourtHouse> HOUSE = EnumProperty.create("house", CourtHouse.class);
	public static final double[][] OUTLINE = {{3, 0, 3, 13, 2.5, 13}, {4.5, 2.5, 5.5, 11.5, 15, 10.5}};

	public HouseMarkerBlock(Properties properties) {
		super(OUTLINE, properties);
		registerDefaultState(defaultBlockState().setValue(HOUSE, CourtHouse.GIVING_LIGHT));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(HOUSE);
	}
}
