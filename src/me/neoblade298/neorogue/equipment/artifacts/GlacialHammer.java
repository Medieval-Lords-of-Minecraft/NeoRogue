package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class GlacialHammer extends Artifact {
	private static final String ID = "glacialHammer";

	public GlacialHammer() {
		super(ID, "Glacial Hammer", Rarity.RARE, EquipmentClass.CLASSLESS);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
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
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_SHOVEL, 
				"Landing a basic attack when you're above <white>50%</white> mana slows the enemy and costs <white>1</white> mana.");
	}
}
