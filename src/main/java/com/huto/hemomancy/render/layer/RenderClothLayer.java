package com.huto.hemomancy.render.layer;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.model.entity.armor.ModelLivingBladeHip;
import com.hutoslib.client.ClientUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;

import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.tileentity.BlockEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.IFluidBlock;

public class RenderClothLayer<T extends Player, M extends PlayerModel<T>> extends RenderLayer<T, M> {
	ModelLivingBladeHip model = new ModelLivingBladeHip();

	public RenderClothLayer(RenderLayerParent<T, M> entityRendererIn) {
		super(entityRendererIn);
	}

	@Override
	public void render(PoseStack matrixStack, MultiBufferSource iRenderTypeBuffer, int packedLight, Player player,
			float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch,
			float scale) {
		getCape(player).update(player);
		if (!player.isInvisible() && !player.isFallFlying() && !player.isSleeping()
				&& ClientUtils.getPartialTicks() != 1) {
			RenderCape cape = getCape(player);
			Vector3d locVec = new Vector3d(
					player.getX() - cape.posX - BlockEntityRendererDispatcher.instance.camera.getPosition().x,
					player.getY() - cape.posY - BlockEntityRendererDispatcher.instance.camera.getPosition().y,
					player.getZ() - cape.posZ - BlockEntityRendererDispatcher.instance.camera.getPosition().z);
			cape.render(matrixStack, iRenderTypeBuffer, player, locVec.x, locVec.y, locVec.z,
					ClientUtils.getPartialTicks());
		}

	}

	private static final LoadingCache<Player, RenderCape> capes = CacheBuilder.newBuilder().weakKeys()
			.expireAfterAccess(10, TimeUnit.SECONDS).ticker(new Ticker() {
				@Override
				public long read() {
					return System.nanoTime();
				}
			}).build(CacheLoader.from(RenderCape::create));

	private static RenderCape getCape(Player player) {
		return capes.getUnchecked(player);
	}

	private static final class RenderCape {
		private static final int PLAYER_SKIP_RANGE = 4 * 4;

		private static final int FLUID_CACHE_CLEAR_RATE = 4;

		private static final int FLUID_CACHE_CLEAR_SIZE = 6;

		private static final int ITERATIONS = 11;

		private static final float DELTA_TIME = 0.0052F;

		private static final float GRAVITY = -2800;

		private static final float FLUID_FORCE = -100;

		private static final int HEIGHT = 8;

		private static final int WIDTH = 4;

		private final ImmutableList<Point> points;

		private final ImmutableList<Quad> quads;

		private final Long2BooleanMap fluidCache = new Long2BooleanOpenHashMap(10);

		private final BlockPos.Mutable scratchPos = new BlockPos.Mutable();

		private double posX;

		private double posY;

		private double posZ;

		private RenderCape(ImmutableList<Point> points, ImmutableList<Quad> quads) {
			this.points = points;
			this.quads = quads;
		}

		private static RenderCape create() {
			return create(WIDTH, HEIGHT);
		}

		private static RenderCape create(int width, int height) {
			List<Point> points = Lists.newArrayList();
			ImmutableList.Builder<Quad> quads = ImmutableList.builder();
			float scale = 2 / 16F;
			int columns = width + 1;
			PlayerCollisionResolver collision = new PlayerCollisionResolver();
			for (int y = 0; y <= height; y++) {
				for (int x = 0; x <= width; x++) {
					ImmutableList.Builder<ConstraintResolver> res = ImmutableList.builder();
					float mass = 1;
					if (y == 0 || (y == 1 && (x == 0 || x == width))) {
						mass = 0;
						res.add(new PlayerPinResolver(-(x - width * 0.5F) * scale, -y * scale));
					}
					if (y > 0 || x > 0) {
						float t = (float) y / height;
						mass = 1 - t * 0.1F;
						LinkResolver link = new LinkResolver();
						if (y > 0) {
							link.attach(points.get(x + (y - 1) * columns), scale, 2.0F + (1.95F - 2.0F) * t);
						}
						if (x > 0) {
							link.attach(points.get(points.size() - 1), scale * (1 + y / (float) height * 0.1F), 1);
						}
						res.add(link);
					}
					res.add(collision);
					points.add(new Point(-(x - width * 0.5F) * scale, -y * scale, 0, mass, res.build()));
					if (x > 0 && y > 0) {
						int p00x = x - 1, p00y = y - 1;
						int p01x = x - 1, p01y = y;
						int p11x = x, p10y = y;
						int p10x = x, p11y = y - 1;
						Quad.Vertex v00 = Quad.vert(points.get(p00x + p00y * columns), p00x, p00y, width, height);
						Quad.Vertex v01 = Quad.vert(points.get(p01x + p01y * columns), p01x, p01y, width, height);
						Quad.Vertex v11 = Quad.vert(points.get(p11x + p10y * columns), p11x, p10y, width, height);
						Quad.Vertex v10 = Quad.vert(points.get(p10x + p11y * columns), p10x, p11y, width, height);
						quads.add(new Quad(v00, v01, v11, v10));
					}
				}
			}
			points.sort((a, b) -> Double.compare(Mth.sqrt(b.posX * b.posX + b.posY + b.posY),
					Mth.sqrt(a.posX * a.posX + a.posY + a.posY)));
			return new RenderCape(ImmutableList.copyOf(points), quads.build());
		}

		private boolean isPresent(Player player) {
			return true;
		}

		private void update(Player player) {
			if (isPresent(player)) {
				updatePlayerPos(player);
				updatePoints(player);
				updateFluidCache(player);
			}
		}

		private void updatePlayerPos(Player player) {
			double dx = player.getX() - posX;
			double dy = player.getY() - posY;
			double dz = player.getX() - posZ;
			double dist = dx * dx + dy * dy + dz * dz;
			if (dist > PLAYER_SKIP_RANGE) {
				for (Point point : points) {
					point.posX += dx;
					point.posY += dy;
					point.posZ += dz;
					point.prevPosX += dx;
					point.prevPosY += dy;
					point.prevPosZ += dz;
				}
			}
			posX = player.getX();
			posY = player.getY();
			posZ = player.getX();
		}

		private void updatePoints(Player player) {
			for (Point point : points) {
				point.update(player.level, this, DELTA_TIME);
			}
			for (int i = 0; i < ITERATIONS; i++) {
				for (int j = points.size(); j-- > 0;) {
					points.get(j).resolveConstraints(player);
				}
			}
		}

		private void updateFluidCache(Player player) {
			if (player.tickCount % FLUID_CACHE_CLEAR_RATE == 0 && fluidCache.size() > FLUID_CACHE_CLEAR_SIZE) {
				fluidCache.clear();
			}
		}

		private boolean isFluid(Level world, float x, float y, float z) {
			long key = scratchPos.set(x, y, z).asLong();
			if (fluidCache.containsKey(key)) {
				return fluidCache.get(key);
			}
			boolean isFluid = isFluid(world.getBlockState(scratchPos));
			fluidCache.put(key, isFluid);
			return isFluid;
		}

		private boolean isFluid(BlockState state) {
			return state.getBlock() instanceof IFluidBlock;
		}

		void render(MatrixStack matrixStack, IRenderTypeBuffer buffers, Player player, double x, double y,
				double z, float delta) {

			matrixStack.pushPose();
			Tessellator tessellator = Tessellator.getInstance();

			BufferBuilder bufferbuilder = tessellator.getBuilder();
			RenderSystem.depthMask(false);
			RenderSystem.clearDepth(4);
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
			ResourceLocation fallBackCape = new ResourceLocation(Hemomancy.MOD_ID,
					"textures/item/crimson_item_glint.png");
			Minecraft.getInstance().getTextureManager().bindForSetup(fallBackCape);
			Matrix4f matrix = matrixStack.last().pose();
			matrixStack.translate((float) x - player.getLookAngle().x, (float) y + 3 - player.getLookAngle().y,
					(float) z - player.getLookAngle().z);

			for (Quad quad : quads) {

				float a = (float) (0xFF00FF >> 24 & 255) / 255.0F;
				float r = (float) (0xFF00FF >> 16 & 255) / 255.0F;
				float g = (float) (0xFF00FF >> 8 & 255) / 255.0F;
				float b = (float) (0xFF00FF & 255) / 255.0F;
				Quad.Vertex v00 = quad.getV00();
				Quad.Vertex v01 = quad.getV01();
				Quad.Vertex v11 = quad.getV11();
				Quad.Vertex v10 = quad.getV10();
				float v00x = v00.getX(delta);
				float v00y = v00.getY(delta);
				float v00z = v00.getZ(delta);
				float v01x = v01.getX(delta);
				float v01y = v01.getY(delta);
				float v01z = v01.getZ(delta);
				float v11x = v11.getX(delta);
				float v11y = v11.getY(delta);
				float v11z = v11.getZ(delta);
				float v10x = v10.getX(delta);
				float v10y = v10.getY(delta);
				float v10z = v10.getZ(delta);
				float nx = (v11y - v00y) * (v10z - v01z) - (v11z - v00z) * (v10y - v01y);
				float ny = (v10x - v01x) * (v11z - v00z) - (v10z - v01z) * (v11x - v00x);
				float nz = (v11x - v00x) * (v10y - v01y) - (v11y - v00y) * (v10x - v01x);
				float len = Mth.sqrt(nx * nx + ny * ny + nz * nz);
				nx /= len;
				ny /= len;
				nz /= len;
				bufferbuilder.vertex(matrix, v00x, v00y, v00z).color(r, g, b, a)
						.uv((float) v00.getU(), (float) v00.getV()).endVertex();
				bufferbuilder.vertex(matrix, v01x, v01y, v01z).color(r, g, b, a)
						.uv((float) v01.getU(), (float) (float) v01.getV()).endVertex();
				bufferbuilder.vertex(matrix, v11x, v11y, v11z).color(r, g, b, a)
						.uv((float) v11.getU(), (float) v11.getV()).endVertex();
				bufferbuilder.vertex(matrix, v10x, v10y, v10z).color(r, g, b, a)
						.uv((float) v10.getU(), (float) v10.getV()).endVertex();
			}

			tessellator.end();
			RenderSystem.depthMask(true);

			matrixStack.popPose();

		}

		private interface ConstraintResolver {
			void resolve(Player player, Point point);
		}

		private static final class Quad {
			private final Vertex v00;

			private final Vertex v01;

			private final Vertex v11;

			private final Vertex v10;

			private Quad(Vertex v00, Vertex v01, Vertex v11, Vertex v10) {
				this.v00 = v00;
				this.v01 = v01;
				this.v11 = v11;
				this.v10 = v10;
			}

			private static Vertex vert(Point point, int u, int v, int width, int height) {
				return new Vertex(point, u / (float) width, v / (float) height);
			}

			private Vertex getV00() {
				return v00;
			}

			private Vertex getV01() {
				return v01;
			}

			private Vertex getV11() {
				return v11;
			}

			private Vertex getV10() {
				return v10;
			}

			private static final class Vertex {
				private final Point point;

				private final double u;

				private final double v;

				private Vertex(Point point, double u, double v) {
					this.point = point;
					this.u = u;
					this.v = v;
				}

				private float getX(float delta) {
					return point.prevPosX + (point.posX - point.prevPosX) * delta;
				}

				private float getY(float delta) {
					return point.prevPosY + (point.posY - point.prevPosY) * delta;
				}

				private float getZ(float delta) {
					return point.prevPosZ + (point.posZ - point.prevPosZ) * delta;
				}

				private double getU() {
					return u;
				}

				private double getV() {
					return v;
				}
			}
		}

		private static final class Point {
			private final ImmutableList<ConstraintResolver> constraintResolvers;
			private float prevPosX;
			private float prevPosY;
			private float prevPosZ;
			private float posX;
			private float posY;
			private float posZ;
			private float motionX;
			private float motionY;
			private float motionZ;
			private float invMass;

			private Point(float posX, float posY, float posZ, float invMass,
					ImmutableList<ConstraintResolver> constraintResolvers) {
				this.posX = posX;
				this.posY = posY;
				this.posZ = posZ;
				this.invMass = invMass;
				this.constraintResolvers = constraintResolvers;
			}

			private void applyForce(float x, float y, float z) {
				motionX += x;
				motionY += y;
				motionZ += z;
			}

			private void update(Level world, RenderCape cape, float delta) {
				applyForce(0, cape.isFluid(world, posX, posY, posZ) ? FLUID_FORCE : GRAVITY, 0);
				float x = posX + (posX - prevPosX) * 0.75F + motionX * 0.5F * (delta * delta);
				float y = posY + (posY - prevPosY) * 0.75F + motionY * 0.5F * (delta * delta);
				float z = posZ + (posZ - prevPosZ) * 0.75F + motionZ * 0.5F * (delta * delta);
				prevPosX = posX;
				prevPosY = posY;
				prevPosZ = posZ;
				posX = x;
				posY = y;
				posZ = z;
				motionX = motionY = motionZ = 0;
			}

			private void resolveConstraints(Player player) {
				for (ConstraintResolver r : constraintResolvers) {
					r.resolve(player, this);
				}
			}
		}

		private static abstract class PlayerResolver implements ConstraintResolver {
			final float getBack(Player player, float offset) {
				if (player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
					return offset;
				}
				return offset + 0.075F;
			}
		}

		private static final class PlayerPinResolver extends PlayerResolver {
			private final float x;

			private final float y;

			private PlayerPinResolver(float x, float y) {
				this.x = x;
				this.y = y;
			}

			@Override
			public void resolve(Player player, Point point) {
				float yaw = (float) Math.toRadians(player.yBodyRot);
				float height;
				float back;
				if (player.isShiftKeyDown()) {
					height = 1.15F;
					back = getBack(player, 0.135F);
				} else {
					height = 1.38F;
					back = getBack(player, 0.14F);
				}
				float vx = Mth.cos(yaw) * x + Mth.cos(yaw - (float) Math.PI / 2) * back;
				float vz = Mth.sin(yaw) * x + Mth.sin(yaw - (float) Math.PI / 2) * back;
				point.posX = (float) player.getX() + vx;
				point.posY = (float) player.getY() + height + y;
				point.posZ = (float) player.getX() + vz;
			}
		}

		private static final class LinkResolver implements ConstraintResolver {
			private final List<Link> constraints = Lists.newArrayList();

			private LinkResolver attach(Point point, float length, float strength) {
				constraints.add(new Link(point, length, strength));
				return this;
			}

			@Override
			public void resolve(Player player, Point point) {
				for (int i = constraints.size(); i-- > 0;) {
					constraints.get(i).resolve(point);
				}
			}

			private static final class Link {
				private static final float EPSILON = 1e-6F;

				private final Point dest;

				private final float length;

				private final float strength;

				private Link(Point dest, float length, float strength) {
					this.dest = dest;
					this.length = length;
					this.strength = strength;
				}

				private void resolve(Point point) {
					float dx = point.posX - dest.posX;
					float dy = point.posY - dest.posY;
					float dz = point.posZ - dest.posZ;
					float dist = Mth.sqrt(dx * dx + dy * dy + dz * dz);
					float d = dist * (point.invMass + dest.invMass);
					float diff = d < EPSILON ? length / 2 : (dist - length) / d;
					float px = dx * diff * strength;
					float py = dy * diff * strength;
					float pz = dz * diff * strength;
					point.posX -= px * point.invMass;
					point.posY -= py * point.invMass;
					point.posZ -= pz * point.invMass;
					dest.posX += px * dest.invMass;
					dest.posY += py * dest.invMass;
					dest.posZ += pz * dest.invMass;
				}
			}
		}

		private static final class PlayerCollisionResolver extends PlayerResolver {
			@Override
			public void resolve(Player player, Point point) {
				float yaw = (float) (Math.toRadians(player.yBodyRot) - Math.PI / 2);
				float dx = Mth.cos(yaw);
				float dz = Mth.sin(yaw);
				float px = (float) player.getX();
				float py = (float) player.getY() + 0.56F;
				float pz = (float) player.getX();
				if (player.isShiftKeyDown()) {
					float dist = getBack(player, 0.45F);
					float backX = px + dx * dist;
					float backZ = pz + dz * dist;
					float dy = 0.52F;
					float len = Mth.sqrt(1 + dy * dy);
					dx /= len;
					dy /= len;
					dz /= len;
					float rz = (point.posX - (float) player.getX()) * dx + (point.posZ - (float) player.getX()) * dz;
					float a = (Mth.clamp(-rz, -0.5F, -0.4F) + 0.5F) / 0.1F;
					collideWithPlane(point, backX, py, backZ, dx, dy, dz, a);
				} else {
					float rx = (point.posX - (float) player.getX()) * Mth.cos(yaw + (float) Math.PI / 2)
							+ (point.posZ - (float) player.getX()) * Mth.sin(yaw + (float) Math.PI / 2);
					float a = 1 - (Mth.clamp(Math.abs(rx), 0.24F, 0.36F) - 0.24F) / (0.36F - 0.24F);
					float dist = getBack(player, 0.14F);
					collideWithPlane(point, px + dx * dist, py, pz + dz * dist, dx, 0, dz, a);
				}
			}

			private float getDistToPlane(Point point, float px, float py, float pz, float nx, float ny, float nz) {
				return nx * (point.posX - px) + ny * (point.posY - py) + nz * (point.posZ - pz);
			}

			private void collideWithPlane(Point point, float px, float py, float pz, float nx, float ny, float nz,
					float amount) {
				float d = getDistToPlane(point, px, py, pz, nx, ny, nz);
				if (d < 0) {
					point.posX -= nx * d * amount;
					point.posY -= ny * d * amount;
					point.posZ -= nz * d * amount;
				}
			}
		}
	}
}