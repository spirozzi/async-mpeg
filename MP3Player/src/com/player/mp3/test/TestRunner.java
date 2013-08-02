package com.player.mp3.test;

import java.io.File;
import java.io.FileNotFoundException;

import javazoom.jl.decoder.JavaLayerException;

import com.player.mp3.MP3Player;

public class TestRunner
{
	// TODO
	public static void main(String[] args) throws InterruptedException,
		FileNotFoundException, JavaLayerException
	{
		File f1 = new File("res\\s1.mp3");
		File f2 = new File("res\\s2.mp3");
		MP3Player player = new MP3Player(new File[] {f1, f2});
		player.playNext();
		Thread.sleep(5000);
		player.playNext();
		Thread.sleep(2000);
		// Play all
		player.playAllOnce();
	}
	
	// TODO
	public static void example() throws InterruptedException,
		FileNotFoundException, JavaLayerException
	{
		File f1 = new File("res\\s1.mp3");
		File f2 = new File("res\\s2.mp3");
		MP3Player player = new MP3Player(new File[] {f1, f2});
		player.playNext();
		Thread.sleep(1000);
		player.playNext();
	}
}