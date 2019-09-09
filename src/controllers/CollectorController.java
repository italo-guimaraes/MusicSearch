package controllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import dao.ArtistsDao;
import dao.LyricsDao;
import dao.UrlDao;
import models.Artist;
import models.Lyrics;

public class CollectorController {
	
	private UrlDao urlDao = new UrlDao();
	private LyricsDao lyricsDao = new LyricsDao();
	private ArtistsDao artistsDao = new ArtistsDao();
	
	private long urlsAccessed = 0;
	private long musicFound = 0;
	private long artistFound = 0;
		
	public static void main(String[] args) {
		
		System.out.println("Start...");
		
		CollectorController collector = new CollectorController();
//		collector.collectUrls("https://www.letras.mus.br");
//		collector.urlDao.saveInFile();
//		collector.lyricsDao.saveInFile();
		collector.artistsDao.saveInFile();		
	}
	
	private void collectUrls(String seed) {
		searchUrl(seed);
		sleep(10000);
		String url = urlDao.getNextUrl();
		int count = 0;
		while (url != null) {
			
			count++;
			if (count >= 100) {
				count = 0;
				System.out.println(" Urls accessed: " + urlsAccessed + 
						"\t Urls in db: " + urlDao.getTotalUrls() + 
						"\t Music in db: " + lyricsDao.getTotalLyrics() +
						"\t Artists in db: " + artistsDao.getTotalArtists());
			} else {
				System.out.println(" Urls accessed: " + urlsAccessed);
			}
			
			searchUrl(url);			
			url = urlDao.getNextUrl();
		}
		urlDao.saveInFile();
	}
	
	private void searchUrl(String url) {
		Element conteudo = conectaUrl(url);
		if(conteudo != null){
			getLinks(conteudo);
			
			try{
				saveIfIsLyrics(conteudo, url);
				saveIfIsArtist(conteudo, url);

			}catch (Exception e) {
				System.err.println("Erro ao pegar o conteudo da pagina " + url);
				System.err.println(e);
			}

			
		}
	}
	
	private Element conectaUrl(String url) {
		String resolvedUrl = resolvedUrl(url);
		if(resolvedUrl != null && !resolvedUrl.isEmpty()){
			Document html;
			try {
				html = Jsoup.connect(resolvedUrl).get();
				Element conteudo = html.select("#all").first();
				urlsAccessed++;
				return conteudo;
			} 
			catch (IOException e) {
				System.err.println("Unable to connect in " + resolvedUrl);
			}
		}
		return null;
	}
	
	private String resolvedUrl(String url) {
		String retorno = null;
		try {
			url = URLDecoder.decode(URLDecoder.decode(url, "UTF-8"), "ISO-8859-1");
			if(url.length() >= 2) {
				if (url.length() > 8 && url.substring(0, 8).equals("https://")) {
					if (url.length() >= 25 && url.substring(0, 25).equals("https://www.letras.mus.br")) {
						retorno = url;
					} else {
						retorno = null;
					}
				} else if (url.length() >= 7 && url.substring(0, 7).equals("http://")) {
					if (url.length() > 24 && url.substring(0, 24).equals("http://www.letras.mus.br")) {
						retorno = url;
					} else {
						retorno = null;
					}
				} else if (url.substring(0, 1).equals("/")) {
					retorno = "https://www.letras.mus.br"+ url;
				}
			}
			return retorno;
		} catch (UnsupportedEncodingException e) {
			System.err.println("Fail in resolvedUrl: UnsupportedEncodingException. "
					+ ""
					+ "" + url);
		}
		catch(Exception e){
			System.err.println("Fail in resolvedUrl: Exception. " + url);
		}
		return retorno;
	}
	
	private void getLinks(Element html) {
		Elements links = html.select("a");
		for (Element element : links) {
			if(element.text() != null && !element.text().equals("")) {
				String textLink = element.text();
				String href = null;
				if(element.hasAttr("href")) {
					href = element.attr("href");
				}
				String imageUrl = null;
				if (element.hasAttr("img")) {				
					imageUrl = element.select("img").first().attr("src");
				} else {
					Element image = element.selectFirst("img");
					if (image != null && image.hasAttr("src")) {
						imageUrl = image.attr("src");
					}
				}
			
				String resolvedUrl = resolvedUrl(href);			
				if(resolvedUrl != null && !resolvedUrl.isEmpty()){
					urlDao.insert(resolvedUrl, textLink, imageUrl);
				}	
			}
		}
	}
	
	private void saveIfIsLyrics(Element conteudo, String url) {
		
		Elements cntLetra = conteudo.getElementsByClass("cnt-letra");
		Elements cntHeadTitle = conteudo.getElementsByClass("cnt-head_title");
		
		if (cntLetra != null && !cntLetra.isEmpty()
				&& cntHeadTitle != null && !cntHeadTitle.isEmpty()
				&& isLyrics(url)) {
			
			Lyrics lyrics = new Lyrics();
			lyrics.setName(cntHeadTitle.first().select("h1").first().text());
			lyrics.setArtistName(cntHeadTitle.first().select("h2").first().text());					
			lyrics.setViews(getViewFromPage(conteudo));
			lyrics.setLyrics(getLyricsFromPage(conteudo));
			lyrics.setUrl(resolvedUrl(url));
			
			musicFound++;
			lyricsDao.saveLyrics(lyrics);
		}
	}
	
	private boolean isLyrics(String url) {
		if (url.contains(".html?")) {
			return false;
		}
		return true;
	}

	private void saveIfIsArtist(Element conteudo, String url) {
		
		Elements cntArtista = conteudo.getElementsByClass("cnt-artista");
		Elements cntHeadTitle = conteudo.getElementsByClass("cnt-head_title");
		
		if (cntArtista != null && !cntArtista.isEmpty()
				&& cntHeadTitle != null && !cntHeadTitle.isEmpty() 
				&& isUrlArtist(url)) {
			
			Artist artist = new Artist();
			artist.setName(cntHeadTitle.first().select("h1").first().text());				
			artist.setViews(getViewFromPage(conteudo));
			artist.setImageUrl(getImageFromPage(conteudo));
			artist.setUrl(resolvedUrl(url));
			
			artistFound++;
			artistsDao.saveArtist(artist);		
		}
	}
	
	private boolean isUrlArtist(String url) {
		String dados[] = url.split("/");
		if (dados.length == 4) {
			return true;
		}		
		return false;
	}
	
	private String getLyricsFromPage(Element conteudo) {
		String lyrics = conteudo.getElementsByClass("cnt-letra").first().text();
		if (lyrics != null && !lyrics.isEmpty()) { 
			return lyrics;
		}
		return null;
	}

	private Long getViewFromPage(Element conteudo) {
		Element cntInfoExib = conteudo.getElementsByClass("cnt-info_exib").first();
		if (cntInfoExib != null) {
			String textView = cntInfoExib.select("b").first().text();
			if (textView != null && !textView.isEmpty()) { 
				textView = textView.replace(" ", "");
				textView = textView.replace(".", "");
				Long view = Long.parseLong(textView);
				return view;
			}
		}
		return 0L;
	}
	
	private String getImageFromPage(Element conteudo) {
		String imageUrl = conteudo.getElementsByClass("cnt-head_title").first().select("img").attr("src");
		if (imageUrl != null && !imageUrl.isEmpty()) { 
			return imageUrl;
		}
		return null;
	}
	
	private void sleep(long time) {
		try {
			Thread.currentThread().sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
