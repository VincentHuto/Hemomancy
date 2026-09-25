package com.vincenthuto.hemomancy.common.menu.tile.crafting;

import com.vincenthuto.hemomancy.common.block.harbinger.crafting.EnzymaticScriptoriumBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.enchanting.ScriptoriumAffinities;
import com.vincenthuto.hemomancy.common.enchanting.ScriptoriumBalance;
import com.vincenthuto.hemomancy.common.enchanting.ScriptoriumCurseRules;
import com.vincenthuto.hemomancy.common.enchanting.ScriptoriumProvenance;
import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.EnzymeItem;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.EnzymaticScriptoriumBlockEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.EnchantingTableBlock;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class EnzymaticScriptoriumMenu extends AbstractContainerMenu {
    private static final int[] BLOOD_COST = {100, 200, 300};
    private final EnzymaticScriptoriumBlockEntity station;
    private final Player player;
    private final ContainerData data;
    private final List<EnchantmentInstance>[] packages;
    private final int[] targets = {-1, -1, -1};
    private int mode;
    private String cachedKey = "";

    public EnzymaticScriptoriumMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, (EnzymaticScriptoriumBlockEntity) inventory.player.level().getBlockEntity(buffer.readBlockPos()));
    }

    @SuppressWarnings("unchecked")
    public EnzymaticScriptoriumMenu(int id, Inventory inventory, EnzymaticScriptoriumBlockEntity station) {
        super(ContainerInit.enzymatic_scriptorium.get(), id);
        this.station = station;
        this.player = inventory.player;
        this.data = new SimpleContainerData(47);
        this.packages = (List<EnchantmentInstance>[]) new List<?>[3];
        for (int i = 0; i < 8; i++) {
            final int tendency = i;
            addSlot(new Slot(station, i, 12 + (i % 4) * 23, 28 + (i / 4) * 25) {
                @Override public boolean mayPlace(ItemStack stack) {
                    return !station.isRiteLocked() && stack.getItem() instanceof EnzymeItem enzyme
                            && enzyme.getTend() == EnumBloodTendency.values()[tendency];
                }
                @Override public boolean mayPickup(Player player) { return !station.isRiteLocked(); }
                @Override public int getMaxStackSize() { return 64; }
            });
        }
        addSlot(new Slot(station, EnzymaticScriptoriumBlockEntity.ITEM, 113, 29) {
            @Override public boolean mayPlace(ItemStack stack) { return !station.isRiteLocked() && !stack.is(Items.BOOK)
                    && (stack.isEnchantable() || stack.has(DataComponentInit.SCRIPTORIUM_PROVENANCE.get())); }
            @Override public boolean mayPickup(Player player) { return !station.isRiteLocked(); }
        });
        addSlot(new Slot(station, EnzymaticScriptoriumBlockEntity.LAPIS, 138, 29) {
            @Override public boolean mayPlace(ItemStack stack) { return !station.isRiteLocked() && stack.is(Items.LAPIS_LAZULI); }
            @Override public boolean mayPickup(Player player) { return !station.isRiteLocked(); }
        });
        addSlot(new Slot(station, EnzymaticScriptoriumBlockEntity.SHARD, 163, 29) {
            @Override public boolean mayPlace(ItemStack stack) { return !station.isRiteLocked() && stack.is(ItemInit.blood_crystal_shard.get()); }
            @Override public boolean mayPickup(Player player) { return !station.isRiteLocked(); }
        });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(inventory, col + row * 9 + 9, 27 + col * 18, 132 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 27 + col * 18, 190));
        addDataSlots(data);
        if (!player.level().isClientSide) refresh();
    }

    public double bloodVolume() { return station.getBloodVolume(); }
    public double bloodCapacity() { return station.getMaxBloodVolume(); }

    public int offerCost(int tier) { return data.get(tier); }
    public int selected(int tube) { return data.get(3 + tube); }
    public int mode() { return data.get(11); }
    public int targetId(int tier) { return data.get(12 + tier); }
    public int tier() { return data.get(15); }
    public int clueId(int tier) { return data.get(16 + tier); }
    public int clueLevel(int tier) { return data.get(19 + tier); }
    public int playerDegree() { return data.get(22); }

    public int enzymeCost(int offer, int tendency) { return data.get(23 + offer * 8 + tendency); }

    public boolean hasOfferEnzymes(int offer) {
        for (int i = 0; i < 8; i++)
            if (station.getItem(i).getCount() < enzymeCost(offer, i)) return false;
        return true;
    }

    private int stationTier() {
        return station.getBlockState().getBlock() instanceof EnzymaticScriptoriumBlock block ? EnzymaticScriptoriumBlock.tier(station.getBlockState()) : 3;
    }

    private int bookshelfPower() {
        return com.vincenthuto.hemomancy.common.enchanting.ScriptoriumPower.power(player.level(), station.getBlockPos());
    }

    private String offerKey() {
        ItemStack input = station.getItem(8);
        StringBuilder key = new StringBuilder(input.toString()).append('/').append(input.getComponents().hashCode())
                .append('/').append(player.getEnchantmentSeed());
        key.append('/').append(bookshelfPower()).append('/').append(mode);
        for (int i = 0; i < 8; i++) key.append('/').append(station.selected(i)).append(':').append(station.getItem(i).getCount());
        return key.toString();
    }

    private void refresh() {
        if (player.level().isClientSide) return;
        for (int i = 0; i < 8; i++) data.set(3 + i, station.selected(i));
        data.set(11, mode);
        data.set(15, stationTier());
        data.set(22, HemoCapabilityAccess.getPlayerDegreeNumber(player));
        String nextKey = offerKey();
        if (nextKey.equals(cachedKey)) return;
        cachedKey = nextKey;
        ItemStack input = station.getItem(8);
        int power = bookshelfPower();
        RandomSource random = RandomSource.create(player.getEnchantmentSeed());
        var registry = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        int[] costs = new int[3];
        for (int tier = 0; tier < 3; tier++) costs[tier] = input.isEmpty() ? 0
                : EnchantmentHelper.getEnchantmentCost(random, tier, power, input);
        for (int tier = 0; tier < 3; tier++) {
            for (int i = 0; i < 8; i++) data.set(23 + tier * 8 + i, 0);
            packages[tier] = List.of();
            targets[tier] = -1;
            data.set(tier, 0);
            data.set(12 + tier, -1);
            data.set(16 + tier, -1);
            data.set(19 + tier, 0);
            if (input.isEmpty() || station.isRiteLocked() || HemoCapabilityAccess.getPlayerDegreeNumber(player) < 3) continue;
            int cost = net.neoforged.neoforge.event.EventHooks.onEnchantmentLevelSet(
                    player.level(), station.getBlockPos(), tier, power, input,
                    costs[tier] < tier + 1 ? 0 : costs[tier]);
            if (cost < tier + 1) continue;
            random.setSeed((long) player.getEnchantmentSeed() + tier);
            List<EnchantmentInstance> result = roll(random, input, cost, registry.getTagOrEmpty(EnchantmentTags.IN_ENCHANTING_TABLE));
            if (result.isEmpty()) continue;
            if (mode > 0) {
                if (stationTier() < (mode == 2 ? 7 : 5) || HemoCapabilityAccess.getPlayerDegreeNumber(player) < (mode == 2 ? 7 : 5)) continue;
                int target = supportedTarget(result);
                if (target < 0) continue;
                EnchantmentInstance original = result.get(target);
                result.set(target, new EnchantmentInstance(original.enchantment, original.level + mode));
                targets[tier] = registry.getId(original.enchantment.value());
                data.set(12 + tier, targets[tier]);
            }
            for (int i = 0; i < 8; i++)
                data.set(23 + tier * 8 + i, station.selected(i));
            packages[tier] = List.copyOf(result);
            data.set(tier, cost);
            EnchantmentInstance clue = result.get(random.nextInt(result.size()));
            data.set(16 + tier, registry.getId(clue.enchantment.value()));
            data.set(19 + tier, clue.level);
        }
        broadcastChanges();
    }

    private List<EnchantmentInstance> roll(RandomSource random, ItemStack input, int cost, Iterable<Holder<Enchantment>> tablePool) {
        int enchantability = input.getEnchantmentValue();
        if (enchantability <= 0) return List.of();
        cost += 1 + random.nextInt(enchantability / 4 + 1) + random.nextInt(enchantability / 4 + 1);
        float variation = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
        cost = Math.clamp(Math.round(cost + cost * variation), 1, Integer.MAX_VALUE);
        List<Holder<Enchantment>> holders = new ArrayList<>();
        tablePool.forEach(holder -> {
            var affinity = ScriptoriumAffinities.get(holder.unwrapKey().orElseThrow().location());
            if (affinity != null && station.selected(affinity.primary().ordinal()) > 0) holders.add(holder);
        });
        List<EnchantmentInstance> legal = EnchantmentHelper.getAvailableEnchantmentResults(cost, input, holders.stream());
        legal.removeIf(entry -> ScriptoriumBalance.enzymeCost(entry.level, entry.enchantment.value().getMaxLevel())
                > station.selected(ScriptoriumAffinities.get(entry.enchantment.unwrapKey().orElseThrow().location()).primary().ordinal()));
        if (legal.isEmpty()) return List.of();
        List<EnchantmentInstance> result = new ArrayList<>();
        EnchantmentInstance picked = pick(random, legal);
        if (picked == null) return result;
        result.add(picked);
        while (random.nextInt(50) <= cost) {
            EnchantmentHelper.filterCompatibleEnchantments(legal, picked);
            if (legal.isEmpty()) break;
            picked = pick(random, legal);
            if (picked == null) break;
            result.add(picked);
            cost /= 2;
        }
        return result;
    }

    private EnchantmentInstance pick(RandomSource random, List<EnchantmentInstance> legal) {
        if (legal.stream().allMatch(entry -> weight(entry) == entry.enchantment.value().getWeight()))
            return net.minecraft.util.random.WeightedRandom.getRandomItem(random, legal).orElse(null);
        double total = 0;
        for (EnchantmentInstance entry : legal) total += weight(entry);
        if (total <= 0) return null;
        double roll = random.nextDouble() * total;
        for (EnchantmentInstance entry : legal) if ((roll -= weight(entry)) < 0) return entry;
        return legal.getLast();
    }

    private double weight(EnchantmentInstance entry) {
        ResourceLocation id = entry.enchantment.unwrapKey().orElseThrow().location();
        ScriptoriumAffinities.Affinity affinity = ScriptoriumAffinities.get(id);
        if (affinity == null) return entry.enchantment.value().getWeight();
        int primary = 0, secondary = 0;
        for (int i = 0; i < 8; i++) {
            EnumBloodTendency tendency = EnumBloodTendency.values()[i];
            if (tendency == affinity.primary()) primary += station.selected(i);
            if (tendency == affinity.secondary()) secondary += station.selected(i);
        }
        return ScriptoriumBalance.weight(entry.enchantment.value().getWeight(), primary, secondary);
    }

    private int supportedTarget(List<EnchantmentInstance> result) {
        List<String> supported = List.of("efficiency", "fortune", "unbreaking", "sharpness", "smite",
                "bane_of_arthropods", "power", "looting", "protection", "feather_falling");
        return java.util.stream.IntStream.range(0, result.size()).boxed()
                .filter(i -> result.get(i).level == result.get(i).enchantment.value().getMaxLevel())
                .filter(i -> result.get(i).enchantment.unwrapKey().map(key -> key.location().getNamespace().equals("minecraft")
                        && supported.contains(key.location().getPath())).orElse(false))
                .min(Comparator.comparing(i -> result.get(i).enchantment.unwrapKey().orElseThrow().location().toString()))
                .orElse(-1);
    }

    @Override public void slotsChanged(net.minecraft.world.Container container) {
        super.slotsChanged(container);
        refresh();
    }

    @Override public void broadcastChanges() {
        refresh();
        super.broadcastChanges();
    }

    @Override public boolean clickMenuButton(Player actor, int button) {
        if (actor != player || station.isRiteLocked() || !stillValid(actor)) return false;
        if (button >= 0 && button < 8) {
            boolean changed = station.select(button);
            refresh();
            return changed;
        }
        if (button >= 20 && button <= 22) {
            int wanted = button - 20;
            if (wanted == 2 && (stationTier() < 7 || HemoCapabilityAccess.getPlayerDegreeNumber(actor) < 7)) return false;
            if (wanted == 1 && (stationTier() < 5 || HemoCapabilityAccess.getPlayerDegreeNumber(actor) < 5)) return false;
            mode = wanted;
            refresh();
            return true;
        }
        if (button == 11) {
            ItemStack input = station.getItem(EnzymaticScriptoriumBlockEntity.ITEM);
            ScriptoriumProvenance provenance = input.get(DataComponentInit.SCRIPTORIUM_PROVENANCE.get());
            ItemStack shard = station.getItem(EnzymaticScriptoriumBlockEntity.SHARD);
            var blood = station.getBloodCapability();
            if (provenance == null || provenance.curse().isBlank() || shard.isEmpty()
                    || blood == null || blood.getBloodVolume() < 300
                    || HemoCapabilityAccess.getPlayerDegreeNumber(actor) < 3) return false;
            EnchantmentHelper.updateEnchantments(input, mutable ->
                    mutable.removeIf(enchantment -> !enchantment.is(EnchantmentTags.CURSE)));
            input.set(DataComponentInit.SCRIPTORIUM_PROVENANCE.get(), ScriptoriumProvenance.CLEAN);
            blood.drain(300);
            shard.shrink(1);
            station.sync();
            cachedKey = "";
            refresh();
            return true;
        }
        if (button < 8 || button > 10) return false;
        int tier = button - 8;
        refresh();
        int cost = data.get(tier);
        ItemStack input = station.getItem(8);
        ItemStack lapis = station.getItem(9);
        var blood = station.getBloodCapability();
        if (cost <= 0 || packages[tier] == null || packages[tier].isEmpty() || !hasOfferEnzymes(tier)
                || input.isEmpty() || !input.isEnchantable() || lapis.getCount() < tier + 1
                || (!actor.getAbilities().instabuild && actor.experienceLevel < cost)
                || blood == null || blood.getBloodVolume() < BLOOD_COST[tier]) return false;
        List<EnchantmentInstance> result = packages[tier];
        int atCap = 0;
        int excess = 0;
        for (EnchantmentInstance entry : result) {
            input.enchant(entry.enchantment, entry.level);
            int natural = entry.enchantment.value().getMaxLevel();
            if (entry.level >= natural) atCap++;
            excess += Math.max(0, entry.level - natural);
        }
        int usedEnzymes = 0;
        for (int i = 0; i < 8; i++) usedEnzymes += enzymeCost(tier, i);
        int strain = ScriptoriumBalance.totalStrain(usedEnzymes, result.size(), atCap, excess);
        String curse = "";
        int severity = 0;
        if (actor.getRandom().nextInt(100) < strain) {
            curse = ScriptoriumCurseRules.applicableCurse(input);
            severity = curse.isBlank() ? 0 : ScriptoriumCurseRules.severity(excess);
        }
        input.set(DataComponentInit.SCRIPTORIUM_PROVENANCE.get(), new ScriptoriumProvenance(curse, severity, excess));
        blood.drain(BLOOD_COST[tier]);
        lapis.shrink(tier + 1);
        int[] enzymeCosts = new int[8];
        for (int i = 0; i < 8; i++) enzymeCosts[i] = enzymeCost(tier, i);
        station.consumeEnzymes(enzymeCosts);
        actor.onEnchantmentPerformed(input, tier + 1);
        station.sync();
        cachedKey = "";
        refresh();
        return true;
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (station.isRiteLocked()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();
        if (index < 11) {
            if (!moveItemStackTo(original, 11, slots.size(), true)) return ItemStack.EMPTY;
        } else if (original.getItem() instanceof EnzymeItem enzyme) {
            int tube = enzyme.getTend().ordinal();
            if (!moveItemStackTo(original, tube, tube + 1, false)) return ItemStack.EMPTY;
        } else if (original.is(Items.LAPIS_LAZULI)) {
            if (!moveItemStackTo(original, 9, 10, false)) return ItemStack.EMPTY;
        } else if (original.is(ItemInit.blood_crystal_shard.get())) {
            if (!moveItemStackTo(original, 10, 11, false)) return ItemStack.EMPTY;
        } else if ((original.isEnchantable() || original.has(DataComponentInit.SCRIPTORIUM_PROVENANCE.get())) && !original.is(Items.BOOK)) {
            if (!moveItemStackTo(original, 8, 9, false)) return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;
        if (original.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        refresh();
        return copy;
    }

    @Override public boolean stillValid(Player player) { return station.stillValid(player); }
}
