package watermarking;

import org.jcodec.api.specific.AVCMP4Adaptor;
import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.codecs.h264.H264Utils;
import org.jcodec.common.AutoFileChannelWrapper;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Size;
import org.jcodec.containers.mp4.MP4Packet;
import org.jcodec.containers.mp4.TrackType;
import org.jcodec.containers.mp4.demuxer.AbstractMP4DemuxerTrack;
import org.jcodec.containers.mp4.demuxer.FramesMP4DemuxerTrack;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.jcodec.containers.mp4.muxer.FramesMP4MuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import utils.ConverterUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nikita Konovalov
 */

public class ConverterThread extends Thread {
    private final Core core;
    private final String inputPath;
    private final String outputPath;

    public ConverterThread(Core core, String inputPath, String outputPath) {
        this.core = core;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
    }

    @Override
    public void run() {
        File inputFile = new File(inputPath);
        File outputFile = new File(outputPath);

        try {
            MP4Demuxer demuxer = new MP4Demuxer(new AutoFileChannelWrapper(inputFile));
            MP4Muxer muxer = new MP4Muxer(NIOUtils.rwFileChannel(outputFile));


            List<AbstractMP4DemuxerTrack> audioTracks = demuxer.getAudioTracks();
            for (AbstractMP4DemuxerTrack audioTrack : audioTracks) {
                FramesMP4MuxerTrack muxerTrack = muxer.addTrack(TrackType.SOUND, (int) audioTrack.getTimescale());
                muxerTrack.addSampleEntry(audioTrack.getSampleEntries()[0]);
                for (int i = 0; i < audioTrack.getFrameCount(); i++) {
                    muxerTrack.addFrame((MP4Packet) audioTrack.nextFrame());
                }
            }

            FramesMP4DemuxerTrack videoTrack = (FramesMP4DemuxerTrack) demuxer.getVideoTrack();
            FramesMP4MuxerTrack muxerTrack = muxer.addTrack(TrackType.VIDEO, (int) videoTrack.getTimescale());

            AVCMP4Adaptor decoder = new AVCMP4Adaptor(videoTrack);
            H264Encoder encoder = new H264Encoder();

            Size size = videoTrack.getMeta().getDimensions();
            long frameCount = videoTrack.getFrameCount();
            List<ByteBuffer> spsList = new ArrayList<ByteBuffer>();
            List<ByteBuffer> ppsList = new ArrayList<ByteBuffer>();

            ByteBuffer outBuffer = ByteBuffer.allocate(size.getWidth() * size.getHeight());
            for (int i = 0; i < frameCount; i++) {
                updateProgress(i, frameCount);

                if (Thread.currentThread().isInterrupted()){
                    return;
                }

                MP4Packet packet = videoTrack.nextFrame();
                Picture picture;
                try {
                    picture = decoder.decodeFrame(packet, decoder.allocatePicture());
                } catch (Exception ex) {
                    continue;
                }
                BufferedImage srcImage = ConverterUtils.toBufferedImage(picture);
                core.getBaseImageProvider().reset();
                core.getBaseImageProvider().setSource(srcImage);

                core.processImage();

                BufferedImage precessedImage = core.getCombinedImage();
                Picture modifiedPicture = ConverterUtils.fromBufferedImage(precessedImage, ColorSpace.YUV420J);

                outBuffer.clear();
                outBuffer = encoder.encodeFrame(modifiedPicture, outBuffer);

                spsList.clear();
                ppsList.clear();
                H264Utils.wipePS(outBuffer, spsList, ppsList);
                H264Utils.encodeMOVPacket(outBuffer);

                MP4Packet modifiedPacket = new MP4Packet(packet, outBuffer);
                muxerTrack.addFrame(modifiedPacket);
            }

            muxerTrack.addSampleEntry(H264Utils.createMOVSampleEntry(spsList, ppsList));

            muxer.writeHeader();
        } catch (Exception e) {
            if (e instanceof ClosedByInterruptException) {
                core.setConvertThreadException(new Exception("Interrupted"));
                return;
            }
            core.setConvertThreadException(e);
        } finally {
          releaseButton();
        }
    }

    private void updateProgress(long current, long total) {
        core.setProgress((double)current / (double)total);
    }

    private void releaseButton() {
        core.enableRenderButton();
    }
}
