#version 150
in vec2 coord;

precision highp float;

out vec4 outColor; // output from the fragment shader

uniform sampler2D textureRendered;
uniform vec2 win_size;

uniform float showFilter;
uniform float timeFilter;

uniform float window_width;
uniform float window_height;


uniform vec2 uResolution;
uniform float uBloomStrength;
uniform sampler2D textureBloom;

float FXAA_REDUCE_MIN = (1.0/ 128.0);
float FXAA_REDUCE_MUL = (1.0 / 8.0);
float FXAA_SPAN_MAX = 8.0;
vec3 SHADE = vec3(0.299, 0.587, 0.114);


void main() {
	if (showFilter == 0) {
		outColor = texture(textureRendered, coord);
	} else {
//		outColor = vec4(fxaa(textureRendered, gl_FragCoord.st, vec2(window_width, window_height)));

		vec2 windowSize = vec2(window_width, window_height);
		vec2 inverseVP = 1.0 / windowSize.xy;
		vec2 v_rgbNW = (gl_FragCoord.st + vec2(-1.0, -1.0)) * inverseVP;
		vec2 v_rgbNE = (gl_FragCoord.st + vec2(1.0, -1.0)) * inverseVP;
		vec2 v_rgbSW = (gl_FragCoord.st + vec2(-1.0, 1.0)) * inverseVP;
		vec2 v_rgbSE = (gl_FragCoord.st + vec2(1.0, 1.0)) * inverseVP;
		vec2 v_rgbM = vec2(gl_FragCoord.st * inverseVP);

		vec3 rgbNW = texture(textureRendered, v_rgbNW).xyz;
		vec3 rgbNE = texture(textureRendered, v_rgbNE).xyz;
		vec3 rgbSW = texture(textureRendered, v_rgbSW).xyz;
		vec3 rgbSE = texture(textureRendered, v_rgbSE).xyz;
		vec4 texColor = texture(textureRendered, v_rgbM);
		vec3 rgbM  = texColor.xyz;


		float lumaNW = dot(rgbNW, SHADE);
		float lumaNE = dot(rgbNE, SHADE);
		float lumaSW = dot(rgbSW, SHADE);
		float lumaSE = dot(rgbSE, SHADE);
		float lumaM  = dot(rgbM,  SHADE);
		float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));
		float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));

		vec2 dir;
		dir.x = -((lumaNW + lumaNE) - (lumaSW + lumaSE));
		dir.y =  ((lumaNW + lumaSW) - (lumaNE + lumaSE));

		float dirReduce = max((lumaNW + lumaNE + lumaSW + lumaSE) *	(0.25 * FXAA_REDUCE_MUL), FXAA_REDUCE_MIN);

		float rcpDirMin = 1.0 / (min(abs(dir.x), abs(dir.y)) + dirReduce);
		dir = min(vec2(FXAA_SPAN_MAX, FXAA_SPAN_MAX),
		max(vec2(-FXAA_SPAN_MAX, -FXAA_SPAN_MAX),
		dir * rcpDirMin)) * inverseVP;

		vec3 rgbA = 0.5 * (
		texture(textureRendered, gl_FragCoord.st * inverseVP + dir * (1.0 / 3.0 - 0.5)).xyz +
		texture(textureRendered, gl_FragCoord.st * inverseVP + dir * (2.0 / 3.0 - 0.5)).xyz);

		vec3 rgbB = rgbA * 0.5 + 0.25 * (
		texture(textureRendered, gl_FragCoord.st * inverseVP + dir * -0.5).xyz +	texture(textureRendered, gl_FragCoord.st * inverseVP + dir * 0.5).xyz);


		float lumaB = dot(rgbB, SHADE);

		if ((lumaB < lumaMin) || (lumaB > lumaMax)) {
			outColor = vec4(rgbA, texColor.a);
		} else {
			outColor = vec4(rgbB, texColor.a);
		}
	}
}
