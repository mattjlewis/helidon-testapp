package uk.mattjlewis.testapp.openapi.model;

public class TestModel {
	private int id;
	private String code;
	private String value;
	
	public TestModel() {
	}

	public TestModel(int id, String code, String value) {
		this.id = id;
		this.code = code;
		this.value = value;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
