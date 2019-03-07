#version 120
varying vec3 position;
varying vec4 uv;
varying vec4 lcolor;
varying float intens;

struct Light {
	vec4 color;
	vec3 position;
	vec3 heading;
	float angle;
};

uniform int chunkX;
uniform int chunkY;
uniform int chunkZ;
uniform sampler2D sampler;
uniform sampler2D lightmap;
uniform mat4 modelview;
uniform Light lights[100];
uniform int lightCount;
uniform int maxLights;
uniform float ticks;

float distSq(vec3 a, vec3 b) {
	return pow((a.x-b.x),2)+pow((a.y-b.y),2)+pow((a.z-b.z),2);
}

float round(float f) {
	if (fract(f) < 0.5f){
		return f - fract(f);
	}
	else {
		return f + (1.0f-fract(f));
	}
}

vec3 snormalize(vec3 v) {
	float len = length(v);
	return (len == 0.0) ? vec3(0.0, 1.0, 0.0) : v / len;
}

//Returns the *angle* between two vectors of any length
float angle(vec3 a, vec3 b) {
	return acos(dot(snormalize(a), snormalize(b)));
}

void main() {
	vec4 pos = gl_ModelViewProjectionMatrix * gl_Vertex;

	position = gl_Vertex.xyz+vec3(chunkX,chunkY,chunkZ);
	vec3 roundedPosition = vec3(0,0,0);
	roundedPosition.x = floor(position.x+0.66f);
	roundedPosition.y = floor(position.y+0.66f);
	roundedPosition.z = floor(position.z+0.66f);

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
		float radius = length(lights[i].heading);
		
		if (distSq(lights[i].position,position) <= pow(radius,2)) {
			//Check cone intensity
			float angleFromHeading = angle(position - lights[i].position, lights[i].heading);
			float angleDelta = max(lights[i].angle - angleFromHeading, 0);
			angleDelta /= (lights[i].angle==0) ? 0.001 : lights[i].angle;
			float coneIntensity = min(pow(angleDelta,2), 1);
			
			//combine cone attenuation with distance attenuation
			float distIntensity = pow(max(0,1.0f-distance(lights[i].position,position)/radius),2);
			float combIntensity = min(coneIntensity, distIntensity);
			
			float faceexposure = 1.0f;
			float intensity = combIntensity * 1.0f * ((max(0,faceexposure)+0.5f)/1.5f);
			
			totalIntens += intensity;
			maxIntens = max(maxIntens,intensity);
		}
	}

	//find the color, whose brightness gets scaled by the total light intensity
	for (int i = 0; i < lightCount; i ++) {
		float radius = length(lights[i].heading);
		
		if (distSq(lights[i].position,position) <= pow(radius,2)) {
			//Check cone intensity
			float angleFromHeading = angle(position - lights[i].position, lights[i].heading);
			float angleDelta = max(lights[i].angle - angleFromHeading, 0);
			angleDelta /= (lights[i].angle==0) ? 0.001 : lights[i].angle;
			float coneIntensity = min(pow(angleDelta,2), 1);
			
			//combine cone attenuation with distance attenuation
			float distIntensity = pow(max(0,1.0f-distance(lights[i].position,position)/radius),2);
			float combIntensity = min(coneIntensity, distIntensity);
			
			float faceexposure = 1.0f;
			float intensity = combIntensity * 1.0f * lights[i].color.w * ((max(0,faceexposure)+0.5f)/1.5f);
			
			sumR += (intensity/totalIntens)*lights[i].color.x;
			sumG += (intensity/totalIntens)*lights[i].color.y;
			sumB += (intensity/totalIntens)*lights[i].color.z;
		}
	}

	//bake the color sums (clamped to zero) into a vec4
	lcolor = vec4(max(sumR*1.5f,0.0f), max(sumG*1.5f,0.0f), max(sumB*1.5f,0.0f), 1.0f);
	intens = min(1.0f, maxIntens);
}