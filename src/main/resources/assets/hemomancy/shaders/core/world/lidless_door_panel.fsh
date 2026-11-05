#version 150

// The void seen through a Lidless Door's panel from outside. Each fragment casts the camera's ray through its point
// on the panel and reads the void along it, so the panel is a window with depth: moving past the door moves the
// view behind it. Positions are camera-relative world space, and so is the passage basis.

#moj_import <fog.glsl>

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float DoorSeed;
uniform vec3 PassageRight;
uniform vec3 PassageUp;
uniform vec3 PassageForward;
uniform float Diamond;
uniform float Live;
uniform float Hard;
uniform vec2 ScreenSize;
uniform sampler2D Sampler0;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec3 rayDir;

out vec4 fragColor;

#moj_import <hemomancy:noise/monolith_noise.glsl>
#moj_import <hemomancy:noise/lidless_void.glsl>
#moj_import <hemomancy:noise/vein_hash3.glsl>
#moj_import <hemomancy:noise/vein_hash1.glsl>
#moj_import <hemomancy:noise/vein_noise.glsl>
#moj_import <hemomancy:noise/vein_fbm.glsl>
#moj_import <hemomancy:noise/vein_cellular.glsl>
#moj_import <hemomancy:noise/lidless_theme.glsl>

void main() {
    vec3 rayWorld = normalize(rayDir);
    vec3 ray = normalize(vec3(dot(rayWorld, PassageRight), dot(rayWorld, PassageUp), dot(rayWorld, PassageForward)));
    vec3 color = lidlessVoid(ray, HemoTime, DoorSeed, 0.0, 1.0);
    vec2 edge = min(texCoord0, 1.0 - texCoord0);
    float seamRect = smoothstep(0.03, 0.0, min(edge.x, edge.y));
    float alpha = 1.0;
    float seam = seamRect;
    vec3 seamColor = vec3(0.42, 0.010, 0.008);
    float seamBlend = 0.0;
    vec3 seamBand = vec3(0.0);
    if (Diamond > 0.5) {
        // Past 1, Diamond says how far the void has turned toward the owner's Chamber; vertex red holds their theme.
        float turned = clamp(Diamond - 1.0, 0.0, 1.0);
        float theme = vertexColor.r * 2.0;
        color = mix(color, lidlessThemeSky(color, ray, theme, HemoTime, DoorSeed), turned);
        // A live eye: the Chamber itself, drawn into Sampler0 from a camera standing where this viewer stands, read by
        // screen position so it is a window with parallax. The rim, lids and seam stay the eye's own.
        if (Live > 0.5) color = texture(Sampler0, gl_FragCoord.xy / ScreenSize).rgb;
        // Over a pale sky an added rim would wash out, so a turned rim is laid on instead.
        seamBlend = turned;
        seamBand = lidlessThemeRing(theme);
        // The portal is a third eye turned on its side: its lids close from left and right toward the vertical centre
        // line. Vertex green is how open it is, and vertex blue 1 when it is seated between a broken Monolith's halves,
        // whose side corners it meets (0.4 of the panel's width out, at its centre height) and behind which its lower
        // edges run, so no gap opens between the void and the stone. Standing alone, as at a Chamber's corridor end, it
        // is a whole diamond. The rim crawls: two layers of noise around it, one slow and broad, one quick and fine.
        // The panel is 3.9 wide and 3.54 tall.
        float opening = vertexColor.g;
        bool seated = vertexColor.b > 0.5;
        vec2 c = texCoord0 - 0.5;
        float ax = abs(c.x);
        if (seated && c.y * 3.54 + 1.77 < ax * 3.9 - 0.216) discard;
        float width = 0.4 * max(opening, 0.012);
        float lower = -min(c.y, 0.0) / 0.5;
        if (seated) lower *= 1.0 - opening;
        float ang = atan(c.y, c.x);
        // Nearly shut, the slit also draws down out of the air, until only a seam between the halves is left.
        float upper = 0.5 * max(smoothstep(0.0, 0.1, opening), seated ? 0.02 : 0.6);
        float r = ax / width + max(c.y, 0.0) / upper + lower;
        float broad = lidlessNoise3(vec3(cos(ang) * 1.6, sin(ang) * 1.6, HemoTime * 0.018 + DoorSeed * 3.0)) - 0.5;
        float fine = lidlessNoise3(vec3(cos(ang) * 4.5 + 7.0, sin(ang) * 4.5, HemoTime * 0.05 + DoorSeed)) - 0.5;
        float settle = seated ? 1.0 - smoothstep(0.22, 0.40, ax / max(opening, 0.05)) : 1.0;
        float rim = 1.02 + (0.24 * broad + 0.10 * fine) * settle * smoothstep(0.0, 0.5, opening);
        alpha = 1.0 - smoothstep(rim - 0.06, rim + 0.03, r);
        seam = smoothstep(rim - 0.28, rim - 0.02, r) * alpha;
        // The lids as they close: dark wet flesh drawing in over the void, a thin bright line where they meet it.
        float lid = (1.0 - smoothstep(0.0, 0.8, opening)) * smoothstep(rim - 0.75, rim - 0.1, r);
        color = mix(color, vec3(0.075, 0.006, 0.012), lid * 0.92);
        // Shut, nothing is left: the fissure between the halves is the stone's own.
        alpha *= smoothstep(0.0, 0.08, opening);
        if (alpha < 0.01) discard;
        // The body is drawn hard, writing depth, and the soft fringe over it without: a fringe pixel that wrote depth
        // after blending over the sky would cull the water or glass behind it and leave a sky-coloured halo.
        if (Hard > 0.5) {
            if (alpha < 0.98) discard;
            alpha = 1.0;
        }
    }
    color = mix(color + seamColor * seam, mix(color, seamBand, seam * 0.85), seamBlend);
    vec4 shaded = vec4(color * ColorModulator.rgb, alpha * vertexColor.a * ColorModulator.a);
    fragColor = linear_fog(shaded, vertexDistance, FogStart, FogEnd, FogColor);
}
