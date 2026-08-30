package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Trap;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LayTrapEvent;

public class Ghostwire extends Equipment {
	private static final String ID = "Ghostwire";
	private static final Circle GHOST_RING = new Circle(0.9);
	private static final ParticleContainer GHOST_EDGE = new ParticleContainer(Particle.SOUL)
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer MATERIALIZE = new ParticleContainer(Particle.REVERSE_PORTAL)
			.count(10).spread(0.1, 0.1).offsetY(0.2).speed(0.01);
	private int threshold, delay = 2;

	public Ghostwire(boolean isUpgraded) {
		super(ID, "Ghostwire", isUpgraded, Rarity.EPIC, EquipmentClass.ARCHER, EquipmentType.ACCESSORY,
				EquipmentProperties.none());
		threshold = isUpgraded ? 2 : 3;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta traps = new ActionMeta();
		data.addTrigger(id, Trigger.LAY_TRAP, (pdata, in) -> {
			LayTrapEvent event = (LayTrapEvent) in;
			Trap trap = (Trap) event.getTrap();
			if (trap.getSourceEquipment() == this || traps.addCount(1) < threshold) return TriggerResult.keep();
			traps.setCount(0);
			data.addTask(new BukkitRunnable() {
				@Override
				public void run() {
					Player player = data.getPlayer();
					Location location = player.getLocation().clone();
					GHOST_RING.play(GHOST_EDGE, location, LocalAxes.xz(), null);
					MATERIALIZE.play(player, location);
					Sounds.teleport.play(player, location);
					data.addTrap(trap.duplicateAt(location, Ghostwire.this));
				}
			}.runTaskLater(NeoRogue.inst(), delay * 20L));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STRING, "Every " + DescUtil.val(threshold == 2 ? "2nd" : "3rd") + " "
				+ GlossaryTag.TRAP.tag(this) + " you lay is duplicated at your feet " + DescUtil.white(delay + "s")
				+ " later. Duplicated traps do not count toward this.");
	}
}