package com.bk.sample.vo;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVo implements Serializable {
  private static final long serialVersionUID = 6155156579929521199L;
  private String productId;
  private int price;
}
