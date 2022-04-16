#version 150
// Normalize objects: https://stackoverflow.com/questions/4309720/how-can-i-find-the-normals-for-each-vertex-of-an-object-so-that-i-can-apply-smoo

in vec2 inPosition; // input from the vertex buffer

uniform mat4 projection;
uniform mat4 view;
uniform float color;
uniform float type;
uniform mat4 model;
uniform float time;
uniform vec3 light;


const float PI = 3.1415;
const float DEVIATION = 0.01;

out vec2 coord;
out vec4 objectPosition;
out vec3 normal;

// Light
out vec3 lightDirection, viewDirection;
out float dist;

vec3 getNormal(vec3 u, vec3 v) {
	return cross(u, v);
}
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
vec3 getSpericalCircle(vec2 position, float radius) {
	float az = position.x * PI + time; // <-1;1> -> <-PI;PI>
	float ze = position.y * PI / 2.0;
	float r = 1.0 * radius;

	float x = r * cos(az) * cos(ze);
	float y = r * sin(az) * cos(ze);
	float z = r * sin(ze);
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
vec3 getCylindric02(vec2 vec){
	float a = 1.0, b = 1.0, c = 1.0, d = 1.0;
	float u = vec.x * PI * (cos(time));
	float v = vec.y; // <-1;1>

	float x = cos(u)*(a + b*cos(v));
	float y = sin(u)*(a + b*cos(v));
	float z = b*sin(v);
	return vec3(x, y, z);
}

void main() {
	coord = inPosition;
	// grid je <0;1> - chci <-1;1> Position changed;
	vec2 position = inPosition * 2 - 1;
	vec3 lastPosition;
	vec4 lightPosition;
	vec3 u, v;
	vec3 objNormal;


	// OBJECTS
	if (type == 0) {
		float radius = 1.0;
		lastPosition = getSpericalCircle(position, radius);
		u = getSpericalCircle(position + vec2(DEVIATION, 0), radius) - getSpericalCircle(position - vec2(DEVIATION, 0), radius);
		v = getSpericalCircle(position + vec2(0, DEVIATION), radius) - getSpericalCircle(position - vec2(0, DEVIATION), radius);
	} else if (type == 1) {
		lastPosition = getKartez02(position);
		u = getKartez02(position + vec2(DEVIATION, 0)) - getKartez02(position - vec2(DEVIATION, 0));
		v = getKartez02(position + vec2(0, DEVIATION)) - getKartez02(position - vec2(0, DEVIATION));
	} else if (type == 2) {
		lastPosition = getKartez01(position);
		// Calculate kartez normal
		u = getKartez01(position + vec2(DEVIATION, 0)) - getKartez01(position - vec2(DEVIATION, 0));
		v = getKartez01(position + vec2(0, DEVIATION)) - getKartez01(position - vec2(0, DEVIATION));
	} else if (type == 3) {
		lastPosition = getSpherical01(position);
		u = getSpherical01(position + vec2(DEVIATION, 0)) - getSpherical01(position - vec2(DEVIATION, 0));
		v = getSpherical01(position + vec2(0, DEVIATION)) - getSpherical01(position - vec2(0, DEVIATION));
	} else if (type == 4) {
		lastPosition = getCylindric01(position);
		u = getCylindric01(position + vec2(DEVIATION, 0)) - getCylindric01(position - vec2(DEVIATION, 0));
		v = getCylindric01(position + vec2(0, DEVIATION)) - getCylindric01(position - vec2(0, DEVIATION));
	} else if (type == 5) {
		lastPosition = getCylindric02(position);
		u = getCylindric02(position + vec2(DEVIATION, 0)) - getCylindric02(position - vec2(DEVIATION, 0));
		v = getCylindric02(position + vec2(0, DEVIATION)) - getCylindric02(position - vec2(0, DEVIATION));
	} else if (type == 666) {
		lastPosition = getSpericalCircle(position, 0.4);
	}


	// Transformation normal to other vectors - PG3_14 s14 #normalTransformation
	normal = transpose(inverse(mat3(model))) * getNormal(u, v);

	// Object position
	objectPosition = model * vec4(lastPosition, 1.0);
	// Light position
	lightPosition = model * vec4(light, 1.0);

	// Light direction
	lightDirection = normalize(light - objectPosition.xyz);
	viewDirection = -objectPosition.xyz;
	dist = length(normalize(light - objectPosition.xyz)); // #dist

	vec4 pos4 = vec4(lastPosition, 1.0);
	gl_Position = projection * view * model * pos4;
} 