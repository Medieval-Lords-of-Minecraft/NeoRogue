package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class WardingRune extends Equipment {
	private static final String ID = "wardingRune";
	private int reduc;
	
	public WardingRune(boolean isUpgraded) {
		super(ID, "Warding Rune", isUpgraded, Rarity.COMMON, EquipmentClass.CLASSLESS,
				EquipmentType.OFFHAND, EquipmentProperties.none());
				reduc = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		int reps = (int) (data.getSessionData().getMaxMana() - 40) / 10;
		ItemStack icon = item.clone();
		ActionMeta am = new ActionMeta();
		icon.setAmount(reps);
		am.setCount(reps);
		Trigger tr = data.getSessionData().getPlayerClass() == EquipmentClass.ARCHER ? Trigger.LEFT_CLICK : Trigger.RIGHT_CLICK;
		data.addTrigger(id, tr, (pdata, in) -> {
			Sounds.fire.play(p, p);
			data.addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.increase(data, reduc, StatTracker.defenseBuffAlly(this)), 20);
			
			am.addCount(-1);
			if (am.getCount() <= 0) {
				Sounds.breaks.play(p, p);
				p.getInventory().setItemInOffHand(null);
				return TriggerResult.remove();
			}
			else {
				icon.setAmount(am.getCount());
				p.getInventory().setItemInOffHand(icon);
				return TriggerResult.keep();
			}
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.QUARTZ_SLAB,
				"On right click (left click for <gold>Archer</gold>), reduce " + GlossaryTag.MAGICAL.tag(this) + " damage taken by " +
				DescUtil.yellow(reduc) + " for <white>1s</white>. " +
				"Can be used once for every <white>10</white> max mana you have over <white>40</white>.");
	}
}
