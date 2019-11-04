package dao;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

import models.Artist;

public class ArtistsDao {
	
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
	private File nomeArquivo = new File("Artists.txt");

	public void saveArtist(Artist artist) {
		
		Document artistDao = new Document();
		
		artistDao.put("url", artist.getUrl());
		artistDao.put("name", artist.getName());
		artistDao.put("views", artist.getViews());
		artistDao.put("imageUrl", artist.getImageUrl());
		
		executeAsync(artistDao);
	}
	
	public long getTotalArtists() {
		return Database.getInstance().getArtists().count();
	}
	
	public Long getTotalArtistsIndexed() {		
		Document queryDoc = new Document();
		queryDoc.put("isIndexed", 1);
		return Database.getInstance().getArtists().count(queryDoc);				
	}
	
	public void saveInFile() {
		criaArquivo(nomeArquivo);
		Database.getInstance().getArtists().find().forEach(saveBlock);
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
			Database.getInstance().getArtists().updateOne(query, upsert, options);
			
		} while (true);	
		
		isExecuting = false;
	}
	
	public void setAllArtistToIndex() {
		
		Document queryDoc = new Document();
		queryDoc.put("isIndexed", 1);
		
		FindIterable<Document> artistsDao = Database.getInstance().getArtists().find(queryDoc);
		if (artistsDao == null ) {
			return;
		}
		
		for (Document artistDao : artistsDao) {			
			String id = artistDao.get("_id").toString();	
			artistDao.put("isIndexed", null);		
			queryDoc.put("_id", new ObjectId(id));
			Database.getInstance().getArtists().replaceOne(queryDoc, artistDao, new UpdateOptions().upsert(true));
		}
	}
	
	public Artist getNextArtist() {
		
		Document queryDoc = new Document();
		queryDoc.put("isIndexed", null);
		
		Document artistDao = Database.getInstance().getArtists().find(queryDoc).limit(1).first();
		if (artistDao == null ) {
			return null;
		}
		artistDao.put("isIndexed", 1);
		String name = artistDao.getString("name");
		Long views = artistDao.getLong("views");
		String id = artistDao.get("_id").toString();		
		
		queryDoc.put("_id", new ObjectId(id));
		Database.getInstance().getArtists().replaceOne(queryDoc, artistDao, new UpdateOptions().upsert(true));
		
		Artist artist = new Artist();
		artist.setId(id);
		artist.setName(name);
		artist.setViews(views);
		
		return artist;		
	}

	public Artist getArtistById(String idDocumento) {
		
		Document queryDoc = new Document();
		queryDoc.put("_id", new ObjectId(idDocumento));
		
		Document artistDao = Database.getInstance().getArtists().find(queryDoc).first();
		
		Artist artist = new Artist();
		artist.setId(artistDao.get("_id").toString());
		artist.setName(artistDao.getString("name"));
		artist.setViews(artistDao.getLong("views"));
		artist.setUrl(artistDao.getString("url"));
		artist.setImageUrl(artistDao.getString("imageUrl"));
		
		return artist;
	}

}
