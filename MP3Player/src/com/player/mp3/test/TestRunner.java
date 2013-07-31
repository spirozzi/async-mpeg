package com.player.mp3.test;

import java.io.File;

import com.player.mp3.MP3Player;

public class TestRunner
{
	public static void main(String[] args) throws InterruptedException
	{
		File f1 = new File("res\\WindowsXPLogonSound.mp3");
		File f2 = new File("res\\WindowsXPDing.mp3");
		MP3Player player = new MP3Player(new File[] {f1, f2});
		player.playNext();
		Thread.sleep(5000);
		player.playNext();
		Thread.sleep(2000);
		
		// playall
		player.playAllOnce();
	}
	
	public static void example() throws InterruptedException
	{
		/*
		 * Remember to import your MP3 file into your src folder so you can
		 * reference it like this: "src\\FileName.mp3" This is an example of
		 * calling code to invoke the new and improved looping MP3 player in
		 * different ways.
		 */
		File f1 = new File("res\\WindowsXPLogonSound.mp3");
		File f2 = new File("res\\WindowsXPDing.mp3");
		
		MP3Player player = new MP3Player(new File[] {f1, f2});
		player.playNext(); // Plays the next MP3 file in the queue.
		Thread.sleep(1000);
		player.playNext();
		
		// Multiple MP3 files example (looping):
		
		// TODO
	}
}