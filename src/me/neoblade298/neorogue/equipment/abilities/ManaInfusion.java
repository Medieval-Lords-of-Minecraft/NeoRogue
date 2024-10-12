package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ManaInfusion extends Equipment {
	private static final String ID = "manaInfusion";
	private int damage, drain, mana = 1;
	
	public ManaInfusion(boolean isUpgraded) {
		super(ID, "Mana Infusion", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 2, 0));
				damage = isUpgraded ? 10 : 5;
				drain = isUpgraded ? 12 : 8;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardEquipmentInstance inst = new StandardEquipmentInstance(p, this, slot, es);
		inst.setAction((pdata, in) -> {
			if (inst.getCount() == 0) {
				inst.setCount(1);
				Sounds.equip.play(p, p);
				this.getDisplay().append(Component.text(" was activated", NamedTextColor.GRAY));
			}
			else {
				inst.setCount(0);
				this.getDisplay().append(Component.text(" was deactivated", NamedTextColor.GRAY));
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);

		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			if (inst.getCount() == 1 && pdata.getMana() >= mana) {
				BasicAttackEvent ev = (BasicAttackEvent) in;
				ev.getMeta().addBuff(BuffType.GENERAL, new Buff(data, damage, 0), BuffOrigin.NORMAL, true);
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
				"Toggleable. When active, your basic attacks consume " + DescUtil.white(mana) + " mana in exchange for increasing their damage by " +
				DescUtil.yellow(damage) + ", and killing an enemy grants you " + DescUtil.yellow(drain) + " mana.");
	}
}
