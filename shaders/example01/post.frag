#version 150
in vec2 coord;

out vec4 outColor; // output from the fragment shader

uniform sampler2D textureRendered;

void main() {
	vec4 textureColor = texture(textureRendered, coord);
	float gray = textureColor.r * 0.33 + textureColor.g * 0.33 + textureColor.b * 0.33;
	outColor = vec4(gray, gray, gray, 1);
}
