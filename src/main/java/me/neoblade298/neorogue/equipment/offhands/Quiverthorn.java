package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class Quiverthorn extends Equipment {
	private static final String ID = "Quiverthorn";
	private static final int BASE_DAMAGE = 30;
	private static final int MAX_STACKS = 20;
	private int bonusDamage;
	private double statusPercent;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CRIT)
		.count(15).spread(0.5, 0.5);
	
	public Quiverthorn(boolean isUpgraded) {
		super(ID, "Quiverthorn", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.OFFHAND, 
				EquipmentProperties.ofWeapon(BASE_DAMAGE, 1, DamageType.SLASHING, Sounds.attackSweep));
		bonusDamage = isUpgraded ? 15 : 10;
		statusPercent = isUpgraded ? 1.5 : 1.0;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		ItemStack icon = item.clone();
		
		// Track stacks when dealing projectile damage
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().hasOrigin(DamageOrigin.PROJECTILE)) return TriggerResult.keep();
			
			if (am.getCount() < MAX_STACKS) {
				am.addCount(1);
				Player p = data.getPlayer();
				int currentStacks = am.getCount();
				
				// At max stacks, play success sound and switch to diamond
				if (currentStacks >= MAX_STACKS) {
					Sounds.success.play(p, p.getLocation());
					icon.withType(Material.DIAMOND_SWORD);
				}
				
				icon.setAmount(currentStacks);
				p.getInventory().setItemInOffHand(icon);
			}
			return TriggerResult.keep();
		});
		
		// Left click to consume stacks and deal damage
		data.addTrigger(id, Trigger.LEFT_CLICK_HIT, (pdata, in) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) in;
			Player p = data.getPlayer();
			if (!data.canBasicAttack(EquipSlot.OFFHAND)) return TriggerResult.keep();
			
			int stacks = am.getCount();
			if (stacks <= 0) return TriggerResult.keep();
			
			weaponSwing(p, data);
			LivingEntity target = ev.getTarget();
			FightData fd = FightInstance.getFightData(target);
			// Calculate base damage with bonus per stack
			double totalDamage = BASE_DAMAGE + (bonusDamage * stacks);
			
			DamageMeta dm = new DamageMeta(data);
			dm.addDamageSlice(new DamageSlice(data, totalDamage, DamageType.PIERCING, 
					DamageStatTracker.of(id + slot, this)));
			dm.isBasicAttack(this, true);
			
			// At max stacks, apply status effects based on target's current statuses
			if (stacks >= MAX_STACKS && fd != null) {
				int burnStacks = fd.getStatus(StatusType.BURN).getStacks();
				int frostStacks = fd.getStatus(StatusType.FROST).getStacks();
				int injuryStacks = fd.getStatus(StatusType.INJURY).getStacks();
				int rendStacks = fd.getStatus(StatusType.REND).getStacks();
				
				if (burnStacks > 0) {
					int toApply = (int) (burnStacks * statusPercent);
					FightInstance.applyStatus(target, StatusType.BURN, data, toApply, -1);
				}
				if (frostStacks > 0) {
					int toApply = (int) (frostStacks * statusPercent);
					FightInstance.applyStatus(target, StatusType.FROST, data, toApply, -1);
				}
				if (injuryStacks > 0) {
					int toApply = (int) (injuryStacks * statusPercent);
					FightInstance.applyStatus(target, StatusType.INJURY, data, toApply, -1);
				}
				if (rendStacks > 0) {
					int toApply = (int) (rendStacks * statusPercent);
					FightInstance.applyStatus(target, StatusType.REND, data, toApply, -1);
				}
				
				pc.play(p, target.getLocation());
			}
			
			FightInstance.dealDamage(dm, target);
			Sounds.anvil.play(p, target.getLocation());
			
			// Consume all stacks and reset icon to iron
			am.setCount(0);
			icon.withType(Material.IRON_SWORD);
			icon.setAmount(1);
			p.getInventory().setItemInOffHand(icon);
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_SWORD,
				"Dealing projectile damage grants a stack (max " + DescUtil.white(MAX_STACKS) + "). " +
				"Left clicking an enemy deals " + DescUtil.white(BASE_DAMAGE) + " + " + DescUtil.yellow(bonusDamage) + " " +
				GlossaryTag.PIERCING.tag(this) + " damage per stack and consumes all stacks. " +
				"At max stacks, applies " + DescUtil.yellow((int)(statusPercent * 100) + "%") + " of the enemy's current " +
				GlossaryTag.BURN.tag(this) + ", " + GlossaryTag.FROST.tag(this) + ", " + GlossaryTag.INJURY.tag(this) + ", and " + GlossaryTag.REND.tag(this) + ".");
	}
}
