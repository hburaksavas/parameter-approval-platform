package com.example.parameterapproval.change;

import jakarta.validation.constraints.Size;

public record DecisionCommand(@Size(max = 1000) String note) { }

