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

 add a delete db button, need to add to it to delete the entire db, remember to delete crossRefs first

 or sql will block, also this will be an issue if I don't also delete the images it has created in internal
 storage.

+ return tasks +

* delete/change recipe/pantryItem image-> check for delete in storage
* expand import/export json for new entities associated with shoppingList and other changes
 - discover how to include the images in the import export. will it be a .zip of images, can it
 be put in the json possibly? stay tuned.


making sure the debug delete db delete entire db, checking all Daos for clearAll()

    everything is going well even found a mis-wire in the hilt with shoppingListItem and ShoppingListEntryDao mis matched

    so i created 2 entries for them.

    also added the removal of images the app creates on db wipe. testing now

    update images delete just fine now using the debug Repository to wrap all the clearAll()

    working where to clean up images leftover from deleting pantryItems or Recipes, also for when a new

    photo is saved over the old photo. need that's 4 deletes i reckon.

just noticed i need to re-work category at some point, I had planned to have the ability for the user

to add category's so i have a temp situation where I load them into the db so i am removing the category.clearAll()

worked deleting the db with the pictures in internal storage

currently working on finding and preventing orphaned images,

if i create a recipe and add a photo then save

followed by editing the recipe and chaning the image and saving,

upon deletion of that recipe I get an logCat ImageCleanUp false some times, which means that image

is left behind and will clutter up internal storage in the long run on this app,

I want a more hygienic internal storage. no file left behind semper file!

now i am hunting down orphans. using ImageCleanUp in log I am checking if any files are orphaned

well to define whats going on right now would be to say

change recipe image -> save -> delete = orphaned image
change recipe image -> save -> back -> select Recipe -> delete = no orphaned image

///////////////////////////////////////////////////////////////////
////////////// current work -> orphan hunter //////////////////////
///////////////////////////////////////////////////////////////////

orphan hunting is a little challenging, its only coming up in the recipe section also, when an image is

changed I could just pop the user back to recipe selection screen as a fast temp work around so that I

can move on to expanding import export which I suspect would be quick.  this does seem to point back

to the recipe feature as being a strong candidate for a re work.

the whole recipe section was the first frontier of this project, I do want to work around with recipes
0
more, after solving the image issue, but I need to finish the import export update first.

best I have so far is that delete db from the debug screen fully wipes all images,

as for pantryItems it appears perfectly hygienic as well

recipes not so much, mainly when you switch images and don't snap back to recipe selection screen

I cleaned up the flow so saving after an edit leaves the screen, good news you hold your place in the scroll

and can re select the recipe without searching. for now before I re-do the recipe features

/////////////////////////////
/////// next ////////////////
/////////////////////////////
// import / export / debug //
/////////////////////////////
//////// debug detour- the images it creates are just one image re referenced, I want to check something
a little more real world and varied, each recipe and pantryItem could have a unique image.

worked on the debug generating mock images instead of referencing the same one over and over,

I want to simulate the memory load on internal storage for different images.

adding random generated images takes more time so i adjusted the loading bar as to not feel like I

was waiting or frozen. it might be an idea to...

just finshed up debug to allow selecting gallery photo or camera photo or generate mock images

for the test data. all looks good on size, I now feel i have a good tool to set limits in the app.

// note sure if I noted but recipes is feeling just as snappy as pantry is lately,

the other night I found some code the was doubled up in a couple spaces along with a couple other

tweaks but it seems to handle volume better than before. I may have broken a new rule about focusing

on task at hand and may have changed some things in recipes while working with other things.

may still circle back to re working it if testing warrants it.

when not plugged in it for sure seems to need it i suppose recipes is still slow at volume now?

ohh jpeg quality went from 80 to 85 maybe that's the performance hit i see?

/////////////////////////////
////// import / export

current state of import export is better than I thought, there are however some things not being

backed up. report coming soon....

alright able to import and export the db now just need to work out zipping up the images

after some work i am now properly deleting the db images and am able to backup the db

images with zip up next.

well actually we are still working on the images, moving them out of the db has created a lot of

scenarios with orphaned images, I am identifying areas that need attention now. go to create a recipe

select and image then decide to discard it bam an orphan! will be adding logic to clean these up.

I am wondering if an overall orphanHunter is a good idea or not.

I have also been working to make sure that images created are deleted when recipes or pantry items are

created or modified and now need to include discarded.

okay so far recipes creation and deletion is orphan free

if you change the image of a recipe you will create an orphan for each switched image

pantry creates 2 files and only deletes 1 atm

if you create a pantryItem with image and save then change the image and save
again followed by a delete you will create 1 orphan image.

I know I also have an orphan state if i back out selecting an image due to the image saving right away

before the dialog save is used.

first up why is pantryItem saving 2 images lets crack this first before we worry about the other orphans

pantryItem is now only saving one, stale uistate from savedPath been replaced with current pickedUri

redefine current orphan points.


discarding a recipe with a selected image does not create orphan

both in create and edit screen switching an image creates and orphan for every switch

pantry switching image seems fine though, and creates no orphans

// okay after a night of chasing behaviour, i recall wanting to use coil,

I am going to go back an break apart some monolithic modules in RecipeDetailScreen, RecipeCreationFrom

and PantryScreen, as to better interact with the images and edit states. switching from byteArray to uri

and internal storage, was not planned out.. I kinda just started converting lol. stay tuned

I plan to start with poping dialogs out of recipedetailscreen first.

okay dialogs have been moved out, going to make a utility for the color picker next

..........
............

/////////////////////////////
////// curent    ////////////
/////////////////////////////
//// recipes rework /////////
/////////////////////////////

decided to modularize recipe detail screen in an attempt to nail down the images getting orphaned

the feature is quite orthogonal atm. The recipe creation form is working really well with image

creation and deletion. I moved a lot of stuff out of the nav graph, and moved some to the tabSwitcher

and edit guard is now more localized.

I have nailed down the tab switch, cancel and back navigation for discard changes in recipe creation

now to circle back to recipe detail and document what is not working.


currently working in recipe details going to take a couple days off will be thinking things over

try to implement an orphan hunter was not successful learned maybe need to understand how uris relate

to database and which are linked and which are orphaned...

well back after a couple days off not much progress made today

try'd couple things with orphan hunter no luck.

I do recall before I left that the recipe creation screen was nailed down

the detail screen got the edit guard working great and a lot of the filed deletion is on par

I think the issue still lies with switching multiple images and saving or discarding is the last

recipe memory leak. it is discouraging and a mystery why orphan hunter wont be my one stop fix,

but i press on to make an air tight system that wont need to rely on a clean up. I think I still

want the orphan hunter however in the long run as there may be things I cant for see. or possibly

a better cleanup system in general?

When I return I plan to map out again with log cat what is creating an orphan and what is

not I may even add an on screen toast or file log for on device testing.

found the cracks where files were falling through. decided to disable tab navigation during

recipe edit. it was causing more issue than good.

moving on to pantry to isolate behaviors that produce orphans

so far it appears to be the multiple image switching that creates orphans.

pantry screen has been tightened up,

I am back to needing a orphan hunter that actually works properly in the event of accidental orphans

app crash, app close at wrong time.

created an MediaOrphanHunter that runs at start up to delete any files not associated with the db

now on to import/export to zip all the images along with the json for the db.

I am hoping this all works well with the MediaOrphanHunter.

finished the backup up package. need to test between phones.

still haven't tested but took some time off and came back and finalized the import export

to use NDJSON I am now trying to load the max data from debugtools and import export that will take

a good while. ill report back!


as a note I should mention that we are crashing from rapid tab switched with the app this full

I think its like 10gb or more.

export worked now trying import on new ndjson.

I still need to look into paging possibly or set a hard limit on recipes.

I was very focused on scalability, but my have gotten carried away as I doubt I will have

5k recipes with 100 ingredients each, and 100k pantryItems. so this may be a non issue.

however if its a simple fix why not.

import/export worked even at max the NDJSON and chunking the data really helped.

I am going to restart to check if duplicate images from the mocking are were included

and or need to be deleted. thinking is this large data set seems to crash the app when moving too

rapidly i'd like to understand this better honestly. I could hide from it and set limits way below

this but it would be so cool to have a small tool that could lift the world if need be lol.

maybe unrealistic. but I wonder if its orthogonal enough maybe.....

at any rate dont forget to run cleanup after mocking images or exporting but no need after export

check if its the mocking or the exporting that is causing double images this may be the crash

culprit.


because I'll tell you what I am rapidly jumping around the app now on max imported data from mocked

images and its not crashing out the app? I think either the debug is making extra images or the

exporting is will be digging in on this.


### which process do I need  to run image clean up on?

// adding to check import -- no issue here with the single image v--

//Create mocking images -- did not create extra files no clean up needed v---

//Create mock data with real image -- only deleted on file on restart negligible v--

//Export db+images -- this too dose not trigger the clean up hmm v--

// using generate mocked images on import/export to see if there is extra whats odd

I dont seem to see the problem anymore with the extra images, I may have fixed this in the

zipImport / zipExporter update.

the final crash state I am seeing even at max data is the going from recipe details back to the

recipe screen.

going to run all these and then restart the app to see which on deletes images

once discovered I will call OrphanMediaHunter to delete the extra performance hogging files.

was not needed apparently. I am over loading the heap I should really use paging 3 lol.

its such a trade off.

I should add that although the above was a good search and pursuit the carsh is more to the fact

that we are running out of memory in the heap. I think in the past I knew when switching from the

blobs for the same reason that having a large dataset would cause OOM errors, and that paging would

be needed. we have improved the image handling with coil and async image. and provided a

MediaOrphanHunter to clean up the extra images. this has imported scalability also using

NDJSON along with chunking the data for loading to prevent from OOM errors.

I plan to apply paging 3 I think for recipes it should be RecipeScreen.kt and for

pantry it should be PantryScreen.kt I shall report back.

🧠 Why Paging Matters Here
Loading all recipes up front:
- Eats memory, especially with image previews.
- Delays initial render—no feedback until everything's ready.
- Makes filtering expensive if the dataset grows.
Paging solves this by:
- Loading only what's visible (plus a small prefetch buffer).
- Streaming in new data as the user scrolls.
- Giving you hooks for load state, retry, and audit overlays.


added some dependencies for paging 3 and room to work with paging 3

also update the RecipeDao and the viewModel,

recipes is a lot more responsive than pantry now. awesome I should do the same paging for

pantry items next however, fun and all I want to tweak my recipe debug and move it from 5k to 15k

I will then see how 10k handles lol. make it 15k its good to go need to hit pantryItems now for

paging 3.


update switched both recipe and pantry to paging 3. going to increase limits and overload it again.

current numbers 100 ingredients, 19k recipes, 100k pantry items --> about to generate and test.

upon testing it is snappier and more responsive, however switching between recipe and pantry will

result in a OOM error eventually I need a way to clear both between switches. I am going to pursue

leaky Canary to try and profile the issue.

with paging 3.

new numbers to try. 100 ingredients, 100k recipes, 500k pantry items -- insanity


##
## start here, pantry screen is crashing out may need to rollback. possibly figure way to
##   refresh memory when switching between tabs.

OOM occurs during pantry scroll due to simultaneous hydration of full

entity list and paged items. Duplicate checks and scan logic query entire

PantryItem set mid-scroll, triggering CursorWindow exhaustion. Fixes include

swapping to projection-based paging (PantryPreview), converting duplicate logic

to Set<String> checks, and isolating single-item lookups. Clean, scalable, and paging-safe.


The pantry scroll crash stems from Room overloading the memory heap by hydrating both a full

entity list and a paged dataset simultaneously. In PantryScreen, we were collecting a full

StateFlow<List<PantryItem>> for duplicate checks and scan code logic—while the paging source

(LazyPagingItems) was actively loading. Compose holds onto previously rendered items during scroll,

 and this dual pressure triggered CursorWindow exhaustion and an OutOfMemoryError. To fix it, we’re

  replacing heavy .any { ... } list scans with projection-based flows: one returns Set<String> for

   scan code and name detection, the other paginates a lean PantryPreview projection. All full-entity

   lookups are now isolated to one-off DAO calls by ID. These changes dramatically reduce memory usage,

   eliminate concurrent hydration, and make the pantry screen scalable even with large datasets.

// maybe break down PantryScreen first!! planning to break down pantry screen before implementing

Pantry screen has been broken apart. still crashing from massive datat set 120k plus. will be trying

again to implement paging 3.

need to hold scroll spot in recipes and pantry

also need to fix search, and naming

search has been reconnected, and naming is functioning correctly however I think my mock data

generator is somehow using same names. after some investigating it may have just been a data double

gen from running it twice possibly or some other user error. I did update the naming scheme for

mock data so it would appear sequential in the recipe and pantry screen. I changed the sliders to

remember there state while in app. The scan screen shrinks now on pause to make room for the sliders

and mock data loading. also codded in for the screen not to time out while scanning is paused

this is to cover import/export plus generate mock data. big files might time out otherwise.

currently loading in

15098 recipes with

100 ingredients

and 101617 pantry items

going to test the import and export

normal use seems solid.

I know export works well but import will crash without some work but I need to create the mock

data first.

update zip importer to handle larger files sizes, still tweaking this. I would like a better progress

tracker on deleting the db and the importer hangs at 39% i'd like to improve this as well.

have progress for delete instead of just hanging on 0%

I have noticed that the lazyColumns are not remembering there position on tab switches,

I will be working on this next.

so pantry screen was super easy just a little bit of boiler plate and it remembers its position

recipeScreen however was another beast due to the fact that its a couple composables.  after a lot

of digging we are now persiting it with a rememberSavable and using a launch effect.



at this point I am only noticing crashes when scrolling very fast and flipping between tabs, with

large data sets unrealistic data sets honestly. I am going to switch over to using things like

memory profiler and leakyCanary to see if the target data sets are working well.

I overloaded the data sets in testing to find the limits.

On a spare phone I created 500k pantry items 100k recipes with 100 ingredients each.

the app scrolled and tabbed just fine. It of course failed to export, as current export is not

suited for that volume. Honestly does it need to be? I think not this is for home use not warehouse

The data set I have been using and playing with on my phone has been about

109k pantry items 19k recipes with 100 ingredients, and it has been a smooth experience. A data set

of this size is still way beyond what a typical house would use. At this data set the app performs

with only slight hick up in scrolling fast and tab switching rapidly. This is something that could

be addressed if a data set of this volume or higher was needed, however this is not the use case, or

intention of that app. it has just been nice to see what can be done. I am making a smaller data

set now with 1k recipes at 100 ingredients each and 49k pantry items. This I consider still way too

high. But will be interesting to see if memeory issues need addressed still or if the app at a lower

data set will be battle hardened from working with the higher data set.

I have not done the best job recording the development but it has been growing both in size and

performance and size has driven said performance along the way.



any hoo I have a couple more odds and ends but I may try to figure out how to finalize this.
