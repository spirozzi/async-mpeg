package com.player.mp3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

/**
 * An asynchronous MPEG player. Utilizes the <code>Player</code> class from the
 * JLayer 1.0.1 library. The file "jl1.0.1.jar" must be included in the build
 * path to utilize this class.
 * 
 * Everything is GNU LGPL v3.
 * 
 * @author Stephen Pirozzi
 * @version 1.0
 */
public final class MP3Player
{
	// Private variables
	
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
	 * played.
	 */
	private volatile boolean isPlaying = false;
	
	/**
	 * State variable that denotes whether playback will continue after all MPEG
	 * files in the playback queue have been played once. Implies
	 * <code>isPlaying</code> is true.
	 */
	private volatile boolean isLooping = false;
	
	// Constructors
	
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
			if (arr[i] == null || arr[i].getPath() == null)
			{
				throw new NullPointerException(
						"MP3Player.java: Null file or filename.");
			}
			playbackQueue.add(arr[i]);
		}
	}
	
	/**
	 * Constructs a new <code>MP3Player</code> instance and queues the
	 * files in the list for playback.
	 * 
	 * @param list
	 *            A list of MPEG files to be queued for playback
	 */
	public MP3Player(List<File> list)
	{
		playbackQueue = new LinkedList<File>();
		for (int i = 0; i < list.size(); i++)
		{
			if (list.get(i) == null || list.get(i).getPath() == null)
			{
				throw new NullPointerException(
						"MP3Player.java: Null file or filename.");
			}
			playbackQueue.add(list.get(i));
		}
	}
	
	// Public methods
	
	/**
	 * Plays the next file in the playback queue. Returns immediately if
	 * playback is still in progress.
	 * 
	 * @throws FileNotFoundException
	 *             if the file to be played is invalid
	 * @throws JavaLayerException
	 *             if the audio cannot be played
	 */
	public void playNext() throws FileNotFoundException, JavaLayerException
	{
		if (isPlaying)
		{
			return;
		}
		playerThread = new PlayerRunnerThread(Playback.PLAY_NEXT);
		playerThread.start();
	}
	
	/**
	 * Plays the queued MPEG file(s) one time each. Returns immediately if
	 * playback is still in progress.
	 * 
	 * @throws FileNotFoundException
	 *             if any file considered for playback is invalid
	 * @throws JavaLayerException
	 *             if the audio cannot be played
	 */
	public void playAllOnce() throws FileNotFoundException, JavaLayerException
	{
		if (isPlaying)
		{
			return;
		}
		playerThread = new PlayerRunnerThread(Playback.PLAY_ALL_ONCE);
		playerThread.start();
	}
	
	/**
	 * Continues to play the MPEG file(s) until the <code>stop</code> method is
	 * called. Returns immediately if playback is still in progress.
	 * 
	 * @throws FileNotFoundException
	 *             if any MPEG file in the playback queue is invalid
	 */
	public void loopAll() throws FileNotFoundException
	{
		if (isPlaying())
		{
			return;
		}
		isLooping = true;
		playerThread = new PlayerRunnerThread(Playback.LOOP_ALL);
		playerThread.start();
	}
	
	/**
	 * Immediately stops playback of the MPEG file currently playing (if
	 * playing) and stops looping playback (if looping).
	 */
	public void stop()
	{
		isLooping = false;
		isPlaying = false;
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
		throw new UnsupportedOperationException(
				"MP3Player.java: Pause method not implemented");
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
		throw new UnsupportedOperationException(
				"MP3Player.java: Resume method not implemented");
	}
	
	// Private inner class
	
	/**
	 * <code>Thread</code> class responsible for the playback of MPEG audio
	 * files in the playback queue.
	 * 
	 * @see java.lang.Thread
	 */
	private class PlayerRunnerThread extends Thread
	{
		/**
		 * Denotes whether the <code>run</code> method should play only the next
		 * file, all files, or loop all files in the playback queue.
		 */
		private final Playback playback;
		
		/**
		 * Constructs a new <code>PlayerRunnerThread</code> instance that
		 * 
		 * @throw JavaLayerException if the file stream cannot be played
		 */
		PlayerRunnerThread(Playback pb)
		{
			playback = pb;
		}
		
		/**
		 * Starts playback of file(s) in the playback queue. Behavior is based
		 * on the <code>Playback</code> constant passed to the constructor.
		 * 
		 * @throws RuntimeException
		 *             if any MPEG file cannot be played
		 */
		@Override
		public void run()
		{
			try
			{
				switch (playback)
				{
					case PLAY_NEXT:
						playNextAsync();
						break;
					case PLAY_ALL_ONCE:
						playAllOnceAsync();
						break;
					case LOOP_ALL:
						loopAllAsync();
						break;
				}
				// Blocks this thread until all playback is complete
			}
			catch (IOException | JavaLayerException e)
			{
				throw new RuntimeException(
						"MP3Player.java: Cannot play MPEG file");
			}
		}
		
		/**
		 * Plays the next file in the playback queue.
		 */
		private void playNextAsync() throws JavaLayerException,
			FileNotFoundException
		{
			player = new Player(getNextFileInputStream());
			player.play();
		}
		
		/**
		 * Plays the queued MPEG file(s) one time each.
		 */
		private void playAllOnceAsync() throws FileNotFoundException,
			JavaLayerException
		{
			for (int i = 0; i < playbackQueue.size(); i++)
			{
				playNextAsync();
			}
		}
		
		/**
		 * Continues to play the MPEG file(s) until the <code>stop</code> method
		 * is called.
		 */
		private void loopAllAsync() throws FileNotFoundException,
			JavaLayerException
		{
			while (isLooping())
			{
				playAllOnceAsync();
			}
		}
		
		/**
		 * Gets the next file stream from the playback queue.
		 */
		private FileInputStream getNextFileInputStream()
			throws FileNotFoundException
		{
			File f = playbackQueue.remove();
			playbackQueue.add(f);
			return new FileInputStream(f);
		}
	}
	
	/**
	 * Used to control playback within the <code>PlayerRunnerThread</code>
	 * class.
	 */
	private static enum Playback
	{
		/**
		 * Only play the next song in the playback queue.
		 */
		PLAY_NEXT,
		
		/**
		 * Play all files in the playback queue once.
		 */
		PLAY_ALL_ONCE,
		
		/**
		 * Keep playing all files in the playback queue until
		 * <code>MP3Player.stop</code> is called.
		 */
		LOOP_ALL;
	}
}