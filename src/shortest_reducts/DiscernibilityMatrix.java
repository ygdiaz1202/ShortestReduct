package shortest_reducts;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DiscernibilityMatrix {
	
	public static boolean[][] loadFromFile(String filename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		int rows = Integer.parseInt(br.readLine().trim());
		int cols = Integer.parseInt(br.readLine().trim());
		boolean[][] bm = new boolean[rows][cols];
		String line = br.readLine();
		int r = 0;
		while(line != null) {
			String[] columns = line.trim().split("\\s+");
			for(int c = 0; c < columns.length; c ++) {
				bm[r][c] = columns[c].trim().charAt(0) == '1';
			}
			line = br.readLine();
			r ++;
		}
		br.close();
		return bm;
	}
	
	public static void saveToFile(boolean [][] BM, String filename) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
		bw.append("" + BM.length);
		bw.newLine();
		bw.append("" + BM[0].length);
		bw.newLine();
		for(int r = 0; r < BM.length; r ++) {
			for(int c = 0; c < BM[r].length; c ++) {
				bw.append(BM[r][c]? '1' : '0');
				if(c < BM[r].length - 1) bw.append(' ');
			}
			bw.newLine();
		}
		bw.close();
	}
		
}
