package a2;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;

import javax.swing.*;
import java.nio.FloatBuffer;

import com.jogamp.common.nio.Buffers;
import org.joml.Matrix4f;

public class Code extends JFrame implements  GLEventListener {
  private GLCanvas myCanvas;
  private int renderingProgram;
  private int vao[] = new int[1];
  private int vbo[] = new int[4];
  private float cameraX, cameraY, cameraZ;
  private float cubeLocX, cubeLocY, cubeLocZ;
  private float pyrLocX, pyrLocY, pyrLocZ;
  private float rodLocX, rodLocY, rodLocZ;

  // allocate variables for display() function
  private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);  // buffer for transfering matrix to uniform
  private Matrix4f pMat = new Matrix4f();  // perspective matrix
  private Matrix4f vMat = new Matrix4f();  // view matrix
  private Matrix4f mMat = new Matrix4f();  // model matrix
  private Matrix4f mvMat = new Matrix4f(); // model-view matrix
  private int mvLoc, pLoc;
  private float aspect;
  private double tf;
  private double startTime;
  private double elapsedTime;
  private int metalTexture;

  public Code()
  {	setTitle("Chapter 4 - program 1c");
    setSize(600, 600);
    myCanvas = new GLCanvas();
    myCanvas.addGLEventListener(this);
    this.add(myCanvas);
    this.setVisible(true);
    Animator animator = new Animator(myCanvas);
    animator.start();
  }

  public void display(GLAutoDrawable drawable)
  {	GL4 gl = (GL4) GLContext.getCurrentGL();
    gl.glClear(GL_DEPTH_BUFFER_BIT);
    gl.glClear(GL_COLOR_BUFFER_BIT);

    gl.glUseProgram(renderingProgram);

    mvLoc = gl.glGetUniformLocation(renderingProgram, "mv_matrix");
    pLoc = gl.glGetUniformLocation(renderingProgram, "p_matrix");

    aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
    pMat.setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);

    vMat.translation(-cameraX, -cameraY, -cameraZ);

    elapsedTime = System.currentTimeMillis() - startTime;
    tf = elapsedTime/1000.0;  // time factor
    mMat.identity();
    // draw pyramid
    // translate and feed
    mMat.translation(pyrLocX, pyrLocY, pyrLocZ);
    mMat.rotateXYZ(1.75f*(float)tf, 1.75f*(float)tf, 1.75f*(float)tf);
    mvMat.identity();
    mvMat.mul(vMat);
    mvMat.mul(mMat);
    gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
    gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
    gl.glVertexAttribPointer(0,3,GL_FLOAT, false, 0,0);
    gl.glEnableVertexAttribArray(0);

    // active buffer one for texture coords
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
    gl.glVertexAttribPointer(1,2,GL_FLOAT,false,  0,0);
    gl.glEnableVertexAttribArray(1);

    // active tecture unit zero and bind to the metal texture object
    gl.glActiveTexture(GL_TEXTURE0);
    gl.glBindTexture(GL_TEXTURE_2D, metalTexture);


    gl.glEnable(GL_DEPTH_TEST);
    gl.glDepthFunc(GL_LEQUAL);

    gl.glDrawArrays(GL_TRIANGLES, 0, 18);




  }

  public void init(GLAutoDrawable drawable) // function copied from tumbling cube
  {	GL4 gl = (GL4) drawable.getGL();
    startTime = System.currentTimeMillis();
    renderingProgram = Utils.createShaderProgram("shaders/vertex.glsl", "shaders/fragment.glsl");
    // the metal texture is from: <a href="https://www.freepik.com/free-photo/empty-brown-rusty-stone-metal-surface-texture_6029183.htm">Image by denamorado on Freepik</a>
    // teh more shiny metal texture is from: <a href="https://www.freepik.com/free-photo/grunge-scratched-brushed-metal-background_21551115.htm#fromView=search&page=1&position=4&uuid=485ba75c-be1d-4557-be65-bcba1513bbcf&query=Metal+texture">Image by kjpargeter on Freepik</a>
    setupVertices();
    setupTextures();
    cameraX = 0.0f; cameraY = 0.0f; cameraZ = 8.0f;
    cubeLocX = 0.0f; cubeLocY = -2.0f; cubeLocZ = 0.0f;
    rodLocX = 0.0f;rodLocY = 0.0f; rodLocZ = 0.0f;
    pyrLocX = 0.3f; pyrLocY = 0.8f; pyrLocZ = 1.0f;
  }

  private void setupTextures() {
    GL4 gl = (GL4) GLContext.getCurrentGL();

    float[] pyrTextureCoordinates =
            { 0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, // top and right faces
                    0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, // back and left faces
                    0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f };


    metalTexture = Utils.loadTexture("grunge-scratched-brushed-metal-background.jpg");
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
    FloatBuffer pTexBuf = Buffers.newDirectFloatBuffer(pyrTextureCoordinates);
    gl.glBufferData(GL_ARRAY_BUFFER, pTexBuf.limit()*4, pTexBuf, GL_STATIC_DRAW);
  }

  private void setupVertices()
  {	GL4 gl = (GL4) GLContext.getCurrentGL();
    float[] cubeVertexPositions =
            {	-1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
                    1.0f, -1.0f, -1.0f, 1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f,
                    1.0f, -1.0f, -1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
                    1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
                    1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
                    -1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
                    -1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
                    -1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
                    -1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,
                    1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,
                    -1.0f,  1.0f, -1.0f, 1.0f,  1.0f, -1.0f, 1.0f,  1.0f,  1.0f,
                    1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f
            };
    float[ ] pyramidPositions =
            { -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, // front face
                    1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, // right face
                    1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, // back face
                    -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, // left face
                    -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, // base – left front
                    1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f // base – right back
            }; // coordinates taken from book
    float[] rodPositions = {

            // ===== Front face =====
            -0.05f, -0.5f,  0.05f,
            0.05f, -0.5f,  0.05f,
            0.05f,  0.5f,  0.05f,

            -0.05f, -0.5f,  0.05f,
            0.05f,  0.5f,  0.05f,
            -0.05f,  0.5f,  0.05f,

            // ===== Back face =====
            -0.05f, -0.5f, -0.05f,
            0.05f,  0.5f, -0.05f,
            0.05f, -0.5f, -0.05f,

            -0.05f, -0.5f, -0.05f,
            -0.05f,  0.5f, -0.05f,
            0.05f,  0.5f, -0.05f,

            // ===== Left face =====
            -0.05f, -0.5f, -0.05f,
            -0.05f, -0.5f,  0.05f,
            -0.05f,  0.5f,  0.05f,

            -0.05f, -0.5f, -0.05f,
            -0.05f,  0.5f,  0.05f,
            -0.05f,  0.5f, -0.05f,

            // ===== Right face =====
            0.05f, -0.5f, -0.05f,
            0.05f,  0.5f,  0.05f,
            0.05f, -0.5f,  0.05f,

            0.05f, -0.5f, -0.05f,
            0.05f,  0.5f, -0.05f,
            0.05f,  0.5f,  0.05f,

            // ===== Top face =====
            -0.05f,  0.5f, -0.05f,
            -0.05f,  0.5f,  0.05f,
            0.05f,  0.5f,  0.05f,

            -0.05f,  0.5f, -0.05f,
            0.05f,  0.5f,  0.05f,
            0.05f,  0.5f, -0.05f,

            // ===== Bottom face =====
            -0.05f, -0.5f, -0.05f,
            0.05f, -0.5f,  0.05f,
            -0.05f, -0.5f,  0.05f,

            -0.05f, -0.5f, -0.05f,
            0.05f, -0.5f, -0.05f,
            0.05f, -0.5f,  0.05f
    };

    gl.glGenVertexArrays(vao.length, vao, 0);
    gl.glBindVertexArray(vao[0]);
    gl.glGenBuffers(vbo.length, vbo, 0);

    // bind and feed in cube positions

    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
    FloatBuffer cubeVertBuf = Buffers.newDirectFloatBuffer(cubeVertexPositions);
    gl.glBufferData(GL_ARRAY_BUFFER, cubeVertBuf.limit()*4, cubeVertBuf, GL_STATIC_DRAW);

    // bind and feed in pyramid positions
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
    FloatBuffer pyramidVertBuf = Buffers.newDirectFloatBuffer(pyramidPositions);
    gl.glBufferData(GL_ARRAY_BUFFER, pyramidVertBuf.limit() * 4, pyramidVertBuf, GL_STATIC_DRAW);



  }

  public static void main(String[] args) { new Code(); }
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
  public void dispose(GLAutoDrawable drawable) {}
}
