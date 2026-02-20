package com.yojori.migration.worker.strategy.sample;

import com.yojori.util.Config;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.List;
import java.util.Map;

/**
 * Sample class for File Migration using Data-Driven Java Strategy.
 * This class processes a List of Maps (Result of a Select Query) to migrate files.
 */
public class FileMigration {

	private static final Log log = LogFactory.getLog(FileMigration.class);

	/**
	 * Main processing method.
	 * 
	 * @param list The data list fetched from the Source DB by JavaMigrationStrategy.
	 *             Each Map represents a row from the Source SQL.
	 */
	@SuppressWarnings("rawtypes")
	public void goMigration(List<Map> list) {
		log.info("#################################################");
		log.info("#################################################");
		log.info("#################################################");
		log.info("#################################################");
		log.info("Starting goMigration with " + (list != null ? list.size() : 0) + " items.");
		
		String fromPath = "D:\\upload\\old\\";
		String toPath = "D:\\upload\\new\\";

		if (list == null || list.isEmpty()) {
			return;
		}

		for (int i = 0; i < list.size(); i++) {
			Map map = list.get(i);
			String file_nm = (String) map.get("ATT_FILE_NM");
			String file_path = (String) map.get("ATT_FILE_PATH");

			if (file_nm == null || file_path == null) {
				log.warn("Skipping row " + i + ": Missing ATT_FILE_NM or ATT_FILE_PATH");
				continue;
			}

			File file = new File(fromPath + file_path + "\\" + file_nm);

			log.info("item " + i + " org full path : " + fromPath + file_path + "\\" + file_nm);

			if (!file.exists()) {
				log.warn("File not found: " + file.getAbsolutePath());
				continue;
			}

			File pDir = file.getParentFile();
			String orgDir = pDir.getPath();
			orgDir = orgDir.replace(fromPath, toPath);

			File intoDir = new File(orgDir);
			if (!intoDir.exists()) {
				intoDir.mkdirs();
			}

			FileInputStream fis = null;
			FileOutputStream fos = null;

			try {
				fis = new FileInputStream(file);
				fos = new FileOutputStream(orgDir + "\\" + file_nm);

				log.info("to File : " + orgDir + "\\" + file_nm);

				byte[] b = new byte[4096]; 
				int cnt = 0;
				while ((cnt = fis.read(b)) != -1) { 
					fos.write(b, 0, cnt); 
				}

			} catch (Exception e) {
				log.error("Error migrating file: " + file.getName(), e);
			} finally {
				try {
					if (fis != null) fis.close();
					if (fos != null) fos.close();
				} catch (IOException e) {
					log.error("Error closing streams", e);
				}
			}
		}
		log.info("goMigration end");
	}

	@SuppressWarnings("rawtypes")
	public void goMigration1(List<Map> list) {
		log.info("Starting goMigration1 with " + (list != null ? list.size() : 0) + " items.");

		if (list == null || list.isEmpty()) {
			return;
		}

		for (int i = 0; i < list.size(); i++) {
			Map map = list.get(i);
			String file_nm = (String) map.get("ATT_FILE_NM");
			String file_path = (String) map.get("ATT_FILE_PATH");
			
			log.info("item " + i + " : ATT_FILE_NM=" + file_nm + ", ATT_FILE_PATH=" + file_path);
		}
		log.info("goMigration1 end");
	}
	
	// Other methods (makeFile, makeParam, etc) can be kept if needed, 
	// but I'm focusing on the representative 'goMigration' method for now.
	// Dependencies like AES256 are kept from user's code.

	public static String getViewString(String content_seq, String key_code, String resolution, String ordering,
			String trans_yn) {
		StringBuffer ap = new StringBuffer().append("{\"content_seq\":\"").append(content_seq)
				.append("\", \"service_type\":\"front\", \"detail_type\":\"").append(key_code)
				.append("\", \"resolution\":\"").append(resolution).append("\", \"order\":\"").append(ordering)
				.append("\", \"trans_yn\":\"").append(trans_yn).append("\"}");

		return ap.toString();
	}
}
