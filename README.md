![.](https://i.imgur.com/H9pDVkk.png)

# Camera Keys ![GitHub Workflow Status (with event)](https://img.shields.io/github/actions/workflow/status/neilrush/Camera-Keys/javaCI.yml) [![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/neilrush/Camera-Keys?include_prereleases&logo=github)](https://github.com/neilrush/Camera-Keys/releases) [![Plugin Installs](http://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/installs/plugin/camera-keys)](https://runelite.net/plugin-hub/neilrush) [![Plugin Rank](http://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/rank/plugin/camera-keys)](https://runelite.net/plugin-hub)

A RuneLite plugin that simply adds hotkey support for camera zoom and compass direction.

<p><img alt="." height="350" src="https://i.imgur.com/gIEA4QE.gif" title="Camera zoom demo" width="350"/>
<img alt="." height="350" src="https://i.imgur.com/cUZAA1F.gif" title="Compass direction demo" width="350"/></p>

## Configuration

![.](https://i.imgur.com/z5pjT9G.png)

<h3>Zoom level</h3>
<p>A Value between <code>-272</code> and <code>1300</code> with larger values being more "zoomed in".</p>

<p> Note: The game only supports some zoom levels with addtional plugins. With no plugins enabled, the range is <code>128</code> to <code>896</code>.
With the RuneLite camera plugin the max range can be increased to be <code>-272</code> to <code>1004</code>. The zoom range can be extended even
further with the <a href="https://github.com/Adam-/runelite-plugins/tree/zoom"> Zoom Extender plugin </a> to be  <code>-272</code> to <code>1400</code>
however I have found that values above <code>1300</code> can be buggy.</p>

<h3>Zoom Key</h3>
The key that activates the set zoom level.

<h3>Activation Type</h3>

Changes the behavior of the zoom key. Values are:
* Hold: Activated when pressed and deactivated when released.
* Toggle: Activated when pressed then deactivated on the next press.
* Set: Simply sets the zoom to the configured value when the key is pressed.
<p> Note: zoom state will cancel if you manually change it away from the set point</p>

<h3>Zoom Icon</h3>
<p>Displays an icon indicating the zoom activation state.</p>

![.](https://i.imgur.com/MEjdkzx.png)

<h3>Compass keys</h3>
<p>The keys that bind to different cardinal directions</p>
