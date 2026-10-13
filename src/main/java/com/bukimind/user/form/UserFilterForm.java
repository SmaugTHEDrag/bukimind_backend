package com.bukimind.user.form;

import lombok.Data;

import java.util.UUID;

@Data
public class UserFilterForm {

    // Search by specific fields
    private String usernameSearch;
    private String emailSearch;
    private String statusSearch;

    // ID range
    private UUID minId;
    private UUID maxId;
}
