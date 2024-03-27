package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SoulShackles extends Equipment {
	private static final String ID = "soulShackles";
	private static final int healthCostPercent = 12;
	private static final int manaCostPercent = 8;
	private static final int staminaCostPercent = 3;
	private static final int range = 5;

	private static final TargetProperties hitScan = TargetProperties.line(range, 10, TargetType.ENEMY);
	
	private int dmgRatio;

	private LivingEntity currTarget;

	public SoulShackles(boolean isUpgraded) {
		super(
				ID, "Soul Shackles", isUpgraded, Rarity.RARE, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(10, 10, 90, range)
		);
		dmgRatio = isUpgraded ? 10 : 5;
	}

	@Override
	public void setupReforges() {
		//addSelfReforge();
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pdata, inputs) -> {
			if (currTarget != null)
				return TriggerResult.keep();
			
			currTarget = TargetHelper.getNearestInSight(p, hitScan);
			if (currTarget == null) {
				return TriggerResult.keep();
			}

			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, PotionEffect.INFINITE_DURATION, 255));
			p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, PotionEffect.INFINITE_DURATION, 255));
			currTarget.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, PotionEffect.INFINITE_DURATION, 255));
			currTarget.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, PotionEffect.INFINITE_DURATION, 255));
			
			data.addTrigger(id, Trigger.PLAYER_TICK, (pdata2, in) -> {
				if (pdata2.isDead() || pdata2.getMana() < 1 || pdata2.getStamina() < 1 || currTarget.isDead()) {
					p.removePotionEffect(PotionEffectType.SLOW);
					p.removePotionEffect(PotionEffectType.JUMP);
					currTarget.removePotionEffect(PotionEffectType.SLOW);
					currTarget.removePotionEffect(PotionEffectType.JUMP);
					currTarget = null;
					return TriggerResult.remove();
				}
				
				double healthChg = pdata2.getMaxHealth() * (healthCostPercent / 100.0);
				double manaChg = pdata2.getMaxHealth() * (manaCostPercent / 100.0);
				double staminaChg = pdata2.getMaxHealth() * (staminaCostPercent / 100.0);

				pdata2.addHealth(-healthChg);
				pdata2.addMana(-manaChg);
				pdata2.addStamina(-staminaChg);

				double sum = healthChg + manaChg + staminaChg;

				FightInstance.applyStatus(currTarget, StatusType.INSANITY, p, (int) Math.ceil(sum / 2), -1);
				FightInstance.dealDamage(pdata2, DamageType.DARK, sum * dmgRatio, currTarget);
				
				return TriggerResult.keep();
			});
			
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.CHAIN,
				"On cast, root yourself and a target enemy. Each second, drain <white>" + healthCostPercent
						+ "%</white> of your max health, <white>" + manaCostPercent
						+ "%</white> of your max mana, and <white>" + staminaCostPercent
						+ "%</white> of your max stamina." + " The target receives half the sum of these as "
						+ GlossaryTag.INSANITY.tag(this) + ", and <yellow>" + dmgRatio + "</yellow> times the sum as "
						+ GlossaryTag.DARK.tag(this) + " damage. This ability cannot be cancelled."
		);
	}
}
