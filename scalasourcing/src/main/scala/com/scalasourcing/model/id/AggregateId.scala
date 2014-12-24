package com.scalasourcing.model.id

trait AggregateId extends Product
{
    def hash: String
}