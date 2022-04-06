#version 150
in vec2 inPosition; // input from the vertex buffer

uniform mat4 projection;
uniform mat4 view;
uniform float color;
uniform int type;

const float PI = 3.1415;

float getZ(vec2 vec) {
	return sin(vec.y * PI * 2);
}

vec3 getSphere(vec2 vec) {
	float az = vec.x * PI; // <-1;1> -> <-PI;PI>
	float ze = vec.y * PI / 2.0; // <-1;1> -> <-PI/2;PI/2>
	float r = 1.0;

	float x = r * cos(az) * cos(ze);
	float y = 2 * r * sin(az) * cos(ze);
	float z = 0.5 * r * sin(ze);
	return vec3(x, y, z);
}

void main() {
	//	texCoord = inPosition;

	// grid je <0;1> - chci <-1;1>
	vec2 position = inPosition * 2 - 1;

	vec3 finalPosition;
	//	vec3 normal;
	if (type == 1) {
		finalPosition = getSphere(position);
		//		normal = getSphereNormal(position);
	} else if (type == 2) {
		finalPosition = vec3(position, getZ(position));
		//		normal = getOtherNormal(position);
	}
	vec4 pos4 = vec4(finalPosition, 1.0);
	gl_Position = projection * view * pos4;
} 