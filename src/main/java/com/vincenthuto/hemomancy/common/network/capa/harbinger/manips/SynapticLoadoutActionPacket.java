package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.*;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class SynapticLoadoutActionPacket implements CustomPacketPayload {
	public enum Action {
		SAVE_OR_OVERWRITE,
		RENAME,
		APPLY
	}

	public static final Type<SynapticLoadoutActionPacket> TYPE =
			new Type<>(Hemomancy.rloc("synaptic_loadout_action_packet"));
	public static final StreamCodec<FriendlyByteBuf, SynapticLoadoutActionPacket> STREAM_CODEC =
			StreamCodec.of(SynapticLoadoutActionPacket::encode, SynapticLoadoutActionPacket::decode);

	public static final double SAVE_BLOOD_COST = 100.0D;
	public static final int SAVE_RAW_XP_COST = 25;
	private static final int REQUIRED_DEGREE = 5;
	private static final double MAX_DISTRIBUTOR_DISTANCE_SQR = 64.0D;

	private final Action action;
	private final BlockPos pos;
	private final int slotIndex;
	private final String name;

	public SynapticLoadoutActionPacket(Action action, BlockPos pos, int slotIndex, String name) {
		this.action = action;
		this.pos = pos;
		this.slotIndex = slotIndex;
		this.name = name != null ? name : "";
	}

	public static void encode(FriendlyByteBuf buf, SynapticLoadoutActionPacket msg) {
		buf.writeEnum(msg.action);
		buf.writeBlockPos(msg.pos);
		buf.writeInt(msg.slotIndex);
		buf.writeUtf(msg.name);
	}

	public static SynapticLoadoutActionPacket decode(FriendlyByteBuf buf) {
		return new SynapticLoadoutActionPacket(buf.readEnum(Action.class), buf.readBlockPos(), buf.readInt(), buf.readUtf());
	}

	public static void handle(final SynapticLoadoutActionPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (!(ctx.player() instanceof ServerPlayer player)) {
				return;
			}

			IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player).orElse(null);
			IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
			if (known == null || volume == null || !volume.isActive()) {
				fail(player, "Blood system is not active.");
				return;
			}
			if (HemoCapabilityAccess.getPlayerDegreeNumber(player) < REQUIRED_DEGREE) {
				fail(player, "Requires the Fifth Degree.");
				return;
			}
			if (!isValidDistributor(player, msg.pos)) {
				fail(player, "No Dendritic Distributor is listening nearby.");
				return;
			}
			int skillLevel = SynapticLoadoutSlotHelper.getUnlockedSlots(player)
					- SynapticLoadoutSlotHelper.BASE_LOADOUT_SLOTS;
			if (!SynapticLoadoutSlotHelper.isSlotUnlocked(msg.slotIndex, skillLevel)) {
				fail(player, "That synaptic pattern slot is not unlocked.");
				return;
			}

			switch (msg.action) {
				case SAVE_OR_OVERWRITE -> saveOrOverwrite(player, known, volume, msg.slotIndex, msg.name);
				case RENAME -> rename(player, known, msg.slotIndex, msg.name);
				case APPLY -> apply(player, known, msg.slotIndex);
			}
		});
	}

	private static void saveOrOverwrite(ServerPlayer player, IKnownManipulations known, IBloodVolume volume,
			int slotIndex, String requestedName) {
		List<String> memorizedNames = memorizedEquippedNames(known);
		if (memorizedNames.isEmpty()) {
			fail(player, "No memorized manipulations are equipped to remember.");
			return;
		}
		if (volume.getBloodVolume() < SAVE_BLOOD_COST) {
			fail(player, "Not enough blood. Need 100 mL.");
			return;
		}
		if (currentRawExperience(player) < SAVE_RAW_XP_COST) {
			fail(player, "Not enough experience. Need 25 XP.");
			return;
		}

		String selected = selectedNameForLoadout(known, memorizedNames);
		ManipulationLoadout loadout = ManipulationLoadout.of(requestedName, selected, memorizedNames, slotIndex);
		volume.drain(SAVE_BLOOD_COST);
		volume.addBloodSpend(SAVE_BLOOD_COST);
		player.giveExperiencePoints(-SAVE_RAW_XP_COST);
		known.setLoadout(slotIndex, loadout);
		BloodVolumeEvents.syncVolume(player, volume);
		syncKnown(player, known);
		player.displayClientMessage(Component.literal("Synaptic pattern saved: " + loadout.name())
				.withStyle(ChatFormatting.DARK_RED), true);
	}

	private static void rename(ServerPlayer player, IKnownManipulations known, int slotIndex, String requestedName) {
		ManipulationLoadout existing = known.getLoadout(slotIndex);
		if (existing.isEmpty()) {
			fail(player, "No synaptic pattern is saved in that slot.");
			return;
		}
		ManipulationLoadout renamed = existing.renamed(requestedName, slotIndex);
		known.setLoadout(slotIndex, renamed);
		syncKnown(player, known);
		player.displayClientMessage(Component.literal("Synaptic pattern renamed: " + renamed.name())
				.withStyle(ChatFormatting.GRAY), true);
	}

	private static void apply(ServerPlayer player, IKnownManipulations known, int slotIndex) {
		ManipulationLoadout loadout = known.getLoadout(slotIndex);
		if (loadout.isEmpty()) {
			fail(player, "No synaptic pattern is saved in that slot.");
			return;
		}
		int maxSlots = ManipSlotHelper.getMaxSlots(player);
		if (ManipulationEquipHelper.countNormalEquippedNames(loadout.manipNames()) > maxSlots) {
			fail(player, "That pattern exceeds your current manipulation capacity.");
			return;
		}
		for (String memoryName : loadout.manipNames()) {
			if (!isKnownMemory(player, known, memoryName)) {
				fail(player, "A saved memory has atrophied: " + memoryName);
				return;
			}
		}
		known.setEquippedManipNames(loadout.manipNames());
		var muscleState = player.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
		for (var memory : com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemory.values()) {
			if (muscleState.isEnabled(memory)
					&& !loadout.manipNames().contains(MemorySlotRef.muscleMemory(memory).storageKey())) {
				muscleState.deactivate(memory);
			}
		}
		MemorySlotRef selected = MemorySlotRef.fromStorageKey(loadout.selectedManipName());
		if (!isKnownMemory(player, known, selected.storageKey()) && !loadout.manipNames().isEmpty()) {
			selected = MemorySlotRef.fromStorageKey(loadout.manipNames().getFirst());
		}
		known.setSelectedMemoryRef(selected);
		syncKnown(player, known);
		player.displayClientMessage(Component.literal("Synaptic pattern applied: " + loadout.name())
				.withStyle(ChatFormatting.DARK_RED), true);
	}

	private static boolean isValidDistributor(ServerPlayer player, BlockPos pos) {
		if (pos == null || player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)
				> MAX_DISTRIBUTOR_DISTANCE_SQR) {
			return false;
		}
		BlockEntity be = player.level().getBlockEntity(pos);
		return be != null && be.getType() == BlockEntityInit.dendritic_distributor.get();
	}

	private static List<String> memorizedEquippedNames(IKnownManipulations known) {
		List<String> names = new ArrayList<>();
		for (String name : known.getEquippedManipNames()) {
			if (isKnownMemory(null, known, name)) {
				names.add(name);
			}
		}
		return ManipulationLoadout.normalizeManipNames(names);
	}

	private static String selectedNameForLoadout(IKnownManipulations known, List<String> memorizedNames) {
		String selected = known.getSelectedMemoryRef().storageKey();
		if (memorizedNames.contains(selected)) {
			return selected;
		}
		return memorizedNames.getFirst();
	}

	private static BloodManipulation findKnownManipulation(IKnownManipulations known, String manipName) {
		if (manipName == null || manipName.isBlank()) {
			return null;
		}
		if (ManipulationRetirementRules.isRetiredManipulation(manipName)) {
			return null;
		}
		for (BloodManipulation manipulation : known.getManipList()) {
			if (manipulation != null && manipName.equals(manipulation.getName())) {
				return manipulation;
			}
		}
		return null;
	}

	private static boolean isKnownMemory(ServerPlayer player, IKnownManipulations known, String storageKey) {
		MemorySlotRef ref = MemorySlotRef.fromStorageKey(storageKey);
		if (ref.kind() == MemoryEntryKind.MANIPULATION) {
			return findKnownManipulation(known, ref.id()) != null;
		}
		return ref.muscleMemory().filter(memory -> player == null
				|| player.getData(HemoAttachmentTypes.MUSCLE_MEMORY).knows(memory)).isPresent();
	}

	private static int currentRawExperience(Player player) {
		int level = player.experienceLevel;
		int total = totalExperienceForLevel(level);
		return total + Math.round(player.experienceProgress * player.getXpNeededForNextLevel());
	}

	private static int totalExperienceForLevel(int level) {
		if (level <= 16) {
			return level * level + 6 * level;
		}
		if (level <= 31) {
			return (int) Math.floor(2.5D * level * level - 40.5D * level + 360.0D);
		}
		return (int) Math.floor(4.5D * level * level - 162.5D * level + 2220.0D);
	}

	private static void syncKnown(ServerPlayer player, IKnownManipulations known) {
		PacketHandler.sendToPlayer(player, new KnownManipulationServerPacket(known));
		ManipulationDiagnosticsSync.sync(player);
	}

	private static void fail(ServerPlayer player, String message) {
		player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.RED), true);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
