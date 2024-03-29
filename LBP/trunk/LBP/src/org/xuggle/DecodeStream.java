//package org.xuggle;
//
//import java.awt.image.BufferedImage;
//import java.io.DataInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.HashMap;
//import java.util.Map;
//
//import com.xuggle.xuggler.Global;
//import com.xuggle.xuggler.IContainer;
//import com.xuggle.xuggler.IContainerFormat;
//import com.xuggle.xuggler.IPacket;
//import com.xuggle.xuggler.IPixelFormat;
//import com.xuggle.xuggler.IStream;
//import com.xuggle.xuggler.IStreamCoder;
//import com.xuggle.xuggler.IVideoPicture;
//import com.xuggle.xuggler.IVideoResampler;
//import com.xuggle.xuggler.Utils;
//import com.xuggle.xuggler.demos.VideoImage;
//
//
//
//public class DecodeStream {
//
//	public static void main(String[] args) throws MalformedURLException,
//			IOException {
//		URL mpegPanas = new URL("http://myIp/rtpOverHttp?Url=nphMpeg4/nil-640x480&Bitrate=" + (256 * 1024));
//
//		String filename = mpegUrl.toString();
//		IContainerFormat format = null;
//		format = IContainerFormat.make();
//		format.setInputFormat("m4v");
//
//		// Let's make sure that we can actually convert video pixel formats.
//		if (!IVideoResampler
//				.isSupported(IVideoResampler.Feature.FEATURE_COLORSPACECONVERSION))
//			throw new RuntimeException("you must install the GPL version"
//					+ " of Xuggler (with IVideoResampler support) for "
//					+ "this demo to work");
//
//		// Create a Xuggler container object
//		IContainer container = IContainer.make();
//		container.setProperty("probesize", "40");
//
//		InputStream in = HttpUtil.openConnection(mpegPanas, "user", "pass");
//
//		if (container.open(new DataInputStream(in), format, true, false) < 0)
//			throw new IllegalArgumentException("could not open file: "
//					+ filename);
//
//		IVideoResampler resampler = null;
//
//		/*
//		 * And once we have that, we draw a window on screen
//		 */
//		openJavaWindow();
//		Map<Integer, IStreamCoder> knownStreams = new HashMap<Integer, IStreamCoder>();
//
//		/*
//		 * Now, we start walking through the container looking at each packet.
//		 */
//		IPacket packet = IPacket.make();
//
//		long firstTimestampInStream = Global.NO_PTS;
//		long systemClockStartTime = 0;
//
//		while (container.readNextPacket(packet) >= 0) {
//			/*
//			 * Now we have a packet, let's see if it belongs to our video stream
//			 */
//			if (packet.isComplete()) {
//				if (knownStreams.get(packet.getStreamIndex()) == null) {
//					container.queryStreamMetaData();
//					IStream stream = container.getStream(packet
//							.getStreamIndex());
//					if (stream.getStreamCoder().open() < 0)
//						throw new RuntimeException(
//								"could not open video decoder for container: "
//										+ filename);
//
//					knownStreams.put(packet.getStreamIndex(),
//							stream.getStreamCoder());
//				}
//
//				IStreamCoder videoCoder = knownStreams.get(packet
//						.getStreamIndex());
//
//				/*
//				 * We allocate a new picture to get the data out of Xuggler
//				 */
//				IVideoPicture picture = IVideoPicture.make(
//						videoCoder.getPixelType(), videoCoder.getWidth(),
//						videoCoder.getHeight());
//
//				int offset = 0;
//				while (offset < packet.getSize()) {
//					/*
//					 * Now, we decode the video, checking for any errors.
//					 */
//					int bytesDecoded = videoCoder.decodeVideo(picture, packet,
//							offset);
//					if (bytesDecoded > 0)
//						// throw new
//						// RuntimeException("got error decoding video in: " +
//						// filename);
//						offset += bytesDecoded;
//					else {
//						System.out.println("Ignore "
//								+ (packet.getSize() - offset) + " bytes");
//						offset = packet.getSize();
//						continue;
//					}
//
//					/*
//					 * Some decoders will consume data in a packet, but will not
//					 * be able to construct a full video picture yet. Therefore
//					 * you should always check if you got a complete picture
//					 * from the decoder
//					 */
//					if (picture.isComplete()) {
//						IVideoPicture newPic = picture;
//						if (resampler == null
//								&& videoCoder.getPixelType() != IPixelFormat.Type.BGR24) {
//							resampler = IVideoResampler.make(
//									videoCoder.getWidth(),
//									videoCoder.getHeight(),
//									IPixelFormat.Type.BGR24,
//									videoCoder.getWidth(),
//									videoCoder.getHeight(),
//									videoCoder.getPixelType());
//							if (resampler == null)
//								throw new RuntimeException(
//										"could not create color space "
//												+ "resampler for: " + filename);
//						}
//
//						if (resampler != null) {
//							// we must resample
//							newPic = IVideoPicture.make(
//									resampler.getOutputPixelFormat(),
//									picture.getWidth(), picture.getHeight());
//							if (resampler.resample(newPic, picture) < 0)
//								throw new RuntimeException(
//										"could not resample video from: "
//												+ filename);
//						}
//						if (newPic.getPixelType() != IPixelFormat.Type.BGR24)
//							throw new RuntimeException("could not decode video"
//									+ " as BGR 24 bit data in: " + filename);
//
//						if (firstTimestampInStream == Global.NO_PTS) {
//							// This is our first time through
//							firstTimestampInStream = picture.getTimeStamp();
//							// get the starting clock time so we can hold up
//							// frames
//							// until the right time.
//							systemClockStartTime = System.currentTimeMillis();
//						} else {
//							long systemClockCurrentTime = System
//									.currentTimeMillis();
//							long millisecondsClockTimeSinceStartofVideo = systemClockCurrentTime
//									- systemClockStartTime;
//							// compute how long for this frame since the first
//							// frame in the
//							// stream.
//							// remember that IVideoPicture and IAudioSamples
//							// timestamps are
//							// always in MICROSECONDS,
//							// so we divide by 1000 to get milliseconds.
//							long millisecondsStreamTimeSinceStartOfVideo = (picture
//									.getTimeStamp() - firstTimestampInStream) / 1000;
//							final long millisecondsTolerance = 50; // and we
//																	// give
//																	// ourselfs
//																	// 50 ms of
//																	// tolerance
//							final long millisecondsToSleep = (millisecondsStreamTimeSinceStartOfVideo - (millisecondsClockTimeSinceStartofVideo + millisecondsTolerance));
//							if (millisecondsToSleep > 0) {
//								try {
//									Thread.sleep(millisecondsToSleep);
//								} catch (InterruptedException e) {
//									// we might get this when the user closes
//									// the dialog box, so
//									// just return from the method.
//									return;
//								}
//							}
//						}
//
//						// And finally, convert the BGR24 to an Java buffered
//						// image
//						BufferedImage javaImage = Utils
//								.videoPictureToImage(newPic);
//
//						// and display it on the Java Swing window
//						updateJavaWindow(javaImage);
//					}
//				}
//			} else {
//				/*
//				 * This packet isn't part of our video stream, so we just
//				 * silently drop it.
//				 */
//				do {
//				} while (false);
//			}
//
//		}
//		/*
//		 * Technically since we're exiting anyway, these will be cleaned up by
//		 * the garbage collector... but because we're nice people and want to be
//		 * invited places for Christmas, we're going to show how to clean up.
//		 */
//		if (container != null) {
//			container.close();
//			container = null;
//		}
//		closeJavaWindow();
//
//	}
//
//	private static VideoImage mScreen = null;
//
//	private static void updateJavaWindow(BufferedImage javaImage) {
//		mScreen.setImage(javaImage);
//	}
//
//	/**
//	 * Opens a Swing window on screen.
//	 */
//	private static void openJavaWindow() {
//		mScreen = new VideoImage();
//	}
//
//	/**
//	 * Forces the swing thread to terminate; I'm sure there is a right way to do
//	 * this in swing, but this works too.
//	 */
//	private static void closeJavaWindow() {
//		System.exit(0);
//	}
//}