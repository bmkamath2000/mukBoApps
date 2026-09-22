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
    int vertexCount=0;
    int colorIndex = 0;
    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 0.0f };


    public Sphere() {
        int trianglesPerFace = (int)Math.pow(4, n - 1);
        int triangleCount = 4 * trianglesPerFace;
        int vertexCount = triangleCount * 3;
        ByteBuffer bb = ByteBuffer.allocateDirect(
                vertexCount * COORDS_PER_VERTEX * 4
        );
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();

        ByteBuffer bc = ByteBuffer.allocateDirect(
                vertexCount * 4 * Float.BYTES
        );
        bc.order(ByteOrder.nativeOrder());
        colorBuffer = bc.asFloatBuffer();

        sphereball(vertexBuffer,colorBuffer,v[0],v[1],v[2],v[3],n);
        System.out.println("count:"+vertexBuffer.capacity());

        vertexBuffer.position(0);
        System.out.println("count2:"+colorBuffer.capacity());
        colorBuffer.position(0);
        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, vertexShaderCode);
        GLES20.glCompileShader(vertexShader);
        int[] compiled = new int[1];

        GLES20.glGetShaderiv(
                vertexShader,
                GLES20.GL_COMPILE_STATUS,
                compiled,
                0
        );

        if (compiled[0] == 0) {
            System.out.println(
                    GLES20.glGetShaderInfoLog(vertexShader)
            );
        }
        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode);
        GLES20.glCompileShader(fragmentShader);
        compiled = new int[1];

        GLES20.glGetShaderiv(
                fragmentShader,
                GLES20.GL_COMPILE_STATUS,
                compiled,
                0
        );

        if (compiled[0] == 0) {
            System.out.println(
                    GLES20.glGetShaderInfoLog(fragmentShader)
            );
        }
// Create a program and link the shaders
        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShader);
        GLES20.glAttachShader(shaderProgram, fragmentShader);
        GLES20.glLinkProgram(shaderProgram);

// Use the shader program
        GLES20.glUseProgram(shaderProgram);
    }

    void triangle(FloatBuffer fb, FloatBuffer cb,
                  float a[], float b[], float c[]) {

        fb.put(a);
        fb.put(b);
        fb.put(c);

        float[] currentColor;

        switch (colorIndex) {
            case 0:
                currentColor = new float[]{1, 0, 0, 1};
                break;
            case 1:
                currentColor = new float[]{0, 1, 0, 1};
                break;
            case 2:
                currentColor = new float[]{0, 0, 1, 1};
                break;
            default:
                currentColor = new float[]{1, 1, 0, 1};
                break;
        }

        cb.put(currentColor);
        cb.put(currentColor);
        cb.put(currentColor);

        vertexCount += 3;
        colorIndex = (colorIndex + 1) % 4;
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

        }
        else {
            triangle(fb, cb, a, b, c);      //draw triangle at end of recursion//
            System.out.print(colorIndex);
        }
    }
    void normalize(float p[])
    {
        double d=0.0;
        int i;
        for(i=0;i<3;i++)
            d+=p[i]*p[i];
        d = 1.5f * Math.sqrt(d);
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
            "uniform mat4 uMVPMatrix;\n" +
                    "attribute vec4 vPosition;\n" +
                    "attribute vec4 vColor;\n" +
                    "varying vec4 fColor;\n" +
                    "void main() {\n" +
                    "    gl_Position = uMVPMatrix * vPosition;\n" +
                    "    fColor = vColor;\n" +
                    "}\n";

    String fragmentShaderCode =
            "precision mediump float;\n" +
                    "varying vec4 fColor;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = fColor;\n" +
                    "}\n";

    private int mPositionHandle;
    private int colorIndexHandle;
    private int mMVPMatrixHandle;

    private final int vertexStride = 0;//COORDS_PER_VERTEX *4; // 4 bytes per vertex

    public void draw(float[] mvpMatrix) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
//        GLES20.glEnable(GLES20.GL_CULL_FACE);
//        GLES20.glCullFace(GLES20.GL_BACK);
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
        int colorHandle =
                GLES20.glGetAttribLocation(shaderProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glVertexAttribPointer(colorIndexHandle, 1,
                GLES20.GL_FLOAT, false,0, colorBuffer);


        GLES20.glEnableVertexAttribArray(colorHandle);

        GLES20.glVertexAttribPointer(
                colorHandle,
                4,
                GLES20.GL_FLOAT,
                false,
                0,
                colorBuffer
        );

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");
        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(colorIndexHandle);
    }
}


