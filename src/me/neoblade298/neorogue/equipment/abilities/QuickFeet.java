package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class QuickFeet extends Equipment {
	private static final String ID = "quickFeet";
	private static final ParticleContainer pc = new ParticleContainer(Particle.FIREWORKS_SPARK).count(25)
			.spread(0.5, 0.5).offsetY(1).speed(0.01);
	private int ev;

	public QuickFeet(boolean isUpgraded) {
		super(ID, "Quick Feet", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(20, 25, 20, 0));
		ev = isUpgraded ? 2 : 1;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(ID, bind, new EquipmentInstance(p, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(p, p);
			data.addTask(new BukkitRunnable() {
				public void run() {
					Sounds.fire.play(p, p);
					pc.play(p, p);
					data.applyStatus(StatusType.EVADE, data, ev, -1);
				}
			}.runTaskLater(NeoRogue.inst(), 100L));
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CYAN_DYE,
				"On cast, charge for <white>5</white> seconds before gaining " + GlossaryTag.EVADE.tag(this, ev, true) + ".");
	}
}
