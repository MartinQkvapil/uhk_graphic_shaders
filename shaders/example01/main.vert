#version 150
in vec2 inPosition; // input from the vertex buffer

uniform mat4 projection;
uniform mat4 view;
uniform float color;
uniform float type;
uniform float time;

const float PI = 3.1415;

float getSimple(vec2 vec) {
	return sin(vec.y * PI * 2);
}

/** Kartezky objects */
vec3 getKartez01(vec2 vec){
	float z = 0.65 * cos(sqrt(20 * vec.x * vec.x + 20 * vec.y * vec.y) + sin(time));
	return vec3(vec.x, vec.y, z);
}
vec3 getKartez02(vec2 vec){
	float x = vec.x;
	float y = vec.y;
	float z = cos((vec.x * vec.y) + time);
	return vec3(x, y, z);
}

/** Spherical objects */
vec3 getSpherical01(vec2 vec) { // From exercise
	float az = vec.x * PI; // <-1;1> -> <-PI;PI>
	float ze = vec.y * PI / 2.0; // <-1;1> -> <-PI/2;PI/2>
	float r = 1.0;

	float x = r * cos(az) * cos(ze);
	float y = 2 * r * sin(az) * cos(ze);
	float z = 0.5 * r * sin(ze);
	return vec3(x, y, z);
}

vec3 getSpherical02(vec2 position) {
	float az = position.x * PI; // <-1;1> -> <-PI;PI>
	float ze = position.y * PI / 2.0;
	float r = 1.0;

	float x = r * sin(ze) * cos(az);
	float y = r * sin(ze) * sin(az);
	float z = r * cos(ze);
	return vec3(x, y, z);
}

/** Cylindric objects */
vec3 getCylindric01(vec2 vec){
	float u = vec.x * PI * (cos(time));
	float v = vec.y; // <-1;1>

	float x = cos(u);
	float y = sin(u);
	float z = v / 2;
	return vec3(x, y, z);
}
// TODO add one more cylindric;

void main() {
	//	texCoord = inPosition;

	// grid je <0;1> - chci <-1;1>
	vec2 position = inPosition * 2 - 1;

	vec3 finalPosition;
	if (type == 0) {
		finalPosition = getKartez01(position);
	} else if (type == 1) {
		finalPosition = getKartez02(position);
	} else if (type == 2) {
		finalPosition = getSpherical01(position);
	} else if (type == 3) {
		finalPosition = getSpherical02(position);
	} else if (type == 4) {
		finalPosition = getCylindric01(position);
	} else if (type == 5) {
		finalPosition = getCylindric01(position);
	} else {
		finalPosition = vec3(position, getSimple(position));
	}

	vec4 pos4 = vec4(finalPosition, 1.0);
	gl_Position = projection * view * pos4;
} 