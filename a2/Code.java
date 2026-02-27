package a2;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import com.jogamp.common.nio.Buffers;
import org.joml.Matrix4f;

public class Code extends JFrame implements  GLEventListener {
  private GLCanvas canvas;
  private int renderingProgram;
  private int[] vao = new int[1];
  private int vbo[] = new int[2];
  private float cameraX, cameraY, cameraZ,
  cubeLocX, cubeLocY, cubeLocZ;
  private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
  private Matrix4f pMat= new Matrix4f();
  private Matrix4f vMat= new Matrix4f();
  private Matrix4f mMat= new Matrix4f();
  private Matrix4f mvMat = new Matrix4f();
  private int mvLoc, pLoc;
  private float aspect;
  double elapsedTime, startTime, tf;
  public static void main(String[] args) {
    new Code();

  }

  public Code() {
    setTitle("155 Assignment 2");
    setSize(800, 600);
    canvas = new GLCanvas();
    canvas.addGLEventListener(this);
    this.add(canvas);
    setVisible(true);
    Animator animator = new Animator(canvas);
    animator.start();
  }
  @Override
  public void init(GLAutoDrawable glAutoDrawable) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    renderingProgram = a2.Utils.createShaderProgram("shaders\\vertex.glsl", "shaders\\fragment.glsl");
    setupVertices();
    cameraX = 0.0f; cameraY = 0.0f; cameraZ = 8.0f;
    cubeLocX = 0.0f; cubeLocY = -2.0f; cubeLocZ = 0.0f;
    startTime = System.currentTimeMillis();

  }

  private void setupVertices() {
    GL4 gl = (GL4) GLContext.getCurrentGL();
// 36 vertices of the 12 triangles making up a 2 x 2 x 2 cube centered at the origin
    float[ ] vertexPositions =
            { -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
                    1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f,
                    1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
                    1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
                    1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f,
                    1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f,
                    -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f
            };
    gl.glGenVertexArrays(vao.length, vao, 0);
    gl.glBindVertexArray(vao[0]);
    gl.glGenBuffers(vbo.length, vbo, 0);
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
    FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(vertexPositions);
    gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

  }

  @Override
  public void dispose(GLAutoDrawable glAutoDrawable) {

  }

  @Override
  public void display(GLAutoDrawable glAutoDrawable) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    gl.glClear(GL_DEPTH_BUFFER_BIT);
    gl.glClear(GL_COLOR_BUFFER_BIT);


    gl.glUseProgram(renderingProgram);
// get references to the uniform variables for the MV and projection matrices
    mvLoc = gl.glGetUniformLocation(renderingProgram, "mv_matrix");
    pLoc = gl.glGetUniformLocation(renderingProgram, "p_matrix");
// build perspective matrix. This one has fovy=60, aspect ratio matches the screen window.
// Values for near and far clipping planes can vary as discussed in Section 4.9
    aspect = (float) canvas.getWidth() / (float) canvas.getHeight();
    pMat.setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
// build view matrix, model matrix, and model-view matrix
    vMat.translation(-cameraX, -cameraY, -cameraZ);

    elapsedTime = System.currentTimeMillis() - startTime;
    tf = elapsedTime / 1000.0;
    mMat.identity();
    mMat.translate((float)Math.sin(.35f*tf)*2.0f, (float)Math.sin(.52f*tf)*2.0f, (float)Math.sin(.7f*tf)*2.0f);
    mMat.rotateXYZ(1.75f*(float)tf, 1.75f*(float)tf, 1.75f*(float)tf);


    mvMat.identity();
    mvMat.mul(vMat);
    mvMat.mul(mMat);
// copy perspective and MV matrices to corresponding uniform variables
    gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
    gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
// associate VBO with the corresponding vertex attribute in the vertex shader
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
    gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(0);
// adjust OpenGL settings and draw model
    gl.glEnable(GL_DEPTH_TEST);
    gl.glDepthFunc(GL_LEQUAL);
    gl.glDrawArrays(GL_TRIANGLES, 0, 36);
  }

  @Override
  public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {

  }
}
