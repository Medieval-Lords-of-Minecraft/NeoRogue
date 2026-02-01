package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class Atrophy extends Equipment {
	private static final String ID = "Atrophy";
	private static TargetProperties tp = TargetProperties.line(8, 2, TargetType.ENEMY);
	private static ParticleContainer pc = new ParticleContainer(Particle.SMOKE).count(50).spread(0.5, 2).offsetY(1);
	private int damage, ins;
	
	public Atrophy(boolean isUpgraded) {
		super(ID, "Atrophy", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 5, tp.range));
		damage = isUpgraded ? 20 : 14;
		ins = isUpgraded ? 90 : 60;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		AtrophyInstance inst = new AtrophyInstance(data, this, slot, es);
		data.addTrigger(ID, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			if (!ev.getTarget().equals(inst.trg)) return TriggerResult.keep();
			FightData fd = FightInstance.getFightData(ev.getTarget());
			ev.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.DARK, DamageStatTracker.of(id + slot, this)));
			fd.applyStatus(StatusType.INSANITY, data, ins, -1);
			return TriggerResult.keep();
		});
		
		data.addTrigger(ID, bind, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.OBSIDIAN,
				"On cast, mark the target you're looking at. Damaging the marked target deals an additional " +
				GlossaryTag.DARK.tag(this, damage, true) + " damage and applies " + GlossaryTag.INSANITY.tag(this, ins, true)
				+ " to them. Marking another target removes the previous mark.");
	}
	
	private class AtrophyInstance extends EquipmentInstance {
		private LivingEntity trg;
		
		public AtrophyInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);
			action = (pdata, in) -> {
				Player p = data.getPlayer();
				trg = TargetHelper.getNearestInSight(p, Atrophy.tp);
				if (trg == null) return TriggerResult.keep();
				Sounds.infect.play(p, trg);
				pc.play(p, trg);
				return TriggerResult.keep();
			};
		}
	}
}
