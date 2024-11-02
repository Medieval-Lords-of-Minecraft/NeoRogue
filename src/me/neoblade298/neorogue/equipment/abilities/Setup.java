package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Setup extends Equipment {
	private static final String ID = "setup";
	private int time, inc, damage;
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANTMENT_TABLE).count(25).spread(1, 1).offsetY(1);
	
	public Setup(boolean isUpgraded) {
		super(ID, "Setup", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		time = 4;
		inc = isUpgraded ? 30 : 20;
		damage = isUpgraded ? 100 : 70;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction act = new StandardPriorityAction(id);
		ItemStack icon = item.clone();
		icon.addEnchantment(Enchantment.LUCK, 1);
		act.setAction((pdata, in) -> {
			if (!p.isSneaking()) return TriggerResult.keep();
			act.addCount(1);
			if (act.getCount() >= time) {
				pc.play(p, p);
				Sounds.enchant.play(p, p);
				data.addBuff(data, true, false, BuffType.GENERAL, inc, DamageOrigin.TRAP);
				act.addCount(-time);

				// This stupid stuff is so I can avoid making an object to store a single boolean
				if (icon.containsEnchantment(Enchantment.LUCK)) {
					icon.setAmount(icon.getAmount() + 1);
				}
				else {
					icon.removeEnchantment(Enchantment.LUCK);
				}
				p.getInventory().setItem(slot, icon);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.PLAYER_TICK, act);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SCAFFOLDING,
				"Passive. Every " + DescUtil.white(time +"s") + " spent crouched during a fight, drop a " + GlossaryTag.TRAP.tag(this) + " at your feet [<white>10s</white>] that deals " +
				GlossaryTag.BLUNT.tag(this, damage, true) + " damage and deactivates when walked over, and increase your " + GlossaryTag.TRAP.tag(this) + 
				" damage by " + DescUtil.yellow(inc + "%") + ".");
	}
}
