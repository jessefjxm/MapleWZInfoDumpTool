package Map;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class MapInfoDumpTool {

	private static boolean debug = false;

	public static void main(String[] args) {
		if (args.length < 2) {
			String[] defaultArgs = { "C:\\Games\\BMS\\wz", "C:\\Games\\BMS\\脚本" };
			args = defaultArgs;
		}
		String path = args[0], outPath = args[1];
		for (int i = 2; i < args.length; i++) {
			if (args[i].equals("--debug")) {
				debug = true;
			}
		}
		if (debug)
			System.out.println("input = " + Arrays.toString(args));
		// 读取翻译文件
		if (debug)
			System.out.println("读取翻译文件：" + path + "\\String.wz\\Map.img.xml");
		TreeMap<Integer, String> mapInfo = readTranslation(path + "\\String.wz\\Map.img.xml");
		// 读取地图文件
		if (debug)
			System.out.println("读取地图文件：" + path + "\\Map.wz\\Map");
		HashSet<Integer> existMaps = readMapWZ(path + "\\Map.wz\\Map", mapInfo);
		// 输出结果
		File writename = new File(outPath + "\\地图基本信息.txt");
		if (writename.exists())
			writename.delete();
		try {
			writename.createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(writename));
			int lastkey = 0, startkey = 0;
			String lastvalue = null, lastline = null;
			for (int key : mapInfo.keySet()) {
				if (!existMaps.contains(key))
					continue;
				String value = mapInfo.get(key);
				if (key == lastkey + 1 && lastvalue != null && lastvalue.equals(value)) {
					lastkey = key;
					continue;
				}
				if (lastkey != startkey) {
					out.write(startkey + " ~TO~> " + lastkey + lastvalue + "\r\n"); // \r\n即为换行
				} else if (lastline != null) {
					out.write(lastline); // \r\n即为换行
				}
				startkey = lastkey = key;
				lastvalue = value;
				lastline = key + value + "\r\n";
			}
			if (lastline != null) {
				out.write(lastline); // \r\n即为换行
			}
			out.flush(); // 把缓存区内容压入文件
			out.close(); // 最后记得关闭文件
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static TreeMap<Integer, String> readTranslation(String path) {
		TreeMap<Integer, String> mapInfo = new TreeMap<>();
		SAXReader reader = new SAXReader();
		try {
			Document doc = reader.read(path);
			Element root = doc.getRootElement();
			for (Iterator<Element> i = root.elementIterator("imgdir"); i.hasNext();) {
				Element mapSet = i.next();
				for (Iterator<Element> j = mapSet.elementIterator("imgdir"); j.hasNext();) {
					Element map = j.next();
					String key = map.attribute("name").getValue().toString();
					String info = "";
					for (Iterator<Element> k = map.elementIterator("string"); k.hasNext();) {
						info += " - " + k.next().attribute("value").getValue().toString();
					}
					mapInfo.put(Integer.parseInt(key), info);
					if (debug)
						System.out.println(key + info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mapInfo;
	}

	private static HashSet<Integer> readMapWZ(String path, TreeMap<Integer, String> mapInfo) {
		HashSet<Integer> blankMaps = new HashSet<>();
		try {
			File rootDir = new File(path);
			if (!rootDir.exists())
				return blankMaps;
			File[] listFiles = rootDir.listFiles();
			for (File f : listFiles) {
				if (!f.isDirectory()) {
					continue;
				}
				File[] listXMLs = f.listFiles();
				for (File xml : listXMLs) {
					String name = xml.getName();
					int mapid = Integer.parseInt(name.substring(0, name.length() - 8));
					blankMaps.add(mapid);
					if (!mapInfo.containsKey(mapid)) {
						mapInfo.put(mapid, " - <NULL>");
						if (debug) {
							System.out.println(mapid + " - <NULL>");
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return blankMaps;
	}
}
