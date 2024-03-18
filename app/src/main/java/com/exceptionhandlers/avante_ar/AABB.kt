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

import kotlin.math.max
import kotlin.math.min

// An axis-aligned bounding box is defined by the minimum and maximum extends in each dimension.
class AABB {
    var minX = Float.MAX_VALUE
    var minY = Float.MAX_VALUE
    var minZ = Float.MAX_VALUE
    var maxX = -Float.MAX_VALUE
    var maxY = -Float.MAX_VALUE
    var maxZ = -Float.MAX_VALUE
    fun update(x: Float, y: Float, z: Float) {
        minX = min(x.toDouble(), minX.toDouble()).toFloat()
        minY = min(y.toDouble(), minY.toDouble()).toFloat()
        minZ = min(z.toDouble(), minZ.toDouble()).toFloat()
        maxX = max(x.toDouble(), maxX.toDouble()).toFloat()
        maxY = max(y.toDouble(), maxY.toDouble()).toFloat()
        maxZ = max(z.toDouble(), maxZ.toDouble()).toFloat()
    }
}
