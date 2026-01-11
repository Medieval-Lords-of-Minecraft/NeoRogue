package me.neoblade298.neorogue.equipment.accessories;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class CobraCrest extends Equipment {
	private static final String ID = "CobraCrest";
	private int dec;
	
	public CobraCrest(boolean isUpgraded) {
		super(ID, "Cobra Crest", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ACCESSORY, EquipmentProperties.none());
		dec = isUpgraded ? 50 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().containsType(DamageType.POISON)) return TriggerResult.keep();
			
			// Apply slowness 1
			ev.getTarget().addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0));
			
			// Decrease magical defense
			FightInstance.getFightData(ev.getTarget()).addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL),
					Buff.increase(data, -dec, BuffStatTracker.defenseDebuffEnemy(buffId, this)), 60);
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIME_DYE,
				"Dealing " + GlossaryTag.POISON.tag(this) + " damage applies " + DescUtil.potion("Slowness", 0, 3) + 
				" and decreases " + GlossaryTag.MAGICAL.tag(this) + " defense by <yellow>" + dec + "</yellow> [<white>3s</white>].");
	}
}
