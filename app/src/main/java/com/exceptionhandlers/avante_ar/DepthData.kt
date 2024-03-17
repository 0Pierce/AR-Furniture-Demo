/*
 * Copyright 2021 Google Inc. All Rights Reserved.
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
package com.exceptionhandlers.avante_ar

import android.media.Image
import android.opengl.Matrix
import android.util.Log
import com.google.ar.core.Anchor
import com.google.ar.core.CameraIntrinsics
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.NotYetAvailableException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sqrt

/**
 * Converts depth data from ARCore depth images to 3D pointclouds. Points are added by calling the
 * Raw Depth API, and reprojected into 3D space.
 */
object DepthData {
    const val FLOATS_PER_POINT = 4 // X,Y,Z,confidence.
    fun create(frame: Frame, cameraPoseAnchor: Anchor): FloatBuffer? {
        try {
            val depthImage = frame.acquireRawDepthImage16Bits()
            val confidenceImage = frame.acquireRawDepthConfidenceImage()

            // To transform 2D depth pixels into 3D points we retrieve the intrinsic camera parameters
            // corresponding to the depth image. See more information about the depth values at
            // https://developers.google.com/ar/develop/java/depth/overview#understand-depth-values.
            val intrinsics = frame.getCamera().getTextureIntrinsics()
            val modelMatrix = FloatArray(16)
            cameraPoseAnchor.getPose().toMatrix(modelMatrix, 0)
            val points = convertRawDepthImagesTo3dPointBuffer(
                depthImage, confidenceImage, intrinsics, modelMatrix
            )
            depthImage.close()
            confidenceImage.close()
            return points
        } catch (e: NotYetAvailableException) {
            // This normally means that depth data is not available yet. This is normal so we will not
            // spam the logcat with this.
        }
        return null
    }

    /** Applies camera intrinsics to convert depth image into a 3D pointcloud.  */
    private fun convertRawDepthImagesTo3dPointBuffer(
        depth: Image,
        confidence: Image,
        cameraTextureIntrinsics: CameraIntrinsics,
        modelMatrix: FloatArray
    ): FloatBuffer {
        // Java uses big endian so we have to change the endianess to ensure we extract
        // depth data in the correct byte order.
        val depthImagePlane = depth.planes[0]
        val depthByteBufferOriginal = depthImagePlane.buffer
        val depthByteBuffer = ByteBuffer.allocate(depthByteBufferOriginal.capacity())
        depthByteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        while (depthByteBufferOriginal.hasRemaining()) {
            depthByteBuffer.put(depthByteBufferOriginal.get())
        }
        depthByteBuffer.rewind()
        val depthBuffer = depthByteBuffer.asShortBuffer()
        val confidenceImagePlane = confidence.planes[0]
        val confidenceBufferOriginal = confidenceImagePlane.buffer
        val confidenceBuffer = ByteBuffer.allocate(confidenceBufferOriginal.capacity())
        confidenceBuffer.order(ByteOrder.LITTLE_ENDIAN)
        while (confidenceBufferOriginal.hasRemaining()) {
            confidenceBuffer.put(confidenceBufferOriginal.get())
        }
        confidenceBuffer.rewind()

        // To transform 2D depth pixels into 3D points we retrieve the intrinsic camera parameters
        // corresponding to the depth image. See more information about the depth values at
        // https://developers.google.com/ar/develop/java/depth/overview#understand-depth-values.
        val intrinsicsDimensions = cameraTextureIntrinsics.getImageDimensions()
        val depthWidth = depth.width
        val depthHeight = depth.height
        val fx = cameraTextureIntrinsics.getFocalLength()[0] * depthWidth / intrinsicsDimensions[0]
        val fy = cameraTextureIntrinsics.getFocalLength()[1] * depthHeight / intrinsicsDimensions[1]
        val cx =
            cameraTextureIntrinsics.getPrincipalPoint()[0] * depthWidth / intrinsicsDimensions[0]
        val cy =
            cameraTextureIntrinsics.getPrincipalPoint()[1] * depthHeight / intrinsicsDimensions[1]

        // Allocate the destination point buffer. If the number of depth pixels is larger than
        // `maxNumberOfPointsToRender` we uniformly subsample. The raw depth image may have
        // different resolutions on different devices.
        val maxNumberOfPointsToRender = 20000f
        val step = ceil(sqrt((depthWidth * depthHeight / maxNumberOfPointsToRender).toDouble()))
            .toInt()
        val points = FloatBuffer.allocate(depthWidth / step * depthHeight / step * FLOATS_PER_POINT)
        val pointCamera = FloatArray(4)
        val pointWorld = FloatArray(4)
        var y = 0
        while (y < depthHeight) {
            var x = 0
            while (x < depthWidth) {

                // Depth images are tightly packed, so it's OK to not use row and pixel strides.
                val depthMillimeters =
                    depthBuffer[y * depthWidth + x].toInt() // Depth image pixels are in mm.
                if (depthMillimeters == 0) {
                    // Pixels with value zero are invalid, meaning depth estimates are missing from
                    // this location.
                    x += step
                    continue
                }
                val depthMeters = depthMillimeters / 1000.0f // Depth image pixels are in mm.

                // Retrieves the confidence value for this pixel.
                val confidencePixelValue = confidenceBuffer[y * confidenceImagePlane.rowStride
                        + x * confidenceImagePlane.pixelStride]
                Log.d("depth", confidencePixelValue.toString())
                Log.d("depth", "Enter")
                val confidenceNormalized =
                    (confidencePixelValue.toInt() and 0xff).toFloat() / 255.0f
                if (confidenceNormalized < 0.3 || depthMeters > 1.5) {
                    // Ignores "low-confidence" pixels.
                    x += step
                    continue


                }

                // Unprojects the depth into a 3D point in camera coordinates.
                pointCamera[0] = depthMeters * (x - cx) / fx
                pointCamera[1] = depthMeters * (cy - y) / fy
                pointCamera[2] = -depthMeters
                pointCamera[3] = 1f

                // Applies model matrix to transform point into world coordinates.
                Matrix.multiplyMV(pointWorld, 0, modelMatrix, 0, pointCamera, 0)
                points.put(pointWorld[0]) // X.
                points.put(pointWorld[1]) // Y.
                points.put(pointWorld[2]) // Z.
                points.put(confidenceNormalized)
                x += step
            }
            y += step
        }
        points.rewind()
        return points
    }

    fun filterUsingPlanes(points: FloatBuffer, allPlanes: Collection<Plane>) {
        val planeNormal = FloatArray(3)

        // Allocates the output buffer.
        val numPoints = points.remaining() / FLOATS_PER_POINT

        // Each plane is checked against each point.
        for (plane in allPlanes) {
            if (plane.trackingState != TrackingState.TRACKING || plane.getSubsumedBy() != null) {
                continue
            }

            // Computes the normal vector of the plane.
            val planePose = plane.getCenterPose()
            planePose.getTransformedAxis(1, 1.0f, planeNormal, 0)

            // Filters points that are too close to the plane.
            for (index in 0 until numPoints) {
                // Retrieves the next point.
                val x = points[FLOATS_PER_POINT * index]
                val y = points[FLOATS_PER_POINT * index + 1]
                val z = points[FLOATS_PER_POINT * index + 2]

                // Transforms point to be in world coordinates, to match plane info.
                val distance =
                    (x - planePose.tx()) * planeNormal[0] + (y - planePose.ty()) * planeNormal[1] + (z - planePose.tz()) * planeNormal[2]

                // Controls the size of objects detected.
                // Smaller values mean smaller objects will be kept.
                // Larger values will only allow detection of larger objects, but also helps reduce noise.
                if (abs(distance.toDouble()) > 0.03) {
                    continue  // Keeps this point, since it's far enough away from the plane.
                }

                // Invalidates points that are too close to planar surfaces.
                points.put(FLOATS_PER_POINT * index, 0f)
                points.put(FLOATS_PER_POINT * index + 1, 0f)
                points.put(FLOATS_PER_POINT * index + 2, 0f)
                points.put(FLOATS_PER_POINT * index + 3, 0f)
            }
        }
    }
}
