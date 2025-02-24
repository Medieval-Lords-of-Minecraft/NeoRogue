package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class FirstStrike extends Equipment {
	private static final String ID = "firstStrike";
	private static final ParticleContainer pc = new ParticleContainer(Particle.PORTAL);
	private static final TargetProperties tp = TargetProperties.radius(4, false, TargetType.ENEMY);
	private int damage;
	
	public FirstStrike(boolean isUpgraded) {
		super(ID, "First Strike", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(5, 5, 10, 4));
		pc.count(50).spread(0.5, 0.5).offsetY(1);
		damage = isUpgraded ? 225 : 150;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_SWORD,
				"On cast, damage the nearest enemy for " + GlossaryTag.PIERCING.tag(this, damage, true) + " and then dash"
				+ " forward. Gaining " + GlossaryTag.STEALTH.tag(this) + " or " + GlossaryTag.EVADE.tag(this) + " takes it off cooldown.");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			Sounds.flap.play(p, p);
			DamageMeta dm = new DamageMeta(data, damage, DamageType.PIERCING);
			LivingEntity trg = TargetHelper.getNearest(p, tp);
			if (trg != null) FightInstance.dealDamage(dm, trg);
			Vector v = p.getEyeLocation().getDirection();
			if (p.isOnGround()) {
				p.teleport(p.getLocation().add(0, 0.2, 0));
			}
			p.setVelocity(v.setY(0).normalize().multiply(2).setY(-1));
			return TriggerResult.keep();
		});
		
		data.addTrigger(ID, bind, inst);
		data.addTrigger(ID, Trigger.PRE_RECEIVE_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (ev.getStatusId().equals(StatusType.EVADE.name()) || ev.getStatusId().equals(StatusType.STEALTH.name())) {
				inst.setCooldown(0);
			}
			return TriggerResult.keep();
		});
	}
}
