Had started out using SQLHelper in java, switched over to Kotlin code for room using Kotlin DSL 
Started using DAO’s Repository’s and ViewModels along with jetpack compose, previously was using fragments
 in activity’s, with xml layouts.
Changed overall architecture for feature base MVVM feature based

I began using Hilt with Kapt, which translated Kotlin code into java for creating Dependency Injection,
later found out about KSP which apparently works directly with Kotlin boast 2x build speeds, so I switched to this

Implemented a NavGraph using navHostController navigation the app, added tab layout,
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
I may already have the coil and compressing under jpeg with the image utilis I’d have to double check.
 After the objects are moved out I will be updating the import/export functionality to now include the
 images that in internal storage, so that a full back up and import can occur. This will see the json
 going to version 2, and the db will need a new migration strategy as it moves from version 1 to version 2.
  (side thought this kinda indirectly is teaching me about how apps are updated in the real world and why an
   app may not be fully backwards compatible, for every version my app would be compatible with it would
   also need a migration strategy pretty much telling all of the evolution of its database.)


I dont want to switch to coil yet as id rather streamline things before switching, it's my thinking that
 this will all work at the 5k and 1k with 50 ingredients mark, if I optimize recipes, if that works I feel
  I will have a fundamentally robust program, then will switch over to paging and coil using file paths

did work on creating new ingredients from recipe screen and adding to shopping list, this creates a dialog to infrom that a pantry item was created and that the quantity in this manner is 0.

also updated add item in shopping list to always default to 1

currently working on various UI elements, I will get to the blob removal later down the road after a bit more UI polish

ui polish is almost satisfied, need to do some more testing of the app, I found a logic error the other day
that was not properly thought out. when adding a new ingredient to a recipe it was creating a pantry item
with  1 quantity, has been like  the for a long time, it wasn't until I added the same feature in the shopping list
that I realized by adding a new ingredient via the recipe creation or shopping list additional item, I was not actually
obtaining those items in my inventory, they were in actuality a place holder that should have a quantity of 0.
this was a small over look but glad I saw it as now things concerned with inventory are honest now. I even added
in some UI prompts to let the user know this is happening.

working on versioning, using 2 phones atm, one is using one ui 7....         android:allowBackup="false"
                                                                             android:fullBackupContent="false"

using backup false is a must or else db persists on one ui 7.

current thinking is that my phone will be the development part, while the one ui 7 phone will be put into real use

creating recipes and pantry Items, I consider both version 1 compatible as long as the one ui7 phone can export a file

that my development phone can import.  I will be moving blobs out of the db later on and will be implementing a new

db and json version along with migration strategies.

currently both apps are current and standing at version 1 still.

I will be working on the shopping list screen, after a little work this morning adding in the ability to

add recipes/ingredients to the list I have decided to streamline the screens, no more creating a shopping list while

adding recipes/ingredients, I am shrinking this to just a naming dialog, followed by an empty list that the user will

populate. as note I should also add in ability to delete items and modify quantity ie reduce.

still tweaking the shopping list, recipes add correctly now, ingredients add correctly now.

I want to implement a way to track which recipes have been added to the shopping list and the ability

to delete an item from the shopping list.

thinking of redoing the shopping list from the ground up. I will need a new entity for tracking recipes that made
the shopping list

revamped the shopping list, previously it was more of a create and set, now its a dynamic make as you go.

I owe this quick turn around to spending a day talking out the flow of the shopping list, and creating a flow with
co-pilot.


Shopping Tab Flow Summary (Finalized and Refined)
 Create a Shopping List
•	Tap + to begin a new list
•	Prompt user to enter a unique name
o	Duplicate names rejected with inline message
•	On confirm:
o	List saved via Room
o	Appears immediately in overview
•	Long-press any list item:
•	Dialog: “Delete list?”
•	Confirm → removes list from storage
Open a Shopping List
•	Tap to enter the selected list view
•    Selected Recipes section (collapsible):
•	Each recipe row includes:
•   Name
•	Count badge: ×N
•	Tap to view/edit
•	Long-press → remove one instance
•	Count decrements: ×N → ×(N−1)
•	Shopping list recalculated accordingly
Add Recipes or Ingredients
     Add a Recipe
•	Tap + → select a recipe
•	App retrieves:
o	Recipe ingredient requirements
o	Pantry stock at that moment
•	Calculates:
•	Total required for ×N batches
•	Subtracts owned quantities
•	Adds only what's still missing to the shopping list
    Stateless logic → no need to track contributions per instance
    Shopping list becomes dynamic reflection of current gaps
Delete a Recipe
•	Long-press → delete one instance of recipe
•	App recalculates total ingredient need for remaining ×(N−1) batches
•	For each related ingredient:
•	Checks if any quantity was manually removed earlier
•	Subtracts only the remaining active contribution
•	Prevents over-subtraction or negative values

 Add Manual Ingredient
Step 1: Tap + → “Add Ingredient”
Step 2: Material 3 SearchBar appears
•	Search strictly by ingredient name
•	No category or quantity previews
Step 3: No match found
•	Create a temporary PantryItem (not saved yet):
PantryItem(name = input, category = defaultOrSelected, quantity = 0, addToShoppingList = true)

Step 4: Confirmation Dialog
•	“Create New Pantry Item?”
•	“‘Coconut Cream’ will be added with zero quantity and flagged for shopping. Continue?”
•	Confirm → saves item to Room, adds to shopping list
•	Cancel → discards temporary entry
Step 5: Behind the Scenes
•	Only confirmed entries persist
•	Cancel avoids pollution
•	Ingredient can be deleted manually if needed—undo supported
    Ingredient Layout & Interaction
    Categorized View
•	Ingredients merged across recipe contributions
•	Grouped by category: Produce, Pantry, Dairy, etc.
•	Each item displays:
•	Name
•	Total quantity needed
•	️ Checkbox → mark “found”
 Toggle
•	“Hide Found Items” → hides checked entries dynamically
    Edit, Delete, Undo/Redo
    Ingredient
•Long-press → remove from shopping list
•	Tracked with manuallyRemoved = true if related to a recipe
•	Undo stack logs this override
Recipe
•	Long-press → remove recipe instance
•	App subtracts only what that instance contributed
•	Adjusts logic to respect any manually removed ingredients
•	Ensures accurate totals without duplication or underflow
Undo/Redo Stack
•	Tracks:
o	Recipe adds/deletes (by count)
o	Ingredient edits/removals
o	Manual overrides
•	Behaviors:
o	Undo reverses last action (e.g. restore deleted ingredient)
o	Redo reapplies undone change
o	Recipe deletions undo both recipe and ingredient recalculations
•	Optional: Persist across sessions for reliability
Data Persistence (Room)
•	Room database stores:
o	 Shopping Lists
o	 Recipe selections + count
o    Pantry items
o    Shopping list ingredients
o	 Undo history
•	Fully survives:
•	Tab switches
•	App restarts
•	Session resumes
•	Open and re-close

looks like I am staring down the blob issue now.

note: need to fix import/export after database shift, will most likely avoid doing migrations and just call this v1 lol
were are still in development after all.

alright gonna plan this out again, that really helped last time ha ha. Sometimes its just fun to grab co-pilot and
inherit a bunch of technical debt, only once I immersed in the problem with tools in hand can I properly plan lol.

So the big Blob move to uri, and possibly paging 3. Goal being a more robust and scalable app

I am going to start by identifying the entities that use blobs.

Recipe.kt uses ByteArray val imageData: ByteArray? = null, RecipeDao as well

PantryItem.kt uses ByteArray  val imageData: ByteArray? = null, PantryItemDao as well

okay this may be a smaller hurdle then I had guessed.

I will be doing internal storage ....

alright wow the turn around lately has been insane, last night I switched over all the byteArrays to uri

I am beyond stocked atm this has given an extreme performance gain, I will admit recipe could still use a look

over to truly optimize, may come back for paging possibly.

originally I had just about finished all 4 tabs of the app and was good to go. so I decided to do some testing

a function to create fake recipes with x amount of ingredients, and fake ingredients, both having images.

at about 100 pantrys items, and 50 recipes with 50 ingredients to each recipe, the app was crashing hard.

mostly in the recipe tab, as the image is probably being called to many times. and as I have learned

ByteArray is fat in a DB out of memory errors prevented any true scalability so I searched and got put

on to uri and local storage. so i went through all the code starting at the recipe and pantry item entities

switching the byteArray over to uri. then daos then repository and view model and endless composable,

I had an interesting approach to finding what I needed to change. I started as I mentioned then found myself

adrift so i hit the play/debug button the IDE began the process of build and compile and would throw an

error for all occurrences of the byteArray which was now uri. I figured that by working at the base it would

ripple through. eventually it became clear to me that I would need to work in my core/utilis/imagePicker and imageUtilis

also due to the time and learning with copilot many features the 4 tabs really are all built a bit different. the

ImagePicker is the main engine but there is also a RecipeImagePicker for the ui of that tab, and unfortunately there

is an image picker buried in the monolith that is pantryScreen also look to pantryUI future note to self.

any ways, happy to report that I seem to be able to load large amount of recipes and pantry items talking

5k pantry Items, 1k recipes, 50 ingredients per recipe. That's way beyond what I expect to be in use the recipe

end least for my use. But its cool to see how a change in architecture such as byteArray to uri in my room db

can have huge effects on scalability and performance. also want to say that I am right now adding more and more

I think I am about to hit the 15k mark on pantry items and 3k recipes loading now..........

this testing aside the next phase will be to update the import export to account for all the new uri and

anything else that may be new and then we will mint this version 1. as a note the 2 phone thing has been

temp suspended while the shopping list was reworked along with this byteArray -> uri Transition.

so many db changes I knew a migration strat would be needed or a hold on version 1 I chose version 1

I deal with the mess of migration, its ascetics I just don't like the look I get the use if we go playStore

no doubt for updates.

any hoo v1 kicks off after fixing of import/export json along with imageFolder.zip or some other variation I will

discover. but the idea is to take the photos along with the db.

testing note. 20k pantry items, 4k recipes, 50 ingredients per recipe. runs normal amazing.

gonna add a button to delete the db for me too.

ohh ohh another point before import/export need to make sure the images are deleted in internal

storage when i delete a recipe or pantryItem or change the image too i guess. so that it dosent

clog up internal storage. then on to import/export.

and the final dream goals are to be setup for playstore maybe and/or google drive api for shared db file.

I have learned that my repository layer works as the interface between internal and external data access

like the db. might be fun to sync dbs with the online db so my wife and I could have a fully synced db.

not really needed but sounds like a fun project.

+ return tasks +

* delete/change recipe/pantryItem image-> check for delete in storage
* expand import/export json for new entities associated with shoppingList and other changes
 - discover how to include the images in the import export. will it be a .zip of images, can it
 be put in the json possibly? stay tuned.



