#version 150
in vec2 textCoord;
out vec4 outColor; // output from the fragment shader

uniform sampler2D textureMosaic;

void main() {
	vec4 textureColor = texture(textureMosaic, textCoord);

//	outColor = vec4(1.0, 0.0, 0.0, 1.0);
	outColor = textureColor;
} 
