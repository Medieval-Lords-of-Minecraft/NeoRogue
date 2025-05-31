package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ManaArc extends Equipment {
	private static final String ID = "manaArc";
	private int mana, damage, elec;

	public ManaArc(boolean isUpgraded) {
		super(ID, "Mana Arc", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(10, 0, 25, 0));
		mana = isUpgraded ? 3 : 4;
		damage = isUpgraded ? 90 : 60;
		elec = isUpgraded ? 45 : 30;
	}

	@Override
	public void setupReforges() {

	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			activate(p, data, am);
			return TriggerResult.keep();
		}, (pl, pdata, in) -> {
			return !am.getBool(); // Only allow casting if it's not already active
		}));
	}

	private void activate(Player p, PlayerFightData data, ActionMeta am) {
		Sounds.equip.play(p, p);
		am.setBool(true);
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (pdata.getMana() <= mana) {
				Sounds.flap.play(p, p);
				am.setBool(false);
				return TriggerResult.remove();
			}
			pdata.addMana(-mana);
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			if (!am.getBool())
				return TriggerResult.remove();

		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHT_BLUE_BANNER,
				"On cast, lose " + DescUtil.yellow(mana) + " mana per second. Until you run out of mana, "
						+ "dealing damage fires a projectile at the target, dealing "
						+ GlossaryTag.LIGHTNING.tag(this, damage, true) + " damage and applying "
						+ GlossaryTag.ELECTRIFIED.tag(this, elec, true) + ".");
	}
}
