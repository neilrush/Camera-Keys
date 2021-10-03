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

import com.google.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.util.ImageUtil;

public class CameraKeysOverlay extends OverlayPanel
{
	private final BufferedImage zoomIcon;
	private final CameraKeysConfig config;

	@Inject
	private CameraKeysOverlay(CameraKeysConfig config, CameraKeysPlugin plugin)
	{
		super(plugin);
		setPosition(OverlayPosition.CANVAS_TOP_RIGHT);
		this.config = config;
		setPriority(OverlayPriority.LOW);
		zoomIcon = ImageUtil.loadImageResource(CameraKeysPlugin.class, "zoomIcon.png");
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.isZoomIndicatorEnabled())
		{
			panelComponent.getChildren().clear();
			ImageComponent imageComponent = new ImageComponent(zoomIcon);
			panelComponent.getChildren().add(imageComponent);

			return super.render(graphics);
		}
		else
		{
			return null;
		}
	}
}
