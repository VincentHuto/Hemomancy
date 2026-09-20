package com.vincenthuto.hemomancy.common.succession;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.*;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import java.util.*;

public final class SuccessionRites {
    public static final int DURATION = 1200;
    private SuccessionRites() {}
    public static boolean isRecipe(ResourceLocation id) {
        return id.getNamespace().equals("hemomancy") && (id.getPath().equals("cardinal_rite/hematic_succession")
                || id.getPath().equals("cardinal_rite/mnemonic_restoration"));
    }
    public static boolean is(ActiveCardinalRite rite) { return !rite.succession().isEmpty(); }
    public static boolean chunksReady(ServerLevel level, ActiveCardinalRite rite) {
        var c = rite.succession();
        if (!level.hasChunkAt(rite.getCenterPos()) || !level.hasChunkAt(BlockPos.of(c.getLong("Body")))
                || !level.hasChunkAt(BlockPos.of(c.getLong("Workplace")))) return false;
        for (Tag raw : c.getList("Offerings", Tag.TAG_COMPOUND))
            if (!level.hasChunkAt(BlockPos.of(((CompoundTag) raw).getLong("Position")))) return false;
        // The complete floor must be available too, including sockets across chunk boundaries.
        for (var pos : BlockPos.betweenClosed(rite.getCenterPos().offset(-7, 0, -7), rite.getCenterPos().offset(7, 0, 7)))
            if (!level.hasChunkAt(pos)) return false;
        return true;
    }
    public static boolean prepare(ServerLevel level, ServerPlayer player, ActiveCardinalRite rite,
            CardinalRiteStationMatcher.StationMatch station) {
        var line = SuccessionResidents.line(player);
        if (station == null || !SuccessionDialogue.eligible(player) || line == null
                || !FoundingFaneSavedData.get(level).isWithinFane(line.getLeaderUUID(), rite.getCenterPos()))
            return fail(player, "eligibility");
        boolean restoring = rite.getRecipeId().getPath().endsWith("mnemonic_restoration");
        var offerings = station.braziers();
        if (offerings.size() != (restoring ? 2 : 3)) return fail(player, "offerings");
        var forward = station.floorMatch().getForwards();
        var up = station.floorMatch().getUp();
        var right = forward.getNormal().cross(up.getNormal());
        for (int i = 0; i < offerings.size(); i++) {
            var socket = station.floor().brazierSockets().get(i);
            var expected = rite.getCenterPos().offset(right.getX()*socket.getX()+forward.getStepX()*socket.getZ(),
                    socket.getY(), right.getZ()*socket.getX()+forward.getStepZ()*socket.getZ());
            if (!offerings.get(i).pos().equals(expected)) return fail(player, "offerings");
        }
        // Floor socket order, not sample identity, assigns the donor and officiant roles.
        var first = offerings.get(0).stack(); var will = offerings.get(1).stack();
        if (!(will.getItem() instanceof BloodVialItem) || SuccessionSamples.identity(will).isEmpty()) return fail(player, "legacy_sample");
        var data = SuccessionSavedData.get(level);
        int lineCap = com.vincenthuto.hemomancy.config.HemoServerConfig.SUCCESSORS_PER_BLOODLINE.get();
        int globalCap = com.vincenthuto.hemomancy.config.HemoServerConfig.SUCCESSORS_GLOBAL.get();
        long living = data.residents.values().stream().filter(r -> data.ledger.life(r.id) != null && data.ledger.life(r.id).alive()).count();
        long own = data.residents.values().stream().filter(r -> r.bloodline.equals(line.getBloodlineUUID()) && data.ledger.life(r.id) != null && data.ledger.life(r.id).alive()).count();
        long pending = data.ledger.reservations().size();
        long ownPending = java.util.stream.StreamSupport.stream(level.getServer().getAllLevels().spliterator(), false)
                .flatMap(l -> CardinalRiteSavedData.get(l).getActiveRites().values().stream())
                .filter(r -> is(r) && r.succession().getUUID("Bloodline").equals(line.getBloodlineUUID())).count();
        if (globalCap > 0 && living + pending >= globalCap || lineCap > 0 && own + ownPending >= lineCap) return fail(player, "cap");
        var context = new CompoundTag(); UUID identity;
        String profession;
        if (restoring) {
            if (!(first.getItem() instanceof BoundMnemonicRemnantItem)) return fail(player, "offerings");
            var relic = BoundMnemonicRemnantItem.data(first);
            if (!relic.hasUUID("Id")) return fail(player, "remnant");
            identity = relic.getUUID("Id"); var person = data.residents.get(identity); var life = data.ledger.life(identity);
            if (person == null || person.dismissed || !person.bloodline.equals(line.getBloodlineUUID())
                    || life == null || life.alive() || life.generation() != relic.getInt("Generation")) return fail(player, "remnant");
            profession = person.profession; context.putInt("Generation", life.generation());
        } else {
            if (!(first.getItem() instanceof BloodVialItem) || SuccessionSamples.identity(first).isEmpty()) return fail(player, "legacy_sample");
            profession = SuccessionSamples.identity(first).getString("Profession"); identity = UUID.randomUUID();
            if (!SuccessionProfessions.ROLES.contains(profession)) return fail(player, "profession");
            if (!offerings.get(2).stack().is(ItemInit.mnemonic_ambergris.get())) return fail(player, "offerings");
        }
        BlockPos body = rite.getCenterPos().offset(right.getX()*2, 2, right.getZ()*2);
        var bodyState = level.getBlockState(body);
        if (!bodyState.is(BlockInit.vacant_effigy.get()) || bodyState.getValue(VacantEffigyBlock.FORMING)) return fail(player, "body");
        for (var active : CardinalRiteSavedData.get(level).getActiveRites().values())
            if (is(active) && active.succession().getLong("Body") == body.asLong()) return fail(player, "reserved");
        BlockPos workplace = SuccessionWorkplaces.find(level, line.getLeaderUUID(), profession, body);
        if (workplace == null) return fail(player, "workplace");
        UUID transaction = UUID.randomUUID();
        var place = new SuccessionLedger.Workplace(level.dimension().location().toString(), workplace.asLong());
        if (!data.ledger.reserve(transaction, place, restoring ? identity : null, context.getInt("Generation"))) return fail(player, "reserved");
        context.putUUID("Transaction", transaction); context.putUUID("Identity", identity);
        context.putUUID("Bloodline", line.getBloodlineUUID()); context.putUUID("FaneOwner", line.getLeaderUUID());
        context.putUUID("Officiant", player.getUUID()); context.putString("OfficiantName", player.getName().getString());
        context.putString("Profession", profession); context.putString("Dimension", place.dimension());
        context.putLong("Workplace", workplace.asLong()); context.putLong("Body", body.asLong());
        context.putBoolean("Restoring", restoring); context.putBoolean("Valid", true);
        context.putLong("Seed", level.random.nextLong());
        var savedOfferings = new ListTag();
        for (var offering : offerings) {
            var entry = new CompoundTag(); entry.putLong("Position", offering.pos().asLong());
            entry.put("Stack", offering.stack().save(level.registryAccess())); savedOfferings.add(entry);
        }
        context.put("Offerings", savedOfferings);
        rite.beginSuccession(context); data.setDirty(); return true;
    }
    public static boolean tick(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
        var c = rite.succession();
        if (c.getBoolean("Resolved")) return false;
        var body = BlockPos.of(c.getLong("Body")); var work = BlockPos.of(c.getLong("Workplace"));
        if (!chunksReady(level, rite)) return true;
        if (rite.getPhase() == CardinalRitePhase.COLLAPSED || recipe == null || !level.getBlockState(body).is(BlockInit.vacant_effigy.get())) return false;
        // The consumed focus medium is no longer required after Life; the floor and plinth remain mandatory.
        if (!structurallyValid(level, rite, recipe, c)) { fail(caster, "interrupted"); return false; }
        var line = BloodlineSavedData.get(level.getServer().overworld()).getBloodline(c.getUUID("Bloodline"));
        if (line != null && !SuccessionWorkplaces.valid(level, c.getUUID("FaneOwner"), c.getString("Profession"), work)) return false;
        int elapsed = c.getInt("Ticks");
        int phase = Math.min(5, elapsed / 200);
        if (elapsed % 200 == 0) caster.displayClientMessage(Component.translatable("hemomancy.succession.phase." + phase), true);
        if (phase == 0) {
            c.putBoolean("BodyCommitted", true);
            if (!level.getBlockState(body).getValue(VacantEffigyBlock.FORMING))
                level.setBlock(body, level.getBlockState(body).setValue(VacantEffigyBlock.FORMING, true), 3);
        }
        if (phase >= 1 && !c.getBoolean("BloodConsumed")) {
            if (!consumeBlood(level, rite)) return false;
            c.putBoolean("BloodConsumed", true);
        }
        if (phase >= 2 && !c.getBoolean("LifeConsumed")) {
            if (!(level.getBlockEntity(rite.getCenterPos()) instanceof CardinalFocusBlockEntity focus)
                    || !focus.getMediumForMatching().is(ItemInit.sanguine_quintessence.get())) return false;
            focus.extractMedium(); c.putBoolean("LifeConsumed", true);
        }
        if (phase >= 3 && !c.getBoolean("MemoryConsumed")) {
            if (!consumeOffering(level, c, c.getBoolean("Restoring") ? 0 : 2)) return false;
            c.putBoolean("MemoryConsumed", true);
        }
        var state = level.getBlockState(body);
        if (state.getValue(VacantEffigyBlock.PHASE) != phase) level.setBlock(body, state.setValue(VacantEffigyBlock.PHASE, phase), 3);
        SuccessionEffects.ritual(level, body, phase, elapsed);
        c.putInt("Ticks", elapsed + 1); rite.tick();
        if (elapsed + 1 < DURATION) return true;
        if (!resolve(level, caster, rite)) fail(caster, "interrupted");
        return false;
    }
    private static boolean structurallyValid(ServerLevel level, ActiveCardinalRite rite, CardinalRiteRecipe recipe, CompoundTag c) {
        // Match the saved floor directly: structure matching normally also demands the now-consumed medium.
        var floor = com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry.get(rite.getMatchedFloorId()).orElse(null);
        if (floor == null) return false;
        var forwards = rite.getFloorForwards(); var up = rite.getFloorUp(); var right = forwards.getNormal().cross(up.getNormal());
        var f = floor.focus();
        var origin = rite.getCenterPos().offset(up.getStepX()*f.getY()-right.getX()*f.getX()-forwards.getStepX()*f.getZ(),
                up.getStepY()*f.getY()-right.getY()*f.getX()-forwards.getStepY()*f.getZ(),
                up.getStepZ()*f.getY()-right.getZ()*f.getX()-forwards.getStepZ()*f.getZ());
        if (floor.pattern().getBlockPattern().matches(level, origin, forwards, up) == null) return false;
        return level.getBlockState(BlockPos.of(c.getLong("Body")).below()).is(BlockInit.venous_stone.get());
    }
    private static boolean consumeBlood(ServerLevel level, ActiveCardinalRite rite) {
        var c = rite.succession(); var data = SuccessionSavedData.get(level);
        var will = SuccessionSamples.identity(offering(level, c, 1));
        boolean valid = data.authentic(will) && sourceMatches(offering(level, c, 1), will)
                && will.hasUUID("Donor") && will.getUUID("Donor").equals(c.getUUID("Officiant"))
                && will.getString("Type").equals("minecraft:player");
        if (!c.getBoolean("Restoring")) {
            var donor = SuccessionSamples.identity(offering(level, c, 0));
            valid &= data.authentic(donor) && sourceMatches(offering(level, c, 0), donor)
                    && donor.hasUUID("Donor") && donor.hasUUID("Bloodline")
                    && donor.getUUID("Bloodline").equals(c.getUUID("Bloodline"))
                    && donor.getString("Type").equals(SuccessionProfessions.entityType(c.getString("Profession")).toString())
                    && data.hasBequest(donor.getUUID("Donor"), c.getUUID("Bloodline"), c.getString("Profession"));
            c.put("DonorSample", donor.copy());
            // Check both vessels before removing either, so a changed offering cannot partially spend a pair.
            if (!matchesOffering(level, c, 0) || !matchesOffering(level, c, 1)) return false;
            consumeOffering(level, c, 0); data.spendSample(donor);
        }
        if (!consumeOffering(level, c, 1)) return false;
        data.spendSample(will); c.putBoolean("Valid", valid); return true;
    }
    private static boolean sourceMatches(ItemStack sample, CompoundTag identity) {
        return com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData.isStorableSample(sample)
                && com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData.rawSource(sample).equals(identity.getString("Type"));
    }
    private static ItemStack offering(ServerLevel level, CompoundTag c, int index) {
        var e = c.getList("Offerings", Tag.TAG_COMPOUND).getCompound(index);
        return ItemStack.parseOptional(level.registryAccess(), e.getCompound("Stack"));
    }
    private static boolean matchesOffering(ServerLevel level, CompoundTag c, int index) {
        var e = c.getList("Offerings", Tag.TAG_COMPOUND).getCompound(index);
        return level.getBlockEntity(BlockPos.of(e.getLong("Position"))) instanceof IronBrazierBlockEntity brazier
                && ItemStack.isSameItemSameComponents(brazier.getOfferingForMatching(), offering(level, c, index));
    }
    private static boolean consumeOffering(ServerLevel level, CompoundTag c, int index) {
        if (!matchesOffering(level, c, index)) return false;
        var e = c.getList("Offerings", Tag.TAG_COMPOUND).getCompound(index);
        return !((IronBrazierBlockEntity) level.getBlockEntity(BlockPos.of(e.getLong("Position")))).consumeOffering().isEmpty();
    }
    private static boolean resolve(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite) {
        var c = rite.succession(); var data = SuccessionSavedData.get(level); var body = BlockPos.of(c.getLong("Body"));
        UUID identity = c.getUUID("Identity"), transaction = c.getUUID("Transaction");
        boolean restoring = c.getBoolean("Restoring");
        var existingLife = data.ledger.life(identity);
        if (existingLife != null && existingLife.alive() && data.residents.containsKey(identity)
                && !data.ledger.reservations().containsKey(transaction)) {
            c.putBoolean("Resolved", true); level.removeBlock(body, false); rite.markComplete(); return true;
        }
        var entity = BuiltInRegistries.ENTITY_TYPE.get(SuccessionProfessions.entityType(c.getString("Profession"))).create(level);
        if (!(entity instanceof ProfessionalHarbingerEntity npc)) return false;
        if (c.getBoolean("Valid")) {
            var record = restoring ? data.residents.get(identity) : createRecord(c);
            if (restoring && record != null) {
                var moved = record.save(); moved.putString("Dimension", c.getString("Dimension"));
                moved.putUUID("FaneOwner", c.getUUID("FaneOwner")); moved.putLong("Workplace", c.getLong("Workplace"));
                record = new SuccessorRecord(moved);
            }
            if (record == null) return false;
            npc.initializeSuccessor(record); npc.setPos(body.getX() + .5, body.getY(), body.getZ() + .5);
            var existing = level.getEntity(identity);
            if (existing != null && (!(existing instanceof ProfessionalHarbingerEntity other) || !other.isSuccessor())) return false;
            if (existing == null && !level.addFreshEntity(npc)) return false;
            boolean committed = restoring ? data.ledger.restore(transaction, identity) : data.ledger.finishBirth(transaction, identity);
            if (!committed) { if (existing == null) npc.discard(); return false; }
            record.displaced = false; data.residents.put(identity, record); SuccessionResidents.join(level, record);
        } else {
            npc.initializeMisbegotten(c.getUUID("Officiant"), c.getLong("Seed"), restoring ? data.residents.get(identity).name : c.getCompound("DonorSample").getString("Name"));
            npc.setPos(body.getX() + .5, body.getY(), body.getZ() + .5);
            if (!level.addFreshEntity(npc)) return false;
            caster.displayClientMessage(Component.translatable("hemomancy.succession.rejection"), false);
        }
        c.putBoolean("Resolved", true); data.setDirty();
        level.removeBlock(body, false); rite.markComplete(); return true;
    }
    private static SuccessorRecord createRecord(CompoundTag c) {
        var t = c.copy(); t.putUUID("Id", c.getUUID("Identity")); var donor = c.getCompound("DonorSample");
        t.putUUID("Donor", donor.getUUID("Donor")); t.putString("DonorName", donor.getString("Name"));
        String[] first = {"Mara", "Iven", "Sera", "Corin", "Neris", "Oren", "Vey", "Talia"};
        String[] last = {"Ashvein", "Reed", "Vellum", "Thorne", "Sallow", "Hearth", "Rill", "Morrow"};
        long seed = c.getLong("Seed"); t.putString("Name", first[Math.floorMod(seed, first.length)] + " " + last[Math.floorMod(seed >> 8, last.length)]);
        return new SuccessorRecord(t);
    }
    public static void cleanup(ServerLevel level, ActiveCardinalRite rite) {
        if (rite == null || !is(rite)) return;
        var c = rite.succession(); if (c.getBoolean("Cleaned")) return;
        var data = SuccessionSavedData.get(level);
        data.ledger.release(c.getUUID("Transaction"));
        if (c.getBoolean("Restoring") && c.getBoolean("MemoryConsumed") && (!c.getBoolean("Resolved") || !c.getBoolean("Valid"))) {
            var r = data.residents.get(c.getUUID("Identity")); var life = data.ledger.life(c.getUUID("Identity"));
            if (r != null && life != null && !life.alive()) {
                var pos = rite.getCenterPos(); level.addFreshEntity(new ItemEntity(level, pos.getX()+.5, pos.getY()+1, pos.getZ()+.5,
                        BoundMnemonicRemnantItem.create(r, life.generation())));
            }
        }
        if (c.getBoolean("BodyCommitted")) {
            var pos = BlockPos.of(c.getLong("Body"));
            if (level.getBlockState(pos).is(BlockInit.vacant_effigy.get())) level.removeBlock(pos, false);
        }
        c.putBoolean("Cleaned", true); data.setDirty();
    }
    private static boolean fail(ServerPlayer player, String reason) {
        player.displayClientMessage(Component.translatable("hemomancy.succession.failure." + reason), false); return false;
    }
}
