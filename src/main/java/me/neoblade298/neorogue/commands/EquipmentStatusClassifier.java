package me.neoblade298.neorogue.commands;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;

final class EquipmentStatusClassifier {
	private static final String STATUS_TYPE = "me/neoblade298/neorogue/session/fight/status/Status$StatusType";

	private EquipmentStatusClassifier() {}

	public static StatusInteractions classify(Equipment equipment) throws IOException {
		return classify(equipment.getClass());
	}

	static StatusInteractions classify(Class<? extends Equipment> equipmentClass) throws IOException {
		Behavior behavior = new Behavior();
		for (Class<?> type = equipmentClass; type != null && Equipment.class.isAssignableFrom(type)
				&& type != Equipment.class; type = type.getSuperclass()) {
			inspectClassFamily(type, behavior);
		}
		behavior.finish();
		return new StatusInteractions(Set.copyOf(behavior.applies), Set.copyOf(behavior.uses));
	}

	public record StatusInteractions(Set<StatusType> applies, Set<StatusType> uses) {}

	private static void inspectClassFamily(Class<?> equipmentClass, Behavior behavior) throws IOException {
		String className = equipmentClass.getName().replace('.', '/');
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
			data.skipNBytes(6);
			skipInterfaces(data);
			skipMembers(data);
			inspectMethods(data, pool, behavior);
		}
	}

	private static void skipInterfaces(DataInputStream data) throws IOException {
		data.skipNBytes(data.readUnsignedShort() * 2L);
	}

	private static void skipMembers(DataInputStream data) throws IOException {
		int count = data.readUnsignedShort();
		for (int index = 0; index < count; index++) {
			data.skipNBytes(6);
			skipAttributes(data);
		}
	}

	private static void inspectMethods(DataInputStream data, ConstantPoolEntry[] pool, Behavior behavior)
			throws IOException {
		int count = data.readUnsignedShort();
		for (int index = 0; index < count; index++) {
			data.skipNBytes(2);
			String methodName = utf8(pool, data.readUnsignedShort());
			data.skipNBytes(2);
			int attributeCount = data.readUnsignedShort();
			for (int attributeIndex = 0; attributeIndex < attributeCount; attributeIndex++) {
				String name = utf8(pool, data.readUnsignedShort());
				int length = data.readInt();
				byte[] attribute = data.readNBytes(length);
				if (name.equals("Code")) inspectCode(attribute, pool, behavior, methodName);
			}
		}
	}

	private static void skipAttributes(DataInputStream data) throws IOException {
		int count = data.readUnsignedShort();
		for (int index = 0; index < count; index++) {
			data.readUnsignedShort();
			data.skipNBytes(Integer.toUnsignedLong(data.readInt()));
		}
	}

	private static void inspectCode(byte[] attribute, ConstantPoolEntry[] pool, Behavior behavior, String methodName)
			throws IOException {
		try (DataInputStream codeData = new DataInputStream(new ByteArrayInputStream(attribute))) {
			codeData.skipNBytes(4);
			byte[] code = codeData.readNBytes(codeData.readInt());
			inspectInstructions(code, pool, behavior, methodName);
		}
	}

	private static void inspectInstructions(byte[] code, ConstantPoolEntry[] pool, Behavior behavior, String methodName)
			throws IOException {
		MethodBehavior method = new MethodBehavior(methodName.equals("<clinit>"));
		for (int offset = 0; offset < code.length;) {
			int opcode = unsigned(code[offset]);
			if (opcode == 0xB2) {
				ConstantPoolEntry field = pool[readUnsignedShort(code, offset + 1)];
				if (STATUS_TYPE.equals(className(pool, field.first))) {
					method.reference(StatusType.valueOf(memberName(pool, field.second)));
				}
			} else if (opcode >= 0xB6 && opcode <= 0xB9) {
				ConstantPoolEntry member = pool[readUnsignedShort(code, offset + 1)];
				method.invoke(memberName(pool, member.second));
			}
			offset += instructionLength(code, offset, opcode);
		}
		behavior.add(method);
	}

	private static int instructionLength(byte[] code, int offset, int opcode) throws IOException {
		return switch (opcode) {
		case 0x10, 0x12, 0x15, 0x16, 0x17, 0x18, 0x19, 0x36, 0x37, 0x38, 0x39, 0x3A,
				0xA9, 0xBC -> 2;
		case 0x11, 0x13, 0x14, 0x84, 0x99, 0x9A, 0x9B, 0x9C, 0x9D, 0x9E, 0x9F, 0xA0,
				0xA1, 0xA2, 0xA3, 0xA4, 0xA5, 0xA6, 0xA7, 0xA8, 0xB2, 0xB3, 0xB4, 0xB5,
				0xB6, 0xB7, 0xB8, 0xBB, 0xBD, 0xC0, 0xC1, 0xC6, 0xC7 -> 3;
		case 0xC5 -> 4;
		case 0xB9, 0xBA, 0xC8, 0xC9 -> 5;
		case 0xAA -> tableSwitchLength(code, offset);
		case 0xAB -> lookupSwitchLength(code, offset);
		case 0xC4 -> wideLength(code, offset);
		default -> 1;
		};
	}

	private static int tableSwitchLength(byte[] code, int offset) throws IOException {
		int aligned = alignSwitch(offset);
		ensureAvailable(code, aligned, 12);
		int low = readInt(code, aligned + 4);
		int high = readInt(code, aligned + 8);
		long entries = (long) high - low + 1;
		if (entries < 0 || entries > Integer.MAX_VALUE / 4) throw new IOException("Invalid tableswitch");
		return aligned - offset + 12 + (int) entries * 4;
	}

	private static int lookupSwitchLength(byte[] code, int offset) throws IOException {
		int aligned = alignSwitch(offset);
		ensureAvailable(code, aligned, 8);
		int pairs = readInt(code, aligned + 4);
		if (pairs < 0 || pairs > Integer.MAX_VALUE / 8) throw new IOException("Invalid lookupswitch");
		return aligned - offset + 8 + pairs * 8;
	}

	private static int alignSwitch(int offset) {
		return (offset + 4) & ~3;
	}

	private static int wideLength(byte[] code, int offset) throws IOException {
		ensureAvailable(code, offset + 1, 1);
		return unsigned(code[offset + 1]) == 0x84 ? 6 : 4;
	}

	private static int readUnsignedShort(byte[] bytes, int offset) throws IOException {
		ensureAvailable(bytes, offset, 2);
		return unsigned(bytes[offset]) << 8 | unsigned(bytes[offset + 1]);
	}

	private static int readInt(byte[] bytes, int offset) throws IOException {
		ensureAvailable(bytes, offset, 4);
		return unsigned(bytes[offset]) << 24 | unsigned(bytes[offset + 1]) << 16
				| unsigned(bytes[offset + 2]) << 8 | unsigned(bytes[offset + 3]);
	}

	private static int unsigned(byte value) {
		return value & 0xFF;
	}

	private static void ensureAvailable(byte[] bytes, int offset, int length) throws IOException {
		if (offset < 0 || offset + length > bytes.length) throw new IOException("Invalid bytecode instruction");
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
		return utf8(pool, pool[classIndex].first);
	}

	private static String memberName(ConstantPoolEntry[] pool, int nameAndTypeIndex) {
		return utf8(pool, pool[nameAndTypeIndex].first);
	}

	private static String utf8(ConstantPoolEntry[] pool, int index) {
		return pool[index].text;
	}

	private record ConstantPoolEntry(int tag, int first, int second, String text) {}

	private static final class MethodBehavior {
		private final EnumSet<StatusType> references = EnumSet.noneOf(StatusType.class);
		private final EnumSet<StatusType> applies = EnumSet.noneOf(StatusType.class);
		private final EnumSet<StatusType> uses = EnumSet.noneOf(StatusType.class);
		private final boolean staticInitializer;
		private StatusType pendingStatus;
		private boolean callsApply;

		private MethodBehavior(boolean staticInitializer) {
			this.staticInitializer = staticInitializer;
		}

		private void reference(StatusType status) {
			references.add(status);
			pendingStatus = status;
		}

		private void invoke(String methodName) {
			if (methodName.equals("applyStatus")) {
				callsApply = true;
				if (pendingStatus != null) applies.add(pendingStatus);
				pendingStatus = null;
			} else if (methodName.equals("getStatus") || methodName.equals("hasStatus")
					|| methodName.equals("isStatus") || methodName.equals("removeStatus")) {
				if (pendingStatus != null) uses.add(pendingStatus);
				pendingStatus = null;
			}
		}
	}

	private static final class Behavior {
		private final EnumSet<StatusType> applies = EnumSet.noneOf(StatusType.class);
		private final EnumSet<StatusType> uses = EnumSet.noneOf(StatusType.class);
		private final EnumSet<StatusType> staticReferences = EnumSet.noneOf(StatusType.class);
		private boolean hasDynamicApply;

		private void add(MethodBehavior method) {
			if (method.callsApply && method.applies.isEmpty()) {
				method.applies.addAll(method.references);
				hasDynamicApply = true;
			}
			applies.addAll(method.applies);
			uses.addAll(method.uses);
			EnumSet<StatusType> remaining = EnumSet.copyOf(method.references);
			remaining.removeAll(method.applies);
			remaining.removeAll(method.uses);
			if (method.staticInitializer) {
				staticReferences.addAll(remaining);
			} else {
				uses.addAll(remaining);
			}
		}

		private void finish() {
			if (hasDynamicApply) applies.addAll(staticReferences);
		}
	}
}