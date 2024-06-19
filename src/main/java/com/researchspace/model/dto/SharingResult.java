package com.researchspace.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SharingResult {
    private List<Long> sharedIds;
    private List<String> publicLinks;
}
