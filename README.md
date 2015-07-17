# PodcastPlayer 0.1
Android application for bookmarking Audiobooks & podcasts. Easily keep of progress for multiple books and shows.

![](http://julianrosser.website/images/app_screenshots/PodcastPlayer0-1.png)

Next Release (0.2)
- Fix current bugs in TODO

TODO
- Make tracker thread finish on exit, start again when needed.
- Build function for deleting bookmarks from SQL DB
- Play last button should skip to beginning of song, or skip back if less than 3 secs.
- When skipping next from paused, track plays. It should use 'startup' boolean.
- 'load files on refresh normally Including button!' - What did this mean?
- Record play history, open last played when opened.
- Don't repeat song when playing random.

Log
- 18/07 - Build SQL Database, Database helper class, linked to CursorAdapter and made add/find functions. Fixed back button crash & incorrect play/pause image.
- 17/07 - Implemented shuffle mode. Completely cleaned, documented and revised MainActivity, MusicService & PlayerFragment. Moved TODO, Log & notes to README file.
- 16/07 - Resumed work on this project. Prevented auto play when first opened. Spent a few hours going over code.
- 18/06 - Implemented bookmarks. Took a few hours to build runOnUiThread. App works well as music player, need to build specialized features now.
- 13/06 - Worked on converting time for display. Put method in service, so it's only called when needed, no need to do this for every song.
- 12/06 - Built library ListView fragment, layout view, added title and activity callbacks. Duration textview and code.
- 11/06 - Started log. Opened project for the first time in weeks, NOT a good idea to leave unfixed bugs, took me a while to debug.

Notes
- ListView - have artist/title on seperate lines to use up space, show percentage/time at right side?
- Should database be opened and closed on data change? would this affect the cursor?
- Should the db be in main activity? Should there just be the DB and no array list? Yes probably
- Should adding a bookmark overwrite old or create new??
- Expand SeekBar on touch for easy seeking?
- Should only update seek bar when in view?
- Use listener to check for play/pause status and update ImageButton?
- Make ShuffleMode a preference, not simple boolean.
- Name - 'PodCats - Audiobook & Podcast bookmarker'     ??
- Should I make intent & fragment references private/hidden?

