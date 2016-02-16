package jian.zhang.oceantidereader.domainobjects;

public class Tide {
	private String mTime;
	private String mFeet;
	private String mLowOrHigh;
	
	public String getTime() {
		return mTime;
	}

	public void setTime(String time) {
		mTime = time;
	}

	public String getFeet() {
		return mFeet;
	}

	public void setFeet(String feet) {
		mFeet = feet;
	}

	public String getLowOrHigh() {
		return mLowOrHigh;
	}

	public void setLowOrHigh(String lowOrHigh) {
		this.mLowOrHigh = lowOrHigh;
	}
}
