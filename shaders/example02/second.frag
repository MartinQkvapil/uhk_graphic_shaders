#version 150
precision highp float;

out vec4 outColor;

in vec4 coord;
in vec2 coordV2;

uniform sampler2D textureRendered;
uniform float window_width;
uniform float window_height;


vec2 resolution = vec2(window_width, window_height);
vec2 win_size = vec2(window_width, window_height);

uniform bool revertTextureBool;
ivec2 revertTexture = ivec2(revertTextureBool);

uniform float edgeThreshold; /* 0.0 - 1.0*/
uniform float edgeThresholdMin; /* 0.0 - 1.0*/

uniform int stepsAround; /* more than 2 */
uniform float stepsThreshold; /* 0.0 - 1.0*/

uniform float subPixelCaption; /* 0.0 - 1.0 */
uniform float subPixelTrim; /* -1.0 - 1.0 */

uniform float showFilter;
uniform float timeFilter;

vec2 set_uv(float flip_vertical, float flip_horizontal, vec2 res) {
	vec2 uv;
	if(all(equal(vec2(0),res))) {
		uv = coord.st;
	} else if(all(greaterThan(res,coord.st))) {
		uv = coord.st;
	} else {
		uv = res;
	}

	// flip
	float condition_y = step(0.1, flip_vertical);
	uv.y = 1.0 -(uv.y *condition_y +(1.0 -uv.y) *(1.0 -condition_y));

	float condition_x = step(0.1, flip_horizontal);
	uv.x = 1.0 -(uv.x *condition_x +(1.0 -uv.x) *(1.0 -condition_x));

	return uv;
}

vec2 set_uv(ivec2 flip, vec2 res) {
	return set_uv(flip.x,flip.y,res);
}

vec2 set_uv() {
	return set_uv(0,0,vec2(0));
}

float lumaColor(vec3 rgb) {
	return rgb.y * (0.587/0.299) + rgb.x;
}

vec3 fxaa_lerp_3(vec3 a, vec3 b, float amount_ofa) {
	return (vec3(-amount_ofa) * b) + ((a * vec3(amount_ofa)) + b);
}

vec4 fxaa_tex_off(sampler2D tex, vec2 pos, ivec2 off, vec2 rcp_frame) {
	float x = pos.x + float(off.x) * rcp_frame.x;
	float y = pos.y + float(off.y) * rcp_frame.y;
	return texture(tex, vec2(x,y));
}

vec3 fxaa_pixel_shader(sampler2D tex, vec2 pos, vec2 rcp_frame) {
	float FXAA_SUBPIX_TRIM_SCALE = 1.0/(1.0 - subPixelTrim);

	vec3 rgbN = fxaa_tex_off(tex, pos.xy, ivec2( 0,-1), rcp_frame).xyz;
	vec3 rgbW = fxaa_tex_off(tex, pos.xy, ivec2(-1, 0), rcp_frame).xyz;
	vec3 rgbM = fxaa_tex_off(tex, pos.xy, ivec2( 0, 0), rcp_frame).xyz;
	vec3 rgbE = fxaa_tex_off(tex, pos.xy, ivec2( 1, 0), rcp_frame).xyz;
	vec3 rgbS = fxaa_tex_off(tex, pos.xy, ivec2( 0, 1), rcp_frame).xyz;

	float lumaN = lumaColor(rgbN);
	float lumaW = lumaColor(rgbW);
	float lumaM = lumaColor(rgbM);
	float lumaE = lumaColor(rgbE);

	float lumaS = lumaColor(rgbS);

	float range_min = min(lumaM, min(min(lumaN, lumaW), min(lumaS, lumaE)));
	float range_max = max(lumaM, max(max(lumaN, lumaW), max(lumaS, lumaE)));

	float range = range_max - range_min;
	if(range < max(edgeThresholdMin, range_max * edgeThreshold)) {
		return rgbM;
	}

	vec3 rgbL = rgbN + rgbW + rgbM + rgbE + rgbS;

	float lumaL = (lumaN + lumaW + lumaE + lumaS) * 0.25;
	float rangeL = abs(lumaL - lumaM);
	float blendL = max(0.0, (rangeL / range) - subPixelTrim) * FXAA_SUBPIX_TRIM_SCALE;
	blendL = min(subPixelCaption, blendL);

	vec3 rgbNW = fxaa_tex_off(tex, pos.xy, ivec2(-1,-1), rcp_frame).xyz;
	vec3 rgbNE = fxaa_tex_off(tex, pos.xy, ivec2( 1,-1), rcp_frame).xyz;
	vec3 rgbSW = fxaa_tex_off(tex, pos.xy, ivec2(-1, 1), rcp_frame).xyz;
	vec3 rgbSE = fxaa_tex_off(tex, pos.xy, ivec2( 1, 1), rcp_frame).xyz;
	rgbL += (rgbNW + rgbNE + rgbSW + rgbSE);
	rgbL *= vec3(1.0/9.0);

	float lumaNW = lumaColor(rgbNW);
	float lumaNE = lumaColor(rgbNE);
	float lumaSW = lumaColor(rgbSW);
	float lumaSE = lumaColor(rgbSE);

	float edgeVert =
		abs((0.25 * lumaNW) + (-0.5 * lumaN) + (0.25 * lumaNE)) +
		abs((0.50 * lumaW ) + (-1.0 * lumaM) + (0.50 * lumaE )) +
		abs((0.25 * lumaSW) + (-0.5 * lumaS) + (0.25 * lumaSE));
	float edgeHorz =
		abs((0.25 * lumaNW) + (-0.5 * lumaW) + (0.25 * lumaSW)) +
		abs((0.50 * lumaN ) + (-1.0 * lumaM) + (0.50 * lumaS )) +
		abs((0.25 * lumaNE) + (-0.5 * lumaE) + (0.25 * lumaSE));

	bool horz_span = edgeHorz >= edgeVert;
	float length_sign = horz_span ? -rcp_frame.y : -rcp_frame.x;

	if(!horz_span) {
		lumaN = lumaW;
		lumaS = lumaE;
	}

	float gradientN = abs(lumaN - lumaM);
	float gradientS = abs(lumaS - lumaM);
		lumaN = (lumaN + lumaM) * 0.5;
		lumaS = (lumaS + lumaM) * 0.5;

	if (gradientN < gradientS) {
		lumaN = lumaS;
		lumaN = lumaS;
		gradientN = gradientS;
		length_sign *= -1.0;
	}

	vec2 posN;
	posN.x = pos.x + (horz_span ? 0.0 : length_sign * 0.5);
	posN.y = pos.y + (horz_span ? length_sign * 0.5 : 0.0);

	gradientN *= stepsThreshold;

	vec2 posP = posN;
	vec2 offNP = horz_span ? vec2(rcp_frame.x, 0.0) : vec2(0.0, rcp_frame.y);
	float luma_end_N = lumaN;
	float luma_end_P = lumaN;
	bool doneN = false;
	bool doneP = false;
	posN += offNP * vec2(-1.0, -1.0);
	posP += offNP * vec2( 1.0,  1.0);

	for(float i = 0; i < stepsAround; i++) {
		if(!doneN) {
			luma_end_N = lumaColor(texture2D(tex, posN.xy).xyz);
		}
		if(!doneP) {
			luma_end_P = lumaColor(texture2D(tex, posP.xy).xyz);
		}

		doneN = doneN || (abs(luma_end_N - lumaN) >= gradientN);
		doneP = doneP || (abs(luma_end_P - lumaN) >= gradientN);

		if(doneN && doneP) {
			break;
		}
		if(!doneN) {
			posN -= offNP;
		}
		if(!doneP) {
			posP += offNP;
		}
	}

	float dstN = horz_span ? pos.x - posN.x : pos.y - posN.y;
	float dstP = horz_span ? posP.x - pos.x : posP.y - pos.y;
	bool directionN = dstN < dstP;
	luma_end_N = directionN ? luma_end_N : luma_end_P;

	if(((lumaM - lumaN) < 0.0) == ((luma_end_N - lumaN) < 0.0)) {
		length_sign = 0.0;
	}

	float spanLength = (dstP + dstN);
	dstN = directionN ? dstN : dstP;
	float sub_pixel_offset = (0.5 + (dstN * (-1.0/spanLength))) * length_sign;

	vec3 rgbF = texture(tex, vec2(
	pos.x + (horz_span ? 0.0 : sub_pixel_offset),
	pos.y + (horz_span ? sub_pixel_offset : 0.0))).xyz;

	return fxaa_lerp_3(rgbL, rgbF, blendL);
}


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

float FXAA_REDUCE_MIN = (1.0 / 128.0);
float FXAA_REDUCE_MUL = (1.0 / 8.0);
float FXAA_SCOPE_MAX = 8.0;

vec3 SHADE = vec3(0.299, 0.587, 0.114);

void main() {
	vec2 uv = set_uv(revertTexture, win_size);
	vec2 inverseVP = vec2(1.0 / win_size.x, 1.0 / win_size.y);

	if (showFilter == 0) {
		outColor = texture(textureRendered, coordV2);
	}
	if (showFilter == 1) {	vec2 inverseVP = 1.0 / vec2(window_width, window_height).xy;
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
	if (showFilter == 2) {
		outColor = vec4(fxaa_pixel_shader(textureRendered, uv, inverseVP), 1.0) * 1.0;
	}


}