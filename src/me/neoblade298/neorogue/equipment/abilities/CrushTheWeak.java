package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class CrushTheWeak extends Equipment {
	private static final String ID = "CrushTheWeak";
	private static final int SHIELDS = 10;
	private int threshold;
	private double damagePercent;
	
	public CrushTheWeak(boolean isUpgraded) {
		super(ID, "Crush the Weak", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		threshold = isUpgraded ? 150 : 200;
		damagePercent = isUpgraded ? 0.6 : 0.4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		
		// Track injury applied and grant permanent shields when threshold is reached
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.INJURY)) return TriggerResult.keep();
			
			am.addCount(ev.getStacks());
			if (am.getCount() >= threshold) {
				Player p = data.getPlayer();
				data.addPermanentShield(p.getUniqueId(), SHIELDS * (am.getCount() / threshold));
				am.setCount(am.getCount() % threshold);
			}
			return TriggerResult.keep();
		});
		
		// Deal additional damage to injured enemies based on current shields
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			LivingEntity target = ev.getTarget();
			FightData fd = FightInstance.getFightData(target);
			
			// Check if target has injury
			if (fd == null || !fd.hasStatus(StatusType.INJURY)) return TriggerResult.keep();
			
			// Get current shields amount
			double currentShields = data.getShields().getAmount();
			if (currentShields <= 0) return TriggerResult.keep();
			
			// Deal additional blunt damage equal to % of current shields
			double bonusDamage = currentShields * damagePercent;
			ev.getMeta().addDamageSlice(new DamageSlice(data, bonusDamage, DamageType.BLUNT, 
					DamageStatTracker.of(id + slot, this)));
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BRICK,
				"Passive. Gain " + GlossaryTag.SHIELDS.tag(this, SHIELDS, false) + " every " + 
				DescUtil.yellow(threshold) + " " + GlossaryTag.INJURY.tag(this) + " you apply. " +
				"Enemies with " + GlossaryTag.INJURY.tag(this) + " take " + 
				DescUtil.yellow((int)(damagePercent * 100) + "%") + " of your current " + 
				GlossaryTag.SHIELDS.tag(this) + " as additional " + GlossaryTag.BLUNT.tag(this) + " damage.");
	}
}
