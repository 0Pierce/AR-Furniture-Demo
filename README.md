# AR-Furniture-Demo 
An Android Augmented Reality app(Demo) designed to let users place furniture and other objects into an empty room using precise measurements
Not all functionality is working due to library updates


# Navigating the project 
To find the src files of the project navigate to ``` app -> src -> main -> java/com/exceptionhandlers/avante_ar```
To find used resources such as the xml layout files, images, and 3D objects navigate to ``` app -> src -> res ```

We have two branches ``` production ``` is our stable and fully tested branch, which holds older versions of the project.
The other branch is ``` development ``` which holds the most up-to-date version of the project and is experimental/beta

# Functionality & Classes

* ```Login/Signup Activities```
  - Straightforward Login/SignUp activities using Firebase Authentication for logging in and registering users

* ```HomePageActivity```
  - The homepage of the activity, currently holds 4 buttons for simple navigation, the camera viewport is the only accessible one right now

* ```LiveViewActivity```
  - The primary activity of this project which holds the (```ARSceneviewport```) that facilitates the AR functionality of the app
  - Most logic involving the 3D models, anchors and anything else involving the camera viewport is done here

  
* ```LiveViewCatalogue```
  - A pop-up scrollable menu which allows the user to select a variety of objects to be placed, currently not fully functional as we focus on the depth API

* ```Depth Package```
  -  Handles the collection of depth data from the camera viewport

 # Resources
 We did a lot of research on what kind of libraries and APIs we need to consider adding to make our lives a little easier. The following are the resources that we are actively using within the project. We will most likely expand this list as we continue

* [ARCore](https://developers.google.com/ar)
  - ARCore is Google’s augmented reality SDK offering cross-platform APIs to build new immersive experiences on Android, iOS, Unity, and Web
  - The workhorse of the app, lays a foundation for all AR capabilities which can be built upon

* [SceneView Library](https://github.com/SceneView/sceneview-android)
  - 3D and AR Android @Composable and layout view with Google Filament and ARCore
  - Handles the rendering of 3D objects within the viewport


 

