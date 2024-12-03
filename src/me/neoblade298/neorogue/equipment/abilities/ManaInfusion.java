package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.equipment.mechanics.IProjectileInstance;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class ManaInfusion extends Equipment {
	private static final String ID = "manaInfusion";
	private int damage, drain, mana = 3;
	private ItemStack activeIcon;
	
	public ManaInfusion(boolean isUpgraded) {
		super(ID, "Mana Infusion", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 2, 0));
				damage = isUpgraded ? 30 : 20;
				drain = isUpgraded ? 4 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			if (inst.getCount() == 0) {
				inst.setCount(1);
				Sounds.equip.play(p, p);
				inst.setIcon(activeIcon);
			}
			else {
				inst.setCount(0);
				inst.setIcon(item);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);

		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in;
			if (!ev.isBowProjectile()) return TriggerResult.keep();
			if (inst.getCount() == 1 && pdata.getMana() >= mana) {
				for (IProjectileInstance pi : ev.getInstances()) {
					ProjectileInstance proj = (ProjectileInstance) pi;
					proj.getMeta().addBuff(true, DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, damage, 0));
				}
				pdata.addMana(-mana);
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.KILL, (pdata, in) -> {
			if (inst.getCount() == 1) {
				pdata.addMana(drain);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LAPIS_LAZULI,
				"Toggleable, off by default. When active, your basic attacks consume " + DescUtil.white(mana) + " mana in exchange for increasing their damage by " +
				DescUtil.yellow(damage) + ", and killing an enemy grants you " + DescUtil.yellow(drain) + " mana.");
				
		activeIcon = item.withType(Material.LAPIS_BLOCK);
	}
}
