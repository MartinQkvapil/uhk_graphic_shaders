#version 150
in vec2 coord;

out vec4 outColor; // output from the fragment shader

uniform sampler2D textureRendered;
uniform float showFilter;
uniform float timeFilter;

void main() {
	vec4 textureColor = texture(textureRendered, coord);

	if (showFilter == 0) {
		vec4 c = texture(textureRendered, coord);
		c = (c + vec4(0.205, 0.209, 0.228, 0.0)) * vec4(1.5,abs(cos(timeFilter)), abs(sin(timeFilter)), 1.0);
		outColor = c;
	} else {
		outColor = textureColor;
	}
}
