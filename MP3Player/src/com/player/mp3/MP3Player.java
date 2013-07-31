package com.player.mp3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

/**
 * An asynchronous MPEG player. Utilizes the <code>Player</code> class from the
 * JLayer 1.0.1 library. The file "jl1.0.1.jar" must be included in the build
 * path to utilize this class.
 * 
 * Everything is GNU GPL v3.
 * 
 * @author Stephen Pirozzi
 * @version 1.0
 */
public final class MP3Player
{
	// *************************************************************************
	// Private variables
	// *************************************************************************
	/**
	 * Used when throwing runtime exceptions to client code.
	 */
	private static final String myClass = "MP3Player.java: ";
	
	/**
	 * A queue of all MPEG files. All files that are removed
	 * from head are added back to the tail so that <code>loopAll</code> will
	 * function correctly.
	 */
	private Queue<File> playbackQueue;
	
	/**
	 * An instance of the Player class from the JLayer lib. Synchronously
	 * plays MPEG files. Used within <code>PlayerRunnerThread</code>.
	 */
	private Player player;
	
	/**
	 * Thread that performs asynchronous playback of MPEG files.
	 */
	private PlayerRunnerThread playerThread;
	
	/**
	 * State variable that denotes whether an MPEG file is currently being
	 * played
	 */
	private volatile boolean isPlaying = false;
	
	/**
	 * State variable that denotes whether playback will continue after all MPEG
	 * files in the playback queue have been played once. Implies
	 * <code>isPlaying</code> is true.
	 */
	private volatile boolean isLooping = false;
	
	// *************************************************************************
	// Constructors
	// *************************************************************************
	/**
	 * Constructs a new <code>MP3Player</code> instance and queues the given
	 * file for playback.
	 * 
	 * @param f
	 *            The MPEG file to be queued for playback
	 */
	public MP3Player(File f)
	{
		this(new File[] {f});
	}
	
	/**
	 * Constructs a new <code>MP3Player</code> instance and queues the
	 * files in the array for playback.
	 * 
	 * @param arr
	 *            An array of MPEG files to be queued for playback
	 */
	public MP3Player(File[] arr)
	{
		playbackQueue = new LinkedList<File>();
		for (int i = 0; i < arr.length; i++)
		{
			if (arr[i] == null || arr[i].getPath().equals(""))
			{
				throw new NullPointerException("Null file or filename.");
			}
			playbackQueue.add(arr[i]);
		}
	}
	
	// *************************************************************************
	// Public methods
	// *************************************************************************
	/**
	 * Immediately stops playing the current audio file (if one is playing) and
	 * begins to play the next file in the queue. Returns immediately if
	 * playback is still in progress.
	 */
	@SuppressWarnings("resource")
	public void playNext()
	{
		if (isPlaying)
		{
			return;
		}
		// Remove the file from the queue
		File f = playbackQueue.remove();
		// Stream closed later by playerThread
		FileInputStream stream = null;
		try
		{
			stream = new FileInputStream(f);
		}
		catch (FileNotFoundException e)
		{
			wrapAndRethrow(e);
		}
		// Add the file back to the queue
		playbackQueue.add(f);
		// Instantiate the async player thread
		playerThread = new PlayerRunnerThread(stream);
		// Start playback
		playerThread.start();
	}
	
	/**
	 * Plays the queued MPEG file(s) one time each. Returns immediately if
	 * playback is still in progress.
	 * 
	 * @throws RuntimeException
	 *             if any MPEG file in the playback queue is invalid
	 */
	public void playAllOnce()
	{
		if (isPlaying)
		{
			return;
		}
		for (int i = 0; i < playbackQueue.size(); i++)
		{
			playNext();
			while (!player.isComplete())
			{
				// Delay loop iteration
				// TODO: make this not block (move to PRT)
			}
		}
	}
	
	/**
	 * Continues to play the MPEG file(s) until the <code>stop</code> method is
	 * called.
	 * 
	 * @throws FileNotFoundException
	 *             if any MPEG file in the playback queue is invalid
	 */
	public void loopAll() throws FileNotFoundException
	{
		isLooping = true;
		while (isLooping)
		{
			playAllOnce();
		}
	}
	
	/**
	 * Immediately stops playback of the MPEG file currently playing (if
	 * playing) and stops looping playback (if looping).
	 */
	public void stop()
	{
		isPlaying = false;
		isLooping = false;
		player.close();
	}
	
	public boolean isPlaying()
	{
		return isPlaying;
	}
	
	public boolean isLooping()
	{
		return isLooping;
	}
	
	/**
	 * Not supported because the underlying <code>Player</code> only has a
	 * <code>play</code> method and a <code>close</code> method.
	 * 
	 * @see javazoom.jl.player.Player
	 * @throws UnsupportedOperationException
	 *             if called at runtime
	 */
	@Deprecated
	public void pause()
	{
		throw new UnsupportedOperationException(myClass
				+ "Pause method not implemented");
	}
	
	/**
	 * Not supported because the underlying <code>Player</code> lacks a
	 * <code>pause</code> method.
	 * 
	 * @see javazoom.jl.player.Player
	 * @throws UnsupportedOperationException
	 *             if called at runtime
	 */
	@Deprecated
	public void resume()
	{
		throw new UnsupportedOperationException(myClass
				+ "Resume method not implemented");
	}
	
	// *************************************************************************
	// Private helper method
	// *************************************************************************
	/**
	 * Wraps more specific exceptions into a custom, descriptive
	 * <code>RuntimeException</code> and throws it. This way, client code is not
	 * forced to catch thrown exceptions. Client code can explicitly choose to
	 * catch RuntimeExceptions instead if error-handling behavior is desired.
	 * 
	 * @param e
	 *            An exception of any kind
	 */
	private static void wrapAndRethrow(Exception e) throws RuntimeException
	{
		String msg = myClass + e.getClass().toString() + ":" + e.getMessage();
		RuntimeException r = new RuntimeException(msg);
		r.setStackTrace(e.getStackTrace());
		throw r;
	}
	
	// *************************************************************************
	// Private inner class
	// *************************************************************************
	/**
	 * Thread class responsible for the playback of MPEG audio files in the
	 * playback queue.
	 * 
	 * @see java.lang.Thread
	 */
	private class PlayerRunnerThread extends Thread
	{
		/**
		 * An MPEG file's stream. Used to instantiate the <code>Player</code>.
		 */
		private final FileInputStream stream;
		
		/**
		 * Constructs a new <code>PlayerRunnerThread</code> instance that
		 * 
		 * @throw RuntimeException if the file stream cannot be played
		 */
		PlayerRunnerThread(FileInputStream fis)
		{
			stream = fis;
			try
			{
				player = new Player(stream);
			}
			catch (JavaLayerException e)
			{
				wrapAndRethrow(e);
			}
		}
		
		/**
		 * Plays the given audio file and closes the stream when playback is
		 * complete.
		 * 
		 * @throws RuntimeException
		 *             if the MPEG file cannot be played
		 */
		@Override
		public void run()
		{
			try
			{
				player.play();
				// Blocks this thread until playback is complete
				stream.close();
			}
			catch (JavaLayerException e)
			{
				wrapAndRethrow(e);
			}
			catch (IOException e)
			{
				wrapAndRethrow(e);
			}
		}
	}
}