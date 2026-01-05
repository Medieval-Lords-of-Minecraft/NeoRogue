package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class LordOfTheNight extends Equipment {
	private static final String ID = "LordOfTheNight";
	private int damageIncrease;
    private double damageIncreaseMult;
	
	public LordOfTheNight(boolean isUpgraded) {
		super(ID, "Lord of the Night", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, 
				EquipmentType.ABILITY, EquipmentProperties.none());
		damageIncrease = isUpgraded ? 30 : 20;
        damageIncreaseMult = damageIncrease / 100.0;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, inputs) -> {
			// Get stealth stacks
			int stealthStacks = data.getStatus(StatusType.STEALTH).getStacks();
			if (stealthStacks <= 0) {
				return TriggerResult.keep();
			}
			
			// Apply damage buff based on stealth stacks
			PreDealDamageEvent ev = (PreDealDamageEvent) inputs;
			double totalIncrease = damageIncreaseMult * stealthStacks;
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), 
				new Buff(data, 0, totalIncrease, StatTracker.damageBuffAlly(id, this)));
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_SWORD, 
			"Increases " + GlossaryTag.GENERAL.tag(this) + " damage by <yellow>" + damageIncrease + "%</yellow> for every stack of " + 
			GlossaryTag.STEALTH.tag(this) + " you have.");
	}
}