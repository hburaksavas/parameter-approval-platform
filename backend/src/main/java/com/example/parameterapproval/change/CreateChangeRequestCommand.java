package com.example.parameterapproval.change;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateChangeRequestCommand(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1000) String description,
        @NotEmpty List<@Valid ChangeItemCommand> items
) { }

