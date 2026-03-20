package a2;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Camera {
    private Vector3f U, V, N;


    private float cameraX, cameraY, cameraZ;
    private final float translationStep;
    private final float rotationStep;


    public Camera(float translationStep, float rotationStep){
        this.rotationStep = rotationStep;
        this.U = new Vector3f();
        this.V = new Vector3f();
        this.N = new Vector3f();
        this.U.set(1.0f, 0.0f, 0.0f);
        this.V.set(0.0f, 1.0f, 0.0f);
        this.N.set(0.0f, 0.0f, -1.0f);
        this.translationStep = translationStep;
    }
    public Camera(float translationStep, float rotationStep, float initialX, float initialY, float initialZ){
        this.translationStep = translationStep;
        this.rotationStep = rotationStep;
        this.cameraX = initialX;
        this.cameraY = initialY;
        this.cameraZ = initialZ;
        this.U = new Vector3f();
        this.V = new Vector3f();
        this.N = new Vector3f();
        this.U.set(1.0f, 0.0f, 0.0f);
        this.V.set(0.0f, 1.0f, 0.0f);
        this.N.set(0.0f, 0.0f, -1.0f);
    }
    public Matrix4f getRotationMatrix(){
        Vector4f col1 = new Vector4f(this.U.x(), this.V.x(), -this.N.x(), 0.0f);
        Vector4f col2 = new Vector4f(this.U.y(), this.V.y(), -this.N.y(), 0.0f);
        Vector4f col3 = new Vector4f(this.U.z(), this.V.z(), -this.N.z(), 0);
        Vector4f col4 = new Vector4f(0,0,0,1);
        return new Matrix4f(col1, col2, col3, col4);
    }
    // functions for moving the camera
    public void moveForward(){ // todo clean up
        // moves the camera forward by the step amount, along the N-Axis of the camera.
        Vector3f oldLocation = new Vector3f(cameraX, cameraY, cameraZ);
        Vector3f copyOfN = new Vector3f(this.N.x(), this.N.y(), this.N.z());
        Vector3f newLocation = oldLocation.add(copyOfN.mul(translationStep));
        this.cameraX = newLocation.x();
        this.cameraY = newLocation.y();
        this.cameraZ = newLocation.z();
    }
    public void moveBackward(){
        // moves the camera backward along the N-axis of the camera by the step amount
        Vector3f oldLocation = new Vector3f(cameraX, cameraY, cameraZ);
        Vector3f copyOfN = new Vector3f(this.N.x(), this.N.y(), this.N.z());
        Vector3f newLocation = oldLocation.sub(copyOfN.mul(translationStep));
        this.cameraX = newLocation.x();
        this.cameraY = newLocation.y();
        this.cameraZ = newLocation.z();
    }
    public void moveRight(){
        // move the camera to the right along the U-axis of the camera by the step amount
        Vector3f oldLocation = new Vector3f(cameraX, cameraY, cameraZ);
        Vector3f copyOfU = new Vector3f(this.U.x(), this.U.y(), this.U.z());
        Vector3f newLocation = oldLocation.add(copyOfU.mul(translationStep));
        this.cameraX = newLocation.x();
        this.cameraY = newLocation.y();
        this.cameraZ = newLocation.z();
    }
    public void moveLeft(){
        Vector3f oldLocation = new Vector3f(cameraX, cameraY, cameraZ);
        Vector3f copyofU = new Vector3f(this.U.x(), this.U.y(), this.U.z());
        Vector3f newLocation = oldLocation.sub(copyofU.mul(translationStep));
        this.cameraX = newLocation.x();
        this.cameraY = newLocation.y();
        this.cameraZ = newLocation.z();
    }
    public void moveUp(){
        // move the camera up along the V axis of the camera by the step amount
        Vector3f oldLocation = new Vector3f(cameraX, cameraY, cameraZ);
        Vector3f copyofV = new Vector3f(this.V.x(), this.V.y(), this.V.z());
        Vector3f newLocation = oldLocation.add(copyofV.mul(translationStep));
        this.cameraX = newLocation.x();
        this.cameraY = newLocation.y();
        this.cameraZ = newLocation.z();
    }
    public void moveDown(){
        // mvoe the camera up along the V axis of the camera by the step amount
        Vector3f oldLocation = new Vector3f(cameraX, cameraY, cameraZ);
        Vector3f copyOfV = new Vector3f(this.V.x(), this.V.y(), this.V.z());
        Vector3f newLocation = oldLocation.sub(copyOfV.mul(translationStep));
        this.cameraX = newLocation.x();
        this.cameraY = newLocation.y();
        this.cameraZ = newLocation.z();
    }

    // functions for turning the camera
    public void turnRight(){
        this.U.rotateAxis(rotationStep, this.V.x(), this.V.y(), this.V.z());
        this.N.rotateAxis(rotationStep, this.V.x(), this.V.y(), this.V.z());
    }
    public void turnLeft(){
        this.U.rotateAxis(-1.0f * rotationStep, this.V.x(), this.V.y(), this.V.z());
        this.N.rotateAxis(-1.0f * rotationStep, this.V.x(), this.V.y(), this.V.z());
    }
    public void turnUp(){
        this.V.rotateAxis(rotationStep, this.U.x(), this.U.y(), this.U.z());
        this.N.rotateAxis(rotationStep, this.U.x(), this.U.y(), this.U.z());
    }
    public void turnDown(){
        this.V.rotateAxis(-1.0f * rotationStep, this.U.x(), this.U.y(), this.U.z());
        this.N.rotateAxis(-1.0f * rotationStep, this.U.x(), this.U.y(), this.U.z());
    }
    public void roll(){
        this.U.rotateAxis(rotationStep, this.N.x(), this.N.y(), this.N.z());
        this.V.rotateAxis(rotationStep, this.N.x(), this.N.y(), this.N.z());
    }

    public float getCameraX() {
        return cameraX;
    }

    public float getCameraY() {
        return cameraY;
    }

    public float getCameraZ() {
        return cameraZ;
    }

}
