# android-mapbox
A quick project that shows the power of MapBox for Android 


## Step 1 
Integrate MapBox SDK and show current user position

## Step 2 
Add a search bar to query addresses.
Used google autocompletion fragment.

## Step 3
Search for addresses using the map gesture.
Used Intent service for reversion geocoding because the operation may take some time. And Async are not deisgned for long processing.

## Step 4
Decided to use sharedpreferences to store the 15 last known location.
It might not be a good solution for performance .. consider using the SqlLite DB.

## Step 5
Made the refacto to be able to easily switch between different maps provider. change fragment using a framelayout.
I did not made the mirror implementation for the Maps provider. I'm only displaying the fragment.

## Step 6
Skipped Unit Test, Instrumented test are required.

## Step 7 
Designed the app with a better UX/UI, smooth transition and cool images.



