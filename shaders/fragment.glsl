#version 430

out vec4 color;
layout (location = 0) in vec4 varyingcolor; // receive gradient colors

uniform vec4 invariantColor;
uniform bool useGradient;

void main(void)
{
    if(useGradient){
        color = varyingcolor;
    }else{
        color = invariantColor;
    }

}