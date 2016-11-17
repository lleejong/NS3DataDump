package com.choilab.proj.skt.NS3DataDump;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;


public class App {

	private static final String external_prog = "./shell_sim.sh";
	static String strOutput = "";
	private static Connection conn;

	
	public static void main(String args[]) {
		String fileName = "./test_input.txt";
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
			conn = DriverManager.getConnection("jdbc:mysql//localhost/simpletest", "root", "");
			
			BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
			String line = null;
			fileReader.readLine();

			// 장비명 그룹명 장비아이피 수집시간 TX_LOSS TX_DELAY TX_JITTER TX 상태 RX_LOSS
			// RX_DELAY RX_JITTER RX 상태
			while ((line = fileReader.readLine()) != null) {
				String[] temp = line.split("\\t");
				double txLoss = Double.parseDouble(temp[4]);
				double txDelay = Double.parseDouble(temp[5]);
				double txJitter = Double.parseDouble(temp[6]);

				double rxLoss = Double.parseDouble(temp[8]);
				double rxDelay = Double.parseDouble(temp[9]);
				double rxJitter = Double.parseDouble(temp[10]);

				String output = exec(external_prog, txLoss + "", txDelay + "", txJitter + "", rxLoss + "", rxDelay + "", rxJitter + "");
				// System.out.println(output);
				double throughput = Double.parseDouble(output);
				NS3Data obj = new NS3Data(txLoss, txDelay, txJitter, rxLoss, rxDelay, rxJitter, throughput);
				updateDB(obj);

			}
			fileReader.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String exec(String... params) throws InterruptedException, IOException {
		String cmd_args = "./shell_sim.sh";
		for (String s : params) {
			cmd_args += " ";
			cmd_args += s;
		}
		System.out.println("-- command arguments : " + cmd_args + "\n");

		Process process = Runtime.getRuntime().exec(params);
		// Process process = new ProcessBuilder(
		// cmd_args).start();
		final InputStream is = process.getInputStream();

		new Thread(new Runnable() {
			public void run() {
				try {
					String line;
					BufferedReader reader = new BufferedReader(new InputStreamReader(is));
					while ((line = reader.readLine()) != null) {
						System.out.println("- " + line);
						strOutput = line;
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (Exception e) {
							e.getMessage();
						} finally {
						}
					}
				}
			}
		}).start();
		// the outer thread waits for the process to finish
		process.waitFor();

		System.out.println("[DONE]" + strOutput);
		return strOutput;
	}

	public static void updateDB(NS3Data obj) {
		String query = "INSERT INTO ns3data " + "(TxDelay, TxJitter, TxLoss, RxDelay, RxJitter, RxLoss, Throughput) " + "VALUES(?, ?, ?, ?, ?, ?, ?)";

		try{
			PreparedStatement pstmt = conn.prepareStatement(query);
			pstmt.setDouble(1, obj.getTxDelay());
			pstmt.setDouble(2, obj.getTxJitter());
			pstmt.setDouble(3, obj.getTxLoss());
			pstmt.setDouble(4, obj.getRxDelay());
			pstmt.setDouble(5, obj.getRxJitter());
			pstmt.setDouble(6, obj.getRxLoss());
			pstmt.setDouble(7, obj.getThroughput());

			pstmt.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
