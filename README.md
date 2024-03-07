# Avante-AR #0969DA
An Android Augmented Reality app designed to let a user place furniture and other objects into an empty room using precise measurements


# Navigating the project #0969DA
To find the src files of the project navigate to ``` app -> main -> java/com/exceptionhandlers/avante_ar```
To find used resources such as the xml layout files, images, and 3D objects navigate to ``` app -> src -> res ```

We have two branches ``` production ``` is our stable and fully tested branch, which holds older versions of the project.
The other branch is ``` development ``` which holds the most up-to-date version of the project and is experimental/beta

# Functionality & Classes

* ```HomePageActivity```
  - A simple landing screen to facilitate testing and in and out access to the AR viewport

* ```LiveViewActivity```
  - The primary activity of this project which holds the fragment(```LiveViewFragment```) that facilitates the AR functionality of the app
  - It also allows us to have greater control of navigation and other activities/elements that do not need to be within the viewport itself
  
* ```LiveViewFragment```
  - A fragment which's only purpose is to hold the SceneView viewport (camera view). All logic pertaining to rendering and object interaction is held here

* ```LiveViewCatalogue```
  - A pop-up scrollable menu which allows the user to select a variety of objects to be placed, currently not fully functional as we focus on the depth API

 # Resources
 We did a lot of research on what kind of libraries and APIs we need to consider adding to make our lives a little easier. The following are the resources that we are actively using within the project. We will most likely expand this list as we continue

*
 

