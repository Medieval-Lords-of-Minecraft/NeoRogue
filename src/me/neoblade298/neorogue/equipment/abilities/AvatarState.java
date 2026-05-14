package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class AvatarState extends Equipment {
	private static final String ID = "AvatarState";
	private double mreg, hreg;
	private int shields;

	public AvatarState(boolean isUpgraded) {
		super(ID, "Avatar State", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(5, 15, 0, 0));
		mreg = isUpgraded ? 2.5 : 1.5;
		hreg = isUpgraded ? 1.5 : 1;
		shields = isUpgraded ? 10 : 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			data.addManaRegen(mreg);
			data.addSimpleShield(p.getUniqueId(), shields, 200);

			data.addTrigger(ID, Trigger.PLAYER_TICK, (pdata2, in2) -> {
				data.addHealth(hreg);
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GLOWSTONE,
				GlossaryTag.POWER.tag(this) + ". Increase mana regen by " + DescUtil.yellow(mreg) + ", health regen by "
						+ DescUtil.yellow(hreg) + ", and gain " + GlossaryTag.SHIELDS.tag(this, shields, true)
						+ " [" + DescUtil.white("10s") + "].");
	}
}
