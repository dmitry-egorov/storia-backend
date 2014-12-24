package com.scalasourcing.model.id

trait StringAggregateId extends AggregateId
{
    def value: String

    def hash = value
    override def toString = value
}
