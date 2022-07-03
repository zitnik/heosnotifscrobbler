# HEOS Last.fm Scrobbler

This is an ugly prototype implementation of a Last.fm scrobbling service getting song info from
Android notifications produced by the HEOS app (used on Denon/Marantz HEOS 
devices).

By ugly, I mean very hackish code. Well, we're getting song info by using reflection on the 
notification object to dig up song artist & title info. This level of ugly.

Currently works with HEOS (`com.dnm.heos.phone`) app version 2.47.350.

## Usage
Get your own API key for Last.fm (https://www.last.fm/api) and set `API_KEY` and `API_SECRET` 
constants in `LastFm.java` accordingly.
Also set your credentials in the `login` method in `LastFm.java`.

Build the app with Android Studio. Install the app, give it permission to read notifications, and 
(if "Service status" in the GUI shows "OK") it should work.
