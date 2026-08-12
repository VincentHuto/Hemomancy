package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.entity.projectile.LivingFlailHeadProjectileEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public final class LivingFlailDeployment {
	public static final String DEPLOYMENT_KEY = "HemomancyLivingFlailDeployment";

	private LivingFlailDeployment() {
	}

	public static boolean isDeployed(ItemStack stack) {
		return deploymentId(stack).isPresent();
	}

	public static Optional<UUID> deploymentId(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data == null) return Optional.empty();
		CompoundTag tag = data.copyTag();
		return readDeployment(tag);
	}

	public static Optional<UUID> readDeployment(CompoundTag tag) {
		return tag.hasUUID(DEPLOYMENT_KEY) ? Optional.of(tag.getUUID(DEPLOYMENT_KEY)) : Optional.empty();
	}

	public static void writeDeployment(CompoundTag tag, UUID deploymentId) {
		tag.putUUID(DEPLOYMENT_KEY, deploymentId);
	}

	public static void markDeployed(ItemStack stack, UUID deploymentId) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		writeDeployment(tag, deploymentId);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static boolean clear(ItemStack stack, UUID deploymentId) {
		if (!deploymentId(stack).filter(deploymentId::equals).isPresent()) return false;
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.remove(DEPLOYMENT_KEY);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return true;
	}

	public static void reconcile(Player player) {
		forEachStack(player, stack -> deploymentId(stack).ifPresent(id -> {
			discardProjectile(player, id);
			clear(stack, id);
		}));
	}

	public static void reconcileMissingProjectile(ServerPlayer player) {
		forEachStack(player, stack -> deploymentId(stack).ifPresent(id -> {
			Entity entity = findProjectile(player, id);
			boolean present = entity instanceof LivingFlailHeadProjectileEntity projectile
					&& player.getUUID().equals(projectile.getOwnerUuid());
			boolean sameDimension = present && entity.level() == player.level();
			if (LivingFlailRecoveryRules.shouldRecover(true, present, sameDimension, false)) {
				if (entity != null) entity.discard();
				clear(stack, id);
			}
		}));
	}

	public static void reconcileForRestoration(ItemStack stack, Player player) {
		deploymentId(stack).ifPresent(id -> {
			discardProjectile(player, id);
			clear(stack, id);
		});
	}

	public static boolean restoreHead(Player player, UUID deploymentId) {
		final boolean[] restored = {false};
		forEachStack(player, stack -> restored[0] |= clear(stack, deploymentId));
		return restored[0];
	}

	public static boolean hasDeployment(Player player, UUID deploymentId) {
		final boolean[] found = {false};
		forEachStack(player, stack -> found[0] |= deploymentId(stack).filter(deploymentId::equals).isPresent());
		return found[0];
	}

	@Nullable
	private static Entity findProjectile(Player player, UUID deploymentId) {
		if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.getServer() == null) return null;
		for (ServerLevel level : serverPlayer.getServer().getAllLevels()) {
			Entity entity = level.getEntity(deploymentId);
			if (entity != null) return entity;
		}
		return null;
	}

	private static void discardProjectile(Player player, UUID deploymentId) {
		Entity entity = findProjectile(player, deploymentId);
		if (entity != null) entity.discard();
	}

	private static void forEachStack(Player player, java.util.function.Consumer<ItemStack> consumer) {
		consumer.accept(player.getMainHandItem());
		consumer.accept(player.getOffhandItem());
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (stack != player.getMainHandItem() && stack != player.getOffhandItem()) consumer.accept(stack);
		}
	}
}
