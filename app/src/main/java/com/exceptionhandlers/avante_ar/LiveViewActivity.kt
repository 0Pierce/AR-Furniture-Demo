package com.exceptionhandlers.avante_ar

import android.annotation.SuppressLint
import android.content.Intent
import android.opengl.GLES20
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.eventFlow
import androidx.lifecycle.lifecycleScope
import com.exceptionhandlers.avante_ar.depth.AABB
import com.exceptionhandlers.avante_ar.depth.BoxRenderer
import com.exceptionhandlers.avante_ar.depth.DepthData
import com.exceptionhandlers.avante_ar.depth.DepthRenderer
import com.exceptionhandlers.avante_ar.depth.PointClusteringHelper
import com.google.android.material.navigation.NavigationView
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.NotYetAvailableException
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.ModelNode


import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.StrictMath.sqrt
import kotlin.math.acos


/*
*Name: LiveViewActivity
*
* ========Authors:========
* Pierce, Amir, Isaac, Anson
*
*
* ========Description:========
* The primary AR Activity which will handle AR functionality/capabilities
* All AR related classes/APIs will culminate here
* TBD
*
*
*
*
* ========Primary Functions:========
*Hold LiveView fragment which creates a AR viewport
* TBD
*
*

* */
class LiveViewActivity : AppCompatActivity(), OnCatalogItemSelectedListener  {


    lateinit var catalogue: FragmentContainerView
    lateinit var sceneViewPort: ARSceneView
    lateinit var loadingView: View
    lateinit var instructionText: TextView
    lateinit var depthBtn : ToggleButton
    private var anchorCount = 0
    private var anchorPositions = mutableListOf<Position>()
    private lateinit var myFrame : Frame
    private lateinit var myAnchor : Anchor

    private var selectedItems = mutableListOf<CatalogItems>()
    private var visibleItems = mutableListOf<CatalogItems>()
    private val depthRenderer = DepthRenderer()
    private val boxRenderer = BoxRenderer()
    private lateinit var btnRemove: Button
    lateinit var navMenuToggle : ActionBarDrawerToggle
    private var anchorsWithNodes = mutableListOf<Pair<AnchorNode, Position>>()


    //Used as a flag and displaying loading icon (Not showing rn)
    var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
        }

    //An automatically positioned anchor from ARCore that enables plane tracking
    var anchorNode: AnchorNode? = null
        set(value) {
            if (field != value) {
                field = value
                updateInstructions()
            }
        }

    //Displays an issue at the top of the screen if one occurs
    var trackingFailureReason: TrackingFailureReason? = null
        set(value) {
            if (field != value) {
                field = value
                updateInstructions()
            }
        }

    //Updates the textView at the top of the screen with messages
    fun updateInstructions() {
        instructionText.text = trackingFailureReason?.let {
            it.getDescription(this)
        } ?: if (anchorNode == null) {
            getString(R.string.point_your_phone_down)
        } else {
            null
        }
    }




    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_view)

        //sceneViewPort.lifecycle.currentState



        //Sets the toggle to open and close the sliding menu
        var drawerMenuLayout = findViewById<DrawerLayout>(R.id.drawerMenuLayout)
        navMenuToggle = ActionBarDrawerToggle(this, drawerMenuLayout, R.string.open, R.string.close)

        drawerMenuLayout.addDrawerListener(navMenuToggle)
        navMenuToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var navView = findViewById<NavigationView>(R.id.navView)
        val btnCatClose = findViewById<ImageButton>(R.id.imgBtnCatClose)

        //Handles the sliding menu item clicks
        navView.setNavigationItemSelectedListener {
            when(it.itemId){

                //Catalogue button
                R.id.btnOne -> {


                        TouchListener()
                        catalogue.isVisible = true
                        btnCatClose.isVisible = true
                        supportFragmentManager.commit{
                            setReorderingAllowed(true)
                            //val fragment = LiveViewCatalogue()
                            add<LiveViewCatalogueFragment>(R.id.catalogueFragment)
                        }
                    drawerMenuLayout.closeDrawer(GravityCompat.START)

                }
                //Depth button
                R.id.btnTwo -> {
                        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
                        Log.d("depth", "Loaded Class")

                        var frameG = sceneViewPort.session?.frame
                        var sessionFR = sceneViewPort.session

                        val camera = frameG!!.getCamera()
                        sessionFR?.let { it ->
                            DepthData.create(
                                frameG,
                                it.createAnchor(camera.getPose())
                            )
                        }
                    drawerMenuLayout.closeDrawer(GravityCompat.START)
                    //onDrawFrame(sessionFR)

                }
                //Remove all button
                R.id.btnThree -> {
                    if(anchorsWithNodes.isEmpty()){
                        Toast.makeText(applicationContext, "Nothing to remove", Toast.LENGTH_SHORT).show()
                    }else{
                        for(item in anchorsWithNodes){
                            sceneViewPort.removeChildNode(item.first)
                            item.first.detachAnchor()
                        }
                        Toast.makeText(applicationContext, "Removed all models", Toast.LENGTH_SHORT).show()
                        anchorsWithNodes.clear()
                    }
                    drawerMenuLayout.closeDrawer(GravityCompat.START)
                }


            }
            true

        }


        //Anson thing
        touchSlop = ViewConfiguration.get(this).scaledTouchSlop
        //btnRemove = findViewById(R.id.btnRemove)

        if(savedInstanceState == null){

            supportFragmentManager.commit{
                setReorderingAllowed(true)
                //val fragment = LiveViewCatalogue()
                add<LiveViewCatalogueFragment>(R.id.catalogueFragment)

            }
        }

        val btnBack = findViewById<Button>(R.id.btnBack)

//        Button to go back to Homepage
        btnBack.setOnClickListener{
            startActivity(Intent(this, HomePageActivity::class.java))

        }

        catalogue=findViewById(R.id.catalogueFragment)
        catalogue.isVisible = false

        btnCatClose.setOnClickListener{
            val fragment = supportFragmentManager.findFragmentById(R.id.catalogueFragment)
            if (fragment != null) {
                supportFragmentManager.beginTransaction().remove(fragment).commit()
            }
            catalogue.isVisible = false
            btnCatClose.isVisible = false
        }


        //Gets the instruction Text ID(Changes text at top of screen nothing else)
        instructionText = findViewById(R.id.instructionText)
        instructionText = findViewById(R.id.instructionText)
        //A loading icon that indicates plane scan (Currently not visible)
        //Acts as a flag for other components
        loadingView = findViewById(R.id.loadingView)

        //Starts a new ARCore session for the sceneviewPort
        sceneViewPort = findViewById<ARSceneView?>(R.id.sceneViewLive).apply {
            //Fairly simple, just enables planes to be rendered
            planeRenderer.isEnabled = true
            planeRenderer.isShadowReceiver = false

            //Buggy
            //cameraStream!!.isDepthOcclusionEnabled = true

            //Unsure may need to remove
            //cameraStream!!.setCulling(true)

            //Renders all  planes
            //planeRenderer.planeRendererMode = PlaneRenderer.PlaneRendererMode.RENDER_ALL

            //Modifies session and sets a automatic depthMode
            //Meaning the mode will activate depending on the phones hardware and if its supported
            //From what I understand DepthMode uses the full depth API for detection (actual name)
            //Theres two types, raw depth API and full depth API
            configureSession { session, config ->
                config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    true -> Config.DepthMode.AUTOMATIC

                    //disables DepthMode if the device does not support it
                    else -> Config.DepthMode.DISABLED
                }


                //UNSURE: but you can guess by going off their names
                //No instant placements (Guessing objects cannot snap?)
                config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
                //Enables HDR light estimation
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR

                //config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE)
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                //Enables object detection
                //config.semanticMode = Config.SemanticMode.ENABLED
                //depthBtn = LiveViewbind.tglBtnDepthAPI



            }



            //Gathers frame data and once a plane is detected, places the first anchor
            onSessionUpdated = { _, frame ->
                if (anchorNode == null) {
                    //Gets the currently tracked planes if there are no anchor nodes
                    Log.d("model", "frame Helmet: "+frame)

                    frame.getUpdatedPlanes()
                        //A iterable interface checking if the current plane is a flat surfaces
                        .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }

                        //If yes, create a new anchor node at the center and track it
                        ?.let { plane ->
                            Log.d("model","frame Helmet in: "+plane)
                            addAnchorNode(plane.createAnchor(plane.centerPose))

                        }
                    //Log.d("model", "frame Helmet: "+frame?.getUpdatedPlanes()?.firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING })


                }
                //onDrawFrame()
            }
            //Self explanatory
            onTrackingFailureChanged = { reason ->
                this@LiveViewActivity.trackingFailureReason = reason
            }



        }


        try {
            // Create the texture and pass it to ARCore session to be filled during update().

            //depthRenderer.createOnGlThread( /*context=*/this)
            boxRenderer.createOnGlThread( /*context=*/this)

        } catch (e: IOException) {
            Log.d("depth", "Cant init renderer: "+ e)
        }







    }





    @SuppressLint("ClickableViewAccessibility")
    private fun TouchListener(){
        Log.d("touch","Entered TouchListener")

        //Screen touch listener



            //Log.d("touch","Ran loop")
            sceneViewPort.setOnTouchListener { _, event ->

              // Toast.makeText(this, "Touch occured", Toast.LENGTH_SHORT).show()

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Handle object selection if a touch event occurs
                        handleObjectSelection(event)
                        true
                    }

                }
                //Makes sure we dont get the helmet when placing cat items
//                if(selectedItems.isEmpty()){
//                    Log.d("touch","Placed Helmet")
//                    sceneViewPort.setOnTouchListener(null)
//                    handleTouchEvent(event)
//                }

                //Only newly selected items will be here
                //And they will trigger the onTouch
                if(selectedItems.isNotEmpty()){
                    Log.d("touch","Placed Cat Item")
                    sceneViewPort.setOnTouchListener(null)
                    spawnCatItem(event)
                }
                true
            }



    }
    private fun handleObjectSelection(event: MotionEvent) {
        // Perform ray casting to check for intersections with objects
        val hitResult = sceneViewPort.frame?.hitTest(event)

       
        if (hitResult != null && !hitResult.isEmpty()) {
            // Retrieve the selected object from hitResult
            val hitPos = hitResult.get(0).hitPose
            //Toast.makeText(this, "Hit object at: "+hitPos, Toast.LENGTH_SHORT).show()

            val dtolerance: Double = 0.2 // Adjust as needed
            val atolerance: Double = 0.0872665 //angular tolerance

            val cx: Double = sceneViewPort.cameraNode.pose!!.tx().toDouble()
            val cy: Double = sceneViewPort.cameraNode.pose!!.ty().toDouble()
            val cz: Double = sceneViewPort.cameraNode.pose!!.tz().toDouble()

            for ((_, anchorPosition) in anchorsWithNodes) {

                val ddx: Double = (hitPos.tx()).toDouble() - anchorPosition.x
                val ddy: Double = (hitPos.ty()).toDouble() - anchorPosition.y
                val ddz: Double = (hitPos.tz()).toDouble() - anchorPosition.z

                val hdx: Double = (hitPos.tx()).toDouble() - cx
                val hdy: Double = (hitPos.ty()).toDouble() - cy
                val hdz: Double = (hitPos.tz()).toDouble() - cz

                val adx: Double = anchorPosition.x - cx
                val ady: Double = anchorPosition.y - cy
                val adz: Double = anchorPosition.z - cz

                val asq: Double = hdx*hdx+hdy*hdy+hdz*hdz
                val bsq: Double = adx*adx+ady*ady+adz*adz
                val csq: Double = ddx*ddx+ddy*ddy+ddz*ddz

                val costerm = asq+bsq-csq //cosine law: c^2 = a^2+b^2-2abcosC, where C is the angle opposite to side length c
                val C = acos(costerm/(2*sqrt(asq)*sqrt(bsq)))

                if(C < atolerance || csq < dtolerance*dtolerance) {
                    Toast.makeText(this, "Hit anchor", Toast.LENGTH_SHORT).show()
                    return
                } else {
                    Toast.makeText(this, "No anchor hit", Toast.LENGTH_SHORT).show()
                }

            }

        }
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //Returns true so we know the user clicked on this specific item
        if(navMenuToggle.onOptionsItemSelected(item)){
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    
    private fun spawnCatItem(event : MotionEvent){

        if (event.action == MotionEvent.ACTION_DOWN) {
            var x : Float = 0.0f
            var y : Float = 0.0f
          for(item in selectedItems){

              try {
                  val hitResult = sceneViewPort.frame?.hitTest(event.x+x, event.y+y)?.firstOrNull()

                  hitResult?.let {
                      val anchor = sceneViewPort.session?.createAnchor(hitResult.hitPose)

                      anchor?.let { addAnchorNode(it, item) }
                      x += 0.5f
                      y += 0.5f

                      visibleItems.add(item)
                  }
              } catch (e: NotYetAvailableException) {
                  e.printStackTrace()
              }

          }
            selectedItems.clear()


        }
    }

    //Place anchor depending where the user clicks
    private fun handleTouchEvent(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN && anchorCount < 4) {
            try {
                val hitResult = sceneViewPort.frame?.hitTest(event.x, event.y)?.firstOrNull()

                hitResult?.let {
                    val anchor = sceneViewPort.session?.createAnchor(hitResult.hitPose)

                    anchor?.let { addAnchorNode(it) }
                }
            } catch (e: NotYetAvailableException) {
                e.printStackTrace()
            }
        }
    }


    //Old greek guy equation
    private fun calculateDistance(position1: Position, position2: Position): Float {
        val deltaX = position2.x - position1.x
        val deltaY = position2.y - position1.y
        val deltaZ = position2.z - position1.z

        // Calculate the distance using the Pythagorean theorem
        return kotlin.math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)
    }



    //Adding a new anchor based on a anchor position
    fun addAnchorNode(anchor: Anchor, item: CatalogItems? = null) {
        Toast.makeText(this, "Anchor", Toast.LENGTH_SHORT).show()
        if (item != null) {
            Log.d("model","Placing: " +item.name)
        }

        sceneViewPort.addChildNode(
            //AnchorNode constructor call, passing along the viewPort engine and anchor position
            //then applying the following to that object

            AnchorNode(sceneViewPort.engine, anchor)
                .apply {
                    isEditable = true
                    isTouchable = true
                    //A type of multi-threading but instead uses a coroutine to start a new task
                    //Without stopping or occupying the main application thread
                    //The way I see it, imagine its a fragment of a Activity, both the fragment
                    //and activity can continue playing animations at the same time, even though
                    //the fragment comes from the Activity
                    lifecycleScope.launch {
                        //Raises the loading flag
                        isLoading = true


                        //If there isnt an item, put down place holder helmet
                        if(item == null){
                            sceneViewPort.modelLoader.loadModelInstance(

                                "https://sceneview.github.io/assets/models/DamagedHelmet.glb"
                                //UNSURE: Makes the model scalable and adjustable?
                            )?.let { modelInstance ->
                                addChildNode(
                                    ModelNode(
                                        modelInstance = modelInstance,
                                        // Scale to fit in a 0.5 meters cube
                                        scaleToUnits = 0.5f,
                                        // Bottom origin instead of center so the model base is on floor
                                        centerOrigin = Position(y = -0.5f)

                                    ).apply {
                                        isEditable = true
                                    }
                                )
                            }
                         //Could be an else, but adding elif here so that, in the future
                         //If we want to adjust certain items models before anchroing we can
                         //and we dont have to change this
                         //Small future proofing
                        }else if(item != null){
                            Log.d("model","Loading furniture")
                            val selectedPath = item.imgPath
                            Log.d("model","Loading furniture: "+ selectedPath)
                            sceneViewPort.modelLoader.loadModelInstance(

                                selectedPath
                                //UNSURE: Makes the model scalable and adjustable?
                            )?.let { modelInstance ->
                                    val anchorNode = AnchorNode(engine = engine, anchor = anchor)
                                    val modelNode = ModelNode(
                                        modelInstance = modelInstance,
                                        // Scale to fit in a 0.5 meters cube
                                        scaleToUnits = 0.5f,
                                        // Bottom origin instead of center so the model base is on floor
                                        centerOrigin = Position(y = -0.5f)

                                    ).apply {
                                        isEditable = true
                                        isTouchable = true

                                    }



                                addChildNode(modelNode)

                                listOf(modelNode, anchorNode).forEach {
                                    it.onDoubleTap = {
                                        Toast.makeText(applicationContext, "Double tab", Toast.LENGTH_SHORT).show()

                                        true
                                    }
                                }
                            }

                        }

                        isLoading = false
                    }
                    //Finally, sets the AnchorNode to the current new Anchor
                    anchorNode = this



                }




        )
        val position = Position(anchor.pose.tx(), anchor.pose.ty(), anchor.pose.tz())
        val anchorNodePair = Pair(anchorNode, position)
        anchorsWithNodes.add(anchorNodePair as Pair<AnchorNode, Position>)




        anchorCount = anchorsWithNodes.size

// Update instructions or perform any other actions when the limit is reached
        if (anchorCount >= 2) {
            instructionText.text = getString(R.string.max_anchors_reached)

            // Calculate and display the distance between the two anchors
            val distance = calculateDistance(anchorsWithNodes[0].second, anchorsWithNodes[1].second)
            Toast.makeText(this, "Distance: $distance meters", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCatalogItemSelected(item: CatalogItems) {
        selectedItems.add(item)
        Toast.makeText(this, "Item selected: ${item.name}", Toast.LENGTH_SHORT).show()
    }


    private fun isTracking(): Boolean {
        val frame = sceneViewPort.session?.frame
        Log.d("depth", "0: "+frame?.camera?.getTrackingFailureReason().toString())
        return frame?.camera?.trackingState == TrackingState.TRACKING
    }


     fun onDrawFrame() {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)


        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.



        if(isTracking()){

            // Notify ARCore session that the view size changed so that the perspective matrix and
            // the video background can be properly adjusted.
            //var frameG = sceneViewPort.session?.frame

            //val frame = sceneViewPort.session?.frame

            // ARCore is tracking, proceed with operations that require tracking



            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.

            var frame: Frame? = sceneViewPort.session?.update()
            Log.d("depth", "1: "+frame?.camera?.getTrackingFailureReason().toString())

            var camera = frame?.getCamera()

            // If frame is ready, render camera preview image to the GL surface.


            // Retrieve the depth data for this frame.
            Log.d("depth", "2: "+frame?.camera?.getTrackingFailureReason().toString())
            Log.d("depth", camera!!.getPose().toString())
            // Retrieve the depth data for this frame.
            var points = sceneViewPort.session?.let { session ->
                frame?.let { frame ->
                    camera?.let { camera ->
                        DepthData.create(frame, session.createAnchor(camera.getPose()))
                    }
                }
            }


            Log.d("depth", "Points: "+points.toString())

            if (points == null) {
                return
            }

            Log.d("depth", "3: "+frame?.camera?.getTrackingFailureReason().toString())

            // Filters the depth data.
            if (sceneViewPort.session != null) {
                DepthData.filterUsingPlanes(points, sceneViewPort.session!!.getAllTrackables(Plane::class.java))
            }
            Log.d("depth", "4: "+frame?.camera?.getTrackingFailureReason().toString())

            // Visualize depth points.
            depthRenderer.update(points)
            if (camera != null) {
                Toast.makeText(this, "Drew camera", Toast.LENGTH_SHORT).show()
                depthRenderer.draw(camera)
            }


            // Draw boxes around clusters of points.
            val clusteringHelper = PointClusteringHelper(points)
            val clusters: List<AABB> = clusteringHelper.findClusters()
            for (aabb in clusters) {
                boxRenderer.draw(aabb, camera)
            }
            Log.d("depth", frame?.camera?.getTrackingFailureReason().toString())

        }else{
            Toast.makeText(this, "Not tracking", Toast.LENGTH_SHORT).show()
        }


    }
    // Variable to store the selected anchor node
    private var selectedAnchorNode: AnchorNode? = null

    // Variable to store the touch slop value
    private var touchSlop = 0




    // Method to select an item
    private fun selectItem(anchorNode: AnchorNode) {
        selectedAnchorNode = anchorNode
        // TO-DO: Can be used for other stuff but right now just for anchor deletion
    }
    private fun deleteSelectedItem() {
        selectedAnchorNode?.let { anchorNode ->
            // Remove the anchor node from the scene
            sceneViewPort.removeChildNode(anchorNode)
            // Remove the anchor node from the list of anchors with nodes
            anchorsWithNodes.removeAll { it.first == anchorNode }
            selectedAnchorNode = null
            updateInstructions()
        }
    }


    // Method to handle long-click event
    fun onItemLongClick(event: MotionEvent) {
        // Iterate through all anchors to check if the long-click is near any anchor
        for ((anchorNode, position) in anchorsWithNodes) {
            // Calculate the distance between the long-click position and the anchor position
            val distance = calculateDistance(Position(event.x, event.y), position)
            // Check if the distance is within the touch slop (a threshold for considering it a long-click)
            if (touchSlop > distance) {
                // Select the anchor node
                selectItem(anchorNode)
                // Delete the selected item
                deleteSelectedItem()
                return
            }
        }
    }
}