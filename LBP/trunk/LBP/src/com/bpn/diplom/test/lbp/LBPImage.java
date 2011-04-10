package com.bpn.diplom.test.lbp;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

public class LBPImage {
	
	public static final Point [] AROUND_8_POINTS = new  java.awt.Point[]{
		new  java.awt.Point(-1, 1),
		new  java.awt.Point( 0, 1),
		new  java.awt.Point( 1, 1),
		new  java.awt.Point( 1, 0),
		new  java.awt.Point( 1,-1),
		new  java.awt.Point( 0,-1),
		new  java.awt.Point(-1,-1),
		new  java.awt.Point(-1, 0)
	};
	
	private File fileImg;
	private Raster raster;
	/** width of raster	 */
	private int widthRaster;
	/** heigth of raster	 */
	private int heigthRaster;
	

	/** set of circle point for calculate LBP code	 */
	private Point [] aroundPoints;
	/** if circle point is ellipse then  radiusX != radiusY*/
	private int radiusX;
	private int radiusY;

	
	/** ����� �������� UniformPattern - ���	����� ��� ��� � ������ ������� (�� 0 � 1 ��� �� 1 � 0) ����������� �� ����� ���� ���.
	 *  ��������: 0000, 00111, 10001, 0001100
	 */
	private Set<Integer> setUniformPattern;
	/** ��������� �� 2^(���������� ��� ��� ����)	 */
	private int keyOfNotUniformCode;
	
	/** for each pixel are calculated color and LBP code	 */
	private CPoint [][] imageMatrix;
	/** width of image matrix	 */
	private int width;
	/** heigth of image matrix	 */
	private int heigth;
	
	
	/** ���������� ������ �� ������� ����� ������� �����������, ��������� �� ��������� � �����������	 */
	private int countBlock;
	/** width of block image  */
	private int widthBlock;
	/** heigth of block image */
	private int heigthBlock;
//	private Block[][] blocks;
	private Block[] blocks;

	/** �������� ����� ������ ���� ����������� ����� */
	private int[] faceVector;
//	private int faceVectorIndex;
	
	long timeStart;
	long timeStartCalculate;

	
//	public static void main(String [] args){
//	}
	
	public static void main(String [] args){
		System.out.println("HELLO\n\n");
		
		
		LBPImage img1 = new LBPImage("img/whiteSquare.bmp", LBPImage.AROUND_8_POINTS, 7);
		LBPImage img2 = new LBPImage("img/whiteSquare.bmp", LBPImage.AROUND_8_POINTS, 7);
		
		System.out.println(MatcherPirsonX2.getDistanceX2(img1, img2));
		
	}
	
	
	public LBPImage(String pathImg, Point [] around, int countBlock){
		this.timeStart = System.currentTimeMillis();
		System.out.println("������ � ������: "+pathImg+" ���������� �����: "+around.length);
		this.fileImg = new File(pathImg);
		try {
			if(!fileImg.exists())
				fileImg.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.raster = this.getImageByLink(fileImg).getData();
		this.widthRaster = raster.getWidth();
		this.heigthRaster = raster.getHeight();
		System.out.println("������ ������: "+widthRaster+"*"+heigthRaster+". ���������� �������� ������: " +raster.getNumBands());
				
		this.timeStartCalculate = System.currentTimeMillis();
		
		this.aroundPoints = around;
		this.radiusX = 0;
		this.radiusY = 0;
		for(Point p : aroundPoints){
			if(p.x > radiusX)
				radiusX = p.x;
			if(p.y > radiusY)
				radiusY = p.y;
		}
		System.out.println("������� ���������� ����� ��� ���� ���. ������ �: " + radiusX + " ������ Y: " + radiusY);
		
		this.setUniformPattern = this.getUniformPatternSet(aroundPoints.length);
		this.keyOfNotUniformCode = (int) (Math.pow(2, aroundPoints.length));
		System.out.println("��������� ��������� �������� Uniform Pattern. ���-�� ��� � ��� ����: "+aroundPoints.length+". "+aroundPoints.length+"*("+aroundPoints.length+"-1)+2="+setUniformPattern.size());
		
		this.imageMatrix = this.buildImageMatrix(raster);
		width = imageMatrix[0].length;
		heigth = imageMatrix.length;
		System.out.println("������� ����������� ���������: "+width+"*"+heigth);
//		this.printImageMatrix(raster);
//		this.printImageMatrix(imageMatrix);
		
		this.countBlock = countBlock;
		this.widthBlock = width / countBlock;
		this.heigthBlock = heigth / countBlock;
		int fromX = (width % countBlock) / 2;
		int toX = width - (width - widthBlock*countBlock - fromX);
		int fromY = (heigth % countBlock) / 2;
		int toY = heigth - (heigth - heigthBlock*countBlock - fromY);
		blocks = new Block[countBlock * countBlock];
		int index = 0;
		for(int y = 0; y < countBlock; y++){
			for(int x = 0; x < countBlock; x++){
				Point from = new Point(fromX + (x*widthBlock), fromY +  + (y*heigthBlock));
				Point to = new Point(from.x + widthBlock, from.y + heigthBlock);
				Block block = new Block(x, y, from, to);
				fillBlockGistogram(block);
				blocks[index] = block;
				index++;
//				System.out.println(from+"-"+to+"\t");
			}
//			System.out.println();
		}	
		System.out.println("��������� ���� �� " + countBlock+"*"+ countBlock + " ������. ������ �����: " + widthBlock+"*"+heigthBlock);
		System.out.println("���������������� ������: x:"+fromX+"-"+toX+" y:"+fromY+"-"+toY);
//		this.printImageBlocks(blocks);
		
		this.faceVector = buildFaceVector(blocks);
		
		long time = System.currentTimeMillis();
		System.out.println("����� �������: "+(time-timeStart)+"�c. ����� ��������: " + (time-timeStartCalculate)+"��");
	}
	
	private int[] buildFaceVector(Block[] blocks){
		int[] vector = new int[countBlock * countBlock * this.setUniformPattern.size()];
		int i = 0;
		for(Block block : blocks){
			for(int val : block.getGistogram().values()){
				vector[i] = val; 
			}
		}
		return vector;
	}
	
	private void fillBlockGistogram(Block block){
		// ������� ����������  � ����� �� �������� ������
		TreeMap<Integer, Integer> gistogram = new TreeMap<Integer, Integer>();
		gistogram.put(this.keyOfNotUniformCode, 1);
		for(Integer codeUP : this.setUniformPattern){
			gistogram.put(codeUP, 1);
		}
		block.setGistogram(gistogram);
		int fromX = block.getFromPoint().x;
		int toX = block.getToPoint().x;
		int fromY = block.getFromPoint().y;
		int toY = block.getToPoint().y;
		for(int y = fromY; y < toY; y++){
			for(int x = fromX; x < toX; x++){
				int code = imageMatrix[y][x].getCodeLBP();
				Integer oldCount = gistogram.get(code);
				if(this.setUniformPattern.contains(code))
					gistogram.put(code, oldCount+1);
				else
					gistogram.put(this.keyOfNotUniformCode, oldCount+1);
			}
		}
	}
	
	/** �� ������ ������ ������� �����������, ������ ����� ����� ����������, ���� � ��� ���.
	 * ������� �������� �� ����� ������������ � �������
	 * @param r �������� ������ ��� ���������� �������� 
	 * @return
	 */
	private CPoint[][] buildImageMatrix(Raster r){
		CPoint [][] matrix = new CPoint[heigthRaster - 2*radiusY][widthRaster - 2*radiusX];
		
		for(int y = radiusY; y < (heigthRaster-radiusY); y++){
			for(int x = radiusX; x < (widthRaster-radiusX); x++){
				int color = r.getSample(x, y, 0);
				CPoint p = new CPoint(x, y, color);
				int codeLBP = getLBPCode(r, p);
				p.setCodeLBP(codeLBP);
				matrix[p.y - radiusY][p.x - radiusX] = p;
			}
		}
		
		return matrix;
	}
	
	
	/** ���������� ��� ��� ��� ����� center �� raster, ��������� ���������� aroundPoints
	 * @param raster
	 * @param center
	 * @param aroundPoints
	 * @return ��� ��� �������� ������� ���������� ��� ������������� ����� ���� ������
	 */
	private int getLBPCode(Raster raster, CPoint center){
		if(raster==null || center==null || aroundPoints==null)
			return -1;
		if(aroundPoints.length > 32) // 32 ���� � int
			return -3;
		int code = 0;
		for(java.awt.Point p : aroundPoints){
			if(p==null)
				return -2;
			int x = center.x + p.x;
			int y = center.y + p.y;
//			System.out.println(x+" "+y);
			int color = raster.getSample(x, y, 0); 
			code = code << 1;
			if(color >= center.color)
				code = code + 1;
		}
		return code;
	}
	
	
	
	/**
	 * ������ ����� ������������ uniform pattern ��� ���������� ����� countBit.
	 * @param countBit
	 * @return
	 */
	private Set<Integer> getUniformPatternSet(int countBit){
		Set<String> set = new TreeSet<String>();
		
		
		String binNumSt0 = "";
		String binNumSt1 = "";
		for(int i = 0; i < countBit; i++){
			binNumSt0 = binNumSt0.concat("0");
			binNumSt1 = binNumSt1.concat("1");
		}
		set.add(binNumSt0);
		set.add(binNumSt1);
		
		String stNum0 = binNumSt0;
		String stNum1 = binNumSt1;
		int stNumLength = stNum0.length(); 
		String inc1 = "1";
		String inc0 = "0";
		for(int i = 0; i < countBit - 2; i++){
			int incLength = inc1.length();
			stNum0 = binNumSt0;
			stNum0 = inc1.concat( stNum0.substring(0, stNumLength-incLength) );
			
			stNum1 = binNumSt1;
			stNum1 = inc0.concat( stNum1.substring(0, stNumLength-incLength) );
			if(Util.isLDPCode(stNum0) && Util.isLDPCode(stNum1)){
				set.add(stNum0);
				set.add(stNum1);
			}else
				return null;
			for(int j = 0; j < (stNumLength - incLength ); j++){
				stNum0 = stNum0.substring(stNumLength-1, stNumLength).concat( stNum0.substring(0, stNumLength-1 ) );
				stNum1 = stNum1.substring(stNumLength-1, stNumLength).concat( stNum1.substring(0, stNumLength-1 ) );
				if(Util.isLDPCode(stNum0) && Util.isLDPCode(stNum1)){
					set.add(stNum0);
					set.add(stNum1);
				}else
					return null;
			}
			inc1 = inc1.concat("1");
			inc0 = inc0.concat("0");			
		}
		
		// ��������, ���������� �������� uniform pattern ������ ���� �(�-1)+2, ��� � ��� ���������� �����
		if(set.size() != (countBit*(countBit-1)+2))
			return null;
		
		Set<Integer> intSet = new java.util.HashSet<Integer>();
		for(String pattern : set){
			Integer intPattern = Util.binToInt(pattern);
			if(intPattern >= 0){
				if(!intSet.add(intPattern)){
					System.out.println("ERROR: ������ ������� � getUniformPatternSet");
					return null;
				}
			} else{
				System.out.println("ERROR: ������������ ������ � getUniformPatternSet");
				return null;
			}
		}
		
		if(intSet.size() == (countBit*(countBit-1)+2))
			return intSet;
		else 
			return null;
	}
	
	
	
	
	/** ������ �������(���� �������) �����������
	 * @param r - �����, ������������ ��� ������� �����������
	 */
	private void printImageMatrix(Raster r){
		System.out.println("������ �������(���� �������) �����������:");
		for(int y = 0; y < heigthRaster; y++){
			for(int x = 0; x < widthRaster; x++){
				System.out.print(r.getSample(x, y, 0)+"\t");
			}
			System.out.println();
		}
	}
	
	/** ������ ������� ����� ��� �����������
	 * @param r - �����, ������������ ��� ������� �����������
	 */
	private void printImageMatrix(CPoint [][] matrix){
		System.out.println("������ ������� ����� ��� �����������:");
		for(int y = 0; y < heigthRaster-2*radiusY; y++){
			for(int x = 0; x < widthRaster-2*radiusX; x++){
				System.out.print(matrix[y][x].getCodeLBP()+"\t");
			}
			System.out.println();
		}
	}
	
	/** ������ ���� ������
	 * @param r - �����, ������������ ��� ������� �����������
	 */
	private void printImageBlocks(Block[] blocks){
		System.out.println("������ ������ �����������:");
		for(Block block : blocks){
				System.out.println(block+"\t");
		}
	}
	
	
	
	public BufferedImage getImageByLink(File file) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(file);
		} catch (IOException e) { 
			e.printStackTrace();
		}
		return img;
	}


	public int[] getFaceVector() {
		return faceVector;
	}


	public void setFaceVector(int[] faceVector) {
		this.faceVector = faceVector;
	}
	
	
	
//	protected String saveImageOnHardDrive(BufferedImage img, String pathDirectory) {
//        Date currentDate = new Date();
//        String pathOnTheCurrentFile = pathDirectory + currentDate.getTime() + "_fractal.jpg";
//
//        try {
//            File outputFile = new File(pathOnTheCurrentFile);
//            ImageIO.write(img, "bmp", outputFile);
//        } catch (IOException e) {
//            Logger.getLogger("log.txt").log(Level.INFO, "Save image", e);
//            return null;
//        }
//        return pathOnTheCurrentFile;
//    }
//	
	
	
}
