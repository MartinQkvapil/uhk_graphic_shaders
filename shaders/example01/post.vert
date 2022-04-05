#version 150
in vec2 inPosition; // input from the vertex buffer

out vec2 textCoord;

void main() {
	textCoord = inPosition;

	// grid je <0, 1> - chci <-1;1>
	vec2 position = inPosition * 2 - 1;
	gl_Position = vec4(position, 0, 1.0);
} 
