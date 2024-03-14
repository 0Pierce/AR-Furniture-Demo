import kotlin.math.abs

//going to try making a modified oct-tree for confidence points
//was originally going for a kd-tree, but the oct-tree would probably be less confusing to use
//the largest unit will be 1m blocks

internal class Point(x1: Double, y1: Double, z1: Double, c: Double) {
    protected var x : Double = x1
    protected var y : Double = y1
    protected var z : Double = z1
    protected var confidence : Double = c

    fun getCoords(): Triple<Double, Double, Double> {
        return Triple(this.x, this.y, this.z)
    }
    fun getConf(): Double {
        return this.confidence
    }
    fun compareCoords(p2: Point, res: Double): Boolean {
        return (abs(this.x-p2.x) <= res && abs(this.y-p2.y) <= res && abs(this.z-p2.z) <= res)
    }
    fun compareConf(p2: Point, res: Double) : Boolean {
        return abs(this.confidence-p2.confidence) <= res
    }
}

internal class Chunk(sl: Double, og: Triple<Double, Double, Double>, res: Double, cres: Double){
    protected var size = 0 //number of points

    protected var leaf = true //will always be a leaf on init, until split further

    protected var sideLength : Double = sl
    protected var origin: Triple<Double, Double, Double> = og // coordinate with the least x/y/z possible in a chunk

    protected var elements = ArrayList<Point>() //always keeps track of what elements are inside

    protected var normConfidence = 0 //average of all contained confidence points
    protected var nc_updated = true //we will use lazy updating, if it isn't updated, it will be updated when the user next wants this value

    protected var confidenceDensity = 0 //sum of all confidence points divided by the volume of the chunk
    protected var cd_updated = true //on init, the values ARE updated, as there are no points inside currently

    protected var octants : Array<Array<Chunk?>> = arrayOf(arrayOf(null, null), arrayOf(null, null), arrayOf(null, null), arrayOf(null, null)) //sub-octants, if any

    protected var resolution : Double = res //if we compare 2 points, how close is close enough?
    protected var confres : Double = cres //if we compare 2 points by confidence, how close is close enough?

    fun insert(p: Point) {

    }

    fun delete(p: Point) { //delete by confidence AND point compare

    }
    protected fun split() { //initializes sub-quadrants, and inserts existing values into them

    }
}

class ConfidenceTree {
    private var meterChunks = HashMap<Triple<Int, Int, Int>, Chunk>() //chunks in this outer layer are a meter cube
}