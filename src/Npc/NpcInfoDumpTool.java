package Npc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class NpcInfoDumpTool {

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
		HashMap<Integer, String> npcInfo = readTranslation(path + "\\String.wz\\Npc.img.xml");
		// 输出结果
		try {
			// 序列化导出
			ObjectOutputStream outSer = new ObjectOutputStream(new FileOutputStream(outPath + "\\NpcInfo.ser"));
			outSer.writeObject(npcInfo);
			outSer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static HashMap<Integer, String> readTranslation(String path) {
		HashMap<Integer, String> npcInfo = new HashMap<>();
		SAXReader reader = new SAXReader();
		try {
			Document doc = reader.read(path);
			Element root = doc.getRootElement();
			for (Iterator<Element> i = root.elementIterator("imgdir"); i.hasNext();) {
				Element npcElement = i.next();
				int id = Integer.parseInt(npcElement.attribute("name").getValue());
				String name = "无名";
				Iterator<Element> j = npcElement.elementIterator("string");
				if (j.hasNext()) {
					Element nameElement = j.next();
					if (nameElement.attribute("value") != null)
						name = nameElement.attribute("value").getValue().toString();
				}
				npcInfo.put(id, name);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return npcInfo;
	}
}
