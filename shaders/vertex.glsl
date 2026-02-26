#version 430

uniform float offset;
uniform float angle;
uniform bool moveincircle;
uniform float sizeIncrement;
uniform bool useGradient;
uniform vec4 invariantColor;
uniform int direction;
layout(location = 0) out vec4 varyingcolor;

void main(void)
{
    // assign colors for gradient even if were not using them.
    if(gl_VertexID == 0){
      varyingcolor = vec4( 1.0, 0.0, 0.0, 1.0);
    }else if(gl_VertexID == 1){
        varyingcolor = vec4(0.0, 1.0, 0.0, 1.0);
    }else{
        varyingcolor = vec4(0.0, 0.0, 1.0, 1.0);
    }
    if(moveincircle){
        if(gl_VertexID == 0){
            gl_Position = vec4( (0.1 + sizeIncrement) + cos(angle), -0.25 + sin(angle), 0.0, 1.0);
        }else if(gl_VertexID == 1){
            gl_Position = vec4((-0.1 - sizeIncrement) + cos(angle), -0.25 + sin(angle), 0.0, 1.0);
        }else{
             gl_Position = vec4( 0.0 + cos(angle), (0.85 + sizeIncrement) + sin(angle), 0.0, 1.0);
        }
    }else{
        switch(direction){
            case 1: // up
                if (gl_VertexID == 0){
                    gl_Position = vec4( (0.1 + sizeIncrement) + offset, -0.25, 0.0, 1.0);
                }
                else if (gl_VertexID == 1){
                    gl_Position = vec4((-0.1 - sizeIncrement) + offset, -0.25, 0.0, 1.0);
                }
                else{
                    gl_Position = vec4( 0.0 + offset, (0.85 + sizeIncrement), 0.0, 1.0);
                }
            break;

            case 2: // down
                if(gl_VertexID == 0){
                    gl_Position = vec4( -0.1 + offset, 0.1 + sizeIncrement, 0.0, 1.0);
                }else if(gl_VertexID == 1){
                    gl_Position = vec4( 0.1 + offset, 0.1 + sizeIncrement, 0.0, 1.0);
                }else{
                    gl_Position = vec4( 0.0 + offset, -0.85 - sizeIncrement, 0.0, 1.0);
                }
            break;

            case 3: // left
                if(gl_VertexID == 0){
                    gl_Position = vec4( (-0.85 - sizeIncrement) + offset, 0.0, 0.0, 1.0);
                }else if(gl_VertexID == 1){
                    gl_Position = vec4( 0.1 + offset, 0.1 + sizeIncrement, 0.0, 1.0);
                }else{
                    gl_Position = vec4( 0.1   + offset, -0.1 - sizeIncrement, 0.0, 1.0);
                }
            break;

            case 4: // right
                if(gl_VertexID == 0){
gl_Position = vec4( -0.1  + offset, 0.1 + sizeIncrement, 0.0, 1.0);
                }else if(gl_VertexID == 1){
gl_Position = vec4( -0.1 + offset, -0.1 - sizeIncrement, 0.0, 1.0);
                }else{
gl_Position = vec4( (0.85 + sizeIncrement) + offset, 0.0, 0.0, 1.0);
                }
            break;

            default:

            break;
        }

    }

}