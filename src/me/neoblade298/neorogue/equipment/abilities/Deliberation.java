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
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Deliberation extends Equipment {
	private static final String ID = "deliberation";
	private int damage;
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANTMENT_TABLE).count(50).speed(0.1);
	
	public Deliberation(boolean isUpgraded) {
		super(ID, "Deliberation", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 10, 15, 0));
		damage = isUpgraded ? 30 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			Sounds.equip.play(p, p);
			data.channel(40);
			data.addTask(new BukkitRunnable() {
				public void run() {
					Sounds.enchant.play(p, p);
					pc.play(p, p);
					data.applyStatus(StatusType.FOCUS, data, 1, -1);
					data.addBuff(data, id, true, false, BuffType.GENERAL, damage * data.getStatus(StatusType.FOCUS).getStacks(), 100);
				}
			}.runTaskLater(NeoRogue.inst(), 20L));
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD,
				"On cast, " + GlossaryTag.CHANNEL.tag(this) + " for <white>2s</white> before gaining " + GlossaryTag.FOCUS.tag(this, 1, false) +
				" and increasing your damage by <yellow>" + damage + "</yellow> multiplied by your current " + GlossaryTag.FOCUS.tag(this) + " for <white>5s</white>.");
	}
}
