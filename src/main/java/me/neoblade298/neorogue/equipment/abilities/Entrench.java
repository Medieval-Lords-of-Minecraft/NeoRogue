package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Entrench extends Equipment implements Power {
	private static final String ID = "Entrench";
	private static final ParticleContainer pc = new ParticleContainer(Particle.FIREWORK)
			.count(20).spread(0.5, 0.5).offsetY(1);
	
	private int shields;

	public Entrench(boolean isUpgraded) {
		super(ID, "Entrench", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		shields = isUpgraded ? 4 : 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.LAY_TRAP, (pdata, in) -> {
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addTrigger(id + "-active", Trigger.LAY_TRAP, (pdata2, in2) -> {
					Player p2 = data.getPlayer();
					data.addPermanentShield(p2.getUniqueId(), shields);
					Sounds.equip.play(p2, p2);
					pc.play(p2, p2);
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));

		data.addTrigger(id + "-deactivate", Trigger.DEACTIVATE_TRAP, (pdata3, in3) -> {
			Player p3 = data.getPlayer();
			data.addPermanentShield(p3.getUniqueId(), shields);
			Sounds.equip.play(p3, p3);
			pc.play(p3, p3);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_BLOCK,
				GlossaryTag.POWER.tag(this) + ". Activates after placing a " + GlossaryTag.TRAP.tag(this) + ". Gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + " every time you place or remove a " +
				GlossaryTag.TRAP.tag(this) + ".");
	}
}
