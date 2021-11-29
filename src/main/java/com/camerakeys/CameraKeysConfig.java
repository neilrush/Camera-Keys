/*
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

import java.awt.event.KeyEvent;
import lombok.AllArgsConstructor;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.ModifierlessKeybind;
import net.runelite.client.config.Range;

@ConfigGroup("camerakeys")
public interface CameraKeysConfig extends Config
{
	@ConfigSection(
		name = "Zoom Key",
		description = "Zoom Key settings",
		position = 0,
		closedByDefault = false
	)
	String ZoomKeySection = "ZoomKey";
	@ConfigSection(
		name = "Compass Keys",
		description = "Compass Key Options",
		position = 1,
		closedByDefault = false
	)
	String CompassKeySection = "CompassKeys";

	@ConfigSection(
		name = "Advanced",
		description = "Advanced Config Options",
		position = 2,
		closedByDefault = true
	)
	String AdvancedSection = "Advanced";

	@ConfigItem(
		position = 0,
		keyName = "zoomKeyEnabled",
		name = "Enable Zoom Key",
		section = ZoomKeySection,
		description = ""
	)
	default boolean zoomKeyEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "zoom",
		name = "Zoom level",
		description = "Zoom level to change to",
		section = ZoomKeySection,
		position = 1
	)
	@Range(
		min = -272,
		max = 1300
	)
	default int zoom()
	{
		return 0;
	}

	@ConfigItem(
		position = 2,
		keyName = "zoomKey",
		name = "Zoom Key",
		section = ZoomKeySection,
		description = "The key that activates/toggles zoom level"
	)
	default ModifierlessKeybind zoomKey()
	{
		return new ModifierlessKeybind(KeyEvent.VK_C, 0); //default to c because optifine lol
	}

	@ConfigItem(
		position = 3,
		keyName = "activationType",
		name = "Activation Type",
		section = ZoomKeySection,
		description = "The activation type of the zoom level key"
	)
	default ActivationType getActivationType()
	{
		return ActivationType.HOLD;
	}

	@ConfigItem(
		position = 4,
		keyName = "zoomIndicator",
		name = "Zoom Icon",
		section = ZoomKeySection,
		description = "Displays an icon when the zoom is in effect"
	)
	default boolean isZoomIndicatorEnabled()
	{
		return true;
	}

	@ConfigItem(
		position = 0,
		keyName = "compassKeysEnabled",
		name = "Enable Compass Keys",
		section = CompassKeySection,
		description = ""
	)
	default boolean compassKeysEnabled()
	{
		return true;
	}

	@ConfigItem(
		position = 1,
		keyName = "northKey",
		name = "North",
		section = CompassKeySection,
		description = "The key that faces the camera north"
	)
	default ModifierlessKeybind northKey()
	{
		return new ModifierlessKeybind(KeyEvent.VK_N, 0);
	}

	@ConfigItem(
		position = 2,
		keyName = "eastKey",
		name = "East",
		section = CompassKeySection,
		description = "The key that faces the camera east"
	)
	default ModifierlessKeybind eastKey()
	{
		return new ModifierlessKeybind(KeyEvent.VK_UNDEFINED, 0);
	}

	@ConfigItem(
		position = 3,
		keyName = "southKey",
		name = "South",
		section = CompassKeySection,
		description = "The key that faces the camera south"
	)
	default ModifierlessKeybind southKey()
	{
		return new ModifierlessKeybind(KeyEvent.VK_UNDEFINED, 0);
	}

	@ConfigItem(
		position = 4,
		keyName = "westKey",
		name = "West",
		section = CompassKeySection,
		description = "The key that faces the camera west"
	)
	default ModifierlessKeybind westKey()
	{
		return new ModifierlessKeybind(KeyEvent.VK_UNDEFINED, 0);
	}

	@ConfigItem(
		position = 0,
		keyName = "disableChatBlocking",
		name = "Disable Chat Blocking",
		section = AdvancedSection,
		description = "Disables this plugin from blocking the chat with \"Press Enter to Chat...\""
	)
	default boolean disableChatBlocking()
	{
		return false;
	}

	@AllArgsConstructor
	enum ActivationType
	{
		HOLD("Hold"),
		TOGGLE("Toggle"),
		SET("Set");

		private final String value;

		@Override
		public String toString()
		{
			return value;
		}
	}
}
