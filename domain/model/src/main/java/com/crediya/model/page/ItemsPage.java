package com.crediya.model.page;

import com.crediya.model.creditapplication.CreditApplication;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemsPage<T> {
    private Integer page;
    private Integer size;
    private Long totalItems;
    private Integer totalPages;
    private List<T> items = new ArrayList<>();
}
