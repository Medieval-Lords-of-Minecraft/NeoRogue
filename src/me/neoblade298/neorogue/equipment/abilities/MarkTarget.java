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

public class MarkTarget extends Equipment {
	private static final String ID = "MarkTarget";
	private static final TargetProperties tp = TargetProperties.radius(8, false, TargetType.ENEMY);
	private int rend;
	private double damage;
	private static final ParticleContainer taunt = new ParticleContainer(Particle.CRIMSON_SPORE).count(50).spread(0.3, 0.3).offsetY(2);
	
	public MarkTarget(boolean isUpgraded) {
		super(ID, "Mark Target", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(10, 0, 12, tp.range));
		rend = isUpgraded ? 80 : 50;
		damage = isUpgraded ? 0.4 : 0.3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	public void setupReforges() {
		addReforge(BasicElementMastery.get(), Crystallize.get(), Brand.get());
		addReforge(KeenSenses.get(), Demoralize.get());
		addReforge(AgilityTraining.get(), MarkTarget2.get());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pd, in) -> {
			Player p = data.getPlayer();
			LivingEntity trg = TargetHelper.getNearest(p, tp);
			if (trg == null) return TriggerResult.keep();
			taunt.play(p, trg);
			Sounds.infect.play(p, trg);
			FightInstance.applyStatus(trg, StatusType.REND, data, rend, -1);
			return TriggerResult.keep();
		}));

		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (!fd.hasStatus(StatusType.REND)) return TriggerResult.keep();
			ev.getMeta().addDamageSlice(new DamageSlice(data, damage * fd.getStatus(StatusType.REND).getStacks(), DamageType.SLASHING,
					DamageStatTracker.of(ID + slot, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_BRICK,
				"On cast, apply " + GlossaryTag.REND.tag(this, rend, true) + " to the enemy you're looking at. Additionally, you passively " +
				"deal an additional " + GlossaryTag.SLASHING.tag(this, damage, true) + " damage per stack of " + GlossaryTag.REND.tag(this) + ".");
	}
}
