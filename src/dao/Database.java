package dao;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Database {
	
	/*
	 * Run Mongodb
	 * sudo service mongod start
	 */
	
	private static Database databaseInstance = null;
	
	private MongoClient mongoClient;
	private MongoDatabase database;
	
	// Tables
	private MongoCollection<Document> urls;
	private MongoCollection<Document> lyrics;
	private MongoCollection<Document> artists;
	
	private Database() {
		mongoClient = new MongoClient("localhost", 27017);
		database = mongoClient.getDatabase("music_search_database");
		createTables();
	}
	
	public static Database getInstance() {
		if (databaseInstance == null) {
			databaseInstance = new Database();
		}
		return databaseInstance;
	}

	private void createTables() {
//		urls = database.getCollection("urls");
//		urls.drop();
//		lyrics = database.getCollection("lyrics");
//		lyrics.drop();
//		artists = database.getCollection("artists");
//		artists.drop();
		
		urls = database.getCollection("urls");
		lyrics = database.getCollection("lyrics");
		artists = database.getCollection("artists");
	}

	public MongoCollection<Document> getUrls() {
		return urls;
	}
	
	public MongoCollection<Document> getLyrics() {
		return lyrics;
	}
	
	public MongoCollection<Document> getArtists() {
		return artists;
	}

}
