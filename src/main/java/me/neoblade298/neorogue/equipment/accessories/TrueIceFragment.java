package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class TrueIceFragment extends Equipment {
	private static final String ID = "TrueIceFragment";
	private static final int ICE_DAMAGE = 10;
	private static final int FROST = 4;
	private static final ParticleContainer FROST_PROC = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.BLUE_ICE.createBlockData()).count(10).spread(0.1, 0.1).offsetY(0.8).speed(0.01);
	private int attackThreshold;

	public TrueIceFragment(boolean isUpgraded) {
		super(ID, "True Ice Fragment", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER, EquipmentType.ACCESSORY);
		attackThreshold = isUpgraded ? 5 : 8;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta attacks = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			PreBasicAttackEvent event = (PreBasicAttackEvent) in;
			event.getMeta().addDamageSlice(new DamageSlice(data, ICE_DAMAGE, DamageType.ICE,
					DamageStatTracker.of(id + slot, this)));
			if (attacks.addCount(1) < attackThreshold) return TriggerResult.keep();

			attacks.setCount(0);
			FightInstance.applyStatus(event.getTarget(), StatusType.FROST, data, FROST, -1, this);
			Player player = data.getPlayer();
			FROST_PROC.play(player, event.getTarget().getLocation());
			Sounds.glass.play(player, event.getTarget());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLUE_ICE, "Basic attacks deal an additional "
				+ GlossaryTag.ICE.tag(this, ICE_DAMAGE) + " damage. Every " + DescUtil.yellow(attackThreshold)
				+ " basic attacks additionally applies " + GlossaryTag.FROST.tag(this, FROST) + ".");
	}
}