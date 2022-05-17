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

float FXAA_REDUCE_MIN = (1.0 / 128.0);
float FXAA_REDUCE_MUL = (1.0 / 8.0);
float FXAA_SCOPE_MAX = 16.0;
vec3 SHADE = vec3(0.299, 0.587, 0.114);

struct v_rgb {
	vec2 NW;
	vec2 NE;
	vec2 SW;
	vec2 SE;
	vec2 M;
};

struct luma {
	float NW;
	float NE;
	float SW;
	float SE;
	float M;
};

void main() {
	if (showFilter == 0) {
		outColor = texture(textureRendered, coord);
	} else {
		vec2 inverseVP = 1.0 / vec2(window_width, window_height).xy;

		v_rgb v_rgb = v_rgb(
			(gl_FragCoord.st + vec2(-1.0, -1.0)) * inverseVP,
			(gl_FragCoord.st + vec2(1.0, -1.0)) * inverseVP,
			(gl_FragCoord.st + vec2(-1.0, 1.0)) * inverseVP,
			(gl_FragCoord.st + vec2(1.0, 1.0)) * inverseVP,
			vec2(gl_FragCoord.st * inverseVP)
		);

		vec4 texColor = texture(textureRendered, v_rgb.M);
		vec3 rgbM  = texColor.xyz;

		luma l = luma(
			dot(texture(textureRendered, v_rgb.NW).xyz, SHADE),
			dot(texture(textureRendered, v_rgb.NE).xyz, SHADE),
			dot(texture(textureRendered, v_rgb.SW).xyz, SHADE),
			dot(texture(textureRendered, v_rgb.SE).xyz, SHADE),
			dot(texture(textureRendered, v_rgb.M).xyz,  SHADE)
		);

		float lumaMin = min(l.M, min(min(l.NW, l.NE), min(l.SW, l.SE)));
		float lumaMax = max(l.M, max(max(l.NW, l.NE), max(l.SW, l.SE)));

		vec2 direction;
		direction.x = -((l.NW + l.NE) - (l.SW + l.SE));
		direction.y =  ((l.NW + l.SW) - (l.NE + l.SE));

		float directionReduce = max((l.NW + l.NE + l.SW + l.SE) *	(0.25 * FXAA_REDUCE_MUL), FXAA_REDUCE_MIN);

		float rcpDirMin = 1.0 / (min(abs(direction.x), abs(direction.y)) + directionReduce);
		direction = min(vec2(FXAA_SCOPE_MAX, FXAA_SCOPE_MAX),
		max(vec2(-FXAA_SCOPE_MAX, -FXAA_SCOPE_MAX),
		direction * rcpDirMin)) * inverseVP;

		vec3 rgbA = 0.5 * (
			texture(textureRendered, gl_FragCoord.st * inverseVP + direction * (1.0 / 3.0 - 0.5)).xyz +
			texture(textureRendered, gl_FragCoord.st * inverseVP + direction * (2.0 / 3.0 - 0.5)).xyz
		);

		vec3 rgbB = rgbA * 0.5 + 0.25 * (
			texture(textureRendered, gl_FragCoord.st * inverseVP + direction * -0.5).xyz +
			texture(textureRendered, gl_FragCoord.st * inverseVP + direction * 0.5).xyz
		);

		float lumaB = dot(rgbB, SHADE);

		if ((lumaB < lumaMin) || (lumaB > lumaMax)) {
			outColor = vec4(rgbA, texColor.a);
		} else {
			outColor = vec4(rgbB, texColor.a);
		}
	}
}