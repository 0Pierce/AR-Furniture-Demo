package com.exceptionhandlers.avante_ar.activities


import android.annotation.SuppressLint
import android.content.Intent
import android.opengl.GLES20
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.exceptionhandlers.avante_ar.R
import com.exceptionhandlers.avante_ar.dataClasses.AnchorHolder
import com.exceptionhandlers.avante_ar.dataClasses.CatalogItem
import com.exceptionhandlers.avante_ar.depth.AABB
import com.exceptionhandlers.avante_ar.depth.BoxRenderer
import com.exceptionhandlers.avante_ar.depth.DepthData
import com.exceptionhandlers.avante_ar.depth.DepthRenderer
import com.exceptionhandlers.avante_ar.depth.PointClusteringHelper
import com.google.android.filament.Engine
import com.google.android.material.navigation.NavigationView
import com.google.ar.core.Anchor
import com.google.ar.core.Anchor.CloudAnchorState
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.NotYetAvailableException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.math.Position
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberMaterialLoader
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.function.BiConsumer

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
class LiveViewActivity : AppCompatActivity(), OnCatalogItemSelectedListener {


    lateinit var catalogue: FragmentContainerView
    lateinit var sceneViewPort: ARSceneView
    lateinit var loadingView: View
    lateinit var instructionText: TextView
    private var anchorCount = 0
    private var selectedItems = mutableListOf<CatalogItem>()
    private var visibleItems = mutableListOf<CatalogItem>()
    private val depthRenderer = DepthRenderer()
    private val boxRenderer = BoxRenderer()
    lateinit var navMenuToggle: ActionBarDrawerToggle
    private var anchorsWithNodes = mutableListOf<Pair<AnchorNode, CatalogItem>>()
    private var selectedAnchors = mutableListOf<Pair<AnchorNode, CatalogItem>>()
    private lateinit var materialLoader: MaterialLoader

    private var cloudmode = HostResolveMode.NONE
    private var uncloudedAnchors = mutableListOf<Pair<AnchorNode, CatalogItem>>()
    private var cloudIDs = mutableListOf<String>()
    private var idToModel = mutableMapOf<String, CatalogItem?>()

    private lateinit var firebaseDBref : DatabaseReference


    private var depthBtnFlag: Boolean = false

    // Cloud Anchor Components

    private val currentMode: HostResolveMode? = null

    //Used as a flag and displaying loading icon
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
            isLoading = true
            getString(R.string.point_your_phone_down)
        } else {
            isLoading = false
            null
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_view)

        firebaseDBref = FirebaseDatabase.getInstance().getReference("SavedAnchors")
        //sceneViewPort.lifecycle.currentState




        //Gets and sets the engine for the fake composableView inside the layout
        //Just used to facilitate the box rendering around 3d models
        val engine = Engine.create()
        findViewById<ComposeView>(R.id.my_composable).setContent {
            MaterialLoaderComposable(engine)
        }

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
            when (it.itemId) {

                //Catalogue button
                R.id.btnOne -> {


                    TouchListener()
                    catalogue.isVisible = true
                    btnCatClose.isVisible = true
                    supportFragmentManager.commit {
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

                    val camera = frameG!!.camera
                    sessionFR?.let { it ->
                        DepthData.create(
                            frameG,
                            it.createAnchor(camera.pose)
                        )


                    }
                    depthBtnFlag = true
                    drawerMenuLayout.closeDrawer(GravityCompat.START)
                    //onDrawFrame()

                }
                //Remove all button
                R.id.btnThree -> {
                    if (anchorsWithNodes.isEmpty()) {
                        Toast.makeText(applicationContext, "Nothing to remove", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        for (item in anchorsWithNodes) {
                            sceneViewPort.removeChildNode(item.first)
                            item.first.detachAnchor()
                        }
                        Toast.makeText(applicationContext, "Removed all models", Toast.LENGTH_SHORT)
                            .show()
                        anchorsWithNodes.clear()
                        selectedAnchors.clear()
                    }
                    drawerMenuLayout.closeDrawer(GravityCompat.START)
                }

                //Remove selected anchors
                R.id.btnFour -> {
                    if (selectedAnchors.isNotEmpty()) {
                        for (anchor in selectedAnchors) {

                            //Gets are the required data of the anchor
                            val anchorNode = anchor.first
                            val item = anchor.second
                            val AnchorPair = Pair(anchorNode, item)

                            //Removes the selected anchor from all the lists and viewport
                            sceneViewPort.removeChildNode(anchorNode)
                            anchorNode.detachAnchor()
                            anchorsWithNodes.remove(AnchorPair)
                            selectedAnchors.remove(anchor)
                        }
                        //Closes the drawer
                        drawerMenuLayout.closeDrawer(GravityCompat.START)
                    } else {
                        Toast.makeText(this, "No anchors selected", Toast.LENGTH_SHORT).show()
                    }


                }

                //Calc distance between two selected anchors
                R.id.btnFive -> {
                    if (selectedAnchors.size == 2) {
                        val pos1 = selectedAnchors[0].first.position
                        val pos2 = selectedAnchors[1].first.position
                        Toast.makeText(
                            this,
                            "Distance: " + calculateDistance(pos1, pos2),
                            Toast.LENGTH_SHORT
                        ).show()
                        drawerMenuLayout.closeDrawer(GravityCompat.START)
                    } else {
                        Toast.makeText(
                            this,
                            "Select 2 anchors, selected: " + selectedAnchors.size + " anchors",
                            Toast.LENGTH_SHORT
                        ).show()
                        drawerMenuLayout.closeDrawer(GravityCompat.START)
                    }
                }
                //Save cloud anchors
                R.id.btnSix -> {

                    if(anchorsWithNodes!= null){
                        drawerMenuLayout.closeDrawer(GravityCompat.START)
                        val prompt = findViewById<ConstraintLayout>(R.id.layoutTitlePrompt)
                        prompt.isVisible = true


                        val btnConfirmTitle = findViewById<Button>(R.id.btnConfirmTitle).setOnClickListener{
                        val edtxtLayoutTitle = findViewById<EditText>(R.id.etxtLayoutTitle)

                            if(edtxtLayoutTitle.text.isNotEmpty()){
                                var title = edtxtLayoutTitle.text.toString()
                                saveAnchors(title)
                            }

                            drawerMenuLayout.closeDrawer(GravityCompat.START)
                        }

                        val btnCancelTitle = findViewById<Button>(R.id.btnCancelTitle).setOnClickListener{
                            prompt.isVisible = false
                            drawerMenuLayout.closeDrawer(GravityCompat.START)
                        }
                    }else{
                        Toast.makeText(this, "No placed anchors", Toast.LENGTH_SHORT).show()
                    }

                }

            }
            true
        }




        if (savedInstanceState == null) {

            supportFragmentManager.commit {
                setReorderingAllowed(true)
                //val fragment = LiveViewCatalogue()
                add<LiveViewCatalogueFragment>(R.id.catalogueFragment)

            }
        }

        val btnBack = findViewById<Button>(R.id.btnBack)

//        Button to go back to Homepage
        btnBack.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))

        }

        catalogue = findViewById(R.id.catalogueFragment)
        catalogue.isVisible = false

        btnCatClose.setOnClickListener {
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


                config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED


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


            //Gathers frame data
            onSessionUpdated = { _, frame ->




//                    if(frame.getUpdatedPlanes().isNullOrEmpty()){
//                        isLoading = true
//                    }else{
//                        isLoading = false
//                    }

//                if (anchorNode == null) {
//                    //Gets the currently tracked planes if there are no anchor nodes
//                    Log.d("model", "frame Helmet: " + frame)
//
//                    frame.getUpdatedPlanes()
//                        //A iterable interface checking if the current plane is a flat surfaces
//                        .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
//
//                        //If yes, create a new anchor node at the center and track it
//                        ?.let { plane ->
//                            Log.d("model", "frame Helmet in: " + plane)
//                            addAnchorNode(plane.createAnchor(plane.centerPose))
//
//                        }
//                    //Log.d("model", "frame Helmet: "+frame?.getUpdatedPlanes()?.firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING })
//
//
//                }
                if (depthBtnFlag == true) {
                    Log.d("onDraw", "Updated onDrawFrame")
                    onDrawFrame()
                }

            }
            //Self explanatory
            onTrackingFailureChanged = { reason ->
                this@LiveViewActivity.trackingFailureReason = reason
            }


        }
        Log.d("depthDraw", "OnCreate: " + sceneViewPort.lifecycle?.currentState)



        try {
            // Create the texture and pass it to ARCore session to be filled during update().

            depthRenderer.createOnGlThread( /*context=*/this)
            boxRenderer.createOnGlThread( /*context=*/this)

        } catch (e: IOException) {
            Log.d("depth", "Cant init renderer: " + e)
        }


    }


    //Helps render the cube around 3d Models
    @Composable
    fun MaterialLoaderComposable(engine: Engine) {
        materialLoader = rememberMaterialLoader(engine)
        // Use materialLoader here
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun TouchListener() {
        Log.d("touch", "Entered TouchListener")

        //Screen touch listener


        //Unneeded?
        //Log.d("touch","Ran loop")
        sceneViewPort.setOnTouchListener { _, event ->

            // Toast.makeText(this, "Touch occured", Toast.LENGTH_SHORT).show()

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Handle object selection if a touch event occurs
                    //handleObjectSelection(event)
                    true
                }

            }

            //Only newly selected items will be here
            //And they will trigger the onTouch
            if (selectedItems.isNotEmpty()) {
                Log.d("touch", "Placed Cat Item")
                sceneViewPort.setOnTouchListener(null)
                spawnCatItem(event)
            }
            true
        }


    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //Returns true so we know the user clicked on this specific item
        if (navMenuToggle.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    private fun spawnCatItem(event: MotionEvent) {

        if (event.action == MotionEvent.ACTION_DOWN) {
            var x: Float = 0.0f
            var y: Float = 0.0f
            for (item in selectedItems) {

                try {
                    val hitResult =
                        sceneViewPort.frame?.hitTest(event.x + x, event.y + y)?.firstOrNull()

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
//    private fun handleTouchEvent(event: MotionEvent) {
//        if (event.action == MotionEvent.ACTION_DOWN && anchorCount < 4) {
//            try {
//                val hitResult = sceneViewPort.frame?.hitTest(event.x, event.y)?.firstOrNull()
//
//                hitResult?.let {
//                    val anchor = sceneViewPort.session?.createAnchor(hitResult.hitPose)
//
//                    anchor?.let { addAnchorNode(it) }
//                }
//            } catch (e: NotYetAvailableException) {
//                e.printStackTrace()
//            }
//        }
//    }


    //Old greek guy equation
    private fun calculateDistance(position1: Position, position2: Position): Float {
        val deltaX = position2.x - position1.x
        val deltaY = position2.y - position1.y
        val deltaZ = position2.z - position1.z

        // Calculate the distance using the Pythagorean theorem
        return kotlin.math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)
    }


    //Adding a new anchor based on a anchor position

    fun addAnchorNode(anchor: Anchor, item: CatalogItem) {
        Toast.makeText(this, "Anchor", Toast.LENGTH_SHORT).show()
        if (item != null) {
            Log.d("model", "Placing: " + item.name)
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
                        if (item == null) {
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
                        } else if (item != null) {
                            Log.d("model", "Loading furniture")
                            val selectedPath = item.imgPath
                            Log.d("model", "Loading furniture: " + selectedPath)
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

                                var materialLoader = MaterialLoader(engine, applicationContext)
                                val boundingBoxNodeDouble = CubeNode(
                                    engine,
                                    size = modelNode.extents,
                                    center = modelNode.center,
                                    materialInstance = materialLoader.createColorInstance(
                                        Color.Green.copy(
                                            alpha = 0.2f
                                        )
                                    )
                                ).apply {
                                    isVisible = false
                                }
                                val boundingBoxNodeLong = CubeNode(
                                    engine,
                                    size = modelNode.extents,
                                    center = modelNode.center,
                                    materialInstance = materialLoader.createColorInstance(
                                        Color.Red.copy(
                                            alpha = 0.2f
                                        )
                                    )
                                ).apply {
                                    isVisible = false
                                }
                                modelNode.addChildNode(boundingBoxNodeDouble)
                                modelNode.addChildNode(boundingBoxNodeLong)
                                addChildNode(modelNode)
                                //Goes through all of the nodes and sets a doubleTap listener
                                listOf(modelNode, anchorNode).forEach {
                                    it.onDoubleTap = {




                                        //Checks if that model already has a box around it
                                        //If no, goes ahead and makes one
                                        if (boundingBoxNodeDouble.isVisible == false) {
                                            Toast.makeText(
                                                applicationContext,
                                                "Double tab",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val anchorNodePair = Pair(anchorNode, item)
                                            //Used to calc the distance between selected anchors
                                            //and other stuff in the future
                                            selectedAnchors.add(anchorNodePair)
                                            boundingBoxNodeDouble.isVisible = true
                                            true

                                            //If yes, removes the box
                                        } else {
                                            val anchorNodePair = Pair(anchorNode, item)
                                            boundingBoxNodeDouble.isVisible = false
                                            selectedAnchors.remove(anchorNodePair)
                                            true
                                        }


                                    }
                                }


                                listOf(modelNode, anchorNode).forEach {

                                    //Locks the longPress model in place preventing it from being moved
                                    //And displays a red box around it
                                    it.onLongPress = {
                                        if (boundingBoxNodeLong.isVisible == false) {
                                            Toast.makeText(
                                                applicationContext,
                                                "Long press",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            boundingBoxNodeLong.isVisible = true
//                                            anchorNode.isEditable = false
//                                            anchorNode.anchor.apply {
//                                                isEditable = false
//                                            }

                                            anchorNode.anchor.apply {
                                                isEditable = false
                                                isPositionEditable = false
                                            }

                                            true

                                            //Removed the long press box around the model
                                        } else {
                                            boundingBoxNodeLong.isVisible = false
//                                            anchorNode.isEditable = true
//                                            anchorNode.anchor.apply {
//                                                isEditable = true
//                                            }
                                            anchorNode.anchor.apply {
                                                isEditable = true
                                                isPositionEditable = true
                                            }
                                            true

                                        }
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
        val anchorNodePair = Pair(anchorNode, item)
        anchorsWithNodes.add(anchorNodePair as Pair<AnchorNode, CatalogItem>)
        uncloudedAnchors.add(anchorNodePair)



        anchorCount = anchorsWithNodes.size

// Update instructions or perform any other actions when the limit is reached
        if (anchorCount >= 4) {
            instructionText.text = getString(R.string.max_anchors_reached)

            // Calculate and display the distance between the two anchors
//            val distance = calculateDistance(anchorsWithNodes[0].second, anchorsWithNodes[1].second)
//            Toast.makeText(this, "Distance: $distance meters", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCatalogItemSelected(item: CatalogItem) {
        selectedItems.add(item)
        Toast.makeText(this, "Item selected: ${item.name}", Toast.LENGTH_SHORT).show()
    }


    private fun isTracking(): Boolean {
        val frame = sceneViewPort.session?.frame
        Log.d("depth", "0: " + frame?.camera?.getTrackingFailureReason().toString())
        return frame?.camera?.trackingState == TrackingState.TRACKING
    }


    fun onDrawFrame() {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)


        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.


        if (isTracking()) {

            // Notify ARCore session that the view size changed so that the perspective matrix and
            // the video background can be properly adjusted.
            //var frameG = sceneViewPort.session?.frame

            //val frame = sceneViewPort.session?.frame

            // ARCore is tracking, proceed with operations that require tracking


            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.

            var frame: Frame? = sceneViewPort.session?.update()
            Log.d("depthDraw", "1: " + frame?.camera?.getTrackingFailureReason().toString())

            var camera = frame?.camera

            // If frame is ready, render camera preview image to the GL surface.


            // Retrieve the depth data for this frame.
            Log.d("depthDraw", "2: " + frame?.camera?.getTrackingFailureReason().toString())
            Log.d("depthDraw", camera!!.getPose().toString())
            // Retrieve the depth data for this frame.

            if (frame == null) {
                Log.d("depthDraw", "Frame null")

            } else {
                Log.d("depthDraw", "frame NOT null")

            }

            if (camera == null) {
                Log.d("depthDraw", "camera null")

            } else {
                Log.d("depthDraw", "camera NOT null")

            }
            var points = sceneViewPort.session?.let { session ->
                frame?.let { frame ->
                    camera?.let { camera ->
                        Log.d("depthDraw", "Inside val points: session; " + session)
                        Log.d("depthDraw", "Inside val points: cam pose; " + camera.getPose())

                        DepthData.create(frame, session.createAnchor(camera.getPose()))
                    }
                }
            }


            Log.d("depthDraw", "Points: " + points.toString())

            if (points == null) {
                return
            }

            Log.d("depthDraw", "3: " + frame?.camera?.getTrackingFailureReason().toString())

            // Filters the depth data.
            if (sceneViewPort.session != null) {
                DepthData.filterUsingPlanes(
                    points,
                    sceneViewPort.session!!.getAllTrackables(Plane::class.java)
                )
            }
            Log.d("depthDraw", "4: " + frame?.camera?.getTrackingFailureReason().toString())

            // Visualize depth points.
            depthRenderer.update(points)
            Log.d("depthDraw", "5: " + frame?.camera?.getTrackingFailureReason().toString())
            if (camera != null) {

                Toast.makeText(this, "Drew camera", Toast.LENGTH_SHORT).show()
                depthRenderer.draw(camera)
            }

            Log.d("depthDraw", "6: " + frame?.camera?.getTrackingFailureReason().toString())
            // Draw boxes around clusters of points.
            val clusteringHelper = PointClusteringHelper(points)
            val clusters: List<AABB> = clusteringHelper.findClusters()
            for (aabb in clusters) {
                boxRenderer.draw(aabb, camera)
            }
            Log.d("depthDraw", frame?.camera?.getTrackingFailureReason().toString())

        } else {
            Toast.makeText(this, "Not tracking", Toast.LENGTH_SHORT).show()
        }


    }




    //Sends hard anchor data(POS,Path) to firebase
    fun saveAnchors(title : String){

        val storedAnchor = firebaseDBref.push().key!!
        val userAuth = FirebaseAuth.getInstance()
        userAuth.currentUser.toString()
        val id = userAuth.currentUser.toString()
        val userData = Pair(id,title)
        for(pair in anchorsWithNodes) run {

            val newAnchor = AnchorHolder(pair.first.position.x, pair.first.position.y,pair.first.position.z,pair.second.imgPath)

            //Wraps all of the data into a single object, sends the user data for every anchorNode
            //This is a temp measure and should be properly implemented later
            val dataPackage = Pair(newAnchor,userData)
            firebaseDBref.child(storedAnchor).setValue(dataPackage)
                .addOnCompleteListener{
                    Toast.makeText(this, "Anchor/s Saved", Toast.LENGTH_SHORT).show()
                }

                .addOnFailureListener{
                    Toast.makeText(this, "Failed to save Anchor/s", Toast.LENGTH_SHORT).show()
                }
        }

    }



    // Cloud Anchor Stuffs

    // Hosting modes
    private enum class HostResolveMode {
        NONE,
        HOSTING,
        RESOLVING
    }

    // Initalizing UI components

    // Initialize UI components (Code needs to be reformatted).
    //hostButton = findViewById<android.widget.Button?>(R.id.host_button)
    // hostButton.setOnClickListener(android.view.View.OnClickListener (
    // { view:android.view.View? -> onHostButtonPress() }))
    // resolveButton = findViewById<android.widget.Button?>(R.id.resolve_button)
    // resolveButton.setOnClickListener(android.view.View.OnClickListener (
    // { view:android.view.View? -> onResolveButtonPress() }))

    // Initialize Cloud Anchor variables
    //currentMode = HostResolveMode.NONE

    private fun enableHosting() {
        val session = sceneViewPort.session ?: return
        val config = Config(session)
        config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
        sceneViewPort.session!!.configure(config)
        cloudmode = HostResolveMode.HOSTING
    }

    private fun enableResolving() {
        val session = sceneViewPort.session ?: return
        val config = Config(session)
        config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
        sceneViewPort.session!!.configure(config)
        cloudmode = HostResolveMode.RESOLVING
    }

    private fun turnCAOff() {
        val session = sceneViewPort.session ?: return
        val config = Config(session)
        config.cloudAnchorMode = Config.CloudAnchorMode.DISABLED
        sceneViewPort.session!!.configure(config)
        cloudmode = HostResolveMode.NONE
    }

    private fun addID(ID: String, state: CloudAnchorState) {
        if (state == CloudAnchorState.SUCCESS) {
            cloudIDs.add(ID)
        } else {
            Toast.makeText(
                this,
                "Add failure for cloud anchor: " + state.toString(),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun addResAnchor(id: String, a: Anchor, state: CloudAnchorState) {
        if (state == CloudAnchorState.SUCCESS) {
            val itemToAdd = idToModel.getOrDefault(id, null)
          //  addAnchorNode(a, itemToAdd)
        } else {
            Toast.makeText(
                this,
                "Resolve failure for cloud anchor: " + state.toString(),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateCA() { //updates either the cloud anchors or local anchors depending on what the mode is
        //if updating local anchors (resolving mode), make sure to put the correct IDs in the ID list beforehand
        val session = sceneViewPort.session ?: return
        if (cloudmode == HostResolveMode.NONE) return
        else if (cloudmode == HostResolveMode.HOSTING) {
            cloudIDs.clear()
            while (uncloudedAnchors.isNotEmpty()) {
                val ca = uncloudedAnchors.removeAt(0);
                val addIDBC: BiConsumer<String, CloudAnchorState> =
                    BiConsumer { x, y -> addID(x, y) }
                session.hostCloudAnchorAsync(ca.first.anchor, 365, addIDBC)
            }
        } else {
            for (id in cloudIDs) {
                val addRA: BiConsumer<Anchor, CloudAnchorState> =
                    BiConsumer { x, y -> addResAnchor(id, x, y) }
                session.resolveCloudAnchorAsync(id, addRA)
            }
        }
    }


    /** Callback function invoked when the Host Button is pressed. */

    private fun onHostButtonPress() {
        if (cloudmode == HostResolveMode.HOSTING) {
            //   resetMode()
            return
        }
    }

    /** Callback function invoked when the Resolve Button is pressed. */

    private fun onResolveButtonPress() {
        if (cloudmode == HostResolveMode.RESOLVING) {
            // resetMode()
            return
        }
    }

    // Reset hosting

    // private void resetMode() {
    // hostBtn.setText(R.string.host_button_text)
    // hostBtn.setEnabled(true)
    // resolveBtn.setText(R.string.host_resolve_text)
    // resolveBtn.setEnabled(true)
    // setNewAnchor(null)
    // HostResolveMode.None ()
// }

    // Import pointCloudRenderer from sceneview
// (https://developers.arcgis.com/javascript/latest/api-reference/esri-renderers-PointCloudRenderer.html)
//    private val pointCloudRenderer: PointCloudRenderer = PointCloudRenderer()
//
//
//    private val anchorLock: Any? = Any()

    // Adding two buttons: one for hosting (sending anchors to the db)
    // resolveButton (to retrieve items from the db)

    // onClick hostbutton:

//    private val hostButton: Button? = null
//    private val resolveButton: Button? = null


//     private fun setNewAnchor(newAnchor: Anchor) {
//        synchronized(anchorLock) {
//           if (anchor != null) {
//            anchor.detach()
//           }
//           anchor = newAnchor
//      }


}