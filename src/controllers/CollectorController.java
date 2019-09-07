package controllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import dao.UrlDao;

public class CollectorController {
	
	private UrlDao urlDao = new UrlDao();
	
	private long urlsAccessed = 0;
		
	public static void main(String[] args) {
		
		System.out.println("Start...");
		
		CollectorController collector = new CollectorController();
//		collector.collectUrls("https://www.letras.mus.br");
		collector.urlDao.saveInFile();
		
	}
	
	private void collectUrls(String seed) {
		searchUrl(seed);
		String url = urlDao.getNextUrl();
		int count = 0;
		while (urlDao.getNextUrl() != null) {
			
			count++;
			if (count > 100) {
				count = 0;
				System.out.println(" Urls accessed: " + urlsAccessed + "\t Urls in db " + urlDao.getTotalUrls());
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

}
