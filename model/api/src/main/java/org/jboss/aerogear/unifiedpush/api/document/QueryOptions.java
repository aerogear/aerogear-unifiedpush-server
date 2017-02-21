package org.jboss.aerogear.unifiedpush.api.document;

public class QueryOptions {

	private Long fromDate;
	private Long toDate;

	public QueryOptions(Long fromDate) {
		super();
		this.fromDate = fromDate;
	}

	public QueryOptions(Long fromDate, Long toDate) {
		super();
		this.fromDate = fromDate;
		this.toDate = toDate;
	}

	public Long getFromDate() {
		return fromDate;
	}

	public void setFromDate(Long fromDate) {
		this.fromDate = fromDate;
	}

	public Long getToDate() {
		return toDate;
	}

	public void setToDate(Long toDate) {
		this.toDate = toDate;
	}

}
