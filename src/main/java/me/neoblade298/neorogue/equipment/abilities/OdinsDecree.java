package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
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
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class OdinsDecree extends Equipment implements Power {
	private static final String ID = "OdinsDecree";
	private static final ParticleContainer pc = new ParticleContainer(Particle.FIREWORK)
			.count(100)
			.spread(0.3, 2)
			.offsetY(1)
			.speed(0.3);
	private static final TargetProperties tp = TargetProperties.radius(20, false, TargetType.ENEMY);
	private int damage, electrified;
	private double chance;
	
	public OdinsDecree(boolean isUpgraded) {
		super(ID, "Odin's Decree", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		damage = isUpgraded ? 125 : 85;
		electrified = isUpgraded ? 10 : 6;
		chance = isUpgraded ? 1.0 : 0.5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().containsType(DamageType.LIGHTNING)) return TriggerResult.keep();
			if (data.getMana() < data.getMaxMana() * 0.5) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata2, in2) -> {
			am.addCount(1);
			if (am.getCount() >= 2) {
				Player p2 = data.getPlayer();
				am.setCount(0);
				if (Math.random() >= chance) return TriggerResult.keep();
				LivingEntity target = TargetHelper.getNearest(p2, tp);
				if (target == null) return TriggerResult.keep();
				pc.play(p2, target.getLocation());
				Sounds.thunder.play(p2, target.getLocation());
				FightInstance.dealDamage(pdata2, DamageType.LIGHTNING, damage, target, 
						DamageStatTracker.of(id + slot, this));
				FightInstance.applyStatus(target, StatusType.ELECTRIFIED, data, electrified, -1, this);
			}
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.LIGHTNING_ROD,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after dealing " + GlossaryTag.LIGHTNING.tag(this) + " damage while above " + DescUtil.val("50%") + " mana. Every " + DescUtil.val("2s") + ", " + DescUtil.val((int)(chance * 100) + "%") + " " +
				"chance to drop a lightning bolt on the nearest enemy, dealing " + 
				GlossaryTag.LIGHTNING.tag(this, damage) + " damage and applying " + 
				GlossaryTag.ELECTRIFIED.tag(this, electrified) + ".");
	}
}
