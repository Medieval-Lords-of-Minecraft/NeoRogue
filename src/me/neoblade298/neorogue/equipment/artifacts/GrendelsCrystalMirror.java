package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class GrendelsCrystalMirror extends Artifact {
	private static final String ID = "grendelsCrystalMirror";
	public GrendelsCrystalMirror() {
		super(ID, "Grendel's Crystal Mirror", Rarity.RARE, EquipmentClass.CLASSLESS);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, new GrendelsCrystalMirrorInstance());
	}
	
	private class GrendelsCrystalMirrorInstance implements TriggerAction {
		private int count = 0;
 
		@Override
		public TriggerResult trigger(PlayerFightData data, Object inputs) {
			if (++count >= 5) {
				count = 0;
				LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) inputs;
				data.addTask(new BukkitRunnable() {
					public void run() {
						ev.getGroup().startWithoutEvent(data);
					}
				}.runTaskLater(NeoRogue.inst(), 10L));
			}
			return TriggerResult.keep();
		}
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GLASS_PANE,
				"Every <white>5th</white> projectile you fire will be fired twice.");
	}
}
