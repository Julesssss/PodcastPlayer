# PodcastPlayer 0.3
Android application for bookmarking Audiobooks & podcasts. Easily track progress of multiple audiobooks and shows.

![](http://julianrosser.website/images/app_screenshots/pp_4.png) ![](http://julianrosser.website/images/app_screenshots/pp_8.png)

TODO
- Don't touch views before big file has finished loading or it'll skip track
- Update dialog title with timer
- Take sorting into account when Bookmark.onCLick()
- how does Rocket player handle track name being too long?? duration is hidden in bookmarks
- show percent in playerFrag??

Log
- 02/08 - Sorting DialogFragment and custom views.
- 01/08 - Added option to sort bookmarks, using SharedPreference. Exit option now in NavDrawer. Fixed delete bug, wasn't checking position. Fixed pause noti bug.
- 31/07 - Designed NavDrawer custom adaptor view, material design icons, item layout & font. Fixed Sting formatting for currentPosition.
- 30/07 - Added percent icon to Bookmark ListView & DB. Display message if bookmarks are empty. Updated Dialog style.
- 29/07 - Made SaveBookmarkDialog class & custom view. List shows notes if available. Material grey re-design. Notification can be dismissed when paused & icon changes.
- 28/07 - Activity destroyed & Re-Created without effecting playback. Material design for ListViews. Exit button. Fixed getDuration while prepping bug.
- 27/07 - App opens to last played song, at last played position. Play time formatted correctly. Various SeekBar bugs. Added Scrollbar to ListView.
- 26/07 - UI updates to Library and Bookmark Lists. Fixed display bugs where SeekBar reset to 0. Cleaned rest of project.
- 25/07 - Delete Bookmarks with context menu. Logic to prevent no tracks on device crash. Fixed back button bug. String format for tracks over an hour. Tidied MA, MS, DOH & Song.
- 24/07 - Load last bookmarked song from Bookmark DB. Tracker Thread now starts when music starts. Updated screenshot.
- 20/07 - Combined trackers to one Thread, Thread lifecycle now matches PlayerFragment. Finally fixed MediaPlayer -380 bugs, player wasn't initialized properly.
- 19/07 - Skip to beginning of the track if < 3secs. Fixed SeekBar update bug.
- 18/07 - Created basic buttons to replace menu options. Update title when opening bookmark.
- 17/07 - Build SQL Database, Database helper class, linked to CursorAdapter and made add/find functions. Fixed back button crash & incorrect play/pause image.
- 16/07 - Implemented shuffle mode. Completely cleaned, documented and revised MainActivity, MusicService & PlayerFragment. Moved TO-DO, Log & notes to README file.
- 15/07 - Resumed work on this project. Prevented auto play when first opened. Spent a few hours going over code.
- 18/06 - Implemented bookmarks. Took a few hours to build runOnUiThread. App works well as music player, need to build specialized features now.
- 13/06 - Worked on converting time for display. Put method in service, so it's only called when needed, no need to do this for every song.
- 12/06 - Built library ListView fragment, layout view, added title and activity callbacks. Duration textview and code.
- 11/06 - Started log. Opened project for the first time in weeks, NOT a good idea to leave unfixed bugs, took me a while to debug.

Notes & things to do later
- Library context menu: delete all BM, add BM, play, delete, play next
- If I change the bookmark format, will be hard to implement new tables. Formatting of displayed bookmarks, should formatt when needed.
- Should % be kept as sql row? or formatted when displaying from data??
- Is shuffle even needed?
- Test This: When opening fragment, add to backstack if not playerfragment
- Wording for Dialog - "Keep old bookmarks", "Replace old bookmark?", "Remove old Bookmarks"??
- Get widget to fire Pending Intent that is recognised by Service
- Does GitHub project load properly?
- Check delete works properly
- What should I do about
- The order of song-list might change, wrong 'LastSong' will be loaded. Does this matter?
- When loading big file, seekBar / timer aren't set until song is ready, problem??.
- might crash if song list changes or song changes, test
- Memory slowly fills, play stutters, then space is freed up. Is this normal?
- ListView - show percentage/time at right side?
- Name - 'PodCats - AudioBook & Podcast bookmarker'     ??
- Find better Unique ID? media_id changes if files move or get renamed, how does other apps handle this?

