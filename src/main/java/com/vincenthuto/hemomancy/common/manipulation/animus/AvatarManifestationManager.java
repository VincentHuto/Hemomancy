package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationServerPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class AvatarManifestationManager {
	private static final ResourceLocation DAMAGE = Hemomancy.rloc("avatar_attack_damage");
	private static final ResourceLocation KNOCKBACK = Hemomancy.rloc("avatar_attack_knockback");
	private static final ResourceLocation SPEED = Hemomancy.rloc("avatar_movement_speed");
	private static final ResourceLocation STEP = Hemomancy.rloc("avatar_step_height");
	private static final ResourceLocation JUMP = Hemomancy.rloc("avatar_jump_strength");
	private static final ResourceLocation SCALE = Hemomancy.rloc("avatar_scale");
	private static final ResourceLocation BLOCK_REACH = Hemomancy.rloc("avatar_block_reach");
	private static final ResourceLocation ENTITY_REACH = Hemomancy.rloc("avatar_entity_reach");
	private static final Map<UUID, State> STATES = new HashMap<>();

	private AvatarManifestationManager() {
	}

	public static boolean toggle(ServerPlayer player, SummonAvatarManip avatar) {
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player).orElse(null);
		if (known == null || !isKnownAndEquipped(known, avatar.getName())) return false;
		if (avatar.getName().equals(known.getActiveAvatarForm())) {
			dismiss(player);
			return false;
		}
		if (!avatar.tryPerformSustainedPassivePulse(player)) return false;
		known.setActiveAvatarForm(avatar.getName());
		long now = player.level().getGameTime();
		STATES.put(player.getUUID(), new State(now + 20, now + 100));
		applyAttributes(player, avatar.getName());
		sync(player, known);
		return true;
	}

	public static void dismiss(ServerPlayer player) {
		STATES.remove(player.getUUID());
		removeAttributes(player);
		HemoCapabilityAccess.getKnownManipulations(player).ifPresent(known -> {
			if (known.getActiveAvatarForm().isBlank()) return;
			known.setActiveAvatarForm("");
			sync(player, known);
		});
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player).orElse(null);
		if (known == null) return;
		String active = known.getActiveAvatarForm();
		State state = STATES.get(player.getUUID());
		if (active.isBlank()) {
			if (state != null) {
				STATES.remove(player.getUUID());
				removeAttributes(player);
			}
			return;
		}
		SummonAvatarManip avatar = ManipulationInit.getByName(active) instanceof SummonAvatarManip current
				? current : null;
		boolean blocked = HemoCapabilityAccess.getUnstainedProgress(player)
				.map(UnstainedAccessRules::blocksKnownBloodPowerUse).orElse(false);
		if (!player.isAlive() || blocked || avatar == null || !isKnownAndEquipped(known, active)) {
			dismiss(player);
			return;
		}

		long now = player.level().getGameTime();
		if (state == null) {
			if (!avatar.tryPerformSustainedPassivePulse(player)) {
				dismiss(player);
				return;
			}
			state = new State(now + 20, now + 100);
			STATES.put(player.getUUID(), state);
			applyAttributes(player, active);
		}
		if (now >= state.nextUpkeep) {
			if (!avatar.tryPerformSustainedPassivePulse(player)) {
				dismiss(player);
				return;
			}
			state.nextUpkeep = now + 20;
		}
		if (now >= state.nextMastery) {
			avatar.recordSuccessfulUse(player);
			state.nextMastery = now + 100;
		}
	}

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) dismiss(player);
	}

	@SubscribeEvent
	public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			STATES.remove(player.getUUID());
			removeAttributes(player);
			HemoCapabilityAccess.getKnownManipulations(player)
					.ifPresent(known -> known.setActiveAvatarForm(""));
		}
	}

	private static boolean isKnownAndEquipped(IKnownManipulations known, String id) {
		return known.getEquippedManipNames().contains(id) && known.getManipList().stream()
				.anyMatch(manipulation -> manipulation != null && id.equals(manipulation.getName()));
	}

	private static void applyAttributes(ServerPlayer player, String avatarForm) {
		AvatarManifestationRules.Stats stats = AvatarManifestationRules.stats(avatarForm).orElse(null);
		if (stats == null) {
			removeAttributes(player);
			return;
		}
		update(player.getAttribute(Attributes.ATTACK_DAMAGE), DAMAGE, stats.attackDamage(),
				AttributeModifier.Operation.ADD_VALUE);
		update(player.getAttribute(Attributes.ATTACK_KNOCKBACK), KNOCKBACK, stats.attackKnockback(),
				AttributeModifier.Operation.ADD_VALUE);
		update(player.getAttribute(Attributes.MOVEMENT_SPEED), SPEED, stats.movementSpeed(),
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		update(player.getAttribute(Attributes.STEP_HEIGHT), STEP, stats.stepHeight(),
				AttributeModifier.Operation.ADD_VALUE);
		update(player.getAttribute(Attributes.JUMP_STRENGTH), JUMP, stats.jumpStrength(),
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		update(player.getAttribute(Attributes.SCALE), SCALE, stats.sizeBonus(),
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		update(player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE), BLOCK_REACH, stats.blockReach(),
				AttributeModifier.Operation.ADD_VALUE);
		update(player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE), ENTITY_REACH, stats.entityReach(),
				AttributeModifier.Operation.ADD_VALUE);
	}

	private static void removeAttributes(ServerPlayer player) {
		remove(player.getAttribute(Attributes.ATTACK_DAMAGE), DAMAGE);
		remove(player.getAttribute(Attributes.ATTACK_KNOCKBACK), KNOCKBACK);
		remove(player.getAttribute(Attributes.MOVEMENT_SPEED), SPEED);
		remove(player.getAttribute(Attributes.STEP_HEIGHT), STEP);
		remove(player.getAttribute(Attributes.JUMP_STRENGTH), JUMP);
		remove(player.getAttribute(Attributes.SCALE), SCALE);
		remove(player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE), BLOCK_REACH);
		remove(player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE), ENTITY_REACH);
	}

	private static void update(AttributeInstance attribute, ResourceLocation id, double amount,
			AttributeModifier.Operation operation) {
		if (attribute == null) return;
		attribute.removeModifier(id);
		if (amount != 0) attribute.addTransientModifier(new AttributeModifier(id, amount, operation));
	}

	private static void remove(AttributeInstance attribute, ResourceLocation id) {
		if (attribute != null) attribute.removeModifier(id);
	}

	private static void sync(ServerPlayer player, IKnownManipulations known) {
		PacketHandler.sendToPlayer(player, new KnownManipulationServerPacket(known));
		KnownManipulationEvents.syncAvatar(player, ((ServerLevel) player.level()).players(),
				known.getActiveAvatarForm());
	}

	private static final class State {
		private long nextUpkeep;
		private long nextMastery;

		private State(long nextUpkeep, long nextMastery) {
			this.nextUpkeep = nextUpkeep;
			this.nextMastery = nextMastery;
		}
	}
}
