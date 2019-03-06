#version 120
varying vec3 position;
varying vec4 uv;
varying vec4 lcolor;
varying float intens;

struct Light {
    vec4 color;
    vec3 position;
	float radius; //TODO: For cone lights this needs to turn into a vec3 whose magnitude is the light's radius
};

uniform vec3 entityPos;
uniform vec3 playerPos;

uniform sampler2D sampler;
uniform sampler2D lightmap;
uniform mat4 modelview;
uniform Light lights[100]; //This may want to move to a uniform buffer, otherwise lights will always be extremely capped.
uniform int lightCount;
uniform int maxLights;
uniform float ticks;
uniform int lightingEnabled; //why an int? why have this only for entities?


float distSq(vec3 a, vec3 b) {
	return pow((a.x-b.x),2)+pow((a.y-b.y),2)+pow((a.z-b.z),2);
}










void main() {
    vec4 pos = gl_ModelViewProjectionMatrix * gl_Vertex;

	position = gl_Vertex.xyz+entityPos;





	gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;
	gl_TexCoord[1] = gl_TextureMatrix[1] * gl_MultiTexCoord1;

	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;

	gl_FrontColor = gl_Color;

	lcolor = vec4(0,0,0,1.0f);
	float sumR = 0;
	float sumG = 0;
	float sumB = 0;
	float count = 0;
	float maxIntens = 0;
	float totalIntens = 0;

	//Find total intensity
	for (int i = 0; i < lightCount; i ++) {
		if (distSq(lights[i].position,position) <= pow(lights[i].radius,2)) {
			//TODO: Cone light falloff calcs go here

			float faceexposure = 1.0f;
			float intensity = pow(max(0,1.0f-distance(lights[i].position,position)/(lights[i].radius)),2) * 1.0f * ((max(0,faceexposure)+0.5f)/1.5f);
			totalIntens += intensity;
			maxIntens = max(maxIntens,intensity);
		}
	}

	//find the color, whose brightness gets scaled by the total light intensity
	for (int i = 0; i < lightCount; i ++) {
		if (distSq(lights[i].position,position) <= pow(lights[i].radius,2)) {
			//TODO: Cone light falloff calcs go here

			float faceexposure = 1.0f;
			float intensity = pow(max(0,1.0f-distance(lights[i].position,position)/(lights[i].radius)),2) * 1.0f * lights[i].color.w * ((max(0,faceexposure)+0.5f)/1.5f);
			sumR += (intensity/totalIntens)*lights[i].color.x;
			sumG += (intensity/totalIntens)*lights[i].color.y;
			sumB += (intensity/totalIntens)*lights[i].color.z;
		}
	}

	//bake the color sums (clamped to zero) into a vec4
	lcolor = vec4(max(sumR*1.5f,0.0f), max(sumG*1.5f,0.0f), max(sumB*1.5f,0.0f), 1.0f);
	intens = min(1.0f, maxIntens);
}