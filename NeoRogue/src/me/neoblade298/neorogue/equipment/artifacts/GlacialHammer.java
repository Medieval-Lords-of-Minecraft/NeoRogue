package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class GlacialHammer extends Artifact {

	public GlacialHammer() {
		super("glacialHammer", "Glacial Hammer", Rarity.RARE, EquipmentClass.CLASSLESS);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			if (data.getMana() > data.getMaxMana() * 0.5) {
				BasicAttackEvent ev = (BasicAttackEvent) in;
				data.addMana(-1);
				ev.getTarget().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 0));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_SHOVEL, 
				"Landing a basic attack when you're above 50% mana slows the enemy and costs 1 mana.");
	}
}
