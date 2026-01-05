package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreEvadeEvent;

public class ThunderclapAndFlash extends Equipment {
	private static final String ID = "ThunderclapAndFlash";
	private static final int ELECTRIFIED_THRESHOLD = 1000;
	private static final ParticleContainer part = new ParticleContainer(Particle.ELECTRIC_SPARK)
			.count(5).spread(0.3, 0.3);
	private static final TargetProperties tp = TargetProperties.line(4, 2, TargetType.ENEMY);
	private int evadeDamage;
	private int castDamage;
	
	public ThunderclapAndFlash(boolean isUpgraded) {
		super(ID, "Thunderclap and Flash", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 15, 10, 0));
		evadeDamage = isUpgraded ? 200 : 150;
		castDamage = isUpgraded ? 150 : 100;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta stacks = new ActionMeta();
		ItemStack icon = item.clone();
		ItemStack activeIcon = icon.withType(Material.LIGHTNING_ROD);
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		
		// On evade: dash forward, deal lightning damage, gain stacks
		data.addTrigger(id, Trigger.EVADE, (pdata, in) -> {
			PreEvadeEvent ev = (PreEvadeEvent) in;
			
			// Get the damager entity from the DamageMeta
			if (ev.getDamageMeta() == null || ev.getDamageMeta().getOwner() == null) {
				return TriggerResult.keep();
			}
			
			LivingEntity damager = ev.getDamageMeta().getOwner().getEntity();
			Location playerLoc = p.getLocation();
			Location damagerLoc = damager.getLocation();

			// Calculate dash direction toward the enemy
			Vector towardEnemy = damagerLoc.toVector().subtract(playerLoc.toVector()).normalize();

			// Dash toward the enemy
			data.dash(towardEnemy);
			
			// Deal lightning damage to the attacker
			FightInstance.dealDamage(pdata, DamageType.LIGHTNING, evadeDamage, damager, 
					DamageStatTracker.of(id + slot, this));
			Sounds.thunder.play(p, p);
			
			// Check total electrified applied
			int totalElectrified = data.getStats().getStatusesApplied().getOrDefault(StatusType.ELECTRIFIED, 0);
			int stacksToGain = totalElectrified >= ELECTRIFIED_THRESHOLD ? 2 : 1;
			
			stacks.addCount(stacksToGain);
			
			// Update icon
			if (stacks.getCount() > 0) {
				ItemStack currentIcon = activeIcon.clone();
				currentIcon.setAmount(Math.min(stacks.getCount(), 64)); // Cap at 64 for display
				inst.setIcon(currentIcon);
			}
			
			return TriggerResult.keep();
		});
		
		// On cast: dash forward, deal lightning damage in a line, consume a stack
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			if (stacks.getCount() <= 0) return TriggerResult.keep();
			
			// Dash forward
			Vector direction = p.getEyeLocation().getDirection().setY(0).normalize();
			data.dash(direction);
			
			// Calculate line endpoints
			Location start = p.getLocation().add(0, 1, 0);
			Location end = start.clone().add(direction.multiply(tp.range));
			
			// Draw line particles
			ParticleUtil.drawLine(p, part, start, end, 0.5);
			Sounds.thunder.play(p, p);
			
			// Deal damage to all entities in line
			for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, start, end, tp)) {
				FightInstance.dealDamage(pdata, DamageType.LIGHTNING, castDamage, ent,
						DamageStatTracker.of(id + slot, this));
			}
			
			// Consume a stack
			stacks.addCount(-1);
			
			// Update icon
			if (stacks.getCount() > 0) {
				ItemStack currentIcon = activeIcon.clone();
				currentIcon.setAmount(Math.min(stacks.getCount(), 64));
				inst.setIcon(currentIcon);
			} else {
				inst.setIcon(icon);
			}
			
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WIND_CHARGE,
				"Passive. Upon " + GlossaryTag.EVADE.tag(this) + ", " + GlossaryTag.DASH.tag(this) + " toward the attacker, " +
				"deal " + GlossaryTag.LIGHTNING.tag(this, evadeDamage, true) + " damage, and gain a stack " +
				"(<white>2</white> stacks if you've applied <white>" + ELECTRIFIED_THRESHOLD + "</white> " + 
				GlossaryTag.ELECTRIFIED.tag(this) + " this fight). On cast, consume a stack to " +
				GlossaryTag.DASH.tag(this) + " forward and deal " + GlossaryTag.LIGHTNING.tag(this, castDamage, true) + 
				" damage in a line.");
	}
}
