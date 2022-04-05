#version 150
in vec2 textCoord;

out vec4 outColor; // output from the fragment shader

uniform sampler2D textureRendered;

void main() {
	vec4 textureColor = texture(textureRendered, textCoord);

	if(gl_FragCoord.y < 200) {
		float gray = textureColor.r * 0.33 + textureColor.g * 0.33 + textureColor.b * 0.33;
		outColor = vec4(gray, gray, gray, 1);
	} else {
		outColor = textureColor;
	}
} 
