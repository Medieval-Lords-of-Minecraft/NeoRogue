package me.neoblade298.neorogue.commands;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;

final class EquipmentCategoryClassifier {
	private static final String FIGHT_INSTANCE = "me/neoblade298/neorogue/session/fight/FightInstance";
	private static final String FIGHT_DATA = "me/neoblade298/neorogue/session/fight/FightData";
	private static final String PLAYER_FIGHT_DATA = "me/neoblade298/neorogue/session/fight/PlayerFightData";
	private static final String DAMAGE_META = "me/neoblade298/neorogue/session/fight/DamageMeta";
	private static final String STATUS_TYPE = "me/neoblade298/neorogue/session/fight/status/StatusType";

	private EquipmentCategoryClassifier() {}

	public static String classify(Equipment equipment) throws IOException {
		Behavior behavior = inspectClassFamily(equipment.getClass());
		if (equipment.getType() == EquipmentType.WEAPON || behavior.dealsDamage) return "OFFENSE";
		if (behavior.grantsDefense()) return "DEFENSE";
		return "OTHER";
	}

	private static Behavior inspectClassFamily(Class<?> equipmentClass) throws IOException {
		String className = equipmentClass.getName().replace('.', '/');
		Behavior behavior = new Behavior();
		try {
			Path location = Path.of(equipmentClass.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (Files.isDirectory(location)) {
				inspectDirectory(location, className, behavior);
			} else {
				inspectJar(location, className, behavior);
			}
		} catch (URISyntaxException ex) {
			throw new IOException("Invalid class location for " + equipmentClass.getName(), ex);
		}
		return behavior;
	}

	private static void inspectDirectory(Path location, String className, Behavior behavior) throws IOException {
		Path classFile = location.resolve(className + ".class");
		Path parent = classFile.getParent();
		String simpleName = classFile.getFileName().toString().replace(".class", "");
		try (Stream<Path> files = Files.list(parent)) {
			for (Path file : files.filter(path -> isClassFamily(path.getFileName().toString(), simpleName)).toList()) {
				try (InputStream input = Files.newInputStream(file)) {
					inspectClass(input, behavior);
				}
			}
		}
	}

	private static void inspectJar(Path location, String className, Behavior behavior) throws IOException {
		try (JarFile jar = new JarFile(location.toFile())) {
			for (JarEntry entry : jar.stream().filter(candidate -> isClassFamily(candidate.getName(), className)).toList()) {
				try (InputStream input = jar.getInputStream(entry)) {
					inspectClass(input, behavior);
				}
			}
		}
	}

	private static boolean isClassFamily(String fileName, String baseName) {
		return fileName.equals(baseName + ".class")
				|| fileName.startsWith(baseName + "$") && fileName.endsWith(".class");
	}

	private static void inspectClass(InputStream input, Behavior behavior) throws IOException {
		try (DataInputStream data = new DataInputStream(input)) {
			if (data.readInt() != 0xCAFEBABE) throw new IOException("Invalid class file");
			data.readUnsignedShort();
			data.readUnsignedShort();
			ConstantPoolEntry[] pool = readConstantPool(data);
			for (ConstantPoolEntry entry : pool) {
				if (entry == null) continue;
				if (entry.tag == 10 || entry.tag == 11) {
					String owner = className(pool, entry.first);
					String name = memberName(pool, entry.second);
					behavior.recordMethod(owner, name);
				} else if (entry.tag == 9) {
					behavior.recordField(className(pool, entry.first), memberName(pool, entry.second));
				}
			}
		}
	}

	private static ConstantPoolEntry[] readConstantPool(DataInputStream data) throws IOException {
		ConstantPoolEntry[] pool = new ConstantPoolEntry[data.readUnsignedShort()];
		for (int index = 1; index < pool.length; index++) {
			int tag = data.readUnsignedByte();
			switch (tag) {
			case 1 -> pool[index] = new ConstantPoolEntry(tag, 0, 0, data.readUTF());
			case 3, 4 -> data.skipNBytes(4);
			case 5, 6 -> {
				data.skipNBytes(8);
				index++;
			}
			case 7, 8, 16, 19, 20 -> pool[index] = new ConstantPoolEntry(tag, data.readUnsignedShort(), 0, null);
			case 9, 10, 11, 12, 17, 18 -> pool[index] = new ConstantPoolEntry(tag,
					data.readUnsignedShort(), data.readUnsignedShort(), null);
			case 15 -> {
				data.readUnsignedByte();
				data.readUnsignedShort();
			}
			default -> throw new IOException("Unsupported constant pool tag " + tag);
			}
		}
		return pool;
	}

	private static String className(ConstantPoolEntry[] pool, int classIndex) {
		ConstantPoolEntry classEntry = pool[classIndex];
		return utf8(pool, classEntry.first);
	}

	private static String memberName(ConstantPoolEntry[] pool, int nameAndTypeIndex) {
		ConstantPoolEntry nameAndType = pool[nameAndTypeIndex];
		return utf8(pool, nameAndType.first);
	}

	private static String utf8(ConstantPoolEntry[] pool, int index) {
		return pool[index].text;
	}

	private record ConstantPoolEntry(int tag, int first, int second, String text) {}

	private static class Behavior {
		private boolean dealsDamage;
		private boolean grantsShields;
		private boolean addsDefenseBuff;
		private boolean tracksAlliedDefense;
		private boolean appliesStatus;
		private boolean referencesProtectOrShell;

		private void recordMethod(String owner, String name) {
			if (owner.equals(FIGHT_INSTANCE) && name.equals("dealDamage")) dealsDamage = true;
			if ((owner.equals(FIGHT_DATA) || owner.equals(PLAYER_FIGHT_DATA))
					&& (name.equals("addShield") || name.equals("addSimpleShield")
							|| name.equals("addPermanentShield"))) {
				grantsShields = true;
			}
			if ((owner.equals(FIGHT_DATA) || owner.equals(PLAYER_FIGHT_DATA) || owner.equals(DAMAGE_META))
					&& name.equals("addDefenseBuff")) {
				addsDefenseBuff = true;
			}
			if (name.equals("defenseBuffAlly") || name.equals("damageBarriered")) tracksAlliedDefense = true;
			if ((owner.equals(FIGHT_DATA) || owner.equals(PLAYER_FIGHT_DATA)) && name.equals("applyStatus")) {
				appliesStatus = true;
			}
		}

		private void recordField(String owner, String name) {
			if (owner.equals(STATUS_TYPE) && (name.equals("PROTECT") || name.equals("SHELL"))) {
				referencesProtectOrShell = true;
			}
		}

		private boolean grantsDefense() {
			return grantsShields || addsDefenseBuff && tracksAlliedDefense
					|| appliesStatus && referencesProtectOrShell;
		}
	}
}