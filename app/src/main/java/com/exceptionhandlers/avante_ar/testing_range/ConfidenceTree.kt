import java.util.PriorityQueue
import kotlin.math.abs

//going to try making a modified oct-tree for confidence points
//was originally going for a kd-tree, but the oct-tree would probably be less confusing to use
//the largest unit will be 1m blocks

internal class Point(x1: Double, y1: Double, z1: Double, c: Double) : Comparable<Point> {
    private var x : Double = x1
    private var y : Double = y1
    private var z : Double = z1
    private var confidence : Double = c

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
    override fun compareTo(other: Point): Int = abs(confidence).compareTo(abs(other.confidence)) //allows ordering by absolute confidence (no matter the sign, larger absolute value means more sure)
                                                                                                 //lowest AC points go FIRST
}

internal class Chunk(sl: Double, og: Triple<Double, Double, Double>, res: Double, cres: Double, ma: Int){
    private var size = 0 //number of points

    private var leaf = true //will always be a leaf on init, until split further

    private var sideLength : Double = sl
    private var origin: Triple<Double, Double, Double> = og // coordinate with the least x/y/z possible in a chunk

    private var elements = PriorityQueue<Point>() //always keeps track of what elements are inside
                                                    //optimized for culling of low-confidence points
                                                    //is iterable, so can be used like a list when needed
    private var max_allowed : Int = ma //optimization to prevent too many levels: how many elements are allowed in a node before it is split

    private var normConfidence = 0.0 //average of all contained confidence points
    private var nc_updated = true //we will use lazy updating, if it isn't updated, it will be updated when the user next wants this value

    private var confidenceDensity = 0.0 //sum of all confidence points divided by the volume of the chunk
    private var cd_updated = true //on init, the values ARE updated, as there are no points inside currently

    private var octants : Array<Array<Array<Chunk?>>> = arrayOf(arrayOf(arrayOf(null, null), arrayOf(null, null)), arrayOf(arrayOf(null, null), arrayOf(null, null))) //sub-octants, if any

    private var resolution : Double = res //if we compare 2 points, how close is close enough?
    private var confres : Double = cres //if we compare 2 points by confidence, how close is close enough?
    fun size(): Int = size
    fun insert(p: Point) {
        size++
        nc_updated = false
        cd_updated = false
        elements.add(p)
        if(size > max_allowed && leaf) { //call split()
            split()
        } else if(size > max_allowed) { //just add to the child octant nodes, they were already initialized
            //get which octant the point is in
            val pc = p.getCoords()
            val xc = ((pc.first-origin.first)*2/sideLength).toInt()
            val yc = ((pc.first-origin.first)*2/sideLength).toInt()
            val zc = ((pc.second-origin.second)*2/sideLength).toInt()
            //add
            octants[xc][yc][zc]!!.insert(p)
        }
    }
    fun cull(n: Int) { //cull a certain number
        val octcull = arrayOf(arrayOf(arrayOf(0, 0), arrayOf(0, 0)), arrayOf(arrayOf(0, 0), arrayOf(0, 0))) //how many should the child nodes cull each?
        for(i in 1..n) {
            if(elements.isEmpty()) break
            size--
            val cpoint = elements.peek()
            val pc = cpoint!!.getCoords() //man wtf are these operators
            val xc = ((pc.first-origin.first)*2/sideLength).toInt()
            val yc = ((pc.first-origin.first)*2/sideLength).toInt()
            val zc = ((pc.second-origin.second)*2/sideLength).toInt()
            octcull[xc][yc][zc]++
            elements.poll()
        }
        if(size <= max_allowed) { //if we have managed to cull this node small enough to do away with child nodes, we just delete them and revert to a leaf
            for(i in 0..1) {
                for(j in 0..1) {
                    for(k in 0..1) {
                        octants[i][j][k]= null
                    }
                }
            }
            leaf = true
        }
        if(leaf) return //no need to cull child nodes if there arent any
        for(i in 0..1) {
            for(j in 0..1) {
                for(k in 0..1) {
                    octants[i][j][k]!!.cull(octcull[i][j][k])
                }
            }
        }
    }
    fun cullTo(n: Int) { //cull to a target population
        cull(size-n)
    }
    //it is not expected to use delete very often, it's only included in case points were added that were faulty
    //as such this method has a pretty bad time complexity
    //use cull instead for deleting low confidence points
    fun delete(p: Point) { //delete by confidence AND point compare
        val templist = ArrayList<Point>()
        var found = false
        while(!elements.isEmpty()) {
            val p2 = elements.peek()!!
            elements.poll()
            if(p.compareConf(p2, confres) && p.compareCoords(p2, resolution)) {
                found = true
            } else {
                templist.add(p2)
            }
        }
        while(templist.isNotEmpty()) {
            elements.add(templist.removeAt(0))
        }
        if(!found || leaf) return
        val pc = p.getCoords() //man wtf are these operators
        val xc = ((pc.first-origin.first)*2/sideLength).toInt()
        val yc = ((pc.first-origin.first)*2/sideLength).toInt()
        val zc = ((pc.second-origin.second)*2/sideLength).toInt()
        octants[xc][yc][zc]?.delete(p)
    }
    fun deletePC(p: Point) { //delete by point compare only
        val templist = ArrayList<Point>()
        var found = false
        while(!elements.isEmpty()) {
            val p2 = elements.peek()!!
            elements.poll()
            if(p.compareConf(p2, confres) && p.compareCoords(p2, resolution)) {
                found = true
            } else {
                templist.add(p2)
            }
        }
        while(templist.isNotEmpty()) {
            elements.add(templist.removeAt(0))
        }
        if(!found || leaf) return
        val pc = p.getCoords() //man wtf are these operators
        val xc = ((pc.first-origin.first)*2/sideLength).toInt()
        val yc = ((pc.first-origin.first)*2/sideLength).toInt()
        val zc = ((pc.second-origin.second)*2/sideLength).toInt()
        octants[xc][yc][zc]?.deletePC(p)
    }
    private fun split() { //initializes sub-quadrants, and inserts existing values into them
        leaf = false
        //init subquadrants
        for(i in 0..1) {
            for(j in 0..1) {
                for(k in 0..1) {
                    val nsl = sideLength/2.0
                    val nog = Triple<Double, Double, Double>(origin.first+(i*1.0)*sideLength/2.0, origin.second+(j*1.0)*sideLength/2.0, origin.third+(k*1.0)*sideLength/2.0)
                    octants[i][j][k] = Chunk(nsl, nog, resolution, confres, max_allowed)
                }
            }
        }
        //gather elements in the appropriate subquadrant
        for(p in elements) {
            //get which octant the point is in
            val pc = p.getCoords()
            val xc = ((pc.first-origin.first)*2/sideLength).toInt()
            val yc = ((pc.first-origin.first)*2/sideLength).toInt()
            val zc = ((pc.second-origin.second)*2/sideLength).toInt()
            //add
            octants[xc][yc][zc]!!.insert(p)
        }
    }
    //calculate/update self normconfidence/confidencedensity
    //called internally when needed
    private fun selfNC(): Double {
        if(nc_updated) return normConfidence
        nc_updated = true
        if(leaf) {
            var sum = 0.0
            for(p in elements) {
                sum += p.getConf()
            }
            normConfidence = sum/size
        } else {
            var sum = 0.0
            for(i in 0..1) {
                for(j in 0..1) {
                    for(k in 0..1) {
                        sum += octants[i][j][k]!!.selfNC()*octants[i][j][k]!!.size
                    }
                }
            }
            normConfidence = sum/size
        }
        return normConfidence
    }
    private fun selfCD(): Double {
        if(cd_updated) return confidenceDensity
        cd_updated = true
        if(leaf) {
            var sum = 0.0
            for(p in elements) {
                sum += p.getConf()
            }
            confidenceDensity = sum/(sideLength*sideLength*sideLength)
        } else {
            var sum = 0.0
            for(i in 0..1) {
                for(j in 0..1) {
                    for(k in 0..1) {
                        sum += octants[i][j][k]!!.selfCD()
                    }
                }
            }
            confidenceDensity = sum/8 //each octant has 1/8 the area
        }
        return confidenceDensity
    }
    fun areaNC(p1: Point, p2: Point): Double {
        return 0.0
    }
    fun areaCD(p1: Point, p2: Point): Double {
        return 0.0
    }
}

class ConfidenceTree {
    private var meterChunks = HashMap<Triple<Int, Int, Int>, Chunk>() //chunks in this outer layer are a meter cube
}