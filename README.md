![.](https://i.imgur.com/H9pDVkk.png)

# NPC Dialog Log <p> [![GitHub Workflow Status (branch)](https://img.shields.io/github/workflow/status/neilrush/Camera-Keys/Java%20CI/master?logo=github)](https://github.com/neilrush/Camera-Keys/actions) [![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/neilrush/Camera-Keys?include_prereleases&logo=github)](https://github.com/neilrush/Camera-Keys/releases) [![Plugin Installs](http://img.shields.io/endpoint?url=https://i.pluginhub.info/shields/installs/plugin/Camera-Keys)](https://runelite.net/plugin-hub/neilrush)

A RuneLite plugin that simply adds hotkey support for camera zoom and compass direction.

<img alt="." height="350" src="https://i.imgur.com/gIEA4QE.gif" title="Camera zoom demo" width="350"/>

<img alt="." height="350" src="https://i.imgur.com/cUZAA1F.gif" title="Compass direction demo" width="350"/>

## Configuration

![.](https://i.imgur.com/z5pjT9G.png)

###Zoom level
A Value between `-272` and `1300` with larger values being more "zoomed in".

<p> Note: The game only support some zoom levels with plugins enabled. With no plugins enabled, the range is <code>128</code> to <code>896</code>.
With the RuneLite camera plugin the max range can be increased to be <code>-272</code> to <code>1004</code>. The zoom range can be extended even
further with the <a href="https://github.com/Adam-/runelite-plugins/tree/zoom"> Zoom Extender plugin </a> to be  <code>-272</code> to <code>1400</code>
however I have found that values above <code>1300</code> can be buggy.</p>

###Zoom Key
The key that activates the set zoom level.

###Activation Type
Changes the behavior of the zoom key. Values are:
* Hold: Activated when pressed and deactivated when released.
* Toggle: Activated when pressed then deactivated on the next press.
* Set: Simply sets the zoom to the configured value when the key is pressed.
<p> Note: zoom state will cancel if you manually change it away from the set point</p>

###Zoom Icon
Displays an icon indicating the zoom activation state.

![.](https://i.imgur.com/MEjdkzx.png)

###Compass keys
The keys that bind to different cardinal directions
