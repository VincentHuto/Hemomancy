package com.vincenthuto.hemomancy.common.item.harbinger.tile.functional;

import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypLayer;
import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypLayerRules;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.Optional;

public final class SpecimenJarData {
	public static final String TAG_SPECIMEN = "Specimen";
	public static final String TAG_MORPHLING_LAYERS = "MorphlingLayers";
	public static final String MORPHLING_POLYP_ID = "hemomancy:morphling_polyp";

	private static final String TAG_ID = "id";
	private static final String TAG_POS = "Pos";
	private static final String TAG_MOTION = "Motion";
	private static final String TAG_ROTATION = "Rotation";
	private static final String TAG_UUID = "UUID";
	private static final String TAG_UUID_LEAST = "UUIDLeast";
	private static final String TAG_UUID_MOST = "UUIDMost";

	private SpecimenJarData() {
	}

	public static boolean hasSpecimen(ItemStack stack) {
		return !getSpecimen(stack).isEmpty();
	}

	public static CompoundTag getSpecimen(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data == null) {
			return new CompoundTag();
		}
		CompoundTag root = data.copyTag();
		return root.contains(TAG_SPECIMEN, Tag.TAG_COMPOUND) ? root.getCompound(TAG_SPECIMEN).copy() : new CompoundTag();
	}

	public static void setSpecimen(ItemStack stack, CompoundTag specimen) {
		CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (specimen == null || specimen.isEmpty()) {
			root.remove(TAG_SPECIMEN);
		} else {
			root.put(TAG_SPECIMEN, specimen.copy());
		}
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
	}

	public static void clearSpecimen(ItemStack stack) {
		setSpecimen(stack, new CompoundTag());
	}

	public static ItemStack createStackWithSpecimen(CompoundTag specimen) {
		ItemStack stack = new ItemStack(BlockInit.specimen_jar.get());
		setSpecimen(stack, specimen);
		return stack;
	}

	public static CompoundTag captureEntity(LivingEntity entity) {
		CompoundTag specimen = new CompoundTag();
		if (entity.save(specimen)) {
			sanitizeStoredEntity(specimen);
			return specimen;
		}
		return new CompoundTag();
	}

	public static Optional<Entity> releaseSpecimen(ServerLevel level, BlockPos pos, CompoundTag specimen) {
		if (specimen == null || specimen.isEmpty() || !specimen.contains(TAG_ID, Tag.TAG_STRING)) {
			return Optional.empty();
		}

		CompoundTag releaseTag = specimen.copy();
		prepareReleaseTag(releaseTag, pos);
		Entity released = EntityType.create(releaseTag, level).orElse(null);
		if (released == null) return Optional.empty();
		released.moveTo(pos.getX() + 0.5D, pos.getY() + 0.05D, pos.getZ() + 0.5D,
				released.getYRot(), released.getXRot());
		if (!level.addFreshEntity(released)) return Optional.empty();
		return Optional.of(released);
	}

	public static Component getSpecimenName(CompoundTag specimen) {
		if (specimen == null || !specimen.contains(TAG_ID, Tag.TAG_STRING)) {
			return Component.translatable("tooltip.hemomancy.specimen_jar.empty");
		}
		return EntityType.byString(specimen.getString(TAG_ID))
				.map(EntityType::getDescription)
				.orElse(Component.literal(specimen.getString(TAG_ID)));
	}

	public static Optional<ResourceLocation> getSpecimenEntityId(ItemStack stack) {
		return getSpecimenEntityId(getSpecimen(stack));
	}

	public static Optional<ResourceLocation> getSpecimenEntityId(CompoundTag specimen) {
		if (specimen == null || !specimen.contains(TAG_ID, Tag.TAG_STRING)) {
			return Optional.empty();
		}
		return Optional.ofNullable(ResourceLocation.tryParse(specimen.getString(TAG_ID)));
	}

	public static int getMorphlingLayerMask(CompoundTag specimen) {
		if (specimen == null || !MORPHLING_POLYP_ID.equals(specimen.getString(TAG_ID))) {
			return 0;
		}
		return MorphlingPolypLayerRules.sanitizeMask(specimen.getInt(TAG_MORPHLING_LAYERS));
	}

	public static List<MorphlingPolypLayer> getMorphlingLayers(CompoundTag specimen) {
		return MorphlingPolypLayerRules.layersFromMask(getMorphlingLayerMask(specimen));
	}

	private static void sanitizeStoredEntity(CompoundTag specimen) {
		specimen.remove(TAG_POS);
		specimen.remove(TAG_MOTION);
		specimen.remove(TAG_UUID);
		specimen.remove(TAG_UUID_LEAST);
		specimen.remove(TAG_UUID_MOST);
	}

	private static void prepareReleaseTag(CompoundTag releaseTag, BlockPos pos) {
		releaseTag.remove(TAG_UUID);
		releaseTag.remove(TAG_UUID_LEAST);
		releaseTag.remove(TAG_UUID_MOST);
		ListTag position = new ListTag();
		position.add(net.minecraft.nbt.DoubleTag.valueOf(pos.getX() + 0.5D));
		position.add(net.minecraft.nbt.DoubleTag.valueOf(pos.getY() + 0.05D));
		position.add(net.minecraft.nbt.DoubleTag.valueOf(pos.getZ() + 0.5D));
		releaseTag.put(TAG_POS, position);
		ListTag motion = new ListTag();
		motion.add(net.minecraft.nbt.DoubleTag.valueOf(0.0D));
		motion.add(net.minecraft.nbt.DoubleTag.valueOf(0.0D));
		motion.add(net.minecraft.nbt.DoubleTag.valueOf(0.0D));
		releaseTag.put(TAG_MOTION, motion);
	}
}
