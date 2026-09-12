package com.vincenthuto.hemomancy.client.screen.radial;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.lwjgl.opengl.GL11.*;

class RadialVeinBorderRenderTest {
    private static final int STRIDE = DefaultVertexFormat.POSITION_COLOR.getVertexSize();

    @Test
    void ribbonSectionsShareTheirEdgesInsteadOfLeavingSeams() throws ReflectiveOperationException {
        try (ByteBufferBuilder bytes = new ByteBufferBuilder(16384);
             MeshData mesh = veins(bytes, 6, 2)) {
            ByteBuffer vertices = mesh.vertexBuffer().order(ByteOrder.nativeOrder());
            Map<Edge, Integer> joins = new HashMap<>();
            for (int vertex = 0; vertex < mesh.drawState().vertexCount(); vertex += 4) {
                int offset = vertex * STRIDE;
                if ((vertices.get(offset + 12) & 255) != 255) continue;
                joins.merge(new Edge(point(vertices, offset), point(vertices, offset + 3 * STRIDE)), 1, Integer::sum);
                joins.merge(new Edge(point(vertices, offset + STRIDE), point(vertices, offset + 2 * STRIDE)), 1, Integer::sum);
            }
            long shared = joins.values().stream().filter(count -> count == 2).count();
            assertTrue(shared > joins.size() * .9, "Ribbon edges should be shared at every interior join");
        }
    }

    private record Point(float x, float y) {}
    private record Edge(Point left, Point right) {}

    private static Point point(ByteBuffer vertices, int offset) {
        return new Point(vertices.getFloat(offset), vertices.getFloat(offset + 4));
    }

    @Test
    void innerAndOuterVeinQuadsFaceTheGuiCamera() throws ReflectiveOperationException {
        for (int slices : new int[]{4, 6}) {
            for (int selected = 0; selected < slices; selected++) {
                try (ByteBufferBuilder bytes = new ByteBufferBuilder(16384);
                     MeshData mesh = veins(bytes, slices, selected)) {
                    ByteBuffer vertices = mesh.vertexBuffer().order(ByteOrder.nativeOrder());
                    assertTrue(mesh.drawState().vertexCount() > 0);
                    for (int vertex = 0; vertex < mesh.drawState().vertexCount(); vertex += 4) {
                        int a = vertex * STRIDE, b = a + STRIDE, c = b + STRIDE;
                        float abX = vertices.getFloat(b) - vertices.getFloat(a);
                        float abY = vertices.getFloat(b + 4) - vertices.getFloat(a + 4);
                        float acX = vertices.getFloat(c) - vertices.getFloat(a);
                        float acY = vertices.getFloat(c + 4) - vertices.getFloat(a + 4);
                        // GUI projection flips Y: a negative screen-space area faces the camera.
                        assertTrue(abX * acY - abY * acX < 0,
                                "Back-facing vein quad in " + slices + "-slice ring, slice " + selected);
                    }
                }
            }
        }
    }

    private static MeshData veins(ByteBufferBuilder bytes, int slices, int selected)
            throws ReflectiveOperationException {
        BufferBuilder buffer = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        var draw = GenericRadialMenu.class.getDeclaredMethod("drawVeinSegments", BufferBuilder.class,
                float.class, float.class, float.class, float.class, float.class, float.class, float.class,
                int.class, double.class);
        draw.setAccessible(true);
        float start = (float)(((selected - .5) / slices + .25) * Math.PI * 2 + Math.PI);
        float end = (float)(((selected + .5) / slices + .25) * Math.PI * 2 + Math.PI);
        draw.invoke(null, buffer, 96f, 96f, 1f, start, end,
                slices == 4 ? 18f : 50f, slices == 4 ? 42f : 86f, selected, 12.5);
        return buffer.buildOrThrow();
    }

    // Optional GPU check: gradlew -I src/test/radial-vein-review.init.gradle radialVeinRenderReview
    public static void main(String[] args) throws Exception {
        if (args.length == 0) return;
        Path output = Path.of(args[0]);
        Files.createDirectories(output);
        assertTrue(GLFW.glfwInit(), "GLFW must initialize for the GPU review");
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        long window = GLFW.glfwCreateWindow(768, 768, "Radial vein render check", 0, 0);
        try {
            assertTrue(window != 0, "Hidden OpenGL context must be created");
            GLFW.glfwMakeContextCurrent(window);
            GL.createCapabilities();
            glViewport(0, 0, 768, 768);
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, 192, 192, 0, -100, 100);
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
            glDisable(GL_DEPTH_TEST);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glFrontFace(GL_CCW);
            glCullFace(GL_BACK);
            for (int slices : new int[]{4, 6}) {
                try (ByteBufferBuilder bytes = new ByteBufferBuilder(16384);
                     MeshData mesh = veins(bytes, slices, 2)) {
                    int reference = render(mesh, false, output.resolve(slices + "-slice-unculled.png"));
                    int visible = render(mesh, true, output.resolve(slices + "-slice-culled.png"));
                    System.out.println(slices + "-slice ring: " + visible + " visible red pixels with culling; "
                            + reference + " without culling");
                    assertTrue(reference > 1000, "The vein geometry must cover visible pixels");
                    assertEquals(reference, visible, "Back-face culling must not discard the selected border");
                }
            }
            assertEquals(GL_NO_ERROR, glGetError());
        } finally {
            if (window != 0) GLFW.glfwDestroyWindow(window);
            GLFW.glfwTerminate();
        }
    }

    private static int render(MeshData mesh, boolean cull, Path output) throws Exception {
        if (cull) glEnable(GL_CULL_FACE); else glDisable(GL_CULL_FACE);
        glClearColor(.18f, .18f, .18f, 1);
        glClear(GL_COLOR_BUFFER_BIT);
        ByteBuffer vertices = mesh.vertexBuffer().order(ByteOrder.nativeOrder());
        glBegin(GL_QUADS);
        for (int i = 0; i < mesh.drawState().vertexCount(); i++) {
            int offset = i * STRIDE;
            glColor4ub(vertices.get(offset + 12), vertices.get(offset + 13),
                    vertices.get(offset + 14), vertices.get(offset + 15));
            glVertex3f(vertices.getFloat(offset), vertices.getFloat(offset + 4), vertices.getFloat(offset + 8));
        }
        glEnd();
        ByteBuffer pixels = MemoryUtil.memAlloc(768 * 768 * 4);
        try {
            glReadPixels(0, 0, 768, 768, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
            BufferedImage image = new BufferedImage(768, 768, BufferedImage.TYPE_INT_ARGB);
            int visible = 0;
            for (int y = 0; y < 768; y++) {
                for (int x = 0; x < 768; x++) {
                    int offset = (y * 768 + x) * 4;
                    int r = pixels.get(offset) & 255, g = pixels.get(offset + 1) & 255;
                    int b = pixels.get(offset + 2) & 255;
                    image.setRGB(x, 767 - y, 0xff000000 | r << 16 | g << 8 | b);
                    if (r > g + 30) visible++;
                }
            }
            ImageIO.write(image, "png", output.toFile());
            return visible;
        } finally {
            MemoryUtil.memFree(pixels);
        }
    }
}
