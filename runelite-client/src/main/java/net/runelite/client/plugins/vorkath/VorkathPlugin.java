/*
 * Copyright (c) 2016-2018, Dewin <dewin@dewinster.com>
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

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Provides;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.AnimationID;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Projectile;
import net.runelite.api.ProjectileID;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.LocalPlayerDeath;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import lombok.Getter;

@PluginDescriptor(
	name = "Vorkath",
	description = "Vorkath overlay",
	tags = {"vorkath", "overlay"},
	enabledByDefault = false
)

public class VorkathPlugin extends Plugin
{

	private static final Set<Integer> VORKATH_NPC_IDS = ImmutableSet.of(
		NpcID.VORKATH,
		NpcID.VORKATH_8058,
		NpcID.VORKATH_8059,
		NpcID.VORKATH_8060,
		NpcID.VORKATH_8061);

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private VorkathOverlay overlay;

	@Inject
	private Notifier notifier;

	@Inject
	private VorkathConfig config;

	@Getter
	private LocalPoint firebombLocation;

	@Getter
	private boolean isInArea;

	@Provides
	VorkathConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(VorkathConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		reset();
		isInArea = false;
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		reset();
		isInArea = false;
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		final NPC npc = npcSpawned.getNpc();

		if (npc != null && VORKATH_NPC_IDS.contains(npc.getId()))
		{
			isInArea = true;
			reset();
		}
	}

	@Subscribe
	public void onProjectileMoved(ProjectileMoved event)
	{
		final Projectile projectile = event.getProjectile();

		if (projectile.getInteracting() != null && client.getGameCycle() >= projectile.getStartMovementCycle())
		{
			return;
		}

		int projectileId = projectile.getId();

		switch (projectileId)
		{
			case ProjectileID.VORKATH_BOMB_AOE:
				reset();
				firebombLocation = event.getPosition();
				if (config.notifyOnFirebomb())
				{
					notifier.notify("BOMBS AWAY!");
				}
				break;
			case ProjectileID.VORKATH_DRAGONBREATH:
			case ProjectileID.VORKATH_RANGED:
			case ProjectileID.VORKATH_MAGIC:
			case ProjectileID.VORKATH_VENOM:
			case ProjectileID.VORKATH_PRAYER_DISABLE:
			case ProjectileID.VORKATH_TICK_FIRE_AOE:
			case ProjectileID.VORKATH_ICE:
				reset();
				break;
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		final Actor actor = event.getActor();

		if (!(actor instanceof NPC))
		{
			return;
		}

		NPC npc = (NPC) actor;

		if (!VORKATH_NPC_IDS.contains(npc.getId()))
		{
			return;
		}

		switch (npc.getAnimation())
		{
			case AnimationID.VORKATH_WAKE_UP:
			case AnimationID.VORKATH_DEATH:
				reset();
				break;
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		final NPC npc = npcDespawned.getNpc();

		if (npc != null && VORKATH_NPC_IDS.contains(npc.getId()))
		{
			isInArea = false;
			reset();
		}
	}

	@Subscribe
	public void onGameState(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			reset();
		}
	}

	@Subscribe
	public void onLocalPlayerDeath(LocalPlayerDeath player)
	{
		reset();
	}

	private void reset()
	{
		firebombLocation = null;
	}
}