package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;

public class PhoenixfireMantle extends Equipment {
	private static final String ID = "phoenixfireMantle";
	private int thres, heal = 2;
	public PhoenixfireMantle(boolean isUpgraded) {
		super(ID, "Phoenixfire Mantle", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ARMOR);
		thres = isUpgraded ? 7 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			ReceivedDamageEvent ev = (ReceivedDamageEvent) in;
			if (ev.getMeta().containsType(DamageCategory.FIRE)) {
				am.addCount(1);

				if (am.getCount() >= thres) {
					am.addCount(-thres);
					data.addHealth(heal);
					Sounds.success.play(p, p);
				}
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER, "Every " + DescUtil.yellow(thres) + " times you deal " + GlossaryTag.FIRE.tag(this) + " damage, heal for " + DescUtil.white(heal) + ".");
	}
}
