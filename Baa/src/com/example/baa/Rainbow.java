/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.baa;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Calendar;

import android.opengl.GLES20;

/**
 * A two-dimensional rectangle for use as a drawn object in OpenGL ES 2.0.
 */
public class Rainbow {

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
            "uniform float time;" +
            "attribute vec4 vPosition;" +
            "attribute vec2 texCoords;" +
            "varying vec2 texCoordinates;" +
            "void main() {" +
            // The matrix must be included as a modifier of gl_Position.
            // Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            "float amplitude;" +
            "vec4 aPosition;" +
 //           "amplitude = vPosition.y;" +
    		"if (vPosition.y >= 0)" +
    			"amplitude = 1.7 - vPosition.y;" +
    		"else amplitude = 1.7 + vPosition.y;" +
    		"highp int index = int(amplitude * 10);" +
    		"amplitude = amplitude * 10 - index;" +
    		"aPosition = vPosition;" +
    		"aPosition.x = vPosition.x + amplitude / 10 * sin(time);" +  		
            " texCoordinates = texCoords;" +
            "gl_Position = uMVPMatrix * aPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "uniform sampler2D tex;" +
            "varying vec2 texCoordinates;" +
            "void main() {" +
            "  gl_FragColor = texture2D(tex, texCoordinates);" +
            "}";

    private final FloatBuffer vertexBuffer;
    private final FloatBuffer textureBuffer;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    float rectangleCoords[];
    
    static final int TEXCOORDS_PER_VERTEX = 2;
    static float textureCoords[];

    short drawOrder[]; // order to draw vertices

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int textureStride = TEXCOORDS_PER_VERTEX * 4; // 4 bytes per vertex

    float color[] = { 0.5f, 0.0f, 0.0f, 1.0f };

	private int mTextureHandle;

	private int mTextureUniformHandler;

	private int texData;
	
	public void generateVertices(int n) {
		rectangleCoords = new float[3 * (2 * n + 2)];
		drawOrder = new short[2 * n * 3];
		textureCoords = new float[4 * n + 4];
	
		for (int i = 0; i < 3 * (2 * n + 2); i += 6) {
			rectangleCoords[i] = -0.15f;
			rectangleCoords[i + 1] = 1.7f - i / 6 * 3.4f / n;
			rectangleCoords[i + 2] = 0f;
			rectangleCoords[i + 3] = 0.15f;
			rectangleCoords[i + 4] = 1.7f - i / 6 * 3.4f / n;
			rectangleCoords[i + 5] = 0f;
		}
		 
		for (int i = 0, j = 0; i < 2 * 3 * n; i += 6, j += 2) {
			drawOrder[i] = (short)j; 
			drawOrder[i + 1] = (short)(j + 1);
			drawOrder[i + 2] = (short)(j + 2);
			drawOrder[i + 3] = (short)(j + 2);
			drawOrder[i + 4] = (short)(j + 1);
			drawOrder[i + 5] = (short)(j + 3);
		}
		
		for (int i = 0; i < 4 * n + 4; i += 4) {
			textureCoords[i] = 0.0f;
			textureCoords[i + 1] = 1.0f - (float)i / (4 * n);
			textureCoords[i + 2] = 1.0f;
			textureCoords[i + 3] = 1.0f - (float)i / (4 * n);
		}
	}

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Rainbow() {
    	generateVertices(20);
    	
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 4 bytes per float)
                rectangleCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(rectangleCoords);
        vertexBuffer.position(0);
        
        // initialize texture byte buffer for shape coordinates
        ByteBuffer tbb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 4 bytes per float)
                textureCoords.length * 4);
        tbb.order(ByteOrder.nativeOrder());
        textureBuffer = tbb.asFloatBuffer();
        textureBuffer.put(textureCoords);
        textureBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
        
        texData = TextureHelper.loadTexture(MultiplayerSurfaceView.context, R.drawable.rainbow);

        // prepare shaders and OpenGL program
        int vertexShader = MenuRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MenuRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);
        
        // load the texture
        mTextureUniformHandler = GLES20.glGetUniformLocation(mProgram, "tex");
        
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texData);
        
        GLES20.glUniform1i(mTextureUniformHandler, 0);

        int timeHandler = GLES20.glGetUniformLocation(mProgram, "time");
        
        Calendar c = Calendar.getInstance();
        float seconds = c.get(Calendar.MILLISECOND);
        GLES20.glUniform1f(timeHandler, seconds / 100);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        
       //get handle to vertex shader's texCoords member
       mTextureHandle = GLES20.glGetAttribLocation(mProgram, "texCoords");
       
       // Enable a handle to the triangle vertices
       GLES20.glEnableVertexAttribArray(mTextureHandle);

       // Prepare the triangle coordinate data
       GLES20.glVertexAttribPointer(
               mTextureHandle, TEXCOORDS_PER_VERTEX,
               GLES20.GL_FLOAT, false,
               textureStride, textureBuffer);
      

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MenuRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MenuRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the rectangle
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

}