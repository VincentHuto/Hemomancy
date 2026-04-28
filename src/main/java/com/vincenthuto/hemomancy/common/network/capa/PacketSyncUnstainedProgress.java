package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

/**
 * Server → Client packet: synchronises the player's current Unstained progress.
 */
public class PacketSyncUnstainedProgress implements CustomPacketPayload {

	public static final Type<PacketSyncUnstainedProgress> TYPE = new Type<>(Hemomancy.rloc("packet_sync_unstained_progress"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncUnstainedProgress> STREAM_CODEC = StreamCodec.of(PacketSyncUnstainedProgress::encode, PacketSyncUnstainedProgress::decode);

    private final boolean begunPurification;
    private final float purity;
    private final boolean clarityUnlocked;
    private final float clarity;
    // Milestone counters
    private final int hemoMobKills, undeadKills, hostileKills, flawlessKills;
    private final int animalsBreed, cropsPlanted, advancementsEarned, nightsSlept, petsHealed;
    // One-time flags
    private final boolean sleptWithHemolysis, killedFirstHemoMob, reachedAbstinence, emptiedBlood, earnedAdvancement;
    // Bonus toggle state
    private final boolean silverWardEnabled, verdigrisAuraEnabled;
    private final boolean usedAltarOfCleansing;

    public PacketSyncUnstainedProgress(IUnstainedProgress p) {
        this.begunPurification = p.hasBegunPurification();
        this.purity = p.getPurity();
        this.clarityUnlocked = p.hasClarityUnlocked();
        this.clarity = p.getClarity();
        this.hemoMobKills = p.getHemoMobKills();
        this.undeadKills = p.getUndeadKills();
        this.hostileKills = p.getHostileKills();
        this.flawlessKills = p.getFlawlessKills();
        this.animalsBreed = p.getAnimalsBreed();
        this.cropsPlanted = p.getCropsPlanted();
        this.advancementsEarned = p.getAdvancementsEarned();
        this.nightsSlept = p.getNightsSlept();
        this.petsHealed = p.getPetsHealed();
        this.sleptWithHemolysis = p.hasSleptWithHemolysis();
        this.killedFirstHemoMob = p.hasKilledFirstHemoMob();
        this.reachedAbstinence = p.hasReachedAbstinence();
        this.emptiedBlood = p.hasEmptiedBlood();
        this.earnedAdvancement = p.hasEarnedAdvancement();
        this.silverWardEnabled = p.isSilverWardEnabled();
        this.verdigrisAuraEnabled = p.isVerdigrisAuraEnabled();
        this.usedAltarOfCleansing = p.hasUsedAltarOfCleansing();
    }

    private PacketSyncUnstainedProgress(FriendlyByteBuf buf) {
        this.begunPurification = buf.readBoolean();
        this.purity = buf.readFloat();
        this.clarityUnlocked = buf.readBoolean();
        this.clarity = buf.readFloat();
        this.hemoMobKills = buf.readVarInt();
        this.undeadKills = buf.readVarInt();
        this.hostileKills = buf.readVarInt();
        this.flawlessKills = buf.readVarInt();
        this.animalsBreed = buf.readVarInt();
        this.cropsPlanted = buf.readVarInt();
        this.advancementsEarned = buf.readVarInt();
        this.nightsSlept = buf.readVarInt();
        this.petsHealed = buf.readVarInt();
        this.sleptWithHemolysis = buf.readBoolean();
        this.killedFirstHemoMob = buf.readBoolean();
        this.reachedAbstinence = buf.readBoolean();
        this.emptiedBlood = buf.readBoolean();
        this.earnedAdvancement = buf.readBoolean();
        this.silverWardEnabled = buf.readBoolean();
        this.verdigrisAuraEnabled = buf.readBoolean();
        this.usedAltarOfCleansing = buf.readBoolean();
    }

    public static void encode(FriendlyByteBuf buf, PacketSyncUnstainedProgress msg) {
        buf.writeBoolean(msg.begunPurification);
        buf.writeFloat(msg.purity);
        buf.writeBoolean(msg.clarityUnlocked);
        buf.writeFloat(msg.clarity);
        buf.writeVarInt(msg.hemoMobKills);
        buf.writeVarInt(msg.undeadKills);
        buf.writeVarInt(msg.hostileKills);
        buf.writeVarInt(msg.flawlessKills);
        buf.writeVarInt(msg.animalsBreed);
        buf.writeVarInt(msg.cropsPlanted);
        buf.writeVarInt(msg.advancementsEarned);
        buf.writeVarInt(msg.nightsSlept);
        buf.writeVarInt(msg.petsHealed);
        buf.writeBoolean(msg.sleptWithHemolysis);
        buf.writeBoolean(msg.killedFirstHemoMob);
        buf.writeBoolean(msg.reachedAbstinence);
        buf.writeBoolean(msg.emptiedBlood);
        buf.writeBoolean(msg.earnedAdvancement);
        buf.writeBoolean(msg.silverWardEnabled);
        buf.writeBoolean(msg.verdigrisAuraEnabled);
        buf.writeBoolean(msg.usedAltarOfCleansing);
    }

    public static PacketSyncUnstainedProgress decode(FriendlyByteBuf buf) {
        return new PacketSyncUnstainedProgress(buf);
    }

    public static void handle(final PacketSyncUnstainedProgress msg, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (player != null) {
                HemoCapabilityAccess.getUnstainedProgress(player)
                        .ifPresent(progress -> {
                            progress.setBegunPurification(msg.begunPurification);
                            progress.setPurity(msg.purity);
                            progress.setClarityUnlocked(msg.clarityUnlocked);
                            progress.setClarity(msg.clarity);
                            // Replay milestones from server counts
                            for (int i = progress.getHemoMobKills(); i < msg.hemoMobKills; i++) progress.addHemoMobKill();
                            for (int i = progress.getUndeadKills(); i < msg.undeadKills; i++) progress.addUndeadKill();
                            for (int i = progress.getHostileKills(); i < msg.hostileKills; i++) progress.addHostileKill();
                            for (int i = progress.getFlawlessKills(); i < msg.flawlessKills; i++) progress.addFlawlessKill();
                            for (int i = progress.getAnimalsBreed(); i < msg.animalsBreed; i++) progress.addAnimalBreed();
                            for (int i = progress.getCropsPlanted(); i < msg.cropsPlanted; i++) progress.addCropPlanted();
                            for (int i = progress.getAdvancementsEarned(); i < msg.advancementsEarned; i++) progress.addAdvancementEarned();
                            for (int i = progress.getNightsSlept(); i < msg.nightsSlept; i++) progress.addNightSlept();
                            for (int i = progress.getPetsHealed(); i < msg.petsHealed; i++) progress.addPetHealed();
                            progress.setSleptWithHemolysis(msg.sleptWithHemolysis);
                            progress.setKilledFirstHemoMob(msg.killedFirstHemoMob);
                            progress.setReachedAbstinence(msg.reachedAbstinence);
                            progress.setEmptiedBlood(msg.emptiedBlood);
                            progress.setEarnedAdvancement(msg.earnedAdvancement);
                            progress.setSilverWardEnabled(msg.silverWardEnabled);
                            progress.setVerdigrisAuraEnabled(msg.verdigrisAuraEnabled);
                            progress.setUsedAltarOfCleansing(msg.usedAltarOfCleansing);
                        });
            }
        });
    }

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
