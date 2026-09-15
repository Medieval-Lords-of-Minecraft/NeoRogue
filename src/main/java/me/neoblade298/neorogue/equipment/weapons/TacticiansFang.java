package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class TacticiansFang extends Equipment {
	private static final String ID = "TacticiansFang";
	private static final ParticleContainer POISON_HIT = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(83, 173, 69), 1F)).count(8).spread(0.15, 0.3).offsetY(1);
	private int damage, poisonedDamage, poisonDuration;
	private double poisonMultiplier;

	public TacticiansFang(boolean isUpgraded) {
		super(ID, "Tactician's Fang", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(30, 1, 0.2, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		damage = isUpgraded ? 120 : 80;
		poisonedDamage = 40;
		poisonMultiplier = isUpgraded ? 0.3 : 0.2;
		poisonDuration = 40;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ItemStack icon = item.clone().withType(Material.WOODEN_SWORD);
		StandardPriorityAction timer = new StandardPriorityAction(ID);
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, sessionEq, slot, es);
		timer.setAction((pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().containsType(DamageCategory.DIRECT)) {
				return TriggerResult.keep();
			}
			inst.setIcon(icon);
			timer.setTime(System.currentTimeMillis());
			data.addTask(new BukkitRunnable() {
				public void run() {
					inst.setIcon(item);
				}
			}.runTaskLater(NeoRogue.inst(), 60));
			return TriggerResult.keep();
		});
		data.addTrigger(ID, Trigger.DEAL_DAMAGE, timer);

		inst.setAction((pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			FightData targetData = FightInstance.getFightData(ev.getTarget());
			boolean isPoisoned = targetData != null && targetData.hasStatus(StatusType.POISON);
			int currentPoison = isPoisoned ? targetData.getStatus(StatusType.POISON).getStacks() : 0;
			boolean hasBonus = timer.getTime() + 3000 < System.currentTimeMillis();
			weaponSwingAndDamage(data.getPlayer(), data, ev.getTarget(), properties.get(PropertyType.DAMAGE)
					+ (hasBonus ? damage : 0) + (isPoisoned ? poisonedDamage : 0));
			if (isPoisoned) {
				int poisonToApply = (int) (currentPoison * poisonMultiplier);
				if (poisonToApply > 0) {
					FightInstance.applyStatus(ev.getTarget(), StatusType.POISON, data, poisonToApply, poisonDuration, this);
				}
				POISON_HIT.play(data.getPlayer(), ev.getTarget());
			}
			return TriggerResult.keep();
		});

		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_SWORD, "Deal an additional " + GlossaryTag.PIERCING.tag(this, damage) + " if "
				+ "you haven't dealt " + GlossaryTag.DIRECT.tag(this) + " damage in " + DescUtil.val(3) + " seconds. "
				+ "Against enemies with " + GlossaryTag.POISON.tag(this) + ", deal an additional "
				+ GlossaryTag.PIERCING.tag(this, poisonedDamage) + " and apply "
				+ DescUtil.val((int) (poisonMultiplier * 100) + "%") + " of their current "
				+ GlossaryTag.POISON.tag(this) + " [" + DescUtil.val(poisonDuration / 20 + "s") + "].");
	}
}