package com.vincenthuto.hemomancy.common.capability;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.antecedent.AntecedentKnowledge;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.HarbingerEquipmentEntityEventHandler;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.LivingStaffBondHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphlingEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemoryEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.rite.IchorianKnowledgeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarStateSyncEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.KnownSummonEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.BloodTendencyEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.LiberKnowledgeEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.stillart.KnownStillArtEvents;
import com.vincenthuto.hemomancy.common.item.harbinger.memory.HematicMemoryToolEvents;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodKnowledge;
import com.vincenthuto.hemomancy.common.network.PacketAntecedentResearch;
import com.vincenthuto.hemomancy.common.network.PacketSyncClinicalBlood;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncLiberKnowledge;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodTendencyServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.LivingStaffProgressServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncManipulationHearts;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncDegree;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncIchorianKnowledge;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncLastDeathMemory;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncMuscleMemory;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncSkills;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncStationUpgrades;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.SyncEquipmentLayerVisibilityPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.VascularSystemServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.SyncTrackingAvatarPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.scars.PacketEquipmentSync;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.scars.PacketSyncScarsState;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.summon.KnownSummonsServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.unstained.KnownStillArtsServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.unstained.PacketSyncUnstainedProgress;
import com.vincenthuto.hemomancy.common.network.morphling.SyncEquippedMorphlingPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The owner-sync table: every player attachment the client mirrors, the payload that carries it, and the lifecycle
 * events that resend it. {@link HemoCapabilityRegistrar} sends the whole table, in this order, after every other
 * login, respawn and dimension-change handler has run. Attachments that change through more paths than their senders
 * can see keep a dirty flag instead, and {@link #flushDirty} sends each one at most once at the end of the tick.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class PlayerStateSync {
	public enum Lifecycle { LOGIN, RESPAWN, DIMENSION }

	private static final Set<Lifecycle> ALWAYS = EnumSet.allOf(Lifecycle.class);

	public enum Entry {
		TENDENCY(HemoAttachmentTypes.BLOOD_TENDENCY, BloodTendencyServerPacket.class, ALWAYS,
				player -> BloodTendencyEvents.syncTendency(player, HemoCapabilityAccess.getBloodTendency(player))),
		VASCULAR(HemoAttachmentTypes.VASCULAR_SYSTEM, VascularSystemServerPacket.class, ALWAYS,
				player -> VascularSystemEvents.syncVascular(player, HemoCapabilityAccess.getVascularSystem(player))),
		BLOOD_VOLUME(HemoAttachmentTypes.BLOOD_VOLUME, BloodVolumeServerPacket.class, ALWAYS,
				player -> BloodVolumeEvents.syncVolume(player, HemoCapabilityAccess.getBloodVolume(player))),
		/** Login only, as before this table: a respawn or dimension resend would be new behaviour. */
		SKILLS(HemoAttachmentTypes.SKILL_PROGRESS, PacketSyncSkills.class, EnumSet.of(Lifecycle.LOGIN),
				SkillPointGainEvents::syncSkills),
		DEGREE(HemoAttachmentTypes.INITIATORY_DEGREE, PacketSyncDegree.class, ALWAYS,
				player -> InitiatoryDegreeEvents.syncDegree(player, HemoCapabilityAccess.getInitiatoryDegree(player))),
		KNOWN_MANIPULATIONS(HemoAttachmentTypes.KNOWN_MANIPULATIONS, KnownManipulationServerPacket.class, ALWAYS,
				KnownManipulationEvents::syncPlayerEvent,
				player -> HemoCapabilityAccess.getKnownManipulations(player).isSyncDirty()),
		AVATAR_FORM(HemoAttachmentTypes.KNOWN_MANIPULATIONS, SyncTrackingAvatarPacket.class, ALWAYS,
				KnownManipulationEvents::syncOwnAvatar),
		LIVING_STAFF(HemoAttachmentTypes.LIVING_STAFF_PROGRESS, LivingStaffProgressServerPacket.class, ALWAYS,
				LivingStaffBondHelper::syncProgress),
		EQUIPPED_MORPHLING(HemoAttachmentTypes.EQUIPPED_MORPHLING, SyncEquippedMorphlingPacket.class, ALWAYS,
				EquippedMorphlingEvents::syncToClient),
		MUSCLE_MEMORY(HemoAttachmentTypes.MUSCLE_MEMORY, PacketSyncMuscleMemory.class, ALWAYS, MuscleMemoryEvents::sync),
		SCARS(HemoAttachmentTypes.SCARS, PacketSyncScarsState.class, ALWAYS, ScarStateSyncEvents::syncToClient),
		/** One payload per equipment slot. */
		EQUIPMENT_SLOTS(HemoAttachmentTypes.HARBINGER_EQUIPMENT, PacketEquipmentSync.class, ALWAYS,
				player -> HarbingerEquipmentEntityEventHandler.syncSlots(player, List.of(player))),
		EQUIPMENT_LAYERS(HemoAttachmentTypes.HARBINGER_EQUIPMENT, SyncEquipmentLayerVisibilityPacket.class, ALWAYS,
				HarbingerEquipmentEntityEventHandler::syncLayerVisibilityToClient),
		KNOWN_SUMMONS(HemoAttachmentTypes.KNOWN_SUMMONS, KnownSummonsServerPacket.class, ALWAYS,
				player -> KnownSummonEvents.sync(player, HemoCapabilityAccess.getKnownSummons(player))),
		LIBER_KNOWLEDGE(HemoAttachmentTypes.LIBER_KNOWLEDGE, PacketSyncLiberKnowledge.class, ALWAYS,
				LiberKnowledgeEvents::sync),
		UNSTAINED_PROGRESS(HemoAttachmentTypes.UNSTAINED_PROGRESS, PacketSyncUnstainedProgress.class, ALWAYS,
				player -> UnstainedProgressEvents.syncProgress(player, HemoCapabilityAccess.getUnstainedProgress(player))),
		KNOWN_STILL_ARTS(HemoAttachmentTypes.KNOWN_STILL_ARTS, KnownStillArtsServerPacket.class, ALWAYS,
				player -> KnownStillArtEvents.sync(player, HemoCapabilityAccess.getKnownStillArts(player))),
		/** No dimension-change resend: adding one is F-20, still pending approval. */
		ICHORIAN_KNOWLEDGE(HemoAttachmentTypes.ICHORIAN_KNOWLEDGE, PacketSyncIchorianKnowledge.class,
				EnumSet.of(Lifecycle.LOGIN, Lifecycle.RESPAWN), IchorianKnowledgeEvents::sync),
		STATION_UPGRADES(HemoAttachmentTypes.STATION_UPGRADES, PacketSyncStationUpgrades.class, ALWAYS,
				player -> HemoCapabilityAccess.stationUpgrades(player).sync(player, true),
				player -> HemoCapabilityAccess.stationUpgrades(player).isSyncDirty()),
		ANTECEDENT(HemoAttachmentTypes.ANTECEDENT, PacketAntecedentResearch.class, ALWAYS, AntecedentKnowledge::sync),
		CLINICAL_BLOOD(HemoAttachmentTypes.CLINICAL_BLOOD, PacketSyncClinicalBlood.class, ALWAYS,
				ClinicalBloodKnowledge::sync),
		LAST_DEATH_MEMORY(HemoAttachmentTypes.LAST_DEATH_MEMORY, PacketSyncLastDeathMemory.class, ALWAYS,
				HematicMemoryToolEvents::syncLastDeathMemory),
		POWER_GUARDRAILS(HemoAttachmentTypes.POWER_GUARDRAILS, PacketSyncManipulationHearts.class, ALWAYS,
				ManipulationReactiveEvents::syncHearts);

		private final DeferredHolder<AttachmentType<?>, ? extends AttachmentType<?>> attachment;
		private final Class<? extends CustomPacketPayload> payload;
		private final Set<Lifecycle> resendOn;
		private final Consumer<ServerPlayer> send;
		private final Predicate<ServerPlayer> dirty;

		Entry(DeferredHolder<AttachmentType<?>, ? extends AttachmentType<?>> attachment,
				Class<? extends CustomPacketPayload> payload, Set<Lifecycle> resendOn, Consumer<ServerPlayer> send) {
			this(attachment, payload, resendOn, send, null);
		}

		Entry(DeferredHolder<AttachmentType<?>, ? extends AttachmentType<?>> attachment,
				Class<? extends CustomPacketPayload> payload, Set<Lifecycle> resendOn, Consumer<ServerPlayer> send,
				Predicate<ServerPlayer> dirty) {
			this.attachment = attachment;
			this.payload = payload;
			this.resendOn = resendOn;
			this.send = send;
			this.dirty = dirty;
		}

		public AttachmentType<?> attachment() {
			return attachment.get();
		}

		/** The payload this entry sends; {@link #EQUIPMENT_SLOTS} sends one per slot. */
		public Class<? extends CustomPacketPayload> payload() {
			return payload;
		}

		public boolean resendsOn(Lifecycle lifecycle) {
			return resendOn.contains(lifecycle);
		}

		public boolean tracksDirty() {
			return dirty != null;
		}

		/** Sends this entry to its owner now. */
		public void send(ServerPlayer player) {
			send.accept(player);
		}
	}

	private PlayerStateSync() {
	}

	/** Sends every entry that resends on {@code lifecycle}, in table order. */
	public static void sendAll(ServerPlayer player, Lifecycle lifecycle) {
		for (Entry entry : Entry.values()) {
			if (entry.resendsOn(lifecycle)) entry.send(player);
		}
	}

	@SubscribeEvent
	public static void flushDirty(ServerTickEvent.Post event) {
		for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
			flushDirty(player);
		}
	}

	/** Sends each dirty entry of one player once. Sending clears the entry's flag. */
	public static void flushDirty(ServerPlayer player) {
		for (Entry entry : Entry.values()) {
			if (entry.tracksDirty() && entry.dirty.test(player)) entry.send(player);
		}
	}
}
