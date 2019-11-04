package index;

import java.text.Normalizer;
import java.util.ArrayList;

import org.bson.Document;

import dao.ArtistsDao;
import dao.IndexDao;
import models.Artist;
import models.Documento;

public class IndexController {
	
	private ArtistsDao artistsDao = new ArtistsDao();
	private IndexDao indexDao = new IndexDao();
	
	public static void main(String[] args) {
		IndexController index = new IndexController();
		index.searchOnIndex("Wesley");
		
		
//		index.createIndex();
//		index.indexDao.saveInFile();
//		index.artistsDao.saveInFile();
	}
	
	public IndexController() {
		System.out.println("Start...");
	}
	
	private void createIndex() {
		
		Artist artist = artistsDao.getNextArtist();
		int count = 0;
		while (artist != null) {
			count++;
			
			insertArtistInIndex(artist);
			
			System.out.println(count + " Artista " + artist.getName() + " inserido com sucesso");
			artist = artistsDao.getNextArtist();
		}		
	}
	
	private void insertArtistInIndex(Artist artist) {
		String namesArtist[] = artist.getName().split(" ");
		
		for (int i = 0; i < namesArtist.length; i++) {
			String palavraTratada = trataPalavra(namesArtist[i]);
			if(palavraTratada.length() > 0){
				Document indice = indexDao.getIndexByWord(palavraTratada);
				if(indice == null) {
					indexDao.insertIndex(palavraTratada, artist.getId(), namesArtist.length, artist.getViews());
				} else {
					indexDao.updateIndex(indice, palavraTratada, artist.getId(), namesArtist.length, artist.getViews());
				}
			}
		}
	}	
	
	public String trataPalavra(String string) {
		String retorno = "";
		String minuscula = string.toLowerCase();
		minuscula = removerAcentos(minuscula);
		for(int i=0;i<minuscula.length();i++){
			if((minuscula.charAt(i) >= 'a' && minuscula.charAt(i) <='z')
				|| minuscula.charAt(i) == ' '
				|| (minuscula.charAt(i) >= '0' && minuscula.charAt(i) <= '9')){
				retorno = retorno + minuscula.charAt(i);
			}
		}
		return retorno;
	}
	
	public void searchOnIndex(String string) {
		System.out.println("Searching " + string);
		ArrayList<Documento> documentos = getIndexsByWords(string);
		
		for (Documento documento : documentos) {
			Artist artist = artistsDao.getArtistById(documento.getIdDocumento());
//			System.out.println(artist.getName() + " " + artist.getUrl());
			System.out.println(artist.getName());
		}
	}
	
	
	private ArrayList<Documento> getIndexsByWords(String search) {
		ArrayList<Documento> documentos = new ArrayList<>();
		String words[] = search.split(" ");
		for (String word : words) {
			String palavraTratada = trataPalavra(word);
			documentos.addAll(indexDao.searchByWord(palavraTratada));
		}
		
		return documentos;
	}
	
	public static String removerAcentos(String str) {
	    return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
	}

}
