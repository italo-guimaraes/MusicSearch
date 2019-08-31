package dao;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Database {
	
	private static Database databaseInstance = null;
	
	private MongoClient mongoClient;
	private MongoDatabase database;
	
	// Tables
	private MongoCollection<Document> urls;
	
	private Database() {
		mongoClient = new MongoClient("localhost", 27017);
		database = mongoClient.getDatabase("music_search_database");
		createTables();
	}
	
	public static Database Database() {
		if (databaseInstance == null) {
			databaseInstance = new Database();
		}
		return databaseInstance;
	}

	private void createTables() {
		urls = database.getCollection("urls");
	}

}
