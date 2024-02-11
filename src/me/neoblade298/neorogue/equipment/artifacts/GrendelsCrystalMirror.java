package me.neoblade298.neorogue.equipment.artifacts;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class GrendelsCrystalMirror extends Artifact {
	public GrendelsCrystalMirror() {
		super("grendelsCrystalMirror", "Grendel's Crystal Mirror", Rarity.RARE, EquipmentClass.CLASSLESS);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, new GrendelsCrystalMirrorInstance());
	}
	
	private class GrendelsCrystalMirrorInstance implements TriggerAction {
		private int count = 3;
 
		@Override
		public TriggerResult trigger(PlayerFightData data, Object inputs) {
			if (count > 0) {
				count--;
				LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) inputs;
				data.addTask(UUID.randomUUID().toString(), new BukkitRunnable() {
					public void run() {
						ev.getGroup().startWithoutEvent(data);
					}
				}.runTaskLater(NeoRogue.inst(), 10L));
				return TriggerResult.keep();
			}
			return TriggerResult.remove();
		}
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GLASS_PANE,
				"The first <white>3</white> sets of 1+ projectiles you fire will be fired twice.");
	}
}
