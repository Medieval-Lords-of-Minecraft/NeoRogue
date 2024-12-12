package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class Brand extends Equipment {
	private static final String ID = "brand";
	private static final TargetProperties tp = TargetProperties.radius(8, false, TargetType.ENEMY),
		aoe = TargetProperties.radius(4, false, TargetType.ENEMY);
	private static final Circle circ = new Circle(aoe.range);
	private int burn;
	private double damage;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CRIMSON_SPORE).count(50).spread(0.3, 0.3).offsetY(2),
		fill = new ParticleContainer(Particle.LAVA),
		edges = new ParticleContainer(Particle.FLAME);
	
	public Brand(boolean isUpgraded) {
		super(ID, "Brand", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(30, 0, 12, tp.range).add(PropertyType.AREA_OF_EFFECT, aoe.range));
		burn = isUpgraded ? 100 : 70;
		damage = isUpgraded ? 0.8 : 0.5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pd, in) -> {
			LivingEntity trg = TargetHelper.getNearest(p, tp);
			if (trg == null) return TriggerResult.keep();
			pc.play(p, trg);
			Sounds.infect.play(p, trg);
			FightInstance.applyStatus(trg, StatusType.BURN, data, burn, -1);
			am.setEntity(trg);
			data.addTask(new BukkitRunnable() {
				public void run() {
					explode(am, data);
				}
			}.runTaskLater(NeoRogue.inst(), 100));
			return TriggerResult.keep();
		}));

		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent	ev = (PreApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.BURN)) return TriggerResult.keep();
			if (am.getEntity() == null) return TriggerResult.keep();
			if (!ev.getTarget().getUniqueId().equals(am.getEntity().getUniqueId())) return TriggerResult.keep();
			ev.getStacksBuffList().add(new Buff(data, 0, 1, StatTracker.IGNORED));
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.KILL_GLOBAL, (pdata, in) -> {
			LivingEntity ent = (LivingEntity) in;
			if (am.getEntity() == null) return TriggerResult.keep();
			if (!am.getEntity().getUniqueId().equals(ent.getUniqueId())) return TriggerResult.keep();
			explode(am, data);
			return TriggerResult.keep();
		});
	}

	private void explode(ActionMeta am, PlayerFightData data) {
		if (am.getEntity() == null) return;
		Player p = data.getPlayer();
		Location loc = am.getEntity().getLocation();
		int stacks = FightInstance.getFightData(am.getEntity()).getStatus(StatusType.BURN).getStacks();
		am.setEntity(null);
		circ.play(p, edges, loc, LocalAxes.xz(), fill);
		Sounds.fire.play(p, loc);
		for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, aoe)) {
			FightInstance.dealDamage(data, DamageType.FIRE, damage * stacks, ent);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER,
				"On cast, apply " + GlossaryTag.BURN.tag(this, burn, true) + " and mark [<white>5s</white>] the enemy you're looking at. Any " + GlossaryTag.BURN.tag(this) + " applied to a marked enemy is doubled. " +
				"After the mark expires or the target is killed, deal " + GlossaryTag.FIRE.tag(this, damage, true) + " damage per stack of " + GlossaryTag.BURN.tag(this) + " on the marked target to all enemies near it.");
	}
}
