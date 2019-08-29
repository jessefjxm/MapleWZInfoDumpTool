package Etc;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author w00481566
 *
 */
public class AchievementInfoDumpTool {

	private static boolean debug = true;

	public static void main(String[] args) {
		if (args.length < 1) {
			String[] defaultArgs = { "D:\\软件\\MapleStory-RPG-master\\Achievement\\AchievementData" };
			// String[] defaultArgs = { "C:\\Games\\BMS\\wz\\Etc\\Achievement\\AchievementData" };
			args = defaultArgs;
		}
		String path = args[0];
		for (int i = 2; i < args.length; i++) {
			if (args[i].equals("--debug")) {
				debug = true;
			}
		}
		if (debug)
			System.out.println("input = " + Arrays.toString(args));
		// 读取文件
		File dir = new File(path);
		if (!dir.exists() || !dir.isDirectory()) {
			System.err.println("不是有效文件夹！");
			return;
		}
		for (File xml : dir.listFiles()) {
			int id = Integer.parseInt(xml.getName().substring(0, xml.getName().length() - 8));
			readXML(xml, id);
		}
		// 输出结果
		/*
		 * File writename = new File(outPath + "\\地图基本信息.txt"); if (writename.exists()) writename.delete(); try {
		 * writename.createNewFile(); BufferedWriter out = new BufferedWriter(new FileWriter(writename)); int lastkey = 0, startkey = 0;
		 * String lastvalue = null, lastline = null; for (int key : mapInfo.keySet()) { if (!existMaps.contains(key)) continue; String
		 * value = mapInfo.get(key); if (key == lastkey + 1 && lastvalue != null && lastvalue.equals(value)) { lastkey = key; continue; }
		 * if (lastkey != startkey) { out.write(startkey + " ~TO~> " + lastkey + lastvalue + "\r\n"); // \r\n即为换行 } else if (lastline !=
		 * null) { out.write(lastline); // \r\n即为换行 } startkey = lastkey = key; lastvalue = value; lastline = key + value + "\r\n"; } if
		 * (lastline != null) { out.write(lastline); // \r\n即为换行 } out.flush(); // 把缓存区内容压入文件 out.close(); // 最后记得关闭文件 } catch (Exception
		 * e) { e.printStackTrace(); }
		 */
		if (debug) {
			System.out.println("// 任务ID -> 成就");
			System.out.print("int[][] questCheckArray = {");
			for (int key : questCheck.keySet()) {
				System.out.print("{" + key + ", " + questCheck.get(key) + "}, ");
			}
			System.out.println("{-1,-1} };");
			System.out.println("// 地图ID -> 成就");
			System.out.print("int[][] discoverCheckArray = {");
			for (int key : discoverCheck.keySet()) {
				System.out.print("{" + key + ", " + discoverCheck.get(key) + "}, ");
			}
			System.out.println("{-1,-1} };");
		}
	}

	private static class AchievementInfo {
		// 主
		int id;
		String mainCategory;
		String subCategory;
		String name;
		String desc;
		int count;

		// 次
		public AchievementInfo(int id, String mainCategory, String subCategory, String name, String desc) {
			this.id = id;
			this.mainCategory = mainCategory;
			this.subCategory = subCategory;
			this.name = name;
			this.desc = desc;
		};
	}

	private static class SubAchievementInfo {
		AchievementInfo achievementInfo;
		int totalId;
		// 次
		int subId;
		String subCategoryname;
		String subMissionType;
		int checkValue;

		public SubAchievementInfo(AchievementInfo achievementInfo, int subId, String subCategoryname, String subMissionType,
				int checkValue) {
			this.achievementInfo = achievementInfo;
			this.subId = subId;
			this.totalId = this.achievementInfo.id * 1000 + subId;
			this.subCategoryname = subCategoryname;
			this.subMissionType = subMissionType;
			this.checkValue = checkValue;
		};

		public String toString() {
			return totalId + " - " + achievementInfo.name + " - " + subCategoryname + " - 要求 " + subMissionType + ":" + checkValue;
		}
	}

	/**
	 * TYPE 根据成就ID查成就信息<br>
	 */
	private static HashMap<Integer, AchievementInfo> achievement = new HashMap<>();
	private static HashMap<Integer, SubAchievementInfo> subAchievement = new HashMap<>();
	/**
	 * 地图探索成就<br>
	 * CHECK 根据地图ID查成就信息<br>
	 */
	private static HashMap<Integer, Integer> discoverCheck = new HashMap<>();
	/**
	 * 完成任务/剧情/副本、清空地区任务成就<br>
	 * CHECK 根据任务ID查成就信息<br>
	 */
	private static HashMap<Integer, Integer> questCheck = new HashMap<>();

	private static void readXML(File file, int id) {
		SAXReader reader = new SAXReader();
		try {
			Document doc = reader.read(file);
			Element root = doc.getRootElement(); // xxx.img
			// <imgdir name="info">
			Iterator<Element> rootIter = root.elementIterator("imgdir");
			Element info = rootIter.next();
			Iterator<Element> j = info.elementIterator("string");
			String mainCategory = j.next().attribute("value").getValue().toString();
			String subCategory = j.next().attribute("value").getValue().toString();
			String name = j.next().attribute("value").getValue().toString();
			String desc = j.next().attribute("value").getValue().toString();
			if (!achievement.containsKey(id)) {
				achievement.put(id, new AchievementInfo(id, mainCategory, subCategory, name, desc));
			}
			AchievementInfo mainAchievement = achievement.get(id);
			// <imgdir name="mission">
			Element mission = rootIter.next();
			String lastName = null;
			for (Iterator<Element> k = mission.elementIterator("imgdir"); k.hasNext();) {
				// <imgdir name="0">
				Element subMission = k.next();
				int subId = Integer.parseInt(subMission.attribute("name").getValue().toString());
				// <string name="name" value="重设内在能力后达到A级"/>
				Iterator<Element> kString = subMission.elementIterator("string");
				String subCategoryname;
				if (kString.hasNext()) {
					subCategoryname = kString.next().attribute("value").getValue().toString();
					lastName = subCategoryname;
				} else {
					subCategoryname = lastName;
				}
				// <imgdir name="subMission">
				Iterator<Element> k2 = subMission.elementIterator("imgdir");
				// <imgdir name="ability_change">
				Iterator<Element> k3 = k2.next().elementIterator("imgdir");
				Element eSubMissionType = k3.next();
				String subMissionType = eSubMissionType.attribute("name").getValue().toString();
				// <imgdir name="score">
				Iterator<Element> k4 = eSubMissionType.elementIterator("imgdir");
				do {
					Element eCheckValue = k4.next();
					if (eCheckValue.attribute("name").getValue().toString().equals("checkValue")) {
						// <imgdir name="checkValue">
						int value = 0;
						switch (subMissionType) {
						case "field_enter":
							Element eCheckValueData = eCheckValue.elementIterator("imgdir").next();
							Iterator<Element> iValues = eCheckValueData.elementIterator("int");
							if (iValues.hasNext()) { // 单个任务
								value = Integer.parseInt(iValues.next().attribute("value").getValue().toString());
								updateMap(discoverCheck,
										new SubAchievementInfo(mainAchievement, subId, subCategoryname, subMissionType, value));
							} else { // 多个任务
								for (Iterator<Element> iCheckValueDataSet = eCheckValueData.elementIterator("imgdir").next()
										.elementIterator("imgdir"); iCheckValueDataSet.hasNext();) {
									value = Integer.parseInt(iCheckValueDataSet.next().elementIterator("int").next().attribute("value")
											.getValue().toString());
									updateMap(discoverCheck,
											new SubAchievementInfo(mainAchievement, subId, subCategoryname, subMissionType, value));
								}
							}
							break;
						case "quest_state_change":
							eCheckValueData = eCheckValue.elementIterator("imgdir").next();
							iValues = eCheckValue.elementIterator("imgdir").next().elementIterator("int");
							if (iValues.hasNext()) { // 单个任务
								value = Integer.parseInt(iValues.next().attribute("value").getValue().toString());
								updateMap(questCheck,
										new SubAchievementInfo(mainAchievement, subId, subCategoryname, subMissionType, value));
							} else { // 多个任务
								for (Iterator<Element> iCheckValueDataSet = eCheckValueData.elementIterator("imgdir").next()
										.elementIterator("int"); iCheckValueDataSet.hasNext();) {
									value = Integer.parseInt(iCheckValueDataSet.next().attribute("value").getValue().toString());
									updateMap(questCheck,
											new SubAchievementInfo(mainAchievement, subId, subCategoryname, subMissionType, value));
								}
							}
							break;
						default:
							break;
						}
						break;
					}
				} while (true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void updateMap(HashMap<Integer, Integer> check, SubAchievementInfo newInfo) {
		int aId = newInfo.achievementInfo.id * 1000 + newInfo.subId;
		subAchievement.put(aId, newInfo);
		check.put(newInfo.checkValue, aId);
	}
}
