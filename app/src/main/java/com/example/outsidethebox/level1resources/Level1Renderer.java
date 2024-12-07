package com.example.outsidethebox.level1resources;

import android.app.Activity;
import android.opengl.GLES30;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;

import gl.Texture;
import gl.Transform;
import gl.renderers.ThirdEyeRenderer;

public class Level1Renderer extends ThirdEyeRenderer {
    Texture cubeTexture;
    Texture cubeGreenTexture;
    ArrayList<Cube> cubes;
    HashSet<Cube> foundCubes;
    Cube rotationMatrixCube;

    int cubeTargetCount = 3;
    float cubeDistance = 20;

    float lookDotError = 0.1f;

    Handler handler;
    Runnable onComplete;
    boolean completed;

    public Level1Renderer(Activity activity, Handler handler, Runnable onComplete){
        super(activity);
        this.onComplete = onComplete;
        this.handler = handler;
    }

    @Override
    public void setup() {
        cubes = new ArrayList<>();
        foundCubes = new HashSet<>();
        cubeTexture = new Texture(getContext(),"box.png");
        cubeGreenTexture = new Texture(getContext(), "box_green.png");

        for(int i = 0; i < cubeTargetCount; i++){
            float xDegrees = (float)Math.random() * 360;
            float yDegrees = (float)Math.random() * 360;
            float zDegrees = (float)Math.random() * 360;

            Cube cube = new Cube();
            cube.setTexture(cubeTexture);
            cube.localTransform.reset();
            cube.localTransform.rotateX(xDegrees);
            cube.localTransform.rotateY(yDegrees);
            cube.localTransform.rotateZ(zDegrees);
            cube.localTransform.translate(0, 0, cubeDistance);

            cubes.add(cube);
        }

        rotationMatrixCube = new Cube();
        rotationMatrixCube.setTexture(cubeTexture);
        rotationMatrixCube.localTransform.translate(0,0, -10);
        rotationMatrixCube.localTransform.updateShader();

        setLightDir(0,-1,-1);
    }

    private double lastTime = 0;
    @Override
    public void simulate(double elapsedDisplayTime) {
        float perSec = (float)(elapsedDisplayTime-lastTime);
        lastTime = elapsedDisplayTime;

        updateRotationTransform(rotationMatrixCube.localTransform);

        for (int i = cubes.size() - 1; i >= 0; i--){
            Cube cube = cubes.get(i);

            float dot = transformForwardDot(rotationMatrixCube.localTransform, cube.localTransform);
            Log.d("Cube" + i, "Cube in range: " + dot);
            if(dot < lookDotError - 1){
                cube.setTexture(cubeGreenTexture);
                foundCubes.add(cube);
                Log.d("Cube" + i, "FOUND CUBE " + i);
            } else {
                if(!foundCubes.contains(cube))
                    cube.setTexture(cubeTexture);
            }

            cube.localTransform.updateShader();
        }

        if(cubeTargetCount == foundCubes.size()){
            completed = true;
            handler.post(onComplete);
        }
    }

    public float transformForwardDot(Transform a, Transform b){
        return a.matrix[8] *  b.matrix[8] + a.matrix[9] * b.matrix[9] + a.matrix[10] * b.matrix[10];
    }

    @Override
    public void draw() {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT| GLES30.GL_DEPTH_BUFFER_BIT);

//        for (int i = 0; i < cubes.size(); i++){
//            cubes.get(i).draw();
//        }

        rotationMatrixCube.draw();
    }
}
