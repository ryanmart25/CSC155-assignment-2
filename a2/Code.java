package a2;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.FloatBuffer;

import com.jogamp.common.nio.Buffers;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Code extends JFrame implements  GLEventListener, KeyListener {
  private static final int VBO_COUNT = 8;
  private static final int CUBE_VERTEX_VBO = 0, CUBE_TEXTURE_VBO = 1, PYRAMID_VERTEX_VBO = 2, PYRAMID_TEXTURE_VBO = 3, ROD_VERTEX_VBO = 4, ROD_TEXTURE_VBO = 5, SHUTTLE_VERTEX_VBO = 6, SHUTTLE_TEXTURE_VBO = 7;
  private static final int VERTEX_LAYOUT = 0, TEXTURE_LAYOUT = 1;
  private static final int SAMPLER_LAYOUT = 0;
  private GLCanvas myCanvas;
  private int renderingProgram;
  private int vao[] = new int[1];
  private int vbo[] = new int[VBO_COUNT];
  // textures
  private int metalTexture;
  private int noiseTexture; // made by me
  private int rustyTexture;
  // VBO layout: cube vert, cube tex, pyramid vert, pyramid tex, rod vert, rod tex, shuttle vert, shuttle  tex
  private float cubeLocX, cubeLocY, cubeLocZ;
  private float pyrLocX, pyrLocY, pyrLocZ;
  private float rodLocX, rodLocY, rodLocZ;
  private float shuttleLocX, shuttleLocY, shuttleLocZ;
  // allocate variables for display() function
  private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);  // buffer for transferring matrix to uniform
  private Matrix4f pMat = new Matrix4f();  // perspective matrix
  private Matrix4f vMat = new Matrix4f();  // view matrix
  private Matrix4f mMat = new Matrix4f();  // model matrix
  private Matrix4f mvMat = new Matrix4f(); // model-view matrix
  private int mvLoc, pLoc;
  private float aspect;
  private double tf;
  private double startTime;
  private double elapsedTime;
  private Camera camera;

  // imported models (code from textbook)
  private final ImportedModel shuttle = new ImportedModel("shuttle.obj");
  public Code()
  {	setTitle("Assignment 2 - Ryan Martinez");
    setSize(600, 600);
    myCanvas = new GLCanvas();
    myCanvas.addGLEventListener(this);
    myCanvas.addKeyListener(this);
    this.add(myCanvas);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setExtendedState(JFrame.MAXIMIZED_BOTH);
    this.setVisible(true);
    Animator animator = new Animator(myCanvas);
    animator.start();
  }

  public void display(GLAutoDrawable drawable)
  {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    gl.glClear(GL_DEPTH_BUFFER_BIT);
    gl.glClear(GL_COLOR_BUFFER_BIT);

    gl.glUseProgram(renderingProgram);

    mvLoc = gl.glGetUniformLocation(renderingProgram, "mv_matrix");
    pLoc = gl.glGetUniformLocation(renderingProgram, "p_matrix");

    aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
    pMat.setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);


    // setup camera matrices
    Matrix4f T = new Matrix4f().translation(camera.getCameraX() * -1.0f, camera.getCameraY() * -1.0f, camera.getCameraZ() * -1.0f);
    Matrix4f R = camera.getRotationMatrix();
    vMat = R.mul(T);

    elapsedTime = System.currentTimeMillis() - startTime;
    tf = elapsedTime/1000.0;  // time factor

    // draw shuttle
    mMat.identity();
    mMat.translation(shuttleLocX, shuttleLocY, shuttleLocZ);
    mMat.rotateXYZ(0.25f, 0.5f, 0);
    mMat.scale(2.0f);
    mvMat.identity();
    mvMat.mul(vMat);
    mvMat.mul(mMat);

    // feed MV matrix into glsl
    gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
    gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));

    // asset context: we're targeting the shuttle's vertex VBO now.
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[SHUTTLE_VERTEX_VBO]);
    gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

    gl.glEnableVertexAttribArray(VERTEX_LAYOUT);
    // activate and feed textures for teh icosahedron
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[SHUTTLE_TEXTURE_VBO]);
    gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(TEXTURE_LAYOUT);
    gl.glActiveTexture(GL_TEXTURE0);
    gl.glBindTexture(GL_TEXTURE_2D, noiseTexture);

    gl.glEnable(GL_DEPTH_TEST);
    gl.glDepthFunc(GL_LEQUAL);

    gl.glDrawArrays(GL_TRIANGLES, 0, shuttle.getNumVertices());
    // draw cube
    mMat.identity();
    mMat.translation(cubeLocX * (float) tf, cubeLocY, cubeLocZ );
    mMat.rotateXYZ(2.25f * (float) tf, 0.0f, 0.0f);
    mvMat.identity();
    mvMat.mul(vMat);
    mvMat.mul(mMat);
    gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
    gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[CUBE_VERTEX_VBO]);
    gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0,0);
    gl.glEnableVertexAttribArray(VERTEX_LAYOUT);
    // bind and load textures
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[CUBE_TEXTURE_VBO]);
    gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0,0);
    gl.glEnableVertexAttribArray(TEXTURE_LAYOUT);
    // activate texture and bind the noise texture
    gl.glActiveTexture(GL_TEXTURE0);
    gl.glBindTexture(GL_TEXTURE_2D, noiseTexture);
    gl.glEnable(GL_DEPTH_TEST);
    gl.glDepthFunc(GL_LEQUAL);
    gl.glDrawArrays(GL_TRIANGLES, 0, 36);

    // draw pyramid
    // translate and feed
    mMat.identity();
    mMat.translation(pyrLocX, pyrLocY, pyrLocZ);
    mMat.rotateXYZ(1.75f*(float)tf, 1.75f*(float)tf, 1.75f*(float)tf);
    mvMat.identity();
    mvMat.mul(vMat);
    mvMat.mul(mMat);
    gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
    gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[PYRAMID_VERTEX_VBO]);
    gl.glVertexAttribPointer(0,3,GL_FLOAT, false, 0,0);
    gl.glEnableVertexAttribArray(VERTEX_LAYOUT);

    // active buffer one for texture coords
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[PYRAMID_TEXTURE_VBO]);
    gl.glVertexAttribPointer(1,2,GL_FLOAT,false,  0,0);
    gl.glEnableVertexAttribArray(TEXTURE_LAYOUT);

    // active tecture unit zero and bind to the metal texture object
    gl.glActiveTexture(GL_TEXTURE0);
    gl.glBindTexture(GL_TEXTURE_2D, metalTexture);


    gl.glEnable(GL_DEPTH_TEST);
    gl.glDepthFunc(GL_LEQUAL);

    gl.glDrawArrays(GL_TRIANGLES, 0, 18);




    // draw rod
    // define position, rotation of model and camera viewpoint
    mMat.identity();
    mMat.translation(rodLocX, rodLocY, rodLocZ);
    mMat.rotateXYZ(0.25f * (float) tf, 0, 0);
    mvMat.identity();
    mvMat.mul(vMat);
    mvMat.mul(mMat);
    // feed MV matrix into glsl
    gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
    gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));

    // assert context: we're targeting to the Rod's vertex VBO now
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[ROD_VERTEX_VBO]);
    gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

    gl.glEnableVertexAttribArray(VERTEX_LAYOUT);
    // activate and feed texture for the rod
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[ROD_TEXTURE_VBO]);
    gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(TEXTURE_LAYOUT);

    gl.glActiveTexture(GL_TEXTURE0);
    gl.glBindTexture(GL_TEXTURE_2D, rustyTexture);


    gl.glEnable((GL_DEPTH_TEST));
    gl.glDepthFunc(GL_LEQUAL);

    gl.glDrawArrays(GL_TRIANGLES, 0, 36);


  }

  public void init(GLAutoDrawable drawable) // function copied from tumbling cube
  {	GL4 gl = (GL4) drawable.getGL();
    startTime = System.currentTimeMillis();
    renderingProgram = Utils.createShaderProgram("shaders/vertex.glsl", "shaders/fragment.glsl");
    // the rusty metal texture is from: <a href="https://www.freepik.com/free-photo/empty-brown-rusty-stone-metal-surface-texture_6029183.htm">Image by denamorado on Freepik</a>
    // teh more shiny metal texture is from: <a href="https://www.freepik.com/free-photo/grunge-scratched-brushed-metal-background_21551115.htm#fromView=search&page=1&position=4&uuid=485ba75c-be1d-4557-be65-bcba1513bbcf&query=Metal+texture">Image by kjpargeter on Freepik</a>
    setupVertices();
    setupTextures();
    System.out.println("shuttle ico getNumVertices(): " + shuttle.getNumVertices());
    System.out.println("shuttle  getVertices().length: " + shuttle.getVertices().length);
    System.out.println("shuttle  getTexCoords().length: " + shuttle.getTexCoords().length);
    //cameraX = 0.0f; cameraY = 0.0f; cameraZ = 12.0f;
    cubeLocX = 0.0f; cubeLocY = -2.0f; cubeLocZ = 0.0f;
    rodLocX = -12.0f;rodLocY = 0.0f; rodLocZ = 0.0f;
    pyrLocX = 5.3f; pyrLocY = 0.8f; pyrLocZ = 1.0f;
    shuttleLocX = -5.0f; shuttleLocY = 5.0f; shuttleLocZ = 0.0f;

    this.camera = new Camera(0.1f, 0.1f, 0.0f, 0.0f, 12.0f);
  }

  private void setupTextures() {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    noiseTexture = Utils.loadTexture("MyTexture.png");
    metalTexture = Utils.loadTexture("grunge-scratched-brushed-metal-background.jpg");
    rustyTexture = Utils.loadTexture("empty-brown-rusty-stone-metal-surface-texture.jpg");
    // setup pyramid texture coordinates

    // define
    float[] pyrTextureCoordinates =
            { 0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, // top and right faces
                    0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, // back and left faces
                    0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f };

    // bind and load

    // setup cube texture coordinates
    // define
    float[] cubeTextureCoordinates =
            {
                    0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                    1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,

                    0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                    1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,

                    1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f,
                    0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,

                    1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f,
                    0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,

                    0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
                    1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,

                    0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f
            };
    float[] rodTextureCoordinates = {

            // front face
            0.0f,0.0f, 1.0f,0.0f, 1.0f,1.0f,
            0.0f,0.0f, 1.0f,1.0f, 0.0f,1.0f,

            // back face
            0.0f,0.0f, 1.0f,1.0f, 1.0f,0.0f,
            0.0f,0.0f, 0.0f,1.0f, 1.0f,1.0f,

            // left face
            0.0f,0.0f, 1.0f,0.0f, 1.0f,1.0f,
            0.0f,0.0f, 1.0f,1.0f, 0.0f,1.0f,

            // right face
            0.0f,0.0f, 1.0f,1.0f, 1.0f,0.0f,
            0.0f,0.0f, 0.0f,1.0f, 1.0f,1.0f,

            // top face
            0.0f,0.0f, 1.0f,0.0f, 1.0f,1.0f,
            0.0f,0.0f, 1.0f,1.0f, 0.0f,1.0f,

            // bottom face
            0.0f,0.0f, 1.0f,1.0f, 0.0f,1.0f,
            0.0f,0.0f, 1.0f,0.0f, 1.0f,1.0f
    };
    // bind and load cube texture coordinates
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[CUBE_TEXTURE_VBO]);
    FloatBuffer cubeTexBuf = Buffers.newDirectFloatBuffer(cubeTextureCoordinates);
    gl.glBufferData(GL_ARRAY_BUFFER, cubeTexBuf.limit()*4, cubeTexBuf, GL_STATIC_DRAW);
    // bind and load pyramid texture coordinates
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[PYRAMID_TEXTURE_VBO]);
    FloatBuffer pTexBuf = Buffers.newDirectFloatBuffer(pyrTextureCoordinates);
    gl.glBufferData(GL_ARRAY_BUFFER, pTexBuf.limit()*4, pTexBuf, GL_STATIC_DRAW);
    // setup rod texture coordinates
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[ROD_TEXTURE_VBO]);
    FloatBuffer rodTexBuf = Buffers.newDirectFloatBuffer(rodTextureCoordinates);
    gl.glBufferData(GL_ARRAY_BUFFER, rodTexBuf.limit()*4, rodTexBuf, GL_STATIC_DRAW);
    // setup icosahedron texture coordimates
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[SHUTTLE_TEXTURE_VBO]);
    FloatBuffer icoTexBuf = Buffers.newDirectFloatBuffer(shuttle.getTexCoords().length * 2);
    unpack(shuttle.getTexCoords(), icoTexBuf);
    icoTexBuf.flip();
    gl.glBufferData(GL_ARRAY_BUFFER, icoTexBuf.limit()*4, icoTexBuf, GL_STATIC_DRAW);
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

            -0.25f, -2.0f,  0.25f,
            0.25f, -2.0f,  0.25f,
            0.25f,  2.0f,  0.25f,

            -0.25f, -2.0f,  0.25f,
            0.25f,  2.0f,  0.25f,
            -0.25f,  2.0f,  0.25f,

            -0.25f, -2.0f, -0.25f,
            0.25f,  2.0f, -0.25f,
            0.25f, -2.0f, -0.25f,

            -0.25f, -2.0f, -0.25f,
            -0.25f,  2.0f, -0.25f,
            0.25f,  2.0f, -0.25f,

            -0.25f, -2.0f, -0.25f,
            -0.25f, -2.0f,  0.25f,
            -0.25f,  2.0f,  0.25f,

            -0.25f, -2.0f, -0.25f,
            -0.25f,  2.0f,  0.25f,
            -0.25f,  2.0f, -0.25f,

            0.25f, -2.0f, -0.25f,
            0.25f,  2.0f,  0.25f,
            0.25f, -2.0f,  0.25f,

            0.25f, -2.0f, -0.25f,
            0.25f,  2.0f, -0.25f,
            0.25f,  2.0f,  0.25f,

            -0.25f,  2.0f, -0.25f,
            -0.25f,  2.0f,  0.25f,
            0.25f,  2.0f,  0.25f,

            -0.25f,  2.0f, -0.25f,
            0.25f,  2.0f,  0.25f,
            0.25f,  2.0f, -0.25f,

            -0.25f, -2.0f, -0.25f,
            0.25f, -2.0f,  0.25f,
            -0.25f, -2.0f,  0.25f,

            -0.25f, -2.0f, -0.25f,
            0.25f, -2.0f, -0.25f,
            0.25f, -2.0f,  0.25f
    };

    gl.glGenVertexArrays(vao.length, vao, 0);
    gl.glBindVertexArray(vao[0]);
    gl.glGenBuffers(vbo.length, vbo, 0);

    // bind and feed in cube positions

    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[CUBE_VERTEX_VBO]);
    FloatBuffer cubeVertBuf = Buffers.newDirectFloatBuffer(cubeVertexPositions);
    gl.glBufferData(GL_ARRAY_BUFFER, cubeVertBuf.limit()*4, cubeVertBuf, GL_STATIC_DRAW);

    // bind and feed in pyramid positions
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[PYRAMID_VERTEX_VBO]);
    FloatBuffer pyramidVertBuf = Buffers.newDirectFloatBuffer(pyramidPositions);
    gl.glBufferData(GL_ARRAY_BUFFER, pyramidVertBuf.limit() * 4, pyramidVertBuf, GL_STATIC_DRAW);

    // bind and feed in rod positions
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[ROD_VERTEX_VBO]);
    FloatBuffer rodVertBuf = Buffers.newDirectFloatBuffer(rodPositions);
    gl.glBufferData(GL_ARRAY_BUFFER, rodVertBuf.limit()*4, rodVertBuf, GL_STATIC_DRAW);

    // bind and feed in icosahedron positions
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[SHUTTLE_VERTEX_VBO]);
    FloatBuffer icoVertBuf = Buffers.newDirectFloatBuffer(shuttle.getNumVertices() * 3);
    unpack(shuttle.getVertices(), icoVertBuf);
    icoVertBuf.flip();
    gl.glBufferData(GL_ARRAY_BUFFER, icoVertBuf.limit()*4, icoVertBuf, GL_STATIC_DRAW);

  }
  private void unpack(Vector3f[] vectorArray, FloatBuffer buffer){
    // unpacks an array of vectors into a float buffer
    for (Vector3f vector : vectorArray){
      vector.get(buffer);
    }
  }
  private void unpack(Vector2f[] vectorArray, FloatBuffer buffer){
    for (Vector2f vector: vectorArray){
      vector.get(buffer);
    }
  }
  public static void main(String[] args) { new Code(); }
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
  public void dispose(GLAutoDrawable drawable) {}

  @Override
  public void keyTyped(KeyEvent e) {

  }

  @Override
  public void keyPressed(KeyEvent e) {
    switch(e.getKeyCode()) {
      case KeyEvent.VK_W -> {
        System.out.println("Move the camera forward!");
        camera.moveForward();
      }
      case KeyEvent.VK_S -> {
        System.out.println("Move the Camera backward!");
        camera.moveBackward();
      }
      case KeyEvent.VK_A -> {
        System.out.println("Move the camera to the left!");
        camera.moveLeft();
      }
      case KeyEvent.VK_D -> {
        System.out.println("Move the camera to the right!");
        camera.moveRight();
      }
      case KeyEvent.VK_Q -> {
        System.out.println("Move the camera up!");
        camera.moveUp();
      }
      case KeyEvent.VK_E -> {
        System.out.println("Move the camera down!");
        camera.moveDown();
      }
      case KeyEvent.VK_LEFT -> {
        System.out.println("Rotate the camera to the left about the V Axis!");
        camera.turnLeft();
      }
      case KeyEvent.VK_RIGHT -> {
        System.out.println("Rotate the camera to the right about the V Axis!");
        camera.turnRight();
      }
      case KeyEvent.VK_UP -> {
        System.out.println("Rotate the camera up about the U Axis!");
        camera.turnUp();
      }
      case KeyEvent.VK_DOWN -> {
        System.out.println("Rotate the camera Down by the U Axis!");
        camera.turnDown();
      }
      case KeyEvent.VK_SPACE -> System.out.println("Toggle the visibility of the world Axes!");
        default -> {
            return;
        }
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {

  }
}
