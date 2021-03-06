package com.dnd.moneyroutine.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DiaryDetail extends ExpenditureCompact implements Serializable {
    private int expenditureId;
    private int categoryId;
    private String detail;
    private int expense;
    private boolean custom;
}
