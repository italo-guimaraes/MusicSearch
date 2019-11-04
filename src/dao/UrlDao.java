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

public class UrlDao {
	
	static Block<Document> printBlock = new Block<Document>() {
	       @Override
	       public void apply(final Document document) {
	           System.out.println(document.toJson());
	       }
	};
	
	
	static Block<Document> saveBlock = new Block<Document>() {
	       @Override
	       public void apply(final Document document) {
	    	   gravarArquivoUrls.println(document.toJson());
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
	private static PrintWriter gravarArquivoUrls;
	private FileWriter arquivoUrls;
	private File nomeArquivoUrls = new File("Urls.txt");
	
	public void insert(String src, String title, String imageUrl) {	
		
		Document urlDao = new Document();
		urlDao.put("src", src);
		if (title != null) {			
			urlDao.put("title", title);
		}
		if (imageUrl != null) {			
			urlDao.put("imageUrl", imageUrl);
		}
		
		Document upsert = new Document();
		upsert.append("$set", urlDao);
		
		
//		query = Filters.eq("src", upsert.getString("src"));
//		Database.getInstance().getUrls().updateOne(query, upsert, options);
		executeAsync(urlDao);
	}
	
	public void print() {
		System.out.println("");
		System.out.println("____________________Show Mongo Urls____________________");
		System.out.println("");
		Database.getInstance().getUrls().find().forEach(printBlock);
		System.out.println("");
		System.out.println("_____________________End Mongo Urls_____________________");
		System.out.println("");
	}
	
	public void saveInFile() {
		criaArquivo(nomeArquivoUrls);
		Database.getInstance().getUrls().find().forEach(saveBlock);
		gravarArquivoUrls.close();
	}
	
	public String getNextUrl() {
		Document queryDoc = new Document();
		queryDoc.put("isCollected", null);
		
		Document urlDao = Database.getInstance().getUrls().find(queryDoc).limit(1).first();
		if (urlDao == null ) {
			return null;
		}
		urlDao.put("isCollected", 1);
		String src = urlDao.getString("src");
		queryDoc.put("src", src);
		Database.getInstance().getUrls().replaceOne(queryDoc, urlDao, new UpdateOptions().upsert(true));
		return src;
	}
	
	public long getTotalUrls() {
		return Database.getInstance().getUrls().count();
	}
	
	//Metodo para criar o arquivo de Index
	private void criaArquivo(File nomeArquivoUrls) {
		try {
			arquivoUrls = new FileWriter(nomeArquivoUrls);
			gravarArquivoUrls = new PrintWriter(arquivoUrls);
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
			query = Filters.eq("src", document.getString("src"));
			Database.getInstance().getUrls().updateOne(query, upsert, options);
			
		} while (true);
		
		isExecuting = false;
	}	
}
