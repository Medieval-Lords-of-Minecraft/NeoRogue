package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;

public class IceShield extends Equipment {
	private static final String ID = "iceShield";
	private int shieldPercent;

	public IceShield(boolean isUpgraded) {
		super(ID, "Ice Shield", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.OFFHAND);
		shieldPercent = isUpgraded ? 50 : 30;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RAISE_SHIELD, (pdata, inputs) -> {
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, PotionEffect.INFINITE_DURATION, 1));
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.LOWER_SHIELD, (pdata, inputs) -> {
			p.removePotionEffect(PotionEffectType.SLOW); // todo: mitigate players spamming to avoid slow
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, (pdata, inputs) -> {
			if (!p.isHandRaised())
				return TriggerResult.keep();
			
			ReceivedDamageEvent ev = (ReceivedDamageEvent) inputs;
			DamageMeta meta = ev.getMeta();
			if (meta.isSecondary())
				return TriggerResult.keep();
			
			//data.addSimpleShield(p.getUniqueId(), TODO getDmg() here * shieldPercent / 100.0, 200);
			p.playSound(p, Sound.ITEM_SHIELD_BLOCK, 1F, 1F);
			return TriggerResult.keep();
		});
	}
	
	@Override
	public void setupItem() {
		item = createItem(
				Material.SHIELD,
				"When raised, you are slowed, and <yellow>" + shieldPercent
						+ "%</yellow> of health damage taken is granted back as " + GlossaryTag.SHIELDS.tag(this)
						+ " for <white>10</white> seconds."
		);
	}
}
