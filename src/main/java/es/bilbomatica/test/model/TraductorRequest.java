package es.bilbomatica.test.model;

public class TraductorRequest {

	private String mkey;
	private String text;
	private String model;

	/**
	 * 
	 */
	public TraductorRequest() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the mkey
	 */
	public String getMkey() {
		return mkey;
	}

	/**
	 * @param mkey the mkey to set
	 */
	public void setMkey(String mkey) {
		this.mkey = mkey;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the model
	 */
	public String getModel() {
		return model;
	}

	/**
	 * @param model the model to set
	 */
	public void setModel(String model) {
		this.model = model;
	}

}
