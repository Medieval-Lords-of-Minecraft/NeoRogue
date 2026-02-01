package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
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

public class DeliberantPace extends Equipment {
	private static final String ID = "DeliberantPace";
	private int ticksRequired;
	private double damagePerStack;
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANT).count(25).spread(1, 1);
	
	public DeliberantPace(boolean isUpgraded) {
		super(ID, "Deliberant Pace", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		ticksRequired = isUpgraded ? 80 : 100; // 4 or 5 seconds (20 ticks = 1 second)
		damagePerStack = isUpgraded ? 0.06 : 0.05; // 6% or 5%
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		ActionMeta am = new ActionMeta();
		am.setCount(0); // Tick counter
		
		// Reset counter when player starts sprinting
		data.addTrigger(id, Trigger.TOGGLE_SPRINT, (pdata, in) -> {		Player p = data.getPlayer();			if (p.isSprinting()) {
				am.setCount(0);
			}
			return TriggerResult.keep();
		});
		
		// Track ticks without sprinting
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {		Player p = data.getPlayer();			// If player is currently sprinting, reset counter
			if (p.isSprinting()) {
				am.setCount(0);
				return TriggerResult.keep();
			}
			
			// Increment counter for not sprinting
			am.addCount(1);
			
			// Every ticksRequired ticks without sprinting, grant a stack of focus
			if (am.getCount() >= ticksRequired) {
				am.setCount(0); // Reset counter
				data.applyStatus(StatusType.FOCUS, data, 1, -1);
				pc.play(p, p);
				Sounds.enchant.play(p, p);
			}
			
			return TriggerResult.keep();
		});
		
		// Add damage buff based on focus stacks
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			int focusStacks = data.getStatus(StatusType.FOCUS).getStacks();
			if (focusStacks <= 0) return TriggerResult.keep();
			
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
				Buff.multiplier(data, damagePerStack * focusStacks, StatTracker.damageBuffAlly(buffId, this)));
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		int seconds = ticksRequired / 20;
		int damagePercent = (int) (damagePerStack * 100);
		item = createItem(Material.BOOK,
				"Passive. Whenever you don't sprint for " + DescUtil.yellow(seconds + "s") + ", gain a stack of " + 
				GlossaryTag.FOCUS.tag(this, 1, false) + ". Every stack of " + GlossaryTag.FOCUS.tag(this) + 
				" increases your general damage by " + DescUtil.yellow(damagePercent + "%") + ".");
	}
}
