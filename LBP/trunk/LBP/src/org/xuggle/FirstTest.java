package org.xuggle;


import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import com.xuggle.mediatool.demos.DisplayWebcamVideo;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.Utils;
	 

public class FirstTest {	 
		public static void main(String a[]) throws Exception {
			
//			String defult = System.getProperty("java.library.path");
//			String xuggle = "C:\\Program Files\\Xuggle;";
//			System.out.println(defult);
//			
//			System.setProperty("java.library.path", defult+xuggle);
			
			String filename = "C:/temp/1.avi";
			File outdir = new File("c:/temp/pictures");
			IContainer container = IContainer.make();
	 
			if (container.open(filename, IContainer.Type.READ, null) < 0)
				throw new IllegalArgumentException("could not open file: " + filename);
			int numStreams = container.getNumStreams();
			int videoStreamId = -1;
			IStreamCoder videoCoder = null;
	 
			// ����� ����� ����� �����
			for (int i = 0; i < numStreams; i++) {
				IStream stream = container.getStream(i);
				IStreamCoder coder = stream.getStreamCoder();
				if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
					videoStreamId = i;
					videoCoder = coder;
					break;
				}
			}
			if (videoStreamId == -1)
				// ������ �� �����
				throw new RuntimeException("could not find video stream in container: " + filename);
	 
			// �������� ������� �����
			if (videoCoder.open() < 0)
				throw new RuntimeException("could not open video decoder for container: " + filename);
	 
			IPacket packet = IPacket.make();
			// � 3-�� �� 5-�� ������������
			long start = 6 * 1000 * 1000;
			long end = 12 * 1000 * 1000;
			// � �������� � 100 ����������
			long step = 500 * 1000;
	 
			END: while (container.readNextPacket(packet) >= 0) {
				if (packet.getStreamIndex() == videoStreamId) {
					IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(), videoCoder.getWidth(), videoCoder.getHeight());
					int offset = 0;
					while (offset < packet.getSize()) {
						int bytesDecoded = videoCoder.decodeVideo(picture, packet, offset);
						// ���� ���-�� ����� �� ���
						if (bytesDecoded < 0)
							throw new RuntimeException("got error decoding video in: " + filename);
						offset += bytesDecoded;
						// � ����� ������, ����� ����� ������������ Resampler. ��.
						// tutorials!
						if (picture.isComplete()) {
							IVideoPicture newPic = picture;
							// � �������������
							long timestamp = picture.getTimeStamp();
							if (timestamp > start) {
								// �������� ����������� BufferedImage
								BufferedImage javaImage = Utils.videoPictureToImage(newPic);
								String fileName = String.format("%07d.png", timestamp);
								ImageIO.write(javaImage, "PNG", new File(outdir, fileName));
								start += step;
							}
							if (timestamp > end) {
								break END;
							}
						}
					}
				}
			}
			if (videoCoder != null) {
				videoCoder.close();
				videoCoder = null;
			}
			if (container != null) {
				container.close();
				container = null;
			}
	 
		}
	}
