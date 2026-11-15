#version 150

#moj_import <fog.glsl>

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float Time;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;

out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), u.x),
               mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x), u.y);
}

void main() {
    float distortion = noise(texCoord0 * 9.0 + vec2(Time * 0.08, -Time * 0.12));
    float rad = distance(texCoord0, vec2(0.5, 0.5)) * 6.0 + distortion * 0.35;
    float t1 = mod(Time, 6.0);
    float t2 = mod(Time + 2.0, 6.0);
    float t3 = mod(Time + 4.0, 6.0);
    float band = min(min(abs(rad - t1), abs(rad - t2)), abs(rad - t3));
    float wave = 1.0 - smoothstep(0.025, 0.22, band);
    float alpha = 0.055 + wave * 0.55 + distortion * 0.035;
    vec4 color = vec4(vec3(1.0, 0.045, 0.02) * (0.7 + wave * 0.65), alpha);
    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
