import org.bytedeco.javacv.FFmpegFrameGrabber ;
import org.bytedeco.javacv.FFmpegFrameRecorder ;

import java.io.IOException ;

public class AudioSplitter {
	public static void main(String[] args) {
	Chaîne inputFilePath = "path/to/file/sample.mp3" ;
	String outputDirectory = "path/to/folder/" ;
	int segmentDurationInSeconds = 600 ; // 10 minutes en secondes

	try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFilePath)) {
	grabber.start() ;

	long longDurationInSeconds = (long) grabber.getLengthInTime() / 1000000 ; // Conversion des microsecondes en secondes
	double frameRate = grabber.getFrameRate() ;

	long segmentStartTime = 0 ;
	long segmentEndTime ;
	int segmentNumber = 1 ;

	while (segmentStartTime < totalDurationInSeconds) {
	String outputFilePath = outputDirectory + "segment_" + segmentNumber + ".mp3" ;

	try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFilePath, 0)) {
	recorder.setAudioChannels(2) 
	recorder.setAudioCodecName("libmp3lame") ; // Règle le codec audio sur MP3
	recorder.setAudioBitrate(192000) ; // Ajuste le débit binaire si nécessaire
	recorder.setSampleRate(44100) ; // Ajuste la fréquence d'échantillonnage si nécessaire
	recorder.setFrameRate(frameRate) ;
	recorder.setFormat("mp3") ; // Règle le format de sortie sur MP3
	recorder.start() ;

	segmentEndTime = Math.min(segmentStartTime + segmentDurationInSeconds, totalDurationInSeconds) ;

	grabber.setTimestamp(segmentStartTime * 1000000) ; // Fixe l'horodatage du grabber à l'heure de début en microsecondes

	while (grabber.getTimestamp() / 1000000 < segmentEndTime) {
	    recorder.record(grabber.grabSamples()) 
	  }
	}

	segmentStartTime = segmentEndTime ;
	numéro de segment++ ;
	}
	} catch (IOException e) {
	e.printStackTrace() ;
	}
	}
}
