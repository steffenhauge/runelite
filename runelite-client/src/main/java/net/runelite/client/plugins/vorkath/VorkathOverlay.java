/*
 * Copyright (c) 2018, Steffen Hauge <steffen.oerum.hauge@hotmail.com>
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
package net.runelite.client.plugins.vorkath;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public class VorkathOverlay extends Overlay
{
	private final Client client;
	private final VorkathConfig config;
	private final VorkathPlugin plugin;

	private static final Color COLOR_FULL_DMG = Color.RED;
	private static final Color COLOR_NO_DMG = Color.GREEN;

	@Inject
	private VorkathOverlay(Client client, VorkathConfig config, VorkathPlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(OverlayPriority.LOW);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.isInArea())
		{
			return null;
		}

		if (config.highlightFirebomb() && plugin.getFirebombLocation() != null)
		{
			LocalPoint loc = plugin.getFirebombLocation();
			renderTile(graphics, loc, COLOR_FULL_DMG);
		}

		if (config.highlightFirebombSafe() && plugin.getFirebombLocation() != null)
		{
			LocalPoint loc = plugin.getFirebombLocation();
			renderTileArea(graphics, loc, COLOR_NO_DMG);
		}

		return null;
	}

	private void renderTileArea(final Graphics2D graphics, final LocalPoint dest, final Color color)
	{
		if (dest == null)
		{
			return;
		}

		WorldArea area = client.getLocalPlayer().getWorldArea();
		if (area == null)
		{
			return;
		}

		for (int dx = -2; dx <= 2; dx++)
		{
			for (int dy = -2; dy <= 2; dy++)
			{
				if ((Math.abs(dx) == 1 || dx == 0) && (Math.abs(dy) == 1 || dy == 0))
				{
					continue;
				}

				LocalPoint lp = new LocalPoint(
					dest.getX() + dx * Perspective.LOCAL_TILE_SIZE + dx * Perspective.LOCAL_TILE_SIZE * (area.getWidth() - 1) / 2,
					dest.getY() + dy * Perspective.LOCAL_TILE_SIZE + dy * Perspective.LOCAL_TILE_SIZE * (area.getWidth() - 1) / 2);

				Polygon poly = Perspective.getCanvasTilePoly(client, lp);
				if (poly == null)
				{
					return;
				}
				OverlayUtil.renderPolygon(graphics, poly, color);
			}
		}
	}

	private void renderTile(final Graphics2D graphics, final LocalPoint dest, final Color color)
	{
		if (dest == null)
		{
			return;
		}

		final Polygon poly = Perspective.getCanvasTilePoly(client, dest);

		if (poly == null)
		{
			return;
		}

		OverlayUtil.renderPolygon(graphics, poly, color);
	}
}