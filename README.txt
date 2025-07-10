Had started out using SQLHelper in java, switched over to Kotlin code for room using Kotlin DSL 
Started using DAO’s Repository’s and ViewModels along with jetpack compose, previously was using fragments
 in activity’s, with xml layouts.
Changed overall architecture for feature base MVVM feature based

I began using Hilt with Kapt, which translated Kotlin code into java for creating Dependency Injection,
later found out about KSP which apparently works directly with Kotlin boast 2x build speeds, so I switched to this

Implemented a NavGraph using navHostControllerto navigation the app, added tab layout,
also created a EditGuard to prevent losing changes this interacts with navigation.

App is almost finished still need to do more work on the shopping list -> will be creating the current version
shopping list before going further to optimize. The Optimization is a big over kill at this point, due to target
 audience my wife and I, the app has proven reliable at the scale we will most likely use it at. But I want to see
 this to the fullest so after I get the last bits of the grocery list nailed down its on to making version 2
 and getting the Blobs replaced with URI

*maybe update the slider so I can get a bit more granular and find the numbers things crash at.

I am using a utility to create data for testing limits of the app, Currently I can load in 5k ingredients
along with 1k recipes each having 50 ingredients cross linked from the 5k ingredients.

I seem to have to problems loading the entire 5k Ingredients but have crashes and out of memory errors
in the Recipe section, I am looking over this view model and daos to check for problems and comparing them
to the smooth and optimized pantry model. I have done a hand full of things to optimize pantry model with
the help of co-pilot and will be doing the same to analyze RecipeViewModel a ViewModel that was the first,
 before I even knew really what a ViewModel was. I also assume in the recipe composables I am probably not
  using any of the lazy elements and will probably find gains there, it would also probably seem I am using
  Flow + stateIn() for all items in pantry but loading full objects into memory for Recipes, I am excessively
   loading images and objects in the recipe’s tab

After I optimize this I will be moving the blobs out of the data base in favor of uri and storing the
image files local to the device, I plan on employing pageing 3 along with coil for displaying the images
I may already have the coil and compressiong under jpeg with the image utilis I’d have to double check.
 After the objects are moved out I will be updating the import/export functionality to now include the
 images that in internal storage, so that a full back up and import can occur. This will see the json
 going to version 2, and the db will need a new migration strategy as it moves from version 1 to version 2.
  (side thought this kinda indirectly is teaching me about how apps are updated in the real world and why an
   app may not be fully backwards compatibable, for every version my app would be compatibable with it would
   also need a mirgration strategy pretty much telling all of the eveolutions of its database.)


I dont want to switch to coil yet as id rather streamline things before switching, it's my thinking that
 this will all work at the 5k and 1k with 50 ingredients mark, if I optimize recipes, if that works I feel
  I will have a fundamentally robust program, then will switch over to paging and coil using file paths

