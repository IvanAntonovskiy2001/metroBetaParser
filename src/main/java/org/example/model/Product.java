package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private String firstCategory;
    private String productName;
    private Double cost;
    private String remainder;
    private  String url;


}
