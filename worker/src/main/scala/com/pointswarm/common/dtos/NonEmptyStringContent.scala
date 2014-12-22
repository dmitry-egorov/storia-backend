package com.pointswarm.common.dtos

trait NonEmptyStringContent {
    require(value != null && !value.trim.isEmpty)
    def value: String
}
