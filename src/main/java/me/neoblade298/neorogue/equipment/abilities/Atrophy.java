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
import me.neoblade298.neorogue.equipment.SessionEquipment;
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
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
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
		ins = isUpgraded ? 8 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		String markId = data.getPlayer().getUniqueId() + "-" + id + "-" + slot;
		AtrophyInstance inst = new AtrophyInstance(data, sessionEq, slot, es, markId);
		data.addTrigger(ID, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (fd == null || !fd.hasStatus(markId)) return TriggerResult.keep();
			ev.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.DARK, DamageStatTracker.of(id + slot, this)));
			fd.applyStatus(StatusType.INSANITY, data, ins, -1, this);
			return TriggerResult.keep();
		});
		
		data.addTrigger(ID, bind, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.OBSIDIAN,
				"On cast, mark the target you're looking at. Damaging the marked target deals an additional " +
				GlossaryTag.DARK.tag(this, damage) + " damage and applies " + GlossaryTag.INSANITY.tag(this, ins)
				+ " to them. Marking another target removes the previous mark.");
	}
	
	private class AtrophyInstance extends EquipmentInstance {
		private FightData markedTarget;
		
		public AtrophyInstance(PlayerFightData data, SessionEquipment sessionEq, int slot, EquipSlot es, String markId) {
			super(data, sessionEq, slot, es);
			action = (pdata, in) -> {
				Player p = data.getPlayer();
				LivingEntity trg = TargetHelper.getNearestInSight(p, Atrophy.tp);
				if (trg == null) return TriggerResult.keep();
				FightData targetData = FightInstance.getFightData(trg);
				if (targetData == null) return TriggerResult.keep();
				if (markedTarget != null && markedTarget.hasStatus(markId)) {
					markedTarget.removeStatus(markId);
				}
				Status mark = Status.createByGenericType(GenericStatusType.BASIC, markId, targetData, true);
				targetData.applyStatus(mark, data, 1, -1, Atrophy.this);
				markedTarget = targetData;
				Sounds.infect.play(p, trg);
				pc.play(p, trg);
				return TriggerResult.keep();
			};
		}
	}
}
