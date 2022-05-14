#version 150
in vec2 coord;

out vec4 outColor; // output from the fragment shader

uniform sampler2D textureRendered;
uniform float showFilter;
uniform float timeFilter;

void main() {
	vec4 textureColor = texture(textureRendered, coord);

	if (showFilter == 0) {
		vec4 c = texture(textureRendered, coord);
		c = (c + vec4(0.205, 0.209, 0.228, 0.0)) * vec4(1.5,abs(cos(timeFilter)), abs(sin(timeFilter)), 1.0);
		outColor = c;
	} else {
		outColor = textureColor;
	}
}


/*#version 150
in vec2 coord;

precision highp float;

out vec4 outColor; // output from the fragment shader

uniform sampler2D textureRendered;

uniform float showFilter;
uniform float timeFilter;

uniform vec2 uResolution;
uniform float uBloomStrength;
uniform sampler2D textureBloom;
#define FXAA_REDUCE_MIN   (1.0/ 128.0)
#define FXAA_REDUCE_MUL   (1.0 / 8.0)
#define FXAA_SPAN_MAX     8.0

float FXAA_SUBPIX_SHIFT = 1.0/4.0;

vec4 applyFXAA(sampler2D tex) {
	vec4 color;
	vec2 fragCoord = gl_FragCoord.xy;
	vec3 rgbNW = texture2D(tex, (fragCoord + vec2(-1.0, -1.0)) * uResolution).xyz;
	vec3 rgbNE = texture2D(tex, (fragCoord + vec2(1.0, -1.0)) * uResolution).xyz;
	vec3 rgbSW = texture2D(tex, (fragCoord + vec2(-1.0, 1.0)) * uResolution).xyz;
	vec3 rgbSE = texture2D(tex, (fragCoord + vec2(1.0, 1.0)) * uResolution).xyz;
	vec3 rgbM  = texture2D(tex, fragCoord  * uResolution).xyz;
	vec3 luma = vec3(0.299, 0.587, 0.114);
	float lumaNW = dot(rgbNW, luma);
	float lumaNE = dot(rgbNE, luma);
	float lumaSW = dot(rgbSW, luma);
	float lumaSE = dot(rgbSE, luma);
	float lumaM  = dot(rgbM,  luma);
	float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));
	float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));

	vec2 dir;
	dir.x = -((lumaNW + lumaNE) - (lumaSW + lumaSE));
	dir.y =  ((lumaNW + lumaSW) - (lumaNE + lumaSE));

	float dirReduce = max((lumaNW + lumaNE + lumaSW + lumaSE) *
	(0.25 * FXAA_REDUCE_MUL), FXAA_REDUCE_MIN);

	float rcpDirMin = 1.0 / (min(abs(dir.x), abs(dir.y)) + dirReduce);
	dir = min(vec2(FXAA_SPAN_MAX, FXAA_SPAN_MAX),
	max(vec2(-FXAA_SPAN_MAX, -FXAA_SPAN_MAX),
	dir * rcpDirMin)) * uResolution;

	vec3 rgbA = 0.5 * (
	texture2D(tex, fragCoord * uResolution + dir * (1.0 / 3.0 - 0.5)).xyz +
	texture2D(tex, fragCoord * uResolution + dir * (2.0 / 3.0 - 0.5)).xyz);
	vec3 rgbB = rgbA * 0.5 + 0.25 * (
	texture2D(tex, fragCoord * uResolution + dir * -0.5).xyz +
	texture2D(tex, fragCoord * uResolution + dir * 0.5).xyz);

	float lumaB = dot(rgbB, luma);
	if ((lumaB < lumaMin) || (lumaB > lumaMax))
	color = vec4(rgbA, 1.0);
	else
	color = vec4(rgbB, 1.0);
	return color;
}


void main() {
	vec4 textureColor = texture(textureRendered, coord);

	if (showFilter == 0) {
		outColor = textureColor;
	} else {
//		vec4 c = texture(textureRendered, coord);
//		c = (c + vec4(0.205, 0.209, 0.228, 0.0)) * vec4(1.5,abs(cos(timeFilter)), abs(sin(timeFilter)), 1.0);
//		outColor = c;

		vec4 color   = applyFXAA(textureRendered);
//		vec3 bloom   = texture2D(textureRendered, vTextureCoord).rgb;
	}

//	vec4 textureColor = applyFXAA(textureRendered);
//	vec3 bloom   = texture2D(textureBloom, coord).rgb;
//	textureColor.rgb    += bloom.rgb * uBloomStrength;
//
	outColor = textureColor;
}
*/