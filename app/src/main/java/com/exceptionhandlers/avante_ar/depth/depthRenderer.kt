package com.exceptionhandlers.avante_ar.depth


import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import com.google.ar.core.Camera
import java.io.IOException
import java.nio.FloatBuffer

class DepthRenderer {
    private val TAG: String = DepthRenderer::class.java.getSimpleName()

    // Shader names.
    private val VERTEX_SHADER_NAME = "shaders/depth_point_cloud.vert"
    private val FRAGMENT_SHADER_NAME = "shaders/depth_point_cloud.frag"

    val BYTES_PER_FLOAT = java.lang.Float.SIZE / 8
    private val BYTES_PER_POINT = BYTES_PER_FLOAT * DepthData.FLOATS_PER_POINT
    private val INITIAL_BUFFER_POINTS = 1000

    private var arrayBuffer = 0
    private var arrayBufferSize = 0

    private var programName = 0
    private var positionAttribute = 0
    private var modelViewProjectionUniform = 0
    private var pointSizeUniform = 0

    private var numPoints = 0

    fun DepthRenderer() {}

    @Throws(IOException::class)
    fun createOnGlThread(context: Context?) {
        ShaderUtil.checkGLError(TAG, "Bind")
        val buffers = IntArray(1)
        GLES20.glGenBuffers(1, buffers, 0)
        arrayBuffer = buffers[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, arrayBuffer)
        arrayBufferSize = INITIAL_BUFFER_POINTS * BYTES_PER_POINT
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, arrayBufferSize, null, GLES20.GL_DYNAMIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        ShaderUtil.checkGLError(TAG, "Create")
        val vertexShader: Int =
            ShaderUtil.loadGLShader(TAG, context, GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_NAME)
        val fragmentShader: Int =
            ShaderUtil.loadGLShader(TAG, context, GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_NAME)
        programName = GLES20.glCreateProgram()
        GLES20.glAttachShader(programName, vertexShader)
        GLES20.glAttachShader(programName, fragmentShader)
        GLES20.glLinkProgram(programName)
        GLES20.glUseProgram(programName)
        ShaderUtil.checkGLError(TAG, "Program")
        positionAttribute = GLES20.glGetAttribLocation(programName, "a_Position")
        modelViewProjectionUniform =
            GLES20.glGetUniformLocation(programName, "u_ModelViewProjection")
        // Sets the point size, in pixels.
        pointSizeUniform = GLES20.glGetUniformLocation(programName, "u_PointSize")
        ShaderUtil.checkGLError(TAG, "Init complete")
    }

    /**
     * Update the OpenGL buffer contents to the provided point. Repeated calls with the same point
     * cloud will be ignored.
     */
    fun update(points: FloatBuffer?) {
        ShaderUtil.checkGLError(TAG, "Update")
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, arrayBuffer)

        // If the array buffer is not large enough to fit the new point cloud, resize it.
        if (points != null) {
            points.rewind()
        }
        if (points != null) {
            numPoints = points.remaining() / DepthData.FLOATS_PER_POINT
        }
        if (numPoints * BYTES_PER_POINT > arrayBufferSize) {
            while (numPoints * BYTES_PER_POINT > arrayBufferSize) {
                arrayBufferSize *= 2
            }
            GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER,
                arrayBufferSize,
                null,
                GLES20.GL_DYNAMIC_DRAW
            )
        }
        GLES20.glBufferSubData(
            GLES20.GL_ARRAY_BUFFER, 0, numPoints * BYTES_PER_POINT, points
        )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        ShaderUtil.checkGLError(TAG, "Update complete")
    }

    /** Render the point cloud. The ARCore point cloud is given in world space.  */
    fun draw(camera: Camera) {
        val projectionMatrix = FloatArray(16)
        camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f)
        val viewMatrix = FloatArray(16)
        camera.getViewMatrix(viewMatrix, 0)
        val viewProjection = FloatArray(16)
        Matrix.multiplyMM(viewProjection, 0, projectionMatrix, 0, viewMatrix, 0)
        ShaderUtil.checkGLError(TAG, "Draw")
        GLES20.glUseProgram(programName)
        GLES20.glEnableVertexAttribArray(positionAttribute)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, arrayBuffer)
        GLES20.glVertexAttribPointer(
            positionAttribute,
            4,
            GLES20.GL_FLOAT,
            false,
            BYTES_PER_POINT,
            0
        )
        GLES20.glUniformMatrix4fv(modelViewProjectionUniform, 1, false, viewProjection, 0)
        // Set point size to 5 pixels.
        GLES20.glUniform1f(pointSizeUniform, 9.0f)
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, numPoints)
        GLES20.glDisableVertexAttribArray(positionAttribute)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        ShaderUtil.checkGLError(TAG, "Draw complete")
    }

}