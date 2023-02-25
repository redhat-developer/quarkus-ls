package com.redhat.qute.services.completions.tags;

public class UpdateOrphanEndTagSectionData {

	private int offset;

	private String tag;

	public UpdateOrphanEndTagSectionData(int offset, String tag) {
		this.offset = offset;
		this.tag = tag;
	}

	public UpdateOrphanEndTagSectionData() {
	}

	public int getOffset() {
		return offset;
	}

	public String getTag() {
		return tag;
	}

}
