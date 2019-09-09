package models;

public class Artist {
	
	private String name;
	private String imageUrl;
	private Long views;
	private String url;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getImageUrl() {
		return imageUrl;
	}
	
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	
	public Long getViews() {
		return views;
	}
	
	public void setViews(Long views) {
		this.views = views;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}	

}
