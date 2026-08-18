package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.SeveredQliphothState;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public final class LivingSicklePruning {
	public static final String STORED_WEAPON_KEY = "HemomancyStoredLivingWeapon";
	public static final String BLOOM_DIMENSION_KEY = "HemomancySickleBloomDimension";
	public static final String BLOOM_POS_KEY = "HemomancySickleBloomPos";
	public static final String BLOOM_OWNER_KEY = "HemomancySickleBloomOwner";

	private LivingSicklePruning() {
	}

	public static boolean interact(Level level, BlockPos pos, Player player, InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);
		if (!isTemporarySickle(held) && !isBaseLivingWeapon(held)) return false;
		if (level.isClientSide) return true;
		if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) return false;

		QliphothBloomSavedData data = QliphothBloomSavedData.get(serverLevel.getServer().overworld());
		String dimension = level.dimension().location().toString();
		QliphothBloomSavedData.BloomEntry bloom = data.getBloomAt(pos, dimension);
		if (bloom == null || !bloom.center().equals(pos)) return false;

		if (isTemporarySickle(held)) {
			if (!isBoundTo(held, bloom, dimension) || !eligible(serverPlayer, bloom, data)) {
				serverPlayer.displayClientMessage(Component.literal("The sickle refuses a wound outside its making.")
						.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
				return true;
			}
			if (!data.severBloom(pos)) return true;
			player.setItemInHand(hand, restoredWeaponStack(held, player.registryAccess()));
			HarbingerCardinalRiteEvents.syncQliphothBlooms(serverLevel.getServer());
			serverLevel.playSound(null, pos, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.BLOCKS, 1.1F, 0.55F);
			serverLevel.sendParticles(ParticleTypes.SQUID_INK, pos.getX() + 0.5D, pos.getY() + 1.1D,
					pos.getZ() + 0.5D, 30, 0.8D, 1.0D, 0.8D, 0.04D);
			serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, pos.getX() + 0.5D, pos.getY() + 1.1D,
					pos.getZ() + 0.5D, 45, 0.8D, 1.0D, 0.8D, 0.025D);
			serverPlayer.displayClientMessage(Component.literal(
					"The living edge parts the trunk. The final refusal waits inside the wound.")
					.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), false);
			return true;
		}

		if (!eligible(serverPlayer, bloom, data)) {
			serverPlayer.displayClientMessage(Component.literal("The tree waits for a valid refusal.")
					.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
			return true;
		}
		player.setItemInHand(hand, createTemporarySickle(held, bloom, dimension, player.registryAccess()));
		serverLevel.playSound(null, pos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.PLAYERS, 0.75F, 0.7F);
		serverPlayer.displayClientMessage(Component.literal("The assumed limb hooks inward, remembering how death harvests.")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
		return true;
	}

	public static boolean isTemporarySickle(ItemStack stack) {
		if (stack.isEmpty() || !stack.is(ItemInit.living_sickle.get())) return false;
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data != null && data.copyTag().contains(STORED_WEAPON_KEY, Tag.TAG_COMPOUND);
	}

	public static ItemStack restoredWeaponStack(ItemStack sickle, HolderLookup.Provider registryAccess) {
		CustomData data = sickle.get(DataComponents.CUSTOM_DATA);
		if (data != null) {
			CompoundTag root = data.copyTag();
			if (root.contains(STORED_WEAPON_KEY, Tag.TAG_COMPOUND)) {
				ItemStack restored = ItemStack.parseOptional(registryAccess, root.getCompound(STORED_WEAPON_KEY));
				if (!restored.isEmpty() && isBaseLivingWeapon(restored)) {
					restored.setCount(1);
					return restored;
				}
			}
		}
		return new ItemStack(ItemInit.living_staff.get());
	}

	public static boolean isBaseLivingWeapon(ItemStack stack) {
		if (!LivingStaffWeaponFormHelper.isTransformedStaffWeapon(stack)) return false;
		Item item = stack.getItem();
		return item == ItemInit.living_blade.get() || item == ItemInit.living_axe.get()
				|| item == ItemInit.living_spear.get() || item == ItemInit.living_baghnakh.get()
				|| item == ItemInit.living_crossbow.get() || item == ItemInit.living_torch.get()
				|| item == ItemInit.living_flail.get();
	}

	private static boolean eligible(ServerPlayer player, QliphothBloomSavedData.BloomEntry bloom,
			QliphothBloomSavedData data) {
		return bloom.ownerUUID().equals(player.getUUID())
				&& data.getState(bloom.center()) == SeveredQliphothState.LIVING
				&& HemoCapabilityAccess.getInitiatoryDegree(player)
						.map(degree -> degree.getDegreeNumber() == 7
								&& degree.getArchonPath() == EnumArchonPath.SILENT_PENDING
								&& degree.isQliphothCommunionDone()
								&& degree.getTotalPomesConsumed() >= 9)
						.orElse(false);
	}

	private static ItemStack createTemporarySickle(ItemStack source,
			QliphothBloomSavedData.BloomEntry bloom, String dimension, HolderLookup.Provider registryAccess) {
		ItemStack sickle = new ItemStack(ItemInit.living_sickle.get());
		CompoundTag root = new CompoundTag();
		Tag saved = source.copyWithCount(1).save(registryAccess);
		if (saved instanceof CompoundTag compound) root.put(STORED_WEAPON_KEY, compound);
		root.putString(BLOOM_DIMENSION_KEY, dimension);
		root.putLong(BLOOM_POS_KEY, bloom.center().asLong());
		root.putUUID(BLOOM_OWNER_KEY, bloom.ownerUUID());
		sickle.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
		return sickle;
	}

	private static boolean isBoundTo(ItemStack sickle, QliphothBloomSavedData.BloomEntry bloom, String dimension) {
		CustomData data = sickle.get(DataComponents.CUSTOM_DATA);
		if (data == null) return false;
		CompoundTag root = data.copyTag();
		return dimension.equals(root.getString(BLOOM_DIMENSION_KEY))
				&& root.getLong(BLOOM_POS_KEY) == bloom.center().asLong()
				&& root.hasUUID(BLOOM_OWNER_KEY)
				&& root.getUUID(BLOOM_OWNER_KEY).equals(bloom.ownerUUID());
	}
}
