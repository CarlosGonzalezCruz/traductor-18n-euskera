package es.bilbomatica.test.model;

public class TraductorResponse {

	private boolean success;
	private int translationErrorCode;
	private int jobTime;
	private int waitingTime;
	private int translationTime;
	private int preprocessingTime;
	private int postprocessingTime;
	private int marianTranslationTime;
	private int userTime;
	private String message;
	private String attention;

	/**
	 * 
	 */
	public TraductorResponse() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the success
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * @param success the success to set
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * @return the translationErrorCode
	 */
	public int getTranslationErrorCode() {
		return translationErrorCode;
	}

	/**
	 * @param translationErrorCode the translationErrorCode to set
	 */
	public void setTranslationErrorCode(int translationErrorCode) {
		this.translationErrorCode = translationErrorCode;
	}

	/**
	 * @return the jobTime
	 */
	public int getJobTime() {
		return jobTime;
	}

	/**
	 * @param jobTime the jobTime to set
	 */
	public void setJobTime(int jobTime) {
		this.jobTime = jobTime;
	}

	/**
	 * @return the waitingTime
	 */
	public int getWaitingTime() {
		return waitingTime;
	}

	/**
	 * @param waitingTime the waitingTime to set
	 */
	public void setWaitingTime(int waitingTime) {
		this.waitingTime = waitingTime;
	}

	/**
	 * @return the translationTime
	 */
	public int getTranslationTime() {
		return translationTime;
	}

	/**
	 * @param translationTime the translationTime to set
	 */
	public void setTranslationTime(int translationTime) {
		this.translationTime = translationTime;
	}

	/**
	 * @return the preprocessingTime
	 */
	public int getPreprocessingTime() {
		return preprocessingTime;
	}

	/**
	 * @param preprocessingTime the preprocessingTime to set
	 */
	public void setPreprocessingTime(int preprocessingTime) {
		this.preprocessingTime = preprocessingTime;
	}

	/**
	 * @return the postprocessingTime
	 */
	public int getPostprocessingTime() {
		return postprocessingTime;
	}

	/**
	 * @param postprocessingTime the postprocessingTime to set
	 */
	public void setPostprocessingTime(int postprocessingTime) {
		this.postprocessingTime = postprocessingTime;
	}

	/**
	 * @return the marianTranslationTime
	 */
	public int getMarianTranslationTime() {
		return marianTranslationTime;
	}

	/**
	 * @param marianTranslationTime the marianTranslationTime to set
	 */
	public void setMarianTranslationTime(int marianTranslationTime) {
		this.marianTranslationTime = marianTranslationTime;
	}

	/**
	 * @return the userTime
	 */
	public int getUserTime() {
		return userTime;
	}

	/**
	 * @param userTime the userTime to set
	 */
	public void setUserTime(int userTime) {
		this.userTime = userTime;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the attention
	 */
	public String getAttention() {
		return attention;
	}

	/**
	 * @param attention the attention to set
	 */
	public void setAttention(String attention) {
		this.attention = attention;
	}

}
