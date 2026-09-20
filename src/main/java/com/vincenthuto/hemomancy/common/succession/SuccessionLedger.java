package com.vincenthuto.hemomancy.common.succession;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import java.util.*;

/** Identity and position locks. All mutations run on the server thread. */
public final class SuccessionLedger {
    public record Workplace(String dimension, long position) {}
    public record Life(boolean alive, int generation) {}
    public record Reservation(Workplace workplace, UUID identity, int generation) {}
    private final Map<UUID, Life> lives = new HashMap<>();
    private final Map<UUID, Reservation> reservations = new HashMap<>();
    private final Map<Workplace, UUID> claims = new HashMap<>();

    public boolean birth(UUID id) { return lives.putIfAbsent(id, new Life(true, 0)) == null; }
    public Life life(UUID id) { return lives.get(id); }
    public Map<UUID, Reservation> reservations() { return Collections.unmodifiableMap(reservations); }
    public boolean available(Workplace workplace) {
        return !claims.containsKey(workplace) && reservations.values().stream().noneMatch(r -> r.workplace.equals(workplace));
    }
    public boolean locked(UUID id) { return reservations.values().stream().anyMatch(r -> id.equals(r.identity)); }
    public boolean reserve(UUID rite, Workplace workplace, UUID identity, int generation) {
        if (reservations.containsKey(rite) || !available(workplace)) return false;
        if (identity != null) {
            Life life = lives.get(identity);
            if (life == null || life.alive || life.generation != generation || locked(identity)) return false;
        }
        reservations.put(rite, new Reservation(workplace, identity, generation));
        return true;
    }
    public void release(UUID rite) { reservations.remove(rite); }
    public void unclaim(UUID identity) { claims.values().removeIf(identity::equals); }
    public boolean claim(UUID identity, Workplace workplace) {
        if (!available(workplace)) return false;
        unclaim(identity);
        claims.put(workplace, identity);
        return true;
    }
    public boolean owns(UUID identity, Workplace workplace) { return identity.equals(claims.get(workplace)); }
    public boolean finishBirth(UUID rite, UUID identity) {
        Reservation reservation = reservations.get(rite);
        if (reservation == null || reservation.identity != null || !birth(identity)) return false;
        release(rite);
        claims.put(reservation.workplace, identity);
        return true;
    }
    public int die(UUID identity) {
        Life life = lives.get(identity);
        if (life == null || !life.alive) return -1;
        unclaim(identity);
        lives.put(identity, new Life(false, life.generation + 1));
        return life.generation + 1;
    }
    public int reissue(UUID identity) {
        Life life = lives.get(identity);
        if (life == null || life.alive || locked(identity)) return -1;
        lives.put(identity, new Life(false, life.generation + 1));
        return life.generation + 1;
    }
    public boolean restore(UUID rite, UUID identity) {
        Reservation reservation = reservations.get(rite);
        Life life = lives.get(identity);
        if (reservation == null || !identity.equals(reservation.identity) || life == null || life.alive
                || life.generation != reservation.generation) return false;
        lives.put(identity, new Life(true, life.generation));
        release(rite);
        claims.put(reservation.workplace, identity);
        return true;
    }
    public CompoundTag save() {
        var tag = new CompoundTag();
        var lifeTags = new ListTag();
        lives.forEach((id, life) -> {
            var entry = new CompoundTag(); entry.putUUID("Id", id); entry.putBoolean("Alive", life.alive);
            entry.putInt("Generation", life.generation); lifeTags.add(entry);
        });
        tag.put("Lives", lifeTags);
        var lockTags = new ListTag();
        reservations.forEach((id, r) -> {
            var entry = positionTag(r.workplace); entry.putUUID("Rite", id);
            if (r.identity != null) entry.putUUID("Identity", r.identity);
            entry.putInt("Generation", r.generation); lockTags.add(entry);
        });
        tag.put("Reservations", lockTags);
        var claimTags = new ListTag();
        claims.forEach((place, id) -> { var entry = positionTag(place); entry.putUUID("Identity", id); claimTags.add(entry); });
        tag.put("Claims", claimTags);
        return tag;
    }
    public static SuccessionLedger load(CompoundTag tag) {
        var ledger = new SuccessionLedger();
        for (Tag raw : tag.getList("Lives", Tag.TAG_COMPOUND)) {
            var e = (CompoundTag) raw;
            ledger.lives.put(e.getUUID("Id"), new Life(e.getBoolean("Alive"), e.getInt("Generation")));
        }
        for (Tag raw : tag.getList("Reservations", Tag.TAG_COMPOUND)) {
            var e = (CompoundTag) raw;
            ledger.reservations.put(e.getUUID("Rite"), new Reservation(position(e), e.hasUUID("Identity") ? e.getUUID("Identity") : null, e.getInt("Generation")));
        }
        for (Tag raw : tag.getList("Claims", Tag.TAG_COMPOUND)) {
            var e = (CompoundTag) raw; ledger.claims.put(position(e), e.getUUID("Identity"));
        }
        return ledger;
    }
    public static CompoundTag positionTag(Workplace place) {
        var tag = new CompoundTag(); tag.putString("Dimension", place.dimension); tag.putLong("Position", place.position); return tag;
    }
    public static Workplace position(CompoundTag tag) { return new Workplace(tag.getString("Dimension"), tag.getLong("Position")); }
}
