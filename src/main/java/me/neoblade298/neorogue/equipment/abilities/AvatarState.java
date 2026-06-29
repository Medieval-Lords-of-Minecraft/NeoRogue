package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class AvatarState extends Equipment implements Power {
	private static final String ID = "AvatarState";
	private static final int ACTIVATION_THRES = 5;
	private double mreg, hreg;
	private int shields;

	public AvatarState(boolean isUpgraded) {
		super(ID, "Avatar State", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.none());
		mreg = isUpgraded ? 2.5 : 1.5;
		hreg = isUpgraded ? 1.5 : 1;
		shields = isUpgraded ? 10 : 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			am.addCount(1);
			if (am.getCount() < ACTIVATION_THRES) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		Player p = data.getPlayer();
		data.addManaRegen(mreg);
		data.addSimpleShield(p.getUniqueId(), shields, 200);

		ActionMeta manaRegenMeta = new ActionMeta();
		data.addTrigger(ID + "_mana_regen", Trigger.PLAYER_TICK, (pdata2, in2) -> {
			manaRegenMeta.addCount(1);
			if (manaRegenMeta.getCount() > 10) {
				data.addManaRegen(-mreg);
				return TriggerResult.remove();
			}
			return TriggerResult.keep();
		});

		ActionMeta healthMeta = new ActionMeta();
		data.addTrigger(ID + "_health", Trigger.PLAYER_TICK, (pdata2, in2) -> {
			healthMeta.addCount(1);
			if (healthMeta.getCount() > 10) return TriggerResult.remove();
			data.addHealth(hreg);
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
	item = createItem(Material.GLOWSTONE,
			GlossaryTag.POWER.tag(this) + ". Activates after casting " + DescUtil.white(ACTIVATION_THRES)
					+ " abilities while above 50% mana. Increase mana regen by " + DescUtil.yellow(mreg) + ", health regen by "
					+ DescUtil.yellow(hreg) + ", and gain " + GlossaryTag.SHIELDS.tag(this, shields, true)
					+ ", all lasting 10s.");
	}
}
