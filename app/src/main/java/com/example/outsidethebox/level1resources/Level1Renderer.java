package com.example.outsidethebox.level1resources;

import android.app.Activity;
import android.opengl.GLES30;

import gl.Texture;
import gl.renderers.ThirdEyeRenderer;

public class Level1Renderer extends ThirdEyeRenderer {

    Texture pyramid_texture;
    Pyramid my_pyramid;

    public Level1Renderer(Activity activity){
        super(activity);
    }

    @Override
    public void setup() {

        pyramid_texture = new Texture(getContext(),"pyramid_texture.png");

        my_pyramid = new Pyramid();
        my_pyramid.setTexture(pyramid_texture);
        my_pyramid.localTransform.translate(0,0, -10);

        setLightDir(0,-1,-1);
    }

    private double lastTime = 0;
    @Override
    public void simulate(double elapsedDisplayTime) {
        float perSec=(float)(elapsedDisplayTime-lastTime);
        lastTime=elapsedDisplayTime;

        my_pyramid.localTransform.rotateX(1*perSec);
        my_pyramid.localTransform.rotateZ(1*perSec);
        my_pyramid.localTransform.rotateY(20*perSec);
        my_pyramid.localTransform.updateShader();
    }

    @Override
    public void draw() {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT| GLES30.GL_DEPTH_BUFFER_BIT);

        my_pyramid.draw();
    }
}
