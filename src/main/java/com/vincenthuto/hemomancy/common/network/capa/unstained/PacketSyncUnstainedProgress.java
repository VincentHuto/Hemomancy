package com.vincenthuto.hemomancy.common.network.capa.unstained;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

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
	private final boolean infectionSuppressed, clarityPrepared, annettaSeveranceUnlocked;
	private final int acceptedObservances, claimedObservances;
	private final boolean baselineRestored, novitiateRetortComplete, novitiateProtectionComplete;
	private final int novitiateDewProduced, novitiateBlocksConsecrated;

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
		this.infectionSuppressed = p.isInfectionSuppressed();
		this.clarityPrepared = p.isClarityPrepared();
		this.annettaSeveranceUnlocked = p.isAnnettaSeveranceUnlocked();
		this.acceptedObservances = p.getAcceptedObservances();
		this.claimedObservances = p.getClaimedObservances();
		this.baselineRestored = p.isBaselineRestored();
		this.novitiateRetortComplete = p.isNovitiateRetortComplete();
		this.novitiateDewProduced = p.getNovitiateDewProduced();
		this.novitiateBlocksConsecrated = p.getNovitiateBlocksConsecrated();
		this.novitiateProtectionComplete = p.isNovitiateProtectionComplete();
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
		this.infectionSuppressed = buf.readBoolean();
		this.clarityPrepared = buf.readBoolean();
		this.annettaSeveranceUnlocked = buf.readBoolean();
		this.acceptedObservances = buf.readVarInt();
		this.claimedObservances = buf.readVarInt();
		this.baselineRestored = buf.readBoolean();
		this.novitiateRetortComplete = buf.readBoolean();
		this.novitiateDewProduced = buf.readVarInt();
		this.novitiateBlocksConsecrated = buf.readVarInt();
		this.novitiateProtectionComplete = buf.readBoolean();
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
		buf.writeBoolean(msg.infectionSuppressed);
		buf.writeBoolean(msg.clarityPrepared);
		buf.writeBoolean(msg.annettaSeveranceUnlocked);
		buf.writeVarInt(msg.acceptedObservances);
		buf.writeVarInt(msg.claimedObservances);
		buf.writeBoolean(msg.baselineRestored);
		buf.writeBoolean(msg.novitiateRetortComplete);
		buf.writeVarInt(msg.novitiateDewProduced);
		buf.writeVarInt(msg.novitiateBlocksConsecrated);
		buf.writeBoolean(msg.novitiateProtectionComplete);
    }

    public static PacketSyncUnstainedProgress decode(FriendlyByteBuf buf) {
        return new PacketSyncUnstainedProgress(buf);
    }

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public int acceptedObservances() {
		return acceptedObservances;
	}

	public int advancementsEarned() {
		return advancementsEarned;
	}

	public int animalsBreed() {
		return animalsBreed;
	}

	public boolean annettaSeveranceUnlocked() {
		return annettaSeveranceUnlocked;
	}

	public boolean baselineRestored() {
		return baselineRestored;
	}

	public boolean begunPurification() {
		return begunPurification;
	}

	public int claimedObservances() {
		return claimedObservances;
	}

	public float clarity() {
		return clarity;
	}

	public boolean clarityPrepared() {
		return clarityPrepared;
	}

	public boolean clarityUnlocked() {
		return clarityUnlocked;
	}

	public int cropsPlanted() {
		return cropsPlanted;
	}

	public boolean earnedAdvancement() {
		return earnedAdvancement;
	}

	public boolean emptiedBlood() {
		return emptiedBlood;
	}

	public int flawlessKills() {
		return flawlessKills;
	}

	public int hemoMobKills() {
		return hemoMobKills;
	}

	public int hostileKills() {
		return hostileKills;
	}

	public boolean infectionSuppressed() {
		return infectionSuppressed;
	}

	public boolean killedFirstHemoMob() {
		return killedFirstHemoMob;
	}

	public int nightsSlept() {
		return nightsSlept;
	}

	public int novitiateBlocksConsecrated() {
		return novitiateBlocksConsecrated;
	}

	public int novitiateDewProduced() {
		return novitiateDewProduced;
	}

	public boolean novitiateProtectionComplete() {
		return novitiateProtectionComplete;
	}

	public boolean novitiateRetortComplete() {
		return novitiateRetortComplete;
	}

	public int petsHealed() {
		return petsHealed;
	}

	public float purity() {
		return purity;
	}

	public boolean reachedAbstinence() {
		return reachedAbstinence;
	}

	public boolean silverWardEnabled() {
		return silverWardEnabled;
	}

	public boolean sleptWithHemolysis() {
		return sleptWithHemolysis;
	}

	public int undeadKills() {
		return undeadKills;
	}

	public boolean usedAltarOfCleansing() {
		return usedAltarOfCleansing;
	}

	public boolean verdigrisAuraEnabled() {
		return verdigrisAuraEnabled;
	}
}
