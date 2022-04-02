#version 150
in vec2 inPosition; // input from the vertex buffer

uniform mat4 view;
uniform mat4 projection;

void main() {
	vec2 position = inPosition;
	position.x += 0.1;
	vec4 pos4 = vec4(position, 0.0, 1.0);
	gl_Position = projection * view * pos4;
} 
