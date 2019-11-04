package models;

public class Documento {
	
	private String idDocumento;
	private int tamanhoDocumento;
	private int avgDoc;
	private long view;
	
	public String getIdDocumento() {
		return idDocumento;
	}
	public void setIdDocumento(String idDoCumento) {
		this.idDocumento = idDoCumento;
	}
	public int getTamanhoDocumento() {
		return tamanhoDocumento;
	}
	public void setTamanhoDocumento(int tamanhoDocumento) {
		this.tamanhoDocumento = tamanhoDocumento;
	}
	public int getAvgDoc() {
		return avgDoc;
	}
	public void setAvgDoc(int avgDoc) {
		this.avgDoc = avgDoc;
	}
	public long getView() {
		return view;
	}
	public void setView(long view) {
		this.view = view;
	}

}
