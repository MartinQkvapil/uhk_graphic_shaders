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

uniform float edgeThreshold; /* 0.0 - 1.0 */
float edgeThresholdMin = 1.f;

uniform int stepsAround; /* more than 2 */
uniform float stepsThreshold; /* 0.0 - 1.0*/

uniform float subPixelCaption; // Prolnutí subpixelů
uniform float subPixelTrim;

uniform float showFilter;
uniform float timeFilter;

vec2 setUv(float vertical, float horizontal, vec2 res) {
	vec2 uv;

	if (all(equal( vec2(0), res))) {
		uv = coord.st;
	} else if(all(greaterThan(res,coord.st))) {
		uv = coord.st;
	} else {
		uv = res;
	}

	float condY = step( 0.1f, vertical);
	uv.y = 1f -(uv.y * condY +(1f -uv.y) *(1f - condY));

	float condX = step( 0.1f, horizontal);
	uv.x = 1f -(uv.x *condX +(1f -uv.x) * (1f - condX));
	return uv;
}

vec2 setUv(vec2 flip, vec2 res) {
	return setUv(flip.x,flip.y,res);
}

float lumaColor(vec3 rgb) {
	return rgb.y * (0.587/0.299) + rgb.x;
}

vec4 fxaaPixelOffset(sampler2D tex, vec2 pos, ivec2 off, vec2 frame) {
	float x = pos.x + float(off.x) * frame.x;
	float y = pos.y + float(off.y) * frame.y;
	return texture(tex, vec2(x,y));
}

vec3 fxaaWithSettings(sampler2D texture, vec2 pos, vec2 frame) {
	float subpixTrimSize = 1.0 / (1.0 - subPixelTrim); // skip low contrast pix

	vec3 rgbN = fxaaPixelOffset(texture, pos.xy, ivec2( 0,-1), frame).xyz;
	vec3 rgbW = fxaaPixelOffset(texture, pos.xy, ivec2(-1, 0), frame).xyz;
	vec3 rgbM = fxaaPixelOffset(texture, pos.xy, ivec2( 0, 0), frame).xyz;
	vec3 rgbE = fxaaPixelOffset(texture, pos.xy, ivec2( 1, 0), frame).xyz;
	vec3 rgbS = fxaaPixelOffset(texture, pos.xy, ivec2( 0, 1), frame).xyz;

	float lumaN = lumaColor(rgbN);
	float lumaW = lumaColor(rgbW);
	float lumaM = lumaColor(rgbM);
	float lumaE = lumaColor(rgbE);
	float lumaS = lumaColor(rgbS);

	float rangeMin = min(lumaM, min(min(lumaN, lumaW), min(lumaS, lumaE)));
	float rangeMax = max(lumaM, max(max(lumaN, lumaW), max(lumaS, lumaE)));

	if(( rangeMax - rangeMin) < max(edgeThresholdMin, rangeMax * edgeThreshold)) {
		return rgbM;
	}

	vec3 rgbL = rgbN + rgbW + rgbM + rgbE + rgbS;

	float lumaL = (lumaN + lumaW + lumaE + lumaS) * 0.25;
	float rangeL = abs(lumaL - lumaM);
	float blendL = max(0.0, (rangeL / (rangeMax - rangeMin)) - subPixelTrim) * subpixTrimSize;

	blendL = min(subPixelCaption, blendL); // Prolnutí subpixelů.

	vec3 rgbNW = fxaaPixelOffset(texture, pos.xy, ivec2(-1,-1), frame).xyz;
	vec3 rgbNE = fxaaPixelOffset(texture, pos.xy, ivec2( 1,-1), frame).xyz;
	vec3 rgbSW = fxaaPixelOffset(texture, pos.xy, ivec2(-1, 1), frame).xyz;
	vec3 rgbSE = fxaaPixelOffset(texture, pos.xy, ivec2( 1, 1), frame).xyz;

	rgbL += (rgbNW + rgbNE + rgbSW + rgbSE);
	rgbL *= vec3(1.0/9.0);

	float lumaNW = lumaColor(rgbNW);
	float lumaNE = lumaColor(rgbNE);
	float lumaSW = lumaColor(rgbSW);
	float lumaSE = lumaColor(rgbSE);

	float vertikalniHrana =
	abs((0.25 * lumaNW) + (-0.5 * lumaN) + (0.25 * lumaNE)) +
	abs((0.50 * lumaW ) + (-1.0 * lumaM) + (0.50 * lumaE )) +
	abs((0.25 * lumaSW) + (-0.5 * lumaS) + (0.25 * lumaSE)
	);

	float horizontalniHrana =
	abs((0.25 * lumaNW) + (-0.5 * lumaW) + (0.25 * lumaSW)) +
	abs((0.50 * lumaN ) + (-1.0 * lumaM) + (0.50 * lumaS )) +
	abs((0.25 * lumaNE) + (-0.5 * lumaE) + (0.25 * lumaSE)
	);

	bool smer = horizontalniHrana >= vertikalniHrana;
	float smerMichani = smer ? -frame.y : -frame.x;

	if(!smer) {
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
		smerMichani *= -1.0;
	}

	vec2 posN;
		posN.x = pos.x + (smer ? 0.0 : smerMichani * 0.5);
		posN.y = pos.y + (smer ? smerMichani * 0.5 : 0.0);

	gradientN *= stepsThreshold;

	vec2 posP = posN;
	vec2 offNP = smer ? vec2(frame.x, 0.0) : vec2(0.0, frame.y);
	float lumaEndN = lumaN;
	float lumaEndP = lumaN;

	bool doneN = false;
	bool doneP = false;

	posN += offNP * vec2(-1.0, -1.0);
	posP += offNP * vec2( 1.0,  1.0);

	for(float i = 0; i < stepsAround; i++) {
		if(!doneN) {
			lumaEndN = lumaColor(texture2D(texture, posN.xy).xyz);
		}
		if(!doneP) {
			lumaEndP = lumaColor(texture2D(texture, posP.xy).xyz);
		}

		doneN = doneN || (abs(lumaEndN - lumaN) >= gradientN);
		doneP = doneP || (abs(lumaEndP - lumaN) >= gradientN);

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

	float vzdalenostN = smer ? pos.x - posN.x : pos.y - posN.y;
	float vzdalenostP = smer ? posP.x - pos.x : posP.y - pos.y;

	bool smerN = vzdalenostN < vzdalenostP;
	lumaEndN = smerN ? lumaEndN : lumaEndP;

	if (((lumaM - lumaN) < 0.0) == ((lumaEndN - lumaN) < 0.0)) {
		smerMichani = 0.0;
	}

	float spanLength = (vzdalenostP + vzdalenostN);
	vzdalenostN = smerN ? vzdalenostN : vzdalenostP;

	float subPixelOffset = (0.5 + (vzdalenostN * (-1.0 / spanLength))) * smerMichani;

	vec3 rgbF = texture2D(texture, vec2(
		pos.x + (smer ? 0.0 : subPixelOffset),
		pos.y + (smer ? subPixelOffset : 0.0))).xyz;

	return (vec3(-blendL) * rgbF) + ((rgbL * vec3(blendL)) + rgbF);
}


struct verRgb {
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

float reduceMin = (1.0 / 128.0);
float reduceMul = (1.0 / 8.0);
float scopeMax = 20.0;
vec3 shade = vec3(0.299, 0.587, 0.114);

void main() {
	vec2 uv = setUv(revertTexture, win_size);
	vec2 inverseVP = vec2(1.0 / win_size.x, 1.0 / win_size.y);

	if (showFilter == 0) {
		outColor = texture(textureRendered, coordV2);
	}
	if (showFilter == 1) {	vec2 inverseVP = 1.0 / vec2(window_width, window_height).xy;
		
		verRgb verRgb = verRgb(
		(gl_FragCoord.st + vec2(-1.0, -1.0)) * inverseVP,
		(gl_FragCoord.st + vec2(1.0, -1.0)) * inverseVP,
		(gl_FragCoord.st + vec2(-1.0, 1.0)) * inverseVP,
		(gl_FragCoord.st + vec2(1.0, 1.0)) * inverseVP,
		vec2(gl_FragCoord.st * inverseVP)
		);

		vec4 texColor = texture(textureRendered, verRgb.M);
		vec3 rgbM  = texColor.xyz;

		luma l = luma(
			dot(texture(textureRendered, verRgb.NW).xyz, shade),
			dot(texture(textureRendered, verRgb.NE).xyz, shade),
			dot(texture(textureRendered, verRgb.SW).xyz, shade),
			dot(texture(textureRendered, verRgb.SE).xyz, shade),
			dot(texture(textureRendered, verRgb.M).xyz,  shade)
		);

		float lumaMin = min(l.M, min(min(l.NW, l.NE), min(l.SW, l.SE)));
		float lumaMax = max(l.M, max(max(l.NW, l.NE), max(l.SW, l.SE)));

		vec2 direction;
		direction.x = -((l.NW + l.NE) - (l.SW + l.SE));
		direction.y =  ((l.NW + l.SW) - (l.NE + l.SE));

		float directionReduce = max((l.NW + l.NE + l.SW + l.SE) *	(0.25 * reduceMul), reduceMin);

		float rcpDirMin = 1.0 / (min(abs(direction.x), abs(direction.y)) + directionReduce);
		direction = min(vec2(scopeMax, scopeMax),
		max(vec2(-scopeMax, -scopeMax),
		direction * rcpDirMin)) * inverseVP;

		vec3 rgbA = 0.5 * (
			texture(textureRendered, gl_FragCoord.st * inverseVP + direction * (1.0 / 3.0 - 0.5)).xyz +
			texture(textureRendered, gl_FragCoord.st * inverseVP + direction * (2.0 / 3.0 - 0.5)).xyz
		);

		vec3 rgbB = rgbA * 0.5 + 0.25 * (
			texture(textureRendered, gl_FragCoord.st * inverseVP + direction * -0.5).xyz +
			texture(textureRendered, gl_FragCoord.st * inverseVP + direction * 0.5).xyz
		);

		float lumaB = dot(rgbB, shade);

		if ((lumaB < lumaMin) || (lumaB > lumaMax)) {
			outColor = vec4(rgbA, texColor.a);
		} else {
			outColor = vec4(rgbB, texColor.a);
		}
	}
	if (showFilter == 2) {
		outColor = vec4(fxaaWithSettings(textureRendered, uv, inverseVP), 1.0) * 1.0;
	}
}
