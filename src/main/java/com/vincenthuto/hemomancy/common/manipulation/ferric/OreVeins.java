package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.manipulation.verb.CrawlSession;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

import java.util.List;
import java.util.function.Predicate;

/**
 * The one ore-vein crawl (core-loop Track 8): Lode Draw's held Projection and the Talaromyces scar's sneaking break
 * both follow a lode with it, through {@link CrawlSession}, and break what they reached through
 * {@code PlayerBlockEdits.breakBlock}. A lode is every connected block sharing one of the origin's
 * {@code c:ores/<kind>} tags (so iron ore and deepslate iron ore are one lode), or the origin's own block when it has
 * no such tag. The crawl claims only what the given tool or tier can harvest.
 */
public final class OreVeins {
	/** How far from its origin a crawl may run. */
	public static final int RADIUS = 8;
	/** The most blocks one crawl takes. */
	public static final int MAX_BLOCKS = 64;

	private OreVeins() {
	}

	/** Whether {@code state} is an ore the crawl follows. */
	public static boolean isOre(BlockState state) {
		return state.is(Tags.Blocks.ORES);
	}

	/** The {@code c:ores/<kind>} tags of {@code origin}: what makes a block part of its lode. */
	public static List<TagKey<Block>> lodeTags(BlockState origin) {
		return origin.getTags()
				.filter(tag -> tag.location().getNamespace().equals("c") && tag.location().getPath().startsWith("ores/"))
				.toList();
	}

	/** Whether {@code candidate} belongs to the lode {@code origin} starts. */
	public static boolean sameLode(BlockState origin, List<TagKey<Block>> lodeTags, BlockState candidate) {
		if (candidate.isAir()) return false;
		if (lodeTags.isEmpty()) return candidate.getBlock() == origin.getBlock();
		for (TagKey<Block> tag : lodeTags) {
			if (candidate.is(tag)) return true;
		}
		return false;
	}

	/**
	 * A crawl from {@code origin} through its lode, claiming only blocks {@code harvestable} accepts. The origin is
	 * claimed at once; {@link CrawlSession#advance} reaches the rest.
	 */
	public static CrawlSession crawl(Level level, BlockPos origin, Predicate<BlockState> harvestable) {
		BlockState start = level.getBlockState(origin);
		List<TagKey<Block>> tags = lodeTags(start);
		return new CrawlSession(level.dimension(), origin, RADIUS, MAX_BLOCKS, new CrawlSession.Selector() {
			@Override
			public boolean selectable(Level at, BlockPos pos) {
				BlockState state = at.getBlockState(pos);
				return sameLode(start, tags, state) && harvestable.test(state);
			}
		});
	}

	/**
	 * Breaks what {@code crawl} reached as {@code player} with {@code tool} (whose enchantments, if any, decide the
	 * drops), leaving {@code except} to the ordinary break that started it. While it runs the player's vein start is
	 * set, so the Talaromyces listener does not open a second vein from these breaks.
	 */
	public static int mine(ServerLevel level, ServerPlayer player, CrawlSession crawl, ItemStack tool, BlockPos except,
			CrawlSession.Broken onBroken) {
		return mine(level, player, crawl, tool, except, onBroken, null);
	}

	/** {@link #mine}, asking {@code gate} before each break (the Talaromyces vein's price per ore). */
	public static int mine(ServerLevel level, ServerPlayer player, CrawlSession crawl, ItemStack tool, BlockPos except,
			CrawlSession.Broken onBroken, CrawlSession.Gate gate) {
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		BlockPos before = known.getLastVeinMineStart();
		known.setLastVeinMineStart(crawl.origin());
		try {
			return crawl.dismantleExcept(level, player, tool, except, onBroken, gate);
		} finally {
			known.setLastVeinMineStart(before == null ? BlockPos.ZERO : before);
		}
	}

	/** Whether a vein is being mined for {@code player} right now; breaks it causes must not start another. */
	public static boolean mining(net.minecraft.world.entity.player.Player player) {
		BlockPos start = HemoCapabilityAccess.getKnownManipulations(player).getLastVeinMineStart();
		return start != null && !BlockPos.ZERO.equals(start);
	}
}
