
package com.wuyigq.system;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class LianyilianTool {
	
	int bg_color = 0x575058;
	int cur_length = 0;//本关需要走的步数
	int cur_line = 0;//本关的行数
	int[][] cur_map;
	static int cnt_slt = 0;//本关解的数量
	
	/**
	 * 读取图片的RGB值
	 * 
	 * @throws Exception
	 */
	public void getImagePixel(String image, int line) throws Exception {
		File file = new File(image);
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int width = bi.getWidth();
		int height = bi.getHeight();
		
        int w = width/line;
        int h = height/line;
        
        int count = 0;
        int[][] colors = new int[line][line];
        int[][] clrMap = new int[line*line/2][5];
        for (int i = 0; i < line; i++){
        	for (int j = 0; j < line; j ++){
        		colors[i][j] = bi.getRGB((int)((j+0.5)*w), (int)((i+0.5)*h)) & 0xffffff;
        		if (colors[i][j] != bg_color){
        			boolean flag = false;
        			for (int k = 0; k <count; k++){
        				if (clrMap[k][0] == colors[i][j]) {
        					if (clrMap[k][3] ==0 && clrMap[k][4]==0) {
	        					flag = true;
	        					clrMap[k][3] = i;
	        					clrMap[k][4] = j;
        					}else
        						System.out.println(String.format("-Data Error--i:%d, j:%d-color:%x", i , j, colors[i][j]));
        					break;
        				}
        			}
        			if (!flag){
        				clrMap[count] = new int[]{colors[i][j], i, j, 0, 0};
        				count++;
        			}
        		}else 
        			colors[i][j] = 0;
        	}
        }
//        System.out.println("---------打印色块数据---------");
//        for (int i = 0; i < line; i++){
//        	for (int j = 0; j < line; j ++){
//        		System.out.print(String.format(" %06x", colors[i][j]));
//        	}
//        	System.out.println();
//        }
        
//        System.out.println("---------打印整合后的色块数据---------");
//        for (int i = 0; i < count; i++){
//        	for (int j = 0; j < 5; j ++){
//        		System.out.print(String.format(" %06x", clrMap[i][j]));
//        	}
//        	System.out.println();
//        }
        cnt_slt = 0;
        findSolution(clrMap, count, line);
	}
 
	public void printResult(int[][] colors){
		System.out.println("----------------------sulotion index:" + cnt_slt);
		for (int i = 0; i < colors.length; i++){
			for (int j = 0; j < colors[i].length; j++)
				System.out.print(String.format("%04x ", colors[i][j]));
			System.out.println();
		}
	}
	
	/**
	 * 判断下一步，是否搜索完成，是否需要换颜色，是否能走通
	 */
	public boolean check(int[][] colors, int step, int x, int y, int clrNo){
		if (colors[x][y] == clrNo){
			if (step >= cur_length) {
				cnt_slt++;
				printResult(colors);
			}
			else if (clrNo<cur_map.length) {
				int[] p = cur_map[clrNo];
				step(colors, step+1, p[1], p[2], clrNo+1, true);
			}
			return false;
		} 
		return colors[x][y] == 0 && cnt_slt <= 0;
	}

	/**
	*递归查找
	*colors元素结构:高8位表示步数，低8位表示颜色编号
	*/
	public void step(int[][] colors, int step, int x, int y, int clrNo, boolean isSkip){
		if (cnt_slt > 0) return;
		colors[x][y] = (step << 8) + clrNo;
		if (x+1<cur_line && check(colors, step+1, x+1, y, clrNo)) step(colors, step+1, x+1, y, clrNo, false);
		if (x>0 && check(colors, step+1, x-1, y, clrNo)) step(colors, step+1, x-1, y, clrNo, false);
		if (y+1<cur_line && check(colors, step+1, x, y+1, clrNo)) step(colors, step+1, x, y+1, clrNo, false);
		if (y>0 && check(colors, step+1, x, y-1, clrNo)) step(colors, step+1, x, y-1, clrNo, false);
		if (isSkip) colors[x][y] = clrNo;
		else colors[x][y] = 0;
	}
	
	public void findSolution(int[][] clrMap, int length, int line){
		if (length <= 0 || length > 0xffff) return;
		
		cur_map = new int[length][];
		cur_line = line;
		cur_length = line*line;
		int[][] colors = new int[line][line];
		for (int i = 0; i < line; i++)
			colors[i] = new int[line];
		int[] p;
		for (int i = 0; i < length; i++)
		{
			p = clrMap[i];
			colors[p[1]][p[2]] = i+1;
			colors[p[3]][p[4]] = i+1;
			cur_map[i] = p;
		}
		
		p = clrMap[0];
		step(colors, 1, p[1], p[2], 1, true);
	}
	
	public static void main(String[] args) throws Exception {
		LianyilianTool rc = new LianyilianTool();
		String dir = "flow\\";
		String[] folders = new String[]{"5高级", "6专家", "7大师"};
		int[] lines = new int[]{8, 9, 10};
		for(int i = 1; i<folders.length; i++){
			for (int j = 1; j <= 50; j++){
				rc.getImagePixel(dir + folders[i] + String.format("\\%02d0.png", j), lines[i]);
				System.out.println("folder:"+folders[i] + ",level=" + j + ",solution count=" + cnt_slt);
			}
		}
	}
}
