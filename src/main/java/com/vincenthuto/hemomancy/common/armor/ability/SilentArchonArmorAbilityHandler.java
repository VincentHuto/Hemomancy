package com.vincenthuto.hemomancy.common.armor.ability;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.projectile.CombatWeaponCarrierProjectile;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.IDispellable;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.ITendencyAlignedWeapon;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingToolItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.SyncSilentSlippingStateS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class SilentArchonArmorAbilityHandler {
	private static final String SILENT_SLIPPING_UNTIL_KEY = "hemomancy:silent_slipping_until";
	private static final String SILENT_SLIPPING_MARKER_KEY = "hemomancy:silent_slipping_active";
	private static final String CLIENT_SILENT_SLIPPING_UNTIL_KEY = "hemomancy:client_silent_slipping_until";
	private static final String SILENT_SLIPPING_COOLDOWN_UNTIL_KEY = "hemomancy:silent_slipping_cooldown_until";
	private static final int SILENT_SLIPPING_DURATION_TICKS = 100;
	private static final int SILENT_SLIPPING_COOLDOWN_TICKS = 240;
	private static final double SILENT_SLIPPING_BLOOD_COST = 250.0D;
	private static final int SAFE_EXIT_RADIUS = 6;
	private static final float SILENT_SLIPPING_FLIGHT_SPEED = 0.035F;
	private static final double SILENT_SEVERANCE_RADIUS = 6.0D;
	private static final float SILENT_SEVERANCE_DAMAGE = 6.0F;
	private static final int SILENT_SEVERANCE_EFFECT_TICKS = 100;
	private static final String LIVING_TOOL_PACKAGE =
			"com.vincenthuto.hemomancy.common.item.harbinger.tool.living";
	private static final String HEMOMANCY_PROJECTILE_PACKAGE =
			"com.vincenthuto.hemomancy.common.entity.projectile";

	private SilentArchonArmorAbilityHandler() {
	}

	public static boolean tryToggleSilentSlipping(ServerPlayer player) {
		if (isSilentSlippingActive(player)) {
			activateSilentSlipping(player);
			return true;
		}
		if (!hasFullSilentArchonSet(player)) {
			player.displayClientMessage(Component.literal("The Silent Archon vestments are incomplete.")
					.withStyle(ChatFormatting.RED), true);
			return false;
		}
		long now = player.level().getGameTime();
		long cooldownUntil = player.getPersistentData().getLong(SILENT_SLIPPING_COOLDOWN_UNTIL_KEY);
		if (cooldownUntil > now) {
			player.displayClientMessage(Component.literal("Silent Slipping is re-forming.")
					.withStyle(ChatFormatting.RED), true);
			return false;
		}
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null || !volume.isActive() || volume.getBloodVolume() < SILENT_SLIPPING_BLOOD_COST
				|| !volume.drain(SILENT_SLIPPING_BLOOD_COST)) {
			player.displayClientMessage(Component.literal("Not enough blood.")
					.withStyle(ChatFormatting.RED), true);
			return false;
		}
		BloodVolumeEvents.syncVolume(player, volume);
		player.getPersistentData().putLong(SILENT_SLIPPING_COOLDOWN_UNTIL_KEY,
				now + SILENT_SLIPPING_COOLDOWN_TICKS);
		activateSilentSlipping(player);
		return true;
	}

	public static void activateSilentSlipping(ServerPlayer player) {
		if (isSilentSlippingActive(player)) {
			stopSilentSlipping(player, true, true);
			player.displayClientMessage(Component.literal("Silent Slipping released.").withStyle(ChatFormatting.GRAY), true);
			return;
		}

		long until = player.level().getGameTime() + SILENT_SLIPPING_DURATION_TICKS;
		player.getPersistentData().putLong(SILENT_SLIPPING_UNTIL_KEY, until);
		player.getPersistentData().putBoolean(SILENT_SLIPPING_MARKER_KEY, true);
		applySilentSlippingMovement(player);
		syncSilentSlipping(player, true, SILENT_SLIPPING_DURATION_TICKS);
		player.serverLevel().sendParticles(ParticleTypes.REVERSE_PORTAL,
				player.getX(), player.getY() + 1.0D, player.getZ(),
				42, 0.45D, 0.75D, 0.45D, 0.035D);
		player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
				SoundSource.PLAYERS, 0.6F, 0.55F);
		player.displayClientMessage(Component.literal("Silent Slipping.").withStyle(ChatFormatting.GRAY), true);
	}

	public static void activateSilentArchonSeverance(ServerPlayer player) {
		Vec3 center = player.position().add(0.0D, 1.0D, 0.0D);
		PacketHandler.sendMonolithShatterBurst(center, 64.0D, player.serverLevel());
		player.serverLevel().sendParticles(ParticleTypes.SCULK_SOUL,
				center.x, center.y, center.z,
				54, 0.9D, 0.45D, 0.9D, 0.045D);
		player.serverLevel().sendParticles(ParticleTypes.REVERSE_PORTAL,
				center.x, center.y, center.z,
				72, 1.15D, 0.65D, 1.15D, 0.075D);
		player.level().playSound(null, player.blockPosition(), SoundEvents.SCULK_SHRIEKER_SHRIEK,
				SoundSource.PLAYERS, 0.7F, 0.55F);
		player.level().playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM,
				SoundSource.PLAYERS, 0.45F, 0.7F);

		AABB bounds = player.getBoundingBox().inflate(SILENT_SEVERANCE_RADIUS);
		for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, bounds,
				entity -> entity != player && entity.isAlive() && !entity.isSpectator())) {
			if (target instanceof Player targetPlayer && (targetPlayer.isCreative() || targetPlayer.isSpectator())) {
				continue;
			}
			target.hurt(player.damageSources().magic(), SILENT_SEVERANCE_DAMAGE);
			target.addEffect(new MobEffectInstance(EffectInit.monolithic_dislocation,
					SILENT_SEVERANCE_EFFECT_TICKS, 0, false, true, true));
			Vec3 away = target.position().subtract(player.position());
			if (away.lengthSqr() > 1.0E-4D) {
				target.setDeltaMovement(target.getDeltaMovement().add(away.normalize().scale(0.35D)).add(0.0D, 0.08D, 0.0D));
				target.hurtMarked = true;
			}
		}
		player.displayClientMessage(Component.literal("Silent Severance.").withStyle(ChatFormatting.DARK_GRAY), true);
	}

	public static boolean isSilentSlippingActive(ServerPlayer player) {
		return player.getPersistentData().getLong(SILENT_SLIPPING_UNTIL_KEY) > player.level().getGameTime();
	}

	public static boolean isSilentSlippingNoClipActive(Player player) {
		if (player.level().isClientSide()) {
			return player.getPersistentData().getLong(CLIENT_SILENT_SLIPPING_UNTIL_KEY) > player.level().getGameTime();
		}
		return player instanceof ServerPlayer serverPlayer && isSilentSlippingActive(serverPlayer);
	}

	public static void applySilentSlippingNoClip(Player player) {
		if (!isSilentSlippingNoClipActive(player)) {
			return;
		}
		player.noPhysics = true;
		player.fallDistance = 0.0F;
		player.resetFallDistance();
	}

	public static void handleClientSilentSlippingState(Player player, boolean active, int remainingTicks) {
		if (active && remainingTicks > 0) {
			player.getPersistentData().putLong(CLIENT_SILENT_SLIPPING_UNTIL_KEY,
					player.level().getGameTime() + remainingTicks);
			applySilentSlippingNoClip(player);
		} else {
			player.getPersistentData().remove(CLIENT_SILENT_SLIPPING_UNTIL_KEY);
			if (!player.isSpectator()) {
				player.noPhysics = false;
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (player.level().isClientSide()) {
			updateClientSilentSlipping(player);
			return;
		}
		if (player instanceof ServerPlayer serverPlayer) {
			updateSilentSlipping(serverPlayer);
		}
	}

	@SubscribeEvent
	public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
		if (suppressMonolithicDislocationOffense(event)) {
			return;
		}
		negateMonolithicDislocationIncomingDamage(event);
	}

	@SubscribeEvent
	public static void onLivingDamage(LivingDamageEvent.Pre event) {
		if (suppressMonolithicDislocationOffense(event)) {
			return;
		}
		if (negateMonolithicDislocationIncomingDamage(event)) {
			return;
		}
		if (event.getEntity() instanceof ServerPlayer player && negateIncomingPhysicalDamage(player, event)) {
			return;
		}
		suppressMundanePhysicalOffense(event);
	}

	public static void updateSilentSlipping(ServerPlayer player) {
		boolean active = isSilentSlippingActive(player);
		boolean marked = player.getPersistentData().getBoolean(SILENT_SLIPPING_MARKER_KEY);
		if (active) {
			if (!hasFullSilentArchonSet(player)) {
				stopSilentSlipping(player, true, true);
				return;
			}
			applySilentSlippingMovement(player);
			if (player.tickCount % 10 == 0) {
				player.serverLevel().sendParticles(ParticleTypes.REVERSE_PORTAL,
						player.getX(), player.getY() + 1.0D, player.getZ(),
						8, 0.25D, 0.45D, 0.25D, 0.015D);
			}
		} else if (marked) {
			stopSilentSlipping(player, true, true);
		}
	}

	public static boolean negateMonolithicDislocationIncomingDamage(LivingDamageEvent.Pre event) {
		if (!event.getEntity().hasEffect(EffectInit.monolithic_dislocation)
				|| isAllowedIntangibleDamage(event.getSource())) {
			return false;
		}
		event.setNewDamage(0.0F);
		event.getEntity().fallDistance = 0.0F;
		event.getEntity().resetFallDistance();
		return true;
	}

	public static boolean negateMonolithicDislocationIncomingDamage(LivingIncomingDamageEvent event) {
		if (!event.getEntity().hasEffect(EffectInit.monolithic_dislocation)
				|| isAllowedIntangibleDamage(event.getSource())) {
			return false;
		}
		event.setAmount(0.0F);
		event.setCanceled(true);
		event.getEntity().fallDistance = 0.0F;
		event.getEntity().resetFallDistance();
		return true;
	}

	public static boolean suppressMonolithicDislocationOffense(LivingDamageEvent.Pre event) {
		if (!isMonolithicDislocationDamageSource(event.getSource())) {
			return false;
		}
		event.setNewDamage(0.0F);
		return true;
	}

	public static boolean suppressMonolithicDislocationOffense(LivingIncomingDamageEvent event) {
		if (!isMonolithicDislocationDamageSource(event.getSource())) {
			return false;
		}
		event.setAmount(0.0F);
		event.setCanceled(true);
		return true;
	}

	public static boolean negateIncomingPhysicalDamage(ServerPlayer player, LivingDamageEvent.Pre event) {
		if (!hasFullSilentArchonSet(player) || !isPhysicalDamage(event.getSource())) {
			return false;
		}
		event.setNewDamage(0.0F);
		player.fallDistance = 0.0F;
		player.resetFallDistance();
		if (player.tickCount % 5 == 0) {
			player.serverLevel().sendParticles(ParticleTypes.SMOKE,
					player.getX(), player.getY() + 1.0D, player.getZ(),
					12, 0.35D, 0.55D, 0.35D, 0.02D);
		}
		return true;
	}

	public static boolean suppressMundanePhysicalOffense(LivingDamageEvent.Pre event) {
		if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)
				|| !hasFullSilentArchonSet(attacker)
				|| !isPhysicalDamage(event.getSource())
				|| isAllowedSilentArchonOffense(attacker, event.getSource())) {
			return false;
		}
		event.setNewDamage(0.0F);
		return true;
	}

	public static boolean isPhysicalDamage(DamageSource source) {
		if (source.is(DamageTypeTags.IS_PROJECTILE)
				|| source.is(DamageTypeTags.IS_EXPLOSION)
				|| source.is(DamageTypeTags.IS_FALL)
				|| source.is(DamageTypes.FALL)
				|| source.is(DamageTypes.FLY_INTO_WALL)
				|| source.is(DamageTypes.FALLING_BLOCK)
				|| source.is(DamageTypes.IN_WALL)) {
			return true;
		}
		Entity direct = source.getDirectEntity();
		if (direct instanceof Projectile) {
			return true;
		}
		return source.isDirect() && source.getEntity() instanceof LivingEntity;
	}

	public static boolean hasFullSilentArchonSet(Player player) {
		return player.getItemBySlot(EquipmentSlot.HEAD).is(ItemInit.silent_archon_helm.get())
				&& player.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.silent_archon_chestplate.get())
				&& player.getItemBySlot(EquipmentSlot.LEGS).is(ItemInit.silent_archon_leggings.get())
				&& player.getItemBySlot(EquipmentSlot.FEET).is(ItemInit.silent_archon_boots.get());
	}

	private static boolean isAllowedSilentArchonOffense(ServerPlayer attacker, DamageSource source) {
		if (isDirectLivingWeaponDamage(attacker, source)) {
			return true;
		}
		return isAllowedIntangibleDamage(source);
	}

	public static boolean isAllowedIntangibleDamage(DamageSource source) {
		Entity direct = source.getDirectEntity();
		if (direct instanceof CombatWeaponCarrierProjectile carrier) {
			return isLivingWeapon(carrier.getCombatWeaponItem());
		}
		if (isHemomancyProjectile(direct)) {
			return true;
		}
		if (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC)) {
			return true;
		}
		return isDirectLivingWeaponDamage(source);
	}

	private static boolean isDirectLivingWeaponDamage(DamageSource source) {
		if (!(source.getEntity() instanceof LivingEntity attacker)) {
			return false;
		}
		return isDirectLivingWeaponDamage(attacker, source);
	}

	private static boolean isDirectLivingWeaponDamage(LivingEntity attacker, DamageSource source) {
		return source.isDirect()
				&& (isLivingWeapon(attacker.getItemBySlot(EquipmentSlot.MAINHAND))
				|| isLivingWeapon(attacker.getItemBySlot(EquipmentSlot.OFFHAND)));
	}

	public static boolean isLivingWeapon(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		Item item = stack.getItem();
		return item instanceof ITendencyAlignedWeapon
				|| item instanceof IDispellable
				|| item instanceof LivingToolItem
				|| item instanceof LivingItem
				|| item.getClass().getName().startsWith(LIVING_TOOL_PACKAGE);
	}

	public static boolean isHemomancyProjectile(Entity entity) {
		return entity != null && entity.getClass().getName().startsWith(HEMOMANCY_PROJECTILE_PACKAGE);
	}

	private static boolean isMonolithicDislocationDamageSource(DamageSource source) {
		Entity sourceEntity = source.getEntity();
		if (sourceEntity instanceof LivingEntity attacker
				&& attacker.hasEffect(EffectInit.monolithic_dislocation)) {
			return true;
		}
		Entity directEntity = source.getDirectEntity();
		return directEntity instanceof LivingEntity attacker
				&& attacker.hasEffect(EffectInit.monolithic_dislocation);
	}

	private static void updateClientSilentSlipping(Player player) {
		long until = player.getPersistentData().getLong(CLIENT_SILENT_SLIPPING_UNTIL_KEY);
		if (until > player.level().getGameTime()) {
			applySilentSlippingNoClip(player);
		} else if (player.getPersistentData().contains(CLIENT_SILENT_SLIPPING_UNTIL_KEY)) {
			player.getPersistentData().remove(CLIENT_SILENT_SLIPPING_UNTIL_KEY);
			if (!player.isSpectator()) {
				player.noPhysics = false;
			}
		}
	}

	private static void applySilentSlippingMovement(ServerPlayer player) {
		applySilentSlippingNoClip(player);
		player.getAbilities().mayfly = true;
		player.getAbilities().flying = true;
		player.getAbilities().setFlyingSpeed(SILENT_SLIPPING_FLIGHT_SPEED);
		player.onUpdateAbilities();
		player.setDeltaMovement(player.getDeltaMovement().multiply(0.82D, 0.82D, 0.82D));
	}

	private static void stopSilentSlipping(ServerPlayer player, boolean moveToSafePosition, boolean syncClient) {
		player.getPersistentData().remove(SILENT_SLIPPING_UNTIL_KEY);
		player.getPersistentData().remove(SILENT_SLIPPING_MARKER_KEY);
		if (moveToSafePosition && isInsideBlock(player)) {
			BlockPos safePos = findNearestSafePosition(player);
			if (safePos != null) {
				player.teleportTo(safePos.getX() + 0.5D, safePos.getY(), safePos.getZ() + 0.5D);
			}
		}
		if (!player.isSpectator()) {
			player.noPhysics = false;
		}
		if (!player.isCreative() && !player.isSpectator()) {
			player.getAbilities().mayfly = false;
			player.getAbilities().flying = false;
		}
		player.getAbilities().setFlyingSpeed(0.05F);
		player.onUpdateAbilities();
		player.fallDistance = 0.0F;
		player.resetFallDistance();
		if (syncClient) {
			syncSilentSlipping(player, false, 0);
		}
	}

	private static void syncSilentSlipping(ServerPlayer player, boolean active, int remainingTicks) {
		PacketHandler.sendToPlayer(player, new SyncSilentSlippingStateS2CPacket(active, remainingTicks));
	}

	private static boolean isInsideBlock(Player player) {
		return !player.level().noCollision(player, player.getBoundingBox().deflate(1.0E-7D));
	}

	private static BlockPos findNearestSafePosition(ServerPlayer player) {
		BlockPos center = player.blockPosition();
		for (int radius = 0; radius <= SAFE_EXIT_RADIUS; radius++) {
			for (int y = -radius; y <= radius; y++) {
				for (int x = -radius; x <= radius; x++) {
					for (int z = -radius; z <= radius; z++) {
						BlockPos candidate = center.offset(x, y, z);
						if (isSafeStandingPosition(player, candidate)) {
							return candidate;
						}
					}
				}
			}
		}
		for (int radius = 0; radius <= SAFE_EXIT_RADIUS; radius++) {
			for (int y = -radius; y <= radius; y++) {
				for (int x = -radius; x <= radius; x++) {
					for (int z = -radius; z <= radius; z++) {
						BlockPos candidate = center.offset(x, y, z);
						if (isOpenPosition(player, candidate)) {
							return candidate;
						}
					}
				}
			}
		}
		return null;
	}

	private static boolean isSafeStandingPosition(ServerPlayer player, BlockPos pos) {
		BlockState feet = player.level().getBlockState(pos);
		BlockState head = player.level().getBlockState(pos.above());
		BlockState below = player.level().getBlockState(pos.below());
		return (feet.isAir() || feet.getCollisionShape(player.level(), pos).isEmpty())
				&& (head.isAir() || head.getCollisionShape(player.level(), pos.above()).isEmpty())
				&& !below.isAir()
				&& below.getBlock() != Blocks.LAVA
				&& !below.getCollisionShape(player.level(), pos.below()).isEmpty();
	}

	private static boolean isOpenPosition(ServerPlayer player, BlockPos pos) {
		BlockState feet = player.level().getBlockState(pos);
		BlockState head = player.level().getBlockState(pos.above());
		return (feet.isAir() || feet.getCollisionShape(player.level(), pos).isEmpty())
				&& (head.isAir() || head.getCollisionShape(player.level(), pos.above()).isEmpty());
	}
}
