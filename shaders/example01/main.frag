#version 150
in vec2 coord;
out vec4 outColor; // output from the fragment shader

uniform float color;
uniform sampler2D currentTexture;


void main() {
	vec4 textureColor = texture(currentTexture, coord);

	if(color == 0) outColor = textureColor; // texture
	if(color == 1) outColor = vec4(1f, 0f, 1f, 1f); // basic color
	if(color == 2) outColor = vec4(coord, 0f, 1f); //
	if(color == 3) outColor = vec4(1.0, 0.0, 1.0, 1.0); // depth in bufferx;
	if(color == 4) outColor = vec4(1.0, 1.0, 1.0, 1.0); // normal xyz
	if(color == 5) outColor = vec4(1.0, 1.0, 1.0, 1.0); // mapping texturer rgba
	if(color == 6) outColor = vec4(1.0, 0.0, 1.0, 1.0); // coord texture uv
	if(color == 7) outColor = vec4(1.0, 1.0, 1.0, 1.0); // light + texture

} 
