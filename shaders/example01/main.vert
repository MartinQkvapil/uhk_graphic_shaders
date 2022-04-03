#version 150
in vec2 inPosition; // input from the vertex buffer

uniform mat4 view;
uniform mat4 projection;

out vec2 textCoord;

void main() {
	textCoord = inPosition;
	vec2 position = inPosition * 2 - 1;
	float z = 0.5 * cos(sqrt(20 * position.x * position.x + 20 * position.y * position.y));
	vec4 pos4 = vec4(position, z, 1.0);
	gl_Position = projection * view * pos4;
} 
