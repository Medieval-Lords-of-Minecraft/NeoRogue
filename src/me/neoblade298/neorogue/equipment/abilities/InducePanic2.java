package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class InducePanic2 extends Equipment {
	private static final String ID = "inducePanic2";
	private static TargetProperties tp = TargetProperties.line(8, 2, TargetType.ENEMY),
		radius = TargetProperties.radius(10, false, TargetType.ENEMY);
	private static ParticleContainer pc = new ParticleContainer(Particle.SMOKE).count(50).spread(0.5, 2).offsetY(1);
	private int stacks;
	
	public InducePanic2(boolean isUpgraded) {
		super(ID, "Induce Panic II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 5, tp.range));
				stacks = isUpgraded ? 150 : 100;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		InducePanicInstance inst = new InducePanicInstance(data, this, slot, es);
		data.addTrigger(ID, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			if (inst.mark == null) return TriggerResult.keep();
			DealDamageEvent ev = (DealDamageEvent) in;
			if (ev.getMeta().containsType(DamageCategory.GENERAL) && ev.getTarget().getUniqueId().equals(inst.mark.getUniqueId())) {
				inst.mark = null;
			}
			return TriggerResult.keep();
		});
	}

	private class InducePanicInstance extends EquipmentInstance	{
		private LivingEntity mark;
		public InducePanicInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);
			InducePanicInstance inst = this;
			Player p = data.getPlayer();
			action = (pdata, in) -> {
				pdata.addTask(new BukkitRunnable() {
					public void run() {
						if (inst.mark == null) return;
						for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, radius)) {
							FightInstance.applyStatus(ent, StatusType.INSANITY, pdata, stacks, -1);
							pc.play(p, ent);
							Sounds.infect.play(p, ent);
						}
					}
				}.runTaskLater(NeoRogue.inst(), 40L));
				return TriggerResult.keep();
			};
		}

		@Override
		public boolean canTrigger(Player p, PlayerFightData data, Object in) {
			if (!super.canTrigger(p, data, in)) return false;
			mark = TargetHelper.getNearestInSight(p, tp);
			return mark != null;
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.OBSIDIAN,
				"On cast, mark the target you're looking at. If you don't deal " + GlossaryTag.GENERAL.tag(this) + " damage to that enemy for <white>2</white> seconds," +
				" apply " + GlossaryTag.INSANITY.tag(this, stacks, true) + " to enemies around them in a radius of <white>10</white>.");
	}
}
