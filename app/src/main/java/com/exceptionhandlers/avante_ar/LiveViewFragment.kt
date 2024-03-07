package com.exceptionhandlers.avante_ar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.exceptionhandlers.avante_ar.databinding.ActivityLiveViewBinding
import com.google.android.filament.Viewport
import com.google.ar.core.Anchor
import io.github.sceneview.ar.ARSceneView
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.Plane
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.exceptions.NotYetAvailableException
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.scene.PlaneRenderer
import io.github.sceneview.math.Position
import io.github.sceneview.math.Size
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch


/*
*Name: LiveViewFragment
*
* ========Authors:========
* Pierce, Amir, Isaac, Anson
*
*
* ========Description:========
* Holds the AR viewscene, most AR logic involving detection and so on will be created here
* TBD
*
*
*
*
*
* ========Primary Functions:========
*Sceneview viewport inside XML
* TBD
*
*

* */
class LiveViewFragment : Fragment(R.layout.fragment_live_view)   {

    lateinit var sceneViewPort: ARSceneView
    lateinit var loadingView: View
    lateinit var instructionText: TextView
    lateinit var depthBtn : ToggleButton

    //Activity binding
    //Simply put, lets us access XML layouts of other activities
    private lateinit var LiveViewbind: ActivityLiveViewBinding

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
            it.getDescription(requireContext())
        } ?: if (anchorNode == null) {
            getString(R.string.point_your_phone_down)
        } else {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Gets the layout of LiveViewActivity
        LiveViewbind = ActivityLiveViewBinding.inflate(layoutInflater)







        //Gets the instruction Text ID(Changes text at top of screen nothing else)
        instructionText = view.findViewById(R.id.instructionText)
        //A loading icon that indicates plane scan (Currently not visible)
        //Acts as a flag for other components
        loadingView = view.findViewById(R.id.loadingView)

        //Starts a new ARCore session for the sceneviewPort
        sceneViewPort = view.findViewById<ARSceneView?>(R.id.sceneViewLive).apply {
            //Fairly simple, just enables planes to be rendered
            planeRenderer.isEnabled = true

            //Renders all  planes
            planeRenderer.planeRendererMode = PlaneRenderer.PlaneRendererMode.RENDER_ALL

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

                //Enables object detection
                //config.semanticMode = Config.SemanticMode.ENABLED
                depthBtn = LiveViewbind.tglBtnDepthAPI



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


            }
            //Self explanatory
            onTrackingFailureChanged = { reason ->
                this@LiveViewFragment.trackingFailureReason = reason
            }
        }



    }

    fun depthAPI(view : ARSceneView, session: Session, config : Config){


    }

    //Adding a new anchor based on a anchor position
    fun addAnchorNode(anchor: Anchor) {
        Toast.makeText(context, "Anchor", Toast.LENGTH_SHORT).show()
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
                        isLoading = false
                    }
                    //Finally, sets the AnchorNode to the current new Anchor
                    anchorNode = this
                }
        )
    }

}