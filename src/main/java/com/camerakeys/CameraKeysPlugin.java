/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * Copyright (c) 2018, Abexlry <abexlry@gmail.com>
 * Copyright (c) 2021, neilrush <neileorushio@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.camerakeys;

import com.google.inject.Provides;
import java.awt.Color;
import java.util.Objects;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ScriptID;
import net.runelite.api.VarClientInt;
import net.runelite.api.VarClientStr;
import net.runelite.api.Varbits;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.keyremapping.KeyRemappingPlugin;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;


@Slf4j
@PluginDescriptor(
	name = "Camera Keys",
	description = "Adds hotkeys for camera zoom and direction",
	tags = {"Camera, Hotkeys, Accessibility"}
)
@PluginDependency(KeyRemappingPlugin.class)
public class CameraKeysPlugin extends Plugin
{

	/**
	 * The string to display when chat is locked.
	 */
	private static final String PRESS_ENTER_TO_CHAT = "Press Enter to Chat...";

	private static final String SCRIPT_EVENT_SET_CHATBOX_INPUT = "setChatboxInput";
	private static final String SCRIPT_EVENT_BLOCK_CHAT_INPUT = "blockChatInput";

	/**
	 * The allowed deviation from the set zoom level before the zoom is canceled.
	 * About 3 "scroll wheel clicks"
	 */
	private static final int ZOOM_CANCEL_THRESHOLD = 50;

	private static final String KEYREMAPPINGPLUGIN_NAME = "keyremappingplugin";

	/**
	 * The script id for toplevelcompassop
	 * <p>
	 * The script that handles setting the compass direction.
	 * <p>
	 * from <a href="https://github.com/runelite/runelite/blob/2b5ea1f0b5c09011ce95a93b01baa2bcf3438895/runelite-client/src/main/scripts/ToplevelCompassOp.rs2asm"> TopLevelCompassOp.rs2asm</a>
	 */
	private static final int COMPASS_SCRIPT_ID = 1050;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private KeyManager keyManager;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private CameraKeysListener cameraKeysListener;

	@Inject
	private CameraKeysConfig cameraKeysConfig;

	@Inject
	private CameraKeysOverlay cameraKeysOverlay;

	@Inject
	private KeyRemappingPlugin keyRemappingPlugin;

	/**
	 * If the user is typing in the unlocked game chat
	 */
	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private boolean typing;

	/**
	 * Enabled status of the features duplicated from the KeyRemappingPlugin.
	 * Should be disabled while the KeyRemapping Plugin is enabled for compatibility reasons.
	 */
	@Getter(AccessLevel.PACKAGE)
	private ChatInputHandlingState chatInputHandlingState = ChatInputHandlingState.DISABLED;

	/**
	 * The current state of the zoom level state machine.
	 */
	private ZoomState zoomState = ZoomState.OFF;

	/**
	 * The zoom level before the zoom key was activated
	 */
	private Integer prevZoomLevel = null;

	/**
	 * The zoom level that was achieved after the zoom was activated.
	 * May not match the config value if it is lower or higher than is possible.
	 */
	private Integer newZoomLevel = null;


	@Override
	protected void startUp() throws Exception
	{
		//Handle chat locking if the keyremapping plugin isnt already
		chatInputHandlingState = pluginManager.isPluginEnabled(keyRemappingPlugin) ? ChatInputHandlingState.DISABLED : ChatInputHandlingState.ENABLED;

		if (chatInputHandlingState == ChatInputHandlingState.ENABLED)
		{
			clientThread.invoke(() ->
			{
				if (client.getGameState() == GameState.LOGGED_IN)
				{
					typing = false;
					lockChat();
					// Clear any typed text
					client.setVar(VarClientStr.CHATBOX_TYPED_TEXT, "");
				}
			});
		}
		else
		{
			//keyremapping is enabled so sync up typing state by checking chat contents
			Widget chatboxInput = client.getWidget(WidgetInfo.CHATBOX_INPUT);
			if (chatboxInput != null)
			{
				String chatboxInputText = chatboxInput.getText();
				int index = chatboxInputText.indexOf(':');
				if (index > -1)
				{
					typing = !chatboxInputText.substring(index).equals(": " + PRESS_ENTER_TO_CHAT);
				}
				else
				{
					typing = false;
				}
			}
			else
			{
				typing = false;
			}
		}

		keyManager.registerKeyListener(cameraKeysListener);
	}

	@Override
	protected void shutDown() throws Exception
	{
		if (chatInputHandlingState == ChatInputHandlingState.ENABLED)
		{
			clientThread.invoke(() ->
			{
				if (client.getGameState() == GameState.LOGGED_IN)
				{
					if (zoomState == ZoomState.ON)
					{
						log.debug("Zoom level change: " + prevZoomLevel + " <-- " + getZoom());
						clientThread.invoke(() -> client.runScript(ScriptID.CAMERA_DO_ZOOM, prevZoomLevel, prevZoomLevel));
					}
					unlockChat();
				}
			});
		}

		overlayManager.remove(cameraKeysOverlay);

		keyManager.unregisterKeyListener(cameraKeysListener);
	}

	@Subscribe
	public void onScriptCallbackEvent(ScriptCallbackEvent scriptCallbackEvent)
	{
		if (chatInputHandlingState == ChatInputHandlingState.ENABLED)
		{
			switch (scriptCallbackEvent.getEventName())
			{
				case SCRIPT_EVENT_SET_CHATBOX_INPUT:
					Widget chatboxInput = client.getWidget(WidgetInfo.CHATBOX_INPUT);
					if (chatboxInput != null && !typing)
					{
						setChatboxWidgetInput(chatboxInput, PRESS_ENTER_TO_CHAT);
					}
					break;
				case SCRIPT_EVENT_BLOCK_CHAT_INPUT:
					if (!typing)
					{
						int[] intStack = client.getIntStack();
						int intStackSize = client.getIntStackSize();
						intStack[intStackSize - 1] = 1;
					}
					break;
			}
		}
	}

	@Subscribe
	public void onClientTick(ClientTick clientTick)
	{
		checkForZoomUpdate();
		checkForZoomCancel();
		checkForChatLockUpdate();
		checkForOverlayUpdate();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		checkForKeyRemappingPluginChange(configChanged);
	}

	/**
	 * Check if the chat box has exclusive input.
	 * <p>
	 * Note: The world map search will take the input from the chat.
	 *
	 * @return If the chatbox has the exclusive input.
	 */
	boolean chatboxFocused()
	{
		Widget chatboxParent = client.getWidget(WidgetInfo.CHATBOX_PARENT);
		if (chatboxParent == null || chatboxParent.getOnKeyListener() == null)
		{
			return false;
		}

		// the search box on the world map can be focused, and chat input goes there, even
		// though the chatbox still has its key listener.
		Widget worldMapSearch = client.getWidget(WidgetInfo.WORLD_MAP_SEARCH);
		return worldMapSearch == null || client.getVar(VarClientInt.WORLD_MAP_SEARCH_FOCUSED) != 1;
	}

	/**
	 * Check if a dialog is open that will grab numerical input, to prevent F-key remapping
	 * from triggering.
	 *
	 * @return if conflicting dialog is open
	 */
	boolean isDialogOpen()
	{
		// Most chat dialogs with numerical input are added without the chatbox or its key listener being removed,
		// so chatboxFocused() is true. The chatbox onkey script uses the following logic to ignore key presses,
		// so we will use it too to not remap F-keys.
		return isHidden(WidgetInfo.CHATBOX_MESSAGES) || isHidden(WidgetInfo.CHATBOX_TRANSPARENT_LINES)
			// We want to block F-key remapping in the bank pin interface too, so it does not interfere with the
			// Keyboard Bankpin feature of the Bank plugin
			|| !isHidden(WidgetInfo.BANK_PIN_CONTAINER);
	}

	/**
	 * @param widgetInfo the widget to check.
	 * @return if the hidden property is true. Will return true if the widget is null;
	 */
	private boolean isHidden(WidgetInfo widgetInfo)
	{
		Widget w = client.getWidget(widgetInfo);
		return w == null || w.isSelfHidden();
	}

	/**
	 * @return the camera zoom level from the client vars
	 */
	private int getZoom()
	{
		return client.getVar(VarClientInt.CAMERA_ZOOM_FIXED_VIEWPORT);
	}

	/**
	 * Lock the chat by clearing input and setting it to PRESS_ENTER_TO_CHAT
	 */
	void lockChat()
	{
		Widget chatboxInput = client.getWidget(WidgetInfo.CHATBOX_INPUT);
		if (chatboxInput != null)
		{
			if (chatInputHandlingState == ChatInputHandlingState.ENABLE)
			{
				chatInputHandlingState = ChatInputHandlingState.ENABLED;
			}
			setChatboxWidgetInput(chatboxInput, PRESS_ENTER_TO_CHAT);
		}
	}

	/**
	 * Unlocks the chat by setting the chatbox input back to "rsn: *"
	 */
	void unlockChat()
	{
		Widget chatboxInput = client.getWidget(WidgetInfo.CHATBOX_INPUT);
		if (chatboxInput != null)
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				final boolean isChatboxTransparent = client.isResized() && client.getVar(Varbits.TRANSPARENT_CHATBOX) == 1;
				final Color textColor = isChatboxTransparent ? JagexColors.CHAT_TYPED_TEXT_TRANSPARENT_BACKGROUND : JagexColors.CHAT_TYPED_TEXT_OPAQUE_BACKGROUND;
				setChatboxWidgetInput(chatboxInput, ColorUtil.wrapWithColorTag(client.getVar(VarClientStr.CHATBOX_TYPED_TEXT) + "*", textColor));
			}
		}
	}

	/**
	 * Handles setting the string after "rsn:" in the chatbox.
	 *
	 * @param widget The chatbox widget.
	 * @param input  The string to set as the input string.
	 */
	private void setChatboxWidgetInput(Widget widget, String input)
	{
		String text = widget.getText();
		int idx = text.indexOf(':');
		if (idx != -1)
		{
			String newText = text.substring(0, idx) + ": " + input;
			widget.setText(newText);
		}
	}

	/**
	 * Toggles zoom based on {@link com.camerakeys.CameraKeysConfig.ActivationType}.
	 *
	 * @param state the key state pressed/released
	 */
	void zoom(keyState state)
	{
		switch (cameraKeysConfig.getActivationType())
		{

			case HOLD:
				zoomHold(state);
				break;
			case TOGGLE:
				zoomToggle(state);
				break;
			case SET:
				zoomSet(state);
				break;
		}
	}

	/**
	 * Handles the zoom logic for HOLD
	 *
	 * @param state the key state pressed/released
	 */
	private void zoomHold(keyState state)
	{
		switch (state)
		{
			case PRESSED:
				zoomState = ZoomState.ZOOM;
				break;
			case RELEASED:
				if (zoomState == ZoomState.ON)
				{
					zoomState = ZoomState.RESET;
				}
				break;
		}
	}

	/**
	 * Handles the zoom logic for TOGGLE
	 *
	 * @param state the key state pressed/released
	 */
	private void zoomToggle(keyState state)
	{
		switch (state)
		{

			case PRESSED:
				switch (zoomState)
				{
					case OFF:
						zoomState = ZoomState.ZOOM;
						break;
					case ON:
						zoomState = ZoomState.RESET;
						break;
					default:
						break;
				}
				break;
			case RELEASED:
				break;
		}
	}

	/**
	 * Handles the zoom logic for SET
	 *
	 * @param state the key state pressed/released
	 */
	private void zoomSet(keyState state)
	{
		switch (state)
		{

			case PRESSED:
				zoomState = ZoomState.SET;
				break;
			case RELEASED:
				break;
		}
	}

	/**
	 * Sets the compass to the specified cardinal direction.
	 *
	 * @param direction the direction for the compass to be set to
	 */
	void setCompassDirection(CardinalDirections direction)
	{
		clientThread.invoke(() -> client.runScript(COMPASS_SCRIPT_ID, direction.value));
	}

	/**
	 * Check if the user set the zoom level to deviate more than {@link #ZOOM_CANCEL_THRESHOLD} in either direction.
	 * If so set {@link #zoomState} to off.
	 */
	private void checkForZoomCancel()
	{
		if (zoomState == ZoomState.ON && newZoomLevel != null && Math.abs(getZoom() - newZoomLevel) > ZOOM_CANCEL_THRESHOLD)
		{
			zoomState = ZoomState.OFF; //user canceled zoom by scrolling
			log.debug("zoom canceled by users set point. Target Zoom: " + newZoomLevel + " User Zoom: " + getZoom());
		}
	}

	/**
	 * Checks if {@link #chatInputHandlingState} is set to ENABLE. If so make
	 * sure chat has been unlocked before re-locking and setting
	 * chatinputhandlingstate to ENABLED.
	 */
	private void checkForChatLockUpdate()
	{
		if (chatInputHandlingState == ChatInputHandlingState.ENABLE)
		{
			client.setVar(VarClientStr.CHATBOX_TYPED_TEXT, "");
			Widget chatboxInput = client.getWidget(WidgetInfo.CHATBOX_INPUT);
			final boolean isChatboxTransparent = client.isResized() && client.getVar(Varbits.TRANSPARENT_CHATBOX) == 1;
			final Color textColor = isChatboxTransparent ? JagexColors.CHAT_TYPED_TEXT_TRANSPARENT_BACKGROUND : JagexColors.CHAT_TYPED_TEXT_OPAQUE_BACKGROUND;

			if (chatboxInput != null)
			{
				String chatboxInputText = chatboxInput.getText();
				int index = chatboxInputText.indexOf(':');

				//check for the default input string "rsn: *"
				if (index > -1 && chatboxInputText.substring(index).equals(": " + ColorUtil.wrapWithColorTag("*", textColor)))
				{
					lockChat();
				}
			}
		}
	}

	/**
	 * Enables/Disables the overlay based on the {@link #zoomState}.
	 */
	private void checkForOverlayUpdate()
	{
		switch (zoomState)
		{
			case OFF:
				overlayManager.remove(cameraKeysOverlay);
				break;
			case ON:
				overlayManager.add(cameraKeysOverlay);
				break;
			default:
				break;

		}
	}

	/**
	 * Handles the ZOOM, SET and RESET {@link #zoomState}.
	 * <p>
	 * Sets the zoom level based on {@link #zoomState}.
	 */
	private void checkForZoomUpdate()
	{
		switch (zoomState)
		{
			case ZOOM:
				prevZoomLevel = getZoom();
				clientThread.invoke(() -> client.runScript(ScriptID.CAMERA_DO_ZOOM, cameraKeysConfig.zoom(), cameraKeysConfig.zoom()));
				newZoomLevel = getZoom(); //get actual zoom after running script may be higher or lower than requested
				log.debug("Zoom level change: " + prevZoomLevel + " --> " + newZoomLevel);
				zoomState = ZoomState.ON;
				break;
			case SET:
				prevZoomLevel = getZoom();
				clientThread.invoke(() -> client.runScript(ScriptID.CAMERA_DO_ZOOM, cameraKeysConfig.zoom(), cameraKeysConfig.zoom()));
				log.debug("Zoom level change: " + prevZoomLevel + " --> " + getZoom());
				zoomState = ZoomState.OFF;
				break;
			case RESET:
				log.debug("Zoom level change: " + prevZoomLevel + " <-- " + getZoom());
				clientThread.invoke(() -> client.runScript(ScriptID.CAMERA_DO_ZOOM, prevZoomLevel, prevZoomLevel));
				zoomState = ZoomState.OFF;
				prevZoomLevel = null;
				newZoomLevel = null;
				break;
			default:
				break;
		}
	}

	/**
	 * Makes sure the functionality that is duplicated by this plugin doesn't
	 * interfere with {@link KeyRemappingPlugin} by checking if the Key
	 * Remapping Plugin has been enabled or disabled. Then the
	 * {@link #chatInputHandlingState} is changed to match.
	 *
	 * @param configChanged The ConfigChanged event
	 */
	private void checkForKeyRemappingPluginChange(ConfigChanged configChanged)
	{
		if (Objects.equals(configChanged.getKey(), KEYREMAPPINGPLUGIN_NAME))
		{
			if (Objects.equals(configChanged.getNewValue(), "false"))
			{
				log.debug("Key Remapping Plugin Disabled. Taking over the handling of the chat box");
				//special case where the keyremapperplugin clears the chat on shutdown and the chat needs to be locked again
				chatInputHandlingState = ChatInputHandlingState.ENABLE;
			}
			else
			{
				log.debug("Key Remapping Plugin Enabled. Stopping the handling of the chat box.");
				chatInputHandlingState = ChatInputHandlingState.DISABLED;
			}
		}
	}

	@Provides
	CameraKeysConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CameraKeysConfig.class);
	}

	enum keyState
	{
		PRESSED,
		RELEASED
	}

	enum ChatInputHandlingState
	{
		DISABLED,
		ENABLE,
		ENABLED
	}

	/**
	 * Represents the possible cardinal direction values used by osrs.
	 */
	enum CardinalDirections
	{
		NORTH(1),
		EAST(3),
		SOUTH(2),
		WEST(4);

		private final int value;

		CardinalDirections(int value)
		{
			this.value = value;
		}

		public int getValue()
		{
			return value;
		}
	}

	private enum ZoomState
	{
		ON,
		ZOOM,
		SET,
		RESET,
		OFF
	}
}
