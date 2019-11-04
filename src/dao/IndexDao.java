package dao;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;

import models.Documento;

public class IndexDao {
	
	static Block<Document> saveBlock = new Block<Document>() {
	       @Override
	       public void apply(final Document document) {
	    	   gravarArquivo.println(document.toJson());
	       }
	};
	
	// File
	private static PrintWriter gravarArquivo;
	private FileWriter arquivo;
	private File nomeArquivo = new File("Index.txt");

	private Document queryDoc;
	
	public void saveInFile() {
		criaArquivo(nomeArquivo);
		Database.getInstance().getIndexs().find().forEach(saveBlock);
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
	
	public void insert() {	
		
	}

	public Document getIndexByWord(String palavraTratada) {
		Document queryIndice = new Document();
		queryIndice.put("indice", palavraTratada);
		return Database.getInstance().getIndexs().find(queryIndice).first();
	}
	
	public void insertIndex(String palavraTratada, String idDoc, int docLenght, Long views) {
		Document indice = new Document();
		indice.put("indice", palavraTratada);
		indice.put("qtdDocs", 1);
		
		ArrayList<Document> documentos = new ArrayList<>();
		
		Document documento = new Document();
		documento.put("doc", idDoc);
		documento.put("tamDoc",docLenght);
		documento.put("views", views);
		documento.put("avgDoc", 1);
		documentos.add(documento);
		
		indice.put("docs", documentos);
		Database.getInstance().getIndexs().insertOne(indice);
	}
	
	public void updateIndex(Document indice, String palavraTratada, String idDoc, int docLenght, Long views) {

		Document queryDoc = new Document();
		queryDoc.put("indice", palavraTratada);
		Document updateDocs = new Document();
		
		ArrayList<Document> listaDocs = (ArrayList<Document>) indice.get("docs");
		
		for (Document document : listaDocs) {			
			if((document.get("doc")).toString() == idDoc && 
					((Number) document.get("tamDoc")).intValue() == docLenght){
				Integer avgDoc = ((Number) document.get("avgDoc")).intValue() + 1;
				document.put("avgDoc", avgDoc);
				updateDocs.append("$set", indice);		
				Database.getInstance().getIndexs().updateOne(queryDoc, updateDocs);
				return;
			}
		}
		
		// Se não exitir documento
		Document newDoc = new Document();
		Integer qtdDoc = indice.getInteger("qtdDocs");
		indice.put("qtdDocs", ++qtdDoc);
		newDoc.put("doc",idDoc);
		newDoc.put("tamDoc",docLenght);
		newDoc.put("views",views);
		newDoc.put("avgDoc", 1);
		listaDocs.add(newDoc);
		updateDocs.append("$set", indice);        
		Database.getInstance().getIndexs().updateOne(queryDoc, updateDocs);
	}
	
	public ArrayList<Documento> searchByWord(String word) {
		
		ArrayList<Documento> documentos = new ArrayList<>();
		queryDoc = new Document();
		queryDoc.put("indice", word);
		
		FindIterable<Document> listIndex = Database.getInstance().getIndexs().find(queryDoc);
		
		for (Document index : listIndex) {
			ArrayList<Document> docs =  (ArrayList<Document>) index.get("docs");
			for (Document doc : docs) {
				Documento documento = new Documento();
				documento.setIdDocumento(doc.get("doc").toString());
				documento.setTamanhoDocumento(doc.getInteger("tamDoc"));
				documento.setAvgDoc(doc.getInteger("avgDoc"));
				documento.setView(doc.getLong("views"));
				documentos.add(documento);
			}			
		}
		
		return documentos;
	}

}
