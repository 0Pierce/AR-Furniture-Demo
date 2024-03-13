//going to try making a modified quadrant tree for confidence points
//was originally going for a kd-tree, but the quadrant tree would probably be less confusing to use
//the largest unit will be 1m blocks

internal class Point {
    val x : Double
    val y : Double
    val z : Double
    val confidence : Double

    class Point constructor(var x1: Double, var y1: Double, var z1: Double, var c: Double) {
        init {
            this.x = x1
            this.y = y1
            this.z = z1
            this.confidence = c
        }
    }
    fun getCoords(): Triple<Double> {
        return Triple(this.x, this.y, this.z)
    }
    fun getConf(): Double {
        return this.confidence
    }
    fun compareCoords(var p2: Point, var res: Double) {
        return (abs(this.x-p2.x) <= res && abs(this.y-p2.y) <= res && abs(this.z-p2.z) <= res)
    }
    fun compareConf(var p2: Point, var res: Double) {
        return abs(this.confidence-p2.confidence) <= res
    }
}

internal class Chunk {
    protected val size = 0 //number of points

    protected val leaf = true //will always be a leaf on init, until split further

    protected val sideLength : Double

    protected val elements : Vector<Point> //always keeps track of what elements are inside

    protected val normConfidence = 0 //average of all contained confidence points
    protected val nc_updated = true //we will use lazy updating, if it isn't updated, it will be updated when the user next wants this value

    protected val confidenceDensity = 0 //sum of all confidence points divided by the volume of the chunk
    protected val cd_updated = true //on init, the values ARE updated, as there are no points inside currently

    protected val quadrants : Array<Array<Chunk>> = arrayOf(arrayOf(Null, Null), arrayOf(Null, Null), arrayOf(Null, Null), arrayOf(Null, Null)) //sub-quadrants, if any

    protected val resolution : Double //if we compare 2 points, how close is close enough?
    protected val confres : Double //if we compare 2 points by confidence, how close is close enough?


    class Chunk constructor(var sl: Double, var res: Double) {
        init {
            this.sideLength = sl
        }
    }

    fun insert(var p: Point) {

    }

    fun delete(var p: Point) { //delete by confidence AND point compare

    }
    protected fun split() { //initializes sub-quadrants, and inserts existing values into them

    }
}

class ConfidenceTree {
    protected val meterChunks = LinkedHashMap<Triple<Int, Int, Int>, Chunk>() //chunks in this outer layer are a meter cube
}