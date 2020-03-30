package com.vonchange.mybatis.tpl.model;


/**
 * 实体字段信息
 * @author vonchange@163.com
 */
public class EntityField {
	private String fieldName;
	private String columnName;
	private String typeName;
	private Boolean isBaseType=false;
	private Boolean isColumn=false;
	private Boolean isId=false;
	//private  String function;
	//private Boolean ignoreDupUpdate;
	private Boolean updateNotNull;
	private String insertIfNull;
	private String insertIfNullFunc;
	private String updateIfNull;
	private String updateIfNullFunc;
	private Boolean ignoreDupUpdate;
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getColumnName() {
		return columnName;
	}

	public Boolean getIgnoreDupUpdate() {
		return ignoreDupUpdate;
	}

	public void setIgnoreDupUpdate(Boolean ignoreDupUpdate) {
		this.ignoreDupUpdate = ignoreDupUpdate;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public Boolean getIsBaseType() {
		return isBaseType;
	}
	public void setIsBaseType(Boolean isBaseType) {
		this.isBaseType = isBaseType;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public Boolean getIsId() {
		return isId;
	}
	public void setIsId(Boolean isId) {
		this.isId = isId;
	}
	public Boolean getIsColumn() {
		return isColumn;
	}
	public void setIsColumn(Boolean isColumn) {
		this.isColumn = isColumn;
	}

	public Boolean getUpdateNotNull() {
		return updateNotNull;
	}

	public void setUpdateNotNull(Boolean updateNotNull) {
		this.updateNotNull = updateNotNull;
	}

	public String getInsertIfNull() {
		return insertIfNull;
	}

	public void setInsertIfNull(String insertIfNull) {
		this.insertIfNull = insertIfNull;
	}

	public String getInsertIfNullFunc() {
		return insertIfNullFunc;
	}

	public void setInsertIfNullFunc(String insertIfNullFunc) {
		this.insertIfNullFunc = insertIfNullFunc;
	}

	public String getUpdateIfNull() {
		return updateIfNull;
	}

	public void setUpdateIfNull(String updateIfNull) {
		this.updateIfNull = updateIfNull;
	}

	public String getUpdateIfNullFunc() {
		return updateIfNullFunc;
	}

	public void setUpdateIfNullFunc(String updateIfNullFunc) {
		this.updateIfNullFunc = updateIfNullFunc;
	}
}
