package com.example.mukesh.rotatingsphere;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class Sphere {
    private final int shaderProgram;
    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    float v[][]={{1.0f,1.0f,1.0f},{-1.0f,-1.0f,1.0f},
            {-1.0f,1.0f,-1.0f}, {1.0f,-1.0f,-1.0f}};
    int n=4;
    int vc1=0;

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 0.0f };
    public Sphere() {

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                10000);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        ByteBuffer bc = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                10000);
        // use the device hardware's native byte order
        bc.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        colorBuffer = bc.asFloatBuffer();
        sphereball(vertexBuffer,colorBuffer,v[0],v[1],v[2],v[3],n);
        System.out.println("count:"+vertexBuffer.capacity());

        vertexBuffer.position(0);
        //System.out.println("count2:"+colorBuffer.capacity());
        colorBuffer.position(0);
        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, vertexShaderCode);
        GLES20.glCompileShader(vertexShader);

        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode);
        GLES20.glCompileShader(fragmentShader);

// Create a program and link the shaders
        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShader);
        GLES20.glAttachShader(shaderProgram, fragmentShader);
        GLES20.glLinkProgram(shaderProgram);

// Use the shader program
        GLES20.glUseProgram(shaderProgram);
    }

    void triangle(FloatBuffer fb, float a[],float b[],float c[])
    {
        fb.put(a);
        fb.put(b);
        fb.put(c);
        vc1+=3;
    }
    void divide_tetra(FloatBuffer fb,FloatBuffer cb,float a[],float b[],float c[],int m)
    {
        float v1[]=new float[3],v2[]=new float[3],v3[]=new float[3];
        int j;
        if(m>0)
        {    /*compute three midpoints*/
            for(j=0;j<3;j++)
                v1[j]=(a[j]+b[j])/2;
            normalize(v1);
            for(j=0;j<3;j++)
                v2[j]=(a[j]+c[j])/2;
            normalize(v2);
            for(j=0;j<3;j++)
                v3[j]=(c[j]+b[j])/2;
            normalize(v3);
            divide_tetra(fb,cb,a,v2,v1,m-1);
            divide_tetra(fb,cb,c,v3,v2,m-1);
            divide_tetra(fb,cb,b,v1,v3,m-1);
            divide_tetra(fb,cb,v1,v2,v3,m-1);
            //cb.put(colors123);
        }
        else {
            triangle(fb, a, b, c);      //draw triangle at end of recursion//
            cb.put((float)(Math.random() * 4.0));  // Use color 0 (red)
            cb.put((float)(Math.random() * 4.0));  // Use color 1 (green)
            cb.put((float)(Math.random() * 4.0));  // Use color 2 (blue)
        }
    }
    void normalize(float p[])
    {
        double d=0.0;
        int i;
        for(i=0;i<3;i++)
            d+=p[i]*p[i];
        d=1.5f*Math.sqrt(d);
        //d=d+(1-d)*MyGLRenderer.getSf();
        if(d>0.0) for(i=0;i<3;i++) p[i]/=d;
    }


    void sphereball(FloatBuffer fb, FloatBuffer cb,float a[],float b[],float c[],float d[],int m)
    {
        normalize(a);
        normalize(b);
        normalize(c);
        normalize(d);
        divide_tetra(fb,cb,a,b,c,m-1);
        divide_tetra(fb,cb,b,c,d,m-1);
        divide_tetra(fb,cb,a,b,d,m-1);
        divide_tetra(fb,cb,a,c,d,m-1);
    }


    String vertexShaderCode =
            "uniform mat4 uMVPMatrix;\n"+
            "attribute vec4 vPosition;\n" +
                    "attribute float vColorIndex;\n" +
                    "varying float fColorIndex;\n" +
                    "void main() {\n" +
                    "    gl_Position = uMVPMatrix * vPosition;\n" +
                    "    fColorIndex = vColorIndex;\n" +
                    "}\n";


    String fragmentShaderCode =
            "precision mediump float;\n" +
                    "varying float fColorIndex;\n" +
                    "uniform vec4 colors[4];\n" +  // 4 colors passed as uniform
                    "void main() {\n" +
                    "    int colorIndex = int(fColorIndex);\n" +  // Cast float to int
                    "    gl_FragColor = colors[colorIndex];\n" + // Use the color index to pick a color
                    "}\n";

    private int mPositionHandle;
    private int colorIndexHandle;
    private int mMVPMatrixHandle;

    private final int vertexStride = 0;//COORDS_PER_VERTEX *4; // 4 bytes per vertex

    public void draw(float[] mvpMatrix) {

        // Add program to OpenGL environment
        GLES20.glUseProgram(shaderProgram);
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        // get handle to fragment shader's vColor member
        colorIndexHandle = GLES20.glGetAttribLocation(shaderProgram, "vColorIndex");
        GLES20.glEnableVertexAttribArray(colorIndexHandle);
        // Set color for drawing the triangle
        GLES20.glVertexAttribPointer(colorIndexHandle, 1,
                GLES20.GL_FLOAT, false,0, colorBuffer);
        // Define the 4 colors (same as in the shader)
        float[] colors = {
                1.0f, 0.0f, 0.0f, 1.0f,  // Red
                0.0f, 1.0f, 0.0f, 1.0f,  // Green
                0.0f, 0.0f, 1.0f, 1.0f,  // Blue
                1.0f, 1.0f, 0.0f, 1.0f   // Yellow
        };

// Pass the color array to the fragment shader as a uniform
        int colorUniformHandle = GLES20.glGetUniformLocation(shaderProgram, "colors");
        GLES20.glUniform4fv(colorUniformHandle, 4, colors, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");
        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vc1);
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(colorIndexHandle);
    }
}


