package com.example.mukesh.seirpinskigasketandroid;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Triangle {
    private final int mProgram;
    private FloatBuffer vertexBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float triangleCoords[] = {   // in counterclockwise order:
            0.0f,  0.622008459f, 0.0f, // top
            -0.5f, -0.311004243f, 0.0f, // bottom left
            0.5f, -0.311004243f, 0.0f  // bottom right
    };
    float v[][]={{-1.0f,-0.5f,0.0f},{1.0f,-0.5f,0.0f},
            {0.0f,1.0f,0.0f}};
    float colors[][]={{1.0f,0.0f,0.0f},{0.0f,1.0f,0.0f},{0.0f,0.0f,1.0f},{0.0f,0.0f,0.0f}};
    public int n=4, prevn=0;
    int vc1=0;
    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    public Triangle() {
        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();
    }
    void triangle(FloatBuffer fb,float a[],float b[],float c[])
    {
        fb.put(a);
        fb.put(b);
        fb.put(c);
        vc1+=3;
    }
    public int findnoofnodes(int nt)
    {
        if(nt<=1)
            return 3;
        else return ((findnoofnodes(nt-1) * 3) - 3);
    }


    void divide_tetra(FloatBuffer fb,float a[],float b[],float c[],int m)
    {
        float v1[]=new float[3],v2[]=new float[3],v3[]=new float[3];
        int j;
        if(m>0)
        {    /*compute three midpoints*/
            for(j=0;j<3;j++)
                v1[j]=(a[j]+b[j])/2;

            for(j=0;j<3;j++)
                v2[j]=(a[j]+c[j])/2;

            for(j=0;j<3;j++)
                v3[j]=(c[j]+b[j])/2;

            divide_tetra(fb,a,v2,v1,m-1);
            divide_tetra(fb,c,v3,v2,m-1);
            divide_tetra(fb,b,v1,v3,m-1);

        }
        else
            triangle(fb, a, b, c);      //draw triangle at end of recursion//
    }


    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";
    private int mPositionHandle;
    private int mColorHandle;

    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX *4; // 4 bytes per vertex
    public void PopulateVBO()
    {
        final int nodesN = findnoofnodes(n+2);
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                (nodesN* 16 ) +100
                //90000
        );
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        //vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        vc1=0;
        divide_tetra(vertexBuffer,v[0],v[1],v[2],n);
        System.out.println("Length of VB"+ vertexBuffer.capacity()+ "VC1"+vc1);
        vertexBuffer.position(0);
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);
    }
    public void draw() {
        if(prevn != n && n > 0)
        {
            prevn = n;
            PopulateVBO();
        }
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,vc1);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
