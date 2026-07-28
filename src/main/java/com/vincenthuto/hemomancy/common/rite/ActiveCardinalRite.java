package com.vincenthuto.hemomancy.common.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Persisted server-authoritative state for both legacy countdown rites and the
 * interactive Harbinger ceremony.
 */
public class ActiveCardinalRite {
	private static final String STATE_VERSION = "StateVersion";
	private static final int CURRENT_STATE_VERSION = 2;

	private final UUID playerUUID;
	private final BlockPos centerPos;
	private final ResourceLocation recipeId;
	private final int totalTicks;
	private final int riteSize;
	private int remainingTicks;

	private CardinalRitePhase phase = CardinalRitePhase.LEGACY;
	private int degree;
	private boolean abbreviated;
	private boolean altarSealed;
	private int phaseTicks;
	private int idleTicks;
	private int disconnectTicks;
	private int instability;
	private int currentWave;
	private int totalWaves;
	private int committedBloodMl;
	private int carriedIchorMl;
	private int carriedIchorTicks;
	private int reservoirBloodMl;
	private int professionStep;
	private int professionMistakes;
	private int professionChoice = -1;
	private int[] anchorBloodMl = new int[0];
	private int[] instabilityRepairBloodMl = new int[0];
	private int[] instabilityDamagePriority = new int[0];
	private final LinkedHashSet<Integer> brokenInstabilityAnchors = new LinkedHashSet<>();
	private final Map<String, Integer> sigilProgress = new HashMap<>();
	private final Set<String> awakenedSigils = new HashSet<>();
	private final Map<UUID, CardinalRiteAllyRole> allyRoles = new HashMap<>();
	private final Set<UUID> sharedPoolOptIns = new HashSet<>();
	private final Map<UUID, Integer> npcReservesMl = new HashMap<>();
	private final Map<UUID, Long> bloodspentUntil = new HashMap<>();
	private final Set<UUID> riteThreats = new HashSet<>();
	private final List<String> waveDeck = new ArrayList<>();

	/**
	 * Compatibility constructor. Rites created through it retain the former
	 * countdown behavior and deserialize as LEGACY.
	 */
	public ActiveCardinalRite(UUID playerUUID, BlockPos centerPos, ResourceLocation recipeId, int totalTicks,
			int riteSize) {
		this.playerUUID = playerUUID;
		this.centerPos = centerPos;
		this.recipeId = recipeId;
		this.totalTicks = Math.max(1, totalTicks);
		this.riteSize = riteSize;
		this.remainingTicks = this.totalTicks;
	}

	public static ActiveCardinalRite interactive(UUID playerUUID, BlockPos centerPos, ResourceLocation recipeId,
			int totalTicks, int riteSize, int degree, boolean abbreviated, int totalWaves) {
		return interactive(playerUUID, centerPos, recipeId, totalTicks, riteSize, degree, abbreviated, totalWaves,
				CardinalRiteCeremonyRules.anchorCount(degree));
	}

	public static ActiveCardinalRite interactive(UUID playerUUID, BlockPos centerPos, ResourceLocation recipeId,
			int totalTicks, int riteSize, int degree, boolean abbreviated, int totalWaves, int anchorCount) {
		ActiveCardinalRite rite = new ActiveCardinalRite(playerUUID, centerPos, recipeId, totalTicks, riteSize);
		rite.phase = CardinalRitePhase.CONSECRATION;
		rite.degree = Math.max(1, degree);
		rite.abbreviated = abbreviated;
		rite.totalWaves = abbreviated ? 1 : Math.max(1, totalWaves);
		rite.anchorBloodMl = new int[Math.max(1, anchorCount)];
		rite.instabilityRepairBloodMl = new int[rite.anchorBloodMl.length];
		return rite;
	}

	public void tick() {
		if (phase == CardinalRitePhase.LEGACY) {
			if (remainingTicks > 0) remainingTicks--;
			return;
		}
		if (!isTerminal()) {
			phaseTicks++;
			if (carriedIchorTicks > 0 && --carriedIchorTicks == 0) {
				carriedIchorMl = 0;
			}
		}
	}

	public boolean fillAnchor(int anchorIndex, int availableBloodMl) {
		if (phase != CardinalRitePhase.CONSECRATION && phase != CardinalRitePhase.ORDEAL
				&& phase != CardinalRitePhase.STILL_INTERVAL) return false;
		if (anchorIndex < 0 || anchorIndex >= anchorBloodMl.length || availableBloodMl <= 0) return false;
		int missing = CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML - anchorBloodMl[anchorIndex];
		if (missing <= 0) return false;
		int accepted = Math.min(missing, availableBloodMl);
		anchorBloodMl[anchorIndex] += accepted;
		committedBloodMl += accepted;
		idleTicks = 0;
		return accepted > 0;
	}

	public int bloodNeededForAnchor(int anchorIndex) {
		if (anchorIndex < 0 || anchorIndex >= anchorBloodMl.length) return 0;
		return Math.max(0, CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML - anchorBloodMl[anchorIndex]);
	}

	public boolean areAnchorsConsecrated() {
		if (anchorBloodMl.length == 0) return false;
		for (int blood : anchorBloodMl) {
			if (blood < CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML) return false;
		}
		return true;
	}

	public int completedRings() {
		int completed = 0;
		for (int ring = 0; ring < degree; ring++) {
			boolean full = true;
			int start = ring * CardinalRiteCeremonyRules.ANCHORS_PER_DEGREE;
			for (int i = start; i < Math.min(start + CardinalRiteCeremonyRules.ANCHORS_PER_DEGREE,
					anchorBloodMl.length); i++) {
				if (anchorBloodMl[i] < CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML) {
					full = false;
					break;
				}
			}
			if (!full) break;
			completed++;
		}
		return completed;
	}

	public boolean enterInscription() {
		if (phase != CardinalRitePhase.CONSECRATION || !areAnchorsConsecrated()) return false;
		setPhase(CardinalRitePhase.INSCRIPTION);
		return true;
	}

	public boolean sealAltar() {
		if (phase != CardinalRitePhase.INSCRIPTION) return false;
		altarSealed = true;
		setPhase(CardinalRitePhase.ORDEAL);
		return true;
	}

	public void completeWave() {
		if (phase != CardinalRitePhase.ORDEAL) return;
		currentWave = Math.min(totalWaves, currentWave + 1);
		if (currentWave >= totalWaves) setPhase(CardinalRitePhase.STILL_INTERVAL);
		else setPhase(CardinalRitePhase.ORDEAL);
	}

	public void finishStillInterval(boolean professionRite) {
		if (phase != CardinalRitePhase.STILL_INTERVAL) return;
		setPhase(professionRite ? CardinalRitePhase.PROFESSION : CardinalRitePhase.CULMINATION);
	}

	public void finishProfession() {
		if (phase == CardinalRitePhase.PROFESSION) setPhase(CardinalRitePhase.CULMINATION);
	}

	public void advanceProfessionStep() {
		if (phase == CardinalRitePhase.PROFESSION) professionStep++;
	}

	public void professionMistake() {
		professionMistakes++;
	}

	public void enterRecoveryWave(boolean severe) {
		if (phase != CardinalRitePhase.PROFESSION) return;
		totalWaves++;
		if (severe) addInstability(20);
		else addInstability(10);
		if (!isTerminal()) setPhase(CardinalRitePhase.ORDEAL);
	}

	public int drainAnchor(int anchorIndex, int requestedMl) {
		if (anchorIndex < 0 || anchorIndex >= anchorBloodMl.length || requestedMl <= 0) return 0;
		int drained = Math.min(requestedMl, anchorBloodMl[anchorIndex]);
		anchorBloodMl[anchorIndex] -= drained;
		return drained;
	}

	public void addInstability(int amount) {
		instability = Math.max(0, Math.min(CardinalRiteCeremonyRules.COLLAPSE_INSTABILITY, instability + amount));
		reconcileInstabilityDamage();
		if (instability >= CardinalRiteCeremonyRules.COLLAPSE_INSTABILITY) {
			setPhase(CardinalRitePhase.COLLAPSED);
		}
	}

	public void stabilize(int amount) {
		addInstability(-Math.max(0, amount));
	}

	public boolean offerInstabilityRepair(int anchorIndex, int offeredBloodMl) {
		if (!isInstabilityDamagedAnchor(anchorIndex) || offeredBloodMl <= 0) return false;
		ensureInstabilityRepairArray();
		int accepted = Math.min(offeredBloodMl,
				CardinalRiteInstabilityBoundaryRules.REPAIR_BLOOD_ML
						- instabilityRepairBloodMl[anchorIndex]);
		if (accepted <= 0) return false;
		instabilityRepairBloodMl[anchorIndex] += accepted;
		if (instabilityRepairBloodMl[anchorIndex]
				>= CardinalRiteInstabilityBoundaryRules.REPAIR_BLOOD_ML) {
			instabilityRepairBloodMl[anchorIndex] = 0;
			brokenInstabilityAnchors.remove(anchorIndex);
			instability = Math.max(0, instability
					- CardinalRiteInstabilityBoundaryRules.repairInstabilityAmount(anchorBloodMl.length));
			reconcileInstabilityDamage();
		}
		return true;
	}

	public int instabilityRepairBloodNeeded(int anchorIndex) {
		if (!isInstabilityDamagedAnchor(anchorIndex)) return 0;
		ensureInstabilityRepairArray();
		return Math.max(0, CardinalRiteInstabilityBoundaryRules.REPAIR_BLOOD_ML
				- instabilityRepairBloodMl[anchorIndex]);
	}

	public boolean isInstabilityDamagedAnchor(int anchorIndex) {
		if (anchorIndex < 0 || anchorIndex >= anchorBloodMl.length) return false;
		return brokenInstabilityAnchors.contains(anchorIndex)
				|| threatenedInstabilityAnchor() == anchorIndex;
	}

	public float instabilityAnchorIntegrity(int anchorIndex) {
		if (brokenInstabilityAnchors.contains(anchorIndex)) return 0.0F;
		if (threatenedInstabilityAnchor() != anchorIndex) return 1.0F;
		return (float) (1.0D - CardinalRiteInstabilityBoundaryRules.flickerProgress(
				instability, anchorBloodMl.length));
	}

	private int threatenedInstabilityAnchor() {
		if (CardinalRiteInstabilityBoundaryRules.flickerProgress(instability, anchorBloodMl.length) <= 0.0D) {
			return -1;
		}
		for (int index : effectiveInstabilityDamagePriority()) {
			if (!brokenInstabilityAnchors.contains(index)) return index;
		}
		return -1;
	}

	private void reconcileInstabilityDamage() {
		int desired = CardinalRiteInstabilityBoundaryRules.brokenAnchorCount(
				instability, anchorBloodMl.length);
		brokenInstabilityAnchors.removeIf(index -> index < 0 || index >= anchorBloodMl.length);
		while (brokenInstabilityAnchors.size() > desired) {
			int last = -1;
			for (int index : brokenInstabilityAnchors) last = index;
			if (last < 0) break;
			brokenInstabilityAnchors.remove(last);
		}
		for (int index : effectiveInstabilityDamagePriority()) {
			if (brokenInstabilityAnchors.size() >= desired) break;
			brokenInstabilityAnchors.add(index);
		}
		ensureInstabilityRepairArray();
		for (int index = 0; index < instabilityRepairBloodMl.length; index++) {
			if (!isInstabilityDamagedAnchor(index)) instabilityRepairBloodMl[index] = 0;
		}
	}

	private void ensureInstabilityRepairArray() {
		if (instabilityRepairBloodMl.length != anchorBloodMl.length) {
			instabilityRepairBloodMl = java.util.Arrays.copyOf(
					instabilityRepairBloodMl, anchorBloodMl.length);
		}
	}

	public void setInstabilityDamagePriority(int[] priority) {
		if (priority == null || priority.length != anchorBloodMl.length) return;
		boolean[] seen = new boolean[anchorBloodMl.length];
		for (int index : priority) {
			if (index < 0 || index >= seen.length || seen[index]) return;
			seen[index] = true;
		}
		if (java.util.Arrays.equals(instabilityDamagePriority, priority)) return;
		instabilityDamagePriority = priority.clone();
		brokenInstabilityAnchors.clear();
		reconcileInstabilityDamage();
	}

	private int[] effectiveInstabilityDamagePriority() {
		if (instabilityDamagePriority.length == anchorBloodMl.length) return instabilityDamagePriority;
		int[] natural = new int[anchorBloodMl.length];
		for (int index = 0; index < natural.length; index++) natural[index] = index;
		return natural;
	}

	public void setSigilProgress(String sigilId, int nodes) {
		if (sigilId != null && !sigilId.isBlank()) sigilProgress.put(sigilId, Math.max(0, nodes));
	}

	public boolean isSigilComplete(String sigilId, int totalNodes) {
		return totalNodes > 0 && sigilProgress.getOrDefault(sigilId, 0) >= totalNodes;
	}

	public boolean awakenSigil(String progressKey) {
		return progressKey != null && !progressKey.isBlank() && awakenedSigils.add(progressKey);
	}

	public boolean isSigilAwakened(String progressKey) {
		return progressKey != null && awakenedSigils.contains(progressKey);
	}

	public int storeReservoirBlood(int offeredMl, int capacityMl) {
		int accepted = Math.min(Math.max(0, offeredMl), Math.max(0, capacityMl - reservoirBloodMl));
		reservoirBloodMl += accepted;
		return accepted;
	}

	public int drawReservoirBlood(int requestedMl) {
		int drawn = Math.min(Math.max(0, requestedMl), reservoirBloodMl);
		reservoirBloodMl -= drawn;
		return drawn;
	}

	public void balanceAnchors() {
		for (int start = 0; start < anchorBloodMl.length; start += CardinalRiteCeremonyRules.ANCHORS_PER_DEGREE) {
			int end = Math.min(anchorBloodMl.length, start + CardinalRiteCeremonyRules.ANCHORS_PER_DEGREE);
			int total = 0;
			for (int i = start; i < end; i++) total += anchorBloodMl[i];
			int average = total / (end - start);
			int remainder = total % (end - start);
			for (int i = start; i < end; i++) anchorBloodMl[i] = average + (i - start < remainder ? 1 : 0);
		}
	}

	public boolean tryUseAttendantCatch(UUID attendant) {
		String key = "attendant:" + attendant;
		if (sigilProgress.getOrDefault(key, -1) == currentWave) return false;
		sigilProgress.put(key, currentWave);
		return true;
	}

	public boolean hasUsedAttendantCatch(UUID attendant) {
		return sigilProgress.getOrDefault("attendant:" + attendant, -1) == currentWave;
	}

	public void assignAlly(UUID ally, CardinalRiteAllyRole role) {
		if (ally != null && role != null) allyRoles.put(ally, role);
	}

	public void removeAlly(UUID ally) {
		allyRoles.remove(ally);
		sharedPoolOptIns.remove(ally);
	}

	public void setSharedPoolOptIn(UUID player, boolean enabled) {
		if (enabled) sharedPoolOptIns.add(player);
		else sharedPoolOptIns.remove(player);
	}

	public void addRiteThreat(UUID threat) {
		if (threat != null) riteThreats.add(threat);
	}

	public void removeRiteThreat(UUID threat) {
		riteThreats.remove(threat);
	}

	public void clearRiteThreats() {
		riteThreats.clear();
	}

	public void carryIchor(int bloodMl) {
		if (bloodMl <= 0) return;
		carriedIchorMl += bloodMl;
		carriedIchorTicks = CardinalRiteCeremonyRules.ICHOR_TTL_TICKS;
	}

	public int consumeCarriedIchor(int requestedMl) {
		int used = Math.min(Math.max(0, requestedMl), carriedIchorMl);
		carriedIchorMl -= used;
		if (carriedIchorMl == 0) carriedIchorTicks = 0;
		return used;
	}

	public boolean isComplete() {
		return phase == CardinalRitePhase.LEGACY ? remainingTicks <= 0 : phase == CardinalRitePhase.COMPLETE;
	}

	public boolean isTerminal() {
		return phase == CardinalRitePhase.COMPLETE || phase == CardinalRitePhase.COLLAPSED;
	}

	public void markComplete() {
		setPhase(CardinalRitePhase.COMPLETE);
	}

	public void markCollapsed() {
		setPhase(CardinalRitePhase.COLLAPSED);
	}

	private void setPhase(CardinalRitePhase phase) {
		this.phase = phase;
		this.phaseTicks = 0;
		this.idleTicks = 0;
	}

	public UUID getPlayerUUID() { return playerUUID; }
	public BlockPos getCenterPos() { return centerPos; }
	public ResourceLocation getRecipeId() { return recipeId; }
	public int getTotalTicks() { return totalTicks; }
	public int getRemainingTicks() { return remainingTicks; }
	public int getRiteSize() { return riteSize; }
	public CardinalRitePhase getPhase() { return phase; }
	public int getDegree() { return degree; }
	public boolean isAbbreviated() { return abbreviated; }
	public boolean isAltarSealed() { return altarSealed; }
	public int getPhaseTicks() { return phaseTicks; }
	public int getIdleTicks() { return idleTicks; }
	public void incrementIdleTicks() { idleTicks++; }
	public int getDisconnectTicks() { return disconnectTicks; }
	public void setDisconnectTicks(int disconnectTicks) { this.disconnectTicks = Math.max(0, disconnectTicks); }
	public int getInstability() { return instability; }
	public CardinalRiteInstability getInstabilityBand() {
		return CardinalRiteCeremonyRules.instabilityBand(instability);
	}
	public int getCurrentWave() { return currentWave; }
	public int getTotalWaves() { return totalWaves; }
	public int getCommittedBloodMl() { return committedBloodMl; }
	public int getCarriedIchorMl() { return carriedIchorMl; }
	public int getCarriedIchorTicks() { return carriedIchorTicks; }
	public int getReservoirBloodMl() { return reservoirBloodMl; }
	public int getProfessionStep() { return professionStep; }
	public int getProfessionMistakes() { return professionMistakes; }
	public int getProfessionChoice() { return professionChoice; }
	public void setProfessionChoice(int professionChoice) { this.professionChoice = professionChoice; }
	public int[] getAnchorBloodMl() { return anchorBloodMl.clone(); }
	public Set<Integer> getBrokenInstabilityAnchors() { return Set.copyOf(brokenInstabilityAnchors); }
	public Map<String, Integer> getSigilProgress() { return Map.copyOf(sigilProgress); }
	public Set<String> getAwakenedSigils() { return Set.copyOf(awakenedSigils); }
	public Map<UUID, CardinalRiteAllyRole> getAllyRoles() { return Map.copyOf(allyRoles); }
	public Set<UUID> getSharedPoolOptIns() { return Set.copyOf(sharedPoolOptIns); }
	public Map<UUID, Integer> getNpcReservesMl() { return npcReservesMl; }
	public Map<UUID, Long> getBloodspentUntil() { return bloodspentUntil; }
	public Set<UUID> getRiteThreats() { return Set.copyOf(riteThreats); }
	public List<String> getWaveDeck() { return waveDeck; }

	public double getProgress() {
		return getProgress(CardinalRiteCeremonyRules.stillIntervalTicks(0));
	}

	public double getProgress(int stillIntervalTicks) {
		if (phase == CardinalRitePhase.LEGACY) {
			return 1.0D - (double) remainingTicks / totalTicks;
		}
		return switch (phase) {
			case CONSECRATION -> anchorBloodMl.length == 0 ? 0.0D
					: 0.25D * committedBloodMl
							/ (anchorBloodMl.length * (double) CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML);
			case INSCRIPTION -> 0.25D;
			case ORDEAL -> 0.25D + (totalWaves == 0 ? 0.0D : 0.5D * currentWave / totalWaves);
			case STILL_INTERVAL -> 0.75D + 0.10D * boundedPhaseProgress(stillIntervalTicks);
			case PROFESSION -> 0.85D;
			case CULMINATION -> 0.95D + 0.05D * boundedPhaseProgress(40);
			case COMPLETE -> 1.0D;
			case COLLAPSED -> 0.0D;
			default -> 0.0D;
		};
	}

	private double boundedPhaseProgress(int durationTicks) {
		if (durationTicks <= 0) return 1.0D;
		return Math.max(0.0D, Math.min(1.0D, phaseTicks / (double) durationTicks));
	}

	public CompoundTag serialize() {
		CompoundTag tag = new CompoundTag();
		tag.putInt(STATE_VERSION, CURRENT_STATE_VERSION);
		tag.putUUID("PlayerUUID", playerUUID);
		tag.putLong("CenterPos", centerPos.asLong());
		tag.putString("RecipeId", recipeId.toString());
		tag.putInt("TotalTicks", totalTicks);
		tag.putInt("RemainingTicks", remainingTicks);
		tag.putInt("RiteSize", riteSize);
		tag.putString("Phase", phase.name());
		tag.putInt("Degree", degree);
		tag.putBoolean("Abbreviated", abbreviated);
		tag.putBoolean("AltarSealed", altarSealed);
		tag.putInt("PhaseTicks", phaseTicks);
		tag.putInt("IdleTicks", idleTicks);
		tag.putInt("DisconnectTicks", disconnectTicks);
		tag.putInt("Instability", instability);
		tag.putInt("CurrentWave", currentWave);
		tag.putInt("TotalWaves", totalWaves);
		tag.putInt("CommittedBloodMl", committedBloodMl);
		tag.putInt("CarriedIchorMl", carriedIchorMl);
		tag.putInt("CarriedIchorTicks", carriedIchorTicks);
		tag.putInt("ReservoirBloodMl", reservoirBloodMl);
		tag.putInt("ProfessionStep", professionStep);
		tag.putInt("ProfessionMistakes", professionMistakes);
		tag.putInt("ProfessionChoice", professionChoice);
		tag.putIntArray("AnchorBloodMl", anchorBloodMl);
		tag.putIntArray("InstabilityRepairBloodMl", instabilityRepairBloodMl);
		tag.putIntArray("InstabilityDamagePriority", instabilityDamagePriority);
		tag.putIntArray("BrokenInstabilityAnchors",
				brokenInstabilityAnchors.stream().mapToInt(Integer::intValue).toArray());

		CompoundTag sigils = new CompoundTag();
		sigilProgress.forEach(sigils::putInt);
		tag.put("SigilProgress", sigils);
		ListTag awakened = new ListTag();
		for (String progressKey : awakenedSigils) {
			CompoundTag entry = new CompoundTag();
			entry.putString("ProgressKey", progressKey);
			awakened.add(entry);
		}
		tag.put("AwakenedSigils", awakened);
		tag.put("Allies", writeAllies());
		tag.put("SharedPoolOptIns", writeUuidList(sharedPoolOptIns));
		tag.put("NpcReserves", writeUuidIntMap(npcReservesMl, "BloodMl"));
		tag.put("BloodspentUntil", writeUuidLongMap(bloodspentUntil, "Until"));
		tag.put("RiteThreats", writeUuidList(riteThreats));
		ListTag waves = new ListTag();
		for (String wave : waveDeck) {
			CompoundTag entry = new CompoundTag();
			entry.putString("Id", wave);
			waves.add(entry);
		}
		tag.put("WaveDeck", waves);
		return tag;
	}

	private ListTag writeAllies() {
		ListTag list = new ListTag();
		allyRoles.forEach((uuid, role) -> {
			CompoundTag entry = new CompoundTag();
			entry.putUUID("UUID", uuid);
			entry.putString("Role", role.name());
			list.add(entry);
		});
		return list;
	}

	private static ListTag writeUuidList(Set<UUID> values) {
		ListTag list = new ListTag();
		for (UUID uuid : values) {
			CompoundTag entry = new CompoundTag();
			entry.putUUID("UUID", uuid);
			list.add(entry);
		}
		return list;
	}

	private static ListTag writeUuidIntMap(Map<UUID, Integer> values, String valueKey) {
		ListTag list = new ListTag();
		values.forEach((uuid, value) -> {
			CompoundTag entry = new CompoundTag();
			entry.putUUID("UUID", uuid);
			entry.putInt(valueKey, value);
			list.add(entry);
		});
		return list;
	}

	private static ListTag writeUuidLongMap(Map<UUID, Long> values, String valueKey) {
		ListTag list = new ListTag();
		values.forEach((uuid, value) -> {
			CompoundTag entry = new CompoundTag();
			entry.putUUID("UUID", uuid);
			entry.putLong(valueKey, value);
			list.add(entry);
		});
		return list;
	}

	public static ActiveCardinalRite deserialize(CompoundTag tag) {
		UUID playerUUID = tag.getUUID("PlayerUUID");
		BlockPos centerPos = BlockPos.of(tag.getLong("CenterPos"));
		ResourceLocation recipeId = ResourceLocation.parse(tag.getString("RecipeId"));
		int totalTicks = tag.getInt("TotalTicks");
		int riteSize = tag.getInt("RiteSize");
		ActiveCardinalRite rite = new ActiveCardinalRite(playerUUID, centerPos, recipeId, totalTicks, riteSize);
		rite.remainingTicks = tag.getInt("RemainingTicks");
		if (!tag.contains(STATE_VERSION)) return rite;

		rite.phase = CardinalRitePhase.byName(tag.getString("Phase"));
		rite.degree = tag.getInt("Degree");
		rite.abbreviated = tag.getBoolean("Abbreviated");
		rite.altarSealed = tag.getBoolean("AltarSealed");
		rite.phaseTicks = tag.getInt("PhaseTicks");
		rite.idleTicks = tag.getInt("IdleTicks");
		rite.disconnectTicks = tag.getInt("DisconnectTicks");
		rite.instability = tag.getInt("Instability");
		rite.currentWave = tag.getInt("CurrentWave");
		rite.totalWaves = tag.getInt("TotalWaves");
		rite.committedBloodMl = tag.getInt("CommittedBloodMl");
		rite.carriedIchorMl = tag.getInt("CarriedIchorMl");
		rite.carriedIchorTicks = tag.getInt("CarriedIchorTicks");
		rite.reservoirBloodMl = tag.getInt("ReservoirBloodMl");
		rite.professionStep = tag.getInt("ProfessionStep");
		rite.professionMistakes = tag.getInt("ProfessionMistakes");
		rite.professionChoice = tag.contains("ProfessionChoice") ? tag.getInt("ProfessionChoice") : -1;
		rite.anchorBloodMl = tag.getIntArray("AnchorBloodMl");
		rite.instabilityRepairBloodMl = tag.getIntArray("InstabilityRepairBloodMl");
		rite.instabilityDamagePriority = tag.getIntArray("InstabilityDamagePriority");
		for (int anchor : tag.getIntArray("BrokenInstabilityAnchors")) {
			rite.brokenInstabilityAnchors.add(anchor);
		}
		rite.reconcileInstabilityDamage();

		CompoundTag sigils = tag.getCompound("SigilProgress");
		for (String key : sigils.getAllKeys()) rite.sigilProgress.put(key, sigils.getInt(key));
		ListTag awakened = tag.getList("AwakenedSigils", Tag.TAG_COMPOUND);
		for (int i = 0; i < awakened.size(); i++) {
			String progressKey = awakened.getCompound(i).getString("ProgressKey");
			if (!progressKey.isBlank()) rite.awakenedSigils.add(progressKey);
		}
		readAllies(tag.getList("Allies", Tag.TAG_COMPOUND), rite.allyRoles);
		readUuidSet(tag.getList("SharedPoolOptIns", Tag.TAG_COMPOUND), rite.sharedPoolOptIns);
		readUuidIntMap(tag.getList("NpcReserves", Tag.TAG_COMPOUND), rite.npcReservesMl, "BloodMl");
		readUuidLongMap(tag.getList("BloodspentUntil", Tag.TAG_COMPOUND), rite.bloodspentUntil, "Until");
		readUuidSet(tag.getList("RiteThreats", Tag.TAG_COMPOUND), rite.riteThreats);
		ListTag waves = tag.getList("WaveDeck", Tag.TAG_COMPOUND);
		for (int i = 0; i < waves.size(); i++) rite.waveDeck.add(waves.getCompound(i).getString("Id"));
		return rite;
	}

	private static void readAllies(ListTag list, Map<UUID, CardinalRiteAllyRole> output) {
		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);
			try {
				output.put(entry.getUUID("UUID"), CardinalRiteAllyRole.valueOf(entry.getString("Role")));
			} catch (IllegalArgumentException ignored) {
				// Ignore removed roles while preserving the rest of the rite.
			}
		}
	}

	private static void readUuidSet(ListTag list, Set<UUID> output) {
		for (int i = 0; i < list.size(); i++) output.add(list.getCompound(i).getUUID("UUID"));
	}

	private static void readUuidIntMap(ListTag list, Map<UUID, Integer> output, String valueKey) {
		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);
			output.put(entry.getUUID("UUID"), entry.getInt(valueKey));
		}
	}

	private static void readUuidLongMap(ListTag list, Map<UUID, Long> output, String valueKey) {
		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);
			output.put(entry.getUUID("UUID"), entry.getLong(valueKey));
		}
	}
}
