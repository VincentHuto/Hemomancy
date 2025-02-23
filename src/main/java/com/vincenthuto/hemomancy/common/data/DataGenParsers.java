package com.vincenthuto.hemomancy.common.data;

public class DataGenParsers {

	public static String genBaseBlockState(String modid, String id) {
		String output = "{\r\n" + "    \"variants\": {\r\n" + "        \"\": { \"model\": \"" + modid + ":block/" + id
				+ "\" }\r\n" + "    }\r\n" + "}";
		return output;

	}

	public static String genBasicBlockModel(String modid, String id) {
		String output = "{\r\n" + "   \"parent\": \"block/cube_all\",\r\n" + "   \"textures\": {\r\n"
				+ "       \"all\": \"" + modid + ":blocks/" + id + "\"\r\n" + "   }\r\n" + "}";
		return output;
	}

	public static String genColumnHorizontalBlockModel(String modid, String id) {
		String output = "{\r\n" + "	\"parent\": \"minecraft:block/cube_column_horizontal\",\r\n"
				+ "	\"textures\": {\r\n" + "		\"end\": \"" + modid + ":blocks/" + id + "_top\",\r\n"
				+ "		\"side\": \"" + modid + ":blocks/" + id + "\"\r\n" + "	}\r\n" + "}";
		return output;
	}

	public static String genColumnVerticalBlockModel(String modid, String id) {
		String output = "{\r\n" + "    \"parent\": \"block/cube_column\",\r\n" + "    \"textures\":\r\n" + "    {\r\n"
				+ "        \"end\": \"" + modid + ":blocks/" + id + "_top\",\r\n" + "        \"side\": \"" + modid
				+ ":blocks/" + id + "\"\r\n" + "    }\r\n" + "}";
		return output;
	}

	public static String genCrosslBlockModel(String modid, String id) {
		String output = "{\r\n" + "    \"parent\": \"block/cross\",\r\n" + "    \"textures\": {\r\n"
				+ "        \"cross\": \"" + modid + ":blocks/" + id + "\"\r\n" + "    }\r\n" + "}\r\n" + "";
		return output;
	}

	public static String genObjBlockModel(String modid, String id, String particleId) {
		String output = "{\r\n" + "  \"parent\":\"forge:block/default\",\r\n" + "  \"loader\": \"forge:obj\",\r\n"
				+ "  \"model\": \"" + modid + ":models/block/" + id + ".obj\",\r\n" + "  	\"textures\": {\r\n"
				+ "		\"particle\": \"" + modid + ":blocks/" + particleId + "\"\r\n" + "		}\r\n" + "}\r\n" + "";
		return output;
	}

	public static String genRotatableBlockstate(String modid, String id) {

		String output = "{\r\n" + "	\"variants\": {\r\n" + "		\"facing=north\": {\r\n"
				+ "			\"model\": \"" + modid + ":block/" + id + "\"\r\n" + "		},\r\n"
				+ "		\"facing=south\": {\r\n" + "			\"model\": \"" + modid + ":block/" + id + "\",\r\n"
				+ "			\"y\": 180\r\n" + "		},\r\n" + "		\"facing=west\": {\r\n" + "			\"model\": \""
				+ modid + ":block/" + id + "\",\r\n" + "			\"y\": 270\r\n" + "		},\r\n"
				+ "		\"facing=east\": {\r\n" + "			\"model\": \"" + modid + ":block/" + id + "\",\r\n"
				+ "			\"y\": 90\r\n" + "		}\r\n" + "	}\r\n" + "}";

		return output;

	}
}
