# Rouge Will Hermit Render Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the current blocky Will client model with a translucent Harbinger Hermit-like silhouette that has no gourd.

**Architecture:** `WillModel` remains typed to `WillEntity`, but its layer definition is replaced with Hermit-derived geometry minus the gourd, rope, and back accessory branch. `WillRenderer` switches to the Harbinger Hermit texture and returns a translucent render type while the model applies a fixed alpha tint.

**Tech Stack:** Java 21, Minecraft NeoForge 1.21.1 client renderer APIs, `EntityModel`, `MobRenderer`, `RenderType.entityTranslucent`.

---

### Task 1: Replace Will Geometry With Hermit-Derived Body

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/will/WillModel.java`

- [ ] **Step 1: Rewrite `WillModel` fields and constructor**

Replace the current `body`/`head` only model structure with the Hermit part tree minus gourd-related fields. Keep the class name, package, `LAYER_LOCATION`, generic entity type, and public constructor signature unchanged.

- [ ] **Step 2: Replace `createBodyLayer()`**

Copy the Harbinger Hermit layer geometry into `WillModel.createBodyLayer()` and remove these branches:

```java
PartDefinition gourd = body.addOrReplaceChild(...);
PartDefinition rope = gourd.addOrReplaceChild(...);
PartDefinition bone = rope.addOrReplaceChild(...);
PartDefinition bone2 = rope.addOrReplaceChild(...);
```

Keep the hood, torso, cloth panels, arms, and legs.

- [ ] **Step 3: Port Hermit animation**

Use the Hermit humanoid animation in `setupAnim(...)`, adjusted for `WillEntity`. Keep head tracking, arm/leg walk motion, and cloth sway. Do not reference `HarbingerHermitEntity` or Hermit farewell state.

- [ ] **Step 4: Apply alpha in `renderToBuffer()`**

Render `whole` with a fixed translucent color:

```java
private static final int WILL_TRANSLUCENT_COLOR = 0x99FFFFFF;

@Override
public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
        int packedColor) {
    whole.render(poseStack, buffer, packedLight, packedOverlay, WILL_TRANSLUCENT_COLOR);
}
```

Expected result: the model renders with the Hermit silhouette and no gourd.

### Task 2: Make Will Renderer Use Hermit Texture And Translucent RenderType

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/entity/mob/will/WillRenderer.java`

- [ ] **Step 1: Change texture constants**

Set both Will origins to the Hermit texture:

```java
private static final ResourceLocation HERMIT_TEXTURE =
        Hemomancy.rloc("textures/entity/harbinger_hermit/harbinger_hermit.png");
```

- [ ] **Step 2: Return the Hermit texture**

Replace origin-based texture selection with:

```java
@Override
public ResourceLocation getTextureLocation(WillEntity entity) {
    return HERMIT_TEXTURE;
}
```

- [ ] **Step 3: Override render type**

Add a render type override:

```java
@Override
protected RenderType getRenderType(WillEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
    return RenderType.entityTranslucent(this.getTextureLocation(entity));
}
```

Import `net.minecraft.client.renderer.RenderType`.

- [ ] **Step 4: Reduce shadow opacity cue**

Set the renderer shadow radius to `0.25F` in the constructor:

```java
super(context, new WillModel(context.bakeLayer(WillModel.LAYER_LOCATION)), 0.25F);
```

Expected result: Wills use the Hermit texture and are drawn through a translucent entity render layer.

### Task 3: Compile Verification

**Files:**
- Verify: Java client model and renderer compile.

- [ ] **Step 1: Run compile**

Run:

```powershell
./gradlew.bat compileJava
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: If compile fails, fix only Will model/renderer errors**

Common expected issues would be missing imports, accidental Hermit entity references, or `ModelPart.render(...)` overload mismatch. Fix those in the touched files only.

- [ ] **Step 3: Optional visual smoke test**

If time permits, run:

```powershell
./gradlew.bat runClient
```

Spawn a Will and confirm the silhouette matches the Harbinger Hermit closely, has no gourd on the back, and is transparent.
