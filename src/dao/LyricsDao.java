package dao;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Queue;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.Block;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

import models.Lyrics;

public class LyricsDao {
	
	static Block<Document> saveBlock = new Block<Document>() {
	       @Override
	       public void apply(final Document document) {
	    	   gravarArquivo.println(document.toJson());
	       }
	};
	
	private UpdateOptions options = new UpdateOptions().upsert(true);
	private Bson query;
	
	// Queue
	private Boolean isAsyncThreadAlive = false;
	private Boolean isExecuting = false;
	private Queue<Document> taskQueue = new ArrayDeque<Document>();
	private Document document = null;
	
	// File
	private static PrintWriter gravarArquivo;
	private FileWriter arquivo;
	private File nomeArquivo = new File("Lyrics.txt");

	public void saveLyrics(Lyrics lyrics) {
		
		Document lyricsDao = new Document();
		lyricsDao.put("url", lyrics.getUrl());
		lyricsDao.put("name", lyrics.getName());
		lyricsDao.put("artist", lyrics.getArtistName());
		lyricsDao.put("views", lyrics.getViews());
		lyricsDao.put("lyrics", lyrics.getLyrics());		
		
		executeAsync(lyricsDao);
		
	}
	
	public long getTotalLyrics() {
		return Database.getInstance().getLyrics().count();
	}
	
	public void saveInFile() {
		criaArquivo(nomeArquivo);
		Database.getInstance().getLyrics().find().forEach(saveBlock);
		gravarArquivo.close();
	}
	
	private void criaArquivo(File nomeArquivo) {
		try {
			arquivo = new FileWriter(nomeArquivo);
			gravarArquivo = new PrintWriter(arquivo);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void executeAsync(Document document) {
		synchronized (taskQueue) {
			taskQueue.add(document);
			scheduleNext();
		}
	}
	
	private void scheduleNext() {
        synchronized(isAsyncThreadAlive) {
        	if (isAsyncThreadAlive) {
        		return;
        	}
        	isAsyncThreadAlive = true;
        }
        
        new Thread() {       		
    		public void run() {
    			synchronized(isAsyncThreadAlive) {
    				isAsyncThreadAlive = false;
    			}
    			execute();
    		}
    	}.start();
    }
	
	private void execute() {
		synchronized (isExecuting) {
			if (isExecuting) {
				return;
			}
			
			isExecuting = true;
		}
		
		do {
			synchronized (taskQueue) {
				document = taskQueue.poll();
				if (document == null) {
					break;
				}
			}
			Document upsert = new Document();
			upsert.append("$set", document);
			query = Filters.eq("url", document.getString("url"));
			Database.getInstance().getLyrics().updateOne(query, upsert, options);
			
		} while (true);
		
		isExecuting = false;
	}	

}
