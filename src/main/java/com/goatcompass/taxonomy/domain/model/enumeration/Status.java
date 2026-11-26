package com.goatcompass.taxonomy.domain.model.enumeration;

/** Vòng đời của một mục taxonomy. Mã không bao giờ bị xoá: lỗi thời thì DEPRECATED + replacedBy. */
public enum Status { DRAFT, ACTIVE, DEPRECATED }
