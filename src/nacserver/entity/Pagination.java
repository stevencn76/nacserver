package nacserver.entity;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Pagination {

	private int currentPage;

	private int pageSize = 20;

	private long pageCount;

	private long totalCount;

	private List<?> result;

	//record start number of page
	private int pageStart;
	
	
	public Pagination() {

	}

	public Pagination(int curretPage, int pageSize) {

		this.currentPage = curretPage;
		this.pageSize = pageSize;
	}

	public Pagination(int currentPage, int pageSize, long totalCount) {

		this.currentPage = currentPage;
		this.pageSize = pageSize;
		this.totalCount = totalCount;
	}

	public Pagination(List<?> result) {

		this.result = result;
	}

	public int getCurrentPage() {

		return currentPage;
	}

	public void setCurrentPage(int currentPage) {

		this.currentPage = currentPage;
	}

	public long getPageCount() {

		return pageCount;
	}

	public void setPageCount(long pageCount) {

		this.pageCount = pageCount;
	}

	public int getPageSize() {

		return pageSize;
	}

	public void setPageSize(int pageSize) {

		this.pageSize = pageSize;
	}

	public List<?> getResult() {

		return result;
	}

	public void setResult(List<?> result) {

		this.result = result;
	}

	public long getTotalCount() {

		return totalCount;
	}

	public void setTotalCount(long totalCount) {

		this.totalCount = totalCount;
	}

	public static long getPageCount(long totalCount, int pageSize) {

		if (totalCount < 0 || pageSize <= 0)
			return 0;
		if (totalCount % pageSize == 0)
			return totalCount / pageSize;
		else
			return totalCount / pageSize + 1;
	}

	public int getPageStart() {
		return pageStart;
	}

	public void setPageStart(int pageStart) {
		this.pageStart = pageStart;
	}

}