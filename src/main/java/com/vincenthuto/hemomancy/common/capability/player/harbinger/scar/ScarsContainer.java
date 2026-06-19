package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar;

import com.vincenthuto.hemomancy.common.init.ScarInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScar;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ScarsContainer extends ItemStackHandler implements IScars, INBTSerializable<CompoundTag> {
	private static final String TAG_KNOWN = "KnownCerebralScars";
	private static final String TAG_ACTIVE = "ActiveCerebralScars";

	private final Set<ResourceLocation> knownCerebralScars = new LinkedHashSet<>();
	private final Set<ResourceLocation> activeCerebralScars = new LinkedHashSet<>();
	private ItemStack fungalScar = ItemStack.EMPTY;
	private LivingEntity holder;
	private boolean effectsBound;

	public ScarsContainer() {
		super(1);
	}

	public ScarsContainer(LivingEntity holder) {
		this();
		this.holder = holder;
	}

	@Override
	public void bindHolder(LivingEntity holder) {
		if (this.holder != holder && this.holder != null && effectsBound) {
			unbindStoredEffects(this.holder);
		}
		this.holder = holder;
		bindStoredEffects();
	}

	@Override
	public Set<ResourceLocation> getKnownCerebralScars() {
		return Set.copyOf(knownCerebralScars);
	}

	@Override
	public Set<ResourceLocation> getActiveCerebralScars() {
		return Set.copyOf(activeCerebralScars);
	}

	@Override
	public boolean knowsCerebralScar(ResourceLocation scarId) {
		return knownCerebralScars.contains(scarId);
	}

	@Override
	public void addKnownCerebralScar(ResourceLocation scarId) {
		if (resolveCerebral(scarId) != null) {
			knownCerebralScars.add(scarId);
		}
	}

	@Override
	public boolean activateCerebralScar(ResourceLocation scarId) {
		ScarDefinition definition = resolveCerebral(scarId);
		if (definition == null) {
			return false;
		}
		knownCerebralScars.add(scarId);
		if (activeCerebralScars.add(scarId) && holder != null && effectsBound) {
			definition.onEquipped(holder, ItemStack.EMPTY);
		}
		return true;
	}

	@Override
	public boolean deactivateCerebralScar(ResourceLocation scarId) {
		ScarDefinition definition = ScarInit.getByName(scarId.toString());
		if (activeCerebralScars.remove(scarId)) {
			if (definition != null && holder != null && effectsBound) {
				definition.onUnequipped(holder, ItemStack.EMPTY);
			}
			return true;
		}
		return false;
	}

	@Override
	public ItemStack getFungalScar() {
		return getStackInSlot(FUNGAL_SLOT);
	}

	@Override
	public void setFungalScar(ItemStack stack) {
		setStackInSlot(FUNGAL_SLOT, stack);
	}

	@Override
	public void forEachActiveCerebralScar(Consumer<ScarDefinition> consumer) {
		for (ResourceLocation scarId : activeCerebralScars) {
			ScarDefinition definition = ScarInit.getByName(scarId.toString());
			if (definition != null && definition.getScarType() == ScarType.CEREBRAL) {
				consumer.accept(definition);
			}
		}
	}

	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return slot == FUNGAL_SLOT && (stack.isEmpty()
				|| stack.getItem() instanceof IScarItem scarItem
				&& scarItem.getScarDefinition().getScarType() == ScarType.FUNGAL);
	}

	@Override
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		if (!isItemValidForSlot(slot, stack)) {
			return stack;
		}
		return super.insertItem(slot, stack, simulate);
	}

	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		if (slot == FUNGAL_SLOT && !isItemValidForSlot(slot, stack)) {
			return;
		}
		super.setStackInSlot(slot, stack);
	}

	@Override
	protected void onContentsChanged(int slot) {
		if (slot != FUNGAL_SLOT) {
			return;
		}
		ItemStack previous = fungalScar;
		ItemStack current = getStackInSlot(FUNGAL_SLOT);
		if (holder != null && effectsBound && !ItemStack.isSameItemSameComponents(previous, current)) {
			if (previous.getItem() instanceof ItemScar oldScar) {
				oldScar.onUnequipped(holder);
			}
			if (current.getItem() instanceof ItemScar newScar) {
				newScar.onEquipped(holder);
			}
		}
		fungalScar = current.copy();
	}

	@Override
	public void tick() {
		if (holder == null) {
			return;
		}
		forEachActiveCerebralScar(definition -> definition.onWornTick(holder, ItemStack.EMPTY));
		ItemStack fungal = getFungalScar();
		if (fungal.getItem() instanceof ItemScar scar) {
			scar.onWornTick(holder);
		}
	}

	public void loadStateFromSync(Set<ResourceLocation> knownIds, Set<ResourceLocation> activeIds, ItemStack fungalStack) {
		knownCerebralScars.clear();
		activeCerebralScars.clear();
		for (ResourceLocation id : knownIds) {
			if (resolveCerebral(id) != null) {
				knownCerebralScars.add(id);
			}
		}
		for (ResourceLocation id : activeIds) {
			if (knownCerebralScars.contains(id) && resolveCerebral(id) != null) {
				activeCerebralScars.add(id);
			}
		}
		setStackInSlot(FUNGAL_SLOT, fungalStack.copy());
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = super.serializeNBT(provider);
		tag.put(TAG_KNOWN, writeIds(knownCerebralScars));
		tag.put(TAG_ACTIVE, writeIds(activeCerebralScars));
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		super.deserializeNBT(provider, nbt);
		fungalScar = getStackInSlot(FUNGAL_SLOT).copy();
		knownCerebralScars.clear();
		activeCerebralScars.clear();
		readIds(nbt.getList(TAG_KNOWN, Tag.TAG_STRING), knownCerebralScars);
		readIds(nbt.getList(TAG_ACTIVE, Tag.TAG_STRING), activeCerebralScars);
		effectsBound = false;
		if (holder != null) {
			bindStoredEffects();
		}
	}

	private void bindStoredEffects() {
		if (holder == null || effectsBound) {
			return;
		}
		forEachActiveCerebralScar(definition -> definition.onEquipped(holder, ItemStack.EMPTY));
		ItemStack fungal = getFungalScar();
		if (fungal.getItem() instanceof ItemScar scar) {
			scar.onEquipped(holder);
		}
		effectsBound = true;
	}

	private void unbindStoredEffects(LivingEntity previousHolder) {
		forEachActiveCerebralScar(definition -> definition.onUnequipped(previousHolder, ItemStack.EMPTY));
		ItemStack fungal = getFungalScar();
		if (fungal.getItem() instanceof ItemScar scar) {
			scar.onUnequipped(previousHolder);
		}
		effectsBound = false;
	}

	private static ScarDefinition resolveCerebral(ResourceLocation scarId) {
		ScarDefinition definition = ScarInit.getByName(scarId.toString());
		return definition != null && definition.getScarType() == ScarType.CEREBRAL ? definition : null;
	}

	private static ListTag writeIds(Set<ResourceLocation> ids) {
		ListTag list = new ListTag();
		for (ResourceLocation id : ids) {
			list.add(StringTag.valueOf(id.toString()));
		}
		return list;
	}

	private static void readIds(ListTag list, Set<ResourceLocation> ids) {
		for (int i = 0; i < list.size(); i++) {
			ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
			if (id != null) {
				ids.add(id);
			}
		}
	}
}
