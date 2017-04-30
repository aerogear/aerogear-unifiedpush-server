package org.jboss.aerogear.unifiedpush.api.document;

public class QueryOptions {

	private Long fromDate;
	private Long toDate;
	private String id;
	private Integer limit;

	public QueryOptions() {

	}

	public QueryOptions(String id) {
		super();
		this.id = id;
	}

	public QueryOptions(Long fromDate) {
		super();
		this.fromDate = fromDate;
	}

	public QueryOptions(Long fromDate, Long toDate) {
		super();
		this.fromDate = fromDate;
		this.toDate = toDate;
	}

	public QueryOptions(Long fromDate, Long toDate, String id, Integer limit) {
		super();
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.id = id;
		this.limit = limit;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

}
