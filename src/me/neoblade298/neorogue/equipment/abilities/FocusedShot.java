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
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class FocusedShot extends Equipment {
	private static final String ID = "focusedShot";
	private int damage;
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANTMENT_TABLE).count(50).speed(0.1);
	
	public FocusedShot(boolean isUpgraded) {
		super(ID, "Focused Shot", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(10, 10, 15, 0));
		damage = isUpgraded ? 100 : 60;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, inputs) -> {
			Sounds.equip.play(p, p);
			data.charge(20);
			data.addTask(new BukkitRunnable() {
				public void run() {
					Sounds.enchant.play(p, p);
					pc.play(p, p);
					primeShot(p, data);
					inst.reduceCooldown(data.getStatus(StatusType.FOCUS).getStacks());
				}
			}.runTaskLater(NeoRogue.inst(), 20L));
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
		
	}

	private void primeShot(Player p, PlayerFightData data) {
		data.addTrigger(ID, Trigger.BASIC_ATTACK, (pdata, in) -> {
			BasicAttackEvent ev = (BasicAttackEvent) in;
			ev.getMeta().addBuff(BuffType.GENERAL, new Buff(data, 0, damage * 0.01), BuffOrigin.NORMAL, true);
			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CYAN_DYE,
				"On cast, " + GlossaryTag.CHARGE.tag(this) + " for <white>1s</white>. Afterwards, your next basic attack " + 
				"has its damage increased by <yellow>" + damage + "%</yellow>. This skill's cooldown is decreased by stacks of " + GlossaryTag.FOCUS.tag(this) + " you have.");
	}
}