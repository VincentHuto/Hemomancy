package com.vincenthuto.hemomancy.common.tile.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.WardAnchorBlock;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Rotation;
import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A Saint mausoleum's ward, held where the client can see it. The template gives the Saint and the threshold slab as
 * offsets from the anchor; when the complex is placed, {@code MausoleumStructure.afterPlace} settles it with the
 * piece's rotation and the world boxes of the warded pieces (past the threshold) and of every hallowed piece.
 */
public class WardAnchorBlockEntity extends HemoBlockEntity {
	private EnumSaintType saint = EnumSaintType.HEMORATH;
	private int[] thresholdFrom = {0, 0, 0};
	private int[] thresholdTo = {0, 0, 0};
	private boolean settled;
	@Nullable
	private AABB threshold;
	private final List<BoundingBox> ward = new ArrayList<>();
	private final List<BoundingBox> hallowed = new ArrayList<>();

	public WardAnchorBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.ward_anchor.get(), pos, state);
	}

	public EnumSaintType saint() {
		return saint;
	}

	public Direction inward() {
		BlockState state = getBlockState();
		return state.hasProperty(WardAnchorBlock.FACING) ? state.getValue(WardAnchorBlock.FACING) : Direction.EAST;
	}

	public boolean settled() {
		return settled;
	}

	/** The threshold slab in the world, one block thick across the corridor; null until settled. */
	@Nullable
	public AABB threshold() {
		return threshold;
	}

	public List<BoundingBox> wardBoxes() {
		return ward;
	}

	public List<BoundingBox> hallowedBoxes() {
		return hallowed;
	}

	public boolean inWard(Vec3 point) {
		return contains(ward, point);
	}

	public boolean inHallowed(Vec3 point) {
		return contains(hallowed, point);
	}

	private static boolean contains(List<BoundingBox> boxes, Vec3 point) {
		BlockPos pos = BlockPos.containing(point);
		for (BoundingBox box : boxes) {
			if (box.isInside(pos)) return true;
		}
		return false;
	}

	/**
	 * Places the template's threshold slab in the world for a piece turned by {@code rotation}, and takes the warded
	 * and hallowed piece boxes.
	 */
	public void settle(Rotation rotation, List<BoundingBox> wardBoxes, List<BoundingBox> hallowedBoxes) {
		BlockPos from = getBlockPos().offset(rotate(thresholdFrom, rotation));
		BlockPos to = getBlockPos().offset(rotate(thresholdTo, rotation));
		threshold = AABB.encapsulatingFullBlocks(from, to);
		ward.clear();
		ward.addAll(wardBoxes);
		hallowed.clear();
		hallowed.addAll(hallowedBoxes);
		settled = true;
		BlockEntitySync.markAndSync(this);
	}

	private static BlockPos rotate(int[] offset, Rotation rotation) {
		return new BlockPos(offset[0], offset[1], offset[2]).rotate(rotation);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (level != null) WardAnchors.add(level, this);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (level != null) WardAnchors.remove(level, this);
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		if (level != null) WardAnchors.remove(level, this);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		writeWard(tag);
		tag.putIntArray("ThresholdFrom", thresholdFrom);
		tag.putIntArray("ThresholdTo", thresholdTo);
	}

	private void writeWard(CompoundTag tag) {
		tag.putString("Saint", saint.name());
		tag.putBoolean("Settled", settled);
		if (threshold != null) {
			ListTag box = new ListTag();
			for (double value : new double[] {threshold.minX, threshold.minY, threshold.minZ, threshold.maxX,
					threshold.maxY, threshold.maxZ}) {
				box.add(net.minecraft.nbt.DoubleTag.valueOf(value));
			}
			tag.put("Threshold", box);
		}
		tag.put("Ward", writeBoxes(ward));
		tag.put("Hallowed", writeBoxes(hallowed));
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		try {
			saint = EnumSaintType.valueOf(tag.getString("Saint").toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException unknown) {
			saint = EnumSaintType.HEMORATH;
		}
		if (tag.contains("ThresholdFrom", Tag.TAG_INT_ARRAY)) thresholdFrom = offset(tag.getIntArray("ThresholdFrom"));
		if (tag.contains("ThresholdTo", Tag.TAG_INT_ARRAY)) thresholdTo = offset(tag.getIntArray("ThresholdTo"));
		settled = tag.getBoolean("Settled");
		ListTag box = tag.getList("Threshold", Tag.TAG_DOUBLE);
		threshold = box.size() == 6 ? new AABB(box.getDouble(0), box.getDouble(1), box.getDouble(2), box.getDouble(3),
				box.getDouble(4), box.getDouble(5)) : null;
		readBoxes(tag.getList("Ward", Tag.TAG_INT_ARRAY), ward);
		readBoxes(tag.getList("Hallowed", Tag.TAG_INT_ARRAY), hallowed);
	}

	private static int[] offset(int[] values) {
		return values.length == 3 ? values : new int[] {0, 0, 0};
	}

	private static ListTag writeBoxes(List<BoundingBox> boxes) {
		ListTag list = new ListTag();
		for (BoundingBox box : boxes) {
			list.add(new IntArrayTag(new int[] {box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ()}));
		}
		return list;
	}

	private static void readBoxes(ListTag list, List<BoundingBox> into) {
		into.clear();
		for (int i = 0; i < list.size(); i++) {
			int[] v = list.getIntArray(i);
			if (v.length == 6) into.add(new BoundingBox(v[0], v[1], v[2], v[3], v[4], v[5]));
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		writeWard(tag);
		return tag;
	}
}
