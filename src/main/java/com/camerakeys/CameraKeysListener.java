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

import com.google.common.base.Strings;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.VarClientStr;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.input.KeyListener;

class CameraKeysListener implements KeyListener
{
	@Inject
	private CameraKeysPlugin plugin;

	@Inject
	private CameraKeysConfig config;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	private final Set<Integer> blockedChars = new HashSet<>();

	@Override
	public void keyTyped(KeyEvent e)
	{
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (!plugin.chatboxFocused())
		{
			return;
		}

		if (!plugin.isTyping())
		{
			if(!blockedChars.contains(e.getKeyCode()) && !plugin.isDialogOpen())
			{
				if (config.zoomKey().matches(e))
				{
					plugin.zoom(CameraKeysPlugin.keyState.PRESSED);
				}

				if (config.northKey().matches(e)) {
					plugin.setCompassDirection(CameraKeysPlugin.CardinalDirections.NORTH);
				}
				if (config.eastKey().matches(e))
				{
					plugin.setCompassDirection(CameraKeysPlugin.CardinalDirections.EAST);
				}
				if (config.southKey().matches(e))
				{
					plugin.setCompassDirection(CameraKeysPlugin.CardinalDirections.SOUTH);
				}
				if (config.westKey().matches(e))
				{
					plugin.setCompassDirection(CameraKeysPlugin.CardinalDirections.WEST);
				}
			}
			switch (e.getKeyCode())
			{
				case KeyEvent.VK_ENTER:
				case KeyEvent.VK_SLASH:
				case KeyEvent.VK_COLON:
					// refocus chatbox
					plugin.setTyping(true);
					if (plugin.getChatInputHandlingState() == CameraKeysPlugin.ChatInputHandlingState.ENABLED)
					{
						clientThread.invoke(plugin::unlockChat);
					}
					break;
			}

			blockedChars.add(e.getKeyCode());
		}
		else
		{
			switch (e.getKeyCode())
			{
				case KeyEvent.VK_ESCAPE:
					plugin.setTyping(false);
					if (plugin.getChatInputHandlingState() == CameraKeysPlugin.ChatInputHandlingState.ENABLED || plugin.getChatInputHandlingState() == CameraKeysPlugin.ChatInputHandlingState.ENABLE)
					{
						// When exiting typing mode, block the escape key
						// so that it doesn't trigger the in-game hotkeys
						e.consume();
						clientThread.invoke(() ->
						{
							client.setVar(VarClientStr.CHATBOX_TYPED_TEXT, "");
							plugin.lockChat();
						});
					}
					break;
				case KeyEvent.VK_ENTER:
					plugin.setTyping(false);
					if (plugin.getChatInputHandlingState() == CameraKeysPlugin.ChatInputHandlingState.ENABLED || plugin.getChatInputHandlingState() == CameraKeysPlugin.ChatInputHandlingState.ENABLE)
					{
						clientThread.invoke(plugin::lockChat);
					}
					break;
				case KeyEvent.VK_BACK_SPACE:
					// Only lock chat on backspace when the typed text is now empty
					if (Strings.isNullOrEmpty(client.getVar(VarClientStr.CHATBOX_TYPED_TEXT)))
					{
						plugin.setTyping(false);
						if (plugin.getChatInputHandlingState() == CameraKeysPlugin.ChatInputHandlingState.ENABLED || plugin.getChatInputHandlingState() == CameraKeysPlugin.ChatInputHandlingState.ENABLE)
						{
							clientThread.invoke(plugin::lockChat);
						}
					}
					break;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		if (config.zoomKey().matches(e))
		{
			plugin.zoom(CameraKeysPlugin.keyState.RELEASED);
		}

		blockedChars.remove(e.getKeyCode());
	}
}
