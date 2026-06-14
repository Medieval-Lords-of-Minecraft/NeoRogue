package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
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
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class CrushTheWeak extends Equipment implements Power {
	private static final String ID = "CrushTheWeak";
	private static final int SHIELDS = 10;
	private int threshold;
	private double damagePercent;
	
	public CrushTheWeak(boolean isUpgraded) {
		super(ID, "Crush the Weak", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		threshold = isUpgraded ? 18 : 25;
		damagePercent = isUpgraded ? 0.5 : 0.35;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta damageCount = new ActionMeta();
		ActionMeta injuryCount = new ActionMeta();
		
		// Track total damage dealt
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			damageCount.addDouble(ev.getTotalDamage());
			return TriggerResult.keep();
		});
		
		// Track injury applications
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (ev.isStatus(StatusType.INJURY)) {
				injuryCount.addCount(1);
			}
			return TriggerResult.keep();
		});
		
		// Poll both conditions
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (damageCount.getDouble() < 500 || injuryCount.getCount() < 5) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ActionMeta am = new ActionMeta();

		// Track injury applied and grant permanent shields when threshold is reached
		data.addTrigger(id + "-active", Trigger.APPLY_STATUS, (pdata2, in2) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in2;
			if (!ev.isStatus(StatusType.INJURY)) return TriggerResult.keep();

			am.addCount(ev.getStacks());
			if (am.getCount() >= threshold) {
				Player p2 = data.getPlayer();
				data.addPermanentShield(p2.getUniqueId(), SHIELDS * (am.getCount() / threshold));
				am.setCount(am.getCount() % threshold);
			}
			return TriggerResult.keep();
		});

		// Deal additional damage to injured enemies based on current shields
		data.addTrigger(id + "-bonus", Trigger.PRE_DEAL_DAMAGE, (pdata2, in2) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in2;
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
				GlossaryTag.POWER.tag(this) + ". Activates after dealing " + DescUtil.white(500) + " damage and applying " + GlossaryTag.INJURY.tag(this) + " " + DescUtil.white(5) + " times. Gain " + GlossaryTag.SHIELDS.tag(this, SHIELDS, false) + " every " + 
				DescUtil.yellow(threshold) + " " + GlossaryTag.INJURY.tag(this) + " you apply. " +
				"Enemies with " + GlossaryTag.INJURY.tag(this) + " take " + 
				DescUtil.yellow((int)(damagePercent * 100) + "%") + " of your current " + 
				GlossaryTag.SHIELDS.tag(this) + " as additional " + GlossaryTag.BLUNT.tag(this) + " damage.");
	}
}
