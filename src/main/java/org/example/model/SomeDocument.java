package org.example.model;

import lombok.*;

@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class SomeDocument {
    private String id;
    private String value;
    private SomeEnum status;
}
