package com.exceptionhandlers.avante_ar

import android.annotation.SuppressLint
import android.content.Intent
import android.opengl.GLES20
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
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
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch
import java.io.IOException

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
    private val depthRenderer = DepthRenderer()
    private val boxRenderer = BoxRenderer()


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



        val btnBack = findViewById<Button>(R.id.btnBack)

//        Button to go back to Homepage
        btnBack.setOnClickListener{
            startActivity(Intent(this, HomePageActivity::class.java))

        }


        catalogue=findViewById(R.id.catalogueFragment)
        val btnCatOpen = findViewById<ImageButton>(R.id.imgBtnCatOpen)
        val btnCatClose = findViewById<ImageButton>(R.id.imgBtnCatClose)
        catalogue.isVisible = false





//        catBind.TestBtn.setOnClickListener{
//            Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
//            startActivity(Intent(this, HomePageActivity::class.java))
//
//        }

        btnCatOpen.setOnClickListener{
            catalogue.isVisible = true
            btnCatClose.isVisible = true
        }

        btnCatClose.setOnClickListener{
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
                config.semanticMode = Config.SemanticMode.ENABLED
                //depthBtn = LiveViewbind.tglBtnDepthAPI



            }



            //On every new frame, it places an anchor, once enough anchors are placed a plane is rendered
            onSessionUpdated = { _, frame ->
                if (anchorNode == null) {
                    //Gets the currently tracked planes if there are no anchor nodes

                    frame.getUpdatedPlanes()
                        //A iterable interface checking if the current plane is a flat surfaces
                        .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
                        //If yes, create a new anchor node at the center and track it
                        ?.let { plane ->
                            addAnchorNode(plane.createAnchor(plane.centerPose))
                        }




                }
                onDrawFrame()
            }
            //Self explanatory
            onTrackingFailureChanged = { reason ->
                this@LiveViewActivity.trackingFailureReason = reason
            }



        }

        
        try {
            // Create the texture and pass it to ARCore session to be filled during update().

            depthRenderer.createOnGlThread( /*context=*/this)
            boxRenderer.createOnGlThread( /*context=*/this)

        } catch (e: IOException) {
            Log.d("depth", "Cant init renderer: "+ e)
        }

        var btnDepth = findViewById<Button>(R.id.btnDepth)



        btnDepth.setOnClickListener {
                Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
                Log.d("depth", "Loaded Class")

                var frameG = sceneViewPort.session?.frame
                var sessionFR = sceneViewPort.session

                val camera = frameG!!.getCamera()
                sessionFR?.let { it ->
                    DepthData.create(
                        frameG,
                        it.createAnchor(camera.getPose()))
                }

                //onDrawFrame(sessionFR)
            // Retrieve the depth data for this frame.

            // Retrieve the depth data for this frame.




        }

        sceneViewPort.setOnTouchListener { _, event ->
            handleTouchEvent(event)
            true
        }





    }

    private fun handleTouchEvent(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN && anchorCount < 2) {
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


    private var anchorsWithNodes = mutableListOf<Pair<AnchorNode, Position>>()

    //Adding a new anchor based on a anchor position
    fun addAnchorNode(anchor: Anchor) {
        Toast.makeText(this, "Anchor", Toast.LENGTH_SHORT).show()






        sceneViewPort.addChildNode(
            //AnchorNode constructor call, passing along the viewPort engine and anchor position
            //then applying the following to that object

            AnchorNode(sceneViewPort.engine, anchor)
                .apply {
                    isEditable = true
                    //A type of multi-threading but instead uses a coroutine to start a new task
                    //Without stopping or occupying the main application thread
                    //The way I see it, imagine its a fragment of a Activity, both the fragment
                    //and activity can continue playing animations at the same time, even though
                    //the fragment comes from the Activity
                    lifecycleScope.launch {
                        //Raises the loading flag
                        isLoading = true
                        //Loads in the placeholder helmet model
                        val selectedPath = LiveViewCatalogue.SelectedItem.getInstance().path
                        sceneViewPort.modelLoader.loadModelInstance(
                            selectedPath
                            //"https://sceneview.github.io/assets/models/DamagedHelmet.glb"
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
                        isLoading = false
                    }
                    //Finally, sets the AnchorNode to the current new Anchor
                    anchorNode = this
                }
        )
        val position = Position(anchor.pose.tx(), anchor.pose.ty(), anchor.pose.tz())
        val anchorNodePair = Pair(anchorNode, position)
        anchorsWithNodes.add(anchorNodePair as Pair<AnchorNode, Position>)

        anchorCount++

// Update instructions or perform any other actions when the limit is reached
        if (anchorCount >= 2) {
            instructionText.text = getString(R.string.max_anchors_reached)

            // Calculate and display the distance between the two anchors
            val distance = calculateDistance(anchorsWithNodes[0].second, anchorsWithNodes[1].second)
            Toast.makeText(this, "Distance: $distance meters", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCatalogItemSelected(item: CatalogItems) {
        TODO("Not yet implemented")
        Toast.makeText(this, "Item selected: ${item.name}", Toast.LENGTH_SHORT).show()
    }



    private fun isTracking(): Boolean {
        val frame = sceneViewPort.session?.frame
        Log.d("depth", "0: "+frame?.camera?.getTrackingFailureReason().toString())
        return frame?.camera?.trackingState == TrackingState.TRACKING
    }

//    fun onDrawFrame(session: ARSession?) {
//        // Clear screen to notify driver it should not load any pixels from previous frame.
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
//
//
//        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
//        try {
//            // Create the texture and pass it to ARCore session to be filled during update().
//
//            depthRenderer.createOnGlThread( /*context=*/this)
//
//        } catch (e: IOException) {
//            Log.d("depth", "Cant init renderer: "+ e)
//        }
//        depthRenderer.createOnGlThread(this);
//
//        if(isTracking()){
//
//            // Notify ARCore session that the view size changed so that the perspective matrix and
//            // the video background can be properly adjusted.
//            //var frameG = sceneViewPort.session?.frame
//
//            //val frame = sceneViewPort.session?.frame
//
//            // ARCore is tracking, proceed with operations that require tracking
//
//
//
//            // Obtain the current frame from ARSession. When the configuration is set to
//            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
//            // camera framerate.
//            var frame: Frame? = session?.update()
//            Log.d("depth", "1: "+frame?.camera?.getTrackingFailureReason().toString())
//
//            var camera = frame?.getCamera()
//
//            // If frame is ready, render camera preview image to the GL surface.
//
//
//            // Retrieve the depth data for this frame.
//            Log.d("depth", "2: "+frame?.camera?.getTrackingFailureReason().toString())
//            Log.d("depth", camera!!.getPose().toString())
//            // Retrieve the depth data for this frame.
//            var points = session?.let { session ->
//                frame?.let { frame ->
//                    camera?.let { camera ->
//                        DepthData.create(frame, session.createAnchor(camera.getPose()))
//                    }
//                }
//            }
//
//
//            Log.d("depth", "Points: "+points.toString())
//
//            if (points == null) {
//                return
//            }
//
//            Log.d("depth", "3: "+frame?.camera?.getTrackingFailureReason().toString())
//
//            // Filters the depth data.
//            if (session != null) {
//                DepthData.filterUsingPlanes(points, session.getAllTrackables(Plane::class.java))
//            }
//            Log.d("depth", "4: "+frame?.camera?.getTrackingFailureReason().toString())
//
//            // Visualize depth points.
//            depthRenderer.update(points)
//            if (camera != null) {
//                Toast.makeText(this, "Drew camera", Toast.LENGTH_SHORT).show()
//                depthRenderer.draw(camera)
//            }
//
//            Log.d("depth", frame?.camera?.getTrackingFailureReason().toString())
//
//        }else{
//            Toast.makeText(this, "Not tracking", Toast.LENGTH_SHORT).show()
//        }
//
//
//
//    }




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
}