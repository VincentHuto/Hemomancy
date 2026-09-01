package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationRetirementRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemoryEntryKind;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemoryEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumBloodFlow;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.IDispellable;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormRules;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager;
import com.vincenthuto.hemomancy.common.manipulation.animus.AvatarManifestationManager;
import com.vincenthuto.hemomancy.common.manipulation.animus.SummonAvatarManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.SummonThrallManip;
import com.vincenthuto.hemomancy.common.manipulation.ferric.ConjurationManip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Unified packet for casting any blood manipulation regardless of type.
 * Replaces the old split between UseQuickManipKeyPacket and UseContManipKeyPacket.
 */
public class UseManipKeyPacket implements CustomPacketPayload {

	public static final Type<UseManipKeyPacket> TYPE = new Type<>(Hemomancy.rloc("use_manip_key_packet"));
	public static final StreamCodec<FriendlyByteBuf, UseManipKeyPacket> STREAM_CODEC = StreamCodec.of(UseManipKeyPacket::encode, UseManipKeyPacket::decode);

	public static UseManipKeyPacket decode(final FriendlyByteBuf buffer) {
		Action action = Action.byId(buffer.readUnsignedByte());
		return new UseManipKeyPacket(action, buffer.readFloat());
	}

	public static void encode(final FriendlyByteBuf buffer, final UseManipKeyPacket message) {
		buffer.writeByte(message.action.ordinal());
		buffer.writeFloat(message.parTick);
	}

	public static void handle(final UseManipKeyPacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null)
				return;
			if (!player.level().isClientSide) {
				boolean bloodPowersBlocked = HemoCapabilityAccess.getUnstainedProgress(player)
						.map(UnstainedAccessRules::blocksKnownBloodPowerUse).orElse(false);
				if (message.action == Action.STOP_CONTINUOUS) {
					ManipulationChannelManager.stop((net.minecraft.server.level.ServerPlayer) player, true);
					return;
				}
				if (message.action == Action.START_CONTINUOUS) {
					if (!bloodPowersBlocked) {
						ManipulationChannelManager.start((net.minecraft.server.level.ServerPlayer) player);
					}
					return;
				}
				float pTic = message.parTick;

				// Allow SummonThrallManip through cooldown when selecting a destination
				boolean bypassCooldown = false;
				IKnownManipulations knownCheck = HemoCapabilityAccess.getKnownManipulations(player).orElse(null);
				if (knownCheck != null && knownCheck.getSelectedMemoryRef().kind() == MemoryEntryKind.MUSCLE_MEMORY) {
					if (bloodPowersBlocked) return;
					handleMuscleMemoryUse(player, knownCheck);
					return;
				}
				if (knownCheck != null && knownCheck.getSelectedManip() != null) {
					BloodManipulation passive = ManipulationInit.getByName(knownCheck.getSelectedManip().getName());
					if (passive != null && passive.getType() == EnumManipulationType.PASSIVE) {
						if (ManipulationRetirementRules.isRetiredManipulation(passive)
								|| !knownCheck.isManipEquipped(passive)) return;
						if (passive instanceof SummonAvatarManip avatar) {
							if (bloodPowersBlocked && !passive.getName().equals(knownCheck.getActiveAvatarForm())) return;
							boolean wasSelected = passive.getName().equals(knownCheck.getActiveAvatarForm());
							boolean active = AvatarManifestationManager.toggle(
									(net.minecraft.server.level.ServerPlayer) player, avatar);
							if (active || wasSelected) {
								player.displayClientMessage(Component.literal(passive.getName().replace('_', ' ')
										+ (active ? " manifested" : " dismissed"))
										.withStyle(active ? ChatFormatting.GREEN : ChatFormatting.GRAY), true);
							}
							return;
						}
						if (bloodPowersBlocked && !knownCheck.isPassiveActive(passive.getName())) return;
						boolean active = knownCheck.togglePassive(passive.getName());
						player.displayClientMessage(Component.literal(passive.getName().replace('_', ' ')
								+ (active ? " enabled" : " disabled"))
								.withStyle(active ? ChatFormatting.GREEN : ChatFormatting.GRAY), true);
						return;
					}
				}
				if (bloodPowersBlocked) return;
				if (knownCheck != null && knownCheck.getSelectedManip() != null) {
					BloodManipulation selManip = ManipulationInit.getByName(knownCheck.getSelectedManip().getName());
					if (selManip instanceof SummonThrallManip && SummonThrallManip.hasPendingThrall(player.getUUID())) {
						bypassCooldown = true;
					}
					if (selManip != null && selManip.ignoresCooldown(player)) {
						bypassCooldown = true;
					}
				}

				if (!bypassCooldown && BloodManipulation.isAnyManipOnCooldown(player)) {
					player.displayClientMessage(Component.literal("Manipulation on cooldown!")
							.withStyle(ChatFormatting.RED), true);
					return;
				}
				IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player)
						.orElseThrow(NullPointerException::new);
				IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
						.orElseThrow(NullPointerException::new);
				if (volume.isActive()) {
					if (known.getSelectedManip() != null) {
						ItemStack mainStack = player.getMainHandItem();
						BloodManipulation selectedManip = ManipulationInit
								.getByName(known.getSelectedManip().getName());
						if (selectedManip != null) {
							if (ManipulationRetirementRules.isRetiredManipulation(selectedManip)) {
								player.displayClientMessage(
										Component.literal("That manipulation has gone dormant.")
												.withStyle(ChatFormatting.DARK_GRAY), true);
								ManipulationRetirementRules.sanitizeKnownManipulations(known);
								return;
							}
							// Check manipulation is equipped
							if (!known.isManipEquipped(selectedManip)) {
								player.displayClientMessage(
										Component.literal("That manipulation is unequipped!")
												.withStyle(ChatFormatting.RED), true);
								return;
							}
							if (selectedManip.getType() == EnumManipulationType.CONTINUOUS) return;
							if (LivingStaffWeaponFormRules.isStaffWeaponFormManip(selectedManip.getName())) {
								LivingStaffWeaponFormHelper.toggleSelectedForm(player, selectedManip);
								return;
							}
							// Handle conjuration dispel logic
							if (selectedManip instanceof ConjurationManip conjure) {
								ItemStack beforeMain = mainStack.copy();
								ItemStack beforeOff = player.getOffhandItem().copy();
								if (!mainStack.isEmpty()) {
									if (mainStack.getItem() instanceof IDispellable dispel) {
										mainStack.shrink(1);
										double bloodCost = dispel.getBaseCost();
										double bloodRefund = Math
												.abs(mainStack.getMaxDamage() - 1000 - mainStack.getDamageValue());
										if (bloodRefund > bloodCost * 0.9) {
											bloodRefund = bloodCost * 0.9;
										}
										volume.fill(bloodRefund);
										mainStack.shrink(1);
										player.displayClientMessage(Component.literal("Dispelled Conjured Item")
												.withStyle(ChatFormatting.RED), true);
									} else {
										player.displayClientMessage(
												Component.literal("Conjuration Requires an Empty Hand!")
														.withStyle(ChatFormatting.RED),
												true);
									}
								} else {
									selectedManip.performAction(player, player.level(), mainStack,
									player.blockPosition(), pTic);
								}
								LivingStaffWeaponFormHelper.syncMorph(player, beforeMain, beforeOff);
							} else {
								// All other manipulation types — just perform
								selectedManip.performAction(player, player.level(), mainStack,
										player.blockPosition(), pTic);
							}
						}
					}
				} else {
					player.displayClientMessage(Component.literal("You lack the skill to manifest this power!")
							.withStyle(ChatFormatting.RED), true);
				}
			}
		});
	}

	private static void handleMuscleMemoryUse(Player player, IKnownManipulations known) {
		if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return;
		var selected = known.getSelectedMemoryRef();
		MuscleMemory memory = selected.muscleMemory().orElse(null);
		if (memory == null || !known.isMemoryEquipped(selected)) return;
		var state = player.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
		if (player.isShiftKeyDown()) {
			if (state.isEnabled(memory)) {
				state.armOverexertion(memory, player.level().getGameTime());
				MuscleMemoryEvents.sync(serverPlayer);
				player.displayClientMessage(Component.translatable(
						"message.hemomancy.muscle_memory.overexertion_armed").withStyle(ChatFormatting.RED), true);
			}
			return;
		}
		if (state.isEnabled(memory)) {
			state.deactivate(memory);
			MuscleMemoryEvents.sync(serverPlayer);
			return;
		}
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		boolean dead = HemoCapabilityAccess.getVascularSystem(player)
				.map(vascular -> vascular.getBloodFlowBySection(memory.section()) == EnumBloodFlow.DEAD)
				.orElse(false);
		if (volume == null || !volume.isActive() || dead || !state.activate(memory)) {
			player.displayClientMessage(Component.translatable(
					"message.hemomancy.muscle_memory.activation_failed").withStyle(ChatFormatting.RED), true);
			return;
		}
		MuscleMemoryEvents.sync(serverPlayer);
	}

	private final Action action;
	private final float parTick;

	public UseManipKeyPacket() {
		this(Action.CAST, 0.0F);
	}

	public UseManipKeyPacket(float par) {
		this(Action.CAST, par);
	}

	private UseManipKeyPacket(Action action, float par) {
		this.action = action;
		this.parTick = par;
	}

	public static UseManipKeyPacket startContinuous() {
		return new UseManipKeyPacket(Action.START_CONTINUOUS, 0.0F);
	}

	public static UseManipKeyPacket stopContinuous() {
		return new UseManipKeyPacket(Action.STOP_CONTINUOUS, 0.0F);
	}

	public enum Action {
		CAST,
		START_CONTINUOUS,
		STOP_CONTINUOUS;

		private static Action byId(int id) {
			return id >= 0 && id < values().length ? values()[id] : CAST;
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
