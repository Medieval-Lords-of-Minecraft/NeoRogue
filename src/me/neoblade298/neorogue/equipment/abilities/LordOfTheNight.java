package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
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
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(35, 0, 0, 0));
		damageIncrease = isUpgraded ? 30 : 20;
        damageIncreaseMult = damageIncrease / 100.0;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata2, inputs) -> {
				int stealthStacks = data.getStatus(StatusType.STEALTH).getStacks();
				if (stealthStacks <= 0) {
					return TriggerResult.keep();
				}
				PreDealDamageEvent ev = (PreDealDamageEvent) inputs;
				double totalIncrease = damageIncreaseMult * stealthStacks;
				ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), 
					new Buff(data, 0, totalIncrease, StatTracker.damageBuffAlly(id, this)));
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_SWORD, 
			GlossaryTag.POWER.tag(this) + ". Increases " + GlossaryTag.GENERAL.tag(this) + " damage by " + DescUtil.yellow(damageIncrease + "%") + " for every stack of " + 
			GlossaryTag.STEALTH.tag(this) + " you have.");
	}
}