#version 120
varying vec3 position;
varying float intens;
varying vec4 lcolor;
varying vec4 uv;

uniform sampler2D sampler;
uniform sampler2D lightmap;
uniform vec3 playerPos;



float round(float f) {
	if (fract(f) < 0.5f) {
		return f - fract(f);
	} else {
		return f + (1.0f-fract(f));
	}
}

void main() {
	vec3 lightdark = texture2D(lightmap,gl_TexCoord[1].st).xyz;
	lightdark = clamp(lightdark,0.0f,1.0f);

	vec3 lcolor_2 = clamp(lcolor.xyz*intens, 0.0f, 1.0f);
	lightdark = lightdark + lcolor_2;
	//lightdark = clamp(lightdark,0.0f,1.0f);

	//combine texture with lighting
	vec4 baseColor = gl_Color * texture2D(sampler,gl_TexCoord[0].st);
	baseColor = baseColor * vec4(lightdark, 1);

	//debug
	//baseColor = vec4(lcolor_2.xyz,1);

	//Fog

	float dist = max((gl_FragCoord.z / gl_FragCoord.w) - gl_Fog.start,0.0f);
	float fog = gl_Fog.density * dist * gl_Fog.density;
	fog = 1.0f-clamp( fog, 0.0f, 1.0f );
	baseColor = vec4(mix( vec3( gl_Fog.color ), baseColor.xyz, fog ).xyz,baseColor.w);



	gl_FragColor = baseColor;

	/*
	vec4 color = vec4((mix(baseColor.xyz*lightdark,baseColor.xyz*lcolor.xyz,intens)),baseColor.w);
	gl_FragColor = color;
	*/
}