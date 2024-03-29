package go;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Christoph Friegel
 * @version 1.0
 */

@XmlRootElement
public class Count {

	String countNr = "-";
	String maxDate = "-";
	String minDate = "-";

	public String getCountNr() {
		return countNr;
	}

	public void setCountNr(String countId) {
		this.countNr = countId;
	}

	public String getMaxDate() {
		return maxDate;
	}

	public void setMaxDate(String maxDate) {
		this.maxDate = maxDate;
	}

	public String getMinDate() {
		return minDate;
	}

	public void setMinDate(String minDate) {
		this.minDate = minDate;
	}

}
