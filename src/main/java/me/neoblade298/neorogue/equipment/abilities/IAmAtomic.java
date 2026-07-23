package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class IAmAtomic extends Equipment implements Power {
	private static final String ID = "IAmAtomic";
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANT).count(25).spread(0.5, 0.5).speed(0.1);
	private static final ParticleContainer atomicEffect = new ParticleContainer(Particle.ELECTRIC_SPARK).count(20).spread(0.5, 0.5).speed(0.3);
	private static final double DAMAGE_INCREASE_PER_THRESHOLD = 0.2; // 20% per threshold
	private static final int ACTIVATION_THRES = 3;
	
	private int intel, manaThreshold;
	
	public IAmAtomic(boolean isUpgraded) {
		super(ID, "I Am Atomic", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		intel = 3;
		manaThreshold = isUpgraded ? 40 : 50;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		int[] riftHitCount = {0};
		boolean[] activated = {false};
		
		// Power: activates after dealing rift damage 3 times
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!hasRiftDamage(ev)) return TriggerResult.keep();
			
			riftHitCount[0]++;
			if (riftHitCount[0] < ACTIVATION_THRES) return TriggerResult.keep();
			if (activated[0]) return TriggerResult.remove();
			activated[0] = true;
			
			Player p = data.getPlayer();
			Sounds.enchant.play(p, p);
			atomicEffect.play(p, p);
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}
	
	private boolean hasRiftDamage(DealDamageEvent ev) {
		return ev.getMeta().hasOrigin(DamageOrigin.RIFT);
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		String buffId = UUID.randomUUID().toString();
		// After activation: gain intellect on rift damage, damage increase from mana
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata2, in2) -> {
			DealDamageEvent ev2 = (DealDamageEvent) in2;
			if (!hasRiftDamage(ev2)) return TriggerResult.keep();

			Player p2 = data.getPlayer();
			data.applyStatus(StatusType.INTELLECT, data, intel, -1, this);
			Sounds.enchant.play(p2, p2);
			pc.play(p2, p2);
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata2, in2) -> {
			PreDealDamageEvent ev2 = (PreDealDamageEvent) in2;

			int manaThresholds = (int) (data.getMana() / manaThreshold);
			if (manaThresholds <= 0) return TriggerResult.keep();

			double damageMultiplier = manaThresholds * DAMAGE_INCREASE_PER_THRESHOLD;
			ev2.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
					Buff.multiplier(data, damageMultiplier, StatTracker.damageBuffAlly(buffId, this)));

			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.TOTEM_OF_UNDYING,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after dealing " + GlossaryTag.RIFT.tag(this) + 
				" damage " + DescUtil.val(ACTIVATION_THRES) + " times. " +
				"Gain " + GlossaryTag.INTELLECT.tag(this, intel) + " every time you deal " +
				GlossaryTag.RIFT.tag(this) + " damage. Damage dealt is increased by " + 
				DescUtil.val("20%") + " for every " + DescUtil.val(manaThreshold) + 
				" mana you have.");
	}
}
