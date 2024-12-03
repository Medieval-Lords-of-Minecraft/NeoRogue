package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.weapons.Quickfire;
import me.neoblade298.neorogue.equipment.weapons.RapidFire;
import me.neoblade298.neorogue.equipment.weapons.SerratedArrow;
import me.neoblade298.neorogue.equipment.weapons.Volley;
import me.neoblade298.neorogue.equipment.weapons.WoodenArrow;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class AgilityTraining extends Equipment {
	private static final String ID = "agilityTraining";
	private int damage, stacks;
	
	public AgilityTraining(boolean isUpgraded) {
		super(ID, "Agility Training", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
				damage = 5;
				stacks = isUpgraded ? 3 : 2;
	}

	@Override
	public void setupReforges() {
		addReforge(PointBlank.get(), Grit.get());
		addReforge(Quickfire.get(), Volley.get(), RapidFire.get());
		addReforge(WoodenArrow.get(), SerratedArrow.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addDamageBuff(DamageBuffType.of(DamageCategory.PHYSICAL), new Buff(data, damage, 0));
		data.addTrigger(id, Trigger.DEALT_DAMAGE, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			DealtDamageEvent ev = (DealtDamageEvent) in;
			if (!ev.getMeta().containsType(DamageCategory.PHYSICAL)) return TriggerResult.keep();
			FightInstance.applyStatus(ev.getTarget(), StatusType.REND, data, stacks, -1);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FEATHER,
				"Passive. Increase all " + GlossaryTag.PHYSICAL.tag(this) + " damage by " + DescUtil.white(damage) + ". " +
				GlossaryTag.PHYSICAL.tag(this) + " damage additionally applies " + GlossaryTag.REND.tag(this, stacks, true) + ".");
	}
}
