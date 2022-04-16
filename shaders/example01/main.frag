#version 150
in vec2 coord;
in vec4 objectPosition;
in vec3 normal;

// light
in vec3 lightDirection;
in vec3 viewDirection;
in float dist;

uniform vec3 light;
uniform int lightType;

out vec4 outColor; // output from the fragment shader

uniform float color;
uniform sampler2D currentTexture;

const vec4 lightColor = vec4(1.0, 1.0, 1.0, 1.0);
const float attenuationConst = 1.0;
const float attenuationLinear = 0.3;
const float attenuationQuadratic = 0.03;
const float power = 20.0;

void main() {
	vec4 textureColor = texture(currentTexture, coord);

	vec3 norm = normalize(normal);
	vec3 lightDir = normalize(lightDirection);
	vec3 viewDir = normalize(viewDirection);

	float NDotL = max(dot(norm,lightDir),0.0);
	float NDotH = max(0.0, dot(norm, normalize(lightDir + viewDir)));

	vec3 reflect = normalize((( 2.0 * norm) * NDotL) - lightDir); // #reflection
	float RDotV = max(0.0, dot(reflect, viewDir));

	vec4 ambient = vec4(0.75,0.75,0.75,1.0); // #ambient
	vec4 diffuse = NDotL * lightColor; // #duffus
	vec4 specular = NDotH * lightColor;

	vec4 totalSpecular = specular * pow(RDotV, power);

	float att = 1.0 / (attenuationConst + attenuationLinear * dist + attenuationQuadratic * dist * dist);
	vec4 lighting = ambient + att * (diffuse + totalSpecular);

	if (max(dot(normalize(-light),normalize(-lightDir)), 0) >  0.8) {
		if (lightType == 0) { lighting = ambient + att * (diffuse + specular); }
		if (lightType == 1) { lighting = att * (diffuse + specular); }
		if (lightType == 2) { lighting = ambient + att * (specular); }
		if (lightType == 3) { lighting = ambient + att; }
	} else {
		lighting = ambient;
	}


	if(color == 0) outColor = vec4(1.0, 0.0, 1.0, 1.0) * lighting; // #colorAndLight
	if(color == 1) outColor = vec4(1.0, 1.0, 0.0, 1.0); // #depthBuff
	if(color == 2) outColor = vec4(coord, 0.0, 1.0); // #colorToTexture
	if(color == 3) outColor = objectPosition; // #objPosition
	if(color == 4) outColor = vec4(normalize(normal),1.0); // #normal // always normalize normal
	if(color == 5) outColor = textureColor; // #texture;
	if(color == 6) outColor = textureColor * lighting; // #lightAndtexture
	if(color == 666) outColor = vec4(1.0, 1.0, 0.0, 1.0); // #lightYellowColor
}
