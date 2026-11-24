package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class IchorianSigilNodeTendrilGeometryTest {
	private static final long SEED = 0x51A7L;

	@Test
	void aClosedSquareHasFourNodesAndALargePatternIsCapped() {
		assertEquals(4, IchorianSigilNodeTendrilGeometry.nodes(square()).size());

		List<ActiveRiteData.SigilSegment> many = new ArrayList<>();
		for (int index = 0; index < 15; index++) {
			many.add(segment(index * 2.0D, 64.1D, 0.0D, index * 2.0D + 1.0D, 64.1D, 3.0D, index));
		}
		assertEquals(IchorianSigilNodeTendrilGeometry.MAX_NODES, IchorianSigilNodeTendrilGeometry.nodes(many).size(),
				"30 distinct endpoints are picked down to the cap");
	}

	@Test
	void growthsLeanTowardTheFormButStopShortAndTaperAway() {
		List<Vec3> nodes = IchorianSigilNodeTendrilGeometry.nodes(square());
		Vec3 hover = IchorianSigilNodeTendrilGeometry.hoverPoint(nodes);
		List<IchorianSigilNodeTendrilGeometry.Strand> strands =
				IchorianSigilNodeTendrilGeometry.strands(nodes, hover, SEED, 120.0F, 1.0F);

		assertEquals(nodes.size() * IchorianSigilNodeTendrilGeometry.TENDRILS_PER_NODE, strands.size());
		for (int strandIndex = 0; strandIndex < strands.size(); strandIndex++) {
			List<IchorianSigilNodeTendrilGeometry.Joint> joints = strands.get(strandIndex).joints();
			Vec3 node = nodes.get(strandIndex / IchorianSigilNodeTendrilGeometry.TENDRILS_PER_NODE);
			double nodeDistance = node.distanceTo(hover);
			boolean trunk = strandIndex % IchorianSigilNodeTendrilGeometry.TENDRILS_PER_NODE == 0;
			IchorianSigilNodeTendrilGeometry.Joint root = joints.getFirst();
			if (trunk) {
				assertEquals(0.0D, root.center().distanceTo(node), 1.0E-9D, "a trunk is rooted in its node");
				assertEquals(IchorianSigilNodeTendrilGeometry.ROOT_HALF_WIDTH, root.halfWidth(), 1.0E-6F);
			} else {
				assertTrue(root.halfWidth() < IchorianSigilNodeTendrilGeometry.ROOT_HALF_WIDTH,
						"an offshoot is thinner than its trunk's root");
				assertTrue(root.center().y > node.y, "an offshoot leaves the trunk above the ground");
			}
			for (int index = 0; index < joints.size(); index++) {
				IchorianSigilNodeTendrilGeometry.Joint joint = joints.get(index);
				assertTrue(joint.center().distanceTo(hover) >= nodeDistance * 0.4D,
						"a growth must stop short of the living form");
				assertTrue(joint.halfWidth() <= IchorianSigilNodeTendrilGeometry.ROOT_HALF_WIDTH + 1.0E-6F,
						"nothing is wider than a trunk's root");
				if (index > 0) {
					assertTrue(joint.opacity() < joints.get(index - 1).opacity(), "opacity falls toward the tip");
				}
			}
			assertEquals(0.0F, joints.getLast().opacity(), 1.0E-6F, "the tip fades out");
			assertEquals(0.0F, joints.getLast().halfWidth(), 1.0E-6F, "the tip draws out to nothing");
			assertTrue(joints.getLast().center().y > root.center().y, "the growth rises");
		}
	}

	@Test
	void noTwoTrunksAreTheSameLength() {
		List<Vec3> nodes = IchorianSigilNodeTendrilGeometry.nodes(square());
		Vec3 hover = IchorianSigilNodeTendrilGeometry.hoverPoint(nodes);
		List<IchorianSigilNodeTendrilGeometry.Strand> strands =
				IchorianSigilNodeTendrilGeometry.strands(nodes, hover, SEED, 120.0F, 1.0F);
		java.util.Set<Long> heights = new java.util.HashSet<>();
		for (int node = 0; node < nodes.size(); node++) {
			var joints = strands.get(node * IchorianSigilNodeTendrilGeometry.TENDRILS_PER_NODE).joints();
			heights.add(Math.round((joints.getLast().center().y - joints.getFirst().center().y) * 1000.0D));
		}
		assertEquals(nodes.size(), heights.size(), "each node's trunk rises to its own height");
	}

	@Test
	void aRootLiesInEveryLinkThatMeetsANodeAndGathersIntoItsTrunk() {
		List<Vec3> nodes = IchorianSigilNodeTendrilGeometry.nodes(square());
		int blood = 0x2A8FA0;
		List<IchorianSigilNodeTendrilGeometry.Strand> roots =
				IchorianSigilNodeTendrilGeometry.roots(square(), nodes, SEED, 120.0F, 1.0F, blood);

		assertEquals(nodes.size() * 2, roots.size(), "each corner of the square is met by two links");
		for (IchorianSigilNodeTendrilGeometry.Strand root : roots) {
			List<IchorianSigilNodeTendrilGeometry.Joint> joints = root.joints();
			IchorianSigilNodeTendrilGeometry.Joint far = joints.getFirst();
			IchorianSigilNodeTendrilGeometry.Joint foot = joints.getLast();
			assertEquals(0.0F, far.opacity(), 1.0E-6F, "a root comes out of the blood from nothing");
			assertEquals(0.0F, far.halfWidth(), 1.0E-6F);
			assertEquals(IchorianSigilNodeTendrilGeometry.ROOT_HALF_WIDTH, foot.halfWidth(), 1.0E-6F,
					"it is as thick as the trunk's foot where it meets it");
			Vec3 node = nodes.stream().min(java.util.Comparator.comparingDouble(
					candidate -> candidate.distanceTo(foot.center()))).orElseThrow();
			assertTrue(node.distanceTo(foot.center()) < 0.02D, "a root ends under its node's trunk");
			assertTrue(far.center().distanceTo(node) > 0.9D, "and starts well out in the link");
			for (IchorianSigilNodeTendrilGeometry.Joint joint : joints) {
				assertTrue(joint.groundAligned(), "a root lies flat in the gouge");
				assertEquals(blood, joint.tint(), "a root is the blood's own colour");
				// The square's links run along x = 0, x = 4, z = 0 and z = 4; the cut is 0.16 to either side.
				double offLine = Math.min(
						Math.min(Math.abs(joint.center().x), Math.abs(joint.center().x - 4.0D)),
						Math.min(Math.abs(joint.center().z), Math.abs(joint.center().z - 4.0D)));
				assertTrue(offLine < 0.1D, "a root stays inside the cut, strayed " + offLine);
			}
		}
	}

	@Test
	void aGrowthLeavesTheGougeInTheBloodsColourAndTurnsCrimson() {
		List<Vec3> nodes = IchorianSigilNodeTendrilGeometry.nodes(square());
		Vec3 hover = IchorianSigilNodeTendrilGeometry.hoverPoint(nodes);
		int blood = 0x2A8FA0;
		var trunk = IchorianSigilNodeTendrilGeometry.strands(nodes, hover, SEED, 120.0F, 1.0F, blood)
				.getFirst().joints();
		assertEquals(blood, trunk.getFirst().tint(), "the foot is the blood's colour");
		int tip = trunk.getLast().tint();
		assertTrue((tip >> 16 & 255) > 120 && (tip >> 8 & 255) < 20 && (tip & 255) < 30, "the tip is crimson");
	}

	@Test
	void noRiseNoStrands() {
		List<Vec3> nodes = IchorianSigilNodeTendrilGeometry.nodes(square());
		Vec3 hover = IchorianSigilNodeTendrilGeometry.hoverPoint(nodes);
		assertTrue(IchorianSigilNodeTendrilGeometry.strands(nodes, hover, SEED, 50.0F, 0.0F).isEmpty());
	}

	@Test
	void theSameInputsGiveTheSameStrandsAndTimeOnlyWrithesThem() {
		List<Vec3> nodes = IchorianSigilNodeTendrilGeometry.nodes(square());
		Vec3 hover = IchorianSigilNodeTendrilGeometry.hoverPoint(nodes);
		List<IchorianSigilNodeTendrilGeometry.Strand> first =
				IchorianSigilNodeTendrilGeometry.strands(nodes, hover, SEED, 40.0F, 1.0F);
		assertEquals(first, IchorianSigilNodeTendrilGeometry.strands(nodes, hover, SEED, 40.0F, 1.0F));

		List<IchorianSigilNodeTendrilGeometry.Strand> later =
				IchorianSigilNodeTendrilGeometry.strands(nodes, hover, SEED, 57.0F, 1.0F);
		for (int index = 0; index < first.size(); index++) {
			Vec3 tip = first.get(index).joints().getLast().center();
			Vec3 laterTip = later.get(index).joints().getLast().center();
			double moved = tip.distanceTo(laterTip);
			assertTrue(moved > 0.0D && moved < 0.5D, "the tip writhes but does not thrash, moved " + moved);
		}
	}

	private static List<ActiveRiteData.SigilSegment> square() {
		return List.of(
				segment(0.0D, 64.1D, 0.0D, 4.0D, 64.1D, 0.0D, 0),
				segment(4.0D, 64.1D, 0.0D, 4.0D, 64.1D, 4.0D, 1),
				segment(4.0D, 64.1D, 4.0D, 0.0D, 64.1D, 4.0D, 2),
				segment(0.0D, 64.1D, 4.0D, 0.0D, 64.1D, 0.0D, 3));
	}

	private static ActiveRiteData.SigilSegment segment(double startX, double startY, double startZ,
			double endX, double endY, double endZ, int linkIndex) {
		return new ActiveRiteData.SigilSegment(startX, startY, startZ, endX, endY, endZ,
				0xAA1122, "hemomancy:reservoir@0", linkIndex, 4, true);
	}
}
