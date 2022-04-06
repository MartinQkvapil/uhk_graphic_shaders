#version 150
in vec2 coord;
out vec4 outColor; // output from the fragment shader

uniform float color;
uniform sampler2D currentTexture;


void main() {
	vec4 textureColor = texture(currentTexture, coord);

	if(color == 0) outColor = textureColor; // texture
	if(color == 1) outColor = vec4(1.0, 0.0, 0.0, 1.0); // RED color

} 
