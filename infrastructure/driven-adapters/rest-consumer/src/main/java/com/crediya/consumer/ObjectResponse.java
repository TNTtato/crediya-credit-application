package com.crediya.consumer;

import com.crediya.model.user.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class ObjectResponse {

    private User user;

}