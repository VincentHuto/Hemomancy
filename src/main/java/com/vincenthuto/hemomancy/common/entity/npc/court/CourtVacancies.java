package com.vincenthuto.hemomancy.common.entity.npc.court;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.ProtectedNpc;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtResidentSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Court members lost to something other than a player: removed with {@code /kill} or fallen out of the world (members
 * are protected, so nothing else can take one). Each loss leaves a vacancy on the member's mark, and a new member of the
 * same role is set down there once a day has passed and no player is near, never more than the mark held. Players
 * never cause a vacancy: a Creative player who strikes a member down has removed it on purpose.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CourtVacancies extends SavedData {
	private static final String NAME = "hemomancy_court_vacancies";
	/** How long a mark stands empty before it is filled again: one day. */
	public static final long REFILL_TICKS = 24000L;
	/** A vacancy is only filled while no player is this close to the mark. */
	public static final double CLEAR_RADIUS = 48.0D;
	public static final int CHECK_INTERVAL = 200;

	/** One empty mark: the role that stood on it, where it faced, and when it may be filled. */
	public record Vacancy(CourtRole role, BlockPos home, float yaw, String mark, long due) {
	}

	private final List<Vacancy> vacancies = new ArrayList<>();

	public static CourtVacancies get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(CourtVacancies::new, CourtVacancies::load, null), NAME);
	}

	/** Whether a member lost this way is replaced: {@code /kill} and the void, never a player's hand. */
	public static boolean replaces(DamageSource source) {
		return source.getEntity() == null && (ProtectedNpc.isKillCommand(source) || source.is(DamageTypes.FELL_OUT_OF_WORLD));
	}

	/** Records the member's mark as empty; members that were never set on a mark (spawn eggs) leave nothing. */
	public void open(CourtMemberEntity member) {
		if (member.home() == null || member.mark().isEmpty()) return;
		vacancies.add(new Vacancy(member.role(), member.home(), member.homeYaw(), member.mark(),
				member.level().getGameTime() + REFILL_TICKS));
		setDirty();
	}

	public List<Vacancy> vacancies() {
		return List.copyOf(vacancies);
	}

	/** Fills every due vacancy whose mark is loaded and clear of players; drops one whose mark is already held. */
	public void fill(ServerLevel level, long now) {
		boolean changed = false;
		for (Iterator<Vacancy> it = vacancies.iterator(); it.hasNext(); ) {
			Vacancy vacancy = it.next();
			if (now < vacancy.due() || !level.isLoaded(vacancy.home())) continue;
			BlockPos home = vacancy.home();
			if (level.getNearestPlayer(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D, CLEAR_RADIUS, false) != null) continue;
			boolean held = !level.getEntitiesOfClass(CourtMemberEntity.class, new AABB(home).inflate(CourtMemberEntity.LEASH * 2),
					member -> member.isAlive() && member.role() == vacancy.role() && home.equals(member.home())).isEmpty();
			if (held || CourtResidentSpawner.place(level, vacancy.role(), home, vacancy.yaw(), vacancy.mark())) {
				it.remove();
				changed = true;
			}
		}
		if (changed) setDirty();
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (event.getLevel() instanceof ServerLevel level && level.getGameTime() % CHECK_INTERVAL == 0) {
			CourtVacancies vacancies = level.getDataStorage().get(new SavedData.Factory<>(CourtVacancies::new, CourtVacancies::load, null), NAME);
			if (vacancies != null && !vacancies.vacancies.isEmpty()) vacancies.fill(level, level.getGameTime());
		}
	}

	private static CourtVacancies load(CompoundTag tag, HolderLookup.Provider registries) {
		CourtVacancies data = new CourtVacancies();
		for (Tag element : tag.getList("Vacancies", Tag.TAG_COMPOUND)) {
			CompoundTag entry = (CompoundTag) element;
			CourtRole role = CourtRole.byId(entry.getString("Role"));
			BlockPos home = NbtUtils.readBlockPos(entry, "Home").orElse(null);
			if (role == null || home == null) continue;
			data.vacancies.add(new Vacancy(role, home, entry.getFloat("Yaw"), entry.getString("Mark"), entry.getLong("Due")));
		}
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		ListTag list = new ListTag();
		for (Vacancy vacancy : vacancies) {
			CompoundTag entry = new CompoundTag();
			entry.putString("Role", vacancy.role().id());
			entry.put("Home", NbtUtils.writeBlockPos(vacancy.home()));
			entry.putFloat("Yaw", vacancy.yaw());
			entry.putString("Mark", vacancy.mark());
			entry.putLong("Due", vacancy.due());
			list.add(entry);
		}
		tag.put("Vacancies", list);
		return tag;
	}
}
