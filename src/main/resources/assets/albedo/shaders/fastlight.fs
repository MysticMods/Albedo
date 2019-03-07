#version 120
varying vec3 position;
varying float intens;
varying vec4 lcolor;
varying vec4 uv;

uniform sampler2D sampler;
uniform sampler2D lightmap;
uniform vec3 playerPos;
uniform int chunkX;
uniform int chunkZ;

float round(float f) {
	if (fract(f) < 0.5f) {
		return f - fract(f);
	} else {
		return f + (1.0f-fract(f));
	}
}

void main() {
	vec3 lightdark = texture2D(lightmap,gl_TexCoord[1].st).xyz;
	lightdark = clamp(lightdark, 0.0f, 1.0f);

	vec3 lcolor_2 = clamp(lcolor.xyz*intens, 0.0f, 1.0f);
	
	//lightdark = lightdark + lcolor_2;   //More washed-out, but more physically correct
	lightdark = max(lightdark, lcolor_2); //Vivid but unrealistic

	//combine texture with lighting
	vec4 baseColor = gl_Color * texture2D(sampler,gl_TexCoord[0].st);
	baseColor = baseColor * vec4(lightdark, 1);

	//debug
	//baseColor = vec4(lcolor_2.xyz,1);

	//Fog
	vec3 dv = position-playerPos;
	float dist = max(sqrt(dv.x*dv.x+dv.y*dv.y+dv.z*dv.z) - gl_Fog.start,0.0f) / (gl_Fog.end-gl_Fog.start);
	float fog = gl_Fog.density * dist;
	fog = 1.0f-clamp( fog, 0.0f, 1.0f );
	baseColor = vec4(mix( vec3( gl_Fog.color ), baseColor.xyz, fog ).xyz,baseColor.w);



	gl_FragColor = baseColor;

	/*
	vec4 color = vec4(max(mix(baseColor.xyz*lightdark,baseColor.xyz*lcolor.xyz,intens),lightdark*baseColor.xyz),baseColor.w);
	gl_FragColor = color;
	*/
}