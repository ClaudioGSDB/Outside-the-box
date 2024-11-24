package com.example.outsidethebox.level1resources;

import gl.modeltypes.ShadedTexturedModel;

public class Pyramid extends ShadedTexturedModel {

    Pyramid(){
        super();

        setXYZ(new float[]{
                1, 0, 1, // Front right vertex of base
                -1, 0, 1, // Front left vertex of base
                -1, 0, -1, // back left vertex of base
                1, 0, -1, // back right vertex of base
                0, 2, 0, // peak of the pyramid
        });


        setTriangles(new short[]{
                0, 4, 1, // front
                0, 3, 4, // right
                3, 2, 4, // back
                1, 4, 2, // left
                1, 2, 0, // base1
                2, 3, 0, // base2
        });

        setUV(new float[]{
                1, 0,
                0, 0,
                0, 1,
                1, 1,
                .5f, .5f
        });

        setNormals(new float[]{
                1, 0, 1,
                -1, 0, 1,
                -1, 0, -1,
                1, 0, -1,
                0, 1, 0,
        });
    }
}
