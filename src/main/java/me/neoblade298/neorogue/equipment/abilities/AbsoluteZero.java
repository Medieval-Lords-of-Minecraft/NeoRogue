package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class AbsoluteZero extends Equipment implements Power {
	private static final String ID = "AbsoluteZero";
	private static final ParticleContainer pc = new ParticleContainer(Particle.SNOWFLAKE).count(100).spread(5, 1).speed(0.3);
	private static final TargetProperties tp = TargetProperties.radius(5, false, TargetType.ENEMY);
	private int thres, frost;
	
	public AbsoluteZero(boolean isUpgraded) {
		super(ID, "Absolute Zero", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		thres = isUpgraded ? 8 : 12;
		frost = isUpgraded ? 10 : 6;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	private static final int ACTIVATION_THRES = 5;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta count = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.FROST)) return TriggerResult.keep();
			count.addCount(1);
			if (count.getCount() < ACTIVATION_THRES) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ItemStack icon = item.clone();
		ItemStack charged = item.clone().withType(Material.PACKED_ICE);
		ActionMeta am = new ActionMeta();
		am.setCount(0);
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);

		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata2, in2) -> {
			am.addCount(1);

			if (am.getCount() >= thres) {
				Player p2 = data.getPlayer();
				am.setCount(0);
				icon.setAmount(1);
				inst.setIcon(icon);

				// Play effects
				pc.play(p2, p2);
				Sounds.glass.play(p2, p2);

				// Apply frost and double existing frost in radius
				for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p2, tp)) {
					FightData fd = FightInstance.getFightData(ent);

					// Get current frost stacks
					int currentFrost = fd.getStatus(StatusType.FROST).getStacks();

					// Apply new frost and double existing frost
					int totalFrostToApply = frost + currentFrost;
					FightInstance.applyStatus(ent, StatusType.FROST, data, totalFrostToApply, -1, this);
				}
			} else {
				// Update icon count
				int count2 = am.getCount();
				if (count2 >= thres - 1) {
					// Show charged version
					charged.setAmount(count2);
					inst.setIcon(charged);
				} else {
					icon.setAmount(count2);
					inst.setIcon(icon);
				}
			}

			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.ICE,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after applying " + GlossaryTag.FROST.tag(this) + " " + DescUtil.white(5) + " times. Every " + DescUtil.yellow(thres) + " times you deal damage, apply " + 
				GlossaryTag.FROST.tag(this, frost, true) + " in a wide radius around you and double " +
				GlossaryTag.FROST.tag(this) + " on all affected enemies.");
	}
}
