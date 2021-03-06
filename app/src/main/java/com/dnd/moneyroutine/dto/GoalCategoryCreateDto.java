package com.dnd.moneyroutine.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoalCategoryCreateDto implements Serializable  {
    private int budget;
    private long categoryId;
    private boolean isCustom;
}
