package com.pointswarm.common.dtos

case class AccountId(value: String) {
    assert(value != null && value.trim.nonEmpty)
    override def toString = value
}

object AccountId {
    implicit def fromString(s: String): AccountId = AccountId(s)
    implicit def toString(id: AccountId): String = id.value
}